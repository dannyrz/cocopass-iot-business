package com.ludong.decode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;
import org.dave.common.database.DatabaseTransaction;

import com.cocopass.helper.CMQ.MQFactory;
import com.cocopass.helper.CMQ.Message;
import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

public class GPSInfoSaveToDBThreadMutil extends Thread {
	// static Logger LOG =
	// Logger.getLogger(GPSInfoSaveToDBThreadMutil.class.getName());

	public void run() {

		com.cocopass.helper.CMQ.CMessageQueue cmq = MQFactory.createMQ(Config.MQName);
		cmq.SetConnection(Config.MQAliasName, Config.MQHost, Config.MQUserName, Config.MQPassword, Config.MQPort);
		cmq.SetService();
		cmq.InitService();
		cmq.SetConsumer(Config.GPSInfoStationDBQueue);

		com.ludong.bll.GPSInfo gpsInfoBLL = new com.ludong.bll.GPSInfo();

		byte[] data;
		List<com.ludong.model.GPSInfo> list = new ArrayList<com.ludong.model.GPSInfo>();
		int k = 0;
		Object message = null;
		String tag = null;
		while (true) {

			try {
				message = cmq.GetNextMessage(Config.ECGPSInfo, Config.GPSInfoStationDBQueue, null);

				if (message == null) {
					continue;
				}
				Message msg = cmq.TranseMessage(message);
				tag = msg.getMessageID();
				data = msg.getContent();
				String strData = new String(data);

				com.ludong.model.GPSInfo gpsInfo = Global.gson.fromJson(strData, com.ludong.model.GPSInfo.class);

				list.add(gpsInfo);

				if (k == Config.BeginSaveGPSInfoNum) {
					gpsInfoBLL.AddMutilList(list);
					list.clear();
					// LOG.debug("-- save gpsinfo one time .");
					k = 0;
				}

				// LOG.debug(k + ":" + strData);
			} catch (Exception er) {
				er.printStackTrace();
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
