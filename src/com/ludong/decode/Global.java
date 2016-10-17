package com.ludong.decode;

import com.cocopass.helper.CProperties;
import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;

public class Global {
	// public static Channel RabbitMQChannel=null;
	public static CProperties iProperties = new CProperties();
	public static Gson gson = new Gson();
	public static ConnectionFactory factory = new ConnectionFactory();

}
