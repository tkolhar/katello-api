package com.redhat.qe.katello.tests.api;

import java.util.List;
import java.util.logging.Logger;

import org.testng.annotations.Test;

import com.google.inject.Inject;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloApiException;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.katello.base.obj.KatelloUser;
import com.redhat.qe.katello.common.KatelloUtils;

@Test(groups={"cfse-api"})
public class UsersTest extends KatelloTestScript {
    @Inject protected Logger log;

	private String username_disabled;
	private String username_enabled;
	
	private Long userid_disabled;

	@Test(groups = { "testUsers" }, description = "Create user (disabled)")
	public void test_createUserDisabled(){
		try{Thread.sleep(1000L);}catch(InterruptedException iex){} // to get new unique id.
		String pid = KatelloUtils.getUniqueID();
		this.username_disabled = "user_"+pid;
		KatelloUser user = null;
        try {
            user = servertasks.createUser(this.username_disabled, KatelloUser.DEFAULT_USER_EMAIL, KatelloUser.DEFAULT_USER_PASS, true);
        } catch (KatelloApiException e) {
            Assert.fail("Could not create user", e);
        }
		Assert.assertNotNull(user.getId(), "Check: not null returned: id");
		boolean disabled = user.isDisabled();
		Assert.assertTrue(disabled, "Check: returned value: disabled=true");
		this.userid_disabled = user.getId();
		
		log.info("Preparing disabled user: ["+this.username_disabled+"]");
	}
	
	@Test(groups = { "testUsers" }, description = "Create user (enabled)")
	public void test_createUserEnabled(){
		try{Thread.sleep(1000L);}catch(InterruptedException iex){} // to get new unique id.
		String pid = KatelloUtils.getUniqueID();
		this.username_enabled = "user_"+pid;
		KatelloUser user = null;
        try {
            user = servertasks.createUser(this.username_enabled, KatelloUser.DEFAULT_USER_EMAIL, KatelloUser.DEFAULT_USER_PASS, false);
        } catch (KatelloApiException e) {
            Assert.fail("Could not create user", e);
        }
		Assert.assertNotNull(user.getId(), "Check: not null returned: id");
		boolean disabled = user.isDisabled();
		Assert.assertFalse(disabled, "Check: returned value: disabled=false");
		
		log.info("Preparing enabled user: ["+this.username_enabled+"]");
	}
	
	@Test(dependsOnMethods={"test_createUserDisabled","test_createUserEnabled"},
			groups = { "testUsers" }, description = "Get all users")
	public void test_getUsers(){
		List<KatelloUser> users = null;
        try {
            users = servertasks.listUsers();           
        } catch (KatelloApiException e) {
            Assert.fail("Could not get user list", e);
        }
		boolean containsAdmin = false;
        boolean userFound_D=false, userFound_E=false;
		for ( KatelloUser user : users ) {
		    if ( user.getUsername().equals("admin")) {
		        containsAdmin = true;
		    }
            if(user.getUsername().equals(this.username_enabled)) {
                userFound_E = true;
                Assert.assertFalse(user.isDisabled(), "Check: enabled user's disabled flag.");
            }
            if(user.getUsername().equals(this.username_disabled)) {
                userFound_D = true;
                Assert.assertTrue(user.isDisabled(), "Check: disabled user's disabled flag.");
            }               
		}
		Assert.assertTrue((userFound_D && userFound_E && containsAdmin), "Check: all users (admin, " + this.username_disabled + ", " + this.username_enabled + ") should be found in the list");
	}

	// TODO - Make this data-driven?
	@Test(dependsOnMethods={"test_createUserDisabled","test_createUserEnabled"},
			groups = { "testUsers" }, description = "Get user")
	public void test_getUser(){
		KatelloUser user = null;
        try {
            user = servertasks.getUser(this.userid_disabled);
        } catch (KatelloApiException e) {
            Assert.fail("Could not get user", e);
        }
		Assert.assertTrue(user.getUsername().contains(this.username_disabled), "Check: returned username");
		Assert.assertTrue(user.isDisabled(), "Check: returned username's disabled status");
	}

	@Test(groups = { "testUsers" }, description = "Update user properties")
	public void test_updateUser(){
		String pid = KatelloUtils.getUniqueID();
		String updUser = "updUser_"+pid;
		KatelloUser user = null;
        try {
            user = servertasks.createUser(updUser, KatelloUser.DEFAULT_USER_EMAIL, KatelloUser.DEFAULT_USER_PASS, false);
        } catch (KatelloApiException e) {
            Assert.fail("Could not create user", e);
        }
		Long userId = user.getId();
		String pwdHash = user.getPassword();
		try{
		    servertasks.updateUser(userId, "disabled", Boolean.TRUE);		   
			KatelloUser updatedUser = servertasks.getUser(userId);
			Assert.assertTrue(updatedUser.isDisabled(), "Check: updated disable status");
			servertasks.updateUser(userId, "password", "123456");
			updatedUser = servertasks.getUser(userId);
			Assert.assertFalse(updatedUser.getPassword().contains(pwdHash), "Check: updated password");
		} catch (KatelloApiException e) {
		    Assert.fail("Error while updating user", e);
		}
	}
	
	@Test(groups = { "testUsers" }, description = "Delete user")
	public void test_deleteUser(){
		String pid = KatelloUtils.getUniqueID();
		String updUser = "delUser_"+pid;
		KatelloUser user = null;
        try {
            user = servertasks.createUser(updUser, KatelloUser.DEFAULT_USER_EMAIL, KatelloUser.DEFAULT_USER_PASS, false);
        } catch (KatelloApiException e) {
            Assert.fail("Could not create user", e);
        }
		Long userId = user.getId();
        String _ret = null;
        try {
            _ret = servertasks.deleteUser(userId);
        } catch (KatelloApiException e) {
            Assert.fail("Could not delete user", e);
        }
        Assert.assertEquals(_ret, String.format("Deleted user '%s'", userId.toString()),
                "Check: returned message of delete command");
        KatelloUser throwAway = null;
        try {
            throwAway = servertasks.getUser(userId);
        } catch (KatelloApiException e) {
            Assert.assertNull(throwAway, // .contains(String.format("Couldn't find User with ID=%s",
                                         // userId.toString())),
                    "Check: returned error message - getUsers()");

        }
	}
	
}
