package com.redhat.qe.katello.tests.cli;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.obj.KatelloChangeset;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;


public class ProductTests  extends KatelloCliTestScript{

	private String org_name;
	private String prov_name;
	private String org_name2;
	private String prov_name2;
	private String prov_name3;
	
	@BeforeClass(description="Prepare an org to work with", groups = {"cli-product"}, alwaysRun=false)
	public void setup_org(){
		SSHCommandResult res;
		String uid = KatelloUtils.getUniqueID();
		this.org_name = "org"+uid;
		this.prov_name = "prov"+uid;
		this.prov_name3 = "prov"+KatelloUtils.getUniqueID();;
		KatelloOrg org = new KatelloOrg(this.org_name, null);
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (org create)");
		KatelloProvider prov = new KatelloProvider(this.prov_name, this.org_name, null, null);
		res = prov.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (provider create)");
		
		prov = new KatelloProvider(this.prov_name3, this.org_name, null, null);
		res = prov.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (provider create)");
		
		uid = KatelloUtils.getUniqueID();
		this.org_name2 = "org"+uid;
		this.prov_name2 = "prov"+uid;
		org = new KatelloOrg(this.org_name2, null);
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (org create)");
		prov = new KatelloProvider(this.prov_name2, this.org_name2, null, null);
		res = prov.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (provider create)");
	}
	

    @Test(description = "List all product for orgs", groups = {"headpin-cli"})
	public void test_listProductDefaultOrg(){
      
    	  String providername = "Red Hat";
	 	  KatelloProduct list_product = new KatelloProduct(null,KatelloProduct.Default_Org,providername,null,null,null,null,null);
	 	  SSHCommandResult res = list_product.cli_list();
	 	  Assert.assertEquals(res.getExitCode().intValue(), 0, "Check - return code");
 	}


	@Test(description="create product - no url specified", groups = {"cli-products"}, enabled=true)
	public void test_createProduct_noUrl(){
		String uid = KatelloUtils.getUniqueID();
		String prodName = "prodCreate-"+uid;
		SSHCommandResult res;
		
		// create product
		KatelloProduct prod = new KatelloProduct(prodName, this.org_name, this.prov_name, null, null, null, null, null);
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
		KatelloProduct prod = new KatelloProduct(prodName, this.org_name, this.prov_name, null, null, PULP_RHEL6_x86_64_REPO, null, true);
		res = prod.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");
		Assert.assertTrue(getOutput(res).contains(String.format(KatelloProduct.OUT_CREATED,prodName)), "Check - returned output string (product create)");
		prod.assert_productExists(null,false);
		
		// check - repo created - we don't know the exact repo name.
		KatelloRepo repo = new KatelloRepo(null, this.org_name, prodName, null, null, null);
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
		KatelloProduct prod = new KatelloProduct(prodName, this.org_name, this.prov_name, null, null, PULP_RHEL6_REPO, null, true);
		res = prod.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");
		Assert.assertTrue(getOutput(res).contains(String.format(KatelloProduct.OUT_CREATED,prodName)), "Check - returned output string (product create)");		
		prod.assert_productExists(null,false);
		
		// check - 2 repos created
		KatelloRepo repo = new KatelloRepo(null, this.org_name, prodName, null, null, null);
		res = repo.list();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo list by product)");
		String match_info = String.format(KatelloRepo.REG_REPO_LIST_ARCH, prodName, "i386").replaceAll("\"", "");
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(match_info),
				"Repo list should contain info about just created repo (requested by: org, product - i386)");
		match_info = String.format(KatelloRepo.REG_REPO_LIST_ARCH, prodName, "x86_64").replaceAll("\"", "");
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(match_info),
				"Repo list should contain info about just created repo (requested by: org, product - x86_64)");
		
		// get packages count for the repos - ==0
		repo = new KatelloRepo(null, this.org_name, prodName, null, null, null);
		resRepos = repo.list();
		match_info = String.format(KatelloRepo.REG_PACKAGE_CNT, "0").replaceAll("\"", "");	
		repoName = KatelloCli.grepCLIOutput("Name", getOutput(resRepos),1); // 1st repo
		KatelloRepo repoWithName = new KatelloRepo(repoName, this.org_name, prodName, null, null, null);
		res = repoWithName.info();
		// output to analyze - should contain: 0
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(match_info),"Repo list of the product - should contain package count 0");

		repoName = KatelloCli.grepCLIOutput("Name", getOutput(resRepos),2); // 2nd repo
		repoWithName = new KatelloRepo(repoName, this.org_name, prodName, null, null, null);
		match_info = String.format(KatelloRepo.REG_PACKAGE_CNT, "0").replaceAll("\"", "");
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
		KatelloProduct prod = new KatelloProduct(prodName, this.org_name, this.prov_name, null, null, PULP_RHEL6_REPO, null, true);
		res = prod.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");
		
		// try to create product second time by the same name
		res = prod.create();
		Assert.assertTrue(res.getExitCode().intValue() == 144, "Check - return code (product create)");
		Assert.assertTrue(getOutput(res).trim().contains("Validation failed: Pulp id has already been taken"),
				"Check - error message pulp id is taken");
	}
	
	@Test(description="create product by the same name which is in other org", groups = {"cli-products"}, enabled=true)
	public void test_createProduct_sameName(){
		String uid = KatelloUtils.getUniqueID();
		String prodName = "existing-"+uid;
		SSHCommandResult res;
		
		// create product
		KatelloProduct prod = new KatelloProduct(prodName, this.org_name, this.prov_name, null, null, PULP_RHEL6_REPO, null, true);
		res = prod.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");
		
		// try to create product second time by the same name but for different org, it should work
		prod = new KatelloProduct(prodName, this.org_name2, this.prov_name2, null, null, PULP_RHEL6_REPO, null, true);
		res = prod.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");
		Assert.assertTrue(getOutput(res).contains(String.format(KatelloProduct.OUT_CREATED,prodName)), "Check - returned output string (product create)");		
		prod.assert_productExists(null,false);
	}
	
	// TODO - product creation failflows + the cases with "Description" variations.
	
	
	@Test(description="product status output check", groups = {"cli-products"}, enabled=true)
	public void test_productStatus() {
		String prodName1 = "prod1-"+KatelloUtils.getUniqueID();
		String prodName2 = "prod2-"+KatelloUtils.getUniqueID();
		String envName1 = "env1-" + KatelloUtils.getUniqueID();
		
		SSHCommandResult res;		

		// create product
		KatelloProduct prod1 = new KatelloProduct(prodName1, this.org_name, this.prov_name, null, null, PULP_RHEL6_i386_REPO, null, true);
		res = prod1.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");
		
		// sync product
		res = prod1.synchronize();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product synchronize)");

		// create env.
		KatelloEnvironment env1 = new KatelloEnvironment(envName1, null, this.org_name, KatelloEnvironment.LIBRARY);
		res = env1.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (environment create)");
		
		// sync product (otherwise promote will fail)
		res = prod1.synchronize();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product synchronize)");
		
		// promote product to the env.
		res = prod1.promote(envName1);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product promote)");
		prod1.syncState = "Finished";

		KatelloProduct prod2 = new KatelloProduct(prodName2, this.org_name, this.prov_name3, null, null, null, null, true);
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
		KatelloProduct prod1 = new KatelloProduct(prodName1, this.org_name, this.prov_name, null, null, null, null, true);
		res = prod1.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");
		
		KatelloProduct prod2 = new KatelloProduct(prodName2, this.org_name, this.prov_name, null, null, null, null, true);
		res = prod2.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");
		
		KatelloProduct prod3 = new KatelloProduct(prodName3, this.org_name, this.prov_name3, null, null, null, null, true);
		res = prod3.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");
		
		SSHCommandResult exec_result = prod1.cli_list_provider(this.prov_name);
		
		assert_productList(exec_result, Arrays.asList(prod1, prod2), Arrays.asList(prod3));
	}

	@Test(description="list the products by environment", groups = {"cli-products"}, enabled=true)
	public void test_listProduct_environment() {
		String prodName1 = "prod1-"+KatelloUtils.getUniqueID();
		String prodName2 = "prod2-"+KatelloUtils.getUniqueID();
		String prodName3 = "prod3-"+KatelloUtils.getUniqueID();
		String envName1 = "env1-" + KatelloUtils.getUniqueID();
		String envName2 = "env2-" + KatelloUtils.getUniqueID();
		
		SSHCommandResult res;		

		// create product
		KatelloProduct prod1 = new KatelloProduct(prodName1, this.org_name, this.prov_name, null, null, PULP_RHEL6_i386_REPO, null, true);
		res = prod1.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");
		
		// sync product
		res = prod1.synchronize();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product synchronize)");

		// create env.
		KatelloEnvironment env1 = new KatelloEnvironment(envName1, null, this.org_name, KatelloEnvironment.LIBRARY);
		res = env1.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (environment create)");
		
		// sync product (otherwise promote will fail)
		res = prod1.synchronize();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product synchronize)");
		
		// promote product to the env.
		res = prod1.promote(envName1);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product promote)");

		// create product
		KatelloProduct prod2 = new KatelloProduct(prodName2, this.org_name, this.prov_name, null, null, REPO_INECAS_ZOO3, null, true);
		res = prod2.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");
		
		// sync product
		res = prod2.synchronize();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product synchronize)");

		// create env.
		KatelloEnvironment env2 = new KatelloEnvironment(envName2, null, this.org_name, KatelloEnvironment.LIBRARY);
		res = env2.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (environment create)");
		
		// sync product (otherwise promote will fail)
		res = prod2.synchronize();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product synchronize)");
		
		// promote product to the env.
		res = prod2.promote(envName2);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product promote)");

		KatelloProduct prod3 = new KatelloProduct(prodName3, this.org_name, this.prov_name3, null, null, null, null, true);
		res = prod3.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");
		
		res = prod1.status();
		String lastSync1 = KatelloCli.grepCLIOutput("Last Sync", getOutput(res).trim(),1);
		prod1.lastSync = lastSync1;
		
		res = prod2.status();
		String lastSync2 = KatelloCli.grepCLIOutput("Last Sync", getOutput(res).trim(),1);
		prod2.lastSync = lastSync2;
		
		res = prod1.cli_list(envName1);
		
		assert_productList(res, Arrays.asList(prod1), Arrays.asList(prod2, prod3));
		
		res = prod1.cli_list(envName2);
		
		assert_productList(res, Arrays.asList(prod2), Arrays.asList(prod1, prod3));
	}
	
	@Test(description="promote product", groups = {"cli-products"}, enabled=true)
	public void test_promoteProduct_NoRepos(){
		String uid = KatelloUtils.getUniqueID();
		String prodName = "promoNoRepo-"+uid;
		SSHCommandResult res;
		
		// create product
		KatelloProduct prod = new KatelloProduct(prodName, this.org_name, this.prov_name, null, null, null, null, null);
		res = prod.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");
		Assert.assertTrue(getOutput(res).contains(String.format(KatelloProduct.OUT_CREATED,prodName)), "Check - returned output string (product create)");
		prod.assert_productExists(null,false);
		
		// create env.
		String envName = "dev-"+uid;
		KatelloEnvironment env = new KatelloEnvironment(envName, null, this.org_name, KatelloEnvironment.LIBRARY);
		res = env.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (environment create)");
		
		// sync product (otherwise promote will fail)
		res = prod.synchronize();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product synchronize)");
		
		// promote product to the env.
		res = prod.promote(envName);
		Assert.assertTrue(res.getExitCode().intValue()==148, "Check - return code (product promote)");
		Assert.assertTrue(getOutput(res).contains(String.format(KatelloProduct.ERR_PROMOTE_NOREPOS,prodName)), "Check - returned output string (product promote)");
	}
	
	@Test(description="promote product", groups = {"cli-products"}, enabled=true)
	public void test_promoteProduct_OneRepo(){
		String uid = KatelloUtils.getUniqueID();
		String prodName = "promo1Repo-"+uid;
		String envName = "dev-"+uid;
		SSHCommandResult res;
		
		// create product
		KatelloProduct prod = new KatelloProduct(prodName, this.org_name, this.prov_name, null, null, PULP_RHEL6_x86_64_REPO, null, true);
		res = prod.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");
		Assert.assertTrue(getOutput(res).contains(String.format(KatelloProduct.OUT_CREATED,prodName)), "Check - returned output string (product create)");
		prod.assert_productExists(null, false);
		
		// create env.
		KatelloEnvironment env = new KatelloEnvironment(envName, null, this.org_name, KatelloEnvironment.LIBRARY);
		res = env.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (environment create)");
		
		// sync product (otherwise promote will fail)
		res = prod.synchronize();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product synchronize)");
		
		// promote product to the env.
		res = prod.promote(envName);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product promote)");
		Assert.assertTrue(getOutput(res).contains(String.format(KatelloProduct.OUT_PROMOTED,prodName,envName)), "Check - returned output string (product promote)");
		
		// product list --environment (1 result - just the product promoted)
		res = prod.cli_list(envName);
		if (prod.syncPlanName == null) prod.syncPlanName = "None";
		if (prod.lastSync == null) prod.lastSync = "";
		if (prod.gpgkey == null) prod.gpgkey = "";
		
		String match_info = String.format(KatelloProduct.REG_PROD_LIST, prodName, prov_name, prod.syncPlanName, prod.lastSync, prod.gpgkey).replaceAll("\"", "");
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(match_info),
				"Product list by environment - just promoted product");
		
		// repo list --environment (1 result).
		// check - repo created - we don't know the exact repo name.
		KatelloRepo repo = new KatelloRepo(null,this.org_name,prodName,null,null,null);
		res = repo.list(envName);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo list by product)");		
		match_info = String.format(KatelloRepo.REG_REPO_LIST_ARCH, prodName, "x86_64").replaceAll("\"", "");
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(match_info),
				"Repo list should contain info about just created repo (requested by: org, environment)");
	}
	
	@Test(description="promote product", groups = {"cli-products"}, enabled=true)
	public void test_promoteProduct_MultipleRepos(){
		String uid = KatelloUtils.getUniqueID();
		String prodName = "promo1Repo-"+uid;
		String envName = "dev-"+uid;
		SSHCommandResult res;
		
		// create product
		KatelloProduct prod = new KatelloProduct(prodName, this.org_name, this.prov_name, null, null, PULP_RHEL6_REPO, null, true);
		res = prod.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");
		Assert.assertTrue(getOutput(res).contains(String.format(KatelloProduct.OUT_CREATED,prodName)), "Check - returned output string (product create)");
		prod.assert_productExists(null, false);
		
		// create env.
		KatelloEnvironment env = new KatelloEnvironment(envName, null, this.org_name, KatelloEnvironment.LIBRARY);
		res = env.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (environment create)");
		
		// sync product (otherwise promote will fail)
		res = prod.synchronize();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product synchronize)");
		
		// promote product to the env.
		res = prod.promote(envName);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product promote)");
		Assert.assertTrue(getOutput(res).contains(String.format(KatelloProduct.OUT_PROMOTED,prodName,envName)), "Check - returned output string (product promote)");
		
		// product list --environment (1 result - just the product promoted)
		res = prod.cli_list(envName);
		if (prod.syncPlanName == null) prod.syncPlanName = "None";
		if (prod.lastSync == null) prod.lastSync = "";
		if (prod.gpgkey == null) prod.gpgkey = "";
		
		String match_info = String.format(KatelloProduct.REG_PROD_LIST, prodName, prov_name, prod.syncPlanName, prod.lastSync, prod.gpgkey).replaceAll("\"", "");
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(match_info),
				"Product list by environment - just promoted product");
		
		// repo list --environment (2 entries).
		KatelloRepo repo = new KatelloRepo(null,this.org_name,prodName,null,null,null);
		res = repo.list(envName);
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
		KatelloProduct prod = new KatelloProduct(prodName, this.org_name, this.prov_name, null, null, PULP_RHEL6_i386_REPO, null, true);
		res = prod.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");
		Assert.assertTrue(getOutput(res).contains(String.format(KatelloProduct.OUT_CREATED,prodName)), "Check - returned output string (product create)");
		prod.assert_productExists(null, false);
		
		// sync product
		res = prod.synchronize();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product synchronize)");
		Assert.assertTrue(getOutput(res).contains(String.format(KatelloProduct.OUT_SYNCHRONIZED,prodName)), "Check - returned output string (product synchronize)");
		
		// get packages count for the repo - !=0
		KatelloRepo repo = new KatelloRepo(null,this.org_name,prodName,null,null,null);
		res = repo.list();
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
		KatelloProduct prod = new KatelloProduct(prodName, this.org_name, this.prov_name, null, null, PULP_RHEL6_REPO, null, true);
		res = prod.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");
		Assert.assertTrue(getOutput(res).contains(String.format(KatelloProduct.OUT_CREATED,prodName)), "Check - returned output string (product create)");
		prod.assert_productExists(null, false);
		
		// sync product
		res = prod.synchronize();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product synchronize)");
		Assert.assertTrue(getOutput(res).contains(String.format(KatelloProduct.OUT_SYNCHRONIZED,prodName)), "Check - returned output string (product synchronize)");
		
		// get packages count for the repos - !=0
		KatelloRepo repo = new KatelloRepo(null,this.org_name,prodName,null,null,null);
		resRepos = repo.list();
		String match_info = String.format(KatelloRepo.REG_PACKAGE_CNT, "0").replaceAll("\"", "");	
		
		repoName = KatelloCli.grepCLIOutput("Name", resRepos.getStdout(),1); // 1st repo
		repo = new KatelloRepo(repoName,this.org_name,prodName,null,null,null);
		res = repo.info();
		// our line to analyze - should not contain: 0
		Assert.assertFalse(getOutput(res).matches(match_info),"Repo list of the product - should not contain package count 0 (after product synchronize)");

		repoName = KatelloCli.grepCLIOutput("Name", resRepos.getStdout(),2); // 2nd repo
		repo = new KatelloRepo(repoName,this.org_name,prodName,null,null,null);
		res = repo.info();
		// our line to analyze - should not contain: 0
		Assert.assertFalse(getOutput(res).matches(match_info),"Repo list of the product - should not contain package count 0 (after product synchronize)");
	}
	
	@Test(description="delete product - included in some changeset", groups = {"cli-products"}, enabled=true)
	public void test_deleteProduct_InChangeset(){
		String uid = KatelloUtils.getUniqueID();
		String prodName = "delProd1-"+uid;
		String envName_dev = "dev-"+uid;
		String csName = "cs-"+uid;
		SSHCommandResult res;
		
		// create product
		KatelloProduct prod = new KatelloProduct(prodName, this.org_name, this.prov_name, null, null, PULP_RHEL6_x86_64_REPO, null, true);
		res = prod.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");
		// sync product
		res = prod.synchronize();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product synchronize)");
		// create env. - dev
		KatelloEnvironment env = new KatelloEnvironment(envName_dev, null, this.org_name, KatelloEnvironment.LIBRARY);
		res = env.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (environment create)");
		// create changeset
		KatelloChangeset cs = new KatelloChangeset(csName, this.org_name, envName_dev);
		res = cs.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (changeset create)");
		// add product to the changeset
		res = cs.update_addProduct(prodName);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (changeset update --add_product)");
		// promote changeset (dev)
		res = cs.apply();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (changeset promote)");
		
		// Assertions - repo list by env
		KatelloRepo repo = new KatelloRepo(null,this.org_name,prodName,null,null,null);
		res = repo.list(envName_dev);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo list --environment)");
		String match_info = String.format(KatelloRepo.REG_REPO_LIST_ARCH, prodName, "x86_64").replaceAll("\"", "");
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(match_info),
				"Repo list by environment - should contain info");
		// Assertions - product list by env
		res = prod.cli_list(envName_dev);
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
		String prodId = KatelloCli.grepCLIOutput("Id", res.getStdout());
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
		repo = new KatelloRepo(null,this.org_name,prodName,null,null,null);
		res = repo.list();
		Assert.assertTrue(res.getExitCode().intValue()==65, "Check - return code (repo list --product)"); // Bug#750464
		Assert.assertTrue(getOutput(res).equals(String.format(KatelloProduct.ERR_COULD_NOT_FIND_PRODUCT, prodName,org_name)), "Check - `repo list --product` output string");
		
		// Assertions - repo list by env.
		repo = new KatelloRepo(null,this.org_name,prodName,null,null,null);
		res = repo.list(envName_dev);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo list --environment)");
		match_info = String.format(KatelloRepo.REG_REPO_LIST_ARCH, prodName, "x86_64").replaceAll("\"", "");
		Assert.assertFalse(getOutput(res).replaceAll("\n", "").matches(match_info), "Check - `repo list --environment` output string");
		
		// Assertions - product list of env.
		res = prod.cli_list(envName_dev);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product list --environment)");
		match_info = String.format(KatelloProduct.REG_PROD_LIST, prodName, prov_name, prod.syncPlanName, prod.lastSync, prod.gpgkey).replaceAll("\"", "");
		Assert.assertFalse(getOutput(res).replaceAll("\n", "").matches(match_info), 
				"Check - list should NOT contain info about product (deleted already)");		
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

		String lastSync = KatelloCli.grepCLIOutput("Last Sync", getOutput(res).trim(),1);
		if (product.syncState.equals("Not synced")) Assert.assertEquals(lastSync, "never");
		else Assert.assertMatch(lastSync, KatelloProduct.REG_PROD_LASTSYNC);
		
		return KatelloCli.grepCLIOutput("Id", getOutput(res).trim(),1);
	}
	
}
