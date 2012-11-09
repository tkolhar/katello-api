package com.redhat.qe.katello.base.obj;

import javax.management.Attribute;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloErrata extends _KatelloObject{
	
	// ** ** ** ** ** ** ** Public constants
	public static final String CMD_INFO = "errata info";
	public static final String CMD_LIST = "errata list";
	
	// ** ** ** ** ** ** ** Class members
	String id;
	String org;
	String product;
	String repo;
	String environment;
	String product_id;
	
	public KatelloErrata(String pId, String pOrg, String pProd, String pRepo, String pEnv){
		this.id = pId;
		this.org = pOrg;
		this.product = pProd;
		this.repo = pRepo;
		this.environment = pEnv;
	}
	
	public void setProductId(String productId) {
		this.product_id = productId;
		this.product = null;
	}
	
	public SSHCommandResult info(){
		opts.clear();
		opts.add(new Attribute("id", id));
		opts.add(new Attribute("org", org));
		if (this.product_id != null) {
			opts.add(new Attribute("product_id", product_id));
		} else {
			opts.add(new Attribute("product", product));
		}
		opts.add(new Attribute("repo", repo));
		opts.add(new Attribute("environment", environment));
		return run(CMD_INFO);
	}
	
	public SSHCommandResult cli_list(){
		opts.clear();
		opts.add(new Attribute("org", org));
		if (this.product_id != null) {
			opts.add(new Attribute("product_id", product_id));
		} else {
			opts.add(new Attribute("product", product));
		}
		opts.add(new Attribute("repo", repo));
		opts.add(new Attribute("environment", environment));
		return run(CMD_LIST);
	}
	
	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **	
	
}
