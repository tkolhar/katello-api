package com.redhat.qe.katello.base.obj;

import java.util.ArrayList;
import javax.management.Attribute;
import com.redhat.qe.auto.testng.Assert;
import com.redhat.qe.katello.base.KatelloApi;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloUser {
	
	// ** ** ** ** ** ** ** Public constants
	public static final String DEFAULT_ADMIN_USER = "admin";
	public static final String DEFAULT_ADMIN_PASS = "admin";
	public static final String DEFAULT_USER_PASS = "testing";
	
	public static final String CMD_CREATE = "user create";
	public static final String CLI_CMD_INFO = "user info";
	public static final String CLI_CMD_LIST = "user list";
	public static final String CMD_ASSIGN_ROLE = "user assign_role";
	public static final String CMD_DELETE_USER = "user delete";
	public static final String CMD_UNASSIGN_ROLE = "user unassign_role";
	public static final String CMD_LIST_ROLES = "user list_roles";
	public static final String CMD_REPORT = "user report";
	public static final String ERR_TEMPLATE_NOTFOUND = 
			"Could not find template [ %s ]";	
	public static final String OUT_CREATE = 
			"Successfully created user [ %s ]";
	public static final String OUT_DELETE =
			"Successfully deleted user [ %s ]";
	public static final String OUT_ASSIGN_ROLE =
			            "User \'%s\' assigned to role \'%s\'";
	public static final String OUT_UNASSIGN_ROLE =
            "User \'%s\' unassigned from role \'%s\'";
	public static final String OUT_FIND_USER_ERROR =
			"Could not find user [ %s ]";
	
	public static final String REG_USER_LIST = ".*Id:\\s+\\d+.*Username:\\s+%s.*Email:\\s+%s.*";
	public static final String REG_USER_ROLE_LIST = ".*\\d+\\s+%s.*";
	
	public static final String API_CMD_INFO = "/users/%s";
	public static final String API_CMD_LIST = "/users";
	
	// ** ** ** ** ** ** ** Class members
	public String username;
	public String email;
	public String password;
	public boolean disabled;
	public String orgname="";
	public String envname = "";

	private KatelloCli cli;
	private ArrayList<Attribute> opts;
	
	public KatelloUser(String pName, String pEmail, String pPassword, boolean pDisabled){
		this.username = pName;
		this.email = pEmail;
		this.password = pPassword;
		this.disabled = pDisabled;
		this.opts = new ArrayList<Attribute>();
	}
	
	public KatelloUser(String pName,String pEmail,String pPassword,boolean pDisabled,String pOrgname,String pEnvname){
		   this.username = pName;
		   this.email = pEmail;
		   this.password = pPassword;
		   this.disabled = pDisabled;
		   this.orgname = pOrgname;
		   this.envname = pEnvname;
		   this.opts = new ArrayList<Attribute>();
			
	}
	
	public static final SSHCommandResult report(String format)
	{
		
		    ArrayList<Attribute> opts= new ArrayList<Attribute>();
		    opts.clear();
		    if(!(format.isEmpty()))
		       opts.add(new Attribute("format",format));
		    KatelloCli cli = new KatelloCli(CMD_REPORT,opts);
		    return cli.run();
		    
		    
	}
	
	public SSHCommandResult create(){
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
		cli = new KatelloCli(CMD_CREATE, opts);
		return cli.run();
	}
	
	public SSHCommandResult cli_info(){
		opts.clear();
		opts.add(new Attribute("username", username));
		cli = new KatelloCli(CLI_CMD_INFO, opts);
		return cli.run();
	}
	
	public SSHCommandResult cli_list(){
		opts.clear();
		cli = new KatelloCli(CLI_CMD_LIST+" -v", opts);
		return cli.run();
	}
	
	public SSHCommandResult assign_role(String role){
		opts.clear();
		opts.add(new Attribute("username", username));
		opts.add(new Attribute("role", role));
		cli = new KatelloCli(CMD_ASSIGN_ROLE, opts);
		return cli.run();
	}
	
	public SSHCommandResult unassign_role(String role){
		opts.clear();
		opts.add(new Attribute("username", username));
		opts.add(new Attribute("role", role));
		cli = new KatelloCli(CMD_UNASSIGN_ROLE, opts);
		return cli.run();
	}
	
	public SSHCommandResult list_roles(){
		opts.clear();
		opts.add(new Attribute("username", username));
		cli = new KatelloCli(CMD_LIST_ROLES, opts);
		return cli.run();
	}
	
	public SSHCommandResult delete(){
		opts.clear();
		opts.add(new Attribute("username", this.username));
		cli = new KatelloCli(CMD_DELETE_USER, opts);
		return cli.run();
	}

	public SSHCommandResult api_info(String userid){
		return new KatelloApi().get(String.format(API_CMD_INFO,userid));
	}

	public SSHCommandResult api_list(){
		return new KatelloApi().get(API_CMD_LIST);
	}
	 
	public SSHCommandResult delete_user(String pName){
		 
		    opts.clear();
		    opts.add(new Attribute("username", username));
		    cli = new KatelloCli(CMD_DELETE_USER,opts);
		    return cli.run();
		     
	}
	
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
		String REGEXP_LIST = ".*Id:\\s+\\d+.*Username:\\s+%s.*Email:\\s+%s.*Disabled:\\s+False.*";
		if(this.disabled)
			REGEXP_LIST = ".*Id:\\s+\\d+.*Username:\\s+%s.*Email:\\s+%s.*Disabled:\\s+True.*";

		String match_info = String.format(REGEXP_LIST,
				this.username,this.email).replaceAll("\"", "");
		Assert.assertTrue(KatelloCliTestScript.sgetOutput(res).replaceAll("\n", "").matches(match_info), 
				String.format("User [%s] should be found in the list",this.username));
		
		// asserts: user info
		res = cli_info();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code ("+CLI_CMD_INFO+")");
		String REGEXP_INFO = ".*Id:\\s+\\d+.*Username:\\s+%s.*Email:\\s+%s.*Disabled:\\s+False.*";
		if(this.disabled)
			REGEXP_INFO = ".*Id:\\s+\\d+.*Username:\\s+%s.*Email:\\s+%s.*Disabled:\\s+True.*";
		match_info = String.format(REGEXP_INFO,
				this.username, this.email).replaceAll("\"", "");
		Assert.assertTrue(KatelloCliTestScript.sgetOutput(res).replaceAll("\n", "").matches(match_info), 
				String.format("User [%s] should contain correct info",this.username));			
	}
	
	
	
}
