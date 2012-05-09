package com.redhat.qe.katello.tests.cli;

import java.util.Vector;
import com.redhat.qe.auto.testng.Assert;
import org.testng.annotations.Test;
import com.redhat.qe.katello.base.KatelloCliDataProvider;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.katello.base.cli.KatelloOrg;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups={"cfse-cli"})
public class OrgTests extends KatelloCliTestScript{
	Vector<KatelloOrg> orgs;
	
	@Test(groups = {"cli-org"}, 
			description = "List all orgs - ACME_Corporation should be there")
	public void test_listOrgs_ACME_Corp(){
		KatelloOrg list_org = new KatelloOrg(null,null);
		SSHCommandResult res = list_org.list();
		Assert.assertEquals(res.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertTrue(getOutput(res).contains(KatelloOrg.DEFAULT_ORG), "Check - contains: ["+KatelloOrg.DEFAULT_ORG+"]");
	}
	
	@Test(groups = {"cli-org"}, 
			description = "Create org - different variations",
			dataProviderClass = KatelloCliDataProvider.class,
			dataProvider = "org_create")
	public void test_createOrg(String name, String descr){		
		KatelloOrg org = new KatelloOrg(name, descr);
		SSHCommandResult res = org.create();
		
		Assert.assertEquals(res.getExitCode().intValue(), 0, "Check - return code");
		if(this.orgs ==null){
			this.orgs = new Vector<KatelloOrg>();
		}
		this.orgs.add(org);
	}
	
	@Test(groups = {"cli-org"}, description = "List orgs - created", 
			dependsOnMethods={"test_createOrg"})
	public void test_infoListOrg(){
		KatelloOrg org;
		String REG_ORG_LIST = ".*Id:\\s+\\d+.*Name:\\s+%s.*Description:\\s+%s.*";

		KatelloOrg list_org = new KatelloOrg(null,null);
		SSHCommandResult res = list_org.list();
		Assert.assertEquals(res.getExitCode().intValue(), 0, "Check - return code (org list)");
		
		for(int i=0;i<this.orgs.size();i++){
			org = this.orgs.elementAt(i);
			if(org.description ==null) org.description = "None";
			String match_list = String.format(REG_ORG_LIST, org.name, org.description).replaceAll("\"", ""); // output not have '"' signs
			Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(match_list), "Check - org matches ["+org.name+"]");
			assert_orgInfo(org); // Assertions - `org info --name %s` 
		}
	}
	
	@Test(description="Update org's description", groups = {"cli-org"})
	public void test_updateOrg(){
		SSHCommandResult res;
		String uniqueID = KatelloTestScript.getUniqueID();
		KatelloOrg org = new KatelloOrg("orgUpd"+uniqueID, "Simple description");		
		
		res = org.create();
		Assert.assertEquals(res.getExitCode().intValue(), 0, "Check - return code");
		String new_desc = String.format("Updated %s",org.description);
		res = org.update(new_desc);
		Assert.assertEquals(res.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertEquals(getOutput(res).trim(), String.format("Successfully updated org [ %s ]",org.name));
		
		// TODO - Enter special characters - check it works. 您好
		// BZ: https://bugzilla.redhat.com/show_bug.cgi?id=741274
		assert_orgInfo(org);
	}
	
	@Test(description="Delete an organization", groups = {"cli-org"})
	public void test_deleteOrg(){
		String uniqueID = KatelloTestScript.getUniqueID();
		KatelloOrg org = new KatelloOrg("orgDel"+uniqueID, null);
		
		org.create();
		SSHCommandResult res = org.delete();
		Assert.assertEquals(res.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertTrue(getOutput(res).contains(String.format("Successfully deleted org [ %s ]",org.name)),"Check - return string");
		
		res = org.info();
		Assert.assertEquals(res.getExitCode(), new Integer(148),"Check - return code [148]");
		Assert.assertEquals(getOutput(res).trim(), 
				String.format("Couldn't find organization '%s'",org.name));
	}
	
	private void assert_orgInfo(KatelloOrg org){
		String REG_ORG_INFO = ".*Id:\\s+\\d+.*Name:\\s+%s.*Description:.*%s.*";
		SSHCommandResult res;
		res = org.info();
		String match_info = String.format(REG_ORG_INFO,org.name,org.description).replaceAll("\"", "");
		Assert.assertEquals(res.getExitCode().intValue(), 0, "Check - return code");
		log.finest(String.format("Org (info) match regex: [%s]",match_info));
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(match_info), 
				String.format("Org [%s] should be found in the result info",org.name));		
	}
}
