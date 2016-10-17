package com.ludong.datacollect;


import org.apache.log4j.Logger;
import com.cocopass.helper.CMQ.MQFactory;
import com.cocopass.helper.CMQ.Message;
import com.ludong.decode.Config;


public class PacketToBossThread extends Thread {
	// static Logger LOG = Logger.getLogger(PacketDecodeThread.class.getName());

	com.ludong.decode.model.MQ mq=null;
	public PacketToBossThread(com.ludong.decode.model.MQ mq){
		this.mq=mq;
	}
	
	public void run() {

		com.cocopass.helper.CMQ.CMessageQueue cmq = MQFactory.createMQ(this.mq.getName());
		cmq.SetConnection(this.mq.getAliasName(), this.mq.getHost(), this.mq.getUserName(), this.mq.getPassword(), this.mq.getPort());
		cmq.SetService();
		cmq.InitService();
		cmq.SetConsumer(Config.ReceivedTerminalBytesToBossQueue);
		
		com.cocopass.helper.CMQ.CMessageQueue bossCMQ = MQFactory.createMQ(Config.BossMQName);
		bossCMQ.SetConnection(Config.BossMQAliasName, Config.BossMQHost, Config.BossMQUserName, Config.BossMQPassword,
				Config.BossMQPort);
		bossCMQ.SetService();


		byte[] data = null;
		while (true) {
			Message msg = null;
			Object message = null;
			String tag = null;
			try {

				message = cmq.GetNextMessage(Config.ECReceivedTerminalBytes, Config.ReceivedTerminalBytesToBossQueue, null);
				if (message == null)
					continue;

				msg = cmq.TranseMessage(message);
				tag = msg.getMessageID();
				data = msg.getContent();

				if (data.length > 7 && data[0] == 0x29 && data[1] == 0x29 && data[data.length - 9] == 0x0D) {		
					bossCMQ.PublishMessage(Config.ECReceivedTerminalBytes, "", data);		
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
