package com.redhat.qe.katello.common;

import java.util.logging.Logger;
import com.redhat.qe.tools.SSHCommandResult;
import com.redhat.qe.tools.SSHCommandRunner;

/**
 * Utility for common (independent from api/cli) and static calls only.<BR>
 * Providing javadoc is appreciated.
 */
public class KatelloUtils {
	private static Logger log = Logger.getLogger(KatelloUtils.class.getName());
	
	/**
	 * Executes ssh command on client-side.<br>
	 * Credentials (passphrase) and other settings are all taken from System.properties.
	 * @param _cmd Command string to execute. Multiple commands could be provided with ";".
	 * @return SSHCommandResult object
	 * @author Garik Khachikyan <gkhachik@redhat.com>
	 */
	public static SSHCommandResult sshOnClient(String _cmd){
		try{
			SSHCommandRunner ssh_client = new SSHCommandRunner(
					System.getProperty("katello.client.hostname", "localhost"), "root", 
					System.getProperty("katello.client.ssh.passphrase", "secret"), 
					System.getProperty("katello.client.sshkey.private", ".ssh/id_dsa"), 
					System.getProperty("katello.client.sshkey.passphrase", "secret"), null);
			return ssh_client.runCommandAndWait(_cmd);
		}catch(Throwable t){
			log.warning("Warning: Could not initialize client's SSHCommandRunner.");
			t.printStackTrace();
		}return null;
	}
	
	/**
	 * Executes ssh command on server-side.<br>
	 * Credentials (passphrase) and other settings are all taken from System.properties.
	 * @param _cmd Command string to execute. Multiple commands could be provided with ";".
	 * @return SSHCommandResult object
	 * @author Garik Khachikyan <gkhachik@redhat.com>
	 */
	public static SSHCommandResult sshOnServer(String _cmd){
		try{
			SSHCommandRunner ssh_server = new SSHCommandRunner(
					System.getProperty("katello.server.hostname", "localhost"), "root", 
					System.getProperty("katello.server.ssh.passphrase", "secret"), 
					System.getProperty("katello.server.sshkey.private", ".ssh/id_dsa"), 
					System.getProperty("katello.server.sshkey.passphrase", "secret"), null);
			return ssh_server.runCommandAndWait(_cmd);
		}catch(Throwable t){
			log.warning("Warning: Could not initialize server's SSHCommandRunner.");
			t.printStackTrace();
		}return null;
	}
	
	public static SSHCommandResult stopKatello(){
		String _cmd = 
				"service katello-jobs stop; " +
				"service katello stop; " +
				"service pulp-server stop; " +
				"service tomcat6 stop; " +
				"service elasticsearch stop;";
		return sshOnServer(_cmd);
	}
	
	public static SSHCommandResult startKatello(){
		String _cmd = 
				"service elasticsearch start; " +
				"service tomcat6 start; " +
				"service pulp-server start; " +
				"service katello start; " +
				"service katello-jobs start;";
		return sshOnServer(_cmd);
	}
}
