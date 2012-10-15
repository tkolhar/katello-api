package com.redhat.qe.katello.base.obj;

import javax.management.Attribute;

import org.codehaus.jackson.annotate.JsonProperty;

import com.redhat.qe.tools.SSHCommandResult;

public class KatelloProvider extends _KatelloObject{
	
	// ** ** ** ** ** ** ** Public constants
	public static final String PROVIDER_REDHAT = "Red Hat";
	public static final String CMD_CREATE = "provider create";
	public static final String CMD_IMPORT_MANIFEST = "provider import_manifest";
	public static final String CLI_CMD_LIST = "provider list -v";
	public static final String CMD_INFO = "provider info";
	public static final String CMD_SYNCHRONIZE = "provider synchronize";
	public static final String CMD_UPDATE = "provider update";
	public static final String CMD_DELETE = "provider delete";
	public static final String CMD_STATUS = "provider status";
	
	public static final String OUT_CREATE = 
			"Successfully created provider [ %s ]";
	public static final String OUT_DELETE = 
			"Deleted provider [ %s ]";
	public static final String OUT_SYNCHRONIZE = 
			"Provider [ %s ] synchronized";
	public static final String ERR_REDHAT_UPDATENAME = 
			"Validation failed: the following attributes can not be updated for the Red Hat provider: [ name ]";
	public static final String OUT_UPDATE = 
			"Successfully updated provider [ %s ]";
	
	public static final String ERR_PROVIDER_DELETE = 
			"Provider cannot be deleted since one of its products or repositories has already been promoted. Using a changeset, please delete the repository from existing environments before deleting it.";
	
	public static final String API_CMD_LIST = "/organizations/%s/providers";
	
	public static final String MANIFEST_12SUBSCRIPTIONS = "manifest-automation-CLI-12subscriptions.zip";
	
	public static final String CDN_URL = "https://cdn.redhat.com";

	public static final String REG_REDHAT_LIST = ".*Id\\s*:\\s+\\d+.*Name\\s*:\\s+"+KatelloProvider.PROVIDER_REDHAT+".*Type\\s*:\\s+Red\\sHat.*Url\\s*:\\s+%s.*";
	
	public static final String REG_REDHAT_STATUS = ".*Id\\s*:\\s+\\d+.*Name\\s*:\\s+"+KatelloProvider.PROVIDER_REDHAT+".*Last\\sSync\\s*:\\s+%s.*Sync\\sState\\s*:\\s+%s.*";
	
	public static final String REG_REDHAT_INFO = ".*Id\\s*:\\s+\\d+.*Name\\s*:\\s+"+KatelloProvider.PROVIDER_REDHAT+".*Type\\s*:\\s+Red Hat.*Url\\s*:\\s+%s.*Org Id\\s*:\\s+%s.*Description\\s*:\\s+%s.*";
	
	public static final String REG_PROVIDER_LIST = ".*Id\\s*:\\s+\\d+.*Name\\s*:\\s+%s.*Type\\s*:\\s+Custom.*Url\\s*:\\s+%s.*Description\\s*:\\s+%s.*";
	
	// ** ** ** ** ** ** ** Class members
	public String name;
	public String org;
	public String description;
	public String url;
	private Long id;
	private Long organizationId;
	private String providerType;
	private String updatedAt;
	
	public KatelloProvider(){super();}
	
	public KatelloProvider(String pName, String pOrg, 
			String pDesc, String pUrl){
		this.name = pName;
		this.org = pOrg;
		this.description = pDesc;
		this.url = pUrl;
	}
	
	public KatelloProvider(Long id, String name, String org, String desc, String url, Long organizationId, String providerType, String updatedAt) {
	    this(name, org, desc, url);
	    this.id = id;
	    this.organizationId = organizationId;
	    this.providerType = providerType;
	    this.updatedAt = updatedAt;
	}

    public String getName() {
        return name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    public String getDescription() {
        return description;
    }

    @JsonProperty("organization_id")
    public Long getOrganizationId() {
        return organizationId;
    }
    
    @JsonProperty("organization_id")
    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }
    
    @JsonProperty("provider_type")
    public String getProviderType() {
        return providerType;
    }

    @JsonProperty("provider_type")
    public void setProviderType(String providerType) {
        this.providerType = providerType;
    }
    
    @JsonProperty("updated_at")
    public String getUpdatedAt() {
        return updatedAt;
    }
    
    @JsonProperty("updated_at")
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @JsonProperty("repository_url")
    public String getRepositoryUrl() {
        return url;
    }
    
    @JsonProperty("repository_url")
    public void setRepositoryUrl(String url) {
        this.url = url;
    }
    
	public SSHCommandResult create(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("description", description));
		opts.add(new Attribute("url", url));
		return run(CMD_CREATE);
	}
	

	public SSHCommandResult import_manifest(String file, Boolean force){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("file", file));
		opts.add(new Attribute("force", ""));
		return run(CMD_IMPORT_MANIFEST);
	}

	public SSHCommandResult cli_list(){
		opts.clear();
		opts.add(new Attribute("org", org));
		return run(CLI_CMD_LIST);
	}
		
	public SSHCommandResult info(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		return run(CMD_INFO);
	}

	public SSHCommandResult synchronize(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		return run(CMD_SYNCHRONIZE);
	}

	public SSHCommandResult update(String new_name, String url, String description){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("new_name", new_name));
		opts.add(new Attribute("url", url));
		opts.add(new Attribute("description", description));
		return run(CMD_UPDATE);
	}

	public SSHCommandResult delete(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		return run(CMD_DELETE);
	}

	public SSHCommandResult status(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		return run(CMD_STATUS);
	}

	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **
}
