package com.redhat.qe.katello.base.cli;

import java.util.ArrayList;
import javax.management.Attribute;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloErrata {
	
	// ** ** ** ** ** ** ** Public constants
	public static final String CMD_INFO = "errata info";
	
	// ** ** ** ** ** ** ** Class members
	String id;
	String org;
	String product;
	String repo;
	String environment;
	
	private KatelloCli cli;
	private ArrayList<Attribute> opts;
	
	public KatelloErrata(String pId, String pOrg, String pProd, String pRepo, String pEnv){
		this.id = pId;
		this.org = pOrg;
		this.product = pProd;
		this.repo = pRepo;
		this.environment = pEnv;
		this.opts = new ArrayList<Attribute>();
	}
	
	public SSHCommandResult info(){
		opts.clear();
		opts.add(new Attribute("id", id));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("product", product));
		opts.add(new Attribute("repo", repo));
		opts.add(new Attribute("environment", environment));
		cli = new KatelloCli(CMD_INFO, opts);
		return cli.run();
	}
	
	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **	
	
}
