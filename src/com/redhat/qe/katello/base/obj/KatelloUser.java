package com.redhat.qe.katello.base.obj;

import java.util.ArrayList;
import javax.management.Attribute;
import com.redhat.qe.auto.testng.Assert;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloUser {
	
	// ** ** ** ** ** ** ** Public constants
	public static final String DEFAULT_ADMIN_USER = "admin";
	public static final String DEFAULT_ADMIN_PASS = "admin";
	public static final String DEFAULT_USER_PASS = "testing";
	
	public static final String CMD_CREATE = "user create";
	public static final String CMD_INFO = "user info";
	public static final String CMD_LIST = "user list";
	public static final String CMD_DELETE = "user delete";
	public static final String CMD_ASSIGN_ROLE = "user assign_role";
	public static final String CMD_UNASSIGN_ROLE = "user unassign_role";
	public static final String CMD_LIST_ROLES = "user list_roles";
	
	public static final String ERR_TEMPLATE_NOTFOUND = 
			"Could not find template [ %s ]";	
	public static final String OUT_CREATE = 
			"Successfully created user [ %s ]";
	
	public static final String REG_USER_LIST = ".*Id:\\s+\\d+.*Username:\\s+%s.*Email:\\s+%s.*";
	public static final String REG_USER_ROLE_LIST = ".*\\d+\\s+%s.*";

	// ** ** ** ** ** ** ** Class members
	public String username;
	public String email;
	public String password;
	public boolean disabled;

	private KatelloCli cli;
	private ArrayList<Attribute> opts;
	
	public KatelloUser(String pName, String pEmail, String pPassword, boolean pDisabled){
		this.username = pName;
		this.email = pEmail;
		this.password = pPassword;
		this.disabled = pDisabled;
		this.opts = new ArrayList<Attribute>();
	}
	
	public SSHCommandResult create(){
		opts.clear();
		opts.add(new Attribute("username", username));
		opts.add(new Attribute("password", password));
		opts.add(new Attribute("email", email));
		if(disabled)
			opts.add(new Attribute("disabled", "true"));
		cli = new KatelloCli(CMD_CREATE, opts);
		return cli.run();
	}
	
	public SSHCommandResult info(){
		opts.clear();
		opts.add(new Attribute("username", username));
		cli = new KatelloCli(CMD_INFO, opts);
		return cli.run();
	}
	
	public SSHCommandResult list(){
		opts.clear();
		cli = new KatelloCli(CMD_LIST+" -v", opts);
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
		cli = new KatelloCli(CMD_DELETE, opts);
		return cli.run();
	}

	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **
	public void asserts_create(){
		SSHCommandResult res;

		// asserts: user list
		res = list();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code ("+CMD_LIST+")");
		String REGEXP_LIST = ".*Id:\\s+\\d+.*Username:\\s+%s.*Email:\\s+%s.*Disabled:\\s+False.*";
		if(this.disabled)
			REGEXP_LIST = ".*Id:\\s+\\d+.*Username:\\s+%s.*Email:\\s+%s.*Disabled:\\s+True.*";

		String match_info = String.format(REGEXP_LIST,
				this.username,this.email).replaceAll("\"", "");
		Assert.assertTrue(KatelloCliTestScript.sgetOutput(res).replaceAll("\n", "").matches(match_info), 
				String.format("User [%s] should be found in the list",this.username));
		
		// asserts: user info
		res = info();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code ("+CMD_INFO+")");
		String REGEXP_INFO = ".*Id:\\s+\\d+.*Username:\\s+%s.*Email:\\s+%s.*Disabled:\\s+False.*";
		if(this.disabled)
			REGEXP_INFO = ".*Id:\\s+\\d+.*Username:\\s+%s.*Email:\\s+%s.*Disabled:\\s+True.*";
		match_info = String.format(REGEXP_INFO,
				this.username, this.email).replaceAll("\"", "");
		Assert.assertTrue(KatelloCliTestScript.sgetOutput(res).replaceAll("\n", "").matches(match_info), 
				String.format("User [%s] should contain correct info",this.username));			
	}
	
}
