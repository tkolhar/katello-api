package com.redhat.qe.katello.tests.installation;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.DeltaCloudInstance;
import com.redhat.qe.katello.base.obj.KatelloActivationKey;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups = { "cfse-cli", "headpin-cli" })
public class TestMultipleAgents extends KatelloCliTestBase {

	protected DeltaCloudInstance server;
	protected String server_name;
	protected ArrayList<DeltaCloudInstance> clients = new ArrayList<DeltaCloudInstance>();
	protected String org_name = null;
	protected String dev_env_name = null;
	protected String test_env_name = null;
	protected String prod_env_name = null;
	protected String zoo_provider_name = null;
	protected String zoo_product_name = null;
	protected String zoo_product_id = null;
	protected String zoo_repo_name = null;
	protected String zoo_repo_pool = null;
	protected String act_key_name = null;
	protected String package_name = "lion";
	
	@Test(description = "setup Deltacloud Server")
	public void testMultipleClients() {

		//server = KatelloUtils.getDeltaCloudServer();
		server_name = "cfseserver2.usersys.redhat.com";//server.getHostName();
		System.setProperty("katello.server.hostname", server_name);
		System.setProperty("katello.client.hostname", server_name);

//		try {
//			Thread.sleep(600000);
//		} catch (InterruptedException iex) {
//		}

		createOrgStuff();
		
		StringTokenizer tok = new StringTokenizer(
				System.getProperty("deltacloud.client.imageid"), ",");
		
		while (tok.hasMoreTokens()) {
			DeltaCloudInstance client = KatelloUtils.getDeltaCloudClient(
					server_name, tok.nextToken());
			clients.add(client);

			testClientConsume(client.getHostName());
			
			KatelloUtils.destroyDeltaCloudMachine(client);
		}
	}

	@AfterClass(alwaysRun = true)
	public void tearDown() {
		//KatelloUtils.destroyDeltaCloudMachine(server);
		for (DeltaCloudInstance client : clients) {
			KatelloUtils.destroyDeltaCloudMachine(client);
		}
	}
	
	private void createOrgStuff() {
		String uid = KatelloUtils.getUniqueID();
		org_name = "Test Org " + uid;
		dev_env_name = "Dev env " + uid;
		zoo_provider_name = "Zoo Prov " + uid;
		zoo_product_name = "Zoo Prod " + uid;
		zoo_repo_name = "Zoo Repo " + uid;
		act_key_name = "activationkey" + uid;

		KatelloOrg org = new KatelloOrg(null, org_name, null);
		exec_result = org.cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");
		
		KatelloEnvironment env = new KatelloEnvironment(null, dev_env_name, null, org_name, KatelloEnvironment.LIBRARY);
		exec_result = env.cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");
		
		KatelloProvider prov = new KatelloProvider(null, zoo_provider_name, org_name, "Zoo4 provider", null);
		exec_result = prov.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		// -- Product
		KatelloProduct prod = new KatelloProduct(null, zoo_product_name, org_name, zoo_provider_name, null, null, null, null, null);
		exec_result = prod.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		// -- Repo
		KatelloRepo repo = new KatelloRepo(null, zoo_repo_name, org_name, zoo_product_name, REPO_INECAS_ZOO3, null, null);
		exec_result = repo.create(true);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = repo.synchronize();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		zoo_repo_pool = org.custom_getPoolId(zoo_product_name);
		Assert.assertNotNull(zoo_repo_pool, "Check - pool Id is not null");
		zoo_product_id = prod.custom_getProductId();
		Assert.assertNotNull(zoo_product_id, "Check - zoo_product_id is not null");
		
		String view = KatelloUtils.promoteProductToEnvironment(null, org_name, zoo_product_name, dev_env_name);
		
		KatelloActivationKey act_key = new KatelloActivationKey(null, org_name, dev_env_name, act_key_name,"Act key created", null, view);
		exec_result = act_key.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");           

		exec_result = act_key.update_add_subscription(zoo_repo_pool);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	}
	
	private void testClientConsume(String client_name) {
		KatelloSystem sys = new KatelloSystem(null, client_name+KatelloUtils.getUniqueID(), org_name, null);
		sys.runOn(client_name);
		exec_result = sys.rhsm_registerForce(act_key_name);
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		
		String arch = KatelloUtils.sshOnClient(client_name, "uname -snrmpio").getStdout();
		
		SSHCommandResult res = KatelloUtils.sshOnClient(client_name, "yum install -y " + package_name);
		Assert.assertTrue(res.getExitCode() == 0, "Package is installed successfully on machine " + arch);
		
		res = KatelloUtils.sshOnClient(client_name, "rpm -qa | grep -E \"" + package_name + "\"");
		Assert.assertTrue(getOutput(res).contains(package_name), "Package " + package_name + " should be installed on machine " + arch);		
	}

}
