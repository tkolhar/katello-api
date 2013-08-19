package com.redhat.qe.katello.tests.installation;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.DeltaCloudInstance;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups = { "cfse-cli", "headpin-cli" })
public class TestMultipleAgents extends KatelloCliTestBase {

	protected DeltaCloudInstance server;
	protected String server_name;
	protected ArrayList<DeltaCloudInstance> clients = new ArrayList<DeltaCloudInstance>();
	protected String org_name = null;
	protected String poolRhel;
	
	@Test(description = "setup Deltacloud Server")
	public void testMultipleClients() {

		server = KatelloUtils.getDeltaCloudServer();
		server_name = server.getHostName();
		System.setProperty("katello.server.hostname", server_name);
		System.setProperty("katello.client.hostname", server_name);

		createOrgStuff();
		
		StringTokenizer tok = new StringTokenizer(
				System.getProperty("deltacloud.client.imageid"), ",");
		
		while (tok.hasMoreTokens()) {
			String type = tok.nextToken().trim();
			DeltaCloudInstance client = KatelloUtils.getDeltaCloudClientCertOnly(
					server_name, DELTACLOUD_IMAGES.get(type));
			clients.add(client);
			
			try {
				Thread.sleep(300000);
			} catch (InterruptedException iex) {
			}
			
			testClientConsume(client.getHostName(), type);
			
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

		KatelloOrg org = new KatelloOrg(null, org_name, null);
		exec_result = org.cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");
		
		SSHCommandResult res;
		KatelloUtils.scpOnClient(null, "data/"+KatelloProvider.MANIFEST_12SUBSCRIPTIONS, "/tmp");

		KatelloProvider rh = new KatelloProvider(null, KatelloProvider.PROVIDER_REDHAT, org_name, null, null);
		res = rh.import_manifest("/tmp/"+KatelloProvider.MANIFEST_12SUBSCRIPTIONS, null);
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - provider import_manifest");
		org = new KatelloOrg(null, org_name, null);
		res = org.subscriptions();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - org subscriptions");
		
		// getting poolid could vary - might be need to make switch case here for different versions...
		poolRhel = KatelloUtils.grepCLIOutput("ID", KatelloCliTestBase.sgetOutput(res));
		if (poolRhel == null || poolRhel.isEmpty()) {
			poolRhel = KatelloUtils.grepCLIOutput("Id", KatelloCliTestBase.sgetOutput(res));
		}
	}
	
	private void testClientConsume(String client_name, String client_type) {
		
		KatelloSystem sys = new KatelloSystem(null, client_name+KatelloUtils.getUniqueID(), org_name, KatelloEnvironment.LIBRARY);
		sys.runOn(client_name);
		exec_result = sys.rhsm_registerForce();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		exec_result = sys.rhsm_subscribe(poolRhel);
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		
		exec_result = sys.list();
		Assert.assertTrue(exec_result.getStdout().contains(sys.name), "Check system " + sys.name + " is registered correctly to RHEL pool");
		
		rhsm_clean(client_name);
		
		exec_result = sys.list();
		Assert.assertFalse(exec_result.getStdout().contains(sys.name), "Check system " + sys.name + " is unregistered");
	}

}
