package com.redhat.qe.katello.base.obj;

import javax.management.Attribute;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloSystemGroup extends _KatelloObject{
	
	// ** ** ** ** ** ** ** Class members
	public String name;
	public String org;
	public String description;
	public int totalSystems = 0;
	public Integer maxSystems;
	
	public static final String CMD_CREATE = "system_group create";
	public static final String CMD_INFO = "system_group info";	
	public static final String CMD_LIST = "system_group list";
	public static final String CMD_LIST_SYSTEMS = "system_group systems";
	public static final String CMD_LIST_ERRATAS = "errata system_group";
	public static final String CMD_LIST_ERRATA_DETAILS = "errata system_group -v";
	public static final String CMD_DELETE = "system_group delete";
	public static final String CMD_UPDATE = "system_group update";
	public static final String CMD_COPY = "system_group copy";
	public static final String CMD_ADD_SYSTEMS = "system_group add_systems";
	public static final String CMD_REMOVE_SYSTEMS = "system_group remove_systems";
	public static final String CMD_PACKAGES = "system_group packages";
	public static final String CMD_ERRATA = "system_group errata";
	
	public static final String OUT_CREATE = 
			"Successfully created system group [ %s ]";
	public static final String OUT_COPY = 
			"Successfully copied system group [ %s ] to [ %s ]";
	public static final String OUT_ADD_SYSTEMS = 
			"Successfully added systems to system group [ %s ]";
	public static final String OUT_REMOVE_SYSTEMS = 
			"Successfully removed systems from system group [ %s ]";
	
	public static final String ERR_SYSTEMGROUP_NOTFOUND = 
			"Could not find system group [ %s ] within organization [ %s ]";
	public static final String ERR_SYSTEMGROUP_EXCEED = 
			"Validation failed: You cannot have more than %s system(s) associated with system group '%s'.";
	
	public static final String REG_SYSTEMGROUP_INFO = ".*Id\\s*:\\s+\\d+.*Name\\s*:\\s+%s.*Description\\s*:\\s+%s.*Total Systems\\s*:\\s+%s.*";
	public static final String REG_SYSTEMGROUP_LIST = ".*\\s+\\d+.*\\s+%s.*";
	public static final String REG_SYSTEM_LIST = ".*\\s+%s.*\\s+%s.*";
	
	public KatelloSystemGroup(String pName, String pOrg) {
		this(pName, pOrg, null, null);
	}
	
	public KatelloSystemGroup(String pName, String pOrg, String pDescription, Integer pmaxSystems) {
		this.name = pName;
		this.org = pOrg;
		this.description = pDescription;
		this.maxSystems = pmaxSystems;
	}
	
	public SSHCommandResult create() {
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("description", description));
		opts.add(new Attribute("max_systems", maxSystems));
		return run(CMD_CREATE);
	}
	
	public SSHCommandResult info(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		return run(CMD_INFO);
	}
	
	public SSHCommandResult update(String newname, String newdescr, Integer newmaxSystems){
		opts.clear();
		opts.add(new Attribute("new_name", newname));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("description", newdescr));
		opts.add(new Attribute("max_systems", newmaxSystems));
		return run(CMD_UPDATE);
	}

	public SSHCommandResult copy(String newname, String newdescr, Integer newmaxSystems){
		opts.clear();
		opts.add(new Attribute("new_name", newname));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("description", newdescr));
		opts.add(new Attribute("max_systems", newmaxSystems));
		return run(CMD_COPY);
	}
	
	public SSHCommandResult add_systems(String system_uuids){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("system_uuids", system_uuids));
		return run(CMD_ADD_SYSTEMS);
	}	
	
	public SSHCommandResult remove_systems(String system_uuids){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("system_uuids", system_uuids));
		return run(CMD_REMOVE_SYSTEMS);
	}	
	
	public SSHCommandResult list(){
		opts.clear();
		opts.add(new Attribute("org", org));
		return run(CMD_LIST);
	}

	public SSHCommandResult list_systems(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		return run(CMD_LIST_SYSTEMS);
	}

	public SSHCommandResult list_erratas(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		return run(CMD_LIST_ERRATAS);
	}

	public SSHCommandResult list_erratas(String type){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("type", type));
		return run(CMD_LIST_ERRATAS);
	}

	public SSHCommandResult list_errata_details(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		return run(CMD_LIST_ERRATA_DETAILS);
	}
	
	public SSHCommandResult delete(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		return run(CMD_DELETE);
	}

	public SSHCommandResult deleteWithSystems(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("delete_systems", "true"));
		return run(CMD_DELETE);
	}
	
	public SSHCommandResult packages_install(String packageName) {
		opts.clear();
		opts.add(new Attribute("install", packageName));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		return run(CMD_PACKAGES);
	}

	public SSHCommandResult erratas_install(String errata) {
		opts.clear();
		opts.add(new Attribute("install", errata));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		return run(CMD_ERRATA);
	}
	
	public SSHCommandResult packages_remove(String packageName) {
		opts.clear();
		opts.add(new Attribute("remove", packageName));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		return run(CMD_PACKAGES);
	}

	public SSHCommandResult packages_update(String packageName) {
		opts.clear();
		opts.add(new Attribute("update", packageName));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		return run(CMD_PACKAGES);
	}
	
	public SSHCommandResult packagegroup_install(String packageGroupName) {
		opts.clear();
		opts.add(new Attribute("install_group", packageGroupName));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		return run(CMD_PACKAGES);
	}

	public SSHCommandResult packagegroup_remove(String packageGroupName) {
		opts.clear();
		opts.add(new Attribute("remove_group", packageGroupName));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		return run(CMD_PACKAGES);
	}

	public SSHCommandResult packagegroup_update(String packageGroupName) {
		opts.clear();
		opts.add(new Attribute("update_group", packageGroupName));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		return run(CMD_PACKAGES);
	}
}
