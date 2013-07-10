package com.redhat.qe.katello.tests.e2e;

import java.io.File;
import java.util.logging.Logger;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloMisc;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.common.KatelloUtils;

import com.redhat.qe.tools.SSHCommandResult;

@Test(groups={"cfse-e2e","headpin-cli"}, singleThreaded = true)
public class StackedSubscriptions extends KatelloCliTestBase {
	
	protected static Logger log = Logger.getLogger(StackedSubscriptions.class.getName());
	
	private String org_name;
	private String env_name;
	private String system_name;
	SSHCommandResult exec_result;
	
	@BeforeClass(description="Init unique names", alwaysRun=true, enabled=true)
	public void setUp(){
		rhsm_clean(cli_worker.getClientHostname());
		
		String uid = KatelloUtils.getUniqueID();
		this.env_name = "Dev-"+uid;
		this.system_name = "system-"+uid;
		this.org_name = "org-manifest-"+uid;
		
		KatelloUtils.sshOnClient(cli_worker.getClientHostname(),"echo '{\"cpu.cpu_socket(s)\":\"8\"}' > /etc/rhsm/facts/sockets.facts");		
		
		KatelloUtils.scpOnClient(cli_worker.getClientHostname(), "data/stack-manifest.zip", "/tmp");
		
		KatelloOrg org = new KatelloOrg(this.cli_worker, this.org_name, null);
		org.cli_create();

		
		KatelloProvider prov = new KatelloProvider(this.cli_worker, KatelloProvider.PROVIDER_REDHAT, this.org_name, null, null);
		exec_result = prov.import_manifest("/tmp"+File.separator+"stack-manifest.zip", new Boolean(true));
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code (provider import_manifest)");
		Assert.assertTrue(getOutput(exec_result).contains(KatelloProvider.OUT_MANIFEST_IMPORTED),"Message - (provider import_manifest)");

		KatelloEnvironment env = new KatelloEnvironment(this.cli_worker, this.env_name, null, this.org_name, KatelloEnvironment.LIBRARY);
		env.cli_create();
	}
	
	
	@AfterClass(description="Cleanup the org - allow others to reuse the manifest", alwaysRun=true, enabled=true)
	public void tearDown(){
		log.finest("Remove the prepared: /etc/rhsm/facts/sockets.facts");
		KatelloUtils.sshOnClient(cli_worker.getClientHostname(), "rm -f /etc/rhsm/facts/sockets.facts");
		try {
			cleanSubscriptions();
		} finally {
			KatelloOrg org = new KatelloOrg(this.cli_worker, this.org_name, null);
			SSHCommandResult res = org.delete();
			Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (org delete)");
		}
	}
	
	@Test(description="Change system to have 8 sockets. Auto subscribe current system. Verify that it's compliance is green.", enabled=true)
	public void test_autosubscribeCompliant() {		
		cleanSubscriptions();
		
		KatelloSystem sys = new KatelloSystem(this.cli_worker, this.system_name, this.org_name, this.env_name);
		exec_result = sys.rhsm_registerForce();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = sys.rhsm_subscribe_auto();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = sys.report(null);
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", "").contains("green"), "Check - compliance is green");
	}

	@Test(description="Change system to have 8 sockets. Register current system but not auto suscibe. Verify that it's compliance is red.", enabled=true, dependsOnMethods={"test_autosubscribeCompliant"})
	public void test_notSubscribeNotCompliant() {
		cleanSubscriptions();
		
		KatelloSystem sys = new KatelloSystem(this.cli_worker, this.system_name, this.org_name, this.env_name);
		exec_result = sys.rhsm_registerForce(); 
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		exec_result = sys.report(null);
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", "").contains("red"), "Check - compliance is red");
	}

	@Test(description="Change system to have 8 sockets. Subscribe current system only to first 2 sockets. Verify that it's compliance is yellow.", enabled=true, dependsOnMethods={"test_notSubscribeNotCompliant"})
	public void test_subscribeNonCompliant() {
		cleanSubscriptions();
		
		KatelloSystem sys = new KatelloSystem(this.cli_worker, this.system_name, this.org_name, this.env_name);
		exec_result = sys.rhsm_registerForce(); 
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		String pool_id = new KatelloMisc().cli_getPoolBySubscription(cli_worker.getClientHostname(),
				"Red Hat Enterprise Linux Server, Self-support \\(1-2 sockets\\) \\(Up to 1 guest\\)", 1);
		exec_result = sys.rhsm_subscribe(pool_id, 1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = sys.report(null);
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", "").contains("yellow"), "Check - compliance is yellow");
	}

	@Test(description="Change system to have 8 sockets. Subscribe current system only to first 2 sockets. " +
			"Verify that it's compliance is yellow. Subscribe 2 socept by loop untill it is compliant.", enabled=true, dependsOnMethods={"test_subscribeNonCompliant"})
	public void test_loopsubscribeCompliant() {
		cleanSubscriptions();
		
		KatelloSystem sys = new KatelloSystem(this.cli_worker, this.system_name, this.org_name, this.env_name);
		exec_result = sys.rhsm_registerForce(); 
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		String pool_id = new KatelloMisc().cli_getPoolBySubscription(cli_worker.getClientHostname(),
				"Red Hat Enterprise Linux Server, Self-support \\(1-2 sockets\\) \\(Up to 1 guest\\)", 1);
		exec_result = sys.rhsm_subscribe(pool_id, 1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = sys.report(null);
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", "").contains("yellow"), "Check - compliance is yellow");
		
		String pool_id2 = new KatelloMisc().cli_getPoolBySubscription(cli_worker.getClientHostname(),
				"Red Hat Enterprise Linux Server, Self-support \\(1-2 sockets\\) \\(Up to 1 guest\\)", 3);
		
		exec_result = sys.rhsm_subscribe(pool_id2, 1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = sys.report(null);
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", "").contains("yellow"), "Check - compliance is yellow");
		
		exec_result = sys.rhsm_subscribe(pool_id2, 1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = sys.report(null);
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", "").contains("yellow"), "Check - compliance is yellow");
		
		exec_result = sys.rhsm_subscribe(pool_id2, 1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = sys.report(null);
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", "").contains("green"), "Check - compliance is green");
	}

	@Test(description="Change system to have 8 sockets. Subscribe current system only to first 2 sockets of one of pools. " +
			"Verify that it's compliance is yellow. Subscribe 2 socket of second pool. Verify that it's compliance is yellow.", enabled=true, dependsOnMethods={"test_loopsubscribeCompliant"})
	public void test_subscribeNonCompliantBothPools() {
		cleanSubscriptions();
		
		KatelloSystem sys = new KatelloSystem(this.cli_worker, this.system_name, this.org_name, this.env_name);
		exec_result = sys.rhsm_registerForce(); 
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		String pool_id = new KatelloMisc().cli_getPoolBySubscription(cli_worker.getClientHostname(),
				"Red Hat Enterprise Linux Server, Self-support \\(1-2 sockets\\) \\(Up to 1 guest\\)", 1);
		String pool_id2 = new KatelloMisc().cli_getPoolBySubscription(cli_worker.getClientHostname(),
				"Red Hat Enterprise Linux Server, Standard \\(1-2 sockets\\) \\(Up to 1 guest\\)", 3);
		
		exec_result = sys.rhsm_subscribe(pool_id, 1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = sys.report(null);
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", "").contains("yellow"), "Check - compliance is yellow");
		
		exec_result = sys.rhsm_subscribe(pool_id2, 1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = sys.report(null);
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", "").contains("yellow"), "Check - compliance is yellow");

	}

	@Test(description="Change system to have 8 sockets. Subscribe current system only to first 2 sockets of one of pools. Verify that it's compliance is yellow. " +
			"Subscribe 8 socket of another product with 2 pools. Verify that it's compliance is yellow.", enabled=true, dependsOnMethods={"test_subscribeNonCompliantBothPools"})
	public void test_subscribeNonCompliantFirstProduct() {
		cleanSubscriptions();
		
		KatelloSystem sys = new KatelloSystem(this.cli_worker, this.system_name, this.org_name, this.env_name);
		exec_result = sys.rhsm_registerForce(); 
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		String pool_id = new KatelloMisc().cli_getPoolBySubscription(cli_worker.getClientHostname(),
				"Red Hat Enterprise Linux Server, Self-support \\(1-2 sockets\\) \\(Up to 1 guest\\)", 1);
		String pool_id2 = new KatelloMisc().cli_getPoolBySubscription(cli_worker.getClientHostname(),
				"Red Hat Enterprise Linux Server, Standard \\(1-2 sockets\\) \\(Up to 4 guests\\)", 3);
		
		exec_result = sys.rhsm_subscribe(pool_id, 1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = sys.report(null);
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", "").contains("yellow"), "Check - compliance is yellow");
		
		exec_result = sys.rhsm_subscribe(pool_id2, 3);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		pool_id2 = new KatelloMisc().cli_getPoolBySubscription(cli_worker.getClientHostname(),
				"Red Hat Enterprise Linux Server, Standard \\(1-2 sockets\\) \\(Up to 1 guest\\)", 3);
		
		exec_result = sys.rhsm_subscribe(pool_id2, 1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = sys.report(null);
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", "").contains("yellow"), "Check - compliance is yellow");

	}
	
	@Test(description="Change system to have 8 sockets. Subscribe current system to 8 sockets by several attempts to have multiple subscriptions. " +
			"Remove one of them. Verify that it's compliance is yellow.", enabled=true, dependsOnMethods="test_subscribeNonCompliantFirstProduct")
	public void test_unsubscribeNonCompliant() {
		cleanSubscriptions();
		
		KatelloSystem sys = new KatelloSystem(this.cli_worker, this.system_name, this.org_name, this.env_name);
		exec_result = sys.rhsm_registerForce(); 
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		String pool_id = new KatelloMisc().cli_getPoolBySubscription(cli_worker.getClientHostname(),
				"Red Hat Enterprise Linux Server, Self-support \\(1-2 sockets\\) \\(Up to 1 guest\\)", 1);
		exec_result = sys.rhsm_subscribe(pool_id, 1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		String pool_id2 = new KatelloMisc().cli_getPoolBySubscription(cli_worker.getClientHostname(),
				"Red Hat Enterprise Linux Server, Self-support \\(1-2 sockets\\) \\(Up to 1 guest\\)", 3);
		
		exec_result = sys.rhsm_subscribe(pool_id2, 1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = sys.rhsm_subscribe(pool_id2, 1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = sys.rhsm_subscribe(pool_id2, 1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = sys.report(null);
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", "").contains("green"), "Check - compliance is green");
		
		exec_result = sys.subscriptions();
		String serialId = KatelloUtils.grepCLIOutput("Serial Id", exec_result.getStdout());
		if (serialId == null || serialId.isEmpty()) {
			serialId = KatelloUtils.grepCLIOutput("Serial ID", exec_result.getStdout());
		}
		
		exec_result = sys.rhsm_unsubscribe(serialId);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = sys.report(null);
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", "").contains("yellow"), "Check - compliance is yellow");
	}
	
	
	@Test(description="Change system to have 8 sockets. Subscribe current system to 8 sockets by several attempts to have multiple subscriptions for example 4. " +
			"Verify that there are 4 separate subscriptions for system.", enabled=true, dependsOnMethods={"test_unsubscribeNonCompliant"})
	public void test_differentSubscriptions() {
		cleanSubscriptions();
		
		KatelloSystem sys = new KatelloSystem(this.cli_worker, this.system_name, this.org_name, this.env_name);
		exec_result = sys.rhsm_registerForce(); 
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		String pool_id = new KatelloMisc().cli_getPoolBySubscription(cli_worker.getClientHostname(),
				"Red Hat Enterprise Linux Server, Self-support \\(1-2 sockets\\) \\(Up to 1 guest\\)", 1);
		exec_result = sys.rhsm_subscribe(pool_id, 1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		String pool_id2 = new KatelloMisc().cli_getPoolBySubscription(cli_worker.getClientHostname(),
				"Red Hat Enterprise Linux Server, Self-support \\(1-2 sockets\\) \\(Up to 1 guest\\)", 3);
		
		exec_result = sys.rhsm_subscribe(pool_id2, 1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = sys.rhsm_subscribe(pool_id2, 1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = sys.rhsm_subscribe(pool_id2, 1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = sys.report(null);
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", "").contains("green"), "Check - compliance is green");
		
		exec_result = sys.subscriptions_count();
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", "").trim().equals("4"), "Check - subscriptions are 4");
	}
	
	private void cleanSubscriptions() {
		KatelloSystem sys = new KatelloSystem(this.cli_worker, null, org_name, null);
		SSHCommandResult res = sys.system_uuids();
		String[] uuids = getOutput(res).split("\n");
		for(String uuid: uuids){
			if(!uuid.trim().isEmpty()){
				sys = new KatelloSystem(this.cli_worker, null, org_name, null, uuid, null, null, null, null, null);
				sys.unsubscribe();
				sys.unregister();
			}
		}
		rhsm_clean(cli_worker.getClientHostname());
	}
}
