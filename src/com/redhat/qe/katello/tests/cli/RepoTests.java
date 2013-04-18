package com.redhat.qe.katello.tests.cli;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.obj.KatelloChangeset;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloGpgKey;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloPackage;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.base.obj.KatelloUser;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.common.TngRunGroups;
import com.redhat.qe.tools.SCPTools;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups={"cfse-cli",TngRunGroups.TNG_KATELLO_Content})
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

	private String providerAutoDiscoverHttpPulpV2;
	private String providerAutoDiscoverFileZoo5;
	private String productAutoDiscoverHttpPulpV2;
	private String productAutoDiscoverFileZoo5;
	
	@BeforeClass(description = "Generate unique objects")
	public void setUp() {
		String uid = KatelloUtils.getUniqueID();
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
				"Package provider", PULP_RHEL6_x86_64_REPO);
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
	}

	//@ TODO bug 918452
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
		//Assert.assertEquals(getOutput(exec_result).trim(), String.format(KatelloRepo.ERR_REPO_EXISTS, repo.name, repo.product));
	}
	
	//@ TODO bug 918452
	@Test(description = "Discover repo", groups = { "cli-repo" })
	public void test_discoverRepo() {

		repo_name = "repo"+KatelloUtils.getUniqueID();
		String url_name = PULP_RHEL6_x86_64_REPO.replace("http://repos.fedorapeople.org", "").replace("/", "_");
		KatelloRepo repo = new KatelloRepo(repo_name, org_name, product_name, PULP_RHEL6_x86_64_REPO, null, null);
		exec_result = repo.discover(provider_name);
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
		KatelloRepo repo1 = new KatelloRepo(repoName1, org_name, product_name, PULP_RHEL6_x86_64_REPO, null, null);
		exec_result = repo1.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = repo1.list();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		assert_repoList(getOutput(exec_result).replaceAll("\n", " "), repo);
		assert_repoList(getOutput(exec_result).replaceAll("\n", " "), repo1);
	}
	
	//@ TODO bug 918452 
	@Test(description = "Update repo gpg key", groups = { "cli-repo" })
	public void test_updateRepo() {

		KatelloRepo repo = createRepo();
		
		repo.gpgkey = gpg_key;
		repo.update_gpgkey();
		
		assert_repoInfo(repo);
		assert_repoStatus(repo);
	}
	
	//@ TODO bug 918452 
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
		
		exec_result = repo.delete();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = repo.info();
		Assert.assertTrue(exec_result.getExitCode() == 65, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), String.format(KatelloRepo.ERR_REPO_NOTFOUND, repo.name, repo.org, repo.product, "Library"));
	}

	@Test(description = "Delete synced repo", groups = { "cli-repo" })
	public void test_deleteSyncedRepo() {

		KatelloRepo repo = createRepo();
		
		exec_result = repo.synchronize();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = repo.delete();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = repo.info();
		Assert.assertTrue(exec_result.getExitCode() == 65, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), String.format(KatelloRepo.ERR_REPO_NOTFOUND, repo.name, repo.org, repo.product, "Library"));
	}
	
	@Test(description = "Call commands on non existing repo", groups = { "cli-repo" })
	public void test_commandsInvalidRepo() {
		
		repo_name = "repo"+KatelloUtils.getUniqueID();;
		
		KatelloRepo repo = new KatelloRepo(repo_name, org_name, product_name, PULP_RHEL6_x86_64_REPO, null, null);
		
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
	
	/**
	 * @see https://github.com/gkhachik/katello-api/issues/278
	 */
	@Test(description="Repository Autodiscovery for existing Product - http method")
	public void test_discoverRepo_MultiRepos_HttpMethod(){
		String uid = KatelloUtils.getUniqueID();
		String url = REPO_DISCOVER_PULP_V2_ALL;
		this.providerAutoDiscoverHttpPulpV2 = "Pulp "+uid;
		this.productAutoDiscoverHttpPulpV2 = "Pulp V2 "+uid;
		new KatelloProvider(this.providerAutoDiscoverHttpPulpV2, this.org_name, "Pulp provider - autodiscovery via http", null).create();
		new KatelloProduct(this.productAutoDiscoverHttpPulpV2, this.org_name, this.providerAutoDiscoverHttpPulpV2, null, null, null, null, null).create();
		KatelloRepo _repo = new KatelloRepo("pulp-v2", this.org_name, this.productAutoDiscoverHttpPulpV2, url, null, null);
		SSHCommandResult res = _repo.discover(this.providerAutoDiscoverHttpPulpV2);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check  -return code");
		Assert.assertTrue(getOutput(_repo.custom_reposCount(null)).equals("10"), "Check - 10 repos were prepared");
	}
	
	/**
	 * @see https://github.com/gkhachik/katello-api/issues/282 
	 */
	@Test(description="Repository Autodiscovery for existing Product - file method")
	public void test_discoverRepo_SingleRepo_FileMethod(){
		String uid = KatelloUtils.getUniqueID();

		KatelloUtils.sshOnServer("rpm -q grinder || yum -y install grinder"); // it's part of katello-pulp yum repo.
		String url = "/var/www/html/auto-discover-"+uid;
		String cmd = String.format(
				"mkdir %s; " +
				"grinder yum --label zoo5 -U http://lzap.fedorapeople.org/fakerepos/zoo5/ -b %s; " +
				"createrepo %s/zoo5/",url,url,url);
		KatelloUtils.sshOnServer(cmd);

		this.providerAutoDiscoverFileZoo5 = "Zoo "+uid;
		this.productAutoDiscoverFileZoo5 = "Zoo5 "+uid;
		new KatelloProvider(this.providerAutoDiscoverFileZoo5, this.org_name, "Zoo provider - autodiscovery via file", null).create();
		new KatelloProduct(this.productAutoDiscoverFileZoo5, this.org_name, this.providerAutoDiscoverFileZoo5, null, null, null, null, null).create();
		KatelloRepo _repo = new KatelloRepo("_zoo5", this.org_name, this.productAutoDiscoverFileZoo5, "file://"+url, null, null);
		SSHCommandResult res = _repo.discover(this.providerAutoDiscoverFileZoo5);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check  -return code");
		Assert.assertTrue(getOutput(_repo.custom_reposCount(null)).equals("1"), "Check - 1 repo was prepared");
	}
	
	/**
	 * @see https://github.com/gkhachik/katello-api/issues/283
	 */
	@Test(description="Auto-discovered repositories can be synced and promoted",
			dependsOnMethods={"test_discoverRepo_MultiRepos_HttpMethod","test_discoverRepo_SingleRepo_FileMethod"})
	public void test_syncAndPromoteAutoDiscoveredRepos(){
		String uid = KatelloUtils.getUniqueID();
		String env_testing = "Testing "+uid;
		SSHCommandResult res = new KatelloEnvironment(env_testing, null, this.org_name, KatelloEnvironment.LIBRARY).cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check  -return code"); // we need to have the env. created.
		
		KatelloProduct prodPulpV2 = new KatelloProduct(this.productAutoDiscoverHttpPulpV2, this.org_name, this.providerAutoDiscoverHttpPulpV2, null, null, null, null, null);
		res = prodPulpV2.synchronize();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check  -return code");
		
		KatelloProduct prodZoo5 = new KatelloProduct(this.productAutoDiscoverFileZoo5, this.org_name, this.providerAutoDiscoverFileZoo5, null, null, null, null, null);
		res = prodZoo5.synchronize();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check  -return code");
		
		KatelloChangeset csTwoProds = new KatelloChangeset("cs-"+productAutoDiscoverHttpPulpV2, this.org_name, env_testing);
		csTwoProds.create();
		csTwoProds.update_addProduct(productAutoDiscoverHttpPulpV2);
		csTwoProds.update_addProduct(productAutoDiscoverFileZoo5);
		res = csTwoProds.apply();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check  -return code");
		Assert.assertTrue(getOutput(res).contains(String.format(KatelloChangeset.OUT_APPLIED,csTwoProds.name)), "Check - stdout successfully applied");
		
		// HTTP. Check - packages synced and promoted too.
		assert_allRepoPackagesSynced(this.org_name, this.productAutoDiscoverHttpPulpV2, env_testing, 10);
		
		// FTP. Check - packages synced and promoted too.
		assert_allRepoPackagesSynced(this.org_name, this.productAutoDiscoverFileZoo5, env_testing, 1);
	}
	
	/**
	 * @see https://github.com/gkhachik/katello-api/issues/284
	 */
	@Test(description="Auto-discovered repositories can use GPG keys from product")
	public void test_discoverRepo_addGPGBeforeDiscovery(){
		// send gpg key to the server /tmp
		SCPTools scp = new SCPTools(
				System.getProperty("katello.client.hostname", "localhost"), 
				System.getProperty("katello.client.ssh.user", "root"), 
				System.getProperty("katello.client.sshkey.private", ".ssh/id_hudson_dsa"), 
				System.getProperty("katello.client.sshkey.passphrase", "null"));
		Assert.assertTrue(scp.sendFile("data"+File.separator+KatelloGpgKey.FILE_GPG_GKHACHIK_KATELLO_API, "/tmp"),
				KatelloGpgKey.FILE_GPG_GKHACHIK_KATELLO_API+" sent successfully");
		
		String uid = KatelloUtils.getUniqueID();
		String url = REPO_DISCOVER_PULP_V2_ALL;
		String providername = "PulpGPG Before "+uid;
		String productname = "PulpGPG Before V2 "+uid;

		KatelloGpgKey key = new KatelloGpgKey("katello-api-"+uid, this.org_name, "/tmp/"+KatelloGpgKey.FILE_GPG_GKHACHIK_KATELLO_API);
		SSHCommandResult res = key.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check  -return code");
		
		new KatelloProvider(providername, this.org_name, null, null).create();
		new KatelloProduct(productname, this.org_name, providername, null, null, null, null, null).create();
		res = new KatelloProduct(productname, this.org_name, 
				providername, null, null, null, null, null).update_gpgkey(key.name);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check  -return code");
		KatelloRepo _repo = new KatelloRepo("pulp-v2", this.org_name, productname, url, null, null);
		res = _repo.discover(providername);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check  -return code");
		Assert.assertTrue(getOutput(_repo.custom_reposCount(null)).equals("10"), "Check - 10 repos were prepared");
		
		assert_allReposGPGAssigned(this.org_name, productname, key.name);
	}
	
	/**
	 * @see https://github.com/gkhachik/katello-api/issues/285
	 */
	@Test(description="Auto-discovered repositories can use GPG keys after creation", dependsOnMethods={"test_discoverRepo_addGPGBeforeDiscovery"})
	public void test_discoverRepo_addGPGAfterDiscovery(){
		String uid = KatelloUtils.getUniqueID();
		String url = REPO_DISCOVER_PULP_V2_ALL;
		String providername = "PulpGPG After "+uid;
		String productname = "PulpGPG After V2 "+uid;

		KatelloGpgKey key = new KatelloGpgKey("katello-api-"+uid, this.org_name, "/tmp/"+KatelloGpgKey.FILE_GPG_GKHACHIK_KATELLO_API);
		SSHCommandResult res = key.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check  -return code");
		
		new KatelloProvider(providername, this.org_name, null, null).create();
		new KatelloProduct(productname, this.org_name, providername, null, null, null, null, null).create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check  -return code");
		KatelloRepo _repo = new KatelloRepo("pulp-v2", this.org_name, productname, url, null, null);
		res = _repo.discover(providername);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check  -return code");
		Assert.assertTrue(getOutput(_repo.custom_reposCount(null)).equals("10"), "Check - 10 repos were prepared");
		
		assert_allReposGPGAssigned(this.org_name, productname, "");

		res = new KatelloProduct(productname, this.org_name, 
				providername, null, null, null, null, null).update_gpgkey(key.name,true);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check  -return code");

		assert_allReposGPGAssigned(this.org_name, productname, key.name);
	}
			
	private void assert_repoInfo(KatelloRepo repo) {
		if (repo.gpgkey == null) repo.gpgkey = "";
		if (repo.progress == null) repo.progress = "Not synced";
		if (repo.lastSync == null) repo.lastSync = "never";

		SSHCommandResult res;
		res = repo.info();

		String match_info = String.format(KatelloRepo.REG_REPO_INFO, repo.name, repo.url, repo.lastSync, repo.progress, repo.gpgkey).replaceAll("\"", "");
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		log.finest(String.format("Repo (info) match regex: [%s]", match_info));
		Assert.assertTrue(getOutput(res).replaceAll("\n", " ").matches(match_info), String.format("Repo [%s] should be found in the result info", repo.name));

	}
	
	private void assert_repoStatus(KatelloRepo repo) {
		if (repo.progress == null) repo.progress = "Not synced";
		if (repo.lastSync == null) repo.lastSync = "never";

		SSHCommandResult res;
		res = repo.status();

		String match_info = String.format(KatelloRepo.REG_REPO_STATUS, repo.lastSync, repo.progress).replaceAll("\"", "");
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
		
		KatelloRepo repo = new KatelloRepo(repo_name, org_name, product_name, PULP_RHEL6_x86_64_REPO, null, null);
		exec_result = repo.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloRepo.OUT_CREATE, repo_name)), "Check - output string (repo create)");
		
		return repo;
	}
	
	private void assert_allRepoPackagesSynced(String orgname, String productname, String envname, int repoCount){
		if(envname==null) envname = KatelloEnvironment.LIBRARY;
		KatelloRepo repo = new KatelloRepo(null, orgname, productname, null, null, null);
		SSHCommandResult res = repo.list(envname);
		int repoCnt = new Integer(getOutput(repo.custom_reposCount(envname))).intValue();
		Assert.assertTrue(repoCnt==repoCount, "Assert - all repos are promoted");
		String repoName; int pkgCount;
		for(int i=0;i<repoCnt;i++){
			repoName = KatelloCli.grepCLIOutput("Name", getOutput(res), (i+1));
			pkgCount = new Integer(getOutput(new KatelloPackage(null, null, orgname, 
					productname, repoName, envname).custom_packagesCount(null))).intValue();
			Assert.assertTrue(pkgCount>0, "Check - packages are synced for: "+repoName);
		}
	}
	
	private void assert_allReposGPGAssigned(String orgname, String productname, String gpgName){
		String repoName; SSHCommandResult res;
		KatelloRepo repo = new KatelloRepo(null, orgname, productname, null, null, null);
		SSHCommandResult resRepoList = repo.list();
		int repoCount = new Integer(getOutput(repo.custom_reposCount(null))).intValue();
		for(int i=0;i<repoCount;i++){
			repoName = KatelloCli.grepCLIOutput("Name", getOutput(resRepoList), (i+1));
			res = new KatelloRepo(repoName, orgname, productname, null, null, null).info();
			String __gpg = KatelloCli.grepCLIOutput("GPG Key", getOutput(res));
			Assert.assertTrue(gpgName.equals(__gpg), "Check - gpg is assigned for: "+repoName);
		}
	}

}
