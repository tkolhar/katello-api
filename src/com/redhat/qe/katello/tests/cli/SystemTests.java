package com.redhat.qe.katello.tests.cli;

import java.util.logging.Logger;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.katello.base.KatelloCliDataProvider;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.obj.KatelloChangeset;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.base.obj.KatelloUser;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.common.TngRunGroups;
import com.redhat.qe.tools.SSHCommandResult;

/**
 * Scenarios in here <b>should</b> go strictly one by one (each time there might be another scenarios doing unregister or whatever else there).
 * @author gkhachik
 *
 */
@Test(groups={"SystemTests",TngRunGroups.TNG_KATELLO_System_Consumer})
public class SystemTests extends KatelloCliTestScript{	
	protected static Logger log = Logger.getLogger(SystemTests.class.getName());

	private SSHCommandResult exec_result;
	private String orgNameRhsms;
	private String orgNameMain;
	private String orgNameNoEnvs;
	
	private String envName_Dev;
	private String envName_Test;
	private String envName_Prod;
	
	private String systemNameRegOnly;
	private String systemNameCustomInfo;
	private String systemNameNoEnvReg;
	
	private String user;
	
	@BeforeClass(description="Generate unique names",groups={"cfse-cli","headpin-cli"})
	public void setUp(){
		String uid = KatelloUtils.getUniqueID();
		this.orgNameRhsms = "orgMain-"+uid;
		this.orgNameMain = "org-sys-"+uid;
		this.orgNameNoEnvs = "orgNoEnv-"+uid;
		
		this.envName_Dev = "Dev-"+uid;
		this.envName_Test = "Test-"+uid;
		this.envName_Prod = "Prod-"+uid;
		this.user = "usr"+uid;
		this.systemNameRegOnly = "sys-RegOnly-"+uid;
		this.systemNameCustomInfo = "sys-CustomInfo-"+uid;
		this.systemNameNoEnvReg = "sys-NoEnvReg-"+uid;
		
		KatelloOrg org = new KatelloOrg(this.orgNameRhsms, null);
		exec_result = org.cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");
		exec_result = new KatelloEnvironment(envName_Dev, null, this.orgNameRhsms, KatelloEnvironment.LIBRARY).cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");

		org = new KatelloOrg(this.orgNameMain, null);
		exec_result = org.cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");

		exec_result = new KatelloEnvironment(envName_Dev, null, orgNameMain, KatelloEnvironment.LIBRARY).cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");
		exec_result = new KatelloEnvironment(envName_Test, null, orgNameMain, KatelloEnvironment.LIBRARY).cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");
		exec_result = new KatelloEnvironment(envName_Prod, null, orgNameMain, KatelloEnvironment.LIBRARY).cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");

		exec_result = new KatelloOrg(this.orgNameNoEnvs, null).cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");

		rhsm_clean(); // clean - in case of it registered
	}

	@Test(description = "RHSM register - org have no environment but Locker only", 
			groups={"cfse-cli","headpin-cli","rhsmRegs"})
	public void test_rhsm_RegLibraryOnly(){
		rhsm_clean();
		KatelloSystem sys = new KatelloSystem(this.systemNameNoEnvReg, this.orgNameNoEnvs, null);
		exec_result = sys.rhsm_register(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 255, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(
				String.format(KatelloSystem.ERR_RHSM_LOBRARY_ONLY,this.orgNameNoEnvs, KatelloEnvironment.LIBRARY)),
				"Check - please create an env.");
	}

	@Test(description = "RHSM register - one environment only", 
			groups={"cfse-cli","headpin-cli","rhsmRegs"}, dependsOnMethods = {"test_rhsm_RegLibraryOnly"})
	public void test_rhsm_RegOneEnvOnly(){
		rhsm_clean();
		// Create the 1st env.
		KatelloEnvironment env = new KatelloEnvironment(this.envName_Dev, null, this.orgNameNoEnvs, KatelloEnvironment.LIBRARY);
		exec_result = env.cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		KatelloSystem sys = new KatelloSystem(this.systemNameNoEnvReg, this.orgNameNoEnvs, null);
		exec_result = sys.rhsm_register(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(exec_result.getStdout().trim().contains(KatelloSystem.OUT_CREATE),
				"Check - output (success)");
		assert_systemInfo(sys);
	}

	@Test(description = "RHSM register - already registered", 
			groups={"cfse-cli","headpin-cli","rhsmRegs"})
	public void test_rhsm_AlreadyReg(){
		rhsm_clean();
		KatelloSystem sys = new KatelloSystem(this.systemNameRegOnly+"-asdf", this.orgNameRhsms, this.envName_Dev);
		exec_result = sys.rhsm_register();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		sys.setName(this.systemNameRegOnly);
		exec_result = sys.rhsm_register();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 1, "Check - return code");
		Assert.assertTrue(exec_result.getStdout().trim().contains(KatelloSystem.ERR_RHSM_REG_ALREADY_FORCE_NEEDED),
				"Check - output (--force needed)");
	}

	@Test(description = "RHSM force register", 
			groups={"cfse-cli","headpin-cli","rhsmRegs"}, dependsOnMethods = {"test_rhsm_AlreadyReg"})
	public void test_rhsm_ForceReg(){
		KatelloSystem sys = new KatelloSystem(this.systemNameRegOnly, this.orgNameRhsms, this.envName_Dev);
		//re-register with --force option
		exec_result = sys.rhsm_registerForce();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code (register --force)");
		String REGEXP_UNREGISTERED = ".*The system with UUID .* has been unregistered.*";
		Assert.assertTrue(exec_result.getStdout().replaceAll("\n", "").matches(REGEXP_UNREGISTERED),"Check - system is unregistered");			
		Assert.assertTrue(exec_result.getStdout().trim().contains(KatelloSystem.OUT_CREATE),
				"Check - output (system registered --force)");
	}

	@Test(description = "RHSM register - more than one environment (no env. specified)", 
			groups={"cfse-cli","headpin-cli","rhsmRegs"}, dependsOnMethods = {"test_rhsm_ForceReg"})
	public void test_rhsm_RegMultiEnv(){
		String uid = KatelloUtils.getUniqueID();
		String system = "rhsm-regMultiEnv-"+uid;
		rhsm_clean();

		// Create the 2nd env.
		new KatelloEnvironment(this.envName_Test, null, this.orgNameRhsms, KatelloEnvironment.LIBRARY).cli_create();
		KatelloSystem sys = new KatelloSystem(system, this.orgNameRhsms, null);
		exec_result = sys.rhsm_register(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 255, "Check - return code");
		Assert.assertTrue(exec_result.getStderr().trim().contains(
				String.format(KatelloSystem.ERR_RHSM_REG_MULTI_ENV, this.orgNameRhsms)),
				"Check - output (rhsm register - multi envs. exist)");
	}

	@Test(description = "RHSM register - env specified", 
			dependsOnMethods = {"test_rhsm_RegMultiEnv"}, groups={"cfse-cli","headpin-cli","rhsmRegs"})
	public void test_rhsm_RegWithEnv(){
		String uid = KatelloUtils.getUniqueID();
		String system = "rhsm-env-"+uid;
		rhsm_clean();

		KatelloSystem sys = new KatelloSystem(system, this.orgNameRhsms, this.envName_Test);
		exec_result = sys.rhsm_register(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(exec_result.getStdout().trim().contains(KatelloSystem.OUT_CREATE),
				"Check - output (success)");
	}

	@Test(description = "RHSM register - same name for 2 environments", 
			dependsOnMethods = {"test_rhsm_RegMultiEnv"}, groups={"cfse-cli","headpin-cli","rhsmRegs"})
	public void test_rhsm_RegSameNameTwoEnvs(){
		String uid = KatelloUtils.getUniqueID();
		String system = "localhost-"+uid;
		rhsm_clean();

		KatelloSystem sys = new KatelloSystem(system, this.orgNameRhsms, this.envName_Dev);
		exec_result = sys.rhsm_register(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(exec_result.getStdout().trim().contains(KatelloSystem.OUT_CREATE),
				"Check - output (success)");
		assert_systemInfo(sys);

		rhsm_clean_only();

		sys = new KatelloSystem(system, this.orgNameRhsms, this.envName_Test);
		exec_result = sys.rhsm_register(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(exec_result.getStdout().trim().contains(KatelloSystem.OUT_CREATE),
				"Check - output (success)");

		sys = new KatelloSystem(null, this.orgNameRhsms, null);
		KatelloCli cli = new KatelloCli("system list --org "+this.orgNameRhsms+" -v | grep \""+system+"\" | wc -l", null);
		exec_result = cli.run();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code (grep: `system list --org`)");
		Assert.assertTrue(exec_result.getStdout().replaceAll("\n", "").trim().equals("2"), "Check - 2 systems are registered with the same name");
	}

	//@ TODO fails because of bug https://bugzilla.redhat.com/show_bug.cgi?id=896074
	@Test(description = "delete registered system and verifies that it is removed successfully",
			groups={"cfse-cli"})
	public void test_deleteSystem(){
		String uid = KatelloUtils.getUniqueID();
		String system = "localhost-"+uid;
		rhsm_clean();

		KatelloSystem sys = new KatelloSystem(system, this.orgNameMain, this.envName_Prod);
		exec_result = sys.rhsm_register(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");

		exec_result = sys.info();
		String uuid = KatelloCli.grepCLIOutput("UUID", getOutput(exec_result).trim(),1);
		sys.setUuid(uuid);

		sys.rhsm_unregister();

		exec_result = sys.remove();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(exec_result.getStdout().trim().contains(String.format(KatelloSystem.OUT_DELETE, uuid)),
				"Check - output (success)");

		exec_result = sys.info();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 65, "Check - return code");

		exec_result = sys.list();
		Assert.assertFalse(exec_result.getStdout().trim().contains(uuid),
				"Check - output (list)");
	}

	@Test(description = "delete registered system by providing invalid credentials",
			groups={"cfse-cli"})
	public void test_deleteSystemInvalidCredentials(){
		String uid = KatelloUtils.getUniqueID();
		String system = "localhost-"+uid;
		KatelloUser invaliduser = new KatelloUser("name", "email@redhat.com", "password", false);
		rhsm_clean();

		KatelloSystem sys = new KatelloSystem(system, this.orgNameMain, this.envName_Prod);
		exec_result = sys.rhsm_register(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");

		exec_result = sys.info();
		String uuid = KatelloCli.grepCLIOutput("UUID", getOutput(exec_result).trim(),1);
		sys.setUuid(uuid);

		sys.rhsm_unregister();

		sys.runAs(invaliduser);
		exec_result = sys.remove();
		Assert.assertEquals(exec_result.getExitCode().intValue(), 145, "Check - return code");
		Assert.assertTrue(exec_result.getStderr().trim().contains("Invalid credentials"),
				"Check - output (error)");
	}

	// TODO - bz#896074 failing due to this
	@Test(description = "delete registered system by user who has not permissions", 
			groups={"cfse-cli"})
	public void test_deleteSystemInvalidAccess(){
		String uid = KatelloUtils.getUniqueID();
		String system = "localhost-"+uid;
		KatelloUser user = new KatelloUser(this.user, "root@localhost", KatelloUser.DEFAULT_USER_PASS, false);
		rhsm_clean();

		KatelloSystem sys = new KatelloSystem(system, this.orgNameMain, this.envName_Prod);
		exec_result = sys.rhsm_register(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");

		exec_result = sys.info();
		String uuid = KatelloCli.grepCLIOutput("UUID", getOutput(exec_result).trim(),1);
		sys.setUuid(uuid);

		sys.rhsm_unregister();

		sys.runAs(user);
		exec_result = sys.remove();
		Assert.assertEquals(exec_result.getExitCode().intValue(), 147, "Check - return code");
		Assert.assertTrue(exec_result.getStderr().trim().contains(String.format(KatelloSystem.ERR_DELETE_ACCESS, user.username)),
				"Check - output (error)");
	}

	@Test(description = "subscribe system to pool",groups={"cfse-cli"})
	public void test_subscribeSystem(){
		
		this.promoteContent(this.orgNameMain, this.envName_Dev);
		rhsm_clean();

		KatelloSystem sys = new KatelloSystem(this.systemNameRegOnly+"-subscribed", this.orgNameMain, this.envName_Dev);
		exec_result = sys.rhsm_register(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");

		exec_result = sys.subscriptions_available();
		String poolId1 = KatelloCli.grepCLIOutput("ID", getOutput(exec_result).trim(),1);
		Assert.assertNotNull(poolId1, "Check - pool Id is not null");

		exec_result = sys.subscribe(poolId1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertEquals(exec_result.getStdout().trim(), 
				String.format(KatelloSystem.OUT_SUBSCRIBE, this.systemNameRegOnly+"-subscribed"),
				"Check - subscribe system output.");
	}

	@Test(description = "register system, clean rhsm, reregister by the same name",
			groups={"cfse-cli"})
	public void test_reRegisterSystem(){
		rhsm_clean();
		KatelloSystem sys = new KatelloSystem("localhost-"+KatelloUtils.getUniqueID(), this.orgNameMain, this.envName_Dev);
		exec_result = sys.rhsm_register();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");

		rhsm_clean_only();
		sys = new KatelloSystem(this.systemNameRegOnly+"-subscribed", this.orgNameMain, this.envName_Dev);
		exec_result = sys.rhsm_register();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(exec_result.getStdout().trim().contains(KatelloSystem.OUT_CREATE),
				"Check - output (success)");
	}

	@Test(description = "rename the system", groups={"cfse-cli"})
	public void test_renameSystem(){
		String uid = KatelloUtils.getUniqueID();
		String system = "rhsm-rename-"+uid;
		rhsm_clean();

		KatelloSystem sys = new KatelloSystem(system, this.orgNameMain, this.envName_Prod);
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

	@Test(description="Add Custom Info - Create custom information for a system", 
			groups={"cfse-cli","headpin-cli","system-customInfo"}, dependsOnGroups={"rhsmRegs"})
	public void test_system_customInfo_add(){
		rhsm_clean();
		
		KatelloSystem sys = new KatelloSystem(this.systemNameCustomInfo, this.orgNameMain, this.envName_Dev);
		exec_result = sys.rhsm_register();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		exec_result = sys.add_custom_info("custom-key","custom-value");
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(
				String.format("Successfully added Custom Information [ custom-key : custom-value ] to System [ %s ]",sys.name)),
				"Check - returned output string");
		exec_result = sys.info();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		String customInfoStr = KatelloCli.grepCLIOutput("Custom Info", getOutput(exec_result));
		Assert.assertTrue(customInfoStr.contains("custom-key"), "Check - stdout contains [custom-key]");
		Assert.assertTrue(customInfoStr.contains("custom-value"), "Check - stdout contains [custom-value]");
	}
	
	@Test(description = "Update Custom Info - Edit custom information for a system",groups={"cfse-cli","headpin-cli","system-customInfo"},
			dependsOnMethods={"test_system_customInfo_add"}, dependsOnGroups={"rhsmRegs"})
	public void test_system_customInfo_update(){
		
		KatelloSystem sys = new KatelloSystem(this.systemNameCustomInfo, this.orgNameMain, this.envName_Dev);
		exec_result = sys.update_custom_info("custom-key", "updated-value");
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(
				String.format("Successfully updated Custom Information [ %s ] for System [ %s ]","custom-key",sys.name)),
				"Check - return string");
		exec_result = sys.info();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		String customInfoStr = KatelloCli.grepCLIOutput("Custom Info", getOutput(exec_result));
		Assert.assertTrue(customInfoStr.contains("custom-key"), "Check - stdout contains [custom-key]");
		Assert.assertTrue(customInfoStr.contains("updated-value"), "Check - stdout contains [updated-value]");
	}

	@Test(description = "Remove Custom Info - Remove custom information for a system",groups={"cfse-cli","headpin-cli","system-customInfo"},
			dependsOnMethods={"test_system_customInfo_update"}, dependsOnGroups={"rhsmRegs"})
	public void test_system_customInfo_remove(){
		KatelloSystem sys = new KatelloSystem(this.systemNameCustomInfo, this.orgNameMain, this.envName_Dev);
		exec_result = sys.remove_custom_info("custom-key");
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(
				String.format("Successfully removed Custom Information from System [ %s ]",sys.name)),
				"Check - return string");
		String customInfoStr = KatelloCli.grepCLIOutput("Custom Info", getOutput(exec_result));
		Assert.assertNull(customInfoStr, "Check - output can not be extracted");
	}
	
	@Test(description = "Add Custom Info - Create custom information for a system - different inputs",
			dataProviderClass = KatelloCliDataProvider.class,dataProvider = "add_custom_info",
			groups={"cfse-cli","headpin-cli","system-customInfo"}, dependsOnMethods={"test_system_customInfo_remove"},
			dependsOnGroups={"rhsmRegs"})
	public void test_system_customInfo_addVariations(String keyname,String value,Integer exitCode,String output){
		
		KatelloSystem sys = new KatelloSystem(this.systemNameCustomInfo, this.orgNameMain, this.envName_Dev);
		exec_result= sys.add_custom_info(keyname,value);
		Assert.assertTrue(exec_result.getExitCode().intValue() == exitCode.intValue(), "Check - return code");
		if(exec_result.getExitCode().intValue() == 0 ){ //
			Assert.assertTrue(getOutput(exec_result).contains(
					String.format("Successfully added Custom Information [ %s : %s ] to System [ %s ]",keyname,value,sys.name)),
					"Check - returned output string");
		}
		else{ // Failure to be checked
			Assert.assertTrue(getOutput(exec_result).contains(output),"Check - returned error string");
		}
	}
	
	@Test(description = "Generate System report in pdf",groups={"cfse-cli","headpin-cli"})
	public void test_pdfReport_System(){
		String uid = KatelloUtils.getUniqueID();
		String sys_name = "sys-pdf-report-" + uid;
		rhsm_clean();
		
		KatelloSystem sys = new KatelloSystem(sys_name, this.orgNameMain, this.envName_Prod);
		exec_result = sys.rhsm_register();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		exec_result = sys.report("pdf");
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
	}

	@Test(description = "List all the releases for an Organisation",groups={"cfse-cli","headpin-cli"})
	public void test_Release_System(){
		String sys_name = "sys-release-" + KatelloUtils.getUniqueID();
		KatelloSystem sys = new KatelloSystem(sys_name, this.orgNameMain, this.envName_Prod);
		exec_result = sys.rhsm_registerForce();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		exec_result = sys.releases();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		// TODO - big changes needed here once subscription to pool is ready.
		/* need to:
		 * 1. enable reposet of RHEL6Server RPMs (will give, I hope, releases 6Server, 6.1, 6.2, 6.3, 6.4)
		 * 2. subscribe to Red Hat Enterprise Linux Server, Self-support (1-2 sockets) (Up to 1 guest) pool
		 * 3. list releases then for that system and make the asserts!
		 * details: ask gkhachik (aka: me)
		 */
	}

	@Test(description = "System facts are displayed appropriately",groups={"cfse-cli","headpin-cli"})
	public void test_Facts_System(){
		rhsm_clean();
		String sys_name = "sys-facts-" + KatelloUtils.getUniqueID();

		KatelloSystem sys = new KatelloSystem(sys_name, this.orgNameMain, this.envName_Prod);
		exec_result = sys.rhsm_register();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		exec_result = sys.facts();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
	}

	@Test(description = "System report Review",groups={"cfse-cli","headpin-cli"})
	public void test_Reportreview_System(){
		rhsm_clean();
		String uid = KatelloUtils.getUniqueID();
		String sys_name = "sys-report-" + uid;
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		KatelloSystem sys = new KatelloSystem(sys_name, this.orgNameMain, this.envName_Prod);
		exec_result = sys.rhsm_register();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		exec_result = sys.report(null);
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		exec_result = sys.report("pdf");
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
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
		String changeset_name = "zooCS-"+uid;
		
		// Create provider:
		KatelloProvider prov = new KatelloProvider(provider_name, orgNameMain,
				"Package provider", null);
		exec_result = prov.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		// Create product:
		KatelloProduct prod = new KatelloProduct(product_name, orgNameMain,
				provider_name, null, null, null, null, null);
		exec_result = prod.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		KatelloRepo repo = new KatelloRepo(repo_name, orgNameMain, product_name, REPO_INECAS_ZOO3, null, null);
		exec_result = repo.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		// promote product to the env prod.
		exec_result = prod.promote(envName_Prod);
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code (product promote)");

		exec_result = repo.synchronize();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		KatelloChangeset cs = new KatelloChangeset(changeset_name, orgNameMain, envName_Prod);
		exec_result = cs.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (changeset create)");

		exec_result = cs.update_addProduct(product_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (changeset update add product)");

		exec_result = cs.apply();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (changeset promote)");
	}

	@AfterTest(description="erase registration made; cleanup",alwaysRun=true)
	public void tearDown(){
		rhsm_clean();
	}
}
