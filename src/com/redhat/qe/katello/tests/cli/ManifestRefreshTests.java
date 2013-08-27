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
	@BeforeClass(description="Create distributor & manifest", groups = {"headpin-cli","cfse-cli"})
	public void setup_org(){

		String uid = KatelloUtils.getUniqueID();
		this.org_name = "org"+uid;
		this.distributor_name="Tmp-"+ uid;
		org = new KatelloOrg(this.cli_worker, this.org_name,null);
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		KatelloUtils.scpOnClient(cli_worker.getServerHostname(), "scripts/stgportal-createDistributor.sh", "/tmp");
		res = KatelloUtils.sshOnServer("chmod a+x /tmp/stgportal-createDistributor.sh;" +
				"/tmp/stgportal-createDistributor.sh "+this.distributor_name);
		res = KatelloUtils.sshOnServer("cat /tmp/file.txt | grep 'id' | cut -d':' -f2 | awk \'NR==1\'");
		this.subscription_id = getOutput(res);
		KatelloUtils.scpOnClient(cli_worker.getServerHostname(), "scripts/stgportal-createManifest.sh", "/tmp");
		res = KatelloUtils.sshOnServer("chmod a+x /tmp/stgportal-createManifest.sh; " +
				"/tmp/stgportal-createManifest.sh "+this.distributor_name+" "+this.subscription_id);
		res = KatelloUtils.sshOnServer("cat /tmp/file_name");
		this.manifest_name = getOutput(res);
	}	

	@Test(description = "Refresh manifests - increase no of subscriptions and refresh",groups={"cfse-cli","headpin-cli"})
	public void test_refreshManifestPortalIncrease(){

		KatelloProvider providerRH;
		providerRH  = new KatelloProvider(this.cli_worker, KatelloProvider.PROVIDER_REDHAT, this.org_name, null, null);

		// import manifest 
		SSHCommandResult exec_result = providerRH.import_manifest(this.manifest_name,true);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).trim().contains(KatelloProvider.OUT_MANIFEST_IMPORTED), "Check output");

		res = KatelloUtils.sshOnServer("/tmp/stgportal-createManifest.sh "+this.distributor_name+" "+this.subscription_id);

		// refresh manifest
		exec_result = providerRH.refresh_manifest();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).trim().contains(KatelloProvider.OUT_MANIFEST_REFRESH), "Check output");

		exec_result = org.delete();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");  

	}

	@AfterClass(description="Delete distributor", alwaysRun=true)
	public void tearDown() {

		KatelloUtils.scpOnClient(cli_worker.getServerHostname(), "scripts/stgportal-deleteDistributor.sh", "/tmp");
		KatelloUtils.sshOnServer("chmod a+x /tmp/stgportal-deleteDistributor.sh; /tmp/stgportal-deleteDistributor.sh "+distributor_name);
	}

}