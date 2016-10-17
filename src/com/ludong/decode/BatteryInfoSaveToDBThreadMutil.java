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

public class BatteryInfoSaveToDBThreadMutil extends Thread {
	// static Logger LOG =
	// Logger.getLogger(GPSInfoSaveToDBThreadMutil.class.getName());

	public void run() {

		com.cocopass.helper.CMQ.CMessageQueue cmq = MQFactory.createMQ(Config.MQName);
		cmq.SetConnection(Config.MQAliasName, Config.MQHost, Config.MQUserName, Config.MQPassword, Config.MQPort);
		cmq.SetService();
		cmq.InitService();
		cmq.SetConsumer(Config.BatteryInfoToDBQueue);

		com.ludong.bll.BatteryInfo bll = new com.ludong.bll.BatteryInfo();

		byte[] data;
		List<com.ludong.model.BatteryInfo> list = new ArrayList<com.ludong.model.BatteryInfo>();
		int k = 0;
		Object message = null;
		String tag = null;
		while (true) {

			try {
				message = cmq.GetNextMessage(Config.ECBatteryInfo, Config.BatteryInfoToDBQueue, null);

				if (message == null) {
					continue;
				}
				Message msg = cmq.TranseMessage(message);
				tag = msg.getMessageID();
				data = msg.getContent();
				String strData = new String(data);

				com.ludong.model.BatteryInfo gpsInfo = Global.gson.fromJson(strData, com.ludong.model.BatteryInfo.class);

				list.add(gpsInfo);

				if (k == Config.BeginSaveBatteryInfoNum) {
					bll.AddMutilList(list);
					list.clear();
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
