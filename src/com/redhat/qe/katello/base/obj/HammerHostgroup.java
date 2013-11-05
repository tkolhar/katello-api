package com.redhat.qe.katello.base.obj;

import java.util.logging.Logger;

import javax.management.Attribute;

import com.redhat.qe.katello.base.threading.KatelloCliWorker;
import com.redhat.qe.tools.SSHCommandResult;

public class HammerHostgroup extends _HammerObject{
    protected static Logger log = Logger.getLogger(HammerHostgroup.class.getName());

	// ** ** ** ** ** ** ** Public constants
	public static final String CLI_CMD_CREATE = "hostgroup create";
	public static final String CLI_CMD_INFO = "hostgroup info";
	public static final String CMD_LIST = "hostgroup list";
	public static final String CMD_DELETE = "hostgroup delete";
	public static final String CMD_UPDATE = "hostgroup update";
	public static final String CMD_PUPPET_CLASSES = "hostgroup puppet_classes";
	public static final String CMD_DELETE_PARAM = "hostgroup delete_parameter";
	public static final String CMD_SET_PARAM = "hostgroup set_parameter";
	
	public static final String OUT_CREATE = 
			"Hostgroup created";
	public static final String OUT_UPDATE = 
			"Hostgroup updated";
	public static final String OUT_DELETE = 
			"Hostgroup deleted";
	public static final String OUT_SET_PARAM =
			"New hostgroup parameter created";
	public static final String OUT_UPDATE_PARAM =
			"Hostgroup parameter updated";
	public static final String OUT_DELETE_PARAM =
			"Hostgroup parameter deleted";
	
	public static final String ERR_NAME_EXISTS = 
			"Name has already been taken";
	public static final String ERR_NOT_FOUND =
			"404 Resource Not Found";
	
	public static final String REG_INFO = "Id\\s*:\\s+\\d+.*Name\\s*:\\s+%s.*Label\\s*:\\s+%s.*Operating System Id\\s*:\\s+%s.*Subnet Id\\s*:\\s+%s.*" +
			"Domain Id\\s*:\\s+%s.*Environment Id\\s*:\\s+%s.*Puppetclass Ids\\s*:\\s+%s.*Ancestry\\s*:\\s+%s.*Parameters\\s*:\\s*%s.*";
	
	// ** ** ** ** ** ** ** Class members
	public String Id;
	public String parent_Id;
	public String name;
	public String label;
	public String os_Id;
	public String subnet_Id;
	public String domain_Id;
	public String arch_Id;
	public String env_Id;
	public String medium_Id;
	public String ptable_Id;
	public String puppet_ca_proxy_Id;
	public String puppet_proxy_Id;
	public String puppetclass_Ids;
	public String ancestry;
	public String parameters;
	
	public HammerHostgroup(){super();}
	
	public HammerHostgroup(KatelloCliWorker kcr, String pName){
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
		args.add(new Attribute("parent-id", this.parent_Id));
		args.add(new Attribute("environment-id", this.env_Id));
		args.add(new Attribute("operatingsystem-id", this.os_Id));
		args.add(new Attribute("architecture-id", this.arch_Id));
		args.add(new Attribute("medium-id", this.medium_Id));
		args.add(new Attribute("ptable-id", this.ptable_Id));
		args.add(new Attribute("puppet-ca-proxy-id", this.puppet_ca_proxy_Id));
		args.add(new Attribute("subnet-id", this.subnet_Id));
		args.add(new Attribute("domain-id", this.domain_Id));
		args.add(new Attribute("puppet-proxy-id", this.puppet_proxy_Id));
		return run(CLI_CMD_CREATE);
	}
	
	public SSHCommandResult cli_info(){
		args.clear();
		args.add(new Attribute("id", this.Id));
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
	
	public SSHCommandResult update(){
		args.clear();
		args.add(new Attribute("id", this.Id));
		args.add(new Attribute("parent-id", this.parent_Id));
		args.add(new Attribute("environment-id", this.env_Id));
		args.add(new Attribute("operatingsystem-id", this.os_Id));
		args.add(new Attribute("architecture-id", this.arch_Id));
		args.add(new Attribute("medium-id", this.medium_Id));
		args.add(new Attribute("ptable-id", this.ptable_Id));
		args.add(new Attribute("puppet-ca-proxy-id", this.puppet_ca_proxy_Id));
		args.add(new Attribute("subnet-id", this.subnet_Id));
		args.add(new Attribute("domain-id", this.domain_Id));
		args.add(new Attribute("puppet-proxy-id", this.puppet_proxy_Id));
		return run(CMD_UPDATE);
	}
	
	public SSHCommandResult puppet_classes(String nested_env_id, String nested_hostgroup_id, String nested_host_id,
			String searchStr, String order, String page, Integer per_page) {
		args.clear();
		args.add(new Attribute("id", this.Id));	
		args.add(new Attribute("search", searchStr));
		args.add(new Attribute("order", order));
		args.add(new Attribute("page", page));
		args.add(new Attribute("per-page", per_page));
		args.add(new Attribute("environment-id", nested_env_id));
		args.add(new Attribute("hostgroup-id", nested_hostgroup_id));
		args.add(new Attribute("host-id", nested_host_id));
		return run(CMD_PUPPET_CLASSES);
	}
	
	public SSHCommandResult set_parameter(String pname, String pvalue) {
		args.clear();
		args.add(new Attribute("hostgroup-id", this.Id));
		args.add(new Attribute("name", pname));
		args.add(new Attribute("value", pvalue));
		return run(CMD_SET_PARAM);
	}
	
	public SSHCommandResult delete_parameter(String pname) {
		args.clear();
		args.add(new Attribute("hostgroup-id", this.Id));
		args.add(new Attribute("name", pname));
		return run(CMD_DELETE_PARAM);
	}
	
	public SSHCommandResult delete() {
		args.clear();
		args.add(new Attribute("id", this.Id));	
		return run(CMD_DELETE);
	}

	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **
}
