package com.redhat.qe.katello.tests.e2e;

import java.io.File;
import java.util.logging.Logger;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloMisc;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.tasks.KatelloTasks;
import com.redhat.qe.tools.SCPTools;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups={"cfse-e2e"})
public class StackedSubscriptions extends KatelloCliTestScript {
	
	protected static Logger log = Logger.getLogger(StackedSubscriptions.class.getName());
	
	private String org_name = "Org-to-import-stack-manifest";
	private String env_name = "Env-for-stack-manifest";
	private String system_name = "system-subscribe-localhost";
	SSHCommandResult exec_result;

	@BeforeClass(description="Init unique names", alwaysRun=true)
	public void setUp(){

		KatelloOrg org = new KatelloOrg(this.org_name, null);
		if (org.cli_info().getExitCode().intValue() != 0) {
			log.info("Seems there is no org with imported stage manifest. Doing it now.");
			SCPTools scp = new SCPTools(
					System.getProperty("katello.client.hostname", "localhost"), 
					System.getProperty("katello.client.ssh.user", "root"), 
					System.getProperty("katello.client.sshkey.private", ".ssh/id_hudson_dsa"), 
					System.getProperty("katello.client.sshkey.passphrase", "null"));
			Assert.assertTrue(scp.sendFile("data"+File.separator+"stack-manifest.zip", "/tmp"),
					"stack-manifest.zip sent successfully");			

			
			org.cli_create();
			KatelloProvider prov = new KatelloProvider(KatelloProvider.PROVIDER_REDHAT, this.org_name, null, null);
			exec_result = prov.import_manifest("/tmp"+File.separator+"stack-manifest.zip", new Boolean(true));
			Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code (provider import_manifest)");
			Assert.assertTrue(getOutput(exec_result).contains("Manifest imported"),"Message - (provider import_manifest)");
			KatelloEnvironment env = new KatelloEnvironment(env_name, null, org_name, KatelloEnvironment.LIBRARY);
			exec_result = env.cli_create();
			Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		} else {
			log.info("There is an org having manifest. Using: ["+this.org_name+"]");
		}		
		
	}
	
	@Test(description="Change system to have 8 sockets. Auto subscribe current system. Verify that it's compliance is green.", enabled=true)
	public void test_autosubscribeCompliant() {
		KatelloUtils.sshOnClient("echo '{\"cpu.cpu_socket(s)\":\"8\"}' > /etc/rhsm/facts/sockets.facts");
		
		rhsm_clean();
		
		KatelloSystem sys = new KatelloSystem(this.system_name, this.org_name, this.env_name);
		exec_result = sys.rhsm_registerForce(); 
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = sys.rhsm_subscribe_auto();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = sys.report();
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", "").contains("green"), "Check - compliance is green");
	}

	@Test(description="Change system to have 8 sockets. Register current system but not auto suscibe. Verify that it's compliance is red.", enabled=true)
	public void test_notSubscribeNotCompliant() {
		KatelloUtils.sshOnClient("echo '{\"cpu.cpu_socket(s)\":\"8\"}' > /etc/rhsm/facts/sockets.facts");
		
		rhsm_clean();
		
		KatelloSystem sys = new KatelloSystem(this.system_name, this.org_name, this.env_name);
		exec_result = sys.rhsm_registerForce(); 
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		exec_result = sys.report();
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", "").contains("red"), "Check - compliance is red");
	}

	@Test(description="Change system to have 8 sockets. Subscribe current system only to first 2 sockets. Verify that it's compliance is yellow.", enabled=true)
	public void test_subscribeNonCompliant() {
		KatelloUtils.sshOnClient("echo '{\"cpu.cpu_socket(s)\":\"8\"}' > /etc/rhsm/facts/sockets.facts");
		
		rhsm_clean();
		
		KatelloSystem sys = new KatelloSystem(this.system_name, this.org_name, this.env_name);
		exec_result = sys.rhsm_registerForce(); 
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		String pool_id = new KatelloMisc().cli_getPoolBySubscription("Red Hat Enterprise Linux Server, Self-support \\(1-2 sockets\\) \\(Up to 1 guest\\)", 1);
		exec_result = sys.rhsm_subscribe(pool_id, 1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = sys.report();
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", "").contains("yellow"), "Check - compliance is yellow");
	}

	@Test(description="Change system to have 8 sockets. Subscribe current system only to first 2 sockets. Verify that it's compliance is yellow. Subscribe 2 socept by loop untill it is compliant.", enabled=true)
	public void test_loopsubscribeCompliant() {
		KatelloUtils.sshOnClient("echo '{\"cpu.cpu_socket(s)\":\"8\"}' > /etc/rhsm/facts/sockets.facts");
		
		rhsm_clean();
		
		KatelloSystem sys = new KatelloSystem(this.system_name, this.org_name, this.env_name);
		exec_result = sys.rhsm_registerForce(); 
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		String pool_id = new KatelloMisc().cli_getPoolBySubscription("Red Hat Enterprise Linux Server, Self-support \\(1-2 sockets\\) \\(Up to 1 guest\\)", 1);
		exec_result = sys.rhsm_subscribe(pool_id, 1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = sys.report();
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", "").contains("yellow"), "Check - compliance is yellow");
		
		String pool_id2 = new KatelloMisc().cli_getPoolBySubscription("Red Hat Enterprise Linux Server, Self-support \\(1-2 sockets\\) \\(Up to 1 guest\\)", 3);
		
		exec_result = sys.rhsm_subscribe(pool_id2, 1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = sys.report();
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", "").contains("yellow"), "Check - compliance is yellow");
		
		exec_result = sys.rhsm_subscribe(pool_id2, 1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = sys.report();
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", "").contains("yellow"), "Check - compliance is yellow");
		
		exec_result = sys.rhsm_subscribe(pool_id2, 1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = sys.report();
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", "").contains("green"), "Check - compliance is green");
	}

	@Test(description="Change system to have 8 sockets. Subscribe current system only to first 2 sockets of one of pools. Verify that it's compliance is yellow. Subscribe 2 socket of second pool. Verify that it's compliance is yellow.", enabled=true)
	public void test_subscribeNonCompliantBothPools() {
		KatelloUtils.sshOnClient("echo '{\"cpu.cpu_socket(s)\":\"8\"}' > /etc/rhsm/facts/sockets.facts");
		
		rhsm_clean();
		
		KatelloSystem sys = new KatelloSystem(this.system_name, this.org_name, this.env_name);
		exec_result = sys.rhsm_registerForce(); 
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		String pool_id = new KatelloMisc().cli_getPoolBySubscription("Red Hat Enterprise Linux Server, Self-support \\(1-2 sockets\\) \\(Up to 1 guest\\)", 1);
		String pool_id2 = new KatelloMisc().cli_getPoolBySubscription("Red Hat Enterprise Linux Server, Standard \\(1-2 sockets\\) \\(Up to 1 guest\\)", 3);
		
		exec_result = sys.rhsm_subscribe(pool_id, 1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = sys.report();
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", "").contains("yellow"), "Check - compliance is yellow");
		
		exec_result = sys.rhsm_subscribe(pool_id2, 1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = sys.report();
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", "").contains("yellow"), "Check - compliance is yellow");

	}

	@Test(description="Change system to have 8 sockets. Subscribe current system only to first 2 sockets of one of pools. Verify that it's compliance is yellow. Subscribe 8 socket of another product with 2 pools. Verify that it's compliance is yellow.", enabled=true)
	public void test_subscribeNonCompliantFirstProduct() {
		KatelloUtils.sshOnClient("echo '{\"cpu.cpu_socket(s)\":\"8\"}' > /etc/rhsm/facts/sockets.facts");
		
		rhsm_clean();
		
		KatelloSystem sys = new KatelloSystem(this.system_name, this.org_name, this.env_name);
		exec_result = sys.rhsm_registerForce(); 
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		String pool_id = new KatelloMisc().cli_getPoolBySubscription("Red Hat Enterprise Linux Server, Self-support \\(1-2 sockets\\) \\(Up to 1 guest\\)", 1);
		String pool_id2 = new KatelloMisc().cli_getPoolBySubscription("Red Hat Enterprise Linux Server, Standard \\(1-2 sockets\\) \\(Up to 4 guests\\)", 3);
		
		exec_result = sys.rhsm_subscribe(pool_id, 1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = sys.report();
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", "").contains("yellow"), "Check - compliance is yellow");
		
		exec_result = sys.rhsm_subscribe(pool_id2, 3);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		pool_id2 = new KatelloMisc().cli_getPoolBySubscription("Red Hat Enterprise Linux Server, Standard \\(1-2 sockets\\) \\(Up to 1 guest\\)", 3);
		
		exec_result = sys.rhsm_subscribe(pool_id2, 1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = sys.report();
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", "").contains("yellow"), "Check - compliance is yellow");

	}
	
	@Test(description="Change system to have 8 sockets. Subscribe current system to 8 sockets by several attempts to have multiple subscriptions. Remove one of them. Verify that it's compliance is yellow.", enabled=true)
	public void test_unsubscribeNonCompliant() {
		KatelloUtils.sshOnClient("echo '{\"cpu.cpu_socket(s)\":\"8\"}' > /etc/rhsm/facts/sockets.facts");
		
		rhsm_clean();
		
		KatelloSystem sys = new KatelloSystem(this.system_name, this.org_name, this.env_name);
		exec_result = sys.rhsm_registerForce(); 
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		String pool_id = new KatelloMisc().cli_getPoolBySubscription("Red Hat Enterprise Linux Server, Self-support \\(1-2 sockets\\) \\(Up to 1 guest\\)", 1);
		exec_result = sys.rhsm_subscribe(pool_id, 1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		String pool_id2 = new KatelloMisc().cli_getPoolBySubscription("Red Hat Enterprise Linux Server, Self-support \\(1-2 sockets\\) \\(Up to 1 guest\\)", 3);
		
		exec_result = sys.rhsm_subscribe(pool_id2, 1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = sys.rhsm_subscribe(pool_id2, 1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = sys.rhsm_subscribe(pool_id2, 1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = sys.report();
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", "").contains("green"), "Check - compliance is green");
		
		exec_result = sys.subscriptions();
		String serialId = KatelloTasks.grepCLIOutput("Serial Id", exec_result.getStdout());
		
		exec_result = sys.rhsm_unsubscribe(serialId);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = sys.report();
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", "").contains("yellow"), "Check - compliance is yellow");
	}
	
	
	@Test(description="Change system to have 8 sockets. Subscribe current system to 8 sockets by several attempts to have multiple subscriptions for example 4. Verify that there are 4 separate subscriptions for system.", enabled=true)
	public void test_differentSubscriptions() {
		KatelloUtils.sshOnClient("echo '{\"cpu.cpu_socket(s)\":\"8\"}' > /etc/rhsm/facts/sockets.facts");
		
		rhsm_clean();
		
		KatelloSystem sys = new KatelloSystem(this.system_name, this.org_name, this.env_name);
		exec_result = sys.rhsm_registerForce(); 
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		String pool_id = new KatelloMisc().cli_getPoolBySubscription("Red Hat Enterprise Linux Server, Self-support \\(1-2 sockets\\) \\(Up to 1 guest\\)", 1);
		exec_result = sys.rhsm_subscribe(pool_id, 1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		String pool_id2 = new KatelloMisc().cli_getPoolBySubscription("Red Hat Enterprise Linux Server, Self-support \\(1-2 sockets\\) \\(Up to 1 guest\\)", 3);
		
		exec_result = sys.rhsm_subscribe(pool_id2, 1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = sys.rhsm_subscribe(pool_id2, 1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = sys.rhsm_subscribe(pool_id2, 1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = sys.report();
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", "").contains("green"), "Check - compliance is green");
		
		exec_result = sys.subscriptions_count();
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", "").trim().equals("4"), "Check - subscriptions are 4");

	}
}
