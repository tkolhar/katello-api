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
	public static final String CMD_LIST = "model list";
	public static final String CMD_DELETE = "model delete";
	public static final String CMD_UPDATE = "model update";
	
	public static final String OUT_CREATE = 
			"Hardware model created";
	public static final String OUT_UPDATE = 
			"Hardware model updated";
	public static final String OUT_DELETE = 
			"Hardware Model [ %s ] deleted";
	
	public static final String ERR_NAME_EXISTS = 
			"Name has already been taken";
	public static final String ERR_NOT_FOUND =
			"HardwareModel with id '%s' not found";
	
	public static final String REG_HWM_INFO = ".*Name\\s*:\\s+%s.*Vendor class\\s*:\\s+%s.*HW model\\s*:\\s+%s.*Info\\s*:\\s+%s.*";
	public static final String REG_HWM_LIST = ".*\\s+%s.*\\s+%s.*\\s+%s.*";
	
	// ** ** ** ** ** ** ** Class members
	public String Id;
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
		args.clear();
		args.add(new Attribute("name", this.name));
		args.add(new Attribute("info", this.info));
		args.add(new Attribute("hardware-model", this.hw_model));
		args.add(new Attribute("vendor-class", this.vendor_class));
		return run(CLI_CMD_CREATE);
	}
	
	public SSHCommandResult cli_info(){
		args.clear();
		args.add(new Attribute("name", this.name));
		return run(CLI_CMD_INFO);
	}
	
	public SSHCommandResult cli_list(){
		args.clear();
		return run(CMD_LIST);
	}
	
	public SSHCommandResult cli_list(String searchStr, String order, String page, Integer per_page) {
		args.clear();
		args.add(new Attribute("search", searchStr));
		args.add(new Attribute("order", order));
		args.add(new Attribute("page", page));
		args.add(new Attribute("per-page", per_page));
		return run(CMD_LIST);
	}
	
	public SSHCommandResult update(String new_name){
		args.clear();
		args.add(new Attribute("name", this.name));
		args.add(new Attribute("new-name", new_name));
		args.add(new Attribute("info", this.info));
		args.add(new Attribute("hardware-model", this.hw_model));
		args.add(new Attribute("vendor-class", this.vendor_class));
		return run(CMD_UPDATE);
	}
	
	public SSHCommandResult update(){
		return update(null);
	}

	public SSHCommandResult delete() {
		args.clear();
		if (this.Id != null) {
			args.add(new Attribute("id", this.Id));	
		} else {
			args.add(new Attribute("name", this.name));
		}
		return run(CMD_DELETE);
	}
	
	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **
}
