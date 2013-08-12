package com.redhat.qe.katello.base.obj;

import com.redhat.qe.katello.base.threading.KatelloCliWorker;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloPing extends _KatelloObject{
	   
	    public static final String CMD_PING = "ping -v";
		
	    public KatelloPing(KatelloCliWorker kcr){
	    	this.kcr = kcr;
	    }
	    
		public SSHCommandResult cli_ping(){
			opts.clear();
			return run(CMD_PING);
		}		
}