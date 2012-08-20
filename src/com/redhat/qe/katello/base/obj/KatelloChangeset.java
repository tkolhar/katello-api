package com.redhat.qe.katello.base.obj;

import java.util.ArrayList;

import javax.management.Attribute;

import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloChangeset {
	
	// ** ** ** ** ** ** ** Public constants
	public static final String CMD_CREATE = "changeset create";
	public static final String CMD_PROMOTE = "changeset promote";
	public static final String CMD_UPDATE = "changeset update";
	public static final String CMD_DELETE = "changeset delete";
	public static final String CMD_INFO = "changeset info";
	public static final String CMD_LIST = "changeset list";
	
	public static final String OUT_CREATE = 
			"Successfully created changeset [ %s ] for environment [ %s ]";
	public static final String OUT_DELETE = 
			"Deleted changeset '%s'"; 
	public static final String ERR_NOT_FOUND = 
			"Could not find changeset [ %s ] within environment [ %s ]";
	public static final String OUT_UPDATE =
			"Successfully updated changeset [ %s ]";
	
	public static final String REG_CHST_INFO = ".*Id:\\s+\\d+.*Name:\\s+%s.*Description:\\s+%s.*State:\\s+%s.*Environment Name:\\s+%s.*";
	public static final String REG_CHST_ID = "Id:\\s+\\d+\\s+Name:";
	public static final String REG_CHST_LIST = ".*\\s+\\d+.*\\s+%s.*\\s+%s.*";
	
	public static final String REG_CHST_PACKAGES = ".*Packages:\\s+.*%s.*";
	public static final String REG_CHST_PRODUCTS = ".*Products:\\s+.*%s.*";
	public static final String REG_CHST_REPOS = ".*Repositories:\\s+.*%s.*";
	public static final String REG_CHST_TEMPLS = ".*System Templates:\\s+.*%s.*";
	public static final String REG_CHST_ERRATA = ".*Errata:\\s+.*%s.*";

	// ** ** ** ** ** ** ** Class members
	public String name;
	public String org;
	public String description;
	public String environment;
	public String state;
	
	private KatelloCli cli;
	private ArrayList<Attribute> opts;

	public KatelloChangeset(String pName, String pOrg, String pEnv){
		this.name = pName;
		this.org = pOrg;
		this.environment = pEnv;
		this.opts = new ArrayList<Attribute>();
	}
	
	public SSHCommandResult create(){
		return create(null);
	}

	public SSHCommandResult create(KatelloUser user){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("environment", environment));
		if (user == null) {
			cli = new KatelloCli(CMD_CREATE, opts);
		} else {
			cli = new KatelloCli(CMD_CREATE, opts, user);
		}
		return cli.run();
	}

	public SSHCommandResult delete(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("environment", environment));
		cli = new KatelloCli(CMD_DELETE, opts);
		return cli.run();
	}
	
	public SSHCommandResult list(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("environment", environment));
		cli = new KatelloCli(CMD_LIST, opts);
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
	
	public SSHCommandResult update_name(String newname){
		opts.clear();
		opts.add(new Attribute("new_name", newname));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("environment", environment));
		cli = new KatelloCli(CMD_UPDATE, opts);
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

	public SSHCommandResult update_removeProduct(String productName){
		opts.clear();
		opts.add(new Attribute("remove_product", productName));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("environment", environment));
		cli = new KatelloCli(CMD_UPDATE, opts);
		return cli.run();
	}
	
	public SSHCommandResult update_add_package(String productName, String pkg) {
		opts.clear();
		opts.add(new Attribute("from_product", productName));
		opts.add(new Attribute("add_package", pkg));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
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

	public SSHCommandResult update_fromProduct_removeRepo(String productName, String repoName){
		opts.clear();
		opts.add(new Attribute("from_product", productName));
		opts.add(new Attribute("remove_repo", repoName));
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

	public SSHCommandResult update_fromProduct_removeErrata(String productName, String errataName){
		opts.clear();
		opts.add(new Attribute("from_product", productName));
		opts.add(new Attribute("remove_erratum", errataName));
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

	public SSHCommandResult update_removeTemplate(String templateName){
		opts.clear();
		opts.add(new Attribute("remove_template", templateName));
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
