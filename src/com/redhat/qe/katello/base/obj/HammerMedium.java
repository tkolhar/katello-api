package com.redhat.qe.katello.base.obj;

import java.util.logging.Logger;

import javax.management.Attribute;

import com.redhat.qe.katello.base.threading.KatelloCliWorker;
import com.redhat.qe.tools.SSHCommandResult;

public class HammerMedium extends _HammerObject{
    protected static Logger log = Logger.getLogger(HammerMedium.class.getName());

    public enum OsFamily {Archlinux, Debian, Gentoo, Redhat, Solaris, Suse, Windows};
    
	// ** ** ** ** ** ** ** Public constants
	public static final String CLI_CMD_CREATE = "medium create";
	public static final String CLI_CMD_INFO = "medium info";
	public static final String CMD_LIST = "medium list";
	public static final String CMD_DELETE = "medium delete";
	public static final String CMD_UPDATE = "medium update";
	public static final String CMD_REMOVEOS = "medium remove_operatingsystem";
	public static final String CMD_ADDOS = "medium add_operatingsystem";
	
	public static final String OUT_CREATE = 
			"Installation medium created";
	public static final String OUT_UPDATE = 
			"Installation medium updated";
	public static final String OUT_DELETE = 
			"Installation medium deleted";
	
	public static final String ERR_NAME_EXISTS = 
			"Name has already been taken";
	public static final String ERR_PATH_EXISTS = 
			"Path has already been taken";
	public static final String ERR_NOT_FOUND =
			"404 Resource Not Found";
	
	public static final String REG_INFO = "Id\\s*:\\s+.*Name\\s*:\\s+%s.*Path\\s*:\\s+%s.*OS Family\\s*:\\s+%s.*OS IDs\\s*:\\s+%s.*";
	
	// ** ** ** ** ** ** ** Class members
	public String Id;
	public String name;
	public String path;
	public String os_family;
	public String operatingsystem_ids;
	
	public HammerMedium(){super();}

	public HammerMedium(KatelloCliWorker kcr, String pName) {
		this.name = pName;
		this.kcr = kcr;
	}
	
	public HammerMedium(KatelloCliWorker kcr, String pName, OsFamily pOs_family, String path) {
		this.name = pName;
		this.kcr = kcr;
		if (pOs_family != null) os_family = pOs_family.toString();
		this.path = path;
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
		args.add(new Attribute("operatingsystem-ids", this.operatingsystem_ids));
		args.add(new Attribute("path", path));
		args.add(new Attribute("os-family", os_family));
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
	
	public SSHCommandResult update(String new_name, String new_path) {
		args.clear();
		if (this.Id != null) {
			args.add(new Attribute("id", this.Id));	
		} else {
			args.add(new Attribute("name", this.name));
		}
		args.add(new Attribute("new-name", new_name));
		args.add(new Attribute("path", new_path));
		args.add(new Attribute("operatingsystem-ids", this.operatingsystem_ids));
		args.add(new Attribute("os-family", os_family));
		return run(CMD_UPDATE);
	}

	public SSHCommandResult add_os(String os_id) {
		args.clear();
		if (this.Id != null) {
			args.add(new Attribute("id", this.Id));	
		} else {
			args.add(new Attribute("name", this.name));
		}
		args.add(new Attribute("operatingsystem-id", os_id));
		return run(CMD_ADDOS);
	}

	public SSHCommandResult remove_os(String os_id) {
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
