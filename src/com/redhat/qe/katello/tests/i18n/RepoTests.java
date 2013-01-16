package com.redhat.qe.katello.tests.i18n;

import java.util.logging.Logger;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.obj.KatelloFilter;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.base.obj.KatelloUser;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

public class RepoTests extends KatelloCliTestScript {

	protected static Logger log = Logger
			.getLogger(RepoTests.class.getName());

	private SSHCommandResult exec_result;

	private String org_name;
	private String user_name;
	private String provider_name;
	private String product_name;
	private String repo_name;
	private String repo_name2;
	private String repo_id2;
	private String filter_name;
	
	@BeforeClass(description = "Generate unique objects", groups={"i18n-init"})
	public void setUp() {
		String uid = KatelloUtils.getUniqueID();
		org_name = "org" + uid;
		user_name = "user" + uid;
		provider_name = "provider" + uid;
		product_name = "product" + uid;
		filter_name = "filter"+uid;
		
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
				"Package provider", null);
		exec_result = prov.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		// Create product:
		KatelloProduct prod = new KatelloProduct(product_name, org_name,
				provider_name, null, null, null, null, null);
		exec_result = prod.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		
		KatelloFilter filter1 = new KatelloFilter(filter_name, org_name, null, null);
		exec_result = filter1.create();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code (filter create)");
	}

	@Test(description = "Create repo")
	public void test_createRepo() {
		repo_name = getText("repo.create.name") + KatelloUtils.getUniqueID();
		
		KatelloRepo repo = new KatelloRepo(repo_name, org_name, product_name, PULP_RHEL6_x86_64_REPO, null, null);
		exec_result = repo.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).equals(getText("repo.create.stdout", repo_name)), "Check - output string (repo create)");
	}
	
	@Test(description = "Create repo exists", dependsOnMethods = {"test_createRepo"})
	public void test_createRepoExists() {

		KatelloRepo repo = new KatelloRepo(repo_name, org_name, product_name, PULP_RHEL6_x86_64_REPO, null, null);
		
		exec_result = repo.create();
		Assert.assertTrue(exec_result.getExitCode() == 153, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), getText("repo.create.stderror.exists", repo.name, repo.product));
	}
	
	@Test(description = "Discover repo", dependsOnMethods = {"test_createRepoExists"})
	public void test_discoverRepo() {

		repo_name2 = getText("repo.create.name") + KatelloUtils.getUniqueID();
		String url_name = PULP_RHEL6_x86_64_REPO.replace("http://repos.fedorapeople.org", "").replace("/", "_");
		url_name = url_name.substring(0, url_name.length()-1);
		KatelloRepo repo = new KatelloRepo(repo_name2, org_name, product_name, PULP_RHEL6_x86_64_REPO, null, null);
		exec_result = repo.discover();
		repo_name2 += url_name;
		repo.name = repo_name2;
		repo.url = repo.url.substring(0, repo.url.length() - 1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(getText("repo.discover.stdout", repo_name2)), "Check - output string (repo discover)");
		
		exec_result = repo.info();
		repo_id2 = KatelloCli.grepCLIOutput("ID", getOutput(exec_result));
	}
	
	@Test(description = "List repos", dependsOnMethods = {"test_discoverRepo", "test_createRepo"})
	public void test_listRepo() {
		KatelloRepo repo = new KatelloRepo(repo_name, org_name, product_name, PULP_RHEL6_x86_64_REPO, null, null);
		
		exec_result = repo.list();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", " ").contains(repo_name));
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", " ").contains(repo_name2));
	}
	
	@Test(description = "Synchronize repository", dependsOnMethods = {"test_createRepo"})
	public void test_syncRepo() {
		
		KatelloRepo repo = new KatelloRepo(repo_name, org_name, product_name, PULP_RHEL6_x86_64_REPO, null, null);
		
		exec_result = repo.synchronize();
		
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).trim().contains(getText("repo.synchronize.stdout", repo.name)));
	}
	
	// @ TODO BZ 869933
	@Test(description = "Try to enable/disable custom repo, check error", dependsOnMethods = {"test_createRepo"})
	public void test_enableDisableRepo() {

		KatelloRepo repo = new KatelloRepo(repo_name, org_name, product_name, PULP_RHEL6_x86_64_REPO, null, null);
		
		exec_result = repo.enable();
		Assert.assertTrue(exec_result.getExitCode() == 148, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), getText("repo.disable.enable.stderror"));
		
		exec_result = repo.disable();
		Assert.assertTrue(exec_result.getExitCode() == 148, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), getText("repo.disable.enable.stderror"));
	}
	
	@Test(description = "Add filter to repo", dependsOnMethods = {"test_createRepo"})
	public void test_addFilter() {

		KatelloRepo repo = new KatelloRepo(repo_name, org_name, product_name, PULP_RHEL6_x86_64_REPO, null, null);
		
		exec_result = repo.add_filter(filter_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), getText("repo.add.filter.stdout", filter_name, repo.name));
	}
	
	@Test(description = "Remove filter from repo", dependsOnMethods = {"test_createRepo", "test_addFilter"})
	public void test_removeFilter() {

		KatelloRepo repo = new KatelloRepo(repo_name, org_name, product_name, PULP_RHEL6_x86_64_REPO, null, null);
			
		exec_result = repo.remove_filter(filter_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), getText("repo.remove.filter.stdout", filter_name, repo.name));		
	}

	
	@Test(description = "Delete repo", dependsOnMethods = {"test_discoverRepo", "test_listRepo"})
	public void test_deleteRepo() {

		KatelloRepo repo = new KatelloRepo(repo_name2, org_name, product_name, PULP_RHEL6_x86_64_REPO, null, null);
		
		exec_result = repo.delete();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), getText("repo.delete.stdout", repo_id2));
		
		exec_result = repo.info();
		Assert.assertTrue(exec_result.getExitCode() == 65, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), getText("repo.info.stderror.notfound", repo.name, repo.org, repo.product, "Library"));
	}
}
