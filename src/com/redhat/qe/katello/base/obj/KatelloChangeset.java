package com.redhat.qe.katello.base.obj;

import javax.management.Attribute;

import com.redhat.qe.katello.base.threading.KatelloCliWorker;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloChangeset extends _KatelloObject{
	
	// ** ** ** ** ** ** ** Public constants
	public static final String CMD_CREATE = "changeset create";
	public static final String CMD_APPLY = "changeset apply";
	public static final String CMD_PROMOTE = "changeset promote";
	public static final String CMD_UPDATE = "changeset update";
	public static final String CMD_DELETE = "changeset delete";
	public static final String CMD_INFO = "changeset info";
	public static final String CMD_LIST = "changeset list -v";
	
	public static final String OUT_CREATE = 
			"Successfully created changeset [ %s ] for environment [ %s ]";
	public static final String OUT_DELETE = 
			"Deleted changeset '%s'"; 
	public static final String ERR_NOT_FOUND = 
			"Could not find changeset [ %s ] within environment [ %s ]";
	public static final String OUT_UPDATE =
			"Successfully updated changeset [ %s ]";
	public static final String OUT_APPLIED = 
			"Changeset [ %s ] applied";

	public static final String ERR_PROMOTION_DELETION = "error: specify either --promotion or --deletion but not both";
	
	public static final String REG_CHST_INFO = ".*ID\\s*:\\s+\\d+.*Name\\s*:\\s+%s.*Description\\s*:\\s+%s.*State\\s*:\\s+%s.*Environment Name\\s*:\\s+%s.*";
	public static final String REG_CHST_ID = "ID\\s*:\\s+\\d+\\s+Name\\s*:";
	public static final String REG_CHST_LIST = ".*\\s+\\d+.*\\s+%s.*\\s+%s.*";
	
	public static final String REG_CHST_PACKAGES = ".*Packages\\s*:\\s+.*%s.*";
	public static final String REG_CHST_PRODUCTS = ".*Products\\s*:\\s+.*%s.*";
	public static final String REG_CHST_REPOS = ".*Repositories\\s*:\\s+.*%s.*";
	public static final String REG_CHST_ERRATA = ".*Errata\\s*:\\s+.*%s.*";

	// ** ** ** ** ** ** ** Class members
	public String name;
	public String org;
	public String description;
	public String environment;
	public String state;
	private boolean isDeletion;
	
	public KatelloChangeset(KatelloCliWorker kcr, String pName, String pOrg, String pEnv){
		this(kcr, pName, pOrg, pEnv, false);
	}

	public KatelloChangeset(KatelloCliWorker kcr, String pName, String pOrg, String pEnv, String descr){
		this(kcr, pName, pOrg, pEnv, false);
		this.description  = descr;
	}

	public KatelloChangeset(KatelloCliWorker kcr, String pName, String pOrg, String pEnv, boolean pisDeletion){
		this.name = pName;
		this.org = pOrg;
		this.environment = pEnv;
		this.isDeletion = pisDeletion; 
		this.kcr = kcr;
	}
	
	public SSHCommandResult create(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("environment", environment));
		if (this.isDeletion) {
			opts.add(new Attribute("deletion", "true"));
		}
		opts.add(new Attribute("description", description));
		return run(CMD_CREATE);
	}

	public SSHCommandResult delete(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("environment", environment));
		return run(CMD_DELETE);
	}
	
	public SSHCommandResult list(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("environment", environment));
		return run(CMD_LIST);
	}
	
	public SSHCommandResult promote(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("environment", environment));
		return run(CMD_PROMOTE);
	}

	public SSHCommandResult apply(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("environment", environment));
		return run(CMD_APPLY);
	}
	
	public SSHCommandResult info(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("environment", environment));
		return run(CMD_INFO);
	}

	public SSHCommandResult info_dependencies(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("environment", environment));
		return run(CMD_INFO + " --dependencies");
	}

	public SSHCommandResult update_name(String newname){
		opts.clear();
		opts.add(new Attribute("new_name", newname));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("environment", environment));
		return run(CMD_UPDATE);
	}

	public SSHCommandResult update_addView(String viewName){
		opts.clear();
		opts.add(new Attribute("add_content_view", viewName));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("environment", environment));
		return run(CMD_UPDATE);
	}

	public SSHCommandResult update_add_distr(String productName, String distr) {
		opts.clear();
		opts.add(new Attribute("from_product", productName));
		opts.add(new Attribute("add_distribution", distr));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("environment", environment));
		opts.add(new Attribute("name", name));
		return run(CMD_UPDATE);
	}

	public SSHCommandResult update_description(String new_descr) {
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("environment", environment));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("description", new_descr));
		return run(CMD_UPDATE);
	}

	public SSHCommandResult update_removeView(String viewName){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("environment", environment));
		opts.add(new Attribute("remove_content_view", viewName));
		return run(CMD_UPDATE);
	}
	
	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **	
	
}
