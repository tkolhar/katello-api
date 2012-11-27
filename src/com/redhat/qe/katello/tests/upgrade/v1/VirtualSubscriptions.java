package com.redhat.qe.katello.tests.upgrade.v1;

import java.io.File;
import java.util.logging.Logger;
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

@Test(groups={"sam-upgrade"}) // TODO - remove the extends: it's for debugging only.
public class VirtualSubscriptions extends KatelloCliTestScript implements KatelloConstants {
	
	protected static Logger log = Logger.getLogger(VirtualSubscriptions.class.getName());
	
	private String uid;
	private String orgName;
	private String envTesting, envDevelopment;
	private KatelloUser samAdmin;
	private String poolRhel, poolVirt;
	private String akRhel, akVirt;

	@Test(description="init object unique names", 
			groups={TNG_PRE_UPGRADE})
	public void init(){
		uid = KatelloUtils.getUniqueID();
		orgName = "SAM QE "+uid;
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
	}
	
//	@Test(description="verify orgs survived the upgrade", 
//			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
//			groups={TNG_POST_UPGRADE})
//	public void checkOrgsSurvived(){
//		KatelloOrg org = new KatelloOrg(_org, null);
//		SSHCommandResult res = org.cli_info();
//		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (org info)");
//	}
//
//	@Test(description="verify environments survived the upgrade", 
//			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
//			groups={TNG_POST_UPGRADE})
//	public void checkEnvironmentsSurvived(){
//		KatelloEnvironment env = new KatelloEnvironment(_env_1, null, _org, KatelloEnvironment.LIBRARY);
//		SSHCommandResult res = env.cli_info();
//		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (env info)");
//		
//		env = new KatelloEnvironment(_env_2, null, _org, KatelloEnvironment.LIBRARY);
//		res = env.cli_info();
//		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (env info)");
//		
//		env = new KatelloEnvironment(_env_3, null, _org, KatelloEnvironment.LIBRARY);
//		res = env.cli_info();
//		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (env info)");
//	}
//
//	
}
