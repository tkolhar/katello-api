package com.redhat.qe.katello.tests.cli;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliDataProvider;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloPermission;
import com.redhat.qe.katello.base.obj.KatelloUserRole;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.common.TngRunGroups;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups=TngRunGroups.TNG_KATELLO_Users_Roles)
public class PermissionTests extends KatelloCliTestBase{
	private String organization;
	private String usr_role;
	private String userRole_create;
	
	@BeforeClass(description="init: create org stuff", groups = {"headpin-cli"})
	public void setUp(){
		SSHCommandResult res;
		String uid = KatelloUtils.getUniqueID();
		this.organization = "permorg-"+uid;
		KatelloOrg org = new KatelloOrg(this.cli_worker, this.organization, null);
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		this.usr_role = "perm-role-"+uid;
		KatelloUserRole perm_role = new KatelloUserRole(cli_worker, this.usr_role, null);
		res = perm_role.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		
		userRole_create = "perm-user_role"+uid;
		KatelloUserRole usr_role = new KatelloUserRole(cli_worker, userRole_create,null); 
		res =usr_role.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
	}

	@Test(description="de571d55-3987-4ce3-b72e-f97abe509bfc", 
			dataProvider="permission_create", dataProviderClass = KatelloCliDataProvider.class, enabled=true)
	public void testPerm_create(String name, String scope,String tags,String verbs,Integer exitCode, String output){
		SSHCommandResult res;

		KatelloPermission perm = new KatelloPermission(cli_worker, name, this.organization, scope, tags, verbs,this.userRole_create);
		res = perm.create();
		Assert.assertTrue(res.getExitCode().intValue() == exitCode.intValue(), "Check - return code");

		if(exitCode.intValue()==0){
			Assert.assertTrue(getOutput(res).contains(String.format(KatelloPermission.OUT_CREATE, name, userRole_create)),"Check - returned output string");
		}else{ // Failure to be checked
			Assert.assertTrue(getOutput(res).contains(output),"Check - returned error string");
		}
	} 

	@Test(description="de571d55-3987-4ce3-b72e-f97abe509bfc",groups={"headpin-cli","cfse-ignore"},
			dataProvider="permission_create_headpinOnly", dataProviderClass = KatelloCliDataProvider.class, enabled=true)
	public void testPerm_createHeadpinOnly(String name, String scope,String tags,String verbs,Integer exitCode, String output){
		SSHCommandResult res;

		KatelloPermission perm = new KatelloPermission(cli_worker, name, this.organization, scope, tags, verbs,this.userRole_create);
		res = perm.create();
		Assert.assertTrue(res.getExitCode().intValue() == exitCode.intValue(), "Check - return code");

		if(exitCode.intValue()==0){
			Assert.assertTrue(getOutput(res).contains(String.format(KatelloPermission.OUT_CREATE, name, userRole_create)),"Check - returned output string");
		}else{ // Failure to be checked
			Assert.assertTrue(getOutput(res).contains(output),"Check - returned error string");
		}
	} 
	
	@Test(description="list available scopes, verbs and tags that can be set in a permission", groups = {"headpin-cli"}, 
			dataProvider="permission_available_verbs", dataProviderClass = KatelloCliDataProvider.class, enabled=true)
	public void testPerm_available_verbs(String scope,Integer exitCode){
		KatelloPermission perm = new KatelloPermission(cli_worker, null, this.organization, scope, null, null, null);
		SSHCommandResult res;
		res = perm.available_verbs(this.organization, scope);
		Assert.assertTrue(res.getExitCode().intValue() == exitCode.intValue(), "Check - return code");
	} 

	@Test(description="9d7b05ad-a6e7-4668-98b6-62cc939b4cf4",enabled=true)
	public void test_delete_permission(){
		String uid = KatelloUtils.getUniqueID();
		String permName="perm-delete-"+ uid; 
		String scope = "environments";
		String verbs = "update_systems,read_contents,read_systems,register_systems,delete_systems";
		SSHCommandResult res;
		KatelloPermission perm_del = new KatelloPermission(cli_worker, permName,this.organization,scope,null,verbs,this.usr_role);
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
	
	@Test(description="9d7b05ad-a6e7-4668-98b6-62cc939b4cf4", groups = {"headpin-cli","cfse-ignore"},enabled=true)
	public void test_delete_permissionHeadpinOnly(){
		String uid = KatelloUtils.getUniqueID();
		String permName="perm-delete-"+ uid; 
		String scope = "organizations";
		String verbs = "update_systems,read,read_systems,register_systems,delete_systems,delete_distributors,update_distributors,read_distributors,register_distributors";
		SSHCommandResult res;
		KatelloPermission perm_del = new KatelloPermission(cli_worker, permName,this.organization,scope,null,verbs,this.usr_role);
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

	@Test(description="create permission with --all_verbs option")
	public void test_createPermissionAllVerbs() {
		String perm_name = "permAllVerbs"+KatelloUtils.getUniqueID();
		KatelloPermission perm = new KatelloPermission(cli_worker, perm_name, null, "organizations", null, null, userRole_create);
		exec_result = perm.create(false, true);
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (perm create)");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloPermission.OUT_CREATE, perm_name, userRole_create)), "Check output");
	}

	@Test(description="invalid parameters for permission create")
	public void test_invalidParameters() {
		String uid = KatelloUtils.getUniqueID();
		String perm_name = "perm" + uid;
		String scope = "providers";
		String bad_scope = "bad scope" + uid;
		String bad_tag = "bad tag" + uid;
		String bad_role = "bad role" + uid;
		// tags + all tags
		KatelloPermission perm = new KatelloPermission(cli_worker, perm_name, null, scope, base_zoo_provider_name, null, usr_role);
		exec_result = perm.create(true);
		Assert.assertTrue(exec_result.getExitCode()==65, "Check exit code (perm create)");
		Assert.assertTrue(getOutput(exec_result).equals(KatelloPermission.ERR_TAG_ALL_TAGS), "Check error message (perm create)");
		// invalid scope
		perm = new KatelloPermission(cli_worker, perm_name, null, bad_scope, null, null, usr_role);
		exec_result = perm.create();
		Assert.assertTrue(exec_result.getExitCode()==65, "Check exit code (perm create)");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloPermission.ERR_INVALID_SCOPE, bad_scope)), "Check error message (perm create)");
		// invalid tag
		perm = new KatelloPermission(cli_worker, perm_name, null, scope, bad_tag, null, usr_role);
		exec_result = perm.create();
		Assert.assertTrue(exec_result.getExitCode()==65, "Check exit code (perm create)");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloPermission.ERR_TAG_NOT_FOUND, bad_tag, scope)), "Check error message (perm create)");
		exec_result = perm.available_verbs(null, null);
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (perm avail verbs)");
		// bad role
		perm = new KatelloPermission(cli_worker, perm_name, null, scope, null, null, bad_role);
		exec_result = perm.create();
		Assert.assertTrue(exec_result.getExitCode()==65, "Check exit code (perm create)");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloUserRole.ERR_NOT_FOUND, bad_role)), "Check error message (perm create)");
	}
}
