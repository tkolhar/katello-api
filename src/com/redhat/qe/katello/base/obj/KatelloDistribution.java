package com.redhat.qe.katello.base.obj;

import javax.management.Attribute;

import com.redhat.qe.katello.base.threading.KatelloCliWorker;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloDistribution extends _KatelloObject{
	
	// ** ** ** ** ** ** ** Public constants
	public static final String CMD_LIST = "distribution list -v";
	public static final String CMD_INFO = "distribution info -v";

	public static final String FEDORA18_DISTRIBUTION = "ks-Fedora-Fedora-18-x86_64";

	public static final String ERR_NOT_FOUND = "Distribution '%s' not found within the repository";

	public static final String REG_DISTRIBUTION_INFO = ".*ID\\s*:\\s*%s\\s*Family\\s*:.*Variant\\s*:.*Version\\s*:.*Files\\s*:.*";

	// ** ** ** ** ** ** ** Class members
	String org;
	String product;
	String repo;
	String environment;
	public String repo_id;
	
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
		if(repo_id == null) {
			opts.add(new Attribute("org", org));
			opts.add(new Attribute("product", product));
			opts.add(new Attribute("repo", repo));
		} else {
			opts.add(new Attribute("repo_id", repo_id));
		}
		opts.add(new Attribute("environment", environment));
		return run(CMD_LIST);
	}

	public SSHCommandResult info(String repo_id, String id) {
		opts.clear();
		opts.add(new Attribute("repo_id", repo_id));
		opts.add(new Attribute("id", id));
		return run(CMD_INFO);
	}
	
	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **
}
