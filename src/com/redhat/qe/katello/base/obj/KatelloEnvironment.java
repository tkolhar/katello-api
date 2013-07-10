package com.redhat.qe.katello.base.obj;

import java.util.logging.Logger;
import javax.management.Attribute;
import org.codehaus.jackson.annotate.JsonProperty;

import com.redhat.qe.katello.base.threading.KatelloCliWorker;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloEnvironment extends _KatelloObject{
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
	private String name;
	String description;
	String org;
	String prior;
	String label;
	private Long priorId;
	private Long id;
	private String updatedAt;
	private Long organizationId;
	private String organizationKey;
	
	public KatelloEnvironment(){super();}
	
	public KatelloEnvironment(KatelloCliWorker kcr, String pName, String pDesc,
			String pOrg, String pPrior){
		this.name = pName;
		this.description = pDesc;
		this.org = pOrg;
		this.prior = pPrior;
		this.kcr = kcr;
	}
	
	protected KatelloEnvironment(KatelloCliWorker kcr, String name, String desc, String org, String prior, Long id, String updatedAt, Long organizationId) {
	    this(kcr, name, desc, org, prior);
	    this.id = id;
	    this.updatedAt = updatedAt;
	    this.organizationId = organizationId;
	}
	
	public KatelloEnvironment(KatelloCliWorker kcr, String name, String desc,
			String org, String prior, String label){
		this(kcr, name, desc, org, prior);
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

	@JsonProperty("updated_at")
    public String getUpdatedAt() {
        return updatedAt;
    }
	
	@JsonProperty("updated_at")
	public void setUpdatedAt(String updatedAt) {
	    this.updatedAt = updatedAt;
	}

    @JsonProperty("organization_id")
    public Long getOrganizationId() {
        return organizationId;
    }

    @JsonProperty("organization_id")
    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    @JsonProperty("organization")
    public String getOrganizationKey() {
        return organizationKey;        
    }
    
    @JsonProperty("organization")
    public void setOrganizationKey(String organizationKey) {
        this.organizationKey = organizationKey;
    }
    
    @JsonProperty("prior_id")
    public Long getPriorId() {
        return priorId;
    }
    
    @JsonProperty("prior_id")
    public void setPriorId(Long priorId) {
        this.priorId = priorId;
    }

    public String getPrior() {
        return prior;
    }
    
    public void setPrior(String prior) {
        this.prior = prior;
    }
    
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
	public String getLabel() {
	    return label;
	}

	public void setLabel(String label) {
	    this.label = label;
	}

	public SSHCommandResult cli_create(){
		return cli_create(null);
	}

	public SSHCommandResult cli_create(KatelloUser user){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("description", description));
		opts.add(new Attribute("prior", prior));
		opts.add(new Attribute("label", label));
		return run(CMD_CREATE);
	}
	
	public SSHCommandResult cli_info(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		return run(CMD_INFO);
	}
	

	public SSHCommandResult cli_list(){
		opts.clear();
		opts.add(new Attribute("org", org));
		return run(CLI_CMD_LIST);
	}

	
	public SSHCommandResult cli_delete(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		return run(CMD_DELETE);
	}
	
	public SSHCommandResult cli_update(String descr){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("description", descr));
		opts.add(new Attribute("prior", prior));
		opts.add(new Attribute("label", label));
		return run(CMD_UPDATE);
	}
	 
	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **
	
}
