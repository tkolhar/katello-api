package com.redhat.qe.katello.base.cli;

import java.util.ArrayList;
import javax.management.Attribute;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloFilter {
	
	// ** ** ** ** ** ** ** Public constants
	public static final String CMD_CREATE = "filter create";


	public static final String OUT_CREATE = 
			"Successfully created filter [ %s ]";
	
	// ** ** ** ** ** ** ** Class members
	String name;
	String org;
	String packages;
	
	private KatelloCli cli;
	private ArrayList<Attribute> opts;

	public KatelloFilter(String pName, String pOrg, String pEnv, String pPackages){
		this.name = pName;
		this.org = pOrg;
		this.packages = pPackages;
		this.opts = new ArrayList<Attribute>();
	}
	
	public SSHCommandResult create(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("packages", packages));
		cli = new KatelloCli(CMD_CREATE, opts);
		return cli.run();
	}

	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **	
	
}
