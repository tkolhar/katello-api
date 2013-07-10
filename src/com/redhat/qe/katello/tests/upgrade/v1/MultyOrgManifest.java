package com.redhat.qe.katello.tests.upgrade.v1;

import java.io.File;
import java.util.logging.Logger;

import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloErrata;
import com.redhat.qe.katello.base.obj.KatelloGpgKey;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloPackage;
import com.redhat.qe.katello.base.obj.KatelloPermission;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.base.obj.KatelloUser;
import com.redhat.qe.katello.base.obj.KatelloUserRole;
import com.redhat.qe.katello.common.KatelloConstants;
import com.redhat.qe.katello.common.KatelloUtils;

import com.redhat.qe.tools.SSHCommandResult;

public class MultyOrgManifest implements KatelloConstants {
	
	protected static Logger log = Logger.getLogger(MultyOrgManifest.class.getName());

	String _org1;
	String _org2;
	String _org3;
	String _org4;
	String _provider1;
	String _product1_1;
	String _product1_2;
	String _repo1_1;
	String _repo1_2;
	String _env1_1;
	String _env1_2;
	String _env1_3;
	String _perm1_1;
	String _perm1_2;
	String _perm1_3;	
	String _system1;
	
	String _provider2;
	String _product2_1;
	String _product2_2;
	String _repo2_1;
	String _repo2_2;
	String _env2_1;
	String _env2_2;
	String _env2_3;
	String _perm2_1;
	String _perm2_2;
	String _perm2_3;;
	String _system2;
	
	String _provider3;
	String _product3_1;
	String _product3_2;
	String _repo3_1;
	String _repo3_2;
	String _env3_1;
	String _env3_2;
	String _env3_3;
	String _env3_4;
	String _env3_5;
	String _env3_6;
	String _perm3_1;
	String _perm3_2;
	String _perm3_3;
	String _perm3_4;
	String _perm3_5;
	String _perm3_6;
	String _system3;
	
	String _provider4;
	String _product4;
	String _repo4;
	String _env4_1;
	String _env4_2;
	String _env4_3;
	String _perm4_1;
	String _perm4_2;
	String _perm4_3;
	String _system4;

	String _user1;
	String _user2;
	String _user3;
	
	String _role1;
	String _role2;
	String _role3;
	final String _perm_actions = "update_systems,read_contents,read_systems,register_systems,delete_systems";
	
	String _gpg_key;

	@Test(description="init object unique names", 
			groups={TNG_PRE_UPGRADE})
	public void init(){
		// setup 4 different organizations with repos promoted to environments
		String _uid = KatelloUtils.getUniqueID();
		_org1 = "Tokyo_"+_uid;
		_provider1 = "Prov_Tokyo_"+_uid;
		_product1_1 = "Prod1_Tokyo_"+_uid;
		_product1_2 = "Prod2_Tokyo_"+_uid;
		_repo1_1 = "Repo1_Tokyo_"+_uid;
		_repo1_2 = "Repo2_Tokyo_"+_uid;
		_env1_1 = "Dev_" + _uid;
		_env1_2 = "QA_" + _uid;
		_env1_3 = "Release_" + _uid;
		_perm1_1 = "Perm1_" + _uid;
		_perm1_2 = "Perm2_" + _uid;
		_perm1_3 = "Perm3_" + _uid;
		_system1 = "Tokyo_" + _uid;

		_uid = KatelloUtils.getUniqueID();
		_org2 = "SanPaulo_"+_uid;
		_provider2 = "Prov_SanPaulo_"+_uid;
		_product2_1 = "Prod1_SanPaulo_"+_uid;
		_product2_2 = "Prod2_SanPaulo_"+_uid;
		_repo2_1 = "Repo1_SanPaulo_"+_uid;
		_repo2_2 = "Repo2_SanPaulo_"+_uid;
		_env2_1 = "Desenvolvimento_" + _uid;
		_env2_2 = "ControleQualidade_" + _uid;
		_env2_3 = "Final_" + _uid;
		_perm2_1 = "Perm1_" + _uid;
		_perm2_2 = "Perm2_" + _uid;
		_perm2_3 = "Perm3_" + _uid;
		_system2 = "SanPaulo"+ _uid;
		
		_uid = KatelloUtils.getUniqueID();
		_org3 = "Paris_"+_uid;
		_provider3 = "Prov_Paris_"+_uid;
		_product3_1 = "Prod1_Paris_"+_uid;
		_product3_2 = "Prod2_Paris_"+_uid;
		_repo3_1 = "Repo1_Paris_"+_uid;
		_repo3_2 = "Repo2_Paris_"+_uid;
		_env3_1 = "Dev1_" + _uid;
		_env3_2 = "QA1_" + _uid;
		_env3_3 = "Release1 and Library_" + _uid;
		_env3_4 = "Dev2_" + _uid;
		_env3_5 = "QA2_" + _uid;
		_env3_6 = "Release2_" + _uid;
		_perm3_1 = "Perm1_" + _uid;
		_perm3_2 = "Perm2_" + _uid;
		_perm3_3 = "Perm3_" + _uid;
		_perm3_4 = "Perm4_" + _uid;
		_perm3_5 = "Perm5_" + _uid;
		_perm3_6 = "Perm6_" + _uid;
		_system3 = "Paris_" + _uid;
		
		_uid = KatelloUtils.getUniqueID();
		_org4 = "torg"+_uid;
		_env4_1 = "Dev_" + _uid;
		_env4_2 = "QA_" + _uid;
		_env4_3 = "GA_" + _uid;
		_perm4_1 = "Perm1_" + _uid;
		_perm4_2 = "Perm2_" + _uid;
		_perm4_3 = "Perm3_" + _uid;
		_system4 = "Dakar_" + _uid;
		
		_user1 = "Akihito_" + KatelloUtils.getUniqueID();
		_user2 = "Dilma_Rousseff_" + KatelloUtils.getUniqueID();
		_user3 = "Ollanta_Humala_" + KatelloUtils.getUniqueID();
		
		_role1 = "Role1_" + KatelloUtils.getUniqueID();
		_role2 = "Role2_" + KatelloUtils.getUniqueID();
		_role3 = "Role3_" + KatelloUtils.getUniqueID();
		
		_gpg_key = "gpg_zoo-" + _uid;
	}
	
	@Test(description="prepare all test data, orgs environments and repos", 
			dependsOnMethods={"init"}, 
			groups={TNG_PRE_UPGRADE})
	public void createOrgsAndSyncRepo(){
		KatelloUtils.sshOnClient(null, KatelloSystem.RHSM_CLEAN);
		KatelloUtils.sshOnClient(null, "rpm -e "+KatelloGpgKey.GPG_PUBKEY_RPM_ZOO+" || true");
		
		SSHCommandResult res;

		KatelloUser user1 = new KatelloUser(null, _user1, _user1+"@redhat.com", "redhat", false);
		user1.cli_create();
		
		KatelloUser user2 = new KatelloUser(null, _user2, _user2+"@redhat.com", "redhat", false);
		user2.cli_create();
		
		KatelloUser user3 = new KatelloUser(null, _user3, _user3+"@redhat.com", "redhat", false);
		user3.cli_create();
		
		KatelloUserRole role1 = new KatelloUserRole(null, _role1, null);
		res = role1.create();
        Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (user_role create)");
                
        KatelloUserRole role2 = new KatelloUserRole(null, _role2, null);
		res = role2.create();
        Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (user_role create)");
        
        KatelloUserRole role3 = new KatelloUserRole(null, _role3, null);
		res = role3.create();
        Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (user_role create)");
        
        user1.assign_role(role1.name);
        user2.assign_role(role2.name);
        user1.assign_role(role2.name);
        user2.assign_role(role1.name);
        user3.assign_role(role3.name);
        
		KatelloOrg org = new KatelloOrg(null, _org1, null);
		KatelloProvider provider = new KatelloProvider(null, _provider1, _org1, null, null);
		KatelloProduct product1 = new KatelloProduct(null, _product1_1, _org1, _provider1, null, null, null, null, null);
		KatelloRepo repo1 = new KatelloRepo(null, _repo1_1, _org1, _product1_1, REPO_INECAS_ZOO3, null, null);		
		KatelloProduct product2 = new KatelloProduct(null, _product1_2, _org1, _provider1, null, null, null, null, null);
		KatelloRepo repo2 = new KatelloRepo(null, _repo1_2, _org1, _product1_2, PULP_RHEL6_x86_64_REPO, null, null);
		KatelloEnvironment env1 = new KatelloEnvironment(null, _env1_1, null, _org1, KatelloEnvironment.LIBRARY);
		KatelloEnvironment env2 = new KatelloEnvironment(null, _env1_2, null, _org1, _env1_1);
		KatelloEnvironment env3 = new KatelloEnvironment(null, _env1_3, null, _org1, _env1_2);    	
		KatelloPermission perm1 = new KatelloPermission(null, _perm1_1, _org1, "environments", _env1_1, _perm_actions, _role1);
		KatelloPermission perm2 = new KatelloPermission(null, _perm1_2, _org1, "environments", _env1_2, _perm_actions, _role2);
		KatelloPermission perm3 = new KatelloPermission(null, _perm1_3, _org1, "environments", _env1_3, _perm_actions, _role3);
		KatelloSystem sys1 = new KatelloSystem(null, _system1, _org1, _env1_3);
		org.cli_create();
		provider.create();
		product1.create();
		product2.create();
		env1.cli_create();
		env2.cli_create();
		env3.cli_create();
		repo1.create(); 
		repo2.create();
		repo1.synchronize();
		repo2.synchronize();
		KatelloUtils.promoteProductsToEnvironments(null, _org1, new String [] {_product1_1, _product1_2}, new String[] {_env1_1, _env1_2, _env1_3});
        perm1.create();
        perm2.create();
        perm3.create();
        KatelloUtils.sshOnClient(null, KatelloSystem.RHSM_CLEAN);
        sys1.rhsm_registerForce();
        
		org = new KatelloOrg(null, _org2, null);
		provider = new KatelloProvider(null, _provider2, _org2, null, null);
		product1 = new KatelloProduct(null, _product2_1, _org2, _provider2, null, null, null, null, null);
		repo1 = new KatelloRepo(null, _repo2_1, _org2, _product2_1, REPO_INECAS_ZOO3, null, null);		
		product2 = new KatelloProduct(null, _product2_2, _org2, _provider2, null, null, null, null, null);
		repo2 = new KatelloRepo(null, _repo2_2, _org2, _product2_2, PULP_RHEL6_x86_64_REPO, null, null);
		env1 = new KatelloEnvironment(null, _env2_1, null, _org2, KatelloEnvironment.LIBRARY);
		env2 = new KatelloEnvironment(null, _env2_2, null, _org2, _env2_1);
		env3 = new KatelloEnvironment(null, _env2_3, null, _org2, _env2_2);
		perm1 = new KatelloPermission(null, _perm2_1, _org2, "environments", _env2_1, _perm_actions, _role1);
		perm2 = new KatelloPermission(null, _perm2_2, _org2, "environments", _env2_2, _perm_actions, _role2);
		perm3 = new KatelloPermission(null, _perm2_3, _org2, "environments", _env2_3, _perm_actions, _role3);
		KatelloSystem sys2 = new KatelloSystem(null, _system2, _org2, _env2_3);
		org.cli_create();
		provider.create();
		product1.create();
		product2.create();
		env1.cli_create();
		env2.cli_create();
		env3.cli_create();
		repo1.create(); 
		repo2.create();
		repo1.synchronize();
		repo2.synchronize();
		KatelloUtils.promoteProductsToEnvironments(null, _org2, new String [] {_product2_1, _product2_2}, new String[] {_env2_1, _env2_2, _env2_3});
		perm1.create();
        perm2.create();
        perm3.create();
        KatelloUtils.sshOnClient(null, KatelloSystem.RHSM_CLEAN);
        sys2.rhsm_registerForce();
		
		org = new KatelloOrg(null, _org3, null);
		provider = new KatelloProvider(null, _provider3, _org3, null, null);
		product1 = new KatelloProduct(null, _product3_1, _org3, _provider3, null, null, null, null, null);
		repo1 = new KatelloRepo(null, _repo3_1, _org3, _product3_1, REPO_INECAS_ZOO3, null, null);		
		product2 = new KatelloProduct(null, _product3_2, _org3, _provider3, null, null, null, null, null);
		repo2 = new KatelloRepo(null, _repo3_2, _org3, _product3_2, PULP_RHEL6_x86_64_REPO, null, null);
		env1 = new KatelloEnvironment(null, _env3_1, null, _org3, KatelloEnvironment.LIBRARY);
		env2 = new KatelloEnvironment(null, _env3_2, null, _org3, _env3_1);
		env3 = new KatelloEnvironment(null, _env3_3, null, _org3, _env3_2);
		KatelloEnvironment env4 = new KatelloEnvironment(null, _env3_4, null, _org3, _env3_3);
		KatelloEnvironment env5 = new KatelloEnvironment(null, _env3_5, null, _org3, _env3_4);
		KatelloEnvironment env6 = new KatelloEnvironment(null, _env3_6, null, _org3, _env3_5);
		perm1 = new KatelloPermission(null, _perm3_1, _org3, "environments", _env3_1, _perm_actions, _role1);
		perm2 = new KatelloPermission(null, _perm3_2, _org3, "environments", _env3_2, _perm_actions, _role2);
		perm3 = new KatelloPermission(null, _perm3_3, _org3, "environments", _env3_3, _perm_actions, _role3);
		KatelloPermission perm4 = new KatelloPermission(null, _perm3_4, _org3, "environments", _env3_4, _perm_actions, _role1);
		KatelloPermission perm5 = new KatelloPermission(null, _perm3_5, _org3, "environments", _env3_5, _perm_actions, _role2);
		KatelloPermission perm6 = new KatelloPermission(null, _perm3_6, _org3, "environments", _env3_6, _perm_actions, _role3);
		KatelloSystem sys3 = new KatelloSystem(null, _system3, _org3, _env3_3);
		org.cli_create();
		provider.create();
		product1.create();
		product2.create();
		env1.cli_create();
		env2.cli_create();
		env3.cli_create();
		env4.cli_create();
		env5.cli_create();
		env6.cli_create();
		repo1.create(); 
		repo2.create();
		repo1.synchronize();
		repo2.synchronize();
		KatelloUtils.promoteProductsToEnvironments(null, _org3, new String [] {_product3_1, _product3_2}, 
				new String[] {_env3_1, _env3_2, _env3_3, _env3_4, _env3_5, _env3_6});
		perm1.create();
        perm2.create();
        perm3.create();
		perm4.create();
        perm5.create();
        perm6.create();
        KatelloUtils.sshOnClient(null, KatelloSystem.RHSM_CLEAN);
        sys3.rhsm_registerForce();
		
        //this org contains imported manifest with RHEL repo promoted to environments
        // DO NOT DELETE THIS ORG IN TEAR_DOWN
		org = new KatelloOrg(null, _org4, null);
		env1 = new KatelloEnvironment(null, _env4_1, null, _org4, KatelloEnvironment.LIBRARY);
		env2 = new KatelloEnvironment(null, _env4_2, null, _org4, _env4_1);
		env3 = new KatelloEnvironment(null, _env4_3, null, _org4, _env4_2);
		perm1 = new KatelloPermission(null, _perm4_1, _org4, "environments", _env4_1, _perm_actions, _role1);
		perm2 = new KatelloPermission(null, _perm4_2, _org4, "environments", _env4_2, _perm_actions, _role2);
		perm3 = new KatelloPermission(null, _perm4_3, _org4, "environments", _env4_3, _perm_actions, _role3);
		
		org.cli_create();
		env1.cli_create();
		env2.cli_create();
		env3.cli_create();
		perm1.create();
        perm2.create();
        perm3.create();
		
		KatelloUtils.scpOnClient(null, "data/export.zip", "/tmp");

		KatelloProvider prov = new KatelloProvider(null, KatelloProvider.PROVIDER_REDHAT, _org4, null, null);
		res = prov.import_manifest("/tmp"+File.separator+"export.zip", new Boolean(true));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (provider import_manifest)");
		
		KatelloRepo repo = new KatelloRepo(null, KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT, _org4, KatelloProduct.RHEL_SERVER, null, null, null);
		res = repo.enable();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo enable)");

		res = repo.synchronize();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo sync)");

		KatelloUtils.promoteProductsToEnvironments(null, _org4, new String [] {KatelloProduct.RHEL_SERVER}, 
				new String[] {_env4_1, _env4_2, _env4_3});

		KatelloSystem sys4 = new KatelloSystem(null, _system4, _org4, _env4_3);
        KatelloUtils.sshOnClient(null, KatelloSystem.RHSM_CLEAN);
        sys4.rhsm_registerForce();
        
		String pool = KatelloUtils.grepCLIOutput("Pool Id",
				KatelloUtils.sshOnClient(null, "subscription-manager list --available --all | sed  -e 's/^ \\{1,\\}//'").getStdout().trim(),1);
		Assert.assertNotNull(pool);
		sys4.rhsm_subscribe(pool);
		
		KatelloUtils.sshOnClient(null, "service goferd restart;");
	}
	
	@Test(description="verify orgs survived the upgrade", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE})
	public void checkOrgsSurvived(){
		KatelloOrg org = new KatelloOrg(null, _org1, null);
		SSHCommandResult res = org.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (org info)");
		
		org = new KatelloOrg(null, _org2, null);
		res = org.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (org info)");
		
		org = new KatelloOrg(null, _org3, null);
		res = org.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (org info)");
		
		org = new KatelloOrg(null, _org4, null);
		res = org.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (org info)");
	}

	@Test(description="verify environments survived the upgrade", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE})
	public void checkEnvironmentsSurvived(){
		KatelloEnvironment env = new KatelloEnvironment(null, _env1_1, null, _org1, KatelloEnvironment.LIBRARY);
		SSHCommandResult res = env.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (env info)");

		env = new KatelloEnvironment(null, _env1_2, null, _org1, KatelloEnvironment.LIBRARY);
		res = env.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (env info)");
		
		env = new KatelloEnvironment(null, _env1_3, null, _org1, KatelloEnvironment.LIBRARY);
		res = env.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (env info)");
		
		env = new KatelloEnvironment(null, _env2_1, null, _org2, KatelloEnvironment.LIBRARY);
		res = env.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (env info)");
		
		env = new KatelloEnvironment(null, _env2_2, null, _org2, KatelloEnvironment.LIBRARY);
		res = env.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (env info)");
		
		env = new KatelloEnvironment(null, _env2_3, null, _org2, KatelloEnvironment.LIBRARY);
		res = env.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (env info)");
		
		env = new KatelloEnvironment(null, _env3_1, null, _org3, KatelloEnvironment.LIBRARY);
		res = env.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (env info)");
		
		env = new KatelloEnvironment(null, _env3_2, null, _org3, KatelloEnvironment.LIBRARY);
		res = env.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (env info)");
		
		env = new KatelloEnvironment(null, _env3_3, null, _org3, KatelloEnvironment.LIBRARY);
		res = env.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (env info)");
		
		env = new KatelloEnvironment(null, _env3_4, null, _org3, KatelloEnvironment.LIBRARY);
		res = env.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (env info)");
		
		env = new KatelloEnvironment(null, _env3_5, null, _org3, KatelloEnvironment.LIBRARY);
		res = env.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (env info)");
		
		env = new KatelloEnvironment(null, _env3_6, null, _org3, KatelloEnvironment.LIBRARY);
		res = env.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (env info)");
		
		env = new KatelloEnvironment(null, _env4_1, null, _org4, KatelloEnvironment.LIBRARY);
		res = env.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (env info)");
		
		env = new KatelloEnvironment(null, _env4_2, null, _org4, KatelloEnvironment.LIBRARY);
		res = env.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (env info)");
		
		env = new KatelloEnvironment(null, _env4_3, null, _org4, KatelloEnvironment.LIBRARY);
		res = env.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (env info)");
	}
	
	@Test(description="verify roles survived the upgrade", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE})
	public void checkRolesSurvived(){
		KatelloUserRole role1 = new KatelloUserRole(null, _role1, null);
		SSHCommandResult res = role1.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (role info)");
		
		role1 = new KatelloUserRole(null, _role2, null);
		res = role1.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (role info)");
		
		role1 = new KatelloUserRole(null, _role3, null);
		res = role1.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (role info)");
	}

	@Test(description="verify permissions survived the upgrade", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE})
	public void checkPermissionsSurvived() {
		KatelloPermission perm = new KatelloPermission(null, null, null, null, null, null, _role1);
		SSHCommandResult res = perm.list();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (role info)");
		
		Assert.assertTrue(res.getStdout().trim().contains(_perm1_1), "Check - permission is in list");
		Assert.assertTrue(res.getStdout().trim().contains(_perm2_1), "Check - permission is in list");
		Assert.assertTrue(res.getStdout().trim().contains(_perm3_1), "Check - permission is in list");
		Assert.assertTrue(res.getStdout().trim().contains(_perm3_4), "Check - permission is in list");
		Assert.assertTrue(res.getStdout().trim().contains(_perm4_1), "Check - permission is in list");
		
		perm = new KatelloPermission(null, null, null, null, null, null, _role2);
		res = perm.list();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (role info)");
		
		Assert.assertTrue(res.getStdout().trim().contains(_perm1_2), "Check - permission is in list");
		Assert.assertTrue(res.getStdout().trim().contains(_perm2_2), "Check - permission is in list");
		Assert.assertTrue(res.getStdout().trim().contains(_perm3_2), "Check - permission is in list");
		Assert.assertTrue(res.getStdout().trim().contains(_perm3_5), "Check - permission is in list");
		Assert.assertTrue(res.getStdout().trim().contains(_perm4_2), "Check - permission is in list");
		
		perm = new KatelloPermission(null, null, null, null, null, null, _role3);
		res = perm.list();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (role info)");
		
		Assert.assertTrue(res.getStdout().trim().contains(_perm1_3), "Check - permission is in list");
		Assert.assertTrue(res.getStdout().trim().contains(_perm2_3), "Check - permission is in list");
		Assert.assertTrue(res.getStdout().trim().contains(_perm3_3), "Check - permission is in list");
		Assert.assertTrue(res.getStdout().trim().contains(_perm3_6), "Check - permission is in list");
		Assert.assertTrue(res.getStdout().trim().contains(_perm4_3), "Check - permission is in list");
	}


	@Test(description="verify users and user's roles survived the upgrade", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE})
	public void checkUsersSurvived() {
		KatelloUser user = new KatelloUser(null, _user1, null, null, false);
		SSHCommandResult res = user.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (user info)");
		
		res = user.list_roles();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (user role list)");
		Assert.assertTrue(res.getStdout().trim().contains(_role1), "Check - locale");
		Assert.assertTrue(res.getStdout().trim().contains(_role2), "Check - locale");
		Assert.assertFalse(res.getStdout().trim().contains(_role3), "Check - locale");
		
		user = new KatelloUser(null, _user2, null, null, false);
		res = user.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (user info)");
		
		res = user.list_roles();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (user role list)");
		Assert.assertTrue(res.getStdout().trim().contains(_role1), "Check - locale");
		Assert.assertTrue(res.getStdout().trim().contains(_role2), "Check - locale");
		Assert.assertFalse(res.getStdout().trim().contains(_role3), "Check - locale");
		
		user = new KatelloUser(null, _user3, null, null, false);
		res = user.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (user info)");
		
		res = user.list_roles();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (user role list)");
		Assert.assertFalse(res.getStdout().trim().contains(_role1), "Check - locale");
		Assert.assertFalse(res.getStdout().trim().contains(_role2), "Check - locale");
		Assert.assertTrue(res.getStdout().trim().contains(_role3), "Check - locale");
	}

	@Test(description="verify providers survived the upgrade", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE})
	public void checkProvidersSurvived() {
		KatelloProvider provider = new KatelloProvider(null, _provider1, _org1, null, null);
		SSHCommandResult res = provider.info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (provider info)");
		
		provider = new KatelloProvider(null, _provider2, _org2, null, null);
		res = provider.info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (provider info)");
		
		provider = new KatelloProvider(null, _provider3, _org3, null, null);
		res = provider.info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (provider info)");
		
		provider = new KatelloProvider(null, KatelloProvider.PROVIDER_REDHAT, _org4, null, null);
		res = provider.info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (provider info)");
	}

	@Test(description="verify products survived the upgrade", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE})
	public void checkProductsSurvived() {
		KatelloProduct product = new KatelloProduct(null, null, _org1, _provider1, null, null, null, null, null);
		SSHCommandResult res = product.cli_list();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (product list)");
		Assert.assertTrue(res.getStdout().trim().contains(_product1_1), "Check - locale");
		Assert.assertTrue(res.getStdout().trim().contains(_product1_2), "Check - locale");
		
		product = new KatelloProduct(null, null, _org2, _provider2, null, null, null, null, null);
		res = product.cli_list();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (product list)");
		Assert.assertTrue(res.getStdout().trim().contains(_product2_1), "Check - locale");
		Assert.assertTrue(res.getStdout().trim().contains(_product2_2), "Check - locale");
		
		product = new KatelloProduct(null, null, _org3, _provider3, null, null, null, null, null);
		res = product.cli_list();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (product list)");
		Assert.assertTrue(res.getStdout().trim().contains(_product3_1), "Check - locale");
		Assert.assertTrue(res.getStdout().trim().contains(_product3_2), "Check - locale");
		
		product = new KatelloProduct(null, null, _org4, KatelloProvider.PROVIDER_REDHAT, null, null, null, null, null);
		res = product.cli_list();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (product list)");
		Assert.assertTrue(res.getStdout().trim().contains(KatelloProduct.RHEL_SERVER), "Check - locale");
	}

	@Test(description="verify repos survived the upgrade", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE})
	public void checkReposSurvived() {
		KatelloRepo repo = new KatelloRepo(null, _repo1_1, _org1, _product1_1, null, null, null);
		SSHCommandResult res = repo.info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (repo info)");
		
		repo = new KatelloRepo(null, _repo1_2, _org1, _product1_2, null, null, null);
		res = repo.info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (repo info)");
		
		repo = new KatelloRepo(null, _repo2_1, _org2, _product2_1, null, null, null);
		res = repo.info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (repo info)");

		repo = new KatelloRepo(null, _repo2_2, _org2, _product2_2, null, null, null);
		res = repo.info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (repo info)");
		
		repo = new KatelloRepo(null, _repo3_1, _org3, _product3_1, null, null, null);
		res = repo.info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (repo info)");
		
		repo = new KatelloRepo(null, _repo3_2, _org3, _product3_2, null, null, null);
		res = repo.info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (repo info)");
		
		repo = new KatelloRepo(null, KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT, _org4, KatelloProduct.RHEL_SERVER, null, null, null);
		res = repo.info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (repo info)");
	}

	@Test(description="verify packages survived the upgrade", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE})
	public void checkPackagesSurvived() {
		KatelloPackage pack = new KatelloPackage(null, null, null, _org1, _product1_1, _repo1_1, _env1_1);
		checkZooPackages(pack.cli_list());
		
		pack = new KatelloPackage(null, null, null, _org1, _product1_2, _repo1_2, _env1_1);
		checkPulpPackages(pack.cli_list());
		
		pack = new KatelloPackage(null, null, null, _org1, _product1_1, _repo1_1, _env1_2);
		checkZooPackages(pack.cli_list());
		
		pack = new KatelloPackage(null, null, null, _org1, _product1_2, _repo1_2, _env1_2);
		checkPulpPackages(pack.cli_list());
		
		pack = new KatelloPackage(null, null, null, _org1, _product1_1, _repo1_1, _env1_3);
		checkZooPackages(pack.cli_list());
		
		pack = new KatelloPackage(null, null, null, _org1, _product1_2, _repo1_2, _env1_3);
		checkPulpPackages(pack.cli_list());
		
		pack = new KatelloPackage(null, null, null, _org2, _product2_1, _repo2_1, _env2_1);
		checkZooPackages(pack.cli_list());
		
		pack = new KatelloPackage(null, null, null, _org2, _product2_2, _repo2_2, _env2_1);
		checkPulpPackages(pack.cli_list());
		
		pack = new KatelloPackage(null, null, null, _org2, _product2_1, _repo2_1, _env2_2);
		checkZooPackages(pack.cli_list());
		
		pack = new KatelloPackage(null, null, null, _org2, _product2_2, _repo2_2, _env2_2);
		checkPulpPackages(pack.cli_list());
		
		pack = new KatelloPackage(null, null, null, _org2, _product2_1, _repo2_1, _env2_3);
		checkZooPackages(pack.cli_list());
		
		pack = new KatelloPackage(null, null, null, _org2, _product2_2, _repo2_2, _env2_3);
		checkPulpPackages(pack.cli_list());
		
		pack = new KatelloPackage(null, null, null, _org3, _product3_1, _repo3_1, _env3_1);
		checkZooPackages(pack.cli_list());
		
		pack = new KatelloPackage(null, null, null, _org3, _product3_2, _repo3_2, _env3_1);
		checkPulpPackages(pack.cli_list());
		
		pack = new KatelloPackage(null, null, null, _org3, _product3_1, _repo3_1, _env3_2);
		checkZooPackages(pack.cli_list());
		
		pack = new KatelloPackage(null, null, null, _org3, _product3_2, _repo3_2, _env3_2);
		checkPulpPackages(pack.cli_list());
		
		pack = new KatelloPackage(null, null, null, _org3, _product3_1, _repo3_1, _env3_3);
		checkZooPackages(pack.cli_list());
		
		pack = new KatelloPackage(null, null, null, _org3, _product3_2, _repo3_2, _env3_3);
		checkPulpPackages(pack.cli_list());
		
		pack = new KatelloPackage(null, null, null, _org3, _product3_1, _repo3_1, _env3_4);
		checkZooPackages(pack.cli_list());
		
		pack = new KatelloPackage(null, null, null, _org3, _product3_2, _repo3_2, _env3_4);
		checkPulpPackages(pack.cli_list());
		
		pack = new KatelloPackage(null, null, null, _org3, _product3_1, _repo3_1, _env3_5);
		checkZooPackages(pack.cli_list());
		
		pack = new KatelloPackage(null, null, null, _org3, _product3_2, _repo3_2, _env3_5);
		checkPulpPackages(pack.cli_list());
		
		pack = new KatelloPackage(null, null, null, _org3, _product3_1, _repo3_1, _env3_6);
		checkZooPackages(pack.cli_list());
		
		pack = new KatelloPackage(null, null, null, _org3, _product3_2, _repo3_2, _env3_6);
		checkPulpPackages(pack.cli_list());
	}
	
	@Test(description="verify systems survived the upgrade", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE})
	public void checkSystemsSurvived() {
		KatelloSystem sys = new KatelloSystem(null, _system1, _org1, _env1_3);
		SSHCommandResult res = sys.info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		
		sys = new KatelloSystem(null, _system2, _org2, _env2_3);
		res = sys.info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		
		sys = new KatelloSystem(null, _system3, _org3, _env3_3);
		res = sys.info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		
		sys = new KatelloSystem(null, _system4, _org4, _env4_3);
		res = sys.info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
	}

	@Test(description="verify that still is possible to create new objects after upgrade", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE})
	public void checkCreateNew() {
		String uid = KatelloUtils.getUniqueID();
		
		KatelloOrg org = new KatelloOrg(null, "NewOrg" + uid, null);
		SSHCommandResult res = org.cli_create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (org create)");
		
		KatelloEnvironment env = new KatelloEnvironment(null, "NewEnv" + uid, null, org.name, KatelloEnvironment.LIBRARY);
		res = env.cli_create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (env create)");
		
		KatelloUser user = new KatelloUser(null, "newuser" + uid, "newuser" + uid + "@redhat.com", "redhat", false, "ja");
		res = user.cli_create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (user create)");
		
		KatelloUserRole role = new KatelloUserRole(null, "newrole" + uid, null);
		res = role.create();
        Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (user_role create)");
        
        KatelloPermission perm = new KatelloPermission(null, "newperm"+uid, org.name, "environments", env.getName(), _perm_actions, role.name);
        res = perm.create();
        Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (permission create)");
        
        KatelloProvider provider = new KatelloProvider(null, "newprov" + uid, org.name, null, null);
        res = provider.create();
        Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (provider create)");
        
		KatelloProduct product = new KatelloProduct(null, "newproduct"+uid, org.name, provider.name, null, null, null, null, null);
		res = product.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");
		
		KatelloRepo repo = new KatelloRepo(null, "newrepo"+uid, org.name, product.name, REPO_INECAS_ZOO3, null, null);	
		res = repo.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo create)");
		
		KatelloSystem newsystem = new KatelloSystem(null, "SãoPaulonew" + uid, _org3, _env3_3);
		KatelloUtils.sshOnClient(null, KatelloSystem.RHSM_CLEAN);
		newsystem.rhsm_registerForce();
		res = newsystem.info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
	}

	@Test(description="verify after upgrade it is possible to edit existing content", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE})
	public void checkEditExisting() {
		KatelloOrg org = new KatelloOrg(null, _org1, null);
		SSHCommandResult res = org.update("new description");
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (org update)");
	}
	
	@Test(description="verify after upgrade errata list is survived", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE})
	public void checkErrataSurvived() {
		KatelloErrata errata = new KatelloErrata(null, null, _org4, KatelloProduct.RHEL_SERVER, KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT, _env4_3);
		SSHCommandResult res = errata.cli_list();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(res.getStdout().trim().contains("RHBA-2012:1312")); //telnet errata
	}
	
	//check that pulp packages are contained in output
	private void checkPulpPackages(SSHCommandResult res) {
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		
		Assert.assertTrue(res.getStdout().trim().contains("pulp-admin"));
		Assert.assertTrue(res.getStdout().trim().contains("pulp"));
		Assert.assertTrue(res.getStdout().trim().contains("python-gofer"));
		Assert.assertTrue(res.getStdout().trim().contains("python-qpid"));
		Assert.assertTrue(res.getStdout().trim().contains("pulp-common"));
		Assert.assertTrue(res.getStdout().trim().contains("pulp-consumer"));
	}

	//check that zoo packages are contained in output
	private void checkZooPackages(SSHCommandResult res) {
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		
		Assert.assertTrue(res.getStdout().trim().contains("lion"));
		Assert.assertTrue(res.getStdout().trim().contains("zebra"));
		Assert.assertTrue(res.getStdout().trim().contains("stork"));
		Assert.assertTrue(res.getStdout().trim().contains("wolf"));
		Assert.assertTrue(res.getStdout().trim().contains("penguin"));
		Assert.assertTrue(res.getStdout().trim().contains("tiger"));
	}

}
