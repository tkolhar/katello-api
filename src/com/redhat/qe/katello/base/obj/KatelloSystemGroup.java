package com.redhat.qe.katello.base.obj;

import java.util.ArrayList;

import javax.management.Attribute;

import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloSystemGroup {
	
	// ** ** ** ** ** ** ** Class members
	public String name;
	public String org;
	public String description;
	public int totalSystems = 0;
	public Integer maxSystems;
	
	private KatelloCli cli;
	private ArrayList<Attribute> opts;
	
	public static final String CMD_CREATE = "system_group create";
	public static final String CMD_INFO = "system_group info";	
	public static final String CMD_LIST = "system_group list";
	public static final String CMD_LIST_SYSTEMS = "system_group systems";
	public static final String CMD_DELETE = "system_group delete";
	public static final String CMD_UPDATE = "system_group update";
	public static final String CMD_COPY = "system_group copy";
	public static final String CMD_ADD_SYSTEMS = "system_group add_systems";
	public static final String CMD_REMOVE_SYSTEMS = "system_group remove_systems";
	
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
	
	public static final String REG_SYSTEMGROUP_INFO = ".*Id:\\s+\\d+.*Name:\\s+%s.*Description:\\s+%s.*Total Systems:\\s+%s.*";
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
		this.opts = new ArrayList<Attribute>();
	}
	
	public SSHCommandResult create() {
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("description", description));
		opts.add(new Attribute("max_systems", maxSystems));
		cli = new KatelloCli(CMD_CREATE, opts);
		return cli.run();
	}
	
	public SSHCommandResult info(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		cli = new KatelloCli(CMD_INFO, opts);
		return cli.run();
	}
	
	public SSHCommandResult update(String newname, String newdescr, Integer newmaxSystems){
		opts.clear();
		opts.add(new Attribute("new_name", newname));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("description", newdescr));
		opts.add(new Attribute("max_systems", newmaxSystems));
		cli = new KatelloCli(CMD_UPDATE, opts);
		return cli.run();
	}

	public SSHCommandResult copy(String newname, String newdescr, Integer newmaxSystems){
		opts.clear();
		opts.add(new Attribute("new_name", newname));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("description", newdescr));
		opts.add(new Attribute("max_systems", newmaxSystems));
		cli = new KatelloCli(CMD_COPY, opts);
		return cli.run();
	}
	
	public SSHCommandResult add_systems(String system_uuids){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("system_uuids", system_uuids));
		cli = new KatelloCli(CMD_ADD_SYSTEMS, opts);
		return cli.run();
	}	
	
	public SSHCommandResult remove_systems(String system_uuids){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("system_uuids", system_uuids));
		cli = new KatelloCli(CMD_REMOVE_SYSTEMS, opts);
		return cli.run();
	}	
	
	public SSHCommandResult list(){
		opts.clear();
		opts.add(new Attribute("org", org));
		cli = new KatelloCli(CMD_LIST, opts);
		return cli.run();
	}

	public SSHCommandResult list_systems(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		cli = new KatelloCli(CMD_LIST_SYSTEMS, opts);
		return cli.run();
	}
	
	public SSHCommandResult delete(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		cli = new KatelloCli(CMD_DELETE, opts);
		return cli.run();
	}
}
