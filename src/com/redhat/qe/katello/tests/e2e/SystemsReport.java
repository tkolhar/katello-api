package com.redhat.qe.katello.tests.e2e;

import java.io.File;
import java.util.logging.Logger;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.base.tngext.TngPriority;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

/**
 * Implementation of one of V1 scenarios:<BR>
 * Description:<BR>
 * Create system (and user) reports. 
 * @author gkhachik
 */
public class SystemsReport extends KatelloCliTestBase{
	protected static Logger log = Logger.getLogger(SystemsReport.class.getName());

	String org;
	private String env_dev;
	private String env_test;
	private String sys_name1;
	private String sys_name2;
	private String sys_name3;
	public static final String MANIFEST_HACKED = "manifest-hacked.zip";
	public static final String EMPTY_HACKED = "manifest-empty.zip";
	public static final String MANIFEST_2SUBSCRIPTIONS = "manifest-automation-CLI-2subscriptions.zip";

	@BeforeClass(description="Init unique names", alwaysRun=true)
	public void setUp(){
		String uid = KatelloUtils.getUniqueID();
		this.env_dev = "Dev-"+uid;
		this.env_test = "Test-"+uid;
		this.org = "wrong-manifest-"+uid;
		KatelloOrg org = new KatelloOrg(cli_worker, this.org, null);
		org.cli_create();
	}
	
	@Test(description="Import hacked manifest")
	public void test_importHackedManifest() {
		KatelloUtils.scpOnClient(cli_worker.getClientHostname(), "data/"+MANIFEST_HACKED, "/tmp");
		
		KatelloProvider prov = new KatelloProvider(this.cli_worker, KatelloProvider.PROVIDER_REDHAT, this.org, null, null);
		SSHCommandResult res = prov.import_manifest("/tmp"+File.separator+MANIFEST_HACKED, new Boolean(true));
		Assert.assertEquals(res.getExitCode().intValue(), 65, "Check - error code (provider import_manifest)");
		Assert.assertTrue(getOutput(res).contains("Provider [ "+KatelloProvider.PROVIDER_REDHAT+" ] failed to import manifest"),"Message - (provider import_manifest)");
	}

	@Test(description="Import empty manifest")
	public void test_importEmptyManifest() {
		KatelloUtils.scpOnClient(cli_worker.getClientHostname(), "data/"+EMPTY_HACKED, "/tmp");
		
		KatelloProvider prov = new KatelloProvider(this.cli_worker, KatelloProvider.PROVIDER_REDHAT, this.org, null, null);
		SSHCommandResult res = prov.import_manifest("/tmp"+File.separator+EMPTY_HACKED, new Boolean(true));
		Assert.assertEquals(res.getExitCode().intValue(), 65, "Check - error code (provider import_manifest)");
		Assert.assertTrue(getOutput(res).contains("Provider [ "+KatelloProvider.PROVIDER_REDHAT+" ] failed to import manifest"),"Message - (provider import_manifes)");
	}
	
	@Test(description="Import correct manifest", dependsOnMethods={"test_importHackedManifest", "test_importEmptyManifest"})
	public void test_importManifest() {
		KatelloUtils.scpOnClient(cli_worker.getClientHostname(), "data/"+MANIFEST_2SUBSCRIPTIONS, "/tmp");

		KatelloProvider prov = new KatelloProvider(this.cli_worker, KatelloProvider.PROVIDER_REDHAT, this.org, null, null);
		SSHCommandResult res = prov.import_manifest("/tmp"+File.separator+MANIFEST_2SUBSCRIPTIONS, new Boolean(true));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (provider import_manifest)");
		Assert.assertTrue(getOutput(res).contains(KatelloProvider.OUT_MANIFEST_IMPORTED),"Message - (provider import_manifest)");
		
		log.finest("put in socket.facts \"1\" - scenario here considers having one CPU socket");
		sshOnClient("echo '{\"cpu.cpu_socket(s)\":\"1\"}' > /etc/rhsm/facts/sockets.facts");
	}
	
	@Test(description="Promote RHEL Server to both environments", dependsOnMethods={"test_importManifest"})
	public void test_disableenableRHELRepo() {
		log.info("Enable repo: ["+KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT+"]");
		
		KatelloProduct prod=new KatelloProduct(this.cli_worker, KatelloProduct.RHEL_SERVER, this.org, KatelloProvider.PROVIDER_REDHAT, null, null, null,null, null);
		SSHCommandResult res = prod.repository_set_enable(KatelloProduct.REPOSET_RHEL6_RPMS);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo set enable)");
		KatelloRepo repo = new KatelloRepo(this.cli_worker, KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT, this.org, KatelloProduct.RHEL_SERVER, null, null, null);
		res = repo.enable();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo enable)");
		Assert.assertTrue(getOutput(res).contains("enabled."),"Message - (repo enable)");
		
		res = repo.disable();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo disable)");
		Assert.assertTrue(getOutput(res).contains("disabled."),"Message - (repo disable)");
		
		res = repo.enable();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo enable)");
		Assert.assertTrue(getOutput(res).contains("enabled."),"Message - (repo enable)");
	}
	
	@Test(description="Promote RHEL Server to both environments", dependsOnMethods={"test_disableenableRHELRepo"})
	public void test_promoteToEnvs(){		
		SSHCommandResult res = new KatelloEnvironment(cli_worker, env_dev,null,org, KatelloEnvironment.LIBRARY).cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		KatelloUtils.promoteProductToEnvironment(cli_worker, org, KatelloProduct.RHEL_SERVER, env_dev);
		
		res = new KatelloEnvironment(cli_worker, env_test,null,org, KatelloEnvironment.LIBRARY).cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		KatelloUtils.promoteProductToEnvironment(cli_worker, org, KatelloProduct.RHEL_SERVER, env_test);
	}
	
	@Test(description="Add 2 system to env: Dev and 1 systems to: Test", dependsOnMethods={"test_promoteToEnvs"})
	public void test_addSystemsToEnvs(){
		String sys = "localhost"+KatelloUtils.getUniqueID();
		sys_name1= sys;
		sys_name2 = "1-"+sys;
		sys_name3 = "2-"+sys;
		rhsm_clean_only();
		rhsm_register(cli_worker.getClientHostname(), org, this.env_dev, sys_name1, true);
		rhsm_clean_only();
		rhsm_register(cli_worker.getClientHostname(), org, this.env_test, sys_name2, true);
		rhsm_clean_only();
		SSHCommandResult res = rhsm_register(cli_worker.getClientHostname(), org, this.env_dev, sys_name3, true);
//		Assert.assertTrue(res.getExitCode().intValue()==1, "Check - return code (system register)");
		String subscriptionStatus = KatelloUtils.grepCLIOutput("Status", getOutput(res).trim()); 
		Assert.assertTrue(subscriptionStatus.trim().equals("Not Subscribed"),"Check - system should not be subscribed (3rd registration)");		
	}
	
	@Test(description="Check red systems >= 1", dependsOnMethods={"test_addSystemsToEnvs"})
	public void test_redSystemsCount(){
		SSHCommandResult res = new KatelloCli("system report --org \""+this.org+"\" --format csv | grep \",red,\" | wc -l", null, null, cli_worker.getClientHostname()).run();
		int redCnt = Integer.parseInt(getOutput(res).trim());
		Assert.assertTrue((redCnt>=1), "Check - red systems cound >=1");
	}
	
	@Test(description="Check green systems >= 2", dependsOnMethods={"test_addSystemsToEnvs"})
	public void test_greenSystemsCount(){
		SSHCommandResult res = new KatelloCli("system report --org \""+this.org+"\" --format csv | grep \",green,\" | wc -l", null, null, cli_worker.getClientHostname()).run();
		int redCnt = Integer.parseInt(getOutput(res).trim());
		Assert.assertTrue((redCnt>=2), "Check - green systems cound >=2");
	}
	
	@Test(description="Check report headers - compliance", dependsOnMethods={"test_addSystemsToEnvs"})
	public void test_reportHeaders_compliance(){
		SSHCommandResult res = new KatelloCli("system report --org "+this.org+" | grep \"| compliance |\" | wc -l", null, null, cli_worker.getClientHostname()).run();
		int hdrCnt = Integer.parseInt(getOutput(res).trim());
		Assert.assertTrue((hdrCnt==1), "Check - header compliance");
		res = new KatelloCli("system report --org "+this.org+" | grep \"| compliant_until |\\|compliant until\" | wc -l", null, null, cli_worker.getClientHostname()).run();
		hdrCnt = Integer.parseInt(getOutput(res).trim());
		Assert.assertTrue((hdrCnt==1), "Check - header compliant_until");
	}
	
	@Test(description="unsibscribe systems", dependsOnMethods={"test_reportHeaders_compliance"})
	public void test_unsubscribeSystems() {
		KatelloSystem sys = new KatelloSystem(this.cli_worker, sys_name1, this.org, this.env_dev);
		SSHCommandResult res = sys.rhsm_identity();
		String system_uuid = KatelloUtils.grepCLIOutput("Current identity is", res.getStdout());
		sys.uuid = system_uuid;
		res = sys.unsubscribe();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (system unsubscribe)");
		
		sys = new KatelloSystem(this.cli_worker, sys_name2, this.org, this.env_test);
		res = sys.rhsm_identity();
		system_uuid = KatelloUtils.grepCLIOutput("Current identity is", res.getStdout());
		sys.uuid = system_uuid;
		res = sys.unsubscribe();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (system unsubscribe)");
	}
	
	@AfterClass(description="Cleanup the org - allow others to reuse the manifest", alwaysRun=true)
	public void tearDown(){
		log.finest("Remove the prepared: /etc/rhsm/facts/sockets.facts");
		sshOnClient("rm -f /etc/rhsm/facts/sockets.facts");

		KatelloOrg org = new KatelloOrg(this.cli_worker, this.org, null);
		SSHCommandResult res = org.delete();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (org delete)");
	}
}
