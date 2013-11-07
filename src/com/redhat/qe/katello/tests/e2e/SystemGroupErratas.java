package com.redhat.qe.katello.tests.e2e;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloActivationKey;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.base.obj.KatelloSystemGroup;
import com.redhat.qe.katello.base.tngext.TngPriority;
import com.redhat.qe.katello.common.KatelloUtils;

public class SystemGroupErratas extends KatelloCliTestBase {

	// Katello objects below
	private String system_name;
	private String group_name;
	private String system_uuid;
	private String act_key_name;
	
	@BeforeClass(description="Generate unique names")
	public void setUp(){
		String uid = KatelloUtils.getUniqueID();
		system_name = "system_"+uid;
		group_name = "group_"+uid;
		act_key_name = "akey"+uid; 
		
		rhsm_clean(); // clean - in case of it registered
		
		String cv_name = KatelloUtils.promoteProductToEnvironment(cli_worker, base_org_name, base_zoo_product_name, base_dev_env_name);
		
		KatelloActivationKey act_key = new KatelloActivationKey(this.cli_worker, base_org_name, base_dev_env_name, act_key_name, "Act key created", null, cv_name);
		exec_result = act_key.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");   
		exec_result = act_key.update_add_subscription(base_zoo_repo_pool);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		KatelloSystem sys = new KatelloSystem(this.cli_worker, system_name, base_org_name, base_dev_env_name);
		exec_result = sys.rhsm_registerForce(act_key_name); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		
		exec_result = sys.rhsm_identity();
		system_uuid = KatelloUtils.grepCLIOutput("Current identity is", exec_result.getStdout());
		
		KatelloSystemGroup group = new KatelloSystemGroup(this.cli_worker, group_name, base_org_name);
		exec_result = group.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = group.add_systems(system_uuid);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		sshOnClient("yum erase -y walrus");
		exec_result = sshOnClient("yum install -y walrus-0.71-1.noarch");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		sshOnClient("sed -i -e \"s/certFrequency.*/certFrequency = 1/\" /etc/rhsm/rhsm.conf");
		sshOnClient("service rhsmcertd restart");
		yum_clean();
		sshOnClient("service goferd restart;");
		try { Thread.sleep(30000); } catch (Exception ex) {}
	}
	
	@Test(description = "List the errata on system group")
	public void test_errataListOnSystemGroup() {
		KatelloSystemGroup group = new KatelloSystemGroup(this.cli_worker, group_name, base_org_name);
		exec_result = group.list_erratas();
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", "").contains(PromoteErrata.ERRATA_ZOO_SEA), "Check - errata list output");
	}
	
	@Test(description = "List the errata details on system group")
	public void test_errataDetailsOnSystemGroup() {
		KatelloSystemGroup group = new KatelloSystemGroup(this.cli_worker, group_name, base_org_name);
		exec_result = group.list_errata_details();
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", "").contains(PromoteErrata.ERRATA_ZOO_SEA), "Check - errata list output");
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", "").contains(this.system_name), "Check - errata list details output contains system name");
	}
	
	@Test(description = "Install the errata on system group", dependsOnMethods={"test_errataListOnSystemGroup", "test_errataDetailsOnSystemGroup"})
	public void test_errataInstallOnSystemGroup() {
		KatelloSystemGroup group = new KatelloSystemGroup(this.cli_worker, group_name, base_org_name);
		exec_result = group.erratas_install(PromoteErrata.ERRATA_ZOO_SEA);
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).trim().contains("Remote action finished"));
		Assert.assertTrue(getOutput(exec_result).trim().contains("Erratum Install Complete"));
		
		//@ TODO Pulp does not support fake erratas, keep disabled for now
//		sshOnClient("service rhsmcertd restart");
//		try { Thread.sleep(3000); } catch (Exception ex) {}
//		
//		exec_result = group.list_erratas();
//		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
//		Assert.assertFalse(getOutput(exec_result).replaceAll("\n", "").contains(PromoteErrata.ERRATA_ZOO_SEA), "Check - errata list output");
	}
}
