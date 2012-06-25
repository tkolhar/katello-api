package com.redhat.qe.katello.tests.e2e;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

import org.testng.annotations.BeforeClass;
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
import com.redhat.qe.katello.base.obj.KatelloSyncPlan;
import com.redhat.qe.katello.base.obj.KatelloSyncPlan.SyncPlanInterval;
import com.redhat.qe.katello.base.obj.KatelloUser;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.tasks.KatelloTasks;
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
	private String changeset_name;
	private KatelloProduct prod; 
	private KatelloRepo repo;
	
	
	@BeforeClass(description="Generate unique objects")
	public void setUp() {
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
		
		KatelloChangeset cs = new KatelloChangeset(changeset_name, org_name, env_name);
		exec_result = cs.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

	}
	
	
	@Test(description="sync never synced repo by syncplan")
	public void test_syncNotSyncedRepo() {
		
		KatelloSyncPlan sp = createSyncPlan(new Date(), SyncPlanInterval.hourly);
		KatelloPackage pack = new KatelloPackage(null, null, org_name, product_name, repo_name, null);
		
		prod.cli_set_plan(sp.name);
		
		DateFormat tformat = new SimpleDateFormat("hh:mm:ss");		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, 59);
		
		exec_result = repo.info();
		String lastSync = KatelloTasks.grepCLIOutput("Last Sync", getOutput(exec_result).trim(),1);
		
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
	
	private KatelloSyncPlan createSyncPlan(Date date, SyncPlanInterval interval) {

		String uid = KatelloTestScript.getUniqueID();
		String spName = "splan" + uid;
		
		DateFormat dformat = new SimpleDateFormat("yyyy-MM-dd");
		DateFormat tformat = new SimpleDateFormat("hh:mm:ss");

		KatelloSyncPlan sp = new KatelloSyncPlan(spName, org_name, null, dformat.format(date), tformat.format(date), interval);
		exec_result = sp.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		return sp;
	}
	

	
}
