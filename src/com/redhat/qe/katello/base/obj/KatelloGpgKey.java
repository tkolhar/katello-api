package com.redhat.qe.katello.base.obj;

import java.util.ArrayList;
import javax.management.Attribute;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloGpgKey {
	
	// ** ** ** ** ** ** ** Public constants
	public static final String CMD_CREATE = "gpg_key create";
	public static final String CMD_INFO = "gpg_key info";
	
	public static final String OUT_CREATE = 
			"Successfully created gpg key [ %s ]"; 

	// ** ** ** ** ** ** ** Class members
	String name;
	String org;
	String file;
		
	private KatelloCli cli;
	private ArrayList<Attribute> opts;
	
	public KatelloGpgKey(String pName, String pOrg, String pFile){
		this.name = pName;
		this.org = pOrg;
		this.file = pFile;
		this.opts = new ArrayList<Attribute>();
	}
	
	public SSHCommandResult create(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("file", file));
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
	
	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **
	
	
}
