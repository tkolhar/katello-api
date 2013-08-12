package com.redhat.qe.katello.tests.deltacloud;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.common.KatelloUtils;

@Test(groups="cfse-dc-errata")
public class SystemErratas extends BaseDeltacloudTest {
	
	@BeforeClass
	public void setUp() {
		rhsm_clean(client_name);
		
		KatelloSystem sys = new KatelloSystem(this.cli_worker, system_name, org_name, env_name);
		sys.runOn(client_name);
		exec_result = sys.rhsm_registerForce(rhel_act_key); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		
		exec_result = sys.rhsm_identity();
		system_uuid = KatelloUtils.grepCLIOutput("Current identity is", exec_result.getStdout());
		
		configureClient(client_name);
	}
	
	private void configureClient(String client) {
		KatelloUtils.sshOnClient(client, "sed -i -e \"s/certFrequency.*/certFrequency = 1/\" /etc/rhsm/rhsm.conf");
		KatelloUtils.sshOnClient(client, "service rhsmcertd restart");
		yum_clean(client);
		KatelloUtils.sshOnClient(client, "service goferd restart;");		
	}
	
	@Test(description = "List the errata on system")
	public void test_errataListOnSystem() {
		
		KatelloSystem system = new KatelloSystem(this.cli_worker, system_name, org_name, null);
		exec_result = system.list_errata_count("RHBA");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertFalse(getOutput(exec_result).replaceAll("\n", "").trim().equals("0"), "Check - erratas are not empty");
		
		exec_result = system.list_errata_count("RHSA");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertFalse(getOutput(exec_result).replaceAll("\n", "").trim().equals("0"), "Check - erratas are not empty");
		
		exec_result = system.list_errata_count("RHEA");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertFalse(getOutput(exec_result).replaceAll("\n", "").trim().equals("0"), "Check - erratas are not empty");
	}
	
	@Test(description = "List the errata details on system group", dependsOnMethods={"test_errataListOnSystem"})
	public void test_errataDetailsOnSystemGroup() {
		
		KatelloSystem system = new KatelloSystem(this.cli_worker, system_name, org_name, null);
		exec_result = system.list_errata_details_count("RHBA");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertFalse(getOutput(exec_result).replaceAll("\n", "").trim().equals("0"), "Check - erratas are not empty");
		
		exec_result = system.list_errata_details_count("RHSA");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertFalse(getOutput(exec_result).replaceAll("\n", "").trim().equals("0"), "Check - erratas are not empty");
		
		exec_result = system.list_errata_details_count("RHEA");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertFalse(getOutput(exec_result).replaceAll("\n", "").trim().equals("0"), "Check - erratas are not empty");
	}
	
}
