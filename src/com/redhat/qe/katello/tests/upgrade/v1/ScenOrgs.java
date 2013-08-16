package com.redhat.qe.katello.tests.upgrade.v1;

import java.util.logging.Logger;
import org.testng.annotations.Test;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.obj.KatelloActivationKey;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloGpgKey;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloPermission;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.base.obj.KatelloSystemGroup;
import com.redhat.qe.katello.base.obj.KatelloUser;
import com.redhat.qe.katello.base.obj.KatelloUserRole;
import com.redhat.qe.katello.common.KatelloConstants;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups={"sam-upgrade"})
public class ScenOrgs implements KatelloConstants {
	
	protected static Logger log = Logger.getLogger(ScenOrgs.class.getName());
	
	String _env_1;
	String _env_2;
	String _env_3;
	String _perm_1;
	String _perm_2;
	String _system;
	String _system_group;
	String _org;
	String _user;
	String _akey;
	String _user_role;
	String _user_role2;
	String _neworg;
	String _newuser;
	String _newsystem;
	int _init_role_count;

	@Test(description="init object unique names", 
			groups={TNG_PRE_UPGRADE})
	public void init(){
		String _uid = KatelloUtils.getUniqueID();
		_org = "torg"+_uid;
		_user = "tuser"+_uid;
		_akey = "akey"+_uid;
		_neworg = "neworg"+_uid;
		_user_role = "role" + _uid;
		_user_role = "role2" + _uid;
		_env_1 = "Dev_" + _uid;
		_env_2 = "QA_" + _uid;
		_env_3 = "GA_" + _uid;
		_perm_1 = "Perm1_" + _uid;
		_perm_2 = "Perm2_" + _uid;
		_system = "Dakar_" + _uid;
		_system_group = "sysgroup" + _uid;
		_newuser = "newuser" + _uid;
		_newsystem = "newsystem" + _uid;
	}
	
	@Test(description="prepare all test data, org, environment, activation key and role", 
			dependsOnMethods={"init"}, 
			groups={TNG_PRE_UPGRADE})
	public void createData(){
		KatelloUtils.sshOnClient(null, KatelloSystem.RHSM_CLEAN);
		KatelloUtils.sshOnClient(null, "rpm -e "+KatelloGpgKey.GPG_PUBKEY_RPM_ZOO+" || true");

		KatelloOrg org = new KatelloOrg(null, _org, null);
		KatelloEnvironment env1 = new KatelloEnvironment(null, _env_1, null, _org, KatelloEnvironment.LIBRARY);
		KatelloEnvironment env2 = new KatelloEnvironment(null, _env_2, null, _org, _env_1);
		KatelloEnvironment env3 = new KatelloEnvironment(null, _env_3, null, _org, _env_2);
		
		SSHCommandResult res = org.cli_create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		res = env1.cli_create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		res = env2.cli_create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		res = env3.cli_create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		
		KatelloUser user = new KatelloUser(null, _user, 
				KatelloUser.DEFAULT_USER_EMAIL, KatelloUser.DEFAULT_ADMIN_PASS, false);
		res = user.cli_create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		
		KatelloActivationKey key = new KatelloActivationKey(null, 
				_org, _env_1, _akey, null, null);
		res = key.create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		
		_init_role_count = Integer.parseInt(new KatelloUserRole(null, null, null).cli_list_count().getStdout());
		
		KatelloUserRole user_role = new KatelloUserRole(null, _user_role, "User Role Created");
		res = user_role.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (user role create)");
		
		user_role = new KatelloUserRole(null, _user_role2, "User Role Created");
		res = user_role.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (user role create)");	
		
		KatelloPermission perm = new KatelloPermission(null, _perm_1, _org, "organizations", null, "update_systems,read,read_systems", _user_role);
		res = perm.create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (permition create)");
		
		perm = new KatelloPermission(null, _perm_2, _org, "organizations", null, "update_systems,read", _user_role2);
		res = perm.create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (permition create)");
		
		KatelloSystem sys = new KatelloSystem(null, _system, _org, _env_1);
		res = sys.rhsm_registerForce(_akey);
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - rhsm register (activationkey)");
	}
	
	@Test(description="verify orgs survived the upgrade", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE})
	public void checkOrgsSurvived(){
		KatelloOrg org = new KatelloOrg(null, _org, null);
		SSHCommandResult res = org.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (org info)");
	}
	
	@Test(description="verify user survived the upgrade", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE})
	public void checkUserSurvived(){
		KatelloUser user = new KatelloUser(null, _user, 
				KatelloUser.DEFAULT_USER_EMAIL, KatelloUser.DEFAULT_ADMIN_PASS, false);
		SSHCommandResult res = user.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (user info)");
	}
	
	@Test(description="verify role survived the upgrade", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE})
	public void checkRoleSurvived(){
		KatelloUserRole user_role = new KatelloUserRole(null, _user_role, "User Role Created");
		SSHCommandResult res = user_role.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (role info)");
		
		user_role = new KatelloUserRole(null, _user_role2, "User Role Created");
		res = user_role.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (role info)");
		
		int count = Integer.parseInt(user_role.cli_list_count().getStdout());
		
		Assert.assertEquals(count, _init_role_count + 2, "Count of roles should remain the same after upgrade");
	}
	
	@Test(description="verify activation key survived the upgrade", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE})
	public void checkActivationKeySurvived(){
		KatelloActivationKey key = new KatelloActivationKey(null, 
				_org, _env_1, _akey, null, null);
		SSHCommandResult res = key.info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (activation key info)");
	}
	
	@Test(description="verify permission survived the upgrade", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE})
	public void checkPermissionSurvived(){
		KatelloPermission perm = new KatelloPermission(null, null, null, null, null, null, _user_role);
		SSHCommandResult res = perm.list();
		Assert.assertTrue(res.getStdout().contains(_perm_1), "permission must be listed");
		
		perm = new KatelloPermission(null, null, null, null, null, null, _user_role2);
		res = perm.list();
		Assert.assertTrue(res.getStdout().contains(_perm_2), "permission must be listed");
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
	
	@Test(description="verify it is possible to create system group, and add systems into it", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE})
	public void checkSysGroupCreate(){
		KatelloSystemGroup systemGroup = new KatelloSystemGroup(null, _system_group, _org);
		SSHCommandResult res = systemGroup.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (system group create)");

    	KatelloSystem sys = new KatelloSystem(null, _system, _org, null);
    	res = sys.rhsm_identity();
		String system_uuid = KatelloUtils.grepCLIOutput("Current identity is", res.getStdout());
		
		res = systemGroup.add_systems(system_uuid);
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (add system)");
		
		sys = new KatelloSystem(null, _newsystem, _org, null);
		res = sys.rhsm_registerForce(_akey);
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - rhsm register (activationkey)");
		
		res = sys.rhsm_identity();
		String system_uuid2 = KatelloUtils.grepCLIOutput("Current identity is", res.getStdout());
		
		res = systemGroup.add_systems(system_uuid2);
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (add system)");
	}
}
