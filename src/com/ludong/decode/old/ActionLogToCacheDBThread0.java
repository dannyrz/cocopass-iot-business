package com.ludong.decode.old;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

import com.ludong.decode.Config;
import com.ludong.decode.Global;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import com.rabbitmq.client.QueueingConsumer;

public class ActionLogToCacheDBThread0 extends Thread {
	// static Logger LOG =
	// Logger.getLogger(ActionLogToCacheDBThread0.class.getName());
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
			rabbitMQChannel.basicConsume(Config.ActionLogToCacheDBQueue, false, consumer);
			// LOG.info("绑定消费者到队列.");
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
				int status = actionLog.GetStatus();
				if (status == 3 || status == 4 || status == 7 || status == 8) {
					key = "InstructionStatus:" + actionLog.GetSerialNumber();
					com.cocopass.helper.CRedis.set(key, String.valueOf(status));
				}
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

}
