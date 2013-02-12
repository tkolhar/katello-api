package com.redhat.qe.katello.base.obj;

import javax.management.Attribute;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloTemplate extends _KatelloObject{
	
	// ** ** ** ** ** ** ** Public constants
	public static final String CMD_CREATE = "template create";
	public static final String CMD_DELETE = "template delete";
	public static final String CMD_LIST = "template list -v";
	public static final String CMD_INFO = "template info -v";
	public static final String CMD_UPDATE = "template update";
	public static final String CMD_EXPORT = "template export";
	
	public static final String FORMAT_TDL = "tdl";
	
	public static final String ERR_TEMPL_NOTFOUND = 
			"Could not find template [ %s ] within environment [ %s ]";
	
	public static final String REG_TEMPL_INFO = ".*Id\\s*:\\s+\\d+.*Name\\s*:\\s+%s.*Revision\\s*:\\s+%s.*Description\\s*:\\s+%s.*Parent Id\\s*:\\s+%s.*";
	public static final String REG_TEMPL_PARAMS = ".*Parameters\\s*:\\s+.*%s\\s*:\\s+%s.*";
	public static final String REG_TEMPL_PACKAGES = ".*Packages\\s*:\\s+.*%s.*";
	public static final String REG_TEMPL_PACKAGEGROUPS = ".*Package Groups\\s*:\\s+.*%s.*";
	public static final String REG_TEMPL_REPOS = ".*Repositories\\s*:\\s+.*%s.*";
	public static final String REG_TEMPL_LIST = ".*\\s+\\d+.*\\s+%s.*\\s+%s.*\\s+%s.*";
	public static final String REG_TEMPL_ID = "Id:\\s+\\d+.*Name\\s*:";
	
	public static final String OUT_CREATE = 
			"Successfully created template [ %s ]";
	
	public static final String OUT_UPDATE = 
			"Successfully updated template [ %s ]";
	
	public static final String ERR_TDL_EXPORT_IMPOSSIBLE = 
			"Template cannot be exported: " +
			"At least repository must be present to export a TDL, " +
			"Exactly one distribution must be present to export a TDL";
	
	// ** ** ** ** ** ** ** Class members
	public String name;
	public String org;
	public String description;
	public String parent;
	public String parentId;
	public String revision;
	
	public KatelloTemplate(String pName, String pDesc,
			String pOrg, String pParent){
		this.name = pName;
		this.description = pDesc;
		this.org = pOrg;
		this.parent = pParent;
	}
	
	public SSHCommandResult create(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("description", description));
		opts.add(new Attribute("parent", parent));
		return run(CMD_CREATE);
	}
	
	public SSHCommandResult info(String environment){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("environment", environment));
		return run(CMD_INFO);
	}
	
	public SSHCommandResult delete(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		return run(CMD_DELETE);
	}
	
	public SSHCommandResult list(){
		opts.clear();
		opts.add(new Attribute("org", org));
		return run(CMD_LIST);
	}
	
	public SSHCommandResult update_add_distribution(String product, String distribution){
		opts.clear();
		opts.add(new Attribute("from_product", product));
		opts.add(new Attribute("add_distribution", distribution));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		return run(CMD_UPDATE);
	}
	
	public SSHCommandResult update_add_repo(String product, String repo){
		opts.clear();
		opts.add(new Attribute("from_product", product));
		opts.add(new Attribute("add_repository", repo));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		return run(CMD_UPDATE);
	}

	public SSHCommandResult update_remove_repo(String product, String repo){
		opts.clear();
		opts.add(new Attribute("from_product", product));
		opts.add(new Attribute("remove_repository", repo));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		return run(CMD_UPDATE);
	}
	
	public SSHCommandResult update_add_package(String pkg){
		return update_add_package(null, pkg);
	}
	
	public SSHCommandResult update_add_package(String product, String pkg){
		opts.clear();
		opts.add(new Attribute("from_product", product));
		opts.add(new Attribute("add_package", pkg));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		return run(CMD_UPDATE);
	}

	public SSHCommandResult update_remove_package(String pkg){
		return update_remove_package(null, pkg);
	}
	
	public SSHCommandResult update_remove_package(String product, String pkg){
		opts.clear();
		opts.add(new Attribute("from_product", product));
		opts.add(new Attribute("remove_package", pkg));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		return run(CMD_UPDATE);
	}
	
	public SSHCommandResult update_add_param(String param, String value){
		opts.clear();
		opts.add(new Attribute("add_param", param));
		opts.add(new Attribute("value", value));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		return run(CMD_UPDATE);
	}
	
	public SSHCommandResult update_remove_param(String param){
		opts.clear();
		opts.add(new Attribute("remove_param", param));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		return run(CMD_UPDATE);
	}
	
	public SSHCommandResult update_name(String newname){
		opts.clear();
		opts.add(new Attribute("new_name", newname));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		return run(CMD_UPDATE);
	}
	
	public SSHCommandResult update_add_package_group(String pkgGrp){
		opts.clear();
		opts.add(new Attribute("add_package_group", pkgGrp));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		return run(CMD_UPDATE);
	}

	public SSHCommandResult update_remove_package_group(String pkgGrp){
		opts.clear();
		opts.add(new Attribute("remove_package_group", pkgGrp));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		return run(CMD_UPDATE);
	}
	
	public SSHCommandResult export(String environment, String file, String format){
		opts.clear();
		opts.add(new Attribute("file", file));
		opts.add(new Attribute("format", format));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("environment", environment));
		return run(CMD_EXPORT);
	}
	
	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **
}
