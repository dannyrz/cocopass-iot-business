package com.ludong.distribution;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;

import com.cocopass.helper.CHttp;
import com.cocopass.helper.CMQ.MQFactory;
import com.cocopass.helper.CMQ.Message;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ludong.decode.Config;
import com.ludong.decode.Global;
import com.ludong.decode.old.ActionLogToCacheDBThread;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

public class ActionLogPushThread extends Thread {
	// static Logger LOG =
	// Logger.getLogger(ActionLogPushThread.class.getName());

	public void run() {
		com.cocopass.helper.CMQ.CMessageQueue cmq = MQFactory.createMQ(Config.MQName);
		cmq.SetConnection(Config.MQAliasName, Config.MQHost, Config.MQUserName, Config.MQPassword, Config.MQPort);
		cmq.SetService();
		cmq.InitService();
		cmq.SetConsumer(Config.ActionLogToBookerQueue);

		com.cocopass.helper.CMQ.CMessageQueue bossCMQ = MQFactory.createMQ(Config.BossMQName);
		bossCMQ.SetConnection(Config.BossMQAliasName, Config.BossMQHost, Config.BossMQUserName, Config.BossMQPassword,
				Config.BossMQPort);
		bossCMQ.SetService();

		com.ludong.model.ActionLog actionLog = null;
		byte[] data;
		com.cocopass.iot.model.PushData po = null;
		String pushResponse = null;
		String pData = null;
		Object message=null;
		String tag=null;
		while (true) {

			try {

			    message = cmq.GetNextMessage(Config.ECActionLog, Config.ActionLogToBookerQueue, null);
				if (message == null)
					continue;

				Message msg = cmq.TranseMessage(message);
				tag = msg.getMessageID();
				data = msg.getContent();

				String strData = new String(data);

				// LOG.debug(strData);

				actionLog = Global.gson.fromJson(strData, com.ludong.model.ActionLog.class);
				int status = actionLog.GetStatus();

				if (status == 3 || status == 4 || status == 7 || status == 8) {
					long timeStamp = new Date().getTime();
					String id = actionLog.GetTerminalID() + ":" + actionLog.GetSerialNumber()+":"+timeStamp;

					po = new com.cocopass.iot.model.PushData();
					po.setID(id);
					po.SetDataTypeID(1);
					po.SetBody(new JsonParser().parse(strData).getAsJsonObject());
					po.SetAppKey(Config.AppKey); // 在
												// actionLog里保存了key，此处无需通过缓存获取key?这样的冗余设计指的商议。
					po.SetTimeStamp(timeStamp);
					po.SetVersion(2.0f);

					String sign = Config.AppSecret + po.GetBody() + po.GetDataTypeID() + po.GetTimeStamp()
							+ po.GetVersion() + Config.AppSecret;
					sign = DigestUtils.md5Hex(sign);
					po.setSign(sign);

					pData = Global.gson.toJson(po);

					// pData= java.net.URLEncoder.encode(pData,"utf-8");
					// String url=Cache.GetPushURL(model.GetTerminalID(),
					// Config.RunMode);
					String url = Config.DistributionUrl;
					// url=url+"?Data="+pData;

					// LOG.debug("actionLog push data:" + pData);

					pushResponse = CHttp.GetResponseBody(url, pData, null);

					com.cocopass.helper.CLoger.Info("actionLog:[" + id + "]:" + pushResponse);
					 
				} 
					cmq.AckMessage(message, tag, false);
				 

				// LOG.debug("\n-----------------------------------------------------------------");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				cmq.NAckMessage(message, tag, false);
				com.cocopass.helper.CLoger.Error(e.getMessage());
				e.printStackTrace();
				
			} finally {
				
				if (po != null) {
					po.SetBody(null);
					po.setResponse(pushResponse);
					pData = Global.gson.toJson(po);
					byte[] bytes = pData.getBytes();
					bossCMQ.PublishMessage(Config.BossECPushLog, "", bytes);
					//com.cocopass.helper.CLoger.Info("P T B P : "+po.getNO());
				}
				tag=null;
				message=null;
				po=null;
			}
		}
	}
}
