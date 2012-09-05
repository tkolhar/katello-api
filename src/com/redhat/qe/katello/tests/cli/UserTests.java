package com.redhat.qe.katello.tests.cli;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloUser;
import com.redhat.qe.katello.base.obj.KatelloUserRole;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups={"cfse-cli","headpin-cli"})
public class UserTests extends KatelloCliTestScript{
	
	List<KatelloUser> users;
	private String organization;
	private String organization2;
	private String env;
	private String env2;
	
	@BeforeClass(description="init: create org stuff")
	public void setUp(){
		SSHCommandResult res;
		String uid = KatelloTestScript.getUniqueID();
		this.organization = "ak-"+uid;
		this.env = "ak-"+uid;
		this.organization2 = "ak2-"+uid;
		this.env2 = "ak2-"+uid;
		KatelloOrg org = new KatelloOrg(this.organization, null);
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		KatelloEnvironment env = new KatelloEnvironment(this.env, null, this.organization, KatelloEnvironment.LIBRARY);
		res = env.cli_create();
		org = new KatelloOrg(this.organization2, null);
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		env = new KatelloEnvironment(this.env2, null, this.organization2, KatelloEnvironment.LIBRARY);
		res = env.cli_create();
	}
		

	@Test(description="create user - for default org", enabled=true)
	public void test_create_DefaultOrg(){
		SSHCommandResult res;
		String uniqueID = KatelloTestScript.getUniqueID();
		String username = "usr-"+uniqueID;
		String userpass = "password";
		String usermail = username+"@localhost";
		
		KatelloUser usr = new KatelloUser(username, usermail, userpass, false);
		res = usr.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code ("+KatelloUser.CMD_CREATE+")");
		Assert.assertTrue(getOutput(res).contains(
				String.format(KatelloUser.OUT_CREATE,username)), 
				"Check - returned output string ("+KatelloUser.CMD_CREATE+")");
		
		usr.asserts_create();
		if(this.users == null){
			this.users = Collections.synchronizedList(new ArrayList<KatelloUser>());
		}
		this.users.add(usr);
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
		res = usr.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code ("+KatelloUser.CMD_CREATE+")");
		Assert.assertTrue(getOutput(res).contains(
				String.format(KatelloUser.OUT_CREATE,username)), 
				"Check - returned output string ("+KatelloUser.CMD_CREATE+")");
		
		usr.asserts_create();
	}
	

	@Test(description = "List all users - admin should be there")
	public void test_listUsers_admin(){
		KatelloUser list_user = new KatelloUser(null,null, null, false);
		SSHCommandResult res = list_user.cli_list();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(res).contains(KatelloUser.DEFAULT_ADMIN_USER), "Check - contains: ["+KatelloUser.DEFAULT_ADMIN_USER+"]");
	}
	
	@Test(description = "List users - created", 
			dependsOnMethods={"test_create_DefaultOrg"})
	public void test_infoListUser(){
		KatelloUser list_user = createUser();
		users.add(list_user);
		
		SSHCommandResult res = list_user.cli_list();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code (user list)");
		
		for(KatelloUser user : this.users){
			String match_list = String.format(KatelloUser.REG_USER_LIST, user.username, user.email).replaceAll("\"", ""); // output not have '"' signs
			Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(match_list), "Check - user matches ["+user.username+"]");
			assert_userInfo(user); // Assertions - `user info --username %s` 
		}
	}
	
	@Test(description = "delete users - for some org provided", enabled = true)
	public void test_DeleteUserOrg() {
		SSHCommandResult res;
		String uniqueID = KatelloTestScript.getUniqueID();
		String username = "user-" + uniqueID;
		String userpass = "password";
		String usermail = username + "@localhost";
		KatelloUser usr = new KatelloUser(username, usermail, userpass, false,
				this.organization, this.env);
		res = usr.cli_create();
		Assert.assertTrue(res.getExitCode().intValue() == 0,
				"Check - return code (" + KatelloUser.CMD_CREATE + ")");
		Assert.assertTrue(
				getOutput(res).contains(
						String.format(KatelloUser.OUT_CREATE, username)),
				"Check - returned output string (" + KatelloUser.CMD_CREATE
						+ ")");

		usr.asserts_create();
		res = usr.delete();
		Assert.assertTrue(res.getExitCode().intValue() == 0,
				"Check - return code (" + KatelloUser.CMD_DELETE_USER + ")");
		Assert.assertTrue(
				getOutput(res).contains(
						String.format(KatelloUser.OUT_DELETE, username)),
				"Checked - returned output string ("
						+ KatelloUser.CMD_DELETE_USER + ")");
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
		res = usr.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code ("+KatelloUser.CMD_CREATE+")");
		Assert.assertTrue(getOutput(res).contains(
				String.format(KatelloUser.OUT_CREATE,username)), 
				"Check - returned output string ("+KatelloUser.CMD_CREATE+")");
		
		usr.asserts_create();
		res = usr.delete();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code ("+KatelloUser.CMD_DELETE_USER+")");
		Assert.assertTrue(getOutput(res).contains(String.format(KatelloUser.OUT_DELETE, username)),
				                                  "Checked - returned output string ("+KatelloUser.CMD_DELETE_USER+")");
		usr.asserts_delete();
	
	}
	
	
	
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
		
	
	} 
	
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
		res = usr.cli_create();
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
	
	
	@Test(description = "Create User and Role, assign role to user")
	public void test_assignRole(){
		
		KatelloUser user = createUser();

		KatelloUserRole role = createRole();
		
		SSHCommandResult res = user.assign_role(role.name);
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code (user assign_role)");
		Assert.assertEquals(getOutput(res).trim(), 
				String.format(KatelloUser.OUT_ASSIGN_ROLE, user.username, role.name));
	}
	
	@Test(description = "Create User and 2 Roles, assign roles to user and then unassign one of them. Verify that only one role is unassigned")
	public void test_unassignRole(){
		
		KatelloUser user = createUser();

		KatelloUserRole role = createRole();
		user.assign_role(role.name);
		
		KatelloUserRole role2 = createRole();
		user.assign_role(role2.name);
		
		SSHCommandResult res = user.unassign_role(role.name);
		
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code (user unassign_role)");
		Assert.assertEquals(getOutput(res).trim(), 
				String.format(KatelloUser.OUT_UNASSIGN_ROLE, user.username, role.name));
		
		res = user.list_roles();
		String out = getOutput(res).replaceAll("\n", "");
		
		String match_list = String.format(KatelloUser.REG_USER_ROLE_LIST, role2.name).replaceAll("\"", ""); // output not have '"' signs
		Assert.assertTrue(out.matches(match_list), "Check - user role matches ["+role2.name+"]");
	}
	
	@Test(description = "Create User and Roles, assign roles to user, verify list_roles shows them")
	public void test_listRoles(){
		
		KatelloUser user = createUser();

		KatelloUserRole role = createRole();
		user.assign_role(role.name);
		
		KatelloUserRole role1 = createRole();
		user.assign_role(role1.name);
		
		KatelloUserRole role2 = createRole();
		user.assign_role(role2.name);
		
		KatelloUserRole role3 = createRole();
		user.assign_role(role3.name);
		
		SSHCommandResult res = user.list_roles();
		
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code (user list_roles)");
		String out = getOutput(res).replaceAll("\n", "");
		
		String match_list = String.format(KatelloUser.REG_USER_ROLE_LIST, role.name).replaceAll("\"", ""); // output not have '"' signs
		Assert.assertTrue(out.matches(match_list), "Check - user role matches ["+role.name+"]");
		
		match_list = String.format(KatelloUser.REG_USER_ROLE_LIST, role.name).replaceAll("\"", ""); // output not have '"' signs
		Assert.assertTrue(out.matches(match_list), "Check - user role matches ["+role1.name+"]");
		
		match_list = String.format(KatelloUser.REG_USER_ROLE_LIST, role.name).replaceAll("\"", ""); // output not have '"' signs
		Assert.assertTrue(out.matches(match_list), "Check - user role matches ["+role2.name+"]");
		
		match_list = String.format(KatelloUser.REG_USER_ROLE_LIST, role.name).replaceAll("\"", ""); // output not have '"' signs
		Assert.assertTrue(out.matches(match_list), "Check - user role matches ["+role3.name+"]");
	}
	
	@Test(description="Delete a user", enabled=true)
	public void test_deleteUser(){
		KatelloUser user = createUser();
		
		SSHCommandResult res = user.delete();
		Assert.assertEquals(res.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertTrue(getOutput(res).contains(String.format(KatelloUser.OUT_DELETE,user.username)),"Check - return string");
		
		res = user.cli_info();
		Assert.assertTrue(res.getExitCode()==65, "Check - return code [65]");
		Assert.assertEquals(getOutput(res).trim(), 
				String.format(KatelloUser.OUT_FIND_USER_ERROR,user.username));
	}
	
	@Test(description="Create a user with default org and environment", enabled=true)
	public void test_createUserDefaultValues() {
		SSHCommandResult res;
		String uniqueID = KatelloTestScript.getUniqueID();
		String username = "user-" + uniqueID;
		String userpass = "password";
		String usermail = username + "@localhost";
		KatelloUser usr = new KatelloUser(username, usermail, userpass, false,
				this.organization, this.env);
		res = usr.cli_create();
		Assert.assertTrue(res.getExitCode().intValue() == 0,
				"Check - return code (" + KatelloUser.CMD_CREATE + ")");
		Assert.assertTrue(
				getOutput(res).contains(
						String.format(KatelloUser.OUT_CREATE, username)),
				"Check - returned output string (" + KatelloUser.CMD_CREATE
						+ ")");

		usr.asserts_create();
	}

	@Test(description="Create a user with default org and environment from other org, verify error", enabled=true)
	public void test_createUserDefaultValuesWrong() {
		SSHCommandResult res;
		String uniqueID = KatelloTestScript.getUniqueID();
		String username = "user-" + uniqueID;
		String userpass = "password";
		String usermail = username + "@localhost";
		KatelloUser usr = new KatelloUser(username, usermail, userpass, false,
				this.organization, this.env2);
		res = usr.cli_create();
		Assert.assertTrue(res.getExitCode().intValue() == 65,"Check - return code (environment delete)");
        Assert.assertTrue(getOutput(res).contains(
				String.format(KatelloEnvironment.ERROR_INFO,env2,this.organization)), 
				"Check - returned output string ("+KatelloUser.CMD_CREATE+")");	
	}
	
	private void assert_userInfo(KatelloUser user){
		SSHCommandResult res;
		res = user.cli_info();
		String match_info = String.format(KatelloUser.REG_USER_LIST,user.username,user.email).replaceAll("\"", "");
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		log.finest(String.format("User (info) match regex: [%s]",match_info));
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(match_info), 
				String.format("User [%s] should be found in the result info",user.username));		
	}
	
	private KatelloUser createUser() {
		String uniqueID = KatelloTestScript.getUniqueID();
		String username = "usr-"+uniqueID;
		String userpass = "password";
		String usermail = username+"@localhost";
		
		KatelloUser user = new KatelloUser(username, usermail, userpass, false);
		user.cli_create();
		
		return user;
	}
	
	private KatelloUserRole createRole() {
		String uniqueID = KatelloTestScript.getUniqueID();
		String rolename = "role-"+uniqueID;
		String descr = "role-desc";
		KatelloUserRole role = new KatelloUserRole(rolename, descr);
		role.create();
		
		return role;
	}
	

}
