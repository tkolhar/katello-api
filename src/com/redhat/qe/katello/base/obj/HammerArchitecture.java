package com.redhat.qe.katello.base.obj;

import java.util.logging.Logger;

import javax.management.Attribute;

import com.redhat.qe.katello.base.threading.KatelloCliWorker;
import com.redhat.qe.tools.SSHCommandResult;

public class HammerArchitecture extends _HammerObject{
    protected static Logger log = Logger.getLogger(HammerArchitecture.class.getName());

	// ** ** ** ** ** ** ** Public constants
	public static final String CLI_CMD_CREATE = "architecture create";
	public static final String CLI_CMD_INFO = "architecture info";
	public static final String CLI_CMD_LIST = "architecture list";
	public static final String CMD_DELETE = "architecture delete";
	public static final String CMD_UPDATE = "architecture update";
	
	public static final String OUT_CREATE = 
			"Architecture created";
	public static final String OUT_UPDATE = 
			"Architecture [ %s ] updated.";
	public static final String OUT_DELETE = 
			"Architecture [ %s ] deleted.";
	
	public static final String ERR_NAME_EXISTS = 
			"Name has already been taken";
	public static final String ERR_NOT_FOUND =
			"Architecture with id '%s' not found";
	
	// ** ** ** ** ** ** ** Class members
	public String name;
	
	public HammerArchitecture(){super();}
	
	public HammerArchitecture(KatelloCliWorker kcr, String pName){
		this.name = pName;
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
		return run(CLI_CMD_CREATE);
	}
	
	public SSHCommandResult cli_info(){
		args.clear();
		args.add(new Attribute("name", this.name));
		return run(CLI_CMD_INFO);
	}
	
	public SSHCommandResult cli_list(){
		args.clear();
		return run(CLI_CMD_LIST);
	}

	public SSHCommandResult cli_search(String search){
		args.clear();
		args.add(new Attribute("search", search));
		return run(CLI_CMD_LIST);
	}
	
	public SSHCommandResult cli_list(String order, Integer page, Integer per_page){
		args.clear();
		args.add(new Attribute("order", order));
		args.add(new Attribute("page", page));
		args.add(new Attribute("per-page", per_page));
		return run(CLI_CMD_LIST);
	}
	
	public SSHCommandResult delete(){
		args.clear();
		args.add(new Attribute("name", this.name));
		return run(CMD_DELETE);
	}
	
	public SSHCommandResult update(String new_name){
		args.clear();
		args.add(new Attribute("name", this.name));
		args.add(new Attribute("new-name", new_name));
		return run(CMD_UPDATE);
	}


	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **
}
