package com.redhat.qe.katello.base.obj;

import javax.management.Attribute;

import com.redhat.qe.katello.base.threading.KatelloCliWorker;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloPackageGroup extends _KatelloObject{

	// ** ** ** ** ** ** ** Public constants
	public static final String CMD_INFO = "package_group info";
	public static final String CMD_LIST = "package_group list";
	public static final String CMD_CATEGORY_LIST = "package_group category_list -v";
	public static final String CMD_CATEGORY_INFO = "package_group category_info";

	
	// ** ** ** ** ** ** ** Class members
	public String id;
	public String name;
	public String description;
	
	public KatelloPackageGroup(KatelloCliWorker kcr, String pId, String pName, String pDescr){
		this.id = pId;
		this.name = pName;
		this.description = pDescr;
		this.kcr = kcr;
	}
	
	public SSHCommandResult cli_info(String repoId) {
		opts.clear();
		opts.add(new Attribute("id", id));
		opts.add(new Attribute("repo_id", repoId));
		return run(CMD_INFO);
	}
	
	public SSHCommandResult cli_list(String repoId) {
		opts.clear();
		opts.add(new Attribute("repo_id", repoId));
		return run(CMD_LIST);
	}

	public SSHCommandResult cli_category_list(String repoId) {
		opts.clear();
		opts.add(new Attribute("repo_id", repoId));
		return run(CMD_CATEGORY_LIST);
	}

	public SSHCommandResult cli_category_info(String repoId, String categoryId) {
		opts.clear();
		opts.add(new Attribute("repo_id", repoId));
		opts.add(new Attribute("id", categoryId));
		return run(CMD_CATEGORY_INFO);
	}
	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **	

}
