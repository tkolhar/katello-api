package com.redhat.qe.katello.tests.e2e;

import java.util.logging.Logger;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.obj.KatelloChangeset;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloPackage;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups={"cfse-e2e"})
public class ChangesetMultyPackageDelete extends KatelloCliTestScript {
	
	protected static Logger log = Logger.getLogger(ChangesetMultyPackageDelete.class.getName());
	
	private String org_name;
	private String env_name;
	private String system_name;
	private String provider_name;
	private String product_name;
	private String repo_name;
	private String provider_name2;
	private String product_name2;
	private String repo_name2;
	private String chst_name;
	private String delchst_name;
	private String readdchst_name;
	private String readdchst_name2;
	private String package_name1 = "lion";
	private String package_name2 = "akuma-debuginfo";
	
	SSHCommandResult exec_result;
	
	@BeforeClass(description="init: create all stuff")
	public void setUp() {
		
		String uid = KatelloUtils.getUniqueID();
		org_name = "org" + uid;
		
		// Create org:
		KatelloOrg org = new KatelloOrg(this.org_name, "Package tests");
		exec_result = org.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	}
	
	@Test(description = "Create changeset of deletion type," +
			" then add already promoted packages to changeset and promote it," +
			" verify that packages does not exist in environment anymore")
	public void test_deletionChangesetRemovePackages() {
		setupRepos();
		KatelloUtils.sshOnClient("yum -y erase " + package_name1);
		KatelloUtils.sshOnClient("yum -y erase " + package_name2);
		
		KatelloPackage package1 = new KatelloPackage(null, null, org_name, product_name, repo_name, env_name);
		exec_result = package1.cli_list();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(package_name1));
		
		KatelloPackage package2 = new KatelloPackage(null, null, org_name, product_name2, repo_name2, env_name);
		exec_result = package2.cli_list();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(package_name2));
		
		KatelloChangeset chst = new KatelloChangeset(delchst_name, org_name, env_name, true);
		exec_result = chst.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (changeset create)");
		
		exec_result = chst.update_add_package(product_name, package_name1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (changeset update)");
		
		exec_result = chst.update_add_package(product_name2, package_name2);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (changeset update)");
		
		exec_result = chst.apply();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (changeset apply)");		

		exec_result = package1.cli_list();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertFalse(getOutput(exec_result).contains(package_name1));

		exec_result = package2.cli_list();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertFalse(getOutput(exec_result).contains(package_name2));
		
		yum_clean();
		
		// verify that package is not available to install
		exec_result = KatelloUtils.sshOnClient("yum -y install " + package_name1);
		Assert.assertFalse(exec_result.getExitCode().intValue()==0, "Check - return code (install package)");
		
		// verify that second package is not available to install
		exec_result = KatelloUtils.sshOnClient("yum -y install " + package_name2);
		Assert.assertFalse(exec_result.getExitCode().intValue()==0, "Check - return code (install package)");
	}

	@Test(description = "Create changeset of promotion type," +
			" then add already reomved packages to changeset and promote it," +
			" verify that packages exist in environment", dependsOnMethods = {"test_deletionChangesetRemovePackages"})
	public void test_promoteChangesetReAddPackages() {
		
		KatelloPackage package1 = new KatelloPackage(null, null, org_name, product_name, repo_name, env_name);

		KatelloPackage package2 = new KatelloPackage(null, null, org_name, product_name2, repo_name2, env_name);
		
		KatelloChangeset chst = new KatelloChangeset(readdchst_name, org_name, env_name);
		exec_result = chst.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (changeset create)");
		
		exec_result = chst.update_add_package(product_name, package_name1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (changeset update)");
		
		exec_result = chst.apply();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (changeset apply)");		

		KatelloChangeset chst2 = new KatelloChangeset(readdchst_name2, org_name, env_name);
		exec_result = chst2.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (changeset create)");
		
		exec_result = chst2.update_add_package(product_name2, package_name2);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (changeset update)");
		
		exec_result = chst2.apply();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (changeset apply)");		

		exec_result = package1.cli_list();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(package_name1));
		
		exec_result = package2.cli_list();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(package_name2));	

		yum_clean();
		
		KatelloUtils.sshOnClient("yum -y erase " + package_name1);
		KatelloUtils.sshOnClient("yum -y erase " + package_name2);
		
		// verify that package is available to install
		exec_result = KatelloUtils.sshOnClient("yum -y install " + package_name1);
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code (install package)");

		// verify that second package is available to install
		exec_result = KatelloUtils.sshOnClient("yum -y install " + package_name2);
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code (install package)");
	}
	
	private void setupRepos() {
		
		String uid = KatelloUtils.getUniqueID();
		String uid2 = KatelloUtils.getUniqueID();
		
		env_name = "env"+uid;
		provider_name = "provider"+uid;
		product_name = "product"+uid;
		repo_name = "repo"+uid;
		provider_name2 = "provider"+uid2;
		product_name2 = "product"+uid2;
		repo_name2 = "repo"+uid2;
		chst_name = "MötleyCrüechangeset"+uid;		
		delchst_name = "我喜欢吃饺子deletion_chst" +uid;
		system_name = "system" +uid;
		readdchst_name = "readd_chst" + uid;
		readdchst_name2 = "readd_chst" + uid2;
		
		// Create provider:
		KatelloProvider prov = new KatelloProvider(provider_name, org_name, "Package provider", null);
		exec_result = prov.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		// Create product:
		KatelloProduct prod = new KatelloProduct(product_name, org_name, provider_name, null, null, null, null, null);
		exec_result = prod.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	
		KatelloRepo repo = new KatelloRepo(repo_name, org_name, product_name, REPO_INECAS_ZOO3, null, null);
		exec_result = repo.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		// Create second provider:
		KatelloProvider prov2 = new KatelloProvider(provider_name2, org_name, "Package provider", null);
		exec_result = prov2.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		// Create second product:
		KatelloProduct prod2 = new KatelloProduct(product_name2, org_name, provider_name2, null, null, null, null, null);
		exec_result = prod2.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	
		// Create second repo
		KatelloRepo repo2 = new KatelloRepo(repo_name2, org_name, product_name2, "http://repos.fedorapeople.org/repos/candlepin/thumbslug/epel-6Server/x86_64/", null, null);//"http://dl.google.com/linux/chrome/rpm/stable/x86_64", null, null);//@TODO should be chrome
		exec_result = repo2.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		KatelloEnvironment env = new KatelloEnvironment(env_name, null, org_name, KatelloEnvironment.LIBRARY);
		exec_result = env.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (env create)");
		
		// promote product to the env.
		exec_result = prod.promote(env_name);
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code (product promote)");
				
		exec_result = repo.synchronize();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		// promote second product to the env.
		exec_result = prod2.promote(env_name);
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code (product promote)");
				
		exec_result = repo2.synchronize();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		// create Changeset
		KatelloChangeset cs = new KatelloChangeset(chst_name, org_name, env_name);
		exec_result = cs.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (changeset create)");
		
		exec_result = cs.update_add_package(product_name, package_name1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (changeset update)");

		exec_result = cs.update_add_package(product_name2, package_name2);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (changeset update)");
		
		exec_result = cs.apply();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (changeset apply)");
		
		rhsm_clean();
		
		KatelloSystem sys = new KatelloSystem(this.system_name, this.org_name, this.env_name);
		exec_result = sys.rhsm_registerForce(); 
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = sys.subscriptions_available();
		String poolId1 = KatelloCli.grepCLIOutput("ID", getOutput(exec_result).trim(),1);
		
		exec_result = sys.rhsm_subscribe(poolId1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = sys.subscriptions_available();
		String poolId2 = KatelloCli.grepCLIOutput("ID", getOutput(exec_result).trim(),1);
		
		exec_result = sys.rhsm_subscribe(poolId2);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		yum_clean();
	}
}
