package com.redhat.qe.katello.tests.e2e;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.common.TngRunGroups;

@Test(groups={"cfse-e2e",TngRunGroups.TNG_KATELLO_Errata}, singleThreaded = true)
public class SystemErratas extends KatelloCliTestBase {

	// Katello objects below
	private String system_name;
	private String cv_name;
	
	@BeforeClass(description="Generate unique names")
	public void setUp(){
		String uid = KatelloUtils.getUniqueID();
		system_name = "system_"+uid;
		
		rhsm_clean(); // clean - in case of it registered
		
		cv_name = KatelloUtils.promoteProductToEnvironment(cli_worker, base_org_name, base_zoo_product_name, base_dev_env_name);
		
		KatelloSystem sys = new KatelloSystem(this.cli_worker, system_name, base_org_name, base_dev_env_name.replaceAll(" ", "_")+"/"+cv_name);
		exec_result = sys.rhsm_registerForce(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		
		exec_result = sys.rhsm_subscribe(base_zoo_repo_pool);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		sshOnClient("yum erase -y walrus");
		exec_result = sshOnClient("yum install -y walrus-0.71-1.noarch");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		sshOnClient("sed -i -e \"s/certFrequency.*/certFrequency = 1/\" /etc/rhsm/rhsm.conf");
		sshOnClient("service rhsmcertd restart");
		yum_clean();
		sshOnClient("service goferd restart;");
	}
	
	/** TCMS scenario is: <a href="https://tcms.engineering.redhat.com/case/243044/?from_plan=7760">here</a> */
	@Test(description = "4aeb7f5c-90f2-4def-b38a-433284d92fad")
	public void test_errataListOnSystem() {
		KatelloSystem system = new KatelloSystem(this.cli_worker, system_name, base_org_name, base_dev_env_name.replaceAll(" ", "_")+"/"+cv_name);
		exec_result = system.list_erratas();
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", "").contains(PromoteErrata.ERRATA_ZOO_SEA), "Check - errata list output");
	}
	
	@Test(description = "List the errata details on system", dependsOnMethods={"test_errataListOnSystem"})
	public void test_errataDetailsOnSystem() {
		KatelloSystem system = new KatelloSystem(this.cli_worker, system_name, base_org_name, base_dev_env_name.replaceAll(" ", "_")+"/"+cv_name);
		exec_result = system.list_errata_details();
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", "").contains(PromoteErrata.ERRATA_ZOO_SEA), "Check - errata list output");
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", "").contains(this.system_name), "Check - errata list details output contains system name");
	}

	/** TCMS scenario is: <a href="https://tcms.engineering.redhat.com/case/134195/?from_plan=7760">here</a> */
	@Test(description = "7cf9e3f5-f328-4225-b972-80e6a93b0a19", dependsOnMethods={"test_errataDetailsOnSystem"})
	public void test_errataListOnUnsubscribedSystem() {
		KatelloSystem system = new KatelloSystem(this.cli_worker, system_name, base_org_name, base_dev_env_name.replaceAll(" ", "_")+"/"+cv_name);
		
		exec_result = sshOnClient("subscription-manager unsubscribe --all");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		
		sshOnClient("sed -i -e \"s/certFrequency.*/certFrequency = 1/\" /etc/rhsm/rhsm.conf");
		sshOnClient("service rhsmcertd restart");
		yum_clean();
		sshOnClient("service goferd restart;");
		try { Thread.sleep(3000); } catch (Exception ex) {}
		
		exec_result = system.list_erratas();
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertFalse(getOutput(exec_result).replaceAll("\n", "").contains(PromoteErrata.ERRATA_ZOO_SEA), "Check - errata list output");
	}
}
