package com.redhat.qe.katello.base.obj;

import java.util.logging.Logger;

import javax.management.Attribute;

import com.redhat.qe.katello.base.threading.KatelloCliWorker;
import com.redhat.qe.tools.SSHCommandResult;

public class HammerHost extends _HammerObject{
    protected static Logger log = Logger.getLogger(HammerHost.class.getName());

	// ** ** ** ** ** ** ** Public constants
	public static final String CLI_CMD_CREATE = "host create";
	public static final String CLI_CMD_INFO = "host info";
	public static final String CMD_LIST = "host list";
	public static final String CMD_DELETE = "host delete";
	public static final String CMD_UPDATE = "host update";
	public static final String CMD_PUPPETRUN = "host puppetrun";
	public static final String CMD_STATUS = "host status";
	public static final String CMD_PUPPET_CLASSES = "host puppet_classes";
	public static final String CMD_REPORTS = "host reports";
	public static final String CMD_FACTS = "host facts";
	public static final String CMD_DELETE_PARAM = "host delete_parameter";
	public static final String CMD_SET_PARAM = "host set_parameter";	
	
	public static final String OUT_CREATE = 
			"Host created";
	public static final String OUT_UPDATE = 
			"Host [ %s ] updated.";
	public static final String OUT_DELETE = 
			"Host [ %s ] deleted.";
	
	public static final String ERR_NAME_EXISTS = 
			"Name has already been taken";
	public static final String ERR_NOT_FOUND =
			"Host with id '%s' not found";
	
	// ** ** ** ** ** ** ** Class members
	public String Id;
	public String name;
	public String environment_id;
	public String ip;
	public String mac;
	public String architecture_id;
	public String domain_id;
	public String puppet_proxy_id;
	public String puppet_class_ids;
	public String operatingsystem_id;
	public String medium_id;
	public String ptable_id;
	public String subnet_id;
	public String compute_resource_id;
	public String sp_subnet_id;
	public String model_id;
	public String hostgroup_id;
	public String owner_id;
	public String puppet_ca_proxy_id;
	public String image_id;
	public String build;
	public String enabled;
	public String provision_method;
	public String managed;
	public String capabilities;
	public String flavour_ref;
	public String image_ref;
	public String tenant_id;
	public String security_groups;
	public String network;
	public String cpus;
	public String memory;
	public String start;
	public String provider;
	public String type;
	
	public HammerHost(){super();}
	
	public HammerHost(KatelloCliWorker kcr, String pName, String environment_id, String architecture_id,
			String domain_id, String puppet_proxy_id, String operatingsystem_id, String ip, String mac, String ptable_id){
		this.name = pName;
		this.kcr = kcr;
		this.environment_id = environment_id;
		this.ip = ip;
		this.mac = mac;
		this.architecture_id = architecture_id;
		this.domain_id = domain_id;
		this.puppet_proxy_id = puppet_proxy_id;
		this.operatingsystem_id = operatingsystem_id;
		this.ptable_id = ptable_id;
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
		args.add(new Attribute("environment-id", this.environment_id));
		args.add(new Attribute("ip", this.ip));
		args.add(new Attribute("mac", this.mac));
		args.add(new Attribute("architecture-id", this.architecture_id));
		args.add(new Attribute("domain-id", this.domain_id));
		args.add(new Attribute("puppet-proxy-id", this.puppet_proxy_id));
		args.add(new Attribute("puppet-class-ids", this.puppet_class_ids));
		args.add(new Attribute("operatingsystem-id", this.operatingsystem_id));
		args.add(new Attribute("medium-id", this.medium_id));
		args.add(new Attribute("ptable-id", this.ptable_id));
		args.add(new Attribute("subnet-id", this.subnet_id));
		args.add(new Attribute("compute-resource-id", this.compute_resource_id));
		args.add(new Attribute("sp-subnet-id", this.sp_subnet_id));
		args.add(new Attribute("model-id", this.model_id));
		args.add(new Attribute("hostgroup-id", this.hostgroup_id));
		args.add(new Attribute("owner-id", this.owner_id));
		args.add(new Attribute("puppet-ca-proxy-id", this.puppet_ca_proxy_id));
		args.add(new Attribute("image-id", this.image_id));
		args.add(new Attribute("build", this.build));
		args.add(new Attribute("enabled", this.enabled));
		args.add(new Attribute("provision-method", this.provision_method));
		args.add(new Attribute("managed", this.managed));
		args.add(new Attribute("capabilities", this.capabilities));
		args.add(new Attribute("flavour-ref", this.flavour_ref));
		args.add(new Attribute("image-ref", this.image_ref));
		args.add(new Attribute("tenant-id", this.tenant_id));
		args.add(new Attribute("security-groups", this.security_groups));
		args.add(new Attribute("network", this.network));
		args.add(new Attribute("cpus", this.cpus));
		args.add(new Attribute("memory", this.memory));
		args.add(new Attribute("start", this.start));
		args.add(new Attribute("provider", this.provider));
		args.add(new Attribute("type", this.type));
		
		return run(CLI_CMD_CREATE);
	}
	
	public SSHCommandResult cli_info(){
		args.clear();
		if (this.Id != null) {
			args.add(new Attribute("id", this.Id));	
		} else {
			args.add(new Attribute("name", this.name));
		}
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
	
	public SSHCommandResult update(String new_name){
		args.clear();
		if (this.Id != null) {
			args.add(new Attribute("id", this.Id));	
		} else {
			args.add(new Attribute("name", this.name));
		}
		args.add(new Attribute("new-name", new_name));
		args.add(new Attribute("environment-id", this.environment_id));
		args.add(new Attribute("ip", this.ip));
		args.add(new Attribute("mac", this.mac));
		args.add(new Attribute("architecture-id", this.architecture_id));
		args.add(new Attribute("domain-id", this.domain_id));
		args.add(new Attribute("puppet-proxy-id", this.puppet_proxy_id));
		args.add(new Attribute("puppet-class-ids", this.puppet_class_ids));
		args.add(new Attribute("operatingsystem-id", this.operatingsystem_id));
		args.add(new Attribute("medium-id", this.medium_id));
		args.add(new Attribute("ptable-id", this.ptable_id));
		args.add(new Attribute("subnet-id", this.subnet_id));
		args.add(new Attribute("compute-resource-id", this.compute_resource_id));
		args.add(new Attribute("sp-subnet-id", this.sp_subnet_id));
		args.add(new Attribute("model-id", this.model_id));
		args.add(new Attribute("hostgroup-id", this.hostgroup_id));
		args.add(new Attribute("owner-id", this.owner_id));
		args.add(new Attribute("puppet-ca-proxy-id", this.puppet_ca_proxy_id));
		args.add(new Attribute("image-id", this.image_id));
		args.add(new Attribute("build", this.build));
		args.add(new Attribute("enabled", this.enabled));
		args.add(new Attribute("provision-method", this.provision_method));
		args.add(new Attribute("managed", this.managed));
		args.add(new Attribute("capabilities", this.capabilities));
		args.add(new Attribute("flavour-ref", this.flavour_ref));
		args.add(new Attribute("image-ref", this.image_ref));
		args.add(new Attribute("tenant-id", this.tenant_id));
		args.add(new Attribute("security-groups", this.security_groups));
		args.add(new Attribute("network", this.network));
		args.add(new Attribute("cpus", this.cpus));
		args.add(new Attribute("memory", this.memory));
		args.add(new Attribute("start", this.start));
		args.add(new Attribute("provider", this.provider));
		args.add(new Attribute("type", this.type));
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

	public SSHCommandResult puppetrun() {
		args.clear();
		if (this.Id != null) {
			args.add(new Attribute("id", this.Id));	
		} else {
			args.add(new Attribute("name", this.name));
		}
		return run(CMD_PUPPETRUN);
	}

	public SSHCommandResult status() {
		args.clear();
		if (this.Id != null) {
			args.add(new Attribute("id", this.Id));	
		} else {
			args.add(new Attribute("name", this.name));
		}
		return run(CMD_STATUS);
	}

	public SSHCommandResult puppet_classes(String nested_env_id, String nested_hostgroup_id, String nested_host_id,
			String searchStr, String order, String page, Integer per_page) {
		args.clear();
		if (this.Id != null) {
			args.add(new Attribute("id", this.Id));	
		} else {
			args.add(new Attribute("name", this.name));
		}
		args.add(new Attribute("search", searchStr));
		args.add(new Attribute("order", order));
		args.add(new Attribute("page", page));
		args.add(new Attribute("per-page", per_page));
		args.add(new Attribute("environment-id", nested_env_id));
		args.add(new Attribute("hostgroup-id", nested_hostgroup_id));
		args.add(new Attribute("host-id", nested_host_id));
		return run(CMD_PUPPET_CLASSES);
	}
	
	public SSHCommandResult reports(String order, String page, Integer per_page) {
		args.clear();
		if (this.Id != null) {
			args.add(new Attribute("id", this.Id));	
		} else {
			args.add(new Attribute("name", this.name));
		}
		args.add(new Attribute("order", order));
		args.add(new Attribute("page", page));
		args.add(new Attribute("per-page", per_page));
		return run(CMD_REPORTS);
	}

	public SSHCommandResult facts(String searchStr, String order, String page, Integer per_page) {
		args.clear();
		if (this.Id != null) {
			args.add(new Attribute("id", this.Id));	
		} else {
			args.add(new Attribute("name", this.name));
		}
		args.add(new Attribute("search", searchStr));
		args.add(new Attribute("order", order));
		args.add(new Attribute("page", page));
		args.add(new Attribute("per-page", per_page));
		return run(CMD_FACTS);
	}

	public SSHCommandResult set_parameter(String pname, String pvalue) {
		args.clear();
		if (this.Id != null) {
			args.add(new Attribute("host-id", this.Id));	
		} else {
			args.add(new Attribute("host-name", this.name));
		}
		args.add(new Attribute("name", pname));
		args.add(new Attribute("value", pvalue));
		return run(CMD_SET_PARAM);
	}
	
	public SSHCommandResult delete_parameter(String pname) {
		args.clear();
		if (this.Id != null) {
			args.add(new Attribute("host-id", this.Id));	
		} else {
			args.add(new Attribute("host-name", this.name));
		}
		args.add(new Attribute("name", pname));
		return run(CMD_DELETE_PARAM);
	}
	
	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **
}
