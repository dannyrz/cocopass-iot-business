package com.ludong.decode;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import com.cocopass.helper.CMQ.MQFactory;
import com.cocopass.helper.CMQ.Message;
import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.MessageProperties;
import com.rabbitmq.client.QueueingConsumer;

/*
 * 用于处理监控一个指令的状态
 */

public class ActionInstructionStatusThread extends Thread {

	// static Logger LOG =
	// Logger.getLogger(ActionInstructionStatusThread.class.getName());
	com.cocopass.helper.CMQ.CMessageQueue cmq = MQFactory.createMQ(Config.MQName);

	public void run() {

		cmq.SetConnection(Config.MQAliasName, Config.MQHost, Config.MQUserName, Config.MQPassword, Config.MQPort);
		cmq.SetService();
		cmq.InitService();
		cmq.SetConsumer(Config.ActionLogOfRequestQueue);


		com.ludong.model.ActionLog actionLog = null;
		byte[] data=null;
		String tag=null;
		String key =null;
		Object message=null;
		while (true) {

			try {
				message = cmq.GetNextMessage(Config.ECActionLog, Config.ActionLogOfRequestQueue, null);
				if (message == null)
					continue;

				Message msg = cmq.TranseMessage(message);
				tag = msg.getMessageID();
				data = msg.getContent();
				String strData = new String(data);
				actionLog = Global.gson.fromJson(strData, com.ludong.model.ActionLog.class);
				int status=actionLog.GetStatus();
//				if(status==1||status==5||status==6)
				if (status == 2) {
					//开启等待判断是否有应答线程
					new ActionInstructionDeal(actionLog).start();
				}
				else if(status==3||status==4||status==7||status==8){
					key = "InstructionStatus:" + actionLog.GetSerialNumber();
					com.cocopass.helper.CRedis.setex(key, 3600, String.valueOf(status));
				}
				cmq.AckMessage(message, tag, false);

			} catch (Exception er) {
				er.printStackTrace();
				com.cocopass.helper.CLoger.Error(er.getMessage());
				if(tag!=null){
					cmq.NAckMessage(message, tag, false);
				}
			}
			finally{
				actionLog=null;
				data=null;
				key=null;
				message=null;
				tag=null;
			}
		}
	}

	//判断是否有应答线程
	private class ActionInstructionDeal extends Thread {
		// static Logger LOG =
		// Logger.getLogger(ActionInstructionDeal.class.getName());
		com.ludong.model.ActionLog actionLog = null;
		//Object message = null;

		private ActionInstructionDeal(com.ludong.model.ActionLog actionLog) {
			this.actionLog = actionLog;
			//this.message = message;
		}

		public void run() {
			String key = "InstructionStatus:" + actionLog.GetSerialNumber();
			String strStatus = null;
			int timeLong = 0;
			while (timeLong < 21000) {
				strStatus = com.cocopass.helper.CRedis.get(key);
				if (strStatus == null) {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						com.cocopass.helper.CLoger.Error(e.getMessage());
					}
					timeLong += 500;
				} else {
					break;
				}
			}
			if (strStatus == null) {
				actionLog.SetStatus(4);
				actionLog.SetSamplingTime(new Date().getTime());
				String jsonActionLog = Global.gson.toJson(actionLog);
				cmq.PublishMessage(Config.ECActionLog, null, jsonActionLog.getBytes());
			}

		}
	}
}
