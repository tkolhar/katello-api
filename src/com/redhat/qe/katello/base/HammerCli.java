package com.redhat.qe.katello.base;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.management.Attribute;

import com.redhat.qe.katello.base.obj.HammerUser;
import com.redhat.qe.katello.common.KatelloConstants;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

public class HammerCli implements KatelloConstants {

	static{new com.redhat.qe.auto.testng.TestScript();}// to make properties be initialized (if they don't still)
	
	public static Logger log = Logger.getLogger(HammerCli.class.getName());
	public static final String OUT_EMPTY_LIST = "[  ]";
	public static String CMD_PY_COVERAGE = ""; // python coverage for CLI commands?
	
	private String command;
	private List<Attribute> args;
	private List<Attribute> opts;
	private String hostName = System.getProperty("katello.client.hostname", "localhost");
	
	public HammerCli(String command,List<Attribute> args,List<Attribute> options){
		this.command = command;
		this.args = args;
		this.opts = options;
		if(this.args==null) this.args = new ArrayList<Attribute>();
		if(this.opts==null) this.opts = new ArrayList<Attribute>();
	}
	
	public HammerCli(String command,List<Attribute> options){
		this.command = command;
		this.args = new ArrayList<Attribute>();
		// @ TODO disable for now as hammer uses default user credentials from internal config file 
//		this.args.add(new Attribute("username", System.getProperty("hammer.admin.user", HammerUser.DEFAULT_ADMIN_USER)));
//		this.args.add(new Attribute("password", System.getProperty("hammer.admin.password", HammerUser.DEFAULT_ADMIN_PASS)));
		this.opts = options;
		if(this.opts==null) this.opts = new ArrayList<Attribute>();
	}
	
	public HammerCli(String command,ArrayList<Attribute> options, HammerUser user, String hostName){
		this.command = command;
		this.args = new ArrayList<Attribute>();
		
		if (user != null) {
			if(user.getUsername()!=null)
				this.args.add(new Attribute("username", user.username));
			if(user.getPassword()!=null)
				this.args.add(new Attribute("password", user.password));
		} else {	
			// @ TODO disable for now as hammer uses default user credentials from internal config file
//			this.args.add(new Attribute("username", System.getProperty("hammer.admin.user", HammerUser.DEFAULT_ADMIN_USER)));
//			this.args.add(new Attribute("password", System.getProperty("hammer.admin.password", HammerUser.DEFAULT_ADMIN_PASS)));
		}
		this.hostName = hostName;
		this.opts = options;
		if(this.opts==null) this.opts = new ArrayList<Attribute>();
	}
	
	public SSHCommandResult run(){
		return runExt("");
	}

	public SSHCommandResult runExt(String cmdTail){
		String cmd = "/usr/bin/hammer --output base"; //the output we all know from katello cli ;)
		String locale = System.getProperty("katello.locale", KATELLO_DEFAULT_LOCALE);
		for(int i=0;i<this.args.size();i++){
			cmd = cmd + " --" + args.get(i).getName()+" \""+args.get(i).getValue().toString()+"\"";
		}
		
		cmd = "export LANG=" + locale + "; " + CMD_PY_COVERAGE + " " + cmd + " " + this.command;
		for(int i=0;i<this.opts.size();i++){
			if(this.opts.get(i).getValue()!=null)
				cmd = cmd + " --" + opts.get(i).getName()+" \""+opts.get(i).getValue().toString()+"\"";
		}
		
		try {
			return KatelloUtils.sshOnClient(hostName, cmd+cmdTail);
		}
		catch (Exception e) {
			e.printStackTrace();
		}return null;
	}

	public void runNowait(){
		String cmd = "hammer";
		String locale = System.getProperty("katello.locale", KATELLO_DEFAULT_LOCALE);
		for(int i=0;i<this.args.size();i++){
			cmd = cmd + " --" + args.get(i).getName()+" \""+args.get(i).getValue().toString()+"\"";
		}
		cmd = "export LANG=" + locale + " && " + cmd + " " + this.command;
		for(int i=0;i<this.opts.size();i++){
			if(this.opts.get(i).getValue()!=null)
				cmd = cmd + " --" + opts.get(i).getName()+" \""+opts.get(i).getValue().toString()+"\"";
		}
		
		try {
			KatelloUtils.sshOnClientNoWait(hostName, cmd);
		}
		catch (Exception e) {
			log.warning(String.format("ERROR running the command (nowait): [%s]",cmd));
			log.warning("ERROR: "+e.getMessage());
			e.printStackTrace();
		}
	}

}
