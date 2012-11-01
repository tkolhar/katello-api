package com.redhat.qe.katello.common;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.redhat.qe.tools.ExecCommands;
import com.redhat.qe.tools.SSHCommandResult;
import com.redhat.qe.tools.SSHCommandRunner;

/**
 * Utility for common (independent from api/cli) and static calls only.<BR>
 * Providing javadoc is appreciated.
 */
public class KatelloUtils {
	private static Logger log = Logger.getLogger(KatelloUtils.class.getName());
	private static Map<String, SSHCommandRunner> _sshClients = new HashMap<String, SSHCommandRunner>();
	private static SSHCommandRunner _sshServer;
	
    public static String run_local(String command){
    	return run_local(false, command);
    }
    
    public static String run_local(boolean showLogResults, String command){
        String out = null; String tmp_cmdFile = "/tmp/katello-"+getUniqueID()+".sh";
        ExecCommands localRunner = new ExecCommands();
        try{
            // cleanup the running buffer file - in case it would exist
            localRunner.submitCommandToLocalWithReturn(false, 
                    "rm -f "+tmp_cmdFile,"");
            FileOutputStream fout = 
                new FileOutputStream(tmp_cmdFile);
            fout.write((command+"\n").getBytes());fout.flush();fout.close();
            log.finest(String.format("Executing local: [%s]",command));
            out = localRunner.submitCommandToLocalWithReturn(
                    false, "/bin/bash "+tmp_cmdFile, ""); // HERE is the run
            
            if(showLogResults){ // log output if specified so.
                // split the lines and out each line.
                String[] split = out.split("\\n");
                for(int i=0;i<split.length;i++){
                    log.info("Output: "+split[i]);
                }
            }
        }catch(IOException iex){
            log.log(Level.SEVERE, iex.getMessage(), iex);
        }finally{
            // cleanup the running buffer file.
            try{localRunner.submitCommandToLocalWithReturn(false, 
                    "rm -f "+tmp_cmdFile,"");
            }catch(IOException ie){log.log(Level.SEVERE, ie.getMessage(), ie);}
        }
        return out;
    }
    
    /**
     * Generates the unique string which is the current (timeInMillis / 1000).
     * @return unique ID string.
     * @author gkhachik
     * @since 15.Feb.2011
     */
    public static String getUniqueID(){
        try{Thread.sleep(1000+Math.abs(new Random().nextInt(200)));}catch(InterruptedException iex){};
        String uid = String.valueOf(
                Calendar.getInstance().getTimeInMillis() / 1000); 
        log.fine(String.format("Generating unique ID: [%s]",uid));
        return uid;
    }
    
    public static String getUUID(){
        return UUID.randomUUID().toString();
    }
    
    /* (non-Javadoc)
     * @see com.redhat.qe.katello.tasks.IKatelloTasks#getDiskFreeForPulpRepos()
     */
    public static long getDiskFreeForPulpRepos(){
        long dfPulpRepos=Long.MAX_VALUE;
        String res = KatelloUtils.sshOnServer("df `grep \"Alias /pulp/repos\" /etc/httpd/conf.d/pulp.conf | awk '{print $3}'` | tail -1 | awk '{print $3}'").getStdout().trim();
        log.fine("Free disk space for Pulp repositories: ["+res+"]");
        dfPulpRepos = new Long(res).longValue();
        return dfPulpRepos;
    }
    
	/**
	 * Executes ssh command on client-side.<br>
	 * Credentials (passphrase) and other settings are all taken from System.properties.
	 * @param _cmd Command string to execute. Multiple commands could be provided with ";".
	 * @return SSHCommandResult object
	 * @author Garik Khachikyan <gkhachik@redhat.com>
	 */
	public static SSHCommandResult sshOnClient(String _cmd){
		return getSSHClient().runCommandAndWait(_cmd);
	}

	/**
	 * Executes ssh command on client-side.<br>
	 * Credentials (passphrase) and other settings are all taken from System.properties.
	 * @param _hostName the client on which command will be executed
	 * @param _cmd Command string to execute. Multiple commands could be provided with ";".
	 * @return SSHCommandResult object
	 * @author Garik Khachikyan <gkhachik@redhat.com>
	 */
	public static SSHCommandResult sshOnClient(String _hostname, String _cmd){
		return getSSHClient(_hostname).runCommandAndWait(_cmd);
	}
	
	/**
	 * Executes ssh command(s) on client side and returns without waiting its result.<br>
	 * Useful for some async commands like: provider synchronize (with option to cancel it later).
	 * @param _hostName
	 * @param _cmd
	 * @author Garik Khachikyan <gkhachik@redhat.com>
	 */
	public static void sshOnClientNoWait(String _hostName, String _cmd){
		getSSHClient(_hostName).runCommand(_cmd);
	}

	/**
	 * Executes ssh command on server-side.<br>
	 * Credentials (passphrase) and other settings are all taken from System.properties.
	 * @param _cmd Command string to execute. Multiple commands could be provided with ";".
	 * @return SSHCommandResult object
	 * @author Garik Khachikyan <gkhachik@redhat.com>
	 */
	public static SSHCommandResult sshOnServer(String _cmd){
		return getSSHServer().runCommandAndWait(_cmd);
	}
	
	public static SSHCommandResult stopKatello(){
		String _cmd = 
				"service mongod stop; " +
				"service katello-jobs stop; " +
				"service katello stop; " +
				"service pulp-server stop; " +
				"service tomcat6 stop; " +
				"service elasticsearch stop;";
		return sshOnServer(_cmd);
	}
	
	public static SSHCommandResult startKatello(){
		String _cmd = 
				"katello-service start";
		return sshOnServer(_cmd);
	}

	protected static SSHCommandRunner getSSHClient(){
		return getSSHClient(null);
	}

	protected static SSHCommandRunner getSSHClient(String _host){
		String hostname = (_host == null ? System.getProperty("katello.client.hostname", "localhost") : _host);
		if (_sshClients.get(hostname) == null){
			try{
				SSHCommandRunner sshClient = new SSHCommandRunner(
						hostname, "root", 
						System.getProperty("katello.client.ssh.passphrase", "secret"), 
						System.getProperty("katello.client.sshkey.private", ".ssh/id_dsa"), 
						System.getProperty("katello.client.sshkey.passphrase", "secret"), null);
				_sshClients.put(hostname, sshClient);
			}catch(Throwable t){
				log.warning("Warning: Could not initialize client's SSHCommandRunner.");
				log.warning("Warning: "+t.getMessage());
				t.printStackTrace();
			}
		}
		return _sshClients.get(hostname);
	}
	
	protected static SSHCommandRunner getSSHServer(){
		if (_sshServer == null){
			try{
				_sshServer = new SSHCommandRunner(
						System.getProperty("katello.server.hostname", "localhost"), "root", 
						System.getProperty("katello.server.ssh.passphrase", "secret"), 
						System.getProperty("katello.server.sshkey.private", ".ssh/id_dsa"), 
						System.getProperty("katello.server.sshkey.passphrase", "secret"), null);
			}catch(Throwable t){
				log.warning("Warning: Could not initialize server's SSHCommandRunner.");
				log.warning("Warning: "+t.getMessage());
				t.printStackTrace();
			}
		}
		return _sshServer;
	}

}
