package com.redhat.qe.katello.base.threading;

import java.util.logging.Logger;

public class KatelloCliWorker {
	private static Logger log = Logger.getLogger(KatelloCliWorker.class.getName());
	
	private String clientHostname;
	private String serverHostname;
	private String threadName;
	private String className;
	private boolean isBusy;
	
	public KatelloCliWorker(String servername, String clientname){
		this.clientHostname = clientname;
		this.serverHostname = servername;
		
		this.isBusy = false;
		this.className = null;
		this.threadName = null;
	}
	
	public static KatelloCliWorker getSingleMode(){
		return new KatelloCliWorker(
				System.getProperty("katello.server.hostname","localhost"), 
				System.getProperty("katello.client.hostname","localhost"));
	}
	
	public synchronized boolean isBusy(){
		return isBusy;
	}
	
	public synchronized void setBusy(String threadName, String className){
		this.isBusy = true;
		this.threadName = threadName;
		this.className = className;
		log.info(">>> set worker busy to: "+clientHostname+" for "+className+" : thread "+threadName);
	}
	
	public synchronized void setFree(){
		log.info("<<< set worker free from: "+clientHostname+" for "+className+" : thread "+threadName);
		this.className = null;
		this.threadName = null;
		this.isBusy = false;
	}
	
	public String getClientHostname(){
		return clientHostname;
	}
	
	public String getServerHostname(){
		return serverHostname;
	}
	
	public String getThreadName(){
		return threadName;
	}
	public String getClassName(){
		return className;
	}
	
	@Override
	public synchronized KatelloCliWorker clone(){
		return new KatelloCliWorker(this.serverHostname, this.clientHostname);
	}
}
