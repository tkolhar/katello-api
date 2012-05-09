package com.redhat.qe.katello.base.cli;

import java.util.ArrayList;

import javax.management.Attribute;

import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloUserRole {
	
	// ** ** ** ** ** ** ** Public constants
	public static final String CMD_CREATE = "user_role create";
	
	// ** ** ** ** ** ** ** Class members
	public String name;
	public String description;
	
	private KatelloCli cli;
	private ArrayList<Attribute> opts;

	public KatelloUserRole(String pName, String pDesc){
		this.name = pName;
		this.description = pDesc;
		this.opts = new ArrayList<Attribute>();
	}
	
	public SSHCommandResult create(){
		opts.clear();
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("description", description));
		cli = new KatelloCli(CMD_CREATE, opts);
		return cli.run();
	}
	
}
