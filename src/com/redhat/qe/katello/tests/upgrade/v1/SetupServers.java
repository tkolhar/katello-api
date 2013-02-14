package com.redhat.qe.katello.tests.upgrade.v1;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.obj.DeltaCloudInstance;
import com.redhat.qe.katello.common.KatelloUtils;

@Test(groups = { "pre-upgrade" })
public class SetupServers extends KatelloCliTestScript {
	
	protected DeltaCloudInstance server;
	protected DeltaCloudInstance client;
	protected DeltaCloudInstance client2;
	protected DeltaCloudInstance client3;
	protected static String server_name;
	protected static String client_name;
	protected static String client_name2;
	protected static String client_name3;
	protected static boolean isDeltacloud = false;
	
	@BeforeSuite(description = "setup Deltacloud Server and client")
	public void setUp() {
		
		isDeltacloud = Boolean.parseBoolean(System.getProperty("runondeltacloud", "false"));
		
		if (isDeltacloud) {
			server = KatelloUtils.getDeltaCloudServer(1);
			server_name = server.getHostName();
			
			client = KatelloUtils.getDeltaCloudClient(server_name, 1);
			client_name = client.getHostName();
			
			client2 = KatelloUtils.getDeltaCloudClient(server_name, 2);
			client_name2 = client2.getHostName();
			
			client3 = KatelloUtils.getDeltaCloudClient(server_name, 3);
			client_name3 = client3.getHostName();
			
			System.setProperty("katello.server.hostname", server.getHostName());
			System.setProperty("katello.client.hostname", client.getHostName());
		}
	}
	
	@AfterSuite
	public void tearDown() {
		if (isDeltacloud) {
//			KatelloUtils.destroyDeltaCloudMachine(server);
//			KatelloUtils.destroyDeltaCloudMachine(client);
//			KatelloUtils.destroyDeltaCloudMachine(client2);
//			KatelloUtils.destroyDeltaCloudMachine(client3);
		}
	}

}
