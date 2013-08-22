package com.redhat.qe.katello.tests.deltacloud;

import java.io.File;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.DeltaCloudInstance;
import com.redhat.qe.katello.base.obj.KatelloActivationKey;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.common.KatelloUtils;

import com.redhat.qe.tools.SSHCommandResult;

public class BaseDeltacloudTest extends KatelloCliTestBase {

	public static final String MANIFEST_12SUBSCRIPTIONS = "manifest-automation-CLI-12subscriptions.zip";
	
	protected SSHCommandResult exec_result;

	// Katello objects below
	protected String uid;
	protected static String org_name;
	protected static String provider_name;
	protected static String product_name;
	protected static String repo_name;
	protected static String env_name;
	protected static String env_name2;
	protected static String system_name;
	protected static String system_name2;
	protected static String system_name3;
	protected static String group_name;
	protected static String group_name2;
	protected static String system_uuid;
	protected static String system_uuid2;
	protected static String system_uuid3;
	protected static DeltaCloudInstance server;
	protected static DeltaCloudInstance client;
	protected static DeltaCloudInstance client2;
	protected static DeltaCloudInstance client3;
	protected static String server_name;
	protected static String client_name;
	protected static String client_name2;
	protected static String client_name3;
	protected static String zoo_repo_view;
	protected static String rhel_repo_view;
	protected static String zoo_act_key;
	protected static String rhel_act_key;
	protected static String poolId1;
	protected static String poolId2;

	public void setUp(){
		if (server != null) return;
		
		uid = KatelloUtils.getUniqueID();
		org_name = "org_"+uid;
		provider_name = "provider_"+uid;
		product_name = "product_"+uid;
		repo_name = "repo_name_"+uid;
		env_name = "env_Dev_"+uid;
		env_name2 = "env_Test_"+uid;
		system_name = "system_"+uid;
		system_name2 = "system2_"+uid;
		system_name3 = "system3_"+uid;
		zoo_act_key = "zookey"+uid;
		rhel_act_key = "rhelkey"+uid;
		
		server = KatelloUtils.getDeltaCloudServer();
		server_name = server.getHostName();
		
		client = KatelloUtils.getDeltaCloudClient(server_name);
		client_name = client.getHostName();
		
		client2 = KatelloUtils.getDeltaCloudClient(server_name);
		client_name2 = client2.getHostName();
		
		client3 = KatelloUtils.getDeltaCloudClient(server_name);
		client_name3 = client3.getHostName();
		
		System.setProperty("katello.server.hostname", server_name);
		System.setProperty("katello.client.hostname", server_name);
		
		// Create org:
		KatelloOrg org = new KatelloOrg(this.cli_worker, org_name, "Package tests");
		exec_result = org.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		// Create provider:
		KatelloProvider prov = new KatelloProvider(this.cli_worker, provider_name, org_name,
				"Package provider", null);
		exec_result = prov.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		// Create product:
		KatelloProduct prod = new KatelloProduct(this.cli_worker, product_name, org_name,
				provider_name, null, null, null, null, null);
		exec_result = prod.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		KatelloRepo repo = new KatelloRepo(this.cli_worker, repo_name, org_name, product_name, REPO_INECAS_ZOO3, null, null);
		exec_result = repo.create(true);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		KatelloEnvironment env = new KatelloEnvironment(this.cli_worker, env_name, null, org_name, KatelloEnvironment.LIBRARY);
		exec_result = env.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (env create)");
		
		env = new KatelloEnvironment(this.cli_worker, env_name2, null, org_name, KatelloEnvironment.LIBRARY);
		exec_result = env.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (env create)");
		
		// promote product to environments
		KatelloUtils.promoteProductsToEnvironments(cli_worker, org_name, new String[] {product_name}, new String[] {env_name, env_name2});
		
		exec_result = repo.synchronize();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		zoo_repo_view = KatelloUtils.promoteProductToEnvironment(cli_worker, org_name, product_name, env_name);
		
		KatelloUtils.scpOnClient(cli_worker.getClientHostname(), "data/"+MANIFEST_12SUBSCRIPTIONS, "/tmp");
		
		prov = new KatelloProvider(this.cli_worker, KatelloProvider.PROVIDER_REDHAT, org_name, null, null);
		prov.runOn(server_name);
		SSHCommandResult res = prov.import_manifest("/tmp"+File.separator+MANIFEST_12SUBSCRIPTIONS, new Boolean(true));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (provider import_manifest)");
		
		prod = new KatelloProduct(this.cli_worker, KatelloProduct.RHEL_SERVER,org_name, KatelloProvider.PROVIDER_REDHAT, null, null, null,null, null);
		res = prod.repository_set_enable(KatelloProduct.REPOSET_RHEL6_RPMS);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo set enable)");
		
		log.info("Enable repo: ["
		+KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT+"]");
		repo = new KatelloRepo(this.cli_worker, KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT, org_name, KatelloProduct.RHEL_SERVER, null, null, null);
		res = repo.enable();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo enable)");
		Assert.assertTrue(getOutput(res).contains("enabled."),"Message - (repo enable)");
		
		//KatelloUtils.promoteProductToEnvironment(cli_runner, org_name, KatelloProduct.RHEL_SERVER, env_name2);
		
		res = repo.synchronize();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo synchronize)");
		waitfor_repodata(repo, 2);
//		res = repo.info();
//		String progress = KatelloUtils.grepCLIOutput("Progress", res.getStdout());
//		Assert.assertTrue(progress.equals("Finished"), "Check: status of repo sync - Finished");
		
		rhel_repo_view = KatelloUtils.promoteProductToEnvironment(cli_worker, org_name, KatelloProduct.RHEL_SERVER, env_name);
		
		exec_result = org.subscriptions();
		poolId1 = KatelloUtils.grepCLIOutput("ID", getOutput(exec_result).trim(),1);
		Assert.assertNotNull(poolId1, "Check - pool Id is not null");
		poolId2 = KatelloUtils.grepCLIOutput("ID", getOutput(exec_result).trim(),2);
		Assert.assertNotNull(poolId2, "Check - pool Id is not null");
		
		KatelloActivationKey act_key = new KatelloActivationKey(this.cli_worker, org_name,env_name,zoo_act_key,"Act key created");
		exec_result = act_key.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");      
		exec_result = act_key.update_content_view(zoo_repo_view);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");      
		exec_result = act_key.update_add_subscription(poolId1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = act_key.update_add_subscription(poolId2);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		act_key = new KatelloActivationKey(this.cli_worker, org_name,env_name,rhel_act_key,"Act key created");
		exec_result = act_key.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");      
		exec_result = act_key.update_content_view(rhel_repo_view);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");      
		exec_result = act_key.update_add_subscription(poolId1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = act_key.update_add_subscription(poolId2);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	}
	
	public void tearDown() {
		if (server == null) return;
		
		KatelloUtils.destroyDeltaCloudMachine(server);
		KatelloUtils.destroyDeltaCloudMachine(client);
		KatelloUtils.destroyDeltaCloudMachine(client2);
		KatelloUtils.destroyDeltaCloudMachine(client3);
		
		server = null;
	}
}
