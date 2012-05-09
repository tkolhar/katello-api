package com.redhat.qe.katello.common;

import java.util.logging.Logger;

/**
 * Stores the general information about the Katello like: username/password,
 * server/password and (later) more settings. Intended to have just one instance 
 * loaded/returned.
 * @author gkhachik
 * @since 14.Feb.2011
 * 
 */
public class KatelloInfo{
	protected static Logger log = Logger.getLogger(KatelloInfo.class.getName());
	
	private String usernameUI = null;
	private String passwordUI = null;
	private String usernameSSH = null;
	private String servername = null;
	private String sshKeyPrivate = null;
	
	private static KatelloInfo katelloInst = null; 
	
	private KatelloInfo(){
		this.servername = System.getProperty("katello.server.hostname","localhost"); // default: localhost
		this.usernameUI = System.getProperty("katello.ui.user","admin"); // default: admin
		this.passwordUI = System.getProperty("katello.ui.passphrase","admin"); // default: admin
		this.usernameSSH = System.getProperty("katello.ssh.user","root"); // default: root
		this.sshKeyPrivate = System.getProperty("katello.sshkey.private","~/.ssh/id_hudson_dsa.pub");
		this.logSettings(); // sysout|log the settings.
	}
	
	public static KatelloInfo getInstance(){
		if(katelloInst ==null) katelloInst = new KatelloInfo();
		return katelloInst;
	}
	
	public String getUsernameUI(){
		return this.usernameUI;
	}
	public String getPasswordUI(){
		return this.passwordUI;
	}
	public String getServername(){
		return this.servername;
	}
	public String getUsernameSSH(){
		return this.usernameSSH;
	}
	public String getSshKeyPrivate(){
		return this.sshKeyPrivate;
	}

	private void logSettings(){
		String prop, value;
		// to out the log.fine - one level up from the TestScript loader which
		// does on level: finer. We need to see the properties values :)
		prop = "katello.server.hostname"; value = this.servername;
		log.fine(String.format("Katello settings: [%s] = [%s]", prop, value));
		prop = "katello.ui.user"; value = this.usernameUI;
		log.fine(String.format("Katello settings: [%s] = [%s]", prop, value));
		prop = "katello.ui.passphrase"; value = KatelloConstants.SYSOUT_PWD_MASK;
		log.fine(String.format("Katello settings: [%s] = [%s]", prop, value));
		prop = "katello.ssh.user"; value = this.usernameSSH;
		log.fine(String.format("Katello settings: [%s] = [%s]", prop, value));
		prop = "katello.ssh.passphrase"; value = KatelloConstants.SYSOUT_PWD_MASK;
		log.fine(String.format("Katello settings: [%s] = [%s]", prop, value));
		prop = "katello.sshkey.private"; value = this.sshKeyPrivate;
		log.fine(String.format("Katello settings: [%s] = [%s]", prop, value));
		prop = "katello.sshkey.passphrase"; value = KatelloConstants.SYSOUT_PWD_MASK;
		log.fine(String.format("Katello settings: [%s] = [%s]", prop, value));
	}
}
