package com.redhat.qe.katello.tests.deltacloud;

import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.common.KatelloUtils;

@Test(groups="cfse-dc-errata")
public class SystemErratas extends BaseDeltacloudTest {
	

	private void setUpErratas(){	
		configureClient(client_name);
		configureClient(client_name2);
		configureClient(client_name3);
	}
	
	private void configureClient(String client) {
		KatelloUtils.sshOnClient(client, "sed -i -e \"s/certFrequency.*/certFrequency = 1/\" /etc/rhsm/rhsm.conf");
		KatelloUtils.sshOnClient(client, "service rhsmcertd restart");
		yum_clean(client);
		KatelloUtils.sshOnClient(client, "service goferd restart;");		
	}
	
	@Test(description = "List the errata on system")
	public void test_errataListOnSystem() {
		setUpErratas();
		
		KatelloSystem system = new KatelloSystem(system_name, org_name, env_name);
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
		setUpErratas();
		
		KatelloSystem system = new KatelloSystem(system_name, org_name, env_name);
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
