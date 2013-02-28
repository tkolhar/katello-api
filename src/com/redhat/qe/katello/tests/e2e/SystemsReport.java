package com.redhat.qe.katello.tests.e2e;

import java.io.File;
import java.util.logging.Logger;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.obj.KatelloChangeset;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SCPTools;
import com.redhat.qe.tools.SSHCommandResult;

/**
 * Implementation of one of V1 scenarios:<BR>
 * Description:<BR>
 * Create system (and user) reports. 
 * @author gkhachik
 */
@Test(groups={"cfse-e2e"})
public class SystemsReport extends KatelloCliTestScript{
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
	
	@Test(description="Import correct manifest", enabled=true, dependsOnMethods={"test_importHackedManifest", "test_importEmptyManifest"})
	public void test_importManifest() {

		SCPTools scp = new SCPTools(
				System.getProperty("katello.client.hostname", "localhost"), 
				System.getProperty("katello.client.ssh.user", "root"), 
				System.getProperty("katello.client.sshkey.private", ".ssh/id_hudson_dsa"), 
				System.getProperty("katello.client.sshkey.passphrase", "null"));
		Assert.assertTrue(scp.sendFile("data"+File.separator+MANIFEST_2SUBSCRIPTIONS, "/tmp"),
				MANIFEST_2SUBSCRIPTIONS+" sent successfully");			

		KatelloProvider prov = new KatelloProvider(KatelloProvider.PROVIDER_REDHAT, this.org, null, null);
		SSHCommandResult res = prov.import_manifest("/tmp"+File.separator+MANIFEST_2SUBSCRIPTIONS, new Boolean(true));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (provider import_manifest)");
		Assert.assertTrue(getOutput(res).contains("Manifest imported"),"Message - (provider import_manifest)");
		
		log.finest("put in socket.facts \"1\" - scenario here considers having one CPU socket");
		KatelloUtils.sshOnClient("echo '{\"cpu.cpu_socket(s)\":\"1\"}' > /etc/rhsm/facts/sockets.facts");
	}
	
	@Test(description="Promote RHEL Server to both environments", enabled=true, dependsOnMethods={"test_importManifest"})
	public void test_promoteToEnvs(){
		log.info("Enable repo: ["+KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT+"]");
		KatelloRepo repo = new KatelloRepo(KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT, this.org, KatelloProduct.RHEL_SERVER, null, null, null);
		SSHCommandResult res = repo.enable();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo enable)");
		Assert.assertTrue(getOutput(res).contains("enabled."),"Message - (repo enable)");
		
		KatelloEnvironment env = new KatelloEnvironment(this.env_dev, null, this.org, KatelloEnvironment.LIBRARY);
		env.cli_create();
		KatelloChangeset cs = new KatelloChangeset("csDev_"+KatelloUtils.getUniqueID(), this.org, this.env_dev);
		cs.create();
		cs.update_addProduct(KatelloProduct.RHEL_SERVER);
		res = cs.apply();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (changeset promote)");
		Assert.assertTrue(getOutput(res).endsWith("applied"),"Message - (changeset promote)");
		
		env = new KatelloEnvironment(this.env_test, null, this.org, this.env_dev);
		env.cli_create();
		cs = new KatelloChangeset("csTest_"+KatelloUtils.getUniqueID(), this.org, this.env_test);
		cs.create();
		cs.update_addProduct(KatelloProduct.RHEL_SERVER);
		res = cs.apply();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (changeset promote)");
		Assert.assertTrue(getOutput(res).endsWith("applied"),"Message - (changeset promote)");		
		
		KatelloOrg org = new KatelloOrg(this.org, null);
    	res = org.subscriptions();
  
	}
	
	@Test(description="Add 2 system to env: Dev and 1 systems to: Test", dependsOnMethods={"test_promoteToEnvs"}, enabled=true)
	public void test_addSystemsToEnvs(){
		String sys = "localhost"+KatelloUtils.getUniqueID();
		sys_name1= sys;
		sys_name2 = "1-"+sys;
		sys_name3 = "2-"+sys;
		rhsm_clean_only();
		rhsm_register(org, this.env_dev, sys_name1, true);
		rhsm_clean_only();
		rhsm_register(org, this.env_test, sys_name2, true);
		rhsm_clean_only();
		SSHCommandResult res = rhsm_register(org, this.env_dev, sys_name3, true);
//		Assert.assertTrue(res.getExitCode().intValue()==1, "Check - return code (system register)");
		String subscriptionStatus = KatelloCli.grepCLIOutput("Status", getOutput(res).trim()); 
		Assert.assertTrue(subscriptionStatus.trim().equals("Not Subscribed"),"Check - system should not be subscribed (3rd registration)");		
	}
	
	@Test(description="Check red systems >= 1", dependsOnMethods={"test_addSystemsToEnvs"}, enabled=true)
	public void test_redSystemsCount(){
		SSHCommandResult res = new KatelloCli("system report --org \""+this.org+"\" --format csv | grep \",red,\" | wc -l", null).run();
		int redCnt = Integer.parseInt(getOutput(res).trim());
		Assert.assertTrue((redCnt>=1), "Check - red systems cound >=1");
	}
	
	@Test(description="Check green systems >= 2", dependsOnMethods={"test_addSystemsToEnvs"}, enabled=true)
	public void test_greenSystemsCount(){
		SSHCommandResult res = new KatelloCli("system report --org \""+this.org+"\" --format csv | grep \",green,\" | wc -l", null).run();
		int redCnt = Integer.parseInt(getOutput(res).trim());
		Assert.assertTrue((redCnt>=2), "Check - green systems cound >=2");
	}
	
	@Test(description="Check report headers - compliance", dependsOnMethods={"test_addSystemsToEnvs"}, enabled=true)
	public void test_reportHeaders_compliance(){
		SSHCommandResult res = new KatelloCli("system report --org "+this.org+" | grep \"| compliance |\" | wc -l", null).run();
		int hdrCnt = Integer.parseInt(getOutput(res).trim());
		Assert.assertTrue((hdrCnt==1), "Check - header compliance");
		res = new KatelloCli("system report --org "+this.org+" | grep \"| compliant_until |\\|compliant until\" | wc -l", null).run();
		hdrCnt = Integer.parseInt(getOutput(res).trim());
		Assert.assertTrue((hdrCnt==1), "Check - header compliant_until");
	}
	
	@Test(description="unsibscribe systems", dependsOnMethods={"test_reportHeaders_compliance"})
	public void test_unsubscribeSystems() {
		KatelloSystem sys = new KatelloSystem(sys_name1, this.org, this.env_dev);
		SSHCommandResult res = sys.rhsm_identity();
		String system_uuid = KatelloCli.grepCLIOutput("Current identity is", res.getStdout());
		sys.uuid = system_uuid;
		res = sys.unsubscribe();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (system unsubscribe)");
		
		sys = new KatelloSystem(sys_name2, this.org, this.env_test);
		res = sys.rhsm_identity();
		system_uuid = KatelloCli.grepCLIOutput("Current identity is", res.getStdout());
		sys.uuid = system_uuid;
		res = sys.unsubscribe();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (system unsubscribe)");
	}
	
	@AfterClass(description="Cleanup the org - allow others to reuse the manifest", alwaysRun=true)
	public void tearDown(){
		log.finest("Remove the prepared: /etc/rhsm/facts/sockets.facts");
		KatelloUtils.sshOnClient("rm -f /etc/rhsm/facts/sockets.facts");

		KatelloOrg org = new KatelloOrg(this.org, null);
		SSHCommandResult res = org.delete();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (org delete)");
	}
}
