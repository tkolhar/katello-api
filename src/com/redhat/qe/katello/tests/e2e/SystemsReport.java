package com.redhat.qe.katello.tests.e2e;

import java.io.File;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import com.redhat.qe.auto.testng.Assert;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.katello.base.obj.KatelloChangeset;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.tasks.KatelloTasks;
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

	@BeforeTest(description="Init unique names", alwaysRun=true)
	public void setUp(){
		String uid = KatelloTestScript.getUniqueID();
		this.env_dev = "Dev-"+uid;
		this.env_test = "Test-"+uid;

		ArrayList<String> orgs = getOrgsWithImportedManifest();
		if(orgs.size()==0){
			log.info("Seems there is no org with imported stage manifest. Doing it now.");
			SCPTools scp = new SCPTools(
					System.getProperty("katello.client.hostname", "localhost"), 
					System.getProperty("katello.client.ssh.user", "root"), 
					System.getProperty("katello.client.sshkey.private", ".ssh/id_hudson_dsa"), 
					System.getProperty("katello.client.sshkey.passphrase", "null"));
			Assert.assertTrue(scp.sendFile("data"+File.separator+"export.zip", "/tmp"),
					"export.zip sent successfully");			
			this.org = "org-manifest-"+uid;
			KatelloOrg org = new KatelloOrg(this.org, null);
			org.cli_create();
			KatelloProvider prov = new KatelloProvider(KatelloProvider.PROVIDER_REDHAT, this.org, null, null);
			SSHCommandResult res = prov.import_manifest("/tmp"+File.separator+"export.zip", new Boolean(true));
			Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (provider import_manifest)");
			Assert.assertTrue(getOutput(res).contains("Manifest imported"),"Message - (provider import_manifest)");
		}else{
			this.org = orgs.get(0);
			log.info("There is an org having manifest. Using: ["+this.org+"]");
		}
	}
	
	@Test(description="Promote RHEL Server to both environments", enabled=true)
	public void test_promoteToEnvs(){
		log.info("Enable repo: ["+KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT+"]");
		KatelloRepo repo = new KatelloRepo(KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT, this.org, KatelloProduct.RHEL_SERVER, null, null, null);
		SSHCommandResult res = repo.enable();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo enable)");
		Assert.assertTrue(getOutput(res).contains("enabled."),"Message - (repo enable)");
		
		KatelloEnvironment env = new KatelloEnvironment(this.env_dev, null, this.org, KatelloEnvironment.LIBRARY);
		env.cli_create();
		KatelloChangeset cs = new KatelloChangeset("csDev_"+KatelloTestScript.getUniqueID(), this.org, this.env_dev);
		cs.create();
		cs.update_addProduct(KatelloProduct.RHEL_SERVER);
		res = cs.promote();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (changeset promote)");
		Assert.assertTrue(getOutput(res).endsWith("promoted"),"Message - (changeset promote)");
		
		env = new KatelloEnvironment(this.env_test, null, this.org, this.env_dev);
		env.cli_create();
		cs = new KatelloChangeset("csTest_"+KatelloTestScript.getUniqueID(), this.org, this.env_test);
		cs.create();
		cs.update_addProduct(KatelloProduct.RHEL_SERVER);
		res = cs.promote();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (changeset promote)");
		Assert.assertTrue(getOutput(res).endsWith("promoted"),"Message - (changeset promote)");		
	}
	
	@Test(description="Add 2 system to env: Dev and 1 systems to: Test", dependsOnMethods={"test_promoteToEnvs"}, enabled=true)
	public void test_addSystemsToEnvs(){
		String sys = "`hostname`"+KatelloTestScript.getUniqueID();
		rhsm_clean();
		rhsm_register(org, this.env_dev, "1-"+sys, true);
		rhsm_clean();
		rhsm_register(org, this.env_test, "2-"+sys, true);
		rhsm_clean();
		SSHCommandResult res = rhsm_register(org, this.env_dev, "3-"+sys, true);
//		Assert.assertTrue(res.getExitCode().intValue()==1, "Check - return code (system register)");
		String subscriptionStatus = KatelloTasks.grepCLIOutput("Status", getOutput(res).trim()); 
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
	
	@AfterTest(description="Cleanup the org - allow others to reuse the manifest", alwaysRun=true)
	public void tearDown(){
		KatelloOrg org = new KatelloOrg(this.org, null);
		SSHCommandResult res = org.delete();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (org delete)");
	}
}
