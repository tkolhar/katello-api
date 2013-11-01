package com.redhat.qe.katello.tests.cli;

import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Logger;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.katello.base.KatelloCliDataProvider;
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
import com.redhat.qe.katello.base.obj.KatelloSystemGroup;
import com.redhat.qe.katello.base.obj.KatelloUser;
import com.redhat.qe.katello.base.obj.KatelloUserRole;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.common.TngRunGroups;
import com.redhat.qe.tools.SSHCommandResult;

/**
 * Scenarios in here <b>should</b> go strictly one by one (each time there might be another scenarios doing unregister or whatever else there).
 * @author gkhachik
 *
 */
@Test(groups={"sys-group-3",TngRunGroups.TNG_KATELLO_System_Consumer})
public class SystemTests extends KatelloCliTestBase{	
	protected static Logger log = Logger.getLogger(SystemTests.class.getName());

	private SSHCommandResult exec_result;
	private String orgNameRhsms;
	private String orgNameMain;
	private String orgNameNoEnvs;
	
	private String envName_Dev;
	private String envName_Test;
	private String envName_Prod;
	
	private String contentName;
	private String contentView;
	
	private String systemNameRegOnly;
	private String systemNameCustomInfo;
	private String systemNameNoEnvReg;
	
	private String user;
	private String user_role;
	private String perm_name;
	
	private String orgNameAwesome;
	private String contentViewRhel6;
	private String systemNameAwesome;
	
	private String sysgroup_name;
	private String org_name;
	private String sys_name;
	private String sys_nonexist_name;
	private String sys_reg_name;
	private String grp_nonexist_name;

	//BZ:1011748
	@BeforeClass(description="Generate unique names",groups={"cfse-cli","headpin-cli"})
	public void setUp(){
		String uid = KatelloUtils.getUniqueID();
		this.orgNameRhsms = "orgMain-"+uid;
		this.orgNameMain = "org-sys-"+uid;
		this.orgNameNoEnvs = "orgNoEnv-"+uid;
		
		this.user = "usr"+uid;
		this.user_role = "Full RHSM "+uid;
		this.perm_name = "perm-notdelete-"+ uid; 
		this.systemNameRegOnly = "sys-RegOnly-"+uid;
		this.systemNameCustomInfo = "sys-CustomInfo-"+uid;
		this.systemNameNoEnvReg = "sys-NoEnvReg-"+uid;
		
		this.orgNameAwesome = "AwesomeOrg-"+uid;
		this.contentViewRhel6 = "rhel6-x86_64-"+uid;
		this.systemNameAwesome = "awesome-system-"+uid;
		
		org_name = "org"+uid;
		sys_name = "system"+uid;
		sys_nonexist_name = "nonexisting-system"+uid;
		sys_reg_name = "system-reg"+uid;
		sysgroup_name = "sysgroup"+uid;
		grp_nonexist_name = "sysgroup-nonexist"+uid;

		KatelloOrg org = new KatelloOrg(this.cli_worker, this.orgNameRhsms, null);
		exec_result = org.cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");

		org = new KatelloOrg(this.cli_worker, this.orgNameMain, null);
		exec_result = org.cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");

		exec_result = new KatelloOrg(this.cli_worker, this.orgNameNoEnvs, null).cli_create();
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
		
		rhsm_clean(); // clean - in case of it registered
		
		this.envName_Dev = null;
		this.envName_Test = null;
		this.envName_Prod = null;

		org = new KatelloOrg(cli_worker, org_name, null);
		exec_result = org.cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check exit code (org create)");
		KatelloSystemGroup sysgroup = new KatelloSystemGroup(cli_worker, sysgroup_name, org_name);
		exec_result = sysgroup.create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check exit code (system group create)");

		KatelloSystem sys =  new KatelloSystem(cli_worker, sys_name, org_name, "Library");
		sys.register();
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
		exec_result = new KatelloEnvironment(this.cli_worker, envName_Dev, null, this.orgNameRhsms, KatelloEnvironment.LIBRARY).cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");
		exec_result = new KatelloEnvironment(this.cli_worker, envName_Test, null, this.orgNameRhsms, KatelloEnvironment.LIBRARY).cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");
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
		
		KatelloContentDefinition contentRhsm = new KatelloContentDefinition(this.cli_worker, contentName, "descritpion", this.orgNameRhsms, contentName);
		exec_result = contentRhsm.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = contentRhsm.publish(this.contentView, this.contentView, "Content View for orgNameRhsms");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		KatelloContentView contentViewRhsm = new KatelloContentView(this.cli_worker, this.contentView, this.orgNameRhsms);
		exec_result = contentViewRhsm.promote_view(envName_Dev);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = contentViewRhsm.promote_view(envName_Test);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
	}

	@Test(description = "RHSM register - org have no environment but Locker only", 
			groups={"cfse-cli","headpin-cli","rhsmRegs"})
	public void test_rhsm_RegLibraryOnly(){
		rhsm_clean();
		KatelloSystem sys = new KatelloSystem(this.cli_worker, this.systemNameNoEnvReg, this.orgNameNoEnvs, null);
		exec_result = sys.rhsm_register(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(exec_result.getStdout().trim().contains(KatelloSystem.OUT_CREATE),
				"Check - output (success)");
		assert_systemInfo(sys);
		rhsm_clean();
	}

	@Test(description = "RHSM register - one environment only", 
			groups={"cfse-cli","rhsmRegs"}, dependsOnMethods = {"test_rhsm_RegLibraryOnly"})
	public void test_rhsm_RegOneEnvOnly(){
		rhsm_clean();
		// Create the 1st env.
		KatelloEnvironment env = new KatelloEnvironment(this.cli_worker, this.envName_Dev, null, this.orgNameNoEnvs, KatelloEnvironment.LIBRARY);
		exec_result = env.cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		KatelloSystem sys = new KatelloSystem(this.cli_worker, this.systemNameNoEnvReg, this.orgNameNoEnvs, null);
		exec_result = sys.rhsm_register(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 255, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloSystem.ERR_RHSM_REG_MULTI_ENV,this.orgNameNoEnvs)));
	}

	@Test(description = "RHSM register - already registered", 
			groups={"cfse-cli","headpin-cli","rhsmRegs"})
	public void test_rhsm_AlreadyReg(){
		rhsm_clean();
		KatelloSystem sys = new KatelloSystem(this.cli_worker, this.systemNameRegOnly+"-alreadyReg", this.orgNameRhsms, this.envName_Dev);
		exec_result = sys.rhsm_register();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		sys.setName(this.systemNameRegOnly);
		exec_result = sys.rhsm_register();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 1, "Check - return code");
		Assert.assertTrue(exec_result.getStdout().trim().contains(KatelloSystem.ERR_RHSM_REG_ALREADY_FORCE_NEEDED),
				"Check - output (--force needed)");
	}
	
	/** TCMS scenario is: <a href="https://tcms.engineering.redhat.com/case/221906/?from_plan=7771">here</a> */
	@Test(description = "262c4394-0718-4336-a114-60130bd7f447", 
			groups={"cfse-cli","headpin-cli","rhsmRegs"})
	public void test_rhsm_ForceReg(){
		rhsm_clean();
		KatelloSystem sys = new KatelloSystem(this.cli_worker, "sys-ForceReg-"+KatelloUtils.getUniqueID(), this.orgNameRhsms, this.envName_Dev);
		exec_result = sys.rhsm_register();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code (register - 1st attempt pass)");
		
		//re-register with --force option
		exec_result = sys.rhsm_registerForce();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code (register --force)");
		String REGEXP_UNREGISTERED = ".*The system with UUID .* has been unregistered.*";
		Assert.assertTrue(exec_result.getStdout().replaceAll("\n", "").matches(REGEXP_UNREGISTERED),"Check - system is unregistered");			
		Assert.assertTrue(exec_result.getStdout().trim().contains(KatelloSystem.OUT_CREATE),
				"Check - output (system registered --force)");
	}

	@Test(description = "RHSM register - more than one environment (no env. specified)", 
			groups={"cfse-cli","rhsmRegs"}, dependsOnMethods = {"test_rhsm_ForceReg"})
	public void test_rhsm_RegMultiEnv(){
		String uid = KatelloUtils.getUniqueID();
		String system = "rhsm-regMultiEnv-"+uid;
		rhsm_clean();

		// Create the 2nd env.
		new KatelloEnvironment(this.cli_worker, this.envName_Test, null, this.orgNameRhsms, KatelloEnvironment.LIBRARY).cli_create();
		KatelloSystem sys = new KatelloSystem(this.cli_worker, system, this.orgNameRhsms, null);
		exec_result = sys.rhsm_register(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 255, "Check - return code");
		Assert.assertTrue(exec_result.getStderr().trim().contains(
				String.format(KatelloSystem.ERR_RHSM_REG_MULTI_ENV, this.orgNameRhsms)),
				"Check - output (rhsm register - multi envs. exist)");
	}

	@Test(description = "RHSM register - env specified", 
			dependsOnMethods = {"test_rhsm_RegMultiEnv"}, groups={"cfse-cli","rhsmRegs"})
	public void test_rhsm_RegWithEnv(){
		String uid = KatelloUtils.getUniqueID();
		String system = "rhsm-env-"+uid;
		rhsm_clean();

		KatelloSystem sys = new KatelloSystem(this.cli_worker, system, this.orgNameRhsms, this.envName_Test);
		exec_result = sys.rhsm_register(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(exec_result.getStdout().trim().contains(KatelloSystem.OUT_CREATE),
				"Check - output (success)");
	}

	@Test(description = "RHSM register - same name for 2 environments", 
			dependsOnMethods = {"test_rhsm_RegMultiEnv"}, groups={"cfse-cli","rhsmRegs"})
	public void test_rhsm_RegSameNameTwoEnvs(){
		String uid = KatelloUtils.getUniqueID();
		String system = "localhost-"+uid;
		rhsm_clean();

		KatelloSystem sys = new KatelloSystem(this.cli_worker, system, this.orgNameRhsms, this.envName_Dev);
		exec_result = sys.rhsm_register(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(exec_result.getStdout().trim().contains(KatelloSystem.OUT_CREATE),
				"Check - output (success)");
		assert_systemInfo(sys);

		rhsm_clean_only();

		sys = new KatelloSystem(this.cli_worker, system, this.orgNameRhsms, this.envName_Test);
		exec_result = sys.rhsm_register(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(exec_result.getStdout().trim().contains(KatelloSystem.OUT_CREATE),
				"Check - output (success)");

		sys = new KatelloSystem(this.cli_worker, null, this.orgNameRhsms, null);
		KatelloCli cli = new KatelloCli("system list --org "+this.orgNameRhsms+" -v | grep \""+system+"\" | wc -l", null,null,cli_worker.getClientHostname());
		exec_result = cli.run();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code (grep: `system list --org`)");
		Assert.assertTrue(exec_result.getStdout().replaceAll("\n", "").trim().equals("2"), "Check - 2 systems are registered with the same name");
	}

	@Test(description = "delete registered system and verifies that it is removed successfully",
			groups={"cfse-cli"})
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

	// TODO - bz#896074 failing due to this
	@Test(description = "delete registered system by user who has not permissions", 
			groups={"cfse-cli"})
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
		Assert.assertTrue(exec_result.getExitCode().intValue()>0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloSystem.ERR_DELETE_ACCESS, user.username)),
				"Check - output (error)");
	}

	@Test(description = "subscribe system to pool",groups={"cfse-cli"})
	public void test_subscribeSystem(){
		
		this.promoteContent(this.orgNameMain, this.envName_Dev);
		rhsm_clean();

		KatelloSystem sys = new KatelloSystem(this.cli_worker, this.systemNameRegOnly+"-subscribed", this.orgNameMain, this.envName_Dev);
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

	@Test(description = "register system, clean rhsm, reregister by the same name",
			groups={"cfse-cli"})
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

	@Test(description = "rename the system", groups={"cfse-cli"})
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

	/** TCMS scenario is: <a href="https://tcms.engineering.redhat.com/case/243212/?from_plan=7771">here</a> */
	@Test(description="2f43f492-8e03-4424-8eb8-b9cd2745cf94", 
			groups={"cfse-cli","headpin-cli","system-customInfo"}, dependsOnGroups={"rhsmRegs"})
	public void test_system_customInfo_add(){
		rhsm_clean();
		
		KatelloSystem sys = new KatelloSystem(this.cli_worker, this.systemNameCustomInfo, this.orgNameMain, this.envName_Dev);
		exec_result = sys.rhsm_register();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		exec_result = sys.add_custom_info("custom-key","custom-value");
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(
				String.format(KatelloSystem.OUT_ADD_CUSTOM_INFO, "custom-key", "custom-value", sys.name)),
				"Check - returned output string");
		exec_result = sys.info();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		String customInfoStr = KatelloUtils.grepCLIOutput("Custom Info", getOutput(exec_result));
		Assert.assertTrue(customInfoStr.contains("custom-key"), "Check - stdout contains [custom-key]");
		Assert.assertTrue(customInfoStr.contains("custom-value"), "Check - stdout contains [custom-value]");
	}
	
	
	/** TCMS scenario is: <a href="https://tcms.engineering.redhat.com/case/243183/?from_plan=7771">here</a> */
	@Test(description = "cf618455-2f04-4994-844f-e3da4369d900",groups={"cfse-cli","headpin-cli","system-customInfo"},
			dependsOnMethods={"test_system_customInfo_add"}, dependsOnGroups={"rhsmRegs"})
	public void test_system_customInfo_update(){
		
		KatelloSystem sys = new KatelloSystem(this.cli_worker, this.systemNameCustomInfo, this.orgNameMain, this.envName_Dev);
		exec_result = sys.update_custom_info("custom-key", "updated-value");
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(
				String.format("Successfully updated Custom Information [ %s ] for System [ %s ]","custom-key",sys.name)),
				"Check - return string");
		exec_result = sys.info();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		String customInfoStr = KatelloUtils.grepCLIOutput("Custom Info", getOutput(exec_result));
		Assert.assertTrue(customInfoStr.contains("custom-key"), "Check - stdout contains [custom-key]");
		Assert.assertTrue(customInfoStr.contains("updated-value"), "Check - stdout contains [updated-value]");
	}

	
	@Test(description="65037d2b-a924-4f36-8e72-25056d80d097",groups={"cfse-cli","headpin-cli","system-customInfo"},
			dependsOnMethods={"test_system_customInfo_update"}, dependsOnGroups={"rhsmRegs"})
	public void test_system_customInfo_remove(){
		KatelloSystem sys = new KatelloSystem(this.cli_worker, this.systemNameCustomInfo, this.orgNameMain, this.envName_Dev);
		exec_result = sys.remove_custom_info("custom-key");
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(
				String.format("Successfully removed Custom Information from System [ %s ]",sys.name)),
				"Check - return string");
		String customInfoStr = KatelloUtils.grepCLIOutput("Custom Info", getOutput(exec_result));
		Assert.assertNull(customInfoStr, "Check - output can not be extracted");
	}
	
	@Test(description = "Add Custom Info - Create custom information for a system - different inputs",
			dataProviderClass = KatelloCliDataProvider.class,dataProvider = "add_custom_info",
			groups={"cfse-cli","headpin-cli","system-customInfo"}, dependsOnMethods={"test_system_customInfo_remove"},
			dependsOnGroups={"rhsmRegs"})
	public void test_system_customInfo_addVariations(String keyname,String value,Integer exitCode,String output){
		
		KatelloSystem sys = new KatelloSystem(this.cli_worker, this.systemNameCustomInfo, this.orgNameMain, this.envName_Dev);
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
	
	
	@Test(description = "2fa1d67c-f5fc-48ce-b632-e71a1f656b7d",groups={"cfse-cli","headpin-cli"})
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

	@Test(description = "List all the releases for an Organisation",groups={"cfse-cli","headpin-cli"})
	public void test_Release_System(){
		String sys_name = "sys-release-" + KatelloUtils.getUniqueID();
		KatelloSystem sys = new KatelloSystem(this.cli_worker, sys_name, this.orgNameMain, this.envName_Prod);
		exec_result = sys.rhsm_registerForce();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		exec_result = sys.releases();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
	}

	@Test(description = "System facts are displayed appropriately",groups={"cfse-cli","headpin-cli"})
	public void test_Facts_System(){
		rhsm_clean();
		String sys_name = "sys-facts-" + KatelloUtils.getUniqueID();

		KatelloSystem sys = new KatelloSystem(this.cli_worker, sys_name, this.orgNameMain, this.envName_Prod);
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
	@Test(description = "ad3b9f0c-fb35-4c06-9156-60b27337583d", 
			groups={"cfse-cli","manifestImported"})
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
	}
	
	/**
	 * @see automation request: https://github.com/gkhachik/katello-api/issues/374
	 * @author gkhachik
	 * @since: 06.May.2013
	 * RHSM register system the a specific RHEL6 release - 6.4 for our case. 
	 */
	@Test(description = "list system releases for RHEL6 64bit repos", 
			groups={"cfse-cli","manifestImported"}, dependsOnMethods={"test_listReleasesAllRhel6"})
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
		Assert.assertTrue(KatelloUtils.grepCLIOutput("Pool Name", getOutput(exec_result)).contains(KatelloProduct.RHEL_SERVER), 
				"Check - stdout contains pool name == '"+KatelloProduct.RHEL_SERVER+"'");
	}

	@Test(description="list system packages")
	public void test_listPackages() {
		String sysname = "system-" + KatelloUtils.getUniqueID();
		KatelloSystem sys =  new KatelloSystem(this.cli_worker, sysname, orgNameMain, envName_Prod);
		exec_result = sys.rhsm_register();
		exec_result = sys.list_packages();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - exit code (list packages)");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloSystem.OUT_LIST_PACKAGES, sysname, orgNameMain)), "Check output (list packages)");
	}



	// TODO bz#974486
	@Test(description="system tasks - wrong system given")
	public void test_badSystemTasks() {
		KatelloSystem sys =  new KatelloSystem(cli_worker, sys_nonexist_name, org_name, null);
		exec_result = sys.tasks();
		Assert.assertTrue(exec_result.getExitCode()==65, "Check exit code (system tasks)");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloSystem.ERR_NOT_FOUND, "boosystem", orgNameMain)), "Check error message (system tasks)");
	}

	// TODO bz#974503
	// TODO bz#1019748
	@Test(description="system task test", enabled=false)
	public void test_systemTask() {
		KatelloSystem sys =  new KatelloSystem(cli_worker, sys_name, org_name, KatelloEnvironment.LIBRARY);
		// create task to get ID
		exec_result = sys.packages_install("fakepackage");
		Assert.assertTrue(getOutput(exec_result).contains("Performing remote action"), "(create task)");
		String taskId = getOutput(exec_result).substring(27, 63); // TODO grep ID
		// get task info
		exec_result = sys.task(taskId);
		Assert.assertTrue(exec_result.getExitCode()==0, "Check return code (system task)");
	}

	// TODO bz#974486
	@Test(description="system tasks", dependsOnMethods={"test_systemTask"}, enabled=false)
	public void test_systemTasks() {
		KatelloSystem sys =  new KatelloSystem(cli_worker, sys_name, org_name, null);
		exec_result = sys.tasks();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (system tasks)");
		Assert.assertTrue(getOutput(exec_result).contains("Package Install"), "Check output (system tasks)");
	}

	@Test(description="system task - bad id given")
	public void test_systemBadTask() {
		KatelloSystem sys =  new KatelloSystem(cli_worker, null, null, null);
		exec_result = sys.task("007");
		Assert.assertTrue(exec_result.getExitCode()==148, "Check return code (system task)");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloSystem.ERR_NO_TASK, "007")), "Check error message (system task)");
	}

	// TODO bz#983428
	@Test(description="system remove_deletion - invalid uuid given")
	public void test_removeDeletionBadUUID() {
		KatelloSystem sys = new KatelloSystem(cli_worker, null, null, null);
		sys.uuid = "007";
		exec_result = sys.remove();
		Assert.assertTrue(exec_result.getExitCode()==148, "Check exit code (system remove_deletion)");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloSystem.ERR_NO_DELETION_RECORD,sys.uuid)), "Check exit code (system remove_deletion)");
	}

	@Test(description="system register")
	public void test_systemRegister() {
		KatelloSystem sys = new KatelloSystem(cli_worker, sys_reg_name, org_name, KatelloEnvironment.LIBRARY);
		exec_result = sys.register();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (system register)");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloSystem.OUT_REGISTRED, sys_reg_name)), "Check output (system register)");
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



	@Test(description="add system to system group")
	public void test_addToGroup() {
		KatelloSystem sys = new KatelloSystem(cli_worker, sys_name, org_name, "Library");
		exec_result = sys.add_to_groups(sysgroup_name);
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (add system to group)");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloSystem.OUT_ADD_TO_GROUPS, sys_name)), "Check output (add system to group)");
	}

	// TODO bz#974098
	@Test(description="add system to nonexisting group")
	public void test_addToNonexistGroup() {
		KatelloSystem sys = new KatelloSystem(cli_worker, sys_name, org_name, "Library");
		exec_result = sys.add_to_groups(grp_nonexist_name);
		Assert.assertTrue(exec_result.getExitCode()==65, "Check exit code (add system to group)");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloSystemGroup.ERR_SYSTEMGROUP_NOTFOUND, grp_nonexist_name, org_name)), "Check output (add system to group)");
	}

	// TODO bz#982572
	@Test(description="add nonexisting system to group")
	public void test_addNonexistSystemToGroup() {
		KatelloSystem sys = new KatelloSystem(cli_worker, sys_nonexist_name, org_name, "Library");
		exec_result = sys.add_to_groups(sysgroup_name);
		Assert.assertTrue(exec_result.getExitCode()==65, "Check exit code (add system to group)");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloSystem.ERR_NOT_FOUND, sys_nonexist_name, org_name)), "Check output (add system to group)");
	}

	@Test(description="remove system from sytem groups")
	public void test_removeFromGroup() {
		KatelloSystem sys = new KatelloSystem(cli_worker, sys_name, org_name, "Library");
		exec_result = sys.remove_from_groups(sysgroup_name);
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (remove system from group)");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloSystem.OUT_REMOVE_FROM_GROUPS, sys_name)), "Check output (remove system from group)");
	}

	// TODO bz#974098
	@Test(description="remove system from system group - group not found")
	public void test_removeFromNonexistGroup() {
		KatelloSystem sys = new KatelloSystem(cli_worker, sys_name, org_name, "Library");
		exec_result = sys.remove_from_groups(grp_nonexist_name);
		Assert.assertTrue(exec_result.getExitCode()==65, "Check exit code (remove system from group)");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloSystemGroup.ERR_SYSTEMGROUP_NOTFOUND, grp_nonexist_name, org_name)), "Check output (remove system from group)");
	}

	// TODO bz#982572
	@Test(description="remove system from system group - system not found")
	public void test_removeNonexistSystemFromGroup() {
		KatelloSystem sys = new KatelloSystem(cli_worker, sys_nonexist_name, org_name, "Library");
		exec_result = sys.remove_from_groups(sysgroup_name);
		Assert.assertTrue(exec_result.getExitCode()==65, "Check exit code (remove system from group)");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloSystem.ERR_NOT_FOUND, sys_nonexist_name, org_name)), "Check output (system remove from group)");
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
