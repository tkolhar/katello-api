package com.redhat.qe.katello.base.obj;

import java.util.Map;
import javax.management.Attribute;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.katello.common.KatelloConstants;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

@JsonIgnoreProperties(ignoreUnknown=true)
public class KatelloSystem extends _KatelloObject{
	
	// ** ** ** ** ** ** ** Public constants	
	public static final String CMD_INFO = "system info";
	public static final String CMD_LIST = "system list";
	public static final String CMD_UPDATE = "system update";
	public static final String CMD_SUBSCRIPTIONS = "system subscriptions";
	public static final String CMD_PACKAGES = "system packages";
	public static final String CMD_REPORT = "system report";
	public static final String CMD_REMOVE = "system remove_deletion";
	public static final String CMD_SUBSCRIBE = "system subscribe";
	public static final String CMD_RELEASES = "system releases";
	public static final String CMD_FACTS = "system facts";
	
	public static final String CMD_ADD_CUSTOM_INFO = "system custom_info add";
	public static final String CMD_UPDATE_CUSTOM_INFO = "system custom_info update";
	public static final String CMD_REMOVE_CUSTOM_INFO = "system custom_info remove";
	
	public static final String CMD_LIST_ERRATAS = "errata system";
	public static final String CMD_LIST_ERRATA_DETAILS = "errata system -v";
	
	public static final String RHSM_CREATE ="subscription-manager register --username %s --password %s";
	public static final String RHSM_CLEAN = "subscription-manager clean";
	public static final String RHSM_SUBSCRIBE = "subscription-manager subscribe";
	public static final String RHSM_UNSUBSCRIBE = "subscription-manager unsubscribe";
	public static final String RHSM_IDENTITY = "subscription-manager identity";
	public static final String RHSM_REGISTER_BYKEY = "subscription-manager register ";
	public static final String RHSM_UNREGISTER = "subscription-manager unregister";
	public static final String RHSM_LIST_CONSUMED = "subscription-manager list --consumed";
	public static final String RHSM_REFRESH = "subscription-manager refresh";
	
	public static final String OUT_CREATE = 
			"The system has been registered with id:";
	public static final String ERR_RHSM_LOBRARY_ONLY = 
			"Organization %s has the '%s' environment only. Please create an environment for system registration.";
	public static final String ERR_RHSM_REG_ALREADY_FORCE_NEEDED = 
			"This system is already registered. Use --force to override";
	public static final String ERR_RHSM_REG_MULTI_ENV = 
			"Organization %s has more than one environment. Please specify target environment for system registration.";
	public static final String ERR_GUEST_HAS_DIFFERENT_HOST = 
			"Guest's host does not match owner of pool: '%s'.";
	public static final String ERR_DELETE_ACCESS = 
			"User %s is not allowed to access api/candlepin_proxies/delete";
	
	public static final String OUT_REMOTE_ACTION_DONE = "Remote action finished:";
	public static final String OUT_RHSM_SUBSCRIBED_OK = 
			"Successfully subscribed the system"; // not a full string, .contains() needed. 
	public static final String OUT_UPDATE = 
			"Successfully updated system [ %s ]";
	public static final String OUT_DELETE = 
			"Successfully removed deletion record for hypervisor with uuid [ %s ]";
	public static final String OUT_SUBSCRIBE = 
			"Successfully attached subscription to System [ %s ]";
	public static final String OUT_SUBSCRIPTIONS_EMPTY = 
			"No Subscriptions found for System [ %s ] in Org [ %s ]";
	public static final String OUT_ADD_CUSTOM_INFO =
			"Successfully added Custom Information [ %s : %s ] to System [ %s ]";
	public static final String OUT_UPDATE_CUSTOM_INFO =
			"Successfully updated Custom Information [ %s ] for System [ %s ]";
	public static final String OUT_REMOVE_CUSTOM_INFO =
			"Successfully removed Custom Information from System [ %s ]";
	
	public static final String API_CMD_INFO = "/consumers/%s";
	public static final String API_CMD_GET_SERIALS = "/consumers/%s/certificates/serials";
	
	//Very sensitive regexp is used here for matching exact subscription in list.
	public static final String REG_SUBSCRIPTION = "Subscription Name\\s*:\\s+%s\\s+SKU\\s*:\\s+\\w{5,15}+\\s+Pool Id\\s*:\\s+\\w{32}+\\s+Quantity\\s*:\\s+%s";
	public static final String REG_SUBSCRIPTION_CFSE = "Product\\s+Name\\s*:\\s*%s\\s+Product\\s+Id\\s*:\\s*\\w{5,15}\\s+Pool\\s+Id\\s*:\\s*\\w{32}+\\s+Quantity\\s*:\\s*%s";
	public static final String REG_POOL_ID = "\\s+\\w{32}+\\s+";
	public static final String REG_SYSTEM_INFO = ".*Name\\s*:\\s+%s.*IPv4 Address\\s*:\\s+\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}.*UUID\\s*:\\s+\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}.*Location\\s*:\\s+%s.*Description\\s*:\\s+%s.*";
	public static final String REG_CUSTOM_INFO = ".*Custom Info\\s*:\\s+[\\s+%s\\s+].*";
	
	public static final String SYSTEM_UUIDS = "system list --org '%s' --noheading -v | grep \"^UUID\\s*:\" | cut -f2 -d: | sed 's/ *$//g' | sed 's/^ *//g'";
	
	public static final String SYSTEM_UNREGISTER = "system unregister --uuid %s --org '%s'";
	public static final String SYSTEM_UNSUBSCRIBE = "system unsubscribe --all --uuid %s --org '%s'";
	
	// ** ** ** ** ** ** ** Class members
	public String name;
	private String org;
	private String env;
	public String uuid;
	public String description;
	public String location;
	private String href;
	private Long environmentId;
	private KatelloOwner owner;
	private Map<String, String> facts;
	private KatelloIdCert idCert;
	private KatelloEnvironment environment;
	
	public KatelloSystem(String pName, String pOrg, String pEnv){
		this.name = pName;
		this.org = pOrg;
		this.env = pEnv;
	}
	
	public KatelloSystem(String name, String org, String env, String uuid, Long environmentId, String href, KatelloOwner owner, Map<String, String> facts, KatelloIdCert idCert) {
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

    public void setEnvironmentName(String envname) {
        this.env = envname;
    }
    
	@JsonProperty("organization")
	public String getOrganization() {
	    return org;
	}
	
	@JsonProperty("organization")
    public void setOrganization(String org) {
        this.org = org;
    }

	@JsonProperty("environment")
	public KatelloEnvironment getEnvironment() {
	    return environment;
	}
	
	@JsonProperty("environment")
	public void setEnvironment(KatelloEnvironment environment) {
	    this.environment = environment;
	}
		
	@JsonProperty("environment_id")
	public Long getEnvironmentId() {
	    if ( environment != null && environmentId == null ) {
	        environmentId = environment.getId();
	    }
	    return environmentId;
	}
	
	@JsonProperty("environment_id")
    public void setEnvironmentId(Long environmentId) {
        this.environmentId = environmentId;
    }

	@JsonProperty("uuid")
	public String getUuid() {
	    return uuid;
	}

	@JsonProperty("uuid")
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

    @JsonProperty("idCert")
	public KatelloIdCert getIdCert() {
	    return idCert;
	}
	
    @JsonProperty("idCert")
    public void setIdCert(KatelloIdCert idCert) {
        this.idCert = idCert;
    }

	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **
	
    public SSHCommandResult rhsm_register(){
		String cmd = RHSM_CREATE;
		if(this.user==null)
			cmd = String.format(RHSM_CREATE,
					System.getProperty("katello.admin.user", KatelloUser.DEFAULT_ADMIN_USER),
					System.getProperty("katello.admin.password", KatelloUser.DEFAULT_ADMIN_PASS));
		else
			cmd = String.format(RHSM_CREATE,user.username,user.password);
			
		if(this.name != null)
			cmd += " --name \""+this.name+"\"";
		if(this.org != null)
			cmd += " --org \""+this.org+"\"";
		if(this.env != null)
			cmd += " --environment \""+this.env+"\"";
		
		return KatelloUtils.sshOnClient(getHostName(), cmd);		
	}
	
	public SSHCommandResult rhsm_registerForce(){
		String cmd = RHSM_CREATE;
		if(this.user==null)
			cmd = String.format(RHSM_CREATE,
					System.getProperty("katello.admin.user", KatelloUser.DEFAULT_ADMIN_USER),
					System.getProperty("katello.admin.password", KatelloUser.DEFAULT_ADMIN_PASS));
		else
			cmd = String.format(RHSM_CREATE,user.username,user.password);
		
		if(this.name != null)
			cmd += " --name \""+this.name+"\"";
		if(this.org != null)
			cmd += " --org \""+this.org+"\"";
		if(this.env != null)
			cmd += " --environment \""+this.env+"\"";
		cmd += " --force";
		
		return KatelloUtils.sshOnClient(getHostName(), cmd);		
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
		
		return KatelloUtils.sshOnClient(getHostName(), cmd);		
	}
	
	
	public SSHCommandResult rhsm_registerForce_multiplekeys(String act_list){
		String cmd = RHSM_REGISTER_BYKEY;
		
		if(this.name != null)
			cmd += " --name \""+this.name+"\"";
		if(this.org != null)
			cmd += " --org \""+this.org+"\"";
		if(act_list != null)
			cmd += " --activationkey \"" + act_list +"\"";
		cmd += " --force";
		
		return KatelloUtils.sshOnClient(getHostName(), cmd);		
	}
	
	public SSHCommandResult rhsm_clean(){
		String cmd = RHSM_CLEAN;		
		return KatelloUtils.sshOnClient(getHostName(), cmd);		
	}

	public SSHCommandResult rhsm_refresh(){
		String cmd = RHSM_REFRESH;
		return KatelloUtils.sshOnClient(getHostName(), cmd);
	}
	
	public SSHCommandResult list(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("environment", env));
		return run(CMD_LIST+" -v");
	}

	public SSHCommandResult info(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		if (env != null) {
			opts.add(new Attribute("environment", env));
		}
		return run(CMD_INFO+" -v");
	}

	public SSHCommandResult remove(){
		opts.clear();
		opts.add(new Attribute("uuid", this.uuid));
		return run(CMD_REMOVE);
	}

	public SSHCommandResult subscribe(String poolid) {
		opts.clear();
		opts.add(new Attribute("pool", poolid));
		opts.add(new Attribute("org", org));
		if (this.uuid != null)
			opts.add(new Attribute("uuid", uuid));
		if (this.name != null)
			opts.add(new Attribute("name", name));
		return run(CMD_SUBSCRIBE);
	}
	
	public SSHCommandResult report(String format){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("environment", env));
		if(format != null)
		{
			opts.add(new Attribute("format",format));
		}	
		return run(CMD_REPORT+" -v");
	}
	
	public SSHCommandResult subscriptions_available(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		return run(CMD_SUBSCRIPTIONS+" --available -v");
	}

	public SSHCommandResult subscriptions() {
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		return run(CMD_SUBSCRIPTIONS);
	}

	public SSHCommandResult releases() {
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		return run(CMD_RELEASES);
	}
	
	public SSHCommandResult facts() {
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		return run(CMD_FACTS);
	}
	
	public SSHCommandResult subscriptions_count() {
		String cmd = CMD_SUBSCRIPTIONS;
		
		if(this.name != null)
			cmd += " --name \""+this.name+"\"";
		if(this.org != null)
			cmd += " --org \""+this.org+"\"";

		if (KatelloConstants.KATELLO_PRODUCT.equals("katello") || KatelloConstants.KATELLO_PRODUCT.equals("headpin")) {
			cmd += " | grep \"Serial ID\" | wc -l";	
		} else {
			cmd += " | grep \"Serial Id\" | wc -l";
		}
		
		KatelloCli cli = new KatelloCli(cmd, null);
		return cli.run();	
	}
	
	public SSHCommandResult packages_install(String packageName){
		opts.clear();
		opts.add(new Attribute("install", packageName));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		return run(CMD_PACKAGES);
	}
	
	public SSHCommandResult rhsm_subscribe(String poolid){
		String cmd = RHSM_SUBSCRIBE;
		
		if(poolid != null)
			cmd += " --pool "+poolid;
		
		return KatelloUtils.sshOnClient(getHostName(), cmd);		
	}

	public SSHCommandResult rhsm_unsubscribe(String serialId){
		String cmd = RHSM_UNSUBSCRIBE;
		
		if(serialId != null)
			cmd += " --serial "+serialId;
		
		return KatelloUtils.sshOnClient(getHostName(), cmd);		
	}
	
	public SSHCommandResult rhsm_subscribe(String poolid, int quantity){
		String cmd = RHSM_SUBSCRIBE;
		
		if(poolid != null)
			cmd += " --pool "+poolid;
		if (quantity != 0) 
			cmd += " --quantity " + quantity;
		
		return KatelloUtils.sshOnClient(getHostName(), cmd);		
	}
	
	public SSHCommandResult rhsm_subscribe_auto(){
		String cmd = RHSM_SUBSCRIBE + " --auto";
		
		return KatelloUtils.sshOnClient(getHostName(), cmd);		
	}
	
	public SSHCommandResult rhsm_identity(){
		String cmd = RHSM_IDENTITY;
		
		return KatelloUtils.sshOnClient(getHostName(), cmd);		
	}

	public SSHCommandResult rhsm_unregister(){
		String cmd = RHSM_UNREGISTER;
		
		return KatelloUtils.sshOnClient(getHostName(), cmd);		
	}
	
	public SSHCommandResult system_uuids(){
		opts.clear();
		return run(String.format(KatelloSystem.SYSTEM_UUIDS, org));
	}
	
	public SSHCommandResult unsubscribe(){
		opts.clear();
		return run(String.format(KatelloSystem.SYSTEM_UNSUBSCRIBE, uuid, org));
	}

	public SSHCommandResult unregister(){
		opts.clear();
		return run(String.format(KatelloSystem.SYSTEM_UNREGISTER, uuid, org));
	}

	public SSHCommandResult update_environment(String newEnvironment){
		opts.clear();
		opts.add(new Attribute("environment", env));
		opts.add(new Attribute("new_environment", newEnvironment));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));

		return run(CMD_UPDATE);
	}

	public SSHCommandResult update_name(String newName){
		opts.clear();
		opts.add(new Attribute("environment", env));
		opts.add(new Attribute("new_name", newName));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));

		return run(CMD_UPDATE);
	}
	
	public SSHCommandResult rhsm_listConsumed(){
		String cmd = RHSM_LIST_CONSUMED;
		
		return KatelloUtils.sshOnClient(getHostName(), cmd);		
	}
	
	public SSHCommandResult list_erratas(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		return run(CMD_LIST_ERRATAS);
	}

	public SSHCommandResult list_erratas(String type){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("type", type));
		return run(CMD_LIST_ERRATAS);
	}

	public SSHCommandResult list_errata_details(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		return run(CMD_LIST_ERRATA_DETAILS);
	}
	
	public SSHCommandResult list_errata_count(String query) {
		String cmd = CMD_LIST_ERRATAS;
		
		if(this.name != null)
			cmd += " --name \""+this.name+"\"";
		if(this.org != null)
			cmd += " --org \""+this.org+"\"";

		cmd += " | grep \"" + query + "\" | wc -l";
		
		KatelloCli cli = new KatelloCli(cmd, null);
		return cli.run();	
	}

	public SSHCommandResult list_errata_names(String query) {
		String cmd = CMD_LIST_ERRATAS;
		
		if(this.name != null)
			cmd += " --name \""+this.name+"\"";
		if(this.org != null)
			cmd += " --org \""+this.org+"\"";

		cmd += " | grep \"" + query + "\" | awk '{print $1}'";
		
		KatelloCli cli = new KatelloCli(cmd, null);
		return cli.run();	
	}
	
	public SSHCommandResult list_errata_details_count(String query) {
		String cmd = CMD_LIST_ERRATA_DETAILS;
		
		if(this.name != null)
			cmd += " --name \""+this.name+"\"";
		if(this.org != null)
			cmd += " --org \""+this.org+"\"";

		cmd += " | grep \"" + query + "\" | wc -l";
		
		KatelloCli cli = new KatelloCli(cmd, null);
		return cli.run();	
	}

	public SSHCommandResult add_custom_info(String keyname, String value){
		opts.clear();
		opts.add(new Attribute("environment", env));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("keyname", keyname));
		opts.add(new Attribute("value", value));

		return run(CMD_ADD_CUSTOM_INFO);
	}	
	
	public SSHCommandResult update_custom_info(String keyname, String value){
		opts.clear();
		opts.add(new Attribute("environment", env));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("keyname", keyname));
		opts.add(new Attribute("value", value));

		return run(CMD_UPDATE_CUSTOM_INFO);
	}	
	
	public SSHCommandResult remove_custom_info(String keyname){
		opts.clear();
		opts.add(new Attribute("environment", env));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("keyname", keyname));

		return run(CMD_REMOVE_CUSTOM_INFO);
	}	

	public SSHCommandResult update_content_view(String view) {
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("content_view", view));

		return run(CMD_UPDATE);
	}
}
