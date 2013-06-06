package com.redhat.qe.katello.base.obj;

import javax.management.Attribute;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloDistributor extends _KatelloObject{

	// ** ** ** ** ** ** ** Public constants
	public static final String CMD_CREATE = "distributor create";
	public static final String CMD_INFO = "distributor info";
	public static final String CMD_ADD_CUSTOM_INFO = "distributor add_custom_info";
	public static final String OUT_CREATE = "Successfully createed distributor [ %s ]";

	// ** ** ** ** ** ** ** Class members
	String org_name;
	String dis_name;

	public KatelloDistributor(String dOrg,String dName){
		this.org_name = dOrg;
		this.dis_name=dName;
	}

	public SSHCommandResult distributor_create(){
		opts.clear();
		opts.add(new Attribute("org", this.org_name));
		opts.add(new Attribute("name", this.dis_name));
		return run(CMD_CREATE);
	}

	public SSHCommandResult distributor_info(){
		opts.clear();
		opts.add(new Attribute("org", this.org_name));
		opts.add(new Attribute("name", this.dis_name));
		return run(CMD_INFO);
	}
	
	public SSHCommandResult add_info(String keyname,String value){
		opts.clear();
		opts.add(new Attribute("org",this.org_name));
		opts.add(new Attribute("name",this.dis_name));
		opts.add(new Attribute("keyname",keyname));
		opts.add(new Attribute("value",value));
		return run(CMD_ADD_CUSTOM_INFO);		
	}
}

