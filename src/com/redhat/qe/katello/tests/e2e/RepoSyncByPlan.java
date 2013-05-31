package com.redhat.qe.katello.tests.e2e;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;
import org.testng.annotations.Test;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloPackage;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.base.obj.KatelloSyncPlan;
import com.redhat.qe.katello.base.obj.KatelloSyncPlan.SyncPlanInterval;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

//TODO [gkhachik] - I am giving up here for now: too hard for debugging to see why the sync plan not works as expected.

@Test(groups={"cfse-e2e"})
public class RepoSyncByPlan extends KatelloCliTestScript{
	protected static Logger log = Logger.getLogger(RepoSyncByPlan.class.getName());
	
	private SSHCommandResult exec_result;
	
	private String org_name;
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
		String lastSync = KatelloUtils.grepCLIOutput("Last Sync", getOutput(exec_result).trim(),1);
		Assert.assertEquals(lastSync, "never", "Repo is synced, but should not");
		
		try {
			KatelloUtils.sshOnServer("date -s " + tformat.format(new Date(cal.getTimeInMillis())));
			waitfor_reposync(repo, lastSync, 3);
			exec_result = pack.cli_list();
			Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
			Assert.assertTrue(getOutput(exec_result).contains("lion"));
		} finally {
			KatelloUtils.sshOnServer("hwclock --hctosys");
		}
	}
	
	@Test(description="sync local repo by sync plan when packages are added in repo")
	public void test_syncLocalRepo() {
		try{
			createLocalRepo();

			KatelloPackage pack = new KatelloPackage(null, null, org_name, product_name, repo_name, null);

			syncRepoBySyncPlanNow(1);

			exec_result = pack.cli_list();
			Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
			Assert.assertTrue(getOutput(exec_result).contains("wolf"));
			Assert.assertTrue(getOutput(exec_result).contains("walrus"));
			Assert.assertFalse(getOutput(exec_result).contains("lion"));

			updateLocalRepo();
			syncRepoBySyncPlanNow(2);
			
			exec_result = pack.cli_list();
			Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

			Assert.assertTrue(getOutput(exec_result).contains("wolf"));
			Assert.assertFalse(getOutput(exec_result).contains("walrus"));
			Assert.assertTrue(getOutput(exec_result).contains("lion"));
		}finally{
			KatelloUtils.sshOnServer("hwclock --hctosys");
		}
	}
	
	private KatelloSyncPlan createSyncPlan(Date date, SyncPlanInterval interval) {

		String uid = KatelloUtils.getUniqueID();
		String spName = "syncPlan-"+uid;
		
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
		provider_name = "provider"+uid;
		product_name = "product"+uid;
		repo_name = "repo"+uid;
		env_name = "env"+uid;
		
		// Create org:
		KatelloOrg org = new KatelloOrg(this.org_name,"Package tests");
		exec_result = org.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		// Create provider:
		KatelloProvider prov = new KatelloProvider(provider_name, org_name, null, null);
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
		provider_name = "provider"+uid;
		product_name = "product"+uid;
		repo_name = "repo"+uid;
		env_name = "env"+uid;	
		repo_path = "/var/www/html/"+uid;
		repo_url = "http://localhost/" + uid;
		
		String shCmd = "rpm -q createrepo || yum -y install createrepo; ";
		shCmd += "mkdir /tmp/"+uid+"; ";
		shCmd += "wget " + REPO_INECAS_ZOO3 + "wolf-9.4-2.noarch.rpm -P "+repo_path+"; ";
		shCmd += "wget " + REPO_INECAS_ZOO3 + "walrus-0.71-1.noarch.rpm -P "+repo_path+";";
		shCmd += "createrepo "+ repo_path+";";
		KatelloUtils.sshOnServer(shCmd);
		
		// Create org:
		KatelloOrg org = new KatelloOrg(this.org_name,null);
		exec_result = org.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		// Create provider:
		KatelloProvider prov = new KatelloProvider(provider_name, org_name, null, null);
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
		String shCmd = "rm -f " + repo_path + "/walrus-0.71-1.noarch.rpm && ";
		shCmd += "createrepo "+repo_path+";";
		KatelloUtils.sshOnServer(shCmd);
	}
	
	private void syncRepoBySyncPlanNow(int hoursAhead){
		KatelloSyncPlan sp = createSyncPlan(new Date(), SyncPlanInterval.hourly);
		prod.cli_set_plan(sp.name);
		
		DateFormat tformat = new SimpleDateFormat("HH:mm:ss");		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, (hoursAhead*60 - 1));
		
		exec_result = repo.info();
		String lastSync = KatelloUtils.grepCLIOutput("Last Sync", getOutput(exec_result).trim(),1);
		
		try {
			KatelloUtils.sshOnServer("date -s " + tformat.format(new Date(cal.getTimeInMillis())));
			waitfor_reposync(repo, lastSync, 3);
		} finally {
			KatelloUtils.sshOnServer("hwclock --hctosys");
		}
	}
}
