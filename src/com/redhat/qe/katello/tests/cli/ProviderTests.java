package com.redhat.qe.katello.tests.cli;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliDataProvider;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.common.TngRunGroups;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups={TngRunGroups.TNG_KATELLO_Providers_Repos})
public class ProviderTests extends KatelloCliTestBase{
	private String org_name;
	private String org_manifest;

	@BeforeClass(description="Prepare an org to work with", groups = {"cli-providers"})
	public void setup_org(){
		
		String uid = KatelloUtils.getUniqueID();
		this.org_name = "org"+uid;
		KatelloOrg org = new KatelloOrg(this.cli_worker, this.org_name,null);
		SSHCommandResult res = org.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		org_manifest = "org-manifest"+uid;
		exec_result = new KatelloOrg(cli_worker, org_manifest, null).cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
	}
	
	@Test(description="Fresh org - check default provider status/info", groups = {"cli-providers"}, enabled=true)
	public void test_freshOrgDefaultRedHatProvider(){
		KatelloProvider prov;
		String uid = KatelloUtils.getUniqueID();
		String tmpOrg = "tmpOrg"+uid;
		KatelloOrg org = new KatelloOrg(this.cli_worker, tmpOrg,null);
		SSHCommandResult res = org.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		
		// assertions - `provider list` 
		// check that default provider of RedHat type is prepared
		prov = new KatelloProvider(this.cli_worker, null, tmpOrg, null, null);
		res = prov.cli_list();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(String.format(KatelloProvider.REG_REDHAT_LIST, KatelloProvider.CDN_URL).replaceAll("\"", "")),
				"Provider \""+KatelloProvider.PROVIDER_REDHAT+"\" should be found in the providers list");

		// assertions - `provider status` 
		// status of "Red Hat" provider
		prov = new KatelloProvider(this.cli_worker, KatelloProvider.PROVIDER_REDHAT, tmpOrg, null, null);
		res = prov.status();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		String match_info = String.format(KatelloProvider.REG_REDHAT_STATUS, "never", "Not synced").replaceAll("\"", "");
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(match_info), 
				"Provider \""+KatelloProvider.PROVIDER_REDHAT+"\" should have sync status: never");
		
		// assertions - `provider info`
		// get info of "Red Hat" provider
		res = new KatelloOrg(this.cli_worker, org_name, null).cli_info();
		
		String orgId = KatelloUtils.grepCLIOutput("ID", getOutput(res));
		prov = new KatelloProvider(this.cli_worker, KatelloProvider.PROVIDER_REDHAT, org_name, null, null);
		res = prov.info();
		match_info = String.format(KatelloProvider.REG_REDHAT_INFO, KatelloProvider.CDN_URL, orgId, "").replaceAll("\"", "");
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(match_info), 
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
		KatelloProvider prov = new KatelloProvider(this.cli_worker, name, org_name, descr, url);
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
		String uid = KatelloUtils.getUniqueID();
		String orgName = "delRH"+uid;
		
		KatelloOrg org = new KatelloOrg(this.cli_worker, orgName, null);
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		
		KatelloProvider prov = new KatelloProvider(this.cli_worker, KatelloProvider.PROVIDER_REDHAT, this.org_name, null, null);
		res = prov.delete();
		Assert.assertTrue(res.getExitCode().intValue()==147, "Check - return code");
		//Assert.assertEquals(getOutput(res).trim(), "Error while deleting provider [ Red Hat ]: Red Hat provider can not be deleted,","Check - returned error string");
		// see BZ#: https://bugzilla.redhat.com/show_bug.cgi?id=754934
		Assert.assertEquals(getOutput(res).trim(), "User admin is not allowed to access api/v1/providers/destroy","Check - returned error string");

		// get the provider info - should be there
		res = prov.info();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(".*Name\\s*:\\s+Red Hat.*"),"Check - returned output string");		
	}
	
	@Test(description="Delete provider Custom - missing parameters", groups={"cli-providers"}, enabled = false)
	public void test_deleteProvider_missingReqParams(){
		String uid = KatelloUtils.getUniqueID();
		String provName = "delProv-"+uid;
		KatelloProvider prov = new KatelloProvider(this.cli_worker, provName, this.org_name, null, null);
		SSHCommandResult res = prov.create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		
		prov = new KatelloProvider(this.cli_worker, null, null, null, null);
		res = prov.delete();
		Assert.assertTrue(res.getExitCode().intValue() == 2, "Check - return code");
		Assert.assertTrue(getOutput(res).contains("Option --org is required; please see --help"),"Check - returned error string - 1");
		Assert.assertTrue(getOutput(res).contains("Option --name is required; please see --help"),"Check - returned error string - 2");
		
		prov = new KatelloProvider(this.cli_worker, null, this.org_name, null, null);
		res = prov.delete();
		Assert.assertTrue(res.getExitCode().intValue() == 2, "Check - return code");
		Assert.assertTrue(getOutput(res).contains("Option --name is required; please see --help"),"Check - returned error string");
		
		prov = new KatelloProvider(this.cli_worker, provName, null, null, null);
		res = prov.delete();
		Assert.assertTrue(res.getExitCode().intValue() == 2, "Check - return code");
		Assert.assertTrue(getOutput(res).contains("Option --org is required; please see --help"),"Check - returned error string");
		
		prov = new KatelloProvider(this.cli_worker, null, "", null, null);
		res = prov.delete();
		Assert.assertTrue(res.getExitCode().intValue() == 2, "Check - return code");
		Assert.assertTrue(getOutput(res).contains("katello: error: --org option requires an argument"),"Check - returned error string");
		
		prov = new KatelloProvider(this.cli_worker, "", this.org_name, null, null);
		res = prov.delete();
		Assert.assertTrue(res.getExitCode().intValue() == 2, "Check - return code");
		Assert.assertTrue(getOutput(res).contains("katello: error: --name option requires an argument"),"Check - returned error string");
	}
	
	@Test(description="Delete provider Custom - different org", groups = {"cli-providers"},enabled=true)
	public void test_deleteProvider_diffOrg(){
		String uid = KatelloUtils.getUniqueID();
		String provName = "delProv-"+uid;
		String org1 = "anotherOrg"+uid;
		
		KatelloOrg org = new KatelloOrg(this.cli_worker, org1,null);
		SSHCommandResult res = org.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");

		KatelloProvider prov = new KatelloProvider(this.cli_worker, provName, this.org_name, null, null);
		res = prov.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		prov = new KatelloProvider(this.cli_worker, provName, org1, null, null);
		res = prov.delete();
		Assert.assertTrue(res.getExitCode().intValue()==65, "Check - return code");
		Assert.assertTrue(getOutput(res).contains("Could not find provider [ "+provName+" ] within organization [ "+org1+" ]"),"Check - returned error string");
	}
	
	@Test(description="Delete provider Custom - no products associated", groups = {"cli-providers"},enabled=true)
	public void test_deleteProvider_noProducts(){
		SSHCommandResult res;
		String uid = KatelloUtils.getUniqueID();
		
		KatelloProvider prov = new KatelloProvider(this.cli_worker, "noProd-"+uid, this.org_name, null, null);
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
		String uid = KatelloUtils.getUniqueID();
		String provName = "noRepos-"+uid;
		String provName_1 = "prov1-"+uid;
		String prodName = "prod-"+uid;
		
		// Create provider, product
		KatelloProvider prov = new KatelloProvider(this.cli_worker, provName, this.org_name, null, null);
		res = prov.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		KatelloProduct prod = new KatelloProduct(this.cli_worker, prodName, this.org_name, provName, null, null, null, null, null);
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
		prov = new KatelloProvider(this.cli_worker, provName_1, this.org_name, null, null);
		res = prov.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		KatelloProduct prod1 = new KatelloProduct(this.cli_worker, prodName, this.org_name, provName_1, null, null, null, null, null);
		res = prod1.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		// Check `product status` - should be shown with provName_1 info there
		res = prod.status();
		if (prod.syncState == null) prod.syncState = "Not synced";
		String match_info = String.format(KatelloProduct.REG_PROD_STATUS, prodName, provName_1, prod.syncState).replaceAll("\"", "");
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(match_info), 
				String.format("Provider [%s] should be found in the result of product info for: [%s]",provName_1,prodName));		
	}
	
	@Test(description="Delete provider which contains repos, one of them is promoted, verify that correct error is shown", groups = {"cli-providers"},enabled=true)
	public void test_deleteProvider_withRepos() {
		
		String uid = KatelloUtils.getUniqueID();
		String provName = "withRepos-"+uid;
		String prodName1 = "prod1-"+KatelloUtils.getUniqueID();
		String prodName2 = "prod2-"+KatelloUtils.getUniqueID();
		String envName1 = "env1-" + KatelloUtils.getUniqueID();
		String repoName1 = "repo1-"+ uid;
		String repoName2 = "repo2-"+ uid;
		
		// Create provider, product
		KatelloProvider prov = new KatelloProvider(this.cli_worker, provName, this.org_name, null, null);
		SSHCommandResult res = prov.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");

		// create product
		KatelloProduct prod1 = new KatelloProduct(this.cli_worker, prodName1, this.org_name, provName, null, null, PULP_RHEL6_i386_REPO, null, true);
		res = prod1.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");
		
		// Create repo - valid url to sync
		KatelloRepo repo1 = new KatelloRepo(this.cli_worker, repoName1, this.org_name, prodName1, PULP_RHEL6_x86_64_REPO, null, null);
		repo1.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		
		// sync product
		res = prod1.synchronize();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product synchronize)");

		// create env.
		KatelloEnvironment env1 = new KatelloEnvironment(this.cli_worker, envName1, null, this.org_name, KatelloEnvironment.LIBRARY);
		res = env1.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (environment create)");
		
		// sync product (otherwise promote will fail)
		res = prod1.synchronize();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product synchronize)");
		
		// promote product to the env.
		KatelloUtils.promoteProductToEnvironment(cli_worker, org_name, prodName1, envName1);
		prod1.syncState = "Finished";

		KatelloProduct prod2 = new KatelloProduct(this.cli_worker, prodName2, this.org_name, provName, REPO_INECAS_ZOO3, null, null, null, true);
		res = prod2.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");

		// Create repo - valid url to sync
		KatelloRepo repo2 = new KatelloRepo(this.cli_worker, repoName2, this.org_name, prodName2, REPO_INECAS_ZOO3, null, null);
		res = repo2.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		
		res = repo2.synchronize();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		
		res = prov.delete();
		Assert.assertTrue(res.getExitCode().intValue()==144, "Check - return code");
		Assert.assertTrue(getOutput(res).contains("Provider cannot be deleted since " +
				"one of its products or repositories has already been promoted. " +
				"Using a changeset, please delete the repository " +
				"from existing environments before deleting it."),"Check - returned error string");
	}

	@Test(description="Delete provider which contains two repos, both of them are synched but not promoted", groups = {"cli-providers"},enabled=true)
	public void test_deleteProvider_withTwoRepos() {
		
		String uid = KatelloUtils.getUniqueID();
		String provName = "withRepos-"+uid;
		String prodName = "prod-"+KatelloUtils.getUniqueID();
		String repoName1 = "repo1-"+ uid;
		String repoName2 = "repo2-"+ uid;
		
		// Create provider, product
		KatelloProvider prov = new KatelloProvider(this.cli_worker, provName, this.org_name, null, null);
		SSHCommandResult res = prov.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");

		// create product
		KatelloProduct prod = new KatelloProduct(this.cli_worker, prodName, this.org_name, provName, null, null, null, null, true);
		res = prod.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");
		
		// Create repo - valid url to sync
		KatelloRepo repo1 = new KatelloRepo(this.cli_worker, repoName1, this.org_name, prodName, PULP_RHEL6_x86_64_REPO, null, null);
		repo1.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");

		res = repo1.synchronize();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		
		// Create repo - valid url to sync
		KatelloRepo repo2 = new KatelloRepo(this.cli_worker, repoName2, this.org_name, prodName, REPO_INECAS_ZOO3, null, null);
		res = repo2.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		
		res = repo2.synchronize();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		
		res = prov.delete();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		Assert.assertTrue(getOutput(res).equals(String.format(KatelloProvider.OUT_DELETE, provName)), "Check - returned output string");
	}

	@Test(description="List / Info providers - no description, no url", groups = {"cli-providers"},enabled=true)
	public void test_listNinfoProviders_noDesc_noUrl(){
		SSHCommandResult res;
		String uid = KatelloUtils.getUniqueID();
		String provName = "listProv1-"+uid;
		
		// Create provider
		KatelloProvider prov = new KatelloProvider(this.cli_worker, provName, this.org_name, null, null);
		res = prov.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		// List
		res = prov.cli_list();
		if (prov.description == null) prov.description = "None";
		if (prov.url == null) prov.url = "None";
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		String match_info = String.format(KatelloProvider.REG_PROVIDER_LIST,provName, prov.url, prov.description).replaceAll("\"", "");
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(match_info), 
				String.format("Provider [%s] should be found in the list with: no description, no url",provName));
		// Info
		res = prov.info();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		match_info = String.format(KatelloProvider.REG_PROVIDER_LIST,provName, prov.url, prov.description).replaceAll("\"", "");
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(match_info), 
				String.format("Provider [%s] should be found in the info with: no description, no url",provName));
	}
	
	@Test(description="List / Info providers", groups = {"cli-providers"},enabled=true)
	public void test_listNinfoProviders(){
		SSHCommandResult res;
		String uid = KatelloUtils.getUniqueID();
		String provName = "listProv1-"+uid;
		String provDesc = "Simple description";
		
		// Create provider
		KatelloProvider prov = new KatelloProvider(this.cli_worker, provName, this.org_name, provDesc, null);
		res = prov.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		//List
		res = prov.cli_list();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		String match_info = String.format(KatelloProvider.REG_PROVIDER_LIST,provName,"None",provDesc).replaceAll("\"", "");
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(match_info), 
				String.format("Provider [%s] should be found in the list",provName));
		// Info
		res = prov.info();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		match_info = String.format(KatelloProvider.REG_PROVIDER_LIST,provName,"None",provDesc).replaceAll("\"", "");
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(match_info), 
				String.format("Provider [%s] should be found in the info",provName));
	}
	
	@Test(description="List / Info providers - no description", groups = {"cli-providers"},enabled=true)
	public void test_listNinfoProviders_noDesc(){
		SSHCommandResult res;
		String uid = KatelloUtils.getUniqueID();
		String provName = "listProvURL-"+uid;
		
		// Create provider
		KatelloProvider prov = new KatelloProvider(this.cli_worker, provName, this.org_name, null, null);
		res = prov.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		// List
		res = prov.cli_list();
		if (prov.description == null) prov.description = "None";
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		String match_info = String.format(KatelloProvider.REG_PROVIDER_LIST,provName,"None", prov.description).replaceAll("\"", "");
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(match_info), 
				String.format("Provider [%s] should be found in the list with: no description",provName));
		// Info
		res = prov.info();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		match_info = String.format(KatelloProvider.REG_PROVIDER_LIST,provName,"None", prov.description).replaceAll("\"", "");
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(match_info), 
				String.format("Provider [%s] should be found in the info with: no description",provName));
	}
	
	@Test(description="Synchronize provider - no products", groups = {"cli-providers"},enabled=true)
	public void test_syncProvider_noProduct(){
		SSHCommandResult res;
		String uid = KatelloUtils.getUniqueID();
		String provName = "syncNoProd-"+uid;
		
		// Create provider
		KatelloProvider prov = new KatelloProvider(this.cli_worker, provName, this.org_name, null, null);
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
		String uid = KatelloUtils.getUniqueID();
		String provName = "syncNoProd-"+uid;
		String prodName = "prod1Repo-"+uid;
		String repoName = "pulpF15_64bit-"+uid;
		
		// Create provider
		KatelloProvider prov = new KatelloProvider(this.cli_worker, provName, this.org_name, null, null);
		res = prov.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		// Create product
		KatelloProduct prod = new KatelloProduct(this.cli_worker, prodName, this.org_name, provName, null, null, null, null, null);
		res = prod.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		// Create repo - valid url to sync
		KatelloRepo repo = new KatelloRepo(this.cli_worker, repoName, this.org_name, prodName, PULP_RHEL6_x86_64_REPO, null, null);
		repo.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		// Sync provider
		res = prov.synchronize();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		Assert.assertTrue(getOutput(res).contains(String.format(KatelloProvider.OUT_SYNCHRONIZE, provName)), "Check - returned output string");
		
		waitfor_repodata(repo, 1);
		assert_repoSynced(repo);
	}
	
	@Test(description="Synchronize provider - multiple products", groups = {"cli-providers"},enabled=true)
	public void test_syncProvider_multiProducts(){
		SSHCommandResult res;
		String uid = KatelloUtils.getUniqueID();
		String provName = "syncNoProd-"+uid;
		String prodName1 = "pulpF15_64bit"+uid;
		String prodName2 = "pulpF15_32bit"+uid;
		String repoName1 = "pulpF15_64bit-"+uid;
		String repoName2 = "pulpF15_32bit-"+uid;
		
		// Create provider
		KatelloProvider prov = new KatelloProvider(this.cli_worker, provName, this.org_name, null, null);
		res = prov.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		// Create products
		KatelloProduct prod1 = new KatelloProduct(this.cli_worker, prodName1, this.org_name, provName, null, null, null, null, null);
		res = prod1.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		KatelloProduct prod2 = new KatelloProduct(this.cli_worker, prodName2, this.org_name, provName, null, null, null, null, null);
		res = prod2.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		// Create repos
		KatelloRepo repo1 = new KatelloRepo(this.cli_worker, repoName1, this.org_name, prodName1, PULP_RHEL6_x86_64_REPO, null, null);
		res = repo1.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		KatelloRepo repo2 = new KatelloRepo(this.cli_worker, repoName2, this.org_name, prodName2, PULP_RHEL6_i386_REPO, null, null);
		res = repo2.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		// Sync provider
		res = prov.synchronize();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		Assert.assertTrue(getOutput(res).contains(String.format(KatelloProvider.OUT_SYNCHRONIZE, provName)), "Check - returned output string");
		
		waitfor_repodata(repo1, 1);
		waitfor_repodata(repo2, 1);
		assert_repoSynced(repo1);
		assert_repoSynced(repo2);
	}
	
	// Import manifest - TODO (need smaller size file for an import).
	
	@Test(description="Try to updateRed Hat provider - name", groups = {"cli-providers"},enabled=true)
	public void test_updateProvider_RedHat_name(){
		SSHCommandResult res;
		KatelloProvider prov = new KatelloProvider(this.cli_worker, KatelloProvider.PROVIDER_REDHAT, this.org_name, null, null);
		res = prov.update("REDHAT", null, null);
		Assert.assertTrue(res.getExitCode().intValue()==166, "Check - return code (provider update)");
		Assert.assertTrue(getOutput(res).contains(KatelloProvider.ERR_REDHAT_UPDATENAME), "Check - returned error string (provider update)");
	}
	
	/**
	 * TODO
	 * # bug: 1004759
	 * @see https://bugzilla.redhat.com/show_bug.cgi?id=1004759
	 */
	@Test(description="Try to updateRed Hat provider - url", groups = {"cli-providers"}, dependsOnMethods = {"test_freshOrgDefaultRedHatProvider"}, enabled=true)
	public void test_updateProvider_RedHat_url(){
		SSHCommandResult res;
		String update_url = "https://localhost:443";
		String match_info;
		
		KatelloProvider prov = new KatelloProvider(this.cli_worker, KatelloProvider.PROVIDER_REDHAT, this.org_name, null, null);
		res = prov.update(null, update_url, null);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (provider update)");
		Assert.assertTrue(getOutput(res).contains(String.format(KatelloProvider.OUT_UPDATE,KatelloProvider.PROVIDER_REDHAT)), 
				"Check - returned error string (provider update)");
		res = new KatelloOrg(this.cli_worker, org_name, null).cli_info();
		String orgId = KatelloUtils.grepCLIOutput("ID", getOutput(res));
		// Info
		res = prov.info();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		if (prov.description == null) prov.description = "None";
		match_info = String.format(KatelloProvider.REG_REDHAT_INFO, update_url, orgId, prov.description).replaceAll("\"", "");
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(match_info),
				String.format("Provider [%s] should be found in the info",KatelloProvider.PROVIDER_REDHAT));
	}
	
	/**
	 * TODO
	 * # bug: 1004759
	 * @see https://bugzilla.redhat.com/show_bug.cgi?id=1004759
	 */
	@Test(description="Try to update custom provider - url", groups = {"cli-providers"}, enabled=true)
	public void test_updateProvider_url() {
		SSHCommandResult res;
		String uid = KatelloUtils.getUniqueID();
		String provName = "prov-"+uid;
		String match_info;
		
		// Create provider
		KatelloProvider prov = new KatelloProvider(this.cli_worker, provName, this.org_name, null, null);
		res = prov.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		
		res = prov.update(null, REPO_INECAS_ZOO3, null);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (provider update)");
		Assert.assertTrue(getOutput(res).contains(String.format(KatelloProvider.OUT_UPDATE, provName)), 
				"Check - returned error string (provider update)");
		// Info
		res = prov.info();
		if (prov.description == null) prov.description = "None";
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		match_info = String.format(KatelloProvider.REG_PROVIDER_LIST,provName,REPO_INECAS_ZOO3, prov.description).replaceAll("\"", "");
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(match_info), 
				String.format("Provider [%s] should be found in the info", provName));
		
		// Sync provider
		res = prov.synchronize();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		Assert.assertTrue(getOutput(res).contains(String.format(KatelloProvider.OUT_SYNCHRONIZE, provName)), "Check - returned output string");
	}

	@Test(description="Try to update custom provider - name", groups = {"cli-providers"}, enabled=true)
	public void test_updateProviderName() {
		SSHCommandResult res;
		String uid = KatelloUtils.getUniqueID();
		String provName = "prov-"+uid;
		String match_info;
		
		// Create provider
		KatelloProvider prov = new KatelloProvider(this.cli_worker, provName, this.org_name, null, null);
		res = prov.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		
		String new_name = "newprov-" + uid;
		
		res = prov.update(new_name, null, null);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (provider update)");
		Assert.assertTrue(getOutput(res).contains(String.format(KatelloProvider.OUT_UPDATE, new_name)), 
				"Check - returned error string (provider update)");
		
		prov.name = new_name;
		// Info
		res = prov.info();
		if (prov.description == null) prov.description = "None";
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		match_info = String.format(KatelloProvider.REG_PROVIDER_LIST,new_name, "", prov.description).replaceAll("\"", "");
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(match_info), 
				String.format("Provider [%s] should be found in the info", new_name));
	}
	
	
	@Test(description="sam only : check whether status of provider's sychronisation is displayed via cli", groups = {"headpin-cli", "cfse-ignore"}, enabled=true)
	public void test_CheckStatusProvider(){
		
		SSHCommandResult res;
		String uid = KatelloUtils.getUniqueID();
		String org_name = "org-status-" + uid;
		KatelloOrg org=new KatelloOrg(this.cli_worker, org_name,null);
		res= org.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		KatelloProvider prov = new KatelloProvider(this.cli_worker, KatelloProvider.PROVIDER_REDHAT, org_name, null, null);
		res=prov.status();
		Assert.assertTrue(res.getExitCode().intValue()==2, "Check - return code");
		Assert.assertTrue(getOutput(res).contains("headpin: error: invalid action: please see --help"),"Check - returned error string");
	}
	
	
	@Test(description="sam only : command should not contain create or delete options", groups = {"headpin-cli", "cfse-ignore"}, enabled=true)
	public void test_Check_createdel_Provider(){
		
		SSHCommandResult res;
		String uid = KatelloUtils.getUniqueID();
		String org_name = "org-create-del" + uid;
		KatelloOrg org=new KatelloOrg(this.cli_worker, org_name,null);
		res= org.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		KatelloProvider prov = new KatelloProvider(this.cli_worker, KatelloProvider.PROVIDER_REDHAT, org_name, null, null);
		res=prov.create();
		Assert.assertTrue(res.getExitCode().intValue()==2, "Check - return code");
		Assert.assertTrue(getOutput(res).contains("headpin: error: invalid action: please see --help"),"Check - returned error string");
		res=prov.delete();
		Assert.assertTrue(res.getExitCode().intValue()==2, "Check - return code");
		Assert.assertTrue(getOutput(res).contains("headpin: error: invalid action: please see --help"),"Check - returned error string");
	}

	@Test(description="test provider cancel_sync")
	public void test_cancelSync() {
		String prov_name = "provider"+KatelloUtils.getUniqueID();
		KatelloProvider prov = new KatelloProvider(cli_worker, prov_name, org_name, null, null);
		exec_result = prov.create();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (create provider)");
		exec_result = prov.cancel_sync();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (provider cancel sync)");
		Assert.assertTrue(getOutput(exec_result).contains(KatelloProvider.OUT_NO_SYNC_RUNNING), "Check output (provider cancel sync)");
	}

	@Test(description="import manifest tests")
	public void test_importManifest() {
		String manifest = "manifest.zip";
		String bad_manifest = "/tmp/badmanifest"+KatelloUtils.getUniqueID();
		KatelloUtils.scpOnClient(cli_worker.getClientHostname(), "data/"+manifest, "/tmp");

		KatelloProvider prov = new KatelloProvider(cli_worker, base_zoo_provider_name, base_org_name, null, null);
		exec_result = prov.import_manifest("/tmp/"+manifest, true);
		Assert.assertTrue(exec_result.getExitCode()==144, "Check exit code (import manifest)");
		Assert.assertTrue(getOutput(exec_result).contains(KatelloProvider.ERR_IMPORT_CUSTOM), "Check error message (import manifest)");

		prov = new KatelloProvider(cli_worker, KatelloProvider.PROVIDER_REDHAT, org_manifest, null, null);
		exec_result = prov.import_manifest(bad_manifest, true);
		Assert.assertTrue(exec_result.getExitCode()==74, "Check exit code (import manifest)");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloProvider.ERR_FILE_NOT_EXIST, bad_manifest)), "Check error message (import manifest)");

		exec_result = prov.import_manifest("/tmp/"+manifest, true);
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (import manifest)");
		Assert.assertTrue(getOutput(exec_result).contains(KatelloProvider.OUT_MANIFEST_IMPORTED), "Check output (import manifest)");
	}

	@Test(description="provider refresh products")
	public void test_refreshProducts() {
		KatelloProvider prov = new KatelloProvider(cli_worker, base_zoo_provider_name, base_org_name, null, null);
		exec_result = prov.refresh_products();
		Assert.assertTrue(exec_result.getExitCode()==144, "Check exit code (refresh products)");
		Assert.assertTrue(getOutput(exec_result).contains(KatelloProvider.ERR_REFRESH_CUSTOM), "Check output (refresh products)");
		prov = new KatelloProvider(cli_worker, KatelloProvider.PROVIDER_REDHAT, org_manifest, null, null);
		exec_result = prov.refresh_products();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (refresh products)");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloProvider.OUT_REFRESH_PRODUCTS, prov.name)), "Check output (refresh products)");
	}

	@AfterClass()
	public void tearDown() {
		new KatelloOrg(cli_worker, org_manifest, null).delete();
	}
}
