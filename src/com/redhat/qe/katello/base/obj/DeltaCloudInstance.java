package com.redhat.qe.katello.base.obj;

import org.apache.deltacloud.client.DeltaCloudClientImpl;
import org.apache.deltacloud.client.Instance;

public class DeltaCloudInstance {
	
	private String ipAddress;
	
	private String hostName;
	
	private String[] configs;
	
	private Instance instance;
	
	private DeltaCloudClientImpl client;

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public Instance getInstance() {
		return instance;
	}

	public void setInstance(Instance instance) {
		this.instance = instance;
	}

	public DeltaCloudClientImpl getClient() {
		return client;
	}

	public void setClient(DeltaCloudClientImpl client) {
		this.client = client;
	}	

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public String[] getConfigs() {
		return configs;
	}

	public void setConfigs(String[] configs) {
		this.configs = configs;
	}

}
