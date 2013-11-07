package com.redhat.qe.katello.base.obj;

import java.util.logging.Logger;

import javax.management.Attribute;

import com.redhat.qe.katello.base.threading.KatelloCliWorker;
import com.redhat.qe.tools.SSHCommandResult;

public class HammerPartitionTable extends _HammerObject{
    protected static Logger log = Logger.getLogger(HammerPartitionTable.class.getName());

	// ** ** ** ** ** ** ** Public constants
	public static final String CLI_CMD_CREATE = "partition_table create";
	public static final String CLI_CMD_INFO = "partition_table info";
	public static final String CMD_LIST = "partition_table list";
	public static final String CMD_DELETE = "partition_table delete";
	public static final String CMD_UPDATE = "partition_table update";
	public static final String CMD_DUMP = "partition_table dump";
	public static final String CMD_REMOVEOS = "partition_table remove_operatingsystem";
	public static final String CMD_ADDOS = "partition_table add_operatingsystem";
	
	public static final String OUT_CREATE = 
			"Partition table created";
	public static final String OUT_UPDATE = 
			"Partition table updated";
	public static final String OUT_DELETE = 
			"Partition table deleted";
	
	public static final String ERR_NAME_EXISTS = 
			"Name has already been taken";
	public static final String ERR_NOT_FOUND =
			"Partition table with id '%s' not found";
	
	// ** ** ** ** ** ** ** Class members
	public String Id;
	public String name;
	public String os_family;
	
	public HammerPartitionTable(){super();}
	
	public HammerPartitionTable(KatelloCliWorker kcr, String pName, String posFamily){
		this.name = pName;
		this.kcr = kcr;
		this.os_family = posFamily;
	}
	
	public String getName() {
	    return name;
	}
	
	public void setName(String name) {
	    this.name = name;
	}

	public SSHCommandResult cli_create(String file){		
		args.clear();
		args.add(new Attribute("name", this.name));
		args.add(new Attribute("os-family", this.os_family));
		args.add(new Attribute("file", file));
		return run(CLI_CMD_CREATE);
	}
	
	public SSHCommandResult cli_info(){
		args.clear();
		args.add(new Attribute("name", this.name));
		return run(CLI_CMD_INFO);
	}
	
	public SSHCommandResult cli_list(){
		args.clear();
		return run(CMD_LIST);
	}

	public SSHCommandResult cli_search(String search){
		args.clear();
		args.add(new Attribute("search", search));
		return run(CMD_LIST);
	}
	
	public SSHCommandResult cli_list(String order, Integer page, Integer per_page){
		args.clear();
		args.add(new Attribute("order", order));
		args.add(new Attribute("page", page));
		args.add(new Attribute("per-page", per_page));
		return run(CMD_LIST);
	}
	
	public SSHCommandResult cli_list(String searchStr, String order, String page, Integer per_page) {
		args.clear();
		args.add(new Attribute("search", searchStr));
		args.add(new Attribute("order", order));
		args.add(new Attribute("page", page));
		args.add(new Attribute("per-page", per_page));
		return run(CMD_LIST);
	}
	
	public SSHCommandResult update(String new_name, String file){
		args.clear();
		if (this.Id != null) {
			args.add(new Attribute("id", this.Id));	
		} else {
			args.add(new Attribute("name", this.name));
		}
		args.add(new Attribute("new-name", new_name));
		args.add(new Attribute("os-family", this.os_family));
		args.add(new Attribute("file", file));
		return run(CMD_UPDATE);
	}

	public SSHCommandResult add_os(String os_id){
		args.clear();
		if (this.Id != null) {
			args.add(new Attribute("id", this.Id));	
		} else {
			args.add(new Attribute("name", this.name));
		}
		args.add(new Attribute("operatingsystem-id", os_id));
		return run(CMD_ADDOS);
	}

	public SSHCommandResult remove_os(String os_id){
		args.clear();
		if (this.Id != null) {
			args.add(new Attribute("id", this.Id));	
		} else {
			args.add(new Attribute("name", this.name));
		}
		args.add(new Attribute("operatingsystem-id", os_id));
		return run(CMD_REMOVEOS);
	}
	
	public SSHCommandResult delete() {
		args.clear();
		if (this.Id != null) {
			args.add(new Attribute("id", this.Id));	
		} else {
			args.add(new Attribute("name", this.name));
		}
		return run(CMD_DELETE);
	}

	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **
}
