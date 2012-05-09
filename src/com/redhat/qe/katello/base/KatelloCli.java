package com.redhat.qe.katello.base;

import java.util.ArrayList;
import javax.management.Attribute;
import com.redhat.qe.tools.SSHCommandResult;
import com.redhat.qe.tools.SSHCommandRunner;

public class KatelloCli{
	static{new com.redhat.qe.auto.testng.TestScript();}// to make properties be initialized (if they don't still)
	
	private String command;
	private ArrayList<Attribute> args;
	private ArrayList<Attribute> opts;
	
	public KatelloCli(String command,ArrayList<Attribute> args,ArrayList<Attribute> options){
		this.command = command;
		this.args = args;
		this.opts = options;
		if(this.args==null) this.args = new ArrayList<Attribute>();
		if(this.opts==null) this.opts = new ArrayList<Attribute>();
	}
	
	public KatelloCli(String command,ArrayList<Attribute> options){
		this.command = command;
		this.args = new ArrayList<Attribute>();
		this.args.add(new Attribute("username", "admin"));
		this.args.add(new Attribute("password", "admin"));
		this.opts = options;
		if(this.opts==null) this.opts = new ArrayList<Attribute>();
	}
		
	public SSHCommandResult run(){
		String cmd = System.getProperty("katello.engine", "katello");
		for(int i=0;i<this.args.size();i++){
			cmd = cmd + " --" + args.get(i).getName()+" \""+args.get(i).getValue().toString()+"\"";
		}
		cmd = cmd + " " + this.command;
		for(int i=0;i<this.opts.size();i++){
			if(this.opts.get(i).getValue()!=null)
				cmd = cmd + " --" + opts.get(i).getName()+" \""+opts.get(i).getValue().toString()+"\"";
		}
		
		try {
			SSHCommandRunner client_sshRunner = null;
			client_sshRunner = new SSHCommandRunner(
					System.getProperty("katello.client.hostname", "localhost"), 
					"root","no_passphrase_here", 
					System.getProperty("katello.client.sshkey.private", ".ssh/id_hudson_dsa"), 
					"no_passphrase_here", null);
			return client_sshRunner.runCommandAndWait(cmd);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
