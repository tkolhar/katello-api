package com.redhat.qe.katello.base.obj;

import javax.management.Attribute;

import com.redhat.qe.katello.base.threading.KatelloCliWorker;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloDistribution extends _KatelloObject{
	
	// ** ** ** ** ** ** ** Public constants
	public static final String CMD_LIST = "distribution list -v";
	public static final String CMD_INFO = "distribution info -v";
	
	// ** ** ** ** ** ** ** Class members
	String org;
	String product;
	String repo;
	String environment;
	
	public KatelloDistribution(KatelloCliWorker kcr, String pOrg, String pProduct,
			String pRepo, String pEnvironment){
		this.org = pOrg;
		this.product = pProduct;
		this.repo = pRepo;
		this.environment = pEnvironment;
		this.kcr = kcr;
	}
	
	public SSHCommandResult list(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("product", product));
		opts.add(new Attribute("repo", repo));
		opts.add(new Attribute("environment", environment));
		return run(CMD_LIST);
	}
	
	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **
}
