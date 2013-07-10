package com.redhat.qe.katello.base.obj;

import java.util.ArrayList;
import javax.management.Attribute;

import com.redhat.qe.katello.base.threading.KatelloCliWorker;
import com.redhat.qe.tools.SSHCommandResult;
public class KatelloClient extends _KatelloObject{
	public static final String CMD_SAVED_OPTIONS = "client saved_options";
	public static final String CMD_FORGET = "client forget";
	public static final String CMD_REMEMBER = "client remember";
	public static final String OUT_REMEMBER = "Successfully remembered option [ %s ]";
	public static final String OUT_FORGET = "Successfully forgot option [ %s ]";
	private ArrayList<Attribute> opts;
	private String option;
	private String value;
	
	public KatelloClient(KatelloCliWorker kcr, String optionName,String valueName){
		
		this.option = optionName;
		this.value = valueName;
		this.opts = new ArrayList<Attribute>();
		this.kcr = kcr;
	}
	
	public KatelloClient(){
		this.opts = new ArrayList<Attribute>();
	}
	
	public SSHCommandResult cli_saved_options(){
		opts.clear();
		return run(CMD_SAVED_OPTIONS);
	}
	
	public SSHCommandResult cli_remember(){
		opts.clear();
		opts.add(new Attribute("option",option));
		opts.add(new Attribute("value",value));
		return run(CMD_REMEMBER);
	}
	
	public SSHCommandResult cli_forget(){
		opts.clear();
		opts.add(new Attribute("option",option));
		return run(CMD_FORGET);
	}
}