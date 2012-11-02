package com.redhat.qe.katello.tests.upgrade.v1;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloPermission;
import com.redhat.qe.katello.base.obj.KatelloUser;
import com.redhat.qe.katello.base.obj.KatelloUserRole;
import com.redhat.qe.katello.common.KatelloConstants;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

public class FillDB implements KatelloConstants{

	private String uid;
	private String orgName;
	private String envNameTesting, envNameDevelopment;
	private String userNameAdmin;
	private String userNameDisabled;
	private String userNameGuest;
	
	private String roleReadAll;
	private String permissionOrgAdmin;
	
	
	@BeforeClass(groups={TNG_PRE_UPGRADE}, description="init strings")
	public void init(){
		uid = KatelloUtils.getUniqueID();
		orgName = "CFSE QE Team "+uid;
		envNameTesting = "Testing";
		envNameDevelopment = "Development";
		userNameAdmin = "cfse-admin-"+uid;
		userNameDisabled = "cfse-disabled-"+uid;
		userNameGuest = "cfse-guest-"+uid;
		roleReadAll = KatelloUserRole.ROLE_READ_EVERYTHING+" "+uid;
	}
	
	@Test(groups={TNG_PRE_UPGRADE}, description="create org, environment, user")
	public void create_OrgEnvUser(){
		SSHCommandResult res;
		KatelloOrg org = new KatelloOrg(orgName, orgName+" description");
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: org.create");
		res = new KatelloEnvironment(envNameTesting, envNameTesting+" decription", orgName, KatelloEnvironment.LIBRARY).cli_create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: environment.create");
		res = new KatelloEnvironment(envNameDevelopment, envNameDevelopment+" decription", orgName, envNameTesting).cli_create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: environment.create");
		res = new KatelloUser(userNameAdmin, KatelloUser.DEFAULT_USER_EMAIL, KatelloUser.DEFAULT_USER_PASS, false).cli_create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: user.create");
		res = new KatelloUser(userNameDisabled, KatelloUser.DEFAULT_USER_EMAIL, KatelloUser.DEFAULT_USER_PASS, true).cli_create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: user.create --disabled true");
		res = new KatelloUser(userNameGuest, KatelloUser.DEFAULT_USER_EMAIL, KatelloUser.DEFAULT_USER_PASS, false).cli_create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: user.create");
		KatelloUser user = new KatelloUser();
		user.username = userNameAdmin;
		res = user.update_defaultOrgEnv(orgName, envNameTesting);
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: user.assignDefaultOrgEnv");
	}
	
	@Test(groups={TNG_POST_UPGRADE}, description="check org, environent, user survived", dependsOnMethods={"create_OrgEnvUser"}) // TODO - remove depends... 
	public void check_OrgEnvUser(){
		SSHCommandResult res;
		String _name, _description, _prior;
		
		KatelloOrg org;
		org = new KatelloOrg(orgName, orgName+" description");
		res = org.cli_info();
		_name = KatelloCli.grepCLIOutput("Name", KatelloCliTestScript.sgetOutput(res));
		_description = KatelloCli.grepCLIOutput("Description", KatelloCliTestScript.sgetOutput(res));
		Assert.assertTrue(res.getExitCode().intValue() == 0, "check: org.info");
		Assert.assertTrue(_name.equals(org.name), "stdout: org.name");
		Assert.assertTrue(_description.equals(org.description), "stdout: org.description");
		
		KatelloEnvironment env;
		env = new KatelloEnvironment(envNameTesting, envNameTesting+" decription", orgName, KatelloEnvironment.LIBRARY);
		res = env.cli_info();
		_name = KatelloCli.grepCLIOutput("Name", KatelloCliTestScript.sgetOutput(res));
		_description = KatelloCli.grepCLIOutput("Description", KatelloCliTestScript.sgetOutput(res));
		_prior = KatelloCli.grepCLIOutput("Prior Environment", KatelloCliTestScript.sgetOutput(res));
		Assert.assertTrue(res.getExitCode().intValue() == 0, "check: environment.info");
		Assert.assertTrue(_name.equals(env.getName()), "stdout: environment.name");
		Assert.assertTrue(_description.equals(env.getDescription()), "stdout: environment.description");
		Assert.assertTrue(_prior.equals(KatelloEnvironment.LIBRARY), "stdout: environment.prior");
		
		env = new KatelloEnvironment(envNameDevelopment, envNameDevelopment+" decription", orgName, envNameTesting);
		res = env.cli_info();
		_name = KatelloCli.grepCLIOutput("Name", KatelloCliTestScript.sgetOutput(res));
		_description = KatelloCli.grepCLIOutput("Description", KatelloCliTestScript.sgetOutput(res));
		_prior = KatelloCli.grepCLIOutput("Prior Environment", KatelloCliTestScript.sgetOutput(res));
		Assert.assertTrue(res.getExitCode().intValue() == 0, "check: environment.info");
		Assert.assertTrue(_name.equals(env.getName()), "stdout: environment.name");
		Assert.assertTrue(_description.equals(env.getDescription()), "stdout: environment.description");
		Assert.assertTrue(_prior.equals(envNameTesting), "stdout: environment.prior");
	}
	
	@Test(groups={TNG_PRE_UPGRADE}, description="create role, permission and assignments", dependsOnMethods={"create_OrgEnvUser"})
	public void create_permissionsRoles(){
		SSHCommandResult res;
		res  = new KatelloUserRole(roleReadAll, roleReadAll+ " description").create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: role.create");
		res = new KatelloPermission("ro-filters-"+uid, orgName, "filters", null, "read", roleReadAll).create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: permission.create");
		res = new KatelloPermission("ro-roles-"+uid, orgName, "roles", null, "read", roleReadAll).create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: permission.create");
	}

}
