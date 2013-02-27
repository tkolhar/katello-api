	package com.redhat.qe.katello.tests.cli;

import java.util.logging.Logger;
import org.testng.annotations.AfterMethod;
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
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

public class SystemTests extends KatelloCliTestScript{	
	protected static Logger log = Logger.getLogger(SystemTests.class.getName());
	
	private SSHCommandResult exec_result;
	private String orgName;
	private String orgName2;
	private String envName_Dev;
	private String envName_Test;
	private String envName_Prod;
	private String provider_name;
	private String product_name;
	private String repo_name;
	private String changeset_name;
	
	@BeforeClass(description="Generate unique names",groups={"cfse-cli"})
	public void setUp(){
		String uid = KatelloUtils.getUniqueID();
		this.orgName = "org-rhsm-"+uid;
		this.orgName2 = "org-sys-"+uid;
		this.envName_Dev = "Dev-"+uid;
		this.envName_Test = "Test-"+uid;
		this.envName_Prod = "Prod-"+uid;
		this.provider_name = "provider_"+uid;
		this.product_name = "product_"+uid;
		this.repo_name = "repo_"+uid;
		this.provider_name = "provider_"+uid;
		this.product_name = "product_"+uid;
		this.repo_name = "repo_name_"+uid;
		this.changeset_name = "changeset_"+uid;
		
		KatelloOrg org = new KatelloOrg(this.orgName, null);
		exec_result = org.cli_create();
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code (org create)");
		Assert.assertTrue(exec_result.getStdout().trim().equals(String.format(KatelloOrg.OUT_CREATE,this.orgName)),
				"Check - returned message");
		
		org = new KatelloOrg(this.orgName2, null);
		exec_result = org.cli_create();
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code (org create)");
		
		// Create provider:
		KatelloProvider prov = new KatelloProvider(provider_name, orgName2,
				"Package provider", null);
		exec_result = prov.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		// Create product:
		KatelloProduct prod = new KatelloProduct(product_name, orgName2,
				provider_name, null, null, null, null, null);
		exec_result = prod.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		KatelloRepo repo = new KatelloRepo(repo_name, orgName2, product_name, REPO_INECAS_ZOO3, null, null);
		exec_result = repo.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		KatelloEnvironment env = new KatelloEnvironment(envName_Prod, null, orgName2, KatelloEnvironment.LIBRARY);
		exec_result = env.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (env create)");
		
		// promote product to the env prod.
		exec_result = prod.promote(envName_Prod);
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code (product promote)");

		exec_result = repo.synchronize();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		KatelloChangeset cs = new KatelloChangeset(changeset_name, orgName2, envName_Prod);
		exec_result = cs.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (changeset create)");
		
		exec_result = cs.update_addProduct(product_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (changeset update add product)");
		
		exec_result = cs.apply();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (changeset promote)");
		
		rhsm_clean(); // clean - in case of it registered
	}
	
	@Test(description = "RHSM register - org have no environment but Locker only", enabled=true,groups={"cfse-cli"})
	public void test_rhsm_RegLockerOnly(){
		KatelloSystem sys = new KatelloSystem("localhost"+KatelloUtils.getUniqueID(), this.orgName, null);
		exec_result = sys.rhsm_register(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 255, "Check - return code");
		Assert.assertEquals(exec_result.getStderr().trim(), 
				String.format(KatelloSystem.ERR_RHSM_LOCKER_ONLY,this.orgName, KatelloEnvironment.LIBRARY),
				"Check - please create an env.");
	}
	
	@Test(description = "RHSM register - one environment only", 
			dependsOnMethods = {"test_rhsm_RegLockerOnly"}, enabled=true,groups={"cfse-cli"})
	public void test_rhsm_RegOneEnvOnly(){
		String uid = KatelloUtils.getUniqueID();
		String system = "rhsm-reg-"+uid;
		
		// Create the env.
		KatelloEnvironment env = new KatelloEnvironment(this.envName_Dev, null, this.orgName, KatelloEnvironment.LIBRARY);
		env.cli_create();		
		KatelloSystem sys = new KatelloSystem(system, this.orgName, null);
		exec_result = sys.rhsm_register(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(exec_result.getStdout().trim().contains(KatelloSystem.OUT_CREATE),
				"Check - output (success)");
		assert_systemInfo(sys);
	}
	
	@Test(description = "RHSM register - already registered", 
			dependsOnMethods = {"test_rhsm_RegOneEnvOnly"}, enabled=true,groups={"cfse-cli"})
	public void test_rhsm_AlreadyReg(){
		String uid = KatelloUtils.getUniqueID();
		String system = "rhsm-reg1-"+uid;
		
		KatelloSystem sys = new KatelloSystem(system, this.orgName, null);
		exec_result = sys.rhsm_register(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(exec_result.getStdout().trim().contains(KatelloSystem.OUT_CREATE),
				"Check - output (success)");
		
		//2-nd attempt
		system = "rhsm-reg2-"+uid;
		sys = new KatelloSystem(system, this.orgName, null);
		exec_result = sys.rhsm_register();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 1, "Check - return code");
		Assert.assertTrue(exec_result.getStdout().trim().contains(KatelloSystem.ERR_RHSM_REG_ALREADY_FORCE_NEEDED),
				"Check - output (--force needed)");
	}
	
	@Test(description = "RHSM force register", 
			dependsOnMethods = {"test_rhsm_AlreadyReg"}, enabled=true,groups={"cfse-cli"})
	public void test_rhsm_ForceReg(){
		String uid = KatelloUtils.getUniqueID();
		String system = "rhsm-force-"+uid;
		
		KatelloSystem sys = new KatelloSystem(system, this.orgName, null);
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
			dependsOnMethods = {"test_rhsm_ForceReg"}, enabled=true,groups={"cfse-cli"})
	public void test_rhsm_RegMultiEnv(){
		String uid = KatelloUtils.getUniqueID();
		String system = "rhsm-regMultiEnv-"+uid;
		
		// Create the 2nd env.
		KatelloEnvironment env = new KatelloEnvironment(this.envName_Test, null, this.orgName, KatelloEnvironment.LIBRARY);
		env.cli_create();		
		KatelloSystem sys = new KatelloSystem(system, this.orgName, null);
		exec_result = sys.rhsm_register(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 255, "Check - return code");
		Assert.assertTrue(exec_result.getStderr().trim().contains(
				String.format(KatelloSystem.ERR_RHSM_REG_MULTI_ENV, this.orgName)),
				"Check - output (rhsm register - multi envs. exist)");
	}
	
	@Test(description = "RHSM register - env specified", 
			dependsOnMethods = {"test_rhsm_RegMultiEnv"}, enabled=true,groups={"cfse-cli"})
	public void test_rhsm_RegWithEnv(){
		String uid = KatelloUtils.getUniqueID();
		String system = "rhsm-env-"+uid;
		
		KatelloSystem sys = new KatelloSystem(system, this.orgName, this.envName_Test);
		exec_result = sys.rhsm_register(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(exec_result.getStdout().trim().contains(KatelloSystem.OUT_CREATE),
				"Check - output (success)");
	}
	
	@Test(description = "RHSM register - same name for 2 environments", 
			dependsOnMethods = {"test_rhsm_RegMultiEnv"}, enabled=true,groups={"cfse-cli"})
	public void test_rhsm_RegSameNameTwoEnvs(){
		String uid = KatelloUtils.getUniqueID();
		String system = "localhost-"+uid;
		
		KatelloSystem sys = new KatelloSystem(system, this.orgName, this.envName_Dev);
		exec_result = sys.rhsm_register(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(exec_result.getStdout().trim().contains(KatelloSystem.OUT_CREATE),
				"Check - output (success)");
		assert_systemInfo(sys);
		
		KatelloUtils.sshOnClient("subscription-manager clean");
		
		sys = new KatelloSystem(system, this.orgName, this.envName_Test);
		exec_result = sys.rhsm_register(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(exec_result.getStdout().trim().contains(KatelloSystem.OUT_CREATE),
				"Check - output (success)");
		
		sys = new KatelloSystem(null, this.orgName, null);
		KatelloCli cli = new KatelloCli("system list --org "+this.orgName+" -v | grep \""+system+"\" | wc -l", null);
		exec_result = cli.run();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code (grep: `system list --org`)");
		Assert.assertTrue(exec_result.getStdout().replaceAll("\n", "").trim().equals("2"), "Check - 2 systems are registered with the same name");
	}

	//@ TODO enable after bug 878867 is fixed.
	@Test(description = "delete registered system and verifies that it is removed successfully", enabled=false,groups={"cfse-cli"})
	public void test_deleteSystem(){
		String uid = KatelloUtils.getUniqueID();
		String system = "localhost-"+uid;
		
		KatelloSystem sys = new KatelloSystem(system, this.orgName, this.envName_Dev);
		exec_result = sys.rhsm_register(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		
		exec_result = sys.info();
		String uuid = KatelloCli.grepCLIOutput("Uuid", getOutput(exec_result).trim(),1);
		sys.setUuid(uuid);
		
		exec_result = sys.remove();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(exec_result.getStdout().trim().contains(KatelloSystem.OUT_DELETE),
				"Check - output (success)");
		
		exec_result = sys.info();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 65, "Check - return code");
	}

	@Test(description = "subscribe system to pool",groups={"cfse-cli"})
	public void test_subscribeSystem(){
		String uid = KatelloUtils.getUniqueID();
		String system = "localhost-"+uid;
		
		KatelloSystem sys = new KatelloSystem(system, this.orgName2, this.envName_Prod);
		exec_result = sys.rhsm_register(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		
		exec_result = sys.subscriptions_available();
		String poolId1 = KatelloCli.grepCLIOutput("Id", getOutput(exec_result).trim(),1);
		Assert.assertNotNull(poolId1, "Check - pool Id is not null");
		
		exec_result = sys.subscribe(poolId1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertEquals(exec_result.getStdout().trim(), 
				String.format(KatelloSystem.OUT_SUBSCRIBE, system),
				"Check - subscribe system output.");
	}

	@Test(description = "rename the system",groups={"cfse-cli"})
	public void test_renameSystem(){
		String uid = KatelloUtils.getUniqueID();
		String system = "rhsm-reg-"+uid;
		
		// Create the env.
		KatelloEnvironment env = new KatelloEnvironment(this.envName_Dev, null, this.orgName, KatelloEnvironment.LIBRARY);
		env.cli_create();		
		KatelloSystem sys = new KatelloSystem(system, this.orgName, null);
		exec_result = sys.rhsm_registerForce(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		
		system = sys.name+"new";
		exec_result = sys.update_name(system);
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		
		exec_result = sys.info();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 65, "Check - return code");
		
		sys.setName(system);
		assert_systemInfo(sys);
	}
	
	
	
	@Test(description = "Remove Custom Info - Remove custom information for a system",groups={"cfse-cli","headpin-cli"})
	public void test_removeCustom_infoSystem(){
		SSHCommandResult res;
		String uid = KatelloUtils.getUniqueID();
		String org_name = "org-" + uid;
		String env_name = "env-" + uid;
		String sys_name = "sys-" + uid;
		String key_name = "keyname-" + uid;
		String value = "value-" + uid;
		KatelloOrg org = new KatelloOrg(org_name,null);
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode().intValue() == 0,"Check - return code");
		KatelloEnvironment env = new KatelloEnvironment(env_name, null,org_name,KatelloEnvironment.LIBRARY);
		res = env.cli_create();
		KatelloSystem sys = new KatelloSystem(sys_name, org_name, env_name);
		res = sys.rhsm_registerForce();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		res = sys.add_custom_info(key_name, value);
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(res).contains(String.format("Successfully added Custom Information [ %s : %s ] to System [ %s ]",key_name,value,sys_name)),"Check - return string");
		res = sys.info();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		res = sys.remove_custom_info(key_name);
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(res).contains(String.format("Successfully removed Custom Information from System [ %s ]",sys_name)),"Check - return string");
		res = org.delete();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		
	}
	
	
	@Test(description = "Update Custom Info - Edit custom information for a system",groups={"cfse-cli","headpin-cli"})
	public void test_updateCustom_infoSystem(){
		SSHCommandResult res;
		String uid = KatelloUtils.getUniqueID();
		String org_name = "org-" + uid;
		String env_name = "env-" + uid;
		String sys_name = "sys-" + uid;
		String key_name = "keyname-" + uid;
		String value = "value-" + uid;
		String update_uid = KatelloUtils.getUniqueID();
		String update_value = "update-value-" + update_uid;
		KatelloOrg org = new KatelloOrg(org_name,null);
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode().intValue() == 0,"Check - return code");
		KatelloEnvironment env = new KatelloEnvironment(env_name, null,org_name,KatelloEnvironment.LIBRARY);
		res = env.cli_create();
		KatelloSystem sys = new KatelloSystem(sys_name, org_name, env_name);
		res = sys.rhsm_registerForce();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		res = sys.add_custom_info(key_name, value);
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(res).contains(String.format("Successfully added Custom Information [ %s : %s ] to System [ %s ]",key_name,value,sys_name)),"Check - return string");
		res = sys.info();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		res = sys.update_custom_info(key_name, update_value);
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(res).contains(String.format("Successfully updated Custom Information [ %s ] for System [ %s ]",key_name,sys_name)),"Check - return string");
		res = sys.info();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		
		
		
	}
	
	
	@Test(description = "Add Custom Info - Create custom information for a system",dataProviderClass = KatelloCliDataProvider.class,dataProvider = "add_custom_info",groups={"cfse-cli","headpin-cli"})
	public void test_addCustom_infoSystem(String keyname,String value,Integer exitCode,String output){
		
			SSHCommandResult res;
			String uid = KatelloUtils.getUniqueID();
			String org_name = "org-add-info-" + uid;
			String env_name = "env-add-info-" + uid;
			String sys_name = "sys-add-info-" + uid;
			KatelloOrg org = new KatelloOrg(org_name,null);
			res = org.cli_create();
			KatelloEnvironment env = new KatelloEnvironment(env_name,null,org_name,KatelloEnvironment.LIBRARY);
			res= env.cli_create();
			KatelloSystem sys = new KatelloSystem(sys_name, org_name, env_name);
			res = sys.rhsm_registerForce();
			res = sys.add_custom_info(keyname,value);
			Assert.assertTrue(res.getExitCode().intValue() == exitCode.intValue(), "Check - return code");
			if(res.getExitCode().intValue() == 0 ){ //
				Assert.assertTrue(getOutput(res).contains(String.format("Successfully added Custom Information [ %s : %s ] to System [ %s ]",keyname,value,sys_name)),"Check - returned output string");
			}
			else
			{ // Failure to be checked
				Assert.assertTrue(getOutput(res).contains(output),"Check - returned error string");
			}
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
		
	@AfterMethod(description = "Clean RHSM data - prepare for next scenario run", alwaysRun = true)
	public void clean_rhsm(){
		KatelloUtils.sshOnClient("subscription-manager clean");
	}
	
	
	@AfterTest(description="erase registration made; cleanup",alwaysRun=true)
	public void tearDown(){
		KatelloUtils.sshOnClient("subscription-manager clean");
	}
}
