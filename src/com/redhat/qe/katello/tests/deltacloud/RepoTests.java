package com.redhat.qe.katello.tests.deltacloud;

import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.obj.KatelloChangeset;
import com.redhat.qe.katello.base.obj.KatelloErrata;
import com.redhat.qe.katello.base.obj.KatelloPackage;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.common.KatelloUtils;

@Test(dependsOnGroups="cfse-pack")
public class RepoTests extends BaseDeltacloudTest {
	
	private String ert1;
	
	@Test(description="promote rhel packages")
	public void test_promoteRHELPackages() {
		KatelloChangeset cs = new KatelloChangeset("package-promote"+KatelloUtils.getUniqueID(), org_name, env_name2);
		cs.runOn(client_name);
		exec_result = cs.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (changeset create)");
		
		exec_result = cs.update_add_package(KatelloProduct.RHEL_SERVER, "zsh");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (changeset update add package)");
		
		exec_result = cs.apply();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (changeset promote)");		
	}
	
	@Test(description="list rhel repo packages promoted to test environment", dependsOnMethods={"test_promoteRHELPackages"})
	public void test_listRHELRepoPackages() {
		KatelloPackage pack = new KatelloPackage(null, null, org_name, KatelloProduct.RHEL_SERVER, KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT, env_name2);
		pack.runOn(client_name);
		exec_result = pack.cli_search("*zsh*");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).trim().contains("zsh"), "Package zsh is listed in environment package list");
	}

	@Test(description="delete promoted rhel packages", dependsOnMethods={"test_listRHELRepoPackages"})
	public void test_deleteRHELPackages() {
		KatelloChangeset cs = new KatelloChangeset("package-delete"+KatelloUtils.getUniqueID(), org_name, env_name2, true);
		cs.runOn(client_name);
		exec_result = cs.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (changeset create)");
		
		exec_result = cs.update_add_package(KatelloProduct.RHEL_SERVER, "zsh");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (changeset update add package)");
		
		exec_result = cs.apply();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (changeset promote)");
	}
	
	@Test(description="list rhel repo packages deleted to test environment", dependsOnMethods={"test_deleteRHELPackages"})
	public void test_listRHELRepoPackagesDeleted() {
		KatelloPackage pack = new KatelloPackage(null, null, org_name, KatelloProduct.RHEL_SERVER, KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT, env_name2);
		pack.runOn(client_name);
		exec_result = pack.cli_search("*zsh*");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertFalse(getOutput(exec_result).trim().contains("zsh"), "Package zsh is not listed in environment package list");
	}
	
	@Test(description="promote rhel distr")
	public void test_promoteRHELDistr() {
		KatelloChangeset cs = new KatelloChangeset("package-promote"+KatelloUtils.getUniqueID(), org_name, env_name2);
		cs.runOn(client_name);
		exec_result = cs.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (changeset create)");
		
		exec_result = cs.update_add_distr(KatelloProduct.RHEL_SERVER, "ks-Red Hat Enterprise Linux-Server-6.4-x86_64");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (changeset update add package)");
		
		exec_result = cs.apply();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (changeset promote)");		
	}
	
	@Test(description="promote rhel errata")
	public void test_promoteRHELErrata() {
		
		KatelloSystem sys = new KatelloSystem(system_name, org_name, null);
		sys.runOn(client_name);
		exec_result = sys.list_errata_names("RHBA");
		ert1 = getOutput(exec_result).replaceAll("\n", ",").split(",")[0];
		
		KatelloChangeset cs = new KatelloChangeset("errata-promote"+KatelloUtils.getUniqueID(), org_name, env_name2);
		cs.runOn(client_name);
		exec_result = cs.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (changeset create)");
		
		exec_result = cs.update_fromProduct_addErrata(KatelloProduct.RHEL_SERVER, ert1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (changeset update add errata)");
		
		exec_result = cs.apply();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (changeset promote)");	
	}
	
	@Test(description="list rhel repo errata promoted to test environment", dependsOnMethods={"test_promoteRHELErrata"})
	public void test_listRHELRepoErrata() {
		KatelloErrata errata = new KatelloErrata(null, org_name, KatelloProduct.RHEL_SERVER, KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT, env_name2, "bugfix");
		errata.runOn(client_name);
		exec_result = errata.cli_list();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).trim().contains(ert1), "Errata " + ert1 + " is listed in environment errata list");
	}

	@Test(description="delete promoted rhel ettata", dependsOnMethods={"test_listRHELRepoErrata"})
	public void test_deleteRHELErrata() {
		KatelloChangeset cs = new KatelloChangeset("errata-delete"+KatelloUtils.getUniqueID(), org_name, env_name2, true);
		cs.runOn(client_name);
		exec_result = cs.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (changeset create)");
		
		exec_result = cs.update_fromProduct_addErrata(KatelloProduct.RHEL_SERVER, ert1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (changeset update add errata)");
		
		exec_result = cs.apply();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (changeset promote)");
	}
	
	@Test(description="list rhel repo errata deleted to test environment", dependsOnMethods={"test_deleteRHELErrata"})
	public void test_listRHELRepoErrataDeleted() {
		KatelloErrata errata = new KatelloErrata(null, org_name, KatelloProduct.RHEL_SERVER, KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT, env_name2, "bugfix");
		errata.runOn(client_name);
		exec_result = errata.cli_list();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertFalse(getOutput(exec_result).trim().contains(ert1), "Errata " + ert1 + " is not listed in environment errata list");
	}
}
