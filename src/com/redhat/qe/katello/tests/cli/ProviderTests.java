package com.redhat.qe.katello.tests.cli;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliDataProvider;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.tasks.KatelloTasks;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups={"cfse-cli"})
public class ProviderTests extends KatelloCliTestScript{
	private String org_name;
	
	@BeforeClass(description="Prepare an org to work with", groups = {"cli-providers"})
	public void setup_org(){
		
		String uid = KatelloTestScript.getUniqueID();
		this.org_name = "org"+uid;
		KatelloOrg org = new KatelloOrg(this.org_name,null);
		SSHCommandResult res = org.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
	}
	
	@Test(description="Fresh org - check default provider status/info", groups = {"cli-providers"}, enabled=true)
	public void test_freshOrgDefaultRedHatProvider(){
		KatelloProvider prov;
		String uid = KatelloTestScript.getUniqueID();
		String tmpOrg = "tmpOrg"+uid;
		KatelloOrg org = new KatelloOrg(tmpOrg,null);
		SSHCommandResult res = org.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		
		// assertions - `provider list` 
		// check that default provider of RedHat type is prepared
		prov = new KatelloProvider(null, tmpOrg, null, null);
		res = prov.cli_list();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		String REGEXP_PROVIDER_REDHAT = ".*Id:\\s+\\d+.*Name:\\s+"+KatelloProvider.PROVIDER_REDHAT+".*Type:\\s+Red\\sHat.*Url:\\s+https://cdn.redhat.com.*";
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(REGEXP_PROVIDER_REDHAT), 
				"Provider \""+KatelloProvider.PROVIDER_REDHAT+"\" should be found in the providers list");

		// assertions - `provider status` 
		// status of "Red Hat" provider
		prov = new KatelloProvider(KatelloProvider.PROVIDER_REDHAT, tmpOrg, null, null);
		res = prov.status();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		String REGEXP_REDHAT_STATUS = ".*Id:\\s+\\d+.*Name:\\s+"+KatelloProvider.PROVIDER_REDHAT+".*Last\\sSync:\\s+never.*Sync\\sState:\\s+Not\\ssynced.*";
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(REGEXP_REDHAT_STATUS), 
				"Provider \""+KatelloProvider.PROVIDER_REDHAT+"\" should have sync status: never");
		
		// assertions - `provider info`
		// get info of "Red Hat" provider
		res = new KatelloOrg(org_name, null).cli_info();
		
		String orgId = KatelloTasks.grepCLIOutput("Id", getOutput(res));
		prov = new KatelloProvider(KatelloProvider.PROVIDER_REDHAT, org_name, null, null);
		res = prov.info();
		String REGEXP_REDHAT_INFO = ".*Id:\\s+\\d+.*Name:\\s+"+KatelloProvider.PROVIDER_REDHAT+".*Type:\\s+Red Hat.*Url:\\s+https://cdn.redhat.com.*Org Id:\\s+"+orgId+".*Description:.*";
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(REGEXP_REDHAT_INFO), 
				"Provider \""+KatelloProvider.PROVIDER_REDHAT+"\" info should be displayed together with org_id");
	}
	
	@Test(description="Create custom provider - different inputs", groups = {"cli-providers"},
			dataProvider="provider_create",dataProviderClass = KatelloCliDataProvider.class, enabled=true)
	public void test_createProvider_output(String name, String descr, String url, Integer exitCode, String output){
		
		String cmd = "provider create --org "+this.org_name;
		if(name!=null)
			cmd = cmd + " --name \""+name+"\"";
		if(descr!=null)
			cmd = cmd + " --description \""+descr+"\"";
		if(url!=null)
			cmd = cmd + " --url \""+url+"\"";
		KatelloProvider prov = new KatelloProvider(name, org_name, descr, url);
		SSHCommandResult  res = prov.create();
		Assert.assertTrue(res.getExitCode().intValue() == exitCode.intValue(), "Check - return code");
		
		if(exitCode.intValue()==0){ //
			Assert.assertTrue(getOutput(res).contains(output),"Check - returned output string");
		}else{ // Failure to be checked
			Assert.assertTrue(getOutput(res).contains(output),"Check - returned error string");
		}
	}
	
	@Test(description="Delete provider - Red Hat", groups = {"cli-providers"}, enabled=true)
	public void test_deleteProvider_RedHat(){
		SSHCommandResult res;
		String uid = KatelloTestScript.getUniqueID();
		String orgName = "delRH"+uid;
		
		KatelloOrg org = new KatelloOrg(orgName, null);
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		
		KatelloProvider prov = new KatelloProvider(KatelloProvider.PROVIDER_REDHAT, this.org_name, null, null);
		res = prov.delete();
		Assert.assertTrue(res.getExitCode().intValue()==147, "Check - return code");
		//Assert.assertEquals(getOutput(res).trim(), "Error while deleting provider [ Red Hat ]: Red Hat provider can not be deleted,","Check - returned error string");
		// see BZ#: https://bugzilla.redhat.com/show_bug.cgi?id=754934
		Assert.assertEquals(getOutput(res).trim(), "User admin is not allowed to access api/providers/destroy","Check - returned error string");

		// get the provider info - should be there
		res = prov.info();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(".*Name:\\s+Red Hat.*"),"Check - returned output string");		
	}
	
	@Test(description="Delete provider Custom - missing parameters", groups={"cli-providers"}, enabled = false)
	public void test_deleteProvider_missingReqParams(){
		String uid = KatelloTestScript.getUniqueID();
		String provName = "delProv-"+uid;
		KatelloProvider prov = new KatelloProvider(provName, this.org_name, null, null);
		SSHCommandResult res = prov.create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		
		prov = new KatelloProvider(null, null, null, null);
		res = prov.delete();
		Assert.assertTrue(res.getExitCode().intValue() == 2, "Check - return code");
		Assert.assertTrue(getOutput(res).contains("Option --org is required; please see --help"),"Check - returned error string - 1");
		Assert.assertTrue(getOutput(res).contains("Option --name is required; please see --help"),"Check - returned error string - 2");
		
		prov = new KatelloProvider(null, this.org_name, null, null);
		res = prov.delete();
		Assert.assertTrue(res.getExitCode().intValue() == 2, "Check - return code");
		Assert.assertTrue(getOutput(res).contains("Option --name is required; please see --help"),"Check - returned error string");
		
		prov = new KatelloProvider(provName, null, null, null);
		res = prov.delete();
		Assert.assertTrue(res.getExitCode().intValue() == 2, "Check - return code");
		Assert.assertTrue(getOutput(res).contains("Option --org is required; please see --help"),"Check - returned error string");
		
		prov = new KatelloProvider(null, "", null, null);
		res = prov.delete();
		Assert.assertTrue(res.getExitCode().intValue() == 2, "Check - return code");
		Assert.assertTrue(getOutput(res).contains("katello: error: --org option requires an argument"),"Check - returned error string");
		
		prov = new KatelloProvider("", this.org_name, null, null);
		res = prov.delete();
		Assert.assertTrue(res.getExitCode().intValue() == 2, "Check - return code");
		Assert.assertTrue(getOutput(res).contains("katello: error: --name option requires an argument"),"Check - returned error string");
	}
	
	@Test(description="Delete provider Custom - different org", groups = {"cli-providers"},enabled=true)
	public void test_deleteProvider_diffOrg(){
		String uid = KatelloTestScript.getUniqueID();
		String provName = "delProv-"+uid;
		String org1 = "anotherOrg"+uid;
		
		KatelloOrg org = new KatelloOrg(org1,null);
		SSHCommandResult res = org.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");

		KatelloProvider prov = new KatelloProvider(provName, this.org_name, null, null);
		res = prov.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		prov = new KatelloProvider(provName, org1, null, null);
		res = prov.delete();
		Assert.assertTrue(res.getExitCode().intValue()==65, "Check - return code");
		Assert.assertTrue(getOutput(res).contains("Could not find provider [ "+provName+" ] within organization [ "+org1+" ]"),"Check - returned error string");
	}
	
	@Test(description="Delete provider Custom - no products associated", groups = {"cli-providers"},enabled=true)
	public void test_deleteProvider_noProducts(){
		SSHCommandResult res;
		String uid = KatelloTestScript.getUniqueID();
		
		KatelloProvider prov = new KatelloProvider("noProd-"+uid, this.org_name, null, null);
		res = prov.create();		
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		res = prov.delete();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		Assert.assertTrue(getOutput(res).equals("Deleted provider [ "+prov.name+" ]"), "Check - returned output string");
		
		this.assert_providerRemoved(prov);
		// try to recreate the provider with the same name: should be possible
		res = prov.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		Assert.assertEquals(getOutput(res).trim(), "Successfully created provider [ "+prov.name+" ]");
	}
	
	@Test(description="Delete provider Custom - with products associated", groups = {"cli-providers"},enabled=true)
	public void test_deleteProvider_noRepos(){
		SSHCommandResult res;
		String uid = KatelloTestScript.getUniqueID();
		String provName = "noRepos-"+uid;
		String provName_1 = "prov1-"+uid;
		String prodName = "prod-"+uid;
		
		// Create provider, product
		KatelloProvider prov = new KatelloProvider(provName, this.org_name, null, null);
		res = prov.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		KatelloProduct prod = new KatelloProduct(prodName, this.org_name, provName, null, null, null, null, null);
		res = prod.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		
		// Delete provider
		res = prov.delete();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		Assert.assertTrue(getOutput(res).equals(String.format(KatelloProvider.OUT_DELETE, provName)), "Check - returned output string");
		
		// Check provider is removed
		this.assert_providerRemoved(prov);

		// Check associated product is gone
		res = prod.status();
		Assert.assertTrue(res.getExitCode().intValue()==65, "Check - return code");
		Assert.assertTrue(getOutput(res).equals(String.format(KatelloProduct.ERR_COULD_NOT_FIND_PRODUCT, prodName,org_name)), "Check - `product status` output string");
		
		// Create another provider with the same product name
		prov = new KatelloProvider(provName_1, this.org_name, null, null);
		res = prov.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		KatelloProduct prod1 = new KatelloProduct(prodName, this.org_name, provName_1, null, null, null, null, null);
		res = prod1.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		// Check `product status` - should be shown with provName_1 info there
		String REGEXP_PRODUCT = ".*Id:\\s+\\d+.*Name:\\s+%s.*Provider Id:\\s+\\d+.*Provider Name:\\s+%s.*";
		res = prod.status();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		String match_info = String.format(REGEXP_PRODUCT,prodName,provName_1).replaceAll("\"", "");
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(match_info), 
				String.format("Provider [%s] should be found in the result of product info for: [%s]",provName_1,prodName));		
	}
	
	//@Test
	public void test_deleteProvider_withRepos(){
		// TODO - to be implemented.
	}
	
	@Test(description="List / Info providers - no description, no url", groups = {"cli-providers"},enabled=true)
	public void test_listNinfoProviders_noDesc_noUrl(){
		SSHCommandResult res;
		String uid = KatelloTestScript.getUniqueID();
		String provName = "listProv1-"+uid;
		
		// Create provider
		KatelloProvider prov = new KatelloProvider(provName, this.org_name, null, null);
		res = prov.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		// List
		res = prov.cli_list();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		String REGEXP_PROVIDER_LIST = ".*Id:\\s+\\d+.*Name:\\s+%s.*Type:\\s+Custom.*Url:\\s+None.*Description:\\s+None";
		String match_info = String.format(REGEXP_PROVIDER_LIST,provName).replaceAll("\"", "");
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(match_info), 
				String.format("Provider [%s] should be found in the list with: no description, no url",provName));
		// Info
		res = prov.info();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		match_info = String.format(REGEXP_PROVIDER_LIST,provName).replaceAll("\"", "");
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(match_info), 
				String.format("Provider [%s] should be found in the info with: no description, no url",provName));
	}
	
	@Test(description="List / Info providers", groups = {"cli-providers"},enabled=true)
	public void test_listNinfoProviders(){
		SSHCommandResult res;
		String uid = KatelloTestScript.getUniqueID();
		String provName = "listProv1-"+uid;
		String provDesc = "Simple description";
		
		// Create provider
		KatelloProvider prov = new KatelloProvider(provName, this.org_name, provDesc, KATELLO_SMALL_REPO);
		res = prov.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		//List
		res = prov.cli_list();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		String REGEXP_PROVIDER_LIST = ".*Id:\\s+\\d+.*Name:\\s+%s.*Type:\\s+Custom.*Url:\\s+%s.*Description:\\s+%s.*";
		String match_info = String.format(REGEXP_PROVIDER_LIST,provName,KATELLO_SMALL_REPO,provDesc).replaceAll("\"", "");
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(match_info), 
				String.format("Provider [%s] should be found in the list",provName));
		// Info
		res = prov.info();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		match_info = String.format(REGEXP_PROVIDER_LIST,provName,KATELLO_SMALL_REPO,provDesc).replaceAll("\"", "");
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(match_info), 
				String.format("Provider [%s] should be found in the info",provName));
	}
	
	@Test(description="List / Info providers - no description", groups = {"cli-providers"},enabled=true)
	public void test_listNinfoProviders_noDesc(){
		SSHCommandResult res;
		String uid = KatelloTestScript.getUniqueID();
		String provName = "listProvURL-"+uid;
		
		// Create provider
		KatelloProvider prov = new KatelloProvider(provName, this.org_name, null, KATELLO_SMALL_REPO);
		res = prov.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		// List
		res = prov.cli_list();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		String REGEXP_PROVIDER_LIST = ".*Id:\\s+\\d+.*Name:\\s+%s.*Type:\\s+Custom.*Url:\\s+%s.*Description:\\s+None";
		String match_info = String.format(REGEXP_PROVIDER_LIST,provName,KATELLO_SMALL_REPO).replaceAll("\"", "");
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(match_info), 
				String.format("Provider [%s] should be found in the list with: no description",provName));
		// Info
		res = prov.info();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		match_info = String.format(REGEXP_PROVIDER_LIST,provName,KATELLO_SMALL_REPO).replaceAll("\"", "");
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(match_info), 
				String.format("Provider [%s] should be found in the info with: no description",provName));
	}
	
	@Test(description="Synchronize provider - no products", groups = {"cli-providers"},enabled=true)
	public void test_syncProvider_noProduct(){
		SSHCommandResult res;
		String uid = KatelloTestScript.getUniqueID();
		String provName = "syncNoProd-"+uid;
		
		// Create provider
		KatelloProvider prov = new KatelloProvider(provName, this.org_name, null, null);
		res = prov.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		// Sync
		res = prov.synchronize();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		Assert.assertTrue(getOutput(res).equals(String.format(KatelloProvider.OUT_SYNCHRONIZE, provName)), "Check - returned output string");
	}

	@Test(description="Synchronize provider - single product", groups = {"cli-providers"},enabled=true)
	public void test_syncProvider_singleProduct(){
		SSHCommandResult res;
		String uid = KatelloTestScript.getUniqueID();
		String provName = "syncNoProd-"+uid;
		String prodName = "prod1Repo-"+uid;
		String repoName = "pulpF15_64bit-"+uid;
		
		// Create provider
		KatelloProvider prov = new KatelloProvider(provName, this.org_name, null, null);
		res = prov.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		// Create product
		KatelloProduct prod = new KatelloProduct(prodName, this.org_name, provName, null, null, null, null, null);
		res = prod.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		// Create repo - valid url to sync
		KatelloRepo repo = new KatelloRepo(repoName, this.org_name, prodName, PULP_F15_x86_64_REPO, null, null);
		repo.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		// Sync provider
		res = prov.synchronize();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		Assert.assertTrue(getOutput(res).contains(String.format(KatelloProvider.OUT_SYNCHRONIZE, provName)), "Check - returned output string");
		
		assert_repoSynced(repo);
	}
	
	@Test(description="Synchronize provider - multiple products", groups = {"cli-providers"},enabled=true)
	public void test_syncProvider_multiProducts(){
		SSHCommandResult res;
		String uid = KatelloTestScript.getUniqueID();
		String provName = "syncNoProd-"+uid;
		String prodName1 = "pulpF15_64bit"+uid;
		String prodName2 = "pulpF15_32bit"+uid;
		String repoName1 = "pulpF15_64bit-"+uid;
		String repoName2 = "pulpF15_32bit-"+uid;
		
		// Create provider
		KatelloProvider prov = new KatelloProvider(provName, this.org_name, null, null);
		res = prov.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		// Create products
		KatelloProduct prod1 = new KatelloProduct(prodName1, this.org_name, provName, null, null, null, null, null);
		res = prod1.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		KatelloProduct prod2 = new KatelloProduct(prodName2, this.org_name, provName, null, null, null, null, null);
		res = prod2.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		// Create repos
		KatelloRepo repo1 = new KatelloRepo(repoName1, this.org_name, prodName1, PULP_F15_x86_64_REPO, null, null);
		res = repo1.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		KatelloRepo repo2 = new KatelloRepo(repoName2, this.org_name, prodName2, PULP_F15_i386_REPO, null, null);
		res = repo2.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		// Sync provider
		res = prov.synchronize();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		Assert.assertTrue(getOutput(res).contains(String.format(KatelloProvider.OUT_SYNCHRONIZE, provName)), "Check - returned output string");
		
		assert_repoSynced(repo1);
		assert_repoSynced(repo2);
	}
	
	// Import manifest - TODO (need smaller size file for an import).
	
	@Test(description="Try to updateRed Hat provider - name", groups = {"cli-providers"},enabled=true)
	public void test_updateProvider_RedHat_name(){
		SSHCommandResult res;
		KatelloProvider prov = new KatelloProvider(KatelloProvider.PROVIDER_REDHAT, this.org_name, null, null);
		res = prov.update("REDHAT", null, null);
		Assert.assertTrue(res.getExitCode().intValue()==144, "Check - return code (provider update)");
		Assert.assertTrue(getOutput(res).contains(KatelloProvider.ERR_REDHAT_UPDATENAME), "Check - returned error string (provider update)");
	}
	
	@Test(description="Try to updateRed Hat provider - url", groups = {"cli-providers"}, dependsOnMethods = {"test_freshOrgDefaultRedHatProvider"}, enabled=true)
	public void test_updateProvider_RedHat_url(){
		SSHCommandResult res;
		String update_url = "https://localhost:443";
		String match_info;
		
		KatelloProvider prov = new KatelloProvider(KatelloProvider.PROVIDER_REDHAT, this.org_name, null, null);
		res = prov.update(null, update_url, null);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (provider update)");
		Assert.assertTrue(getOutput(res).contains(String.format(KatelloProvider.OUT_UPDATE,KatelloProvider.PROVIDER_REDHAT)), 
				"Check - returned error string (provider update)");
		// Info
		res = prov.info();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		String REGEXP_PROVIDER_LIST = ".*Id:\\s+\\d+.*Name:\\s+%s.*Type:\\s+.*Url:\\s+%s.*";
		match_info = String.format(REGEXP_PROVIDER_LIST,KatelloProvider.PROVIDER_REDHAT,update_url).replaceAll("\"", "");
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(match_info), 
				String.format("Provider [%s] should be found in the info",KatelloProvider.PROVIDER_REDHAT));
	}
	
	
	// Update - TODO for custom provider.
	
}
