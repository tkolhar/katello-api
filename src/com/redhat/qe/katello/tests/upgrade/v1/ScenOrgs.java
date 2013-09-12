package com.redhat.qe.katello.tests.upgrade.v1;

import java.util.logging.Logger;
import org.testng.annotations.Test;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloActivationKey;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloGpgKey;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloPermission;
import com.redhat.qe.katello.base.obj.KatelloProvider;
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
	String _perm_3;
	String _system;
	String _system2;
	String _system3;
	String _system_group;
	String _org;
	String _user;
	String _akey;
	String _akey2;
	String _akey3;
	String _user_role;
	String _user_role2;
	String _user_role3;
	String _neworg;
	String _newuser;
	String _newsystem;
	String _poolRhel;
	int _init_role_count;

	@Test(description="init object unique names", 
			groups={TNG_PRE_UPGRADE})
	public void init(){
		String _uid = KatelloUtils.getUniqueID();
		_org = "torg"+_uid;
		_user = "tuser"+_uid;
		_newuser = "newuser" + _uid;
		String ldap_type = System.getProperty("ldap.server.type", "");		
		if ("posix".equals(ldap_type)) {
			_user = "omaciel";
			_newuser = "sthirugn";
		} else if ("free_ipa".equals(ldap_type) || "active_directory".equals(ldap_type)) {
			_user = "admin-user2";
			_newuser = "admin-user3";
		}
		
		_akey = "akey"+_uid;
		_akey2 = "akey2"+_uid;
		_akey3 = "akey3"+_uid;
		_neworg = "neworg"+_uid;
		_user_role = "role" + _uid;
		_user_role2 = "role2" + _uid;
		_user_role3 = "role3" + _uid;
		_env_1 = "Dev_" + _uid;
		_env_2 = "QA_" + _uid;
		_env_3 = "GA_" + _uid;
		_perm_1 = "Perm1_" + _uid;
		_perm_2 = "Perm2_" + _uid;
		_perm_3 = "Perm3_" + _uid;
		_system = "Dakar_" + _uid;
		_system2 = "Paris_" + _uid;
		_system3 = "Madrid_" + _uid;
		_system_group = "sysgroup" + _uid;
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
				KatelloUser.DEFAULT_USER_EMAIL, System.getProperty("katello.admin.password", KatelloUser.DEFAULT_ADMIN_PASS), false);
		res = user.cli_create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		
		KatelloActivationKey key = new KatelloActivationKey(null, 
				_org, _env_1, _akey, null, null);
		res = key.create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		
		key = new KatelloActivationKey(null, 
				_org, _env_2, _akey2, null, null);
		res = key.create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		
		key = new KatelloActivationKey(null, 
				_org, _env_3, _akey3, null, null);
		res = key.create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		
		_init_role_count = Integer.parseInt(new KatelloUserRole(null, null, null).cli_list_count().getStdout().trim());
		
		KatelloUserRole user_role = new KatelloUserRole(null, _user_role, "Environments");
		res = user_role.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (user role create)");
		
		user_role = new KatelloUserRole(null, _user_role2, "Activation Keys");
		res = user_role.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (user role create)");	
		
		user_role = new KatelloUserRole(null, _user_role3, "Roles");
		res = user_role.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (user role create)");	
		
		KatelloPermission perm = new KatelloPermission(null, _perm_1, _org, "environments", null, "read_contents,update_systems", _user_role);
		res = perm.create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (permition create)");
		
		perm = new KatelloPermission(null, _perm_2, _org, "activation_keys", null, "manage_all", _user_role2);
		res = perm.create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (permition create)");
		
		perm = new KatelloPermission(null, _perm_3, _org, "roles", null, "delete,read", _user_role3);
		res = perm.create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (permition create)");
		
		KatelloUtils.scpOnClient(null, "data/"+KatelloProvider.MANIFEST_12SUBSCRIPTIONS, "/tmp");

		KatelloProvider rh = new KatelloProvider(null, KatelloProvider.PROVIDER_REDHAT, _org, null, null);
		res = rh.import_manifest("/tmp/"+KatelloProvider.MANIFEST_12SUBSCRIPTIONS, null);
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - provider import_manifest");
		org = new KatelloOrg(null, _org, null);
		res = org.subscriptions();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - org subscriptions");
		// getting poolid could vary - might be need to make switch case here for different versions...
		_poolRhel = KatelloUtils.grepCLIOutput("ID", KatelloCliTestBase.sgetOutput(res));
		if (_poolRhel == null || _poolRhel.isEmpty()) {
			_poolRhel = KatelloUtils.grepCLIOutput("Id", KatelloCliTestBase.sgetOutput(res));
		}
		
		KatelloSystem sys = new KatelloSystem(null, _system, _org, _env_1);
		sys.runOn(SetupServers.client_name);
		res = sys.rhsm_registerForce(_akey);
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - rhsm register (activationkey)");
		res = sys.rhsm_subscribe(_poolRhel);
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - rhsm subscribe");
		
		sys = new KatelloSystem(null, _system2, _org, _env_2);
		sys.runOn(SetupServers.client_name2);
		res = sys.rhsm_registerForce(_akey2);
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - rhsm register (activationkey)");
		res = sys.rhsm_subscribe(_poolRhel);
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - rhsm subscribe");
		
		sys = new KatelloSystem(null, _system3, _org, _env_3);
		sys.runOn(SetupServers.client_name3);
		res = sys.rhsm_registerForce();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - rhsm register (activationkey)");
		res = sys.rhsm_subscribe(_poolRhel);
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - rhsm subscribe");
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
				null, null, false);
		SSHCommandResult res = user.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (user info)");
	}
	
	@Test(description="verify environments survived the upgrade as a created system groups", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE})
	public void checkEnvironmentsSurvived() {
		KatelloEnvironment env = new KatelloEnvironment(null, null, null, _org, KatelloEnvironment.LIBRARY);
		SSHCommandResult res = env.cli_list();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		Assert.assertFalse(res.getStdout().contains(_env_1), "Environment name is not in environment list output");
		Assert.assertFalse(res.getStdout().contains(_env_2), "Environment name is not in environment list output");
		Assert.assertFalse(res.getStdout().contains(_env_3), "Environment name is not in environment list output");
		
		KatelloSystemGroup systemGroup = new KatelloSystemGroup(null, _env_1, _org);
		res = systemGroup.info();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (system group info)");
		Assert.assertTrue(res.getStdout().contains(_env_1), "Environment name is in system group info output");
		
		systemGroup = new KatelloSystemGroup(null, _env_2, _org);
		res = systemGroup.info();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (system group info)");
		Assert.assertTrue(res.getStdout().contains(_env_2), "Environment name is in system group info output");
		
		systemGroup = new KatelloSystemGroup(null, _env_3, _org);
		res = systemGroup.info();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (system group info)");
		Assert.assertTrue(res.getStdout().contains(_env_3), "Environment name is in system group info output");
	}
	
	@Test(description="verify role survived the upgrade", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE})
	public void checkRoleSurvived(){
		KatelloUserRole user_role = new KatelloUserRole(null, _user_role, null);
		SSHCommandResult res = user_role.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (role info)");
		Assert.assertTrue(res.getStdout().contains(_user_role), "Role name is in info output");
		
		user_role = new KatelloUserRole(null, _user_role2, null);
		res = user_role.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (role info)");
		Assert.assertTrue(res.getStdout().contains(_user_role2), "Role name is in info output");
		
		user_role = new KatelloUserRole(null, _user_role3, null);
		res = user_role.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (role info)");
		Assert.assertTrue(res.getStdout().contains(_user_role3), "Role name is in info output");
		
		res = user_role.cli_list();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (role list)");
		Assert.assertTrue(res.getStdout().contains(_user_role), "Role name is in list output");
		Assert.assertTrue(res.getStdout().contains(_user_role2), "Role name is in list output");
		Assert.assertTrue(res.getStdout().contains(_user_role3), "Role name is in list output");
		
		int count = Integer.parseInt(user_role.cli_list_count().getStdout().trim());
		
		Assert.assertEquals(count, _init_role_count + 3, "Count of roles should remain the same after upgrade");
	}
	
	@Test(description="verify activation key survived the upgrade", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE})
	public void checkActivationKeySurvived(){
		KatelloActivationKey key = new KatelloActivationKey(null, 
				_org, null, _akey, null, null);
		SSHCommandResult res = key.info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (activation key info)");
		
		key = new KatelloActivationKey(null, _org, null, _akey2, null, null);
		res = key.info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (activation key info)");
		
		key = new KatelloActivationKey(null, _org, null, _akey3, null, null);
		res = key.info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (activation key info)");
	}
	
	@Test(description="verify systems survived the upgrade", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE})
	public void checkSystemsSurvived(){
		KatelloSystem sys = new KatelloSystem(null, _system, _org, null);
		SSHCommandResult res = sys.info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (activation key info)");
		Assert.assertTrue(res.getStdout().contains(_system), "System name is in info output");
		Assert.assertTrue(res.getStdout().contains(KatelloEnvironment.LIBRARY), "Library is in info output");
		Assert.assertTrue(res.getStdout().contains(_akey), "Activation key is in info output");
		
		sys = new KatelloSystem(null, _system2, _org, null);
		res = sys.info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (activation key info)");
		Assert.assertTrue(res.getStdout().contains(_system2), "System name is in info output");
		Assert.assertTrue(res.getStdout().contains(KatelloEnvironment.LIBRARY), "Library is in info output");
		Assert.assertTrue(res.getStdout().contains(_akey2), "Activation key is in info output");
		
		sys = new KatelloSystem(null, _system2, _org, null);
		res = sys.info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (activation key info)");
		Assert.assertTrue(res.getStdout().contains(_system2), "System name is in info output");
		Assert.assertTrue(res.getStdout().contains(KatelloEnvironment.LIBRARY), "Library is in info output");
		
		res = sys.list();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (system list)");
		Assert.assertTrue(res.getStdout().contains(_system), "System name is in list output");
		Assert.assertTrue(res.getStdout().contains(_system2), "System name is in list output");
		Assert.assertTrue(res.getStdout().contains(_system3), "System name is in list output");
		
		KatelloSystemGroup systemGroup = new KatelloSystemGroup(null, _env_1, _org);
		res = systemGroup.list_systems();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		Assert.assertTrue(res.getStdout().contains(_system), "System name is in list output");
		Assert.assertFalse(res.getStdout().contains(_system2), "System name is not in list output");
		Assert.assertFalse(res.getStdout().contains(_system3), "System name is not in list output");
		
		systemGroup = new KatelloSystemGroup(null, _env_2, _org);
		res = systemGroup.list_systems();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		Assert.assertTrue(res.getStdout().contains(_system2), "System name is in list output");
		Assert.assertFalse(res.getStdout().contains(_system), "System name is not in list output");
		Assert.assertFalse(res.getStdout().contains(_system3), "System name is not in list output");
		
		systemGroup = new KatelloSystemGroup(null, _env_3, _org);
		res = systemGroup.list_systems();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		Assert.assertTrue(res.getStdout().contains(_system3), "System name is in list output");
		Assert.assertFalse(res.getStdout().contains(_system2), "System name is not in list output");
		Assert.assertFalse(res.getStdout().contains(_system), "System name is not in list output");
	}
	
	@Test(description="verify that permissions not related to environments survived the upgrade", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE})
	public void checkPermissionSurvived(){
		KatelloPermission perm = new KatelloPermission(null, null, null, null, null, null, _user_role);
		SSHCommandResult res = perm.list();
		// permissions for environments must be gone
		Assert.assertFalse(res.getStdout().contains(_perm_1), "permission must be listed");
		
		perm = new KatelloPermission(null, null, null, null, null, null, _user_role2);
		res = perm.list();
		Assert.assertTrue(res.getStdout().contains(_perm_2), "permission must be listed");
		
		perm = new KatelloPermission(null, null, null, null, null, null, _user_role3);
		res = perm.list();
		Assert.assertTrue(res.getStdout().contains(_perm_3), "permission must be listed");
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
			groups={TNG_POST_UPGRADE}, dependsOnMethods={"checkSystemsSurvived"})
	public void checkSysGroupCreate(){
		KatelloSystemGroup systemGroup = new KatelloSystemGroup(null, _system_group, _org);
		SSHCommandResult res = systemGroup.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (system group create)");

    	KatelloSystem sys = new KatelloSystem(null, _system, _org, null);
    	sys.runOn(SetupServers.client_name);
    	res = sys.rhsm_identity();
		String system_uuid = KatelloUtils.grepCLIOutput("Current identity is", res.getStdout());
		
		res = systemGroup.add_systems(system_uuid);
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (add system)");
		
		sys = new KatelloSystem(null, _newsystem, _org, null);
		sys.runOn(SetupServers.client_name);
		res = sys.rhsm_registerForce(_akey);
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - rhsm register (activationkey)");
		
		res = sys.rhsm_identity();
		String system_uuid2 = KatelloUtils.grepCLIOutput("Current identity is", res.getStdout());
		
		res = systemGroup.add_systems(system_uuid2);
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (add system)");
	}
}
