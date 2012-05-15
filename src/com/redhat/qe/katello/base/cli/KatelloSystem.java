package com.redhat.qe.katello.base.cli;

import java.util.ArrayList;

import javax.management.Attribute;

import com.redhat.qe.katello.base.KatelloApi;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.katello.tasks.KatelloTasks;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloSystem {
	
	// ** ** ** ** ** ** ** Public constants	
	public static final String RHSM_DEFAULT_USER = "admin";
	public static final String RHSM_DEFAULT_PASS = "admin";
	
	public static final String CMD_INFO = "system info";
	public static final String CMD_LIST = "system list";
	public static final String CMD_SUBSCRIPTIONS = "system subscriptions";
	public static final String CMD_PACKAGES = "system packages";
	
	public static final String RHSM_CREATE = 
			String.format("subscription-manager register --username %s --password %s",
					RHSM_DEFAULT_USER,RHSM_DEFAULT_PASS);
	
	public static final String OUT_CREATE = 
			"The system has been registered with id:";
	public static final String ERR_RHSM_LOCKER_ONLY = 
			"Organization %s has '%s' environment only. Please create an environment for system registration.";
	public static final String ERR_RHSM_REG_ALREADY_FORCE_NEEDED = 
			"This system is already registered. Use --force to override";
	public static final String ERR_RHSM_REG_MULTI_ENV = 
			"Organization %s has more than one environment. Please specify target environment for system registration.";
	public static final String OUT_REMOTE_ACTION_DONE = "Remote action finished:";

	public static final String API_CMD_INFO = "/consumers/%s";
	public static final String API_CMD_GET_SERIALS = "/consumers/%s/certificates/serials";
	
	
	// ** ** ** ** ** ** ** Class members
	String name;
	String org;
	String environment;
	
	private KatelloTasks cliRhsm;	
	private KatelloCli cli;
	private ArrayList<Attribute> opts;
	
	public KatelloSystem(KatelloTasks pCliRhsm, String pName, String pOrg, String pEnv){
		this.cliRhsm = pCliRhsm;
		this.name = pName;
		this.org = pOrg;
		this.environment = pEnv;
		this.opts = new ArrayList<Attribute>();
	}
	
	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **
	
	public SSHCommandResult rhsm_register(){
		String cmd = RHSM_CREATE;
		
		if(this.name != null)
			cmd += " --name \""+this.name+"\"";
		if(this.org != null)
			cmd += " --org \""+this.org+"\"";
		if(this.environment != null)
			cmd += " --environment \""+this.environment+"\"";
		
		return cliRhsm.execute_remote(cmd);		
	}
	
	public SSHCommandResult rhsm_registerForce(){
		String cmd = RHSM_CREATE;
		
		if(this.name != null)
			cmd += " --name \""+this.name+"\"";
		if(this.org != null)
			cmd += " --org \""+this.org+"\"";
		if(this.environment != null)
			cmd += " --environment \""+this.environment+"\"";
		cmd += " --force";
		
		return cliRhsm.execute_remote(cmd);		
	}
	
	public SSHCommandResult list(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("environment", environment));
		cli = new KatelloCli(CMD_LIST+" -v", opts);
		return cli.run();
	}
	
	public SSHCommandResult subscriptions_available(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		cli = new KatelloCli(CMD_SUBSCRIPTIONS+" --available -v", opts);
		return cli.run();
	}
	
	public SSHCommandResult packages_install(String packageName){
		opts.clear();
		opts.add(new Attribute("install", packageName));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		cli = new KatelloCli(CMD_PACKAGES, opts);
		return cli.run();
	}
	
	public SSHCommandResult api_info(String byId){
		return new KatelloApi().get(String.format(API_CMD_INFO, byId));
	}
	
	public SSHCommandResult api_getSerials(String customerid){
		return new KatelloApi().get(String.format(API_CMD_GET_SERIALS, customerid));
	}
}
