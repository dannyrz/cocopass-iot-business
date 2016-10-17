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
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

public class BatteryInfoPushThread extends Thread {


	public void run() {
		com.cocopass.helper.CMQ.CMessageQueue cmq = MQFactory.createMQ(Config.MQName);
		cmq.SetConnection(Config.MQAliasName, Config.MQHost, Config.MQUserName, Config.MQPassword, Config.MQPort);
		cmq.SetService();
		cmq.InitService();
		cmq.SetConsumer(Config.BatteryInfoToBookerQueue);

		com.cocopass.helper.CMQ.CMessageQueue bossCMQ = MQFactory.createMQ(Config.BossMQName);
		bossCMQ.SetConnection(Config.BossMQAliasName, Config.BossMQHost, Config.BossMQUserName, Config.BossMQPassword,
				Config.BossMQPort);
		bossCMQ.SetService();

		long deliveryTag = 0;
		com.ludong.model.BatteryInfo batteryInfo = null;
		byte[] data;
		com.cocopass.iot.model.PushData po = null;
		String pushResponse = null;
		String pData = null;
		while (true) {

			try {

				Object message = cmq.GetNextMessage(Config.ECBatteryInfo, Config.BatteryInfoToBookerQueue, null);
				if (message == null)
					continue;
				Message msg = cmq.TranseMessage(message);
				String tag = msg.getMessageID();
				data = msg.getContent();

				String strData = new String(data);
				

				batteryInfo = Global.gson.fromJson(strData, com.ludong.model.BatteryInfo.class);

				// 最新数据入缓存
				com.cocopass.helper.CRedis.setex(String.valueOf(batteryInfo.getSessionID()), 24 * 3600, strData);

				// if (batteryInfo.getChargingPortStatus() > 1) {
				long timeStamp = new Date().getTime();
				String id = batteryInfo.GetTerminalID() + ":" + timeStamp;

				po = new com.cocopass.iot.model.PushData();
				po.setID(id);
				po.SetDataTypeID(4);
				po.SetBody(new JsonParser().parse(strData).getAsJsonObject());
				po.SetAppKey(Config.AppKey); // 在
											// actionLog里保存了key，此处无需通过缓存获取key?这样的冗余设计指的商议。
				po.SetTimeStamp(new Date().getTime());
				po.SetVersion(2.0f);
				String sign = Config.AppSecret + po.GetBody() + po.GetDataTypeID() + po.GetTimeStamp() + po.GetVersion()
						+ Config.AppSecret;
				sign = DigestUtils.md5Hex(sign);
				po.setSign(sign);

				pData = Global.gson.toJson(po);

				String url = Config.DistributionUrl;
				// url=url+"?Data="+pData;

				// LOG.debug("push data:" + pData);

				pushResponse = CHttp.GetResponseBody(url, pData, null);

				com.cocopass.helper.CLoger.Info("batteryInfo:[" + id + "]:" + pushResponse);

				// LOG.info("push success:"+ result+":"+ pData);
				// LOG.debug("push url =" + url + " response txt:" + result);

				// if (result.toLowerCase().equals("success")) {
				// cmq.AckMessage(message, tag, false);
				// LOG.debug("batteryinfo push ack success");
				// } else {
				// LOG.debug("batteryinfo reque ack false");
				// }
				// } else {
				cmq.AckMessage(message, tag, false);

				// }
				// System.out.println(result);

				// LOG.debug("\n-----------------------------------------------------------------");
			}

			catch (Exception e) {
				// TODO Auto-generated catch block
				com.cocopass.helper.CLoger.Error(e.getMessage());
				e.printStackTrace();
			}

			finally {
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
