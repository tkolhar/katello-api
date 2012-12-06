package com.redhat.qe.katello.base.obj;

import javax.management.Attribute;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloUserRole extends _KatelloObject{
	
	public static final String ROLE_READ_EVERYTHING = "Read Everything";
	public static final String ROLE_ADMINISTRATOR = "Administrator";
	
	// ** ** ** ** ** ** ** Public constants
	public static final String CMD_CREATE = "user_role create";
	public static final String OUT_CREATE = "Successfully created user role [ %s ]";
	public static final String CMD_INFO = "user_role info -v";
	public static final String CLI_CMD_LIST = "user_role list -v";
	public static final String CMD_UPDATE = "user_role update";
	public static final String CMD_DELETE = "user_role delete";
	public static final String CMD_LDAP_GRP_ADD = "user_role add_ldap_group";
	public static final String CMD_LDAP_GRP_REMOVE = "user_role remove_ldap_group";
	
	public static final String ERROR_INFO =
			"Cannot find user role '%s'";
	public static final String OUT_UPDATE =  
			"Successfully updated user role [ %s ]";
	public static final String OUT_DELETE = 
			"Successfully deleted user role [ %s ]";
	public static final String OUT_LDAP_ADD =
			"Successfully added LDAP group [ %s ] to the user role [ %s ]";
	public static final String  OUT_LDAP_REMOVE =
			"Successfully removed LDAP group [ %s ] from the user role [ %s ]"; 
	
	// ** ** ** ** ** ** ** Class members
	public String name;
	public String description;
	
	public KatelloUserRole(String pName, String pDesc){
		this.name = pName;
		this.description = pDesc;
	}
	
	public SSHCommandResult create(){
		opts.clear();
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("description", description));
		return run(CMD_CREATE);
	}
	
	
	public SSHCommandResult cli_info(){
		opts.clear();
		opts.add(new Attribute("name", name));
		return run(CMD_INFO);
	}
	

	public SSHCommandResult cli_list(){
		opts.clear();
		return run(CLI_CMD_LIST);
	}
	
	
	public SSHCommandResult cli_update(String new_name){
		opts.clear();
		opts.add(new Attribute("new_name", new_name));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("description", description));
		return run(CMD_UPDATE);
	}
	
	
	public SSHCommandResult cli_delete(){
		opts.clear();
		opts.add(new Attribute("name", name));
		return run(CMD_DELETE);
	}
	
	
	public SSHCommandResult cli_add_ldap_group(String group_name){
		opts.clear();
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("group_name",group_name));
		return run(CMD_LDAP_GRP_ADD);
	}
	
	
	public SSHCommandResult cli_remove_ldap_group(String group_name){
		opts.clear();
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("group_name",group_name));
		return run(CMD_LDAP_GRP_REMOVE);
	}
}
