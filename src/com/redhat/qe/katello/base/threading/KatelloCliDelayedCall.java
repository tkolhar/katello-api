package com.redhat.qe.katello.base.threading;

import java.util.logging.Logger;

public class KatelloCliDelayedCall implements Runnable{
	protected static Logger log = Logger.getLogger(KatelloCliDelayedCall.class.getName());
	public static final int TIMEOUT = 7200; //7200; // in seconds
	public enum RunStatus {
		NOT_STARTED,// ..
		PENDING,  	// ~~ 
		RUNNING, 	// ++
		FINISHED,	// --
		TIMEOUT		// ==
	}; 

	private String classname;
	private String method;
	private RunStatus status;
	private int ticker = 0;

	public KatelloCliDelayedCall(String classname, String method) {
		this.classname = classname;
		this.method = method;
		setStatus(RunStatus.NOT_STARTED);
	}
	
	public synchronized void setStatus(RunStatus status){
		this.status = status;
		if(this.status.equals(RunStatus.NOT_STARTED))
			log.finest(String.format(">>>>>> :: %s.%s() :: %s ::",classname,method,getStatus().toString()));
		if(this.status.equals(RunStatus.PENDING))
			log.finest(String.format("------ :: %s.%s() :: %s sec. :: %s ::",classname,method,ticker,getStatus().toString()));
		if(this.status.equals(RunStatus.RUNNING))
			log.finest(String.format("++++++ :: %s.%s() :: %s sec. :: %s ::",classname,method,ticker,getStatus().toString()));
		if(this.status.equals(RunStatus.FINISHED))
			log.finest(String.format("<<<<<< :: %s.%s() :: %s sec. :: %s ::",classname,method,ticker,getStatus().toString()));
		if(this.status.equals(RunStatus.TIMEOUT))
			log.warning(String.format("====== :: %s.%s() :: %s sec. :: %s ::",classname,method,ticker,getStatus().toString()));
	}
	
	public String getClassname(){return classname;}
	public String getMethodname(){return method;}
	public synchronized RunStatus getStatus(){return status;}

	@Override
	public void run() {
		setStatus(RunStatus.PENDING);
		while(ticker<TIMEOUT+1 && !getStatus().equals(RunStatus.FINISHED)) {
			ticker++;
			try{Thread.sleep(1000);}catch(InterruptedException iex){}
		}
		if(ticker>=(TIMEOUT+1)){
			setStatus(RunStatus.TIMEOUT);
		}
	}
}