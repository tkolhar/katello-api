package com.redhat.qe.katello.base.obj;

import java.util.ArrayList;

import javax.management.Attribute;

import com.redhat.qe.auto.testng.Assert;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.tasks.KatelloTasks;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloActivationKey {

	String org;
	String environment;
	String name;
	String description;
	String template;
	
	private KatelloCli cli;
	private ArrayList<Attribute> opts;
	
	private String id;
	private String environment_id;
	private String template_id;
	
	public static final String CMD_CREATE = "activation_key create";
	public static final String CMD_INFO = "activation_key info";
	public static final String CMD_LIST = "activation_key list";
	
	public static final String ERR_TEMPLATE_NOTFOUND = 
			"Could not find template [ %s ]";	
	public static final String OUT_CREATE = 
			"Successfully created activation key [ %s ]";

	public KatelloActivationKey(String pOrg, String pEnv, String pName, String pDesc, String pTemplate){
		this.org = pOrg;
		this.environment = pEnv;
		this.name = pName;
		this.description = pDesc;
		this.template = pTemplate;
		this.opts = new ArrayList<Attribute>();
	}
	
	public SSHCommandResult create(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("environment", environment));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("description", description));
		opts.add(new Attribute("template", template));
		cli = new KatelloCli(CMD_CREATE, opts);
		return cli.run();
	}
	
	public SSHCommandResult info(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		cli = new KatelloCli(CMD_INFO, opts);
		return cli.run();
	}
	
	public SSHCommandResult list(){
		opts.clear();
		opts.add(new Attribute("org", org));
		cli = new KatelloCli(CMD_LIST+" -v", opts);
		return cli.run();
	}

	public SSHCommandResult list(String pEnvironment){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("environment", pEnvironment));
		cli = new KatelloCli(CMD_LIST+" -v", opts);
		return cli.run();
	}
	
	
	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **
	public void asserts_create(){
		SSHCommandResult res;
		if(this.id==null)
			updateIDs();
		
		// asserts: activation_key list
		res = list(this.environment);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (activation_key list)");
		String REGEXP_AK_LIST = ".*Id:\\s+\\d+.*Name:\\s+%s.*Environment Id:\\s+%s.*System Template Id:\\s+%s.*";

		String match_info = String.format(REGEXP_AK_LIST,
				this.name,this.environment_id,this.template_id).replaceAll("\"", "");
		if(this.template_id==null){
			match_info = String.format(REGEXP_AK_LIST,
					this.name,this.environment_id,"None").replaceAll("\"", "");
		}
		Assert.assertTrue(KatelloCliTestScript.sgetOutput(res).replaceAll("\n", "").matches(match_info), 
				String.format("Activation key [%s] should be found in the list",this.name));
		
		// asserts: activation_key info
		res = info();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (activation_key info)");
		String REGEXP_AK_INFO = ".*Id:\\s+\\d+.*Name:\\s+%s.*Environment Id:\\s+%s.*System Template Id:\\s+%s.*Pools:.*";
		match_info = String.format(REGEXP_AK_INFO,
				this.name,this.environment_id,this.template_id).replaceAll("\"", "");
		if(this.template_id==null){
			match_info = String.format(REGEXP_AK_INFO,
					this.name,this.environment_id,"None").replaceAll("\"", "");				
		}
		Assert.assertTrue(KatelloCliTestScript.sgetOutput(res).replaceAll("\n", "").matches(match_info), 
				String.format("Activation key [%s] should contain correct info",this.name));			
	}
	
	/**
	 * Retrieves the IDs (or does updates) like:<BR>
	 * id - activation key id in DB<BR>
	 * environment_id - id of the environment<BR>
	 * template_id - id of the template (could be null)<BR>
	 * subscriptions - array of pool_ids (could be null) 
	 */
	private void updateIDs(){
		SSHCommandResult res;
		// retrieve environment_id
		if(this.environment != null){
			KatelloEnvironment env = new KatelloEnvironment(this.environment, null, this.org, KatelloEnvironment.LIBRARY);
			res = env.cli_info();
			Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (environment info)");
			this.environment_id = KatelloTasks.grepCLIOutput("Id", res.getStdout());				
		}
		//retrieve template_id for an environment
		if(this.template !=null){
			KatelloTemplate tmpl = new KatelloTemplate(template, null, this.org, null);
			res = tmpl.info(this.environment);
			Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (template info)");
			this.template_id = KatelloTasks.grepCLIOutput("Id", res.getStdout());				
		}
		// retrieve id
		if(this.name != null){
			res = info();
			this.id = KatelloTasks.grepCLIOutput("Id", res.getStdout());
//			this.subscriptions = KatelloTasks.grepCLIOutput("Pools", res.getStdout());
		}
	}
}
