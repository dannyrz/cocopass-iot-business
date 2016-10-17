package com.ludong.decode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

import org.dave.common.database.DatabaseConnectionPool;

import com.ludong.data.process.LatestOnlineTimeUpdateThread;
import com.ludong.data.process.OnlineAnalyzeThread;
import com.ludong.datacollect.LoginInfoToBossThread;
import com.ludong.datacollect.PacketToBossThread;
import com.ludong.decode.debug.PacketDecodeDebugThread;
import com.ludong.decode.old.ActionLogPushThread0;
import com.ludong.decode.old.BatteryInfoPushThread0;
import com.ludong.decode.old.StationPushThread0;
import com.ludong.distribution.ActionLogPushThread;
import com.ludong.distribution.AlarmPushThread;
import com.ludong.distribution.BatteryInfoPushThread;
import com.ludong.distribution.StationPushThread;

public class Work {

	public static void Start() {

		// 主要用于同步 原始报文，推送日志，和指令调用记录。BOSS服务器主动调用节点队列
		if (Config.IsRunDebugThread) {
			Config.DebugTerminalID=0;
			while(Config.DebugTerminalID<1){
				Scanner sc = new Scanner(System.in);   
		        System.out.print("输入监控的设备ID:"); 
		        try{
		        	Config.DebugTerminalID=sc.nextLong(); 
		        }
		        catch(Exception er){
		        	
		        }
			}
			
			PacketDecodeDebugThread packetDecodedEBUGThread = new PacketDecodeDebugThread();
			packetDecodedEBUGThread.start();
			//ActionLogToDBThread actionLogToDBThread = new ActionLogToDBThread();
			//actionLogToDBThread.start();
//			PushLogToDBThreadMutil pushLogToDBThreadMutil=new PushLogToDBThreadMutil();
//			pushLogToDBThreadMutil.start();
			
			
			 //OnlineAnalyzeThread onlineAnalyzeThread = new OnlineAnalyzeThread();
			 //onlineAnalyzeThread.start();
			
			 //LatestOnlineTimeUpdateThread latestOnlineTimeUpdateThread = new LatestOnlineTimeUpdateThread();
			 //latestOnlineTimeUpdateThread.start();
			
		} else {
			if (Config.RunMode == 0) {
				// OnlineAnalyzeThread onlineAnalyzeThread = new
				// OnlineAnalyzeThread();
				// onlineAnalyzeThread.start();
				//
				// LatestOnlineTimeUpdateThread latestOnlineTimeUpdateThread =
				// new LatestOnlineTimeUpdateThread();
				// latestOnlineTimeUpdateThread.start();

				LoginInfoToDBThread loginInfoToDBThread = new LoginInfoToDBThread();
				loginInfoToDBThread.start();
				
				AuthInfoToDBThread authInfoToDBThread = new AuthInfoToDBThread();
				authInfoToDBThread.start();
				
				ActionLogToDBThread actionLogToDBThread = new ActionLogToDBThread();
				actionLogToDBThread.start();
				
				for (int i = 0; i < Config.PushDataSaveToDBThreadNum; i++) {
					PushLogToDBThreadMutil pushLogToDBThreadMutil=new PushLogToDBThreadMutil();
					pushLogToDBThreadMutil.start();
				}

				for (com.ludong.decode.model.MQ mq : Config.ListStationMQ) {
					PacketToBossThread packetToBossThread = new PacketToBossThread(mq);
					packetToBossThread.start();
				}

			}

			// 主要用于同步 原始报文，推送日志，和指令调用记录。节点服务器主动推送到BOSS队列
//			else if (Config.RunMode == 1) {
//				 PacketToBossThread packetToBossThread = new PacketToBossThread();
//				 packetToBossThread.start();
//			}

 

			if (Config.IsRunDecode) {
				for (int i = 0; i < Config.PacketDecodeThreadNum; i++) {
					PacketDecodeThread packetDecodeThread = new PacketDecodeThread();
					packetDecodeThread.start();
				}
			}

			if (Config.IsRunGPSInfoSave) {
				for (int i = 0; i < Config.GPSInfoSaveToDBThreadNum; i++) {

					GPSInfoSaveToDBThreadMutil gpsInfoSaveToDBThreadMutil = new GPSInfoSaveToDBThreadMutil();
					gpsInfoSaveToDBThreadMutil.start();

					GPSInfoToCacheDBThread gpsInfoToCacheDBThread = new GPSInfoToCacheDBThread();
					gpsInfoToCacheDBThread.start();

				}
			}
			
			if (Config.IsRunBatteryInfoSave) {
				for (int i = 0; i < Config.BatteryInfoSaveToDBThreadNum; i++) {
					BatteryInfoSaveToDBThreadMutil batteryInfoSaveToDBThreadMutil = new BatteryInfoSaveToDBThreadMutil();
					batteryInfoSaveToDBThreadMutil.start();
				}
			}

			if (Config.IsRunActionLogPush) {

				//ActionLogToCacheDBThread actionLogToCacheDBThread = new ActionLogToCacheDBThread();
				//actionLogToCacheDBThread.start();

				ActionInstructionStatusThread actionInstructionStatusThread = new ActionInstructionStatusThread();
				actionInstructionStatusThread.start();

				ActionLogPushThread actionLogPushThread = new ActionLogPushThread();
				actionLogPushThread.start();

			}
			// WeatherSynThread weatherSynThread=new WeatherSynThread();
			// weatherSynThread.start();

			if (Config.IsRunStationInfoPush) {
				StationPushThread stationPushThread = new StationPushThread();
				stationPushThread.start();
			}

			if (Config.IsRunBatteryInfoPush) {
				BatteryInfoPushThread batteryInfoPushThread = new BatteryInfoPushThread();
				batteryInfoPushThread.start();
			}
			
			if (Config.IsRunAlarmPush) {
				AlarmPushThread alarmPushThread = new AlarmPushThread();
				alarmPushThread.start();
			}

		}

	}
}