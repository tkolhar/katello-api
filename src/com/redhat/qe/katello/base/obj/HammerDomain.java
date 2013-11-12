package com.redhat.qe.katello.base.obj;

import java.util.logging.Logger;

import javax.management.Attribute;

import com.redhat.qe.katello.base.threading.KatelloCliWorker;
import com.redhat.qe.tools.SSHCommandResult;

public class HammerDomain extends _HammerObject {
	protected static Logger log = Logger.getLogger(HammerDomain.class.getName());
	
	// ** ** ** ** ** ** ** Public constants
	public static final String CMD_CREATE = "domain create";
	public static final String CMD_UPDATE = "domain update";
	public static final String CMD_DELETE = "domain delete";
	public static final String CMD_LIST = "domain list";
	public static final String CMD_INFO = "domain info";
	public static final String CMD_DELETE_PARAMETER = "domain delete_parameter";
	public static final String CMD_SET_PARAMETER = "domain set_parameter";
	
	public static final String OUT_CREATE = "Domain created";
	public static final String OUT_UPDATE = "Domain updated";
	public static final String OUT_DELETE = "Domain deleted";
	
	public static final String ERR_DUPLICATE_DESCRIPTION = "Could not create the domain:" + "\n" + "  Description has already been taken";
	public static final String ERR_DUPLICATE_NAME = "Could not create the domain:"+ "\n" + "  DNS domain has already been taken";
	
	public static final String REG_DOMAIN_INFO = ".*Name\\s*:\\s+%s.*Full Name\\s*:\\s+%s.*DNS Id\\s*:\\s+%s.*";

	// ** ** ** ** ** ** ** Class members
	public String Id;
	public String name;
	public String fullName;
	public String dns_id;
	
	public HammerDomain(){super();}
	
	public HammerDomain(KatelloCliWorker kcr, String pName)
	{
		this.kcr = kcr;
		this.name = pName;
	}
	
	public HammerDomain(KatelloCliWorker kcr, String pName, String pFullName, String pDnsId)
	{
		this.kcr = kcr;
		this.name = pName;
		this.fullName = pFullName ;
		this.dns_id = pDnsId;
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
		args.add(new Attribute("fullname", this.fullName));
		args.add(new Attribute("dns-id", this.dns_id));
		return run(CMD_CREATE);
	}
	
	public SSHCommandResult cli_info() {
		args.clear();
		args.add(new Attribute("name", this.name));
		return run(CMD_INFO);
	}

	public SSHCommandResult cli_list() {
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
	
	public SSHCommandResult update(String newName) {
		args.clear();
		args.add(new Attribute("name", this.name));
		args.add(new Attribute("fullname", this.fullName));
		args.add(new Attribute("dns-id", this.dns_id));
		args.add(new Attribute("new-name", newName));
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
}
