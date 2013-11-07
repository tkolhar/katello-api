package com.redhat.qe.katello.tests.hammer.cli;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.HammerUser;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

public class UserTests extends KatelloCliTestBase {
	private SSHCommandResult exec_result;
	private String login;
	private String password;
	private String mail;
	private String id;
	private String newId;
	private String newLogin;
	
	@BeforeClass(description="Prepare data to work with")
	public void setup(){
		String uid = KatelloUtils.getUniqueID();
		this.login = "testUser-"+uid;
		this.password = "testUser-"+uid;
		this.mail = login+"@redhat.com";
	}
	
	@Test(description="Create User")
	public void test_userCreate() {
		String uid = KatelloUtils.getUniqueID();
		
		HammerUser usr = new HammerUser(cli_worker, login, password, mail);
		exec_result = usr.create("1");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(HammerUser.HAMMER_OUT_CREATE), "Check - returned output string");
		//Create an admin user
		HammerUser adminUsr = new HammerUser(cli_worker, "admin-"+uid, "admin-"+uid, "admin-"+uid+"@redhat.com", "admin", "user", true);
		exec_result = adminUsr.create("1");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(HammerUser.HAMMER_OUT_CREATE), "Check - returned output string");
	}
	
	@Test(description="Create duplicate login. Verify error message", dependsOnMethods={"test_userCreate"})
	public void test_userDuplicateCreate() {
		HammerUser usr = new HammerUser(cli_worker, login, password, mail, "test", "user", false);
		String uid = KatelloUtils.getUniqueID();
		//duplicate login
		exec_result = usr.create("1");
		Assert.assertTrue(exec_result.getExitCode() == 65, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(HammerUser.HAMMER_ERR_CREATE), "Check - returned output string");
		//multiple login for same user
		newLogin = "newLogin-"+uid;
		usr.login = newLogin;
		exec_result = usr.create("1");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(HammerUser.HAMMER_OUT_CREATE), "Check - returned output string");
	}
	
	@Test(description="Verify user info", dependsOnMethods={"test_userDuplicateCreate"} )
	public void test_userInfo() {
		HammerUser usr = new HammerUser(cli_worker, login, password, mail);
		exec_result = usr.list(login, null, null);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		id = KatelloUtils.grepCLIOutput("Id",KatelloUtils.grepOutBlock("Login", login, getOutput(exec_result)));
		usr.setId(new Long(id));
		assert_UserInfo(usr);
		//verify multiple login info exists
		usr.login = newLogin;
		usr.firstName = "test";
		usr.lastName = "user";
		exec_result = usr.list(newLogin, null, null);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		newId = KatelloUtils.grepCLIOutput("Id",KatelloUtils.grepOutBlock("Login", newLogin, getOutput(exec_result)));
		usr.setId(new Long(newId));
		assert_UserInfo(usr);
	}
	
	@Test(description="update previously created user", dependsOnMethods={"test_userInfo"})
	public void test_userUpdate() {
		HammerUser usr = new HammerUser(cli_worker, login, password, mail);
		String uid = KatelloUtils.getUniqueID();
		usr.login = "new-"+login;
		usr.password = "new-"+password;
		usr.firstName = "newFName-"+uid;
		usr.lastName = "newLName-"+uid;
		usr.mail = "new-"+mail;
		exec_result = usr.list(login, null, null);
		
		usr.setId(new Long(KatelloUtils.grepCLIOutput("Id",KatelloUtils.grepOutBlock("Login", login, getOutput(exec_result)))));
		exec_result = usr.update();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(HammerUser.HAMMER_OUT_UPDATE), "Check - returned output string");
		assert_UserInfo(usr);
	}
	//TODO: user list using search, order, page
	
	@Test(description="delete users", dependsOnMethods={"test_userUpdate"})
	public void test_userDelete() {
		HammerUser usr = new HammerUser(cli_worker, "new-"+login, "new-"+password, "new-"+mail);
		exec_result = usr.delete(id);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(HammerUser.HAMMER_OUT_DELETE), "Check - returned output string");
		exec_result = usr.delete(newId);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(HammerUser.HAMMER_OUT_DELETE), "Check - returned output string");
	}

	private void assert_UserInfo(HammerUser user) {
		if (user.firstName == null) user.firstName = "";
		if (user.lastName == null) user.lastName = "";

		exec_result = user.info();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");

		String match_info = String.format(HammerUser.REG_USER_INFO, user.getId(), user.login, user.firstName+" "+user.lastName, user.mail).replaceAll("\"", "");
		log.finest(String.format("User (info) match regex: [%s]", match_info));
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", " ").matches(match_info), "User info should match the provided info");
	}
	
	private String getID(String property, String value, String output) {
		String _return = null;
		String[] lines = output.split("\\n");
		int propertyCol = 0;
		int idCol = 0;
		
		String[] headerCols = lines[1].split("\\|");
		for(String column:headerCols ) {
			if(column.trim().equals(property)) break;
			propertyCol++;
		}
		for(String column:headerCols ) {
			if(column.trim().equals("ID")) break;
			idCol++;
		}
		
		for(String line:lines ){
			if(line.startsWith("---") || line.trim().equals("")) continue; // skip it.
			String[] lineCols = line.split("\\|");
			if(lineCols[propertyCol].trim().equals(value)){
				_return = lineCols[idCol].trim();
				break;
			}
		}
		return _return;
	}
}
