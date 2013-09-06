package com.redhat.qe.katello.tests.cli;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;


import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloSystem;
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

		KatelloUtils.scpOnClient(cli_worker.getServerHostname(), "scripts/pull-Pythonscript.sh", "/tmp");
		res = KatelloUtils.sshOnServer("chmod a+x /tmp/pull-Pythonscript.sh;" +
				"/tmp/pull-Pythonscript.sh ");
		KatelloUtils.scpOnClient(cli_worker.getServerHostname(), "scripts/stgportal-createDistributor.sh", "/tmp");
		KatelloUtils.scpOnClient(cli_worker.getServerHostname(), "scripts/stgportal-createManifest.sh", "/tmp");
		KatelloUtils.scpOnClient(cli_worker.getServerHostname(), "scripts/stgportal-decreaseSubscription.sh", "/tmp");
		KatelloUtils.scpOnClient(cli_worker.getServerHostname(), "scripts/stgportal-deleteDistributor.sh", "/tmp");
		KatelloUtils.scpOnClient(cli_worker.getServerHostname(), "scripts/cleanup-tempFiles.sh", "/tmp");

	}			

	private String obtain_distributorManifest(String dis_name)
	{
		String manifest_nm;
		String subs_id;
		res = KatelloUtils.sshOnServer("chmod a+x /tmp/stgportal-createDistributor.sh;" +
				"/tmp/stgportal-createDistributor.sh "+dis_name);
		res = KatelloUtils.sshOnServer("cat /tmp/file.txt | grep 'id' | cut -d':' -f2 | cut -d',' -f1 |awk \'NR==1\'");
		subs_id = getOutput(res);
		this.subscription_id=getOutput(res);
		res = KatelloUtils.sshOnServer("chmod a+x /tmp/stgportal-createManifest.sh; " +
				"sh /tmp/stgportal-createManifest.sh "+dis_name+" "+subs_id);
		res = KatelloUtils.sshOnServer("cat /tmp/file_name");
		manifest_nm = getOutput(res);
		return manifest_nm;
	}

	@Test(description = "Refresh manifests - increase no of subscriptions and refresh ID:262242",groups={"cfse-cli","headpin-cli"})
	public void test_refreshManifestPortalIncrease(){

		String uid = KatelloUtils.getUniqueID();
		this.org_name = "org"+uid;
		this.distributor_name="Tmp-"+ uid;
		String sys = "sys-name_"+uid;
		org = new KatelloOrg(this.cli_worker, this.org_name,null);
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");

		this.manifest_name=obtain_distributorManifest(this.distributor_name);

		KatelloProvider providerRH;
		providerRH  = new KatelloProvider(this.cli_worker, KatelloProvider.PROVIDER_REDHAT, this.org_name, null, null);

		// import manifest 
		try{Thread.sleep(5000);}catch(InterruptedException iex){}
		SSHCommandResult exec_result = providerRH.import_manifest(this.manifest_name,true);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).trim().contains(KatelloProvider.OUT_MANIFEST_IMPORTED), "Check output");
		try{Thread.sleep(5000);}catch(InterruptedException iex){}

		res = KatelloUtils.sshOnServer("/tmp/stgportal-createManifest.sh "+this.distributor_name+" "+this.subscription_id);

		// refresh manifest
		try{Thread.sleep(5000);}catch(InterruptedException iex){}
		exec_result = providerRH.refresh_manifest();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).trim().contains(KatelloProvider.OUT_MANIFEST_REFRESH), "Check output");
		try{Thread.sleep(5000);}catch(InterruptedException iex){}

		//Register and subscribe the system
		KatelloSystem sys_reg = new KatelloSystem(this.cli_worker,sys,this.org_name,null);
		exec_result = sys_reg.rhsm_registerForce();
		Assert.assertTrue(exec_result.getExitCode() == 0,"Check - return code");

		exec_result = sys_reg.subscriptions_available();
		String poolId1 = KatelloUtils.grepCLIOutput("ID", getOutput(exec_result).trim(),1);
		Assert.assertNotNull(poolId1, "Check - pool Id is not null");

		exec_result = sys_reg.subscribe(poolId1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertEquals(exec_result.getStdout().trim(), 
				String.format(KatelloSystem.OUT_SUBSCRIBE, sys),
				"Check - subscribe system output.");

		exec_result = org.delete();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");  

		KatelloUtils.sshOnServer("chmod a+x /tmp/stgportal-deleteDistributor.sh; /tmp/stgportal-deleteDistributor.sh "+this.distributor_name);
	}

	@Test(description = "Refresh manifests - decrease no of subscriptions and refresh ID:267992",groups={"cfse-cli","headpin-cli"})
	public void test_refreshManifestPortalDecrease(){

		String uid = KatelloUtils.getUniqueID();
		this.org_name = "org"+uid;
		this.distributor_name="Tmp-"+ uid;
		String sys = "sys-name_"+uid;
		org = new KatelloOrg(this.cli_worker, this.org_name,null);
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");

		this.manifest_name=obtain_distributorManifest(this.distributor_name);
		try{Thread.sleep(5000);}catch(InterruptedException iex){}

		KatelloProvider providerRH;
		providerRH  = new KatelloProvider(this.cli_worker, KatelloProvider.PROVIDER_REDHAT, this.org_name, null, null);

		// import manifest 
		SSHCommandResult exec_result = providerRH.import_manifest(this.manifest_name,true);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).trim().contains(KatelloProvider.OUT_MANIFEST_IMPORTED), "Check output");

		exec_result = org.subscriptions();
		Assert.assertTrue(exec_result.getExitCode() == 0,"Check - return code");

		//Register and subscribe the system
		KatelloSystem sys_reg = new KatelloSystem(this.cli_worker,sys,this.org_name,null);
		exec_result = sys_reg.rhsm_registerForce();
		Assert.assertTrue(exec_result.getExitCode() == 0,"Check - return code");

		exec_result = sys_reg.subscriptions_available();
		String poolId1 = KatelloUtils.grepCLIOutput("ID", getOutput(exec_result).trim(),1);
		Assert.assertNotNull(poolId1, "Check - pool Id is not null");

		exec_result = sys_reg.subscribe(poolId1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertEquals(exec_result.getStdout().trim(), 
				String.format(KatelloSystem.OUT_SUBSCRIBE, sys),
				"Check - subscribe system output.");

		res = KatelloUtils.sshOnServer("chmod a+x /tmp/stgportal-decreaseSubscription.sh;"+"/tmp/stgportal-decreaseSubscription.sh "+this.distributor_name+" "+this.subscription_id);

		// refresh manifest
		try{Thread.sleep(5000);}catch(InterruptedException iex){}
		exec_result = providerRH.refresh_manifest();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).trim().contains(KatelloProvider.OUT_MANIFEST_REFRESH), "Check output");
		try{Thread.sleep(5000);}catch(InterruptedException iex){}

		// System looses the subscriptions assigned
		exec_result = sys_reg.subscriptions_available();
		poolId1 = KatelloUtils.grepCLIOutput("ID", getOutput(exec_result).trim(),1);
		Assert.assertNull(poolId1, "Pool ID is null.");

		exec_result = sys_reg.info();
		Assert.assertTrue(exec_result.getExitCode() == 0,"Check - return code");

		// Re-attach subscriptions
		res = KatelloUtils.sshOnServer("/tmp/stgportal-createManifest.sh "+this.distributor_name+" "+this.subscription_id);

		// refresh manifest
		try{Thread.sleep(5000);}catch(InterruptedException iex){}
		exec_result = providerRH.refresh_manifest();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).trim().contains(KatelloProvider.OUT_MANIFEST_REFRESH), "Check output");
		try{Thread.sleep(5000);}catch(InterruptedException iex){}

		//check available subscription and subscribe the system
		exec_result = sys_reg.subscriptions_available();
		poolId1 = KatelloUtils.grepCLIOutput("ID", getOutput(exec_result).trim(),1);
		Assert.assertNotNull(poolId1, "Check - pool Id is not null");

		exec_result = sys_reg.subscribe(poolId1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertEquals(exec_result.getStdout().trim(), 
				String.format(KatelloSystem.OUT_SUBSCRIBE, sys),
				"Check - subscribe system output.");

		exec_result = org.delete();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");  

		KatelloUtils.sshOnServer("chmod a+x /tmp/stgportal-deleteDistributor.sh; /tmp/stgportal-deleteDistributor.sh "+this.distributor_name);
	}

	@Test(description = "Refresh manifests - delete distributor and refresh ID:268146",groups={"cfse-cli","headpin-cli"})
	public void test_refreshDeleteDistributor(){

		String uid = KatelloUtils.getUniqueID();
		this.org_name = "org"+uid;
		this.distributor_name="Tmp-"+ uid;
		String sys = "sys-name_"+uid;
		org = new KatelloOrg(this.cli_worker, this.org_name,null);
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");

		this.manifest_name=obtain_distributorManifest(this.distributor_name);

		KatelloProvider providerRH;
		providerRH  = new KatelloProvider(this.cli_worker, KatelloProvider.PROVIDER_REDHAT, this.org_name, null, null);

		// import manifest 
		SSHCommandResult exec_result = providerRH.import_manifest(this.manifest_name,true);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).trim().contains(KatelloProvider.OUT_MANIFEST_IMPORTED), "Check output");
		try{Thread.sleep(5000);}catch(InterruptedException iex){}

		exec_result = org.subscriptions();
		Assert.assertTrue(exec_result.getExitCode() == 0,"Check - return code");

		//Register and subscribe the system
		KatelloSystem sys_reg = new KatelloSystem(this.cli_worker,sys,this.org_name,null);
		exec_result = sys_reg.rhsm_registerForce();
		Assert.assertTrue(exec_result.getExitCode() == 0,"Check - return code");

		exec_result = sys_reg.subscriptions_available();
		String poolId1 = KatelloUtils.grepCLIOutput("ID", getOutput(exec_result).trim(),1);
		Assert.assertNotNull(poolId1, "Check - pool Id is not null");

		exec_result = sys_reg.subscribe(poolId1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertEquals(exec_result.getStdout().trim(), 
				String.format(KatelloSystem.OUT_SUBSCRIBE, sys),
				"Check - subscribe system output.");

		KatelloUtils.sshOnServer("chmod a+x /tmp/stgportal-deleteDistributor.sh; /tmp/stgportal-deleteDistributor.sh "+this.distributor_name);

		// refresh manifest
		try{Thread.sleep(5000);}catch(InterruptedException iex){}
		exec_result = providerRH.refresh_manifest();
		Assert.assertTrue(exec_result.getExitCode() == 65, "Check - return code");
		try{Thread.sleep(5000);}catch(InterruptedException iex){}

		// System looses the subscriptions assigned
		exec_result = sys_reg.subscriptions_available();
		poolId1 = KatelloUtils.grepCLIOutput("ID", getOutput(exec_result).trim(),1);
		Assert.assertNull(poolId1, "Pool ID is null.");

		exec_result = sys_reg.info();
		Assert.assertTrue(exec_result.getExitCode() == 0,"Check - return code");

		exec_result = org.delete();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code"); 

		KatelloUtils.sshOnServer("chmod a+x /tmp/stgportal-deleteDistributor.sh; /tmp/stgportal-deleteDistributor.sh "+this.distributor_name);
	}

	@AfterClass(description="Clean up the server - remove TempFiles", alwaysRun=true)
	public void tearDown() {

		KatelloUtils.sshOnServer("chmod a+x /tmp/cleanup-tempFiles.sh; /tmp/cleanup-tempFiles.sh");
	}
}