package com.redhat.qe.katello.tests.disconnected;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloDisconnected;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

public class DisconnectedTests {
	public static final String DISCONNECTED_MAIN_MANIFEST = "disconnected.manifest.zip";
	public static final String DISCONNECTED_REPO_TO_SYNC = "rhel-6-server-sam-rpms-6_4-x86_64";
	public static final String DISCONNECTED_EXPORT_DIR = "/tmp/export";
	public static final String DISCONNECTED_HACKED_MANIFEST = "manifest-hacked.zip";
	
	private KatelloDisconnected disc;
	private SSHCommandResult res;
	
	@BeforeClass(description="setup stuff")
	public void setUp(){
		disc = new KatelloDisconnected();
		KatelloUtils.sshOnServer("rm -rf ~/.katello-disconnected/");
		KatelloUtils.sshOnServer("rpm -qf $(which rsync) || yum -y install rsync"); // this is really importnant step for the export command: it uses rsync
		KatelloUtils.sshOnServer(String.format("rm -rf %s; mkdir %s",
				DISCONNECTED_EXPORT_DIR,DISCONNECTED_EXPORT_DIR));
	}
	
	/**
	 * setup --oauth-secret <grep from /etc/pulp/server.conf>
	 */
	@Test(description="bb9dbe17-222d-4b4a-b779-ec2f968c4ac4")
	public void test_setupMinimal(){
		res = KatelloUtils.sshOnServer("egrep \"^oauth_secret: \" /etc/pulp/server.conf | cut -f2 -d' '");
		String oauth_secret = KatelloCliTestBase.sgetOutput(res);
		res = disc.setup(oauth_secret);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		
		KatelloUtils.sshOnServer("pulp-manage-db ; service httpd restart;");
	}
	
	/**
	 * import -m /tmp/disconnected.manifest.zip
	 */
	@Test(description="TODO", dependsOnMethods={"test_setupMinimal"})
	public void test_importManifest(){
		KatelloUtils.scpOnClient(System.getProperty("katello.server.hostname", "localhost"), 
				"data/"+DISCONNECTED_MAIN_MANIFEST, "/tmp");
		res = disc.importManifest("/tmp/"+DISCONNECTED_MAIN_MANIFEST);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		res = disc.custom_listCount(false); // by default all repos are enabled after import. 
		Assert.assertTrue(Integer.valueOf(KatelloCliTestBase.sgetOutput(res)).intValue()>70, "Check repos - default are all enabled");
	}
	
	// TODO: verify the error message. BZ 1009110
		@Test(description="import invalid/hacked manifest", dependsOnMethods={"test_importManifest"})
		public void test_importInvalidManifest() {
			KatelloUtils.scpOnClient(System.getProperty("katello.server.hostname", "localhost"), 
					"data/"+DISCONNECTED_HACKED_MANIFEST, "/tmp");
			res = disc.importManifest("/tmp/"+DISCONNECTED_HACKED_MANIFEST);
			Assert.assertTrue(res.getExitCode().intValue()==1, "Check - return code");
			Assert.assertTrue(KatelloCliTestBase.sgetOutput(res).isEmpty(), "Check - output string");
		}
		
		@Test(description="disable a particular repo", dependsOnMethods={"test_importManifest"})
		public void test_disableRepo() {
			res = disc.disable(DISCONNECTED_REPO_TO_SYNC, false);
			Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
			res = disc.custom_listRepo(false, DISCONNECTED_REPO_TO_SYNC);
			Assert.assertTrue(KatelloCliTestBase.sgetOutput(res).isEmpty(), "Check - output string");
			res = disc.custom_listRepo(true, DISCONNECTED_REPO_TO_SYNC);
			Assert.assertTrue(KatelloCliTestBase.sgetOutput(res).contains(DISCONNECTED_REPO_TO_SYNC), "Check - output string");
		}
		
		//TODO: BZ 1020476
		@Test(description="disable non-existing repo", dependsOnMethods={"test_disableRepo"})
		public void test_disableInvalidRepo() {
			//try to disable an already disabled repo
			res = disc.disable(DISCONNECTED_REPO_TO_SYNC, false);
			Assert.assertTrue(res.getExitCode().intValue() == 1, "Check - return code");
			//try to disable a non-existing repo
			String invalidRepo = "rhel-6-server-sam-rpms-6_4-x64_86";
			res = disc.disable(invalidRepo, false);
			Assert.assertTrue(res.getExitCode().intValue() == 1, "Check - return code");
		}
	/**
	 * disable --all
	 */
	@Test(description="TODO", dependsOnMethods={"test_disableRepo"})
	public void test_disableAll(){
		res = disc.custom_listCount(false);
		int beforeDisable = Integer.valueOf(KatelloCliTestBase.sgetOutput(res)).intValue();
		
		res = disc.disable(null, true);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		res = disc.configure();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		
		res = disc.custom_listCount(false);
		int afterDisable = Integer.valueOf(KatelloCliTestBase.sgetOutput(res)).intValue();
		Assert.assertTrue((beforeDisable-afterDisable>0 && afterDisable<=3), "Check - repos count after disable all");
	}
	
	@Test(description="TODO", dependsOnMethods={"test_disableAll"})
	public void test_enableRepo(){
		res = disc.enable(DISCONNECTED_REPO_TO_SYNC, false);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		res = disc.configure();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		res = disc.custom_listRepo(false, DISCONNECTED_REPO_TO_SYNC);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		Assert.assertTrue(KatelloCliTestBase.sgetOutput(res).contains(DISCONNECTED_REPO_TO_SYNC), "Check - repo is in enabled repo list");
	}
	
	//TODO: BZ 1020476
	@Test(description="enable invalid repo", dependsOnMethods={"test_enableRepo"})
	public void test_enableInvalidRepo() {
		//try to enable an already enabled repo
		res = disc.enable(DISCONNECTED_REPO_TO_SYNC, false);
		Assert.assertTrue(res.getExitCode().intValue() == 1, "Check - return code");
		//try to disable a non-existing repo
		String invalidRepo = "rhel-6-server-sam-rpms-6_4-x64_86";
		res = disc.enable(invalidRepo, false);
		Assert.assertTrue(res.getExitCode().intValue() == 1, "Check - return code");
	}
	
	@Test(description="enable all repos", dependsOnMethods={"test_enableRepo"})
	public void test_enableAll() {
		res = disc.enable(null, true);
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		res = disc.custom_listCount(false);
		Assert.assertTrue(Integer.valueOf(KatelloCliTestBase.sgetOutput(res)).intValue()>70, "Check repos - all enabled");
	}
	
	@Test(description="TODO", dependsOnMethods={"test_enableAll"})
	public void test_syncRepo(){
		res = disc.sync(DISCONNECTED_REPO_TO_SYNC);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
	}
	
	@Test(description="TODO", dependsOnMethods={"test_syncRepo"})
	public void test_watchSync(){
		res = disc.watch();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		Assert.assertTrue(KatelloCliTestBase.sgetOutput(res).contains(
				"repo: ["+DISCONNECTED_REPO_TO_SYNC+"] packages remaining: [0]"), "Check - output contains package remaining 0");
		Assert.assertTrue(KatelloCliTestBase.sgetOutput(res).contains(
				"Watching finished"), "Check - output contains finished");
	}
	
	@Test(description="TODO", dependsOnMethods={"test_watchSync"})
	public void test_exportToTmp(){ // watch command assures that sync is completed.
		res = disc.export(DISCONNECTED_REPO_TO_SYNC, "/tmp/export");
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		Assert.assertTrue(KatelloCliTestBase.sgetOutput(res).contains("Done exporting content"), "Check - output export done");
		res = KatelloUtils.sshOnServer("ls "+DISCONNECTED_EXPORT_DIR);
		Assert.assertTrue(KatelloCliTestBase.sgetOutput(res).contains("content-export-00"), "Check - output export files(1)");
		Assert.assertTrue(KatelloCliTestBase.sgetOutput(res).contains("expand_export.sh"), "Check - output export files(2)");
	}
}
