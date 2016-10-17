package com.ludong.decode.old;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;
import com.ludong.decode.Config;
import com.ludong.decode.Global;
import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

public class GPSInfoToCacheDBThread0 extends Thread {

	static Logger LOG = Logger.getLogger(GPSInfoToCacheDBThread0.class.getName());

	public void run() {

		Connection connection = null;
		try {
			connection = Global.factory.newConnection();
		} catch (IOException | TimeoutException e1) {
			// TODO Auto-generated catch block
			LOG.error(e1.getMessage());
		}
		Channel rabbitMQChannel = null;
		try {
			rabbitMQChannel = connection.createChannel();
			rabbitMQChannel.basicQos(0, 100, false);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			LOG.error(e1.getMessage());
		}
		QueueingConsumer consumer = null;
		try {
			// rabbitMQChannel.queueDeclare(Config.GPSInfoStationDBQueue, true,
			// false, false, null);
			consumer = new QueueingConsumer(rabbitMQChannel);
			rabbitMQChannel.basicConsume(Config.GPSInfoToCacheDBQueue, false, consumer);
		} catch (IOException er) {
			// TODO Auto-generated catch block
			er.printStackTrace();
			LOG.error(er.getMessage());
		}

		// com.ludong.bll.GPSInfo gpsInfoBLL=new com.ludong.bll.GPSInfo();

		QueueingConsumer.Delivery delivery = null;
		String jGpsInfo = null;
		// long result=0;
		long deliveryTag;

		while (true) {
			String key = null;
			com.ludong.model.GPSInfo gpsInfo = null;
			String SamplingTime = null;
			try {
				delivery = consumer.nextDelivery();
				jGpsInfo = new String(delivery.getBody());

				// LOG.debug(jGpsInfo);

				gpsInfo = Global.gson.fromJson(jGpsInfo, com.ludong.model.GPSInfo.class);
				key = "Terminal:" + gpsInfo.GetTerminalID();
				SamplingTime = String.valueOf(gpsInfo.GetSamplingTime());
				com.cocopass.helper.CRedis.SetMapValue(key, "LatestSamplingTime", SamplingTime);
				if (gpsInfo.GetPositionState()) {

					com.cocopass.helper.CRedis.SetMapValue(key, "Latitude", String.valueOf(gpsInfo.GetLatitude()));
					com.cocopass.helper.CRedis.SetMapValue(key, "Longitude", String.valueOf(gpsInfo.GetLongitude()));
					com.cocopass.helper.CRedis.SetMapValue(key, "BDLocation", gpsInfo.getBDLocation());
					com.cocopass.helper.CRedis.SetMapValue(key, "GDLocation", gpsInfo.getGDLocation());
					com.cocopass.helper.CRedis.SetMapValue(key, "SamplingTime", SamplingTime);

					// com.cocopass.helper.CRedis.SetMapValue(key,
					// "PositionSamplingTime",
					// com.cocopass.helper.CDate.ToString("yyyy-MM-dd HH:mm:ss",
					// com.cocopass.helper.CDate.MillionsToDate((gpsInfo.GetSamplingTime()+8*3600*1000))));
				}
			} catch (Exception er) {
				LOG.error(er.getMessage());
			} finally {
				deliveryTag = delivery.getEnvelope().getDeliveryTag();
				try {
					rabbitMQChannel.basicAck(deliveryTag, false);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					LOG.error(e.getMessage());
				}
			}
		}
	}
}
