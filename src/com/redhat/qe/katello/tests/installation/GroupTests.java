package com.redhat.qe.katello.tests.installation;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloUserRole;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups = { "cfse-cli", "headpin-cli" })
public class GroupTests extends KatelloCliTestBase {
	
	private String role_name;
	private final String LDAP_GROUP = "admin-group";
	private final String INVALID_LDAP_GROUP = "exgirlfriends";
	
	@BeforeClass(description="init: create role stuff")
	public void setUp(){
		String uid = KatelloUtils.getUniqueID();
		this.role_name = "role"+uid;
		KatelloUserRole role = new KatelloUserRole(cli_worker, role_name, "test role");
		SSHCommandResult res = role.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
	}
	
	//@ TODO bz#969394
	@Test(description="Add a valid ldap group name to the new role. Verify that group is added successfully.")
	public void test_addGroup() {
		KatelloUserRole role = new KatelloUserRole(cli_worker, role_name, "test role");
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
		KatelloUserRole role = new KatelloUserRole(cli_worker, role_name, "test role");
		SSHCommandResult res = role.cli_add_ldap_group(INVALID_LDAP_GROUP);
		Assert.assertTrue(res.getExitCode().intValue() == 166,"Check - error code (user role add ldap group)");
		Assert.assertTrue(getOutput(res).contains("Validation failed: Ldap group does not exist in your current LDAP system. Please choose a different group, or contact your LDAP administrator to have this group created"), "Check error code");
		res = role.cli_info();
		Assert.assertTrue(res.getExitCode().intValue() == 0,"Check - return code (user role info)");
		Assert.assertFalse(getOutput(res).contains(INVALID_LDAP_GROUP), INVALID_LDAP_GROUP + " is not in role.");
	}
}
