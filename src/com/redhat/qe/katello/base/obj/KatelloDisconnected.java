package com.redhat.qe.katello.base.obj;

import java.util.logging.Logger;
import com.redhat.qe.katello.base.KatelloDisconnectedCli;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloDisconnected extends _KatelloObject{
	static{new com.redhat.qe.auto.testng.TestScript();}
	
	protected static Logger log = Logger.getLogger(KatelloDisconnected.class.getName());

	// ** ** ** ** ** ** ** Public constants	
	public static final String CMD_SETUP_MINIMAL = "setup --oauth-secret %s";
	
	private String cmd = "_none_";
	public KatelloDisconnected(){super();}

	public SSHCommandResult setup(String oauth_secret){
		cmd = "setup";
		if(oauth_secret!=null)
			cmd += " --oauth-secret "+oauth_secret;
		return new KatelloDisconnectedCli(cmd).run();
	}

	public SSHCommandResult importManifest(String manifest_zip){ // import is java reserved keyword, what a pity :D
		cmd = "import";
		if(manifest_zip!=null)
			cmd += " -m "+manifest_zip;
		return new KatelloDisconnectedCli(cmd).run();
	}
	
	public SSHCommandResult list(Boolean disabled){
		cmd = "list";
		if(disabled.booleanValue())
			cmd += " --disabled";
		return new KatelloDisconnectedCli(cmd).run();
	}

	public SSHCommandResult custom_listRepo(Boolean disabled, String reponame){
		cmd = "list";
		if(disabled.booleanValue())
			cmd += " --disabled";
		cmd +="|grep \""+reponame+"\"";
		return new KatelloDisconnectedCli(cmd).run();
	}

	public SSHCommandResult custom_listCount(Boolean disabled){
		cmd = "list";
		if(disabled.booleanValue())
			cmd += " --disabled";
		cmd +="|wc -l";
		return new KatelloDisconnectedCli(cmd).run();
	}
	
	public SSHCommandResult disable(String reponame, Boolean all){
		cmd = "disable";
		if(reponame!=null)
			cmd += " -r "+reponame;
		if(all.booleanValue())
			cmd += " --all";
		return new KatelloDisconnectedCli(cmd).run();
	}

	public SSHCommandResult configure(){
		cmd = "configure";
		return new KatelloDisconnectedCli(cmd).run();
	}

	public SSHCommandResult enable(String reponame, Boolean all){
		cmd = "enable";
		if(reponame!=null)
			cmd += " -r "+reponame;
		if(all.booleanValue())
			cmd += " --all";
		return new KatelloDisconnectedCli(cmd).run();
	}
	
	public SSHCommandResult sync(String reponame){
		cmd = "sync";
		if(reponame!=null)
			cmd += " -r "+reponame;
		return new KatelloDisconnectedCli(cmd).run();		
	}
	
	public SSHCommandResult watch(){
		cmd = "watch";
		return new KatelloDisconnectedCli(cmd).run();		
	}
	
	public SSHCommandResult export(String reponame, String filename){
		cmd = "export";
		if(reponame!=null)
			cmd += " -r "+reponame;
		if(filename!=null)
			cmd += " -t "+filename;
		return new KatelloDisconnectedCli(cmd).run();		
	}
	
	public SSHCommandResult refresh(String caFile){
		cmd = "refresh";
		if(caFile != null)
			cmd += " --cdnca " + caFile;
		return new KatelloDisconnectedCli(cmd).run();
	}
	
	public SSHCommandResult info(){
		cmd = "info";
		return new KatelloDisconnectedCli(cmd).run();
	}
	
	public SSHCommandResult clean(){
		cmd = "clean";
		return new KatelloDisconnectedCli(cmd).run();
	}
}
