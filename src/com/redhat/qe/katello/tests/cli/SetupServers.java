package com.redhat.qe.katello.tests.cli;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.obj.DeltaCloudInstance;
import com.redhat.qe.katello.common.KatelloUtils;

@Test(groups = { "cfse-cli", "headpin-cli"})
public class SetupServers extends KatelloCliTestScript {
	
	protected DeltaCloudInstance server;
	protected DeltaCloudInstance client;
	protected String server_name;
	protected String client_name;
	protected static boolean isDeltacloud = false;
	
	@BeforeSuite(description = "setup Deltacloud Server and client", alwaysRun=true)
	public void setUp() {
		
		isDeltacloud = Boolean.parseBoolean(System.getProperty("runondeltacloud", "false"));
		
		if (isDeltacloud) {
			server = KatelloUtils.getDeltaCloudServer();
			server_name = server.getHostName();
			
			client = KatelloUtils.getDeltaCloudClient(server_name);
			client_name = client.getHostName();
			
			System.setProperty("katello.server.hostname", server.getHostName());
			System.setProperty("katello.client.hostname", server.getHostName());
		}
		KatelloUtils.disableYumRepo("beaker");
		KatelloUtils.disableYumRepo("epel");
	}
	
	@AfterSuite(alwaysRun=true)
	public void tearDown() {
		if (isDeltacloud) {
			KatelloUtils.destroyDeltaCloudMachine(server);
			KatelloUtils.destroyDeltaCloudMachine(client);
		}
		KatelloUtils.enableYumRepo("beaker");
		KatelloUtils.enableYumRepo("epel");
	}

}
