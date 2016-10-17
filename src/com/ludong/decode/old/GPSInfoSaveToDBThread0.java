package com.ludong.decode.old;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;
import org.dave.common.database.DatabaseTransaction;
import com.ludong.decode.Config;
import com.ludong.decode.Global;
import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

public class GPSInfoSaveToDBThread0 extends Thread {
	// static Logger LOG =
	// Logger.getLogger(GPSInfoSaveToDBThread.class.getName());
	public void run() {

		Connection connection = null;
		try {
			connection = Global.factory.newConnection();
		} catch (IOException | TimeoutException e1) {
			// TODO Auto-generated catch block
			// LOG.error(e1.toString());
		}
		Channel rabbitMQChannel = null;
		try {
			rabbitMQChannel = connection.createChannel();
			rabbitMQChannel.basicQos(0, 100, false);
		} catch (IOException e1) {
			// TODO Auto-generated catch block 5397
			com.cocopass.helper.CLoger.Error(e1.getMessage());
		}
		QueueingConsumer consumer = null;
		try {
			// rabbitMQChannel.queueDeclare(Config.GPSInfoStationDBQueue, true,
			// false, false, null);
			consumer = new QueueingConsumer(rabbitMQChannel);
			rabbitMQChannel.basicConsume(Config.GPSInfoStationDBQueue, false, consumer);
		} catch (IOException er) {
			// TODO Auto-generated catch block
			er.printStackTrace();
			// LOG.error(er.getMessage());
		}

		com.ludong.bll.GPSInfo gpsInfoBLL = new com.ludong.bll.GPSInfo();

		QueueingConsumer.Delivery delivery = null;
		String jGpsInfo = null;
		long result = 0;
		long deliveryTag;
		while (true) {
			try {
				delivery = consumer.nextDelivery();
				jGpsInfo = new String(delivery.getBody());
				com.ludong.model.GPSInfo gpsInfo = Global.gson.fromJson(jGpsInfo, com.ludong.model.GPSInfo.class);
				result = gpsInfoBLL.Add(gpsInfo);

			} catch (Exception er) {
				// LOG.error(jGpsInfo+"\n"+er.getMessage());
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
