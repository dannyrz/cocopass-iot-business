package com.ludong.decode;

import java.util.ArrayList;
import java.util.List;

import com.cocopass.helper.CMQ.MQFactory;
import com.cocopass.helper.CMQ.Message;

public class AuthInfoToDBThread  extends Thread{
	
	
	public void run() {

		com.cocopass.helper.CMQ.CMessageQueue cmq = MQFactory.createMQ(Config.MQName);
		cmq.SetConnection(Config.MQAliasName, Config.MQHost, Config.MQUserName, Config.MQPassword, Config.MQPort);
		cmq.SetService();
		cmq.InitService();
		cmq.SetConsumer(Config.AuthInfoToDBQueue);

		com.cocopass.bll.AuthInfo bll = new com.cocopass.bll.AuthInfo();

		byte[] data;
		

		Object message = null;
		String tag = null;
		while (true) {

			try {
				message = cmq.GetNextMessage(Config.ECAuthInfo, Config.AuthInfoToDBQueue, null);

				if (message == null) {
					continue;
				}
				Message msg = cmq.TranseMessage(message);
				tag = msg.getMessageID();
				data = msg.getContent();
				String strData = new String(data);

				com.cocopass.iot.model.AuthInfo model = Global.gson.fromJson(strData, com.cocopass.iot.model.AuthInfo.class);
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
