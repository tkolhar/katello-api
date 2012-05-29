package com.redhat.qe.katello.tests.cli;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.redhat.qe.auto.testng.Assert;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.katello.base.obj.KatelloChangeset;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloPackage;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.base.obj.KatelloUser;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups={"cfse-cli"})
public class PackageTests extends KatelloCliTestScript {

	protected static Logger log = Logger.getLogger(PackageTests.class.getName());
	
	private SSHCommandResult exec_result;
	
	private String org_name;
	private String user_name; // not used still: `roles` needs to be in place in ordre to give user access to exec commands.
	private String provider_name;
	private String product_name;
	private String repo_name;
	private String env_name;
	private String changeset_name;

	
	@BeforeTest(description="Generate unique names")
	public void setUp(){
		String uid = KatelloTestScript.getUniqueID();
		org_name = "org"+uid;
		user_name = "user"+uid;
		provider_name = "provider"+uid;
		product_name = "product"+uid;
		repo_name = "repo"+uid;
		env_name = "env"+uid;
		changeset_name = "changeset"+uid;
		
		
		
		// Create org:
		KatelloOrg org = new KatelloOrg(this.org_name,"Package tests");
		exec_result = org.cli_create();
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		
		// Create user:
		KatelloUser user = new KatelloUser(user_name, "root@localhost", KatelloUser.DEFAULT_USER_PASS, false);
		exec_result = user.cli_create();
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		
		// Create provider:
		KatelloProvider prov = new KatelloProvider(provider_name, org_name, "Package provider", PULP_F15_x86_64_REPO);
		exec_result = prov.create();
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		
		// Create product:
		KatelloProduct prod = new KatelloProduct(product_name, org_name, provider_name, null, null, null, null, null);
		exec_result = prod.create();
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
	
		KatelloRepo repo = new KatelloRepo(repo_name, org_name, product_name, PULP_F15_x86_64_REPO, null, null);
		exec_result = repo.create();
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		
		KatelloEnvironment env = new KatelloEnvironment(env_name, null, org_name, KatelloEnvironment.LIBRARY);
		exec_result = env.cli_create();
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		
		KatelloChangeset cs = new KatelloChangeset(changeset_name, org_name, env_name);
		exec_result = cs.create();
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		
		prov.synchronize();
		prod.synchronize();
		repo.synchronize();

		cs.promote();
	}
	
	@Test(description="package list", groups = {"cli-packages"}, enabled=true)
	public void test_packageList() {
		
		KatelloPackage pack = new KatelloPackage(null, null, org_name, product_name, repo_name, null);
		
		exec_result = pack.cli_list();
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		
		Assert.assertTrue(getOutput(exec_result).contains("pulp-admin"));
		Assert.assertTrue(getOutput(exec_result).contains("pulp"));
		Assert.assertTrue(getOutput(exec_result).contains("python-gofer"));
		Assert.assertTrue(getOutput(exec_result).contains("python-qpid"));
		Assert.assertTrue(getOutput(exec_result).contains("pulp-common"));
		Assert.assertTrue(getOutput(exec_result).contains("pulp-consumer"));
		
	}
	
	@Test(description="package search", groups = {"cli-packages"}, enabled=true)
	public void test_packageSearch() {
		
		KatelloPackage pack = new KatelloPackage(null, null, org_name, product_name, repo_name, null);
		
		exec_result = pack.cli_search("pulp-common*");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		
		Assert.assertTrue(getOutput(exec_result).contains("pulp-common"), "Check - package name should exist in list result");
		
		Pattern pattern = Pattern.compile(KatelloPackage.REG_PACKAGE_ID);
		Matcher matcher = pattern.matcher(getOutput(exec_result));
		
		Assert.assertTrue(matcher.find(), "Check - package Id should exist in list result");
		
		String id = matcher.group();
		pack.id = id;
		pack.name = "pulp-common";
		
		assert_packageInfo(pack);
		
	}
	
	private void assert_packageInfo(KatelloPackage pack){
		SSHCommandResult res;
		res = pack.cli_info();
		Assert.assertEquals(res.getExitCode().intValue(), 0, "Check - return code");
		
		Assert.assertTrue(getOutput(res).contains(pack.id), "Check - package Id should exist in list result");		
		Assert.assertTrue(getOutput(res).contains(pack.name), "Check - package name should exist in list result");
	}
	
	
	
}
