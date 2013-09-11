package com.redhat.qe.katello.base;

import java.util.logging.Logger;
import com.redhat.qe.katello.common.KatelloConstants;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloDisconnectedCli implements KatelloConstants {

	static{new com.redhat.qe.auto.testng.TestScript();}// to make properties be initialized (if they don't still)
	
	public static Logger log = Logger.getLogger(KatelloDisconnectedCli.class.getName());
	
	private String command;
	private String hostName = System.getProperty("katello.client.hostname", "localhost");
	
	public KatelloDisconnectedCli(String command){
		this.command = command;
	}
	
	public SSHCommandResult run(){
		return runExt("");
	}

	public SSHCommandResult runExt(String cmdTail){
		String cmd = "katello-disconnected "+command;		
		try {
			return KatelloUtils.sshOnClient(hostName, cmd+cmdTail);
		}
		catch (Exception e) {
			log.warning(String.format("ERROR running the command: [%s]",cmd));
			log.warning("ERROR: "+e.getMessage());
			e.printStackTrace();
		}return null;
	}

	public void runNowait(){
		String cmd = "katello-disconnected "+command;
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
