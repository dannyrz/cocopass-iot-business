package com.ludong.decode.model;

public class MQ {
	  String Name;
	  String Host;
	  String UserName;
	  String Password;
	  int Port;
	  String AliasName = "";
	  
	public String getName() {
		return Name;
	}
	public void setName(String name) {
		Name = name;
	}
	public String getHost() {
		return Host;
	}
	public void setHost(String host) {
		Host = host;
	}
	public String getUserName() {
		return UserName;
	}
	public void setMQUserName(String userName) {
		UserName = userName;
	}
	public String getPassword() {
		return Password;
	}
	public void setMQPassword(String password) {
		Password = password;
	}
	public int getPort() {
		return Port;
	}
	public void setMQPort(int port) {
		Port = port;
	}
	public String getAliasName() {
		return AliasName;
	}
	public void setMQAliasName(String aliasName) {
		AliasName = aliasName;
	}
	  
	  
}
