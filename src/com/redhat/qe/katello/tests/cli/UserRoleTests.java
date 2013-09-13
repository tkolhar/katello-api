package com.redhat.qe.katello.tests.cli;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliDataProvider;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloUserRole;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.common.TngRunGroups;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups={TngRunGroups.TNG_KATELLO_Users_Roles, "cli-UserRoleTests"})
public class UserRoleTests extends KatelloCliTestBase{

//	@BeforeClass(description="setup",alwaysRun=true)
//	public void setUp(){
//		
//	}
//	
	@Test(description="53f5c215-3dd8-45e7-819d-1fdf69886d63", groups = {"headpin-cli"}, 
			dataProvider="user_role_create", dataProviderClass = KatelloCliDataProvider.class, enabled=true)
	public void testUserRole_create(String name, String descr, Integer exitCode, String output){
		SSHCommandResult res;

		KatelloUserRole user_role = new KatelloUserRole(cli_worker, name, descr);
		res = user_role.create();
		Assert.assertTrue(res.getExitCode().intValue() == exitCode.intValue(), "Check - return code");

		if(exitCode.intValue()==0){ //
			Assert.assertTrue(getOutput(res).contains(output),"Check - returned output string");
		}else{ // Failure to be checked
			Assert.assertTrue(getOutput(res).contains(output),"Check - returned error string");
		}
	}
	
	@Test(description="c989e331-ec5b-4eb1-9cec-b6414c8dccf5",groups = {"headpin-cli"})
	public void test_createNegative(){
		KatelloUserRole user_role = new KatelloUserRole(cli_worker, "", "Blank name. All other cases are covered in: KatelloCliDataProvider. This is for TCMS - don't remove it please.");
		SSHCommandResult res = user_role.create();
		Assert.assertTrue(res.getExitCode().intValue() == 2, "Check - return code");
	}

	@Test(description="e1333c42-9edd-43f6-901f-087d6601624f",groups = {"headpin-cli"})
	public void test_createPositive(){
		KatelloUserRole user_role = new KatelloUserRole(cli_worker, "hello world "+KatelloUtils.getUniqueID(), "Some positive placeholder for TCMS. All other cases are covered in: KatelloCliDataProvider. This is for TCMS - don't remove it please.");
		SSHCommandResult res = user_role.create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
	}

	@Test(description="35b4dfe0-4ece-467d-8efd-500c51dcfed2",groups = {"headpin-cli"})
	public void testUserRole_update()
	{
		SSHCommandResult res;
		String uid = KatelloUtils.getUniqueID();
		String name = "role-"+uid;
		String descr = "Test user role created";
		String new_name = "role-newname" + uid;
		KatelloUserRole user_role  = new KatelloUserRole(cli_worker, name,descr);
		res = user_role.create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(res).contains(
				String.format(KatelloUserRole.OUT_CREATE,name)), 
				"Check - returned output string ("+KatelloUserRole.CMD_CREATE+")");
		res = user_role.cli_info();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		res = user_role.cli_update(new_name);
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(res).contains(
				String.format(KatelloUserRole.OUT_UPDATE,name)), 
				"Check - returned output string ("+KatelloUserRole.CMD_UPDATE+")");
	}

	@Test(description="8f10c697-fc84-4b96-8530-9ce935e7fb17", groups = {"headpin-cli"})
	public void test_delete_UserRole(){
		String uid = KatelloUtils.getUniqueID();
		String user_role_name="user_role-delete-"+ uid; 
		SSHCommandResult res;
		KatelloUserRole user_role = new KatelloUserRole(cli_worker, user_role_name, "User Role Created");
		res = user_role.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (user role create)");
		Assert.assertTrue(getOutput(res).contains(
				String.format(KatelloUserRole.OUT_CREATE,user_role_name)), 
				"Check - returned output string ("+KatelloUserRole.CMD_CREATE+")");
		res = user_role.cli_delete();
		Assert.assertTrue(res.getExitCode().intValue() == 0,"Check - return code (user role delete)");
		Assert.assertTrue(getOutput(res).contains(
				String.format(KatelloUserRole.OUT_DELETE,user_role_name)), 
				"Check - returned output string ("+KatelloUserRole.CMD_DELETE+")");

		res = user_role.cli_info();
		Assert.assertTrue(res.getExitCode().intValue() == 65,"Check - return code (user role delete)");
		Assert.assertTrue(getOutput(res).contains(
				String.format(KatelloUserRole.ERROR_INFO,user_role_name)), 
				"Check - returned output string ("+KatelloUserRole.CMD_INFO+")");	
		res = user_role.cli_list();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (user role list)");

	}  

	//todo : add ldapgroup/removeldap group
	@Test(description="Add ldap group to a UserRole", groups = {"headpin-cli","openldap"},enabled=true)
	public void test_add_ldap_group() {
		String uid = KatelloUtils.getUniqueID();
		String user_role_name="user_role-ldap_grp-"+ uid;
		String ldap_group = "group-"+uid;
		SSHCommandResult res;
		int exitCode;
		KatelloUserRole user_role = new KatelloUserRole(cli_worker, user_role_name, "User Role Created");
		res = user_role.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (user role create)");
		Assert.assertTrue(getOutput(res).contains(
				String.format(KatelloUserRole.OUT_CREATE,user_role_name)), 
				"Check - returned output string ("+KatelloUserRole.CMD_CREATE+")");
		res = user_role.cli_add_ldap_group(ldap_group);
		exitCode = res.getExitCode().intValue();
		if (exitCode == 244)
		{
			throw (new SkipException("Skip"));
		}
		else
			if(exitCode == 0)
			{
				Assert.assertTrue(res.getExitCode().intValue() == 0,"Check - return code (user role add ldap group)");
				Assert.assertTrue(getOutput(res).contains(
						String.format(KatelloUserRole.OUT_LDAP_ADD,ldap_group,user_role_name)), 
						"Check - returned output string ("+KatelloUserRole.CMD_LDAP_GRP_ADD+")");
				res = user_role.cli_info();
				Assert.assertTrue(res.getExitCode().intValue() == 0,"Check - return code (user role info)");
			}	                       	            
	}  

	@Test(description="Remove ldap group to a UserRole", groups = {"headpin-cli","openldap"},enabled=true)
	public void test_remove_ldap_group(){
		String uid = KatelloUtils.getUniqueID();
		String user_role_name="user_role-ldap_grp-"+ uid;
		String ldap_group = "group-"+uid;
		SSHCommandResult res;
		int exitCode;
		KatelloUserRole user_role = new KatelloUserRole(cli_worker, user_role_name, "User Role Created");
		res = user_role.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (user role create)");
		Assert.assertTrue(getOutput(res).contains(
				String.format(KatelloUserRole.OUT_CREATE,user_role_name)), 
				"Check - returned output string ("+KatelloUserRole.CMD_CREATE+")");
		res = user_role.cli_add_ldap_group(ldap_group);
		exitCode = res.getExitCode().intValue();
		if (exitCode == 244)
		{
			throw (new SkipException("Skip"));
		}
		else
			if(exitCode == 0)
			{
				Assert.assertTrue(res.getExitCode().intValue() == 0,"Check - return code (user role add ldap group)");
				Assert.assertTrue(getOutput(res).contains(
						String.format(KatelloUserRole.OUT_LDAP_ADD,ldap_group,user_role_name)), 
						"Check - returned output string ("+KatelloUserRole.CMD_LDAP_GRP_ADD+")");
				res = user_role.cli_info();
				Assert.assertTrue(res.getExitCode().intValue() == 0,"Check - return code (user role info)");
				res = user_role.cli_remove_ldap_group(ldap_group);
				Assert.assertTrue(res.getExitCode().intValue() == 0,"Check - return code (user role remove ldap group)");
				Assert.assertTrue(getOutput(res).contains(
						String.format(KatelloUserRole.OUT_LDAP_REMOVE,ldap_group,user_role_name)), 
						"Check - returned output string ("+KatelloUserRole.CMD_LDAP_GRP_REMOVE+")");
				res = user_role.cli_info();
				Assert.assertTrue(res.getExitCode().intValue() == 0,"Check - return code (user role info)");
			}	            
	}    

	@Test(description="update role description")
	public void test_updateDescription() {
		SSHCommandResult res;
		String uid = KatelloUtils.getUniqueID();
		String role_name="role"+ uid;
		String description = "user role description";
		KatelloUserRole role = new KatelloUserRole(cli_worker, role_name, null);
		res = role.create();
		Assert.assertTrue(res.getExitCode()==0, "Check exit code (create role)");
		res = role.cli_update_description(description);
		Assert.assertTrue(res.getExitCode()==0, "Check exit code (update role)");
		res = role.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check exit code (role info)");
		Assert.assertTrue(getOutput(res).contains(description));
	}

	@AfterClass(description="Cleanup the trash we did.", alwaysRun=true, groups = {"headpin-cli"}, enabled=true)
	public void tearDown(){
		Object[][] roles = KatelloCliDataProvider.user_role_create();
		for(Object[] role: roles){
			new KatelloUserRole(cli_worker, (String)role[0], null).cli_delete(); // we don't care if some of them will fail.
		}
	}
}
