package com.redhat.qe.katello.tests.installation;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.obj.DeltaCloudInstance;
import com.redhat.qe.katello.common.KatelloUtils;

@Test(groups = { "cfse-cli", "headpin-cli" })
public class SetupServers extends KatelloCliTestScript {

	protected DeltaCloudInstance server;
	protected String server_name;
	protected static boolean isDeltacloud = false;

	@BeforeSuite(description = "setup Deltacloud Server")
	public void setUp() {

		isDeltacloud = Boolean.parseBoolean(System.getProperty(
				"runondeltacloud", "false"));

		if (isDeltacloud) {
			server = KatelloUtils.getDeltaCloudServer();
			server_name = server.getHostName();

			if (Boolean.parseBoolean(System.getProperty(
					"deltacloud.installserver", "true"))) {
				System.setProperty("katello.server.hostname", server_name);
				System.setProperty("katello.client.hostname", server_name);
			}
		}
	}

	@AfterSuite(alwaysRun=true)
	public void tearDown() {
		if (isDeltacloud) {
			//KatelloUtils.destroyDeltaCloudMachine(server);
		}
	}

}
