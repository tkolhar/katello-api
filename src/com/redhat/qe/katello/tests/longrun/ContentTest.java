package com.redhat.qe.katello.tests.longrun;

import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import com.redhat.qe.katello.common.KatelloUtils;
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
import com.redhat.qe.katello.base.obj.KatelloUser;
import com.redhat.qe.tools.SSHCommandResult;

/**
 * Consuming content from the synced RHEL6Server.	
 * @author gkhachik
 *
 */
@Test(groups={"cfse-cli"})
public class ContentTest extends KatelloCliTestScript{

	private SSHCommandResult res;
	private String org;
	private String envTesting;
	private String uid = KatelloUtils.getUniqueID();
	private String contView;
	private String sysName;

	@BeforeClass(description="init: create initial stuff")
	public void setUp(){
		String manifestZip = "manifest.zip";
		
		this.org = "Awesome Org "+uid;
		this.envTesting = "Testing";
		this.contView = "RHEL6";
		this.sysName = "awesomeSystem-"+uid;
		
		res = new KatelloOrg(org, null).cli_create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - exit.Code");
		res = new KatelloEnvironment(envTesting, null, org, KatelloEnvironment.LIBRARY).cli_create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - exit.Code");
		KatelloUtils.scpOnClient("data/"+manifestZip, "/tmp");
		res = new KatelloProvider(KatelloProvider.PROVIDER_REDHAT, org, null, null).import_manifest("/tmp/"+manifestZip, new Boolean(true));
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		KatelloProduct prod=new KatelloProduct(KatelloProduct.RHEL_SERVER,org, KatelloProvider.PROVIDER_REDHAT, null, null, null,null, null);
		res = prod.repository_set_enable(KatelloProduct.REPOSET_RHEL6_RPMS);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo set enable)");
		KatelloRepo repo = new KatelloRepo(KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT,org, KatelloProduct.RHEL_SERVER, null, null, null);
		SSHCommandResult res = repo.enable();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo enable)");
		Assert.assertTrue(getOutput(res).contains("enabled."),"Message - (repo enable)");
	}

	@Test(description="Sync RHEL6Server content and promote to Testing env.")
	public void syncAndPromoteRhel6(){
		// sync
		KatelloRepo repo = new KatelloRepo(KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT,org, KatelloProduct.RHEL_SERVER, null, null, null);
		res = repo.synchronize();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo synchronize)");
		
		// promote
		KatelloContentDefinition cd = new KatelloContentDefinition("cd"+uid, null, org, null);
		res = cd.create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - exit.Code");
		res = cd.add_product(KatelloProduct.RHEL_SERVER);
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - exit.Code");
		res = cd.publish(contView, null, null);
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - exit.Code");
		KatelloContentView cont = new KatelloContentView(contView, org);
		res = cont.promote_view(envTesting);
	}

	@Test(description = "register consumer with --servicelevel and --release arguments", dependsOnMethods={"syncAndPromoteRhel6"})
	public void test_regConsumer(){
		KatelloSystem sys = new KatelloSystem(sysName, org, envTesting+"/"+contView);
		res = sys.rhsm_registerForceWithReleaseSLA("6Server", "Self-support", true, true);
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - exit.Code");
		// TODO - make output assertions
		// TODO - check also that it gets automatically subscribed.
		
//		
//		
//		res = KatelloUtils.sshOnClient("rpm -qa | grep yum-utils");
//		int exitCode = res.getExitCode().intValue();
//		if(exitCode == 1)
//		{
//			res = KatelloUtils.sshOnClient("yum install -y yum-utils");
//			Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
//
//		}
//		res = KatelloUtils.sshOnClient("yum-config-manager --enable beaker-HighAvailability beaker-LoadBalancer beaker-ResilientStorage beaker-ScalableFileSystem beaker-Server beaker-debuginfo beaker-harness beaker-optional beaker-tasks");
//		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
//		res = KatelloUtils.sshOnClient("subscription-manager register --user admin --password admin --org "+ this.org +" --environment "+this.envTesting +" --force");
//		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
//		res = KatelloUtils.sshOnClient("yum repolist");
//		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
//		res = KatelloUtils.sshOnClient("yum install -y zsh");
//		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
//
	}
	
	@AfterClass(description="cleanup the stuff", alwaysRun=true)
	public void tearDown(){
		// TODO - enable org.delete();
//		res = new KatelloOrg(org,null).delete();
//		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - exit.Code");
	}
}
