package com.redhat.qe.katello.base.obj;

import java.util.logging.Logger;
import javax.management.Attribute;
import org.codehaus.jackson.annotate.JsonProperty;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloOrg extends _KatelloObject{
    protected static Logger log = Logger.getLogger(KatelloOrg.class.getName());
    private static String defaultOrg = null;

	// ** ** ** ** ** ** ** Public constants
	public static final String DEFAULT_ORG = "ACME_Corporation";
	
	public static final String CLI_CMD_CREATE = "org create";
	public static final String CLI_CMD_INFO = "org info";
	public static final String CLI_CMD_LIST = "org list";
	public static final String CMD_SUBSCRIPTIONS = "org subscriptions";
	public static final String CMD_UEBERCERT = "org uebercert";
	public static final String CMD_DELETE = "org delete";
	public static final String CMD_UPDATE = "org update";
	public static final String CMD_ADD_SYS_INFO = "org add_default_system_info";
	public static final String CMD_APPLY_SYS_INFO = "org apply_default_system_info";
	public static final String CMD_REMOVE_SYS_INFO = "org remove_default_system_info";
	
	public static final String API_CMD_INFO = "/organizations/%s";
	
	public static final String OUT_CREATE = 
			"Successfully created org [ %s ]";
	public static final String ERR_ORG_EXISTS_MUST_BE_UNIQUE = 
			"Validation failed: Name has already been taken, Label already exists (including organizations being deleted), Organization Names and labels must be unique across all organizations";
	public static final String ERR_ORG_EXISTS = 
			"Validation failed: Name has already been taken, Label already exists (including organizations being deleted)";
	public static final String OUT_ADD_SYS_INFO = 
			"Successfully added default custom info key [ %s ] to Org [ %s ]";
	public static final String OUT_APPLY_SYS_INFO = 
			"Successfully applied default custom info keys to [ %s ] systems in Org [ %s ]";
	public static final String OUT_REMOVE_SYS_INFO = 
			"Successfully removed default custom info key [ %s ] for Org [ %s ]";
	
	public static final String ERR_NAME_INVALID = 
			"Validation failed: Name cannot contain characters other than alpha numerals, space,'_', '-'.";
	public static final String ERR_ORG_NOTFOUND = 
			"Couldn't find organization '%s'";
	public static final String ERR_ORG_NAME_EXISTS = 
			"Validation failed: Name has already been taken";
	public static final String ERR_ORG_LABEL_EXISTS = 
			"Validation failed: Label already exists (including organizations being deleted)";
	
	public static final String REG_ORG_LIST = ".*ID\\s*:\\s+\\d+.*Name\\s*:\\s+%s.*Description\\s*:\\s+%s.*";
	public static final String REG_ORG_INFO = ".*ID\\s*:\\s+\\d+.*Name\\s*:\\s+%s.*Description\\s*:\\s+%s.*";
	public static final String REG_CUSTOM_INFO = ".*Default System Info Keys\\s*:\\s+[\\s+%s\\s+].*";
	
	public static final String OUT_ORG_SUBSCR = ".*Subscription\\s*:\\s*%s.*";
	
	// ** ** ** ** ** ** ** Class members
	public String name;
	public String description;
	public String label;
	private Long id;
	private String cpKey;
	
	public KatelloOrg(){super();}
	
	public KatelloOrg(String pName, String pDesc){
		this.name = pName;
		this.description = pDesc;
	}
	
	protected KatelloOrg(Long id, String name, String description) {
	    this(name, description);
	    this.id = id;
	}
	
	public KatelloOrg(String name, String description, String label) {
	    this(name, description);
	    this.label = label;
	}

	public Long getId() {
	    return id;
	}
	
	public void setId(Long id) {
	    this.id = id;
	}
	
	public String getName() {
	    return name;
	}
	
	public void setName(String name) {
	    this.name = name;
	}
	
	@JsonProperty("cp_key")
	public String getCpKey() {
	    return cpKey;
	}

	@JsonProperty("cp_key")
	public void setCpKey(String cpKey) {
	    this.cpKey = cpKey;
	}
	
	public String getLabel() {
	    return label;
	}
	
	public void setLabel(String label) {
	    this.label = label;
	}

	public SSHCommandResult cli_create(){		
		opts.clear();
		opts.add(new Attribute("name", this.name));
		opts.add(new Attribute("description", this.description));
		opts.add(new Attribute("label", this.label));
		return run(CLI_CMD_CREATE);
	}
	
	public SSHCommandResult cli_info(){
		opts.clear();
		opts.add(new Attribute("name", this.name));
		return run(CLI_CMD_INFO);
	}
	
	public SSHCommandResult cli_list(){
		opts.clear();
		return run(CLI_CMD_LIST+" -v");
	}
		
	public SSHCommandResult subscriptions(){
		opts.clear();
		opts.add(new Attribute("name", this.name));
		return run(CMD_SUBSCRIPTIONS);
	}

	public SSHCommandResult uebercert(){
		opts.clear();
		opts.add(new Attribute("name", this.name));
		return run(CMD_UEBERCERT);
	}

	public SSHCommandResult delete(){
		opts.clear();
		opts.add(new Attribute("name", this.name));
		return run(CMD_DELETE);
	}
	
	public SSHCommandResult update(String new_description){
		opts.clear();
		opts.add(new Attribute("name", this.name));
		opts.add(new Attribute("description", new_description));
		return run(CMD_UPDATE);
	}

	public SSHCommandResult add_system_info(String keyname){
		opts.clear();
		opts.add(new Attribute("name", this.name));
		opts.add(new Attribute("keyname", keyname));
		return run(CMD_ADD_SYS_INFO);
	}

	public SSHCommandResult apply_system_info(){
		opts.clear();
		opts.add(new Attribute("name", this.name));
		return run(CMD_APPLY_SYS_INFO);
	}
	
	public SSHCommandResult remove_system_info(String keyname){
		opts.clear();
		opts.add(new Attribute("name", this.name));
		opts.add(new Attribute("keyname", keyname));
		return run(CMD_REMOVE_SYS_INFO);
	}
	
	public static String getDefaultOrg(){
		if(defaultOrg!=null) 
			return defaultOrg;
		SSHCommandResult res = KatelloUtils.sshOnServer("cat /etc/katello/katello-configure.conf | grep -E \"org_name\\s*=\" | cut -f2 -d\"=\" | sed 's/ *$//g' | sed 's/^ *//g'");
		defaultOrg = KatelloCliTestScript.sgetOutput(res);
		if(defaultOrg.isEmpty())
			defaultOrg = DEFAULT_ORG;
		return defaultOrg;
	}
	
	public static String getPoolId(String orgName, String productName){
		SSHCommandResult res = new KatelloOrg(orgName, null).subscriptions(); // all subscriptinos
		String outBlock = KatelloCli.grepOutBlock(
				"Subscription", productName, KatelloCliTestScript.sgetOutput(res)); // filter our product's output block
		return KatelloCli.grepCLIOutput("ID", outBlock); // grep poolid
	}

	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **
}
