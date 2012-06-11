package com.redhat.qe.katello.tests.cli;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.redhat.qe.auto.testng.Assert;
import com.redhat.qe.katello.base.KatelloCliDataProvider;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.tools.SSHCommandResult;
public class EnvironmentTests extends KatelloCliTestScript{
	   
	    private String organization;

		@BeforeClass(description="init: create org stuff", groups = {"headpin-cli"})
		public void setUp(){
			SSHCommandResult res;
			String uid = KatelloTestScript.getUniqueID();
			this.organization = "env-"+uid;
			KatelloOrg org = new KatelloOrg(this.organization, null);
			res = org.cli_create();
			Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
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
	
		
		@Test(description="Environment info",groups = {"headpin-cli"})
		public void testEnv_info()
		{
			SSHCommandResult res;
			String uid = KatelloTestScript.getUniqueID();
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
		            String uid = KatelloTestScript.getUniqueID();
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
			String uid = KatelloTestScript.getUniqueID();
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
		
		
		

}
