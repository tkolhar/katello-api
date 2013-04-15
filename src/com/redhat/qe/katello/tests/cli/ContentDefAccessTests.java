package com.redhat.qe.katello.tests.cli;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.obj.KatelloContentView;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloPermission;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.base.obj.KatelloUser;
import com.redhat.qe.katello.base.obj.KatelloUserRole;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

	
@Test(groups={"cfse-cli"})
public class ContentDefAccessTests extends KatelloCliTestScript{

	private SSHCommandResult exec_result;
	private String org_name;
	private String provider_name;
	private String product_name;
	private String repo_name;
	private String user_create;
	private String role_create;
	private String perm_create;
	private String user_modify;
	private String role_modify;
	private String perm_modify;
	private String content_name1;
	private String content_name2;
	private String content_name3;
	private String content_name4;
	private String user_delete;
	private String role_delete;
	private String perm_delete;
	private String user_read;
	private String role_read;
	private String perm_read;
	private String user_publish;
	private String role_publish;
	private String perm_publish;
	private String content_publish1;
	private String content_publish2;
	private String user_rpub;
	private String role_rpub;
	private String perm_rpub_v;
	private String perm_rpub_d;
	private String perm_rpub_o;
	private String content_rpub1;
	private String content_rpub2;
	private String view_name1;
	private String view_name2;
	
	@BeforeClass(description="init: create initial stuff")
	public void setUp() {
		String uid = KatelloUtils.getUniqueID();
		org_name = "org"+uid;
		provider_name = "provider"+uid;
		product_name = "product"+uid;
		repo_name = "repo"+uid;
		user_create = "user1"+uid;
		role_create = "role1"+uid;
		perm_create = "perm1"+uid;
		user_delete = "user2"+uid;
		role_delete = "role2"+uid;
		perm_delete = "perm2"+uid;
		user_read = "user3"+uid;
		role_read = "role3"+uid;
		perm_read = "perm3"+uid;
		content_name1 = "content1"+uid;
		content_name2 = "content2"+uid;
		content_name3 = "content3"+uid;
		content_name4 = "content4"+uid;
		content_publish1 = "contentp1"+uid;
		content_publish2 = "contentp2"+uid;
		user_publish = "user5"+uid;
		role_publish = "role5"+uid;
		perm_publish = "perm5"+uid;

		user_modify = "user4"+uid;
		role_modify = "role4"+uid;
		perm_modify = "perm4"+uid;

		user_rpub = "userrp"+uid;
		role_rpub = "rolerp"+uid;
		perm_rpub_v = "permrp1"+uid;
		perm_rpub_d = "permrp2"+uid;
		perm_rpub_o = "permrp3"+uid;
		content_rpub1 = "contentrp1"+uid;
		content_rpub2 = "contentrp2"+uid;
		view_name1 = "view1"+uid;
		view_name2 = "view2"+uid;

		
		// Create org:
		KatelloOrg org = new KatelloOrg(this.org_name, "Permission tests");
		exec_result = org.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		// Create provider:
		KatelloProvider prov = new KatelloProvider(provider_name, org_name, "Package provider", PULP_RHEL6_x86_64_REPO);
		exec_result = prov.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		// Create product:
		KatelloProduct prod = new KatelloProduct(product_name, org_name, provider_name, null, null, null, null, null);
		exec_result = prod.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		KatelloRepo repo = new KatelloRepo(repo_name, org_name, product_name, PULP_RHEL6_x86_64_REPO, null, null);
		exec_result = repo.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		KatelloUser user = new KatelloUser(this.user_create, "root@localhost", KatelloUser.DEFAULT_USER_PASS, false);
		exec_result = user.cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code (user create)");
		
		KatelloUserRole role = new KatelloUserRole(this.role_create, "create content definition");
		exec_result = role.create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code (user_role create)");
		
		KatelloPermission perm = new KatelloPermission(perm_create, this.org_name, "content_view_definitions", null,
				"read,create", this.role_create);
		exec_result = perm.create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code (perm create)");
		
		perm = new KatelloPermission("org_perm"+KatelloUtils.getUniqueID(), this.org_name, "organizations", null,
				"delete_distributors,delete_systems,update_distributors,update,update_systems,read_distributors,read,read_systems,register_distributors,register_systems,sync", this.role_create);
		exec_result = perm.create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code (perm create)");
		
		exec_result = user.assign_role(this.role_create);
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code (user assign_role)");
		
		// user delete
		KatelloUser user2 = new KatelloUser(user_delete, "root@localhost", KatelloUser.DEFAULT_USER_PASS, false);
		exec_result = user2.cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code (user create)");

		KatelloUserRole role2 = new KatelloUserRole(role_delete, "delete content definition");
		exec_result = role2.create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code (user_role create)");

		KatelloPermission perm2 = new KatelloPermission(perm_delete, this.org_name, "content_view_definitions", null,
				"read,delete", this.role_delete);
		exec_result = perm2.create(true);
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code (perm create)");
		exec_result = user2.assign_role(role_delete);
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code (user assign_role)");

		// user read
		KatelloUser user3 = new KatelloUser(user_read, "root@localhost", KatelloUser.DEFAULT_USER_PASS, false);
		exec_result = user3.cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code (user create)");
		
		KatelloUserRole role3 = new KatelloUserRole(role_read, "delete content definition");
		exec_result = role3.create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code (user_role create)");

		KatelloPermission perm3 = new KatelloPermission(perm_read, this.org_name, "content_view_definitions", null,
				"read", this.role_read);
		exec_result = perm3.create(true);
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code (perm create)");
		exec_result = user3.assign_role(role_read);
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code (user assign_role)");

		// content to delete
		KatelloContentView content = new KatelloContentView(content_name3, "description", org_name, content_name3);
		exec_result = content.create_definition();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		// publish user and stuff
		user = new KatelloUser(user_publish, "root@localhost", KatelloUser.DEFAULT_USER_PASS, false);
		exec_result = user.cli_create();

		role = new KatelloUserRole(role_publish, "create content definition");
		exec_result = role.create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code (user_role create)");

		perm = new KatelloPermission(perm_publish, this.org_name, "content_view_definitions", null,
				"read,update,publish", role_publish);
		exec_result = perm.create(true);
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code (perm create)");

		exec_result = user.assign_role(role_publish);
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code (user assign_role)");

		content = new KatelloContentView(content_publish1, "description", org_name, content_publish1);
		exec_result = content.create_definition();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		content = new KatelloContentView(content_publish2, "description", org_name, content_publish2);
		exec_result = content.create_definition();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		// stuff for modify tests
		content = new KatelloContentView(content_name4, "description", org_name, content_name4);
		exec_result = content.create_definition();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check - exit code (create definition)");

		role = new KatelloUserRole(role_modify, "description");
		exec_result = role.create();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check - exit code (create role)");

		perm = new KatelloPermission(perm_modify, org_name, "content_view_definitions", null, "read,update", role_modify);
		exec_result = perm.create(true);
		Assert.assertTrue(exec_result.getExitCode()==0, "Check - exit code (create role)");

		user = new KatelloUser(user_modify, "root@localhost", KatelloUser.DEFAULT_USER_PASS, false);
		exec_result = user.cli_create();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check - exit code (create user)");

		exec_result = user.assign_role(role_modify);
		Assert.assertTrue(exec_result.getExitCode()==0, "Check - exit code (assign role)");

		// read published access stuff
		content = new KatelloContentView(content_rpub1, "description", org_name, content_rpub1);
		exec_result = content.create_definition();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check - exit code (content create)");

		exec_result = content.publish(view_name1, view_name1, "description");
		Assert.assertTrue(exec_result.getExitCode()==0, "Check - exit code (content publish)");

		content = new KatelloContentView(content_rpub2, "description", org_name, content_rpub2);
		exec_result = content.create_definition();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check - exit code (content create)");

		exec_result = content.publish(view_name2, view_name2, "description");
		Assert.assertTrue(exec_result.getExitCode()==0, "Check - exit code (content publish)");

		user = new KatelloUser(user_rpub, KatelloUser.DEFAULT_USER_EMAIL, KatelloUser.DEFAULT_USER_PASS, false);
		exec_result = user.cli_create();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check - exit code (user create)");

		role = new KatelloUserRole(role_rpub, "read published content view");
		exec_result = role.create();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check - exit code (role create)");

		perm = new KatelloPermission(perm_rpub_v, org_name, "content_views", view_name1, "read", role_rpub);
		exec_result = perm.create();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check - exit code (perm create)");

		perm = new KatelloPermission(perm_rpub_d, org_name, "content_view_definitions", null, "create,delete,update,publish,read", role_rpub);
		exec_result = perm.create(true);
		Assert.assertTrue(exec_result.getExitCode()==0, "Check - exit code (perm create)");

		perm = new KatelloPermission(perm_rpub_o, org_name, "organizations", null, "read", role_rpub);
		exec_result = perm.create(true);
		Assert.assertTrue(exec_result.getExitCode()==0, "Check - exit code (perm create)");

		exec_result = user.assign_role(role_rpub);
		Assert.assertTrue(exec_result.getExitCode()==0, "Check - exit code (assign role)");
	}
	
	@Test(description = "check access in creating new content definition")
	public void test_CreateAccess() {
		KatelloUser user = new KatelloUser(this.user_create, "root@localhost", KatelloUser.DEFAULT_USER_PASS, false);
		KatelloContentView content = new KatelloContentView(content_name1, "descritpion", org_name, content_name1);
		content.runAs(user);
		exec_result = content.create_definition();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloContentView.OUT_CREATE_DEFINITION, content_name1)), "Check - out string (content create)");
	}

	@Test(description = "no access in creating new content definition", dependsOnMethods={"test_CreateAccess"})
	public void test_CreateNoAccess() {
		KatelloPermission perm = new KatelloPermission(perm_create, this.org_name, "content_view_definitions", null,
				"read,create", this.role_create);
		exec_result = perm.delete();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code (perm create)");
		
		perm = new KatelloPermission(perm_create, this.org_name, "content_view_definitions", null,
				"read", this.role_create);
		exec_result = perm.create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code (perm create)");
		
		KatelloUser user = new KatelloUser(this.user_create, "root@localhost", KatelloUser.DEFAULT_USER_PASS, false);
		KatelloContentView content = new KatelloContentView(content_name2, "descritpion", org_name, content_name2);
		content.runAs(user);
		exec_result = content.create_definition();
		Assert.assertTrue(exec_result.getExitCode() == 147, "Check - error code");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloContentView.ERR_CREATE_DENIED, this.user_create)), "Check - error string (content create)");
	}

	//@ TODO bug 950420
	@Test(description="try to delete content definition without permissions", dependsOnMethods={"test_CreateAccess"})
	public void test_DeleteNoAccess() {
		KatelloUser user = new KatelloUser(user_read, "root@localhost", KatelloUser.DEFAULT_USER_PASS, false);
		KatelloContentView content = new KatelloContentView(content_name1, "description", org_name, content_name1);
		content.runAs(user);
		exec_result = content.definition_delete();
		Assert.assertTrue(exec_result.getExitCode()==147, "Check = error code (delete content definition)");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloContentView.ERR_DELETE_DENIED, this.user_read)), "Check - error string (content delete)");
		user = new KatelloUser(user_publish, "root@localhost", KatelloUser.DEFAULT_USER_PASS, false);
		content.runAs(user);
		exec_result = content.definition_delete();
		Assert.assertTrue(exec_result.getExitCode()==147, "Check = error code (delete content definition)");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloContentView.ERR_DELETE_DENIED, this.user_read)), "Check - error string (content delete)");
	}

	//@ TODO bug 950420
	@Test(description="check permissions to delete content definition")
	public void test_DeleteAccess() {
		KatelloUser user = new KatelloUser(user_delete, "root@localhost", KatelloUser.DEFAULT_USER_PASS, false);
		KatelloContentView content = new KatelloContentView(content_name3, "description", org_name, content_name3);
		content.runAs(user);
		exec_result = content.definition_delete();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check - return code (delete content definition)");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloContentView.OUT_DELETE_DEFINITION, content_name3)), "Check - output message");
	}

	@Test(description="publish access")
	public void test_PublishAccess() {
		KatelloUser user = new KatelloUser(user_publish, "root@localhost", KatelloUser.DEFAULT_USER_PASS, false);
		KatelloContentView content = new KatelloContentView(content_publish1, "description", org_name, content_publish1);
		content.runAs(user);
		exec_result = content.publish("view_name1", "view_name1", "view description");
		Assert.assertTrue(exec_result.getExitCode()==0, "Check - exit code (publish content)");
		Assert.assertContainsMatch(getOutput(exec_result), "published successfully", null, "Check - output message (publish content)");
	}

	@Test(description="publish no access")
	public void test_PublishNoAccess() {
		KatelloUser user = new KatelloUser(user_read, "root@localhost", KatelloUser.DEFAULT_USER_PASS, false);
		KatelloContentView content = new KatelloContentView(content_publish2, "description", org_name, content_publish2);
		content.runAs(user);
		exec_result = content.publish("view_name2", "view_name2", "view description");
		Assert.assertTrue(exec_result.getExitCode()==147, "Check - error code (publish content)");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloContentView.ERR_PUBLISH_DENIED, user_read)), "Check - error string (content create)");
	}

	@Test(description="read access to content definition")
	public void test_ReadAccess() {
		KatelloUser user = new KatelloUser(user_read, "root@localhost", KatelloUser.DEFAULT_USER_PASS, false);
		KatelloContentView content = new KatelloContentView(content_publish1, "description", org_name, content_publish1);
		content.runAs(user);
		exec_result = content.definition_info();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check - return code (content info)");
		exec_result = content.definition_list();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check - return code (content list)");
	}

	@Test(description="no access to content definition")
	public void test_ReadNoAccess() {
		String uid = KatelloUtils.getUniqueID();
		String user_nobody = "nobody" + uid;
		KatelloUser user = new KatelloUser(user_nobody, "root@localhost", KatelloUser.DEFAULT_USER_PASS, false);
		exec_result = user.cli_create();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check - return code (acreted user)");
		KatelloContentView content = new KatelloContentView(content_publish1, "description", org_name, content_publish1);
		content.runAs(user);
		exec_result = content.definition_info();
		Assert.assertTrue(exec_result.getExitCode()==147, "Check - return code (content info)");
		exec_result = content.definition_list();
		Assert.assertTrue(exec_result.getExitCode()==147, "Check - return code (content list)");
	}

	//@ TODO bug 948733
	@Test(description="modify access to content view definition")
	public void test_modifyAccess() {
		KatelloUser user = new KatelloUser(user_modify, "root@localhost", KatelloUser.DEFAULT_USER_PASS, false);
		KatelloContentView content = new KatelloContentView(content_name4, "description", org_name, content_name4);
		content.runAs(user);
		exec_result = content.definition_update("new description");
		Assert.assertTrue(exec_result.getExitCode()==0, "Check - exit code (update content)");
		// TODO check output message
	}

	//@ TODO bug 948733
	@Test(description="no modify access to content view definition")
	public void test_modifyNoAccess() {
		KatelloUser user = new KatelloUser(user_read, "root@localhost", KatelloUser.DEFAULT_USER_PASS, false);
		KatelloContentView content = new KatelloContentView(content_name4, "description", org_name, content_name4);
		content.runAs(user);
		exec_result = content.definition_update("new description II");
		Assert.assertTrue(exec_result.getExitCode()==147, "Check - exit code (update content)");
		// TODO check error message
	}

	@Test(description="test read permission to content view")
	public void test_ReadPublishedAccess() {
		KatelloUser user = new KatelloUser(user_rpub, KatelloUser.DEFAULT_USER_EMAIL, KatelloUser.DEFAULT_USER_PASS, false);
		KatelloContentView content = new KatelloContentView(view_name1, "description", org_name, view_name1);
		content.runAs(user);
		exec_result = content.view_list();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check - exit code (list views)");
		Assert.assertTrue(getOutput(exec_result).contains(view_name1), "Check - output");
		Assert.assertFalse(getOutput(exec_result).contains(view_name2), "Check - output");
		exec_result = content.view_info(view_name1);
		Assert.assertTrue(exec_result.getExitCode()==0, "Check - exit code (list views)");
	}

	@Test(description="test read permission to content view")
	public void test_ReadPublishedNoAccess() {
		KatelloUser user = new KatelloUser(user_read, KatelloUser.DEFAULT_USER_EMAIL, KatelloUser.DEFAULT_USER_PASS, false);
		KatelloContentView content = new KatelloContentView(view_name1, "description", org_name, view_name1);
		content.runAs(user);
		exec_result = content.view_list();
		Assert.assertTrue(exec_result.getExitCode()==147, "Check - exit code (list views)");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloContentView.ERR_VIEW_READ, this.user_read)), "Check - error string (view read)");
		exec_result = content.view_info(view_name1);
		Assert.assertTrue(exec_result.getExitCode()==147, "Check - exit code (list views)");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloContentView.ERR_VIEW_READ, this.user_read)), "Check - error string (view read)");
	}

}
