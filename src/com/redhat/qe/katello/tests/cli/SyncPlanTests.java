package com.redhat.qe.katello.tests.cli;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloSyncPlan;
import com.redhat.qe.katello.base.obj.KatelloSyncPlan.SyncPlanInterval;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups = { "cfse-cli" })
public class SyncPlanTests extends KatelloCliTestScript {

	protected static Logger log = Logger
			.getLogger(PackageTests.class.getName());

	private SSHCommandResult exec_result;

	private String org_name;
	private String syncplan_name;

	
	@BeforeClass(description = "Generate unique objects")
	public void setUp() {
		String uid = KatelloUtils.getUniqueID();
		org_name = "org" + uid;

		// Create org:
		KatelloOrg org = new KatelloOrg(this.org_name, "Package tests");
		exec_result = org.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

	}
	
	@Test(description = "Create sync plan", groups = { "cli-sync_plan" })
	public void test_createSyncPlan() {

		KatelloSyncPlan sp = createSyncPlan(new Date(), SyncPlanInterval.hourly);
		
		assert_syncplanInfo(sp);
		assert_syncplanList(Arrays.asList(sp), new ArrayList<KatelloSyncPlan>());
	}

	@Test(description = "Create sync plan which name exists", groups = { "cli-sync_plan" })
	public void test_createSyncPlanExists() {

		KatelloSyncPlan sp = createSyncPlan(new Date(), SyncPlanInterval.hourly);
		
		exec_result = sp.create();
		Assert.assertTrue(exec_result.getExitCode() == 144, "Check - return code (sync plan create)");
		Assert.assertEquals(getOutput(exec_result).trim(), "Validation failed: Name has already been taken");
	}

	@Test(description = "Create sync plan which date is wrong", groups = { "cli-sync_plan" })
	public void test_createSyncPlanWrongDate() {

		String uid = KatelloUtils.getUniqueID();
		syncplan_name = "splan" + uid;
		
		DateFormat tformat = new SimpleDateFormat("HH:mm:ss");

		KatelloSyncPlan sp = new KatelloSyncPlan(syncplan_name, org_name, null, "2012-11", tformat.format(new Date()), SyncPlanInterval.hourly);
		exec_result = sp.create();
		Assert.assertTrue(exec_result.getExitCode() == 65, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), "Date format is invalid. Required: YYYY-MM-DD");
	}

	@Test(description = "Create sync plan which time is wrong", groups = { "cli-sync_plan" })
	public void test_createSyncPlanWrongTime() {

		String uid = KatelloUtils.getUniqueID();
		syncplan_name = "splan" + uid;
		
		DateFormat dformat = new SimpleDateFormat("yyyy-MM-dd");

		KatelloSyncPlan sp = new KatelloSyncPlan(syncplan_name, org_name, null, dformat.format(new Date()), "11:77:88", SyncPlanInterval.hourly);
		exec_result = sp.create();
		Assert.assertTrue(exec_result.getExitCode() == 65, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), "Time format is invalid. Required: HH:MM:SS[+HH:MM]");
	}

	@Test(description = "Create sync plan update it's name", groups = { "cli-sync_plan" })
	public void test_updateSyncPlanName() {
		KatelloSyncPlan sp = createSyncPlan(new Date(), SyncPlanInterval.hourly);
		
		String oldName = sp.name;
		String newName = sp.name + "new";
		
		exec_result = sp.update_name(newName);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		sp.name = newName;
		// @ TODO remove this line when bug 837000 is fixed 
		sp.interval = SyncPlanInterval.none.toString();
		assert_syncplanInfo(sp);
		
		sp.name = oldName;
		exec_result = sp.info();
		Assert.assertTrue(exec_result.getExitCode() == 65, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), String.format(KatelloSyncPlan.ERR_NOT_FOUND, sp.name));
	}

	@Test(description = "Create sync plan update it's date", groups = { "cli-sync_plan" })
	public void test_updateSyncPlanDate() {
		KatelloSyncPlan sp = createSyncPlan(new Date(), SyncPlanInterval.hourly);
		
		DateFormat dformat = new SimpleDateFormat("yyyy-MM-dd");
		DateFormat tformat = new SimpleDateFormat("HH:mm:ss");
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, 1);
		exec_result = sp.update_date(dformat.format(cal.getTime()), tformat.format(cal.getTime()));
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		sp.date = dformat.format(cal.getTime());
		sp.time = tformat.format(cal.getTime());
		// @ TODO remove this line when bug 837000 is fixed
		sp.interval = SyncPlanInterval.none.toString();
		assert_syncplanInfo(sp);
	}
	
	@Test(description = "Create 2 sync plans, delete one of them", groups = { "cli-sync_plan" })
	public void test_deleteSyncplan() {
		KatelloSyncPlan sp = createSyncPlan(new Date(), SyncPlanInterval.hourly);
		KatelloSyncPlan sp2 = createSyncPlan(new Date(), SyncPlanInterval.daily);

		exec_result = sp.delete();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (changeset delete)");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloSyncPlan.OUT_DELETE, sp.name)), "Check - output string (sync plan delete)");
		
		assert_syncplanList(Arrays.asList(sp2), Arrays.asList(sp));
		
		exec_result = sp.delete();
		Assert.assertTrue(exec_result.getExitCode() == 65, "Check - return code (sync plan delete)");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloSyncPlan.ERR_NOT_FOUND, sp.name)), "Check - output string (sync plan not exists)");
	}
	
	private KatelloSyncPlan createSyncPlan(Date date, SyncPlanInterval interval) {

		String uid = KatelloUtils.getUniqueID();
		syncplan_name = "splan" + uid;
		
		DateFormat dformat = new SimpleDateFormat("yyyy-MM-dd");
		DateFormat tformat = new SimpleDateFormat("HH:mm:ss");

		KatelloSyncPlan sp = new KatelloSyncPlan(syncplan_name, org_name, null, dformat.format(date), tformat.format(date), interval);
		exec_result = sp.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		return sp;
	}
	
	private void assert_syncplanList(List<KatelloSyncPlan> splans, List<KatelloSyncPlan> excludeSplans) {

		exec_result = new KatelloSyncPlan(null, org_name, null, null, null, null).list();

		//sync plans that exist in list
		for(KatelloSyncPlan sp : splans) {
			if (sp.description == null) sp.description = "None";
			String match_info = String.format(KatelloSyncPlan.REG_SYNCPLAN_LIST, sp.name, sp.description, sp.date.replaceAll("-", "/") + " " + sp.time, sp.interval).replaceAll("\"", "");
			Assert.assertTrue(getOutput(exec_result).replaceAll("\n", " ").matches(match_info), String.format("Sync Plan [%s] should be found in the result list", sp.name));
		}
		
		//sync plans that should not exist in list
		for(KatelloSyncPlan sp : excludeSplans) {			
			if (sp.description == null) sp.description = "None";
			String match_info = String.format(KatelloSyncPlan.REG_SYNCPLAN_LIST, sp.name, sp.description, sp.date.replaceAll("-", "/") + " " + sp.time, sp.interval).replaceAll("\"", "");
			Assert.assertFalse(getOutput(exec_result).replaceAll("\n", " ").matches(match_info), String.format("Sync Plan [%s] should be found in the result list", sp.name));
		}
	}
	
	private void assert_syncplanInfo(KatelloSyncPlan sp) {
		if (sp.description == null) sp.description = "None";
		
		exec_result = sp.info();

		String match_info = String.format(KatelloSyncPlan.REG_SYNCPLAN_INFO, sp.name, sp.description, sp.date.replaceAll("-", "/") + " " + sp.time, sp.interval).replaceAll("\"", "");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		log.finest(String.format("Sync Plan (info) match regex: [%s]", match_info));
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", " ").matches(match_info), String.format("Sync Plan [%s] should be found in the result info", sp.name));
	}
	
}
