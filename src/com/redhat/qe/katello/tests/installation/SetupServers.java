package com.redhat.qe.katello.tests.installation;

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

		@BeforeSuite(description = "setup Deltacloud Server and client")
		public void setUp() {

			isDeltacloud = Boolean.parseBoolean(System.getProperty("runondeltacloud", "false"));

			if (isDeltacloud) {
				server = KatelloUtils.getDeltaCloudServer(1);
				server_name = server.getHostName();

				client = KatelloUtils.getDeltaCloudClient(server_name, 1);
				client_name = client.getHostName();

				System.setProperty("katello.server.hostname", server_name);
				System.setProperty("katello.client.hostname", client_name);
			}
		}

		@AfterSuite
		public void tearDown() {
			if (isDeltacloud) {
				KatelloUtils.destroyDeltaCloudMachine(server);
				KatelloUtils.destroyDeltaCloudMachine(client);
			}
		}

}
