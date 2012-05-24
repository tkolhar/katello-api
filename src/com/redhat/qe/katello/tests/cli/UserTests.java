package com.redhat.qe.katello.tests.cli;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;

import com.redhat.qe.auto.testng.Assert;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.katello.base.obj.KatelloUser;
import com.redhat.qe.katello.base.obj.KatelloUserRole;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups={"cfse-cli","headpin-cli"})
public class UserTests extends KatelloCliTestScript{
	
	List<KatelloUser> users;

	@Test(description="create user - for default org")
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
		if(this.users == null){
			this.users = Collections.synchronizedList(new ArrayList<KatelloUser>());
		}
		this.users.add(usr);
	}
	
	// TODO - with dataProvider provide more variations of user names in create action.
	
	@Test(description="create user - for default org (disabled)")
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
	
	@Test(description = "List all users - admin should be there")
	public void test_listUsers_admin(){
		KatelloUser list_user = new KatelloUser(null,null, null, false);
		SSHCommandResult res = list_user.list();
		Assert.assertEquals(res.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertTrue(getOutput(res).contains(KatelloUser.DEFAULT_ADMIN_USER), "Check - contains: ["+KatelloUser.DEFAULT_ADMIN_USER+"]");
	}
	
	@Test(description = "List users - created", 
			dependsOnMethods={"test_create_DefaultOrg"})
	public void test_infoListUser(){
		KatelloUser list_user = createUser();
		users.add(list_user);
		
		SSHCommandResult res = list_user.list();
		Assert.assertEquals(res.getExitCode().intValue(), 0, "Check - return code (user list)");
		
		for(KatelloUser user : this.users){
			String match_list = String.format(KatelloUser.REG_USER_LIST, user.username, user.email).replaceAll("\"", ""); // output not have '"' signs
			Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(match_list), "Check - user matches ["+user.username+"]");
			assert_userInfo(user); // Assertions - `user info --username %s` 
		}
	}
	
	@Test(description = "Create User and Role, assign role to user")
	public void test_assignRole(){
		
		KatelloUser user = createUser();

		KatelloUserRole role = createRole();
		
		SSHCommandResult res = user.assign_role(role.name);
		Assert.assertEquals(res.getExitCode().intValue(), 0, "Check - return code (user assign_role)");
		Assert.assertEquals(getOutput(res).trim(), 
				String.format("User '%s' assigned to role '%s'", user.username, role.name));
	}
	
	@Test(description = "Create User and 2 Roles, assign roles to user and then unassign one of them. Verify that only one role is unassigned")
	public void test_unassignRole(){
		
		KatelloUser user = createUser();

		KatelloUserRole role = createRole();
		user.assign_role(role.name);
		
		KatelloUserRole role2 = createRole();
		user.assign_role(role2.name);
		
		SSHCommandResult res = user.unassign_role(role.name);
		
		Assert.assertEquals(res.getExitCode().intValue(), 0, "Check - return code (user unassign_role)");
		Assert.assertEquals(getOutput(res).trim(), 
				String.format("User '%s' unassigned from role '%s'", user.username, role.name));
		
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
		
		Assert.assertEquals(res.getExitCode().intValue(), 0, "Check - return code (user list_roles)");
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
	
	@Test(description="Delete a user", enabled=false)
	public void test_deleteUser(){
		KatelloUser user = createUser();
		
		SSHCommandResult res = user.delete();
		Assert.assertEquals(res.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertTrue(getOutput(res).contains(String.format("Successfully deleted user [ %s ]",user.username)),"Check - return string");
		
		res = user.info();
		Assert.assertEquals(res.getExitCode(), new Integer(65),"Check - return code [65]");
		Assert.assertEquals(getOutput(res).trim(), 
				String.format("Could not find user [ %s ]",user.username));
	}
	
	private void assert_userInfo(KatelloUser user){
		SSHCommandResult res;
		res = user.info();
		String match_info = String.format(KatelloUser.REG_USER_LIST,user.username,user.email).replaceAll("\"", "");
		Assert.assertEquals(res.getExitCode().intValue(), 0, "Check - return code");
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
		user.create();
		
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
