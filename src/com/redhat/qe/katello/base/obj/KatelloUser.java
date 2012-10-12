package com.redhat.qe.katello.base.obj;

import javax.management.Attribute;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloUser extends _KatelloObject{

	// ** ** ** ** ** ** ** Public constants
	public static final String DEFAULT_ADMIN_USER = "admin";
	public static final String DEFAULT_ADMIN_PASS = "admin";
	public static final String DEFAULT_USER_PASS = "testing";
	public static final String DEFAULT_USER_EMAIL = "root@localhost";

	public static final String CMD_CREATE = "user create";
	public static final String CLI_CMD_INFO = "user info";
	public static final String CLI_CMD_LIST = "user list";
	public static final String CMD_ASSIGN_ROLE = "user assign_role";
	public static final String CMD_DELETE_USER = "user delete";
	public static final String CMD_UNASSIGN_ROLE = "user unassign_role";
	public static final String CMD_LIST_ROLES = "user list_roles";
	public static final String CMD_REPORT = "user report";
	public static final String OUT_CREATE = 
			"Successfully created user [ %s ]";
	public static final String OUT_DELETE =
			"Successfully deleted user [ %s ]";
	public static final String OUT_ASSIGN_ROLE =
			"User \'%s\' assigned to role \'%s\'";
	public static final String OUT_UNASSIGN_ROLE =
			"User \'%s\' unassigned from role \'%s\'";
	public static final String OUT_FIND_USER_ERROR =
			"Could not find user \'%s\'";

	public static final String REG_USER_LIST = ".*Id\\s*:\\s*\\d+.*Username\\s*:\\s*%s.*Email\\s*:\\s*%s.*";
	public static final String REG_USER_ROLE_LIST = ".*\\d+\\s*%s.*";

	public static final String API_CMD_INFO = "/users/%s";
	public static final String API_CMD_LIST = "/users";
	public static final String API_CMD_CREATE = "/users";

	// ** ** ** ** ** ** ** Class members
	public String username;
	public String email;
	public String password;
	public boolean disabled;
	public String orgname="";
	public String envname = "";
	private Long id;
	public String locale = "";
	
	
	public KatelloUser() {super();} // For resteasy

	public KatelloUser(String pName, String pEmail, String pPassword, boolean pDisabled){
		this.username = pName;
		this.email = pEmail;
		this.password = pPassword;
		this.disabled = pDisabled;
	}

	public KatelloUser(String pName, String pEmail, String pPassword, boolean pDisabled, String pLocale){
		this.username = pName;
		this.email = pEmail;
		this.password = pPassword;
		this.disabled = pDisabled;
		this.locale = pLocale;
	}
	
	public KatelloUser(String pName,String pEmail,String pPassword,boolean pDisabled,String pOrgname,String pEnvname){
		this(pName,pEmail, pPassword, pDisabled);
		this.orgname = pOrgname;
		this.envname = pEnvname;
	}

	public KatelloUser(String pName,String pEmail,String pPassword,boolean pDisabled,String pOrgname,String pEnvname, Long id){
		this(pName, pEmail, pPassword, pDisabled, pOrgname, pEnvname);
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public String getPassword() {
		return password;
	}

	public String getUsername() {
		return username;
	}

	public SSHCommandResult report(String format)
	{
		opts.clear();
		if(!(format.isEmpty()))
			opts.add(new Attribute("format",format));
		return run(CMD_REPORT);
	}

	public SSHCommandResult cli_create(){
		opts.clear();
		opts.add(new Attribute("username", username));
		opts.add(new Attribute("password", password));
		opts.add(new Attribute("email", email));
		if(disabled)
			opts.add(new Attribute("disabled", "true"));
		if(!(orgname.isEmpty()))
			opts.add(new Attribute("default_organization",orgname));
		if(!(envname.isEmpty()))
			opts.add(new Attribute("default_environment",envname));
		if(!(locale.isEmpty()))
			opts.add(new Attribute("default_locale",locale));
		return run(CMD_CREATE);
	}

	public SSHCommandResult cli_info(){
		opts.clear();
		opts.add(new Attribute("username", username));
		return run(CLI_CMD_INFO);
	}

	public SSHCommandResult cli_list(){
		opts.clear();
		return run(CLI_CMD_LIST+" -v");
	}

	public SSHCommandResult assign_role(String role){
		opts.clear();
		opts.add(new Attribute("username", username));
		opts.add(new Attribute("role", role));
		return run(CMD_ASSIGN_ROLE);
	}

	public SSHCommandResult unassign_role(String role){
		opts.clear();
		opts.add(new Attribute("username", username));
		opts.add(new Attribute("role", role));
		return run(CMD_UNASSIGN_ROLE);
	}

	public SSHCommandResult list_roles(){
		opts.clear();
		opts.add(new Attribute("username", username));
		return run(CMD_LIST_ROLES);
	}

	public SSHCommandResult delete(){
		opts.clear();
		opts.add(new Attribute("username", this.username));
		return run(CMD_DELETE_USER);
	}

	//	public static KatelloUser api_info(String userid) throws KatelloApiException {
	//		KatelloApiResponse response = KatelloApi.get(String.format(API_CMD_INFO,userid));
	//		if ( response.getReturnCode() < 300 ) {
	//		    String json = response.getContent();
	//		    JSONObject juser = KatelloTestScript.toJSONObj(json);
	//		    // String pName,String pEmail,String pPassword,boolean pDisabled,String pOrgname,String pEnvname
	//            String defOrg = juser.get("default_organization") == null ? null : (String)juser.get("default_organization");
	//            String defEnv = juser.get("default_environment") == null ? null : (String)juser.get("default_environment");
	//		    KatelloUser user = new KatelloUser((String)juser.get("username"), (String)juser.get("email"), (String)juser.get("password"), ((Boolean)juser.get("disabled")).booleanValue(), defOrg, defEnv, (Long)juser.get("id"));
	//		    return user;
	//		}
	//		throw new KatelloApiException(response);
	//	}
	//
	//	public static List<KatelloUser> api_list() throws KatelloApiException {
	//	    List<KatelloUser> users = new ArrayList<KatelloUser>();
	//		KatelloApiResponse response = KatelloApi.get(API_CMD_LIST);		
	//		if ( response.getReturnCode() < 300 ) {
	//		    String json = response.getContent();
	//		    JSONArray jusers = KatelloTestScript.toJSONArr(json);
	//		    for ( int i = 0; i < jusers.size(); ++i ) {
	//		        JSONObject juser = (JSONObject)jusers.get(i);
	//                String defOrg = juser.get("default_organization") == null ? null : (String)juser.get("default_organization");
	//                String defEnv = juser.get("default_environment") == null ? null : (String)juser.get("default_environment");
	//	            KatelloUser user = new KatelloUser((String)juser.get("username"), (String)juser.get("email"), (String)juser.get("password"), ((Boolean)juser.get("disabled")).booleanValue(), defOrg, defEnv, (Long)juser.get("id"));
	//	            users.add(user);
	//		    }
	//		    return users;
	//		}
	//		throw new KatelloApiException(response);
	//	}
	//
	//	public static KatelloUser api_create(String username, String email, String password, boolean disabled) throws KatelloApiException {
	//		List<NameValuePair> opts = new ArrayList<NameValuePair>();
	//		opts.add(new BasicNameValuePair("username", username));
	//		opts.add(new BasicNameValuePair("password", password));
	//		opts.add(new BasicNameValuePair("disabled", String.valueOf(disabled)));
	//		opts.add(new BasicNameValuePair("email", email));
	//		KatelloPostParam[] params = {new KatelloPostParam(null, opts)};
	//		KatelloApiResponse response = KatelloApi.post(params, API_CMD_CREATE);
	//		if ( response.getReturnCode() < 300 ) {
	//		    JSONObject juser = KatelloTestScript.toJSONObj(response.getContent());
	//            String defOrg = juser.get("default_organization") == null ? null : (String)juser.get("default_organization");
	//            String defEnv = juser.get("default_environment") == null ? null : (String)juser.get("default_environment");
	//            KatelloUser user = new KatelloUser((String)juser.get("username"), (String)juser.get("email"), (String)juser.get("password"), ((Boolean)juser.get("disabled")).booleanValue(), defOrg, defEnv, (Long)juser.get("id"));
	//            return user;
	//		}
	//		throw new KatelloApiException(response);
	//	}

	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **

	public void asserts_delete(){
		SSHCommandResult res;
		//asserts: user list
		res = cli_list();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code ("+CLI_CMD_LIST+")");

	}
	public void asserts_create(){
		SSHCommandResult res;

		// asserts: user list
		res = cli_list();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code ("+CLI_CMD_LIST+")");
		String REGEXP_LIST = ".*Id\\s*:\\s*\\d+.*Username\\s*:\\s*%s.*Email\\s*:\\s*%s.*Disabled\\s*:\\s*False.*Default Organization\\s*:\\s*%s.*Default Environment\\s*:\\s*%s.*";
		if(this.disabled)
			REGEXP_LIST = ".*Id\\s*:\\s*\\d+.*Username\\s*:\\s*%s.*Email\\s*:\\s*%s.*Disabled\\s*:\\s*True.*Default Organization\\s*:\\s*%s.*Default Environment\\s*:\\s*%s.*";

		String match_info = String.format(REGEXP_LIST,
				this.username,this.email, this.orgname != null ? this.orgname : "None", this.envname != null ? this.envname : "None").replaceAll("\"", "");

		Assert.assertTrue(KatelloCliTestScript.sgetOutput(res).replaceAll("\n", "").matches(match_info), 
				String.format("User [%s] should be found in the list",this.username));


		// asserts: user info
		res = cli_info();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code ("+CLI_CMD_INFO+")");
		String REGEXP_INFO = ".*Id\\s*:\\s*\\d+.*Username\\s*:\\s*%s.*Email\\s*:\\s*%s.*Disabled\\s*:\\s*False.*Default Organization\\s*:\\s*%s.*Default Environment\\s*:\\s*%s.*";
		if(this.disabled)
			REGEXP_INFO =  ".*Id\\s*:\\s*\\d+.*Username\\s*:\\s*%s.*Email\\s*:\\s*%s.*Disabled\\s*:\\s*True.*Default Organization\\s*:\\s*%s.*Default Environment\\s*:\\s*%s.*";
		match_info = String.format(REGEXP_INFO,
				this.username, this.email, this.orgname != null ? this.orgname : "None", this.envname != null ? this.envname : "None").replaceAll("\"", "");
		Assert.assertTrue(KatelloCliTestScript.sgetOutput(res).replaceAll("\n", "").matches(match_info), 
				String.format("User [%s] should contain correct info",this.username));			
	}

}
