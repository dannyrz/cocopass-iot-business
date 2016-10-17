package com.ludong.decode.old;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;
import com.ludong.decode.Config;
import com.ludong.decode.Global;
import com.cocopass.helper.CByte;
import com.google.gson.Gson;
import com.ludong.decode.Global;
import com.ludong.iot.IPacket;
import com.ludong.iot.PacketFactory;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
//import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import com.rabbitmq.client.QueueingConsumer;

import com.microsoft.windowsazure.services.servicebus.*;
import com.microsoft.windowsazure.services.servicebus.models.*;
import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.core.*;
import javax.xml.datatype.*;

public class PacketDecodeThread0 extends Thread {
	Logger LOG = Logger.getLogger(PacketDecodeThread0.class.getName());
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

		try {
			rabbitMQChannel.queueDeclare(Config.ReceivedTerminalBytesQueue, true, false, false, null);
			LOG.info("声明通道连接原始报文队列完成 .");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOG.error(e.getMessage());
		}

		QueueingConsumer consumer = new QueueingConsumer(rabbitMQChannel);

		LOG.info("声明通道消费者完成 .");
		try {
			rabbitMQChannel.basicConsume(Config.ReceivedTerminalBytesQueue, false, consumer);
			LOG.info("绑定消费者到队列.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOG.error(e.getMessage());
		}
		QueueingConsumer.Delivery delivery = null;
		IPacket packetInstance = null;
		com.ludong.iot.PacketBase pb = null;
		List<Object> listModel = null;
		byte[] data = null;
		long c = 0;
		String message = null;
		float version = 0;
		long tid = 0;
		long deliveryTag = 0;
		String jobj = null;
		byte[] bytes = null;
		String className = null;
		String exchangeName = null;
		String publishKey = null;

		LOG.info("即将进入循环获取队列数据.");

		while (true) {
			try {
				delivery = consumer.nextDelivery();
				data = delivery.getBody();
				if (data[0] == 0x29 || data[1] == 0x29 || data[data.length - 1] == 0x0D) {
					message = CByte.BytesToHexString(data);
					LOG.debug(message);
					pb = new com.ludong.iot.PacketBase(data);
					version = pb.GetVersion();
					packetInstance = PacketFactory.GetPacketInstance(version, data);
					tid = packetInstance.GetTerminalID();
					listModel = packetInstance.Decode();
					if (listModel != null) {
						for (Object obj : listModel) {
							jobj = gson.toJson(obj);

							bytes = jobj.getBytes();
							className = obj.getClass().getName();

							LOG.debug(className + ":" + jobj);

							exchangeName = Global.iProperties.GetValue(className);
							publishKey = Global.iProperties.GetValue(exchangeName + "-Key");
							rabbitMQChannel.basicPublish(exchangeName, publishKey,
									MessageProperties.PERSISTENT_TEXT_PLAIN, bytes);

							/*
							 * 微软云测试
							 */

						}
					}
				}

			} catch (Exception er) {
				er.printStackTrace();
				LOG.error("Packet:" + message + "\n" + er.toString());
			} finally {
				try {
					deliveryTag = delivery.getEnvelope().getDeliveryTag();
					rabbitMQChannel.basicAck(deliveryTag, false);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					LOG.error(e.getMessage());
				}
				c += 1;
			}
		}
	}
}
