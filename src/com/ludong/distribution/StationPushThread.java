package com.ludong.distribution;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeoutException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;

import com.cocopass.helper.CHttp;
import com.cocopass.helper.CMQ.MQFactory;
import com.cocopass.helper.CMQ.Message;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.ludong.decode.Config;
import com.ludong.decode.Global;
import com.ludong.decode.old.StationPushThread0;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

public class StationPushThread extends Thread {
	static Logger LOG = Logger.getLogger(StationPushThread0.class.getName());

	public void run() {
		com.cocopass.helper.CMQ.CMessageQueue cmq = MQFactory.createMQ(Config.MQName);
		cmq.SetConnection(Config.MQAliasName, Config.MQHost, Config.MQUserName, Config.MQPassword, Config.MQPort);
		cmq.SetService();
		cmq.InitService();
		cmq.SetConsumer(Config.StationInfoToBookerQueue);

		com.cocopass.helper.CMQ.CMessageQueue bossCMQ = MQFactory.createMQ(Config.BossMQName);
		bossCMQ.SetConnection(Config.BossMQAliasName, Config.BossMQHost, Config.BossMQUserName, Config.BossMQPassword,
				Config.BossMQPort);
		bossCMQ.SetService();

		long deliveryTag = 0;
		com.ludong.model.Station station = null;
		byte[] data;
		com.cocopass.iot.model.PushData po = null;
		String pushResponse = null;
		String pData = null;
		while (true) {

			try {

				Object message = cmq.GetNextMessage(Config.ECStationInfo, Config.StationInfoToBookerQueue, null);
				if (message == null)
					continue;
				Message msg = cmq.TranseMessage(message);
				String tag = msg.getMessageID();
				data = msg.getContent();

				String strData = new String(data);

				station = Global.gson.fromJson(strData, com.ludong.model.Station.class);

				long timeStamp = new Date().getTime();
				String id = station.getTerminalID() + ":" + timeStamp;

				po = new com.cocopass.iot.model.PushData();
				po.setID(id);
				po.SetDataTypeID(3);
				po.SetBody(new JsonParser().parse(strData).getAsJsonObject());
				po.SetAppKey(Config.AppKey); // 在
											// actionLog里保存了key，此处无需通过缓存获取key?这样的冗余设计指的商议。
				po.SetTimeStamp(timeStamp);
				po.SetVersion(2.0f);
				String sign = Config.AppSecret + po.GetBody() + po.GetDataTypeID() + po.GetTimeStamp() + po.GetVersion()
						+ Config.AppSecret;
				sign = DigestUtils.md5Hex(sign);
				po.setSign(sign);

				pData = Global.gson.toJson(po);

				// pData= java.net.URLEncoder.encode(pData,"utf-8");
				// String url=Cache.GetPushURL(model.GetTerminalID(),
				// Config.RunMode);
				String url = Config.DistributionUrl;
				// url=url+"?Data="+pData;

				// LOG.debug("push data:"+ pData);

				pushResponse = CHttp.GetResponseBody(url, pData, null);

				com.cocopass.helper.CLoger.Info("stationInfo:[" + id + "]:" + pushResponse);
				// LOG.info("push success:" + result + ":" + pData);

				if (pushResponse.toLowerCase().equals("success")) {
					cmq.AckMessage(message, tag, false);
					// System.out.println("push ack success");
				} else {
					// System.out.println("reque ack false");
					cmq.NAckMessage(message, tag, false);
				}

				// System.out.println(result);

				// System.out.println("\n-----------------------------------------------------------------");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				LOG.error(e.getMessage());
				e.printStackTrace();
			} finally {
				if (po != null) {
					po.SetBody(null);
					po.setResponse(pushResponse);
					pData = Global.gson.toJson(po);
					byte[] bytes = pData.getBytes();
					bossCMQ.PublishMessage(Config.BossECPushLog, "", bytes);
				}
			}
		}
	}

}
