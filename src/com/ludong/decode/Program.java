/**
 * 
 */
package com.ludong.decode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.cocopass.helper.*;
import com.ludong.iot.IPacket;
import com.ludong.iot.PacketFactory;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.MessageProperties;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

/**
 * @author 朱日昭
 *
 */
public class Program {

	/**
	 * @param args
	 */
	// static Logger LOG = Logger.getLogger(Program.class.getName());

	// private static String QReceivedTerminalBytes =
	// "ReceivedTerminalBytes-V1.0"; // ReceivedTerminalBytes
	// private static String QDownInstruction = "DownInstruction-V1.0";
	//static Gson gson = new Gson();
	// static com.cocopass.helper.CProperties cp= new
	// com.cocopass.helper.CProperties();

	public static void main(String[] args) throws IOException, TimeoutException {
		// TODO Auto-generated method stub

		// com.cocopass.iot.bll.LuYuanDongService.GetAuthCookie("http://192.168.4.102/service/",
		// "aabb");

		// byte[]
		// timestampByteArray=com.cocopass.helper.CByte.longToBytes(1464666302);
		// LOG.debug(com.cocopass.helper.CByte.BytesToHexString(timestampByteArray));
		// int a=1;
		// if(a>0){
		// return;
		// }

		// LOG.info("programe start.");
		// com.cocopass.helper.CLoger.Info("hello,clogher");

		Init();

		Work.Start();

		com.cocopass.helper.CLoger.Info("programe start.");
		// int a=1;
		// while(a>0)
		// {
		//
		// }

		// LOG.info("init config and connect-poll finished .");

		// try {
		// channel = connection.createChannel();
		// channel.exchangeDeclare("JsonGPSInfo", "fanout", true);
		// channel.exchangeDeclare("JsonControllerInfo", "fanout", true);
		// channel.exchangeDeclare("JsonBatteryInfo", "fanout", true);
		// Map<String, Object> iargs = new HashMap<String, Object>();
		/*
		 * iargs.put("x-message-ttl", 3000); iargs.put("x-expires", 60000);
		 * channel.queueDeclare("testaa",true , false, true, iargs); //"",
		 * false, false, true, iargs 队列会在没消费者时删除 //"", true, false, true, iargs
		 * 队列会在没消费者时删除 //"testaa", true, false, true, iargs 队列会在没消费者时删除
		 */
		//
		// iargs.put("x-message-ttl", 3000);
		// channel.queueDeclare("Q1JsonGPSInfo",true , false, true, iargs);
		// //第4个参数没有观察到，或者是时间未到？
		// channel.queueDeclare("Q1JsonControllerInfo",false , false, true,
		// iargs);
		// channel.queueDeclare("Q1JsonBatteryInfo",false , false, true, iargs);
		// channel.queueDeclare("Q1JsonALarm",false , false, true, iargs);
		// "testaa",true , false, true, iargs 队列里的消息会减少
		// channel.queueBind("Q1JsonGPSInfo" , "JsonGPSInfo", "");
		// channel.queueBind("Q1JsonControllerInfo" , "JsonControllerInfo", "");
		// channel.queueBind("Q1JsonBatteryInfo" , "JsonBatteryInfo", "");
		// //第一个参数为空，最后一次定义的的队列能收到消息，估计是同个通道的缘故。
		// channel.queueBind("Q1JsonALarm" , "JsonAlarm", "");
		// } catch (IOException e) {
		// TODO Auto-generated catch block
		// e.printStackTrace();
		// }

	}

	static void Init() {
		// com.cocopass.helper.Config config=new com.cocopass.helper.Config();
		// String[] arrayKey=new String[]{"RabbitMQHost"};
		// Map<String, String> mapConfig = config.GetProperties(arrayKey);
		// System.out.println(mapConfig.get("RabbitMQHost"));

		try {
			// Load app.properties

			Global.iProperties.SetProperties();
			
			Config.DebugTerminalID = Long.parseLong(Global.iProperties.GetValue("DebugTerminalID"));
			Config.IsRunDebugThread = Boolean.parseBoolean(Global.iProperties.GetValue("IsRunDebugThread"));

			Config.RunMode = Integer.parseInt(Global.iProperties.GetValue("RunMode"));

			Config.WeatherAPIURL = Global.iProperties.GetValue("WeatherAPIURL");
			Config.CityListURL = Global.iProperties.GetValue("CityListURL");

			Config.ECBatteryInfo = Global.iProperties.GetValue("ECBatteryInfo");
			Config.ECGPSInfo = Global.iProperties.GetValue("ECGPSInfo");
			Config.ECStationInfo = Global.iProperties.GetValue("ECStationInfo");
			Config.ECAuthInfo = Global.iProperties.GetValue("ECAuthInfo");
			Config.ECLoginInfo = Global.iProperties.GetValue("ECLoginInfo");
			Config.ECPushLog = Global.iProperties.GetValue("ECPushLog");
			Config.ECAlarm = Global.iProperties.GetValue("ECAlarm");		
			
			Config.GPSInfoStationDBQueue = Global.iProperties.GetValue("GPSInfoStationDBQueue");
			Config.GPSInfoToCacheDBQueue = Global.iProperties.GetValue("GPSInfoToCacheDBQueue");
			
			Config.ActionLogToCacheDBQueue = Global.iProperties.GetValue("ActionLogToCacheDBQueue");
			Config.ActionLogOfRequestQueue = Global.iProperties.GetValue("ActionLogOfRequestQueue");
			Config.ActionLogToDBQueue = Global.iProperties.GetValue("ActionLogToDBQueue");
			
			Config.StationInfoToBookerQueue = Global.iProperties.GetValue("StationInfoToBookerQueue");
			Config.StationInfoToBookerQueue = Global.iProperties.GetValue("StationInfoToBookerQueue");
			Config.BatteryInfoToBookerQueue = Global.iProperties.GetValue("BatteryInfoToBookerQueue");
			

			Config.RedisPort = Integer.parseInt(Global.iProperties.GetValue("RedisPort"));
			Config.RedisHost = Global.iProperties.GetValue("RedisHost");
			Config.RedisPassword = Global.iProperties.GetValue("RedisPassword");
			Config.RedisPoolMaxActive = Integer.parseInt(Global.iProperties.GetValue("RedisPoolMaxActive"));
			Config.RedisPoolMaxIdle = Integer.parseInt(Global.iProperties.GetValue("RedisPoolMaxIdle"));
			Config.RedisPoolMaxWaitMillis = Long.parseLong(Global.iProperties.GetValue("RedisPoolMaxWaitMillis"));
			Config.RedisPoolTimeOut = Integer.parseInt(Global.iProperties.GetValue("RedisPoolTimeOut"));
			
			

			if (Config.RunMode == 1) {
				Config.AppKey = Global.iProperties.GetValue("AppKey");
				Config.AppSecret = Global.iProperties.GetValue("AppSecret");
				Config.DistributionUrl = Global.iProperties.GetValue("DistributionUrl");
				Config.ActionLogToBookerQueue = Global.iProperties.GetValue("ActionLogToBookerQueue");
			}
			else if(Config.RunMode == 0){
				String stationMQ=Global.iProperties.GetValue("StationMQ");
				Config.ListStationMQ = Global.gson.fromJson(stationMQ,  new TypeToken<List<com.ludong.decode.model.MQ>>() {  }.getType());  
			}

			Config.ECActionLog = Global.iProperties.GetValue("ECActionLog");
			com.cocopass.helper.CRedis.StartPool(Config.RedisPoolMaxActive, Config.RedisPoolMaxIdle,
					Config.RedisPoolMaxWaitMillis, Config.RedisHost, Config.RedisPort, Config.RedisPoolTimeOut,
					Config.RedisPassword);

			Config.ECReceivedTerminalBytes = Global.iProperties.GetValue("ECReceivedTerminalBytes");
			
			Config.ReceivedTerminalBytesQueue = Global.iProperties.GetValue("ReceivedTerminalBytesQueue");
			Config.ReceivedTerminalBytesToBossQueue = Global.iProperties.GetValue("ReceivedTerminalBytesToBossQueue");
			Config.ReceivedBytesOnlineAnalyzeQueue= Global.iProperties.GetValue("ReceivedBytesOnlineAnalyzeQueue");
			
			Config.LoginInfoToDBQueue= Global.iProperties.GetValue("LoginInfoToDBQueue");
			Config.BatteryInfoToDBQueue= Global.iProperties.GetValue("BatteryInfoToDBQueue");
			Config.AuthInfoToDBQueue= Global.iProperties.GetValue("AuthInfoToDBQueue");
			Config.PushLogToDBQueue=Global.iProperties.GetValue("PushLogToDBQueue");
			
			Config.AlarmToBookerQueue=Global.iProperties.GetValue("AlarmToBookerQueue");
			
			// QDownInstruction=cp.GetValue("QDownInstruction");

			// Config.RabbitMQHost =
			// Global.iProperties.GetValue("RabbitMQHost");
			// Config.RabbitMQUserName =
			// Global.iProperties.GetValue("RabbitMQUserName");
			// Config.RabbitMQPassword =
			// Global.iProperties.GetValue("RabbitMQPassword");
			// Config.RabbitMQPort =
			// Integer.parseInt(Global.iProperties.GetValue("RabbitMQPort"));

			//
			// Global.factory.setHost(Config.RabbitMQHost);
			// Global.factory.setUsername(Config.RabbitMQUserName);
			// Global.factory.setPassword(Config.RabbitMQPassword);
			// Global.factory.setPort(Config.RabbitMQPort);

			Config.MQName = Global.iProperties.GetValue("MQName");
			Config.MQAliasName = Global.iProperties.GetValue("MQAliasName");
			Config.MQHost = Global.iProperties.GetValue("MQHost");
			Config.MQUserName = Global.iProperties.GetValue("MQUserName");
			Config.MQPassword = Global.iProperties.GetValue("MQPassword");
			Config.MQPort = Integer.parseInt(Global.iProperties.GetValue("MQPort"));

			Config.BossMQName = Global.iProperties.GetValue("BossMQName");
			Config.BossMQAliasName = Global.iProperties.GetValue("BossMQAliasName");
			Config.BossMQHost = Global.iProperties.GetValue("BossMQHost");
			Config.BossMQUserName = Global.iProperties.GetValue("BossMQUserName");
			Config.BossMQPassword = Global.iProperties.GetValue("BossMQPassword");
			Config.BossMQPort = Integer.parseInt(Global.iProperties.GetValue("BossMQPort"));

			Config.BossECPushLog = Global.iProperties.GetValue("BossECPushLog");

			Config.GPSInfoSaveToDBThreadNum = Integer.parseInt(Global.iProperties.GetValue("GPSInfoSaveToDBThreadNum"));
			Config.PacketDecodeThreadNum = Integer.parseInt(Global.iProperties.GetValue("PacketDecodeThreadNum"));
			Config.BatteryInfoSaveToDBThreadNum = Integer.parseInt(Global.iProperties.GetValue("BatteryInfoSaveToDBThreadNum"));
			Config.PushDataSaveToDBThreadNum = Integer.parseInt(Global.iProperties.GetValue("PushDataSaveToDBThreadNum"));
			
			Config.BeginSaveBatteryInfoNum = Integer.parseInt(Global.iProperties.GetValue("BeginSaveBatteryInfoNum"));
			Config.BeginSavePushDataNum = Integer.parseInt(Global.iProperties.GetValue("BeginSavePushDataNum"));
			 
			Config.IsRunAlarmPush = Boolean.parseBoolean(Global.iProperties.GetValue("IsRunAlarmPush"));
			Config.IsRunActionLogPush = Boolean.parseBoolean(Global.iProperties.GetValue("IsRunActionLogPush"));
			Config.IsRunBatteryInfoPush = Boolean.parseBoolean(Global.iProperties.GetValue("IsRunBatteryInfoPush"));
			Config.IsRunStationInfoPush = Boolean.parseBoolean(Global.iProperties.GetValue("IsRunStationInfoPush"));
			Config.IsRunGPSInfoSave = Boolean.parseBoolean(Global.iProperties.GetValue("IsRunGPSInfoSave"));
			Config.IsRunBatteryInfoSave = Boolean.parseBoolean(Global.iProperties.GetValue("IsRunBatteryInfoSave"));
			Config.IsRunDecode = Boolean.parseBoolean(Global.iProperties.GetValue("IsRunDecode"));
 
			

		} catch (Exception e) {
			e.printStackTrace();
			com.cocopass.helper.CLoger.Error(e.getMessage());
		}
	}

}
