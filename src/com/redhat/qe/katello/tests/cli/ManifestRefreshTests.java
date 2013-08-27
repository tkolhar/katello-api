package com.redhat.qe.katello.tests.cli;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

public class ManifestRefreshTests extends KatelloCliTestBase{
	private String org_name;
	private KatelloOrg org;
	private String distributor_name;
	private String manifest_name;
	private String subscription_id;
	private SSHCommandResult res;
	@BeforeClass(description="Prepare an org to work with", groups = {"headpin-cli","cfse-cli"})
	public void setup_org(){

		String uid = KatelloUtils.getUniqueID();
		this.org_name = "org"+uid;
		this.distributor_name="Dis-"+ uid;
		org = new KatelloOrg(this.cli_worker, this.org_name,null);
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		KatelloUtils.scpOnClient(cli_worker.getServerHostname(), "scripts/my_script.sh", "/tmp");
		KatelloUtils.sshOnServer("chmod a+x /tmp/my_script.sh");
		res = KatelloUtils.sshOnServer("sh /tmp/my_script.sh "+System.getProperty("katello.stage.login")+" "+System.getProperty("katello.stage.password")+" "+this.distributor_name);
		res = KatelloUtils.sshOnServer("cat /tmp/file.txt | grep 'id' | cut -d':' -f2 | awk \'NR==1\'");
		this.subscription_id = getOutput(res);
		KatelloUtils.scpOnClient(cli_worker.getServerHostname(), "scripts/create_manifest.sh", "/tmp");
		KatelloUtils.sshOnServer("chmod a+x /tmp/create_manifest.sh");
		res = KatelloUtils.sshOnServer("sh /tmp/create_manifest.sh "+System.getProperty("katello.stage.login")+" "+System.getProperty("katello.stage.password")+" "+this.distributor_name+" "+this.subscription_id);
		res = KatelloUtils.sshOnServer("cat /tmp/file_name");
		this.manifest_name = getOutput(res);
	}	

	@Test(description = "Refresh Manifests - increase no of subscriptions and refresh",groups={"cfse-cli","headpin-cli"})
	public void test_refreshManifestPortalIncrease(){

		KatelloProvider providerRH;
		providerRH  = new KatelloProvider(this.cli_worker, KatelloProvider.PROVIDER_REDHAT, this.org_name, null, null);

		// import manifest 
		SSHCommandResult exec_result = providerRH.import_manifest(this.manifest_name,true);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).trim().contains(KatelloProvider.OUT_MANIFEST_IMPORTED), "Check output");

		res = KatelloUtils.sshOnServer("sh /tmp/create_manifest.sh "+System.getProperty("katello.stage.login")+" "+System.getProperty("katello.stage.password")+" "+this.distributor_name+" "+this.subscription_id);

		// refresh manifest
		exec_result = providerRH.refresh_manifest();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).trim().contains(KatelloProvider.OUT_MANIFEST_REFRESH), "Check output");

		exec_result = org.delete();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");  

	}

	@AfterClass(description="Delete Distributor", alwaysRun=true)
	public void tearDown() {

		KatelloUtils.scpOnClient(cli_worker.getServerHostname(), "scripts/cleanup_script.sh", "/tmp");
		KatelloUtils.sshOnServer("chmod a+x /tmp/cleanup_script.sh");
		KatelloUtils.sshOnServer("sh /tmp/cleanup_script.sh "+System.getProperty("katello.stage.login")+" "+System.getProperty("katello.stage.password")+" "+distributor_name);
	}

}