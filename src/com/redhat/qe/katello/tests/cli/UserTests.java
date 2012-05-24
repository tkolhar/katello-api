package com.redhat.qe.katello.tests.cli;

import org.testng.annotations.Test;

import com.redhat.qe.auto.testng.Assert;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.katello.base.obj.KatelloUser;
import com.redhat.qe.tools.SSHCommandResult;
import com.redhat.qe.katello.base.obj.KatelloUserRole;

@Test(groups={"cfse-cli","headpin-cli"})
public class UserTests extends KatelloCliTestScript{
/*
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
	
	
	@Test(description="delete users - for some org provided", enabled=true)
	public void test_DeleteUserOrg(){
		SSHCommandResult res;
		String uniqueID = KatelloTestScript.getUniqueID();
		String username = "user-"+uniqueID;
		String userpass = "password";
		String usermail = username+"@localhost";
        String orgname  = "ACME_Corporation";
        String envname  = "DEV";
		KatelloUser usr = new KatelloUser(username, usermail, userpass, false, orgname,envname);
		res = usr.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code ("+KatelloUser.CMD_CREATE+")");
		Assert.assertTrue(getOutput(res).contains(
				String.format(KatelloUser.OUT_CREATE,username)), 
				"Check - returned output string ("+KatelloUser.CMD_CREATE+")");
		
		usr.asserts_create();
		res = usr.delete_user(username);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code ("+KatelloUser.CMD_DELETE_USER+")");
		Assert.assertTrue(getOutput(res).contains(String.format(KatelloUser.OUT_DELETE, username)),
				                                  "Checked - returned output string ("+KatelloUser.CMD_DELETE_USER+")");
		usr.asserts_delete();
	
	}
	

	@Test(description="delete users - for default org ", enabled=true)
	public void test_DeleteUserDefaultOrg(){
		SSHCommandResult res;
		String uniqueID = KatelloTestScript.getUniqueID();
		String username = "user-"+uniqueID;
		String userpass = "password";
		String usermail = username+"@localhost";
		
		KatelloUser usr = new KatelloUser(username, usermail, userpass, false);
		res = usr.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code ("+KatelloUser.CMD_CREATE+")");
		Assert.assertTrue(getOutput(res).contains(
				String.format(KatelloUser.OUT_CREATE,username)), 
				"Check - returned output string ("+KatelloUser.CMD_CREATE+")");
		
		usr.asserts_create();
		res = usr.delete_user(username);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code ("+KatelloUser.CMD_DELETE_USER+")");
		Assert.assertTrue(getOutput(res).contains(String.format(KatelloUser.OUT_DELETE, username)),
				                                  "Checked - returned output string ("+KatelloUser.CMD_DELETE_USER+")");
		usr.asserts_delete();
	
	}
	*/
	
	/*
	@Test(description="Generates User Report - pdf format", enabled=true)
	public void test_UserReport_pdf(){
		SSHCommandResult res;
		String format = "pdf";
		res = KatelloUser.report(format);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code ("+KatelloUser.CMD_REPORT+")");
		
	
	}
	
	@Test(description="Generates User Report - html format", enabled=true)
	public void test_UserReport_html(){
		SSHCommandResult res;
		String format = "html";
		res = KatelloUser.report(format);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code ("+KatelloUser.CMD_REPORT+")");
		
	
	}
	
	@Test(description="Generates User Report - default format", enabled=true)
	public void test_UserReport(){
		SSHCommandResult res;
		String format = "";
		res = KatelloUser.report(format);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code ("+KatelloUser.CMD_REPORT+")");
		
	
	}
	
	
	@Test(description="Generates User Report - csv format", enabled=true)
	public void test_UserReport_csv(){
		SSHCommandResult res;
		String format = "csv";
		res = KatelloUser.report(format);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code ("+KatelloUser.CMD_REPORT+")");
		
	
	} */
	
	@Test(description="assign roles to users", enabled=true)
	public void test_AssignUserRoles(){
		
		SSHCommandResult res;
		String uniqueID = KatelloTestScript.getUniqueID();
		String username = "user-"+uniqueID;
		String userpass = "password";
		String usermail = username+"@localhost";
		String unique_role_ID = KatelloTestScript.getUniqueID();
		String user_role_name = "user-role"+unique_role_ID;
		String role_desc = "Assigned " + user_role_name + " to user " + username; 
		KatelloUser usr = new KatelloUser(username, usermail, userpass, false);
		res = usr.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code ("+KatelloUser.CMD_CREATE+")");
		Assert.assertTrue(getOutput(res).contains(
				String.format(KatelloUser.OUT_CREATE,username)), 
				"Check - returned output string ("+KatelloUser.CMD_CREATE+")");
		
		usr.asserts_create();
		KatelloUserRole usr_role = new KatelloUserRole(user_role_name,role_desc);
        res = usr_role.create();
        Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code ("+KatelloUserRole.CMD_CREATE+")");
        Assert.assertTrue(getOutput(res).contains(String.format(KatelloUserRole.OUT_CREATE, user_role_name)),
        		                                 "Check - returned output string ("+KatelloUserRole.CMD_CREATE+")");

        res = usr.assign_role(usr_role.name);
        Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code ("+KatelloUser.CMD_ASSIGN_ROLE+")");
        Assert.assertTrue(getOutput(res).contains(String.format(KatelloUser.OUT_ASSIGN_ROLE, username,user_role_name)), 
        		                                  "Check - returned output string ("+KatelloUser.CMD_ASSIGN_ROLE+")");
	
	}
	
	
}
