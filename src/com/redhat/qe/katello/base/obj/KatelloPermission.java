package com.redhat.qe.katello.base.obj;

import java.util.ArrayList;
import javax.management.Attribute;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloPermission {
	
	// ** ** ** ** ** ** ** Public constants
	public static final String CMD_CREATE = "permission create";
	public static final String CMD_AVAIL_VERB = "permission available_verbs";
	public static final String CMD_DELETE = "permission delete";
	public static final String OUT_CREATE = "Successfully created permission [ %s ] for user role [ %s ]";
	public static final String OUT_DELETE = "Successfully deleted permission [ %s ] for role [ %s ]";
	public static final String CMD_LIST = "permission list";
	
	// ** ** ** ** ** ** ** Class members
	String name;
	String org;
	String scope;
	String tags;
	String verbs;
	String user_role;
	
	private KatelloCli cli;
	private ArrayList<Attribute> opts;

	public KatelloPermission(String pName, String pOrg,
			String pScope, String pTags, String pVerbs, String pUserRole){
		this.name = pName;
		this.org = pOrg;
		this.scope = pScope;
		this.tags = pTags;
		this.verbs = pVerbs;
		this.user_role = pUserRole;
		this.opts = new ArrayList<Attribute>();
	}
	
	public SSHCommandResult create(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("scope", scope));
		opts.add(new Attribute("tags", tags));
		opts.add(new Attribute("verbs", verbs));
		opts.add(new Attribute("user_role", user_role));
		cli = new KatelloCli(CMD_CREATE, opts);
		return cli.run();
	}
	
	
	public static SSHCommandResult available_verbs(String orgName,String scopeName){
		
		ArrayList<Attribute> optns=new ArrayList<Attribute>();
		KatelloCli cli_cmd;
		optns.clear();
		optns.add(new Attribute("org", orgName));
		optns.add(new Attribute("scope", scopeName));
		cli_cmd = new KatelloCli(CMD_AVAIL_VERB, optns);
		return cli_cmd.run();
	}
	
	
	public SSHCommandResult delete(){
		opts.clear();
		
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("user_role", user_role));
		cli = new KatelloCli(CMD_DELETE, opts);
		return cli.run();
	}
	
	public SSHCommandResult list(){
		opts.clear();
		opts.add(new Attribute("user_role", user_role));
		cli = new KatelloCli(CMD_LIST, opts);
		return cli.run();
	}
	
}
