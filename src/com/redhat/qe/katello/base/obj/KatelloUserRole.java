package com.redhat.qe.katello.base.obj;

import java.util.ArrayList;

import javax.management.Attribute;

import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloUserRole {
	
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
			"Cannot find user role \'%s\'";
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
	
	private KatelloCli cli;
	private ArrayList<Attribute> opts;

	public KatelloUserRole(String pName, String pDesc){
		this.name = pName;
		this.description = pDesc;
		this.opts = new ArrayList<Attribute>();
	}
	
	public SSHCommandResult create(){
		opts.clear();
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("description", description));
		cli = new KatelloCli(CMD_CREATE, opts);
		return cli.run();
	}
	
	
	public SSHCommandResult cli_info(){
		opts.clear();
		opts.add(new Attribute("name", name));
		cli = new KatelloCli(CMD_INFO, opts);
		return cli.run();
	}
	

	public SSHCommandResult cli_list(){
		opts.clear();
		cli = new KatelloCli(CLI_CMD_LIST, opts);
		return cli.run();
	}
	
	
	public SSHCommandResult cli_update(String new_name){
		opts.clear();
		opts.add(new Attribute("new_name", new_name));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("description", description));
		cli = new KatelloCli(CMD_UPDATE, opts);
		return cli.run();
	}
	
	
	public SSHCommandResult cli_delete(){
		opts.clear();
		opts.add(new Attribute("name", name));
		cli = new KatelloCli(CMD_DELETE, opts);
		return cli.run();
	}
	
	
	public SSHCommandResult cli_add_ldap_group(String group_name){
		opts.clear();
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("group_name",group_name));
		cli = new KatelloCli(CMD_LDAP_GRP_ADD, opts);
		return cli.run();
	}
	
	
	public SSHCommandResult cli_remove_ldap_group(String group_name){
		opts.clear();
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("group_name",group_name));
		cli = new KatelloCli(CMD_LDAP_GRP_REMOVE, opts);
		return cli.run();
	}
}
