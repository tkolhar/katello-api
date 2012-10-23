package com.redhat.qe.katello.base.obj;

import java.util.ArrayList;
import javax.management.Attribute;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.tools.SSHCommandResult;

@JsonIgnoreProperties(ignoreUnknown=true)
class _KatelloObject {

	protected KatelloUser user;
	protected ArrayList<Attribute> opts;
	
	public _KatelloObject(){
		this.opts = new ArrayList<Attribute>();
		this.user = null;
	}
	
	public void runAs(KatelloUser user){
		this.user = user;
	}
	
	protected SSHCommandResult run(String cmd){
		KatelloCli cli;
		if(user == null) 
			cli = new KatelloCli(cmd, opts); // as default admin
		else 
			cli = new KatelloCli(cmd, opts, user); // as the user specified
		return cli.run();
	}

	protected void runNowait(String cmd){
		KatelloCli cli;
		if(user == null) 
			cli = new KatelloCli(cmd, opts); // as default admin
		else 
			cli = new KatelloCli(cmd, opts, user); // as the user specified
		cli.runNowait();
	}
}
