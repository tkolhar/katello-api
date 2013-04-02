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
	private String content_name1;
	private String content_name2;
	
	
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
		content_name1 = "content1"+uid;
		content_name2 = "content2"+uid;
		
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

	//@ TODO bug 947464
	@Test(description="try to delete content definition without permissions", dependsOnMethods={"test_CreateAccess"})
	public void test_DeleteNoAccess() {
		KatelloUser user = new KatelloUser(user_create, "root@localhost", KatelloUser.DEFAULT_USER_PASS, false);
		KatelloContentView content = new KatelloContentView(content_name1, "description", org_name, content_name1);
		content.runAs(user);
		exec_result = content.definition_delete();
		Assert.assertTrue(exec_result.getExitCode()==147, "Check = error code (delete content definition)");
		// TODO check error message User #{} is not allowed to access #{}
		//Assert.assertTrue()
	}

	//@ TODO bug 947464
	@Test(description="check permissions to delete content definition", dependsOnMethods={"test_DeleteNoAccess"})
	public void test_DeleteAccess() {
		// delete old permission
		KatelloPermission perm = new KatelloPermission(perm_create, this.org_name, "content_view_definitions", null,
				"read,create", this.role_create);
		exec_result = perm.delete();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code (perm delete)");
		// create new one with delete privileges
		perm = new KatelloPermission(perm_create, this.org_name, "content_view_definitions", null,
				"read,delete,create", this.role_create);
		exec_result = perm.create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code (perm create)");

		// now delete that content definition
		KatelloUser user = new KatelloUser(user_create, "root@localhost", KatelloUser.DEFAULT_USER_PASS, false);
		KatelloContentView content = new KatelloContentView(content_name1, "description", org_name, content_name1);
		content.runAs(user);
		exec_result = content.definition_delete();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check - return code (delete content definition)");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloContentView.OUT_DELETE_DEFINITION, content_name1)), "Check - output message");
	}

}
