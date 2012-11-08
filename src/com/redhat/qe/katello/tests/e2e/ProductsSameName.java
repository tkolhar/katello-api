package com.redhat.qe.katello.tests.e2e;

import java.util.logging.Logger;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.obj.KatelloChangeset;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloPackage;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

public class ProductsSameName extends KatelloCliTestScript {
	
	protected static Logger log = Logger.getLogger(ProductsSameName.class.getName());
	
	private SSHCommandResult exec_result;

	// Katello objects below
	private String org_name;
	private String repo_name;
	private String repo_name2;
	private String env_name;
	private String provider_name;
	private String product_name;
	private String product_name2;
	private String changeset_name;
	private String system_name;
	private String product_id, product_id2;
	
	@BeforeClass(description="Generate unique objects")
	public void setUp(){
		String uid = KatelloUtils.getUniqueID();
		org_name = "org"+uid;
		provider_name = "provider"+uid;
		product_name = "product"+uid;
		product_name2 = product_name;
		repo_name = "repo1"+uid;
		repo_name2 = "repo2"+uid;
		env_name = "env"+uid;
		changeset_name = "changeset"+uid;
		system_name = "system"+uid;
		
		// Create org:
		KatelloOrg org = new KatelloOrg(this.org_name,"Package tests");
		exec_result = org.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		// Create provider:
		KatelloProvider prov = new KatelloProvider(provider_name, org_name, "Package provider", null);
		exec_result = prov.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		// Create product 1:
		KatelloProduct prod = new KatelloProduct(product_name, org_name, provider_name, null, null, null, null, null);
		exec_result = prod.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = prod.cli_list();
		product_id = KatelloCli.grepCLIOutput("Id", getOutput(exec_result), 1);
	
		// Create product 2:
		KatelloProduct prod2 = new KatelloProduct(product_name2, org_name, provider_name, null, null, null, null, null);
		exec_result = prod2.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = prod.cli_list();
		product_id2 = KatelloCli.grepCLIOutput("Id", getOutput(exec_result), 1);
				
		KatelloRepo repo = new KatelloRepo(repo_name, org_name, null, PULP_RHEL6_x86_64_REPO, null, null,null,product_id);
		exec_result = repo.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		KatelloRepo repo2 = new KatelloRepo(repo_name2, org_name, null, REPO_INECAS_ZOO3, null, null,null,product_id2);
		exec_result = repo2.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		KatelloEnvironment env = new KatelloEnvironment(env_name, null, org_name, KatelloEnvironment.LIBRARY);
		exec_result = env.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		KatelloChangeset cs = new KatelloChangeset(changeset_name, org_name, env_name);
		exec_result = cs.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		repo.synchronize();		
		repo2.synchronize();
		
		cs.update_addProductId(product_id);		
		cs.update_addProductId(product_id2);
		cs.promote();	
	}
	
	@Test(description="package list of two repos")
	public void test_packageList() {
		
		KatelloPackage pack = new KatelloPackage(null, null, org_name, null, repo_name, null);
		pack.setProductId(product_id); 
		
		exec_result = pack.cli_list();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		Assert.assertTrue(getOutput(exec_result).contains("pulp-admin"));
		Assert.assertTrue(getOutput(exec_result).contains("pulp"));
		Assert.assertTrue(getOutput(exec_result).contains("python-gofer"));
		Assert.assertTrue(getOutput(exec_result).contains("python-qpid"));
		Assert.assertTrue(getOutput(exec_result).contains("pulp-common"));
		Assert.assertTrue(getOutput(exec_result).contains("pulp-consumer"));
				
		KatelloPackage pack2 = new KatelloPackage(null, null, org_name, null, repo_name2, null);
		pack2.setProductId(product_id2);
		
		exec_result = pack2.cli_list();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		Assert.assertTrue(getOutput(exec_result).contains("lion"));
		Assert.assertTrue(getOutput(exec_result).contains("wolf"));
		Assert.assertTrue(getOutput(exec_result).contains("zebra"));
		Assert.assertTrue(getOutput(exec_result).contains("stork"));		
	}

	@Test(description="package info of two repos")
	public void test_packageInfo() {
		
		KatelloPackage pack = new KatelloPackage("8915dec5-f572-4c16-b3c7-66cbfb56c037", null, org_name, null, repo_name, null);
		pack.setProductId(product_id); 
		
		exec_result = pack.cli_info();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains("pulp-admin"));

				
		KatelloPackage pack2 = new KatelloPackage("2c27c49a-8c64-47d8-978d-06bf69656e3f", null, org_name, null, repo_name2, null);
		pack2.setProductId(product_id2);
		
		exec_result = pack2.cli_info();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains("lion"));	
	}
	
	@Test(description="install packages of two repos")
	public void testInstallPackage() {
		
		rhsm_clean();
		
		KatelloSystem sys = new KatelloSystem(this.system_name, this.org_name, this.env_name);
		exec_result = sys.rhsm_registerForce(); 
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = sys.subscriptions_available();
		String poolId1 = KatelloCli.grepCLIOutput("Id", getOutput(exec_result).trim(),1);
		
		exec_result = sys.rhsm_subscribe(poolId1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = sys.subscriptions_available();
		String poolId2 = KatelloCli.grepCLIOutput("Id", getOutput(exec_result).trim(),1);
		
		exec_result = sys.rhsm_subscribe(poolId2);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = KatelloUtils.sshOnClient("yum -y install lion");
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code (install lion)");

		exec_result = KatelloUtils.sshOnClient("yum -y install pulp-consumer");
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code (install lion)");
	}
	
	@AfterClass(description="uninstall rpm-s, cleanup the rhsm registration", alwaysRun=true)
	public void destroy(){
		log.info("cleanup previousely installed rpm-s");
		KatelloUtils.sshOnClient("yum -y erase pulp-consumer lion");
		rhsm_clean_only();
	}

}
