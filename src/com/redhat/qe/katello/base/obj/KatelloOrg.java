package com.redhat.qe.katello.base.obj;

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import javax.management.Attribute;
import org.codehaus.jackson.annotate.JsonProperty;
import org.testng.Assert;

import com.redhat.qe.katello.base.KatelloCliTestBase;
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
	public static final String CMD_DEFAULT_INFO_ADD = "org default_info add";
	public static final String CMD_DEFAULT_INFO_APPLY = "org default_info sync";
	public static final String CMD_DEFAULT_INFO_REMOVE = "org default_info remove";
//	public static final String CMD_ADD_SYS_INFO = "org default_info add";
//	public static final String CMD_APPLY_SYS_INFO = "org default_info apply";
//	public static final String CMD_REMOVE_SYS_INFO = "org default_info remove --type system";
//	public static final String CMD_ADD_DISTRIBUTOR_INFO = "org default_info add";
//	public static final String CMD_APPLY_DISTRIBUTOR_INFO = "org default_info apply";
//	public static final String CMD_REMOVE_DISTRIBUTOR_INFO = "org default_info remove";
	
	public static final String API_CMD_INFO = "/organizations/%s";
	
	public static final String OUT_CREATE = 
			"Successfully created org [ %s ]";
	public static final String ERR_ORG_EXISTS_MUST_BE_UNIQUE = 
			"Validation failed: Name has already been taken, Label already exists (including organizations being deleted), Organization Names and labels must be unique across all organizations";
	public static final String ERR_ORG_EXISTS = 
			"Validation failed: Name has already been taken, Label already exists (including organizations being deleted)";
	public static final String OUT_ADD_SYS_INFO = 
			"Successfully added [ System ] default custom info [ %s ] to Org [ %s ]";
	public static final String OUT_ADD_DISTRIBUTOR_INFO =
			"Successfully added [ Distributor ] default custom info [ %s ] to Org [ %s ]";
	public static final String OUT_APPLY_SYS_INFO = 
			"Organization [ %s ] completed applying default info";
	public static final String OUT_REMOVE_SYS_INFO = 
			"Successfully removed [ System ] default custom info [ %s ] for Org [ %s ]";
	
	public static final String ERR_NAME_INVALID = 
			"Validation failed: Name cannot contain characters other than alpha numerals, space, '_', '-'";
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

	public SSHCommandResult update_servicelevel(String servicelevel){
		opts.clear();
		opts.add(new Attribute("name", this.name));
		opts.add(new Attribute("servicelevel", servicelevel));
		return run(CMD_UPDATE);
	}
	
	public SSHCommandResult default_info_add(String keyname, String type){
		opts.clear();
		opts.add(new Attribute("name", this.name));
		opts.add(new Attribute("keyname", keyname));
		opts.add(new Attribute("type", type));
		return run(CMD_DEFAULT_INFO_ADD);
	}

	public SSHCommandResult default_info_apply(String type){
		opts.clear();
		opts.add(new Attribute("name", this.name));
		opts.add(new Attribute("type", type));
		return run(CMD_DEFAULT_INFO_APPLY);
	}
	
	public SSHCommandResult default_info_remove(String keyname, String type){
		opts.clear();
		opts.add(new Attribute("name", this.name));
		opts.add(new Attribute("keyname", keyname));
		opts.add(new Attribute("type", type));
		return run(CMD_DEFAULT_INFO_REMOVE);
	}

//	public SSHCommandResult add_distributor_info(String keyname) {
//		opts.clear();
//		opts.add(new Attribute("name", this.name));
//		opts.add(new Attribute("keyname", keyname));
//		opts.add(new Attribute("type", "distributor"));
//		return run(CMD_DEFAULT_INFO_ADD);
//	}
//
//	public SSHCommandResult apply_distributor_info() {
//		opts.clear();
//		opts.add(new Attribute("name", this.name));
//		opts.add(new Attribute("type", "distributor"));
//		return run(CMD_DEFAULT_INFO_APPLY);
//	}
//
//	public SSHCommandResult remove_distributor_info(String keyname) {
//		opts.clear();
//		opts.add(new Attribute("name", this.name));
//		opts.add(new Attribute("keyname", keyname));
//		opts.add(new Attribute("type", "distributor"));
//		return run(CMD_DEFAULT_INFO_REMOVE);
//	}

	public ArrayList<String> custom_listNames(){
		ArrayList<String> _ret = new ArrayList<String>();
		opts.clear();
		SSHCommandResult res = runExt(CLI_CMD_LIST+" -v","| grep -e \"^Name\" |cut -f2 -d:");
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - exit.Code==0");
		StringTokenizer toks = new StringTokenizer(KatelloCliTestBase.sgetOutput(res), "\n");
		while(toks.hasMoreTokens()) 
			_ret.add(toks.nextToken().trim());
		return _ret;
	}
	
	public static String getDefaultOrg(){
		if(defaultOrg!=null) 
			return defaultOrg;
		SSHCommandResult res = KatelloUtils.sshOnServer("cat /etc/katello/katello-configure.conf | grep -E \"org_name\\s*=\" | cut -f2 -d\"=\" | sed 's/ *$//g' | sed 's/^ *//g'");
		defaultOrg = KatelloCliTestBase.sgetOutput(res);
		if(defaultOrg.isEmpty())
			defaultOrg = DEFAULT_ORG;
		return defaultOrg;
	}
	
	public static String getPoolId(String orgName, String productName){
		SSHCommandResult res = new KatelloOrg(orgName, null).subscriptions(); // all subscriptions
		String outBlock = KatelloUtils.grepOutBlock(
				"Subscription", productName, KatelloCliTestBase.sgetOutput(res)); // filter our product's output block
		return KatelloUtils.grepCLIOutput("ID", outBlock); // grep poolid
	}

	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **
}
