package com.redhat.qe.katello.tests.deltacloud;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.obj.KatelloSystemGroup;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.tests.e2e.PromoteErrata;

@Test(groups="cfse-dc-errata")
public class SystemGroupErratas extends BaseDeltacloudTest {
	

	private void setUpErratas(){
		KatelloUtils.sshOnClient(client_name, "yum erase -y walrus");
		exec_result = KatelloUtils.sshOnClient(client_name, "yum install -y walrus-0.71-1.noarch");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		KatelloUtils.sshOnClient(client_name2, "yum erase -y walrus");
		exec_result = KatelloUtils.sshOnClient(client_name2, "yum install -y walrus-0.71-1.noarch");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
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
	
	@Test(description = "List the errata on system group")
	public void test_errataListOnSystemGroup() {
		setUpErratas();
		
		KatelloSystemGroup group = new KatelloSystemGroup(group_name, org_name);
		group.runOn(client_name);
		exec_result = group.list_erratas();
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", "").contains(PromoteErrata.ERRATA_ZOO_SEA), "Check - errata list output");
		
		group = new KatelloSystemGroup(group_name2, org_name);
		group.runOn(client_name2);
		exec_result = group.list_erratas("security");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", "").contains(PromoteErrata.ERRATA_ZOO_SEA), "Check - errata list output");
	}
	
	@Test(description = "List the errata details on system group", dependsOnMethods={"test_errataListOnSystemGroup"})
	public void test_errataDetailsOnSystemGroup() {
		setUpErratas();
		
		verifyErrataDetailsOnSystemGroup(group_name, 2, Arrays.asList(system_name, system_name2), Arrays.asList(system_name3));
		
		verifyErrataDetailsOnSystemGroup(group_name2, 1, Arrays.asList(system_name2), Arrays.asList(system_name, system_name3));
	}
	
	@Test(description = "Install the errata on system group", dependsOnMethods={"test_errataDetailsOnSystemGroup"})
	public void test_errataInstallOnSystemGroup() {
		KatelloSystemGroup group = new KatelloSystemGroup(group_name, org_name);
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

	@Test(description = "Install the errata which has package dependency on system group", dependsOnMethods={"test_errataInstallOnSystemGroup"})
	public void test_erratInstallWithDependencyOnSystemGroup() {
		setUpErratas();
		
		KatelloSystemGroup group = new KatelloSystemGroup(group_name, org_name);
		group.runOn(client_name2);
		
		exec_result = group.erratas_install("RHBA-2012:1007");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).trim().contains("Remote action finished"));
		Assert.assertTrue(getOutput(exec_result).trim().contains("Erratum Install Complete"));

		
		KatelloUtils.sshOnClient(client_name, "service rhsmcertd restart");
		try { Thread.sleep(65000); } catch (Exception ex) {}
		
		exec_result = group.list_erratas();
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertFalse(getOutput(exec_result).replaceAll("\n", "").contains("RHBA-2012:1007"), "Check - errata list output");
	}
	
	@Test(description = "Install the list of errata on system group", dependsOnMethods={"test_erratInstallWithDependencyOnSystemGroup"})
	public void test_errataListInstallOnSystemGroup() {		
		setUpErratas();
		
		KatelloSystemGroup group = new KatelloSystemGroup(group_name, org_name);
		group.runOn(client_name2);
		
		exec_result = group.list_errata_names("RHBA");
		String ert1 = getOutput(exec_result).replaceAll("\n", ",").split(",")[0];
		String ert2 = getOutput(exec_result).replaceAll("\n", ",").split(",")[1];
		
		exec_result = group.list_errata_names("RHEA");
		String ert3 = getOutput(exec_result).replaceAll("\n", ",").split(",")[0];
		
		exec_result = group.erratas_install(ert1 + "," + ert2 + "," + ert3);
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).trim().contains("Remote action finished"));
		Assert.assertTrue(getOutput(exec_result).trim().contains("Erratum Install Complete"));
		
		KatelloUtils.sshOnClient(client_name, "service rhsmcertd restart");
		try { Thread.sleep(65000); } catch (Exception ex) {}
		
		exec_result = group.list_erratas();
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertFalse(getOutput(exec_result).replaceAll("\n", "").contains(ert1), "Check - errata list output");
		Assert.assertFalse(getOutput(exec_result).replaceAll("\n", "").contains(ert2), "Check - errata list output");
		Assert.assertFalse(getOutput(exec_result).replaceAll("\n", "").contains(ert3), "Check - errata list output");
	}
	
	@Test(description = "Install the errata on clonned system group", dependsOnMethods={"test_errataListInstallOnSystemGroup"})
	public void test_errataInstallOnClonnedSystemGroup() {
		setUpErratas();
		
		KatelloSystemGroup group = new KatelloSystemGroup(group_name, org_name);
		group.runOn(client_name);
		
		exec_result = group.copy("cloned" + group.name, null, null);
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		
		group = new KatelloSystemGroup("cloned" + group.name, org_name);
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
	
	private void verifyErrataDetailsOnSystemGroup(String groupName, int systemCount, List<String> existingSystems, List<String> excludeSystems) {
		KatelloSystemGroup group = new KatelloSystemGroup(groupName, org_name);
		group.runOn(client_name);
		exec_result = group.list_errata_details();
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", "").contains(PromoteErrata.ERRATA_ZOO_SEA), "Check - errata list output");
		for (String sys : existingSystems) {
			Assert.assertTrue(getOutput(exec_result).replaceAll("\n", "").contains(sys), "Check - errata list details output contains system name");
		}
		for (String sys : excludeSystems) {
			Assert.assertFalse(getOutput(exec_result).replaceAll("\n", "").contains(sys), "Check - errata list details output contains system name");
		}
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", "").matches(String.format(KatelloSystemGroup.REG_SYSTEMGROUP_ERRATA_INFO, systemCount)), "Check - errata list output");
	}
}
