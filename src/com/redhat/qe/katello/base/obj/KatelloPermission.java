package com.redhat.qe.katello.base.obj;

import javax.management.Attribute;

import com.redhat.qe.katello.base.threading.KatelloCliWorker;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloPermission extends _KatelloObject{
	
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
	
	public KatelloPermission(KatelloCliWorker kcr, String pName, String pOrg,
			String pScope, String pTags, String pVerbs, String pUserRole){
		this.name = pName;
		this.org = pOrg;
		this.scope = pScope;
		this.tags = pTags;
		this.verbs = pVerbs;
		this.user_role = pUserRole;
		this.kcr = kcr;
	}
	
	public SSHCommandResult create(){
		return create(false);
	}
	
	public SSHCommandResult create(boolean all_tags){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("scope", scope));
		opts.add(new Attribute("tags", tags));
		opts.add(new Attribute("verbs", verbs));
		opts.add(new Attribute("user_role", user_role));
		if(all_tags)
			return run(CMD_CREATE+" --all_tags");
		else
			return run(CMD_CREATE);
	}

	public SSHCommandResult create(boolean all_tags, boolean all_verbs){
		String cmd = CMD_CREATE;
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("scope", scope));
		opts.add(new Attribute("tags", tags));
		opts.add(new Attribute("verbs", verbs));
		opts.add(new Attribute("user_role", user_role));
		if(all_tags)
			cmd += " --all_tags";
		if(all_verbs)
			cmd += " --all_verbs";
		return run(cmd);
	}

	public SSHCommandResult available_verbs(String orgName,String scopeName){
		opts.clear();
		opts.add(new Attribute("org", orgName));
		opts.add(new Attribute("scope", scopeName));
		return run(CMD_AVAIL_VERB);
	}
	
	public SSHCommandResult delete(){
		opts.clear();
		
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("user_role", user_role));
		return run(CMD_DELETE);
	}
	
	public SSHCommandResult list(){
		opts.clear();
		opts.add(new Attribute("user_role", user_role));
		return run(CMD_LIST);
	}
	
}
