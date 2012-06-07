package com.redhat.qe.katello.base.obj;

import java.util.ArrayList;
import javax.management.Attribute;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloGpgKey {
	public static final String GPG_PUBKEY_RPM_ZOO = 
			"gpg-pubkey-f78fb195-4f0d5ba1";
	public static final String REPO_GPG_FILE_ZOO = 
			"http://inecas.fedorapeople.org/fakerepos/zoo/RPM-GPG-KEY-dummy-packages-generator";
	
	// ** ** ** ** ** ** ** Public constants
	public static final String CLI_CMD_CREATE = "gpg_key create";
	public static final String CLI_CMD_INFO = "gpg_key info";
	public static final String CLI_CMD_LIST = "gpg_key list -v";
	public static final String CLI_CMD_DELETE = "gpg_key delete";
	
	public static final String OUT_CREATE = 
			"Successfully created GPG key [ %s ]"; 
	public static final String ERR_KEY_NOT_FOUND = 
			"Could not find gpg key [ %s ]"; 

	// ** ** ** ** ** ** ** Class members
	String name;
	String org;
	String file;
		
	private KatelloCli cli;
	private ArrayList<Attribute> opts;
	
	public KatelloGpgKey(String pName, String pOrg, String pFile){
		this.name = pName;
		this.org = pOrg;
		this.file = pFile;
		this.opts = new ArrayList<Attribute>();
	}
	
	public SSHCommandResult cli_create(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("file", file));
		cli = new KatelloCli(CLI_CMD_CREATE, opts);
		return cli.run();
	}

	public SSHCommandResult cli_info(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		cli = new KatelloCli(CLI_CMD_INFO, opts);
		return cli.run();
	}
	
	public SSHCommandResult cli_list(){
		opts.clear();
		opts.add(new Attribute("org", org));
		cli = new KatelloCli(CLI_CMD_LIST, opts);
		return cli.run();
	}

	public SSHCommandResult cli_delete(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		cli = new KatelloCli(CLI_CMD_DELETE, opts);
		return cli.run();
	}

	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **
	
	
}
