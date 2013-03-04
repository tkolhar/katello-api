package com.redhat.qe.katello.tests.cli;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliDataProvider;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SCPTools;
import com.redhat.qe.tools.SSHCommandResult;
import java.io.File;

public class OrgTests extends KatelloCliTestScript{
	List<KatelloOrg> orgs = Collections.synchronizedList(new ArrayList<KatelloOrg>());
	String uid = KatelloUtils.getUniqueID();
	SSHCommandResult exec_result;
	String orgName_Exists = "EXIST-"+uid;
	String orgLabel_Exists = "LBL"+uid;

	@AfterClass(description="Remove org objects", alwaysRun=true)
	public void tearDown() {
		for (KatelloOrg org : orgs) {
			org.delete();
		}
	}	
	
	@Test(description = "List all orgs - default org should be there",groups={"cfse-cli","headpin-cli"})
	public void test_listOrgs_DefaultOrg(){
		KatelloOrg list_org = new KatelloOrg(null,null);
		SSHCommandResult res = list_org.cli_list();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(res).contains(KatelloOrg.getDefaultOrg()), "Check - contains default org");
	}
	
	@Test(description = "Create org - different variations",
			dataProviderClass = KatelloCliDataProvider.class,
			dataProvider = "org_create",groups={"cfse-cli","headpin-cli"})
	public void test_createOrg(String name, String descr){
		KatelloOrg org = new KatelloOrg(name, descr);
		SSHCommandResult res = org.cli_create();
		
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");

		this.orgs.add(org);
	}
	
	
	@Test(description = "Create org - different variations", groups={"cfse-cli","headpin-cli"})
	public void test_createOrgNonLatin(){		
		String uniqueID = KatelloUtils.getUniqueID();
		
		KatelloOrg org1 = new KatelloOrg("Орга низация" + uniqueID, "");
		SSHCommandResult res = org1.cli_create();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		this.orgs.add(org1);
		
		KatelloOrg org2 = new KatelloOrg("կազմա կերպություն" + uniqueID, "");
		res = org2.cli_create();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		this.orgs.add(org2);
		
		KatelloOrg org3 = new KatelloOrg("组 织" + uniqueID, "");
		res = org3.cli_create();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		this.orgs.add(org3);
	}
	
	@Test(description = "List orgs - created", 
			dependsOnMethods={"test_createOrg", "test_createOrgNonLatin"},groups={"cfse-cli","headpin-cli"})
	public void test_infoListOrg(){
		String uniqueID = KatelloUtils.getUniqueID();
		KatelloOrg list_org = new KatelloOrg("orgUpd"+uniqueID, "Simple description");		
		list_org.cli_create();
		
		orgs.add(list_org);
		
		list_org = new KatelloOrg(null,null);
		SSHCommandResult res = list_org.cli_list();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code (org list)");
		
		for(KatelloOrg org : orgs){
			if(org.description ==null) org.description = "None";
			String match_list = String.format(KatelloOrg.REG_ORG_LIST, org.name, org.description).replaceAll("\"", ""); // output not have '"' signs
			Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(match_list), "Check - org matches ["+org.name+"]");
			assert_orgInfo(org); // Assertions - `org info --name %s` 
		}
	}
	
	@Test(description="Update org's description",groups={"cfse-cli","headpin-cli"})
	public void test_updateOrg(){
		SSHCommandResult res;
		String uniqueID = KatelloUtils.getUniqueID();
		KatelloOrg org = new KatelloOrg("orgUpd"+uniqueID, "Simple description");		
		
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		String new_desc = String.format("Updated %s",org.description);
		res = org.update(new_desc);
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		Assert.assertEquals(getOutput(res).trim(), String.format("Successfully updated org [ %s ]",org.name));
		
		org.description = "您好" + org.description;
		res = org.update(org.description);
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		Assert.assertEquals(getOutput(res).trim(), String.format("Successfully updated org [ %s ]",org.name));
		
		res = org.cli_list();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code (org list)");

		assert_orgInfo(org);
	}
	
	@Test(description="Delete an organization",groups={"cfse-cli","headpin-cli"})
	public void test_deleteOrg(){
		String uniqueID = KatelloUtils.getUniqueID();
		KatelloOrg org = new KatelloOrg("orgDel"+uniqueID, null);
		
		org.cli_create();
		SSHCommandResult res = org.delete();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(res).contains(String.format("Successfully deleted org [ %s ]",org.name)),"Check - return string");
		
		res = org.cli_info();
		Assert.assertTrue(res.getExitCode() == 148, "Check - return code [148]");
		Assert.assertEquals(getOutput(res).trim(), 
				String.format(KatelloOrg.ERR_ORG_NOTFOUND,org.name));
	}
	
	@Test(description="Delete an organization which does not exist",groups={"cfse-cli","headpin-cli"})
	public void test_deleteOrgNotExist(){
		String uniqueID = KatelloUtils.getUniqueID();
		KatelloOrg org = new KatelloOrg("orgDel"+uniqueID, null);
		
		SSHCommandResult res = org.delete();
		Assert.assertTrue(res.getExitCode() == 148, "Check - return code [148]");
		Assert.assertEquals(getOutput(res).trim(), 
				String.format(KatelloOrg.ERR_ORG_NOTFOUND,org.name));
	}
	
	@Test(description="List org subscriptions.",groups={"cfse-cli"})
	public void test_orgSubscriptions(){
		String uniqueID = KatelloUtils.getUniqueID();
		String orgName = "subscriptions-" + uniqueID;
		KatelloOrg org = new KatelloOrg(orgName, null); // or you can provide null -> "some simple description here"
		SSHCommandResult res = org.cli_create();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code (org create)");
		
		String  providerName = "provider" + uniqueID;
		KatelloProvider prov = new KatelloProvider(providerName, orgName, "Fedora provider", null);
		res = prov.create();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		
		String productName = "product" + uniqueID;
		KatelloProduct prod = new KatelloProduct(productName, orgName, providerName, null, null, null, null, null);
		res = prod.create();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		
		String productName1 = "product1" + uniqueID;
		prod = new KatelloProduct(productName1, orgName, providerName, null, null, null, null, null);
		res = prod.create();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		
		res = org.subscriptions();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code (org subscriptions)"); // check: ($? is 0)
		
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(String.format(KatelloOrg.OUT_ORG_SUBSCR, productName)), "Check - Subscriptions contains " + productName);
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(String.format(KatelloOrg.OUT_ORG_SUBSCR, productName1)), "Check - Subscriptions contains " + productName1);
	}
	
	@Test(description = "Create org - existing",groups={"cfse-cli","headpin-cli"})
	public void test_createOrgExists(){
		String uniqueID = KatelloUtils.getUniqueID();
		KatelloOrg org = new KatelloOrg("orgCrt"+uniqueID, "Simple description");	
		org.cli_create();

		KatelloOrg org2 = new KatelloOrg("orgCrt"+uniqueID, "Simple description");	
		SSHCommandResult res = org2.cli_create();
		
		Assert.assertTrue(res.getExitCode() == 144, "Check - return code [144]");
		Assert.assertEquals(getOutput(res).trim(), 
				KatelloOrg.ERR_ORG_EXISTS_MUST_BE_UNIQUE);
	}
	
	@Test(description = "Create org - name is invalid",groups={"cfse-cli","headpin-cli"})
	public void test_createOrgInvalidName(){
		String uniqueID = KatelloUtils.getUniqueID();
		KatelloOrg org = new KatelloOrg("orgCrt"+uniqueID + " very ++== invalid name", "Simple description");	
		SSHCommandResult res = org.cli_create();
		
		Assert.assertTrue(res.getExitCode() == 144, "Check - return code [144]");
		Assert.assertEquals(getOutput(res).trim(), 
				KatelloOrg.ERR_NAME_INVALID);
	}
	
	@Test(description = "Create org - name is already used",groups={"cfse-cli","headpin-cli"})
	public void test_createOrgExistingName(){
		KatelloOrg org = new KatelloOrg(orgName_Exists, "existing org", "label"+KatelloUtils.getUniqueID());
		org.cli_create();
		exec_result = new KatelloOrg(orgName_Exists, "existing org 2", "new"+org.label).cli_create();
		
		Assert.assertTrue(exec_result.getExitCode() == 144, "Check - return code [144]");
		Assert.assertEquals(getOutput(exec_result).trim(), KatelloOrg.ERR_ORG_NAME_EXISTS);
	}
	
	@Test(description = "Create org - label is already used",groups={"cfse-cli","headpin-cli"})
	public void test_createOrgExistingLabel(){
		KatelloOrg org = new KatelloOrg("labelExists-"+uid, "existing label", orgLabel_Exists);
		org.cli_create();
		exec_result = new KatelloOrg("new"+org.name, "existing label", orgLabel_Exists).cli_create();
		
		Assert.assertTrue(exec_result.getExitCode() == 144, "Check - return code [144]");
		Assert.assertEquals(getOutput(exec_result).trim(), KatelloOrg.ERR_ORG_LABEL_EXISTS);
	}
	
	@Test(description = "Create org - name and label are already used",groups={"cfse-cli","headpin-cli"}, dependsOnMethods={"test_createOrgExistingLabel"})
	public void test_createOrgExistingNameAndLabel(){
		KatelloOrg org = new KatelloOrg("bothExist-"+uid, "existing both name and label", "bothExist-"+uid);
		org.cli_create();
		exec_result = new KatelloOrg(org.name, org.description, org.label).cli_create();
		
		Assert.assertTrue(exec_result.getExitCode() == 144, "Check - return code [144]");
		Assert.assertEquals(getOutput(exec_result).trim(), KatelloOrg.ERR_ORG_EXISTS_MUST_BE_UNIQUE);
	}	

	   
	@Test(description = "Delete Organization with Systems ",groups={"cfse-cli","headpin-cli"})
	public void test_Delete_OrgWithSystems(){
		
		String uniqueID = KatelloUtils.getUniqueID();
	    String org_system_name = "org_system-" + uniqueID;
	    String env_system_name = "env_system-" + uniqueID;
	    String sys_del_name = "system_del-" + uniqueID;
	    KatelloOrg org_system = new KatelloOrg(org_system_name, null);
	    exec_result = org_system.cli_create();
	    Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	    KatelloEnvironment env_system = new KatelloEnvironment(env_system_name,null,org_system_name,KatelloEnvironment.LIBRARY);
	    exec_result = env_system.cli_create();
	    Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	    KatelloSystem system_del = new KatelloSystem(sys_del_name,org_system_name, env_system_name);
	    exec_result = system_del.rhsm_registerForce();
	    exec_result = system_del.list();
	    Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	    exec_result = org_system.delete();
	    Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

	}
	
	
	@Test(description = "Attempt to upload an already imported manifest in a different ORG",groups={"cfse-cli","headpin-cli"})
	public void test_UploadManifestDiffOrg(){

		String uniqueID = KatelloUtils.getUniqueID();
		String org_name = "Raleigh-" + uniqueID;
		String diff_org_name = "Durham-" + uniqueID;
		KatelloOrg org = new KatelloOrg(org_name,null);
		exec_result = org.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		KatelloProvider provider = new KatelloProvider(KatelloProvider.PROVIDER_REDHAT,org_name,null,null);
		SCPTools scp = new SCPTools(
				System.getProperty("katello.server.hostname", "localhost"), 
				System.getProperty("katello.server.ssh.user", "root"), 
				System.getProperty("katello.server.sshkey.private", ".ssh/id_hudson_dsa"), 
				System.getProperty("katello.server.sshkey.passphrase", "null"));
		Assert.assertTrue(scp.sendFile("data"+File.separator+"stack-manifest.zip", "/tmp"),
				"stack-manifest.zip sent successfully");			
		exec_result = provider.import_manifest("/tmp"+File.separator+"stack-manifest.zip", new Boolean(true));
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		KatelloOrg diff_org = new KatelloOrg(diff_org_name,null);
		exec_result = diff_org.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		provider = new KatelloProvider(KatelloProvider.PROVIDER_REDHAT,diff_org_name,null,null);
		exec_result = provider.import_manifest("/tmp"+File.separator+"stack-manifest.zip", new Boolean(true));
		Assert.assertTrue(exec_result.getExitCode() == 144, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(String.format("This distributor has already been imported by another owner")),"Check - return string");
		exec_result  = org.delete();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = diff_org.delete();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	
	}

 	

	@Test(description = "Attempt to upload an already imported manifest in the same ORG",groups={"cfse-cli","headpin-cli"})
	public void test_UploadManifestSameOrg(){

		String uniqueID = KatelloUtils.getUniqueID();
		String org_name = "Raleigh-" + uniqueID;
		KatelloOrg org = new KatelloOrg(org_name,null);
		exec_result = org.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		KatelloProvider provider = new KatelloProvider(KatelloProvider.PROVIDER_REDHAT,org_name,null,null);
		SCPTools scp = new SCPTools(
				System.getProperty("katello.server.hostname", "localhost"), 
				System.getProperty("katello.server.ssh.user", "root"), 
				System.getProperty("katello.server.sshkey.private", ".ssh/id_hudson_dsa"), 
				System.getProperty("katello.server.sshkey.passphrase", "null"));
		Assert.assertTrue(scp.sendFile("data"+File.separator+"stack-manifest.zip", "/tmp"),
				"stack-manifest.zip sent successfully");			
		exec_result = provider.import_manifest("/tmp"+File.separator+"stack-manifest.zip", new Boolean(true));
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(String.format("Manifest imported")),"Check - return string");
		exec_result = provider.import_manifest("/tmp"+File.separator+"stack-manifest.zip", new Boolean(true));
		Assert.assertTrue(exec_result.getExitCode() == 144, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(String.format("Import is the same as existing data")),"Check - return string");
		exec_result  = org.delete();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	}
	
	
	@Test(description = "Delete a manifest from an ORG and upload the same to an other ORG",groups={"cfse-cli","headpin-cli"})
	public void test_ReUploadManifestDiffOrg(){
		
		String uniqueID = KatelloUtils.getUniqueID();
		String org_name = "Raleigh-" + uniqueID;
		String diff_org_name = "Durham-" + uniqueID;
		KatelloOrg org = new KatelloOrg(org_name,null);
		exec_result = org.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		KatelloProvider provider = new KatelloProvider(KatelloProvider.PROVIDER_REDHAT,org_name,null,null);
		SCPTools scp = new SCPTools(
				System.getProperty("katello.server.hostname", "localhost"), 
				System.getProperty("katello.server.ssh.user", "root"), 
				System.getProperty("katello.server.sshkey.private", ".ssh/id_hudson_dsa"), 
				System.getProperty("katello.server.sshkey.passphrase", "null"));
		Assert.assertTrue(scp.sendFile("data"+File.separator+"stack-manifest.zip", "/tmp"),
				"stack-manifest.zip sent successfully");			
		exec_result = provider.import_manifest("/tmp"+File.separator+"stack-manifest.zip", new Boolean(true));
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = provider.delete_manifest();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		KatelloOrg diff_org = new KatelloOrg(diff_org_name,null);
		exec_result = diff_org.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		provider = new KatelloProvider(KatelloProvider.PROVIDER_REDHAT,diff_org_name,null,null);
		exec_result = provider.import_manifest("/tmp"+File.separator+"stack-manifest.zip", new Boolean(true));
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result	= org.delete();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = diff_org.delete();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

	}
	
	private void assert_orgInfo(KatelloOrg org){
		
		SSHCommandResult res;
		res = org.cli_info();
		String match_info = String.format(KatelloOrg.REG_ORG_INFO,org.name,org.description).replaceAll("\"", "");
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		log.finest(String.format("Org (info) match regex: [%s]",match_info));
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(match_info), 
				String.format("Org [%s] should be found in the result info",org.name));		
	}
}
