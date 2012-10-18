package com.redhat.qe.katello.tests.i18n;

import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

public class OrgTests extends KatelloCliTestScript {
	
	private String orgName;
	private String orgDescr;
	private String orgNewDescr;
	
	@Test(description = "Create org - name in different locale", groups={"cfse-cli"})
	public void test_createOrg() {
		String uid = KatelloUtils.getUniqueID();
		orgName = getText("org.create.name") + uid;
		orgDescr = getText("org.create.description") + uid;
		
		KatelloOrg org = new KatelloOrg(orgName, orgDescr);
		
		SSHCommandResult res = org.cli_create();
		
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		Assert.assertEquals(getOutput(res).trim(), getText("org.create.stdout", orgName));
	}
	
	@Test(description = "Update org description", dependsOnMethods = {"test_createOrg"}, groups={"cfse-cli"})
	public void test_updateOrg() {
		orgNewDescr = getText("org.update.description");
		
		KatelloOrg org = new KatelloOrg(orgName, null);
		
		SSHCommandResult res = org.update(orgNewDescr);
		
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		Assert.assertEquals(getOutput(res).trim(), getText("org.update.stdupdate", orgName));
	}

}
