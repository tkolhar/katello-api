package com.redhat.qe.katello.tests.cli;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloUser;
import com.redhat.qe.katello.base.obj.KatelloUserRole;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.common.TngRunGroups;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups={"headpin-cli",TngRunGroups.TNG_KATELLO_Users_Roles})
public class UserNoRoleNoAccess extends KatelloCliTestScript {
	
	private String users;	
	private String organization;
	private String env;
	private String user_email;
	private String user_role_name;
	private SSHCommandResult res;
	private KatelloOrg org;
	private KatelloEnvironment environment;
	private KatelloUser user;
	private KatelloUserRole user_role;
	@BeforeClass(description="init: create initial stuff")
	public void setUp()
	{
		
		String uid = KatelloUtils.getUniqueID();
		this.organization = "org-"+uid;
		this.env = "env-"+uid;
		this.users = "user-" +uid;
		this.user_email = "user-"+ uid + "@redhat.com";
		this.user_role_name = "user-role-" + uid;
		org = new KatelloOrg(this.organization, null);
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		environment = new KatelloEnvironment(this.env, "test environment",this.organization, "Library");
		res = environment.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		user = new KatelloUser(this.users,this.user_email,"password",false,this.organization,this.env);
		res =  user.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		user_role = new KatelloUserRole(this.user_role_name,"test role");
		res = user_role.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");


	}
	
  	
	@Test(description="check for organizations command", enabled=true)
	public void test_User_Org_Commands()
	{
		
		org.runAs(user);
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==147, "Check - return code");
		res = org.cli_info();
		Assert.assertTrue(res.getExitCode().intValue()==147, "Check - return code");
		res = org.cli_list();
		Assert.assertTrue(res.getExitCode().intValue()==147, "Check - return code");
		res = org.subscriptions();
		Assert.assertTrue(res.getExitCode().intValue()==147, "Check - return code");         		
		res = org.update("Org Updated");
		Assert.assertTrue(res.getExitCode().intValue()==147, "Check - return code");
		res = org.delete();
		Assert.assertTrue(res.getExitCode().intValue()==147, "Check - return code");

	}

	  
	@Test(description="check for users command", enabled=true)
	public void test_User_Commands()
	{
		
		user.runAs(user);       
		res = user.cli_list();
		Assert.assertTrue(res.getExitCode().intValue()==147, "Check - return code");
		res = user.list_roles();
		Assert.assertTrue(res.getExitCode().intValue()==147, "Check - return code");
		res = user.report("pdf");
		Assert.assertTrue(res.getExitCode().intValue()==147, "Check - return code");
		res = user.delete();
		Assert.assertTrue(res.getExitCode().intValue()==147, "Check - return code");

	}
	
	
	@Test(description="check for users roles command", enabled=true)
	public void test_UserRole_Commands()
	{
		
		user_role.runAs(user);       
		res = user_role.cli_list();
		Assert.assertTrue(res.getExitCode().intValue()==147, "Check - return code");
		res = user_role.cli_info();
		Assert.assertTrue(res.getExitCode().intValue()==147, "Check - return code");
		res = user_role.cli_update("user-role-update");
		Assert.assertTrue(res.getExitCode().intValue()==147, "Check - return code");
		res = user_role.cli_delete();
		Assert.assertTrue(res.getExitCode().intValue()==147, "Check - return code");
		
	}
	

}
