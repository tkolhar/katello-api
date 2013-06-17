package com.redhat.qe.katello.base;

import java.util.Random;

import org.testng.annotations.DataProvider;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.obj.KatelloUserRole;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloCliDataProvider {

	
	@DataProvider(name = "org_create")
	public static Object[][] org_create(){
		String uniqueID1 = KatelloUtils.getUniqueID();
		try{Thread.sleep(1000+Math.abs(new Random().nextInt(500)));}catch(InterruptedException iex){};
		String uniqueID2 = KatelloUtils.getUniqueID();
		return new Object[][] {
				{ "orgNoDescr_"+uniqueID1, null },
				{ "org "+uniqueID2+"", "Org with space"}
		};
	}
	
	/**
	 * Object[] contains of:<BR>
	 * provider:<BR>
	 * &nbsp;&nbsp;name<br>
	 * &nbsp;&nbsp;description<br>  
	 * &nbsp;&nbsp;url<br>
	 * &nbsp;&nbsp;exit_code<br>
	 * &nbsp;&nbsp;output
	 */

	@DataProvider(name="provider_create")
	public static Object[][] provider_create(){
		// TODO - the cases with unicode characters still missing - there 
		// is a bug: to display that characters.
		String uid = KatelloUtils.getUniqueID();
		return new Object[][] {
				// name
				{ "aa", null, null, new Integer(0), "Successfully created provider [ aa ]"},
				{ "11", null, null, new Integer(0), "Successfully created provider [ 11 ]"},
				{ "1a", null, null, new Integer(0), "Successfully created provider [ 1a ]"},
				{ "a1", null, null, new Integer(0), "Successfully created provider [ a1 ]"},
				{ strRepeat("0123456789", 12)+"abcdefgh", null, null, new Integer(0), "Successfully created provider [ "+strRepeat("0123456789", 12)+"abcdefgh"+" ]"},
				{ "prov-"+uid, null, null, new Integer(0), "Successfully created provider [ prov-"+uid+" ]"},
				{ "prov "+uid, "Provider with space in name", null, new Integer(0), "Successfully created provider [ prov "+uid+" ]"},
				{ null, null, null, new Integer(2), System.getProperty("katello.engine", "katello")+": error: Option --name is required; please see --help"},
				{ " ", null, null, new Integer(166), "Name can't be blank"},
				{ " a", null, null, new Integer(166), "Validation failed: Name must not contain leading or trailing white spaces."},
				{ "a ", null, null, new Integer(166), "Validation failed: Name must not contain leading or trailing white spaces."},
				{ "a", null, null, new Integer(0), "Successfully created provider [ a ]"},
				{ "?1", null, null, new Integer(166), "Validation failed: Name cannot contain characters other than alpha numerals, space, '_', '-'"},
				{ strRepeat("0123456789", 12)+"abcdefghi", null, null, new Integer(166), "Validation failed: Name cannot contain more than 128 characters"},
				// description
				{ "desc-specChars"+uid, "\\!@%^&*(<_-~+=//\\||,.>)", null, new Integer(0), "Successfully created provider [ desc-specChars"+uid+" ]"},
				{ "desc-255Chars"+uid, strRepeat("0123456789", 25)+"abcde", null, new Integer(0), "Successfully created provider [ desc-255Chars"+uid+" ]"},
				{ "desc-256Chars"+uid, strRepeat("0123456789", 25)+"abcdef", null, new Integer(166), "Validation failed: Description cannot contain more than 255 characters"},
				// url
				{ "url-httpOnly"+uid, null, "http://", new Integer(2), System.getProperty("katello.engine", "katello")+": error: option --url: invalid format"}, // see below
				{ "url-httpsOnly"+uid, null, "https://", new Integer(2), System.getProperty("katello.engine", "katello")+": error: option --url: invalid format"}, // according changes of: tstrachota
				{ "url-redhatcom"+uid, null, "http://redhat.com/", new Integer(0), "Successfully created provider [ url-redhatcom"+uid+" ]"},
				{ "url-with_space"+uid, null, "http://url with space/", new Integer(0), "Successfully created provider [ url-with_space"+uid+" ]"},
				// misc
				{ "duplicate"+uid, null, null, new Integer(0), "Successfully created provider [ duplicate"+uid+" ]"},
				{ "duplicate"+uid, null, null, new Integer(166), "Validation failed: Name has already been taken"}
		};		
	}
	
	@DataProvider(name="provider_create_diffType")
	public static Object[][] provider_create_diffType(){
		String KTL_PROD = System.getProperty("katello.engine", "katello");
		return new Object[][] {
				{ "C", new Integer(2), KTL_PROD+": error: option --type: invalid choice: 'C' (choose from 'redhat', 'custom')"},
				{ "Custom", new Integer(2), KTL_PROD+": error: option --type: invalid choice: 'Custom' (choose from 'redhat', 'custom')"},
				{ "CUSTOM", new Integer(2), KTL_PROD+": error: option --type: invalid choice: 'CUSTOM' (choose from 'redhat', 'custom')"},
				{ "rh", new Integer(2), KTL_PROD+": error: option --type: invalid choice: 'rh' (choose from 'redhat', 'custom')"},
				{ "RedHat", new Integer(2), KTL_PROD+": error: option --type: invalid choice: 'RedHat' (choose from 'redhat', 'custom')"},
				{ "REDHAT", new Integer(2), KTL_PROD+": error: option --type: invalid choice: 'REDHAT' (choose from 'redhat', 'custom')"},
				{ "^custom", new Integer(2), KTL_PROD+": error: option --type: invalid choice: '^custom' (choose from 'redhat', 'custom')"},
				{ " custom", new Integer(2), KTL_PROD+": error: option --type: invalid choice: ' custom' (choose from 'redhat', 'custom')"},
				{ "custom ", new Integer(2), KTL_PROD+": error: option --type: invalid choice: 'custom ' (choose from 'redhat', 'custom')"}
		};		
	}

	@DataProvider(name="provider_delete")
	public static Object[][] provider_delete(){
		return new Object[][] {
				// org
				{ null, null, new Integer(2), "Option --org is required; please see --help"},
				{ null, null, new Integer(2), "Option --name is required; please see --help"}
		};		
	}

	public static String strRepeat(String src, int times){
		String res = "";
		for(int i=0;i<times; i++)
			res = res + src; 
		return res;
	}
	
	@DataProvider(name="client_remember")
	public static Object[][] client_remember(){
		   String uid = KatelloUtils.getUniqueID();
		   return new Object[][] {
				
				
				{ "organizations-"+uid, "org-value",new Integer(0),"Successfully remembered option [ organizations-"+uid+" ]"},
				{ "providers-"+uid, "prov-value",new Integer(0),"Successfully remembered option [ providers-"+ uid +" ]"},
				{ "environments-"+uid, "env-value",new Integer(0),"Successfully remembered option [ environments-"+ uid +" ]"},
				{ strRepeat("0123456789", 12)+"abcdefgh-"+uid, "long-value", new Integer(0), "Successfully remembered option [ "+strRepeat("0123456789", 12)+"abcdefgh-"+ uid +" ]"},
				{ "opt-"+uid, "val-"+uid, new Integer(0), "Successfully remembered option [ opt-"+uid+" ]"},
				{ "opt "+uid, "Option with space in name", new Integer(0), "Successfully remembered option [ opt "+uid+" ]"},
		};		
	}
	
	@DataProvider(name="permission_available_verbs")
	public static Object[][] permission_available_verbs(){
		return new Object[][] {
				// org
				{ "organizations", new Integer(0)},
				{ "providers", new Integer(0)},
				{ "environments", new Integer(0)},
		};		
	}
	
	@DataProvider(name="permission_create")
	public static Object[][] permission_create(){
		String uid = KatelloUtils.getUniqueID();
		String user_role = "perm-user_role"+uid;
		SSHCommandResult res;
		KatelloUserRole usr_role = new KatelloUserRole(user_role,null); 
		res =usr_role.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		return new Object[][] {
				// name
				//String name, String scope,String tags,String verbs,Integer exitCode, String output
				{ "perm-all-verbs-"+uid, "environments", null, "read_contents,update_systems,delete_systems,read_systems,register_systems",user_role,new Integer(0),"Successfully created permission [ " + "perm-all-verbs-"+ uid + " ] for user role [ "+ user_role +" ]"},
				{ "perm-read_contents-env-"+uid, "environments", null, "read_contents",user_role,new Integer(0),"Successfully created permission [ " + "perm-read_contents-env-"+ uid  + " ] for user role [ "+ user_role +" ]"},
				{ "perm-read_update-env-"+uid, "environments", null, "read_contents,update_systems",user_role,new Integer(0),"Successfully created permission [ "+ "perm-read_update-env-"+uid+" ] for user role [ "+ user_role +" ]"},
				{ "perm-del_read_reg-verbs-env-"+uid, "environments", null, "delete_systems,read_systems,register_systems",user_role,new Integer(0),"Successfully created permission [ "+ "perm-del_read_reg-verbs-env-"+uid + " ] for user role [ "+ user_role +" ]"},
				{ "perm-register-verbs-env-"+uid, "environments", null, "register_systems",user_role,new Integer(0),"Successfully created permission [ " +"perm-register-verbs-env-" +uid + " ] for user role [ "+ user_role +" ]"},
				{ "perm-exclude_register-verbs-env-"+uid, "environments", null, "read_contents,update_systems,delete_systems,read_systems",user_role,new Integer(0),"Successfully created permission [ "+  "perm-exclude_register-verbs-env-"+uid +" ] for user role [ "+ user_role +" ]"},
				{ "perm-update_register-verbs-env-"+uid, "environments", null, "update_systems,register_systems",user_role,new Integer(0),"Successfully created permission [ "+"perm-update_register-verbs-env-"+uid+" ] for user role [ "+ user_role +" ]"},
				{ "perm-read_update-verbs-provider-"+uid, "providers", null, "read,update",user_role,new Integer(0),"Successfully created permission [ "+"perm-read_update-verbs-provider-"+uid +" ] for user role [ "+ user_role +" ]"},
				{ "perm-read-verbs-provider-"+uid, "providers", null, "read",user_role,new Integer(0),"Successfully created permission [ "+"perm-read-verbs-provider-"+uid +" ] for user role [ "+ user_role +" ]"},
				{ "perm-update-verbs-provider-"+uid, "providers", null, "update",user_role,new Integer(0),"Successfully created permission [ "+ "perm-update-verbs-provider-"+uid +" ] for user role [ "+ user_role +" ]"},
				{ "perm-all-org"+uid, "organizations", null, "delete_systems,update,update_systems,read,read_systems,register_systems",user_role,new Integer(0),"Successfully created permission [ "+ "perm-all-org"+uid +" ] for user role [ "+ user_role +" ]"},
				{ "perm-all-tags-verbs-"+uid, "environments", "Library", "read_contents,update_systems,delete_systems,read_systems,register_systems",user_role,new Integer(0),"Successfully created permission [ " + "perm-all-tags-verbs-"+uid + " ] for user role [ "+ user_role +" ]"},
				{ "perm-some_verbs-org"+uid, "organizations", null, "update,update_systems,read,read_systems",user_role,new Integer(0),"Successfully created permission [ "+ "perm-some_verbs-org"+uid +" ] for user role [ "+ user_role +" ]"},
		};
	}
	
	@DataProvider(name="user_role_create")
	public static Object[][] user_role_create(){
		String uid = KatelloUtils.getUniqueID();
		return new Object[][] {
				// name
				{ uid+"-aa", null, new Integer(0), "Successfully created user role [ "+uid+"-aa ]"},
				{ uid+"-11", null, new Integer(0), "Successfully created user role [ "+uid+"-11 ]"},
				{ uid+"-1a", null, new Integer(0), "Successfully created user role [ "+uid+"-1a ]"},
				{ uid+"-a1", null, new Integer(0), "Successfully created user role [ "+uid+"-a1 ]"},
				{ uid+strRepeat("0123456789", 11)+"abcdefgh", null, new Integer(0), "Successfully created user role [ "+uid+strRepeat("0123456789", 11)+"abcdefgh"+" ]"},
				{ "user_role-"+uid, null, new Integer(0), "Successfully created user role [ user_role-"+uid+" ]"},
				{ "user_role "+uid, "Provider with space in name", new Integer(0), "Successfully created user role [ user_role "+uid+" ]"},
				{ " ", null, new Integer(166), "Name can't be blank"},
				{ " a", null, new Integer(166), "Validation failed: Name must not contain leading or trailing white spaces."},
				{ "a ", null, new Integer(166), "Validation failed: Name must not contain leading or trailing white spaces."},
				{ "a", null, new Integer(166), "Validation failed: Name must contain at least 3 character"},
				{ "?1", null, new Integer(166), "Validation failed: Name must contain at least 3 character"},
			    { strRepeat("0123456789", 12)+"abcdefghi", null, new Integer(166), "Validation failed: Name is too long (maximum is 128 characters)"},
//				// description
				{ "desc-specChars"+uid, "\\!@%^&*(<_-~+=//\\||,.>)", new Integer(0), "Successfully created user role [ desc-specChars"+uid+" ]"},
				
				{ "desc-256Chars"+uid, strRepeat("0123456789", 25)+"abcdef", new Integer(166), "Validation failed: Description is too long (maximum is 250 characters)"},
				// misc
				{ "duplicate"+uid, null, new Integer(0), "Successfully created user role [ duplicate"+uid+" ]"},
 				
		};
	}
	@DataProvider(name="environment_create")
	public static Object[][] environment_create(){
		String uid = KatelloUtils.getUniqueID();
		return new Object[][] {
				// name
				{ "env-aa", null, new Integer(0), "Successfully created environment [ env-aa ]"},
				{ "env-11", null, new Integer(0), "Successfully created environment [ env-11 ]"},
				{ "env-1a", null, new Integer(0), "Successfully created environment [ env-1a ]"},
				{ "env-a1", null, new Integer(0), "Successfully created environment [ env-a1 ]"},
				{ strRepeat("0123456789", 12)+"abcdefgh", null, new Integer(0), "Successfully created environment [ "+strRepeat("0123456789", 12)+"abcdefgh"+" ]"},
				{ strRepeat("0123456789", 12)+"abcdefg", null, new Integer(0), "Successfully created environment [ "+strRepeat("0123456789", 12)+"abcdefg"+" ]"},
				{ "env-"+uid, null, new Integer(0), "Successfully created environment [ env-"+uid+" ]"},
				{ "env "+uid, "Provider with space in name", new Integer(0), "Successfully created environment [ env "+uid+" ]"},
				{ " ", null, new Integer(166), "Name can't be blank"},
				{ " a", null, new Integer(166), "Validation failed: Name must not contain leading or trailing white spaces."},
				{ "a ", null, new Integer(166), "Validation failed: Name must not contain leading or trailing white spaces."},
				{ "a", null, new Integer(0), "Successfully created environment [ a ]"},
				{ "?1", null, new Integer(166), "Validation failed: Name cannot contain characters other than alpha numerals, space, '_', '-'"},
			    { strRepeat("0123456789", 12)+"abcdefghi", null, new Integer(166), "Validation failed: Name cannot contain more than 128 characters"},
//				// description
				{ "desc-specChars"+uid, "\\!@%^&*(<_-~+=//\\||,.>)", new Integer(0), "Successfully created environment [ desc-specChars"+uid+" ]"},
				{ "desc-255Chars"+uid, strRepeat("0123456789", 25)+"abcde", new Integer(0), "Successfully created environment [ desc-255Chars"+uid+" ]"},
				{ "desc-256Chars"+uid, strRepeat("0123456789", 25)+"abcdef", new Integer(166), "Validation failed: Description cannot contain more than 255 characters"},
				// misc
				{ "duplicate"+uid, null, new Integer(0), "Successfully created environment [ duplicate"+uid+" ]"},
		};
	}
	
	/**
	 * Object[] contains of:<BR>
	 * activation key:<BR>
	 * &nbsp;&nbsp;name<br>
	 * &nbsp;&nbsp;description<br>
	 * &nbsp;&nbsp;exit_code<br>
	 * &nbsp;&nbsp;output
	 */
	@DataProvider(name="activationkey_create")
	public static Object[][] activationkey_create(){
		String uid = KatelloUtils.getUniqueID();
		return new Object[][] {
				// name
				{ "aa", null, new Integer(0), "Successfully created activation key [ aa ]"},
				{ "11", null, new Integer(0), "Successfully created activation key [ 11 ]"},
				{ "1a", null, new Integer(0), "Successfully created activation key [ 1a ]"},
				{ "a1", null, new Integer(0), "Successfully created activation key [ a1 ]"},
				{ strRepeat("0123456789", 12)+"abcdefgh", null, new Integer(0), "Successfully created activation key [ "+strRepeat("0123456789", 12)+"abcdefgh"+" ]"},
				{ "ak-"+uid, null, new Integer(0), "Successfully created activation key [ ak-"+uid+" ]"},
				{ "ak "+uid, "Provider with space in name", new Integer(0), "Successfully created activation key [ ak "+uid+" ]"},

				{ null, null, new Integer(2), System.getProperty("katello.engine", "katello")+": error: Option --name is required; please see --help"},
				
				
				{ null, null, new Integer(2), System.getProperty("katello.engine", "katello")+": error: Option --name is required; please see --help"},

				{ null, null, new Integer(2), System.getProperty("katello.engine", "katello")+": error: Option --name is required; please see --help"},

				{ " ", null, new Integer(166), "Name can't be blank"},
				{ " a", null, new Integer(166), "Validation failed: Name must not contain leading or trailing white spaces."},
				{ "a ", null, new Integer(166), "Validation failed: Name must not contain leading or trailing white spaces."},
				{ "a", null, new Integer(0), "Successfully created activation key [ a ]"},
				{ ">1", null, new Integer(166), "Validation failed: Name cannot contain characters other than alpha numerals, space, '_', '-'"},
			    { strRepeat("0123456789", 12)+"abcdefghi", null, new Integer(166), "Validation failed: Name cannot contain more than 128 characters"},
//				// description
				{ "desc-specChars"+uid, "\\!@%^&*(<_-~+=//\\||,.>)", new Integer(0), "Successfully created activation key [ desc-specChars"+uid+" ]"},
				{ "desc-255Chars"+uid, strRepeat("0123456789", 25)+"abcde", new Integer(0), "Successfully created activation key [ desc-255Chars"+uid+" ]"},
				{ "desc-256Chars"+uid, strRepeat("0123456789", 25)+"abcdef", new Integer(166), "Validation failed: Description cannot contain more than 255 characters"},
				// misc
				{ "duplicate"+uid, null, new Integer(0), "Successfully created activation key [ duplicate"+uid+" ]"},
 				{ "duplicate"+uid, null, new Integer(166), "Validation failed: Name has already been taken"}
		};
	}
	
	@DataProvider(name="add_custom_info")
	public static Object[][] add_custom_info(){
		String uid = KatelloUtils.getUniqueID();
		return new Object[][] {
				{ "env-aa", "env-aa", new Integer(0),null},
				{ strRepeat("0123456789", 12)+"abcdefgh",strRepeat("0123456789", 12)+"abcdefgh", new Integer(0),null},
				{ " ", "value", new Integer(166),"Validation failed: Keyname can't be blank"},
				{ "desc-specChars"+uid, "\\!@%^&*(<_-~+=//\\||,.>)", new Integer(0),null},
				{"desc-256Chars"+uid, strRepeat("0123456789", 25)+"abcdef",new Integer(166), "Validation failed: Value is too long (maximum is 255 characters)"},
				{strRepeat("0123456789", 25)+"abcdef", "desc-256Chars", new Integer(166), "Validation failed: Keyname is too long (maximum is 255 characters)"},
		};
	}
}
