package com.redhat.qe.katello.base.obj;

import java.util.logging.Logger;

import javax.management.Attribute;

import com.redhat.qe.tools.SSHCommandResult;

public class KatelloContentFilter extends _KatelloObject{
    protected static Logger log = Logger.getLogger(KatelloContentFilter.class.getName());
	
	public static final String CMD_CREATE = "content definition filter create";
	public static final String CMD_DELETE = "content definition filter delete";
	
	
	public static final String REG_VIEW_INFO = ".*ID\\s*:\\s*\\d+.*Name\\s*:\\s*%sLabel\\s*:\\s*%s.*Description\\s*:\\s*%s.*Org\\s*:\\s*%s.*Definition\\s*:\\s*%s.*Environments\\s*:\\s*%s.*Versions\\s*:\\s*%s.*Repos\\s*:\\s*%s.*";
	
	// ** ** ** ** ** ** ** Class members
	public String name;
	public String definition;
	private Long id;
	public String org;
	
	public KatelloContentFilter(){super();}
	
	public KatelloContentFilter(String pName, String pOrg, String pDefinition){
		this.name = pName;
		this.org = pOrg;
		this.definition = pDefinition;
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

	public SSHCommandResult create() {		
		opts.clear();
		opts.add(new Attribute("name", this.name));
		opts.add(new Attribute("definition", this.definition));
		opts.add(new Attribute("org", this.org));
		return run(CMD_CREATE);
	}

	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **
}
