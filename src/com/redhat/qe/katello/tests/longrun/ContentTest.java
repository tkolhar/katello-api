package com.redhat.qe.katello.tests.longrun;

import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliLongrunBase;
import com.redhat.qe.katello.base.obj.KatelloContentDefinition;
import com.redhat.qe.katello.base.obj.KatelloContentView;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.tools.SSHCommandResult;

/**
 * Consuming content from the synced RHEL6Server.	
 * @author gkhachik
 *
 */
@Test(groups={"cfse-cli"})
public class ContentTest extends KatelloCliLongrunBase {

	private SSHCommandResult res;
	private String envTesting;
	private String uid = KatelloUtils.getUniqueID();
	private String contView;
	private String sysName;
	
	private boolean repoSynced = false;

	@BeforeClass(description="init: create initial stuff")
	public void setUp(){
		this.base_org_name = "Awesome Org "+uid;
		this.envTesting = "Testing-"+uid;
		this.contView = "RHEL6-"+uid;
		this.sysName = "awesomeSystem-"+uid;
		
		if(!findSyncedRhelToUse()){
			res = new KatelloOrg(this.cli_worker, base_org_name, null).cli_create();
			Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - exit.Code");
			KatelloUtils.scpOnClient(cli_worker.getClientHostname(), "data/"+KatelloProvider.MANIFEST_12SUBSCRIPTIONS, "/tmp");
			res = new KatelloProvider(this.cli_worker, KatelloProvider.PROVIDER_REDHAT, base_org_name, null, null).import_manifest("/tmp/"+KatelloProvider.MANIFEST_12SUBSCRIPTIONS, new Boolean(true));
			Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
			KatelloProduct prod=new KatelloProduct(this.cli_worker, KatelloProduct.RHEL_SERVER, base_org_name, KatelloProvider.PROVIDER_REDHAT, null, null, null,null, null);
			res = prod.repository_set_enable(KatelloProduct.REPOSET_RHEL6_RPMS);
			Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo set enable)");
			KatelloRepo repo = new KatelloRepo(this.cli_worker, KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT, base_org_name, KatelloProduct.RHEL_SERVER, null, null, null);
			SSHCommandResult res = repo.enable();
			Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo enable)");
			Assert.assertTrue(getOutput(res).contains("enabled."),"Message - (repo enable)");
		}
		
		res = new KatelloEnvironment(this.cli_worker, envTesting, null, base_org_name, KatelloEnvironment.LIBRARY).cli_create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - exit.Code");
	}

	@Test(description="Sync RHEL6Server content and promote to Testing env.")
	public void test_syncAndPromoteRhel6(){
		// sync
		KatelloRepo repo = new KatelloRepo(this.cli_worker, KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT, base_org_name, KatelloProduct.RHEL_SERVER, null, null, null);

		res = repo.status();
		this.repoSynced = !KatelloUtils.grepCLIOutput("Last Sync", getOutput(res)).equals("never");
		if(!repoSynced){
			res = repo.synchronize();
			Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo synchronize)");
		}
		
		res = repo.status();
		Assert.assertFalse(KatelloUtils.grepCLIOutput("Package Count", getOutput(res)).equals("0"), "Check - package count is NOT 0");
		
		// promote
		KatelloContentDefinition cd = new KatelloContentDefinition(cli_worker, "cd"+uid, null, base_org_name, null);
		res = cd.create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - exit.Code");
		res = cd.add_repo(repo.product, repo.name);
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - exit.Code");
		res = cd.publish(contView, null, null);
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - exit.Code");
		KatelloContentView cont = new KatelloContentView(cli_worker, contView, base_org_name);
		res = cont.promote_view(envTesting);
	}
	
	@Test(description="Check product status after the sync", dependsOnMethods={"test_syncAndPromoteRhel6"})
	public void test_productStatusSynced(){
		res = new KatelloProduct(this.cli_worker, KatelloProduct.RHEL_SERVER, base_org_name, KatelloProvider.PROVIDER_REDHAT, null, null, null,null, null).status();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - exit.Code");
		Assert.assertFalse(KatelloUtils.grepCLIOutput("Last Sync", getOutput(res)).equals("never"), "Check - output DOES'NT equal last_sync == never");
		Assert.assertFalse(KatelloUtils.grepCLIOutput("Sync State", getOutput(res)).equals("Not synced"), "Check - output DOES'NT equal sync_state == Not synced");
	}

	@Test(description = "register consumer with --servicelevel and --release arguments", dependsOnMethods={"test_productStatusSynced"})
	public void test_regConsumer(){
		KatelloSystem sys = new KatelloSystem(this.cli_worker, sysName, base_org_name, envTesting+"/"+contView);
		res = sys.rhsm_registerForceWithReleaseSLA("6Server", "Self-support", true, true);
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - exit.Code");
		String output = getOutput(res);
		String regExp = ".*The system has been registered with ID:.*" +
				"Service level set to:\\s+Self-support.*" +
				"Installed Product Current Status:.*" +
				"Product Name:\\s+"+KatelloProduct.RHEL_SERVER+".*" +
				"Status:\\s+Subscribed.*";
		Assert.assertTrue(output.replaceAll("\n", "").matches(regExp), 
				"Check - output matches to regexp");
	}

	@AfterClass(description="cleanup the stuff", alwaysRun=true)
	public void tearDown(){
		// rhsm_clean();
		// TODO - do clean rpm repos that has been installed.
	}
}
