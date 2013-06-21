package com.redhat.qe.katello.tests.e2e;

import java.io.File;
import java.util.logging.Logger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.obj.KatelloContentDefinition;
import com.redhat.qe.katello.base.obj.KatelloContentView;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.common.TngRunGroups;
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
	private String contViewName; 
	private SSHCommandResult exec_result;
	
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
		new KatelloEnvironment(env_dev, null, this.org, KatelloEnvironment.LIBRARY).cli_create();
		new KatelloEnvironment(env_test, null, this.org, KatelloEnvironment.LIBRARY).cli_create();
		this.contViewName = "contView-"+uid;
	}
	
	@Test(description="Import hacked manifest", groups={TngRunGroups.TNG_KATELLO_System_Consumer})
	public void test_importHackedManifest() {
		
		KatelloUtils.scpOnClient("data/"+MANIFEST_HACKED, "/tmp");
		
		KatelloProvider prov = new KatelloProvider(KatelloProvider.PROVIDER_REDHAT, this.org, null, null);
		SSHCommandResult res = prov.import_manifest("/tmp"+File.separator+MANIFEST_HACKED, new Boolean(true));
		Assert.assertEquals(res.getExitCode().intValue(), 65, "Check - error code (provider import_manifest)");
		Assert.assertTrue(getOutput(res).contains("Provider [ "+KatelloProvider.PROVIDER_REDHAT+" ] failed to import manifest"),"Message - (provider import_manifest)");
	}

	@Test(description="Import empty manifest", groups={TngRunGroups.TNG_KATELLO_System_Consumer})
	public void test_importEmptyManifest() {
		
		KatelloUtils.scpOnClient("data/"+EMPTY_HACKED, "/tmp");
		
		KatelloProvider prov = new KatelloProvider(KatelloProvider.PROVIDER_REDHAT, this.org, null, null);
		SSHCommandResult res = prov.import_manifest("/tmp"+File.separator+EMPTY_HACKED, new Boolean(true));
		Assert.assertEquals(res.getExitCode().intValue(), 65, "Check - error code (provider import_manifest)");
		Assert.assertTrue(getOutput(res).contains("Provider [ "+KatelloProvider.PROVIDER_REDHAT+" ] failed to import manifest"),"Message - (provider import_manifes)");
	}
	
	@Test(description="Import correct manifest", dependsOnMethods={"test_importHackedManifest", "test_importEmptyManifest"},
			groups={TngRunGroups.TNG_KATELLO_System_Consumer})
	public void test_importManifest() {

		KatelloUtils.scpOnClient("data/"+MANIFEST_2SUBSCRIPTIONS, "/tmp");

		KatelloProvider prov = new KatelloProvider(KatelloProvider.PROVIDER_REDHAT, this.org, null, null);
		SSHCommandResult res = prov.import_manifest("/tmp"+File.separator+MANIFEST_2SUBSCRIPTIONS, new Boolean(true));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (provider import_manifest)");
		Assert.assertTrue(getOutput(res).contains(KatelloProvider.OUT_MANIFEST_IMPORTED),"Message - (provider import_manifest)");
		
		log.finest("put in socket.facts \"1\" - scenario here considers having one CPU socket");
		KatelloUtils.sshOnClient("echo '{\"cpu.cpu_socket(s)\":\"1\"}' > /etc/rhsm/facts/sockets.facts");
	}
	
	@Test(description="Promote RHEL Server to both environments", dependsOnMethods={"test_importManifest"},
			groups={TngRunGroups.TNG_KATELLO_System_Consumer})
	public void test_disableenableRHELRepo() {
		log.info("Enable repo: ["+KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT+"]");
		
		KatelloProduct prod=new KatelloProduct(KatelloProduct.RHEL_SERVER, this.org, KatelloProvider.PROVIDER_REDHAT, null, null, null,null, null);
		SSHCommandResult res = prod.repository_set_enable(KatelloProduct.REPOSET_RHEL6_RPMS);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo set enable)");
		KatelloRepo repo = new KatelloRepo(KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT, this.org, KatelloProduct.RHEL_SERVER, null, null, null);
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
	
	@Test(description="Promote RHEL Server to both environments", dependsOnMethods={"test_disableenableRHELRepo"},
			groups={TngRunGroups.TNG_KATELLO_System_Consumer})
	public void test_promoteToEnvs(){
		KatelloContentDefinition cvd = new KatelloContentDefinition("cd"+KatelloUtils.getUniqueID(), null, org, null);
		cvd.create();
		cvd.add_product(KatelloProduct.RHEL_SERVER);
		cvd.publish(contViewName, null, null);
		KatelloContentView cv = new KatelloContentView(contViewName, org);
		exec_result = cv.promote_view(env_dev);
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");
		exec_result = cv.promote_view(env_test);
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");
	}
	
	@Test(description="Add 2 system to env: Dev and 1 systems to: Test", dependsOnMethods={"test_promoteToEnvs"},
			groups={TngRunGroups.TNG_KATELLO_System_Consumer})
	public void test_addSystemsToEnvs(){
		String sys = "localhost"+KatelloUtils.getUniqueID();
		sys_name1= sys;
		sys_name2 = "1-"+sys;
		sys_name3 = "2-"+sys;
		rhsm_clean_only();
		rhsm_register(org, env_dev+"/"+contViewName, sys_name1, true);
		rhsm_clean_only();
		rhsm_register(org, env_test+"/"+contViewName, sys_name2, true);
		rhsm_clean_only();
		SSHCommandResult res = rhsm_register(org, env_dev+"/"+contViewName, sys_name3, true);
		String subscriptionStatus = KatelloUtils.grepCLIOutput("Status", getOutput(res).trim()); 
		Assert.assertTrue(subscriptionStatus.trim().equals("Not Subscribed"),"Check - system should not be subscribed (3rd registration)");		
	}
	
	/** TCMS scenario is: <a href="https://tcms.engineering.redhat.com/case/231404/?from_plan=7771">here</a> */
	@Test(description="85c9e53d-5eb8-4f35-878a-7e4ba7618b97", dependsOnMethods={"test_addSystemsToEnvs"}, 
			groups={TngRunGroups.TNG_KATELLO_System_Consumer})
	public void test_redSystemsCount(){
		SSHCommandResult res = new KatelloCli("system report --org \""+this.org+"\" --format csv | grep \",red,\" | wc -l", null).run();
		int redCnt = Integer.parseInt(getOutput(res).trim());
		Assert.assertTrue((redCnt>=1), "Check - red systems cound >=1");
	}
	
	@Test(description="Check green systems >= 2", dependsOnMethods={"test_addSystemsToEnvs"})
	public void test_greenSystemsCount(){
		SSHCommandResult res = new KatelloCli("system report --org \""+this.org+"\" --format csv | grep \",green,\" | wc -l", null).run();
		int redCnt = Integer.parseInt(getOutput(res).trim());
		Assert.assertTrue((redCnt>=2), "Check - green systems cound >=2");
	}
	
	@Test(description="Check report headers - compliance", dependsOnMethods={"test_addSystemsToEnvs"})
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
		String system_uuid = KatelloUtils.grepCLIOutput("Current identity is", res.getStdout());
		sys.uuid = system_uuid;
		res = sys.unsubscribe();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (system unsubscribe)");
		
		sys = new KatelloSystem(sys_name2, this.org, env_test);
		res = sys.rhsm_identity();
		system_uuid = KatelloUtils.grepCLIOutput("Current identity is", res.getStdout());
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
