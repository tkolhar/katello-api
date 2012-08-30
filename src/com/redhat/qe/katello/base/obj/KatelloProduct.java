package com.redhat.qe.katello.base.obj;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.management.Attribute;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.testng.Assert;

import com.redhat.qe.katello.base.KatelloApi;
import com.redhat.qe.katello.base.KatelloApiException;
import com.redhat.qe.katello.base.KatelloApiResponse;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.tools.SSHCommandResult;

//@JsonIgnoreProperties(ignoreUnknown=true)
public class KatelloProduct {
	protected static Logger log = Logger.getLogger(KatelloProduct.class.getName());
	
	public static final String RHEL_SERVER = "Red Hat Enterprise Linux Server";
	public static final String Default_Org = "ACME_Corporation";
	
	// ** ** ** ** ** ** ** Public constants
	public static final String CMD_CREATE = "product create";
	public static final String CLI_CMD_LIST = "product list -v";
	public static final String CMD_STATUS = "product status";
	public static final String CMD_SYNC = "product synchronize";
	public static final String CMD_PROMOTE = "product promote";
	public static final String CMD_DELETE = "product delete";
	public static final String CMD_SET_PLAN = "product set_plan";
	
	/** Parameters:<BR>1: product_name<BR>2: org_name */
	public static final String ERR_COULD_NOT_FIND_PRODUCT = 
		"Could not find product [ %s ] within organization [ %s ]";
	/** Parameters:<BR>1: product_name */
	public static final String OUT_CREATED = 
		"Successfully created product [ %s ]";
	/** Parameters:<BR>1: product_name<BR>2: env_name */
	public static final String OUT_PROMOTED = 
		"Product [ %s ] promoted to environment [ %s ]";
	/** Parameters:<BR>1: product_name */
	public static final String OUT_SYNCHRONIZED = 
		"Product [ %s ] synchronized";
	public static final String OUT_DELETED = 
		"Deleted product '%s'";
	public static final String OUT_NOT_SYNCHRONIZED_YET = 
			"Product '%s' was not synchronized yet";
	public static final String ERR_PROMOTE_NOREPOS = 
			"Couldn't find Product with cp_id = ";
	public static final String ERR_GPGKEY_NOTFOUND = 
			"Couldn't find GpgKey with name = %s";

	public static final String API_CMD_LIST = "/organizations/%s/products"; // by org
	
	// ** ** ** ** ** ** ** Class members
	String name;
	String org;
	String provider;
	String description;
	String gpgkey;
	String url;
	boolean nodisc = false;
	boolean assumeyes = false;
	
	private KatelloCli cli;
	private ArrayList<Attribute> opts;

	public KatelloProduct() {} // No-arg ctor for resteasy
	
	public KatelloProduct(
			String pName, String pOrg, String pProv, 
			String pDesc, String pGpgkey, String pUrl,
			Boolean bNodisc, Boolean bAssumeyes){
		this.name = pName;
		this.org = pOrg;
		this.provider = pProv;
		this.description = pDesc;
		this.gpgkey = pGpgkey;
		this.url = pUrl;
		if(bNodisc != null)
			this.nodisc = bNodisc.booleanValue();
		if(bAssumeyes != null)
			this.assumeyes = bAssumeyes.booleanValue();
		this.opts = new ArrayList<Attribute>();
	}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }

	public SSHCommandResult create(){
		return create(null);
	}
	
	public SSHCommandResult create(KatelloUser user){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("provider", provider));
		opts.add(new Attribute("description", description));
		opts.add(new Attribute("gpgkey", gpgkey));
		opts.add(new Attribute("url", url));
		if(nodisc)
			opts.add(new Attribute("nodisc", ""));
		if(assumeyes)
			opts.add(new Attribute("assumeyes", ""));
		if (user == null) {
			cli = new KatelloCli(CMD_CREATE, opts);
		} else {
			cli = new KatelloCli(CMD_CREATE, opts, user);
		}
		return cli.run();
	}
	
	public SSHCommandResult cli_list(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("provider", provider));
		cli = new KatelloCli(CLI_CMD_LIST, opts);
		return cli.run();
	}

	public SSHCommandResult cli_list(String environment){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("provider", provider));
		opts.add(new Attribute("environment", environment));
		cli = new KatelloCli(CLI_CMD_LIST, opts);
		return cli.run();
	}
	
	public SSHCommandResult status(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		cli = new KatelloCli(CMD_STATUS, opts);
		return cli.run();
	}

	
	public SSHCommandResult cli_set_plan(String plan) {
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("plan", plan));
		cli = new KatelloCli(CMD_SET_PLAN, opts);
		return cli.run();
	}
	
	public SSHCommandResult synchronize(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		cli = new KatelloCli(CMD_SYNC, opts);
		return cli.run();
	}
	
	public SSHCommandResult promote(String environment){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("environment", environment));
		cli = new KatelloCli(CMD_PROMOTE, opts);
		return cli.run();
	}

	public SSHCommandResult delete(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		cli = new KatelloCli(CMD_DELETE, opts);
		return cli.run();
	}
	
//	public static List<KatelloProduct> api_list(String org) throws KatelloApiException {
//	    List<KatelloProduct> products = new ArrayList<KatelloProduct>();
//		KatelloApiResponse response = KatelloApi.get(String.format(API_CMD_LIST, org));
//		if ( response.getReturnCode() < 300 ) {
//		    String json = response.getContent();
//		    JSONArray jproducts = KatelloTestScript.toJSONArr(json);
//		    for ( int i = 0; i < jproducts.size(); ++i ) {
//		        JSONObject product = (JSONObject)jproducts.get(i);
//		        products.add(new KatelloProduct( (String)product.get("name"), org, (String)product.get("provider_name"), 
//            (String)product.get("description"), (String)product.get("gpg_key_id"), /*(String)product.get("url")*/"",
//            Boolean.TRUE, Boolean.FALSE));
//		    }
//		    return products;
//		}
//		throw new KatelloApiException(response);
//	}

	
	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **
	
	public void assert_productExists(String envName, boolean synced){
		SSHCommandResult res;
		String REGEXP_PRODUCT_LIST;
		
		REGEXP_PRODUCT_LIST = ".*Name:\\s+"+this.name+".*Provider Name:\\s+"+this.provider+".*";
		log.info("Assertions: product exists");
		res = cli_list();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		Assert.assertTrue(KatelloCliTestScript.sgetOutput(res).replaceAll("\n", "").matches(REGEXP_PRODUCT_LIST),
				"List should contain info about product (requested by: provider)");

		if(envName!=null){
			res = cli_list(envName);
			Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
			Assert.assertTrue(KatelloCliTestScript.sgetOutput(res).replaceAll("\n", "").matches(REGEXP_PRODUCT_LIST), 
					"List should contain info about product (requested by: environment)");
		}
		
		if(!synced){
			res = status();
			Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
			String REGEXP_PRODUCT_STATUS = ".*Name:\\s+"+this.name+".*Provider Name:\\s+"+this.provider+".*Last Sync:\\s+never.*Sync State:\\s+Not synced.*";
			Assert.assertTrue(KatelloCliTestScript.sgetOutput(res).replaceAll("\n", "").matches(REGEXP_PRODUCT_STATUS), 
					"List should contain status of product (not synced)");
		}else{
			// TODO - needs an implementation - when product is synchronized.
		}
	}

}
