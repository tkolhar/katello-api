package com.redhat.qe.katello.tests.installation;

import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliDataProvider;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.DeltaCloudInstance;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.common.KatelloUtils;

@Test(groups = { "cfse-cli", "headpin-cli" })
public class TestMultipleAgents extends KatelloCliTestBase {

	public static final String IS_BEAKER = "IS BEAKER";
	protected DeltaCloudInstance server;
	protected String server_name;
	protected ArrayList<DeltaCloudInstance> clients = new ArrayList<DeltaCloudInstance>();
	protected String org_name = "SAM matrix";
	protected String poolRhel;
	
	@BeforeClass(description = "setup Deltacloud Server", alwaysRun=true)
	public void setUp() {
		server_name = System.getProperty("katello.server.hostname");
		
		if (server_name == null || server_name.isEmpty() || !KatelloUtils.isKatelloAvailable(server_name)) {
			server = KatelloUtils.getDeltaCloudServer();
			server_name = server.getHostName();
			System.setProperty("katello.server.hostname", server_name);
		}
		System.setProperty("katello.client.hostname", server_name);
		createOrgStuff();
	}
	protected static Logger log = Logger.getLogger(TestMultipleAgents.class.getName());
	
	@Test(description = "provision client and run test on it", dataProvider = "multiple_agents", 
			dataProviderClass = KatelloCliDataProvider.class)
	public void testMultipleClients(String type) {
		
		// in case of: IS_BEAKER the data provider will have just 1 loop and it will end within this if scope ONLY.
		if(type.equals(IS_BEAKER)){
			String hostname = System.getProperty("katello.client.hostname"); // getting bkr machine hostname
			testClientConsume(hostname, KatelloUtils.getServerReleaseInfo(hostname)); // collect release/arch info
			rhsm_clean(hostname);
			return;
		}
		
		DeltaCloudInstance client = KatelloUtils.getDeltaCloudClientCertOnly(
				server_name, DELTACLOUD_IMAGES.get(type));
		clients.add(client);
				
		KatelloUtils.disableYumRepo(client.getIpAddress(),"beaker");
		KatelloUtils.disableYumRepo(client.getIpAddress(),"epel");
		KatelloUtils.disableYumRepo(client.getIpAddress(),"katello-tools");
		
		try {
			configClient(client.getIpAddress());
			testClientConsume(client.getIpAddress(), type);
		} finally {
			rhsm_clean(client.getIpAddress());
			KatelloUtils.destroyDeltaCloudMachine(client);
		}
	}

	@AfterClass(alwaysRun = true)
	public void tearDown() {
		for (DeltaCloudInstance client : clients) {
			KatelloUtils.destroyDeltaCloudMachine(client);
		}
	}
	
	private void createOrgStuff() {
		KatelloOrg org = new KatelloOrg(null, org_name, null);
		if (org.cli_info().getExitCode().intValue() != 0) {
			exec_result = org.cli_create();
			Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");
		}
		
		exec_result = org.subscriptions();
		if (!getOutput(exec_result).contains("Red Hat Employee Subscription")) {
			KatelloUtils.scpOnClient(null, "data/"+KatelloProvider.MANIFEST_SAM_MATRIX, "/tmp");

			KatelloProvider rh = new KatelloProvider(null, KatelloProvider.PROVIDER_REDHAT, org_name, null, null);
			exec_result = rh.import_manifest("/tmp/"+KatelloProvider.MANIFEST_SAM_MATRIX, null);
			Assert.assertTrue(exec_result.getExitCode().intValue()==0, "exit(0) - provider import_manifest");	
		}
	}
	
	private void configClient(String client_name) {
		KatelloUtils.sshOnClient(client_name, "yum install -y yum-plugin-security yum-security");
		KatelloUtils.sshOnClient(client_name, "yum erase -y telnet");
	}
	
	private void testClientConsume(String client_name, String client_type) {
		
		Pattern pattern = Pattern.compile(KatelloSystem.REG_OS_VERSION);
		Matcher matcher = pattern.matcher(client_type);
		Assert.assertTrue(matcher.find(), "Check - Release version should exist in provided client " + client_type);
		String release = matcher.group();
		
		KatelloSystem sys = new KatelloSystem(null, client_type+" "+KatelloUtils.getUniqueID(), org_name, null);
		sys.runOn(client_name);
		exec_result = sys.rhsm_registerForce_release(release, true, true);
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Client " + client_type + " must register to server successfully");

		yum_clean(client_name);
		
		// Installing package
		exec_result = KatelloUtils.sshOnClient(client_name, "yum install -y telnet");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Return code - telnet package should be installed on client " + client_type);
		
		exec_result = KatelloUtils.sshOnClient(client_name, "rpm -q telnet");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Return code - telnet package should be installed on client " + client_type);
		Assert.assertTrue(getOutput(exec_result).trim().contains("telnet-"), "telnet package should be installed on client " + client_type);
		
		// Installing errata
		exec_result = sys.yum_errata_list("RHBA", client_type.matches(".*RHEL\\s+5.*"));
		String[] erratas = exec_result.getStdout().split("\\n");
		
		Assert.assertTrue(erratas != null && erratas.length != 0, "Available Errata list is empty for client " + client_type);
		
		exec_result = KatelloUtils.sshOnClient(client_name, "yum --advisory " + erratas[0] + " update -y");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Return code - errata " + erratas[0] + " should be installed on client " + client_type);
		Assert.assertTrue(exec_result.getStdout().contains("Complete"), "Return text - errata " + erratas[0] + " should be installed on client " + client_type);
		
		// Installing package group
		if (client_type.matches(".*RHEL\\s+5.*")) {
			exec_result = KatelloUtils.sshOnClient(client_name, "yum -y groupinstall \"DNS Name Server\"");
			Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Return code - package group \"DNS Name Server\" should be installed on client " + client_type);
			Assert.assertTrue(exec_result.getStdout().contains("Complete"), "Return text - package group \"DNS Name Server\" should be installed on client " + client_type);
		} else {
			exec_result = KatelloUtils.sshOnClient(client_name, "yum -y groupinstall \"SNMP Support\"");
			Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Return code - package group \"SNMP Support\" should be installed on client " + client_type);
			Assert.assertTrue(exec_result.getStdout().contains("Complete"), "Return text - package group \"SNMP Support\" should be installed on client " + client_type);
		}
	}

	
}
