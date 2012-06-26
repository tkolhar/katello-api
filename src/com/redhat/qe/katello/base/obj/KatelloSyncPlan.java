package com.redhat.qe.katello.base.obj;

import java.util.ArrayList;
import javax.management.Attribute;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloSyncPlan {
	
	public enum SyncPlanInterval {none,hourly,daily,weekly};
	
	// ** ** ** ** ** ** ** Public constants
	public static final String CMD_CREATE = "sync_plan create";
	

	// ** ** ** ** ** ** ** Class members
	public String name;
	public String org;
	public String description;
	public String date;
	public String time;
	public String interval = null;

	private KatelloCli cli;
	private ArrayList<Attribute> opts;
	
	public KatelloSyncPlan(String name, String org, String description, String date, String time, SyncPlanInterval interval){
		this.name = name;
		this.org = org;
		this.description = description;
		this.date = date;
		this.time = time;
		if (interval != null)
			this.interval = interval.toString();// else is null
		this.opts = new ArrayList<Attribute>();
	}
	
	public SSHCommandResult create(){
		opts.clear();
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("description", description));
		opts.add(new Attribute("date", date));
		opts.add(new Attribute("time", time));
		opts.add(new Attribute("interval", interval));
		cli = new KatelloCli(CMD_CREATE, opts);
		return cli.run();
	}

	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **
	
}
