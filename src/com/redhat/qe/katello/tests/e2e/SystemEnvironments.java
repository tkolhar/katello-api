package com.redhat.qe.katello.tests.e2e;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloActivationKey;
import com.redhat.qe.katello.base.obj.KatelloContentView;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups={"cfse-e2e"})
public class SystemEnvironments extends KatelloCliTestBase {
	
	private SSHCommandResult exec_result;

	// Katello objects below
	private String org_name;
	private String provider_name;
	private String product_name;
	private String repo_name;
	private String env_name_Dev, env_name_Test, env_name_Prod;
	private String system_name;
	private String act_key_name;
	private String contentView_name;
	
	@BeforeClass(description="Generate unique names")
	public void setUp(){
		String uid = KatelloUtils.getUniqueID();
		org_name = "org_"+uid;
		provider_name = "provider_"+uid;
		product_name = "product_"+uid;
		repo_name = "repo_name_"+uid;
		env_name_Dev = "env_Dev_"+uid;
		env_name_Test = "env_Test_"+uid;
		env_name_Prod = "env_Prod_"+uid;
		system_name = "system_"+uid;
		act_key_name = "actkey"+uid;
		
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
		
		env = new KatelloEnvironment(env_name_Test, null, org_name, KatelloEnvironment.LIBRARY);
		exec_result = env.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (env create)");
		
		exec_result = repo.synchronize();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		this.contentView_name = KatelloUtils.promoteProductToEnvironment(this.org_name, product_name, env_name_Dev);
		KatelloContentView view = new KatelloContentView(this.contentView_name, org_name);
		exec_result = view.promote_view(env_name_Prod);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = org.subscriptions();
		String poolId1 = KatelloUtils.grepCLIOutput("ID", getOutput(exec_result).trim(),1);
		Assert.assertNotNull(poolId1, "Check - pool Id is not null");
		
		KatelloActivationKey act_key = new KatelloActivationKey(org_name, env_name_Dev, act_key_name,"Act key created");
		exec_result = act_key.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");      
		exec_result = act_key.update_add_content_view(contentView_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");      
		exec_result = act_key.update_add_subscription(poolId1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		
		KatelloSystem sys = new KatelloSystem(system_name, this.org_name, this.env_name_Dev);
		exec_result = sys.rhsm_registerForce(act_key_name); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		
		KatelloUtils.sshOnClient("service goferd restart;");
	}
	
	@Test(description = "Move system from one environment to another")
	public void test_moveSystem() {
		KatelloSystem sys = new KatelloSystem(system_name, this.org_name, this.env_name_Dev);
		exec_result = sys.update_environment(this.env_name_Prod);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), String.format(KatelloSystem.OUT_UPDATE, this.system_name));
		sys.setEnvironmentName(this.env_name_Prod);
		exec_result = sys.info();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(this.env_name_Prod), "Environment name in system info");
	}
	
	@Test(description = "Move system from one environment to another which does not have content view, see the error.")
	public void test_moveSystemError() {
		KatelloSystem sys = new KatelloSystem(system_name, this.org_name, this.env_name_Dev);
		exec_result = sys.update_environment(this.env_name_Test);
		Assert.assertFalse(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).trim().contains(String.format("Validation failed: Content view '%s' is not in environment '%s'", this.contentView_name, this.env_name_Test)));
	}

	@Test(description = "Install some package in system after moving to another environment", dependsOnMethods={"test_moveSystem"})
	public void test_installPackageOnMovedSystem() {
		KatelloUtils.sshOnClient("yum -y erase wolf lion");
		
		KatelloSystem sys = new KatelloSystem(system_name, this.org_name, env_name_Prod);
		exec_result = sys.packages_install("lion");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		
		exec_result = KatelloUtils.sshOnClient("rpm -q lion");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code (rpm -q lion)");
		Assert.assertTrue(getOutput(exec_result).trim().contains("lion-"));
	}
	
	@Test(description = "Install some package in system after moving back to original environment", dependsOnMethods={"test_installPackageOnMovedSystem"})
	public void test_installPackageOnSystem() {
		KatelloSystem sys = new KatelloSystem(system_name, this.org_name, this.env_name_Prod);
		exec_result = sys.update_environment(this.env_name_Dev);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		KatelloUtils.sshOnClient("yum -y erase wolf lion");
		
		sys.setEnvironmentName(this.env_name_Dev);
		exec_result = sys.packages_install("lion");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		
		exec_result = KatelloUtils.sshOnClient("rpm -q lion");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code (rpm -q lion)");
		Assert.assertTrue(getOutput(exec_result).trim().contains("lion-"));
	}
}
