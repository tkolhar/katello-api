package com.redhat.qe.katello.tests.i18n;

import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

public class OrgTests extends KatelloCliTestBase {
	
	private String orgName;
	private String orgDescr;
	private String orgNewDescr;
	
	@Test(description = "Create org - name in different locale")
	public void test_createOrg() {
		String uid = KatelloUtils.getUniqueID();
		orgName = getText("org.create.name") + uid;
		orgDescr = getText("org.create.description") + uid;
		
		KatelloOrg org = new KatelloOrg(this.cli_worker, orgName, orgDescr);
		
		SSHCommandResult res = org.cli_create();
		
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(res).trim().contains(getText("org.create.stdout", orgName)));
	}
	
	@Test(description = "Update org description", dependsOnMethods = {"test_createOrg"})
	public void test_updateOrg() {
		orgNewDescr = getText("org.update.description");
		
		KatelloOrg org = new KatelloOrg(this.cli_worker, orgName, null);
		
		SSHCommandResult res = org.update(orgNewDescr);
		
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(res).trim().contains(getText("org.update.stdupdate", orgName)));
	}

	@Test(description = "Retrieves org", dependsOnMethods = {"test_updateOrg"})
	public void test_readOrg() {
		
		KatelloOrg org = new KatelloOrg(this.cli_worker, orgName, null);
		
		SSHCommandResult res = org.cli_info();
		
		String match_info = getText("org.info.stdout.regexp", orgName, orgNewDescr, "").replaceAll("\"", "");
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		log.finest(String.format("Org (info) match regex: [%s]",match_info));
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(match_info), 
				String.format("Org [%s] should be found in the result info",org.name));		
	}

	@Test(description = "Lists orgs", dependsOnMethods = {"test_readOrg"})
	public void test_listOrg() {
		
		KatelloOrg org = new KatelloOrg(this.cli_worker, orgName, null);
		
		SSHCommandResult res = org.cli_list();
		
		String match_info = getText("org.list.stdout.regexp", orgName, orgNewDescr).replaceAll("\"", "");
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		log.finest(String.format("Org (list) match regex: [%s]",match_info));
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(match_info), 
				String.format("Org [%s] should be found in the result list",org.name));		
	}
	
	@Test(description = "Deletes org", dependsOnMethods = {"test_listOrg"})
	public void test_deleteOrg() {
		
		KatelloOrg org = new KatelloOrg(this.cli_worker, orgName, null);
		
		SSHCommandResult res = org.delete();
		
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(res).trim().contains(getText("org.delete.stdout", orgName)));
	}
}
