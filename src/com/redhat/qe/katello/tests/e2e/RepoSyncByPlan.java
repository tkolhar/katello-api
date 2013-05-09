package com.redhat.qe.katello.tests.e2e;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloPackage;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.base.obj.KatelloSyncPlan;
import com.redhat.qe.katello.base.obj.KatelloSyncPlan.SyncPlanInterval;
import com.redhat.qe.katello.base.obj.KatelloUser;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups={"cfse-e2e"})
public class RepoSyncByPlan extends KatelloCliTestScript{
	protected static Logger log = Logger.getLogger(RepoSyncByPlan.class.getName());
	
	private SSHCommandResult exec_result;
	
	private String org_name;
	private String user_name;
	private String provider_name;
	private String product_name;
	private String repo_name;
	private String env_name;
	private String repo_path;
	private String repo_url;
	private String uid;
	private KatelloProduct prod; 
	private KatelloRepo repo;
	
	
	@Test(description="sync never synced repo by syncplan")
	public void test_syncNotSyncedRepo() {
		createZooRepo();
		
		KatelloSyncPlan sp = createSyncPlan(new Date(), SyncPlanInterval.hourly);
		KatelloPackage pack = new KatelloPackage(null, null, org_name, product_name, repo_name, null);
		
		prod.cli_set_plan(sp.name);
		
		DateFormat tformat = new SimpleDateFormat("HH:mm:ss");		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, 59);
		
		exec_result = repo.info();
		String lastSync = KatelloCli.grepCLIOutput("Last Sync", getOutput(exec_result).trim(),1);
		
		Assert.assertEquals(lastSync, "never", "Repo is synced, but should not");
		
		try {
		
			KatelloUtils.sshOnServer("date -s " + tformat.format(new Date(cal.getTimeInMillis())));
			
			waitfor_reposync(repo, lastSync, 3);
			
			exec_result = pack.cli_list();
			Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
			
			Assert.assertTrue(getOutput(exec_result).contains("lion"));
				
		} finally {
			cal = Calendar.getInstance();
			cal.add(Calendar.MINUTE, -59);
			KatelloUtils.sshOnServer("date -s " + tformat.format(new Date(cal.getTimeInMillis())));
		}
		
	}
	
	@Test(description="sync local repo by sync plan when packages are added in repo")
	public void test_syncLocalRepo() {
		createLocalRepo();
		
		KatelloSyncPlan sp = createSyncPlan(new Date(), SyncPlanInterval.hourly);
		KatelloPackage pack = new KatelloPackage(null, null, org_name, product_name, repo_name, null);
		
		prod.cli_set_plan(sp.name);
		
		DateFormat tformat = new SimpleDateFormat("HH:mm:ss");		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, 59);
		
		exec_result = repo.info();
		String lastSync = KatelloCli.grepCLIOutput("Last Sync", getOutput(exec_result).trim(),1);
		
		exec_result = pack.cli_list();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		Assert.assertTrue(getOutput(exec_result).contains("wolf"));
		Assert.assertTrue(getOutput(exec_result).contains("walrus"));
		Assert.assertFalse(getOutput(exec_result).contains("lion"));
		
		updateLocalRepo();
		
		
		try {
		
			KatelloUtils.sshOnServer("date -s " + tformat.format(new Date(cal.getTimeInMillis())));
			
			waitfor_reposync(repo, lastSync, 3);
			
			exec_result = pack.cli_list();
			Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
			
			Assert.assertTrue(getOutput(exec_result).contains("wolf"));
			Assert.assertFalse(getOutput(exec_result).contains("walrus"));
			Assert.assertTrue(getOutput(exec_result).contains("lion"));
				
		} finally {
			cal = Calendar.getInstance();
			cal.add(Calendar.MINUTE, -59);
			KatelloUtils.sshOnServer("date -s " + tformat.format(new Date(cal.getTimeInMillis())));
		}
		
		
	}
	
	private KatelloSyncPlan createSyncPlan(Date date, SyncPlanInterval interval) {

		String uid = KatelloUtils.getUniqueID();
		String spName = "splan" + uid;
		
		DateFormat dformat = new SimpleDateFormat("yyyy-MM-dd");
		DateFormat tformat = new SimpleDateFormat("HH:mm:ss");

		KatelloSyncPlan sp = new KatelloSyncPlan(spName, org_name, null, dformat.format(date), tformat.format(date), interval);
		exec_result = sp.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		return sp;
	}
	
	private void createZooRepo() {
		uid = KatelloUtils.getUniqueID();
		org_name = "org"+uid;
		user_name = "user"+uid;
		provider_name = "provider"+uid;
		product_name = "product"+uid;
		repo_name = "repo"+uid;
		env_name = "env"+uid;
		
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
		prod = new KatelloProduct(product_name, org_name, provider_name, null, null, null, null, null);
		exec_result = prod.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	
		repo = new KatelloRepo(repo_name, org_name, product_name, REPO_INECAS_ZOO3, null, null);
		exec_result = repo.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		KatelloEnvironment env = new KatelloEnvironment(env_name, null, org_name, KatelloEnvironment.LIBRARY);
		exec_result = env.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");		
	}

	/**
	 * Creates local repo which packages are from REPO_INECAS_ZOO3.
	 */
	private void createLocalRepo() {
		uid = KatelloUtils.getUniqueID();
		org_name = "org"+uid;
		user_name = "user"+uid;
		provider_name = "provider"+uid;
		product_name = "product"+uid;
		repo_name = "repo"+uid;
		env_name = "env"+uid;	
		repo_path = "/var/www/html/"+uid;
		repo_url = "http://localhost/" + uid;
		
		KatelloUtils.sshOnServer("yum -y install createrepo");
		KatelloUtils.sshOnServer("mkdir /tmp/"+uid);
		KatelloUtils.sshOnServer("createrepo " + repo_path);
		/**KatelloUtils.sshOnServer("touch /etc/yum.repos.d/" + uid + ".repo");
		KatelloUtils.sshOnServer("echo \"[localrepo]\" >> /etc/yum.repos.d/" + uid + ".repo");
		KatelloUtils.sshOnServer("echo \"name=Fedora Core $releasever - My Local Repo\" >> /etc/yum.repos.d/" + uid + ".repo");
		KatelloUtils.sshOnServer("echo \"baseurl=file:///tmp/\" " + uid + " >> /etc/yum.repos.d/" + uid + ".repo");
		KatelloUtils.sshOnServer("echo \"enabled=1\" >> /etc/yum.repos.d/" + uid + ".repo");
		KatelloUtils.sshOnServer("echo \"gpgcheck=0\" >> /etc/yum.repos.d/" + uid + ".repo");**/
		KatelloUtils.sshOnServer("wget " + REPO_INECAS_ZOO3 + "wolf-9.4-2.noarch.rpm -P "+repo_path);
		KatelloUtils.sshOnServer("wget " + REPO_INECAS_ZOO3 + "walrus-0.71-1.noarch.rpm -P "+repo_path);
		KatelloUtils.sshOnServer("createrepo "+repo_path);
		
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
		prod = new KatelloProduct(product_name, org_name, provider_name, null, null, null, null, null);
		exec_result = prod.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	
		repo = new KatelloRepo(repo_name, org_name, product_name, repo_url, null, null);
		exec_result = repo.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		KatelloEnvironment env = new KatelloEnvironment(env_name, null, org_name, KatelloEnvironment.LIBRARY);
		exec_result = env.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		KatelloUtils.promoteProductToEnvironment(org_name, product_name, env_name);
	}
	
	private void updateLocalRepo() {
		KatelloUtils.sshOnServer("wget " + REPO_INECAS_ZOO3 + "lion-0.4-1.noarch.rpm  -P "+repo_path);
		KatelloUtils.sshOnServer("rm " + repo_path + "/walrus-0.71-1.noarch.rpm -f");
		KatelloUtils.sshOnServer("createrepo "+repo_path);
	}
	
}
