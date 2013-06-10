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
	
//	private String ert1;
//	
//	@Test(description="promote rhel packages")
//	public void test_promoteRHELPackages() {
//		KatelloUtils.promotePackagesToEnvironment(org_name, KatelloProduct.RHEL_SERVER, KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT, new String[] {"zsh"}, env_name2);
//	}
//	
//	@Test(description="list rhel repo packages promoted to test environment", dependsOnMethods={"test_promoteRHELPackages"})
//	public void test_listRHELRepoPackages() {
//		KatelloPackage pack = new KatelloPackage(null, null, org_name, KatelloProduct.RHEL_SERVER, KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT, env_name2);
//		exec_result = pack.cli_search("*zsh*");
//		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
//		Assert.assertTrue(getOutput(exec_result).trim().contains("zsh"), "Package zsh is listed in environment package list");
//	}
//
//	@Test(description="delete promoted rhel packages", dependsOnMethods={"test_listRHELRepoPackages"})
//	public void test_deleteRHELPackages() {
//		KatelloUtils.removePackagesFromEnvironment(org_name, KatelloProduct.RHEL_SERVER, KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT, new String[] {"zsh"}, env_name2);
//	}
//	
//	//@ TODO enable when bug 918093 is fixed
//	@Test(description="list rhel repo packages deleted to test environment", dependsOnMethods={"test_deleteRHELPackages"}, enabled=true)
//	public void test_listRHELRepoPackagesDeleted() {
//		KatelloPackage pack = new KatelloPackage(null, null, org_name, KatelloProduct.RHEL_SERVER, KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT, env_name2);
//		exec_result = pack.cli_search("*zsh*");
//		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
//		Assert.assertFalse(getOutput(exec_result).trim().contains("zsh"), "Package zsh is not listed in environment package list");
//	}
//	
//	@Test(description="promote rhel distr")
//	public void test_promoteRHELDistr() {
//		KatelloChangeset cs = new KatelloChangeset("package-promote"+KatelloUtils.getUniqueID(), org_name, env_name2);
//		exec_result = cs.create();
//		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (changeset create)");
//		
//		exec_result = cs.update_add_distr(KatelloProduct.RHEL_SERVER, "ks-Red Hat Enterprise Linux-Server-6.4-x86_64");
//		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (changeset update add package)");
//		
//		exec_result = cs.apply();
//		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (changeset promote)");		
//	}
//	
//	@Test(description="install zsh package")
//	public void test_installRHELPackage() {
//		KatelloUtils.sshOnClient(client_name3, "yum clean all");
//		exec_result = KatelloUtils.sshOnClient(client_name3, "yum repolist");
//		Assert.assertTrue(getOutput(exec_result).trim().contains("Red Hat Enterprise Linux Server"), "Contains RHEL repo");
//		KatelloUtils.sshOnClient(client_name3, "yum erase -y zsh");
//		exec_result = KatelloUtils.sshOnClient(client_name3, "yum install -y zsh beaker*");
//		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code (yum install zsh)");
//		exec_result = KatelloUtils.sshOnClient(client_name3, "rpm -q zsh");
//		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code (rpm -q zsh)");
//	}
//	
//	@Test(description="promote rhel errata")
//	public void test_promoteRHELErrata() {
//		
//		KatelloSystem sys = new KatelloSystem(system_name, org_name, null);
//		exec_result = sys.list_errata_names("RHBA");
//		ert1 = getOutput(exec_result).replaceAll("\n", ",").split(",")[0];
//	
//		KatelloUtils.promoteErratasToEnvironment(org_name, KatelloProduct.RHEL_SERVER, KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT, new String[] {ert1}, env_name2);
//	}
//	
//	@Test(description="list rhel repo errata promoted to test environment", dependsOnMethods={"test_promoteRHELErrata"})
//	public void test_listRHELRepoErrata() {
//		KatelloErrata errata = new KatelloErrata(null, org_name, KatelloProduct.RHEL_SERVER, KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT, env_name2, "bugfix");
//		exec_result = errata.cli_list();
//		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
//		Assert.assertTrue(getOutput(exec_result).trim().contains(ert1), "Errata " + ert1 + " is listed in environment errata list");
//	}
//
//	@Test(description="delete promoted rhel ettata", dependsOnMethods={"test_listRHELRepoErrata"})
//	public void test_deleteRHELErrata() {
//		KatelloUtils.removeErratasFromEnvironment(org_name, KatelloProduct.RHEL_SERVER, KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT, new String[] {ert1}, env_name2);
//	}
//	
//	@Test(description="list rhel repo errata deleted to test environment", dependsOnMethods={"test_deleteRHELErrata"})
//	public void test_listRHELRepoErrataDeleted() {
//		KatelloErrata errata = new KatelloErrata(null, org_name, KatelloProduct.RHEL_SERVER, KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT, env_name2, "bugfix");
//		exec_result = errata.cli_list();
//		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
//		Assert.assertFalse(getOutput(exec_result).trim().contains(ert1), "Errata " + ert1 + " is not listed in environment errata list");
//	}
}
