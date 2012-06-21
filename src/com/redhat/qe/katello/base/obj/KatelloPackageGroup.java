package com.redhat.qe.katello.base.obj;

import java.util.ArrayList;

import javax.management.Attribute;

import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloPackageGroup {

	// ** ** ** ** ** ** ** Public constants
	public static final String CMD_INFO = "package_group info";
	public static final String CMD_LIST = "package_group list";

	
	// ** ** ** ** ** ** ** Class members
	public String id;
	public String name;
	public String description;
	
	private KatelloCli cli;
	private ArrayList<Attribute> opts;
	
	
	public KatelloPackageGroup(String pId, String pName, String pDescr){
		this.id = pId;
		this.name = pName;
		this.description = pDescr;
		this.opts = new ArrayList<Attribute>();
	}
	
	public SSHCommandResult cli_info(String repoId) {
		opts.clear();
		opts.add(new Attribute("id", id));
		opts.add(new Attribute("repo_id", repoId));
		cli = new KatelloCli(CMD_INFO, opts);
		return cli.run();
	}
	
	public SSHCommandResult cli_list(String repoId) {
		opts.clear();
		opts.add(new Attribute("repo_id", repoId));
		cli = new KatelloCli(CMD_LIST, opts);
		return cli.run();
	}
	
	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **	

}
