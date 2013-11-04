package com.redhat.qe.katello.base.obj;

import javax.management.Attribute;

import com.redhat.qe.katello.base.threading.KatelloCliWorker;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloPackage extends _KatelloObject{

	// ** ** ** ** ** ** ** Public constants
	public static final String CMD_INFO = "package info";
	public static final String CMD_LIST = "package list";
	public static final String CMD_LIST_V = "package list -v"; //gkhachik - too many usages of cli_list() call. Better have a new -v option for packagesCount();
	public static final String CMD_SEARCH = "package search --noheading";
	
	public static final String REG_PACKAGE_ID = "\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}";
	public static final String REG_CHS_PROMOTE_ERROR = "Validation failed: Repository of the package '%s' has not been promoted into the target environment!";
	public static final String REG_CHS_DEL_ERROR = "Package's product not found within environment you want to promote from.";
	
	// ** ** ** ** ** ** ** Class members
	public String id;
	public String org;
	public String product;
	public String product_label;
	public String product_id;
	public String repo;
	public String environment;
	public String name;
	public String content_view;
	public String content_view_label;
	public String content_view_id;
	
	public KatelloPackage(KatelloCliWorker kcr, String pId, String pName, String pOrg, String pProd, String pRepo, String pEnv){
		this.id = pId;
		this.name = pName;
		this.org = pOrg;
		this.product = pProd;
		this.repo = pRepo;
		this.environment = pEnv;
		this.kcr = kcr;
	}

	public KatelloPackage(KatelloCliWorker kcr, String pOrg, String pProd, String pRepo, String pContnetView){
		this.org = pOrg;
		this.product = pProd;
		this.repo = pRepo;
		this.content_view = pContnetView;
		this.kcr = kcr;
	}
	
	public void setProductId(String productId) {
		this.product_id = productId;
		this.product_label = null;
	}
	
	public void setProductLabel(String productLabel) {
		this.product_id = null;
		this.product_label = productLabel;
	}
	
	public SSHCommandResult cli_info(){
		opts.clear();
		opts.add(new Attribute("id", id));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("product", product));
		opts.add(new Attribute("repo", repo));
		opts.add(new Attribute("environment", environment));
		opts.add(new Attribute("content_view", content_view));
		opts.add(new Attribute("content_view_label", content_view_label));
		opts.add(new Attribute("content_view_id", content_view_id));
		opts.add(new Attribute("product_label", product_label));
		opts.add(new Attribute("product_id", product_id));
		return run(CMD_INFO);
	}
	
	public SSHCommandResult cli_list(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("product", product));
		opts.add(new Attribute("repo", repo));
		opts.add(new Attribute("environment", environment));
		opts.add(new Attribute("product_label", product_label));
		opts.add(new Attribute("product_id", product_id));
		opts.add(new Attribute("content_view", content_view));
		opts.add(new Attribute("content_view_label", content_view_label));
		opts.add(new Attribute("content_view_id", content_view_id));
		return run(CMD_LIST);
	}
	
	public SSHCommandResult cli_search(String query){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("product", product));
		opts.add(new Attribute("repo", repo));
		opts.add(new Attribute("environment", environment));
		opts.add(new Attribute("query", query));
		opts.add(new Attribute("product_label", product_label));
		opts.add(new Attribute("product_id", product_id));
		opts.add(new Attribute("content_view", content_view));
		opts.add(new Attribute("content_view_label", content_view_label));
		opts.add(new Attribute("content_view_id", content_view_id));
		return run(CMD_SEARCH);
	}
	
	public SSHCommandResult custom_packagesCount(String environment){
		opts.clear();
		if(environment == null) 
			environment = KatelloEnvironment.LIBRARY;
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("product", product));
		opts.add(new Attribute("repo", repo));
		opts.add(new Attribute("environment", environment));
		opts.add(new Attribute("product_label", product_label));
		opts.add(new Attribute("product_id", product_id));
		opts.add(new Attribute("content_view", content_view));
		opts.add(new Attribute("content_view_label", content_view_label));
		opts.add(new Attribute("content_view_id", content_view_id));
		return runExt(CMD_LIST_V, " | grep -e \"^Name.*\\:\" | wc -l"); // -v option here in the command is really important
	}

	public String custom_packageId(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("product", product));
		opts.add(new Attribute("repo", repo));
		opts.add(new Attribute("environment", environment));
		opts.add(new Attribute("product_label", product_label));
		opts.add(new Attribute("product_id", product_id));
		opts.add(new Attribute("content_view", content_view));
		opts.add(new Attribute("content_view_label", content_view_label));
		opts.add(new Attribute("content_view_id", content_view_id));
		return runExt(CMD_LIST, " | grep \"" + name + "\" | awk '{print $1}'").getStdout().replaceAll("\n", ",").split(",")[0];
	}
	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **	
}
