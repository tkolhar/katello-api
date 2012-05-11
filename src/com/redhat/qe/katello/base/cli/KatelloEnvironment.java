package com.redhat.qe.katello.base.cli;

import java.util.ArrayList;
import javax.management.Attribute;

import com.redhat.qe.katello.base.KatelloApi;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloEnvironment {
	
	// ** ** ** ** ** ** ** Public constants
	public static final String LIBRARY = "Library";
	
	public static final String CMD_CREATE = "environment create";
	public static final String CMD_INFO = "environment info -v";
	public static final String CLI_CMD_LIST = "environment list";
	
	public static final String OUT_CREATE = 
			"Successfully created environment [ %s ]";

	public static final String API_CMD_LIST = "/organizations/%s/environments";
	
	// ** ** ** ** ** ** ** Class members
	String name;
	String description;
	String org;
	String prior;
	
	private KatelloCli cli;
	private ArrayList<Attribute> opts;
	
	public KatelloEnvironment(String pName, String pDesc,
			String pOrg, String pPrior){
		this.name = pName;
		this.description = pDesc;
		this.org = pOrg;
		this.prior = pPrior;
		this.opts = new ArrayList<Attribute>();
	}
	
	public SSHCommandResult create(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("description", description));
		opts.add(new Attribute("prior", prior));
		cli = new KatelloCli(CMD_CREATE, opts);
		return cli.run();
	}
	
	public SSHCommandResult info(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		cli = new KatelloCli(CMD_INFO, opts);
		return cli.run();
	}
	
	public SSHCommandResult api_list(){
		KatelloApi api = new KatelloApi();
		return api.get(String.format(API_CMD_LIST, this.org));
	}
	
	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **
}
