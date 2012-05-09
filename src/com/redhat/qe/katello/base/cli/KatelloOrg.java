package com.redhat.qe.katello.base.cli;

import java.util.ArrayList;
import javax.management.Attribute;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloOrg {
	
	// ** ** ** ** ** ** ** Public constants
	public static final String DEFAULT_ORG = "ACME_Corporation";
	
	public static final String CMD_CREATE = "org create";
	public static final String CMD_INFO = "org info";
	public static final String CMD_LIST = "org list";
	public static final String CMD_SUBSCRIPTIONS = "org subscriptions";
	public static final String CMD_UEBERCERT = "org uebercert";
	public static final String CMD_DELETE = "org delete";
	public static final String CMD_UPDATE = "org update";
	
	
	public static final String ERR_TEMPLATE_NOTFOUND = 
			"Could not find template [ %s ]";	
	public static final String OUT_CREATE = 
			"Successfully created org [ %s ]";

	// ** ** ** ** ** ** ** Class members
	public String name;
	public String description;
	
	private KatelloCli cli;
	private ArrayList<Attribute> opts;
	
	public KatelloOrg(String pName, String pDesc){
		this.name = pName;
		this.description = pDesc;
		this.opts = new ArrayList<Attribute>();
	}
	
	public SSHCommandResult create(){		
		opts.clear();
		opts.add(new Attribute("name", this.name));
		opts.add(new Attribute("description", this.description));
		cli = new KatelloCli(CMD_CREATE, opts);
		return cli.run();
	}
	
	public SSHCommandResult info(){
		opts.clear();
		opts.add(new Attribute("name", this.name));
		cli = new KatelloCli(CMD_INFO, opts);
		return cli.run();
	}
	
	public SSHCommandResult list(){
		opts.clear();
		KatelloCli cli = new KatelloCli(CMD_LIST+" -v", opts);
		return cli.run();
	}
		
	public SSHCommandResult subscriptions(){
		opts.clear();
		opts.add(new Attribute("name", this.name));
		cli = new KatelloCli(CMD_SUBSCRIPTIONS, opts);
		return cli.run();
	}

	public SSHCommandResult uebercert(){
		opts.clear();
		opts.add(new Attribute("name", this.name));
		cli = new KatelloCli(CMD_UEBERCERT, opts);
		return cli.run();
	}

	public SSHCommandResult delete(){
		opts.clear();
		opts.add(new Attribute("name", this.name));
		cli = new KatelloCli(CMD_DELETE, opts);
		return cli.run();
	}
	
	public SSHCommandResult update(String new_description){
		opts.clear();
		opts.add(new Attribute("name", this.name));
		opts.add(new Attribute("description", new_description));
		cli = new KatelloCli(CMD_UPDATE, opts);
		return cli.run();
	}

	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **
}
