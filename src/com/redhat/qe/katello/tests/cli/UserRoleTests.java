package com.redhat.qe.katello.tests.cli;
import org.testng.annotations.Test;
import org.testng.SkipException;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliDataProvider;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.katello.base.obj.KatelloUserRole;
import com.redhat.qe.tools.SSHCommandResult;

public class UserRoleTests extends KatelloCliTestScript{
	
	
	
	@Test(description="create User Role", groups = {"headpin-cli"}, 
			dataProvider="user_role_create", dataProviderClass = KatelloCliDataProvider.class, enabled=true)
	public void testUserRole_create(String name, String descr, Integer exitCode, String output){
		SSHCommandResult res;
		
		KatelloUserRole user_role = new KatelloUserRole(name, descr);
		res = user_role.create();
		Assert.assertTrue(res.getExitCode().intValue() == exitCode.intValue(), "Check - return code");
		
		if(exitCode.intValue()==0){ //
			Assert.assertTrue(getOutput(res).contains(output),"Check - returned output string");
		}else{ // Failure to be checked
			Assert.assertTrue(getOutput(res).contains(output),"Check - returned error string");
		}
	}
	
	
	
	
	@Test(description="User Role Update",groups = {"headpin-cli"})
	public void testUserRole_update()
	{
		SSHCommandResult res;
		String uid = KatelloTestScript.getUniqueID();
		String name = "role-"+uid;
		String descr = "Test user role created";
		String new_name = "role-newname" + uid;
		KatelloUserRole user_role  = new KatelloUserRole(name,descr);
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
	
	
	
	
	@Test(description="delete a UserRole", groups = {"headpin-cli"},enabled=true)
	public void test_delete_UserRole(){
	            String uid = KatelloTestScript.getUniqueID();
	            String user_role_name="user_role-delete-"+ uid; 
	            SSHCommandResult res;
	            KatelloUserRole user_role = new KatelloUserRole(user_role_name, "User Role Created");
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
	            String uid = KatelloTestScript.getUniqueID();
	            String user_role_name="user_role-ldap_grp-"+ uid;
	            String ldap_group = "group-"+uid;
	            SSHCommandResult res;
	            int exitCode;
	            KatelloUserRole user_role = new KatelloUserRole(user_role_name, "User Role Created");
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
	            String uid = KatelloTestScript.getUniqueID();
	            String user_role_name="user_role-ldap_grp-"+ uid;
	            String ldap_group = "group-"+uid;
	            SSHCommandResult res;
	            int exitCode;
	            KatelloUserRole user_role = new KatelloUserRole(user_role_name, "User Role Created");
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
	
	
    
	
}
