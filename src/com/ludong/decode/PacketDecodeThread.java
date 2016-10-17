package com.ludong.decode;

import java.util.List;

import org.apache.log4j.Logger;

import com.cocopass.helper.CByte;
import com.cocopass.helper.CMQ.MQFactory;
import com.cocopass.helper.CMQ.Message;
import com.google.gson.Gson;
import com.ludong.iot.IPacket;
import com.ludong.iot.PacketFactory;
import com.mysql.jdbc.log.Log;
import com.rabbitmq.client.MessageProperties;

public class PacketDecodeThread extends Thread {
	// static Logger LOG = Logger.getLogger(PacketDecodeThread.class.getName());

	public void run() {

		com.cocopass.helper.CMQ.CMessageQueue cmq = MQFactory.createMQ(Config.MQName);
		cmq.SetConnection(Config.MQAliasName, Config.MQHost, Config.MQUserName, Config.MQPassword, Config.MQPort);
		cmq.SetService();
		cmq.InitService();
		cmq.SetConsumer(Config.ReceivedTerminalBytesQueue);

		IPacket packetInstance = null;
		com.ludong.iot.PacketBase pb = null;
		List<Object> listModel = null;
		float version = 0;
		String jobj = null;
		long tid = 0;
		byte[] bytes = null;
		String className = null;
		String exchangeName = null;
		String publishKey = null;
		byte[] data = null;
		while (true) {
			Message msg = null;
			Object message = null;
			String tag = null;
			try {

				message = cmq.GetNextMessage(Config.ECReceivedTerminalBytes, Config.ReceivedTerminalBytesQueue, null);
				if (message == null)
					continue;

				msg = cmq.TranseMessage(message);
				tag = msg.getMessageID();
				data = msg.getContent();

				if (data.length > 7 && data[0] == 0x29 && data[1] == 0x29 && (data[data.length - 1] == 0x0D||data[data.length - 9] == 0x0D)) {
					pb = new com.ludong.iot.PacketBase(data);
					version = pb.GetVersion();
					packetInstance = PacketFactory.GetPacketInstance(version, data);
					tid = packetInstance.GetTerminalID();

					listModel = packetInstance.Decode();
					if (listModel != null) {
						for (Object obj : listModel) {
							
							if(obj==null)
								continue;
							
							jobj = Global.gson.toJson(obj);
							bytes = jobj.getBytes();
							className = obj.getClass().getName();
							tid = packetInstance.GetTerminalID();

							//com.cocopass.helper.CLoger.Info(className);

							exchangeName = Global.iProperties.GetValue(className);
							publishKey = Global.iProperties.GetValue(exchangeName + "-Key");
							
							cmq.PublishMessage(exchangeName, publishKey,bytes);

						}
					}

				}
				cmq.AckMessage(message,tag, false);
				// LOG.debug(msg.getMessageID() + " un fit format message get
				// and ack delete ");

			} catch (Exception er) {
				er.printStackTrace();
				com.cocopass.helper.CLoger.Error(er.getMessage()+data);
				if (msg != null) {
					cmq.AckMessage(message, msg.getMessageID(), false);
				}
			}
		}

	}
}
