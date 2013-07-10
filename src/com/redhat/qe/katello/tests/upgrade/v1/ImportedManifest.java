package com.redhat.qe.katello.tests.upgrade.v1;

import java.util.logging.Logger;
import org.testng.annotations.Test;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloGpgKey;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.base.obj.KatelloUser;
import com.redhat.qe.katello.common.KatelloConstants;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups={"sam-upgrade"})
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
	String _neworg;
	String _newuser;
	String _newsystem;

	@Test(description="init object unique names", 
			groups={TNG_PRE_UPGRADE})
	public void init(){
		String _uid = KatelloUtils.getUniqueID();
		_org = "torg"+_uid;
		_neworg = "neworg"+_uid;
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
		_newuser = "newuser" + _uid;
		_newsystem = "newsystem" + _uid;
	}
	
	@Test(description="prepare all test data, orgs environments and repos", 
			dependsOnMethods={"init"}, 
			groups={TNG_PRE_UPGRADE})
	public void createOrgsAndSyncRepo(){
		KatelloUtils.sshOnClient(null, KatelloSystem.RHSM_CLEAN);
		KatelloUtils.sshOnClient(null, "rpm -e "+KatelloGpgKey.GPG_PUBKEY_RPM_ZOO+" || true");
				
        //this org contains imported manifest with RHEL repo promoted to environments
        // DO NOT DELETE THIS ORG IN TEAR_DOWN
		KatelloOrg org = new KatelloOrg(null, _org, null);
		KatelloEnvironment env1 = new KatelloEnvironment(null, _env_1, null, _org, KatelloEnvironment.LIBRARY);
		KatelloEnvironment env2 = new KatelloEnvironment(null, _env_2, null, _org, _env_1);
		KatelloEnvironment env3 = new KatelloEnvironment(null, _env_3, null, _org, _env_2);
		
		org.cli_create();
		env1.cli_create();
		env2.cli_create();
		env3.cli_create();
		//@Owner: please, any reason why there is not a single assertion even here ??? [gkhachik]
	}
	
	@Test(description="verify orgs survived the upgrade", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE})
	public void checkOrgsSurvived(){
		KatelloOrg org = new KatelloOrg(null, _org, null);
		SSHCommandResult res = org.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (org info)");
	}

	@Test(description="verify environments survived the upgrade", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE})
	public void checkEnvironmentsSurvived(){
		KatelloEnvironment env = new KatelloEnvironment(null, _env_1, null, _org, KatelloEnvironment.LIBRARY);
		SSHCommandResult res = env.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (env info)");
		
		env = new KatelloEnvironment(null, _env_2, null, _org, KatelloEnvironment.LIBRARY);
		res = env.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (env info)");
		
		env = new KatelloEnvironment(null, _env_3, null, _org, KatelloEnvironment.LIBRARY);
		res = env.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (env info)");
	}

	@Test(description="verify it is possible to create org", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE})
	public void checkOrgCreate(){
		KatelloOrg org = new KatelloOrg(null, _neworg, null);
		SSHCommandResult res = org.cli_create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (org create)");		
	}

	@Test(description="verify it is possible to remove org", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE}, dependsOnMethods={"checkOrgCreate"})
	public void checkOrgRemove(){
		KatelloOrg org = new KatelloOrg(null, _neworg, null);
		SSHCommandResult res = org.delete();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (org remove)");		
	}
	
	@Test(description="verify it is possible to create user", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE})
	public void checkUserCreate(){
		KatelloUser user = new KatelloUser(null, _newuser, "newuser@localhost", "redhat", false);
		SSHCommandResult res = user.cli_create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (user create)");	
	}

	@Test(description="verify it is possible to remove user", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE}, dependsOnMethods={"checkUserCreate"})
	public void checkUserRemove(){
		KatelloUser user = new KatelloUser(null, _newuser, "newuser@localhost", "redhat", false);
		SSHCommandResult res = user.delete();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (user remove)");		
	}
}
