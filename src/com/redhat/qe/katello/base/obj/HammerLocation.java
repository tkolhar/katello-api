package com.redhat.qe.katello.base.obj;

import java.util.logging.Logger;

import javax.management.Attribute;

import com.redhat.qe.katello.base.threading.KatelloCliWorker;
import com.redhat.qe.tools.SSHCommandResult;

public class HammerLocation extends _HammerObject{
    protected static Logger log = Logger.getLogger(HammerLocation.class.getName());

    public enum OsFamily {Archlinux, Debian, Gentoo, Redhat, Solaris, Suse, Windows};
    
	// ** ** ** ** ** ** ** Public constants
	public static final String CLI_CMD_CREATE = "location create";
	public static final String CLI_CMD_INFO = "location info";
	public static final String CMD_LIST = "location list";
	public static final String CMD_DELETE = "location delete";
	public static final String CMD_UPDATE = "location update";

	public static final String CMD_REMOVE_ENV = "location remove_environment";
	public static final String CMD_ADD_ENV = "location add_environment";
	public static final String CMD_REMOVE_SUBNET = "location remove_subnet";
	public static final String CMD_ADD_SUBNET = "location add_subnet";
	public static final String CMD_REMOVE_ORG = "location remove_organization";
	public static final String CMD_ADD_ORG = "location add_organization";
	public static final String CMD_REMOVE_DOMAIN = "location remove_domain";
	public static final String CMD_ADD_DOMAIN = "location add_domain";
	public static final String CMD_REMOVE_USER = "location remove_user";
	public static final String CMD_ADD_USER = "location add_user";
	public static final String CMD_REMOVE_HOSTGROUP = "location remove_hostgroup";
	public static final String CMD_ADD_HOSTGROUP = "location add_hostgroup";
	public static final String CMD_REMOVE_PROXY = "location remove_smartproxy";
	public static final String CMD_ADD_PROXY = "location add_smartproxy";
	public static final String CMD_REMOVE_COMPUTERES = "location remove_computeresource";
	public static final String CMD_ADD_COMPUTERES = "location add_computeresource";
	public static final String CMD_REMOVE_MEDIUM = "location remove_medium";
	public static final String CMD_ADD_MEDIUM = "location add_medium";
	public static final String CMD_REMOVE_CONFIG = "location remove_configtemplate";
	public static final String CMD_ADD_CONFIG = "location add_configtemplate";
	
	public static final String OUT_CREATE = 
			"Location created";
	public static final String OUT_UPDATE = 
			"Location updated";
	public static final String OUT_DELETE = 
			"Location deleted";
	
	public static final String ERR_NAME_EXISTS = 
			"Name has already been taken";
	public static final String ERR_NOT_FOUND =
			"404 Resource Not Found";

	// ** ** ** ** ** ** ** Class members
	public String Id;
	public String name;
	
	public HammerLocation(){super();}

	public HammerLocation(KatelloCliWorker kcr, String pName) {
		this.name = pName;
		this.kcr = kcr;
	}

	
	public String getName() {
	    return name;
	}
	
	public void setName(String name) {
	    this.name = name;
	}

	public SSHCommandResult cli_create() {
		args.clear();
		args.add(new Attribute("name", this.name));
		return run(CLI_CMD_CREATE);
	}
	
	public SSHCommandResult cli_info() {
		args.clear();
		if (this.Id != null) {
			args.add(new Attribute("id", this.Id));	
		} else {
			args.add(new Attribute("name", this.name));
		}
		return run(CLI_CMD_INFO);
	}
	
	public SSHCommandResult cli_list() {
		args.clear();
		return run(CMD_LIST);
	}

	public SSHCommandResult cli_search(String search) {
		args.clear();
		args.add(new Attribute("search", search));
		return run(CMD_LIST);
	}
	
	public SSHCommandResult cli_list(String order, Integer page, Integer per_page) {
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
	
	public SSHCommandResult update(String new_name) {
		args.clear();
		if (this.Id != null) {
			args.add(new Attribute("id", this.Id));	
		} else {
			args.add(new Attribute("name", this.name));
		}
		args.add(new Attribute("new-name", new_name));
		return run(CMD_UPDATE);
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
