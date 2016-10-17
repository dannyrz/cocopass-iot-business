package com.ludong.decode;

import java.util.ArrayList;
import java.util.List;

import com.cocopass.helper.CMQ.MQFactory;
import com.cocopass.helper.CMQ.Message;

public class LoginInfoToDBThread  extends Thread{
	
	
	public void run() {

		com.cocopass.helper.CMQ.CMessageQueue cmq = MQFactory.createMQ(Config.MQName);
		cmq.SetConnection(Config.MQAliasName, Config.MQHost, Config.MQUserName, Config.MQPassword, Config.MQPort);
		cmq.SetService();
		cmq.InitService();
		cmq.SetConsumer(Config.LoginInfoToDBQueue);

		com.cocopass.bll.LoginInfo bll = new com.cocopass.bll.LoginInfo();

		byte[] data;
		

		Object message = null;
		String tag = null;
		while (true) {

			try {
				message = cmq.GetNextMessage(Config.ECLoginInfo, Config.LoginInfoToDBQueue, null);

				if (message == null) {
					continue;
				}
				Message msg = cmq.TranseMessage(message);
				tag = msg.getMessageID();
				data = msg.getContent();
				String strData = new String(data);

				com.cocopass.iot.model.LoginInfo model = Global.gson.fromJson(strData, com.cocopass.iot.model.LoginInfo.class);
				bll.Add(model);

				
			} catch (Exception er) {
				er.printStackTrace();
				com.cocopass.helper.CLoger.Error(er.getMessage());
			} finally {
				if (message != null) {
					cmq.AckMessage(message, tag, false);
				}
			}
		}
	}

}
