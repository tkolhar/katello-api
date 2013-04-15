package com.redhat.qe.katello.base.obj;

import java.util.logging.Logger;

import javax.management.Attribute;

import com.redhat.qe.tools.SSHCommandResult;

public class KatelloContentDefinition extends _KatelloObject{
    protected static Logger log = Logger.getLogger(KatelloContentDefinition.class.getName());
    
	public static final String CMD_DEFINITION_CREATE = "content definition create";
	public static final String CMD_DEFINITION_INFO = "content definition info";
	public static final String CMD_DEFINITION_LIST = "content definition list -v";
	public static final String CMD_DEFINITION_UPDATE = "content definition update";
	public static final String CMD_DEFINITION_DELETE = "content definition delete";
	public static final String CMD_DEFINITION_PUBLISH = "content definition publish";
	public static final String CMD_DEFINITION_ADD_PRODUCT = "content definition add_product";
	public static final String CMD_DEFINITION_REMOVE_PRODUCT = "content definition remove_product";
	public static final String CMD_DEFINITION_ADD_REPO = "content definition add_repo";
	public static final String CMD_DEFINITION_REMOVE_REPO = "content definition remove_repo";
	public static final String CMD_DEFINITION_ADD_VIEW = "content definition add_view";
	public static final String CMD_DEFINITION_REMOVE_VIEW = "content definition remove_view";
	public static final String CMD_CLONE = "content definition clone";
    
	public static final String OUT_CLONE_SUCCESSFUL = 
			"Successfully created cloned definition [ %s ]";

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
	
	private KatelloContentDefinition(String pName, String pDesc){
		this.name = pName;
		this.description = pDesc;
	}
	
	public KatelloContentDefinition(String name, String description, String pOrg, String label) {
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
	
// ** ** ** public CLI methods	
	public SSHCommandResult create(){		
		return create(false);
	}

	public SSHCommandResult create(boolean isComposite){		
		opts.clear();
		opts.add(new Attribute("name", this.name));
		opts.add(new Attribute("description", this.description));
		opts.add(new Attribute("label", this.label));
		opts.add(new Attribute("org", this.org));
		if (isComposite) opts.add(new Attribute("composite", "true"));
		return run(CMD_DEFINITION_CREATE);
	}
	
	public SSHCommandResult info(){
		opts.clear();
		if (this.id != null) {
			opts.add(new Attribute("id", this.id));
		} else {
			opts.add(new Attribute("name", this.name));
		}
		opts.add(new Attribute("org", this.org));
		return run(CMD_DEFINITION_INFO);
	}
	
	public SSHCommandResult list(){
		opts.clear();
		opts.add(new Attribute("org", this.org));
		return run(CMD_DEFINITION_LIST);
	}

	public SSHCommandResult delete(){
		opts.clear();
		opts.add(new Attribute("id", this.id));
		opts.add(new Attribute("org", this.org));
		opts.add(new Attribute("label", this.label));
		return run(CMD_DEFINITION_DELETE);
	}
	
	public SSHCommandResult update(String new_description){
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
		opts.add(new Attribute("content_view", view));
		return run(CMD_DEFINITION_ADD_VIEW);
	}
	
	public SSHCommandResult remove_view(String view){
		opts.clear();
		opts.add(new Attribute("name", this.name));
		opts.add(new Attribute("org", this.org));
		opts.add(new Attribute("content_view", view));
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

	public SSHCommandResult clone(String name, String label, String description){
		opts.clear();
		opts.add(new Attribute("org", this.org));
		opts.add(new Attribute("from_name", this.name));
		opts.add(new Attribute("from_label", this.label));
		opts.add(new Attribute("from_id", this.id));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("label", label));
		opts.add(new Attribute("description", description));
		return run(CMD_CLONE);
	}
	
}
