package com.redhat.qe.katello.tests.deltacloud;

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
import com.redhat.qe.tools.SSHCommandResult;

public class BaseDeltacloudTest extends KatelloCliTestScript {

	protected SSHCommandResult exec_result;

	// Katello objects below
	protected String org_name;
	protected String provider_name;
	protected String product_name;
	protected String repo_name;
	protected String env_name;
	protected String changeset_name;
	protected String system_name;
	protected String system_name2;
	protected String system_name3;
	protected String group_name;
	protected String group_name2;
	protected String system_uuid;
	protected String system_uuid2;
	protected String system_uuid3;
	protected DeltaCloudInstance server;
	protected DeltaCloudInstance client;
	protected DeltaCloudInstance client2;
	protected DeltaCloudInstance client3;
	protected String server_name;
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
		changeset_name = "changeset_"+uid;
		system_name = "system_"+uid;
		system_name2 = "system2_"+uid;
		system_name3 = "system3_"+uid;
		group_name = "group_"+uid;
		group_name2 = "group2_"+uid;
		
		server = KatelloUtils.getDeltaCloudServer(1);
		server_name = server.getHostName();
		
		client = KatelloUtils.getDeltaCloudClient(server_name, 1);
		client_name = client.getHostName();
		
		client2 = KatelloUtils.getDeltaCloudClient(server_name, 2);
		client_name2 = client2.getHostName();
		
		client3 = KatelloUtils.getDeltaCloudClient(server_name, 3);
		client_name3 = client3.getHostName();
		
		// Create org:
		KatelloOrg org = new KatelloOrg(this.org_name, "Package tests");
		org.runOn(client_name);
		exec_result = org.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		// Create provider:
		KatelloProvider prov = new KatelloProvider(provider_name, org_name,
				"Package provider", null);
		prov.runOn(client_name);
		exec_result = prov.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		// Create product:
		KatelloProduct prod = new KatelloProduct(product_name, org_name,
				provider_name, null, null, null, null, null);
		prod.runOn(client_name);
		exec_result = prod.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		KatelloRepo repo = new KatelloRepo(repo_name, org_name, product_name, REPO_INECAS_ZOO3, null, null);
		repo.runOn(client_name);
		exec_result = repo.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		KatelloEnvironment env = new KatelloEnvironment(env_name, null, org_name, KatelloEnvironment.LIBRARY);
		env.runOn(client_name);
		exec_result = env.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (env create)");
		
		// promote product to the env dev.
		exec_result = prod.promote(env_name);
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code (product promote)");

		exec_result = repo.synchronize();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		KatelloChangeset cs = new KatelloChangeset(changeset_name, org_name, env_name);
		cs.runOn(client_name);
		exec_result = cs.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (changeset create)");
		
		exec_result = cs.update_addProduct(product_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (changeset update add product)");
		
		exec_result = cs.apply();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (changeset promote)");
		
		KatelloSystem sys = new KatelloSystem(system_name, this.org_name, this.env_name);
		sys.runOn(client_name);
		exec_result = sys.rhsm_registerForce(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		
		exec_result = sys.rhsm_identity();
		system_uuid = KatelloCli.grepCLIOutput("Current identity is", exec_result.getStdout());
		
		exec_result = sys.subscriptions_available();
		String poolId1 = KatelloCli.grepCLIOutput("Id", getOutput(exec_result).trim(),1);
		Assert.assertNotNull(poolId1, "Check - pool Id is not null");
		
		exec_result = sys.rhsm_subscribe(poolId1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		sys = new KatelloSystem(system_name2, this.org_name, this.env_name);
		sys.runOn(client_name2);
		exec_result = sys.rhsm_registerForce(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		
		exec_result = sys.rhsm_identity();
		system_uuid2 = KatelloCli.grepCLIOutput("Current identity is", exec_result.getStdout());
		
		exec_result = sys.rhsm_subscribe(poolId1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		sys = new KatelloSystem(system_name3, this.org_name, this.env_name);
		sys.runOn(client_name3);
		exec_result = sys.rhsm_registerForce(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		
		exec_result = sys.rhsm_identity();
		system_uuid3 = KatelloCli.grepCLIOutput("Current identity is", exec_result.getStdout());
		
		exec_result = sys.rhsm_subscribe(poolId1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		KatelloSystemGroup group = new KatelloSystemGroup(group_name, this.org_name);
		group.runOn(client_name);
		exec_result = group.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = group.add_systems(system_uuid);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		exec_result = group.add_systems(system_uuid2);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = group.add_systems(system_uuid3);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		group = new KatelloSystemGroup(group_name2, this.org_name);
		group.runOn(client_name);
		exec_result = group.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = group.add_systems(system_uuid2);
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
