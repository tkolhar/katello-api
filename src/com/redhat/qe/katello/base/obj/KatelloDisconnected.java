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
}
