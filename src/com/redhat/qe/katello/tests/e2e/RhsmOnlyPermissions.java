package com.redhat.qe.katello.tests.e2e;

import java.util.logging.Logger;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloPackage;
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
 * Implementing E2E scenario for the case when:<br>
 * Admin registers a user called: rhsm-only giving RHSM systems full permission for specific environment only.<br>
 * This user should be able to:<br>
 * 1. register systems for that environment<br>
 * 2. Request systems related calls (list, info, delete) for that env.<br>
 * 3. everything else should be prohibited.
 * @author gkhachik
 *
 */
@Test(groups={"cfse-e2e"})
public class RhsmOnlyPermissions extends KatelloCliTestScript{
	private static Logger log = Logger.getLogger(RhsmOnlyPermissions.class.getName());

	private String org;
	private String env_dev;
	private String env_test;
	private String user;
	private String user_role;
	private String system;
	private String prod;
	private String repo;
	private String contentView;

	@BeforeClass(description="Init org/env", alwaysRun=true)
	public void setUp(){
		String uid = KatelloUtils.getUniqueID();
		this.env_dev = "Dev-"+uid;
		this.env_test = "Test-"+uid;
		this.user = "rhsm-"+uid;
		this.user_role = "Full RHSM "+uid;
		this.system = "sys-"+this.user;

		this.org = "org-RHSM-only-"+uid;
		KatelloOrg org = new KatelloOrg(this.org, null);
		org.cli_create();
		KatelloEnvironment env = new KatelloEnvironment(this.env_dev, null, this.org, KatelloEnvironment.LIBRARY);
		env.cli_create();
		env = new KatelloEnvironment(this.env_test, null, this.org, KatelloEnvironment.LIBRARY);
		env.cli_create();		
	}
	
	@Test(description="Create user & user_role")
	public void test_createUserAndRole(){
		
		log.info("Preparing: user, user_role");
		KatelloUser user = new KatelloUser(this.user, "root@localhost", KatelloUser.DEFAULT_USER_PASS, false);
		user.cli_create();
		user.asserts_create();
		KatelloUserRole role = new KatelloUserRole(this.user_role, "Full RHSM access for an env. scope");
		SSHCommandResult res = role.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (user_role create)");
	}
	
	@Test(description="Sync Zoo3 repo", dependsOnMethods={"test_createUserAndRole"})
	public void test_syncZoo3(){
		String uid = KatelloUtils.getUniqueID();
		String providerName = "Zoo3_"+uid; 
		this.prod = "Zoo 3 - "+uid;
		this.repo = "Zoo3-"+uid;

		log.info("Sync and promote Zoo3 to the dev env. Subscribe the system to it.");
		KatelloProvider prov = new KatelloProvider(providerName, this.org, null, null);
		prov.create();
		KatelloProduct prod = new KatelloProduct(this.prod, this.org, providerName, null, null, null, null, null);
		prod.create();
		KatelloRepo repo = new KatelloRepo(this.repo, this.org, this.prod, REPO_INECAS_ZOO3, null, null);
		repo.create();
		repo.synchronize();
		
		this.contentView = KatelloUtils.promoteProductToEnvironment(this.org, this.prod, this.env_dev);
	}

	@Test(description="Create permission and assign to user", dependsOnMethods={"test_syncZoo3"})
	public void test_permissionAssign(){
		SSHCommandResult res;
		log.info("Create RHSM full access permission and assign it to the user.");
		res = new KatelloPermission("env-"+user_role, org, "environments", env_dev, "read_changesets,read_systems,register_systems,delete_systems", user_role).create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (create permission - environments)");
		res = new KatelloPermission("cv-"+user_role, org, "content_views", contentView, "read,subscribe", user_role).create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (create permission - content_views)");
		KatelloUser user = new KatelloUser(this.user, null, null, false);
		res = user.assign_role(this.user_role);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (user assign_role)");
		Assert.assertTrue(getOutput(res).equals(
				String.format("User '%s' assigned to role '%s'",this.user, this.user_role)), 
				"Check - return code (user assign_role)");
	}
	
	@Test(description="Register user", dependsOnMethods={"test_permissionAssign"})
	public void test_rhsmRegisterSystem(){
		SSHCommandResult res;
		
		log.info("Register the system");
		rhsm_clean();
		
		KatelloSystem sys = new KatelloSystem(this.system, this.org, this.env_dev+"/"+this.contentView);
		sys.runAs(new KatelloUser(user, null, KatelloUser.DEFAULT_USER_PASS, false));
		res = sys.rhsm_register();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (rhsm register)");
		Assert.assertTrue(getOutput(res).contains("The system has been registered"), "Check - message (registered)");
		try{Thread.sleep(3000);}catch(InterruptedException iex){}
	}
	
	@Test(description="Subscribe system to Zoo3", dependsOnMethods={"test_rhsmRegisterSystem"})
	public void test_subscribeSystemToZoo3(){
		
		log.info("Subscribing system to the pool of: Zoo3");
		KatelloSystem sys = new KatelloSystem(this.system, this.org, null);
		KatelloUser user = new KatelloUser(this.user, null, KatelloUser.DEFAULT_USER_PASS, false);
		sys.runAs(user);
		SSHCommandResult res = new KatelloOrg(this.org, null).subscriptions();
		String pool = KatelloCli.grepCLIOutput("ID", getOutput(res).trim(),1);

		res = sys.rhsm_subscribe(pool);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (rhsm subscribe)");
		String MATCH_SUBSCRIBED = "Successfully.*";
		Assert.assertTrue(getOutput(res).matches(MATCH_SUBSCRIBED), "Check - message (subscribed)");
	}
	
	@Test(description="Yum operations", dependsOnMethods={"test_subscribeSystemToZoo3"})
	public void test_yumOperations(){
		
		log.info("Checks on: yum repolist, packages count");
		SSHCommandResult res = KatelloUtils.sshOnClient("yum clean all; yum repolist --disablerepo \\* --enablerepo \\*\""+this.repo+"\"\\* | grep "+this.repo);
		Assert.assertFalse(getOutput(res).equals("repolist: 0"), "Yum repolist contains the repo just subscribed");
		
		String sRev = new StringBuffer(getOutput(res).trim()).reverse().toString();
		String pkgCountRev = sRev.substring(0, sRev.indexOf(" ")+1);
		int pkgFromYum = Integer.parseInt(new StringBuffer(pkgCountRev).reverse().toString().trim());
		
		KatelloPackage pkgs = new KatelloPackage(this.org, this.prod, this.repo, this.contentView);
		res = pkgs.custom_packagesCount(null); // we specified the contentView already.
		int pkgFromKatello = Integer.parseInt(getOutput(res));
		
		Assert.assertTrue((pkgFromYum==pkgFromKatello), "Check: package counts for both yum and katello repo");
	}
	
	@Test(description="trying to register the system to another env user has no access to",
			dependsOnMethods={"test_yumOperations"})
	public void test_illegalAccess_AnotherEnv(){
		log.info("Trying register the system to an environment user has no access to");
		rhsm_clean();
		
		KatelloSystem sys = new KatelloSystem(this.system, this.org, this.env_test);
		sys.runAs(new KatelloUser(user, null, KatelloUser.DEFAULT_USER_PASS, false));
		SSHCommandResult res = sys.rhsm_register();
		Assert.assertTrue(res.getExitCode().intValue()==255, "Check - return code (rhsm register)");
		Assert.assertTrue(getOutput(res).contains("No such environment: "+this.env_test), "Check - message (registered)");
	}
}
