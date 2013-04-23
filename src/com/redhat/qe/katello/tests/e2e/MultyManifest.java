package com.redhat.qe.katello.tests.e2e;

import java.io.File;
import java.util.logging.Logger;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SCPTools;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups={"cfse-e2e"})
public class MultyManifest extends KatelloCliTestScript{
	protected static Logger log = Logger.getLogger(MultyManifest.class.getName());

	String org;
	private String env_dev;
	private String sys_name;

	public static final String MANIFEST_MULTY = "manifest-multy.zip";

	@BeforeClass(description="Init unique names", alwaysRun=true)
	public void setUp(){
		String uid = KatelloUtils.getUniqueID();
		this.env_dev = "Dev-"+uid;
		this.org = "multy-manifest-"+uid;
		this.sys_name = "localhost-"+uid;
		KatelloOrg org = new KatelloOrg(this.org, null);
		org.cli_create();
	}
	
	@Test(description="Import multy manifest", enabled=true)
	public void test_importMultyManifest() {
		
		SCPTools scp = new SCPTools(
				System.getProperty("katello.client.hostname", "localhost"), 
				System.getProperty("katello.client.ssh.user", "root"), 
				System.getProperty("katello.client.sshkey.private", ".ssh/id_hudson_dsa"), 
				System.getProperty("katello.client.sshkey.passphrase", "null"));
		Assert.assertTrue(scp.sendFile("data"+File.separator+MANIFEST_MULTY, "/tmp"),
				MANIFEST_MULTY+" sent successfully");			
		
		KatelloProvider prov = new KatelloProvider(KatelloProvider.PROVIDER_REDHAT, this.org, null, null);
		SSHCommandResult res = prov.import_manifest("/tmp"+File.separator+MANIFEST_MULTY, new Boolean(true));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (provider import_manifest)");
		Assert.assertTrue(getOutput(res).contains(KatelloProvider.OUT_MANIFEST_IMPORTED),"Message - (provider import_manifest)");
	}
	
	@Test(description="Add system to dev environment", dependsOnMethods={"test_importMultyManifest"}, enabled=true)
	public void test_addSystemsToEnvs(){
		KatelloEnvironment env = new KatelloEnvironment(this.env_dev, null, this.org, KatelloEnvironment.LIBRARY);
		SSHCommandResult res = env.cli_create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		
		KatelloSystem sys = new KatelloSystem(sys_name, this.org, this.env_dev);
		res = sys.rhsm_registerForce(); 
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
	}
	
	@Test(description="list the subscriptions on environment", dependsOnMethods={"test_addSystemsToEnvs"}, enabled=true)
	public void test_subscriptionList() {
		SSHCommandResult res = KatelloUtils.sshOnClient("subscription-manager list --available --all | sed  -e 's/^ \\{1,\\}//'");
		Assert.assertTrue(getOutput(res).trim().contains("JBoss Enterprise Application Platform ELS Program, 64 Core Standard"), "Contains all pools from manifest");
		Assert.assertTrue(getOutput(res).trim().contains("High Performance Network (8 sockets) for IBM POWER"), "Contains all pools from manifest");
		Assert.assertTrue(getOutput(res).trim().contains("Red Hat Enterprise Linux Server for HPC Compute Node, Self-support (8 sockets) (Up to 1 guest)"), "Contains all pools from manifest");
		Assert.assertTrue(getOutput(res).trim().contains("CloudForms Employee Subscription"), "Contains all pools from manifest");
		Assert.assertTrue(getOutput(res).trim().contains("OpenShift Employee Subscription"), "Contains all pools from manifest");
	}
	
	@AfterClass(description="Cleanup the org - allow others to reuse the manifest", alwaysRun=true)
	public void tearDown(){
		KatelloOrg org = new KatelloOrg(this.org, null);
		SSHCommandResult res = org.delete();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (org delete)");
	}
}
