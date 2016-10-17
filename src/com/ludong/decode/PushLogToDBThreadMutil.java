package com.ludong.decode;

import java.util.ArrayList;
import java.util.List;

import com.cocopass.helper.CMQ.MQFactory;
import com.cocopass.helper.CMQ.Message;

public class PushLogToDBThreadMutil  extends Thread{

	public void run() {

		com.cocopass.helper.CMQ.CMessageQueue cmq = MQFactory.createMQ(Config.MQName);
		cmq.SetConnection(Config.MQAliasName, Config.MQHost, Config.MQUserName, Config.MQPassword, Config.MQPort);
		cmq.SetService();
		cmq.InitService();
		cmq.SetConsumer(Config.PushLogToDBQueue);

		com.cocopass.bll.PushData bll = new com.cocopass.bll.PushData();

		byte[] data;
		List<com.cocopass.iot.model.PushData> list = new ArrayList<com.cocopass.iot.model.PushData>();
		int k = 0;
		Object message = null;
		String tag = null;
		while (true) {

			try {
				message = cmq.GetNextMessage(Config.ECPushLog, Config.PushLogToDBQueue, null);

				if (message == null) {
					continue;
				}
				Message msg = cmq.TranseMessage(message);
				tag = msg.getMessageID();
				data = msg.getContent();
				String strData = new String(data);

				com.cocopass.iot.model.PushData model = Global.gson.fromJson(strData, com.cocopass.iot.model.PushData.class);
				
				list.add(model);

				if (k == Config.BeginSavePushDataNum) {
					bll.AddMutilList(list);
					list.clear();
					k = 0;
				}

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
