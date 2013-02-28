package com.redhat.qe.katello.tests.e2e;

import java.io.File;
import java.util.logging.Logger;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SCPTools;
import com.redhat.qe.tools.SSHCommandResult;


@Test(groups={"cfse-e2e"})
public class ImportManifest extends KatelloCliTestScript{
	protected static Logger log = Logger.getLogger(ImportManifest.class.getName());

	String org;

	public static final String MANIFEST_HACKED = "manifest-hacked.zip";
	public static final String EMPTY_HACKED = "manifest-empty.zip";

	@BeforeClass(description="Init unique names", alwaysRun=true)
	public void setUp(){
		String uid = KatelloUtils.getUniqueID();
		this.org = "wrong-manifest-"+uid;
		KatelloOrg org = new KatelloOrg(this.org, null);
		org.cli_create();
	}
	
	@Test(description="Import hacked manifest", enabled=true)
	public void test_importHackedManifest() {
		
		SCPTools scp = new SCPTools(
				System.getProperty("katello.client.hostname", "localhost"), 
				System.getProperty("katello.client.ssh.user", "root"), 
				System.getProperty("katello.client.sshkey.private", ".ssh/id_hudson_dsa"), 
				System.getProperty("katello.client.sshkey.passphrase", "null"));
		Assert.assertTrue(scp.sendFile("data"+File.separator+MANIFEST_HACKED, "/tmp"),
				MANIFEST_HACKED+" sent successfully");			
		
		KatelloProvider prov = new KatelloProvider(KatelloProvider.PROVIDER_REDHAT, this.org, null, null);
		SSHCommandResult res = prov.import_manifest("/tmp"+File.separator+MANIFEST_HACKED, new Boolean(true));
		Assert.assertEquals(res.getExitCode().intValue(), 144, "Check - error code (provider import_manifest)");
		Assert.assertTrue(getOutput(res).contains("unable to extract export archive"),"Message - (provider import_manifest)");
	}

	@Test(description="Import empty manifest", enabled=true)
	public void test_importEmptyManifest() {
		
		SCPTools scp = new SCPTools(
				System.getProperty("katello.client.hostname", "localhost"), 
				System.getProperty("katello.client.ssh.user", "root"), 
				System.getProperty("katello.client.sshkey.private", ".ssh/id_hudson_dsa"), 
				System.getProperty("katello.client.sshkey.passphrase", "null"));
		Assert.assertTrue(scp.sendFile("data"+File.separator+EMPTY_HACKED, "/tmp"),
				EMPTY_HACKED+" sent successfully");			
		
		KatelloProvider prov = new KatelloProvider(KatelloProvider.PROVIDER_REDHAT, this.org, null, null);
		SSHCommandResult res = prov.import_manifest("/tmp"+File.separator+EMPTY_HACKED, new Boolean(true));
		Assert.assertEquals(res.getExitCode().intValue(), 144, "Check - error code (provider import_manifest)");
		Assert.assertTrue(getOutput(res).contains("unable to extract export archive"),"Message - (provider import_manifest)");
	}
	
	@AfterClass(description="Cleanup the org - allow others to reuse the manifest", alwaysRun=true)
	public void tearDown(){
		KatelloOrg org = new KatelloOrg(this.org, null);
		SSHCommandResult res = org.delete();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (org delete)");
	}
}
