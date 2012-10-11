package com.redhat.qe.katello.base.obj;

import java.util.ArrayList;
import javax.management.Attribute;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloSyncPlan {
	
	public enum SyncPlanInterval {none,hourly,daily,weekly};
	
	// ** ** ** ** ** ** ** Public constants
	public static final String CMD_CREATE = "sync_plan create";
	public static final String CMD_INFO = "sync_plan info";
	public static final String CMD_LIST = "sync_plan list";
	public static final String CMD_UPDATE = "sync_plan update";
	public static final String CMD_DELETE = "sync_plan delete";
	
	public static final String REG_SYNCPLAN_INFO = ".*Id\\s*:\\s+\\d+.*Name\\s*:\\s+%s.*Description\\s*:\\s*%s.*Start date\\s*:\\s+%s.*Interval\\s*:\\s+%s.*";
	public static final String REG_SYNCPLAN_LIST = ".*\\s+\\d+.*\\s+%s.*\\s+%s.*\\s+%s.*\\s+%s.*";
	public static final String ERR_NOT_FOUND = "Cannot find sync plan [ %s ]";
	public static final String OUT_DELETE = "Successfully deleted sync plan [ %s ]";
	
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
	
	public SSHCommandResult update_name(String newname){
		opts.clear();
		opts.add(new Attribute("new_name", newname));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		cli = new KatelloCli(CMD_UPDATE, opts);
		return cli.run();
	}

	public SSHCommandResult update_date(String newdate, String newtime){
		opts.clear();
		opts.add(new Attribute("date", newdate));
		opts.add(new Attribute("time", newtime));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		cli = new KatelloCli(CMD_UPDATE, opts);
		return cli.run();
	}
	
	
	public SSHCommandResult update_interval(SyncPlanInterval newinterval){
		opts.clear();
		opts.add(new Attribute("interval", newinterval.toString()));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		cli = new KatelloCli(CMD_UPDATE, opts);
		return cli.run();
	}
	
	public SSHCommandResult info(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		cli = new KatelloCli(CMD_INFO, opts);
		return cli.run();
	}

	public SSHCommandResult list(){
		opts.clear();
		opts.add(new Attribute("org", org));
		cli = new KatelloCli(CMD_LIST, opts);
		return cli.run();
	}
	
	public SSHCommandResult delete(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		cli = new KatelloCli(CMD_DELETE, opts);
		return cli.run();
	}
	
	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **
	
}
