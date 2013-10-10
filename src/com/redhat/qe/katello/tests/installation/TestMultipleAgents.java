package com.redhat.qe.katello.tests.installation;

import java.util.ArrayList;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.katello.base.KatelloCliDataProvider;
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
	protected String rhel5_zoo_provider_name = null;
	protected String rhel5_zoo_product_name = null;
	protected String rhel5_zoo_product_id = null;
	protected String rhel5_zoo_repo_name = null;
	protected String rhel5_zoo_repo_pool = null;

	protected String act_key_name = null;
	protected String package_name = "lion";

	protected String rhel5_act_key_name = null;
	protected String rhel5_package_name = "lion";
	
	
	@BeforeClass(description = "setup Deltacloud Server")
	public void setUp() {
		server = KatelloUtils.getDeltaCloudServer();
		server_name = server.getHostName();
		System.setProperty("katello.server.hostname", server_name);
		System.setProperty("katello.client.hostname", server_name);

		createOrgStuff();
	}
	
	@Test(description = "provision client and run test on it", dataProvider = "multiple_agents", 
			dataProviderClass = KatelloCliDataProvider.class)
	public void testMultipleClients(String type) {

		DeltaCloudInstance client = KatelloUtils.getDeltaCloudClientCertOnly(
				server_name, DELTACLOUD_IMAGES.get(type));
		clients.add(client);
				
		KatelloUtils.disableYumRepo(client.getIpAddress(),"beaker");
		KatelloUtils.disableYumRepo(client.getIpAddress(),"epel");
		KatelloUtils.disableYumRepo(client.getIpAddress(),"katello-tools");
		
		try {
			testClientConsume(client.getIpAddress(), type);
		} finally {
			KatelloUtils.destroyDeltaCloudMachine(client);
		}
	}

	@AfterClass(alwaysRun = true)
	public void tearDown() {
		KatelloUtils.destroyDeltaCloudMachine(server);
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

		rhel5_zoo_provider_name = "RHEL5 Zoo Prov " + uid;
		rhel5_zoo_product_name = "RHEL5 Zoo Prod " + uid;
		rhel5_zoo_repo_name = "RHEL5 Zoo Repo " + uid;
		rhel5_act_key_name = "rhel5activationkey" + uid;

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

		prov = new KatelloProvider(null, rhel5_zoo_provider_name, org_name, "rhel5 Zoo provider", null);
		exec_result = prov.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		// -- Product
		prod = new KatelloProduct(null, rhel5_zoo_product_name, org_name, rhel5_zoo_provider_name, null, null, null, null, null);
		exec_result = prod.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		// -- Repo
		repo = new KatelloRepo(null, rhel5_zoo_repo_name, org_name, rhel5_zoo_product_name, "http://inecas.fedorapeople.org/fakerepos/zoo-rhel5-new/", null, null);
		exec_result = repo.create(true);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = repo.synchronize();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		rhel5_zoo_repo_pool = org.custom_getPoolId(rhel5_zoo_product_name);
		Assert.assertNotNull(rhel5_zoo_repo_pool, "Check - pool Id is not null");
		rhel5_zoo_product_id = prod.custom_getProductId();
		Assert.assertNotNull(rhel5_zoo_product_id, "Check - zoo_product_id is not null");
		
		view = KatelloUtils.promoteProductToEnvironment(null, org_name, rhel5_zoo_product_name, dev_env_name);
		act_key = new KatelloActivationKey(null, org_name, dev_env_name, rhel5_act_key_name,"Act key created", null, view);
		exec_result = act_key.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");           
		exec_result = act_key.update_add_subscription(rhel5_zoo_repo_pool);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

	}
	
	//TODO bug fails for RHEL 5 bz#988776
	private void testClientConsume(String client_name, String client_type) {
		String actKey = null;
		String packName = null;
		
		if (client_type.matches(".*RHEL\\s+5.*")) {
			actKey = rhel5_act_key_name;
			packName = rhel5_package_name;
		} else {
			actKey = act_key_name;
			packName = package_name;
		}
		
		rhsm_clean(client_name);
		
		KatelloSystem sys = new KatelloSystem(null, client_type+" "+KatelloUtils.getUniqueID(), org_name, null);
		sys.runOn(client_name);
		exec_result = sys.rhsm_registerForce(actKey);
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Client " + client_type + " should be subscribed to server successfully.");
		
		yum_clean(client_name);
		
		SSHCommandResult res = KatelloUtils.sshOnClient(client_name, "yum install -y " + packName);
		Assert.assertTrue(res.getExitCode() == 0, "Package is installed successfully on machine " + client_type);
		
		res = KatelloUtils.sshOnClient(client_name, "rpm -qa | grep -E \"" + packName + "\"");
		Assert.assertTrue(getOutput(res).contains(packName), "Package " + packName + " should be installed on machine " + client_type);		
	}

}
