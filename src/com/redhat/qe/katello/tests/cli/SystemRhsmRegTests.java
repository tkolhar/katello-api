package com.redhat.qe.katello.tests.cli;

import java.util.logging.Logger;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloContentDefinition;
import com.redhat.qe.katello.base.obj.KatelloContentView;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.base.tngext.TngPriority;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.common.TngRunGroups;
import com.redhat.qe.tools.SSHCommandResult;

/**
 * RHSM related scenarios only.
 * @author gkhachik
 *
 */
@TngPriority(200)
@Test(groups={TngRunGroups.TNG_KATELLO_System_Consumer})
public class SystemRhsmRegTests extends KatelloCliTestBase{	
	protected static Logger log = Logger.getLogger(SystemRhsmRegTests.class.getName());

	private SSHCommandResult exec_result;
	private String orgNameRhsms;
	private String orgNameNoEnvs;
	
	private String envName_Dev;
	private String envName_Test;
	
	private String contentName;
	private String contentView;
	
	private String systemNameRegOnly;
	private String systemNameNoEnvReg;
	
	@BeforeClass(description="Generate unique names",groups={"headpin-cli"})
	public void setUp(){
		String uid = KatelloUtils.getUniqueID();
		this.orgNameRhsms = "orgMain-"+uid;
		this.orgNameNoEnvs = "orgNoEnv-"+uid;
		
		this.systemNameRegOnly = "sys-RegOnly-"+uid;
		this.systemNameNoEnvReg = "sys-NoEnvReg-"+uid;
		
		exec_result = new KatelloOrg(this.cli_worker, this.orgNameRhsms, null).cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");

		exec_result = new KatelloOrg(this.cli_worker, this.orgNameNoEnvs, null).cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");
		
		this.envName_Dev = null;
		this.envName_Test = null;
	}
	
	@BeforeClass(description="init: katello specific, no headpin", dependsOnMethods={"setUp"})
	public void setUp_katelloOnly(){
		String uid = KatelloUtils.getUniqueID();
		this.envName_Dev = "Dev-"+uid;
		this.envName_Test = "Test-"+uid;
		this.contentName = "content-" + uid;
		this.contentView = "contentView-"+uid;
		
		exec_result = new KatelloEnvironment(this.cli_worker, envName_Dev, null, this.orgNameRhsms, KatelloEnvironment.LIBRARY).cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");
		exec_result = new KatelloEnvironment(this.cli_worker, envName_Test, null, this.orgNameRhsms, KatelloEnvironment.LIBRARY).cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");
		//Associate content view to the environments
		
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

	@Test(description = "RHSM register - org have no environment but Locker only", groups={"headpin-cli"})
	public void test_rhsm_RegLibraryOnly(){
		rhsm_clean();
		KatelloSystem sys = new KatelloSystem(this.cli_worker, this.systemNameNoEnvReg, this.orgNameNoEnvs, null);
		exec_result = sys.rhsm_register(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(exec_result.getStdout().trim().contains(KatelloSystem.OUT_CREATE),
				"Check - output (success)");
		assert_systemInfo(sys);
	}

	@Test(description = "RHSM register - one environment only", dependsOnMethods = {"test_rhsm_RegLibraryOnly"})
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

	@Test(description = "RHSM register - already registered", groups={"headpin-cli"})
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
	@Test(description = "262c4394-0718-4336-a114-60130bd7f447", groups={"headpin-cli"})
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

	@Test(description = "RHSM register - more than one environment (no env. specified)", dependsOnMethods = {"test_rhsm_ForceReg"})
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

	@Test(description = "RHSM register - env specified", dependsOnMethods = {"test_rhsm_RegMultiEnv"})
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

	@Test(description = "RHSM register - same name for 2 environments", dependsOnMethods = {"test_rhsm_RegMultiEnv"})
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
}
