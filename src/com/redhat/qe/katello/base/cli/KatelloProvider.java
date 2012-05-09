package com.redhat.qe.katello.base.cli;

import java.util.ArrayList;

import javax.management.Attribute;

import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloProvider {
	
	// ** ** ** ** ** ** ** Public constants
	public static final String PROVIDER_REDHAT = "Red Hat";
	public static final String CMD_CREATE = "provider create";
	public static final String CMD_IMPORT_MANIFEST = "provider import_manifest";
	public static final String CMD_LIST = "provider list -v";
	public static final String CMD_INFO = "provider info";
	public static final String CMD_SYNCHRONIZE = "provider synchronize";
	public static final String CMD_UPDATE = "provider update";
	public static final String CMD_DELETE = "provider delete";
	public static final String CMD_STATUS = "provider status";
	
	public static final String OUT_CREATE = 
			"Successfully created provider [ %s ]";
	public static final String OUT_DELETE = 
			"Deleted provider [ %s ]";
	public static final String OUT_SYNCHRONIZE = 
			"Provider [ %s ] synchronized";
	public static final String ERR_REDHAT_UPDATENAME = 
			"Validation failed: the following attributes can not be updated for the Red Hat provider: [ name ]";
	public static final String OUT_UPDATE = 
			"Successfully updated provider [ %s ]";

	// ** ** ** ** ** ** ** Class members
	public String name;
	public String org;
	public String description;
	public String url;
	
	private KatelloCli cli;
	private ArrayList<Attribute> opts;

	public KatelloProvider(String pName, String pOrg, 
			String pDesc, String pUrl){
		this.name = pName;
		this.org = pOrg;
		this.description = pDesc;
		this.url = pUrl;
		this.opts = new ArrayList<Attribute>();
	}
	
	public SSHCommandResult create(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("description", description));
		opts.add(new Attribute("url", url));
		cli = new KatelloCli(CMD_CREATE, opts);
		return cli.run();
	}

	public SSHCommandResult import_manifest(String file, Boolean force){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("file", file));
		opts.add(new Attribute("force", ""));
		cli = new KatelloCli(CMD_IMPORT_MANIFEST, opts);
		return cli.run();
	}

	public SSHCommandResult list(){
		opts.clear();
		opts.add(new Attribute("org", org));
		cli = new KatelloCli(CMD_LIST, opts);
		return cli.run();
	}
	
	public SSHCommandResult info(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		cli = new KatelloCli(CMD_INFO, opts);
		return cli.run();
	}

	public SSHCommandResult synchronize(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		cli = new KatelloCli(CMD_SYNCHRONIZE, opts);
		return cli.run();
	}

	public SSHCommandResult update(String new_name, String url, String description){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("new_name", new_name));
		opts.add(new Attribute("url", url));
		opts.add(new Attribute("description", description));
		cli = new KatelloCli(CMD_UPDATE, opts);
		return cli.run();
	}

	public SSHCommandResult delete(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		cli = new KatelloCli(CMD_DELETE, opts);
		return cli.run();
	}

	public SSHCommandResult status(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		cli = new KatelloCli(CMD_STATUS, opts);
		return cli.run();
	}

	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **
}
