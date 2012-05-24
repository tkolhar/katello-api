package com.redhat.qe.katello.tests.cli;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.redhat.qe.auto.testng.Assert;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.katello.base.obj.KatelloChangeset;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.tasks.KatelloTasks;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups={"cfse-cli","headpin-cli"})
public class ProductTests  extends KatelloCliTestScript{

	private String org_name;
	private String prov_name;
	
	@BeforeClass(description="Prepare an org to work with", groups = {"cli-product"})
	public void setup_org(){
		SSHCommandResult res;
		String uid = KatelloTestScript.getUniqueID();
		this.org_name = "org"+uid;
		this.prov_name = "prov"+uid;
		KatelloOrg org = new KatelloOrg(this.org_name, null);
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (org create)");
		KatelloProvider prov = new KatelloProvider(this.prov_name, this.org_name, null, null);
		res = prov.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (provider create)");
	}
	

     @Test(description = "List all product for orgs")
	 public void test_listProductDefaultOrg(){
      
    	  String providername = "Red Hat";
	 	  KatelloProduct list_product = new KatelloProduct(null,KatelloTestScript.default_org,providername,null,null,null,null,null);
	 	  SSHCommandResult res = list_product.cli_list();
	 	  Assert.assertEquals(res.getExitCode().intValue(), 0, "Check - return code");
 	}


	@Test(description="create product - no url specified", groups = {"cli-products"}, enabled=true)
	public void test_createProduct_noUrl(){
		String uid = KatelloTestScript.getUniqueID();
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
		String uid = KatelloTestScript.getUniqueID();
		String prodName = "prod1Repo-"+uid;
		SSHCommandResult res;
		
		// create product
		KatelloProduct prod = new KatelloProduct(prodName, this.org_name, this.prov_name, null, null, PULP_F15_x86_64_REPO, null, true);
		res = prod.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");
		Assert.assertTrue(getOutput(res).contains(String.format(KatelloProduct.OUT_CREATED,prodName)), "Check - returned output string (product create)");
		prod.assert_productExists(null,false);
		
		// check - repo created - we don't know the exact repo name.
		KatelloRepo repo = new KatelloRepo(null, this.org_name, prodName, null, null, null);
		res = repo.list();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo list by product)");		
		String REGEXP_PRODUCT_LIST = ".*Id:\\s+\\d+.*Name:\\s+"+prodName+"_.*";
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(REGEXP_PRODUCT_LIST),
				"Repo list should contain info about just created repo (requested by: org, product)");
	}
	
	@Test(description="create product - with multiple repos", groups = {"cli-products"}, enabled=true)
	public void test_createProduct_urlMultipleRepo(){
		String uid = KatelloTestScript.getUniqueID();
		String prodName = "prod2Repos-"+uid;
		SSHCommandResult res, resRepos; String repoName;
		
		// create product
		KatelloProduct prod = new KatelloProduct(prodName, this.org_name, this.prov_name, null, null, PULP_F15_REPO, null, true);
		res = prod.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");
		Assert.assertTrue(getOutput(res).contains(String.format(KatelloProduct.OUT_CREATED,prodName)), "Check - returned output string (product create)");		
		prod.assert_productExists(null,false);
		
		// check - 2 repos created
		KatelloRepo repo = new KatelloRepo(null, this.org_name, prodName, null, null, null);
		res = repo.list();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo list by product)");
		String REGEXP_PRODUCT_LIST_I386 = "..*Id:\\s+\\d+.*Name:\\s+"+prodName+"_.*_i386.*";
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(REGEXP_PRODUCT_LIST_I386),
				"Repo list should contain info about just created repo (requested by: org, product - i386)");
		String REGEXP_PRODUCT_LIST_X86_64 = ".*Id:\\s+\\d+.*Name:\\s+"+prodName+"_.*_x86_64.*";
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(REGEXP_PRODUCT_LIST_X86_64),
				"Repo list should contain info about just created repo (requested by: org, product - x86_64)");
		
		// get packages count for the repos - ==0
		repo = new KatelloRepo(null, this.org_name, prodName, null, null, null);
		resRepos = repo.list();
		String REGEXP_PACKAGE_CNT = ".*Package Count:\\s+0.*";		
		String[] lines;String line;;
		
		repoName = KatelloTasks.grepCLIOutput("Name", getOutput(resRepos),1); // 1st repo
		KatelloRepo repoWithName = new KatelloRepo(repoName, this.org_name, prodName, null, null, null);
		res = repoWithName.info();
		lines = getOutput(res).split("\n");
		for(int i=0;i<lines.length;i++){
			line = lines[i];
			if(line.startsWith("Package Count:")){
				// our line to analyze - should contain: 0
				Assert.assertTrue(line.matches(REGEXP_PACKAGE_CNT),"Repo list of the product - should contain package count 0");
			}
		}
		repoName = KatelloTasks.grepCLIOutput("Name", getOutput(resRepos),2); // 2nd repo
		repoWithName = new KatelloRepo(repoName, this.org_name, prodName, null, null, null);
		res = repoWithName.info();
		lines = getOutput(res).split("\n");
		for(int i=0;i<lines.length;i++){
			line = lines[i];
			if(line.startsWith("Package Count:")){
				// our line to analyze - should contain: 0
				Assert.assertTrue(line.matches(REGEXP_PACKAGE_CNT),"Repo list of the product - should contain package count 0");
			}
		}
	}
	

	// TODO - product creation failflows + the cases with "Description" variations. + duplicate names and so
	
	// TODO - `product list --provider`
	
	// TODO - `product list --environment`
	
	// TODO - `product status`
	
	@Test(description="promote product", groups = {"cli-products"}, enabled=true)
	public void test_promoteProduct_NoRepos(){
		String uid = KatelloTestScript.getUniqueID();
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
		res = env.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (environment create)");
		
		// sync product (otherwise promote will fail)
		res = prod.synchronize();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product synchronize)");
		
		// promote product to the env.
		res = prod.promote(envName);
		Assert.assertTrue(res.getExitCode().intValue()==244, "Check - return code (product promote)");
		Assert.assertTrue(getOutput(res).contains(String.format(KatelloProduct.ERR_PROMOTE_NOREPOS,prodName)), "Check - returned output string (product promote)");
	}
	
	@Test(description="promote product", groups = {"cli-products"}, enabled=true)
	public void test_promoteProduct_OneRepo(){
		String uid = KatelloTestScript.getUniqueID();
		String prodName = "promo1Repo-"+uid;
		String envName = "dev-"+uid;
		SSHCommandResult res;
		
		// create product
		KatelloProduct prod = new KatelloProduct(prodName, this.org_name, this.prov_name, null, null, PULP_F15_x86_64_REPO, null, true);
		res = prod.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");
		Assert.assertTrue(getOutput(res).contains(String.format(KatelloProduct.OUT_CREATED,prodName)), "Check - returned output string (product create)");
		prod.assert_productExists(null, false);
		
		// create env.
		KatelloEnvironment env = new KatelloEnvironment(envName, null, this.org_name, KatelloEnvironment.LIBRARY);
		res = env.create();
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
		String REGEXP_PRODUCT_LIST = ".*Id:\\s+\\d+Name:\\s+"+prodName+".*Provider Name:\\s+"+prov_name+".*";
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(REGEXP_PRODUCT_LIST),
				"Product list by environment - just promoted product");
		
		// repo list --environment (1 result).
		// check - repo created - we don't know the exact repo name.
		KatelloRepo repo = new KatelloRepo(null,this.org_name,prodName,null,null,null);
		res = repo.list(envName);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo list by product)");		
		String REGEXP_REPO_LIST = ".*Id:\\s+\\d+.*Name:\\s+"+prodName+"_.*";
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(REGEXP_REPO_LIST),
				"Repo list should contain info about just created repo (requested by: org, environment)");
	}
	
	@Test(description="promote product", groups = {"cli-products"}, enabled=true)
	public void test_promoteProduct_MultipleRepos(){
		String uid = KatelloTestScript.getUniqueID();
		String prodName = "promo1Repo-"+uid;
		String envName = "dev-"+uid;
		SSHCommandResult res;
		
		// create product
		KatelloProduct prod = new KatelloProduct(prodName, this.org_name, this.prov_name, null, null, PULP_F15_REPO, null, true);
		res = prod.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");
		Assert.assertTrue(getOutput(res).contains(String.format(KatelloProduct.OUT_CREATED,prodName)), "Check - returned output string (product create)");
		prod.assert_productExists(null, false);
		
		// create env.
		KatelloEnvironment env = new KatelloEnvironment(envName, null, this.org_name, KatelloEnvironment.LIBRARY);
		res = env.create();
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
		String REGEXP_PRODUCT_LIST = ".*Id:\\s+\\d+Name:\\s+"+prodName+".*Provider Name:\\s+"+prov_name+".*";
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(REGEXP_PRODUCT_LIST),
				"Product list by environment - just promoted product");
		
		// repo list --environment (2 entries).
		KatelloRepo repo = new KatelloRepo(null,this.org_name,prodName,null,null,null);
		res = repo.list(envName);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo list by product)");
		String REGEXP_PRODUCT_LIST_I386 = ".*Id:\\s+\\d+.*Name:\\s+"+prodName+"_.*_i386.*";
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(REGEXP_PRODUCT_LIST_I386),
				"Repo list should contain info about just created repo (requested by: org, product - i386)");
		String REGEXP_PRODUCT_LIST_X86_64 = ".*Id:\\s+\\d+.*Name:\\s+"+prodName+"_.*_x86_64.*";
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(REGEXP_PRODUCT_LIST_X86_64),
				"Repo list should contain info about just created repo (requested by: org, product - x86_64)");
	}

	@Test(description="sync product - single repo", groups = {"cli-products"}, enabled=true)
	public void test_syncronizeProduct_SingleRepo(){
		String uid = KatelloTestScript.getUniqueID();
		String prodName = "sync1Repo-"+uid;
		SSHCommandResult res;

		// create product
		KatelloProduct prod = new KatelloProduct(prodName, this.org_name, this.prov_name, null, null, PULP_F15_i386_REPO, null, true);
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
		String REGEXP_REPO_LIST = ".*Package Count:\\s+0.*";
		Assert.assertFalse(getOutput(res).replaceAll("\n", "").matches(REGEXP_REPO_LIST),
				"Repo list of the product - should not contain package count 0 (after product synchronize)");
	}

	@Test(description="sync product - multiple repos", groups = {"cli-products"}, enabled=true)
	public void test_syncronizeProduct_MultipleRepos(){
		String uid = KatelloTestScript.getUniqueID();
		String prodName = "syncManyRepos-"+uid;
		SSHCommandResult res, resRepos; String repoName;

		// create product
		KatelloProduct prod = new KatelloProduct(prodName, this.org_name, this.prov_name, null, null, PULP_F15_REPO, null, true);
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
		String REGEXP_PACKAGE_CNT = ".*Package Count:\\s+0.*";		
		String[] lines;String line;;
		
		repoName = KatelloTasks.grepCLIOutput("Name", resRepos.getStdout(),1); // 1st repo
		repo = new KatelloRepo(repoName,this.org_name,prodName,null,null,null);
		res = repo.info();
		lines = getOutput(res).split("\n");
		for(int i=0;i<lines.length;i++){
			line = lines[i];
			if(line.startsWith("Package Count:")){
				// our line to analyze - should not contain: 0
				Assert.assertFalse(line.matches(REGEXP_PACKAGE_CNT),"Repo list of the product - should not contain package count 0 (after product synchronize)");
			}
		}
		repoName = KatelloTasks.grepCLIOutput("Name", resRepos.getStdout(),2); // 2nd repo
		repo = new KatelloRepo(repoName,this.org_name,prodName,null,null,null);
		res = repo.info();
		lines = getOutput(res).split("\n");
		for(int i=0;i<lines.length;i++){
			line = lines[i];
			if(line.startsWith("Package Count:")){
				// our line to analyze - should not contain: 0
				Assert.assertFalse(line.matches(REGEXP_PACKAGE_CNT),"Repo list of the product - should not contain package count 0 (after product synchronize)");
			}
		}
	}
	
	// TODO - implement: https://bugzilla.redhat.com/show_bug.cgi?id=749517
	// Duplicate product names - creating same product name for different orgs (even provider could have the same name)
	
	@Test(description="delete product - included in some changeset", groups = {"cli-products"}, enabled=true)
	public void test_deleteProduct_InChangeset(){
		String uid = KatelloTestScript.getUniqueID();
		String prodName = "delProd1-"+uid;
		String envName_dev = "dev-"+uid;
		String csName = "cs-"+uid;
		SSHCommandResult res;
		
		// create product
		KatelloProduct prod = new KatelloProduct(prodName, this.org_name, this.prov_name, null, null, PULP_F15_x86_64_REPO, null, true);
		res = prod.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");
		// sync product
		res = prod.synchronize();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product synchronize)");
		// create env. - dev
		KatelloEnvironment env = new KatelloEnvironment(envName_dev, null, this.org_name, KatelloEnvironment.LIBRARY);
		res = env.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (environment create)");
		// create changeset
		KatelloChangeset cs = new KatelloChangeset(csName, this.org_name, envName_dev);
		res = cs.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (changeset create)");
		// add product to the changeset
		res = cs.update_addProduct(prodName);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (changeset update --add_product)");
		// promote changeset (dev)
		res = cs.promote();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (changeset promote)");
		
		// Assertions - repo list by env
		KatelloRepo repo = new KatelloRepo(null,this.org_name,prodName,null,null,null);
		res = repo.list(envName_dev);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo list --environment)");
		String REGEXP_PRODUCT_LIST_X86_64 = ".*Id:\\s+\\d+.*Name:\\s+"+prodName+"_.*_x86_64.*";
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(REGEXP_PRODUCT_LIST_X86_64),
				"Repo list by environment - should contain info");
		// Assertions - product list by env
		res = prod.cli_list(envName_dev);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product list --environment)");
		String REGEXP_PRODUCT_LIST = ".*Name:\\s+"+prodName+".*Provider Name:\\s+"+this.prov_name+".*";
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(REGEXP_PRODUCT_LIST), 
				"List should contain info about product (requested by: environment)");
		
		// Final action - DELETE the product
		// ... but get its id first. To check the output string.
		res = prod.status();
		String prodId = KatelloTasks.grepCLIOutput("Id", res.getStdout());
		res = prod.delete();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");
		Assert.assertTrue(getOutput(res).contains(String.format(KatelloProduct.OUT_DELETED,prodId)), "Check - returned output string (product delete)");
		
		// Assertions - product list of the org
		res = prod.cli_list();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product list --provider)");
		REGEXP_PRODUCT_LIST = ".*Name:\\s+"+prodName+".*Provider Name:\\s+"+this.prov_name+".*";
		Assert.assertFalse(getOutput(res).replaceAll("\n", "").matches(REGEXP_PRODUCT_LIST), 
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
		String REGEXP_NOREPO = ".*Id:\\s+\\d+.*Name:\\s+.*";
		Assert.assertFalse(getOutput(res).replaceAll("\n", "").matches(REGEXP_NOREPO), "Check - `repo list --environment` output string");
		
		// Assertions - product list of env.
		res = prod.cli_list(envName_dev);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product list --environment)");
		REGEXP_PRODUCT_LIST = ".*Name:\\s+"+prodName+".*Provider Name:\\s+"+this.prov_name+".*";
		Assert.assertFalse(getOutput(res).replaceAll("\n", "").matches(REGEXP_PRODUCT_LIST), 
				"Check - list should NOT contain info about product (deleted already)");		
	}
}
