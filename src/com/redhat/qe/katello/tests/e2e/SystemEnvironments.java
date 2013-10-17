package com.redhat.qe.katello.tests.e2e;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloActivationKey;
import com.redhat.qe.katello.base.obj.KatelloContentView;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.common.KatelloUtils;

@Test(groups={"cfse-e2e"}, singleThreaded = true)
public class SystemEnvironments extends KatelloCliTestBase {

	// Katello objects below
	private String env_name_Dev, env_name_Test, env_name_Prod;
	private String system_name;
	private String act_key_name;
	private String contentView_name;
	
	@BeforeClass(description="Generate unique names")
	public void setUp(){
		String uid = KatelloUtils.getUniqueID();
		env_name_Dev = "env_Dev_"+uid;
		env_name_Test = "env_Test_"+uid;
		env_name_Prod = "env_Prod_"+uid;
		system_name = "system_"+uid;
		act_key_name = "actkey"+uid;
		
		rhsm_clean(); // clean - in case of it registered

		KatelloEnvironment env = new KatelloEnvironment(this.cli_worker, env_name_Dev, null, base_org_name, KatelloEnvironment.LIBRARY);
		exec_result = env.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (env create)");
		
		env = new KatelloEnvironment(this.cli_worker, env_name_Prod, null, base_org_name, KatelloEnvironment.LIBRARY);
		exec_result = env.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (env create)");
		
		env = new KatelloEnvironment(this.cli_worker, env_name_Test, null, base_org_name, KatelloEnvironment.LIBRARY);
		exec_result = env.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (env create)");

		this.contentView_name = KatelloUtils.promoteProductToEnvironment(cli_worker, base_org_name, base_zoo_product_name, env_name_Dev);
		KatelloContentView view = new KatelloContentView(cli_worker, this.contentView_name, base_org_name);
		exec_result = view.promote_view(env_name_Prod);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		KatelloActivationKey act_key = new KatelloActivationKey(this.cli_worker, base_org_name, env_name_Dev, act_key_name,"Act key created", null, contentView_name);
		exec_result = act_key.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");      
		exec_result = act_key.update_add_subscription(base_zoo_repo_pool);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		KatelloSystem sys = new KatelloSystem(this.cli_worker, system_name, base_org_name, this.env_name_Dev);
		exec_result = sys.rhsm_registerForce(act_key_name); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		
		sshOnClient("service goferd restart;");
	}
	
	@Test(description = "Move system from one environment to another")
	public void test_moveSystem() {
		KatelloSystem sys = new KatelloSystem(this.cli_worker, system_name, base_org_name, this.env_name_Dev);
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
		KatelloSystem sys = new KatelloSystem(this.cli_worker, system_name, base_org_name, this.env_name_Dev);
		exec_result = sys.update_environment(this.env_name_Test);
		Assert.assertFalse(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).trim().contains(String.format("Validation failed: Content view '%s' is not in environment '%s'", this.contentView_name, this.env_name_Test)));
	}

	@Test(description = "Install some package in system after moving to another environment", dependsOnMethods={"test_moveSystem"})
	public void test_installPackageOnMovedSystem() {
		sshOnClient("yum -y erase wolf lion");
		
		KatelloSystem sys = new KatelloSystem(this.cli_worker, system_name, base_org_name, env_name_Prod);
		exec_result = sys.packages_install("lion");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		
		exec_result = sshOnClient("rpm -q lion");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code (rpm -q lion)");
		Assert.assertTrue(getOutput(exec_result).trim().contains("lion-"));
	}
	
	@Test(description = "Install some package in system after moving back to original environment", dependsOnMethods={"test_installPackageOnMovedSystem"})
	public void test_installPackageOnSystem() {
		KatelloSystem sys = new KatelloSystem(this.cli_worker, system_name, base_org_name, this.env_name_Prod);
		exec_result = sys.update_environment(this.env_name_Dev);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		sshOnClient("yum -y erase wolf lion");
		
		sys.setEnvironmentName(this.env_name_Dev);
		exec_result = sys.packages_install("lion");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		
		exec_result = sshOnClient("rpm -q lion");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code (rpm -q lion)");
		Assert.assertTrue(getOutput(exec_result).trim().contains("lion-"));
	}
}
