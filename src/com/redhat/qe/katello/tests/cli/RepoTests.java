package com.redhat.qe.katello.tests.cli;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloGpgKey;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloPackage;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloPuppetModule;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.base.obj.KatelloUser;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.common.TngRunGroups;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups={"cfse-cli",TngRunGroups.TNG_KATELLO_Providers_Repos})
public class RepoTests extends KatelloCliTestBase {

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
	
	private String orgWithManifest;
	private String secondOrg;
	private String repo_upload_yum;
	private String repo_upload_puppet;
	private String rpm_pkg_name;
	private String puppet_module_name;
	
	@BeforeClass(description = "Generate unique objects")
	public void setUp() {
		String uid = KatelloUtils.getUniqueID();
		org_name = "org" + uid;
		user_name = "user" + uid;
		provider_name = "provider" + uid;
		product_name = "product" + uid;
		repo_upload_yum = "repo-yum-"+uid;
		repo_upload_puppet = "repo-puppet-"+uid;

		file_name = "/tmp/RPM-GPG-KEY-dummy-packages-generator";
		gpg_key = "gpgkey-"+uid;

		this.orgWithManifest = "OrgWithManifest-"+KatelloUtils.getUniqueID();

		// Create org:
		KatelloOrg org = new KatelloOrg(this.cli_worker, this.org_name, "Package tests");
		exec_result = org.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		// Create user:
		KatelloUser user = new KatelloUser(cli_worker, user_name, "root@localhost",
				KatelloUser.DEFAULT_USER_PASS, false);
		exec_result = user.cli_create();
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

		// wget the gpg file (before creating the key)
		String cmd = "rm -f "+this.file_name+"; " +
				"curl -sk "+KatelloGpgKey.REPO_GPG_FILE_ZOO+" -o "+this.file_name;
		exec_result = sshOnClient(cmd);
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code (get gpg file)");

		KatelloGpgKey gpg = new KatelloGpgKey(cli_worker, gpg_key, org_name, file_name);
		exec_result = gpg.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		// create repos and wget files for content upload
		KatelloRepo repo = new KatelloRepo(cli_worker, repo_upload_yum, org_name, product_name, null, null, null);
		exec_result = repo.create();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (repo create)");
		repo = new KatelloRepo(cli_worker, repo_upload_puppet, org_name, product_name, null, null, null);
		repo.content_type = "puppet";
		exec_result = repo.create();
		puppet_module_name = "adob-good-2.0.0.tar.gz";
		rpm_pkg_name = "bat-3.10.7-1.noarch.rpm";
		sshOnClient(String.format("rm -f /tmp/%s; wget %s%s -O /tmp/%s", rpm_pkg_name, REPO_HHOVSEPY_ZOO4, rpm_pkg_name, rpm_pkg_name));
		String puppetmodule_url = "https://raw.github.com/pulp/pulp_puppet/master/pulp_puppet_plugins/test/data/good-modules/adob-good/pkg/adob-good-2.0.0.tar.gz";
		sshOnClient(String.format("rm -f /tmp/%s; wget %s -O /tmp/%s", puppet_module_name, puppetmodule_url, puppet_module_name));
	}

	@Test(description = "Create repo")
	public void test_createRepo() {

		KatelloRepo repo = createRepo();
		
		assert_repoInfo(repo);
		assert_repoStatus(repo);
	}
	
	@Test(description = "Create repo exists")
	public void test_createRepoExists() {

		KatelloRepo repo = createRepo();
		
		exec_result = repo.create();
		Assert.assertTrue(exec_result.getExitCode() == 153, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), KatelloRepo.ERR_LABEL_EXISTS);
		//@ TODO switch to correct check when output is fixed
		//Assert.assertEquals(getOutput(exec_result).trim(), String.format(KatelloRepo.ERR_REPO_EXISTS, repo.name, repo.product));
	}

	@Test(description = "Create repo without url. Try to sync the repo.")
	public void test_createRepoWithoutURL() {
		
		repo_name = "repoNoURL-"+KatelloUtils.getUniqueID();;
		
		KatelloRepo repo = new KatelloRepo(this.cli_worker, repo_name, org_name, product_name, null, null, null);
		exec_result = repo.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloRepo.OUT_CREATE, repo_name)), "Check - output string (repo create without URL)");	
		
		exec_result = repo.synchronize();
		
		Assert.assertTrue(exec_result.getExitCode() == 65, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).trim().contains(String.format(KatelloRepo.ERR_REPO_SYNC_FAIL, repo.name)));

	}

	@Test(description = "Discover repo")
	public void test_discoverRepo() {

		repo_name = "repo"+KatelloUtils.getUniqueID();
		String url_name = PULP_RHEL6_x86_64_REPO.replace("http://repos.fedorapeople.org", "").replace("/", "_");
		KatelloRepo repo = new KatelloRepo(this.cli_worker, repo_name, org_name, product_name, PULP_RHEL6_x86_64_REPO, null, null);
		exec_result = repo.discover(provider_name);
		repo_name += url_name;
		repo.name = repo_name;
		repo.url = repo.url.substring(0, repo.url.length() - 1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloRepo.OUT_DISCOVER, repo_name)), "Check - output string (repo discover)");
		
		assert_repoInfo(repo);
		assert_repoStatus(repo);
	}
	
	@Test(description = "List repos")
	public void test_listRepo() {
		
		KatelloRepo repo = createRepo();
		
		String repoName1 = repo_name + "1";
		KatelloRepo repo1 = new KatelloRepo(this.cli_worker, repoName1, org_name, product_name, PULP_RHEL6_x86_64_REPO, null, null);
		exec_result = repo1.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = repo1.list();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		assert_repoList(getOutput(exec_result).replaceAll("\n", " "), repo);
		assert_repoList(getOutput(exec_result).replaceAll("\n", " "), repo1);

		repo1.product = null;
		exec_result = repo1.list();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	}
	
	@Test(description = "Update repo gpg key")
	public void test_updateRepo() {

		KatelloRepo repo = createRepo();
		
		repo.gpgkey = gpg_key;
		repo.update_gpgkey();
		
		assert_repoInfo(repo);
		assert_repoStatus(repo);
	}
	
	@Test(description = "Synchronize repository")
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
	
	@Test(description = "Try to enable/disable custom repo, check error")
	public void test_enableDisableRepo() {

		KatelloRepo repo = createRepo();
		
		exec_result = repo.enable();
		Assert.assertTrue(exec_result.getExitCode() == 148, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), "Disable/enable is not supported for custom repositories.");
		
		exec_result = repo.disable();
		Assert.assertTrue(exec_result.getExitCode() == 148, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), "Disable/enable is not supported for custom repositories.");
	}
	
	@Test(description = "Delete repo")
	public void test_deleteRepo() {

		KatelloRepo repo = createRepo();
		
		exec_result = repo.delete();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = repo.info();
		Assert.assertTrue(exec_result.getExitCode() == 65, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), String.format(KatelloRepo.ERR_REPO_NOTFOUND, repo.name, repo.org, repo.product, "Library"));
	}

	@Test(description = "Delete synced repo")
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
	
	@Test(description = "Cancel repo synchronization when no sync. running")
	public void test_cancelNoSynchronization() {
		KatelloRepo repo = createRepo();
		exec_result = repo.cancel_sync();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (repo cancel sync)");
		Assert.assertTrue(getOutput(exec_result).equals(KatelloRepo.OUT_NO_SYNC_RUNNIG), "Check output (repo cancel sync)");
	}

	@Test(description="unset repo gpgkey")
	public void test_removeRepoGpgKey() {
		String repo_name = "repo"+KatelloUtils.getUniqueID();
		KatelloRepo repo = new KatelloRepo(cli_worker, repo_name, org_name, product_name, PULP_RHEL6_x86_64_REPO, gpg_key, null);
		exec_result = repo.create();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (repo create)");
		exec_result = repo.update_nogpgkey();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (repo unset gpgkey)");
		exec_result = repo.info();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (repo info)");
		String key = KatelloUtils.grepCLIOutput("GPG Key", getOutput(exec_result));
		Assert.assertTrue(key.isEmpty(), "Check exit code (repo info - no key)");
	}

	@Test(description="Enable, disable Red Hat repository", dependsOnMethods={"test_listRedHatProductRepos"})
	public void test_enableDisableRedHatRepo() {
		String product_name = KatelloProduct.RHEL_SERVER;
		String reposet_name = KatelloProduct.REPOSET_RHEL6_RPMS;
		
		KatelloProduct product = new KatelloProduct(cli_worker, product_name, orgWithManifest, null, null, null, null, null, null);
		exec_result = product.repository_set_enable(reposet_name);
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (enable repo set)");
		KatelloRepo repo = new KatelloRepo(cli_worker, KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT, orgWithManifest, product_name, null, null, null);

		// enable repo
		exec_result = repo.enable();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (enable repo)");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloRepo.OUT_REPO_ENABLED, KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT)), "Check output (enable repo)");
		exec_result = repo.list();
		Assert.assertTrue(getOutput(exec_result).contains(KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT), "Check output (repo listed)");
		// disable repo
		exec_result = repo.disable();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (disable repo)");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloRepo.OUT_REPO_DISABLED, KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT)), "Check output (disable repo)");
		exec_result = repo.list();
		Assert.assertFalse(getOutput(exec_result).contains(KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT), "Check output (disabled repo not listed)");
	}

	@Test(description="test repo list --include_disabled", dependsOnMethods={"test_enableDisableRedHatRepo"})
	public void test_repoListIncludeDisabled() {
		KatelloRepo repo = new KatelloRepo(cli_worker, null, orgWithManifest, null, null, null, null);
		exec_result = repo.listAll();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (repo list --include_disabled)");
		Assert.assertTrue(getOutput(exec_result).contains(KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT), "Check output (repo listed)");
	}

	@Test(description = "Call commands on non existing repo")
	public void test_commandsInvalidRepo() {
		
		repo_name = "repo"+KatelloUtils.getUniqueID();;
		
		KatelloRepo repo = new KatelloRepo(this.cli_worker, repo_name, org_name, product_name, PULP_RHEL6_x86_64_REPO, null, null);
		
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
		new KatelloProvider(this.cli_worker, this.providerAutoDiscoverHttpPulpV2, this.org_name, "Pulp provider - autodiscovery via http", null).create();
		new KatelloProduct(this.cli_worker, this.productAutoDiscoverHttpPulpV2, this.org_name, this.providerAutoDiscoverHttpPulpV2, null, null, null, null, null).create();
		KatelloRepo _repo = new KatelloRepo(this.cli_worker, "pulp-v2", this.org_name, this.productAutoDiscoverHttpPulpV2, url, null, null);
		SSHCommandResult res = _repo.discover(this.providerAutoDiscoverHttpPulpV2);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check  -return code");
		Assert.assertTrue(getOutput(_repo.custom_reposCount(null,null)).equals("8"), "Check - 8 repos were prepared");
	}

	/**
	 * @see https://github.com/gkhachik/katello-api/issues/282 
	 */
	@Test(description="Repository Autodiscovery for existing Product - file method")
	public void test_discoverRepo_SingleRepo_FileMethod(){
		String uid = KatelloUtils.getUniqueID();

		KatelloUtils.sshOnServer("rpm -q grinder || yum -y install "+RPM_GRINDER_RHEL6);
		String url = "/var/www/html/auto-discover-"+uid;
		String cmd = String.format(
				"mkdir %s; " +
				"grinder yum --label zoo5 -U http://lzap.fedorapeople.org/fakerepos/zoo5/ -b %s; " +
				"createrepo %s/zoo5/",url,url,url);
		KatelloUtils.sshOnServer(cmd);

		this.providerAutoDiscoverFileZoo5 = "Zoo "+uid;
		this.productAutoDiscoverFileZoo5 = "Zoo5 "+uid;
		new KatelloProvider(this.cli_worker, this.providerAutoDiscoverFileZoo5, this.org_name, "Zoo provider - autodiscovery via file", null).create();
		new KatelloProduct(this.cli_worker, this.productAutoDiscoverFileZoo5, this.org_name, this.providerAutoDiscoverFileZoo5, null, null, null, null, null).create();
		KatelloRepo _repo = new KatelloRepo(this.cli_worker, "_zoo5", this.org_name, this.productAutoDiscoverFileZoo5, "file://"+url, null, null);
		SSHCommandResult res = _repo.discover(this.providerAutoDiscoverFileZoo5);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check  -return code");
		Assert.assertTrue(getOutput(_repo.custom_reposCount(null,null)).equals("1"), "Check - 1 repo was prepared");
	}

	/**
	 * @see https://github.com/gkhachik/katello-api/issues/283
	 * TODO: bz#961780  repo list should be changed to accept option --content_view
	 */
	@Test(description="Auto-discovered repositories can be synced and promoted",
			dependsOnMethods={"test_discoverRepo_MultiRepos_HttpMethod","test_discoverRepo_SingleRepo_FileMethod"})
	public void test_syncAndPromoteAutoDiscoveredRepos(){
		String uid = KatelloUtils.getUniqueID();
		String env_testing = "Testing "+uid;
		SSHCommandResult res = new KatelloEnvironment(this.cli_worker, env_testing, null, this.org_name, KatelloEnvironment.LIBRARY).cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check  -return code"); // we need to have the env. created.
		
		KatelloProduct prodPulpV2 = new KatelloProduct(this.cli_worker, this.productAutoDiscoverHttpPulpV2, this.org_name, this.providerAutoDiscoverHttpPulpV2, null, null, null, null, null);
		res = prodPulpV2.synchronize();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check  -return code");
		
		KatelloProduct prodZoo5 = new KatelloProduct(this.cli_worker, this.productAutoDiscoverFileZoo5, this.org_name, this.providerAutoDiscoverFileZoo5, null, null, null, null, null);
		res = prodZoo5.synchronize();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check  -return code");
		
		KatelloUtils.promoteProductToEnvironment(cli_worker, org_name, productAutoDiscoverHttpPulpV2, env_testing);
		KatelloUtils.promoteProductToEnvironment(cli_worker, org_name, productAutoDiscoverFileZoo5, env_testing);
		
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
		KatelloUtils.scpOnClient(cli_worker.getClientHostname(), "data/"+KatelloGpgKey.FILE_GPG_GKHACHIK_KATELLO_API, "/tmp");
		
		String uid = KatelloUtils.getUniqueID();
		String url = REPO_DISCOVER_PULP_V2_ALL;
		String providername = "PulpGPG Before "+uid;
		String productname = "PulpGPG Before V2 "+uid;

		KatelloGpgKey key = new KatelloGpgKey(cli_worker, "katello-api-"+uid, this.org_name, "/tmp/"+KatelloGpgKey.FILE_GPG_GKHACHIK_KATELLO_API);
		SSHCommandResult res = key.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check  -return code");
		
		new KatelloProvider(this.cli_worker, providername, this.org_name, null, null).create();
		new KatelloProduct(this.cli_worker, productname, this.org_name, providername, null, null, null, null, null).create();
		res = new KatelloProduct(this.cli_worker, productname, this.org_name, 
				providername, null, null, null, null, null).update_gpgkey(key.name);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check  -return code");
		KatelloRepo _repo = new KatelloRepo(this.cli_worker, "pulp-v2", this.org_name, productname, url, null, null);
		res = _repo.discover(providername);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check  -return code");
		Assert.assertTrue(getOutput(_repo.custom_reposCount(null,null)).equals("8"), "Check - 8 repos were prepared");
		
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

		KatelloGpgKey key = new KatelloGpgKey(cli_worker, "katello-api-"+uid, this.org_name, "/tmp/"+KatelloGpgKey.FILE_GPG_GKHACHIK_KATELLO_API);
		exec_result = key.cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check  -return code");
		
		new KatelloProvider(this.cli_worker, providername, this.org_name, null, null).create();
		new KatelloProduct(this.cli_worker, productname, this.org_name, providername, null, null, null, null, null).create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check  -return code");
		KatelloRepo _repo = new KatelloRepo(this.cli_worker, "pulp-v2", this.org_name, productname, url, null, null);
		exec_result = _repo.discover(providername);
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check  -return code");
		Assert.assertTrue(getOutput(_repo.custom_reposCount(null,null)).equals("8"), "Check - 8 repos were prepared");
		
		assert_allReposGPGAssigned(this.org_name, productname, "");

		exec_result = new KatelloProduct(this.cli_worker, productname, this.org_name, 
				providername, null, null, null, null, null).update_gpgkey(key.name,true);
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check  -return code");

		assert_allReposGPGAssigned(this.org_name, productname, key.name);
	}

	/**
	 * @see https://github.com/gkhachik/katello-api/issues/405
	 */
	@Test(description="95fd7f1c-711d-4e47-a5fb-76cf04caeb71")
	public void test_listRedHatProductRepos(){
		exec_result = new KatelloOrg(this.cli_worker, this.orgWithManifest, null).cli_create();
		KatelloUtils.scpOnClient(cli_worker.getClientHostname(), "data/"+MANIFEST_MANIFEST_ZIP, "/tmp");
		exec_result = new KatelloProvider(this.cli_worker, KatelloProvider.PROVIDER_REDHAT, this.orgWithManifest, null, null).import_manifest("/tmp/"+MANIFEST_MANIFEST_ZIP, true);
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check  -return code");
		exec_result = new KatelloProduct(this.cli_worker, KatelloProduct.RHEL_SERVER, this.orgWithManifest, KatelloProvider.PROVIDER_REDHAT, null, null, null, null, null).repository_set_enable(
				KatelloProduct.REPOSET_RHEL6_RPMS);
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check  -return code");
		exec_result = new KatelloRepo(this.cli_worker, null, this.orgWithManifest, KatelloProduct.RHEL_SERVER, null, null, null).custom_reposCount(null, true);
		Assert.assertTrue(new Integer(getOutput(exec_result)).intValue()>0, "Check - repos count >0");
	}

	@Test(description="upload rmp to yum repository")
	public void test_uploadContentYum() {
		KatelloRepo repo = new KatelloRepo(cli_worker, repo_upload_yum, org_name, product_name, REPO_INECAS_ZOO3, null, null);
		exec_result = repo.content_upload("/tmp/"+rpm_pkg_name, "yum", null);
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (repo upload rpm)");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloRepo.OUT_CONTENT_UPLOADED, rpm_pkg_name)), "Check output (repo upload rpm)");
		KatelloPackage pkg = new KatelloPackage(cli_worker, org_name, product_name, repo_upload_yum, null);
		exec_result = pkg.cli_list();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (package list)");
		Assert.assertTrue(getOutput(exec_result).contains(rpm_pkg_name), "Check output (package list)");
	}

	@Test(description="upload puppet module to puppet repository")
	public void test_uploadContentPuppet() {
		KatelloRepo repo = new KatelloRepo(cli_worker, repo_upload_puppet, org_name, product_name, null, null, null);
		exec_result = repo.content_upload("/tmp/" + puppet_module_name, "puppet", null);
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (upload puppet module)");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloRepo.OUT_CONTENT_UPLOADED, puppet_module_name)), "Check output (upload puppet module)");
		KatelloPuppetModule module = new KatelloPuppetModule(cli_worker, org_name, repo_upload_puppet, product_name);
		exec_result = module.list();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (list modules)");
		Assert.assertTrue(getOutput(exec_result).contains(puppet_module_name.split("-")[0]), "Check output (list modules)");
	}

	@Test(description="try to upload wrong type of content to repository - check errors")
	public void test_uploadContentInvalidContent() {
		KatelloRepo repo = new KatelloRepo(cli_worker, repo_upload_yum, org_name, product_name, null, null, null);
		exec_result = repo.content_upload("/tmp/"+puppet_module_name, "puppet", null);
		Assert.assertTrue(exec_result.getExitCode()==65, "Check exit code (upload puppet to yum repo)");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloRepo.ERR_NOT_ACCEPT_PUPPET, repo_upload_yum)), "Check error (upload puppet to yum repo)");
		exec_result = repo.content_upload("/tmp/"+puppet_module_name, "yum", null);
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloRepo.ERR_INVALID_RPM, "/tmp/"+puppet_module_name)), "Check error (invlid rpm)");
		repo = new KatelloRepo(cli_worker, repo_upload_puppet, org_name, product_name, null, null, null);
		exec_result = repo.content_upload("/tmp/"+rpm_pkg_name, "puppet", null);
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloRepo.ERR_INVALID_MODULE, "/tmp/"+rpm_pkg_name)), "Check error (invlid puppet)");
		exec_result = repo.content_upload("/tmp/"+rpm_pkg_name, "yum", null);
		Assert.assertTrue(exec_result.getExitCode()==65, "Check exit code (upload rpm to puppet repo)");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloRepo.ERR_NOT_ACCEPT_YUM, repo_upload_puppet)), "Check error (upload puppet to yum repo)");
		exec_result = repo.content_upload("/tmp/"+rpm_pkg_name, "wrongtype", null);
		Assert.assertTrue(exec_result.getExitCode()==65, "Check exit code (upload wrong type)");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloRepo.ERR_INVALID_TYPE, "wrongtype")), "Check error (upload wrong type)");
	}

	/*
	 * https://github.com/gkhachik/katello-api/issues/418
	 */
	@Test(description="Repo list for Red Hat product should be displayed", dependsOnMethods={"test_listRedHatProductRepos"})
	public void test_listEnabledRepos(){
		String uid = KatelloUtils.getUniqueID();
		this.secondOrg = "anotherORg-"+uid;
		String manifest = "manifest-automation-CLI-2subscriptions.zip";
		//create second org
		exec_result = new KatelloOrg(this.cli_worker, this.secondOrg, null).cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check  -return code");
		//import another manifest for the second org
		KatelloUtils.scpOnClient(cli_worker.getClientHostname(), "data/"+manifest, "/tmp");
		exec_result = new KatelloProvider(this.cli_worker, KatelloProvider.PROVIDER_REDHAT, this.secondOrg, null, null).import_manifest("/tmp/"+manifest, true);
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check  -return code");
		//enable different set of repos for the two orgs
		exec_result = new KatelloProduct(this.cli_worker, KatelloProduct.RHEL_SERVER, this.secondOrg, KatelloProvider.PROVIDER_REDHAT, null, null, null, null, null).repository_set_enable("Red Hat Enterprise Linux 6 Server (ISOs)");
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check  -return code");
		//list repos - check count
		exec_result = new KatelloRepo(this.cli_worker, null, this.secondOrg, KatelloProduct.RHEL_SERVER, null, null, null).custom_reposCount(null, true);
		Assert.assertTrue(new Integer(getOutput(exec_result)).intValue()>0, "Check - repos count >0");
		exec_result = new KatelloRepo(this.cli_worker, null, this.orgWithManifest, KatelloProduct.RHEL_SERVER, null, null, null).custom_reposCount(null, true);
		Assert.assertTrue(new Integer(getOutput(exec_result)).intValue()>0, "Check - repos count >0");  
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
		
		KatelloRepo repo = new KatelloRepo(this.cli_worker, repo_name, org_name, product_name, PULP_RHEL6_x86_64_REPO, null, null);
		exec_result = repo.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloRepo.OUT_CREATE, repo_name)), "Check - output string (repo create)");
		
		return repo;
	}
	
	private void assert_allRepoPackagesSynced(String orgname, String productname, String envname, int repoCount){
		if(envname==null) envname = KatelloEnvironment.LIBRARY;
		KatelloRepo repo = new KatelloRepo(this.cli_worker, null, orgname, productname, null, null, null);
		SSHCommandResult res = repo.list(envname);
		int repoCnt = new Integer(getOutput(repo.custom_reposCount(envname,null))).intValue();
		Assert.assertTrue(repoCnt==repoCount, "Assert - all repos are promoted");
		String repoName; int pkgCount;
		for(int i=0;i<repoCnt;i++){
			repoName = KatelloUtils.grepCLIOutput("Name", getOutput(res), (i+1));
			pkgCount = new Integer(getOutput(new KatelloPackage(cli_worker, null, null, orgname, 
					productname, repoName, envname).custom_packagesCount(null))).intValue();
			Assert.assertTrue(pkgCount>0, "Check - packages are synced for: "+repoName);
		}
	}
	
	private void assert_allReposGPGAssigned(String orgname, String productname, String gpgName){
		String repoName; SSHCommandResult res;
		KatelloRepo repo = new KatelloRepo(this.cli_worker, null, orgname, productname, null, null, null);
		SSHCommandResult resRepoList = repo.list();
		int repoCount = new Integer(getOutput(repo.custom_reposCount(null,null))).intValue();
		for(int i=0;i<repoCount;i++){
			repoName = KatelloUtils.grepCLIOutput("Name", getOutput(resRepoList), (i+1));
			res = new KatelloRepo(this.cli_worker, repoName, orgname, productname, null, null, null).info();
			String __gpg = KatelloUtils.grepCLIOutput("GPG Key", getOutput(res));
			Assert.assertTrue(gpgName.equals(__gpg), "Check - gpg is assigned for: "+repoName);
		}
	}

	@AfterClass(description="remove the org(s) with manifests", alwaysRun=true)
	public void tearDown(){
		new KatelloOrg(this.cli_worker, this.orgWithManifest, null).delete(); // we don't care with the result.
		new KatelloOrg(cli_worker, org_name, null).delete();
		new KatelloOrg(cli_worker, this.secondOrg, null).delete();
	}
}
