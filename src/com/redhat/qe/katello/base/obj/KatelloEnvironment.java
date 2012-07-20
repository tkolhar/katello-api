package com.redhat.qe.katello.base.obj;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.management.Attribute;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.redhat.qe.katello.base.KatelloApi;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.katello.base.KatelloPostParam;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloEnvironment {
	protected static Logger log = Logger.getLogger(KatelloEnvironment.class.getName());
	
	// ** ** ** ** ** ** ** Public constants
	public static final String LIBRARY = "Library";
	
	public static final String CMD_CREATE = "environment create";
	public static final String CMD_INFO = "environment info -v";
	public static final String CLI_CMD_LIST = "environment list -v";
	public static final String CMD_DELETE = "environment delete";
	public static final String CMD_UPDATE = "environment update";
	
	
	public static final String OUT_CREATE = 
			"Successfully created environment [ %s ]";
	public static final String OUT_DELETE = 
			"Successfully deleted environment [ %s ]";
	public static final String ERROR_INFO =
			"Could not find environment [ %s ] within organization [ %s ]";
	public static final String OUT_UPDATE =  
			"Successfully updated environment [ %s ]";
	public static final String API_CMD_LIST = "/organizations/%s/environments";
	public static final String API_CMD_CREATE = "/organizations/%s/environments";
	
	// ** ** ** ** ** ** ** Class members
	String name;
	String description;
	String org;
	String prior;
	private String prior_id = null;
	
	private KatelloCli cli;
	private ArrayList<Attribute> opts;
	
	public KatelloEnvironment(String pName, String pDesc,
			String pOrg, String pPrior){
		this.name = pName;
		this.description = pDesc;
		this.org = pOrg;
		this.prior = pPrior;
		this.opts = new ArrayList<Attribute>();
	}
	
	public SSHCommandResult cli_create(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("description", description));
		opts.add(new Attribute("prior", prior));
		cli = new KatelloCli(CMD_CREATE, opts);
		return cli.run();
	}
	
	public SSHCommandResult cli_info(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		cli = new KatelloCli(CMD_INFO, opts);
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
		cli = new KatelloCli(CMD_DELETE, opts);
		return cli.run();
	}
	
	public SSHCommandResult cli_update(String descr){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("description", descr));
		opts.add(new Attribute("prior", prior));
		cli = new KatelloCli(CMD_UPDATE, opts);
		return cli.run();
	}
	 
	public String api_list(){
		return KatelloApi.get(String.format(API_CMD_LIST, this.org));
	}
	
	public String api_create(){
	    List<NameValuePair> opts = new ArrayList<NameValuePair>();
	    opts.add(new BasicNameValuePair("name", name));
		opts.add(new BasicNameValuePair("description", description));
		opts.add(new BasicNameValuePair("prior", get_prior_id()));
		KatelloPostParam[] params = {new KatelloPostParam("environment", opts)};
		return KatelloApi.post(params, String.format(API_CMD_CREATE,org));
	}
	
	public String get_prior_id(){
		if(prior_id==null) store_id();
		return prior_id;
	}

	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **
	
	/**
	 * Retrieves environment.prior.id from API call and stores in prior_id property, note: ID could not be updated. 
	 */
	private void store_id(){
		if(prior_id==null){
			String str_envs = api_list();
			JSONArray json_envs = KatelloTestScript.toJSONArr(str_envs);
			for(int i=0;i<json_envs.size();i++){
				JSONObject json_env = (JSONObject)json_envs.get(i);
				if(json_env.get("name").equals(prior)){
					this.prior_id = ((Long)json_env.get("id")).toString();
					break;
				}
			}
		}
		if(prior_id==null)
			log.warning("Unable to retrieve environment.id for: ["+name+"]");
	}
}
