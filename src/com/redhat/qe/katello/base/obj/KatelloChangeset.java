package com.redhat.qe.katello.base.obj;

import javax.management.Attribute;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloChangeset extends _KatelloObject{
	
	// ** ** ** ** ** ** ** Public constants
	public static final String CMD_CREATE = "changeset create";
	public static final String CMD_APPLY = "changeset apply";
	public static final String CMD_PROMOTE = "changeset promote";
	public static final String CMD_UPDATE = "changeset update";
	public static final String CMD_DELETE = "changeset delete";
	public static final String CMD_INFO = "changeset info";
	public static final String CMD_LIST = "changeset list";
	
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
	
	public KatelloChangeset(String pName, String pOrg, String pEnv){
		this(pName, pOrg, pEnv, false);
	}

	public KatelloChangeset(String pName, String pOrg, String pEnv, boolean pisDeletion){
		this.name = pName;
		this.org = pOrg;
		this.environment = pEnv;
		this.isDeletion = pisDeletion; 
	}
	
	public SSHCommandResult create(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("environment", environment));
		if (this.isDeletion) {
			opts.add(new Attribute("deletion", "true"));
		}
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
	
	public SSHCommandResult update_name(String newname){
		opts.clear();
		opts.add(new Attribute("new_name", newname));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("environment", environment));
		return run(CMD_UPDATE);
	}
	
	public SSHCommandResult update_addProduct(String productName){
		opts.clear();
		opts.add(new Attribute("add_product", productName));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("environment", environment));
		return run(CMD_UPDATE);
	}

	public SSHCommandResult update_addProductId(String product_id){
		opts.clear();
		opts.add(new Attribute("add_product_id", product_id));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("environment", environment));
		return run(CMD_UPDATE);
	}

	public SSHCommandResult update_removeProduct(String productName){
		opts.clear();
		opts.add(new Attribute("remove_product", productName));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("environment", environment));
		return run(CMD_UPDATE);
	}

	public SSHCommandResult update_removeProductId(String product_id){
		opts.clear();
		opts.add(new Attribute("remove_product_id", product_id));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("environment", environment));
		return run(CMD_UPDATE);
	}
	
	public SSHCommandResult update_add_package(String productName, String pkg) {
		opts.clear();
		opts.add(new Attribute("from_product", productName));
		opts.add(new Attribute("add_package", pkg));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("environment", environment));
		opts.add(new Attribute("name", name));
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
	
	public SSHCommandResult update_fromProduct_addRepo(String productName, String repoName){
		opts.clear();
		opts.add(new Attribute("from_product", productName));
		opts.add(new Attribute("add_repo", repoName));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("environment", environment));
		return run(CMD_UPDATE);
	}
	
	public SSHCommandResult update_fromProductId_addRepo(String productId, String repoName){
		opts.clear();
		opts.add(new Attribute("from_product_id", productId));
		opts.add(new Attribute("add_repo", repoName));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("environment", environment));
		return run(CMD_UPDATE);
	}

	public SSHCommandResult update_fromProduct_removeRepo(String productName, String repoName){
		opts.clear();
		opts.add(new Attribute("from_product", productName));
		opts.add(new Attribute("remove_repo", repoName));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("environment", environment));
		return run(CMD_UPDATE);
	}

	public SSHCommandResult update_fromProduct_addErrata(String productName, String errataName){
		opts.clear();
		opts.add(new Attribute("from_product", productName));
		opts.add(new Attribute("add_erratum", errataName));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("environment", environment));
		return run(CMD_UPDATE);
	}

	public SSHCommandResult update_fromProduct_removeErrata(String productName, String errataName){
		opts.clear();
		opts.add(new Attribute("from_product", productName));
		opts.add(new Attribute("remove_erratum", errataName));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("environment", environment));
		return run(CMD_UPDATE);
	}
	
	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **	
	
}
