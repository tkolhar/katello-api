package com.redhat.qe.katello.tests.installation;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloPing;
import com.redhat.qe.katello.base.obj.KatelloUser;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups = { "cfse-cli", "headpin-cli" })
public class UserTests extends KatelloCliTestBase {
	
	private String username;
	
	@BeforeClass(description="init: create org stuff")
	public void setUp(){
		this.username = "admin-user2";
	}
	
	@Test(description="Create user with correct username, verify that it is created, login by that user")
	public void test_createUser() {
		
		KatelloUser user = new KatelloUser();
		user.username = username;
		
		SSHCommandResult res = user.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code ("+KatelloUser.CMD_CREATE+")");
		Assert.assertTrue(getOutput(res).contains(
				String.format(KatelloUser.OUT_CREATE,username)), 
				"Check - returned output string ("+KatelloUser.CMD_CREATE+")");

		user.password = "redhat";
		
		KatelloPing ping = new KatelloPing(cli_worker);
		ping.runAs(user);
		res = ping.cli_ping();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check services up");
	}

	@Test(description="Create user with invalid username, verify that it is not created, login by that user and verify that error is shown")
	public void test_createUserInvalid() {
		
		KatelloUser user = new KatelloUser();
		user.username = "invalid & username @ ldap";
		
		SSHCommandResult res = user.cli_create();
		Assert.assertFalse(res.getExitCode().intValue()==0, "Check - error code ("+KatelloUser.CMD_CREATE+")");
		Assert.assertTrue(getOutput(res).contains("Validation failed: Username does not exist in your current LDAP system. Please choose a different user, or contact your LDAP administrator if you think this message is in error."), "Check - returned error string ("+KatelloUser.CMD_CREATE+")");

		user.password = "redhat";
		
		KatelloPing ping = new KatelloPing(cli_worker);
		ping.runAs(user);
		res = ping.cli_ping();
		Assert.assertFalse(res.getExitCode().intValue()==0, 
				"Check - return code (invalid credentials)");
		Assert.assertTrue(getOutput(res).equals(KatelloUser.ERR_INVALID_CREDENTIALS), 
				"Check - error string (invalid credentials)");
	}
	
	@Test(description="Create user with wrong parameters, verify that it is not created, login by that user and verify that error is shown")
	public void test_createUserWrongParams() {
		
		KatelloUser user = new KatelloUser();
		user.username = "testusername";
		user.email = "test@example.com";
		user.password = "redhat";
		
		SSHCommandResult res = user.cli_create();
		Assert.assertFalse(res.getExitCode().intValue()==0, "Check - error code ("+KatelloUser.CMD_CREATE+")");
		
		KatelloPing ping = new KatelloPing(cli_worker);
		ping.runAs(user);
		res = ping.cli_ping();
		Assert.assertFalse(res.getExitCode().intValue()==0, 
				"Check - return code (invalid credentials)");
		Assert.assertTrue(getOutput(res).equals(KatelloUser.ERR_INVALID_CREDENTIALS), 
				"Check - error string (invalid credentials)");
	}
}
