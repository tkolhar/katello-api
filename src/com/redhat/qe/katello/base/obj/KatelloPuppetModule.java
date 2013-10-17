package com.redhat.qe.katello.base.obj;

import javax.management.Attribute;

import com.redhat.qe.katello.base.threading.KatelloCliWorker;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloPuppetModule extends _KatelloObject {

	public static final String CMD_LIST = "puppet_module list -v";
	public static final String CMD_INFO = "puppet_module info";
	public static final String CMD_SEARCH = "puppet_module search";

	public static final String ERR_NOT_FOUND = "Puppet module with id '%s' not found";

	public String id;
	public String org;
	public String repo, repo_id;
	public String product, product_id, product_label;
	public String environment;

	public KatelloPuppetModule(KatelloCliWorker krc, String org, String repo, String product) {
		this.org = org;
		this.repo = repo;
		this.product = product;
	}

	public SSHCommandResult list() {
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("repo", repo));
		opts.add(new Attribute("product", product));
		opts.add(new Attribute("environment", environment));
		return run(CMD_LIST);
	}

	public SSHCommandResult info() {
		opts.clear();
		opts.add(new Attribute("id", id));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("repo", repo));
		opts.add(new Attribute("product", product));
		return run(CMD_INFO);
	}

	public SSHCommandResult search(String query) {
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("repo", repo));
		opts.add(new Attribute("product", product));
		opts.add(new Attribute("query", query));
		return run(CMD_SEARCH);
	}
}
