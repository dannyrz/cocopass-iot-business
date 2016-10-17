package com.ludong.decode.old;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.ludong.decode.Config;
import com.ludong.decode.Global;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.MessageProperties;
import com.rabbitmq.client.QueueingConsumer;

public class ActionInstructionStatusThread0 extends Thread {

	// static Logger LOG =
	// Logger.getLogger(ActionInstructionStatusThread0.class.getName());
	Gson gson = new Gson();

	public void run() {
		Connection connection = null;
		try {
			connection = Global.factory.newConnection();
		} catch (IOException | TimeoutException e1) {
			// TODO Auto-generated catch block
			// LOG.error(e1.getMessage());
			e1.printStackTrace();

		}
		Channel rabbitMQChannel = null;
		try {
			rabbitMQChannel = connection.createChannel();
			rabbitMQChannel.basicQos(0, 100, false);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			// LOG.error(e1.getMessage());
			e1.printStackTrace();
		}

		QueueingConsumer consumer = new QueueingConsumer(rabbitMQChannel);

		try {
			rabbitMQChannel.basicConsume(Config.ActionLogOfRequestQueue, false, consumer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// LOG.error(e.getMessage());
		}
		QueueingConsumer.Delivery delivery = null;

		String jsonActionLog;
		long deliveryTag;
		String key = null;
		com.ludong.model.ActionLog actionLog = null;
		while (true) {

			try {
				delivery = consumer.nextDelivery();
				jsonActionLog = new String(delivery.getBody());
				actionLog = gson.fromJson(jsonActionLog, com.ludong.model.ActionLog.class);
				new ActionInstructionDeal(actionLog).start();
			} catch (Exception er) {
				er.printStackTrace();
				// LOG.error(er.getMessage());
			} finally {
				deliveryTag = delivery.getEnvelope().getDeliveryTag();
				try {
					rabbitMQChannel.basicAck(deliveryTag, false);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					// LOG.error(e.getMessage());
				}
			}
		}
	}

	private class ActionInstructionDeal extends Thread {
		Logger LOG = Logger.getLogger(ActionInstructionDeal.class.getName());
		com.ludong.model.ActionLog actionLog = null;

		public ActionInstructionDeal(com.ludong.model.ActionLog actionLog) {
			this.actionLog = actionLog;
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
					}
					timeLong += 500;
				} else {
					break;
				}
			}
			if (strStatus == null) {
				actionLog.SetStatus(4);
				actionLog.SetSamplingTime(new Date().getTime());
				String jsonActionLog = gson.toJson(actionLog);
				Connection connection = null;
				Channel rabbitMQChannel = null;
				try {
					connection = Global.factory.newConnection();
					rabbitMQChannel = connection.createChannel();
					rabbitMQChannel.basicPublish(Config.ECActionLog, "", MessageProperties.PERSISTENT_TEXT_PLAIN,
							jsonActionLog.getBytes());
					rabbitMQChannel.abort();
					// rabbitMQChannel.close();
					connection.close();

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					LOG.error(e.getMessage());
				}
			}
		}

	}
}
