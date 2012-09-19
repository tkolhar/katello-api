package com.redhat.qe.katello.base.obj;

import java.util.ArrayList;
import java.util.Map;

import javax.management.Attribute;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

@JsonIgnoreProperties(ignoreUnknown=true)
public class KatelloSystem {
	
	// ** ** ** ** ** ** ** Public constants	
	public static final String CMD_INFO = "system info";
	public static final String CMD_LIST = "system list";
	public static final String CMD_SUBSCRIPTIONS = "system subscriptions";
	public static final String CMD_PACKAGES = "system packages";
	public static final String CMD_REPORT = "system report";
	
	public static final String RHSM_CREATE = 
			String.format("subscription-manager register --username %s --password %s",
					System.getProperty("katello.admin.user", "admin"),
					System.getProperty("katello.admin.password", "admin"));
	public static final String RHSM_CLEAN = "subscription-manager clean";
	public static final String RHSM_SUBSCRIBE = "subscription-manager subscribe";
	public static final String RHSM_UNSUBSCRIBE = "subscription-manager unsubscribe";
	public static final String RHSM_IDENTITY = "subscription-manager identity";
	public static final String RHSM_REGISTER_BYKEY = "subscription-manager register ";
	
	public static final String OUT_CREATE = 
			"The system has been registered with id:";
	public static final String ERR_RHSM_LOCKER_ONLY = 
			"Organization %s has '%s' environment only. Please create an environment for system registration.";
	public static final String ERR_RHSM_REG_ALREADY_FORCE_NEEDED = 
			"This system is already registered. Use --force to override";
	public static final String ERR_RHSM_REG_MULTI_ENV = 
			"Organization %s has more than one environment. Please specify target environment for system registration.";
	public static final String OUT_REMOTE_ACTION_DONE = "Remote action finished:";
	public static final String OUT_RHSM_SUBSCRIBED_OK = 
			"Successfully subscribed the system"; // not a full string, .contains() needed. 

	public static final String API_CMD_INFO = "/consumers/%s";
	public static final String API_CMD_GET_SERIALS = "/consumers/%s/certificates/serials";
	
	//Very sensitive regexp is used here for matching exact subscription in list.
	public static final String REG_SUBSCRIPTION = "Subscription Name:\\s+%s\\s+SKU:\\s+\\w{5,15}+\\s+Pool Id:\\s+\\w{32}+\\s+Quantity:\\s+%s";
	public static final String REG_SUBSCRIPTION_CFSE = "ProductName:\\s+%s\\s+ProductId:\\s+\\w{5,15}\\s+PoolId:\\s+\\w{32}+\\s+Quantity:\\s+%s";
	public static final String REG_POOL_ID = "\\s+\\w{32}+\\s+";
	
	// ** ** ** ** ** ** ** Class members
	public String name;
	private String org;
//	@SuppressWarnings("unused")
	private String env;
	public String uuid;
	private String href;
	private Long environmentId;
	private KatelloOwner owner;
	private Map<String, String> facts;
	private Map<String, Object> idCert;
	private Map<String,Object> environment;
	
	private KatelloCli cli;
	private ArrayList<Attribute> opts;
	
	public KatelloSystem() {} // For resteasy
	
	public KatelloSystem(String pName, String pOrg, String pEnv){
		this.name = pName;
		this.org = pOrg;
		this.env = pEnv;
		this.opts = new ArrayList<Attribute>();
	}
	
	public KatelloSystem(String name, String org, String env, String uuid, Long environmentId, String href, KatelloOwner owner, Map<String, String> facts, Map<String, Object> idCert) {
	    this(name, org, env);
        this.uuid = uuid;
        this.environmentId = environmentId;
	    this.owner = owner;
	    this.href = href;
	    this.facts = facts;
	    this.idCert = idCert;
	}
	
	// Accessors
	public String getName() {
	    return name;
	}
	
    public void setName(String name) {
        this.name = name;
    }

	@JsonProperty("organization")
	public String getOrganization() {
	    return org;
	}
	
	@JsonProperty("organization")
    public void setOrganization(String org) {
        this.org = org;
    }

	public Map<String,Object> getEnvironment() {
	    return environment;
	}
	
	public void setEnvironment(Map<String,Object> environment) {
	    this.environment = environment;
	}
	
//	@JsonProperty("envrionment")
//	public Map<String,Object> getEnvironmentMap() {
//	    return environmentMap;
//	}
//	
//	@JsonProperty("environment")
//	public void setEnvironmentMap(Map<String,Object> environmentMap) {
//	    this.environmentMap = environmentMap;
//	}
	
	public Long getEnvironmentId() {
	    if ( environment != null ) {
	        environmentId = Long.valueOf(environment.get("id").toString());
	    }
	    return environmentId;
	}
	
    public void setEnvironmentId(Long environmentId) {
        this.environmentId = environmentId;
    }

	public String getUuid() {
	    return uuid;
	}
	
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

	public KatelloOwner getOwner() {
	    return owner;
	}
	
    public void setOwner(KatelloOwner owner) {
        this.owner = owner;
    }

	public String getHref() {
	    return href;
	}
	
    public void setHref(String href) {
        this.href = href;
    }

	public Map<String, String> getFacts() {
	    return facts;
	}
	
    public void setFacts(Map<String, String> facts) {
        this.facts = facts;
    }

	public Map<String, Object> getIdCert() {
	    return idCert;
	}
	
    public void setIdCert(Map<String, Object> idCert) {
        this.idCert = idCert;
    }

	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **
	
    public SSHCommandResult rhsm_register(){
		String cmd = RHSM_CREATE;
		
		if(this.name != null)
			cmd += " --name \""+this.name+"\"";
		if(this.org != null)
			cmd += " --org \""+this.org+"\"";
		if(this.env != null)
			cmd += " --environment \""+this.env+"\"";
		
		return KatelloUtils.sshOnClient(cmd);		
	}
	
	public SSHCommandResult rhsm_registerForce(){
		String cmd = RHSM_CREATE;
		
		if(this.name != null)
			cmd += " --name \""+this.name+"\"";
		if(this.org != null)
			cmd += " --org \""+this.org+"\"";
		if(this.env != null)
			cmd += " --environment \""+this.env+"\"";
		cmd += " --force";
		
		return KatelloUtils.sshOnClient(cmd);		
	}

	public SSHCommandResult rhsm_registerForce(String activationkey){
		String cmd = RHSM_REGISTER_BYKEY;
		
		if(this.name != null)
			cmd += " --name \""+this.name+"\"";
		if(this.org != null)
			cmd += " --org \""+this.org+"\"";
		if(activationkey != null)
			cmd += " --activationkey \""+activationkey+"\"";
		cmd += " --force";
		
		return KatelloUtils.sshOnClient(cmd);		
	}
	
	public SSHCommandResult rhsm_clean(){
		String cmd = RHSM_CLEAN;		
		return KatelloUtils.sshOnClient(cmd);		
	}

	public SSHCommandResult list(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("environment", env));
		cli = new KatelloCli(CMD_LIST+" -v", opts);
		return cli.run();
	}

	public SSHCommandResult report(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("environment", env));
		cli = new KatelloCli(CMD_REPORT+" -v", opts);
		return cli.run();
	}
	
	public SSHCommandResult subscriptions_available(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		cli = new KatelloCli(CMD_SUBSCRIPTIONS+" --available -v", opts);
		return cli.run();
	}
	
	public SSHCommandResult subscriptions() {
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		cli = new KatelloCli(CMD_SUBSCRIPTIONS, opts);
		return cli.run();
	}

	public SSHCommandResult subscriptions_count() {
		String cmd = CMD_SUBSCRIPTIONS;
		
		if(this.name != null)
			cmd += " --name \""+this.name+"\"";
		if(this.org != null)
			cmd += " --org \""+this.org+"\"";

		cmd += " | grep \"Serial Id\" | wc -l";
		
		cli = new KatelloCli(cmd, null);
		return cli.run();	
	}
	
	public SSHCommandResult packages_install(String packageName){
		opts.clear();
		opts.add(new Attribute("install", packageName));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		cli = new KatelloCli(CMD_PACKAGES, opts);
		return cli.run();
	}
	
	public SSHCommandResult rhsm_subscribe(String poolid){
		String cmd = RHSM_SUBSCRIBE;
		
		if(poolid != null)
			cmd += " --pool "+poolid;
		
		return KatelloUtils.sshOnClient(cmd);		
	}

	public SSHCommandResult rhsm_unsubscribe(String serialId){
		String cmd = RHSM_UNSUBSCRIBE;
		
		if(serialId != null)
			cmd += " --serial "+serialId;
		
		return KatelloUtils.sshOnClient(cmd);		
	}
	
	public SSHCommandResult rhsm_subscribe(String poolid, int quantity){
		String cmd = RHSM_SUBSCRIBE;
		
		if(poolid != null)
			cmd += " --pool "+poolid;
		if (quantity != 0) 
			cmd += " --quantity " + quantity;
		
		return KatelloUtils.sshOnClient(cmd);		
	}
	
	public SSHCommandResult rhsm_subscribe_auto(){
		String cmd = RHSM_SUBSCRIBE + " --auto";
		
		return KatelloUtils.sshOnClient(cmd);		
	}
	
	public SSHCommandResult rhsm_identity(){
		String cmd = RHSM_IDENTITY;
		
		return KatelloUtils.sshOnClient(cmd);		
	}
	
//	@SuppressWarnings("unchecked")
//    public static KatelloSystem api_info(String byId) throws KatelloApiException {
//		KatelloApiResponse response = KatelloApi.get(String.format(API_CMD_INFO, byId));
//		if ( response.getReturnCode() == 200 ) {
//		    String json = response.getContent();
//		    JSONObject jobj = KatelloTestScript.toJSONObj(json);
//		    JSONObject jenv = (JSONObject)jobj.get("environment");
//		    JSONObject jown = (JSONObject)jobj.get("owner");
//		    KatelloOwner owner = new KatelloOwner((Long)jown.get("id"), (String)jown.get("key"), (String)jown.get("displayName"), (String)jown.get("href"));
//		    return new KatelloSystem((String)jobj.get("name"), (String)jenv.get("organization"), (String)jenv.get("name"), (String)jobj.get("uuid"), (Long)jenv.get("id"), (String)jobj.get("href"), owner, (Map<String,String>)jobj.get("facts"), (Map<String,Object>)jobj.get("idCert"));          
//        }
//		throw new KatelloApiException(response);
//	}
//	
//	public static List<Long> api_getSerials(String customerId) throws KatelloApiException {
//	    KatelloApiResponse response = KatelloApi.get(String.format(API_CMD_GET_SERIALS, customerId));
//	    if ( response.getReturnCode() == 200 ) {
//	        List<Long> _return = new ArrayList<Long>();
//	        JSONArray serials = KatelloTestScript.toJSONArr(response.getContent());
//	        for ( Object serial : serials ) {
//	            _return.add((Long)((JSONObject)serial).get("serial"));
//	        }
//	        return _return;
//	    }
//	    throw new KatelloApiException(response);
//	}
}
