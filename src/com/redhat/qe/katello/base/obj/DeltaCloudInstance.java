package com.redhat.qe.katello.base.obj;

import org.ovirt.engine.sdk.entities.VM;

public class DeltaCloudInstance {
	
	private String ipAddress;
	
	private String hostName;
	
	private String[] configs;
	
	private VM instance;
	
	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public VM getInstance() {
		return instance;
	}

	public void setInstance(VM instance) {
		this.instance = instance;
		this.ipAddress = instance.getGuestInfo().getIps().getIPs().get(0).getAddress();
		// TODO - not sure if we don't need to set the hostname too. 
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