package com.redhat.qe.katello.base.obj;

import javax.management.Attribute;

import com.redhat.qe.katello.base.threading.KatelloCliWorker;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloDistributor extends _KatelloObject{

	// ** ** ** ** ** ** ** Public constants
	public static final String CMD_CREATE = "distributor create";
	public static final String CMD_INFO = "distributor info";
	public static final String CMD_ADD_CUSTOM_INFO = "distributor add_custom_info";
	public static final String CMD_REMOVE_CUSTOM_INFO = "distributor remove_custom_info";
	public static final String CMD_UPDATE_CUSTOM_INFO = "distributor update_custom_info";
	public static final String CMD_DELETE = "distributor delete";
	public static final String CMD_UPDATE = "distributor update";
	public static final String CMD_LIST = "distributor list";
	public static final String CMD_SUBSCRIBE = "distributor subscribe";
	public static final String CMD_SUBSCRIPTIONS= "distributor subscriptions";
	public static final String CMD_UNSUBSCRIBE = "distributor unsubscribe";
	
	public static final String OUT_CREATE = "Successfully created distributor [ %s ]";
	public static final String OUT_INFO = "Successfully added Custom Information [ %s : %s ] to Distributor [ %s ]";
	public static final String OUT_REMOVE_INFO = "Successfully removed Custom Information from Distributor [ %s ]";
	public static final String OUT_UPDATE_INFO = "Successfully updated Custom Information [ %s ] for Distributor [ %s ]";
	public static final String OUT_INVALID_KEY = "Couldn't find custom info with keyname '%s'";
	public static final String OUT_DELETE = "Successfully deleted Distributor [ %s ]";
	public static final String OUT_UPDATE = "Successfully updated distributor [ %s ]";
	
	public static final String ERR_COULD_NOT_UPDATE_INFO ="Could not update Custom Information [ %s ] for Distributor [ %s ]";
	public static final String ERR_VALUE_TOO_LONG = "Validation failed: Value is too long (maximum is 255 characters)";
	public static final String ERR_KEY_TOO_LONG = "Validation failed: Keyname is too long (maximum is 255 characters)";
	public static final String ERR_DUPLICATE_KEY = "Validation failed: Keyname already exists for this object" ;
	public static final String ERR_NOT_FOUND = "Could not find Distributor [ %s ] in Org [ %s ]";
	public static final String ERR_NOT_FOUND_IN_ENV = "Could not find Distributor [ %s ] in Environment [ %s ] in Org [ %s ]"; 
	
	// ** ** ** ** ** ** ** Class members
	String org;
	public String name;
	public String uuid;
	public String environment;

	public KatelloDistributor(KatelloCliWorker kcr, String dOrg,String dName){
		this.org = dOrg;
		this.name=dName;
		this.kcr = kcr;
	}

	public KatelloDistributor(KatelloCliWorker kcr, String dOrg, String dName, String dEnv) {
		this(kcr, dOrg, dName);
		this.environment = dEnv;
	}

	public SSHCommandResult distributor_create(){
		opts.clear();
		opts.add(new Attribute("org", this.org));
		opts.add(new Attribute("name", this.name));
		opts.add(new Attribute("environment", this.environment));
		return run(CMD_CREATE);
	}

	public SSHCommandResult distributor_info(){
		opts.clear();
		opts.add(new Attribute("org", this.org));
		opts.add(new Attribute("name", this.name));
		opts.add(new Attribute("environment", this.environment));
		opts.add(new Attribute("uuid",uuid));
		return run(CMD_INFO);
	}
	
	public SSHCommandResult add_info(String keyname,String value,String uuid){
		opts.clear();
		opts.add(new Attribute("org",org));
		opts.add(new Attribute("name",name));
		opts.add(new Attribute("uuid",uuid));
		opts.add(new Attribute("keyname",keyname));
		opts.add(new Attribute("value",value));
		return run(CMD_ADD_CUSTOM_INFO);		
	}
	
	public SSHCommandResult remove_info(String keyname){
		opts.clear();
		opts.add(new Attribute("org",this.org));
		opts.add(new Attribute("name",this.name));
		opts.add(new Attribute("uuid",uuid));
		opts.add(new Attribute("keyname",keyname));
		return run(CMD_REMOVE_CUSTOM_INFO);		
	}
	
	public SSHCommandResult update_info(String keyname,String value,String uuid){
		opts.clear();
		opts.add(new Attribute("org",org));
		opts.add(new Attribute("name",name));
		opts.add(new Attribute("uuid",uuid));
		opts.add(new Attribute("keyname",keyname));
		opts.add(new Attribute("value",value));
		return run(CMD_UPDATE_CUSTOM_INFO);		
	}

	public SSHCommandResult update(String newName, String newEnv, String newDescr) {
		opts.clear();
		opts.add(new Attribute("org",org));
		opts.add(new Attribute("name",name));
		opts.add(new Attribute("new_name", newName));
		opts.add(new Attribute("new_environment", newEnv));
		opts.add(new Attribute("description", newDescr));
		return run(CMD_UPDATE);
	}

	public SSHCommandResult distributor_delete(){
		opts.clear();
		opts.add(new Attribute("org", this.org));
		opts.add(new Attribute("name", this.name));
		return run(CMD_DELETE);
	}

	public SSHCommandResult list() {
		opts.clear();
		opts.add(new Attribute("org", this.org));
		opts.add(new Attribute("environment", this.environment));
		return run(CMD_LIST);
	}

	public SSHCommandResult available_subscriptions() {
		opts.clear();
		opts.add(new Attribute("org", this.org));
		opts.add(new Attribute("name", this.name));
		return run(CMD_SUBSCRIPTIONS + " --available");
	}

	public SSHCommandResult subscriptions() {
		opts.clear();
		opts.add(new Attribute("org", this.org));
		opts.add(new Attribute("name", this.name));
		return run(CMD_SUBSCRIPTIONS);
	}

	public SSHCommandResult subscribe(String pool) {
		opts.clear();
		opts.add(new Attribute("org", this.org));
		opts.add(new Attribute("name", this.name));
		opts.add(new Attribute("pool", pool));
		return run(CMD_SUBSCRIBE);
	}

	public SSHCommandResult unsubscribe(String pool) {
		opts.clear();
		opts.add(new Attribute("org", this.org));
		opts.add(new Attribute("name", this.name));
		opts.add(new Attribute("entitlement", pool));// TODO: --serial ? --all ?
		return run(CMD_UNSUBSCRIBE);
	}
}
