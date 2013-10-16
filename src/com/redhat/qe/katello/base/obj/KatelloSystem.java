package com.redhat.qe.katello.base.obj;

import java.util.Map;
import javax.management.Attribute;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.katello.base.threading.KatelloCliWorker;
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
	public static final String CMD_TASK = "system task";
	public static final String CMD_TASKS = "system tasks";
	public static final String CMD_ADD_TO_GROUPS = "system add_to_groups";
	public static final String CMD_REMOVE_FROM_GROUPS = "system remove_from_groups";
	public static final String CMD_REGISTER = "system register";
	
	public static final String CMD_ADD_CUSTOM_INFO = "system custom_info add";
	public static final String CMD_UPDATE_CUSTOM_INFO = "system custom_info update";
	public static final String CMD_REMOVE_CUSTOM_INFO = "system custom_info remove";
	
	public static final String CMD_LIST_ERRATAS = "errata system";
	public static final String CMD_LIST_ERRATA_DETAILS = "errata system -v";
	
	public static final String RHSM_REGISTER ="subscription-manager register --username %s --password %s";
	public static final String RHSM_CLEAN = "subscription-manager clean";
	public static final String RHSM_SUBSCRIBE = "subscription-manager subscribe";
	public static final String RHSM_UNSUBSCRIBE = "subscription-manager unsubscribe";
	public static final String RHSM_IDENTITY = "subscription-manager identity";
	public static final String RHSM_REGISTER_BY_AK = "subscription-manager register ";
	public static final String RHSM_UNREGISTER = "subscription-manager unregister";
	public static final String RHSM_LIST_CONSUMED = "subscription-manager list --consumed";
	public static final String RHSM_REFRESH = "subscription-manager refresh";
	public static final String RHSM_ENVIRONMENTS ="subscription-manager environments --username %s --password %s";
	
	public static final String OUT_CREATE = 
			"The system has been registered with ID:";
	public static final String ERR_RHSM_LOBRARY_ONLY = 
			"Organization %s has the '%s' environment only. Please create an environment for system registration.";
	public static final String ERR_RHSM_REG_ALREADY_FORCE_NEEDED = 
			"This system is already registered. Use --force to override";
	public static final String ERR_RHSM_REG_MULTI_ENV = 
			"Organization %s has more than one environment. Please specify target environment for system registration.";
	public static final String ERR_GUEST_HAS_DIFFERENT_HOST = 
			"Guest's host does not match owner of pool: '%s'.";
	public static final String ERR_DELETE_ACCESS = 
			"Invalid credentials";
	public static final String ERR_UPDATE = "User %s is not allowed to access api/v1/systems/update";
	public static final String ERR_BLANK_KEYNAME = "Validation failed: Keyname can't be blank";
	public static final String ERR_DUPLICATE_KEYNAME = "Validation failed: Keyname already exists for this object";
	public static final String ERR_INVALID_KEY = "Couldn't find custom info with keyname '%s'"; 
	public static final String ERR_KEY_TOO_LONG = "Validation failed: Keyname is too long (maximum is 255 characters)";
	public static final String ERR_VALUE_TOO_LONG = "Validation failed: Value is too long (maximum is 255 characters)";
	public static final String ERR_NOT_FOUND = "Could not find System [ %s ] in Org [ %s ]";
	public static final String ERR_NO_DELETION_RECORD = "Deletion record for hypervisor %s not found.";
	public static final String ERR_NO_TASK = "Couldn't find TaskStatus with uuid = %s";
	
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
	public static final String OUT_RHSM_REGISTERED_OK = 
			"The system has been registered with id:";
	public static final String OUT_LIST_PACKAGES = "Package Information for System [ %s ] in Org [ %s ]";
	public static final String OUT_ADD_TO_GROUPS = "Successfully added system groups to system [ %s ]";
	public static final String OUT_REMOVE_FROM_GROUPS = "Successfully removed system groups from system [ %s ]";
	public static final String OUT_UNSUBSCRIBE = "Successfully removed subscription from System [ %s ]";
	public static final String OUT_REGISTRED = "Successfully registered system [ %s ]";
	public static final String OUT_UNREGISTRED = "Successfully unregistered System [ %s ]";
	
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
	
	public KatelloSystem(KatelloCliWorker kcr, String pName, String pOrg, String pEnv){
		this.name = pName;
		this.org = pOrg;
		this.env = pEnv;
		this.kcr = kcr;
	}
	
	public KatelloSystem(KatelloCliWorker kcr, String name, String org, String env, 
			String uuid, Long environmentId, String href, KatelloOwner owner, 
			Map<String, String> facts, KatelloIdCert idCert) {
	    this(kcr, name, org, env);
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
		String cmd = RHSM_REGISTER;
		if(this.user==null)
			cmd = String.format(RHSM_REGISTER,
					System.getProperty("katello.admin.user", KatelloUser.DEFAULT_ADMIN_USER),
					System.getProperty("katello.admin.password", KatelloUser.DEFAULT_ADMIN_PASS));
		else
			cmd = String.format(RHSM_REGISTER,user.username,user.password);
			
		if(this.name != null)
			cmd += " --name \""+this.name+"\"";
		if(this.org != null)
			cmd += " --org \""+this.org+"\"";
		if(this.env != null)
			cmd += " --environment \""+this.env+"\"";
		
		return KatelloUtils.sshOnClient(getHostName(), cmd);		
	}
	
	public SSHCommandResult rhsm_registerForce(){
		String cmd = RHSM_REGISTER;
		if(this.user==null)
			cmd = String.format(RHSM_REGISTER,
					System.getProperty("katello.admin.user", KatelloUser.DEFAULT_ADMIN_USER),
					System.getProperty("katello.admin.password", KatelloUser.DEFAULT_ADMIN_PASS));
		else
			cmd = String.format(RHSM_REGISTER,user.username,user.password);
		
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
		String cmd = RHSM_REGISTER_BY_AK;
		
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
		String cmd = RHSM_REGISTER_BY_AK;
		
		if(this.name != null)
			cmd += " --name \""+this.name+"\"";
		if(this.org != null)
			cmd += " --org \""+this.org+"\"";
		if(act_list != null)
			cmd += " --activationkey \"" + act_list +"\"";
		cmd += " --force";
		
		return KatelloUtils.sshOnClient(getHostName(), cmd);		
	}
	
	public SSHCommandResult rhsm_registerForce_release(String release, boolean autosubscribe, boolean force){
		String cmd = RHSM_REGISTER;
		if(this.user==null)
			cmd = String.format(RHSM_REGISTER,
					System.getProperty("katello.admin.user", KatelloUser.DEFAULT_ADMIN_USER),
					System.getProperty("katello.admin.password", KatelloUser.DEFAULT_ADMIN_PASS));
		else
			cmd = String.format(RHSM_REGISTER,user.username,user.password);
		
		if(this.name != null)
			cmd += " --name \""+this.name+"\"";
		if(this.org != null)
			cmd += " --org \""+this.org+"\"";
		if(this.env != null)
			cmd += " --environment \""+this.env+"\"";
		if(release != null)
			cmd += " --release \""+release+"\"";
		if(force)
			cmd += " --force";
		if(autosubscribe)
			cmd += " --autosubscribe";
		
		return KatelloUtils.sshOnClient(getHostName(), cmd);		
	}
	
	public SSHCommandResult rhsm_registerForceWithReleaseSLA(String release, String sla, boolean autosubscribe, boolean force){
		String cmd = RHSM_REGISTER;
		if(this.user==null)
			cmd = String.format(RHSM_REGISTER,
					System.getProperty("katello.admin.user", KatelloUser.DEFAULT_ADMIN_USER),
					System.getProperty("katello.admin.password", KatelloUser.DEFAULT_ADMIN_PASS));
		else
			cmd = String.format(RHSM_REGISTER,user.username,user.password);
		
		if(this.name != null)
			cmd += " --name \""+this.name+"\"";
		if(this.org != null)
			cmd += " --org \""+this.org+"\"";
		if(this.env != null)
			cmd += " --environment \""+this.env+"\"";
		if(release != null)
			cmd += " --release \""+release+"\"";
		if(sla != null)
			cmd += " --servicelevel \""+sla+"\" --auto-attach";
		if(force)
			cmd += " --force";
		if(autosubscribe)
			cmd += " --autosubscribe";
		
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
		opts.add(new Attribute("environment", env));
		return run(CMD_PACKAGES);
	}
	
	
	public SSHCommandResult packages_install_group(String packagegroupName){
		opts.clear();
		opts.add(new Attribute("install_groups", packagegroupName));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		return run(CMD_PACKAGES);
	}
	
	public SSHCommandResult list_packages() {
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		return run(CMD_PACKAGES);
	}
	
	public SSHCommandResult packages_update(String packageNames) {
		opts.clear();
		opts.add(new Attribute("update", packageNames));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		return run(CMD_PACKAGES);
	}


	public SSHCommandResult packages_remove(String packageNames) {
		opts.clear();
		opts.add(new Attribute("remove", packageNames));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		return run(CMD_PACKAGES);
	}


	public SSHCommandResult packages_remove_groups(String packageGroupNames) {
		opts.clear();
		opts.add(new Attribute("remove_groups", packageGroupNames));
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
	
	public SSHCommandResult rhsm_environments(){
		String cmd = RHSM_ENVIRONMENTS;
		if(org != null)
			cmd += " --org \""+org+"\"";
		if(this.user==null)
			cmd = String.format(cmd,
					System.getProperty("katello.admin.user", KatelloUser.DEFAULT_ADMIN_USER),
					System.getProperty("katello.admin.password", KatelloUser.DEFAULT_ADMIN_PASS));
		else
			cmd = String.format(cmd,user.username,user.password);
		
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

	public SSHCommandResult unsubscribe(String poolID){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("entitlement", poolID));
		return run("system unsubscribe");
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
	
	public SSHCommandResult task(String taskId) {
		opts.clear();
		opts.add(new Attribute("id", taskId));
		return run(CMD_TASK);
	}

	public SSHCommandResult tasks() {
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		return run(CMD_TASKS);
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
		opts.add(new Attribute("uuid", uuid));
		
		return run(CMD_UPDATE_CUSTOM_INFO);
	}	
	
	public SSHCommandResult remove_custom_info(String keyname){
		opts.clear();
		opts.add(new Attribute("environment", env));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("keyname", keyname));
		opts.add(new Attribute("uuid", uuid));
		
		return run(CMD_REMOVE_CUSTOM_INFO);
	}	

	public SSHCommandResult update_content_view(String view) {
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("content_view", view));

		return run(CMD_UPDATE);
	}

	public SSHCommandResult add_to_groups(String groups) {
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("system_groups", groups));
		return run(CMD_ADD_TO_GROUPS);
	}

	public SSHCommandResult remove_from_groups(String groups) {
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("system_groups", groups));
		return run(CMD_REMOVE_FROM_GROUPS);
	}

	public SSHCommandResult register() {
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("environment", env));
		return run(CMD_REGISTER);
	}
}
