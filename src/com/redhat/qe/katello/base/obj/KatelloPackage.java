package com.redhat.qe.katello.base.obj;

import javax.management.Attribute;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloPackage extends _KatelloObject{

	// ** ** ** ** ** ** ** Public constants
	public static final String CMD_INFO = "package info";
	public static final String CMD_LIST = "package list";
	public static final String CMD_SEARCH = "package search";
	
	public static final String REG_PACKAGE_ID = "\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}";
	
	// ** ** ** ** ** ** ** Class members
	public String id;
	public String org;
	public String product;
	public String product_label;
	public String product_id;
	public String repo;
	public String environment;
	public String name;
	
	public KatelloPackage(String pId, String pName, String pOrg, String pProd, String pRepo, String pEnv){
		this.id = pId;
		this.name = pName;
		this.org = pOrg;
		this.product = pProd;
		this.repo = pRepo;
		this.environment = pEnv;
	}
	
	public void setProductId(String productId) {
		this.product_id = productId;
		this.product_label = null;
	}
	
	public void setProductLabel(String productLabel) {
		this.product_id = null;
		this.product_label = productLabel;
	}
	
	public SSHCommandResult cli_info(){
		opts.clear();
		opts.add(new Attribute("id", id));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("product", product));
		opts.add(new Attribute("repo", repo));
		opts.add(new Attribute("environment", environment));
		opts.add(new Attribute("product_label", product_label));
		opts.add(new Attribute("product_id", product_id));
		return run(CMD_INFO);
	}
	
	public SSHCommandResult cli_list(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("product", product));
		opts.add(new Attribute("repo", repo));
		opts.add(new Attribute("environment", environment));
		opts.add(new Attribute("product_label", product_label));
		opts.add(new Attribute("product_id", product_id));
		return run(CMD_LIST);
	}
	
	public SSHCommandResult cli_search(String query){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("product", product));
		opts.add(new Attribute("repo", repo));
		opts.add(new Attribute("environment", environment));
		opts.add(new Attribute("query", query));
		opts.add(new Attribute("product_label", product_label));
		opts.add(new Attribute("product_id", product_id));
		return run(CMD_SEARCH);
	}
	
	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **	
}
