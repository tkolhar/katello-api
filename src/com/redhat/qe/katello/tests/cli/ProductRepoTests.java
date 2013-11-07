package com.redhat.qe.katello.tests.cli;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloGpgKey;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.base.tngext.TngPriority;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.common.TngRunGroups;
import com.redhat.qe.tools.SSHCommandResult;

@TngPriority(25)
@Test(groups={TngRunGroups.TNG_KATELLO_Providers_Repos})
public class ProductRepoTests extends KatelloCliTestBase {

	protected static Logger log = Logger
			.getLogger(PackageTests.class.getName());

	private SSHCommandResult exec_result;

	private String org_name;
	private String provider_name;
	private String product_name;
	private String product_id;
	private String repo_name;
	private String gpg_key;
	private String file_name;

	@BeforeClass(description = "Generate unique objects")
	public void setUp() {
		String uid = KatelloUtils.getUniqueID();
		org_name = "org" + uid;
		provider_name = "provider" + uid;
		product_name = "product" + uid;

		file_name = "/tmp/RPM-GPG-KEY-dummy-packages-generator";
		gpg_key = "gpgkey-"+uid;


		// Create org:
		KatelloOrg org = new KatelloOrg(this.cli_worker, this.org_name, "Package tests");
		exec_result = org.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		// Create provider:
		KatelloProvider prov = new KatelloProvider(this.cli_worker, provider_name, org_name,
				"Package provider", PULP_RHEL6_x86_64_REPO);
		exec_result = prov.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		// Create product:
		KatelloProduct prod = new KatelloProduct(this.cli_worker, product_name, org_name,
				provider_name, null, null, null, null, null);
		exec_result = prod.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = prod.cli_list();
		product_id = KatelloUtils.grepCLIOutput("ID", getOutput(exec_result).trim(),1);

		prod = new KatelloProduct(this.cli_worker, product_name, org_name,
				provider_name, null, null, null, null, null);
		prod.create();
		
		// wget the gpg file (before creating the key)
		String cmd = "rm -f "+this.file_name+"; " +
				"curl -sk "+KatelloGpgKey.REPO_GPG_FILE_ZOO+" -o "+this.file_name;
		exec_result = sshOnClient(cmd);
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code (get gpg file)");

		KatelloGpgKey gpg = new KatelloGpgKey(cli_worker, gpg_key, org_name, file_name);
		exec_result = gpg.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
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
		Assert.assertEquals(getOutput(exec_result).trim(), KatelloRepo.ERR_LABEL_EXISTS);
		//@ TODO switch to correct check when output is fixed
		//Assert.assertEquals(getOutput(exec_result).trim(), String.format(KatelloRepo.ERR_REPO_EXISTS, repo.name, product_name));
	}
	
	@Test(description = "Discover repo", groups = { "cli-repo" })
	public void test_discoverRepo() {

		repo_name = "repo"+KatelloUtils.getUniqueID();
		String url_name = PULP_RHEL6_x86_64_REPO.replace("http://repos.fedorapeople.org", "").replace("/", "_");
		KatelloRepo repo = new KatelloRepo(this.cli_worker, repo_name, org_name, null, PULP_RHEL6_x86_64_REPO, null, null, null, product_id);
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
		KatelloRepo repo1 = new KatelloRepo(this.cli_worker, repoName1, org_name, null, PULP_RHEL6_x86_64_REPO, null, null, null, product_id);
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
	
	@Test(description = "Synchronize repository", groups = { "cli-repo" })
	public void test_syncRepo() {

		DateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		
		KatelloRepo repo = createRepo();
		
		exec_result = repo.synchronize();
		
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).trim().contains(String.format(KatelloRepo.OUT_REPO_SYNCHED, repo.name)));
		
		exec_result = repo.status();
		Pattern pattern = Pattern.compile(KatelloRepo.REG_REPO_LASTSYNC);
		Matcher matcher = pattern.matcher(getOutput(exec_result));
		Assert.assertTrue(matcher.find(), "Check - last sync date should exist in repo status");
		String dateString = matcher.group();
		
		try {
			format.parse(dateString);
		} catch (ParseException e) {
			Assert.fail("Invalid date is returned");
		}
		
		repo.lastSync = dateString;
		
		assert_repoInfo(repo);
		assert_repoStatus(repo);
		
		exec_result = repo.list();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		assert_repoList(getOutput(exec_result).replaceAll("\n", " "), repo);
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
		Assert.assertEquals(getOutput(exec_result).trim(), String.format(KatelloRepo.ERR_REPO_NOTFOUND, repo.name, repo.org, product_name, "Library"));
	}
	
	@Test(description = "Call commands on non existing repo", groups = { "cli-repo" })
	public void test_commandsInvalidRepo() {
		repo_name = "repo"+KatelloUtils.getUniqueID();;
		
		KatelloRepo repo = new KatelloRepo(this.cli_worker, repo_name, org_name, null, PULP_RHEL6_x86_64_REPO, null, null, null, product_id);
		
		exec_result = repo.disable();
		Assert.assertTrue(exec_result.getExitCode() == 65, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), String.format(KatelloRepo.ERR_REPO_NOTFOUND, repo.name, repo.org, product_name, "Library"));
		
		exec_result = repo.enable();
		Assert.assertTrue(exec_result.getExitCode() == 65, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), String.format(KatelloRepo.ERR_REPO_NOTFOUND, repo.name, repo.org, product_name, "Library"));
		
		exec_result = repo.info();
		Assert.assertTrue(exec_result.getExitCode() == 65, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), String.format(KatelloRepo.ERR_REPO_NOTFOUND, repo.name, repo.org, product_name, "Library"));
		
		exec_result = repo.synchronize();
		Assert.assertTrue(exec_result.getExitCode() == 65, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), String.format(KatelloRepo.ERR_REPO_NOTFOUND, repo.name, repo.org, product_name, "Library"));
		
		repo.gpgkey = gpg_key;
		exec_result = repo.update_gpgkey();
		Assert.assertTrue(exec_result.getExitCode() == 65, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), String.format(KatelloRepo.ERR_REPO_NOTFOUND, repo.name, repo.org, product_name, "Library"));
	}

	private void assert_repoInfo(KatelloRepo repo) {
		if (repo.gpgkey == null) repo.gpgkey = "";
		if (repo.lastSync == null) repo.lastSync = "never";

		SSHCommandResult res;
		res = repo.info();

		String match_info = String.format(KatelloRepo.REG_REPO_INFO, repo.name, repo.url, repo.lastSync, repo.gpgkey).replaceAll("\"", "");
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		log.finest(String.format("Repo (info) match regex: [%s]", match_info));
		Assert.assertTrue(getOutput(res).replaceAll("\n", " ").matches(match_info), String.format("Repo [%s] should be found in the result info", repo.name));

	}
	
	private void assert_repoStatus(KatelloRepo repo) {
		if (repo.lastSync == null) repo.lastSync = "never";

		SSHCommandResult res;
		res = repo.status();

		String match_info = String.format(KatelloRepo.REG_REPO_STATUS, repo.lastSync).replaceAll("\"", "");
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		log.finest(String.format("Repo (status) match regex: [%s]", match_info));
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(match_info), String.format("Repo [%s] should be found in the result", repo.name));

	}
	
	private void assert_repoList(String result, KatelloRepo repo) {
		if (repo.lastSync == null) repo.lastSync = "never";

		String match_info = String.format(KatelloRepo.REG_REPO_LIST, repo.name, repo.lastSync).replaceAll("\"", "");
		Assert.assertTrue(result.matches(match_info), String.format("Repo [%s] should be found in the result list", repo.name));
	}
	
	private KatelloRepo createRepo() {
		repo_name = "repo"+KatelloUtils.getUniqueID();;
		
		KatelloRepo repo = new KatelloRepo(this.cli_worker, repo_name, org_name, null, PULP_RHEL6_x86_64_REPO, null, null, null, product_id);
		exec_result = repo.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloRepo.OUT_CREATE, repo_name)), "Check - output string (repo create)");
		
		return repo;
	}

}
