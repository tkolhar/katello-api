package com.redhat.qe.katello.tests.cli;

import java.util.logging.Logger;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.auto.testng.Assert;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.katello.base.obj.KatelloChangeset;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloFilter;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.base.obj.KatelloUser;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups = { "cfse-cli" })
public class RepoTests extends KatelloCliTestScript {

	protected static Logger log = Logger
			.getLogger(PackageTests.class.getName());

	private SSHCommandResult exec_result;

	private String org_name;
	private String user_name;
	private String provider_name;
	private String product_name;
	private String repo_name;
	private String env_name;
	private String changeset_name;

	@BeforeClass(description = "Generate unique objects")
	public void setUp() {
		String uid = KatelloTestScript.getUniqueID();
		org_name = "org" + uid;
		user_name = "user" + uid;
		provider_name = "provider" + uid;
		product_name = "product" + uid;
		repo_name = "repo" + uid;
		env_name = "env" + uid;
		changeset_name = "changeset" + uid;

		// Create org:
		KatelloOrg org = new KatelloOrg(this.org_name, "Package tests");
		exec_result = org.cli_create();
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0,
				"Check - return code");

		// Create user:
		KatelloUser user = new KatelloUser(user_name, "root@localhost",
				KatelloUser.DEFAULT_USER_PASS, false);
		exec_result = user.cli_create();
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0,
				"Check - return code");

		// Create provider:
		KatelloProvider prov = new KatelloProvider(provider_name, org_name,
				"Package provider", PULP_F15_x86_64_REPO);
		exec_result = prov.create();
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0,
				"Check - return code");

		// Create product:
		KatelloProduct prod = new KatelloProduct(product_name, org_name,
				provider_name, null, null, null, null, null);
		exec_result = prod.create();
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0,
				"Check - return code");

		

		/**KatelloEnvironment env = new KatelloEnvironment(env_name, null,
				org_name, KatelloEnvironment.LIBRARY);
		exec_result = env.cli_create();
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0,
				"Check - return code");

		KatelloChangeset cs = new KatelloChangeset(changeset_name, org_name,
				env_name);
		exec_result = cs.create();
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0,
				"Check - return code");

		prov.synchronize();
		prod.synchronize();
		repo.synchronize();

		cs.promote();**/
	}

	@Test(description = "Create repo", groups = { "cli-filter" })
	public void test_createRepo() {
		
		KatelloRepo repo = new KatelloRepo(repo_name, org_name, product_name,
				PULP_F15_x86_64_REPO, null, null);
		exec_result = repo.create();
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0,
				"Check - return code");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloRepo.OUT_CREATE, repo_name)), 
				"Check - output string (repo create)");

	}
	
	private void assert_repoInfo(KatelloRepo repo) {
		if (repo.gpgkey == null) repo.gpgkey = "";
		
		SSHCommandResult res;
		res = repo.info();
		/**String match_info = String.format(KatelloRepo.REG_FILTER_INFO, filter.name, filter.description, filter.packages).replaceAll("\"", "");
		Assert.assertEquals(res.getExitCode().intValue(), 0, "Check - return code");
		log.finest(String.format("Filter (info) match regex: [%s]",match_info));
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(match_info), 
				String.format("Filter [%s] should be found in the result info",filter.name));**/		
	}

}
