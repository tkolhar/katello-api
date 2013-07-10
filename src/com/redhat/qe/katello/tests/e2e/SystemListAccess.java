package com.redhat.qe.katello.tests.e2e;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloPermission;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.base.obj.KatelloUser;
import com.redhat.qe.katello.base.obj.KatelloUserRole;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

/**
 * Automation of bug #773137.
 * Create user, assign role which has only read systems of Dev environment.
 * List systems by created user, verify that only systems registered in Dev environment are listed.
 * 
 * @author hhovsepy
 *
 */
@Test(groups={"cfse-e2e"}, singleThreaded = true)
public class SystemListAccess extends KatelloCliTestBase {
	
	private SSHCommandResult exec_result;

	// Katello objects below
	private String org_name;
	private String env_name_Dev, env_name_Test;
	private String system_name1, system_name2;
	private String user_name;
	private String user_role;
	
	@BeforeClass(description="Generate unique names")
	public void setUp(){
		String uid = KatelloUtils.getUniqueID();
		org_name = "org_"+uid;
		env_name_Dev = "env_Dev_"+uid;
		env_name_Test = "env_Test_"+uid;
		system_name1 = "system_Dev"+uid;
		system_name2 = "system_Test"+uid;
		user_name = "user_system_only"+uid;
		user_role = "Read Systems only"+uid;
		
		rhsm_clean(); // clean - in case of it registered
		
		// Create org:
		KatelloOrg org = new KatelloOrg(this.cli_worker, this.org_name, "Package tests");
		exec_result = org.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		KatelloEnvironment env = new KatelloEnvironment(this.cli_worker, env_name_Dev, null, org_name, KatelloEnvironment.LIBRARY);
		exec_result = env.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (env create)");
		
		env = new KatelloEnvironment(this.cli_worker, env_name_Test, null, org_name, KatelloEnvironment.LIBRARY);
		exec_result = env.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (env create)");
		
		KatelloUser user = new KatelloUser(cli_worker, this.user_name, "root@localhost", KatelloUser.DEFAULT_USER_PASS, false);
		exec_result = user.cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code (user create)");
		
		KatelloUserRole role = new KatelloUserRole(cli_worker, this.user_role, "Read systems only");
		exec_result = role.create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code (user_role create)");
		
		KatelloPermission perm = new KatelloPermission(cli_worker, this.user_role, this.org_name, "environments", this.env_name_Dev, 
				"read_systems", this.user_role);
		exec_result = perm.create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code (permission create)");
		
		exec_result = user.assign_role(this.user_role);
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code (user assign_role)");
		
		KatelloSystem sys = new KatelloSystem(this.cli_worker, system_name1, this.org_name, env_name_Dev);
		exec_result = sys.rhsm_registerForce();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code (register --force)");
		
		rhsm_clean_only();
		
		sys = new KatelloSystem(this.cli_worker, system_name2, this.org_name, env_name_Test);
		exec_result = sys.rhsm_registerForce();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code (register --force)");
		
		sys = new KatelloSystem(this.cli_worker, null, this.org_name, null);
		exec_result = sys.list();
		Assert.assertTrue(getOutput(exec_result).trim().contains(system_name1), "System 1 should be contained");
		Assert.assertTrue(getOutput(exec_result).trim().contains(system_name2), "System 2 should be contained");
	}
	
	@Test(description = "Login by created user, list the systems and verify that only system in Dev environment is listed")
	public void test_listSystem(){
		KatelloUser user = new KatelloUser(cli_worker, this.user_name, "root@localhost", KatelloUser.DEFAULT_USER_PASS, false);
		KatelloSystem sys = new KatelloSystem(this.cli_worker, system_name1, this.org_name, null);
		sys.runAs(user);
		
		exec_result = sys.list();
		Assert.assertTrue(getOutput(exec_result).trim().contains(system_name1), "System 1 should be contained");
		Assert.assertFalse(getOutput(exec_result).trim().contains(system_name2), "System 2 should not be contained");
	}
}
