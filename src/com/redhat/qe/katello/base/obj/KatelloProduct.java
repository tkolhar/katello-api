package com.redhat.qe.katello.base.obj;

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import javax.management.Attribute;
import org.testng.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloProduct extends _KatelloObject{
	protected static Logger log = Logger.getLogger(KatelloProduct.class.getName());
	
	public static final String RHEL_SERVER = "Red Hat Enterprise Linux Server";
	public static final String RHEL_SERVER_MARKETING_POOL = 
			"Red Hat Enterprise Linux Server, Self-support (1-2 sockets) (Up to 1 guest)";
	public static final String REPOSET_RHEL6_RPMS = "Red Hat Enterprise Linux 6 Server (RPMs)";
	
	// ** ** ** ** ** ** ** Public constants
	public static final String CMD_CREATE = "product create";
	public static final String CLI_CMD_LIST = "product list -v";
	public static final String CMD_STATUS = "product status";
	public static final String CMD_SYNC = "product synchronize";
	public static final String CMD_PROMOTE = "product promote";
	public static final String CMD_DELETE = "product delete";
	public static final String CMD_SET_PLAN = "product set_plan";
	public static final String CMD_UPDATE = "product update";
	public static final String CMD_REMOVE_PLAN = "product remove_plan";
	public static final String CMD_ADD_FILTER = "product add_filter";
	public static final String CMD_REMOVE_FILTER = "product remove_filter";
	public static final String CMD_FILTER_LIST = "product list_filters";
	public static final String CMD_ENABLE_REPO_SET= "product repository_set_enable";
	
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
	public static final String ERR_HAS_NO_REPO = 
			"Product [ %s ] has no repository";

	public static final String API_CMD_LIST = "/organizations/%s/products"; // by org
	
	public static final String REG_PROD_LIST = ".*ID\\s*:\\s+\\d+.*Name\\s*:\\s+%s.*Provider ID\\s*:\\s+\\d+.*Provider Name\\s*:\\s+%s.*Sync Plan Name\\s*:\\s+%s.*Last Sync\\s*:\\s+%s.*GPG key\\s*:\\s*%s.*";
	public static final String REG_PROD_STATUS = ".*ID\\s*:\\s+\\d+.*Name\\s*:\\s+%s.*Provider ID\\s*:\\s+\\d+.*Provider Name\\s*:\\s+%s.*Last Sync\\s*:\\s+.*Sync State\\s*:\\s+%s.*";
	public static final String REG_PROD_LASTSYNC = "\\d{4}/\\d{2}/\\d{2}\\s\\d{2}:\\d{2}:\\d{2}";
	
	// ** ** ** ** ** ** ** Class members
	public String name;
	public String id;
	String org;
	public String provider;
	public String providerId;
	public String syncPlanName;
	public String lastSync;
	public String syncState;
	String description;
	public String gpgkey;
	String url;
	boolean nodisc = false;
	boolean assumeyes = false;
	
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
	}
	
	public KatelloProduct(
			String pName, String id, String pOrg, String pProv, 
			String pDesc, String pGpgkey, String pUrl,
			Boolean bNodisc, Boolean bAssumeyes){
		this(pName, pOrg, pProv, pDesc, pGpgkey, pUrl, bNodisc, bAssumeyes);
		this.id = id;
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
		return run(CMD_CREATE);
	}
	
	public SSHCommandResult cli_list(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("provider", provider));
		return run(CLI_CMD_LIST);
	}

	public SSHCommandResult cli_list(String environment){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("environment", environment));
		return run(CLI_CMD_LIST);
	}

	public SSHCommandResult cli_list_provider(String prov){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("provider", prov));
		return run(CLI_CMD_LIST);
	}

	public SSHCommandResult repository_set_enable(String repoSetName){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("set_name",repoSetName));
		return run(CMD_ENABLE_REPO_SET);
	}
	
	public SSHCommandResult update_description(String new_description){
		opts.clear();
		opts.add(new Attribute("org", org));
		if (this.id != null) {
			opts.add(new Attribute("id", id));
		} else {
			opts.add(new Attribute("name", this.name));
		}
		opts.add(new Attribute("description", new_description));
		return run(CMD_UPDATE);
	}
	
	public SSHCommandResult add_filter(String filter){
		opts.clear();
		opts.add(new Attribute("filter", filter));
		opts.add(new Attribute("org", org));
		if (this.id != null) {
			opts.add(new Attribute("id", id));
		} else {
			opts.add(new Attribute("name", this.name));
		}
		return run(CMD_ADD_FILTER);
	}
	public SSHCommandResult list_filters(){
		opts.clear();
		opts.add(new Attribute("org", org));
		if (this.id != null) {
			opts.add(new Attribute("id", id));
		} else {
			opts.add(new Attribute("name", this.name));
		}
		return run(CMD_FILTER_LIST);
	}
	
	public SSHCommandResult remove_filter(String filter){
		opts.clear();
		opts.add(new Attribute("filter", filter));
		opts.add(new Attribute("org", org));
		if (this.id != null) {
			opts.add(new Attribute("id", id));
		} else {
			opts.add(new Attribute("name", this.name));
		}
		return run(CMD_REMOVE_FILTER);
	}
	
	public SSHCommandResult status(){
		opts.clear();
		opts.add(new Attribute("org", org));
		if (this.id != null) {
			opts.add(new Attribute("id", id));
		} else {
			opts.add(new Attribute("name", this.name));
		}
		return run(CMD_STATUS);
	}
	
	public SSHCommandResult cli_set_plan(String plan) {
		opts.clear();
		opts.add(new Attribute("org", org));
		if (this.id != null) {
			opts.add(new Attribute("id", id));
		} else {
			opts.add(new Attribute("name", this.name));
		}
		opts.add(new Attribute("plan", plan));
		return run(CMD_SET_PLAN);
	}
	
	public SSHCommandResult cli_remove_plan() {
		opts.clear();
		opts.add(new Attribute("org", org));
		if (this.id != null) {
			opts.add(new Attribute("id", id));
		} else {
			opts.add(new Attribute("name", this.name));
		}
		return run(CMD_REMOVE_PLAN);
	}
	
	public SSHCommandResult synchronize(){
		opts.clear();
		opts.add(new Attribute("org", org));
		if (this.id != null) {
			opts.add(new Attribute("id", id));
		} else {
			opts.add(new Attribute("name", this.name));
		}
		return run(CMD_SYNC);
	}

	public SSHCommandResult delete(){
		opts.clear();
		opts.add(new Attribute("org", org));
		if (this.id != null) {
			opts.add(new Attribute("id", id));
		} else {
			opts.add(new Attribute("name", this.name));
		}
		return run(CMD_DELETE);
	}
	
	public SSHCommandResult update_gpgkey(String gpgkey){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("gpgkey", gpgkey));
		return run(CMD_UPDATE);
	}
	
	public SSHCommandResult update_gpgkey(String gpgkey, boolean recursive){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("gpgkey", gpgkey));
		if(recursive)
			opts.add(new Attribute("recursive",""));
		return run(CMD_UPDATE);
	}

	public ArrayList<String> custom_listNames(){
		ArrayList<String> _ret = new ArrayList<String>();
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("provider", provider));
		SSHCommandResult res = runExt(CLI_CMD_LIST+" -v","| grep -e \"^Name\" |cut -f2 -d:");
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - exit.Code==0");
		StringTokenizer toks = new StringTokenizer(KatelloCliTestBase.sgetOutput(res), "\n");
		while(toks.hasMoreTokens()) 
			_ret.add(toks.nextToken().trim());
		return _ret;
	}

	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **
	
	public void assert_productExists(String envName, boolean synced){
		SSHCommandResult res;
		String REGEXP_PRODUCT_LIST;
		
		REGEXP_PRODUCT_LIST = ".*Name\\s*:\\s+"+this.name+".*Provider Name\\s*:\\s+"+this.provider+".*";
		log.info("Assertions: product exists");
		res = cli_list();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		Assert.assertTrue(KatelloCliTestBase.sgetOutput(res).replaceAll("\n", "").matches(REGEXP_PRODUCT_LIST),
				"List should contain info about product (requested by: provider)");

		if(envName!=null){
			res = cli_list(envName);
			Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
			Assert.assertTrue(KatelloCliTestBase.sgetOutput(res).replaceAll("\n", "").matches(REGEXP_PRODUCT_LIST), 
					"List should contain info about product (requested by: environment)");
		}
		
		if(!synced){
			res = status();
			Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
			String REGEXP_PRODUCT_STATUS = ".*Name\\s*:\\s+"+this.name+".*Provider Name\\s*:\\s+"+this.provider+".*Last Sync\\s*:\\s+never.*Sync State\\s*:\\s+Not synced.*";
			Assert.assertTrue(KatelloCliTestBase.sgetOutput(res).replaceAll("\n", "").matches(REGEXP_PRODUCT_STATUS), 
					"List should contain status of product (not synced)");
		}else{
			// TODO - needs an implementation - when product is synchronized.
		}
	}

}
