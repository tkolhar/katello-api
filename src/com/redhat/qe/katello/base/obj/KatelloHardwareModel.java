package com.redhat.qe.katello.base.obj;

import java.util.logging.Logger;

import javax.management.Attribute;

import com.redhat.qe.tools.SSHCommandResult;

public class KatelloHardwareModel extends _KatelloObject{
    protected static Logger log = Logger.getLogger(KatelloHardwareModel.class.getName());

	// ** ** ** ** ** ** ** Public constants
	public static final String CLI_CMD_CREATE = "hw_model create";
	public static final String CLI_CMD_INFO = "hw_model info";
	public static final String CLI_CMD_LIST = "hw_model list";
	public static final String CMD_DELETE = "hw_model delete";
	public static final String CMD_UPDATE = "hw_model update";
	
	public static final String OUT_CREATE = 
			"Hardware Model [ %s ] created";
	public static final String OUT_UPDATE = 
			"Hardware Model [ %s ] updated";
	public static final String OUT_DELETE = 
			"Hardware Model [ %s ] deleted";
	
	public static final String ERR_NAME_EXISTS = 
			"Name has already been taken";
	public static final String ERR_NOT_FOUND =
			"HardwareModel with id '%s' not found";
	
	public static final String REG_HWM_INFO = ".*Name\\s*:\\s+%s.*Info\\s*:\\s*%s.+Vendor class\\s*:\\s*%s.*HW model\\s*:\\s+%s.*";
	public static final String REG_HWM_LIST = ".*\\s+%s.*\\s+%s.*\\s+%s.*";
	
	// ** ** ** ** ** ** ** Class members
	public String name;
	public String hw_model;
	public String info;
	public String vendor_class;
	
	public KatelloHardwareModel(){super();}
	
	public KatelloHardwareModel(String pName){
		this.name = pName;
	}
	
	public KatelloHardwareModel(String pName, String phwModel, String pinfo, String pvendorClass){
		this.name = pName;
		this.hw_model = phwModel;
		this.info = pinfo;
		this.vendor_class = pvendorClass;
	}
	
	public String getName() {
	    return name;
	}
	
	public void setName(String name) {
	    this.name = name;
	}

	public SSHCommandResult cli_create(){		
		opts.clear();
		opts.add(new Attribute("name", this.name));
		opts.add(new Attribute("info", this.info));
		opts.add(new Attribute("hw_model", this.hw_model));
		opts.add(new Attribute("vendor_class", this.vendor_class));
		return run(CLI_CMD_CREATE);
	}
	
	public SSHCommandResult cli_info(){
		opts.clear();
		opts.add(new Attribute("name", this.name));
		return run(CLI_CMD_INFO);
	}
	
	public SSHCommandResult cli_list(){
		opts.clear();
		return run(CLI_CMD_LIST+" -v");
	}

	public SSHCommandResult delete(){
		opts.clear();
		opts.add(new Attribute("name", this.name));
		return run(CMD_DELETE);
	}
	
	public SSHCommandResult update(String new_name){
		opts.clear();
		opts.add(new Attribute("name", this.name));
		opts.add(new Attribute("new_name", new_name));
		opts.add(new Attribute("info", this.info));
		opts.add(new Attribute("hw_model", this.hw_model));
		opts.add(new Attribute("vendor_class", this.vendor_class));
		return run(CMD_UPDATE);
	}
	
	public SSHCommandResult update(){
		return update(null);
	}


	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **
}
