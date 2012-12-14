package com.redhat.qe.katello.tests.deltacloud;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.obj.KatelloSystemGroup;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.tests.e2e.PromoteErrata;

public class SystemGroupErratas extends BaseDeltacloudTest {
	
	@BeforeClass(description="Generate unique names")
	public void setUp(){
		super.setUp();
		
		KatelloUtils.sshOnClient(client_name, "yum erase -y walrus");
		exec_result = KatelloUtils.sshOnClient(client_name, "yum install -y walrus-0.71-1.noarch");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		configureClient(client_name);
		configureClient(client_name2);
		configureClient(client_name3);
	}
	
	@AfterSuite
	public void tearDown() {
		super.tearDown();
	}
	
	private void configureClient(String client) {
		KatelloUtils.sshOnClient(client, "sed -i -e \"s/certFrequency.*/certFrequency = 1/\" /etc/rhsm/rhsm.conf");
		KatelloUtils.sshOnClient(client, "service rhsmcertd restart");
		yum_clean(client);
		KatelloUtils.sshOnClient(client, "service goferd restart;");		
	}
	
	@Test(description = "List the errata on system group")
	public void test_errataListOnSystemGroup() {
		KatelloSystemGroup group = new KatelloSystemGroup(group_name, this.org_name);
		group.runOn(client_name);
		exec_result = group.list_erratas();
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", "").contains(PromoteErrata.ERRATA_ZOO_SEA), "Check - errata list output");
	}
	
	@Test(description = "List the errata details on system group")
	public void test_errataDetailsOnSystemGroup() {
		KatelloSystemGroup group = new KatelloSystemGroup(group_name, this.org_name);
		group.runOn(client_name);
		exec_result = group.list_errata_details();
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", "").contains(PromoteErrata.ERRATA_ZOO_SEA), "Check - errata list output");
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", "").contains(this.system_name), "Check - errata list details output contains system name");
	}
	
	@Test(description = "Install the errata on system group", dependsOnMethods={"test_errataListOnSystemGroup", "test_errataDetailsOnSystemGroup"})
	public void test_errataInstallOnSystemGroup() {
		KatelloSystemGroup group = new KatelloSystemGroup(group_name, this.org_name);
		group.runOn(client_name);
		exec_result = group.erratas_install(PromoteErrata.ERRATA_ZOO_SEA);
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).trim().contains("Remote action finished"));
		Assert.assertTrue(getOutput(exec_result).trim().contains("Erratum Install Complete"));
		
		KatelloUtils.sshOnClient(client_name, "service rhsmcertd restart");
		try { Thread.sleep(65000); } catch (Exception ex) {}
		
		exec_result = group.list_erratas();
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertFalse(getOutput(exec_result).replaceAll("\n", "").contains(PromoteErrata.ERRATA_ZOO_SEA), "Check - errata list output");
	}
}
