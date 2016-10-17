package com.ludong.decode;

import com.cocopass.helper.CMQ.MQFactory;
import com.cocopass.helper.CMQ.Message;

public class ActionLogToDBThread  extends Thread{
	public void run() {
	com.cocopass.helper.CMQ.CMessageQueue cmq = MQFactory.createMQ(Config.MQName);
	cmq.SetConnection(Config.MQAliasName, Config.MQHost, Config.MQUserName, Config.MQPassword, Config.MQPort);
	cmq.SetService();
	cmq.InitService();
	cmq.SetConsumer(Config.ActionLogToDBQueue);

	com.ludong.bll.ActionLog bll = new com.ludong.bll.ActionLog();

	byte[] data;
	

	Object message = null;
	String tag = null;
	while (true) {

		try {
			message = cmq.GetNextMessage(Config.ECActionLog, Config.ActionLogToDBQueue, null);

			if (message == null) {
				continue;
			}
			Message msg = cmq.TranseMessage(message);
			tag = msg.getMessageID();
			data = msg.getContent();
			String strData = new String(data);

			com.ludong.model.ActionLog model = Global.gson.fromJson(strData, com.ludong.model.ActionLog.class);
			bll.Add(model);
			cmq.AckMessage(message, tag, false);
			
		} catch (Exception er) {
			er.printStackTrace();
			com.cocopass.helper.CLoger.Error(er.getMessage());
			cmq.NAckMessage(message, tag, false);
		} finally {
//			if (message != null) {
//				cmq.AckMessage(message, tag, false);
//
//			}
		}
	}
}
}
