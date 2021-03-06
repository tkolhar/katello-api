package com.redhat.qe.katello.base.obj;

import java.util.logging.Logger;

import javax.management.Attribute;

import com.redhat.qe.katello.base.threading.KatelloCliWorker;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloContentView extends _KatelloObject{
    protected static Logger log = Logger.getLogger(KatelloContentView.class.getName());
	
	public static final String CMD_PROMOTE_VIEW = "content view promote";
	public static final String CMD_REFRESH_VIEW = "content view refresh";
	public static final String CMD_VIEW_INFO = "content view info";
	public static final String CMD_VIEW_LIST = "content view list";
	public static final String CMD_VIEW_DELETE = "content view delete";
	
	public static final String OUT_PROMOTE =
			"Content view [ %s ] promoted to environment [ %s ]";
	public static final String OUT_REFRESH =
			"Content view [ %s ] was successfully refreshed.";
	public static final String OUT_DELETE = "Content view [ %s ] was successfully deleted.";
	public static final String OUT_REG_REFRESH_ASYNC = "Refresh task \\[ [0-9a-f-]+ \\] was successfully created.";
	public static final String OUT_REG_PROMOTE_ASYNC = "Promotion task \\[ [0-9a-f-]+ \\] was successfully created.";
		
	public static final String ERR_VIEW_READ =
			"User %s is not allowed to access api/v1/content_views/index";
	public static final String ERR_PROMOTE_DENIED =
			"User %s is not allowed to access api/v1/content_views/promote";
	public static final String ERR_CANNOT_DELETE = "Content view [ %s ] cannot be deleted since it is in non-library environments.";
	public static final String ERR_NOT_FOUND = "Could not find content view [ %s ] within organization [ %s ]";
	
	public static final String REG_VIEW_INFO = ".*ID\\s*:\\s*\\d+.*Name\\s*:\\s*%sLabel\\s*:\\s*%s.*Description\\s*:\\s*%s.*Org\\s*:\\s*%s.*Definition\\s*:\\s*%s.*Environments\\s*:\\s*%s.*Versions\\s*:\\s*%s.*";
	
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
	
	public KatelloContentView(KatelloCliWorker kcr, String pName, String pOrg){
		this.name = pName;
		this.org = pOrg;
		this.kcr = kcr;
	}
	
	public KatelloContentView(KatelloCliWorker kcr, String name, String description, String pOrg, String label) {
	    this(kcr, name, description);
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

	public SSHCommandResult refresh_view() {
		opts.clear();
		opts.add(new Attribute("name", this.name));
		opts.add(new Attribute("org", this.org));
		return run(CMD_REFRESH_VIEW);
	}
	
	public SSHCommandResult promote_view(String environment) {
		opts.clear();
		opts.add(new Attribute("name", this.name));
		opts.add(new Attribute("environment", environment));
		opts.add(new Attribute("org", this.org));
		return run(CMD_PROMOTE_VIEW);
	}
	
	public SSHCommandResult view_list() {
		opts.clear();
		opts.add(new Attribute("org", this.org));
		return run(CMD_VIEW_LIST);
	}

	public SSHCommandResult view_info() {
		opts.clear();
		opts.add(new Attribute("org", this.org));
		opts.add(new Attribute("name", this.name));
		return run(CMD_VIEW_INFO);
	}

	public SSHCommandResult delete_view() {
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		return run(CMD_VIEW_DELETE);
	}

	public SSHCommandResult promote_async(String environment) {
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("environment", environment));
		return run(CMD_PROMOTE_VIEW + " --async");
	}

	public SSHCommandResult refresh_async() {
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		return run(CMD_REFRESH_VIEW + " --async");
	}

	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **
}
