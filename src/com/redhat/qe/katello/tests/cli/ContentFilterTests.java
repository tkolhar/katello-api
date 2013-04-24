package com.redhat.qe.katello.tests.cli;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.obj.KatelloContentDefinition;
import com.redhat.qe.katello.base.obj.KatelloContentFilter;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.common.TngRunGroups;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups=TngRunGroups.TNG_KATELLO_Content)
public class ContentFilterTests extends KatelloCliTestScript{
	
	public static final String ERRATA_ZOO_SEA = "RHEA-2012:0002";
	
	String uid = KatelloUtils.getUniqueID();
	String org_name = "orgcon-"+ uid;
	String env_name = "envcon-"+ uid;
	String prov_name = "provcon-" + uid;
	String prod_name = "prodcon-"+ uid;
	String repo_name = "repocon-" + uid;
	String condef_name = "condef-" + uid;
	String filter_name = "filter";

	
	SSHCommandResult exec_result;
	KatelloOrg org;
	KatelloEnvironment env;
	KatelloProvider prov;
	KatelloProduct prod;
	KatelloRepo repo;
	KatelloContentDefinition condef;
	
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

		exec_result = filter.add_rule("{}", KatelloContentFilter.CONTENT_ERRATUM, KatelloContentFilter.TYPE_INCLUDES);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	}


	@Test(description="create content definition filter")
	void test_filterCreate() {
		KatelloContentFilter filter = new KatelloContentFilter("testfilter", org_name, condef_name);
		exec_result = filter.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloContentFilter.OUT_CREATED, "testfilter")), "Check output");
	}

	@Test(description="delete content definition filter",dependsOnMethods={"test_filterCreate"})
	void test_filterDelete() {
		KatelloContentFilter filter = new KatelloContentFilter("testfilter", org_name, condef_name);
		exec_result = filter.delete();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloContentFilter.OUT_DELETED, "testfilter")), "Check output");
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


	//@ TODO bug 955612
	@Test(description="create errata include filter rule with date and type")
	public void test_includeErrataFilterDayType() {
		String [] errata_types = {KatelloContentFilter.ERRATA_TYPE_BUGFIX};
		KatelloContentFilter filter = new KatelloContentFilter(filter_name, org_name, condef_name);

		exec_result = filter.add_rule_errata(KatelloContentFilter.TYPE_INCLUDES, null, null, errata_types);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		exec_result = filter.add_rule_errata(KatelloContentFilter.TYPE_INCLUDES, "2013-04-15", "2014-04-16", errata_types);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		// TODO test start_date > end_date
		exec_result = filter.add_rule_errata(KatelloContentFilter.TYPE_INCLUDES, "2013-04-15", "2012-04-15", null);
		Assert.assertFalse(exec_result.getExitCode() == 0, "Check - return code");
	}

	@Test(description="create errata include filter rule with errata ids")
	public void test_includeErrataFilterIds() {
		String [] errata = {"RHEA-2012:0002", "RHEA-2012:0003"};
		KatelloContentFilter filter = new KatelloContentFilter(filter_name, org_name, condef_name);

		exec_result = filter.add_rule_errata(KatelloContentFilter.TYPE_INCLUDES, errata);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		exec_result = filter.add_rule("{}", KatelloContentFilter.CONTENT_ERRATUM, KatelloContentFilter.TYPE_INCLUDES);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	}

	//@ TODO bug 955612
	@Test(description="create errata exclude filter rule with date and type")
	public void test_excludeErrataFilterDayType() {
		String [] errata_types = {KatelloContentFilter.ERRATA_TYPE_BUGFIX};
		KatelloContentFilter filter = new KatelloContentFilter(filter_name, org_name, condef_name);

		exec_result = filter.add_rule_errata(KatelloContentFilter.TYPE_EXCLUDES, null, null, errata_types);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		exec_result = filter.add_rule_errata(KatelloContentFilter.TYPE_EXCLUDES, "2013-04-15", "2014-04-16", errata_types);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		// TODO test start_date > end_date
		exec_result = filter.add_rule_errata(KatelloContentFilter.TYPE_EXCLUDES, "2013-04-15", "2012-04-15", null);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	}

	@Test(description="create errata exclude filter rule")
	public void test_excludeErrataFilterIds() {
		KatelloContentFilter filter = new KatelloContentFilter(filter_name, org_name, condef_name);

		String [] errata = {"RHEA-2012:0002", "RHEA-2012:0003"};
		exec_result = filter.add_rule_errata(KatelloContentFilter.TYPE_EXCLUDES, errata);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		exec_result = filter.add_rule("{}", KatelloContentFilter.CONTENT_ERRATUM, KatelloContentFilter.TYPE_EXCLUDES);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
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
	
	@AfterClass
	public void tearDown() {
		exec_result = org.delete();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	}

}
