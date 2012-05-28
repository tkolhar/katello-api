	package com.redhat.qe.katello.tests.cli;

import java.util.logging.Logger;

import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.redhat.qe.auto.testng.Assert;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups={"cfse-cli","headpin-cli"})
public class SystemTests extends KatelloCliTestScript{	
	protected static Logger log = Logger.getLogger(SystemTests.class.getName());
	
	private SSHCommandResult exec_result;
	private String orgName;
	private String envName_Dev;
	private String envName_Test;
	
	@BeforeTest(description="Generate unique names")
	public void setUp(){
		String uid = KatelloTestScript.getUniqueID();
		this.orgName = "org-rhsm-"+uid;
		this.envName_Dev = "Dev-"+uid;
		this.envName_Test = "Test-"+uid;
		
		KatelloOrg org = new KatelloOrg(this.orgName, null);
		exec_result = org.cli_create();
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code (org create)");
		Assert.assertTrue(exec_result.getStdout().trim().equals(String.format(KatelloOrg.OUT_CREATE,this.orgName)),
				"Check - returned message");
		
		rhsm_clean(); // clean - in case of it registered
		exec_result = clienttasks.execute_remote(KatelloSystem.RHSM_CREATE);
		if(exec_result.getStderr().contains("certificate verify failed")){ // It's Jenkins's special server with it's own certificate. Exit Scenarios
			log.warning("Seems your server uses its own certificate: RHSM tests can't run there - certificate issue");
			throw new SkipException("RHSM tests can not run on this specific server. Certificate issues.");
		}
		rhsm_clean();
	}
	
	@Test(description = "RHSM register - org have no environment but Locker only", enabled=true)
	public void test_rhsm_RegLockerOnly(){
		KatelloSystem sys = new KatelloSystem(clienttasks, "localhost"+KatelloTestScript.getUniqueID(), this.orgName, null);
		exec_result = sys.rhsm_register(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 255, "Check - return code");
		Assert.assertEquals(exec_result.getStderr().trim(), 
				String.format(KatelloSystem.ERR_RHSM_LOCKER_ONLY,this.orgName, KatelloEnvironment.LIBRARY),
				"Check - please create an env.");
	}
	
	@Test(description = "RHSM register - one environment only", 
			dependsOnMethods = {"test_rhsm_RegLockerOnly"}, enabled=true)
	public void test_rhsm_RegOneEnvOnly(){
		String uid = KatelloTestScript.getUniqueID();
		String system = "rhsm-reg-"+uid;
		
		// Create the env.
		KatelloEnvironment env = new KatelloEnvironment(this.envName_Dev, null, this.orgName, KatelloEnvironment.LIBRARY);
		env.cli_create();		
		KatelloSystem sys = new KatelloSystem(clienttasks, system, this.orgName, null);
		exec_result = sys.rhsm_register(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(exec_result.getStdout().trim().contains(KatelloSystem.OUT_CREATE),
				"Check - output (success)");
	}
	
	@Test(description = "RHSM register - already registered", 
			dependsOnMethods = {"test_rhsm_RegOneEnvOnly"}, enabled=true)
	public void test_rhsm_AlreadyReg(){
		String uid = KatelloTestScript.getUniqueID();
		String system = "rhsm-reg1-"+uid;
		
		KatelloSystem sys = new KatelloSystem(clienttasks, system, this.orgName, null);
		exec_result = sys.rhsm_register(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(exec_result.getStdout().trim().contains(KatelloSystem.OUT_CREATE),
				"Check - output (success)");
		
		//2-nd attempt
		system = "rhsm-reg2-"+uid;
		sys = new KatelloSystem(clienttasks, system, this.orgName, null);
		exec_result = sys.rhsm_register();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 1, "Check - return code");
		Assert.assertTrue(exec_result.getStdout().trim().contains(KatelloSystem.ERR_RHSM_REG_ALREADY_FORCE_NEEDED),
				"Check - output (--force needed)");
	}
	
	@Test(description = "RHSM force register", 
			dependsOnMethods = {"test_rhsm_AlreadyReg"}, enabled=true)
	public void test_rhsm_ForceReg(){
		String uid = KatelloTestScript.getUniqueID();
		String system = "rhsm-force-"+uid;
		
		KatelloSystem sys = new KatelloSystem(clienttasks, system, this.orgName, null);
		exec_result = sys.rhsm_register(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(exec_result.getStdout().trim().contains(KatelloSystem.OUT_CREATE),
				"Check - output (success)");
		
		//re-register with --force option
		exec_result = sys.rhsm_registerForce();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code (register --force)");
		String REGEXP_UNREGISTERED = ".*The system with UUID .* has been unregistered.*";
		Assert.assertTrue(exec_result.getStdout().replaceAll("\n", "").matches(REGEXP_UNREGISTERED),"Check - system is unregistered");			
		Assert.assertTrue(exec_result.getStdout().trim().contains(KatelloSystem.OUT_CREATE),
				"Check - output (system registered --force)");
	}

	@Test(description = "RHSM register - more than one environment (no env. specified)", 
			dependsOnMethods = {"test_rhsm_ForceReg"}, enabled=true)
	public void test_rhsm_RegMultiEnv(){
		String uid = KatelloTestScript.getUniqueID();
		String system = "rhsm-regMultiEnv-"+uid;
		
		// Create the 2nd env.
		KatelloEnvironment env = new KatelloEnvironment(this.envName_Test, null, this.orgName, KatelloEnvironment.LIBRARY);
		env.cli_create();		
		KatelloSystem sys = new KatelloSystem(clienttasks, system, this.orgName, null);
		exec_result = sys.rhsm_register(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 255, "Check - return code");
		Assert.assertTrue(exec_result.getStderr().trim().contains(
				String.format(KatelloSystem.ERR_RHSM_REG_MULTI_ENV, this.orgName)),
				"Check - output (rhsm register - multi envs. exist)");
	}
	
	@Test(description = "RHSM register - env specified", 
			dependsOnMethods = {"test_rhsm_RegMultiEnv"}, enabled=true)
	public void test_rhsm_RegWithEnv(){
		String uid = KatelloTestScript.getUniqueID();
		String system = "rhsm-env-"+uid;
		
		KatelloSystem sys = new KatelloSystem(clienttasks, system, this.orgName, this.envName_Test);
		exec_result = sys.rhsm_register(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(exec_result.getStdout().trim().contains(KatelloSystem.OUT_CREATE),
				"Check - output (success)");
	}
	
	@Test(description = "RHSM register - same name for 2 environments", 
			dependsOnMethods = {"test_rhsm_RegMultiEnv"}, enabled=true)
	public void test_rhsm_RegSameNameTwoEnvs(){
		String uid = KatelloTestScript.getUniqueID();
		String system = "localhost-"+uid;
		
		KatelloSystem sys = new KatelloSystem(clienttasks, system, this.orgName, this.envName_Dev);
		exec_result = sys.rhsm_register(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(exec_result.getStdout().trim().contains(KatelloSystem.OUT_CREATE),
				"Check - output (success)");
		
		clienttasks.execute_remote("subscription-manager clean");
		
		sys = new KatelloSystem(clienttasks, system, this.orgName, this.envName_Test);
		exec_result = sys.rhsm_register(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(exec_result.getStdout().trim().contains(KatelloSystem.OUT_CREATE),
				"Check - output (success)");
		
		sys = new KatelloSystem(clienttasks, null, this.orgName, null);
		KatelloCli cli = new KatelloCli("system list --org "+this.orgName+" -v | grep \""+system+"\" | wc -l", null);
		exec_result = cli.run();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code (grep: `system list --org`)");
		Assert.assertTrue(exec_result.getStdout().replaceAll("\n", "").trim().equals("2"), "Check - 2 systems are registered with the same name");
	}
		
	@AfterMethod(description = "Clean RHSM data - prepare for next scenario run", alwaysRun = true)
	public void clean_rhsm(){
		clienttasks.execute_remote("subscription-manager clean");
	}
	
	
	@AfterTest(description="erase registration made; cleanup",alwaysRun=true)
	public void tearDown(){
		clienttasks.execute_remote("subscription-manager clean");
	}
}
