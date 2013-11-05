package com.redhat.qe.katello.base.obj;

import java.util.logging.Logger;

import javax.management.Attribute;

import com.redhat.qe.katello.base.threading.KatelloCliWorker;
import com.redhat.qe.tools.SSHCommandResult;

public class HammerOs extends _HammerObject {
	protected static Logger log = Logger.getLogger(HammerOs.class.getName());
	
	// ** ** ** ** ** ** ** Public constants
	public static final String CMD_CREATE = "os create";
	public static final String CMD_UPDATE = "os update";
	public static final String CMD_DELETE = "os delete";
	public static final String CMD_LIST = "os list";
	public static final String CMD_INFO = "os info";
	public static final String CMD_DELETE_PARAMETER = "os delete_parameter";
	public static final String CMD_SET_PARAMETER = "os set_parameter";
	public static final String CMD_REMOVE_PTABLE = "os remove_ptable";
	public static final String CMD_ADD_PTABLE = "os add_ptable";
	public static final String CMD_REMOVE_ARCH = "os remove_architecture";
	public static final String CMD_ADD_ARCH = "os add_architecture";
	public static final String CMD_REMOVE_CONFIG = "os remove_configtemplate";
	public static final String CMD_ADD_CONFIG = "os add_configtemplate";
	
	public static final String OUT_CREATE = "Operating system created";
	public static final String OUT_UPDATE = "Operating system updated";
	public static final String OUT_DELETE = "Operating system deleted";
	public static final String OUT_SET_PARAM =
			"New os parameter created";
	public static final String OUT_UPDATE_PARAM =
			"Os parameter updated";
	public static final String OUT_DELETE_PARAM =
			"Os parameter deleted";
	
	public static final String ERR_CREATE = "Could not create the Operating system:";
	public static final String ERR_NOT_FOUND =
			"404 Resource Not Found";
	
	public static final String REG_OS_INFO = "Id\\s*:\\s+\\d+.*Name\\s*:\\s+%s.*Release name\\s*:\\s+%s.*Family\\s*:\\s+%s.*Installation media\\s*:\\s+%s" +
			".*Architectures\\s*:\\s+%s.*Partition tables\\s*:\\s+%s.*Config templates\\s*:\\s+%s.*Parameters\\s*:\\s*%s";

	// ** ** ** ** ** ** ** Class members
	public String Id;
	public String name;
	public String label;
	public String relName;
	public String arch_ids;
	public String config_ids;
	public String medium_ids;
	public String ptable_ids;
	public String major;
	public String minor;
	public String family;
	public String parameters;
	public String installMedia;
	
	public HammerOs(){super();}
	
	public HammerOs(KatelloCliWorker kcr, String pId)
	{
		this.kcr = kcr;
		this.Id = pId;
	}
	
	public HammerOs(KatelloCliWorker kcr, String pName, String pmajor, String pminor)
	{
		this.kcr = kcr;
		this.name = pName;
		this.major = pmajor;
		this.minor = pminor;
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
		args.add(new Attribute("major", this.major));
		args.add(new Attribute("minor", this.minor));
		args.add(new Attribute("architecture-ids", this.arch_ids));
		args.add(new Attribute("config-template-ids", this.config_ids));
		args.add(new Attribute("medium-ids", this.medium_ids));
		args.add(new Attribute("ptable-ids", this.ptable_ids));
		args.add(new Attribute("family", this.family));
		args.add(new Attribute("release-name", this.relName));
		return run(CMD_CREATE);
	}
	
	public SSHCommandResult cli_info(String label) {
		args.clear();
		if (this.Id != null) {
			args.add(new Attribute("id", this.Id));
		} else {
			args.add(new Attribute("label", label));
		}
		return run(CMD_INFO);
	}

	public SSHCommandResult cli_search(String search){
		args.clear();
		args.add(new Attribute("search", search));
		return run(CMD_LIST);
	}
	
	public SSHCommandResult cli_list() {
		args.clear();
		return run(CMD_LIST);
	}
	
	public SSHCommandResult cli_list(String order, Integer page, Integer per_page) {
		args.clear();
		args.add(new Attribute("order", order));
		args.add(new Attribute("page", page));
		args.add(new Attribute("per-page", per_page));
		return run(CMD_LIST);
	}
	
	public SSHCommandResult update() {
		args.clear();
		if (this.Id != null) {
			args.add(new Attribute("id", this.Id));	
		} else {
			args.add(new Attribute("name", this.name));
		}
		args.add(new Attribute("label", this.label));
		args.add(new Attribute("major", this.major));
		args.add(new Attribute("minor", this.minor));
		args.add(new Attribute("architecture-ids", this.arch_ids));
		args.add(new Attribute("config-template-ids", this.config_ids));
		args.add(new Attribute("medium-ids", this.medium_ids));
		args.add(new Attribute("ptable-ids", this.ptable_ids));
		args.add(new Attribute("family", this.family));
		args.add(new Attribute("release-name", this.relName));
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
	
	public SSHCommandResult set_parameter(String pname, String pvalue) {
		args.clear();
		args.add(new Attribute("os-id", Id));
		args.add(new Attribute("name", pname));
		args.add(new Attribute("value", pvalue));
		return run(CMD_SET_PARAMETER);
	}
	
	public SSHCommandResult delete_parameter(String pname) {
		args.clear();
		args.add(new Attribute("os-id", Id));
		args.add(new Attribute("name", pname));
		return run(CMD_DELETE_PARAMETER);
	}
	
	public SSHCommandResult add_config(String configtemplate) {
		args.clear();
		if (this.Id != null) {
			args.add(new Attribute("id", this.Id));	
		} else {
			args.add(new Attribute("name", this.name));
		}
		args.add(new Attribute("configtemplate", configtemplate));
		return run(CMD_ADD_CONFIG);
	}

	public SSHCommandResult remove_config(String configtemplate) {
		args.clear();
		if (this.Id != null) {
			args.add(new Attribute("id", this.Id));	
		} else {
			args.add(new Attribute("name", this.name));
		}
		args.add(new Attribute("configtemplate", configtemplate));
		return run(CMD_REMOVE_CONFIG);
	}
	
	public SSHCommandResult add_architecture(String architecture) {
		args.clear();
		if (this.Id != null) {
			args.add(new Attribute("id", this.Id));	
		} else {
			args.add(new Attribute("name", this.name));
		}
		args.add(new Attribute("architecture", architecture));
		return run(CMD_ADD_ARCH);
	}

	public SSHCommandResult remove_architecture(String architecture) {
		args.clear();
		if (this.Id != null) {
			args.add(new Attribute("id", this.Id));	
		} else {
			args.add(new Attribute("name", this.name));
		}
		args.add(new Attribute("architecture", architecture));
		return run(CMD_REMOVE_ARCH);
	}
	
}
