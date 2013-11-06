package com.redhat.qe.katello.base.obj;

import java.util.logging.Logger;

import javax.management.Attribute;

import com.redhat.qe.katello.base.threading.KatelloCliWorker;
import com.redhat.qe.tools.SSHCommandResult;

public class HammerTemplate extends _HammerObject{
    protected static Logger log = Logger.getLogger(HammerTemplate.class.getName());

    public enum TemplateType {script, snippet, provision, PXELinux, gPXE};
    
	// ** ** ** ** ** ** ** Public constants
	public static final String CLI_CMD_CREATE = "template create";
	public static final String CLI_CMD_INFO = "template info";
	public static final String CMD_LIST = "template list";
	public static final String CMD_DELETE = "template delete";
	public static final String CMD_UPDATE = "template update";
	public static final String CMD_DUMP = "template dump";
	public static final String CMD_REMOVEOS = "template remove_operatingsystem";
	public static final String CMD_ADDOS = "template add_operatingsystem";
	public static final String CMD_KINDS = "template kinds";
	
	public static final String OUT_CREATE = 
			"Config template created";
	public static final String OUT_UPDATE = 
			"Config template updated";
	public static final String OUT_DELETE = 
			"Config template deleted";
	
	public static final String ERR_NAME_EXISTS = 
			"Name has already been taken";
	public static final String ERR_NOT_FOUND =
			"Config template with id '%s' not found";
	
	// ** ** ** ** ** ** ** Class members
	public String Id;
	public String name;
	public String type;
	public String operatingsystem_ids;
	
	public HammerTemplate(){super();}
	
	public HammerTemplate(KatelloCliWorker kcr, String pName, TemplateType ptype){
		this.name = pName;
		this.kcr = kcr;
		if (ptype != null) type = ptype.toString();
	}
	
	public String getName() {
	    return name;
	}
	
	public void setName(String name) {
	    this.name = name;
	}

	public SSHCommandResult cli_create(String file){		
		args.clear();
		args.add(new Attribute("name", this.name));
		args.add(new Attribute("operatingsystem-ids", this.operatingsystem_ids));
		args.add(new Attribute("file", file));
		args.add(new Attribute("type", type));
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

	public SSHCommandResult cli_search(String search){
		args.clear();
		args.add(new Attribute("search", search));
		return run(CMD_LIST);
	}
	
	public SSHCommandResult cli_list(String order, Integer page, Integer per_page){
		args.clear();
		args.add(new Attribute("order", order));
		args.add(new Attribute("page", page));
		args.add(new Attribute("per-page", per_page));
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
	
	public SSHCommandResult update(String file){
		args.clear();
		if (this.Id != null) {
			args.add(new Attribute("id", this.Id));	
		} else {
			args.add(new Attribute("name", this.name));
		}
		args.add(new Attribute("operatingsystem-ids", this.operatingsystem_ids));
		args.add(new Attribute("file", file));
		args.add(new Attribute("type", type));
		return run(CMD_UPDATE);
	}

	public SSHCommandResult add_os(String os_id){
		args.clear();
		if (this.Id != null) {
			args.add(new Attribute("id", this.Id));	
		} else {
			args.add(new Attribute("name", this.name));
		}
		args.add(new Attribute("operatingsystem-id", os_id));
		return run(CMD_ADDOS);
	}

	public SSHCommandResult remove_os(String os_id){
		args.clear();
		if (this.Id != null) {
			args.add(new Attribute("id", this.Id));	
		} else {
			args.add(new Attribute("name", this.name));
		}
		args.add(new Attribute("operatingsystem-id", os_id));
		return run(CMD_REMOVEOS);
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
