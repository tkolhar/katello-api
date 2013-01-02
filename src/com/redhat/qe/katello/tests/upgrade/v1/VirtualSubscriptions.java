package com.redhat.qe.katello.tests.upgrade.v1;

import java.io.File;
import java.util.logging.Logger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.obj.KatelloActivationKey;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloGpgKey;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.base.obj.KatelloUser;
import com.redhat.qe.katello.base.obj.KatelloUserRole;
import com.redhat.qe.katello.common.KatelloConstants;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SCPTools;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups={"sam-upgrade"})
public class VirtualSubscriptions implements KatelloConstants {
	
	protected static Logger log = Logger.getLogger(VirtualSubscriptions.class.getName());
	
	private String uid;
	private String orgName;
	private String envTesting, envDevelopment;
	private KatelloUser samAdmin;
	private String poolRhel, poolVirt;
	private String akRhel, akVirt;
	private String[] clients; // 2 at least
	private String uuid1;
	private String serial1;

	@BeforeClass(description="assure to have at least 2 clients there", 
			groups={TNG_PRE_UPGRADE},
			alwaysRun=true)
	public void checkClients(){
		String clientsStr = System.getProperty("katello.upgrade.clients", "");
		clients = clientsStr.split(",");
		if(clientsStr.isEmpty() || clients.length <2 ||  clients[0].isEmpty() || clients[1].isEmpty()) {
			Assert.fail("Please specify \"katello.upgrade.clients\" with at least 2 clients");
		}
		Assert.assertFalse(clients[1].equalsIgnoreCase(clients[0]), "2 clients are different");
		
		log.finest("put in socket.facts \"1\" - scenario here considers having all clients with 1 CPU socket");
		KatelloUtils.sshOnClient(clients[0],"echo '{\"cpu.cpu_socket(s)\":\"1\"}' > /etc/rhsm/facts/sockets.facts");
		KatelloUtils.sshOnClient(clients[1],"echo '{\"cpu.cpu_socket(s)\":\"1\"}' > /etc/rhsm/facts/sockets.facts");
	}
	
	@Test(description="init object unique names", 
			groups={TNG_PRE_UPGRADE})
	public void init(){
		uid = KatelloUtils.getUniqueID();
		orgName = "SAM-QE-"+uid;
		envTesting = "Testing";
		envDevelopment = "Development";
		akRhel = "ak_RHEL-"+uid;
		akVirt = "ak_Virt-"+uid;
	}
	
	@Test(description="create SAM admin user", 
			dependsOnMethods={"init"}, 
			groups={TNG_PRE_UPGRADE})
	public void createSamAdmin(){
		SSHCommandResult res;
		samAdmin = new KatelloUser("samAdmin-"+uid, 
				KatelloUser.DEFAULT_USER_EMAIL, KatelloUser.DEFAULT_ADMIN_PASS, false);
		res = samAdmin.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - user create (admin)");
		res = samAdmin.assign_role(KatelloUserRole.ROLE_ADMINISTRATOR);
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - user assign_role (admin)");
	}
	
	@Test(description="prepare test data: org, environments", 
			dependsOnMethods={"createSamAdmin"}, 
			groups={TNG_PRE_UPGRADE})
	public void createOrgEnvs(){
		SSHCommandResult res;
		KatelloUtils.sshOnClient(KatelloSystem.RHSM_CLEAN);
		KatelloUtils.sshOnClient("rpm -e "+KatelloGpgKey.GPG_PUBKEY_RPM_ZOO+" || true");
		
		KatelloOrg org = new KatelloOrg(orgName, null);
		org.runAs(samAdmin); 
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - org create");
		KatelloEnvironment env = new KatelloEnvironment(envTesting, null, orgName, KatelloEnvironment.LIBRARY);
		env.runAs(samAdmin);
		res = env.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - environment create (Testing)");
		env = new KatelloEnvironment(envDevelopment, null, orgName, envTesting);
		env.runAs(samAdmin);
		res = env.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - environment create (Development)");
	}
	
	@Test(description="import the manifest", 
			dependsOnMethods={"createOrgEnvs"}, 
			groups={TNG_PRE_UPGRADE})
	public void importManifest(){
		SSHCommandResult res;
		SCPTools scp = new SCPTools(
				System.getProperty("katello.client.hostname", "localhost"), 
				System.getProperty("katello.client.ssh.user", "root"), 
				System.getProperty("katello.client.sshkey.private", ".ssh/id_hudson_dsa"), 
				System.getProperty("katello.client.sshkey.passphrase", "null"));
		Assert.assertTrue(scp.sendFile("data"+File.separator+KatelloProvider.MANIFEST_12SUBSCRIPTIONS, "/tmp"),
				KatelloProvider.MANIFEST_12SUBSCRIPTIONS+" sent successfully");

		KatelloProvider rh = new KatelloProvider(KatelloProvider.PROVIDER_REDHAT, orgName, null, null);
		rh.runAs(samAdmin);
		res = rh.import_manifest("/tmp/"+KatelloProvider.MANIFEST_12SUBSCRIPTIONS, null);
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - provider import_manifest");
		KatelloOrg org = new KatelloOrg(orgName, null); org.runAs(samAdmin);
		res = org.subscriptions();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - org subscriptions");
		Assert.assertTrue(KatelloCliTestScript.sgetOutput(res).contains("(Up to 1 guest)"), "stdout - contains guest");
		// getting poolid could vary - might be need to make switch case here for different versions...
		poolRhel = KatelloCli.grepCLIOutput("Id", KatelloCliTestScript.sgetOutput(res));
		String consumed = KatelloCli.grepCLIOutput("Consumed", KatelloCliTestScript.sgetOutput(res));
		Assert.assertTrue(!poolRhel.isEmpty(), "stdout - poolid exists");
		Assert.assertTrue(Integer.parseInt(consumed)==0, "stdout - consumed 0");
	}
	
	@Test(description="subscribe via activation key", 
			dependsOnMethods={"importManifest"}, 
			groups={TNG_PRE_UPGRADE})
	public void prepareAKAndSubscribe(){
		SSHCommandResult res;
		KatelloActivationKey key = new KatelloActivationKey(
				orgName, envTesting, akRhel, null, null); key.runAs(samAdmin);
		res = key.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - activation_key create (Testing)");
		res = key.update_add_subscription(poolRhel);
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - activation_key add_subscription (RHEL)");
		KatelloSystem sys = new KatelloSystem(clients[0], orgName, null);
		sys.runOn(clients[0]); sys.rhsm_clean();// clean on 1st client
		
		sys = new KatelloSystem(clients[0]+"-"+uid, orgName, envTesting);
		sys.runAs(samAdmin);
		sys.runOn(clients[0]);
		res = sys.rhsm_registerForce(akRhel);
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - rhsm register (activationkey)");
	}
	
	@Test(description="verify: virtual pool appeared", 
			dependsOnMethods={"prepareAKAndSubscribe"},
			groups={TNG_PRE_UPGRADE})
	public void checkvirtPoolAppeared(){
		SSHCommandResult res;
		KatelloOrg org = new KatelloOrg(orgName, null); org.runAs(samAdmin);
		res = org.subscriptions();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - org subscriptions");
		Assert.assertTrue(KatelloCliTestScript.sgetOutput(res).contains("(Up to 1 guest)"), "stdout - contains guest");

		String block = KatelloCli.grepOutBlock("Id", poolRhel, KatelloCliTestScript.sgetOutput(res));
		String consumedCountRhel = KatelloCli.grepCLIOutput("Consumed", block);
		Assert.assertTrue(consumedCountRhel.equals("1"), "stdout - consumed just 1 socket");// as we manually echo-ed it - see @BeforeClass
		
		String pool1 = KatelloCli.grepCLIOutput("Id", KatelloCliTestScript.sgetOutput(res),1);
		String pool2 = KatelloCli.grepCLIOutput("Id", KatelloCliTestScript.sgetOutput(res),2);
		String pool3Null = KatelloCli.grepCLIOutput("Id", KatelloCliTestScript.sgetOutput(res),3);
		
		Assert.assertNotNull(pool1, "stdout - pool1 not null");
		Assert.assertNotNull(pool2, "stdout - pool2 not null");
		Assert.assertNull(pool3Null, "stdout - pool3 IS null"); // so we have just 2 pools for the org.

		// store the virtual poolID
		block = KatelloCli.grepOutBlock("Consumed", "0", KatelloCliTestScript.sgetOutput(res));
		poolVirt = KatelloCli.grepCLIOutput("Id", block);
		Assert.assertNotNull(pool1, "stdout - virtual pool not null");
	}
	
	@Test(description="try to assign client[1] to that virt. pool. Should fail.", 
			dependsOnMethods={"checkvirtPoolAppeared"},
			groups={TNG_PRE_UPGRADE})
	public void subscribeToVirt(){
		SSHCommandResult res;
		KatelloActivationKey key = new KatelloActivationKey(
				orgName, envDevelopment, akVirt, null, null); key.runAs(samAdmin);
		res = key.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - activation_key create (Development)");
		res = key.update_add_subscription(poolVirt);
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - activation_key add_subscription (Virtual Guest)");
		KatelloSystem sys = new KatelloSystem(clients[1], orgName, null);
		sys.runOn(clients[1]); sys.rhsm_clean();// clean on 1st client
		
		sys = new KatelloSystem("new-"+clients[1]+"-"+uid, orgName, envDevelopment);
		sys.runAs(samAdmin);
		sys.runOn(clients[1]);
		res = sys.rhsm_registerForce(akVirt);
		Assert.assertTrue(res.getExitCode().intValue()==255, "exit(1) - rhsm register failure (activationkey virtual)");
		Assert.assertTrue(KatelloCliTestScript.sgetOutput(res).contains(String.format(KatelloSystem.ERR_GUEST_HAS_DIFFERENT_HOST, poolVirt)), 
				"stderr - guest has different host not matching pool owner");
	}
	
	
	@Test(description="unregister client[0]. Virtual pool should gone! Check AK too.", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE},
			groups={TNG_POST_UPGRADE})
	public void unregisterRhelClient(){
		SSHCommandResult res;
		KatelloSystem sys = new KatelloSystem(clients[0], orgName, envTesting);
		sys.runAs(samAdmin); sys.runOn(clients[0]);
		res = sys.rhsm_unregister();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - rhsm unregister");
		
		// org subscriptions
		KatelloOrg org = new KatelloOrg(orgName, null); org.runAs(samAdmin);
		res = org.subscriptions();
		String block = KatelloCli.grepOutBlock("Id", poolRhel, KatelloCliTestScript.sgetOutput(res));
		Assert.assertNotNull(block, "stdout - RHEL subscription returned");
		block = KatelloCli.grepOutBlock("Id", poolVirt, KatelloCliTestScript.sgetOutput(res));
		Assert.assertNull(block, "stdout - Virtual subscription has gone");
		block = KatelloCli.grepOutBlock("Consumed", "0", KatelloCliTestScript.sgetOutput(res));
		Assert.assertNotNull(block, "stdout - RHEL consumers == 0");
		
		// activation key (virtual)
		KatelloActivationKey key = new KatelloActivationKey(orgName, envDevelopment, akVirt, null, null);
		key.runAs(samAdmin);
		res = key.info();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - activation_key info");
		Assert.assertFalse(KatelloCliTestScript.sgetOutput(res).contains(poolVirt),  
				"stdout - activation_key info does not contain virt poolId");
	}
	
	@Test(description="Register back client[0]", 
			dependsOnMethods={"unregisterRhelClient"},
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE},
			groups={TNG_POST_UPGRADE})
	public void subscribeBackRhelClient(){
		SSHCommandResult res;
		KatelloSystem sys = new KatelloSystem(clients[0]+"-"+uid, orgName, envTesting);
		sys.runAs(samAdmin);
		sys.runOn(clients[0]);
		res = sys.rhsm_registerForce(akRhel);
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - rhsm register (activationkey)");
		res = sys.rhsm_identity();
		uuid1 = KatelloCli.grepCLIOutput("Current identity is", KatelloCliTestScript.sgetOutput(res));
		Assert.assertNotNull(uuid1, "stdout - uuid1 is not null");
	}
	
	@Test(description="post-upgrade check subscriptions preserved.", 
			dependsOnMethods={"subscribeBackRhelClient"},
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE},
			groups={TNG_POST_UPGRADE})
	public void checkSubscriptionsPreserved(){
		SSHCommandResult res;
		KatelloOrg org = new KatelloOrg(orgName, null); org.runAs(samAdmin);
		res = org.subscriptions();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - org subscriptions");
		Assert.assertTrue(KatelloCliTestScript.sgetOutput(res).contains("(Up to 1 guest)"), "stdout - contains guest");

		String block = KatelloCli.grepOutBlock("Id", poolRhel, KatelloCliTestScript.sgetOutput(res));
		String consumedCountRhel = KatelloCli.grepCLIOutput("Consumed", block);
		Assert.assertTrue(consumedCountRhel.equals("1"), "stdout - consumed just 1 socket");
		
		String pool1 = KatelloCli.grepCLIOutput("Id", KatelloCliTestScript.sgetOutput(res),1);
		String pool2 = KatelloCli.grepCLIOutput("Id", KatelloCliTestScript.sgetOutput(res),2);
		String pool3Null = KatelloCli.grepCLIOutput("Id", KatelloCliTestScript.sgetOutput(res),3);
		
		Assert.assertNotNull(pool1, "stdout - pool1 not null");
		Assert.assertNotNull(pool2, "stdout - pool2 not null");
		Assert.assertNull(pool3Null, "stdout - pool3 IS null"); // so we have just 2 pools for the org.
	}
	
	@Test(description="post-upgrade check system remained subscribed.", 
			dependsOnMethods={"checkSubscriptionsPreserved"},
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE},
			groups={TNG_POST_UPGRADE})
	public void checkSubscribedRhelClient(){
		SSHCommandResult res;
		
		KatelloSystem sys = new KatelloSystem(clients[0]+"-"+uid, orgName, envTesting);
		sys.runAs(samAdmin);// sys.runOn(clients[0]);
		res = sys.subscriptions();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - system subscriptions");
		String retPoolId = KatelloCli.grepCLIOutput("Subscription ID", KatelloCliTestScript.sgetOutput(res));
		serial1 = KatelloCli.grepCLIOutput("Serial ID", KatelloCliTestScript.sgetOutput(res));
		Assert.assertNotNull(poolRhel, "stdout - RHEL pool not null (through `org subscriptions`)");
		Assert.assertNotNull(retPoolId, "stdout - RHEL pool not null (through `system subscriptions`)");
		String retConsumed = KatelloCli.grepCLIOutput("Consumed", KatelloCliTestScript.sgetOutput(res));
		Assert.assertTrue(retConsumed.equals("1"), "stdout - consumed 1");
		String retQuantity = KatelloCli.grepCLIOutput("Quantity", KatelloCliTestScript.sgetOutput(res));
		Assert.assertTrue(retQuantity.equals("1"), "stdout - quantity 1");
		
		sys.runOn(clients[0]);
		res = sys.rhsm_listConsumed();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - rhsm list (consumed)");
		String retSerial = KatelloCli.grepCLIOutput("Serial Number", KatelloCliTestScript.sgetOutput(res));
		Assert.assertNotNull(retConsumed,"stdout - consumed serial not null");
		Assert.assertTrue(retSerial.equals(serial1), "stdout - serial numbers are equals (rhsm vs cli)");
	}
	
	@Test(description="post-upgrade check system remained subscribed.", 
			dependsOnMethods={"checkSubscribedRhelClient"},
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE},
			groups={TNG_POST_UPGRADE})
	public void subscribeClient2(){
		SSHCommandResult res;
		KatelloSystem sys = new KatelloSystem(clients[1]+"-"+uid, orgName, envDevelopment);
		sys.runAs(samAdmin);
		sys.runOn(clients[1]);
		res = sys.rhsm_registerForce();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - rhsm register");
		res = sys.rhsm_subscribe(poolRhel);
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - rhsm subscribe");
		res = sys.rhsm_identity();
		String uuid2 = KatelloCli.grepCLIOutput("Current identity is", KatelloCliTestScript.sgetOutput(res));
		Assert.assertNotNull(uuid2, "stdout - uuid is not null");
	}
	
	@AfterClass(description="adjust sockets back", alwaysRun=true)
	public void adjustBackSockets(){
		log.finest("Remove the prepared: /etc/rhsm/facts/sockets.facts");
		KatelloUtils.sshOnClient(clients[0],"rm -f /etc/rhsm/facts/sockets.facts");
		KatelloUtils.sshOnClient(clients[1],"rm -f /etc/rhsm/facts/sockets.facts");
	}
}
