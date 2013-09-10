package com.redhat.qe.katello.tests.cli;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloUser;
import com.redhat.qe.katello.base.obj.KatelloUserRole;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.common.TngRunGroups;

@Test(groups=TngRunGroups.TNG_KATELLO_Organizations)
public class UserOrgDeleteTests extends KatelloCliTestBase {
	List<KatelloOrg> orgs = Collections.synchronizedList(new ArrayList<KatelloOrg>());
	String uid = KatelloUtils.getUniqueID();
	String org_name1 = "org1"+uid;
	String org_name2 = "org2"+uid;
	
	String user_name1 = "user1"+uid;
	String user_name2 = "user2"+uid;
	
	String user_mail1 = user_name1+"@localhost";
	String user_mail2 = user_name2+"@localhost";


	// TODO bug 1001609
	@Test(description = "Creates user, org, imports manifest and deletes org and user, verify that it will not fail.",groups={"cfse-cli","headpin-cli"})
	public void test_deleteOrgFirst() {
		KatelloUser usr = new KatelloUser(cli_worker, user_name1, user_mail1, KatelloUser.DEFAULT_USER_PASS, false);
		exec_result = usr.cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code ("+KatelloUser.CMD_CREATE+")");
		
		exec_result = usr.assign_role(KatelloUserRole.ROLE_ADMINISTRATOR);
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");
		
		KatelloOrg org = new KatelloOrg(this.cli_worker, org_name1, null);
		org.runAs(usr);
		exec_result = org.cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");
		orgs.add(org);
		
		KatelloProvider prov = new KatelloProvider(this.cli_worker, KatelloProvider.PROVIDER_REDHAT, org_name1, null, null);
		KatelloUtils.scpOnClient(cli_worker.getClientHostname(), "data/"+KatelloProvider.MANIFEST_2SUBSCRIPTIONS, "/tmp"); // send manifest zip to the client's /tmp dir.
		exec_result = prov.import_manifest(
				"/tmp/"+KatelloProvider.MANIFEST_2SUBSCRIPTIONS, false); // import manifest
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");

		exec_result = prov.refresh_manifest();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		
		exec_result = org.delete();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		
		exec_result = usr.delete();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
	}

	@Test(description = "Creates user, org, imports manifest and deletes user and org, verify that it will not fail.",
			groups={"cfse-cli","headpin-cli"}, dependsOnMethods={"test_deleteOrgFirst"})
	public void test_deleteUserFirst() {
		KatelloUser usr = new KatelloUser(cli_worker, user_name2, user_mail2, KatelloUser.DEFAULT_USER_PASS, false);
		exec_result = usr.cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code ("+KatelloUser.CMD_CREATE+")");
		
		exec_result = usr.assign_role(KatelloUserRole.ROLE_ADMINISTRATOR);
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");
		
		KatelloOrg org = new KatelloOrg(this.cli_worker, org_name2, null);
		org.runAs(usr);
		exec_result = org.cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");
		orgs.add(org);
		
		KatelloProvider prov = new KatelloProvider(this.cli_worker, KatelloProvider.PROVIDER_REDHAT, org_name2, null, null);
		KatelloUtils.scpOnClient(cli_worker.getClientHostname(), "data/"+KatelloProvider.MANIFEST_12SUBSCRIPTIONS, "/tmp"); // send manifest zip to the client's /tmp dir.
		exec_result = prov.import_manifest(
				"/tmp/"+KatelloProvider.MANIFEST_2SUBSCRIPTIONS, false); // import manifest
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");

		exec_result = prov.refresh_manifest();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		
		exec_result = usr.delete();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		
		org.runAs(null);
		exec_result = org.delete();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
	}
	
	@AfterClass(description="Remove org objects", alwaysRun=true)
	public void tearDown() {
		for (KatelloOrg org : orgs) {
			try{
				org.delete();
			}catch(Exception ex){/*simply ignore if there might be exceptions*/}
		}
	}	
}
