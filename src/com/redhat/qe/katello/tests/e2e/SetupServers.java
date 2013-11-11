package com.redhat.qe.katello.tests.e2e;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.DeltaCloudInstance;
import com.redhat.qe.katello.common.KatelloUtils;

public class SetupServers extends KatelloCliTestBase {
	
	protected DeltaCloudInstance server;
	protected ArrayList<DeltaCloudInstance> clients = new ArrayList<DeltaCloudInstance>();;
	protected String server_name;
	protected static boolean isDeltacloud = false;
	
	@BeforeSuite(description = "setup Deltacloud Server and client", alwaysRun=true)
	public void setUp() {
		
		isDeltacloud = Boolean.parseBoolean(System.getProperty("runondeltacloud", "false"));
		
		if (isDeltacloud) {
			server = KatelloUtils.getDeltaCloudServer();
			server_name = server.getHostName();

			StringTokenizer tok = new StringTokenizer(System.getProperty("deltacloud.client.imageid"),",");
			StringBuffer workers = new StringBuffer();
			while(tok.hasMoreTokens()){
				DeltaCloudInstance client = KatelloUtils.getDeltaCloudClient(server_name, tok.nextToken());
				workers.append(client.getHostName());
				if (tok.hasMoreTokens()) workers.append(",");
				clients.add(client);
			}
			
			System.setProperty("katello.server.hostname", server_name);
			System.setProperty("katello.workers.list", workers.toString());
		}
	}
	
	@AfterSuite(alwaysRun=true)
	public void tearDown() {
		boolean keepAlive = Boolean.parseBoolean(System.getProperty("deltacloud.keepalive", "false"));
		if (isDeltacloud && !keepAlive) {
			KatelloUtils.destroyDeltaCloudMachine(server);
			for (DeltaCloudInstance client : clients) {
				KatelloUtils.destroyDeltaCloudMachine(client);
			}
		} else if (isDeltacloud && keepAlive) {
			KatelloUtils.logServerInfo(server_name, System.getProperty("katello.workers.list"));
		}
	}
}
