package com.redhat.qe.katello.tests.e2e;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.obj.KatelloChangeset;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups={"cfse-e2e"})
public class SystemEnvironments extends KatelloCliTestScript {
	
	private SSHCommandResult exec_result;

	// Katello objects below
	private String org_name;
	private String provider_name;
	private String product_name;
	private String repo_name;
	private String env_name_Dev, env_name_Prod;
	private String changeset_name;
	private String system_name;
	
	@BeforeClass(description="Generate unique names")
	public void setUp(){
		String uid = KatelloUtils.getUniqueID();
		org_name = "org_"+uid;
		provider_name = "provider_"+uid;
		product_name = "product_"+uid;
		repo_name = "repo_name_"+uid;
		env_name_Dev = "env_Dev_"+uid;
		env_name_Prod = "env_Prod_"+uid;
		changeset_name = "changeset_"+uid;
		system_name = "system_"+uid;
		
		rhsm_clean(); // clean - in case of it registered
		
		// Create org:
		KatelloOrg org = new KatelloOrg(this.org_name, "Package tests");
		exec_result = org.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		// Create provider:
		KatelloProvider prov = new KatelloProvider(provider_name, org_name,
				"Package provider", null);
		exec_result = prov.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		// Create product:
		KatelloProduct prod = new KatelloProduct(product_name, org_name,
				provider_name, null, null, null, null, null);
		exec_result = prod.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		KatelloRepo repo = new KatelloRepo(repo_name, org_name, product_name, REPO_INECAS_ZOO3, null, null);
		exec_result = repo.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		KatelloEnvironment env = new KatelloEnvironment(env_name_Dev, null, org_name, KatelloEnvironment.LIBRARY);
		exec_result = env.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (env create)");
		
		env = new KatelloEnvironment(env_name_Prod, null, org_name, KatelloEnvironment.LIBRARY);
		exec_result = env.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (env create)");
		
		// promote product to the env dev.
		exec_result = prod.promote(env_name_Dev);
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code (product promote)");

		exec_result = repo.synchronize();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		KatelloChangeset cs = new KatelloChangeset(changeset_name, org_name, env_name_Dev);
		exec_result = cs.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (changeset create)");
		
		exec_result = cs.update_addProduct(product_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (changeset update add product)");
		
		exec_result = cs.apply();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (changeset promote)");
		
		KatelloSystem sys = new KatelloSystem(system_name, this.org_name, this.env_name_Dev);
		exec_result = sys.rhsm_registerForce(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		
		exec_result = sys.subscriptions_available();
		String poolId1 = KatelloCli.grepCLIOutput("Id", getOutput(exec_result).trim(),1);
		Assert.assertNotNull(poolId1, "Check - pool Id is not null");
		
		exec_result = sys.rhsm_subscribe(poolId1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		KatelloUtils.sshOnClient("service goferd restart;");
	}
	
	@Test(description = "Move system from one environment to another")
	public void test_moveSystem() {
		KatelloSystem sys = new KatelloSystem(system_name, this.org_name, this.env_name_Dev);
		exec_result = sys.update_environment(this.env_name_Prod);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), String.format(KatelloSystem.OUT_UPDATE, this.system_name));
		
		exec_result = sys.info();
		Assert.assertTrue(exec_result.getExitCode() == 65, "Check - return code");
		
		sys.setEnvironmentName(this.env_name_Prod);
		exec_result = sys.info();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	}

	@Test(description = "Install some package in system after moving to another environment", dependsOnMethods={"test_moveSystem"})
	public void test_installPackageOnSystemError() {
		KatelloUtils.sshOnClient("yum -y erase wolf lion");
		KatelloUtils.sshOnClient("subscription-manager refresh");
		yum_clean();
		
		KatelloSystem sys = new KatelloSystem(system_name, this.org_name, null);
		exec_result = sys.packages_install("lion");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 65, "Check - return code");
		
		exec_result = KatelloUtils.sshOnClient("rpm -q lion");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 1, "Check - return code (rpm -q lion)");
		Assert.assertEquals(getOutput(exec_result).trim(), "package lion is not installed");
	}
	
	@Test(description = "Install some package in system after moving back to original environment", dependsOnMethods={"test_installPackageOnSystemError"})
	public void test_installPackageOnSystem() {
		KatelloSystem sys = new KatelloSystem(system_name, this.org_name, this.env_name_Prod);
		exec_result = sys.update_environment(this.env_name_Dev);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		KatelloUtils.sshOnClient("yum -y erase wolf lion");
		KatelloUtils.sshOnClient("subscription-manager refresh");
		yum_clean();
		
		sys.setEnvironmentName(this.env_name_Dev);
		exec_result = sys.packages_install("lion");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		
		exec_result = KatelloUtils.sshOnClient("rpm -q lion");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code (rpm -q lion)");
		Assert.assertTrue(getOutput(exec_result).trim().contains("lion-"));
	}
}
