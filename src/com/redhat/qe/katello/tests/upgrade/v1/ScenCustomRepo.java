package com.redhat.qe.katello.tests.upgrade.v1;

import java.util.logging.Logger;

import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.katello.base.obj.KatelloChangeset;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloGpgKey;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.common.KatelloConstants;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

public class ScenCustomRepo implements KatelloConstants{
	protected static Logger log = Logger.getLogger(ScenCustomRepo.class.getName());

	String _uid = KatelloUtils.getUniqueID();
	String _org;
	String _provider;
	String _product;
	String _repo;
	String _env;
	String _changeset;
	String _system;
	String _gpg_key;
	String[] clients = null;
	
	@BeforeGroups(description="check that at there is at least one separate katello client provided to install remotelly on it", groups={TNG_PRE_UPGRADE, TNG_UPGRADE, TNG_POST_UPGRADE})
	public void checkUpgradeClients() {
		String clientsStr = System.getProperty("katello.upgrade.clients", "");
		clients = clientsStr.split(",");
		if(clientsStr.isEmpty() || clients[0].isEmpty()) {
			Assert.fail("Please specify \"katello.upgrade.clients\" with at least 1 client");
		}
	}
	
	@Test(description="init object unique names", 
			groups={TNG_PRE_UPGRADE})
	public void init(){
		_org = "upgV1-"+_uid;
		_provider = "Zoo "+_uid;
		_product = "Zoo "+_uid;
		_repo = "zoo3-"+_uid;
		_env = "env-" + _uid;
		_changeset = "chs-" + _uid;
		_system = "localhost-" + _uid;
		_gpg_key = "gpg_zoo-" + _uid;
	}
	
	@Test(description="prepare and sync the repo", 
			dependsOnMethods={"init"}, 
			groups={TNG_PRE_UPGRADE})
	public void createAndSyncRepo(){
		KatelloUtils.sshOnClient("yum -y erase wolf lion || true");
		KatelloUtils.sshOnClient(KatelloSystem.RHSM_CLEAN);
		KatelloUtils.sshOnClient("rpm -e "+KatelloGpgKey.GPG_PUBKEY_RPM_ZOO+" || true");
		
		
		KatelloOrg org = new KatelloOrg(_org, null);
		
		KatelloUtils.sshOnClient("wget "+KatelloGpgKey.REPO_GPG_FILE_ZOO+" -O /tmp/RPM-GPG-KEY-dummy-packages-generator");
		KatelloGpgKey gpg_key = new KatelloGpgKey(_gpg_key, _org, "/tmp/RPM-GPG-KEY-dummy-packages-generator");
		
		KatelloProvider provider = new KatelloProvider(_provider, _org, null, null);
		KatelloProduct product = new KatelloProduct(_product, _org, _provider, null, null, null, null, null);
		KatelloRepo repo = new KatelloRepo(_repo, _org, _product, REPO_INECAS_ZOO3, _gpg_key, null);
		
		KatelloEnvironment env = new KatelloEnvironment(_env, null, _org, KatelloEnvironment.LIBRARY);
		KatelloChangeset cs = new KatelloChangeset(_changeset, _org, _env);
    	
		org.cli_create();
		gpg_key.cli_create();
		provider.create();
		product.create();
		env.cli_create();
		repo.create(); 
		repo.synchronize();
		cs.create();
		cs.update_addProduct(_product);
		cs.promote();
		

		KatelloSystem sys = new KatelloSystem(_system, _org, _env);
		sys.setHostName(clients[0]);
		sys.rhsm_registerForce();
		String pool = KatelloCli.grepCLIOutput("PoolId", sys.subscriptions_available().getStdout().trim(),1);
		Assert.assertNotNull(pool);
		sys.rhsm_subscribe(pool);
		
		KatelloUtils.sshOnClient("service goferd restart;");
	}
	
	@Test(description="verify org survived the upgrade", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE})
	public void checkOrgSurvived(){
		KatelloOrg org = new KatelloOrg(_org, null);
		SSHCommandResult res = org.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (org info)");
	}

	@Test(description="verify repo survived the upgrade", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE})
	public void checkRepoSurvived(){
		KatelloRepo repo = new KatelloRepo(_repo, _org, _product, REPO_INECAS_ZOO3, null, null);
		SSHCommandResult res = repo.info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (org info)");
	}
	
	@Test(description="verify after the upgrade package can be installed on system", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE},
			dependsOnMethods={"checkRepoSurvived"},
			groups={TNG_POST_UPGRADE})
	public void checkPackageInstalls() {
		KatelloUtils.sshOnClient("yum clean all");
		KatelloUtils.sshOnClient("yum repolist");
		KatelloUtils.sshOnClient("rpm --import /tmp/RPM-GPG-KEY-dummy-packages-generator");
		
		KatelloSystem sys = new KatelloSystem(_system, _org, null);
		sys.setHostName(clients[0]);
		SSHCommandResult res = sys.packages_install("lion");
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (remote install lion)");
		Assert.assertTrue(res.getStdout().trim().contains(KatelloSystem.OUT_REMOTE_ACTION_DONE),
				"Check - output string (remote action finished)");
		Assert.assertTrue(res.getStdout().trim().contains("lion-"),
				"Check - output string (contains package name installed)");
		res = KatelloUtils.sshOnClient("rpm -q lion");
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (rpm -q lion)");
	}	
	
}
