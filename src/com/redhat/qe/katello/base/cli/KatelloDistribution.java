package com.redhat.qe.katello.base.cli;

import java.util.ArrayList;
import javax.management.Attribute;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloDistribution {
	
	// ** ** ** ** ** ** ** Public constants
	public static final String CMD_LIST = "distribution list -v";
	public static final String CMD_INFO = "distribution info -v";
	
	// ** ** ** ** ** ** ** Class members
	String org;
	String product;
	String repo;
	String environment;
	
	private KatelloCli cli;
	private ArrayList<Attribute> opts;
	
	public KatelloDistribution(String pOrg, String pProduct,
			String pRepo, String pEnvironment){
		this.org = pOrg;
		this.product = pProduct;
		this.repo = pRepo;
		this.environment = pEnvironment;
		this.opts = new ArrayList<Attribute>();
	}
	
	public SSHCommandResult list(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("product", product));
		opts.add(new Attribute("repo", repo));
		opts.add(new Attribute("environment", environment));
		cli = new KatelloCli(CMD_LIST, opts);
		return cli.run();
	}
	
	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **
}
