package com.ludong.decode.old;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import com.cocopass.helper.CMQ.MQFactory;
import com.cocopass.helper.CMQ.Message;
import com.google.gson.Gson;
import com.ludong.decode.Config;
import com.ludong.decode.Global;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import com.rabbitmq.client.QueueingConsumer;

public class ActionLogToCacheDBThread extends Thread {
	// static Logger LOG =
	// Logger.getLogger(ActionLogToCacheDBThread.class.getName());

	public void run() {
		com.cocopass.helper.CMQ.CMessageQueue cmq = MQFactory.createMQ(Config.MQName);
		cmq.SetConnection(Config.MQAliasName, Config.MQHost, Config.MQUserName, Config.MQPassword, Config.MQPort);
		cmq.SetService();
		cmq.InitService();
		cmq.SetConsumer(Config.ActionLogToCacheDBQueue);

		// String jsonActionLog;
		String key = null;
		com.ludong.model.ActionLog actionLog = null;
		byte[] data;
		String tag = null;
		Object message = null;
		while (true) {
			try {
				message = cmq.GetNextMessage(Config.ECActionLog, Config.ActionLogToCacheDBQueue, null);

				if (message == null) {
					continue;
				}
				Message msg = cmq.TranseMessage(message);
				tag = msg.getMessageID();
				data = msg.getContent();
				String strData = new String(data);

				actionLog = Global.gson.fromJson(strData, com.ludong.model.ActionLog.class);
				int status = actionLog.GetStatus();
				if (status == 3 || status == 4 || status == 7 || status == 8) {
					key = "InstructionStatus:" + actionLog.GetSerialNumber();
					com.cocopass.helper.CRedis.setex(key, 3600, String.valueOf(status));
				}
				cmq.AckMessage(message, tag, false);
				// LOG.debug("actionLog status to redis ack success!");
			} catch (Exception er) {
				er.printStackTrace();
				com.cocopass.helper.CLoger.Error(er.getMessage());
				if(tag!=null){
					cmq.NAckMessage(message, tag, false);
				}
			}
			finally{
				data=null;
				key=null;
				message=null;
				tag=null;
			}

		}
	}

}
