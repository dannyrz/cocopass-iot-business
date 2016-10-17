package com.ludong.data.process;


import java.util.Date;

import org.apache.log4j.Logger;
import com.cocopass.helper.CMQ.MQFactory;
import com.cocopass.helper.CMQ.Message;
import com.ludong.decode.Config;
import com.ludong.iot.IPacket;
import com.ludong.iot.PacketFactory;


public class LatestOnlineTimeUpdateThread extends Thread {


	public void run() {

		com.cocopass.helper.CMQ.CMessageQueue cmq = MQFactory.createMQ(Config.MQName);
		cmq.SetConnection(Config.MQAliasName, Config.MQHost, Config.MQUserName, Config.MQPassword, Config.MQPort);
		cmq.SetService();
		cmq.InitService();
		cmq.SetConsumer(Config.ReceivedBytesOnlineAnalyzeQueue);
		
		byte[] data = null;
		
		while (true) {
			
			Message msg = null;
			Object message = null;
			String tag = null;
			try {
				message = cmq.GetNextMessage(Config.ECReceivedTerminalBytes, Config.ReceivedBytesOnlineAnalyzeQueue, null);
				if (message == null)
					continue;

				msg = cmq.TranseMessage(message);
				tag = msg.getMessageID();
				data = msg.getContent();

				int len=data.length;
				if (len > 7 && data[0] == 0x29 && data[1] == 0x29 && data[len - 9] == 0x0D) {
					com.ludong.iot.PacketBase  pb = new com.ludong.iot.PacketBase(data);
					float version = pb.GetVersion();
					IPacket packetInstance = PacketFactory.GetPacketInstance(version, data);
					long tid = packetInstance.GetTerminalID();
					String key="Terminal:"+tid;
					long serverSampleTime=com.cocopass.helper.CByte.bytesToLong(new byte[]{data[len - 8],data[len - 7],data[len - 6],data[len - 5],data[len - 4],data[len - 3],data[len - 2],data[len - 1]});
					String strLatestSamplingTime=com.cocopass.helper.CRedis.getMapValue(key, "LatestSamplingTime");
					long latestSamplingTime=0;
					if(!com.cocopass.helper.CString.IsNullOrEmpty(strLatestSamplingTime))
						latestSamplingTime=Long.parseLong(strLatestSamplingTime);
					if(	serverSampleTime>latestSamplingTime){
						com.cocopass.helper.CRedis.SetMapValue(key, "LatestSamplingTime", String.valueOf(serverSampleTime));
					}
				} 
			} catch (Exception er) {
				er.printStackTrace();
				com.cocopass.helper.CLoger.Error(er.getMessage());
			}
			finally{
				if (msg != null) {
					cmq.AckMessage(message, tag, false);
				}
			}
		}

	}
}
