package com.redhat.qe.katello.tests.deltacloud;

import java.io.File;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.obj.DeltaCloudInstance;
import com.redhat.qe.katello.base.obj.KatelloChangeset;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.base.obj.KatelloSystemGroup;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SCPTools;
import com.redhat.qe.tools.SSHCommandResult;

public class BaseDeltacloudTest extends KatelloCliTestScript {

	public static final String MANIFEST_12SUBSCRIPTIONS = "manifest-automation-CLI-12subscriptions.zip";
	
	protected SSHCommandResult exec_result;

	// Katello objects below
	protected static String org_name;
	protected static String provider_name;
	protected static String product_name;
	protected static String repo_name;
	protected static String env_name;
	protected static String env_name2;
	protected static String changeset_name;
	protected static String changeset_name2;
	protected static String changeset_name3;
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

	public void setUp(){
		if (server != null) return;
		
		String uid = KatelloUtils.getUniqueID();
		org_name = "org_"+uid;
		provider_name = "provider_"+uid;
		product_name = "product_"+uid;
		repo_name = "repo_name_"+uid;
		env_name = "env_Dev_"+uid;
		env_name2 = "env_Test_"+uid;
		changeset_name = "changeset_"+uid;
		changeset_name2 = "chsrhel_"+uid;
		changeset_name3 = "chsrhel3_"+uid;
		system_name = "system_"+uid;
		system_name2 = "system2_"+uid;
		system_name3 = "system3_"+uid;
		group_name = "group_"+uid;
		group_name2 = "group2_"+uid;
		
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
		KatelloOrg org = new KatelloOrg(org_name, "Package tests");
		exec_result = org.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		// Create provider:
		KatelloProvider prov = new KatelloProvider(provider_name, org_name,
				"Package provider", null);
		exec_result = prov.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		// Create product:
		KatelloProduct prod = new KatelloProduct(product_name, org_name,
				provider_name, null, null, null, null, null);
		exec_result = prod.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		KatelloRepo repo = new KatelloRepo(repo_name, org_name, product_name, REPO_INECAS_ZOO3, null, null);
		exec_result = repo.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		KatelloEnvironment env = new KatelloEnvironment(env_name, null, org_name, KatelloEnvironment.LIBRARY);
		exec_result = env.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (env create)");
		
		env = new KatelloEnvironment(env_name2, null, org_name, KatelloEnvironment.LIBRARY);
		exec_result = env.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (env create)");
		
		// promote product to the env dev.
		exec_result = prod.promote(env_name);
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code (product promote)");

		exec_result = prod.promote(env_name2);
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code (product promote)");
		
		exec_result = repo.synchronize();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		KatelloChangeset cs = new KatelloChangeset(changeset_name, org_name, env_name);
		exec_result = cs.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (changeset create)");
		
		exec_result = cs.update_addProduct(product_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (changeset update add product)");
		
		exec_result = cs.apply();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (changeset promote)");
		
		SCPTools scp = new SCPTools(
		System.getProperty("katello.server.hostname", "localhost"), 
		System.getProperty("katello.server.ssh.user", "root"), 
		System.getProperty("katello.server.sshkey.private", ".ssh/id_hudson_dsa"), 
		System.getProperty("katello.server.sshkey.passphrase", "null"));
		Assert.assertTrue(scp.sendFile("data"+File.separator+MANIFEST_12SUBSCRIPTIONS, "/tmp"),
				MANIFEST_12SUBSCRIPTIONS+" sent successfully");			
			
		prov = new KatelloProvider(KatelloProvider.PROVIDER_REDHAT, org_name, null, null);
		prov.runOn(server_name);
		SSHCommandResult res = prov.import_manifest("/tmp"+File.separator+MANIFEST_12SUBSCRIPTIONS, new Boolean(true));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (provider import_manifest)");
		
		prod = new KatelloProduct(KatelloProduct.RHEL_SERVER,org_name, KatelloProvider.PROVIDER_REDHAT, null, null, null,null, null);
		res = prod.repository_set_enable(KatelloProduct.REPO_SET_NAME,KatelloProduct.RHEL_SERVER);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo set enable)");
		
		log.info("Enable repo: ["
		+KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT+"]");
		repo = new KatelloRepo(KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT, org_name, KatelloProduct.RHEL_SERVER, null, null, null);
		res = repo.enable();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo enable)");
		Assert.assertTrue(getOutput(res).contains("enabled."),"Message - (repo enable)");
		
		cs = new KatelloChangeset(changeset_name3, org_name, env_name2);
		cs.create();
		res = cs.update_addProduct(KatelloProduct.RHEL_SERVER);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (changeset add_product)");
		res = cs.apply();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (changeset promote)");
		
		res = repo.synchronize();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo synchronize)");
		res = repo.info();
		int pkgCount = Integer.parseInt(KatelloCli.grepCLIOutput("Package Count", res.getStdout()));
		String progress = KatelloCli.grepCLIOutput("Progress", res.getStdout());
		Assert.assertTrue(pkgCount>0, "Check - Packages >0");
		Assert.assertTrue(progress.equals("Finished"), "Check: status of repo sync - Finished");
		
		cs = new KatelloChangeset(changeset_name2, org_name, env_name);
		cs.create();
		res = cs.update_addProduct(KatelloProduct.RHEL_SERVER);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (changeset add_product)");
		res = cs.apply();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (changeset promote)");
		
		KatelloSystem sys = new KatelloSystem(system_name, org_name, env_name);
		sys.runOn(client_name);
		exec_result = sys.rhsm_registerForce(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		
		exec_result = sys.rhsm_identity();
		system_uuid = KatelloCli.grepCLIOutput("Current identity is", exec_result.getStdout());
		
		exec_result = sys.subscriptions_available();
		String poolId1 = KatelloCli.grepCLIOutput("Id", getOutput(exec_result).trim(),1);
		String poolId2 = KatelloCli.grepCLIOutput("Id", getOutput(exec_result).trim(),2);
		Assert.assertNotNull(poolId1, "Check - pool Id is not null");
		
		exec_result = sys.rhsm_subscribe(poolId1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = sys.rhsm_subscribe(poolId2);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		sys = new KatelloSystem(system_name2, org_name, env_name);
		sys.runOn(client_name2);
		exec_result = sys.rhsm_registerForce(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		
		exec_result = sys.rhsm_identity();
		system_uuid2 = KatelloCli.grepCLIOutput("Current identity is", exec_result.getStdout());
		
		exec_result = sys.rhsm_subscribe(poolId1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		exec_result = sys.rhsm_subscribe(poolId2);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		sys = new KatelloSystem(system_name3, org_name, env_name);
		sys.runOn(client_name3);
		exec_result = sys.rhsm_registerForce(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		
		exec_result = sys.rhsm_identity();
		system_uuid3 = KatelloCli.grepCLIOutput("Current identity is", exec_result.getStdout());
		
		exec_result = sys.rhsm_subscribe(poolId1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = sys.rhsm_subscribe(poolId2);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		KatelloSystemGroup group = new KatelloSystemGroup(group_name, org_name);
		exec_result = group.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = group.add_systems(system_uuid);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		exec_result = group.add_systems(system_uuid2);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = group.add_systems(system_uuid3);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		group = new KatelloSystemGroup(group_name2, org_name);
		exec_result = group.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = group.add_systems(system_uuid2);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		KatelloUtils.sshOnClient(client_name, "service goferd restart;");
		KatelloUtils.sshOnClient(client_name2, "service goferd restart;");
		KatelloUtils.sshOnClient(client_name3, "service goferd restart;");
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
