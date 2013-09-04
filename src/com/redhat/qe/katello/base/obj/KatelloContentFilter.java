package com.redhat.qe.katello.base.obj;

import java.util.logging.Logger;

import javax.management.Attribute;

import com.redhat.qe.tools.SSHCommandResult;
import com.redhat.qe.katello.base.obj.helpers.FilterRuleErrataIds;
import com.redhat.qe.katello.base.obj.helpers.FilterRuleErrataDayType;
import com.redhat.qe.katello.base.obj.helpers.FilterRulePackage;
import com.redhat.qe.katello.base.obj.helpers.FilterRulePackageGroups;
import com.redhat.qe.katello.base.threading.KatelloCliWorker;

public class KatelloContentFilter extends _KatelloObject{
    protected static Logger log = Logger.getLogger(KatelloContentFilter.class.getName());
	
	public static final String CMD_CREATE = "content definition filter create";
	public static final String CMD_DELETE = "content definition filter delete";
	public static final String CMD_ADD_RULE = "content definition filter add_rule";
	public static final String CMD_REMOVE_RULE = "content definition filter remove_rule";
	public static final String CMD_ADD_PRODUCT = "content definition filter add_product";
	public static final String CMD_ADD_REPO = "content definition filter add_repo";
	public static final String CMD_REMOVE_PRODUCT = "content definition filter remove_product";
	public static final String CMD_REMOVE_REPO = "content definition filter remove_repo";
	public static final String CMD_INFO = "content definition filter info";
	public static final String CMD_LIST = "content definition filter list";

	public static final String OUT_CREATED = "Successfully created filter [ %s ]";
	public static final String OUT_DELETED = "Successfully deleted filter [ %s ]";
	public static final String OUT_ADD_PRODUCT = "Added product [ %s ] to filter [ %s ]";
	public static final String OUT_ADD_REPO = "Added repository [ %s ] to filter [ %s ]";
	public static final String OUT_REMOVE_PRODUCT  = "Removed product [ %s ] from filter [ %s ]";
	public static final String OUT_REMOVE_REPO = "Removed repository [ %s ] from filter [ %s ]";
	public static final String OUT_ADD_RULE = "Successfully created rule [ %s ]";
	public static final String OUT_REMOVE_RULE = "Successfully removed rule [ %s ]";

	public static final String ERR_ERRATA_DATE = "Validation failed: Parameters Invalid date range. The erratum rule start date must come before the end date";
	public static final String ERR_NOT_FOUND = "Could not find filter [ %s ].";
	
	public static final String TYPE_EXCLUDES = "excludes";
	public static final String TYPE_INCLUDES = "includes";
	public static final String CONTENT_PACKAGE = "rpm";
	public static final String CONTENT_PACKAGE_GROUP = "package_group";
	public static final String CONTENT_ERRATUM = "erratum";
	public static final String ERRATA_TYPE_ENHANCEMENT = "enhancement";
	public static final String ERRATA_TYPE_SECURITY = "security";
	public static final String ERRATA_TYPE_BUGFIX = "bugfix";
	
	public static final String REG_FILTER_INFO = "Id: [0-9]+\\s+Content: %s\\s+Type: %s\\s+Rule:\\s+(\\{((?!Id:).)*\\})";

	// ** ** ** ** ** ** ** Class members
	public String name;
	public String definition;
	private Long id;
	public String org;
	
	public KatelloContentFilter(){super();}
	
	public KatelloContentFilter(KatelloCliWorker kcr, String pName, String pOrg, String pDefinition){
		this.name = pName;
		this.org = pOrg;
		this.definition = pDefinition;
		this.kcr = kcr;
	}

	public Long getId() {
	    return id;
	}
	
	public void setId(Long id) {
	    this.id = id;
	}
	
	public String getName() {
	    return name;
	}
	
	public void setName(String name) {
	    this.name = name;
	}

	public SSHCommandResult create() {		
		opts.clear();
		opts.add(new Attribute("name", this.name));
		opts.add(new Attribute("definition", this.definition));
		opts.add(new Attribute("org", this.org));
		return run(CMD_CREATE);
	}

	public SSHCommandResult delete() {
		opts.clear();
		opts.add(new Attribute("name", this.name));
		opts.add(new Attribute("definition", this.definition));
		opts.add(new Attribute("org", this.org));
		return run(CMD_DELETE);
	}

	public SSHCommandResult info() {
		opts.clear();
		opts.add(new Attribute("org", this.org));
		opts.add(new Attribute("definition", this.definition));
		opts.add(new Attribute("name", this.name));
		opts.add(new Attribute("id", this.id));
		return run(CMD_INFO);
	}

	public SSHCommandResult list() {
		opts.clear();
		opts.add(new Attribute("org", this.org));
		opts.add(new Attribute("definition", this.definition));
		return run(CMD_LIST);
	}

	public SSHCommandResult add_rule(String rule, String content, String type) {
		opts.clear();
		opts.add(new Attribute("org", this.org));
		opts.add(new Attribute("definition", this.definition));
		opts.add(new Attribute("name", this.name));
		opts.add(new Attribute("content", content));
		opts.add(new Attribute("type", type));
		opts.add(new Attribute("rule", rule));
		return run(CMD_ADD_RULE);
	}

	public SSHCommandResult add_rule(String type, FilterRulePackageGroups groups) {
		return add_rule(groups.filterRule(), CONTENT_PACKAGE_GROUP, type);
	}

	public SSHCommandResult add_rule(String type, FilterRulePackage [] packages) {
		return add_rule(FilterRulePackage.filterRule(packages), CONTENT_PACKAGE, type);
	}

	public SSHCommandResult add_rule(String type, FilterRuleErrataIds errata) {
		return add_rule(errata.filterRule(), CONTENT_ERRATUM, type);
	}

	public SSHCommandResult add_rule(String type, FilterRuleErrataDayType errata) {
		return add_rule(errata.filterRule(), CONTENT_ERRATUM, type);
	}

	public SSHCommandResult remove_rule(String rule_id) {
		opts.clear();
		opts.add(new Attribute("org", this.org));
		opts.add(new Attribute("definition", this.definition));
		opts.add(new Attribute("name", this.name));
		opts.add(new Attribute("rule_id", rule_id));
		return run(CMD_REMOVE_RULE);
	}

	public SSHCommandResult add_product(String product) {
		opts.clear();
		opts.add(new Attribute("org", this.org));
		opts.add(new Attribute("definition", this.definition));
		opts.add(new Attribute("name", this.name));
		opts.add(new Attribute("product", product));
		return run(CMD_ADD_PRODUCT);
	}

	public SSHCommandResult remove_product(String product) {
		opts.clear();
		opts.add(new Attribute("org", this.org));
		opts.add(new Attribute("definition", this.definition));
		opts.add(new Attribute("name", this.name));
		opts.add(new Attribute("product", product));
		return run(CMD_REMOVE_PRODUCT);
	}

	public SSHCommandResult add_repo(String product, String repo) {
		opts.clear();
		opts.add(new Attribute("org", this.org));
		opts.add(new Attribute("definition", this.definition));
		opts.add(new Attribute("name", this.name));
		opts.add(new Attribute("product", product));
		opts.add(new Attribute("repo", repo));
		return run(CMD_ADD_REPO);
	}

	public SSHCommandResult remove_repo(String product, String repo) {
		opts.clear();
		opts.add(new Attribute("org", this.org));
		opts.add(new Attribute("definition", this.definition));
		opts.add(new Attribute("name", this.name));
		opts.add(new Attribute("product", product));
		opts.add(new Attribute("repo", repo));
		return run(CMD_REMOVE_REPO);
	}

	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **
}
