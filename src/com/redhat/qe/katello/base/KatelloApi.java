package com.redhat.qe.katello.base;

import com.redhat.qe.tools.SSHCommandResult;
import com.redhat.qe.tools.SSHCommandRunner;

public class KatelloApi{
	static{new com.redhat.qe.auto.testng.TestScript();}// to make properties be initialized (if they don't still)
	
	public static final String _POST = "curl -sk -u%s:%s " +
			"-H \"Accept: application/json\" " +
			"-H \"content-type: application/json\" -d \"%s\" " +
			"-X POST https://%s/%s/api%s";
	
	public SSHCommandResult get(String call){
		try{
			SSHCommandRunner client_sshRunner = new SSHCommandRunner(
					System.getProperty("katello.client.hostname", "localhost"), 
					"root","no_passphrase_here", 
					System.getProperty("katello.client.sshkey.private", ".ssh/id_hudson_dsa"), 
					"no_passphrase_here", null);
			String cmd = "curl -sk -u %s:%s https://%s/%s/api%s";
			cmd = String.format(cmd, 
					System.getProperty("katello.admin.user", "admin"),
					System.getProperty("katello.admin.password", "admin"),
					System.getProperty("katello.server.hostname", "localhost"),
					System.getProperty("katello.product", "katello"),
					call);
			return client_sshRunner.runCommandAndWait(cmd);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return null;
	}
	
	public SSHCommandResult post(KatelloPostParam[] params, String call){
		try{
			SSHCommandRunner client_sshRunner = new SSHCommandRunner(
					System.getProperty("katello.client.hostname", "localhost"), 
					"root","no_passphrase_here", 
					System.getProperty("katello.client.sshkey.private", ".ssh/id_hudson_dsa"), 
					"no_passphrase_here", null);
			String cmd = "curl -sk -u%s:%s " +
					"-H \"Accept: application/json\" " +
					"-H \"content-type: application/json\" -d \"%s\" " +
					"-X POST https://%s/%s/api%s";
			
			String content = "";
			for(KatelloPostParam param: params){
				content = String.format("%s%s,", content, param);
			}
			if(content.length()>1) 
				content = content.substring(0,content.length()-1); // cut last symbol - ","
			content = String.format("{%s}", content);
			
			cmd = String.format(cmd, 
					System.getProperty("katello.admin.user", "admin"),
					System.getProperty("katello.admin.password", "admin"),
					content,
					System.getProperty("katello.server.hostname", "localhost"),
					System.getProperty("katello.product", "katello"),
					call);
			return client_sshRunner.runCommandAndWait(cmd);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return null;
	}

	
}
