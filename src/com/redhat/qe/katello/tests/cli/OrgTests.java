package com.redhat.qe.katello.tests.cli;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliDataProvider;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloDistributor;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.common.TngRunGroups;
import com.redhat.qe.tools.SSHCommandResult;
import java.io.File;

@Test(groups=TngRunGroups.TNG_KATELLO_Organizations)
public class OrgTests extends KatelloCliTestBase{
	List<KatelloOrg> orgs = Collections.synchronizedList(new ArrayList<KatelloOrg>());
	String uid = KatelloUtils.getUniqueID();
	SSHCommandResult exec_result;
	String orgName_Exists = "EXIST-"+uid;
	String orgLabel_Exists = "LBL"+uid;
	String org_name = "org"+uid;

	String org_system_name = "org_system-"+uid;
	String env_system_name = "Library"; // initially - for headpin

	@BeforeClass(description="Generate unique objects", groups={"cfse-cli","headpin-cli"})
	public void setUp() {
		KatelloOrg org = new KatelloOrg(this.cli_worker, "FOO"+uid,"Package tests", "BAR"+uid);
		exec_result = org.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		orgs.add(org);

		exec_result = new KatelloOrg(this.cli_worker, org_system_name, null).cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");

		exec_result = new KatelloOrg(this.cli_worker, org_name, null).cli_create();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check - return code (org create)");
	}

	@BeforeClass(description="init: katello specific, no headpin", dependsOnMethods={"setUp"})
	public void setUp_katelloOnly(){
		this.env_system_name = "env_system-"+uid;
		exec_result = new KatelloEnvironment(this.cli_worker, env_system_name, null, org_system_name, KatelloEnvironment.LIBRARY).cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
	}

	@Test(description = "List all orgs - default org should be there",groups={"cfse-cli","headpin-cli"})
	public void test_listOrgs_DefaultOrg(){
		KatelloOrg list_org = new KatelloOrg(this.cli_worker, null,null);
		SSHCommandResult res = list_org.cli_list();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(res).contains(KatelloOrg.getDefaultOrg()), "Check - contains default org");
	}

	//TODO: BZ 987670
	@Test(description = "Create org - different variations",
			dataProviderClass = KatelloCliDataProvider.class,
			dataProvider = "org_create",groups={"cfse-cli","headpin-cli"})
	public void test_createOrg(String name, String lable, String descr, Integer exitCode, String output){
		KatelloOrg org = new KatelloOrg(this.cli_worker, name, descr);
		SSHCommandResult res = org.cli_create();

		Assert.assertTrue(res.getExitCode() == exitCode.intValue(), "Check - return code");
		if(exitCode.intValue()==0){
			Assert.assertTrue(getOutput(res).contains(output),"Check - returned output string");
		}else{ // Failure to be checked
			Assert.assertTrue(getOutput(res).contains(output),"Check - returned error string");
		}

		this.orgs.add(org);
	}

	/** TCMS scenario is: <a href="https://tcms.engineering.redhat.com/case/243076/?from_plan=7791">here</a> */
	@Test(description = "9fbfc747-c343-4c94-b76e-5c262db355c9", groups={"cfse-cli","headpin-cli"})
	public void test_createOrgNonLatin(){		
		String uniqueID = KatelloUtils.getUniqueID();

		KatelloOrg org1 = new KatelloOrg(this.cli_worker, "Орга низация" + uniqueID, "");
		SSHCommandResult res = org1.cli_create();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		this.orgs.add(org1);

		KatelloOrg org2 = new KatelloOrg(this.cli_worker, "կազմա կերպություն" + uniqueID, "");
		res = org2.cli_create();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		this.orgs.add(org2);

		KatelloOrg org3 = new KatelloOrg(this.cli_worker, "组 织" + uniqueID, "");
		res = org3.cli_create();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		this.orgs.add(org3);
	}

	@Test(description = "List orgs - created", 
			dependsOnMethods={"test_createOrg", "test_createOrgNonLatin"},groups={"cfse-cli","headpin-cli"})
	public void test_infoListOrg(){
		String uniqueID = KatelloUtils.getUniqueID();
		KatelloOrg list_org = new KatelloOrg(this.cli_worker, "orgUpd"+uniqueID, "Simple description");		
		list_org.cli_create();

		orgs.add(list_org);

		list_org = new KatelloOrg(this.cli_worker, null,null);
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
		KatelloOrg org = new KatelloOrg(this.cli_worker, "orgUpd"+uniqueID, "Simple description");		

		res = org.cli_create();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		String new_desc = String.format("Updated %s",org.description);
		org.description = new_desc;
		res = org.update(new_desc);
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		Assert.assertEquals(getOutput(res).trim(), String.format("Successfully updated organization [ %s ]",org.name));

		res = org.cli_list();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code (org list)");

		assert_orgInfo(org);
	}

	/** TCMS scenario is: <a href="https://tcms.engineering.redhat.com/case/243077/?from_plan=7791">here</a> */
	@Test(description="dc5d228a-b998-4845-b050-f9d2fcfa5ec3",groups={"cfse-cli","headpin-cli"})
	public void test_deleteOrg(){
		String uniqueID = KatelloUtils.getUniqueID();
		KatelloOrg org = new KatelloOrg(this.cli_worker, "orgDel"+uniqueID, null);

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
		KatelloOrg org = new KatelloOrg(this.cli_worker, "orgDel"+uniqueID, null);

		SSHCommandResult res = org.delete();
		Assert.assertTrue(res.getExitCode() == 148, "Check - return code [148]");
		Assert.assertEquals(getOutput(res).trim(), 
				String.format(KatelloOrg.ERR_ORG_NOTFOUND,org.name));
	}

	@Test(description="List org subscriptions.",groups={"cfse-cli"})
	public void test_orgSubscriptions(){
		String uniqueID = KatelloUtils.getUniqueID();
		String orgName = "subscriptions-" + uniqueID;
		KatelloOrg org = new KatelloOrg(this.cli_worker, orgName, null); // or you can provide null -> "some simple description here"
		SSHCommandResult res = org.cli_create();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code (org create)");

		String  providerName = "provider" + uniqueID;
		KatelloProvider prov = new KatelloProvider(this.cli_worker, providerName, orgName, "Fedora provider", null);
		res = prov.create();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");

		String productName = "product" + uniqueID;
		KatelloProduct prod = new KatelloProduct(this.cli_worker, productName, orgName, providerName, null, null, null, null, null);
		res = prod.create();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");

		String productName1 = "product1" + uniqueID;
		prod = new KatelloProduct(this.cli_worker, productName1, orgName, providerName, null, null, null, null, null);
		res = prod.create();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");

		res = org.subscriptions();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code (org subscriptions)"); // check: ($? is 0)

		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(String.format(KatelloOrg.OUT_ORG_SUBSCR, productName)), "Check - Subscriptions contains " + productName);
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(String.format(KatelloOrg.OUT_ORG_SUBSCR, productName1)), "Check - Subscriptions contains " + productName1);
	}

	/** TCMS scenario is: <a href="https://tcms.engineering.redhat.com/case/243073/?from_plan=7791">here</a> */
	@Test(description = "5a63c8c1-3add-4269-86d7-9b3caac413db",groups={"cfse-cli","headpin-cli"})
	public void test_createOrgExists(){
		String uniqueID = KatelloUtils.getUniqueID();
		KatelloOrg org = new KatelloOrg(this.cli_worker, "orgCrt"+uniqueID, "Simple description");	
		org.cli_create();

		KatelloOrg org2 = new KatelloOrg(this.cli_worker, "orgCrt"+uniqueID, "Simple description");	
		SSHCommandResult res = org2.cli_create();

		Assert.assertTrue(res.getExitCode() == 166, "Check - return code [166]");
		Assert.assertEquals(getOutput(res).trim(), KatelloOrg.ERR_ORG_EXISTS_MUST_BE_UNIQUE);
	}

	//TODO: BZ: 987670
	@Test(description = "Create org - name with special characters",groups={"cfse-cli","headpin-cli"})
	public void test_createOrgNameSpecialCharacters(){
		String uniqueID = KatelloUtils.getUniqueID();
		KatelloOrg org = new KatelloOrg(this.cli_worker, "orgCrt"+uniqueID + " very < invalid name", "Simple description");	
		SSHCommandResult res = org.cli_create();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code [0]");
		Assert.assertEquals(getOutput(res).trim(), 
				String.format(KatelloOrg.OUT_CREATE, "orgCrt"+uniqueID + " very < invalid name"));

		org = new KatelloOrg(this.cli_worker, "orgCrt"+uniqueID + " invalid name > very", "Simple description");	
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code [0]");
		Assert.assertEquals(getOutput(res).trim(), 
				String.format(KatelloOrg.OUT_CREATE, "orgCrt"+uniqueID + " invalid name > very"));

		org = new KatelloOrg(this.cli_worker, "orgCrt"+uniqueID + " very / Invalid name", "Simple description");	
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code [0]");
		Assert.assertEquals(getOutput(res).trim(), 
				String.format(KatelloOrg.OUT_CREATE, "orgCrt"+uniqueID + " very / Invalid name"));
	}

	@Test(description = "Create org - name is already used",groups={"cfse-cli","headpin-cli"})
	public void test_createOrgExistingName(){
		KatelloOrg org = new KatelloOrg(this.cli_worker, orgName_Exists, "existing org", "label"+KatelloUtils.getUniqueID());
		org.cli_create();
		exec_result = new KatelloOrg(this.cli_worker, orgName_Exists, "existing org 2", "new"+org.label).cli_create();

		Assert.assertTrue(exec_result.getExitCode() == 166, "Check - return code [166]");
		Assert.assertEquals(getOutput(exec_result).trim(), KatelloOrg.ERR_ORG_NAME_EXISTS);
	}

	@Test(description = "Create org - label is already used",groups={"cfse-cli","headpin-cli"})
	public void test_createOrgExistingLabel(){
		KatelloOrg org = new KatelloOrg(this.cli_worker, "labelExists-"+uid, "existing label", orgLabel_Exists);
		org.cli_create();
		exec_result = new KatelloOrg(this.cli_worker, "new"+org.name, "existing label", orgLabel_Exists).cli_create();

		Assert.assertTrue(exec_result.getExitCode() == 166, "Check - return code [166]");
		Assert.assertEquals(getOutput(exec_result).trim(), KatelloOrg.ERR_ORG_LABEL_EXISTS);
	}

	@Test(description = "Create org - name and label are already used",groups={"cfse-cli","headpin-cli"}, dependsOnMethods={"test_createOrgExistingLabel"})
	public void test_createOrgExistingNameAndLabel(){
		KatelloOrg org = new KatelloOrg(this.cli_worker, "bothExist-"+uid, "existing both name and label", "bothExist-"+uid);
		org.cli_create();
		exec_result = new KatelloOrg(this.cli_worker, org.name, org.description, org.label).cli_create();

		Assert.assertTrue(exec_result.getExitCode() == 166, "Check - return code [166]");
		Assert.assertEquals(getOutput(exec_result).trim(), KatelloOrg.ERR_ORG_EXISTS_MUST_BE_UNIQUE);
	}	

	@Test(description = "Delete Organization with Systems ",groups={"cfse-cli","headpin-cli"})
	public void test_deleteOrgWithSystems(){
		String uniqueID = KatelloUtils.getUniqueID();
		String sys_del_name = "system_del-" + uniqueID;
		KatelloSystem system_del = new KatelloSystem(this.cli_worker, sys_del_name,org_system_name, env_system_name);
		exec_result = system_del.rhsm_registerForce();
		exec_result = system_del.list();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = new KatelloOrg(this.cli_worker, org_system_name, null).delete();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	}

	@Test(description = "699bb444-5cfd-4e6f-ab50-cb77292bc57a",groups={"cfse-cli","headpin-cli", TngRunGroups.TNG_KATELLO_Manifests_CDN})
	public void test_UploadManifestDiffOrg(){

		KatelloProvider providerRH;
		String uniqueID = KatelloUtils.getUniqueID();
		String org_name = "Raleigh-" + uniqueID;
		String diff_org_name = "Durham-" + uniqueID;

		// upload the manifest to server.
		KatelloUtils.scpOnClient(cli_worker.getClientHostname(), "data/stack-manifest.zip", "/tmp");

		// create org-1
		KatelloOrg org = new KatelloOrg(this.cli_worker, org_name,null);
		exec_result = org.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		// create org-2
		KatelloOrg diff_org = new KatelloOrg(this.cli_worker, diff_org_name,null);
		exec_result = diff_org.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		try {
			// import manifest for the org-1
			providerRH = new KatelloProvider(this.cli_worker, KatelloProvider.PROVIDER_REDHAT,org_name,null,null);
			exec_result = providerRH.import_manifest("/tmp"+File.separator+"stack-manifest.zip", new Boolean(true));
			Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

			// now try to import the _same_ manifest for another org. 
			providerRH = new KatelloProvider(this.cli_worker, KatelloProvider.PROVIDER_REDHAT,diff_org_name,null,null);
			exec_result = providerRH.import_manifest("/tmp"+File.separator+"stack-manifest.zip", new Boolean(true));
			Assert.assertTrue(exec_result.getExitCode().intValue() == 65, "Check - return code");
			Assert.assertTrue(getOutput(exec_result).contains("Provider [ Red Hat ] failed to import manifest:"),"Check - return string");
		} finally {
			exec_result  = org.delete();
			Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
			if (diff_org != null) {
				exec_result = diff_org.delete();
				Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
			}
		}
	}

	@Test(description = "ee630a0d-7455-4b92-8069-b14b3d9d1173",groups={"cfse-cli","headpin-cli", TngRunGroups.TNG_KATELLO_Manifests_CDN})
	public void test_UploadManifestSameOrg(){

		String uniqueID = KatelloUtils.getUniqueID();
		String org_name = "Raleigh-" + uniqueID;
		KatelloOrg org = new KatelloOrg(this.cli_worker, org_name,null);
		exec_result = org.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		KatelloProvider provider = new KatelloProvider(this.cli_worker, KatelloProvider.PROVIDER_REDHAT,org_name,null,null);
		KatelloUtils.scpOnClient(cli_worker.getClientHostname(),"data/stack-manifest.zip", "/tmp");
		try {
			exec_result = provider.import_manifest("/tmp"+File.separator+"stack-manifest.zip", new Boolean(true));
			Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
			Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloProvider.OUT_MANIFEST_IMPORTED)),"Check - return string");
			exec_result = provider.import_manifest("/tmp"+File.separator+"stack-manifest.zip", new Boolean(true));
			Assert.assertTrue(exec_result.getExitCode().intValue() == 65, "Check - return code");
			Assert.assertTrue(getOutput(exec_result).contains(String.format("Provider [ Red Hat ] failed to import manifest:")),"Check - return string");
		} finally {
			exec_result  = org.delete();
			Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		}
	}

	@Test(description = "b5e801df-5661-42f7-b1ba-d09a68c7fd92",groups={"headpin-cli", "cfse-ignore"}, enabled=false)// TODO - gkhachik delete manifest gone from recent sat-6.0.1
	public void test_ReUploadManifestDiffOrg(){
		KatelloUtils.scpOnClient(cli_worker.getClientHostname(), "data/"+"stack-manifest.zip", "/tmp");

		String uniqueID = KatelloUtils.getUniqueID();
		String org_name = "Raleigh-" + uniqueID;
		String diff_org_name = "Durham-" + uniqueID;
		KatelloOrg org = new KatelloOrg(this.cli_worker, org_name,null);
		KatelloOrg diff_org = null;
		exec_result = org.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		KatelloProvider provider = new KatelloProvider(this.cli_worker, KatelloProvider.PROVIDER_REDHAT,org_name,null,null);
		try {
			exec_result = provider.import_manifest("/tmp"+File.separator+"stack-manifest.zip", new Boolean(true));
			Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
			exec_result = provider.delete_manifest();
			Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
			diff_org = new KatelloOrg(this.cli_worker, diff_org_name,null);
			exec_result = diff_org.cli_create();
			Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
			provider = new KatelloProvider(this.cli_worker, KatelloProvider.PROVIDER_REDHAT,diff_org_name,null,null);
			exec_result = provider.import_manifest("/tmp"+File.separator+"stack-manifest.zip", new Boolean(true));
			Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		} finally {
			exec_result	= org.delete();
			Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
			if (diff_org != null) {
				exec_result = diff_org.delete();
				Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
			}
		}
	}


	@Test(description = "Set or Get SLA for an ORG",groups={"cfse-cli","headpin-cli"})
	public void test_SLAOrg() {
		String uniqueID = KatelloUtils.getUniqueID();
		String org_name = "sla-org-" + uniqueID;
		String sys_name = "sys-sla-org";
		String org_no_import_manifest = "sla-org-no-import" + uniqueID;
		KatelloOrg org = new KatelloOrg(this.cli_worker, org_name,null);
		exec_result = org.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		KatelloUtils.scpOnClient(cli_worker.getClientHostname(), "data/manifest_org_sla.zip", "/tmp");
		KatelloProvider prov = new KatelloProvider(this.cli_worker, KatelloProvider.PROVIDER_REDHAT,org_name,null,null);
		exec_result = prov.import_manifest("/tmp"+File.separator+"manifest_org_sla.zip", new Boolean(true));
		exec_result = org.update_servicelevel("Self-support");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = org.cli_info();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		String DefaultInfoStr = KatelloUtils.grepCLIOutput("Default Service Level", getOutput(exec_result));
		Assert.assertTrue(DefaultInfoStr.contains("Self-support"), "Check - stdout contains updated service level");

		KatelloSystem sys = new KatelloSystem(this.cli_worker, sys_name+"-subscribed",org_name,KatelloEnvironment.LIBRARY);
		if(System.getProperty("katello.engine", "katello").equals("headpin"))
			sys.setEnvironmentName(null); // Seems to me there is really no better way to make environment == null (just for this case).
		exec_result = sys.rhsm_registerForce(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");

		exec_result = sys.subscriptions_available();
		String poolId1 = KatelloUtils.grepCLIOutput("ID", getOutput(exec_result).trim(),1);
		Assert.assertNotNull(poolId1, "Check - pool Id is not null");

		exec_result = sys.subscribe(poolId1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertEquals(exec_result.getStdout().trim(), 
				String.format(KatelloSystem.OUT_SUBSCRIBE, sys_name +"-subscribed"),
				"Check - subscribe system output.");

		exec_result = sys.info();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		String Servicelevel = KatelloUtils.grepCLIOutput("Service Level", getOutput(exec_result));
		Assert.assertTrue(Servicelevel.contains("Self-support"), "Check - stdout contains updated service level");

		KatelloOrg org_no = new KatelloOrg(this.cli_worker, org_no_import_manifest,null);
		exec_result = org_no.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = org_no.cli_info();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		String DefaultInfo = KatelloUtils.grepCLIOutput("Default Service Level", getOutput(exec_result));
		Assert.assertTrue(DefaultInfo.contains("None"), "Check - stdout contains default service level as None");

		exec_result=org.delete();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	}

	/** TCMS scenario is: <a href="https://tcms.engineering.redhat.com/case/242174/?from_plan=7791">here</a> */
	@Test(description="a7e5daa8-b235-47c1-b9c1-c0b05771ba0d")
	public void test_updateOrgNonUtf8Chars(){
		String orgName= "ブッチャーストリート"+uid;
		String orgDescription = "ブッチャーテストの説明";
		KatelloOrg org = new KatelloOrg(this.cli_worker, orgName,orgDescription);
		exec_result = org.cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check -return code");
		exec_result = org.cli_info();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check -return code");
		Assert.assertTrue(KatelloUtils.grepCLIOutput("Description", getOutput(exec_result)).equals(orgDescription), "Check - stdout contains description");
		Assert.assertTrue(KatelloUtils.grepCLIOutput("Name", getOutput(exec_result)).equals(orgName), "Check - stdout contains name");

		// update description now.
		String upDesc = "=Über Sûdlüîëk Stérèô Übersetzung";
		exec_result = org.update(upDesc);
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check -return code");
		exec_result = org.cli_info();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check -return code");
		Assert.assertTrue(KatelloUtils.grepCLIOutput("Description", getOutput(exec_result)).equals(upDesc), "Check - stdout contains description");

		//		// add new env.
		//		KatelloEnvironment env = new KatelloEnvironment(this.cli_runner, "ブッチャ", "チャーテストの説", orgName, KatelloEnvironment.LIBRARY);
		//		exec_result = env.cli_create();
		//		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check -return code");
		//		exec_result = env.cli_info();
		//		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check -return code");
		//		Assert.assertTrue(KatelloUtils.grepCLIOutput("Description", getOutput(exec_result)).equals(env.getDescription()), "Check - stdout contains description");
		//		Assert.assertTrue(KatelloUtils.grepCLIOutput("Name", getOutput(exec_result)).equals(env.getName()), "Check - stdout contains name");
		//		
		//		// edit env. description
		//		exec_result = env.cli_update(upDesc);
		//		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check -return code");
		//		env.cli_info();
		//		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check -return code");
		//		Assert.assertTrue(KatelloUtils.grepCLIOutput("Description", getOutput(exec_result)).equals(upDesc), "Check - stdout contains description");

		// TODO - recently not possible to use special characters:
		// Validation failed: Name cannot contain characters other than alpha numerals, space, '_', '-'
	}

	@Test(description="add system custom information key",
			dataProviderClass=KatelloCliDataProvider.class, dataProvider="org_add_custom_info")
	public void test_addSysCustomKey(String keyname, Integer exitCode, String output) {
		KatelloOrg org = new KatelloOrg(this.cli_worker, org_name, null);
		exec_result = org.default_info_add(keyname,"system");
		Assert.assertTrue(exec_result.getExitCode()==exitCode.intValue(), "Check exit code (add custom info)");
		if(exec_result.getExitCode()==0)
			Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloOrg.OUT_ADD_SYS_INFO, keyname, org_name)), "Check output (add custom info)");
		else
			Assert.assertTrue(getOutput(exec_result).contains(output), "Check error (add custom info)");
	}

	@Test(description="add distributor custom information key",
			dataProviderClass=KatelloCliDataProvider.class, dataProvider="org_add_custom_info")
	public void test_addDistributorCustomKey(String keyname, Integer exitCode, String output) {
		KatelloOrg org = new KatelloOrg(this.cli_worker, org_name, null);
		exec_result = org.default_info_add(keyname,"distributor");
		Assert.assertTrue(exec_result.getExitCode()==exitCode.intValue(), "Check exit code (add custom info)");
		if(exec_result.getExitCode()==0)
			Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloOrg.OUT_ADD_DISTRIBUTOR_INFO, keyname, org_name)), "Check output (add custom info)");
		else
			Assert.assertTrue(getOutput(exec_result).contains(output), "Check error (add custom info)");
	}

	//TODO: Failing due to BZ: 973907
	@Test(description = "Org name with a dot, verify that providers, product, environments are handled normally",groups={"katello-cli"})
	public void test_OrgNameContainsDot(){

		String uid = KatelloUtils.getUniqueID();
		String provName = "listProv1-"+uid;
		String envName = "listEnv-"+uid;
		String prodName = "prod-"+uid;
		String desc = "Simple description";
		String orgName = "org."+uid;
		String sysName = "Sys-"+uid;
		SSHCommandResult res;

		KatelloOrg org = new KatelloOrg(this.cli_worker, orgName, "Org name with a dot");
		exec_result = org.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		// Create Environment
		KatelloEnvironment env = new KatelloEnvironment(this.cli_worker, envName, desc, orgName, KatelloEnvironment.LIBRARY);
		res = env.cli_create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code (Enviornment - create)");
		//List
		res = env.cli_list();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (Environment - list)");

		//Create System
		rhsm_clean();
		KatelloSystem sys = new KatelloSystem(this.cli_worker, sysName, orgName, envName);
		res = sys.rhsm_register();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code (System - create)");
		//list
		res = sys.list();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (System - list)");

		// Create provider
		KatelloProvider prov = new KatelloProvider(this.cli_worker, provName, orgName, desc, KATELLO_SMALL_REPO);
		res = prov.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (Provider - create)");
		//List
		res = prov.cli_list();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (Provider - list)");
		String match_info = String.format(KatelloProvider.REG_PROVIDER_LIST,provName,KATELLO_SMALL_REPO,desc).replaceAll("\"", "");
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(match_info),
				String.format("Provider [%s] should be found in the list",provName));

		// Create Product
		KatelloProduct prod = new KatelloProduct(this.cli_worker, prodName, orgName, provName, desc, null, null, null, null);
		res = prod.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (Product - create)");
		//List
		res = prod.cli_list();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (Provider - list)");		
	}

	@Test(description = "Default distributor info - at organisation level",groups={"cfse-cli","headpin-cli"})
	public void test_defaultDistributorInfoOrg(){

		String org_rm_name = "org-remove-"+ uid;
		String keyname = "testkey_"+ uid;
		String distributor_name = "dis_name" + uid;
		KatelloOrg org_rm_info = new KatelloOrg(this.cli_worker, org_rm_name,"Remove default info for distributor");
		exec_result = org_rm_info.cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		exec_result = org_rm_info.default_info_add(keyname,"distributor");
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains("Successfully added [ Distributor ] default custom info [ " + keyname + " ] to Org [ "+ org_rm_name +" ]"),"Check - returned string");
		exec_result = org_rm_info.default_info_apply("distributor");
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains("Organization [ " + org_rm_name + " ] completed syncing default info"),"Check - returned string");
		KatelloDistributor distributor = new KatelloDistributor(cli_worker, org_rm_name,distributor_name);
		exec_result = distributor.distributor_create();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains("Successfully created distributor [ " + distributor_name + " ]"),"Check - returned string");
		exec_result = distributor.distributor_info();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		String dInfo = KatelloUtils.grepCLIOutput("Custom Info", getOutput(exec_result));
		Assert.assertTrue(dInfo.contains("[ "+keyname+":  ]"), "Check - stdout contains default info added at organisation level");
		exec_result = org_rm_info.default_info_remove(keyname,"distributor"); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains("Successfully removed [ Distributor ] default custom info [ "+ keyname +" ] for Org [ "+ org_rm_name +" ]"), "Check - stdout removed distributor default info");
		exec_result = org_rm_info.delete();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
	}

	@Test(description = "Default distributor info remove - at organisation level",groups={"cfse-cli","headpin-cli"})
	public void test_defaultDistributorRemoveInfoOrg(){

		String uid = KatelloUtils.getUniqueID();
		String org_rm_name = "org-remove-"+ uid;
		String keyname = "testkey_"+ uid;
		String distributor_name = "dis_name" + uid;
		String invalid_key = "invalid_key-" + uid;
		KatelloOrg org_rm_info = new KatelloOrg(this.cli_worker, org_rm_name,"Remove default info for distributor");
		exec_result = org_rm_info.cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		exec_result = org_rm_info.default_info_add(keyname,"distributor");
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains("Successfully added [ Distributor ] default custom info [ " + keyname + " ] to Org [ "+ org_rm_name +" ]"),"Check - returned string");
		exec_result = org_rm_info.default_info_apply("distributor");
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains("Organization [ " + org_rm_name + " ] completed syncing default info"),"Check - returned string");
		KatelloDistributor distributor = new KatelloDistributor(cli_worker, org_rm_name,distributor_name);
		exec_result = distributor.distributor_create();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains("Successfully created distributor [ " + distributor_name + " ]"),"Check - returned string");
		exec_result = distributor.distributor_info();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		String dInfo = KatelloUtils.grepCLIOutput("Custom Info", getOutput(exec_result));
		Assert.assertTrue(dInfo.contains("[ "+keyname+":  ]"), "Check - stdout contains default info added at organisation level");
		exec_result = org_rm_info.default_info_remove(keyname,"distributor"); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains("Successfully removed [ Distributor ] default custom info [ "+ keyname +" ] for Org [ "+ org_rm_name +" ]"), "Check - stdout removed distributor default info");
		exec_result = org_rm_info.default_info_apply("distributor");
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains("Organization [ " + org_rm_name + " ] completed syncing default info"),"Check - returned string");
		exec_result = org_rm_info.default_info_remove(invalid_key,"distributor");
		Assert.assertTrue(exec_result.getExitCode().intValue() == 148, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains("Couldn't find default_info with keyname [ "+ invalid_key +" ]"), "Check - stdout removed distributor default info");
		exec_result = org_rm_info.delete();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");		
	}

	@AfterClass(description="Remove org objects", alwaysRun=true)
	public void tearDown() {
		for (KatelloOrg org : orgs) {
			org.delete();
		}
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
