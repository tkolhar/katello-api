package com.redhat.qe.katello.tests.cli;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloPermission;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloSystemGroup;
import com.redhat.qe.katello.base.obj.KatelloUser;
import com.redhat.qe.katello.base.obj.KatelloUserRole;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.common.TngRunGroups;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups={"cfse-cli",TngRunGroups.TNG_KATELLO_Users_Roles})
public class UserPermissionTests extends KatelloCliTestBase {
	
	private String org;
	private String users[];
	private KatelloUser user1;
	private KatelloUser user2;
	private String roles[];
	private String permissions[];
	private String envs[];
	private String uid;
	private String provider;
	private String group;
	
	@BeforeClass(description="init: create org stuff")
	public void setUp(){
		SSHCommandResult res;
		uid = KatelloUtils.getUniqueID();
		
		this.org = "LDAP-org-"+uid;
		this.provider = "LDAP-prov-"+uid;
		this.group = "LDAP-group-"+uid;
		
		this.users = new String[2];
		this.users[0] = "ramesh-test"+uid;
		this.users[1] = "test-user"+uid;
		
		this.roles = new String[2];
		this.roles[0] = "LDAProle1" + uid;
		this.roles[1] = "LDAProles2" + uid;
		
		this.permissions = new String[5];
		this.permissions[0] = "Environmentsperm1" + uid;
		this.permissions[1] = "Orgsperm2" + uid;
		this.permissions[2] = "Provperm3" + uid;
		this.permissions[3] = "Groupsperm5" + uid;
		this.permissions[4] = "Groupsperm6" + uid;
		
		this.envs = new String[2];
		this.envs[0] = "Dev" + uid;
		this.envs[1] = "Test" + uid;
		
		KatelloOrg org = new KatelloOrg(this.cli_worker, this.org, null);
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		
		KatelloEnvironment env = new KatelloEnvironment(this.cli_worker, this.envs[0], null, this.org, KatelloEnvironment.LIBRARY);
		res = env.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		
		env = new KatelloEnvironment(this.cli_worker, this.envs[1], null, this.org, KatelloEnvironment.LIBRARY);
		res = env.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		
		KatelloProvider prov = new KatelloProvider(this.cli_worker, this.provider, this.org, null, null);
		res = prov.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		
		KatelloSystemGroup sysgrp = new KatelloSystemGroup(this.cli_worker, this.group, this.org);
		res = sysgrp.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
	}
	
	@AfterClass(description="remove all created stuff", alwaysRun=true)
	public void tearDown() {
		
		KatelloPermission perm;
		for (String prm : permissions) {
			for (String rl : roles) {
				perm = new KatelloPermission(cli_worker, prm, this.org, null, null, 
					null, rl);
				perm.delete();
			}
		}
		
		KatelloUser usr;
		for (String user : users) {
			usr = new KatelloUser(cli_worker, user, null, null, true);
			usr.delete();
		}
		
		KatelloUserRole role;
		for (String rl : roles) {
			role = new KatelloUserRole(cli_worker, rl, null);
			role.cli_delete();
		}
		
		KatelloOrg org = new KatelloOrg(this.cli_worker, this.org, null);
		org.delete();
	}
	
	@Test(description="create users - for default org", enabled=true)
	public void test_createUsers() {
		String userpass = "Redhat@1234";
		String usermail = users[0] + "@localhost";
		
		user1 = new KatelloUser(cli_worker, users[0], usermail, userpass, false);
		SSHCommandResult res = user1.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		
		usermail = users[1] + "@localhost";
		user2 = new KatelloUser(cli_worker, users[1], usermail, userpass, false);
		res = user2.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
	}
	
	@Test(description="create roles - for default org", dependsOnMethods={"test_createUsers"}, enabled=true)
	public void test_createRoles() {
		
		KatelloUserRole role = new KatelloUserRole(cli_worker, roles[0], "Role 1");
		SSHCommandResult res = role.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");

		role = new KatelloUserRole(cli_worker, roles[1], "Role 2");
		res = role.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		
		res = user1.assign_role(roles[0]);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		res = user2.assign_role(roles[1]);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
	}
	
	@Test(description="create permissions - for default org", dependsOnMethods={"test_createUsers", "test_createRoles"}, enabled=true)
	public void test_createPermissions() {
		
		KatelloPermission perm = new KatelloPermission(cli_worker, this.permissions[0], this.org, "environments", null, 
				"register_systems", this.roles[0]);
		SSHCommandResult res = perm.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		
		perm = new KatelloPermission(cli_worker, this.permissions[1], this.org, "organizations", null, 
				"read,update", this.roles[0]);
		res = perm.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		
		perm = new KatelloPermission(cli_worker, this.permissions[2], this.org, "providers", provider, 
				"read", this.roles[1]);
		res = perm.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		
		perm = new KatelloPermission(cli_worker, this.permissions[3], this.org, "system_groups", null, 
				"create, delete, update, read", this.roles[1]);
		res = perm.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		
		perm = new KatelloPermission(cli_worker, this.permissions[4], this.org, "system_groups", group, 
				"delete, update, read", this.roles[1]);
		res = perm.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
	}
	
	@Test(description="create permissions - for default org", dependsOnMethods={"test_createPermissions"}, enabled=true)
	public void test_userAccess() {
		SSHCommandResult res;
		
		KatelloOrg organization = new KatelloOrg(this.cli_worker, org, "");
		organization.runAs(user1);
		res = organization.update("test description");
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		res = organization.delete();
		Assert.assertTrue(res.getExitCode().intValue()==147, "Check - return code");
		organization.runAs(null);
		
		KatelloProvider prov = new KatelloProvider(this.cli_worker, "Prov" + uid, org, "", null);
		prov.runAs(user2);
		res = prov.create();
		Assert.assertTrue(res.getExitCode().intValue()==147, "Check - return code");
		prov = new KatelloProvider(this.cli_worker, provider, org, "", null);
		prov.runAs(user2);
		res = prov.update(null, null, "new descr");
		Assert.assertTrue(res.getExitCode().intValue()==147, "Check - return code");
		res = prov.info();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		res = prov.delete();
		Assert.assertTrue(res.getExitCode().intValue()==147, "Check - return code");
		prov.runAs(null);
		
		KatelloSystemGroup group = new KatelloSystemGroup(this.cli_worker, "testgroup" + uid, org);
		group.runAs(user2);
		res = group.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		group = new KatelloSystemGroup(this.cli_worker, group.name, org);
		group.runAs(user2);
		res = group.update(null, "new descr", 5);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		res = group.info();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		res = group.delete();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
	}


}
