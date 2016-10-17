package com.ludong.decode;

import java.util.List;

import com.ludong.decode.Config;

public class Config {

	public static long DebugTerminalID = 0;

	public static int RunMode = 0;
	public static String AppKey = "";
	public static String AppSecret = "";
	public static String DistributionUrl = "";

	public static int RedisPort = 0;
	public static String RedisHost = "127.0.0.1";
	public static String RedisPassword = "";
	public static int RedisPoolMaxActive = 500;
	public static int RedisPoolMaxIdle = 5;
	public static long RedisPoolMaxWaitMillis = 100000;
	public static int RedisPoolTimeOut = 10000;

	public static String ECStationInfo = "";
	public static String ECBatteryInfo = "";
	public static String ECGPSInfo = "";
	public static String ECAuthInfo = "";
	public static String ECLoginInfo = "";
	public static String ECPushLog = "";
	public static String ECAlarm = "";
	
	public static String AuthInfoToDBQueue = "";

	public static String GPSInfoStationDBQueue = "";
	public static String GPSInfoToCacheDBQueue = "";
	
	public static String AlarmToBookerQueue = "";
	
	public static String ActionLogToCacheDBQueue = "";
	public static String ActionLogOfRequestQueue = "";
	public static String ActionLogToBookerQueue = "";
	public static String ActionLogToDBQueue	= "";
	
	public static String StationInfoToBookerQueue = "";
	
	public static String BatteryInfoToBookerQueue = "";
	public static String BatteryInfoToDBQueue = "";
	
	public static String LoginInfoToBossQueue = "";
	public static String LoginInfoToDBQueue="";
	
	public static String ReceivedBytesOnlineAnalyzeQueue="";
	public static String ReceivedTerminalBytesQueue = "";
	public static String ReceivedTerminalBytesToBossQueue= "";
	
	public static String PushLogToDBQueue="";
	
	

	public static String WeatherAPIURL = "";
	public static String CityListURL = "";
	public static String ECReceivedTerminalBytes = "";
	public static String ECActionLog = "";

	// public static String RabbitMQHost="";
	// public static String RabbitMQUserName="";
	// public static String RabbitMQPassword="";
	// public static int RabbitMQPort = 5672;

	public static String MQName;
	public static String MQHost;
	public static String MQUserName;
	public static String MQPassword;
	public static int MQPort;
	public static String MQAliasName = "";

	public static String BossMQName;
	public static String BossMQHost;
	public static String BossMQUserName;
	public static String BossMQPassword;
	public static int BossMQPort;
	public static String BossMQAliasName = "";
	public static String BossECPushLog = "";
	
	public static List<com.ludong.decode.model.MQ> ListStationMQ =null;

	public static String AzureBusMQ = "";

	public static int PacketDecodeThreadNum = 1;
	public static int GPSInfoSaveToDBThreadNum = 1;
	public static int BatteryInfoSaveToDBThreadNum = 1;
	public static int PushDataSaveToDBThreadNum= 1;
	
	public static int BeginSaveGPSInfoNum = 1;
	public static int BeginSaveBatteryInfoNum = 1;
	public static int BeginSavePushDataNum = 1;
	

	public static boolean IsRunActionLogPush = false;
	public static boolean IsRunBatteryInfoPush = false;
	public static boolean IsRunStationInfoPush = false;
	public static boolean  IsRunBatteryInfoSave = false;
	public static boolean IsRunGPSInfoSave = false;
	public static boolean IsRunDecode = false;
	public static boolean IsRunDebugThread = false;
	public static boolean IsRunAlarmPush = false;
}
