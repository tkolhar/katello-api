package com.redhat.qe.katello.base;

import java.util.ArrayList;

import javax.management.Attribute;

import com.redhat.qe.tools.SSHCommandResult;
import com.redhat.qe.tools.SSHCommandRunner;

public class KatelloApi{
	static{new com.redhat.qe.auto.testng.TestScript();}// to make properties be initialized (if they don't still)
	
	private ArrayList<Attribute> options;
	public KatelloApi(ArrayList<Attribute> options){
		this.options = options;
	}
	
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
	
	public SSHCommandResult post(String call){
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
			String content = ""; // let's constrcut the content. will be need to remote the last "," sign...
			if(content != null)
				for(Attribute option: options){
					if(option.getValue() != null)
						content = String.format("%s'%s':'%s',", content,option.getName(),option.getValue());
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
