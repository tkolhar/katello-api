package com.redhat.qe.katello.tests.cli;

import java.util.logging.Logger;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.redhat.qe.auto.testng.Assert;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.katello.base.obj.KatelloGpgKey;
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
	private String gpg_key;
	private String file_name;

	@BeforeTest(description = "Generate unique objects")
	public void setUp() {
		String uid = KatelloTestScript.getUniqueID();
		org_name = "org" + uid;
		user_name = "user" + uid;
		provider_name = "provider" + uid;
		product_name = "product" + uid;
		env_name = "env" + uid;
		changeset_name = "changeset" + uid;
		file_name = "/tmp/RPM-GPG-KEY-dummy-packages-generator";
		gpg_key = "gpgkey-"+uid;


		// Create org:
		KatelloOrg org = new KatelloOrg(this.org_name, "Package tests");
		exec_result = org.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		// Create user:
		KatelloUser user = new KatelloUser(user_name, "root@localhost",
				KatelloUser.DEFAULT_USER_PASS, false);
		exec_result = user.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		// Create provider:
		KatelloProvider prov = new KatelloProvider(provider_name, org_name,
				"Package provider", PULP_F15_x86_64_REPO);
		exec_result = prov.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		// Create product:
		KatelloProduct prod = new KatelloProduct(product_name, org_name,
				provider_name, null, null, null, null, null);
		exec_result = prod.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		KatelloGpgKey gpg = new KatelloGpgKey(gpg_key, org_name, file_name);
		exec_result = gpg.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		/**
		 * KatelloEnvironment env = new KatelloEnvironment(env_name, null,
		 * org_name, KatelloEnvironment.LIBRARY); exec_result =
		 * env.cli_create();
		 * Assert.assertEquals(exec_result.getExitCode().intValue(), 0,
		 * "Check - return code");
		 * 
		 * KatelloChangeset cs = new KatelloChangeset(changeset_name, org_name,
		 * env_name); exec_result = cs.create();
		 * Assert.assertEquals(exec_result.getExitCode().intValue(), 0,
		 * "Check - return code");
		 * 
		 * prov.synchronize(); prod.synchronize(); repo.synchronize();
		 * 
		 * cs.promote();
		 **/
	}

	@Test(description = "Create repo", groups = { "cli-repo" })
	public void test_createRepo() {

		KatelloRepo repo = createRepo();
		
		assert_repoInfo(repo);
		assert_repoStatus(repo);
	}
	
	@Test(description = "Create repo exists", groups = { "cli-repo" })
	public void test_createRepoExists() {

		KatelloRepo repo = createRepo();
		
		exec_result = repo.create();
		Assert.assertTrue(exec_result.getExitCode() == 153, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), String.format(KatelloRepo.ERR_REPO_EXISTS, repo.name, repo.product));
	}
	
	@Test(description = "Discover repo", groups = { "cli-repo" })
	public void test_discoverRepo() {

		repo_name = "repo"+KatelloTestScript.getUniqueID();
		String url_name = PULP_F15_x86_64_REPO.replace("http://repos.fedorapeople.org", "").replace("/", "_");
		url_name = url_name.substring(0, url_name.length()-1);
		KatelloRepo repo = new KatelloRepo(repo_name, org_name, product_name, PULP_F15_x86_64_REPO, null, null);
		exec_result = repo.discover();
		repo_name += url_name;
		repo.name = repo_name;
		repo.url = repo.url.substring(0, repo.url.length() - 1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloRepo.OUT_DISCOVER, repo_name)), "Check - output string (repo discover)");
		
		assert_repoInfo(repo);
		assert_repoStatus(repo);
	}
	
	@Test(description = "List repos", groups = { "cli-repo" })
	public void test_listRepo() {
		
		KatelloRepo repo = createRepo();
		
		String repoName1 = repo_name + "1";
		KatelloRepo repo1 = new KatelloRepo(repoName1, org_name, product_name, PULP_F15_x86_64_REPO, null, null);
		exec_result = repo1.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = repo1.list();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		assert_repoList(getOutput(exec_result).replaceAll("\n", " "), repo);
		assert_repoList(getOutput(exec_result).replaceAll("\n", " "), repo1);
	}
	
	@Test(description = "Update repo gpg key", groups = { "cli-repo" })
	public void test_updateRepo() {

		KatelloRepo repo = createRepo();
		
		repo.gpgkey = gpg_key;
		repo.update_gpgkey();
		
		assert_repoInfo(repo);
		assert_repoStatus(repo);
	}
	
	@Test(description = "Try to enable/disable custom repo, check error", groups = { "cli-repo" })
	public void test_enableDisableRepo() {

		KatelloRepo repo = createRepo();
		
		exec_result = repo.enable();
		Assert.assertTrue(exec_result.getExitCode() == 148, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), "Disable/enable is not supported for custom repositories.");
		
		exec_result = repo.disable();
		Assert.assertTrue(exec_result.getExitCode() == 148, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), "Disable/enable is not supported for custom repositories.");
	}
	
	@Test(description = "Delete repo", groups = { "cli-repo" })
	public void test_deleteRepo() {

		KatelloRepo repo = createRepo();
		
		repo.delete();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = repo.info();
		Assert.assertTrue(exec_result.getExitCode() == 65, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), String.format(KatelloRepo.ERR_REPO_NOTFOUND, repo.name, repo.org, repo.product, "Library"));
	}

	private void assert_repoInfo(KatelloRepo repo) {
		if (repo.gpgkey == null) repo.gpgkey = "";
		if (repo.progress == null) repo.progress = "Not synced";
		if (repo.lastSync == null) repo.lastSync = "never";

		SSHCommandResult res;
		res = repo.info();

		String match_info = String.format(KatelloRepo.REG_REPO_INFO, repo.name, repo.url, repo.lastSync, repo.progress, repo.gpgkey).replaceAll("\"", "");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		log.finest(String.format("Repo (info) match regex: [%s]", match_info));
		Assert.assertTrue(getOutput(res).replaceAll("\n", " ").matches(match_info), String.format("Repo [%s] should be found in the result info", repo.name));

	}
	
	private void assert_repoStatus(KatelloRepo repo) {
		if (repo.progress == null) repo.progress = "Not synced";
		if (repo.lastSync == null) repo.lastSync = "never";

		SSHCommandResult res;
		res = repo.status();

		String match_info = String.format(KatelloRepo.REG_REPO_STATUS, repo.lastSync, repo.progress).replaceAll("\"", "");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		log.finest(String.format("Repo (status) match regex: [%s]", match_info));
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(match_info), String.format("Repo [%s] should be found in the result", repo.name));

	}
	
	private void assert_repoList(String result, KatelloRepo repo) {
		if (repo.lastSync == null) repo.lastSync = "never";

		String match_info = String.format(KatelloRepo.REG_REPO_LIST, repo.name, repo.lastSync).replaceAll("\"", "");
		Assert.assertTrue(result.matches(match_info), String.format("Repo [%s] should be found in the result list", repo.name));
	}
	
	private KatelloRepo createRepo() {
		repo_name = "repo"+KatelloTestScript.getUniqueID();;
		
		KatelloRepo repo = new KatelloRepo(repo_name, org_name, product_name, PULP_F15_x86_64_REPO, null, null);
		exec_result = repo.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloRepo.OUT_CREATE, repo_name)), "Check - output string (repo create)");
		
		return repo;
	}
	
	

}
