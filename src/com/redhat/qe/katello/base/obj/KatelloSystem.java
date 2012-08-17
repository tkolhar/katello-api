package com.redhat.qe.katello.base.obj;

import java.util.ArrayList;
import javax.management.Attribute;
import com.redhat.qe.katello.base.KatelloApi;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloSystem {
	
	// ** ** ** ** ** ** ** Public constants	
	public static final String RHSM_DEFAULT_USER = "admin";
	public static final String RHSM_DEFAULT_PASS = "admin";
	
	public static final String CMD_INFO = "system info";
	public static final String CMD_LIST = "system list";
	public static final String CMD_SUBSCRIPTIONS = "system subscriptions";
	public static final String CMD_PACKAGES = "system packages";
	public static final String CMD_REPORT = "system report";
	
	public static final String RHSM_CREATE = 
			String.format("subscription-manager register --username %s --password %s",
					RHSM_DEFAULT_USER,RHSM_DEFAULT_PASS);
	public static final String RHSM_CLEAN = "subscription-manager clean";
	public static final String RHSM_SUBSCRIBE = "subscription-manager subscribe";
	public static final String RHSM_UNSUBSCRIBE = "subscription-manager unsubscribe";
	public static final String RHSM_IDENTITY = "subscription-manager identity";
	public static final String RHSM_REGISTER_BYKEY = "subscription-manager register ";
	
	public static final String OUT_CREATE = 
			"The system has been registered with id:";
	public static final String ERR_RHSM_LOCKER_ONLY = 
			"Organization %s has '%s' environment only. Please create an environment for system registration.";
	public static final String ERR_RHSM_REG_ALREADY_FORCE_NEEDED = 
			"This system is already registered. Use --force to override";
	public static final String ERR_RHSM_REG_MULTI_ENV = 
			"Organization %s has more than one environment. Please specify target environment for system registration.";
	public static final String OUT_REMOTE_ACTION_DONE = "Remote action finished:";
	public static final String OUT_RHSM_SUBSCRIBED_OK = 
			"Successfully subscribed the system"; // not a full string, .contains() needed. 

	public static final String API_CMD_INFO = "/consumers/%s";
	public static final String API_CMD_GET_SERIALS = "/consumers/%s/certificates/serials";
	
	public static final String REG_SUBSCRIPTION = "Subscription Name:\\s+%s\\s+Pool Id:\\s+\\w{32}+\\s+Quantity:\\s+%s";
	public static final String REG_SUBSCRIPTION_CFSE = "ProductName:\\s+%s\\s+ProductId:\\s+\\w{5,15}\\s+PoolId:\\s+\\w{32}+\\s+Quantity:\\s+%s";
	public static final String REG_POOL_ID = "\\s+\\w{32}+\\s+";
	
	// ** ** ** ** ** ** ** Class members
	public String name;
	String org;
	String environment;
	public String uuid;
	
	private KatelloCli cli;
	private ArrayList<Attribute> opts;
	
	public KatelloSystem(String pName, String pOrg, String pEnv){
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
		
		return KatelloUtils.sshOnClient(cmd);		
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
		
		return KatelloUtils.sshOnClient(cmd);		
	}

	public SSHCommandResult rhsm_registerForce(String activationkey){
		String cmd = RHSM_REGISTER_BYKEY;
		
		if(this.name != null)
			cmd += " --name \""+this.name+"\"";
		if(this.org != null)
			cmd += " --org \""+this.org+"\"";
		if(activationkey != null)
			cmd += " --activationkey \""+activationkey+"\"";
		cmd += " --force";
		
		return KatelloUtils.sshOnClient(cmd);		
	}
	
	public SSHCommandResult rhsm_clean(){
		String cmd = RHSM_CLEAN;		
		return KatelloUtils.sshOnClient(cmd);		
	}

	public SSHCommandResult list(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("environment", environment));
		cli = new KatelloCli(CMD_LIST+" -v", opts);
		return cli.run();
	}

	public SSHCommandResult report(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("environment", environment));
		cli = new KatelloCli(CMD_REPORT+" -v", opts);
		return cli.run();
	}
	
	public SSHCommandResult subscriptions_available(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		cli = new KatelloCli(CMD_SUBSCRIPTIONS+" --available -v", opts);
		return cli.run();
	}
	
	public SSHCommandResult subscriptions() {
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		cli = new KatelloCli(CMD_SUBSCRIPTIONS, opts);
		return cli.run();
	}

	public SSHCommandResult subscriptions_count() {
		String cmd = CMD_SUBSCRIPTIONS;
		
		if(this.name != null)
			cmd += " --name \""+this.name+"\"";
		if(this.org != null)
			cmd += " --org \""+this.org+"\"";

		cmd += " | grep \"Serial Id\" | wc -l";
		
		cli = new KatelloCli(cmd, null);
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
	
	public SSHCommandResult rhsm_subscribe(String poolid){
		String cmd = RHSM_SUBSCRIBE;
		
		if(poolid != null)
			cmd += " --pool "+poolid;
		
		return KatelloUtils.sshOnClient(cmd);		
	}

	public SSHCommandResult rhsm_unsubscribe(String serialId){
		String cmd = RHSM_UNSUBSCRIBE;
		
		if(serialId != null)
			cmd += " --serial "+serialId;
		
		return KatelloUtils.sshOnClient(cmd);		
	}
	
	public SSHCommandResult rhsm_subscribe(String poolid, int quantity){
		String cmd = RHSM_SUBSCRIBE;
		
		if(poolid != null)
			cmd += " --pool "+poolid;
		if (quantity != 0) 
			cmd += " --quantity " + quantity;
		
		return KatelloUtils.sshOnClient(cmd);		
	}
	
	public SSHCommandResult rhsm_subscribe_auto(){
		String cmd = RHSM_SUBSCRIBE + " --auto";
		
		return KatelloUtils.sshOnClient(cmd);		
	}
	
	public SSHCommandResult rhsm_identity(){
		String cmd = RHSM_IDENTITY;
		
		return KatelloUtils.sshOnClient(cmd);		
	}
	
	public SSHCommandResult api_info(String byId){
		return new KatelloApi().get(String.format(API_CMD_INFO, byId));
	}
	
	public SSHCommandResult api_getSerials(String customerid){
		return new KatelloApi().get(String.format(API_CMD_GET_SERIALS, customerid));
	}
}
