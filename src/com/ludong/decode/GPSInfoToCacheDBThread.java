package com.ludong.decode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import com.cocopass.helper.CMQ.MQFactory;
import com.cocopass.helper.CMQ.Message;
import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

public class GPSInfoToCacheDBThread extends Thread {

	// static Logger LOG =
	// Logger.getLogger(GPSInfoToCacheDBThread.class.getName());

	public void run() {

		com.cocopass.helper.CMQ.CMessageQueue cmq = MQFactory.createMQ(Config.MQName);
		cmq.SetConnection(Config.MQAliasName, Config.MQHost, Config.MQUserName, Config.MQPassword, Config.MQPort);
		cmq.SetService();
		cmq.InitService();
		cmq.SetConsumer(Config.GPSInfoToCacheDBQueue);

		byte[] data;

		int k = 0;
		Object message = null;
		String tag = null;
		String key = null;
		while (true) {

			try {
				message = cmq.GetNextMessage(Config.ECGPSInfo, Config.GPSInfoToCacheDBQueue, null);
				if (message == null) {
					continue;
				}
				Message msg = cmq.TranseMessage(message);
				tag = msg.getMessageID();
				data = msg.getContent();
				String strData = new String(data);

				com.ludong.model.GPSInfo gpsInfo = Global.gson.fromJson(strData, com.ludong.model.GPSInfo.class);

				key = "Terminal:" + gpsInfo.GetTerminalID();
				String SamplingTime = String.valueOf(gpsInfo.GetSamplingTime());
				//com.cocopass.helper.CRedis.SetMapValue(key, "LatestSamplingTime", SamplingTime);
				if (gpsInfo.GetPositionState() || gpsInfo.GetLatitude() > 0) {

					com.cocopass.helper.CRedis.SetMapValue(key, "Latitude", String.valueOf(gpsInfo.GetLatitude()));
					com.cocopass.helper.CRedis.SetMapValue(key, "Longitude", String.valueOf(gpsInfo.GetLongitude()));
					com.cocopass.helper.CRedis.SetMapValue(key, "BDLocation", gpsInfo.getBDLocation());
					com.cocopass.helper.CRedis.SetMapValue(key, "GDLocation", gpsInfo.getGDLocation());
					com.cocopass.helper.CRedis.SetMapValue(key, "SamplingTime", SamplingTime);

					// com.cocopass.helper.CRedis.SetMapValue(key,
					// "PositionSamplingTime",
					// com.cocopass.helper.CDate.ToString("yyyy-MM-dd HH:mm:ss",
					// com.cocopass.helper.CDate.MillionsToDate((gpsInfo.GetSamplingTime()+8*3600*1000))));
				}
			} catch (Exception er) {
				com.cocopass.helper.CLoger.Error(er.getMessage());
			} finally {
				if (message != null) {
					cmq.AckMessage(message, tag, false);
					k = k + 1;
				}
			}
		}
	}
}
