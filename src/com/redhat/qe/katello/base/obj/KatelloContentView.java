package com.redhat.qe.katello.base.obj;

import java.util.logging.Logger;

import javax.management.Attribute;

import com.redhat.qe.tools.SSHCommandResult;

public class KatelloContentView extends _KatelloObject{
    protected static Logger log = Logger.getLogger(KatelloContentView.class.getName());
	
	public static final String CMD_DEFINITION_CREATE = "content definition create";
	public static final String CMD_DEFINITION_INFO = "content definition info";
	public static final String CMD_DEFINITION_LIST = "content definition list";
	public static final String CMD_DEFINITION_UPDATE = "content definition update";
	public static final String CMD_DEFINITION_DELETE = "content definition delete";
	public static final String CMD_DEFINITION_PUBLISH = "content definition publish";
	public static final String CMD_DEFINITION_ADD_PRODUCT = "content definition add_product";
	public static final String CMD_DEFINITION_REMOVE_PRODUCT = "content definition remove_product";
	public static final String CMD_DEFINITION_ADD_REPO = "content definition add_repo";
	public static final String CMD_DEFINITION_REMOVE_REPO = "content definition remove_repo";
	public static final String CMD_DEFINITION_ADD_VIEW = "content definition add_view";
	public static final String CMD_DEFINITION_REMOVE_VIEW = "content definition remove_view";
	public static final String CMD_PROMOTE_VIEW = "content view promote";
	public static final String CMD_REFRESH_VIEW = "content view refresh";
	public static final String CMD_VIEW_INFO = "content view info";
	public static final String CMD_VIEW_LIST = "content view list";
	
	public static final String OUT_CREATE_DEFINITION = 
			"Successfully created content view definition [ %s ]";
	public static final String OUT_DELETE_DEFINITION = 
			"Successfully deleted definition [ %s ]";
	public static final String OUT_ADD_SYS_INFO = 
			"Successfully added default custom info key [ %s ] to Org [ %s ]";
	public static final String OUT_ADD_PRODUCT = 
			"Added product [ %s ] to definition [ %s ]";
	public static final String OUT_REMOVE_PRODUCT = 
			"Removed product [ %s ] to definition [ %s ]";
	public static final String OUT_ADD_REPO = 
			"Added repository [ %s ] to definition [ %s ]";
	public static final String OUT_REMOVE_REPO = 
			"Removed repository [ %s ] to definition [ %s ]";
	public static final String OUT_APPLY_SYS_INFO = 
			"Successfully applied default custom info keys to [ %s ] systems in Org [ %s ]";
	public static final String OUT_REMOVE_SYS_INFO = 
			"Successfully removed default custom info key [ %s ] for Org [ %s ]";
	public static final String OUT_PROMOTE =
			"Content view [ %s ] promoted to environment [ %s ]";
	public static final String OUT_REFRESH =
			"Content view [ %s ] was successfully refreshed.";
	
	public static final String ERR_DEFINITION_EXISTS = 
			"Validation failed: Label has already been taken, Name has already been taken";
	public static final String ERR_NAME_INVALID = 
			"Validation failed: Name cannot contain characters other than alpha numerals, space,'_', '-'.";
	public static final String ERR_NAME_EMPTY = 
			"Name can't be blank, Name must contain at least 1 character";
	public static final String ERR_NAME_LONG = 
			"Validation failed: Name cannot contain more than 128 characters, Label cannot contain more than 128 characters";
	public static final String ERR_ORG_NOTFOUND = 
			"Couldn't find organization '%s'";
	public static final String ERR_CREATE_DENIED = 
			"User %s is not allowed to access api/content_view_definitions/create";
	public static final String ERR_PUBLISH_DENIED =
			"User %s is not allowed to access api/content_view_definitions/publish";
	public static final String ERR_DELETE_DENIED =
			"User %s is not allowed to access api/content_view_definitions/destroy";
	public static final String ERR_VIEW_READ =
			"User %s is not allowed to access api/content_views/index";
	public static final String ERR_PROMOTE_DENIED =
			"User %s is not allowed to access api/content_views/promote";
	
	public static final String REG_DEF_INFO = ".*ID\\s*:\\s+\\d+.*Name\\s*:\\s+%sLabel\\s*:\\s+%s.*Description\\s*:\\s+%s.*Org\\s*:\\s+%s.*Published Views\\s*:\\s+%s.*Component Views\\s*:\\s+%s.*Products\\s*:\\s+%s.*Repos\\s*:\\s*%s.*";
	public static final String REG_DEF_LIST = ".*\\s+\\d+.*\\s+%s.*\\s+%s.*\\s+%s.*\\s+%s.*";
	public static final String REG_VIEW_INFO = ".*ID\\s*:\\s*\\d+.*Name\\s*:\\s*%sLabel\\s*:\\s*%s.*Description\\s*:\\s*%s.*Org\\s*:\\s*%s.*Definition\\s*:\\s*%s.*Environments\\s*:\\s*%s.*Versions\\s*:\\s*%s.*Repos\\s*:\\s*%s.*";
	
	// ** ** ** ** ** ** ** Class members
	public String name;
	public String description;
	public String label;
	private Long id;
	public String org;
	public String publishedViews = "";
	public String componentViews = "";
	public String products = "";
	public String repos = "";
	
	public KatelloContentView(){super();}
	
	private KatelloContentView(String pName, String pDesc){
		this.name = pName;
		this.description = pDesc;
	}
	
	public KatelloContentView(String name, String description, String pOrg, String label) {
	    this(name, description);
	    this.org = pOrg;
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
	
	public String getLabel() {
	    return label;
	}
	
	public void setLabel(String label) {
	    this.label = label;
	}

	public SSHCommandResult create_definition(){		
		return create_definition(false);
	}

	public SSHCommandResult create_definition(boolean isComposite){		
		opts.clear();
		opts.add(new Attribute("name", this.name));
		opts.add(new Attribute("description", this.description));
		opts.add(new Attribute("label", this.label));
		opts.add(new Attribute("org", this.org));
		if (isComposite) opts.add(new Attribute("composite", "true"));
		return run(CMD_DEFINITION_CREATE);
	}
	
	public SSHCommandResult definition_info(){
		opts.clear();
		if (this.id != null) {
			opts.add(new Attribute("id", this.id));
		} else {
			opts.add(new Attribute("name", this.name));
		}
		opts.add(new Attribute("org", this.org));
		return run(CMD_DEFINITION_INFO);
	}
	
	public SSHCommandResult definition_list(){
		opts.clear();
		opts.add(new Attribute("org", this.org));
		return run(CMD_DEFINITION_LIST+" -v");
	}

	public SSHCommandResult definition_delete(){
		opts.clear();
		opts.add(new Attribute("id", this.id));
		opts.add(new Attribute("org", this.org));
		opts.add(new Attribute("label", this.label));
		return run(CMD_DEFINITION_DELETE);
	}
	
	public SSHCommandResult definition_update(String new_description){
		opts.clear();
		opts.add(new Attribute("name", this.name));
		opts.add(new Attribute("description", new_description));
		opts.add(new Attribute("org", this.org));
		opts.add(new Attribute("label", this.label));
		return run(CMD_DEFINITION_UPDATE);
	}

	public SSHCommandResult add_repo(String product, String repo){
		opts.clear();
		opts.add(new Attribute("name", this.name));
		opts.add(new Attribute("org", this.org));
		opts.add(new Attribute("product", product));
		opts.add(new Attribute("repo", repo));
		return run(CMD_DEFINITION_ADD_REPO);
	}
	
	public SSHCommandResult remove_repo(String product, String repo){
		opts.clear();
		opts.add(new Attribute("name", this.name));
		opts.add(new Attribute("org", this.org));
		opts.add(new Attribute("product", product));
		opts.add(new Attribute("repo", repo));
		return run(CMD_DEFINITION_REMOVE_REPO);
	}
	
	public SSHCommandResult add_product(String product){
		opts.clear();
		opts.add(new Attribute("name", this.name));
		opts.add(new Attribute("org", this.org));
		opts.add(new Attribute("product", product));
		return run(CMD_DEFINITION_ADD_PRODUCT);
	}
	
	public SSHCommandResult remove_product(String product){
		opts.clear();
		opts.add(new Attribute("name", this.name));
		opts.add(new Attribute("org", this.org));
		opts.add(new Attribute("product", product));
		return run(CMD_DEFINITION_REMOVE_PRODUCT);
	}

	public SSHCommandResult add_view(String view){
		opts.clear();
		opts.add(new Attribute("name", this.name));
		opts.add(new Attribute("org", this.org));
		opts.add(new Attribute("view_name", view));
		return run(CMD_DEFINITION_ADD_VIEW);
	}
	
	public SSHCommandResult remove_view(String view){
		opts.clear();
		opts.add(new Attribute("name", this.name));
		opts.add(new Attribute("org", this.org));
		opts.add(new Attribute("view_name", view));
		return run(CMD_DEFINITION_REMOVE_VIEW);
	}

	public SSHCommandResult publish(String name, String label, String description){
		opts.clear();
		opts.add(new Attribute("name", this.name));
		opts.add(new Attribute("org", this.org));
		opts.add(new Attribute("view_name", name));
		opts.add(new Attribute("view_label", label));
		opts.add(new Attribute("description", description));
		return run(CMD_DEFINITION_PUBLISH);
	}

	public SSHCommandResult refresh_view(String view){
		opts.clear();
		opts.add(new Attribute("name", view));
		opts.add(new Attribute("org", this.org));
		return run(CMD_REFRESH_VIEW);
	}
	
	public SSHCommandResult promote_view(String view, String environment){
		opts.clear();
		opts.add(new Attribute("name", view));
		opts.add(new Attribute("environment", environment));
		opts.add(new Attribute("org", this.org));
		return run(CMD_PROMOTE_VIEW);
	}
	
	public SSHCommandResult view_list() {
		opts.clear();
		opts.add(new Attribute("org", this.org));
		return run(CMD_VIEW_LIST);
	}

	public SSHCommandResult view_info(String name) {
		opts.clear();
		opts.add(new Attribute("org", this.org));
		opts.add(new Attribute("name", name));
		return run(CMD_VIEW_INFO);
	}

	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **
}
