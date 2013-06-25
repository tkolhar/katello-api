package com.redhat.qe.katello.tests.e2e;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.common.TngRunGroups;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups={"cfse-e2e",TngRunGroups.TNG_KATELLO_Errata})
public class SystemErratas extends KatelloCliTestBase {
	
	private SSHCommandResult exec_result;

	// Katello objects below
	private String org_name;
	private String provider_name;
	private String product_name;
	private String repo_name;
	private String env_name;
	private String system_name;
	private String cv_name;
	
	@BeforeClass(description="Generate unique names")
	public void setUp(){
		String uid = KatelloUtils.getUniqueID();
		org_name = "org_"+uid;
		provider_name = "provider_"+uid;
		product_name = "product_"+uid;
		repo_name = "repo_name_"+uid;
		env_name = "env_Dev_"+uid;
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
		
		KatelloEnvironment env = new KatelloEnvironment(env_name, null, org_name, KatelloEnvironment.LIBRARY);
		exec_result = env.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (env create)");
		
		exec_result = repo.synchronize();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		cv_name = KatelloUtils.promoteProductToEnvironment(org_name, product_name, env_name);
		
		KatelloSystem sys = new KatelloSystem(system_name, this.org_name, this.env_name+"/"+cv_name);
		exec_result = sys.rhsm_registerForce(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		
		exec_result = sys.subscriptions_available();
		String poolId1 = KatelloUtils.grepCLIOutput("ID", getOutput(exec_result).trim(),1);
		Assert.assertNotNull(poolId1, "Check - pool Id is not null");
		
		exec_result = sys.rhsm_subscribe(poolId1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		KatelloUtils.sshOnClient("yum erase -y walrus");
		exec_result = KatelloUtils.sshOnClient("yum install -y walrus-0.71-1.noarch");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		KatelloUtils.sshOnClient("sed -i -e \"s/certFrequency.*/certFrequency = 1/\" /etc/rhsm/rhsm.conf");
		KatelloUtils.sshOnClient("service rhsmcertd restart");
		yum_clean();
		KatelloUtils.sshOnClient("service goferd restart;");
	}
	
	/** TCMS scenario is: <a href="https://tcms.engineering.redhat.com/case/243044/?from_plan=7760">here</a> */
	@Test(description = "4aeb7f5c-90f2-4def-b38a-433284d92fad")
	public void test_errataListOnSystem() {
		KatelloSystem system = new KatelloSystem(system_name, this.org_name, this.env_name+"/"+cv_name);
		exec_result = system.list_erratas();
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", "").contains(PromoteErrata.ERRATA_ZOO_SEA), "Check - errata list output");
	}
	
	@Test(description = "List the errata details on system", dependsOnMethods={"test_errataListOnSystem"})
	public void test_errataDetailsOnSystem() {
		KatelloSystem system = new KatelloSystem(system_name, this.org_name, this.env_name+"/"+cv_name);
		exec_result = system.list_errata_details();
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", "").contains(PromoteErrata.ERRATA_ZOO_SEA), "Check - errata list output");
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", "").contains(this.system_name), "Check - errata list details output contains system name");
	}

	/** TCMS scenario is: <a href="https://tcms.engineering.redhat.com/case/134195/?from_plan=7760">here</a> */
	@Test(description = "7cf9e3f5-f328-4225-b972-80e6a93b0a19", dependsOnMethods={"test_errataDetailsOnSystem"})
	public void test_errataListOnUnsubscribedSystem() {
		KatelloSystem system = new KatelloSystem(system_name, this.org_name, this.env_name+"/"+cv_name);
		
		exec_result = KatelloUtils.sshOnClient("subscription-manager unsubscribe --all");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		
		KatelloUtils.sshOnClient("sed -i -e \"s/certFrequency.*/certFrequency = 1/\" /etc/rhsm/rhsm.conf");
		KatelloUtils.sshOnClient("service rhsmcertd restart");
		yum_clean();
		KatelloUtils.sshOnClient("service goferd restart;");
		try { Thread.sleep(10000); } catch (Exception ex) {}
		
		exec_result = system.list_erratas();
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertFalse(getOutput(exec_result).replaceAll("\n", "").contains(PromoteErrata.ERRATA_ZOO_SEA), "Check - errata list output");
	}
}
