package com.redhat.qe.katello.base.obj;

import javax.management.Attribute;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.threading.KatelloCliWorker;
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
	public static final String CMD_UPDATE = "user update";
	public static final String OUT_CREATE = 
			"Successfully created user [ %s ]";
	public static final String OUT_UPDATE = 
			"Successfully updated user [ %s ]"; 
	public static final String OUT_DELETE =
			"Successfully deleted user [ %s ]";
	public static final String OUT_ASSIGN_ROLE =
			"User \'%s\' assigned to role \'%s\'";
	public static final String OUT_UNASSIGN_ROLE =
			"User \'%s\' unassigned from role \'%s\'";
	public static final String OUT_FIND_USER_ERROR =
			"Could not find user [ %s ]";

	public static final String ERR_INVALID_CREDENTIALS = 
			"Invalid credentials";
	public static final String ERR_NOT_ALLOWED_TO_ACCESS = 
			"User %s is not allowed to access";
	public static final String ERR_LOCALE =
			"Validation failed: Default locale must be one of bn, de, en, es, fr, gu, hi, it, ja, kn, ko, mr, or, pa, pt-BR, ru, ta, te, zh-CN, zh-TW";
	
	public static final String REG_USER_LIST = ".*ID\\s*:\\s*\\d+.*Username\\s*:\\s*%s.*Email\\s*:\\s*%s.*";
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

	public KatelloUser(KatelloCliWorker kcr, String pName, String pEmail, String pPassword, boolean pDisabled){
		this.username = pName;
		this.email = pEmail;
		this.password = pPassword;
		this.disabled = pDisabled;
		this.kcr = kcr;
	}

	public KatelloUser(KatelloCliWorker kcr, String pName, String pEmail, String pPassword, boolean pDisabled, String pLocale){
		this.username = pName;
		this.email = pEmail;
		this.password = pPassword;
		this.disabled = pDisabled;
		this.locale = pLocale;
		this.kcr = kcr;
	}
	
	public KatelloUser(KatelloCliWorker kcr, String pName,String pEmail,String pPassword,boolean pDisabled,String pOrgname,String pEnvname){
		this(kcr, pName,pEmail, pPassword, pDisabled);
		this.orgname = pOrgname;
		this.envname = pEnvname;
	}

	public KatelloUser(KatelloCliWorker kcr, String pName,String pEmail,String pPassword,boolean pDisabled,String pOrgname,String pEnvname, Long id){
		this(kcr, pName, pEmail, pPassword, pDisabled, pOrgname, pEnvname);
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
	
	public SSHCommandResult update_defaultOrgEnv(String org, String env){
		opts.clear();
		opts.add(new Attribute("username", username));
		opts.add(new Attribute("default_organization", org));
		opts.add(new Attribute("default_environment", env));
		return run(CMD_UPDATE);
	}

	public SSHCommandResult update_defaultOrg(String org){
		opts.clear();
		opts.add(new Attribute("username", username));
		opts.add(new Attribute("default_organization", org));
		return run(CMD_UPDATE+" --no_default_environment");
	}
	
	public SSHCommandResult update_userCredentials(String password, String email, boolean isDisabled){
		opts.clear();
		opts.add(new Attribute("username", username));
		opts.add(new Attribute("password", password));
		opts.add(new Attribute("email", email));
		opts.add(new Attribute("disabled", isDisabled));
		return run(CMD_UPDATE);
	}

	public SSHCommandResult update_locale(String loc) {
		opts.clear();
		opts.add(new Attribute("username", username));
		opts.add(new Attribute("default_locale", loc));
		return run(CMD_UPDATE);
	}

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
		String REGEXP_LIST = ".*ID\\s*:\\s*\\d+.*Username\\s*:\\s*%s.*Email\\s*:\\s*%s.*Disabled\\s*:\\s*False.*Default Organization\\s*:\\s*%s.*Default Environment\\s*:\\s*%s.*";
		if(this.disabled)
			REGEXP_LIST = ".*ID\\s*:\\s*\\d+.*Username\\s*:\\s*%s.*Email\\s*:\\s*%s.*Disabled\\s*:\\s*True.*Default Organization\\s*:\\s*%s.*Default Environment\\s*:\\s*%s.*";

		String match_info = String.format(REGEXP_LIST,
				this.username,this.email, this.orgname != null ? this.orgname : "None", this.envname != null ? this.envname : "None").replaceAll("\"", "");

		Assert.assertTrue(KatelloCliTestBase.sgetOutput(res).replaceAll("\n", "").matches(match_info), 
				String.format("User [%s] should be found in the list",this.username));

		// asserts: user info
		res = cli_info();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code ("+CLI_CMD_INFO+")");
		String REGEXP_INFO = ".*ID\\s*:\\s*\\d+.*Username\\s*:\\s*%s.*Email\\s*:\\s*%s.*Disabled\\s*:\\s*False.*Default Organization\\s*:\\s*%s.*Default Environment\\s*:\\s*%s.*";
		if(this.disabled)
			REGEXP_INFO =  ".*ID\\s*:\\s*\\d+.*Username\\s*:\\s*%s.*Email\\s*:\\s*%s.*Disabled\\s*:\\s*True.*Default Organization\\s*:\\s*%s.*Default Environment\\s*:\\s*%s.*";
		match_info = String.format(REGEXP_INFO,
				this.username, this.email, this.orgname != null ? this.orgname : "None", this.envname != null ? this.envname : "None").replaceAll("\"", "");
		Assert.assertTrue(KatelloCliTestBase.sgetOutput(res).replaceAll("\n", "").matches(match_info), 
				String.format("User [%s] should contain correct info",this.username));			
	}

}
