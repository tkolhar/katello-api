package com.redhat.qe.katello.tests.e2e;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.base.obj.KatelloSystemGroup;
import com.redhat.qe.katello.common.KatelloUtils;

@Test(groups={"cfse-e2e"}, singleThreaded = true)
public class SystemGroupErratas extends KatelloCliTestBase {

	// Katello objects below
	private String system_name;
	private String group_name;
	private String system_uuid;
	
	@BeforeClass(description="Generate unique names")
	public void setUp(){
		String uid = KatelloUtils.getUniqueID();
		system_name = "system_"+uid;
		group_name = "group_"+uid;
		
		rhsm_clean(); // clean - in case of it registered
		
		String cv_name = KatelloUtils.promoteProductToEnvironment(cli_worker, base_org_name, base_zoo_product_name, base_dev_env_name);
		
		KatelloSystem sys = new KatelloSystem(this.cli_worker, system_name, base_org_name, base_dev_env_name.replaceAll(" ", "_")+"/"+cv_name);
		exec_result = sys.rhsm_registerForce(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		
		exec_result = sys.rhsm_identity();
		system_uuid = KatelloUtils.grepCLIOutput("Current identity is", exec_result.getStdout());
		
		exec_result = sys.rhsm_subscribe(base_zoo_repo_pool);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
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
		
		sshOnClient("service rhsmcertd restart");
		try { Thread.sleep(3000); } catch (Exception ex) {}
		
		exec_result = group.list_erratas();
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertFalse(getOutput(exec_result).replaceAll("\n", "").contains(PromoteErrata.ERRATA_ZOO_SEA), "Check - errata list output");
	}
}
