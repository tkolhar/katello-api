package com.redhat.qe.katello.tests.e2e;

import java.util.logging.Logger;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.obj.KatelloChangeset;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.base.obj.KatelloTemplate;
import com.redhat.qe.katello.base.obj.KatelloUser;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups={"cfse-e2e"})
public class PromotePackageWithDashes extends KatelloCliTestScript {
	protected static Logger log = Logger.getLogger(PromotePackageWithDashes.class.getName());
		
	private SSHCommandResult exec_result;
	
	private String org_name;
	private String user_name;
	private String provider_name;
	private String product_name;
	private String repo_name;
	private String env_name;
	private String env_name2;
	private String chst_name, chst_name2;
	private String templ_name;
	private final String packageName = "pulp-selinux-server";
	
	@BeforeClass(description="Generate unique objects")
	public void setUp(){
		String uid = KatelloUtils.getUniqueID();
		org_name = "org"+uid;
		user_name = "user"+uid;
		provider_name = "provider"+uid;
		product_name = "product"+uid;
		repo_name = "repo"+uid;
		env_name = "env1"+uid;	
		env_name2 = "env2"+uid;
		chst_name = "changeset"+uid;
		chst_name2 = "changeset2"+uid;
		templ_name = "template"+uid;
		
		// Create org:
		KatelloOrg org = new KatelloOrg(this.org_name,"Package tests");
		exec_result = org.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		// Create user:
		KatelloUser user = new KatelloUser(user_name, "root@localhost", KatelloUser.DEFAULT_USER_PASS, false);
		exec_result = user.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		// Create provider:
		KatelloProvider prov = new KatelloProvider(provider_name, org_name, "Package provider", null);
		exec_result = prov.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		// Create product:
		KatelloProduct prod = new KatelloProduct(product_name, org_name, provider_name, null, null, null, null, null);
		exec_result = prod.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	
		KatelloRepo repo = new KatelloRepo(repo_name, org_name, product_name, PULP_RHEL6_x86_64_REPO, null, null);
		exec_result = repo.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		KatelloEnvironment env = new KatelloEnvironment(env_name, null, org_name, KatelloEnvironment.LIBRARY);
		exec_result = env.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		exec_result = prod.promote(env_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		env = new KatelloEnvironment(env_name2, null, org_name, KatelloEnvironment.LIBRARY);
		exec_result = env.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		exec_result = prod.promote(env_name2);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = repo.synchronize();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	}
	
	@Test(description = "Create changeset, than add package to changeset and promote it", groups = { "cli-changeset" })
	public void test_promotePackageByCS() {
		// create Changeset
		KatelloChangeset cs = new KatelloChangeset(chst_name, org_name, env_name);
		exec_result = cs.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (changeset create)");
		
		exec_result = cs.update_add_package(product_name, packageName);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloChangeset.OUT_UPDATE, chst_name)), "Check - output string (changeset update)");
		
		exec_result = cs.apply();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	}
	
	@Test(description = "Create template, than add package to template, add it to changeset and promote it", groups = { "cli-changeset" })
	public void test_promotePackageByTemplate() {
		
		KatelloTemplate tpl = new KatelloTemplate(templ_name, null, org_name, null);
		exec_result = tpl.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
			
		exec_result = tpl.update_add_package(product_name, packageName);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		// create Changeset
		KatelloChangeset cs = new KatelloChangeset(chst_name2, org_name, env_name2);
		exec_result = cs.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (changeset create)");
		
		//@ TODO uncomment when bug 
		/**exec_result = cs.update_addTemplate(templ_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloChangeset.OUT_UPDATE, chst_name2)), "Check - output string (changeset update)");
		
		exec_result = cs.apply();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");**/
	}
}
