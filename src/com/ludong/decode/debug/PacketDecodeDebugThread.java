package com.ludong.decode.debug;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.log4j.Logger;

import com.cocopass.helper.CByte;
import com.cocopass.helper.CMQ.MQFactory;
import com.cocopass.helper.CMQ.Message;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ludong.decode.Config;
import com.ludong.decode.Global;
import com.ludong.iot.IPacket;
import com.ludong.iot.PacketFactory;
import com.rabbitmq.client.MessageProperties;

public class PacketDecodeDebugThread extends Thread {
	static Logger LOG = Logger.getLogger(PacketDecodeDebugThread.class.getName());

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

				if (data.length > 7 && data[0] == 0x29 && data[1] == 0x29 &&  (data[data.length - 1] == 0x0D||data[data.length - 9] == 0x0D)) {
					pb = new com.ludong.iot.PacketBase(data);
					version = pb.GetVersion();
					packetInstance = PacketFactory.GetPacketInstance(version, data);
					tid = packetInstance.GetTerminalID();

					if (Config.DebugTerminalID==0||tid == Config.DebugTerminalID) {

						LOG.debug(CByte.BytesToHexString(data));

						listModel = packetInstance.Decode();
						if (listModel != null) {
							for (Object obj : listModel) {
								
//								jobj = Global.gson.toJson(obj);
//								bytes = jobj.getBytes();
//								className = obj.getClass().getName();
//								tid = packetInstance.GetTerminalID();
																
								com.cocopass.helper.CObject.ReflectPrintln(obj);
								
								LOG.debug("---------------------------------------------------------------------");

								//exchangeName = Global.iProperties.GetValue(className);
								//publishKey = Global.iProperties.GetValue(exchangeName + "-Key");
								//
								
							}
						}
					}

				}
				cmq.AckMessage(message, msg.getMessageID(), false);
				// LOG.debug(msg.getMessageID() + " un fit format message get and ack delete ");

			} catch (Exception er) {
				er.printStackTrace();
				com.cocopass.helper.CLoger.Error(er.getMessage());
				if (msg != null) {
					cmq.AckMessage(message, tag, false);
				}
			}
		}

	}
}
