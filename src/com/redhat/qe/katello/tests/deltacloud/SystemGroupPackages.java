package com.redhat.qe.katello.tests.deltacloud;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

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

@Test(groups={"cfse-dc"})
public class SystemGroupPackages extends KatelloCliTestScript {
	
	private SSHCommandResult exec_result;

	// Katello objects below
	private String org_name;
	private String provider_name;
	private String product_name;
	private String repo_name;
	private String env_name;
	private String changeset_name;
	private String system_name;
	private String system_name2;
	private String system_name3;
	private String group_name;
	private String system_uuid;
	private String system_uuid2;
	private String system_uuid3;
	private DeltaCloudInstance server;
	private DeltaCloudInstance client;
	private DeltaCloudInstance client2;
	private DeltaCloudInstance client3;
	private String server_name;
	private String client_name;
	private String client_name2;
	private String client_name3;
	
	@BeforeClass(description="Generate unique names")
	public void setUp(){
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
		
		server = KatelloUtils.getDeltaCloudServer(1);
		server_name = server.getHostName();
		
		client = KatelloUtils.getDeltaCloudClient(server.getHostName(), 1);
		client_name = client.getHostName();
		
		client2 = KatelloUtils.getDeltaCloudClient(server.getHostName(), 2);
		client_name2 = client2.getHostName();
		
		client3 = KatelloUtils.getDeltaCloudClient(server.getHostName(), 3);
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
		
		KatelloUtils.sshOnClient(client_name, "service goferd restart;");
		KatelloUtils.sshOnClient(client_name2, "service goferd restart;");
		KatelloUtils.sshOnClient(client_name3, "service goferd restart;");
	}
	
	@AfterClass
	public void tearDown() {
		KatelloUtils.destroyDeltaCloudMachine(server);
		KatelloUtils.destroyDeltaCloudMachine(client);
		KatelloUtils.destroyDeltaCloudMachine(client2);
		KatelloUtils.destroyDeltaCloudMachine(client3);
	}
	
	@Test(description = "Install lion package in system group, verify that wolf and lion are installed")
	public void test_installPackageOnSystemGroup() {
		
		KatelloUtils.sshOnClient(client_name, "yum -y erase wolf lion");
		KatelloUtils.sshOnClient(client_name2, "yum -y erase wolf lion");
		KatelloUtils.sshOnClient(client_name3, "yum -y erase wolf lion");
		
		KatelloSystemGroup group = new KatelloSystemGroup(group_name, this.org_name);
		group.runOn(client_name);
		exec_result = group.packages_install("lion");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).trim().contains("Remote action finished"));
		Assert.assertTrue(getOutput(exec_result).trim().contains("Package Install Complete"));
		
		verifyPackageInstalled(client_name);
		
		verifyPackageInstalled(client_name2);
		
		verifyPackageInstalled(client_name3);
	}
	
	private void verifyPackageInstalled(String clientName) {
		exec_result = KatelloUtils.sshOnClient(clientName, "rpm -q lion");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code (rpm -q lion)");
		Assert.assertTrue(getOutput(exec_result).trim().contains("lion-"));
		
		exec_result = KatelloUtils.sshOnClient(clientName, "rpm -q wolf");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code (rpm -q wolf)");
		Assert.assertTrue(getOutput(exec_result).trim().contains("wolf-"));
	}

	@Test(description = "Remove wolf package from system group, verify that wolf and lion are removed", dependsOnMethods={"test_installPackageOnSystemGroup"})
	public void test_removePackageFromSystemGroup() {
		
		KatelloSystemGroup group = new KatelloSystemGroup(group_name, this.org_name);
		group.runOn(client_name);
		exec_result = group.packages_remove("wolf");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).trim().contains("Remote action finished"));
		Assert.assertTrue(getOutput(exec_result).trim().contains("Package Remove Complete"));

		verifyPackageRemoved(client_name);
		
		verifyPackageRemoved(client_name2);
		
		verifyPackageRemoved(client_name3);
	}
	
	private void verifyPackageRemoved(String clientName) {
		exec_result = KatelloUtils.sshOnClient(clientName, "rpm -q lion");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 1, "Check - return code (rpm -q lion)");
		Assert.assertTrue(getOutput(exec_result).trim().contains("package lion is not installed"));
		
		exec_result = KatelloUtils.sshOnClient(clientName, "rpm -q wolf");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 1, "Check - return code (rpm -q wolf)");
		Assert.assertTrue(getOutput(exec_result).trim().contains("package wolf is not installed"));
	}
	
	@Test(description = "Install lion zebra tiger packages in system group, verify that packages are installed")
	public void test_installPackagesOnSystemGroup() {
		
		KatelloUtils.sshOnClient(client_name, "yum -y erase zebra lion tiger");
		KatelloUtils.sshOnClient(client_name2, "yum -y erase zebra lion tiger");
		KatelloUtils.sshOnClient(client_name3, "yum -y erase zebra lion tiger");
		
		KatelloSystemGroup group = new KatelloSystemGroup(group_name, this.org_name);
		group.runOn(client_name);
		exec_result = group.packages_install("lion,zebra,tiger");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).trim().contains("Remote action finished"));
		Assert.assertTrue(getOutput(exec_result).trim().contains("Package Install Complete"));
		
		verifyPackagesInstalled(client_name);
		
		verifyPackagesInstalled(client_name2);
		
		verifyPackagesInstalled(client_name3);
	}
	
	private void verifyPackagesInstalled(String clientName) {
		exec_result = KatelloUtils.sshOnClient(clientName, "rpm -q lion");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code (rpm -q lion)");
		Assert.assertTrue(getOutput(exec_result).trim().contains("lion-"));
		
		exec_result = KatelloUtils.sshOnClient(clientName, "rpm -q zebra");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code (rpm -q zebra)");
		Assert.assertTrue(getOutput(exec_result).trim().contains("zebra-"));

		exec_result = KatelloUtils.sshOnClient(clientName, "rpm -q tiger");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code (rpm -q tiger)");
		Assert.assertTrue(getOutput(exec_result).trim().contains("tiger-"));		
	}
	
	@Test(description = "Install birds package group in system group, verify that all birds packages are installed")
	public void test_installPackageGroupOnSystemGroup() {
		
		KatelloUtils.sshOnClient(client_name, "yum -y erase stork cockateel penguin duck");
		KatelloUtils.sshOnClient(client_name2, "yum -y erase stork cockateel penguin duck");
		KatelloUtils.sshOnClient(client_name3, "yum -y erase stork cockateel penguin duck");
		
		KatelloSystemGroup group = new KatelloSystemGroup(group_name, this.org_name);
		group.runOn(client_name);
		exec_result = group.packagegroup_install("birds");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).trim().contains("Remote action finished"));
		Assert.assertTrue(getOutput(exec_result).trim().contains("Package Group Install Complete"));
		
		verifyPackageGroupInstalled(client_name);
		
		verifyPackageGroupInstalled(client_name2);
		
		verifyPackageGroupInstalled(client_name3);
	}
	
	private void verifyPackageGroupInstalled(String clientName) {
		exec_result = KatelloUtils.sshOnClient(clientName, "rpm -q stork");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code (rpm -q stork)");
		Assert.assertTrue(getOutput(exec_result).trim().contains("stork-"));
		
		exec_result = KatelloUtils.sshOnClient(clientName, "rpm -q cockateel");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code (rpm -q cockateel)");
		Assert.assertTrue(getOutput(exec_result).trim().contains("cockateel-"));

		exec_result = KatelloUtils.sshOnClient(clientName, "rpm -q penguin");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code (rpm -q penguin)");
		Assert.assertTrue(getOutput(exec_result).trim().contains("penguin-"));
		
		exec_result = KatelloUtils.sshOnClient(clientName, "rpm -q duck");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code (rpm -q duck)");
		Assert.assertTrue(getOutput(exec_result).trim().contains("duck-"));
	}

	@Test(description = "Remove stork and cockateel packages from system group, verify that packages are removed", dependsOnMethods={"test_installPackageGroupOnSystemGroup"})
	public void test_removePackagesFromSystemGroup() {
		
		KatelloSystemGroup group = new KatelloSystemGroup(group_name, this.org_name);
		group.runOn(client_name);
		exec_result = group.packages_remove("stork,cockateel");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).trim().contains("Remote action finished"));
		Assert.assertTrue(getOutput(exec_result).trim().contains("Package Remove Complete"));
		
		verifyPackagesRemoved(client_name);
		
		verifyPackagesRemoved(client_name2);
		
		verifyPackagesRemoved(client_name3);
	}
	
	private void verifyPackagesRemoved(String clientName) {
		exec_result = KatelloUtils.sshOnClient(clientName, "rpm -q stork");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 1, "Check - return code (rpm -q stork)");
		Assert.assertTrue(getOutput(exec_result).trim().contains("package stork is not installed"));
		
		exec_result = KatelloUtils.sshOnClient(clientName, "rpm -q cockateel");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 1, "Check - return code (rpm -q cockateel)");
		Assert.assertTrue(getOutput(exec_result).trim().contains("package cockateel is not installed"));	
	}
	
	@Test(description = "Update birds package group in system group, verify that all birds packages are installed", dependsOnMethods={"test_removePackagesFromSystemGroup"})
	public void test_updatePackageGroupOnSystemGroup() {
		
		KatelloSystemGroup group = new KatelloSystemGroup(group_name, this.org_name);
		group.runOn(client_name);
		exec_result = group.packagegroup_update("birds");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).trim().contains("Remote action finished"));
		Assert.assertTrue(getOutput(exec_result).trim().contains("Package Group Install Complete"));
		
		verifyPackageGroupUpdated(client_name);
		
		verifyPackageGroupUpdated(client_name2);
		
		verifyPackageGroupUpdated(client_name3);
	}
	
	private void verifyPackageGroupUpdated(String clientName) {
		exec_result = KatelloUtils.sshOnClient(clientName, "rpm -q stork");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code (rpm -q stork)");
		Assert.assertTrue(getOutput(exec_result).trim().contains("stork-"));
		
		exec_result = KatelloUtils.sshOnClient(clientName, "rpm -q cockateel");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code (rpm -q cockateel)");
		Assert.assertTrue(getOutput(exec_result).trim().contains("cockateel-"));

		exec_result = KatelloUtils.sshOnClient(clientName, "rpm -q penguin");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code (rpm -q penguin)");
		Assert.assertTrue(getOutput(exec_result).trim().contains("penguin-"));
		
		exec_result = KatelloUtils.sshOnClient(clientName, "rpm -q duck");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code (rpm -q duck)");
		Assert.assertTrue(getOutput(exec_result).trim().contains("duck-"));
	}
	
	@Test(description = "Remove birds package group from system group, verify that all packages are removed", dependsOnMethods={"test_updatePackageGroupOnSystemGroup"})
	public void test_removePackageGroupFromSystemGroup() {
		
		KatelloSystemGroup group = new KatelloSystemGroup(group_name, this.org_name);
		group.runOn(client_name);
		exec_result = group.packagegroup_remove("birds");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).trim().contains("Remote action finished"));
		Assert.assertTrue(getOutput(exec_result).trim().contains("Package Group Remove Complete"));
		
		verifyPackageGroupRemoved(client_name);
		
		verifyPackageGroupRemoved(client_name2);
		
		verifyPackageGroupRemoved(client_name3);
	}
	
	private void verifyPackageGroupRemoved(String clientName) {
		exec_result = KatelloUtils.sshOnClient(clientName, "rpm -q stork");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 1, "Check - return code (rpm -q stork)");
		Assert.assertTrue(getOutput(exec_result).trim().contains("package stork is not installed"));
		
		exec_result = KatelloUtils.sshOnClient(clientName, "rpm -q cockateel");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 1, "Check - return code (rpm -q cockateel)");
		Assert.assertTrue(getOutput(exec_result).trim().contains("package cockateel is not installed"));

		exec_result = KatelloUtils.sshOnClient(clientName, "rpm -q penguin");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 1, "Check - return code (rpm -q penguin)");
		Assert.assertTrue(getOutput(exec_result).trim().contains("package penguin is not installed"));
		
		exec_result = KatelloUtils.sshOnClient(clientName, "rpm -q duck");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 1, "Check - return code (rpm -q duck)");
		Assert.assertTrue(getOutput(exec_result).trim().contains("package duck is not installed"));
	}
	
	@Test(description = "Install birds,mammals package groups in system group, verify that all birds and mammals packages are installed")
	public void test_installPackageGroupsOnSystemGroup() {
		
		KatelloUtils.sshOnClient(client_name, "yum -y erase stork cockateel penguin duck lion tyger wolf zebra");
		KatelloUtils.sshOnClient(client_name2, "yum -y erase stork cockateel penguin duck lion tyger wolf zebra");
		KatelloUtils.sshOnClient(client_name3, "yum -y erase stork cockateel penguin duck lion tyger wolf zebra");
		
		KatelloSystemGroup group = new KatelloSystemGroup(group_name, this.org_name);
		group.runOn(client_name);
		exec_result = group.packagegroup_install("birds, mammals");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).trim().contains("Remote action finished"));
		Assert.assertTrue(getOutput(exec_result).trim().contains("Package Group Install Complete"));
		
		verifyPackageGroupsInstalled(client_name);
		
		verifyPackageGroupsInstalled(client_name2);
		
		verifyPackageGroupsInstalled(client_name3);
	}
	
	private void verifyPackageGroupsInstalled(String clientName) {
		exec_result = KatelloUtils.sshOnClient(clientName, "rpm -q stork");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code (rpm -q stork)");
		Assert.assertTrue(getOutput(exec_result).trim().contains("stork-"));
		
		exec_result = KatelloUtils.sshOnClient(clientName, "rpm -q cockateel");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code (rpm -q cockateel)");
		Assert.assertTrue(getOutput(exec_result).trim().contains("cockateel-"));

		exec_result = KatelloUtils.sshOnClient(clientName, "rpm -q penguin");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code (rpm -q penguin)");
		Assert.assertTrue(getOutput(exec_result).trim().contains("penguin-"));
		
		exec_result = KatelloUtils.sshOnClient(clientName, "rpm -q duck");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code (rpm -q duck)");
		Assert.assertTrue(getOutput(exec_result).trim().contains("duck-"));
		
		exec_result = KatelloUtils.sshOnClient(clientName, "rpm -q lion");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code (rpm -q lion)");
		Assert.assertTrue(getOutput(exec_result).trim().contains("lion-"));
		
		exec_result = KatelloUtils.sshOnClient(clientName, "rpm -q zebra");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code (rpm -q zebra)");
		Assert.assertTrue(getOutput(exec_result).trim().contains("zebra-"));

		exec_result = KatelloUtils.sshOnClient(clientName, "rpm -q tiger");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code (rpm -q tiger)");
		Assert.assertTrue(getOutput(exec_result).trim().contains("tiger-"));
	}
	
	@Test(description = "Remove stork,cockateel,tiger,lion packages from system group, verify that packages are removed",
			dependsOnMethods={"test_installPackageGroupsOnSystemGroup"})
	public void test_removePackagesFromMultyPackageGroup() {
		
		KatelloSystemGroup group = new KatelloSystemGroup(group_name, this.org_name);
		group.runOn(client_name);
		exec_result = group.packages_remove("stork,cockateel,tiger,lion");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).trim().contains("Remote action finished"));
		Assert.assertTrue(getOutput(exec_result).trim().contains("Package Remove Complete"));
		
		verifyPackagesRemovedFromGroup(client_name);
		
		verifyPackagesRemovedFromGroup(client_name2);
		
		verifyPackagesRemovedFromGroup(client_name3);
	}
	
	private void verifyPackagesRemovedFromGroup(String clientName) {
		exec_result = KatelloUtils.sshOnClient(clientName, "rpm -q stork");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 1, "Check - return code (rpm -q stork)");
		Assert.assertTrue(getOutput(exec_result).trim().contains("package stork is not installed"));
		
		exec_result = KatelloUtils.sshOnClient(clientName, "rpm -q cockateel");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 1, "Check - return code (rpm -q cockateel)");
		Assert.assertTrue(getOutput(exec_result).trim().contains("package cockateel is not installed"));
		
		exec_result = KatelloUtils.sshOnClient(clientName, "rpm -q lion");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 1, "Check - return code (rpm -q lion)");
		Assert.assertTrue(getOutput(exec_result).trim().contains("package lion is not installed"));
		
		exec_result = KatelloUtils.sshOnClient(clientName, "rpm -q tiger");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 1, "Check - return code (rpm -q tiger)");
		Assert.assertTrue(getOutput(exec_result).trim().contains("package tiger is not installed"));
	}
	
	
	@Test(description = "Update birds,mammals package groups in system group, verify that all birds and mammals packages are installed",
			dependsOnMethods={"test_removePackagesFromMultyPackageGroup"})
	public void test_updatePackageGroupsOnSystemGroup() {
		
		KatelloSystemGroup group = new KatelloSystemGroup(group_name, this.org_name);
		group.runOn(client_name);
		exec_result = group.packagegroup_update("birds,mammals");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).trim().contains("Remote action finished"));
		Assert.assertTrue(getOutput(exec_result).trim().contains("Package Group Install Complete"));
		
		verifyUpdatePackageGroups(client_name);
		
		verifyUpdatePackageGroups(client_name2);
		
		verifyUpdatePackageGroups(client_name3);
	}
	
	private void verifyUpdatePackageGroups(String clientName) {
		exec_result = KatelloUtils.sshOnClient(clientName, "rpm -q stork");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code (rpm -q stork)");
		Assert.assertTrue(getOutput(exec_result).trim().contains("stork-"));
		
		exec_result = KatelloUtils.sshOnClient(clientName, "rpm -q cockateel");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code (rpm -q cockateel)");
		Assert.assertTrue(getOutput(exec_result).trim().contains("cockateel-"));
		
		exec_result = KatelloUtils.sshOnClient(clientName, "rpm -q lion");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code (rpm -q lion)");
		Assert.assertTrue(getOutput(exec_result).trim().contains("lion-"));

		exec_result = KatelloUtils.sshOnClient(clientName, "rpm -q tiger");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code (rpm -q tiger)");
		Assert.assertTrue(getOutput(exec_result).trim().contains("tiger-"));	
	}
	
	@Test(description = "Remove birds,mammals package group from system group, verify that all packages are removed",
			dependsOnMethods={"test_updatePackageGroupsOnSystemGroup"})
	public void test_removePackageGroupsFromSystemGroup() {
		
		KatelloSystemGroup group = new KatelloSystemGroup(group_name, this.org_name);
		group.runOn(client_name);
		exec_result = group.packagegroup_remove("birds,mammals");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).trim().contains("Remote action finished"));
		Assert.assertTrue(getOutput(exec_result).trim().contains("Package Group Remove Complete"));
		
		verifyPackageGroupsRemoved(client_name);
		
		verifyPackageGroupsRemoved(client_name2);
		
		verifyPackageGroupsRemoved(client_name3);
	}
	
	private void verifyPackageGroupsRemoved(String clientName) {
		exec_result = KatelloUtils.sshOnClient(clientName, "rpm -q stork");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 1, "Check - return code (rpm -q stork)");
		Assert.assertTrue(getOutput(exec_result).trim().contains("package stork is not installed"));
		
		exec_result = KatelloUtils.sshOnClient(clientName, "rpm -q cockateel");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 1, "Check - return code (rpm -q cockateel)");
		Assert.assertTrue(getOutput(exec_result).trim().contains("package cockateel is not installed"));

		exec_result = KatelloUtils.sshOnClient(clientName, "rpm -q penguin");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 1, "Check - return code (rpm -q penguin)");
		Assert.assertTrue(getOutput(exec_result).trim().contains("package penguin is not installed"));
		
		exec_result = KatelloUtils.sshOnClient(clientName, "rpm -q duck");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 1, "Check - return code (rpm -q duck)");
		Assert.assertTrue(getOutput(exec_result).trim().contains("package duck is not installed"));
		
		exec_result = KatelloUtils.sshOnClient(clientName, "rpm -q lion");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 1, "Check - return code (rpm -q lion)");
		Assert.assertTrue(getOutput(exec_result).trim().contains("package lion is not installed"));
		
		exec_result = KatelloUtils.sshOnClient(clientName, "rpm -q tiger");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 1, "Check - return code (rpm -q tiger)");
		Assert.assertTrue(getOutput(exec_result).trim().contains("package tiger is not installed"));
		
		exec_result = KatelloUtils.sshOnClient(clientName, "rpm -q zebra");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 1, "Check - return code (rpm -q zebra)");
		Assert.assertTrue(getOutput(exec_result).trim().contains("package zebra is not installed"));
	}
}
