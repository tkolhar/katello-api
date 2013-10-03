package com.redhat.qe.katello.base.obj;

import java.util.ArrayList;

import javax.management.Attribute;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import com.redhat.qe.katello.base.HammerCli;
import com.redhat.qe.katello.base.threading.KatelloCliWorker;
import com.redhat.qe.tools.SSHCommandResult;

@JsonIgnoreProperties(ignoreUnknown=true)
class _HammerObject {

	protected HammerUser user;
	protected String hostName = System.getProperty("katello.client.hostname", "localhost");
	protected ArrayList<Attribute> opts;
	protected KatelloCliWorker kcr = null;
	
	public _HammerObject(){
		this.opts = new ArrayList<Attribute>();
		this.user = null;
	}
	
	public void runAs(HammerUser user){
		this.user = user;
	}
	
    public String getHostName() {
    	if(kcr!=null)
    		return kcr.getClientHostname();
    	else
    		return hostName;
    }
    
    public void runOn(String hostName) {
    	this.hostName = hostName;
    }
	
	protected SSHCommandResult run(String cmd){
		HammerCli cli;
		if(kcr!=null)
			cli = new HammerCli(cmd, opts, user, kcr.getClientHostname());
		else
			cli = new HammerCli(cmd, opts, user, hostName); // as the user specified on specified host
		return cli.run();
	}

	protected void runNowait(String cmd){
		HammerCli cli;
		if(kcr!=null)
			cli = new HammerCli(cmd, opts, user, kcr.getClientHostname());
		else
			cli = new HammerCli(cmd, opts, user, hostName); // as the user specified on specified host
		cli.runNowait();
	}

	protected SSHCommandResult runExt(String cmd, String cmdTail){
		HammerCli cli;
		if(kcr!=null)
			cli = new HammerCli(cmd, opts, user, kcr.getClientHostname());
		else
			cli = new HammerCli(cmd, opts, user, hostName); // as the user specified on specified host
		return cli.runExt(cmdTail);
	}
}
