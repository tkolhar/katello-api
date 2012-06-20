package com.redhat.qe.katello.tests.api;

import java.io.IOException;
import java.util.logging.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.testng.annotations.Test;

import com.redhat.qe.auto.testng.Assert;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.katello.base.obj.KatelloUser;

@Test(groups={"cfse-api","headpin-api"})
public class UsersTest extends KatelloTestScript {
	protected static Logger log = Logger.getLogger(UsersTest.class.getName());

	private String username_disabled;
	private String username_enabled;
	
	private Long userid_disabled;

	@Test(groups = { "testUsers" }, description = "Create user (disabled)")
	public void test_createUserDisabled(){
		try{Thread.sleep(1000L);}catch(InterruptedException iex){} // to get new unique id.
		String pid = KatelloTestScript.getUniqueID();
		this.username_disabled = "user_"+pid;
		KatelloUser user = new KatelloUser(this.username_disabled, KatelloUser.DEFAULT_USER_EMAIL, KatelloUser.DEFAULT_USER_PASS, true);
		String s= user.api_create().getStdout();
		JSONObject juser = KatelloTestScript.toJSONObj(s);
		Assert.assertNotNull(juser.get("id"), "Check: not null returned: id");
		Boolean disabled = (Boolean)juser.get("disabled");
		Assert.assertTrue(disabled.booleanValue(), "Check: returned value: disabled=true");
		this.userid_disabled = (Long)juser.get("id");
		
		log.info("Preparing disabled user: ["+this.username_disabled+"]");
	}
	
	@Test(groups = { "testUsers" }, description = "Create user (enabled)")
	public void test_createUserEnabled(){
		try{Thread.sleep(1000L);}catch(InterruptedException iex){} // to get new unique id.
		String pid = KatelloTestScript.getUniqueID();
		this.username_enabled = "user_"+pid;
		KatelloUser user = new KatelloUser(username_enabled, KatelloUser.DEFAULT_USER_EMAIL, 
				KatelloUser.DEFAULT_USER_PASS, false);
		String s = getOutput(user.api_create());
		JSONObject juser = KatelloTestScript.toJSONObj(s);
		Assert.assertNotNull(juser.get("id"), "Check: not null returned: id");
		Boolean disabled = (Boolean)juser.get("disabled");
		Assert.assertFalse(disabled.booleanValue(), "Check: returned value: disabled=false");
		
		log.info("Preparing enabled user: ["+this.username_enabled+"]");
	}
	
	@Test(dependsOnMethods={"test_createUserDisabled","test_createUserEnabled"},
			groups = { "testUsers" }, description = "Get all users")
	public void test_getUsers(){
		String _ret = new KatelloUser(null,null,null,false).api_list().getStdout();
		Assert.assertTrue(_ret.contains("\"username\":\"admin\""), "Check: \"admin\" user exists");
		JSONArray users = KatelloTestScript.toJSONArr(_ret);
		JSONObject tmpUsr;
		boolean userFound_D=false, userFound_E=false;
		for(int i=0;i<users.size();i++){
			tmpUsr = (JSONObject)users.get(i);
			if(tmpUsr.get("username").equals(this.username_enabled)){
				userFound_E = true;
				Assert.assertFalse(((Boolean)tmpUsr.get("disabled")).booleanValue(), "Check: enabled user's disabled flag.");
			}
			if(tmpUsr.get("username").equals(this.username_disabled)){
				userFound_D = true;
				Assert.assertTrue(((Boolean)tmpUsr.get("disabled")).booleanValue(), "Check: disabled user's disabled flag.");
			}				
		}
		Assert.assertTrue((userFound_D && userFound_E), "Check: both users should be found in the list");
	}

	@Test(dependsOnMethods={"test_createUserDisabled","test_createUserEnabled"},
			groups = { "testUsers" }, description = "Get user")
	public void test_getUser(){
		String _ret =  new KatelloUser(null,null,null,false).api_info(this.userid_disabled.toString()).getStdout();
		Assert.assertTrue(_ret.contains("\"username\":\""+this.username_disabled+"\""), "Check: returned username");
		Assert.assertTrue(_ret.contains("\"disabled\":true"), "Check: returned username's disabled status");
	}

	@Test(groups = { "testUsers" }, description = "Update user properties")
	public void test_updateUser(){
		String pid = KatelloTestScript.getUniqueID();
		String updUser = "updUser_"+pid;
		KatelloUser user = new KatelloUser(updUser, KatelloUser.DEFAULT_USER_EMAIL, 
				KatelloUser.DEFAULT_USER_PASS, false);
		String s = getOutput(user.api_create());
		JSONObject juser = KatelloTestScript.toJSONObj(s);
		Long userId = (Long)juser.get("id");
		String pwdHash = (String)juser.get("password");
		try{
			servertasks.apiKatello_PUT("{'user':{'disabled':true}}", "/users/"+userId.toString());
			String _ret = new KatelloUser(null,null,null,false).api_info(userId.toString()).getStdout();
			Assert.assertTrue(_ret.contains("\"disabled\":true"), "Check: updated disable status");
			servertasks.apiKatello_PUT("{'user':{'password':'123456'}}", "/users/"+userId.toString());
			_ret = new KatelloUser(null,null,null,false).api_info(userId.toString()).getStdout();
			Assert.assertFalse(_ret.contains("\"password\":\""+pwdHash+"\""), "Check: updated password");
		}catch(IOException ie){
			log.severe(ie.getMessage());
		}
	}
	
	@Test(groups = { "testUsers" }, description = "Delete user")
	public void test_deleteUser(){
		String pid = KatelloTestScript.getUniqueID();
		String updUser = "delUser_"+pid;
		KatelloUser user = new KatelloUser(updUser, KatelloUser.DEFAULT_USER_EMAIL, 
				KatelloUser.DEFAULT_USER_PASS, false);
		String s = getOutput(user.api_create());
		JSONObject juser = KatelloTestScript.toJSONObj(s);
		Long userId = (Long)juser.get("id");
		try{
			String _ret = servertasks.apiKatello_DELETE("/users/"+userId.toString()).trim();
			Assert.assertEquals(_ret, String.format("Deleted user '%s'",userId.toString()),
					"Check: returned message of delete command");
			_ret = new KatelloUser(null,null,null,false).api_info(userId.toString()).getStdout();;
			Assert.assertTrue(_ret.contains(
					String.format("\"errors\":[\"Couldn't find User with ID=%s\"]", userId.toString())), 
					"Check: returned error message - getUsers()");
			_ret = new KatelloUser(null,null,null,false).api_info(userId.toString()).getStdout();;
			Assert.assertTrue(_ret.contains(
					String.format("\"errors\":[\"Couldn't find User with ID=%s\"]", userId.toString())), 
					"Check: returned error message - getUser(id)");
		}catch(IOException ie){
			log.severe(ie.getMessage());
		}
	}
	
}
