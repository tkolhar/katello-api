package com.redhat.qe.katello.base.obj;

import java.util.ArrayList;
import javax.management.Attribute;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.tools.SSHCommandResult;

@JsonIgnoreProperties(ignoreUnknown=true)
class _KatelloObject {

	protected KatelloUser user;
	protected String hostName = System.getProperty("katello.client.hostname", "localhost");
	protected ArrayList<Attribute> opts;
	
	public _KatelloObject(){
		this.opts = new ArrayList<Attribute>();
		this.user = null;
	}
	
	public void runAs(KatelloUser user){
		this.user = user;
	}
	
    public String getHostName() {
    	return hostName;
    }
    
    public void setHostName(String hostName) {
    	this.hostName = hostName;
    }
	
	protected SSHCommandResult run(String cmd){
		KatelloCli cli = new KatelloCli(cmd, opts, user, hostName); // as the user specified on specified host
		return cli.run();
	}

	protected void runNowait(String cmd){
		KatelloCli cli = new KatelloCli(cmd, opts, user, hostName); // as the user specified on specified host
		cli.runNowait();
	}
}
