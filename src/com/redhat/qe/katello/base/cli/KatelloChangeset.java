package com.redhat.qe.katello.base.cli;

import java.util.ArrayList;

import javax.management.Attribute;

import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloChangeset {
	
	// ** ** ** ** ** ** ** Public constants
	public static final String CMD_CREATE = "changeset create";
	public static final String CMD_PROMOTE = "changeset promote";
	public static final String CMD_UPDATE = "changeset update";
	public static final String CMD_INFO = "changeset info";
	
	public static final String OUT_CREATE = 
			"Successfully created changeset [ %s ]"; 

	// ** ** ** ** ** ** ** Class members
	String name;
	String org;
	String environment;
	
	private KatelloCli cli;
	private ArrayList<Attribute> opts;

	public KatelloChangeset(String pName, String pOrg, String pEnv){
		this.name = pName;
		this.org = pOrg;
		this.environment = pEnv;
		this.opts = new ArrayList<Attribute>();
	}
	
	public SSHCommandResult create(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("environment", environment));
		cli = new KatelloCli(CMD_CREATE, opts);
		return cli.run();
	}
	
	public SSHCommandResult promote(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("environment", environment));
		cli = new KatelloCli(CMD_PROMOTE, opts);
		return cli.run();
	}
	
	
	public SSHCommandResult info(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("environment", environment));
		cli = new KatelloCli(CMD_INFO, opts);
		return cli.run();
	}
	
	public SSHCommandResult update_addProduct(String productName){
		opts.clear();
		opts.add(new Attribute("add_product", productName));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("environment", environment));
		cli = new KatelloCli(CMD_UPDATE, opts);
		return cli.run();
	}
	
	public SSHCommandResult update_fromProduct_addRepo(String productName, String repoName){
		opts.clear();
		opts.add(new Attribute("from_product", productName));
		opts.add(new Attribute("add_repo", repoName));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("environment", environment));
		cli = new KatelloCli(CMD_UPDATE, opts);
		return cli.run();
	}

	public SSHCommandResult update_fromProduct_addErrata(String productName, String errataName){
		opts.clear();
		opts.add(new Attribute("from_product", productName));
		opts.add(new Attribute("add_erratum", errataName));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("environment", environment));
		cli = new KatelloCli(CMD_UPDATE, opts);
		return cli.run();
	}

	public SSHCommandResult update_addTemplate(String templateName){
		opts.clear();
		opts.add(new Attribute("add_template", templateName));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("environment", environment));
		cli = new KatelloCli(CMD_UPDATE, opts);
		return cli.run();
	}

	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **	
	
}
