package com.redhat.qe.katello.base;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.testng.annotations.DataProvider;

import com.redhat.qe.katello.base.obj.KatelloActivationKey;
import com.redhat.qe.katello.base.obj.KatelloDistributor;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.tests.installation.TestMultipleAgentsDummy;

public class KatelloCliDataProvider {

	
	@DataProvider(name = "org_create")
	public static Object[][] org_create(){
		String uniqueID1 = KatelloUtils.getUniqueID();
		String uniqueID2 = KatelloUtils.getUniqueID();
		//name,label, description, ExitCode, Output
		return new Object[][] {
				{ "orgNoDescr_"+uniqueID1,null, null, new Integer(0), String.format(KatelloOrg.OUT_CREATE, "orgNoDescr_"+uniqueID1)},
				{ "org "+uniqueID2+"", null, "Org with space", new Integer(0), String.format(KatelloOrg.OUT_CREATE, "org "+uniqueID2+"")},
				{strRepeat("0123456789", 25)+"abcde", null, "Org name with 255 characters", new Integer(0), String.format(KatelloOrg.OUT_CREATE, strRepeat("0123456789", 25)+"abcde")},
				//{"\\!@%^&*(<_-~+=//\\||,.>)"+uniqueID1, null, "Org name with special characters", new Integer(0), String.format(KatelloOrg.OUT_CREATE, "\\!@%^&*(<_-~+=//\\||,.>)"+uniqueID1)},
				{"!@#$%^&*()_+{}|:?[];.,"+uniqueID1, null, "Org name with special characters", new Integer(0), String.format(KatelloOrg.OUT_CREATE, "!@#$%^&*()_+{}|:?[];.,"+uniqueID1)}
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
				{ "aa", null, null, new Integer(0), String.format(KatelloProvider.OUT_CREATE,"aa")},
				{ "11", null, null, new Integer(0), String.format(KatelloProvider.OUT_CREATE,"11")},
				{ "1a", null, null, new Integer(0), String.format(KatelloProvider.OUT_CREATE,"1a")},
				{ "a1", null, null, new Integer(0), String.format(KatelloProvider.OUT_CREATE,"a1")},
				{ strRepeat("0123456789", 25)+"abcde", null, null, new Integer(0), String.format(KatelloProvider.OUT_CREATE,strRepeat("0123456789", 25)+"abcde")},
				{ "prov-"+uid, null, null, new Integer(0), String.format(KatelloProvider.OUT_CREATE,"prov-"+uid)},
				{ "prov "+uid, "Provider with space in name", null, new Integer(0), String.format(KatelloProvider.OUT_CREATE,"prov "+uid)},
				{ null, null, null, new Integer(2), System.getProperty("katello.engine", "katello")+": error: Option --name is required; please see --help"},
				{ " ", null, null, new Integer(166), "Name can't be blank"},
				{ " a", null, null, new Integer(166), "Validation failed: Name must not contain leading or trailing white spaces."},
				{ "a ", null, null, new Integer(166), "Validation failed: Name must not contain leading or trailing white spaces."},
				{ "a", null, null, new Integer(0), String.format(KatelloProvider.OUT_CREATE,"a")},
				{ "\\!@%^&*(<_-~+=//\\||,.>)", "Name with special characters", null, new Integer(0), String.format(KatelloProvider.OUT_CREATE, "\\!@%^&*(<_-~+=//\\||,.>)")},
				{ strRepeat("0123456789", 25)+"abcdef", null, null, new Integer(166), "Validation failed: Name cannot contain more than 255 characters"},
				// description
				{ "desc-specChars"+uid, "\\!@%^&*(<_-~+=//\\||,.>)", null, new Integer(0), String.format(KatelloProvider.OUT_CREATE,"desc-specChars"+uid)},
				{ "desc-255Chars"+uid, strRepeat("0123456789", 25)+"abcde", null, new Integer(0), String.format(KatelloProvider.OUT_CREATE,"desc-255Chars"+uid)},
				{ "desc-256Chars"+uid, strRepeat("0123456789", 25)+"abcdef", null, new Integer(0), String.format(KatelloProvider.OUT_CREATE,"desc-256Chars"+uid)},
				// url
				{ "url-httpOnly"+uid, null, "http://", new Integer(2), System.getProperty("katello.engine", "katello")+": error: option --url: invalid format"}, // see below
				{ "url-httpsOnly"+uid, null, "https://", new Integer(2), System.getProperty("katello.engine", "katello")+": error: option --url: invalid format"}, // according changes of: tstrachota
				{ "url-redhatcom"+uid, null, "http://redhat.com/", new Integer(0), String.format(KatelloProvider.OUT_CREATE,"url-redhatcom"+uid)},
				{ "url-with_space"+uid, null, "http://url with space/", new Integer(0), String.format(KatelloProvider.OUT_CREATE,"url-with_space"+uid)},
				// misc
				{ "duplicate"+uid, null, null, new Integer(0), String.format(KatelloProvider.OUT_CREATE,"duplicate"+uid)},
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
		return new Object[][] {
				// name
				//String name, String scope,String tags,String verbs,Integer exitCode, String output
				{ "perm-all-verbs-"+uid, "environments", null, "read_contents,update_systems,delete_systems,read_systems,register_systems",new Integer(0),null},
				{ "perm-read_contents-env-"+uid, "environments", null, "read_contents",new Integer(0),null},
				{ "perm-read_update-env-"+uid, "environments", null, "read_contents,update_systems",new Integer(0),null},
				{ "perm-del_read_reg-verbs-env-"+uid, "environments", null, "delete_systems,read_systems,register_systems",new Integer(0),null},
				{ "perm-register-verbs-env-"+uid, "environments", null, "register_systems",new Integer(0),null},
				{ "perm-exclude_register-verbs-env-"+uid, "environments", null, "read_contents,update_systems,delete_systems,read_systems",new Integer(0),null},
				{ "perm-update_register-verbs-env-"+uid, "environments", null, "update_systems,register_systems",new Integer(0),null},
				{ "perm-read_update-verbs-provider-"+uid, "providers", null, "read,update",new Integer(0),null},
				{ "perm-read-verbs-provider-"+uid, "providers", null, "read",new Integer(0),null},
				{ "perm-update-verbs-provider-"+uid, "providers", null, "update",new Integer(0),null},
				{ "perm-all-org"+uid, "organizations", null, "delete_systems,update,update_systems,read,read_systems,register_systems",new Integer(0),null},
				{ "perm-all-tags-verbs-"+uid, "environments", "Library", "read_contents,update_systems,delete_systems,read_systems,register_systems",new Integer(0),null},
				{ "perm-some_verbs-org"+uid, "organizations", null, "update,update_systems,read,read_systems",new Integer(0),null},
		};
	}
	
	@DataProvider(name="permission_create_headpinOnly")
	public static Object[][] permission_create_headpinOnly(){
		String uid = KatelloUtils.getUniqueID();
		return new Object[][] {
				// name
				//String name, String scope,String tags,String verbs,Integer exitCode, String output
				{ "perm-read_update-verbs-provider-"+uid, "providers", null, "read,update",new Integer(0),null},
				{ "perm-read-verbs-provider-"+uid, "providers", null, "read",new Integer(0),null},
				{ "perm-update-verbs-provider-"+uid, "providers", null, "update",new Integer(0),null},
				{ "perm-all-org"+uid, "organizations", null, "delete_distributors,update_distributors,read_distributors,register_distributors,delete_systems,update,update_systems,read,read_systems,register_systems",new Integer(0),null},
				{ "perm-some_verbs-org"+uid, "organizations", null, "update,update_systems,read,read_systems",new Integer(0),null},
				{ "perm-some_dis_verbs-org"+uid, "organizations", null, "update,delete_distributors,update_distributors,read_distributors,register_distributors",new Integer(0),null},
				{ "perm-some_sys_verbs-org"+uid, "organizations", null, "delete_systems,update_systems,read_systems,register_systems",new Integer(0),null},
				{ "perm-read_verbs-org"+uid, "organizations", null, "read",new Integer(0),null},
				{ "perm-update_verbs-org"+uid, "organizations", null, "update",new Integer(0),null},
				{ "perm-update_system_verbs-org"+uid, "organizations", null, "update_systems",new Integer(0),null},
				{ "perm-update_dis_verbs-org"+uid, "organizations", null, "update_distributors",new Integer(0),null},
				{ "perm-delete_system_verbs-org"+uid, "organizations", null, "delete_systems",new Integer(0),null},
				{ "perm-delete_dis_verbs-org"+uid, "organizations", null, "delete_distributors",new Integer(0),null},
				{ "perm-register_dis_verbs-org"+uid, "organizations", null, "register_distributors",new Integer(0),null},
				{ "perm-register_sys_verbs-org"+uid, "organizations", null, "register_systems",new Integer(0),null},
				{ "perm-read_sys_verbs-org"+uid, "organizations", null, "read_systems",new Integer(0),null},
				{ "perm-read_dis_verbs-org"+uid, "organizations", null, "read_distributors",new Integer(0),null},
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
//			    { strRepeat("0123456789", 12)+"abcdefghi", null, new Integer(166), "Validation failed: Name is too long (maximum is 128 characters)"},
//				// description
				{ "desc-specChars"+uid, "\\!@%^&*(<_-~+=//\\||,.>)", new Integer(0), "Successfully created user role [ desc-specChars"+uid+" ]"},
				
				{ "desc-256Chars"+uid, strRepeat("0123456789", 25)+"abcdef", new Integer(0), "Successfully created user role [ desc-256Chars"+ uid +" ]"},
				// misc
				{ "duplicate"+uid, null, new Integer(0), "Successfully created user role [ duplicate"+uid+" ]"},
				
 				
		};
	}
	@DataProvider(name="environment_create")
	public static Object[][] environment_create(){
		String uid = KatelloUtils.getUniqueID();
		return new Object[][] {
				// name
				{ "env-aa", null, new Integer(0), String.format(KatelloEnvironment.OUT_CREATE, "env-aa")},
				{ "env-11", null, new Integer(0), String.format(KatelloEnvironment.OUT_CREATE, "env-11")},
				{ "env-1a", null, new Integer(0), String.format(KatelloEnvironment.OUT_CREATE, "env-1a")},
				{ "env-a1", null, new Integer(0), String.format(KatelloEnvironment.OUT_CREATE, "env-a1")},
				{ strRepeat("0123456789", 25)+"abcde", "Environment name with 255 characters", new Integer(0), String.format(KatelloEnvironment.OUT_CREATE, strRepeat("0123456789", 25)+"abcde")},
				{ "env-"+uid, null, new Integer(0), String.format(KatelloEnvironment.OUT_CREATE, "env-"+uid)},
				{ "env "+uid, "Provider with space in name", new Integer(0), String.format(KatelloEnvironment.OUT_CREATE, "env "+uid)},
				{ " ", null, new Integer(166), KatelloEnvironment.ERROR_BLANK_NAME},
				{ " a", null, new Integer(166), "Validation failed: Name must not contain leading or trailing white spaces."},
				{ "a ", null, new Integer(166), "Validation failed: Name must not contain leading or trailing white spaces."},
				{ "a", null, new Integer(0), String.format(KatelloEnvironment.OUT_CREATE, "a")},
				{ "\\!@%^&*(<_-~+=//\\||,.>)", "Environment name with special characters", new Integer(0), String.format(KatelloEnvironment.OUT_CREATE, "\\!@%^&*(<_-~+=//\\||,.>)")},
			    // description
				{ "desc-specChars"+uid, "\\!@%^&*(<_-~+=//\\||,.>)", new Integer(0), "Successfully created environment [ desc-specChars"+uid+" ]"},
				{ "desc-255Chars"+uid, strRepeat("0123456789", 25)+"abcde", new Integer(0), "Successfully created environment [ desc-255Chars"+uid+" ]"},
				{ "desc-256Chars"+uid, strRepeat("0123456789", 25)+"abcdef", new Integer(0), "Successfully created environment [ desc-256Chars"+uid+" ]"},
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
				{ "aa", null, new Integer(0), String.format(KatelloActivationKey.OUT_CREATE, "aa")},
				{ "11", null, new Integer(0), String.format(KatelloActivationKey.OUT_CREATE, "11")},
				{ "1a", null, new Integer(0), String.format(KatelloActivationKey.OUT_CREATE, "1a")},
				{ "a1", null, new Integer(0), String.format(KatelloActivationKey.OUT_CREATE, "a1")},
				{ strRepeat("0123456789", 25)+"abcde", null, new Integer(0), String.format(KatelloActivationKey.OUT_CREATE, strRepeat("0123456789", 25)+"abcde")},
				{ "ak-"+uid, null, new Integer(0), String.format(KatelloActivationKey.OUT_CREATE, "ak-"+uid)},
				{ "ak "+uid, "Provider with space in name", new Integer(0), String.format(KatelloActivationKey.OUT_CREATE, "ak "+uid)},
				{ " ", null, new Integer(166), KatelloActivationKey.ERROR_BLANK_NAME},
				{ " a", null, new Integer(166), KatelloActivationKey.ERROR_NAME_WHITESPACE},
				{ "a ", null, new Integer(166), KatelloActivationKey.ERROR_NAME_WHITESPACE},
				{ "a", null, new Integer(0), String.format(KatelloActivationKey.OUT_CREATE, "a")},
				{ "(\\!@%^&*(<_-~+=//\\||,.>)", null, new Integer(0), String.format(KatelloActivationKey.OUT_CREATE, "(\\!@%^&*(<_-~+=//\\||,.>)" )},
			    { strRepeat("0123456789", 25)+"abcdef", null, new Integer(166), KatelloActivationKey.ERROR_LONG_NAME},
//				// description
				{ "desc-specChars"+uid, "\\!@%^&*(<_-~+=//\\||,.>)", new Integer(0), String.format(KatelloActivationKey.OUT_CREATE, "desc-specChars"+uid)},
				{ "desc-255Chars"+uid, strRepeat("0123456789", 25)+"abcde", new Integer(0), String.format(KatelloActivationKey.OUT_CREATE, "desc-255Chars"+uid)},
				{ "desc-256Chars"+uid, strRepeat("0123456789", 25)+"abcdef", new Integer(0), String.format(KatelloActivationKey.OUT_CREATE, "desc-256Chars"+uid)},
				// misc
				{ "duplicate"+uid, null, new Integer(0), String.format(KatelloActivationKey.OUT_CREATE, "duplicate"+uid)},
 				{ "duplicate"+uid, null, new Integer(166), KatelloActivationKey.ERROR_DUPLICATE_NAME}
		};
	}
	
	@DataProvider(name="add_custom_info")
	public static Object[][] add_custom_info(){
		String uid = KatelloUtils.getUniqueID();
		return new Object[][] {
				{ "env-aa", "env-aa", new Integer(0),null},
				{ strRepeat("0123456789", 12)+"abcdefgh",strRepeat("0123456789", 12)+"abcdefgh", new Integer(0),null},
				{ " ", "value", new Integer(166),KatelloSystem.ERR_BLANK_KEYNAME},
				{ "desc-specChars"+uid, "\\!@%^&*(_-~+=\\||,.)", new Integer(0),null},
				{"desc-256Chars"+uid, strRepeat("0123456789", 25)+"abcdef",new Integer(166), "Validation failed: Value is too long (maximum is 255 characters)"},
				{strRepeat("0123456789", 25)+"abcdef", "desc-256Chars", new Integer(166), "Validation failed: Keyname is too long (maximum is 255 characters)"},
				{ "special chars <h1>html</h1>", "html in keyname", new Integer(0), null},
				{ "html in value", "special chars <h1>html</h1>", new Integer(0), null},
				{"key-blank-val", "", new Integer(2), "Usage: headpin <options> system custom_info add <options>\n\nheadpin: error: Option --value is required; please see --help"},
				{strRepeat("0123456789", 25)+"abcdef", "desc-256Chars", new Integer(166), KatelloSystem.ERR_KEY_TOO_LONG},
				{strRepeat("0123456789", 25)+"abcde", "desc-255Chars", new Integer(0), null},
				{"desc-255Chars", strRepeat("0123456789", 25)+"abcde", new Integer(0), null},
				{"desc-256Chars", strRepeat("0123456789", 25)+"abcdef", new Integer(166), KatelloSystem.ERR_VALUE_TOO_LONG},
				{"special:@!#$%^&*()","value_foo@!#$%^&*()", new Integer(0),null},
		};
	}

	/**
	 * 1. keyname<br>
	 * 2. keyvalue<br>
	 * 3. distributor name<br>
	 * 4. return code<br>
	 * 5. output/error string
	 * @return
	 */
	@DataProvider(name="add_distributor_custom_info")
	public static Object[][] add_distributor_custom_info()
	{
		String uid = KatelloUtils.getUniqueID();
		String dis_name = "distAddCustomInfo-"+uid;
		return new Object[][]{
				{"testkey-"+uid,"testvalue-"+uid,dis_name,new Integer(0), String.format(KatelloDistributor.OUT_INFO,"testkey-"+uid,"testvalue-"+uid,dis_name)},
				{"","blank-key"+uid,dis_name,new Integer(166),"Validation failed: Keyname can't be blank"},
				{"blank-value"+uid,"",dis_name,new Integer(0),String.format(KatelloDistributor.OUT_INFO,"blank-value"+uid,"",dis_name)},
				{strRepeat("0123456789",25)+"abcde","255charKey",dis_name,new Integer(0),String.format(KatelloDistributor.OUT_INFO,strRepeat("0123456789",25)+"abcde","255charKey",dis_name)},
				{strRepeat("0123456789",25)+"abcdef","256charKey",dis_name,new Integer(166),KatelloDistributor.ERR_KEY_TOO_LONG},
				{"255charValue"+uid, strRepeat("0123456789",25)+"abcde", dis_name,new Integer(0),String.format(KatelloDistributor.OUT_INFO,"255charValue"+uid, strRepeat("0123456789",25)+"abcde", dis_name)},
				{"256charValue"+uid, strRepeat("0123456789",25)+"abcdef", dis_name,new Integer(166), KatelloDistributor.ERR_VALUE_TOO_LONG},
				{"testkey-"+uid,"duplicate-key"+uid,dis_name,new Integer(166), KatelloDistributor.ERR_DUPLICATE_KEY},
				{"duplicate-value"+uid,"testvalue-"+uid,dis_name,new Integer(0),String.format(KatelloDistributor.OUT_INFO,"duplicate-value"+uid,"testvalue-"+uid,dis_name)},
				{"\\!@%^&*(_-~+=\\||,.)"+uid,"\\!@%^&*(_-~+=\\||,.)"+uid,dis_name,new Integer(0),String.format(KatelloDistributor.OUT_INFO,"\\!@%^&*(_-~+=\\||,.)"+uid,"\\!@%^&*(_-~+=\\||,.)"+uid,dis_name)},
				{"special chars <h1>html</h1>", "html in keyname", dis_name, new Integer(0), String.format(KatelloDistributor.OUT_INFO,"special chars <h1>html</h1>","html in keyname",dis_name)},
				{"html in value", "special chars <h1>html</h1>", dis_name, new Integer(0), String.format(KatelloDistributor.OUT_INFO,"html in value","special chars <h1>html</h1>",dis_name)},
		};
	}

	@DataProvider(name="org_add_custom_info")
	public static Object[][] org_add_custom_info() {
	
		return new Object[][] {
				{"custom-key", new Integer(0), null},
				{ " ", new Integer(166), KatelloOrg.ERR_BLANK_KEY},
				{ strRepeat("0123456789", 25)+"abcde", new Integer(0), null},
				//{ strRepeat("0123456789", 25)+"abcdef", new Integer(166), KatelloOrg.ERR_KEY_TOO_LONG},
				{ strRepeat("0123456789", 25)+"abcdef", new Integer(0), null},
				{ "custom-key", new Integer(166), KatelloOrg.ERR_DUPLICATE_DISTRIBUTOR_KEY},
				{ "special chars \\!@%^&*(_-~+=\\||,.)", new Integer(0), null},
				{ "special chars <h1>html</h1>", new Integer(0), null},
				
		};
	}
	
	@DataProvider(name="create_distributor")
	public static Object[][] create_distributor()
	{
		String uid = KatelloUtils.getUniqueID();
		return new Object[][]{
				{"test_distributor"+uid,new Integer(0), String.format(KatelloDistributor.OUT_CREATE,"test_distributor"+uid)},		
				{"",new Integer(166),"Validation failed: Name can't be blank"},
				{strRepeat("0123456789",12)+uid,new Integer(0),String.format(KatelloDistributor.OUT_CREATE,strRepeat("0123456789",12)+uid)},
				{strRepeat("0123456789",30)+uid,new Integer(166),"Validation failed: Name is too long (maximum is 250 characters)"},
				{"\\!@%^&*(<_-~+=//\\||,.>)"+uid,new Integer(144),""},				
				{"test_distributor"+uid,new Integer(166),"Validation failed: Name already taken"},	
		};		
	}
	
	@DataProvider(name = "user_create")
	public static Object[][] user_create(){
		String uid = KatelloUtils.getUniqueID();
		return new Object[][] {
				//name, email, password, disabled
				{"newUserName"+uid, "newUserName@redhat.com", "newUserName", false }, 
				{"նոր օգտվող"+uid, "newUser@redhat.com", "նոր օգտվող", false},
				{"新用戶"+uid, "newUser@redhat.com", "新用戶", false},
				{"नए उपयोगकर्ता"+uid, "newUser@redhat.com", "नए उपयोगकर्ता", false},
				{"нового пользователя"+uid, "newUser@redhat.com", "нового пользователя", false},
				{"uusi käyttäjä"+uid, "newUser@redhat.com", "uusi käyttäjä", false},
				{"νέος χρήστης"+uid, "newUser@redhat.com", "νέος χρήστης", false},
		};
	}

	@DataProvider(name="changeset_create")
	public static Object[][] changeset_create() {
		return new Object[][] {
				//{String name, String description, Integer exit_code, String output},
				{"changeset ok", "", new Integer(0), null},
				{"!@#$%^&*()_+{}|:?[];.,", "special characters", new Integer(0), null},
				{strRepeat("0123456789", 25)+"abcdef", "too long name", new Integer(166), "Validation failed: Name cannot contain more than 255 characters"},
				{"too long description", strRepeat("0123456789", 25)+"abcdef", new Integer(0), null},
				{"<h1>changeset</h1>", "html in name", new Integer(0), null},
				{"html in description", "<h1>changeset description</h1>", new Integer(0), null},
		};

	}
	protected static Logger log = Logger.getLogger(TestMultipleAgentsDummy.class.getName());
	
	@DataProvider(name="multiple_agents")
	public static Object[][] multiple_agents() {
		
		log.info("** ** ** "+System.getProperty("deltacloud.client.imageid","DUMMY !!!!"));
		List<Object[]> images = new ArrayList<Object[]>(); 
		StringTokenizer tok = new StringTokenizer(
				System.getProperty("deltacloud.client.imageid",""), ",");
		while (tok.hasMoreTokens()) {
			images.add(new Object[] {tok.nextToken().trim()});
		}
		return images.toArray(new Object[images.size()][]);
	}
}
