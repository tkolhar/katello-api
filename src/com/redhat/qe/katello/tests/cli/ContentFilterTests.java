package com.redhat.qe.katello.tests.cli;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.obj.KatelloActivationKey;
import com.redhat.qe.katello.base.obj.KatelloContentDefinition;
import com.redhat.qe.katello.base.obj.KatelloContentFilter;
import com.redhat.qe.katello.base.obj.KatelloContentView;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.base.obj.helpers.FilterRuleErrataIds;
import com.redhat.qe.katello.base.obj.helpers.FilterRuleErrataDayType;
import com.redhat.qe.katello.base.obj.helpers.FilterRulePackage;
import com.redhat.qe.katello.base.obj.helpers.FilterRulePackageGroups;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.common.TngRunGroups;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups=TngRunGroups.TNG_KATELLO_Content)
public class ContentFilterTests extends KatelloCliTestScript {
	
	public static final String ERRATA_ZOO_SEA = "RHEA-2012:0002";
	public static final String REG_EMPTY_RULE = "\\{\\}";

	String uid = KatelloUtils.getUniqueID();
	String org_name = "orgcon-"+ uid;
	String env_name = "envcon-"+ uid;
	String prov_name = "provcon-" + uid;
	String prod_name = "prodcon-"+ uid;
	String repo_name = "repocon-" + uid;
	String condef_name = "condef-" + uid;
	String filter_name = "filter";
	String filter_delete = "filter to delete";
	String pubview_name = "pubview-" + uid;
	String system_name1 = "system-" + uid;
	String act_key_name = "act_key-" + uid;
	
	SSHCommandResult exec_result;
	KatelloOrg org;
	KatelloEnvironment env;
	KatelloProvider prov;
	KatelloProduct prod;
	KatelloRepo repo;
	KatelloContentDefinition condef;
	KatelloContentView conview;
	KatelloActivationKey act_key;
	KatelloSystem sys;
	String system_uuid1;
	
	@BeforeClass(description="Generate unique objects")
	public void setUp() {
		org = new KatelloOrg(org_name,null);
		exec_result = org.cli_create();		              
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		env = new KatelloEnvironment(env_name,null,org_name,KatelloEnvironment.LIBRARY);
		exec_result = env.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		prov = new KatelloProvider(prov_name,org_name,null,null);
		exec_result = prov.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		prod = new KatelloProduct(prod_name,org_name,prov_name,null, null, null,null, null);
		exec_result = prod.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		repo = new KatelloRepo(repo_name,org_name,prod_name,REPO_INECAS_ZOO3, null, null);
		exec_result = repo.create(true);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = prod.promote(env_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	
		
		exec_result = repo.synchronize();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		condef = new KatelloContentDefinition(condef_name,null,org_name,null);
		exec_result = condef.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		exec_result = condef.add_product(prod_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		KatelloContentFilter filter = new KatelloContentFilter(filter_name, org_name, condef_name);
		exec_result = filter.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	}


	@Test(description="create content definition filter")
	void test_filterCreate() {
		KatelloContentFilter filter = new KatelloContentFilter(filter_delete, org_name, condef_name);
		exec_result = filter.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloContentFilter.OUT_CREATED, filter_delete)), "Check output");
	}

	@Test(description="delete content definition filter",dependsOnMethods={"test_filterCreate"})
	void test_filterDelete() {
		KatelloContentFilter filter = new KatelloContentFilter(filter_delete, org_name, condef_name);
		exec_result = filter.delete();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloContentFilter.OUT_DELETED, filter_delete)), "Check output");
	}


	@Test(description="add product to filter")
	void test_filterAddProduct() {
		KatelloContentFilter filter = new KatelloContentFilter(filter_name, org_name, condef_name);
		exec_result = filter.add_product(prod_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloContentFilter.OUT_ADD_PRODUCT, prod_name, filter_name)), "Check output");
	}

	@Test(description="remove product from filter",dependsOnMethods={"test_filterAddProduct"})
	void test_filterRemoveProduct() {
		KatelloContentFilter filter = new KatelloContentFilter(filter_name, org_name, condef_name);
		exec_result = filter.remove_product(prod_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloContentFilter.OUT_REMOVE_PRODUCT, prod_name, filter_name)), "Check output");
	}

	@Test(description="add repository to filter")
	void test_filterAddRepo() {
		KatelloContentFilter filter = new KatelloContentFilter(filter_name, org_name, condef_name);
		exec_result = filter.add_repo(prod_name, repo_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloContentFilter.OUT_ADD_REPO, repo_name, filter_name)), "Check output");
	}

	@Test(description="remove repository from filter",dependsOnMethods={"test_filterAddRepo"})
	void test_filterRemoveRepo() {
		KatelloContentFilter filter = new KatelloContentFilter(filter_name, org_name, condef_name);
		exec_result = filter.remove_repo(prod_name, repo_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloContentFilter.OUT_REMOVE_REPO, repo_name, filter_name)), "Check output");
	}


	@Test(description="create errata include filter rule with date and type")
	public void test_includeErrataFilterDayType() {
		String [] errata_types = {KatelloContentFilter.ERRATA_TYPE_BUGFIX};
		KatelloContentFilter filter = new KatelloContentFilter(filter_name, org_name, condef_name);

		FilterRuleErrataDayType errata1 = new FilterRuleErrataDayType(null, null, errata_types);
		FilterRuleErrataDayType errata2 = new FilterRuleErrataDayType("2013-04-15", "2014-04-16", errata_types);
		FilterRuleErrataDayType errata3 = new FilterRuleErrataDayType("2013-04-15", "2012-04-15", null);

		exec_result = filter.add_rule(KatelloContentFilter.TYPE_INCLUDES, errata1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		exec_result = filter.add_rule(KatelloContentFilter.TYPE_INCLUDES, errata2);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		exec_result = filter.add_rule(KatelloContentFilter.TYPE_INCLUDES, errata3);
		Assert.assertFalse(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).equals(KatelloContentFilter.ERR_ERRATA_DATE), "Check - error message");

		assert_filterInfo(filter_name, KatelloContentFilter.CONTENT_ERRATUM, KatelloContentFilter.TYPE_INCLUDES, errata1.ruleRegExp());
		assert_filterInfo(filter_name, KatelloContentFilter.CONTENT_ERRATUM, KatelloContentFilter.TYPE_INCLUDES, errata2.ruleRegExp());
	}

	@Test(description="create errata include filter rule with errata ids")
	public void test_includeErrataFilterIds() {
		KatelloContentFilter filter = new KatelloContentFilter(filter_name, org_name, condef_name);

		String ids [] = { "RHEA-2012:0002", "RHEA-2012:0003" };
		FilterRuleErrataIds errata = new FilterRuleErrataIds(ids);

		exec_result = filter.add_rule(KatelloContentFilter.TYPE_INCLUDES, errata);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		exec_result = filter.add_rule("{}", KatelloContentFilter.CONTENT_ERRATUM, KatelloContentFilter.TYPE_INCLUDES);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		assert_filterInfo(filter_name, KatelloContentFilter.CONTENT_ERRATUM, KatelloContentFilter.TYPE_INCLUDES, errata.ruleRegExp());
		assert_filterInfo(filter_name, KatelloContentFilter.CONTENT_ERRATUM, KatelloContentFilter.TYPE_INCLUDES, REG_EMPTY_RULE);
	}

	@Test(description="create errata exclude filter rule with date and type")
	public void test_excludeErrataFilterDayType() {
		String [] errata_types = {KatelloContentFilter.ERRATA_TYPE_BUGFIX};
		KatelloContentFilter filter = new KatelloContentFilter(filter_name, org_name, condef_name);

		FilterRuleErrataDayType errata1 = new FilterRuleErrataDayType(null, null, errata_types);
		FilterRuleErrataDayType errata2 = new FilterRuleErrataDayType("2013-04-15", "2014-04-16", errata_types);
		FilterRuleErrataDayType errata3 = new FilterRuleErrataDayType("2013-04-15", "2012-04-15", null);

		exec_result = filter.add_rule(KatelloContentFilter.TYPE_EXCLUDES, errata1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		exec_result = filter.add_rule(KatelloContentFilter.TYPE_EXCLUDES, errata2);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		exec_result = filter.add_rule(KatelloContentFilter.TYPE_EXCLUDES, errata3);
		Assert.assertTrue(exec_result.getExitCode() != 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).equals(KatelloContentFilter.ERR_ERRATA_DATE), "Check - error message");

		assert_filterInfo(filter_name, KatelloContentFilter.CONTENT_ERRATUM, KatelloContentFilter.TYPE_EXCLUDES, errata1.ruleRegExp());
		assert_filterInfo(filter_name, KatelloContentFilter.CONTENT_ERRATUM, KatelloContentFilter.TYPE_EXCLUDES, errata2.ruleRegExp());
	}

	@Test(description="create errata exclude filter rule")
	public void test_excludeErrataFilterIds() {
		KatelloContentFilter filter = new KatelloContentFilter(filter_name, org_name, condef_name);

		String ids [] = { "RHEA-2012:0002", "RHEA-2012:0003" };
		FilterRuleErrataIds errata = new FilterRuleErrataIds(ids);

		exec_result = filter.add_rule(KatelloContentFilter.TYPE_EXCLUDES, errata);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		exec_result = filter.add_rule(KatelloContentFilter.TYPE_EXCLUDES, new FilterRuleErrataIds());
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		assert_filterInfo(filter_name, KatelloContentFilter.CONTENT_ERRATUM, KatelloContentFilter.TYPE_EXCLUDES, errata.ruleRegExp());
		assert_filterInfo(filter_name, KatelloContentFilter.CONTENT_ERRATUM, KatelloContentFilter.TYPE_EXCLUDES, REG_EMPTY_RULE);
	}

	//@ TODO bug 956151
	@Test(description="filter add_rule - check output message")
	public void test_filterAddRuleOutput() {
		KatelloContentFilter filter = new KatelloContentFilter(filter_name, org_name, condef_name);
		exec_result = filter.add_rule("{}", KatelloContentFilter.CONTENT_ERRATUM, KatelloContentFilter.TYPE_INCLUDES);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		// TODO check output message after bugfix^
		Assert.assertFalse(getOutput(exec_result).equals(""), "Check - output ");
	}

	//@ TODO bug 956151
	@Test(description="remove rule from filter")
	public void test_removeRule() {
		KatelloContentFilter filter = new KatelloContentFilter(filter_name, org_name, condef_name);
		exec_result = filter.info();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		String rule_id = KatelloCli.grepCLIOutput("    Id", getOutput(exec_result).trim(),1);
		exec_result = filter.remove_rule(rule_id);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		// TODO check output message after bugfix^
		Assert.assertFalse(getOutput(exec_result).equals(""), "Check - output ");
	}

	@Test(description="add include package_groups filter rules")
	public void test_includePackageGroupFilter() {
		KatelloContentFilter filter = new KatelloContentFilter(filter_name, org_name, condef_name);

		FilterRulePackageGroups groups = new FilterRulePackageGroups("group1", "group2", "group3");

		exec_result = filter.add_rule(KatelloContentFilter.TYPE_INCLUDES, groups);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		exec_result = filter.add_rule(KatelloContentFilter.TYPE_INCLUDES, new FilterRulePackageGroups());
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		assert_filterInfo(filter_name, KatelloContentFilter.CONTENT_PACKAGE_GROUP, KatelloContentFilter.TYPE_INCLUDES, groups.ruleRegExp());
		assert_filterInfo(filter_name, KatelloContentFilter.CONTENT_PACKAGE_GROUP, KatelloContentFilter.TYPE_INCLUDES, REG_EMPTY_RULE);
	}

	@Test(description="add exclude package_groups filter rules")
	public void test_excludePackageGroupFilter() {
		KatelloContentFilter filter = new KatelloContentFilter(filter_name, org_name, condef_name);

		FilterRulePackageGroups groups = new FilterRulePackageGroups("group4", "group5", "group6");

		exec_result = filter.add_rule(KatelloContentFilter.TYPE_EXCLUDES, groups);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		exec_result = filter.add_rule(KatelloContentFilter.TYPE_EXCLUDES, new FilterRulePackageGroups());
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		assert_filterInfo(filter_name, KatelloContentFilter.CONTENT_PACKAGE_GROUP, KatelloContentFilter.TYPE_EXCLUDES, groups.ruleRegExp());
		assert_filterInfo(filter_name, KatelloContentFilter.CONTENT_PACKAGE_GROUP, KatelloContentFilter.TYPE_EXCLUDES, REG_EMPTY_RULE);
	}

	@Test(description="add include package filter rules")
	public void test_includePackageFilter() {
		KatelloContentFilter filter = new KatelloContentFilter(filter_name, org_name, condef_name);

		FilterRulePackage [] packages = {
			new FilterRulePackage("camel"),
			new FilterRulePackage("zebra", "0.1.0", null, null),
			new FilterRulePackage("lion", null, "1.2.0", null),
			new FilterRulePackage("tiger", null, null, "3.3.0"),
			new FilterRulePackage("walrus", null, "2.5.0", "4.2.0"),
		};

		exec_result = filter.add_rule(KatelloContentFilter.TYPE_INCLUDES, packages);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		exec_result = filter.add_rule(KatelloContentFilter.TYPE_INCLUDES, new FilterRulePackage [] {});
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		assert_filterInfo(filter_name, KatelloContentFilter.CONTENT_PACKAGE, KatelloContentFilter.TYPE_INCLUDES, REG_EMPTY_RULE);
		assert_filterInfo(filter_name, KatelloContentFilter.CONTENT_PACKAGE, KatelloContentFilter.TYPE_INCLUDES, FilterRulePackage.ruleRegExp(packages));
	}

	@Test(description="add exclude package filter rules")
	public void test_excludePackageFilter() {
		KatelloContentFilter filter = new KatelloContentFilter(filter_name, org_name, condef_name);

		FilterRulePackage [] packages = {
			new FilterRulePackage("elephant"),
			new FilterRulePackage("penguin", "1.2.0", null, null),
			new FilterRulePackage("bear", null, "1.6.0", null),
			new FilterRulePackage("cow", null, null, "4.1.0"),
			new FilterRulePackage("fox", null, "2.2.0", "2.4.0"),
		};

		exec_result = filter.add_rule(KatelloContentFilter.TYPE_EXCLUDES, packages);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		exec_result = filter.add_rule(KatelloContentFilter.TYPE_EXCLUDES, new FilterRulePackage [] {});
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		assert_filterInfo(filter_name, KatelloContentFilter.CONTENT_PACKAGE, KatelloContentFilter.TYPE_EXCLUDES, REG_EMPTY_RULE);
		assert_filterInfo(filter_name, KatelloContentFilter.CONTENT_PACKAGE, KatelloContentFilter.TYPE_EXCLUDES, FilterRulePackage.ruleRegExp(packages));
	}

	@AfterClass
	public void tearDown() {
		exec_result = org.delete();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	}


	private void assert_filterInfo(String filter_name, String filter_content, String rule_type, String ruleRegExp) {
		KatelloContentFilter filter = new KatelloContentFilter(filter_name, org_name, condef_name);
		SSHCommandResult res = filter.info();
		String output = getOutput(res).replaceAll("\n", " ");
		Pattern pattern = Pattern.compile(String.format(KatelloContentFilter.REG_FILTER_INFO, filter_content, rule_type));
		Matcher matcher = pattern.matcher(output);
		boolean found = false;
		// for all found rules
		while(matcher.find()) {
			String rule_found = matcher.group(1).replaceAll("(u'|')", ""); // remove u', ' from found rule
			// found rule matches given regular expression ?
			if(rule_found.matches(ruleRegExp)) {
				found = true;
				break;
			}
		}
		Assert.assertTrue(found, "Check - filter contains given rule");
	}

}
