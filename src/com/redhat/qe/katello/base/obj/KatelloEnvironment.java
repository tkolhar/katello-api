package com.redhat.qe.katello.base.obj;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.management.Attribute;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.redhat.qe.katello.base.KatelloApi;
import com.redhat.qe.katello.base.KatelloApiException;
import com.redhat.qe.katello.base.KatelloApiResponse;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.katello.base.KatelloPostParam;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.tools.SSHCommandResult;

@JsonIgnoreProperties(ignoreUnknown=true)
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
	private String name;
	String description;
	String org;
	String prior;
	private String prior_id = null;
	private Long priorId;
	private Long id;
	private String updatedAt;
	private Long organizationId;
	private String organizationKey;
	
	private KatelloCli cli;
	private ArrayList<Attribute> opts;
	
	// No-arg constructor for RestEasy
	public KatelloEnvironment() {}
	
	public KatelloEnvironment(String pName, String pDesc,
			String pOrg, String pPrior){
		this.name = pName;
		this.description = pDesc;
		this.org = pOrg;
		this.prior = pPrior;
		this.opts = new ArrayList<Attribute>();
	}
	
	protected KatelloEnvironment(String name, String desc, String org, String prior, Long id, String updatedAt, Long organizationId) {
	    this(name, desc, org, prior);
	    this.id = id;
	    this.updatedAt = updatedAt;
	    this.organizationId = organizationId;
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
    
	public SSHCommandResult cli_create(){
		return cli_create(null);
	}

	public SSHCommandResult cli_create(KatelloUser user){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("description", description));
		opts.add(new Attribute("prior", prior));
		if (user == null) {
			cli = new KatelloCli(CMD_CREATE, opts);
		} else {
			cli = new KatelloCli(CMD_CREATE, opts, user);
		}
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
	 
	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **
	
}
