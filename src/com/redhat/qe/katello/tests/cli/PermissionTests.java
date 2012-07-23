package com.redhat.qe.katello.tests.cli;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliDataProvider;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.katello.base.obj.KatelloPermission;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloUserRole;
import com.redhat.qe.tools.SSHCommandResult;
public class PermissionTests extends KatelloCliTestScript{
	         private String organization;
	         private String usr_role;
	         @BeforeClass(description="init: create org stuff", groups = {"headpin-cli"})
	         public void setUp(){
		                
	        	         SSHCommandResult res;
	        	         String uid = KatelloTestScript.getUniqueID();
	        	         this.organization = "permorg-"+uid;
	        	         KatelloOrg org = new KatelloOrg(this.organization, null);
	        	         res = org.cli_create();
	        	         Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
	        	         this.usr_role = "perm-role-"+uid;
	        	         KatelloUserRole perm_role = new KatelloUserRole(this.usr_role, null);
	        	         res = perm_role.create();
	        	         Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
	        	         
	         }
	         
	         
	        @Test(description="create Permissions", groups = {"headpin-cli"}, 
	 				dataProvider="permission_create", dataProviderClass = KatelloCliDataProvider.class, enabled=true)
	 		public void testPerm_create(String name, String scope,String tags,String verbs,String user_role,Integer exitCode, String output){
	 			SSHCommandResult res;
	 			
	 			KatelloPermission perm = new KatelloPermission(name, this.organization, scope, tags, verbs,user_role);
	 			res = perm.create();
	 			Assert.assertTrue(res.getExitCode().intValue() == exitCode.intValue(), "Check - return code");
	 			
	 			if(exitCode.intValue()==0){ //
	 				Assert.assertTrue(getOutput(res).contains(output),"Check - returned output string");
	 			}else{ // Failure to be checked
	 				Assert.assertTrue(getOutput(res).contains(output),"Check - returned error string");
	 			}
	 		} 
	    
	         
	        
	        @Test(description="list available scopes, verbs and tags that can be set in a permission", groups = {"headpin-cli"}, 
	 				dataProvider="permission_available_verbs", dataProviderClass = KatelloCliDataProvider.class, enabled=true)
	 		public void testPerm_available_verbs(String scope,Integer exitCode){
	 			SSHCommandResult res;
	 			res = KatelloPermission.available_verbs(this.organization, scope);
	 			Assert.assertTrue(res.getExitCode().intValue() == exitCode.intValue(), "Check - return code");
	 				 			
	 		} 
	      
	        
	        @Test(description="delete a permission", groups = {"headpin-cli"},enabled=true)
			public void test_delete_permission(){
			            String uid = KatelloTestScript.getUniqueID();
			            String permName="perm-delete-"+ uid; 
			            String scope = "environments";
			            String verbs = "update_systems,read_contents,read_systems,register_systems,delete_systems";
			            SSHCommandResult res;
			            KatelloPermission perm_del = new KatelloPermission(permName,this.organization,scope,null,verbs,this.usr_role);
			            res = perm_del.create();
			            Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (permission create)");
			            Assert.assertTrue(getOutput(res).contains(
			    				String.format(KatelloPermission.OUT_CREATE,permName,this.usr_role)), 
			    				"Check - returned output string ("+KatelloPermission.CMD_CREATE+")");
			            res = perm_del.list();
			            Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (permission list)");
			            res = perm_del.delete();
			            Assert.assertTrue(res.getExitCode().intValue() == 0,"Check - return code (permission delete)");
			            Assert.assertTrue(getOutput(res).contains(
			    				String.format(KatelloPermission.OUT_DELETE,permName,this.usr_role)), 
			    				"Check - returned output string ("+KatelloPermission.CMD_DELETE+")");
			            res = perm_del.list();
			            Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (permission list)");
			            
			}    
	        
	        
	         
	        
	        
	        

}
