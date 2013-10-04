package com.redhat.qe.katello.base.obj;

import java.util.logging.Logger;

import javax.management.Attribute;

import com.redhat.qe.katello.base.threading.KatelloCliWorker;
import com.redhat.qe.tools.SSHCommandResult;

public class HammerHardwareModel extends _HammerObject {
    protected static Logger log = Logger.getLogger(HammerHardwareModel.class.getName());

	// ** ** ** ** ** ** ** Public constants
	public static final String CLI_CMD_CREATE = "model create";
	public static final String CLI_CMD_INFO = "model info";
	public static final String CLI_CMD_LIST = "model list";
	public static final String CMD_DELETE = "model delete";
	public static final String CMD_UPDATE = "model update";
	
	public static final String OUT_CREATE = 
			"Hardware model created";
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
	
	public HammerHardwareModel(){super();}
	
	public HammerHardwareModel(KatelloCliWorker kcr, String pName){
		this.name = pName;
		this.kcr = kcr;
	}
	
	public HammerHardwareModel(KatelloCliWorker kcr, String pName, String phwModel, String pinfo, String pvendorClass){
		this.name = pName;
		this.hw_model = phwModel;
		this.info = pinfo;
		this.vendor_class = pvendorClass;
		this.kcr = kcr;
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
		opts.add(new Attribute("hardware-model", this.hw_model));
		opts.add(new Attribute("vendor-class", this.vendor_class));
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
		opts.add(new Attribute("hardware-model", this.hw_model));
		opts.add(new Attribute("vendor-class", this.vendor_class));
		return run(CMD_UPDATE);
	}
	
	public SSHCommandResult update(){
		return update(null);
	}


	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **
}
