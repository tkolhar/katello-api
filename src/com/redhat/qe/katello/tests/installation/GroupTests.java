package com.redhat.qe.katello.tests.installation;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.obj.KatelloUserRole;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups = { "cfse-cli", "headpin-cli" })
public class GroupTests extends KatelloCliTestScript {
	
	private String role_name;
	private final String LDAP_GROUP = "admins";
	private final String INVALID_LDAP_GROUP = "exgirlfriends";
	
	@BeforeClass(description="init: create role stuff")
	public void setUp(){
		String uid = KatelloUtils.getUniqueID();
		this.role_name = "role"+uid;
		KatelloUserRole role = new KatelloUserRole(role_name, "test role");
		SSHCommandResult res = role.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
	}
	
	@Test(description="Add a valid ldap group name to the new role. Verify that group is added successfully.")
	public void test_addGroup() {
		KatelloUserRole role = new KatelloUserRole(role_name, "test role");
		SSHCommandResult res = role.cli_add_ldap_group(LDAP_GROUP);
		Assert.assertTrue(res.getExitCode().intValue() == 0,"Check - return code (user role add ldap group)");
		Assert.assertTrue(getOutput(res).contains(
				String.format(KatelloUserRole.OUT_LDAP_ADD,LDAP_GROUP,role_name)), 
				"Check - returned output string ("+KatelloUserRole.CMD_LDAP_GRP_ADD+")");
		res = role.cli_info();
		Assert.assertTrue(res.getExitCode().intValue() == 0,"Check - return code (user role info)");
		Assert.assertTrue(getOutput(res).contains(LDAP_GROUP), LDAP_GROUP + " is in role.");
	}

	@Test(description="Add a invalid ldap group name to the new role. Verify that group is not added and error is shown.")
	public void test_addInvalidGroup() {
		KatelloUserRole role = new KatelloUserRole(role_name, "test role");
		SSHCommandResult res = role.cli_add_ldap_group(INVALID_LDAP_GROUP);
		Assert.assertTrue(res.getExitCode().intValue() == 144,"Check - error code (user role add ldap group)");
		Assert.assertTrue(getOutput(res).contains("error"), "Check error code");
		res = role.cli_info();
		Assert.assertTrue(res.getExitCode().intValue() == 0,"Check - return code (user role info)");
		Assert.assertFalse(getOutput(res).contains(LDAP_GROUP), LDAP_GROUP + " is not in role.");
	}
}
