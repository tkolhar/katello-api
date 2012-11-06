package com.redhat.qe.katello.tests.upgrade.v1;

import java.util.logging.Logger;

import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.katello.base.obj.KatelloChangeset;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloFilter;
import com.redhat.qe.katello.base.obj.KatelloGpgKey;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.base.obj.KatelloTemplate;
import com.redhat.qe.katello.base.obj.KatelloUser;
import com.redhat.qe.katello.base.obj.KatelloUserRole;
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
	String _templ;
	String _filter;
	String _user1;
	String _role1;
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
		_templ = "templ-" + _uid;
		_filter = "filter" + _uid;
		_user1 = "user" + _uid;
		_role1 = "role" + _uid;
	}
	
	@Test(description="prepare and sync the repo", 
			dependsOnMethods={"init"}, 
			groups={TNG_PRE_UPGRADE})
	public void createAndSyncRepo(){
		KatelloUtils.sshOnClient(clients[0], "yum -y erase wolf lion || true");
		KatelloUtils.sshOnClient(clients[0], KatelloSystem.RHSM_CLEAN);
		KatelloUtils.sshOnClient(clients[0], "rpm -e "+KatelloGpgKey.GPG_PUBKEY_RPM_ZOO+" || true");
		
		
		KatelloOrg org = new KatelloOrg(_org, null);
		
		KatelloUtils.sshOnClient(clients[0], "wget "+KatelloGpgKey.REPO_GPG_FILE_ZOO+" -O /tmp/RPM-GPG-KEY-dummy-packages-generator");
		KatelloGpgKey gpg_key = new KatelloGpgKey(_gpg_key, _org, "/tmp/RPM-GPG-KEY-dummy-packages-generator");
		
		KatelloProvider provider = new KatelloProvider(_provider, _org, null, null);
		KatelloProduct product = new KatelloProduct(_product, _org, _provider, null, null, null, null, null);
		KatelloRepo repo = new KatelloRepo(_repo, _org, _product, REPO_INECAS_ZOO3, _gpg_key, null);
		
		KatelloEnvironment env = new KatelloEnvironment(_env, null, _org, KatelloEnvironment.LIBRARY);
		KatelloChangeset cs = new KatelloChangeset(_changeset, _org, _env);
		KatelloTemplate templ1 = new KatelloTemplate(_templ, null, _org, null);
		KatelloFilter filter1 = new KatelloFilter(_filter, _org, _env, "");
		KatelloUser user1 = new KatelloUser(_user1, _user1+"@redhat.com", "redhat", false);
		KatelloUserRole role1 = new KatelloUserRole(_role1, null);
		
		
		SSHCommandResult res = user1.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");		
		res = role1.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		res = user1.assign_role(role1.name);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		res = gpg_key.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		res = provider.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		res = product.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		res = env.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		res = repo.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		res = repo.synchronize();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		res = templ1.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		res = filter1.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		res = cs.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		res = cs.update_addProduct(_product);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		res = cs.promote();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");

		KatelloSystem sys = new KatelloSystem(_system, _org, _env);
		sys.runOn(clients[0]);
		sys.rhsm_registerForce();
		String pool = KatelloCli.grepCLIOutput("PoolId", sys.subscriptions_available().getStdout().trim(),1);
		Assert.assertNotNull(pool);
		sys.rhsm_subscribe(pool);
		
		KatelloUtils.sshOnClient(clients[0], "service goferd restart;");
		KatelloUtils.sshOnClient(clients[0], "rpm --import /tmp/RPM-GPG-KEY-dummy-packages-generator");
		
		remoteInstall();
	}
	
	@Test(description="verify org survived the upgrade", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE})
	public void checkOrgSurvived(){
		KatelloOrg org = new KatelloOrg(_org, null);
		SSHCommandResult res = org.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (org info)");
		
		KatelloUserRole role1 = new KatelloUserRole(_role1, null);
		res = role1.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (role info)");
		
		KatelloUser user = new KatelloUser(_user1, null, null, false);
		res = user.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (user info)");
		
		KatelloTemplate templ = new KatelloTemplate(_templ, null, _org, null);
		res = templ.info(null);
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		
		KatelloFilter filter = new KatelloFilter(_filter, _org, _env, "");
		res = filter.cli_info();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
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
		KatelloUtils.sshOnClient(clients[0], "yum clean all");
		KatelloUtils.sshOnClient(clients[0], "yum repolist");
		KatelloUtils.sshOnClient(clients[0], "rpm --import /tmp/RPM-GPG-KEY-dummy-packages-generator");
		KatelloUtils.sshOnClient(clients[0], "sed -i 's/5674/5671/g' /etc/gofer/plugins/katelloplugin.conf");
		KatelloUtils.sshOnClient(clients[0], "service goferd restart;");
		KatelloUtils.sshOnClient(clients[0], "yum -y erase wolf lion || true");
		
		remoteInstall();
	}
	
	private void remoteInstall() {
		
		KatelloSystem sys = new KatelloSystem(_system, _org, null);
		sys.runOn(clients[0]);
		SSHCommandResult res = sys.packages_install("lion");
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (remote install lion)");
		Assert.assertTrue(res.getStdout().trim().contains(KatelloSystem.OUT_REMOTE_ACTION_DONE),
				"Check - output string (remote action finished)");
		Assert.assertTrue(res.getStdout().trim().contains("lion-"),
				"Check - output string (contains package name installed)");
		res = KatelloUtils.sshOnClient(clients[0], "rpm -q lion");
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (rpm -q lion)");
	}
	
}
