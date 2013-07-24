package com.redhat.qe.katello.tests.cli;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliDataProvider;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloContentDefinition;
import com.redhat.qe.katello.base.obj.KatelloContentView;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.common.TngRunGroups;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups=TngRunGroups.TNG_KATELLO_Environment)
public class EnvironmentTests extends KatelloCliTestBase{

	@BeforeClass(description="init: create org stuff")
	public void setUp(){
		KatelloEnvironment env = new KatelloEnvironment(this.cli_worker, "BAR", "BAR env", base_org_name, KatelloEnvironment.LIBRARY);
		SSHCommandResult res = env.cli_create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
	}

	//TODO BZ: 987670
	@Test(description="create Environment",  
			dataProvider="environment_create", dataProviderClass = KatelloCliDataProvider.class, enabled=true)
	public void testEnv_create(String name, String descr, Integer exitCode, String output){
		SSHCommandResult res;

		KatelloEnvironment env = new KatelloEnvironment(this.cli_worker, name, descr, base_org_name, KatelloEnvironment.LIBRARY);
		res = env.cli_create();
		Assert.assertTrue(res.getExitCode().intValue() == exitCode.intValue(), "Check - return code");

		if(exitCode.intValue()==0){ //
			Assert.assertTrue(getOutput(res).contains(output),"Check - returned output string");
		}else{ // Failure to be checked
			Assert.assertTrue(getOutput(res).contains(output),"Check - returned error string");
		}
	} 

	@Test(description="create Environment which name is Library, verify error es shown")
	public void testCreateEnvironmentError() {
		SSHCommandResult res;
		String output = "Validation failed: Name : 'Library' is a built-in environment, Name of environment must be unique within one organization, Label : 'Library' is a built-in environment, Label of environment must be unique within one organization";

		KatelloEnvironment env = new KatelloEnvironment(this.cli_worker, KatelloEnvironment.LIBRARY, "Library env", base_org_name, KatelloEnvironment.LIBRARY);
		res = env.cli_create();
		Assert.assertEquals(res.getExitCode().intValue(), 166, "Check - return code");
		Assert.assertTrue(getOutput(res).contains(output),"Check - returned error string");

		env = new KatelloEnvironment(this.cli_worker, KatelloEnvironment.LIBRARY, "Library env", base_org_name, "BAR");
		res = env.cli_create();
		Assert.assertEquals(res.getExitCode().intValue(), 166, "Check - return code");
		Assert.assertTrue(getOutput(res).contains(output),"Check - returned error string");
	}

	@Test(description="Environment info")
	public void testEnv_info()
	{
		SSHCommandResult res;
		String uid = KatelloUtils.getUniqueID();
		String name = "env-"+uid;
		String descr = "Environment "+ name  + " Created";
		KatelloEnvironment env = new KatelloEnvironment(this.cli_worker, name,descr,base_org_name,KatelloEnvironment.LIBRARY);
		res = env.cli_create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(res).contains(
				String.format(KatelloEnvironment.OUT_CREATE,name)), 
				"Check - returned output string ("+KatelloEnvironment.CMD_CREATE+")");
		res = env.cli_info();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");


	}

	// @ TODO 961112
	@Test(description="delete a environment",enabled=true)
	public void test_delete_environment(){
		String uid = KatelloUtils.getUniqueID();
		String envName="env-delete_act_key-"+ uid; 
		SSHCommandResult res;
		KatelloEnvironment env = new KatelloEnvironment(this.cli_worker, envName, "Environment created", base_org_name, KatelloEnvironment.LIBRARY);
		res = env.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (environment create)");
		Assert.assertTrue(getOutput(res).contains(
				String.format(KatelloEnvironment.OUT_CREATE,envName)), 
				"Check - returned output string ("+KatelloEnvironment.CMD_CREATE+")");
		res = env.cli_delete();
		Assert.assertTrue(res.getExitCode().intValue() == 0,"Check - return code (environment delete)");
		Assert.assertTrue(getOutput(res).contains(
				String.format(KatelloEnvironment.OUT_DELETE,envName)), 
				"Check - returned output string ("+KatelloEnvironment.CMD_DELETE+")");

		res = env.cli_info();
		Assert.assertTrue(res.getExitCode().intValue() == 65,"Check - return code (environment delete)");
		Assert.assertTrue(getOutput(res).contains(
				String.format(KatelloEnvironment.ERROR_INFO,envName,base_org_name)), 
				"Check - returned output string ("+KatelloEnvironment.CMD_INFO+")");	
		res = env.cli_list();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (environment list)");
	}    

	@Test(description="Environment update")
	public void testEnv_update()
	{
		SSHCommandResult res;
		String uid = KatelloUtils.getUniqueID();
		String name = "env-"+uid;
		KatelloEnvironment env = new KatelloEnvironment(this.cli_worker, name,null,base_org_name,KatelloEnvironment.LIBRARY);
		res = env.cli_create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(res).contains(
				String.format(KatelloEnvironment.OUT_CREATE,name)), 
				"Check - returned output string ("+KatelloEnvironment.CMD_CREATE+")");
		res = env.cli_info();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		String descr = "Updating environment";
		res = env.cli_update(descr);
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(res).contains(
				String.format(KatelloEnvironment.OUT_UPDATE,name)), 
				"Check - returned output string ("+KatelloEnvironment.CMD_UPDATE+")");
		res = env.cli_info();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");

	}


	@Test(description="Register same system to different environments")
	public void testEnv_Regsystem()
	{

		SSHCommandResult res;
		KatelloSystem sys_reg;
		String uid = KatelloUtils.getUniqueID();
		String sys_name = "sys-reg-" + uid;
		String env1_name = "env1-"+uid;
		uid = KatelloUtils.getUniqueID();
		String env2_name = "env2-"+uid;
		String contentName = "content-" + uid;
		String contentView = "contentView-"+uid;
		String descr_env1 = "Environment "+ env1_name  + " Created";
		String descr_env2 = "Environment "+ env2_name + " Created";
		
		KatelloEnvironment env1 = new KatelloEnvironment(this.cli_worker, env1_name,descr_env1,base_org_name,KatelloEnvironment.LIBRARY);
		KatelloEnvironment env2 = new KatelloEnvironment(this.cli_worker, env2_name,descr_env2,base_org_name,KatelloEnvironment.LIBRARY);
		sys_reg = new KatelloSystem(this.cli_worker, sys_name,base_org_name,env1_name);
		
		KatelloContentDefinition content = new KatelloContentDefinition(this.cli_worker, contentName, "descritpion", base_org_name, contentName);
		exec_result = content.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = content.publish(contentView, contentView, "New Content View");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		res = env1.cli_create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(res).contains(String.format(KatelloEnvironment.OUT_CREATE,env1_name)),"Check - returned output string ("+KatelloEnvironment.CMD_CREATE+")");
		
		res = env2.cli_create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(res).contains(String.format(KatelloEnvironment.OUT_CREATE,env2_name)),"Check - returned output string ("+KatelloEnvironment.CMD_CREATE+")");
		
		KatelloContentView conView = new KatelloContentView(this.cli_worker, contentView, base_org_name);
		exec_result = conView.promote_view(env1_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = conView.promote_view(env2_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		res = sys_reg.rhsm_registerForce();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(res.getStdout().trim().contains(KatelloSystem.OUT_CREATE),"Check - output (success)");
		res = env1.cli_info();      
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		sys_reg = new KatelloSystem(this.cli_worker, sys_name,base_org_name,env2_name); 
		
		res = sys_reg.rhsm_registerForce();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(res.getStdout().trim().contains(KatelloSystem.OUT_CREATE),"Check - output (success)");
		res = env2.cli_info();      
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");

	}

	/** TCMS scenario is: <a href="https://tcms.engineering.redhat.com/case/273128/?from_plan=7843">here</a> */
	@Test(description="62ac0445-1d73-4cdd-a759-224e0adfb42c")
	public void testDeleteLibrary() {
		SSHCommandResult res;
		String uid = KatelloUtils.getUniqueID();
		String orgname = "org-"+uid;

		KatelloOrg org = new KatelloOrg(this.cli_worker, orgname, null);
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode() == 0, "check exit code (create org)");

		KatelloEnvironment env = new KatelloEnvironment(this.cli_worker, KatelloEnvironment.LIBRARY, null, orgname, null);
		res = env.cli_delete();
		Assert.assertTrue(res.getExitCode() != 0, "check exit code (delete Library)");
	}
}
