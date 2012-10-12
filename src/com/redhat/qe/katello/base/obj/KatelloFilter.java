package com.redhat.qe.katello.base.obj;

import java.util.ArrayList;
import javax.management.Attribute;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloFilter extends _KatelloObject{
	
	// ** ** ** ** ** ** ** Public constants
	public static final String CMD_CREATE = "filter create";
	public static final String CLI_CMD_INFO = "filter info";
	public static final String CLI_CMD_LIST = "filter list";
	public static final String CMD_DELETE = "filter delete";
	public static final String CLI_CMD_ADD_PACKAGE = "filter add_package";
	public static final String CLI_CMD_REMOVE_PACKAGE = "filter remove_package";

	public static final String OUT_CREATE = 
			"Successfully created filter [ %s ]";
	
	public static final String ERR_FILTER_NOTFOUND = 
			"Couldn't find filter '%s'";
	
	public static final String OUT_PACKAGE_ADD = 
			"Successfully added package [ %s ] to filter [ %s ]";
	public static final String OUT_PACKAGE_REMOVE = 
			"Successfully removed package [ %s ] from filter [ %s ]";
	
	public static final String REG_FILTER_INFO = ".*Name\\s*:\\s+%s.*Description\\s*:\\s+%s.*Package List\\s*:\\s+%s.*";
	public static final String REG_FILTER_LIST = ".*%s\\s+%s.*";
	
	// ** ** ** ** ** ** ** Class members
	public String name;
	public String org;
	public String packages;
	public String description;
	
	public KatelloFilter(String pName, String pOrg, String pEnv, String pPackages){
		this.name = pName;
		this.org = pOrg;
		this.packages = pPackages;
		this.opts = new ArrayList<Attribute>();
	}
	
	public SSHCommandResult create(){
		opts.clear();
		opts.add(new Attribute("org", this.org));
		opts.add(new Attribute("name", this.name));
		opts.add(new Attribute("packages", this.packages));
		return run(CMD_CREATE);
	}
	
	public SSHCommandResult cli_info(){
		opts.clear();
		opts.add(new Attribute("org", this.org));
		opts.add(new Attribute("name", this.name));
		return run(CLI_CMD_INFO);
	}
	
	public SSHCommandResult cli_list(){
		opts.clear();
		opts.add(new Attribute("org", this.org));
		return run(CLI_CMD_LIST);
	}
	
	public SSHCommandResult delete(){
		opts.clear();
		opts.add(new Attribute("name", this.name));
		opts.add(new Attribute("org", this.org));
		return run(CMD_DELETE);
	}
	
	public SSHCommandResult cli_addPackage(String packageName){
		opts.clear();
		opts.add(new Attribute("name", this.name));
		opts.add(new Attribute("org", this.org));
		opts.add(new Attribute("package", packageName));
		return run(CLI_CMD_ADD_PACKAGE);
	}

	public SSHCommandResult cli_removePackage(String packageName){
		opts.clear();
		opts.add(new Attribute("name", this.name));
		opts.add(new Attribute("org", this.org));
		opts.add(new Attribute("package", packageName));
		return run(CLI_CMD_REMOVE_PACKAGE);
	}
	
	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **	
	
}
