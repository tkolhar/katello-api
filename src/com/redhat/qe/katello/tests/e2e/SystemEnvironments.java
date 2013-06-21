package com.redhat.qe.katello.tests.e2e;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.obj.KatelloContentView;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.common.TngRunGroups;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups={"cfse-e2e",TngRunGroups.TNG_KATELLO_Environment})
public class SystemEnvironments extends KatelloCliTestScript {
	
	private SSHCommandResult exec_result;

	// Katello objects below
	private String org_name;
	private String provider_name;
	private String product_name;
	private String repo_name;
	private String env_name_Dev, env_name_Prod;
	private String system_name;
	private String contentView_name;
	
	@BeforeClass(description="Generate unique names")
	public void setUp(){
		String uid = KatelloUtils.getUniqueID();
		org_name = "org_"+uid;
		provider_name = "provider_"+uid;
		product_name = "product_"+uid;
		repo_name = "repo_name_"+uid;
		env_name_Dev = "env_Dev_"+uid;
		env_name_Prod = "env_Prod_"+uid;
		system_name = "system_"+uid;
		
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
		
		exec_result = repo.synchronize();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		this.contentView_name = KatelloUtils.promoteProductToEnvironment(this.org_name, product_name, env_name_Dev);
		
		KatelloSystem sys = new KatelloSystem(system_name, this.org_name, this.env_name_Dev+"/"+this.contentView_name);
		exec_result = sys.rhsm_registerForce(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		
		exec_result = sys.subscriptions_available();
		String poolId1 = KatelloUtils.grepCLIOutput("ID", getOutput(exec_result).trim(),1);
		Assert.assertNotNull(poolId1, "Check - pool Id is not null");
		
		exec_result = sys.rhsm_subscribe(poolId1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		KatelloUtils.sshOnClient("service goferd restart;");
	}
	
	/** TCMS scenario is: <a href="https://tcms.engineering.redhat.com/case/195828/?from_plan=7843">here</a> */
	@Test(description = "98f6b05a-2be8-4444-bccb-e39ef9495ec7")
	public void test_moveSystem() {
		exec_result = new KatelloContentView(contentView_name, org_name).promote_view(env_name_Prod);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		KatelloSystem sys = new KatelloSystem(system_name, this.org_name, this.env_name_Dev);
		exec_result = sys.update_environment(this.env_name_Prod);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), String.format(KatelloSystem.OUT_UPDATE, this.system_name));
		
		exec_result = sys.info();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code"); // TODO - https://bugzilla.redhat.com/show_bug.cgi?id=976366
		
		sys.setEnvironmentName(this.env_name_Prod);
		exec_result = sys.info();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	}

	/** TCMS scenario is: <a href="https://tcms.engineering.redhat.com/case/195542/?from_plan=7843">here</a> */
	@Test(description = "546e2f06-7c69-428d-90a6-1bdcb470597f", dependsOnMethods={"test_moveSystem"})
	public void test_installPackageOnSystemError() {
		KatelloUtils.sshOnClient("yum -y erase wolf lion");
		KatelloUtils.sshOnClient("subscription-manager refresh");
		yum_clean();
		
		KatelloSystem sys = new KatelloSystem(system_name, this.org_name, this.env_name_Dev);
		exec_result = sys.packages_install("lion");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		
		exec_result = KatelloUtils.sshOnClient("rpm -q lion");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code (rpm -q lion)");
		Assert.assertTrue(getOutput(exec_result).contains("lion-"),"Check - lion is installed");// TODO bz#976366
	}
	
	/** TCMS scenario is: <a href="https://tcms.engineering.redhat.com/case/196118/?from_plan=7843">here</a> */
	@Test(description = "05228e05-6de9-49e9-a8db-0227ad6385e5", dependsOnMethods={"test_installPackageOnSystemError"})
	public void test_installPackageOnSystem() {
		KatelloSystem sys = new KatelloSystem(system_name, this.org_name, this.env_name_Prod);
		exec_result = sys.update_environment(this.env_name_Dev);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		KatelloUtils.sshOnClient("yum -y erase wolf lion");
		KatelloUtils.sshOnClient("subscription-manager refresh");
		yum_clean();
		
		sys.setEnvironmentName(this.env_name_Dev);
		exec_result = sys.packages_install("lion");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		
		exec_result = KatelloUtils.sshOnClient("rpm -q lion");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code (rpm -q lion)");
		Assert.assertTrue(getOutput(exec_result).trim().contains("lion-"));
	}
}
