package com.redhat.qe.katello.tests.e2e;

import java.util.logging.Logger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.obj.KatelloChangeset;
import com.redhat.qe.katello.base.obj.KatelloContentDefinition;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloPermission;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.base.obj.KatelloUser;
import com.redhat.qe.katello.base.obj.KatelloUserRole;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

/**
 * Story 1: Basic Patch Management<BR>
 * ===============================<BR>
 * 1) Install katello.<BR>
 * 2) Create a new Org and create a user who can manage providers, systems and environments.<BR>
 * 3) Create a Custom Provider with a product and repo.<BR>
 * 4) Sync the content.<BR>
 * 5) Prepare content view and promote the product to the environment via that content view.<BR>
 * 6) From a client machine RHSM register to Katello.<BR>
 * 7) Install a package via yum.<BR>
 * 8) See the package list and facts for the machine in command line.<BR>
 * 9) Unsubsribe the machine, see that the machine no longer subscribed in the UI.<BR>
 * 10) Verify that yum can no longer access the content.
 */
@Test(groups={"cfse-e2e"})
public class BPMTests extends KatelloCliTestScript{
	protected static Logger log = Logger.getLogger(BPMTests.class.getName());
	
	private SSHCommandResult exec_result;

	// Katello objects below
	private String awesomeOrg;
	private String awesomeAdmin;
	private String providerPulp;
	private String productPulp64Bit;
	private String repoPulp64Bit;
	private String envDev;
	private String awesomeSystem;
	private String rhsmPoolId;
	private String contentViewDev;
	
	private KatelloUser ktlOrgAdmin;
	
	@BeforeClass(description="Generate unique names")
	public void setUp(){
		String uid = KatelloUtils.getUniqueID();
		awesomeOrg = "awesomeOrg-"+uid;
		awesomeAdmin = "awesomeAdmin-"+uid;
		providerPulp = "providerPulp-"+uid;
		productPulp64Bit = "prodPulp-"+uid;
		repoPulp64Bit = "repoPulp-"+uid;
		envDev = "Development";
		awesomeSystem = "awesomeSystem-"+uid;
		contentViewDev = "contView-"+uid;
		rhsmPoolId = null; // going to be set after listing avail. subscriptions.
	}
	
	@Test(description="Create a new Org and create an admin user having default org/environment.")
	public void test_createOrgUser(){
		// Create org:
		KatelloOrg org = new KatelloOrg(this.awesomeOrg,"BPM tests");
		exec_result = org.cli_create();
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		exec_result = new KatelloEnvironment(envDev,null,awesomeOrg,KatelloEnvironment.LIBRARY).cli_create();
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");

		// Create user:
		KatelloUser user = new KatelloUser(awesomeAdmin, awesomeAdmin+"@localhost", KatelloUser.DEFAULT_USER_PASS, false, awesomeOrg, envDev);
		exec_result = user.cli_create();
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
	}
	
	@Test(description="Create permossions/roles as needed for the org Admin user", dependsOnMethods={"test_createOrgUser"})
	public void test_orgAdminRolesPermissions(){
		String uid = KatelloUtils.getUniqueID();
		KatelloUserRole roleOrgAdmin = new KatelloUserRole("role-"+awesomeAdmin, "Administrator for "+awesomeOrg);
		exec_result = roleOrgAdmin.create();
		
		exec_result = new KatelloPermission("prwActivationKeys-"+uid, awesomeOrg, "activation_keys", null, null, roleOrgAdmin.name).create(true, true);
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		exec_result = new KatelloPermission("prwContentViewDefinitions-"+uid, awesomeOrg, "content_view_definitions", null, null, roleOrgAdmin.name).create(true, true);
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		exec_result = new KatelloPermission("prwContentViews-"+uid, awesomeOrg, "content_views", null, null, roleOrgAdmin.name).create(true, true);
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		exec_result = new KatelloPermission("prwEnvironments-"+uid, awesomeOrg, "environments", null, null, roleOrgAdmin.name).create(true, true);
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		exec_result = new KatelloPermission("prwOrganizations-"+uid, awesomeOrg, "organizations", null, null, roleOrgAdmin.name).create(true, true);
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		exec_result = new KatelloPermission("prwProviders-"+uid, awesomeOrg, "providers", null, null, roleOrgAdmin.name).create(true, true);
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		exec_result = new KatelloPermission("prwSystemGroups-"+uid, awesomeOrg, "system_groups", null, null, roleOrgAdmin.name).create(true, true);
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		
		exec_result = new KatelloUser(awesomeAdmin, null, null, false).assign_role(roleOrgAdmin.name);
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		
		this.ktlOrgAdmin = new KatelloUser(awesomeAdmin, null, KatelloUser.DEFAULT_USER_PASS, false, null, null); // set the KatelloUser object to be used later on. 
	}
	
	@Test(description="Create a Custom Provider, Product and Repo",
			dependsOnMethods={"test_orgAdminRolesPermissions"})
	public void test_createProviderProductRepo(){		
		// Create provider
		KatelloProvider prov = new KatelloProvider(providerPulp, awesomeOrg, "Pulp provider", null);
		prov.runAs(ktlOrgAdmin);
		exec_result = prov.create();
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		
		// Create product
		KatelloProduct prod = new KatelloProduct(productPulp64Bit, awesomeOrg, providerPulp, null, null, null, null, null);
		prod.runAs(ktlOrgAdmin);
		exec_result = prod.create();
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");

		// Create repo
		KatelloRepo repo = new KatelloRepo(repoPulp64Bit, awesomeOrg, productPulp64Bit, PULP_RHEL6_x86_64_REPO, null, null);
		repo.runAs(ktlOrgAdmin);
		exec_result = repo.create();
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
	}
	
	@Test(description="Sync the content.", dependsOnMethods={"test_createProviderProductRepo"})
	public void test_syncRepo(){
		// Repo synchronize:
		KatelloRepo repo = new KatelloRepo(repoPulp64Bit, awesomeOrg, productPulp64Bit, PULP_RHEL6_x86_64_REPO, null, null);
		repo.runAs(ktlOrgAdmin);
		exec_result = repo.synchronize();
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
	}
	
	@Test(description="Promote the content to the new environment.", dependsOnMethods={"test_syncRepo"})
	public void test_promoteContent(){
		String uid = KatelloUtils.getUniqueID();
		KatelloContentDefinition contDef = new KatelloContentDefinition("cd-"+uid, null, awesomeOrg, null);
		contDef.runAs(ktlOrgAdmin);
		exec_result = contDef.create();
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		exec_result = contDef.add_product(productPulp64Bit);
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		exec_result = contDef.publish(this.contentViewDev, null, null);
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		KatelloChangeset cs = new KatelloChangeset("cs-"+uid, awesomeOrg, envDev);
		cs.runAs(ktlOrgAdmin);
		exec_result = cs.create();
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		exec_result = cs.update_addView(contentViewDev);
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		exec_result = cs.apply();
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
	}
	
	@Test(description="From a client machine RHSM register to Katello.", dependsOnMethods={"test_promoteContent"})
	public void test_rhsmRegister(){
		log.info("Clean RHSM registration");
		rhsm_clean();
		
		KatelloSystem sys = new KatelloSystem(this.awesomeSystem, this.awesomeOrg, this.envDev+"/"+this.contentViewDev);
		sys.runAs(new KatelloUser(awesomeAdmin, null, KatelloUser.DEFAULT_USER_PASS, false));
		exec_result = sys.rhsm_register(); 
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		log.finest("Sleeping 3 sec. giving chance system to recognize the registration.");
		try{Thread.sleep(3000);}catch(InterruptedException iex){}
	}
	
	@Test(description="List available subscriptions", dependsOnMethods={"test_rhsmRegister"})
	public void test_rhsm_listAvailableSubscriptions(){
		KatelloSystem sys = new KatelloSystem(this.awesomeSystem, this.awesomeOrg, this.envDev);
		sys.runAs(ktlOrgAdmin);
		exec_result = sys.subscriptions_available();
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");		
		Assert.assertTrue(getOutput(exec_result).contains(productPulp64Bit), "Check - subscription.ProductName");
		Assert.assertTrue(getOutput(exec_result).contains("Unlimited"), "Check - subscription.Quantity");
		rhsmPoolId = KatelloCli.grepCLIOutput("ID", getOutput(exec_result).trim(),1);
		Assert.assertNotNull(rhsmPoolId, "Check Pool Id is retrieved.");
		log.fine(String.format("Subscription is available for product: [%s] with poolid: [%s]",
				productPulp64Bit,rhsmPoolId));
	}
	
	@Test(description="Subscribe to pool", dependsOnMethods={"test_rhsm_listAvailableSubscriptions"})
	public void test_rhsm_subscribeToPool(){
		KatelloSystem sys = new KatelloSystem(this.awesomeSystem, this.awesomeOrg, this.envDev);
		sys.runAs(ktlOrgAdmin);
		exec_result = sys.rhsm_subscribe(rhsmPoolId);
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).trim().startsWith("Successfully"), 
				"Check - returned message (Successfully)");
		Assert.assertTrue(getOutput(exec_result).trim().contains(rhsmPoolId) || getOutput(exec_result).trim().contains(productPulp64Bit), 
				"Check - returned message (pool ID)");
	}
	
	//@ TODO bug 896600
	@Test(description="Yum should work - yum info pulp-admin-client", 
			dependsOnMethods={"test_rhsm_subscribeToPool"})
	public void test_yuminfo(){
		String pkg_pulp_consumer = "pulp-admin-client";
		exec_result = KatelloUtils.sshOnClient("yum info "+pkg_pulp_consumer+" --disablerepo=* --enablerepo=*"+repoPulp64Bit+"*");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		String YUM_INFO_PULP_CONSUMER = 
				".*Available Packages"+
				".*Name\\s+:\\s+"+pkg_pulp_consumer+
				".*Repo\\s+:\\s+.*"+repoPulp64Bit+".*";
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", "").matches(YUM_INFO_PULP_CONSUMER), 
				"package "+pkg_pulp_consumer+" should be returned in yum info");
	}
	
	@AfterClass(description="erase registration made; cleanup",alwaysRun=true)
	public void tearDown(){
		rhsm_clean();
	}
}
