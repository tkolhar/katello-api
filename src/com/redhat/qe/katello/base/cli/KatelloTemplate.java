package com.redhat.qe.katello.base.cli;

import java.util.ArrayList;

import javax.management.Attribute;

import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloTemplate {
	
	// ** ** ** ** ** ** ** Public constants
	public static final String CMD_CREATE = "template create";
	public static final String CMD_INFO = "template info -v";
	public static final String CMD_UPDATE = "template update";
	public static final String CMD_EXPORT = "template export";
	
	public static final String FORMAT_TDL = "tdl";
	
	// ** ** ** ** ** ** ** Class members
	String name;
	String org;
	String description;
	String parent;
	
	private KatelloCli cli;
	private ArrayList<Attribute> opts;

	public KatelloTemplate(String pName, String pDesc,
			String pOrg, String pParent){
		this.name = pName;
		this.description = pDesc;
		this.org = pOrg;
		this.parent = pParent;
		this.opts = new ArrayList<Attribute>();
	}
	
	public SSHCommandResult create(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("description", description));
		opts.add(new Attribute("parent", parent));
		cli = new KatelloCli(CMD_CREATE, opts);
		return cli.run();
	}
	
	public SSHCommandResult info(String environment){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("environment", environment));
		cli = new KatelloCli(CMD_INFO, opts);
		return cli.run();
	}
	
	public SSHCommandResult update_add_distribution(String product, String distribution){
		opts.clear();
		opts.add(new Attribute("from_product", product));
		opts.add(new Attribute("add_distribution", distribution));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		cli = new KatelloCli(CMD_UPDATE, opts);
		return cli.run();
	}
	
	public SSHCommandResult update_add_repo(String product, String repo){
		opts.clear();
		opts.add(new Attribute("from_product", product));
		opts.add(new Attribute("add_repo", repo));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		cli = new KatelloCli(CMD_UPDATE, opts);
		return cli.run();
	}

	public SSHCommandResult update_add_package(String pkg){
		opts.clear();
		opts.add(new Attribute("add_package", pkg));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		cli = new KatelloCli(CMD_UPDATE, opts);
		return cli.run();
	}

	public SSHCommandResult update_add_package_group(String pkgGrp){
		opts.clear();
		opts.add(new Attribute("add_package_group", pkgGrp));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		cli = new KatelloCli(CMD_UPDATE, opts);
		return cli.run();
	}
	
	public SSHCommandResult export(String environment, String file, String format){
		opts.clear();
		opts.add(new Attribute("file", file));
		opts.add(new Attribute("format", format));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("environment", environment));
		cli = new KatelloCli(CMD_EXPORT, opts);
		return cli.run();
	}
	
	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **
}
