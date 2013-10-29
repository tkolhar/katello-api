package com.redhat.qe.katello.tests.cli;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.redhat.qe.Assert;
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
public class ProductTests  extends KatelloCliTestBase{
	private String org_name;
	private String prov_name;
	private String org_name2;
	private String prov_name2;
	private String prov_name3;
	private String org_manifest;
	
	@BeforeClass(description="Prepare an org to work with", groups={"cli-products","headpin-cli"}, alwaysRun=true)
	public void setup_org(){
		SSHCommandResult res;
		String uid = KatelloUtils.getUniqueID();
		this.org_name = "org"+uid;
		KatelloOrg org = new KatelloOrg(this.cli_worker, this.org_name, null);
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (org create)");		
		uid = KatelloUtils.getUniqueID();
		this.org_name2 = "org"+uid;
		org = new KatelloOrg(this.cli_worker, this.org_name2, null);
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (org create)");
		org_manifest = "org-manifest"+uid;
		exec_result = new KatelloOrg(this.cli_worker, org_manifest, null).cli_create();
	}

	@BeforeClass(description="init: katello specific, no headpin", dependsOnMethods={"setup_org"})
	public void setUp_katelloOnly(){
		SSHCommandResult res;
		String uid = KatelloUtils.getUniqueID();
		this.prov_name = "prov"+uid;
		this.prov_name3 = "prov"+KatelloUtils.getUniqueID();
		this.prov_name2 = "prov"+uid;
		KatelloProvider prov = new KatelloProvider(this.cli_worker, this.prov_name, this.org_name, null, null);
		res = prov.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (provider create)");	
		prov = new KatelloProvider(this.cli_worker, this.prov_name3, this.org_name, null, null);
		res = prov.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (provider create)");
		prov = new KatelloProvider(this.cli_worker, this.prov_name2, this.org_name2, null, null);
		res = prov.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (provider create)");
	}

    @Test(description = "List all product for orgs", groups = {"headpin-cli"})
	public void test_listProductDefaultOrg(){
    	  String providername = KatelloProvider.PROVIDER_REDHAT;
	 	  KatelloProduct list_product = new KatelloProduct(this.cli_worker, null,KatelloOrg.getDefaultOrg(),providername,null,null,null,null,null);
	 	  exec_result = list_product.cli_list();
	 	  Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
	 	  KatelloProduct prod = new KatelloProduct(cli_worker, null, null, base_org_name, null, null, null, null, null, null);
	 	  exec_result = prod.cli_list();
	 	  Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (product list)");
 	}


	@Test(description="create product - no url specified", groups = {"cli-products"}, enabled=true)
	public void test_createProduct_noUrl(){
		String uid = KatelloUtils.getUniqueID();
		String prodName = "prodCreate-"+uid;
		SSHCommandResult res;
		
		// create product
		KatelloProduct prod = new KatelloProduct(this.cli_worker, prodName, this.org_name, this.prov_name, null, null, null, null, null);
		res = prod.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");
		Assert.assertTrue(getOutput(res).contains(String.format(KatelloProduct.OUT_CREATED,prodName)), "Check - returned output string (product create)");		
		prod.assert_productExists(null,false);
	}
	
	@Test(description="create product - with single repo", groups = {"cli-products"}, enabled=true)
	public void test_createProduct_urlSingleRepo(){
		String uid = KatelloUtils.getUniqueID();
		String prodName = "prod1Repo-"+uid;
		SSHCommandResult res;
		
		// create product
		KatelloProduct prod = new KatelloProduct(this.cli_worker, prodName, this.org_name, this.prov_name, null, null, PULP_RHEL6_x86_64_REPO, null, true);
		res = prod.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");
		Assert.assertTrue(getOutput(res).contains(String.format(KatelloProduct.OUT_CREATED,prodName)), "Check - returned output string (product create)");
		prod.assert_productExists(null,false);
		
		// check - repo created - we don't know the exact repo name.
		KatelloRepo repo = new KatelloRepo(this.cli_worker, null, this.org_name, prodName, null, null, null);
		res = repo.list();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo list by product)");		
		String match_info = String.format(KatelloRepo.REG_REPO_LIST_ARCH, prodName, "x86_64").replaceAll("\"", "");
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(match_info),
				"Repo list should contain info about just created repo (requested by: org, product)");
	}
	
	@Test(description="create product - with multiple repos", groups = {"cli-products"}, enabled=true)
	public void test_createProduct_urlMultipleRepo(){
		String uid = KatelloUtils.getUniqueID();
		String prodName = "prod2Repos-"+uid;
		SSHCommandResult res, resRepos; String repoName;
		
		// create product
		KatelloProduct prod = new KatelloProduct(this.cli_worker, prodName, this.org_name, this.prov_name, null, null, PULP_RHEL6_REPO, null, true);
		res = prod.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");
		Assert.assertTrue(getOutput(res).contains(String.format(KatelloProduct.OUT_CREATED,prodName)), "Check - returned output string (product create)");		
		prod.assert_productExists(null,false);
		
		// check - 2 repos created
		KatelloRepo repo = new KatelloRepo(this.cli_worker, null, this.org_name, prodName, null, null, null);
		res = repo.list();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo list by product)");
		String match_info = String.format(KatelloRepo.REG_REPO_LIST_ARCH, prodName, "i386").replaceAll("\"", "");
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(match_info),
				"Repo list should contain info about just created repo (requested by: org, product - i386)");
		match_info = String.format(KatelloRepo.REG_REPO_LIST_ARCH, prodName, "x86_64").replaceAll("\"", "");
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(match_info),
				"Repo list should contain info about just created repo (requested by: org, product - x86_64)");
		
		// get packages count for the repos - ==0
		repo = new KatelloRepo(this.cli_worker, null, this.org_name, prodName, null, null, null);
		resRepos = repo.list();
		match_info = String.format(KatelloRepo.REG_PACKAGE_CNT, "0").replaceAll("\"", "");	
		repoName = KatelloUtils.grepCLIOutput("Name", getOutput(resRepos),1); // 1st repo
		KatelloRepo repoWithName = new KatelloRepo(this.cli_worker, repoName, this.org_name, prodName, null, null, null);
		waitfor_repodata(repoWithName, 1);
		res = repoWithName.info();
		// output to analyze - should contain: 0
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(match_info),"Repo list of the product - should contain package count 0");

		repoName = KatelloUtils.grepCLIOutput("Name", getOutput(resRepos),2); // 2nd repo
		repoWithName = new KatelloRepo(this.cli_worker, repoName, this.org_name, prodName, null, null, null);
		match_info = String.format(KatelloRepo.REG_PACKAGE_CNT, "0").replaceAll("\"", "");
		waitfor_repodata(repoWithName, 1);
		res = repoWithName.info();
		// output to analyze - should contain: 0
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(match_info),"Repo list of the product - should contain package count 0");
	}
	
	@Test(description="create product by existing name", groups = {"cli-products"}, enabled=true)
	public void test_createProduct_exists(){
		String uid = KatelloUtils.getUniqueID();
		String prodName = "existing-"+uid;
		SSHCommandResult res;
		
		// create product
		KatelloProduct prod = new KatelloProduct(this.cli_worker, prodName, this.org_name, this.prov_name, null, null, PULP_RHEL6_REPO, null, true);
		res = prod.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");
		
		// try to create product second time by the same name
		res = prod.create();
		Assert.assertFalse(res.getExitCode().intValue()==0, "Check - return code (product create)");
	}
	
	@Test(description="create product by the same name which is in other org", groups = {"cli-products"}, enabled=true)
	public void test_createProduct_sameName(){
		String uid = KatelloUtils.getUniqueID();
		String prodName = "existing-"+uid;
		SSHCommandResult res;
		
		// create product
		KatelloProduct prod = new KatelloProduct(this.cli_worker, prodName, this.org_name, this.prov_name, null, null, PULP_RHEL6_REPO, null, true);
		res = prod.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");
		
		// try to create product second time by the same name but for different org, it should work
		prod = new KatelloProduct(this.cli_worker, prodName, this.org_name2, this.prov_name2, null, null, PULP_RHEL6_REPO, null, true);
		res = prod.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");
		Assert.assertTrue(getOutput(res).contains(String.format(KatelloProduct.OUT_CREATED,prodName)), "Check - returned output string (product create)");		
		prod.assert_productExists(null,false);
	}
	
	// TODO - product creation failflows + the cases with "Description" variations.
	
	
	@Test(description="product status output check", groups = {"cli-products"}, enabled=false) //TODO - gkhachik via content views
	public void test_productStatus() {
		String prodName1 = "prod1-"+KatelloUtils.getUniqueID();
		String prodName2 = "prod2-"+KatelloUtils.getUniqueID();
		String envName1 = "env1-" + KatelloUtils.getUniqueID();
		
		SSHCommandResult res;		

		// create product
		KatelloProduct prod1 = new KatelloProduct(this.cli_worker, prodName1, this.org_name, this.prov_name, null, null, PULP_RHEL6_i386_REPO, null, true);
		res = prod1.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");
		
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
//		res = prod1.promote(envName1);
//		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product promote)");
		prod1.syncState = "Finished";

		KatelloProduct prod2 = new KatelloProduct(this.cli_worker, prodName2, this.org_name, this.prov_name3, null, null, null, null, true);
		res = prod2.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");
		
		assert_productStatus(prod1);
		
		assert_productStatus(prod2);
		
	}

	
	@Test(description="list the products by provider", groups = {"cli-products"}, enabled=true)
	public void test_listProduct_provider() {
		String prodName1 = "prod1-"+KatelloUtils.getUniqueID();
		String prodName2 = "prod2-"+KatelloUtils.getUniqueID();
		String prodName3 = "prod3-"+KatelloUtils.getUniqueID();
		SSHCommandResult res;
		
		// create product
		KatelloProduct prod1 = new KatelloProduct(this.cli_worker, prodName1, this.org_name, this.prov_name, null, null, null, null, true);
		res = prod1.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");
		
		KatelloProduct prod2 = new KatelloProduct(this.cli_worker, prodName2, this.org_name, this.prov_name, null, null, null, null, true);
		res = prod2.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");
		
		KatelloProduct prod3 = new KatelloProduct(this.cli_worker, prodName3, this.org_name, this.prov_name3, null, null, null, null, true);
		res = prod3.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");
		
		SSHCommandResult exec_result = prod1.cli_list_provider(this.prov_name);
		
		assert_productList(exec_result, Arrays.asList(prod1, prod2), Arrays.asList(prod3));
	}

	@Test(description="list the products by environment", groups = {"cli-products"}, enabled=false)
	public void test_listProduct_environment() {
		String prodName1 = "prod1-"+KatelloUtils.getUniqueID();
		String prodName2 = "prod2-"+KatelloUtils.getUniqueID();
		String prodName3 = "prod3-"+KatelloUtils.getUniqueID();
		String envName1 = "env1-" + KatelloUtils.getUniqueID();
		String envName2 = "env2-" + KatelloUtils.getUniqueID();
		
		SSHCommandResult res;		

		// create product
		KatelloProduct prod1 = new KatelloProduct(this.cli_worker, prodName1, this.org_name, this.prov_name, null, null, PULP_RHEL6_i386_REPO, null, true);
		res = prod1.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");
		
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
		KatelloUtils.promoteProductToEnvironment(this.cli_worker, this.org_name, prodName1, envName1);

		// create product
		KatelloProduct prod2 = new KatelloProduct(this.cli_worker, prodName2, this.org_name, this.prov_name, null, null, REPO_INECAS_ZOO3, null, true);
		res = prod2.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");
		
		// sync product
		res = prod2.synchronize();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product synchronize)");

		// create env.
		KatelloEnvironment env2 = new KatelloEnvironment(this.cli_worker, envName2, null, this.org_name, KatelloEnvironment.LIBRARY);
		res = env2.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (environment create)");
		
		// sync product (otherwise promote will fail)
		res = prod2.synchronize();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product synchronize)");
		
		// promote product to the env.
		KatelloUtils.promoteProductToEnvironment(this.cli_worker, this.org_name, prodName2, envName2);

		KatelloProduct prod3 = new KatelloProduct(this.cli_worker, prodName3, this.org_name, this.prov_name3, null, null, null, null, true);
		res = prod3.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");
		
		res = prod1.status();
		String lastSync1 = KatelloUtils.grepCLIOutput("Last Sync", getOutput(res).trim(),1);
		prod1.lastSync = lastSync1;
		
		res = prod2.status();
		String lastSync2 = KatelloUtils.grepCLIOutput("Last Sync", getOutput(res).trim(),1);
		prod2.lastSync = lastSync2;
		
		res = prod1.cli_list();
		
		assert_productList(res, Arrays.asList(prod1), Arrays.asList(prod2, prod3));
		
		res = prod1.cli_list();
		
		assert_productList(res, Arrays.asList(prod2), Arrays.asList(prod1, prod3));
	}
	
	@Test(description="promote product", groups = {"cli-products"}, enabled=true)
	public void test_promoteProduct_NoRepos(){
		String uid = KatelloUtils.getUniqueID();
		String prodName = "promoNoRepo-"+uid;
		SSHCommandResult res;
		
		// create product
		KatelloProduct prod = new KatelloProduct(this.cli_worker, prodName, this.org_name, this.prov_name, null, null, null, null, null);
		res = prod.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");
		Assert.assertTrue(getOutput(res).contains(String.format(KatelloProduct.OUT_CREATED,prodName)), "Check - returned output string (product create)");
		prod.assert_productExists(null,false);
		
		// create env.
		String envName = "dev-"+uid;
		KatelloEnvironment env = new KatelloEnvironment(this.cli_worker, envName, null, this.org_name, KatelloEnvironment.LIBRARY);
		res = env.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (environment create)");
		
		// sync product (otherwise promote will fail)
		res = prod.synchronize();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product synchronize)");
		
		// promote product to the env.
		KatelloUtils.promoteProductToEnvironment(cli_worker, org_name, prodName, envName);
	}
	
	@Test(description="promote product", groups = {"cli-products"}, enabled=false)
	public void test_promoteProduct_OneRepo(){
		String uid = KatelloUtils.getUniqueID();
		String prodName = "promo1Repo-"+uid;
		String envName = "dev-"+uid;
		SSHCommandResult res;
		
		// create product
		KatelloProduct prod = new KatelloProduct(this.cli_worker, prodName, this.org_name, this.prov_name, null, null, PULP_RHEL6_x86_64_REPO, null, true);
		res = prod.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");
		Assert.assertTrue(getOutput(res).contains(String.format(KatelloProduct.OUT_CREATED,prodName)), "Check - returned output string (product create)");
		prod.assert_productExists(null, false);
		
		// create env.
		KatelloEnvironment env = new KatelloEnvironment(this.cli_worker, envName, null, this.org_name, KatelloEnvironment.LIBRARY);
		res = env.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (environment create)");
		
		// sync product (otherwise promote will fail)
		res = prod.synchronize();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product synchronize)");
		
		// promote product to the env.
		String contentView = KatelloUtils.promoteProductToEnvironment(cli_worker, org_name, prodName, envName);

		// product list --environment (1 result - just the product promoted)
		res = prod.cli_list();
		if (prod.syncPlanName == null) prod.syncPlanName = "None";
		if (prod.lastSync == null) prod.lastSync = "";
		if (prod.gpgkey == null) prod.gpgkey = "";
		
		String match_info = String.format(KatelloProduct.REG_PROD_LIST, prodName, prov_name, prod.syncPlanName, prod.lastSync, prod.gpgkey).replaceAll("\"", "");
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(match_info),
				"Product list by environment - just promoted product");
		
		// repo list --environment --content_view (1 result).
		// check - repo created - we don't know the exact repo name.
		KatelloRepo repo = new KatelloRepo(this.cli_worker, null,this.org_name,prodName,null,null,null);
		res = repo.list(envName, contentView);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo list by product)");		
		match_info = String.format(KatelloRepo.REG_REPO_LIST_ARCH, prodName, "x86_64").replaceAll("\"", "");
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(match_info),
				"Repo list should contain info about just created repo (requested by: org, environment)");
	}
	
	@Test(description="promote product", groups = {"cli-products"}, enabled=false)
	public void test_promoteProduct_MultipleRepos(){
		String uid = KatelloUtils.getUniqueID();
		String prodName = "promo1Repo-"+uid;
		String envName = "dev-"+uid;
		SSHCommandResult res;
		
		// create product
		KatelloProduct prod = new KatelloProduct(this.cli_worker, prodName, this.org_name, this.prov_name, null, null, PULP_RHEL6_REPO, null, true);
		res = prod.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");
		Assert.assertTrue(getOutput(res).contains(String.format(KatelloProduct.OUT_CREATED,prodName)), "Check - returned output string (product create)");
		prod.assert_productExists(null, false);
		
		// create env.
		KatelloEnvironment env = new KatelloEnvironment(this.cli_worker, envName, null, this.org_name, KatelloEnvironment.LIBRARY);
		res = env.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (environment create)");
		
		// sync product (otherwise promote will fail)
		res = prod.synchronize();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product synchronize)");
		
		// promote product to the env.
		String contentView = KatelloUtils.promoteProductToEnvironment(cli_worker, org_name, prodName, envName);

		// product list --environment (1 result - just the product promoted)
		res = prod.cli_list();
		if (prod.syncPlanName == null) prod.syncPlanName = "None";
		if (prod.lastSync == null) prod.lastSync = "";
		if (prod.gpgkey == null) prod.gpgkey = "";
		
		String match_info = String.format(KatelloProduct.REG_PROD_LIST, prodName, prov_name, prod.syncPlanName, prod.lastSync, prod.gpgkey).replaceAll("\"", "");
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(match_info),
				"Product list by environment - just promoted product");
		
		// repo list --environment --content_view (2 entries).
		KatelloRepo repo = new KatelloRepo(this.cli_worker, null,this.org_name,prodName,null,null,null);
		res = repo.list(envName, contentView);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo list by product)");
		match_info = String.format(KatelloRepo.REG_REPO_LIST_ARCH, prodName, "i386").replaceAll("\"", "");
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(match_info),
				"Repo list should contain info about just created repo (requested by: org, product - i386)");
		match_info = String.format(KatelloRepo.REG_REPO_LIST_ARCH, prodName, "x86_64").replaceAll("\"", "");
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(match_info),
				"Repo list should contain info about just created repo (requested by: org, product - x86_64)");
	}

	@Test(description="sync product - single repo", groups = {"cli-products"}, enabled=true)
	public void test_syncronizeProduct_SingleRepo(){
		String uid = KatelloUtils.getUniqueID();
		String prodName = "sync1Repo-"+uid;
		SSHCommandResult res;

		// create product
		KatelloProduct prod = new KatelloProduct(this.cli_worker, prodName, this.org_name, this.prov_name, null, null, PULP_RHEL6_x86_64_REPO, null, true);
		res = prod.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");
		Assert.assertTrue(getOutput(res).contains(String.format(KatelloProduct.OUT_CREATED,prodName)), "Check - returned output string (product create)");
		prod.assert_productExists(null, false);
		
		// sync product
		res = prod.synchronize();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product synchronize)");
		Assert.assertTrue(getOutput(res).contains(String.format(KatelloProduct.OUT_SYNCHRONIZED,prodName)), "Check - returned output string (product synchronize)");		
		
		KatelloRepo repo = new KatelloRepo(this.cli_worker, null,this.org_name,prodName,null,null,null);
		res = repo.list();
		String repoName = KatelloUtils.grepCLIOutput("Name", res.getStdout(),1); // 1st repo
		repo = new KatelloRepo(this.cli_worker, repoName,this.org_name,prodName,null,null,null);
		
		waitfor_repodata(repo, 1);
		
		// get packages count for the repo - !=0
		res = repo.info();
		String match_info = String.format(KatelloRepo.REG_PACKAGE_CNT, "0").replaceAll("\"", "");
		Assert.assertFalse(getOutput(res).replaceAll("\n", "").matches(match_info),
				"Repo list of the product - should not contain package count 0 (after product synchronize)");
	}

	@Test(description="sync product - multiple repos", groups = {"cli-products"}, enabled=true)
	public void test_syncronizeProduct_MultipleRepos(){
		String uid = KatelloUtils.getUniqueID();
		String prodName = "syncManyRepos-"+uid;
		SSHCommandResult res, resRepos; String repoName;

		// create product
		KatelloProduct prod = new KatelloProduct(this.cli_worker, prodName, this.org_name, this.prov_name, null, null, PULP_RHEL6_REPO, null, true);
		res = prod.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");
		Assert.assertTrue(getOutput(res).contains(String.format(KatelloProduct.OUT_CREATED,prodName)), "Check - returned output string (product create)");
		prod.assert_productExists(null, false);
		
		// sync product
		res = prod.synchronize();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product synchronize)");
		Assert.assertTrue(getOutput(res).contains(String.format(KatelloProduct.OUT_SYNCHRONIZED,prodName)), "Check - returned output string (product synchronize)");
		
		// get packages count for the repos - !=0
		KatelloRepo repo = new KatelloRepo(this.cli_worker, null,this.org_name,prodName,null,null,null);
		resRepos = repo.list();
		String match_info = String.format(KatelloRepo.REG_PACKAGE_CNT, "0").replaceAll("\"", "");	
		
		repoName = KatelloUtils.grepCLIOutput("Name", resRepos.getStdout(),1); // 1st repo
		repo = new KatelloRepo(this.cli_worker, repoName,this.org_name,prodName,null,null,null);
		waitfor_repodata(repo, 1);
		res = repo.info();
		// our line to analyze - should not contain: 0
		Assert.assertFalse(getOutput(res).matches(match_info),"Repo list of the product - should not contain package count 0 (after product synchronize)");

		repoName = KatelloUtils.grepCLIOutput("Name", resRepos.getStdout(),2); // 2nd repo
		repo = new KatelloRepo(this.cli_worker, repoName,this.org_name,prodName,null,null,null);
		waitfor_repodata(repo, 1);
		res = repo.info();
		// our line to analyze - should not contain: 0
		Assert.assertFalse(getOutput(res).matches(match_info),"Repo list of the product - should not contain package count 0 (after product synchronize)");
	}

	@Test(description="delete product - included in some changeset", groups = {"cli-products"}, enabled=false) // TODO - gkhachik via content views
	public void test_deleteProduct_InChangeset(){
		String uid = KatelloUtils.getUniqueID();
		String prodName = "delProd1-"+uid;
		String envName_dev = "dev-"+uid;
		SSHCommandResult res;
		
		// create product
		KatelloProduct prod = new KatelloProduct(this.cli_worker, prodName, this.org_name, this.prov_name, null, null, PULP_RHEL6_x86_64_REPO, null, true);
		res = prod.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");
		// sync product
		res = prod.synchronize();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product synchronize)");
		// create env. - dev
		KatelloEnvironment env = new KatelloEnvironment(this.cli_worker, envName_dev, null, this.org_name, KatelloEnvironment.LIBRARY);
		res = env.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (environment create)");
		
		String contentView = KatelloUtils.promoteProductToEnvironment(cli_worker, org_name, prodName, envName_dev);
		
		// Assertions - repo list by env and content view
		KatelloRepo repo = new KatelloRepo(this.cli_worker, null,this.org_name,prodName,null,null,null);
		res = repo.list(envName_dev, contentView);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo list --environment)");
		String match_info = String.format(KatelloRepo.REG_REPO_LIST_ARCH, prodName, "x86_64").replaceAll("\"", "");
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(match_info),
				"Repo list by environment - should contain info");
		// Assertions - product list
		res = prod.cli_list();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product list --environment)");
		if (prod.syncPlanName == null) prod.syncPlanName = "None";
		if (prod.lastSync == null) prod.lastSync = "";
		if (prod.gpgkey == null) prod.gpgkey = "";
		
		match_info = String.format(KatelloProduct.REG_PROD_LIST, prodName, prov_name, prod.syncPlanName, prod.lastSync, prod.gpgkey).replaceAll("\"", "");
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(match_info), 
				"List should contain info about product (requested by: environment)");
		
		// Final action - DELETE the product
		// ... but get its id first. To check the output string.
		res = prod.status();
		String prodId = KatelloUtils.grepCLIOutput("ID", res.getStdout());
		res = prod.delete();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");
		Assert.assertTrue(getOutput(res).contains(String.format(KatelloProduct.OUT_DELETED,prodId)), "Check - returned output string (product delete)");
		
		// Assertions - product list of the org
		res = prod.cli_list();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product list --provider)");
		match_info = String.format(KatelloProduct.REG_PROD_LIST, prodName, prov_name, prod.syncPlanName, prod.lastSync, prod.gpgkey).replaceAll("\"", "");
		Assert.assertFalse(getOutput(res).replaceAll("\n", "").matches(match_info), 
				"Check - list should NOT contain info about product (deleted already)");
		
		// Assertions - repo list by product
		repo = new KatelloRepo(this.cli_worker, null,this.org_name,prodName,null,null,null);
		res = repo.list();
		Assert.assertTrue(res.getExitCode().intValue()==65, "Check - return code (repo list --product)"); // Bug#750464
		Assert.assertTrue(getOutput(res).equals(String.format(KatelloProduct.ERR_COULD_NOT_FIND_PRODUCT, prodName,org_name)), "Check - `repo list --product` output string");
		
		// Assertions - repo list by env and content view.
		repo = new KatelloRepo(this.cli_worker, null,this.org_name,prodName,null,null,null);
		res = repo.list(envName_dev, contentView);
		Assert.assertTrue(res.getExitCode().intValue()==65, "Check - return code (repo list --environment)");
		Assert.assertTrue(getOutput(res).equals(String.format(KatelloProduct.ERR_COULD_NOT_FIND_PRODUCT, prodName,org_name)), "Check - `repo list --environment` output string");
		match_info = String.format(KatelloRepo.REG_REPO_LIST_ARCH, prodName, "x86_64").replaceAll("\"", "");
		Assert.assertFalse(getOutput(res).replaceAll("\n", "").matches(match_info), "Check - `repo list --environment` output string");
		
		// Assertions - product list.
		res = prod.cli_list();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product list --environment)");
		match_info = String.format(KatelloProduct.REG_PROD_LIST, prodName, prov_name, prod.syncPlanName, prod.lastSync, prod.gpgkey).replaceAll("\"", "");
		Assert.assertFalse(getOutput(res).replaceAll("\n", "").matches(match_info), 
				"Check - list should NOT contain info about product (deleted already)");		
	}

	@Test(description="test enable/disable repository sets")
	public void test_repoSetEnableDisable() {
		// import manifest
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (create org");
		KatelloUtils.scpOnClient(cli_worker.getClientHostname(), "data/"+KatelloProvider.MANIFEST_CLI2, "/tmp");
		exec_result = new KatelloProvider(cli_worker, KatelloProvider.PROVIDER_REDHAT, org_manifest, null, null).import_manifest("/tmp/"+KatelloProvider.MANIFEST_CLI2, true);
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (import manifest)");
		KatelloProduct prod = new KatelloProduct(cli_worker, KatelloProduct.RHEL_SERVER, org_manifest, KatelloProvider.PROVIDER_REDHAT, null, null, null, null, null);
		// list repo sets
		exec_result = prod.repository_sets();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check exit code (repo sets)");
		// enable reposet
		exec_result = prod.repository_set_enable(KatelloProduct.REPOSET_RHEL6_RPMS);
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check exit code (repo set enable)");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloProduct.OUT_REPO_SET_ENABLED, KatelloProduct.REPOSET_RHEL6_RPMS)), "Check outpu (enable repo set)");
		KatelloRepo repo = new KatelloRepo(cli_worker, KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT, org_manifest, "Red Hat Enterprise Linux Server", null, null, null);
		exec_result = repo.listAll();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check exit code (repo list)");
		Assert.assertTrue(getOutput(exec_result).contains(KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT), "Check outpu (repo list)");
		// disable repo set
		exec_result = prod.repository_set_disable(KatelloProduct.REPOSET_RHEL6_RPMS);
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check exit code (repo set enable)");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloProduct.OUT_REPO_SET_DISABLED, KatelloProduct.REPOSET_RHEL6_RPMS)), "Check outpu (enable repo set)");
	}

	@Test(description="cancel synchronization")
	public void test_cancelSync() {
		KatelloProduct prod = new KatelloProduct(cli_worker, base_zoo_product_name, base_org_name, base_zoo_provider_name, null, null, null, null, null);
		exec_result = prod.cancel_sync();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check exit code (cancel sync)");
		Assert.assertTrue(getOutput(exec_result).equals(KatelloProduct.OUT_NO_SYNC_RUNNING), "Check output (cancel sync)");
	}

	@AfterClass(alwaysRun=true)
	public void tearDown() {
		new KatelloOrg(this.cli_worker, org_manifest, null).delete();
	}

	private void assert_productList(SSHCommandResult exec_result, List<KatelloProduct> products, List<KatelloProduct> excludeProducts) {

		//products that exist in list
		for(KatelloProduct prod : products) {
			if (prod.syncPlanName == null) prod.syncPlanName = "None";
			if (prod.lastSync == null) prod.lastSync = "never";
			if (prod.gpgkey == null) prod.gpgkey = "";
			
			String match_info = String.format(KatelloProduct.REG_PROD_LIST, prod.getName(), prod.provider, prod.syncPlanName, prod.lastSync, prod.gpgkey).replaceAll("\"", "");
			Assert.assertTrue(getOutput(exec_result).replaceAll("\n", " ").matches(match_info), String.format("Product [%s] should be found in the result list", prod.getName()));
		}
		
		//products that should not exist in list
		for(KatelloProduct prod : excludeProducts) {
			if (prod.syncPlanName == null) prod.syncPlanName = "None";
			if (prod.lastSync == null) prod.lastSync = "never";
			if (prod.gpgkey == null) prod.gpgkey = "";
			
			String match_info = String.format(KatelloProduct.REG_PROD_LIST, prod.getName(), prod.provider, prod.syncPlanName, prod.lastSync, prod.gpgkey).replaceAll("\"", "");
			Assert.assertFalse(getOutput(exec_result).replaceAll("\n", " ").matches(match_info), String.format("Product [%s] should be found in the result list", prod.getName()));
		}
	}
	
	private String assert_productStatus(KatelloProduct product) {
		if (product.syncState == null) product.syncState = "Not synced";

		SSHCommandResult res = product.status();

		String match_info = String.format(KatelloProduct.REG_PROD_STATUS, product.name, product.provider, product.syncState).replaceAll("\"", "");
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		log.finest(String.format("Product (status) match regex: [%s]", match_info));
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(match_info), String.format("Product [%s] should be found in the result", product.name));

		String lastSync = KatelloUtils.grepCLIOutput("Last Sync", getOutput(res).trim(),1);
		if (product.syncState.equals("Not synced")) Assert.assertEquals(lastSync, "never");
		else Assert.assertMatch(lastSync, KatelloProduct.REG_PROD_LASTSYNC);
		
		return KatelloUtils.grepCLIOutput("ID", getOutput(res).trim(),1);
	}
}
