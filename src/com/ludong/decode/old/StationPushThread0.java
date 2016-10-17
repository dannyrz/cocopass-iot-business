package com.ludong.decode.old;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeoutException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;

import com.cocopass.helper.CHttp;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.ludong.decode.Config;
import com.ludong.decode.Global;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

public class StationPushThread0 extends Thread {
	Logger LOG = Logger.getLogger(StationPushThread0.class.getName());
	Gson gson = new Gson();

	public void run() {
		Connection connection = null;
		try {
			connection = Global.factory.newConnection();
		} catch (IOException | TimeoutException e1) {
			// TODO Auto-generated catch block
			LOG.error(e1.getMessage());
			e1.printStackTrace();

		}
		Channel rabbitMQChannel = null;
		try {
			rabbitMQChannel = connection.createChannel();
			rabbitMQChannel.basicQos(0, 100, false);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			LOG.error(e1.getMessage());
			e1.printStackTrace();
		}

		QueueingConsumer consumer = new QueueingConsumer(rabbitMQChannel);

		try {
			rabbitMQChannel.basicConsume(Config.StationInfoToBookerQueue, false, consumer);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOG.error(e.getMessage());
		}
		QueueingConsumer.Delivery delivery = null;

		String jsonActionLog;
		long deliveryTag = 0;
		com.ludong.model.Station station = null;
		byte[] data;
		while (true) {

			try {

				delivery = consumer.nextDelivery();
				data = delivery.getBody();

				deliveryTag = delivery.getEnvelope().getDeliveryTag();

				String strData = new String(data);

				//station = gson.fromJson(strData, com.ludong.model.Station.class);

				com.cocopass.iot.model.PushData po = new com.cocopass.iot.model.PushData();
				po.SetDataTypeID(3);
				po.SetBody(new JsonParser().parse(strData).getAsJsonObject());
				po.SetAppKey(Config.AppKey); // 在
											// actionLog里保存了key，此处无需通过缓存获取key?这样的冗余设计指的商议。
				po.SetTimeStamp(new Date().getTime());
				po.SetVersion(2.0f);
				String sign = Config.AppSecret + po.GetBody() + po.GetDataTypeID() + po.GetTimeStamp() + po.GetVersion()
						+ Config.AppSecret;
				sign = DigestUtils.md5Hex(sign);
				po.setSign(sign);

				String pData = gson.toJson(po);

				// pData= java.net.URLEncoder.encode(pData,"utf-8");
				// String url=Cache.GetPushURL(model.GetTerminalID(),
				// Config.RunMode);
				String url = Config.DistributionUrl;
				// url=url+"?Data="+pData;

				LOG.debug("push data:" + pData);

				String result = CHttp.GetResponseBody(url, pData, null);

				LOG.debug("push url =" + url + "  response txt:" + result);

				if (result.toLowerCase().equals("success")) {
					rabbitMQChannel.basicAck(deliveryTag, false);
					System.out.println("batteryinfo push ack success");
				} else {
					rabbitMQChannel.basicNack(deliveryTag, false, true);
					System.out.println("batteryinfo reque ack false");
				}

				// System.out.println(result);

				System.out.println("\n-----------------------------------------------------------------");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				try {
					rabbitMQChannel.basicNack(deliveryTag, false, true);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				e.printStackTrace();
			} catch (ShutdownSignalException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ConsumerCancelledException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
