package com.redhat.qe.katello.tests.e2e;

import java.util.logging.Logger;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloChangeset;
import com.redhat.qe.katello.base.obj.KatelloContentView;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloErrata;
import com.redhat.qe.katello.base.obj.KatelloPackage;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups={"cfse-e2e"}, singleThreaded = true)
public class PromoteChangeset extends KatelloCliTestBase {
	protected static Logger log = Logger.getLogger(PromoteChangeset.class.getName());
	
	private String uniqueID = KatelloUtils.getUniqueID();
	private String env1 = "DEV"+uniqueID;
	private String env2 = "GA"+uniqueID;
	private String env3 = "QE"+uniqueID;
	private String package_content_view;
	private String errata_content_view;
	
	@BeforeClass(description="Init unique names", alwaysRun=true)
	public void setUp(){
		SSHCommandResult res;
				
		KatelloEnvironment env = new KatelloEnvironment(this.cli_worker, this.env1, null, base_org_name, KatelloEnvironment.LIBRARY);
		res = env.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		env = new KatelloEnvironment(this.cli_worker, this.env2, null, base_org_name, this.env1);
		res = env.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		env = new KatelloEnvironment(this.cli_worker, this.env3, null, base_org_name, this.env2);
		res = env.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
	}

	
	@Test(description="Promote package to DEV")
	public void test_promotePackageToDEV(){
		package_content_view = KatelloUtils.promotePackagesToEnvironment(cli_worker, base_org_name, base_zoo_product_name, base_zoo_repo_name, new String[] {"lion"}, this.env1);
		
		KatelloPackage pack = new KatelloPackage(cli_worker, base_org_name, base_zoo_product_name, base_zoo_repo_name, package_content_view);
		SSHCommandResult res = pack.cli_list();
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").contains("lion"), "Check - package list output");
	}

	//@ TODO 918157
	@Test(description="Promote errata to DEV")
	public void test_promoteErrataToDEV(){
		errata_content_view = KatelloUtils.promoteErratasToEnvironment(cli_worker, base_org_name, base_zoo_product_name, base_zoo_repo_name, new String[] {PromoteErrata.ERRATA_ZOO_SEA}, this.env1);

		KatelloErrata err = new KatelloErrata(cli_worker, base_org_name, base_zoo_product_name, base_zoo_repo_name, errata_content_view);
		SSHCommandResult res = err.cli_list();
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").contains(PromoteErrata.ERRATA_ZOO_SEA), "Check - errata list output");
	}
	
	@Test(description="Promote package to GA", dependsOnMethods={"test_promotePackageToDEV"})
	public void test_promotePackageToGA(){
		KatelloContentView view = new KatelloContentView(cli_worker, package_content_view, base_org_name);
		exec_result = view.promote_view(this.env2);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		KatelloPackage pack = new KatelloPackage(cli_worker, base_org_name, base_zoo_product_name, base_zoo_repo_name, package_content_view);
		SSHCommandResult res = pack.cli_list();
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").contains("lion"), "Check - package list output");
	}

	//@ TODO 918157
	@Test(description="Promote errata to GA", dependsOnMethods={"test_promoteErrataToDEV"})
	public void test_promoteErrataToGA(){
		KatelloContentView view = new KatelloContentView(cli_worker, errata_content_view, base_org_name);
		exec_result = view.promote_view(this.env2);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		KatelloErrata err = new KatelloErrata(cli_worker, base_org_name, base_zoo_product_name, base_zoo_repo_name, errata_content_view);
		SSHCommandResult res = err.cli_list();
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").contains(PromoteErrata.ERRATA_ZOO_SEA), "Check - errata list output");
	}

	@Test(description="Delete package from GA", dependsOnMethods={"test_promotePackageToGA"})
	public void test_deletePackageFromGA(){
		KatelloChangeset changeset2 = new KatelloChangeset(cli_worker, "deletepackage"+uniqueID, base_org_name, this.env2, true);
		exec_result = changeset2.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = changeset2.update_addView(package_content_view);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");		
		exec_result = changeset2.apply();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	}

	@Test(description="Delete errata from GA", dependsOnMethods={"test_promoteErrataToGA"})
	public void test_deleteErrataFromGA(){
		KatelloChangeset changeset2 = new KatelloChangeset(cli_worker, "deleteerratas"+uniqueID, base_org_name, this.env2, true);
		exec_result = changeset2.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = changeset2.update_addView(errata_content_view);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");		
		exec_result = changeset2.apply();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	}
}
