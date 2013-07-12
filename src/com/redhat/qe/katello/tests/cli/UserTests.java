package com.redhat.qe.katello.tests.cli;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.redhat.qe.katello.base.KatelloCliDataProvider;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloUser;
import com.redhat.qe.katello.base.obj.KatelloUserRole;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.common.TngRunGroups;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups={"cfse-cli",TngRunGroups.TNG_KATELLO_Users_Roles})
public class UserTests extends KatelloCliTestBase{
	
	List<KatelloUser> users;
	private String uid = KatelloUtils.getUniqueID();
	private String organization;
	private String organization2;
	private String env;
	private String env2;
	
	@BeforeClass(description="init: create org stuff", groups={"cfse-cli","headpin-cli"})
	public void setUp(){
		SSHCommandResult res;
		this.organization = "org-"+uid;
		this.env = "Library"; // initially - for headpin
		this.organization2 = "org2-"+uid;
		this.env2 = "env2-"+uid;
		KatelloOrg org = new KatelloOrg(this.cli_worker, this.organization, null);
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		KatelloEnvironment env = new KatelloEnvironment(this.cli_worker, this.env, null, this.organization, KatelloEnvironment.LIBRARY);
		res = env.cli_create();
		org = new KatelloOrg(this.cli_worker, this.organization2, null);
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		env = new KatelloEnvironment(this.cli_worker, this.env2, null, this.organization2, KatelloEnvironment.LIBRARY);
		res = env.cli_create();
	}

	@BeforeClass(description="init: katello specific, no headpin", dependsOnMethods={"setUp"}, groups={"cfse-cli"})
	public void setUp_katelloOnly(){
		this.env = "ak-"+uid;
		SSHCommandResult exec_result = new KatelloEnvironment(this.cli_worker, this.env, null, organization, KatelloEnvironment.LIBRARY).cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
	}

	@Test(description="create user - for default org", groups={"headpin-cli"})
	public void test_create_DefaultOrg(){
		SSHCommandResult res;
		String uniqueID = KatelloUtils.getUniqueID();
		String username = "usr-"+uniqueID;
		String userpass = "password";
		String usermail = username+"@localhost";
		
		KatelloUser usr = new KatelloUser(cli_worker, username, usermail, userpass, false);
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

	@Test(description="create user - for default org (disabled)", groups={"headpin-cli"})
	public void test_createDisabled_DefaultOrg(){
		SSHCommandResult res;
		String uniqueID = KatelloUtils.getUniqueID();
		String username = "disabled-"+uniqueID;
		String userpass = "password";
		String usermail = username+"@localhost";
		
		KatelloUser usr = new KatelloUser(cli_worker, username, usermail, userpass, true);
		res = usr.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code ("+KatelloUser.CMD_CREATE+")");
		Assert.assertTrue(getOutput(res).contains(
				String.format(KatelloUser.OUT_CREATE,username)), 
				"Check - returned output string ("+KatelloUser.CMD_CREATE+")");
		
		usr.asserts_create();
	}
	
	@Test(description="update user info - valid username", groups={"headpin-cli"})
	public void test_updateUserInfo(){
		SSHCommandResult res;
		String uniqueID = KatelloUtils.getUniqueID();
		String username = "user-"+uniqueID;
		String userpass = "password";
		String usermail = username+"@localhost";
		
		KatelloUser usr = new KatelloUser(cli_worker, username, usermail, userpass, true);
		res = usr.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code ("+KatelloUser.CMD_CREATE+")");
		Assert.assertTrue(getOutput(res).contains(
				String.format(KatelloUser.OUT_CREATE,username)), 
				"Check - returned output string ("+KatelloUser.CMD_CREATE+")");
		usr.asserts_create();
		
		res = usr.update_defaultOrgEnv(this.organization, this.env);
		//Assert successfull update
		//user info
		// assert all info correct
	}

	@Test(description = "List all users - admin should be there", groups={"headpin-cli"})
	public void test_listUsers_admin(){
		KatelloUser list_user = new KatelloUser(cli_worker, null,null, null, false);
		SSHCommandResult res = list_user.cli_list();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(res).contains(KatelloUser.DEFAULT_ADMIN_USER), "Check - contains: ["+KatelloUser.DEFAULT_ADMIN_USER+"]");
	}
	
	@Test(description = "List users - created", 
			dependsOnMethods={"test_create_DefaultOrg"}, groups={"headpin-cli"})
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
	
	@Test(description = "66c06eec-8698-453d-b956-f5aa6c7d0c8e", groups={"headpin-cli"})
	public void test_DeleteUserOrg() {
		SSHCommandResult res;
		String uniqueID = KatelloUtils.getUniqueID();
		String username = "user-" + uniqueID;
		String userpass = "password";
		String usermail = username + "@localhost";
		KatelloUser usr = new KatelloUser(cli_worker, username, usermail, userpass, false,
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

	@Test(description="delete users - for default org ", groups={"headpin-cli"})
	public void test_DeleteUserDefaultOrg(){
		SSHCommandResult res;
		String uniqueID = KatelloUtils.getUniqueID();
		String username = "user-"+uniqueID;
		String userpass = "password";
		String usermail = username+"@localhost";
		
		KatelloUser usr = new KatelloUser(cli_worker, username, usermail, userpass, false);
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
	
	
	@Test(description="Generates User Report - pdf format", groups={"headpin-cli"})
	public void test_UserReport_pdf(){
		sshOnClient("rm -f katello_users_report.pdf");
		SSHCommandResult res;
		String format = "pdf";
		KatelloUser usr = new KatelloUser();
		res = usr.report(format);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code ("+KatelloUser.CMD_REPORT+")");
		
		res = sshOnClient("ls -la | grep katello_users_report.pdf");
		Assert.assertTrue(getOutput(res).contains("katello_users_report.pdf"));
	}
	
	@Test(description="Generates User Report - html format", groups={"headpin-cli"})
	public void test_UserReport_html(){
		SSHCommandResult res;
		String format = "html";
		res = new KatelloUser().report(format);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code ("+KatelloUser.CMD_REPORT+")");
	}
	
	@Test(description="Generates User Report - default format", groups={"headpin-cli"})
	public void test_UserReport(){
		SSHCommandResult res;
		String format = "";
		res = new KatelloUser().report(format);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code ("+KatelloUser.CMD_REPORT+")");
	}
	
	@Test(description="Generates User Report - csv format", groups={"headpin-cli"})
	public void test_UserReport_csv(){
		SSHCommandResult res;
		String format = "csv";
		res = new KatelloUser().report(format);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code ("+KatelloUser.CMD_REPORT+")");
	} 
	
	@Test(description="87b757c2-6782-4d9c-8d64-ce5f5ce872ac", groups={"headpin-cli"})
	public void test_AssignUserRoles(){
		
		SSHCommandResult res;
		String uniqueID = KatelloUtils.getUniqueID();
		String username = "user-"+uniqueID;
		String userpass = "password";
		String usermail = username+"@localhost";
		String unique_role_ID = KatelloUtils.getUniqueID();
		String user_role_name = "user-role"+unique_role_ID;
		String role_desc = "Assigned " + user_role_name + " to user " + username; 
		KatelloUser usr = new KatelloUser(cli_worker, username, usermail, userpass, false);
		res = usr.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code ("+KatelloUser.CMD_CREATE+")");
		Assert.assertTrue(getOutput(res).contains(
				String.format(KatelloUser.OUT_CREATE,username)), 
				"Check - returned output string ("+KatelloUser.CMD_CREATE+")");
		
		usr.asserts_create();
		KatelloUserRole usr_role = new KatelloUserRole(cli_worker, user_role_name,role_desc);
        res = usr_role.create();
        Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code ("+KatelloUserRole.CMD_CREATE+")");
        Assert.assertTrue(getOutput(res).contains(String.format(KatelloUserRole.OUT_CREATE, user_role_name)),
        		"Check - returned output string ("+KatelloUserRole.CMD_CREATE+")");

        res = usr.assign_role(usr_role.name);
        Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code ("+KatelloUser.CMD_ASSIGN_ROLE+")");
        Assert.assertTrue(getOutput(res).contains(String.format(KatelloUser.OUT_ASSIGN_ROLE, username,user_role_name)), 
        		"Check - returned output string ("+KatelloUser.CMD_ASSIGN_ROLE+")");
	}
	
	
	@Test(description = "Create User and Role, assign role to user", groups={"headpin-cli"})
	public void test_assignRole(){
		KatelloUser user = createUser();
		KatelloUserRole role = createRole();
		
		SSHCommandResult res = user.assign_role(role.name);
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code (user assign_role)");
		Assert.assertEquals(getOutput(res).trim(), 
				String.format(KatelloUser.OUT_ASSIGN_ROLE, user.username, role.name));
	}
	
	@Test(description = "fbeb8d03-e33f-46b1-82e0-fb7540e4b49c", groups={"headpin-cli"})
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
	
	@Test(description = "Create User and Roles, assign roles to user, verify list_roles shows them", groups={"headpin-cli"})
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
	
	@Test(description="Delete a user", groups={"headpin-cli"})
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
	
	@Test(description="Create a user with default org and environment", groups={"headpin-cli"})
	public void test_createUserDefaultValues() {
		SSHCommandResult res;
		String uniqueID = KatelloUtils.getUniqueID();
		String username = "user-" + uniqueID;
		String userpass = "password";
		String usermail = username + "@localhost";
		KatelloUser usr = new KatelloUser(cli_worker, username, usermail, userpass, false,
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

	@Test(description="Create a user with default org and environment from other org, verify error", groups={"headpin-cli"})
	public void test_createUserDefaultValuesWrong() {
		SSHCommandResult res;
		String uniqueID = KatelloUtils.getUniqueID();
		String username = "user-" + uniqueID;
		String userpass = "password";
		String usermail = username + "@localhost";
		KatelloUser usr = new KatelloUser(cli_worker, username, usermail, userpass, false,
				this.organization, this.env2);
		res = usr.cli_create();
		Assert.assertTrue(res.getExitCode().intValue() == 65,"Check - return code (environment delete)");
        Assert.assertTrue(getOutput(res).contains(
				String.format(KatelloEnvironment.ERROR_INFO,env2,this.organization)), 
				"Check - returned output string ("+KatelloUser.CMD_CREATE+")");	
	}
	
	
	@Test(description="access to cli calls by providing an empty password", groups={"headpin-cli"}, enabled=false) // TODO - try to find out why it fails on group running - TODO for gkhachik
	public void test_getAccessWithEmptyPassword(){
		KatelloUser userAdmin = new KatelloUser(cli_worker, System.getProperty("katello.admin.user"), 
				null,"", false);
		KatelloOrg org = new KatelloOrg(this.cli_worker, organization, null);
		org.runAs(userAdmin);
		SSHCommandResult res = org.cli_list();
		Assert.assertTrue(res.getExitCode().intValue()==145, 
				"Check - return code (invalid credentials)");
		Assert.assertTrue(getOutput(res).equals(KatelloUser.ERR_INVALID_CREDENTIALS), 
				"Check - error string (invalid credentials)");
	}
	
	@Test(description="fb2f0a12-b2e8-4654-a20f-d986080d5f05", groups={"headpin-cli"}, enabled=true) // TODO - try to find out why it fails on group running - TODO for gkhachik
	public void test_loginIncorrectUsername() {
		KatelloUser userAdmin = new KatelloUser(cli_worker, "wrong", 
				null, System.getProperty("katello.admin.password"), false);
		KatelloOrg org = new KatelloOrg(this.cli_worker, organization, null);
		org.runAs(userAdmin);
		SSHCommandResult res = org.cli_list();
		Assert.assertTrue(res.getExitCode().intValue()==145, 
				"Check - return code (invalid credentials)");
		Assert.assertTrue(getOutput(res).equals(KatelloUser.ERR_INVALID_CREDENTIALS), 
				"Check - error string (invalid credentials)");
	}

	@Test(description="78b4fdca-479d-4022-9b28-3eda1455bbff", groups={"headpin-cli"}, enabled=true) // TODO - try to find out why it fails on group running - TODO for gkhachik
	public void test_loginIncorrectPassword() {
		KatelloUser userAdmin = new KatelloUser(cli_worker, System.getProperty("katello.admin.user"), 
				null, "wrong", false);
		KatelloOrg org = new KatelloOrg(this.cli_worker, organization, null);
		org.runAs(userAdmin);
		SSHCommandResult res = org.cli_list();
		Assert.assertTrue(res.getExitCode().intValue()==145, 
				"Check - return code (invalid credentials)");
		Assert.assertTrue(getOutput(res).equals(KatelloUser.ERR_INVALID_CREDENTIALS), 
				"Check - error string (invalid credentials)");
	}

	@Test(description="8569772a-82fc-4412-8d5c-fcffafa6a5de", groups={"headpin-cli"})
	public void test_loginIncorrectCredentials() {
		KatelloUser userAdmin = new KatelloUser(cli_worker, "wrong", 
				null, "wrong", false);
		KatelloOrg org = new KatelloOrg(this.cli_worker, organization, null);
		org.runAs(userAdmin);
		SSHCommandResult res = org.cli_list();
		Assert.assertTrue(res.getExitCode().intValue()==145, 
				"Check - return code (invalid credentials)");
		Assert.assertTrue(getOutput(res).equals(KatelloUser.ERR_INVALID_CREDENTIALS), 
				"Check - error string (invalid credentials)");
	}
	
	@Test(description="Read-only user for an organization can only view information but cannot modify it", groups={"headpin-cli"})
	public void test_ReadonlyUser(){

		SSHCommandResult res;
	    String uniqueID = KatelloUtils.getUniqueID();
	    String readonly_user_name = "read_only-" + uniqueID;
	    String readonly_email = readonly_user_name + "@redhat.com";
	    String readonly_pass = "redhat";
	    String org_name = "readonly_org-"+ uniqueID;
	    KatelloOrg org = new KatelloOrg(this.cli_worker, org_name,null);
	    KatelloUser readonly_user = new KatelloUser(cli_worker, readonly_user_name,readonly_email,readonly_pass,false,this.organization,this.env);
	    res = readonly_user.cli_create();
	    Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code (" + KatelloUser.CMD_CREATE + ")");
	    res=readonly_user.assign_role(KatelloUserRole.ROLE_READ_EVERYTHING);
	    Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
	    org.runAs(readonly_user);
	    res = org.cli_create();
	    Assert.assertTrue(res.getExitCode().intValue() == 147, "Check - return code");
	}

	@Test(description = "create user - non-Latin variations of user names", groups={"headpin-cli"}, 
			dataProviderClass=KatelloCliDataProvider.class, dataProvider="user_create")
	public void test_createUser_variations(String username, String userpass, String usermail, Boolean pDisable){
		SSHCommandResult res;
		KatelloUser user = new KatelloUser(cli_worker, username, usermail, userpass, pDisable);
		res = user.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code ("+KatelloUser.CMD_CREATE+")");
		Assert.assertTrue(getOutput(res).contains(
				String.format(KatelloUser.OUT_CREATE,username)), 
				"Check - returned output string ("+KatelloUser.CMD_CREATE+")");

		user.asserts_create();
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
		String uniqueID = KatelloUtils.getUniqueID();
		String username = "usr-"+uniqueID;
		String userpass = "password";
		String usermail = username+"@localhost";
		
		KatelloUser user = new KatelloUser(cli_worker, username, usermail, userpass, false);
		user.cli_create();
		
		return user;
	}
	
	private KatelloUserRole createRole() {
		String uniqueID = KatelloUtils.getUniqueID();
		String rolename = "role-"+uniqueID;
		String descr = "role-desc";
		KatelloUserRole role = new KatelloUserRole(cli_worker, rolename, descr);
		role.create();
		
		return role;
	}
}