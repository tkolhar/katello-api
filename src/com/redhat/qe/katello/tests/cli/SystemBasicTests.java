package com.redhat.qe.katello.tests.cli;

import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Logger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloChangeset;
import com.redhat.qe.katello.base.obj.KatelloContentDefinition;
import com.redhat.qe.katello.base.obj.KatelloContentView;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloPermission;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.base.obj.KatelloUser;
import com.redhat.qe.katello.base.obj.KatelloUserRole;
import com.redhat.qe.katello.base.tngext.TngPriority;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.common.TngRunGroups;
import com.redhat.qe.tools.SSHCommandResult;

@TngPriority(100)
@Test(groups={TngRunGroups.TNG_KATELLO_System_Consumer})
public class SystemBasicTests extends KatelloCliTestBase{	
	protected static Logger log = Logger.getLogger(SystemBasicTests.class.getName());

	private String orgNameMain;
	private String org_name;

	private String envName_Dev;
	private String envName_Test;
	private String envName_Prod;
	
	private String contentName;
	private String contentView;
	
	private String systemNameRegOnly;
	private String sys_reg_name;
	
	private String user;
	private String user_role;
	private String perm_name;
	
	private String orgNameAwesome;
	private String contentViewRhel6;
	private String systemNameAwesome;
	
	@BeforeClass(description="Generate unique names",groups={"headpin-cli"})
	public void setUp(){
		String uid = KatelloUtils.getUniqueID();
		this.orgNameMain = "org-sys-"+uid;
		this.org_name = "orgReg-"+uid;
		
		this.user = "usr"+uid;
		this.user_role = "Full RHSM "+uid;
		this.perm_name = "perm-notdelete-"+ uid; 
		this.systemNameRegOnly = "sys-RegOnly-"+uid;
		this.sys_reg_name = "sys-reg-"+uid;
		
		this.orgNameAwesome = "AwesomeOrg-"+uid;
		this.contentViewRhel6 = "rhel6-x86_64-"+uid;
		this.systemNameAwesome = "awesome-system-"+uid;
		
		exec_result = new KatelloOrg(cli_worker, org_name, null).cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check exit code (org create)");

		exec_result = new KatelloOrg(this.cli_worker, this.orgNameMain, null).cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");

		exec_result = new KatelloOrg(this.cli_worker, this.orgNameAwesome,null).cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");

		KatelloUser user = new KatelloUser(cli_worker, this.user, "root@localhost", KatelloUser.DEFAULT_USER_PASS, false);
		exec_result = user.cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code (user create)");
		KatelloUserRole role = new KatelloUserRole(cli_worker, this.user_role, "not delete to system");
		exec_result = role.create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code (user_role create)");
		KatelloPermission perm = new KatelloPermission(cli_worker, perm_name, this.orgNameMain, "organizations", null,
				"update_systems,read,read_systems,register_systems", this.user_role);
		exec_result = perm.create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code (perm create)");
		exec_result = user.assign_role(this.user_role);
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code (user assign_role)");
		
		this.envName_Dev = null;
		this.envName_Test = null;
		this.envName_Prod = null;
	}
	
	@BeforeClass(description="init: katello specific, no headpin", dependsOnMethods={"setUp"})
	public void setUp_katelloOnly(){
		String uid = KatelloUtils.getUniqueID();
		this.envName_Dev = "Dev-"+uid;
		this.envName_Test = "Test-"+uid;
		this.envName_Prod = "Prod-"+uid;
		this.contentName = "content-" + uid;
		this.contentView = "contentView-"+uid;
		
		KatelloPermission perm = new KatelloPermission(cli_worker, perm_name, this.orgNameMain, "environments", null,
				"update_systems,read_contents,read_systems,register_systems", this.user_role);
		exec_result = perm.create();
		exec_result = new KatelloEnvironment(this.cli_worker, envName_Dev, null, orgNameMain, KatelloEnvironment.LIBRARY).cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");
		exec_result = new KatelloEnvironment(this.cli_worker, envName_Test, null, orgNameMain, KatelloEnvironment.LIBRARY).cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");
		exec_result = new KatelloEnvironment(this.cli_worker, envName_Prod, null, orgNameMain, KatelloEnvironment.LIBRARY).cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");
		//Associate content view to the environments
		KatelloContentDefinition contentMain = new KatelloContentDefinition(this.cli_worker, contentName, "descritpion", orgNameMain, contentName);
		exec_result = contentMain.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = contentMain.publish(this.contentView, this.contentView, "Content View for orgNameMain");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		KatelloContentView contentView = new KatelloContentView(this.cli_worker, this.contentView, orgNameMain);
		exec_result = contentView.promote_view(envName_Prod);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = contentView.promote_view(envName_Test);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = contentView.promote_view(envName_Dev);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	}

	@Test(description = "delete registered system and verifies that it is removed successfully")
	public void test_deleteSystem(){
		String uid = KatelloUtils.getUniqueID();
		String system = "localhost-"+uid;
		rhsm_clean();

		KatelloSystem sys = new KatelloSystem(this.cli_worker, system, this.orgNameMain, this.envName_Prod);
		exec_result = sys.rhsm_register(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");

		exec_result = sys.info();
		String uuid = KatelloUtils.grepCLIOutput("UUID", getOutput(exec_result).trim(),1);
		sys.setUuid(uuid);

		sys.rhsm_unregister();

		exec_result = sys.remove();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(exec_result.getStdout().trim().contains(
				String.format(KatelloSystem.OUT_DELETE, uuid)), "Check - output (success)");

		exec_result = sys.info();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 65, "Check - return code");

		exec_result = sys.list();
		Assert.assertFalse(exec_result.getStdout().trim().contains(uuid), "Check - output (list)");
	}

	@Test(description = "delete registered system by providing invalid credentials")
	public void test_deleteSystemInvalidCredentials(){
		String uid = KatelloUtils.getUniqueID();
		String system = "localhost-"+uid;
		KatelloUser invaliduser = new KatelloUser(cli_worker, "name", "email@redhat.com", "password", false);
		rhsm_clean();

		KatelloSystem sys = new KatelloSystem(this.cli_worker, system, this.orgNameMain, this.envName_Prod);
		exec_result = sys.rhsm_register(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");

		exec_result = sys.info();
		String uuid = KatelloUtils.grepCLIOutput("UUID", getOutput(exec_result).trim(),1);
		sys.setUuid(uuid);

		sys.rhsm_unregister();

		sys.runAs(invaliduser);
		exec_result = sys.remove();
		Assert.assertEquals(exec_result.getExitCode().intValue(), 145, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains("Invalid credentials"), "Check - output (error)");
	}

	@Test(description = "delete registered system by user who has not permissions")
	public void test_deleteSystemInvalidAccess(){
		String uid = KatelloUtils.getUniqueID();
		String system = "localhost-"+uid;
		KatelloUser user = new KatelloUser(cli_worker, this.user, "root@localhost", KatelloUser.DEFAULT_USER_PASS, false);
		rhsm_clean();

		KatelloSystem sys = new KatelloSystem(this.cli_worker, system, this.orgNameMain, this.envName_Prod);
		exec_result = sys.rhsm_register(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");

		exec_result = sys.info();
		String uuid = KatelloUtils.grepCLIOutput("UUID", getOutput(exec_result).trim(),1);
		sys.setUuid(uuid);

		sys.rhsm_unregister();

		sys.runAs(user);
		exec_result = sys.remove();
		Assert.assertTrue(exec_result.getExitCode().intValue()==147, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloSystem.ERR_DELETE_ACCESS, user.username)),
				"Check - output (error)");
	}

	@Test(description = "subscribe system to pool")
	public void test_subscribeSystem(){
		this.promoteContent(this.orgNameMain, this.envName_Dev);
		KatelloSystem sys = new KatelloSystem(this.cli_worker, this.systemNameRegOnly+"-subscribed", this.orgNameMain, this.envName_Dev);
		rhsm_clean();
		exec_result = sys.rhsm_register(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");

		exec_result = sys.subscriptions_available();
		String poolId1 = KatelloUtils.grepCLIOutput("ID", getOutput(exec_result).trim(),1);
		Assert.assertNotNull(poolId1, "Check - pool Id is not null");

		exec_result = sys.subscribe(poolId1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertEquals(exec_result.getStdout().trim(), 
				String.format(KatelloSystem.OUT_SUBSCRIBE, this.systemNameRegOnly+"-subscribed"),
				"Check - subscribe system output.");
	}

	@Test(description = "register system, clean rhsm, reregister by the same name")
	public void test_reRegisterSystem(){
		rhsm_clean();
		KatelloSystem sys = new KatelloSystem(this.cli_worker, "localhost-"+KatelloUtils.getUniqueID(), this.orgNameMain, this.envName_Dev);
		exec_result = sys.rhsm_register();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");

		rhsm_clean_only();
		sys = new KatelloSystem(this.cli_worker, this.systemNameRegOnly+"-subscribed", this.orgNameMain, this.envName_Dev);
		exec_result = sys.rhsm_register();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(exec_result.getStdout().trim().contains(KatelloSystem.OUT_CREATE),
				"Check - output (success)");
	}

	@Test(description = "rename the system")
	public void test_renameSystem(){
		String uid = KatelloUtils.getUniqueID();
		String system = "rhsm-rename-"+uid;
		rhsm_clean();

		KatelloSystem sys = new KatelloSystem(this.cli_worker, system, this.orgNameMain, this.envName_Prod);
		exec_result = sys.rhsm_register(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");

		system = sys.name+"new";
		exec_result = sys.update_name(system);
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");

		try { Thread.sleep(1000); } catch (Exception e) {}
		exec_result = sys.info();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 65, "Check - return code");

		sys.setName(system);
		assert_systemInfo(sys);
	}

	@Test(description = "2fa1d67c-f5fc-48ce-b632-e71a1f656b7d",groups={"headpin-cli"})
	public void test_pdfReport_System(){
		String uid = KatelloUtils.getUniqueID();
		String sys_name = "sys-pdf-report-" + uid;
		rhsm_clean();
		
		KatelloSystem sys = new KatelloSystem(this.cli_worker, sys_name, this.orgNameMain, this.envName_Prod);
		exec_result = sys.rhsm_register();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		exec_result = sys.report("pdf");
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
	}

	@Test(description = "List all the releases for an Organisation",groups={"headpin-cli"})
	public void test_release_System(){
		String sys_name = "sys-release-" + KatelloUtils.getUniqueID();
		KatelloSystem sys = new KatelloSystem(this.cli_worker, sys_name, this.orgNameMain, this.envName_Prod);
		exec_result = sys.rhsm_registerForce();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		exec_result = sys.releases();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
	}

	@Test(description = "System facts are displayed appropriately",groups={"headpin-cli"})
	public void test_facts_System(){
		rhsm_clean();
		String sys_name = "sys-facts-" + KatelloUtils.getUniqueID();

		KatelloSystem sys = new KatelloSystem(this.cli_worker, sys_name, this.orgNameMain, this.envName_Prod);
		exec_result = sys.rhsm_register();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		exec_result = sys.facts();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");

		sys.setEnvironmentName(null);
		exec_result = sys.facts();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
	}

	@Test(description = "System report Review",groups={"headpin-cli"})
	public void test_Reportreview_System(){
		rhsm_clean();
		String uid = KatelloUtils.getUniqueID();
		String sys_name = "sys-report-" + uid;
		KatelloSystem sys = new KatelloSystem(this.cli_worker, sys_name, this.orgNameMain, this.envName_Prod);
		exec_result = sys.rhsm_register();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		exec_result = sys.report(null);
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		exec_result = sys.report("pdf");
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
	}
	
	/**
	 * @see automation request: https://github.com/gkhachik/katello-api/issues/349
	 * @author gkhachik
	 * @since: 06.May.2013
	 * List releases for the system - RHEL6 64 bit repos getting enabled.<BR>
	 * TCMS scenario is: <a href="https://tcms.engineering.redhat.com/case/261760/?from_plan=7771">here</a>
	 */
	@Test(description = "ad3b9f0c-fb35-4c06-9156-60b27337583d", groups={"manifestImported"})
	public void test_listReleasesAllRhel6(){
		KatelloUtils.scpOnClient(cli_worker.getClientHostname(), "data/"+KatelloProvider.MANIFEST_2SUBSCRIPTIONS, "/tmp"); // send manifest zip to the client's /tmp dir.
		
		exec_result = new KatelloProvider(this.cli_worker, KatelloProvider.PROVIDER_REDHAT, this.orgNameAwesome, null, null).import_manifest(
				"/tmp/"+KatelloProvider.MANIFEST_2SUBSCRIPTIONS, false); // import manifest
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		exec_result = new KatelloProduct(this.cli_worker, KatelloProduct.RHEL_SERVER,this.orgNameAwesome,KatelloProvider.PROVIDER_REDHAT,
				null,null,null,null,null).repository_set_enable(KatelloProduct.REPOSET_RHEL6_RPMS);
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		exec_result = new KatelloEnvironment(this.cli_worker, "Testing", null, this.orgNameAwesome, KatelloEnvironment.LIBRARY).cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		exec_result = new KatelloRepo(this.cli_worker, null,this.orgNameAwesome,KatelloProduct.RHEL_SERVER,
				null,null,null).custom_repoListByRegexp("^Name.*x86_64.*", true);
		// parse repos to be enabled and used
		StringTokenizer toks = new StringTokenizer(getOutput(exec_result),"\n");
		Vector<String> repos64bit = new Vector<String>();
		while(toks.hasMoreTokens()){
			repos64bit.addElement(toks.nextToken().trim());
		} // now we have the repos. Let's enable it and add to content view definition.

		KatelloChangeset cs1 = new KatelloChangeset(cli_worker, "cs1", this.orgNameAwesome, "Testing");
		exec_result = cs1.create();// changeset is unique within Org - I hope so :D
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		KatelloContentDefinition contDef1 = new KatelloContentDefinition(cli_worker, "cvd1", null, orgNameAwesome, null);
		exec_result = contDef1.create();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");		
		for(String repo: repos64bit){
			exec_result = new KatelloRepo(this.cli_worker, repo, orgNameAwesome, KatelloProduct.RHEL_SERVER, null, null, null).enable();
			Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code"); // enable
			exec_result = contDef1.add_repo(KatelloProduct.RHEL_SERVER, repo);
			Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code"); // add to content definition
		}
		exec_result = contDef1.publish(this.contentViewRhel6, null, null);
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		exec_result = cs1.update_addView(contentViewRhel6);
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		exec_result = cs1.apply();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		
		// RHSM register the system
		KatelloSystem sys =  new KatelloSystem(this.cli_worker, systemNameAwesome, this.orgNameAwesome, "Testing");
		exec_result = sys.rhsm_registerForce();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		exec_result = sys.releases();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains("6.1"), "Check - stdout contains system release 6.1");
		Assert.assertTrue(getOutput(exec_result).contains("6.2"), "Check - stdout contains system release 6.2");
		Assert.assertTrue(getOutput(exec_result).contains("6.3"), "Check - stdout contains system release 6.3");
		Assert.assertTrue(getOutput(exec_result).contains("6.4"), "Check - stdout contains system release 6.4");
		Assert.assertTrue(getOutput(exec_result).contains("6Server"), "Check - stdout contains system release 6Server");

		exec_result = sys.info();
		sys.uuid = KatelloUtils.grepCLIOutput("UUID", getOutput(exec_result));
		exec_result = sys.releases();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
	}
	
	/**
	 * @see automation request: https://github.com/gkhachik/katello-api/issues/374
	 * @author gkhachik
	 * @since: 06.May.2013
	 * RHSM register system the a specific RHEL6 release - 6.4 for our case. 
	 */
	@Test(description = "list system releases for RHEL6 64bit repos", groups={"manifestImported"}, dependsOnMethods={"test_listReleasesAllRhel6"})
	public void test_rhsmRegWithOptionRelease(){
		String release = "6.4";
		
		KatelloSystem sys =  new KatelloSystem(this.cli_worker, systemNameAwesome, this.orgNameAwesome, "Testing");
		exec_result = sys.rhsm_registerForce_release(release, true, true); // forced - as the system most probably will be registered - previous scenario.
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		exec_result = sys.info();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		// check release field of the system in Katello
		Assert.assertTrue(KatelloUtils.grepCLIOutput("OS Release", getOutput(exec_result)).equals(release), "Check - stdout contains system's OS Release == "+release);
		// Check that systems gets subscribed (--autosubscribe option) - another check would not hurt ;)
		sys.setEnvironmentName(null); // does not work with --environment option (either name or environment)
		exec_result = sys.subscriptions();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
//		Assert.assertTrue(KatelloUtils.grepCLIOutput("Pool Name", getOutput(exec_result)).contains(KatelloProduct.RHEL_SERVER), 
//				"Check - stdout contains pool name == '"+KatelloProduct.RHEL_SERVER+"'");
	}

	@Test(description="list system packages")
	public void test_listPackages() {
		rhsm_clean();
		String sysname = "system-" + KatelloUtils.getUniqueID();
		KatelloSystem sys =  new KatelloSystem(this.cli_worker, sysname, orgNameMain, envName_Prod);
		exec_result = sys.rhsm_register();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - exit code (list packages)");
		exec_result = sys.list_packages();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - exit code (list packages)");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloSystem.OUT_LIST_PACKAGES, sysname, orgNameMain)), "Check output (list packages)");
	}

	@Test(description="system remove_deletion - invalid uuid given")
	public void test_removeDeletionBadUUID() {
		KatelloSystem sys = new KatelloSystem(cli_worker, null, null, null);
		sys.uuid = "007";
		exec_result = sys.remove();
		Assert.assertTrue(exec_result.getExitCode()==148, "Check exit code (system remove_deletion)");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloSystem.ERR_NO_DELETION_RECORD,sys.uuid)), "Check exit code (system remove_deletion)");
	}

	@Test(description="system register")
	public void test_systemRegister() {
		KatelloSystem sys = new KatelloSystem(cli_worker, sys_reg_name, org_name, KatelloEnvironment.LIBRARY);
		exec_result = sys.register();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (system register)");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloSystem.OUT_REGISTRED, sys_reg_name)), "Check output (system register)");

		String def_name = "definition"+KatelloUtils.getUniqueID();
		String view_name = "view "+def_name;
		KatelloContentDefinition def = new KatelloContentDefinition(cli_worker, def_name, null, org_name, null);
		exec_result = def.create();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (create definition)");
		exec_result = def.publish(view_name, null, null);
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (publish definition)");
		sys = new KatelloSystem(cli_worker, sys_reg_name+"X", org_name, KatelloEnvironment.LIBRARY);
		sys.contentview = view_name;
		exec_result = sys.register();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (register system)");
		exec_result = sys.info();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (system info)");
		Assert.assertTrue(getOutput(exec_result).contains(view_name), "Check output (system info)");
	}

	@Test(description="system unregister", dependsOnMethods={"test_systemRegister"})
	public void test_systemUnregister() {
		KatelloSystem sys = new KatelloSystem(cli_worker, sys_reg_name, org_name, null);
		sys.list();
		exec_result = sys.info();
		sys.uuid = KatelloUtils.grepCLIOutput("UUID", getOutput(exec_result));
		exec_result = sys.unregister();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (system unregister)");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloSystem.OUT_UNREGISTRED, sys.uuid)), "Check output (system unregister)");
	}

	private void assert_systemInfo(KatelloSystem system) {
		if (system.description == null) system.description = "Initial Registration Params";
		if (system.location == null) system.location = "None";

		SSHCommandResult res;
		res = system.info();

		String match_info = String.format(KatelloSystem.REG_SYSTEM_INFO, system.name, system.location, system.description).replaceAll("\"", "");
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		log.finest(String.format("System (info) match regex: [%s]", match_info));
		Assert.assertTrue(getOutput(res).replaceAll("\n", " ").matches(match_info), String.format("System [%s] should be found in the result info", system.name));
	}
	
	private void promoteContent(String orgName, String env){
		String uid = KatelloUtils.getUniqueID();
		String provider_name = "zooProv-"+uid;
		String product_name = "zooProd-"+uid;
		String repo_name = "zooRepo-"+uid;
		
		// Create provider:
		KatelloProvider prov = new KatelloProvider(this.cli_worker, provider_name, orgNameMain,
				"Package provider", null);
		exec_result = prov.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		// Create product:
		KatelloProduct prod = new KatelloProduct(this.cli_worker, product_name, orgNameMain,
				provider_name, null, null, null, null, null);
		exec_result = prod.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		KatelloRepo repo = new KatelloRepo(this.cli_worker, repo_name, orgNameMain, product_name, REPO_INECAS_ZOO3, null, null);
		exec_result = repo.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		// promote product to the env prod.
		KatelloUtils.promoteProductToEnvironment(cli_worker, orgNameMain, product_name, envName_Prod);

		exec_result = repo.synchronize();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		KatelloUtils.promoteProductToEnvironment(cli_worker, orgNameMain, product_name, envName_Prod);
	}

	@AfterClass(description="erase registration made; cleanup",alwaysRun=true)
	public void tearDown(){
		rhsm_clean();
		exec_result = new KatelloOrg(this.cli_worker, this.orgNameAwesome, null).cli_info();
		if(exec_result.getExitCode().intValue()==0){
			new KatelloOrg(this.cli_worker, this.orgNameAwesome, null).delete(); // remove the org with that manifest - enable the manifest to be reused.
		}
	}
}
