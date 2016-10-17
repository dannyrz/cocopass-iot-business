package com.ludong.datacollect;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeoutException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;

import com.cocopass.helper.CHttp;
import com.cocopass.helper.CMQ.MQFactory;
import com.cocopass.helper.CMQ.Message;
import com.google.gson.Gson;
import com.ludong.decode.Config;
import com.ludong.decode.Global;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

public class LoginInfoToBossThread extends Thread {
	// static Logger LOG =
	// Logger.getLogger(BatteryInfoPushThread.class.getName());

	public void run() {
		com.cocopass.helper.CMQ.CMessageQueue cmq = MQFactory.createMQ(Config.MQName);
		cmq.SetConnection(Config.MQAliasName, Config.MQHost, Config.MQUserName, Config.MQPassword, Config.MQPort);
		cmq.SetService();
		cmq.InitService();
		cmq.SetConsumer(Config.LoginInfoToBossQueue);

		com.cocopass.helper.CMQ.CMessageQueue bossCMQ = MQFactory.createMQ(Config.BossMQName);
		bossCMQ.SetConnection(Config.BossMQAliasName, Config.BossMQHost, Config.BossMQUserName, Config.BossMQPassword,
				Config.BossMQPort);
		bossCMQ.SetService();

		byte[] data;
		while (true) {

			try {

				Object message = cmq.GetNextMessage(Config.ECLoginInfo, Config.LoginInfoToBossQueue, null);
				if (message == null)
					continue;
				Message msg = cmq.TranseMessage(message);
				String tag = msg.getMessageID();
				data = msg.getContent();
				bossCMQ.PublishMessage(Config.ECLoginInfo, "", data);		
				cmq.AckMessage(message, tag, false);
			}

			catch (Exception e) {
				// TODO Auto-generated catch block
				com.cocopass.helper.CLoger.Error(e.getMessage());
				e.printStackTrace();
			}

		 
		}
	}
}
