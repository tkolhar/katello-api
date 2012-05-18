package com.redhat.qe.katello.tests.cli;

import org.testng.annotations.Test;

import com.redhat.qe.auto.testng.Assert;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.katello.base.obj.KatelloUser;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups={"cfse-cli","headpin-cli"})
public class UserTests extends KatelloCliTestScript{

	@Test(description="create user - for default org", enabled=true)
	public void test_create_DefaultOrg(){
		SSHCommandResult res;
		String uniqueID = KatelloTestScript.getUniqueID();
		String username = "usr-"+uniqueID;
		String userpass = "password";
		String usermail = username+"@localhost";
		
		KatelloUser usr = new KatelloUser(username, usermail, userpass, false);
		res = usr.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code ("+KatelloUser.CMD_CREATE+")");
		Assert.assertTrue(getOutput(res).contains(
				String.format(KatelloUser.OUT_CREATE,username)), 
				"Check - returned output string ("+KatelloUser.CMD_CREATE+")");
		
		usr.asserts_create();
	}
	
	// TODO - with dataProvider provide more variations of user names in create action.
	
	@Test(description="create user - for default org (disabled)", enabled=true)
	public void test_createDisabled_DefaultOrg(){
		SSHCommandResult res;
		String uniqueID = KatelloTestScript.getUniqueID();
		String username = "disabled-"+uniqueID;
		String userpass = "password";
		String usermail = username+"@localhost";
		
		KatelloUser usr = new KatelloUser(username, usermail, userpass, true);
		res = usr.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code ("+KatelloUser.CMD_CREATE+")");
		Assert.assertTrue(getOutput(res).contains(
				String.format(KatelloUser.OUT_CREATE,username)), 
				"Check - returned output string ("+KatelloUser.CMD_CREATE+")");
		
		usr.asserts_create();
	}
	
}
