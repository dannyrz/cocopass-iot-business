package com.ludong.data.process;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.cocopass.helper.CMQ.MQFactory;
import com.ludong.decode.Config;
import com.ludong.decode.Global;

public class OnlineAnalyzeThread extends Thread{
	public void run() {
		
//		com.cocopass.helper.CMQ.CMessageQueue cmq = MQFactory.createMQ(Config.MQName);
//		cmq.SetConnection(Config.MQAliasName, Config.MQHost, Config.MQUserName, Config.MQPassword, Config.MQPort);
//		cmq.SetService();
//		cmq.InitService();
		
		
		long oneMin=60*1000;
		HashMap<Long , Boolean> map = new HashMap<Long , Boolean>();   
		com.cocopass.bll.OnlineShaft onlineshaftBLL=new com.cocopass.bll.OnlineShaft();
		com.cocopass.bll.Terminal terminalBLL=new com.cocopass.bll.Terminal();
		int i=1;
		List<com.cocopass.iot.model.Terminal> list=null;
		while(true){
			
			//System.out.print("start:"+i+":"+new Date().getTime());
			
			
			try {
				 list=terminalBLL.GetList("",1,20000);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(list!=null&&list.size()>0){
				for(com.cocopass.iot.model.Terminal terminal:list){
					long t0=new Date().getTime();
					long terminalID=terminal.getID();
					String key="Terminal:"+terminalID;
					String strLatestSamplingTime = com.cocopass.helper.CRedis.getMapValue(key, "LatestSamplingTime");
					long t1=0;
					if(!com.cocopass.helper.CString.IsNullOrEmpty(strLatestSamplingTime)){
						t1=Long.parseLong(strLatestSamplingTime);
					}
					long ts=Math.abs(t0-t1);
					boolean isOnline=(ts<oneMin);
					if(!map.containsKey(terminalID)||map.get(terminalID)!=isOnline){
						map.put(terminalID, isOnline);
						com.cocopass.iot.model.OnLineShaft model=new com.cocopass.iot.model.OnLineShaft();
						model.setAddTime(new Date().getTime());
						model.setIsOnline(isOnline);
						model.setTerminalID(terminalID);
						
						//String json=Global.gson.toJson(model);
						
						onlineshaftBLL.Add(model);
						
						//cmq.PublishMessage("EC-OnLineShaft", "", json.getBytes());
						//com.cocopass.helper.CLoger.Info(isOnline+" add onlineshaft success!");
					}
				}
				list.clear();
			}
			
		 

		}
		
	}
}