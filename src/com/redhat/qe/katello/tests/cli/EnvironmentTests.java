package com.redhat.qe.katello.tests.cli;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliDataProvider;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;
public class EnvironmentTests extends KatelloCliTestScript{
	   
	    private String organization;

		@BeforeClass(description="init: create org stuff", groups = {"headpin-cli"})
		public void setUp(){
			SSHCommandResult res;
			String uid = KatelloUtils.getUniqueID();
			this.organization = "env-"+uid;
			KatelloOrg org = new KatelloOrg(this.organization, null);
			res = org.cli_create();
			Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
			
			KatelloEnvironment env = new KatelloEnvironment("BAR", "BAR env", this.organization, KatelloEnvironment.LIBRARY);
			res = env.cli_create();
			Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		}
		
		
		@Test(description="create Environment", groups = {"headpin-cli"}, 
				dataProvider="environment_create", dataProviderClass = KatelloCliDataProvider.class, enabled=true)
		public void testEnv_create(String name, String descr, Integer exitCode, String output){
			SSHCommandResult res;
			
			KatelloEnvironment env = new KatelloEnvironment(name, descr, this.organization, KatelloEnvironment.LIBRARY);
			res = env.cli_create();
			Assert.assertTrue(res.getExitCode().intValue() == exitCode.intValue(), "Check - return code");
			
			if(exitCode.intValue()==0){ //
				Assert.assertTrue(getOutput(res).contains(output),"Check - returned output string");
			}else{ // Failure to be checked
				Assert.assertTrue(getOutput(res).contains(output),"Check - returned error string");
			}
		} 
	
		@Test(description="create Environment which name is Library, verify error es shown", groups = {"headpin-cli"})
		public void testCreateEnvironmentError() {
			SSHCommandResult res;
			String output = "Validation failed: Name : 'Library' is a built-in environment, Name of environment must be unique within one organization, Label : 'Library' is a built-in environment, Label of environment must be unique within one organization";
			
			KatelloEnvironment env = new KatelloEnvironment(KatelloEnvironment.LIBRARY, "Library env", this.organization, KatelloEnvironment.LIBRARY);
			res = env.cli_create();
			Assert.assertEquals(res.getExitCode().intValue(), 144, "Check - return code");
			Assert.assertTrue(getOutput(res).contains(output),"Check - returned error string");
			
			env = new KatelloEnvironment(KatelloEnvironment.LIBRARY, "Library env", this.organization, "BAR");
			res = env.cli_create();
			Assert.assertEquals(res.getExitCode().intValue(), 144, "Check - return code");
			Assert.assertTrue(getOutput(res).contains(output),"Check - returned error string");
		}
		
		@Test(description="Environment info",groups = {"headpin-cli"})
		public void testEnv_info()
		{
			SSHCommandResult res;
			String uid = KatelloUtils.getUniqueID();
			String name = "env-"+uid;
			String descr = "Environment "+ name  + " Created";
			KatelloEnvironment env = new KatelloEnvironment(name,descr,this.organization,KatelloEnvironment.LIBRARY);
			res = env.cli_create();
			Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
			Assert.assertTrue(getOutput(res).contains(
    				String.format(KatelloEnvironment.OUT_CREATE,name)), 
    				"Check - returned output string ("+KatelloEnvironment.CMD_CREATE+")");
			res = env.cli_info();
			Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
			
			 
		}
		
		
		@Test(description="delete a environment", groups = {"headpin-cli"},enabled=true)
		public void test_delete_environment(){
		            String uid = KatelloUtils.getUniqueID();
		            String envName="env-delete_act_key-"+ uid; 
		            SSHCommandResult res;
		            KatelloEnvironment env = new KatelloEnvironment(envName, "Environment created", this.organization, KatelloEnvironment.LIBRARY);
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
		    				String.format(KatelloEnvironment.ERROR_INFO,envName,this.organization)), 
		    				"Check - returned output string ("+KatelloEnvironment.CMD_INFO+")");	
		            res = env.cli_list();
		            Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (environment list)");
		            
		}    
		
		
		@Test(description="Environment update",groups = {"headpin-cli"})
		public void testEnv_update()
		{
			SSHCommandResult res;
			String uid = KatelloUtils.getUniqueID();
			String name = "env-"+uid;
			KatelloEnvironment env = new KatelloEnvironment(name,null,this.organization,KatelloEnvironment.LIBRARY);
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
		
	  	   
		@Test(description="Register same system to different environments",groups = {"headpin-cli"})
		public void testEnv_Regsystem()
		{

			SSHCommandResult res;
			KatelloSystem sys_reg;
			String uid = KatelloUtils.getUniqueID();
			String sys_name = "sys-reg-" + uid;
			String env1_name = "env1-"+uid;
			uid = KatelloUtils.getUniqueID();
	  	    String env2_name = "env2-"+uid;
	  	    String descr_env1 = "Environment "+ env1_name  + " Created";
	  	    String descr_env2 = "Environment "+ env2_name + " Created";
	  	    KatelloEnvironment env1 = new KatelloEnvironment(env1_name,descr_env1,this.organization,KatelloEnvironment.LIBRARY);
	  	    KatelloEnvironment env2 = new KatelloEnvironment(env2_name,descr_env2,this.organization,KatelloEnvironment.LIBRARY);
	  	    sys_reg = new KatelloSystem(sys_name,this.organization,env1_name);
	  	    res = env1.cli_create();
	  	    Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
	  	    Assert.assertTrue(getOutput(res).contains(String.format(KatelloEnvironment.OUT_CREATE,env1_name)),"Check - returned output string ("+KatelloEnvironment.CMD_CREATE+")");
	  	    res = sys_reg.rhsm_registerForce();
	  	    Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
	  	    Assert.assertTrue(res.getStdout().trim().contains(KatelloSystem.OUT_CREATE),"Check - output (success)");
	  	    res = env1.cli_info();      
	  	    Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
	  	    sys_reg = new KatelloSystem(sys_name,this.organization,env2_name);  
	  	    res = env2.cli_create();
	  	    Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
	  	    Assert.assertTrue(getOutput(res).contains(String.format(KatelloEnvironment.OUT_CREATE,env2_name)),"Check - returned output string ("+KatelloEnvironment.CMD_CREATE+")");
	  	    res = sys_reg.rhsm_registerForce();
	  	    Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
	  	    Assert.assertTrue(res.getStdout().trim().contains(KatelloSystem.OUT_CREATE),"Check - output (success)");
	  	    res = env2.cli_info();      
	  	    Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");

		}
}