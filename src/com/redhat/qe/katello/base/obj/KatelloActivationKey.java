package com.redhat.qe.katello.base.obj;

import javax.management.Attribute;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.threading.KatelloCliWorker;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloActivationKey extends _KatelloObject{

	String org;
	String environment;
	String name;
	String description;
	String limit;
	String content_view;
	
	private String id;
	private String environment_id;
	
	public static final String CMD_CREATE = "activation_key create";
	public static final String CMD_INFO = "activation_key info";
	public static final String CMD_LIST = "activation_key list";
	public static final String CMD_DELETE = "activation_key delete";
	public static final String CMD_UPDATE = "activation_key update";
	public static final String CMD_ADD_SYSTEMGROUP = "activation_key add_system_group";
	public static final String CMD_REMOVE_SYSTEMGROUP = "activation_key remove_system_group";	
	public static final String OUT_CREATE = 
			"Successfully created activation key [ %s ]";
	public static final String OUT_DELETE =
			"Successfully deleted activation key [ %s ]";
	public static final String OUT_ADD_SYSTEMGROUP = 
			"Successfully added system group to activation key [ %s ]";
	public static final String OUT_REMOVE_SYSTEMGROUP = 
			"Successfully removed system group from activation key [ %s ]";
	public static final String ERROR_INFO =
			"Could not find activation key [ %s ]";
	public static final String ERROR_EXCEED =
			"Usage limit (%s) exhausted for activation key '%s'";	
	public static final String ERROR_BLANK_NAME = "Name can't be blank";
	public static final String ERROR_NAME_WHITESPACE = "Validation failed: Name must not contain leading or trailing white spaces.";
	public static final String ERROR_DUPLICATE_NAME = "Validation failed: Name has already been taken";
	public static final String ERROR_LONG_NAME = "Validation failed: Name cannot contain more than 255 characters";
	public static final String ERR_NO_KEYS = "No keys found in organization [ %s ]";
	public static final String ERR_NO_ENV_KEYS = "No keys found in organization [ %s ] environment [ %s ]";
	public static final String ERR_WRONG_LIMIT = "Usage limit [ %s ] must be higher than one";
	public static final String ERR_NOT_FOUND = "Could not find activation key [ %s ]";
	public static final String ERR_GROUP_NOT_FOUND = "Could not find system group [ %s ]";
	
	public KatelloActivationKey(KatelloCliWorker kcr, String pOrg, String pEnv, String pName, String pDesc){
		this(kcr, pOrg,pEnv,pName,pDesc,null);
		
	}
	
	public KatelloActivationKey(KatelloCliWorker kcr, String pOrg, String pEnv, String pName, String pDesc, String pLimit){
		this.org = pOrg;
		this.environment = pEnv;
		this.name = pName;
		this.description = pDesc;
		this.limit = pLimit;
		this.kcr = kcr;
	}
	
	public KatelloActivationKey(KatelloCliWorker kcr, String pOrg, String pEnv, String pName, String pDesc, String pLimit, String pContentView){
		this(kcr, pOrg,pEnv,pName,pDesc,pLimit);
		this.content_view = pContentView;
	}
	
	public SSHCommandResult create(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("environment", environment));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("description", description));
		opts.add(new Attribute("limit", limit));
		opts.add(new Attribute("content_view", content_view));
		return run(CMD_CREATE);
	}
	
	public SSHCommandResult info(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		return run(CMD_INFO);
	}
	
	public SSHCommandResult list(){
		opts.clear();
		opts.add(new Attribute("org", org));
		return run(CMD_LIST+" -v");
	}

	public SSHCommandResult list(String environment) {
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("environment", environment));
		return run(CMD_LIST+" -v");
	}

	public SSHCommandResult extend_limit(String newlimit){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("limit", newlimit));
		return run(CMD_UPDATE);
	}
	
	public SSHCommandResult add_system_group(String pSystemGroup){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("system_group", pSystemGroup));
		return run(CMD_ADD_SYSTEMGROUP);
	}
	
	public SSHCommandResult update_add_subscription(String subscriptionId){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("environment", environment));
		opts.add(new Attribute("add_subscription", subscriptionId));
		return run(CMD_UPDATE);
	}

	public SSHCommandResult update_content_view(String view) {
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("environment", environment));
		opts.add(new Attribute("content_view", view));
		return run(CMD_UPDATE);
	}

	public SSHCommandResult update_remove_subscription(String subscriptionId){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("remove_subscription", subscriptionId));
		return run(CMD_UPDATE);
	}

	public SSHCommandResult update_description(String new_description) {
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("description", new_description));
		return run(CMD_UPDATE);
	}

	public SSHCommandResult update_name(String new_name) {
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("new_name", new_name));
		return run(CMD_UPDATE);
	}

	public SSHCommandResult update_environment(String new_environment) {
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("environment", new_environment));
		return run(CMD_UPDATE);
	}

	public SSHCommandResult remove_system_group(String pSystemGroup){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("system_group", pSystemGroup));
		return run(CMD_REMOVE_SYSTEMGROUP);
	}
	
	public SSHCommandResult delete(){
		   opts.clear();
		   opts.add(new Attribute("name",name));
		   opts.add(new Attribute("org",org));
		   return run(CMD_DELETE+" -v");
		    
	}
	
	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **
	public void asserts_create(){
		SSHCommandResult res;
		if(this.id==null)
			updateIDs();
		
		// asserts: activation_key list
		res = list(); // if environment != null; result will be returned by that env. only 
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (activation_key list)");
		String REGEXP_AK_LIST = ".*ID\\s*:\\s*\\d+.*Name\\s*:\\s*%s.*Environment ID\\s*:\\s*%s.*";
		if(System.getProperty("katello.engine", "katello").equals("headpin"))
            REGEXP_AK_LIST = ".*ID\\s*:\\s*\\d+.*Name\\s*:\\s*%s.*";
		
		String match_info = String.format(REGEXP_AK_LIST,
					this.name,this.environment_id).replaceAll("\"", "");
		
		Assert.assertTrue(KatelloCliTestBase.sgetOutput(res).replaceAll("\n", "").matches(match_info), 
				String.format("Activation key [%s] should be found in the list",this.name));
		
		// asserts: activation_key info
		res = info();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (activation_key info)");
		String REGEXP_AK_INFO = ".*ID\\s*:\\s*\\d+.*Name\\s*:\\s*%s.*Usage Limit\\s*:\\s*%s.*Environment ID\\s*:\\s*%s.*Pools\\s*:.*";		
		 if(System.getProperty("katello.engine", "katello").equals("headpin"))
             REGEXP_AK_INFO = ".*ID\\s*:\\s*\\d+.*Name\\s*:\\s*%s.*Usage Limit\\s*:\\s*%s.*Pools\\s*:.*";
		match_info = String.format(REGEXP_AK_INFO,
				this.name, (this.limit != null ? this.limit : "unlimited"), this.environment_id).replaceAll("\"", "");
		
		Assert.assertTrue(KatelloCliTestBase.sgetOutput(res).replaceAll("\n", "").matches(match_info), 
				String.format("Activation key [%s] should contain correct info",this.name));			
	}
	
	/**
	 * Retrieves the IDs (or does updates) like:<BR>
	 * id - activation key id in DB<BR>
	 * environment_id - id of the environment<BR>
	 * subscriptions - array of pool_ids (could be null) 
	 */
	private void updateIDs(){
		SSHCommandResult res;
		// retrieve environment_id
		if(this.environment != null){
			KatelloEnvironment env = new KatelloEnvironment(this.kcr, this.environment, null, this.org, KatelloEnvironment.LIBRARY);
			res = env.cli_info();
			Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (environment info)");
			this.environment_id = KatelloUtils.grepCLIOutput("ID", res.getStdout());				
		}
		
		// retrieve id
		if(this.name != null){
			res = info();
			this.id = KatelloUtils.grepCLIOutput("ID", res.getStdout());
		}
	}
}
