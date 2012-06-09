package com.redhat.qe.katello.tests.cli;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.redhat.qe.auto.testng.Assert;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.katello.base.obj.KatelloFilter;
import com.redhat.qe.katello.base.obj.KatelloGpgKey;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.base.obj.KatelloUser;
import com.redhat.qe.katello.common.KatelloUtils;
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
	private String gpg_key;
	private String file_name;
	private String filter_name;
	private String filter_name2;
	private KatelloFilter filter1;
	private KatelloFilter filter2;

	@BeforeClass(description = "Generate unique objects")
	public void setUp() {
		String uid = KatelloTestScript.getUniqueID();
		org_name = "org" + uid;
		user_name = "user" + uid;
		provider_name = "provider" + uid;
		product_name = "product" + uid;

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

		// wget the gpg file (before creating the key)
		String cmd = "rm -f "+this.file_name+"; " +
				"curl -sk "+KatelloGpgKey.REPO_GPG_FILE_ZOO+" -o "+this.file_name;
		exec_result = KatelloUtils.sshOnClient(cmd);
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code (get gpg file)");

		KatelloGpgKey gpg = new KatelloGpgKey(gpg_key, org_name, file_name);
		exec_result = gpg.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		filter_name = "filter"+uid;
		filter1 = new KatelloFilter(filter_name, org_name, null, null);
		exec_result = filter1.create();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code (filter create)");
		
		filter_name2 = "filter2"+uid;
		filter2 = new KatelloFilter(filter_name2, org_name, null, null);
		exec_result = filter2.create();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code (filter create)");
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
		
		repo.progress = "Finished";
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
		Assert.assertEquals(getOutput(exec_result).trim(), String.format(KatelloRepo.ERR_REPO_NOTFOUND, repo.name, repo.org, repo.product, "Library"));
	}
	
	@Test(description = "Add filter to repo", groups = { "cli-repo" })
	public void test_addFilter() {

		KatelloRepo repo = createRepo();
		
		exec_result = repo.add_filter(filter_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), String.format(KatelloRepo.OUT_FILTER_ADDED, filter_name, repo.name));
		
		assert_repoFilterList(repo, Arrays.asList(filter1), new ArrayList<KatelloFilter>());
	}
	
	@Test(description = "Remove filter from repo", groups = { "cli-repo" })
	public void test_removeFilter() {

		KatelloRepo repo = createRepo();
		
		exec_result = repo.add_filter(filter_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = repo.add_filter(filter_name2);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		assert_repoFilterList(repo, Arrays.asList(filter1, filter2), new ArrayList<KatelloFilter>());
		
		repo.remove_filter(filter_name);
		
		assert_repoFilterList(repo, Arrays.asList(filter2), Arrays.asList(filter1));
	}
	
	@Test(description = "Call commands on non existing repo", groups = { "cli-repo" })
	public void test_commandsInvalidRepo() {
		
		repo_name = "repo"+KatelloTestScript.getUniqueID();;
		
		KatelloRepo repo = new KatelloRepo(repo_name, org_name, product_name, PULP_F15_x86_64_REPO, null, null);
		
		exec_result = repo.add_filter(filter_name);
		Assert.assertTrue(exec_result.getExitCode() == 65, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), String.format(KatelloRepo.ERR_REPO_NOTFOUND, repo.name, repo.org, repo.product, "Library"));
		
		exec_result = repo.remove_filter(filter_name2);
		Assert.assertTrue(exec_result.getExitCode() == 65, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), String.format(KatelloRepo.ERR_REPO_NOTFOUND, repo.name, repo.org, repo.product, "Library"));
		
		exec_result = repo.disable();
		Assert.assertTrue(exec_result.getExitCode() == 65, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), String.format(KatelloRepo.ERR_REPO_NOTFOUND, repo.name, repo.org, repo.product, "Library"));
		
		exec_result = repo.enable();
		Assert.assertTrue(exec_result.getExitCode() == 65, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), String.format(KatelloRepo.ERR_REPO_NOTFOUND, repo.name, repo.org, repo.product, "Library"));
		
		exec_result = repo.info();
		Assert.assertTrue(exec_result.getExitCode() == 65, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), String.format(KatelloRepo.ERR_REPO_NOTFOUND, repo.name, repo.org, repo.product, "Library"));
		
		exec_result = repo.synchronize();
		Assert.assertTrue(exec_result.getExitCode() == 65, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), String.format(KatelloRepo.ERR_REPO_NOTFOUND, repo.name, repo.org, repo.product, "Library"));
		
		repo.gpgkey = gpg_key;
		exec_result = repo.update_gpgkey();
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
	
	private void assert_repoFilterList(KatelloRepo repo, List<KatelloFilter> filters, List<KatelloFilter> excludeFilters) {

		SSHCommandResult res;
		res = repo.list_filters();

		//filters that exist in list
		for(KatelloFilter flt : filters){
			if(flt.description ==null) flt.description = "None";
			String match_list = String.format(KatelloRepo.REG_FILTER_LIST, flt.name, flt.description).replaceAll("\"", ""); // output not have '"' signs
			Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(match_list), "Check - filter matches ["+flt.name+"]");
		}
		
		//filters that should not exist in list
		for(KatelloFilter flt : excludeFilters){
			if(flt.description ==null) flt.description = "None";
			String match_list = String.format(KatelloRepo.REG_FILTER_LIST, flt.name, flt.description).replaceAll("\"", ""); // output not have '"' signs
			Assert.assertFalse(getOutput(res).replaceAll("\n", "").matches(match_list), "Check - filter not matches ["+flt.name+"]");
		}
		
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
