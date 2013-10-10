package com.redhat.qe.katello.tests.e2e;

import java.util.logging.Logger;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloPackage;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

@Test(singleThreaded = true)
public class ProductsSameName extends KatelloCliTestBase {
	
	protected static Logger log = Logger.getLogger(ProductsSameName.class.getName());
	
	private SSHCommandResult exec_result;
	private static final String package1 = "acme-package";
	private static final String package2 = "lion";

	// Katello objects below
	private String org_name;
	private String repo_name;
	private String repo_name2;
	private String env_name;
	private String provider_name;
	private String product_name;
	private String product_name2;
	private String system_name;
	private String package_id, package_id2;
	private String product_id, product_id2;
	
	private String contentView;
	
	@BeforeClass(description="Generate unique objects")
	public void setUp(){
		String uid = KatelloUtils.getUniqueID();
		org_name = "org"+uid;
		provider_name = "provider"+uid;
		product_name = "product"+uid;
		product_name2 = product_name;
		repo_name = "repo-1-"+uid;
		repo_name2 = "repo-2-"+uid;
		env_name = "env"+uid;
		system_name = "system"+uid;
		
		// Create org:
		KatelloOrg org = new KatelloOrg(this.cli_worker, this.org_name,"Package tests");
		exec_result = org.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		// Create provider:
		KatelloProvider prov = new KatelloProvider(this.cli_worker, provider_name, org_name, "Package provider", null);
		exec_result = prov.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		// Create product 1:
		KatelloProduct prod = new KatelloProduct(this.cli_worker, product_name, org_name, provider_name, null, null, null, null, null);
		exec_result = prod.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = prod.cli_list();
		product_id = KatelloUtils.grepCLIOutput("ID", getOutput(exec_result), 1);
	
		// Create product 2:
		KatelloProduct prod2 = new KatelloProduct(this.cli_worker, product_name2, org_name, provider_name, null, null, null, null, null);
		exec_result = prod2.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = prod.cli_list();
		product_id2 = KatelloUtils.grepCLIOutput("ID", getOutput(exec_result), 1);
				
		KatelloRepo repo = new KatelloRepo(this.cli_worker, repo_name, org_name, null, REPO_HHOVSEPY_ZOO4, null, null,null,product_id);
		exec_result = repo.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		KatelloRepo repo2 = new KatelloRepo(this.cli_worker, repo_name2, org_name, null, REPO_INECAS_ZOO3, null, null,null,product_id2);
		exec_result = repo2.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		KatelloEnvironment env = new KatelloEnvironment(this.cli_worker, env_name, null, org_name, KatelloEnvironment.LIBRARY);
		exec_result = env.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		
		exec_result = prov.synchronize();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code"); // provider sync will do sync both repos
		
		this.contentView = KatelloUtils.promoteProductIDsToEnvironment(cli_worker, org_name, new String[] {product_id, product_id2}, env_name);
		
		sshOnClient(String.format("yum -y erase %s %s || true",package1,package2));
	}
	
	@Test(description="package list of two repos")
	public void test_packageList() {
		
		KatelloPackage pack = new KatelloPackage(cli_worker, null, null, org_name, null, repo_name, null);
		pack.setProductId(product_id);
		pack.content_view = this.contentView;
		
		exec_result = pack.cli_list();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		Assert.assertTrue(getOutput(exec_result).contains(package1), "found package: "+package1);
				
		KatelloPackage pack2 = new KatelloPackage(cli_worker, null, null, org_name, null, repo_name2, null);
		pack2.setProductId(product_id2);
		pack2.content_view = this.contentView;
		
		exec_result = pack2.cli_list();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		Assert.assertTrue(getOutput(exec_result).contains(package2), "found package: "+package2);
		pack.name = package1;
		package_id = pack.custom_packageId();
		Assert.assertNotNull(package_id, "Package ID is not null");
		pack2.name = package2;
		package_id2 = pack2.custom_packageId();
		Assert.assertNotNull(package_id2, "Package ID is not null");
	}

	//@ TODO bz#921103
	@Test(description="package info of two repos", dependsOnMethods={"test_packageList"})
	public void test_packageInfo() {
		
		KatelloPackage pack = new KatelloPackage(cli_worker, package_id, null, org_name, null, repo_name, null);
		pack.setProductId(product_id); 
		pack.content_view = this.contentView;
		exec_result = pack.cli_info();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(package1));

		KatelloPackage pack2 = new KatelloPackage(cli_worker, package_id2, null, org_name, null, repo_name2, null);
		pack2.setProductId(product_id2);
		pack2.content_view = this.contentView;
		exec_result = pack2.cli_info();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(package2));	
	}
	
	@Test(description="install packages of two repos")
	public void testInstallPackage() {
		rhsm_clean();
		
		KatelloSystem sys = new KatelloSystem(this.cli_worker, this.system_name, this.org_name, this.env_name+"/"+this.contentView);
		exec_result = sys.rhsm_registerForce(); 
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = sys.subscriptions_available();
		String poolId1 = KatelloUtils.grepCLIOutput("ID", getOutput(exec_result).trim(),1);
		
		exec_result = sys.subscribe(poolId1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = sys.subscriptions_available();
		String poolId2 = KatelloUtils.grepCLIOutput("ID", getOutput(exec_result).trim(),1);
		
		exec_result = sys.subscribe(poolId2);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		sys.rhsm_refresh();
		
		exec_result = sshOnClient("yum -y install "+package2);
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code (install "+package2+")");

		exec_result = sshOnClient("yum -y install "+package1);
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code (install "+package1+")");
	}
	
	@AfterClass(description="uninstall rpm-s, cleanup the rhsm registration", alwaysRun=true)
	public void destroy(){
		log.info("cleanup previousely installed rpm-s");
		sshOnClient(String.format("yum -y erase %s %s || true",package1,package2));
		rhsm_clean();
	}

}
