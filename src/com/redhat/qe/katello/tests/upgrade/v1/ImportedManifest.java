package com.redhat.qe.katello.tests.upgrade.v1;

import java.io.File;
import java.util.logging.Logger;

import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.katello.base.obj.KatelloChangeset;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloErrata;
import com.redhat.qe.katello.base.obj.KatelloGpgKey;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.common.KatelloConstants;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SCPTools;
import com.redhat.qe.tools.SSHCommandResult;

public class ImportedManifest implements KatelloConstants {
	
	protected static Logger log = Logger.getLogger(ImportedManifest.class.getName());
	
	String _provider;
	String _product;
	String _repo;
	String _env_1;
	String _env_2;
	String _env_3;
	String _changeset_1;
	String _changeset_2;
	String _changeset_3;
	String _perm_1;
	String _perm_2;
	String _perm_3;
	String _system;
	String _org;

	@Test(description="init object unique names", 
			groups={TNG_PRE_UPGRADE})
	public void init(){
		String _uid = KatelloUtils.getUniqueID();
		_org = "torg"+_uid;
		_env_1 = "Dev_" + _uid;
		_env_2 = "QA_" + _uid;
		_env_3 = "GA_" + _uid;
		_changeset_1 = "toDev_" + _uid;
		_changeset_2 = "toQA_" + _uid;
		_changeset_3 = "toGA_" + _uid;
		_perm_1 = "Perm1_" + _uid;
		_perm_2 = "Perm2_" + _uid;
		_perm_3 = "Perm3_" + _uid;
		_system = "Dakar_" + _uid;
	}
	
	@Test(description="prepare all test data, orgs environments and repos", 
			dependsOnMethods={"init"}, 
			groups={TNG_PRE_UPGRADE})
	public void createOrgsAndSyncRepo(){
		KatelloUtils.sshOnClient(KatelloSystem.RHSM_CLEAN);
		KatelloUtils.sshOnClient("rpm -e "+KatelloGpgKey.GPG_PUBKEY_RPM_ZOO+" || true");
		
		SSHCommandResult res;
				
        //this org contains imported manifest with RHEL repo promoted to environments
        // DO NOT DELETE THIS ORG IN TEAR_DOWN
		KatelloOrg org = new KatelloOrg(_org, null);
		KatelloEnvironment env1 = new KatelloEnvironment(_env_1, null, _org, KatelloEnvironment.LIBRARY);
		KatelloEnvironment env2 = new KatelloEnvironment(_env_2, null, _org, _env_1);
		KatelloEnvironment env3 = new KatelloEnvironment(_env_3, null, _org, _env_2);
		
		org.cli_create();
		env1.cli_create();
		env2.cli_create();
		env3.cli_create();
		
		SCPTools scp = new SCPTools(
				System.getProperty("katello.client.hostname", "localhost"), 
				System.getProperty("katello.client.ssh.user", "root"), 
				System.getProperty("katello.client.sshkey.private", ".ssh/id_hudson_dsa"), 
				System.getProperty("katello.client.sshkey.passphrase", "null"));
		Assert.assertTrue(scp.sendFile("data"+File.separator+"export.zip", "/tmp"),
				"export.zip sent successfully");			

		KatelloProvider prov = new KatelloProvider(KatelloProvider.PROVIDER_REDHAT, _org, null, null);
		res = prov.import_manifest("/tmp"+File.separator+"export.zip", new Boolean(true));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (provider import_manifest)");
		
		KatelloRepo repo = new KatelloRepo(KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT, _org, KatelloProduct.RHEL_SERVER, null, null, null);
		res = repo.enable();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo enable)");

		res = repo.synchronize();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo sync)");
		
		KatelloChangeset cs1 = new KatelloChangeset(_changeset_1, _org, _env_1);
		KatelloChangeset cs2 = new KatelloChangeset(_changeset_2, _org, _env_2);
		KatelloChangeset cs3 = new KatelloChangeset(_changeset_3, _org, _env_3);
		cs1.create();
		cs2.create();
		cs3.create();
		
		res = cs1.update_addProduct(KatelloProduct.RHEL_SERVER);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (changeset add_product)");
		res = cs1.promote();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (changeset apply)");
		
		res = cs2.update_addProduct(KatelloProduct.RHEL_SERVER);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (changeset add_product)");
		res = cs2.promote();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (changeset apply)");
		
		res = cs3.update_addProduct(KatelloProduct.RHEL_SERVER);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (changeset add_product)");
		res = cs3.promote();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (changeset apply)");

		KatelloSystem sys4 = new KatelloSystem(_system, _org, _env_3);
        KatelloUtils.sshOnClient(KatelloSystem.RHSM_CLEAN);
        sys4.rhsm_registerForce();
        
		String pool = KatelloCli.grepCLIOutput("Pool Id",
				KatelloUtils.sshOnClient("subscription-manager list --available --all | sed  -e 's/^ \\{1,\\}//'").getStdout().trim(),1);
		Assert.assertNotNull(pool);
		sys4.rhsm_subscribe(pool);
		
		KatelloUtils.sshOnClient("service goferd restart;");
	}
	
	@Test(description="verify orgs survived the upgrade", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE})
	public void checkOrgsSurvived(){
		KatelloOrg org = new KatelloOrg(_org, null);
		SSHCommandResult res = org.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (org info)");
	}

	@Test(description="verify environments survived the upgrade", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE})
	public void checkEnvironmentsSurvived(){
		KatelloEnvironment env = new KatelloEnvironment(_env_1, null, _org, KatelloEnvironment.LIBRARY);
		SSHCommandResult res = env.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (env info)");
		
		env = new KatelloEnvironment(_env_2, null, _org, KatelloEnvironment.LIBRARY);
		res = env.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (env info)");
		
		env = new KatelloEnvironment(_env_3, null, _org, KatelloEnvironment.LIBRARY);
		res = env.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (env info)");
	}

	@Test(description="verify providers survived the upgrade", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE})
	public void checkProvidersSurvived() {
		KatelloProvider provider = new KatelloProvider(KatelloProvider.PROVIDER_REDHAT, _org, null, null);
		SSHCommandResult res = provider.info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (provider info)");
	}

	@Test(description="verify products survived the upgrade", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE})
	public void checkProductsSurvived() {
		KatelloProduct product = new KatelloProduct(null, _org, KatelloProvider.PROVIDER_REDHAT, null, null, null, null, null);
		SSHCommandResult res = product.cli_list();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (product list)");
		Assert.assertTrue(res.getStdout().trim().contains(KatelloProduct.RHEL_SERVER), "Check - locale");
	}

	@Test(description="verify repos survived the upgrade", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE})
	public void checkReposSurvived() {
		KatelloRepo repo = new KatelloRepo(KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT, _org, KatelloProduct.RHEL_SERVER, null, null, null);
		SSHCommandResult res = repo.info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (repo info)");
	}
	
	@Test(description="verify systems survived the upgrade", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE})
	public void checkSystemsSurvived() {
		KatelloSystem sys = new KatelloSystem(_system, _org, _env_3);
		SSHCommandResult res = sys.info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
	}

	@Test(description="verify that still is possible to create new objects after upgrade", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE})
	public void checkCreateNewSystem() {
		String uid = KatelloUtils.getUniqueID();

		KatelloSystem newsystem = new KatelloSystem("new" + uid, _org, _env_3);
		KatelloUtils.sshOnClient(KatelloSystem.RHSM_CLEAN);
		newsystem.rhsm_registerForce();
		SSHCommandResult res = newsystem.info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
	}

	@Test(description="verify after upgrade it is possible to edit existing content", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE})
	public void checkEditExisting() {
		KatelloOrg org = new KatelloOrg(_org, null);
		SSHCommandResult res = org.update("new description");
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (org update)");
	}
	
	@Test(description="verify after upgrade errata list is survived", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE})
	public void checkErrataSurvived() {
		KatelloErrata errata = new KatelloErrata(null, _org, KatelloProduct.RHEL_SERVER, KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT, _env_3);
		SSHCommandResult res = errata.cli_list();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(res.getStdout().trim().contains("RHBA-2012:1312")); //telnet errata
	}
}
