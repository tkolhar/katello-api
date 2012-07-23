package com.redhat.qe.katello.tests.cli;
import org.testng.annotations.Test;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliDataProvider;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.katello.base.obj.KatelloClient;

import com.redhat.qe.tools.SSHCommandResult;
@Test(groups={"headpin-cli"})
public class ClientTests extends KatelloCliTestScript{
	
	private SSHCommandResult exec_result;
	
	@Test(description = "Saved_options list options saved in the client config")
	public void test_SavedOptions(){
		KatelloClient client_obj= new KatelloClient();
		exec_result = client_obj.cli_saved_options(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
	}
	
	
	@Test(description = "forget : remove an option from the client config")
	public void test_Forget(){
			
		String uid = KatelloTestScript.getUniqueID();
		String option = "op-"+uid;
	    String value = "val-"+uid;
		KatelloClient client_obj= new KatelloClient(option,value);
		exec_result = client_obj.cli_remember();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code (client remember)");
		Assert.assertTrue(getOutput(exec_result).contains(
 				String.format(KatelloClient.OUT_REMEMBER,option)), 
 				"Check - returned output string ("+KatelloClient.CMD_REMEMBER+")");
		exec_result = client_obj.cli_forget();
	    Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code (client forget)");
		Assert.assertTrue(getOutput(exec_result).contains(
 				String.format(KatelloClient.OUT_FORGET,option)), 
 				"Check - returned output string ("+KatelloClient.CMD_FORGET+")");		
		
	}
	
	
	
	@Test(description="Client Remember", groups = {"headpin-cli"}, 
			dataProvider="client_remember", dataProviderClass = KatelloCliDataProvider.class, enabled=true)
	public void testClient_Remember(String option, String value, Integer exitCode, String output){
		SSHCommandResult exec_result;
		
		KatelloClient client_obj = new KatelloClient(option,value);
		exec_result = client_obj.cli_remember();
		Assert.assertTrue(exec_result.getExitCode().intValue() == exitCode.intValue(), "Check - return code");
		
		if(exitCode.intValue()==0){ //
			Assert.assertTrue(getOutput(exec_result).contains(output),"Check - returned output string");
		}else{ // Failure to be checked
			Assert.assertTrue(getOutput(exec_result).contains(output),"Check - returned error string");
		}
	} 
   
}
