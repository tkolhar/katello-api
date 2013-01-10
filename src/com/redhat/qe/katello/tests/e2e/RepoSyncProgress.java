package com.redhat.qe.katello.tests.e2e;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.obj.KatelloChangeset;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.base.obj.KatelloSyncPlan;
import com.redhat.qe.katello.base.obj.KatelloSyncPlan.SyncPlanInterval;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

/**
 * Implementation of following E2E scenario:<BR>
 * <pre>
 * Description:
 *     Use rpm-s from: AEOLUS_F17_REPO {@link #AEOLUS_F17_REPO}
 *     following command returns list of all rpms:
 *         https://gist.github.com/759f71808686aa4937e1
 *     
 *     prepare a repo (locally via: http://localhost/pub/aeolus-f17) and put some context there.
 *     sync that repo in Katello
 *     make a sync plan (hourly interval) to be started in the future
 *     register/consume a system
 *     assign that plan to the product
 *     add more rpm-s in the repo: /var/www/html/pub/aeolus-f17
 *     change system date: date +%T -s "HH:MM:SS" #// to a reasonable time
 *     wait until scheduled sync happens first time
 *     check packages count
 *     add more packages
 *     change servers time (like 98 min passed)
 *     wait until second sync is done
 *     check packages count
 * </pre>
 * @author gkhachik
 * @since 07.Nov.2012 
 */
@Test(groups={"cfse-e2e"})
public class RepoSyncProgress extends KatelloCliTestScript{
	protected static Logger log = Logger.getLogger(RepoSyncProgress.class.getName());

	private String orgName;
	private String envTesting;
	private String productName;
	private String repoName;
	private String[] rpms;
	private String uid;
	private String systemName;
	private String syncPlanName;
	private long syncDate;
	
	public static final String AEOLUS_F17_REPO = "http://repos.fedorapeople.org/repos/aeolus/conductor/latest-release/fedora-17/x86_64/";

	@BeforeClass(description="Init unique names", alwaysRun=true)
	public void setUp(){
		SSHCommandResult res;
		uid = KatelloUtils.getUniqueID();
		this.envTesting = "Testing";
		this.orgName = "Syncing Aeolus "+uid;
		this.productName = "Aeolus "+uid;
		this.repoName = "aeolus-f17-rhel6-x86_64-"+uid;
		this.systemName = System.getProperty("katello.server.hostname","localhost")+"-"+uid;
		
		res = KatelloUtils.sshOnServer("hwclock --hctosys"); // do make a sync to hardware clock before the scenario start
		
		KatelloOrg org = new KatelloOrg(this.orgName, null);
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: org.create");
		res = new KatelloEnvironment(envTesting, null, orgName, KatelloEnvironment.LIBRARY).cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: environment.create");
		String out = getOutput(KatelloUtils.sshOnClient(
				"curl -sk "+AEOLUS_F17_REPO+" | grep -oE \"^<a href=\\\".*.rpm\" | cut -d\\\" -f2"));
		rpms = out.trim().split("\n");
	}

	@Test(description="create Katello objects: prov, prod, repo")
	public void test_prepareRepo(){
		SSHCommandResult res;
		res = new KatelloProvider(productName,orgName,null,null).create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: provider.create");
		res = new KatelloProduct(productName, orgName, productName, null, null, null, null, null).create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: product.create");
		res = new KatelloRepo(repoName, orgName, productName, 
				"http://"+System.getProperty("katello.server.hostname","localhost")+"/pub/aeolus-f17", 
				null, null).create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: repo.create");
	}
	
	@Test(description="first version of local createrepo", dependsOnMethods={"test_prepareRepo"})
	public void test_createrepo1(){
		SSHCommandResult res;
		Assert.assertTrue(rpms.length>5, "Check - at least 5 rpms are fetched");
		res = KatelloUtils.sshOnServer("rm -rf /var/www/html/pub/aeolus-f17/ && mkdir /var/www/html/pub/aeolus-f17/");
		Assert.assertTrue(res.getExitCode().intValue()==0, "directory should be created");
		KatelloUtils.sshOnServer("wget "+AEOLUS_F17_REPO+rpms[0]+" -O /var/www/html/pub/aeolus-f17/"+rpms[0]);
		KatelloUtils.sshOnServer("wget "+AEOLUS_F17_REPO+rpms[1]+" -O /var/www/html/pub/aeolus-f17/"+rpms[1]);
		res = KatelloUtils.sshOnServer("ls /var/www/html/pub/aeolus-f17/*.rpm | wc -l");
		Assert.assertTrue(getOutput(res).equals("2"), "Check - packages downloaded");
		res = KatelloUtils.sshOnServer("createrepo /var/www/html/pub/aeolus-f17/");
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: $? 0");
	}
	
	@Test(description="first repo sync: without sync_plan", dependsOnMethods={"test_createrepo1"})
	public void test_syncRepoRound1(){
		SSHCommandResult res;
		res = new KatelloRepo(repoName, orgName, productName, 
				"http://"+System.getProperty("katello.server.hostname","localhost")+"/pub/aeolus-f17", 
				null, null).synchronize();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: repo.synchronize");
		KatelloChangeset cs1 = new KatelloChangeset("csRound1-"+uid, orgName, envTesting);
		res = cs1.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: changeset.create");
		res = cs1.update_addProduct(productName);
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: changeset.update-add_product");
		res = cs1.promote();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: changeset.promote");
	}
	
	@Test(description="subscribe and check packages", dependsOnMethods={"test_syncRepoRound1"})
	public void test_subscribeToRepo(){
		SSHCommandResult res;
		
		rhsm_clean();
		res = rhsm_register(orgName, envTesting, systemName, false);
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: rhsm.register");
		KatelloSystem sys = new KatelloSystem(systemName, orgName, envTesting, null, null, null, null, null, null);
		res = sys.subscriptions_available();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: system.subscriptions-available");
		String poolId = KatelloCli.grepCLIOutput("ID", getOutput(res));
		Assert.assertTrue(poolId!=null, "Check poolID is returned");
		res = sys.rhsm_subscribe(poolId);
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: rhsm.subscribe");
	}
	
	@Test(description="list packages count via `yum list available` in the subscribed repo - 2",
			dependsOnMethods={"test_subscribeToRepo"})
	public void test_packagesCountRound1(){
		SSHCommandResult res;
		
		KatelloUtils.sshOnClient("yum clean all");
		res = KatelloUtils.sshOnClient(String.format(
				"yum list available --disablerepo \\* --enablerepo \\*%s\\* | grep \"%s\" | wc -l",
				repoName,repoName)); // disablerepo just for speed up the yum list 
		Assert.assertTrue(getOutput(res).equals("2"),"Check yum list alvailable returns 2 packages");
	}
	
	@Test(description="second round: add more packages. not sync yet", 
			dependsOnMethods={"test_packagesCountRound1"})
	public void test_addMorePackages(){
		SSHCommandResult res;
		KatelloUtils.sshOnServer("wget "+AEOLUS_F17_REPO+rpms[2]+" -O /var/www/html/pub/aeolus-f17/"+rpms[2]);
		KatelloUtils.sshOnServer("wget "+AEOLUS_F17_REPO+rpms[3]+" -O /var/www/html/pub/aeolus-f17/"+rpms[3]);
		res = KatelloUtils.sshOnServer("ls /var/www/html/pub/aeolus-f17/*.rpm | wc -l");
		Assert.assertTrue(getOutput(res).equals("4"), "Check - packages downloaded");
		res = KatelloUtils.sshOnServer("createrepo /var/www/html/pub/aeolus-f17/");
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: $? 0");
	}
	
	@Test(description="create sync plan, assign to product, wait to become synced",
			dependsOnMethods={"test_addMorePackages"})
	public void test_makeSyncPlanWaitForSync(){
		SSHCommandResult res;
		syncPlanName = "hourly-Aeolus-sync-"+uid;
		DateFormat dformat = new SimpleDateFormat("yyyy-MM-dd");
		DateFormat tformat = new SimpleDateFormat("HH:mm:ss");
		
		
		long now = Calendar.getInstance().getTimeInMillis();
		syncDate = Calendar.getInstance().getTimeInMillis() + 60000;
		
		// date +%T -s "HH:MM:SS"
		KatelloUtils.sshOnServer("date +%T -s \""+tformat.format(new Date(now))+"\""); // < --- SET server's date/time !!!
		res = KatelloUtils.sshOnServer("date");
		log.info(String.format("Date on server side now is set: [%s]",getOutput(res)));

		KatelloSyncPlan sync = new KatelloSyncPlan(syncPlanName, orgName, null, 
				dformat.format(Calendar.getInstance().getTime()), 
				tformat.format(new Date(syncDate)), 
				SyncPlanInterval.hourly);
		res = sync.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: sync_plan.create");
		res = new KatelloProduct(productName, orgName, null, null, null, null, null, null).cli_set_plan(syncPlanName);
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: product.set_plan");
		log.info("Sleep 2min and wakeup having product synced with 4 packages :)");
		try{Thread.sleep(120000);}catch(InterruptedException iex){};
		res = KatelloUtils.sshOnServer("hwclock --hctosys"); // // < --- UNSET back server's date/time !!!
		
		KatelloChangeset cs1 = new KatelloChangeset("csRound2-"+uid, orgName, envTesting);
		res = cs1.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: changeset.create");
		res = cs1.update_addProduct(productName);
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: changeset.update-add_product");
		res = cs1.promote();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: changeset.promote");
	}
	
	@Test(description="list packages count via `yum list available` in the subscribed repo - 4",
			dependsOnMethods={"test_makeSyncPlanWaitForSync"})
	public void test_packagesCountRound2(){
		SSHCommandResult res;
		
		KatelloUtils.sshOnClient("yum clean all");
		res = KatelloUtils.sshOnClient(String.format(
				"yum list available --disablerepo \\* --enablerepo \\*%s\\* | grep \"%s\" | wc -l",
				repoName,repoName)); // disablerepo just for speed up the yum list 
		Assert.assertTrue(getOutput(res).equals("4"),"Check yum list alvailable returns 4 packages");
	}
	
	@Test(description="add third package portion to be synced by schedule. simulate that 1 hour passed.",
			dependsOnMethods={"test_packagesCountRound2"})
	public void test_addThirdPackagePortion(){
		SSHCommandResult res;
		KatelloUtils.sshOnServer("wget "+AEOLUS_F17_REPO+rpms[4]+" -O /var/www/html/pub/aeolus-f17/"+rpms[4]);
		KatelloUtils.sshOnServer("wget "+AEOLUS_F17_REPO+rpms[5]+" -O /var/www/html/pub/aeolus-f17/"+rpms[5]);
		res = KatelloUtils.sshOnServer("ls /var/www/html/pub/aeolus-f17/*.rpm | wc -l");
		Assert.assertTrue(getOutput(res).equals("6"), "Check - packages downloaded");
		res = KatelloUtils.sshOnServer("createrepo /var/www/html/pub/aeolus-f17/");
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: $? 0");
	}
	
	@Test(description="make the scheduled sync plan to do another round.",
			dependsOnMethods={"test_addThirdPackagePortion"})
	public void test_makeSyncPlanOneHourPassed(){
		SSHCommandResult res;
		
		DateFormat tformat = new SimpleDateFormat("HH:mm:ss");
		long makeSystemToDate = syncDate + (59 * 60000); // like 58 min already passed.
		
		// date +%T -s "HH:MM:SS"
		KatelloUtils.sshOnServer("date +%T -s \""+tformat.format(new Date(makeSystemToDate))+"\""); // < --- SET server's date/time !!!
		res = KatelloUtils.sshOnServer("date");
		log.info(String.format("Simulate like 59 min passed and set: [%s]",getOutput(res)));
		log.info("Sleep 2min and wakeup having product synced with 6 packages :)");
		try{Thread.sleep(120000);}catch(InterruptedException iex){};
		res = KatelloUtils.sshOnServer("hwclock --hctosys"); // // < --- UNSET back server's date/time !!!
		
		KatelloChangeset cs1 = new KatelloChangeset("csRound3-"+uid, orgName, envTesting);
		res = cs1.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: changeset.create");
		res = cs1.update_addProduct(productName);
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: changeset.update-add_product");
		res = cs1.promote();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: changeset.promote");
	}

	@Test(description="list packages count via `yum list available` in the subscribed repo - 6",
			dependsOnMethods={"test_makeSyncPlanOneHourPassed"})
	public void test_packagesCountRound3(){
		SSHCommandResult res;
		
		KatelloUtils.sshOnClient("yum clean all");
		res = KatelloUtils.sshOnClient(String.format(
				"yum list available --disablerepo \\* --enablerepo \\*%s\\* | grep \"%s\" | wc -l",
				repoName,repoName)); // disablerepo just for speed up the yum list 
		Assert.assertTrue(getOutput(res).equals("6"),"Check yum list alvailable returns 4 packages");
	}
	
	@AfterClass(description="cleanup the stuff", alwaysRun=true)
	public void tearDown(){
		rhsm_clean();
		SSHCommandResult res = KatelloUtils.sshOnServer("hwclock --hctosys"); // sync back again - for any case.
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check restored system date to hardware clock");
		res = new KatelloOrg(this.orgName, null).delete();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: org.delete");
	}
}
