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

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.obj.DeltaCloudInstance;
import com.redhat.qe.katello.base.obj.KatelloPing;
import com.redhat.qe.katello.deltacloud.DeltaCloudAPI;
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
	 * @param _cmd
	 * @author Garik Khachikyan <gkhachik@redhat.com>
	 */
	public static void sshOnClientNoWait(String _cmd){
		getSSHClient().runCommand(_cmd);
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
	
	/**
	 * Useful for starting services of: katello|cfse
	 * @return res Object
	 */
	public static SSHCommandResult stopKatello(){
		SSHCommandResult res = sshOnServer("which katello-service");
		String _cmd;
		if (res.getExitCode() != 0) {
			_cmd = 
				"service mongod stop; " +
				"service katello-jobs stop; " +
				"service katello stop; " +
				"service pulp-server stop; " +
				"service tomcat6 stop; " +
				"service elasticsearch stop;";
		} else {
			_cmd = "katello-service stop";
		}		 
		return sshOnServer(_cmd);
	}
	
	/**
	 * Useful for starting services of: headpin|sam
	 * @return res Object
	 */
	public static SSHCommandResult stopHeadpin(){
		SSHCommandResult res = sshOnServer("which katello-service");
		String _cmd;
		if (res.getExitCode() != 0) {
			_cmd =
				"service katello-jobs stop; " +
				"service katello stop; " +
				"service thumbslug stop; " +
				"service httpd stop; " + 
				"service tomcat6 stop; " +
				"service elasticsearch stop;";
		}else{
			_cmd = "katello-service stop";
		}
		return sshOnServer(_cmd);
	}

	/**
	 * Useful for starting services of: katello|cfse
	 * @return res Object
	 */
	public static SSHCommandResult startKatello(){
		SSHCommandResult res = sshOnServer("which katello-service");
		String _cmd;
		if (res.getExitCode() != 0) {
			_cmd = 
				"service elasticsearch start; " +
				"service tomcat6 start; " +
				"service pulp-server start; " +
				"service katello start; " +
				"service katello-jobs start;";
		} else {
			_cmd = "katello-service start";
		}
		return sshOnServer(_cmd);
	}

	/**
	 * Useful for starting services of: headpin|sam
	 * @return res Object
	 */
	public static SSHCommandResult startHeadpin(){
		SSHCommandResult res = sshOnServer("which katello-service");
		String _cmd;
		if (res.getExitCode() != 0) {
			_cmd =
				"service elasticsearch start; " +
				"service tomcat6 start; " +
				"service httpd start; " +
				"service thumbslug start; " +
				"service katello start; " +
				"service katello-jobs start;";
		} else{
			_cmd = "katello-service start";
		}
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
	
	/**
	 * Create new server in DeltaCloud, start it, and install CFSE server on it.
	 * @param number the number of server, which will be in range of 1..KatelloConstants.DELTACLOUD_SERVERS.length
	 * @return DeltaCloud server instance.
	 */
	public static DeltaCloudInstance getDeltaCloudServer(int number) {
		return getDeltaCloudServer(number, false);
	}
	
	/**
	 * Create new server in DeltaCloud but do not start it.
	 * @param number the number of server, which will be in range of 1..KatelloConstants.DELTACLOUD_SERVERS.length
	 * @return DeltaCloud server instance.
	 */
	public static DeltaCloudInstance getDeltaCloudServerNoWait(int number) {
		return getDeltaCloudServer(number, true);
	}
	
	/**
	 * Private method for creating DeltaCloud server machine, configuring it.
	 */
	private static DeltaCloudInstance getDeltaCloudServer(int number, boolean nowait) {
		
		if (number > KatelloConstants.DELTACLOUD_SERVERS.length) return null;
		String[] configs = KatelloConstants.DELTACLOUD_SERVERS[number-1];
		
		DeltaCloudInstance server = DeltaCloudAPI.provideServer(nowait, configs[0]);
		
		Assert.assertNotNull(server.getClient());
		
		System.setProperty("katello.server.hostname", server.getIpAddress());
		
		if (!nowait) {
			configureDDNS(server, configs);
			installServer(server);
		} else {
			server.setConfigs(configs);
		}
		
		return server;
	}

	/**
	 * Create DeltaCloud client machine, install CFSE client on it and configure it to server.
	 * @param server the server hostname to configure with.
	 * @param number the number of server, which will be in range of 1..KatelloConstants.DELTACLOUD_CLIENTS.length
	 * @return DeltaCloud client machine instance.
	 */
	public static DeltaCloudInstance getDeltaCloudClient(String server, int number) {
		
		if (number > KatelloConstants.DELTACLOUD_CLIENTS.length) return null;
		String[] configs = KatelloConstants.DELTACLOUD_CLIENTS[number-1];

		DeltaCloudInstance client = DeltaCloudAPI.provideClient(false, configs[0]);

		Assert.assertNotNull(client.getClient());
		
		configureDDNS(client, configs);
		
		_sshClients.put(client.getHostName(), _sshClients.get(client.getIpAddress()));
		
		installClient(client, server);
		
		return client;
	}
	
	/**
	 * Destroys the machine from DeltaCloud.
	 * @IMPORTANT EACH PROVIDED DELTACLOUD MACHINE SHOULD BE DESTROYED AFTER TEST
	 * @param machine DeltaCloudInstance server to destroy.
	 */
	public static void destroyDeltaCloudMachine(DeltaCloudInstance machine) {
		DeltaCloudAPI.destroyMachine(machine);
		_sshClients.remove(machine.getHostName());
		_sshClients.remove(machine.getIpAddress());
	}
	
	/**
	 * Starts, installs CFSE server on provided machine.
	 * @param machine DeltaCloudInstance server to start.
	 */
	public static void  startDeltaCloudServer(DeltaCloudInstance machine) {
		if (!machine.getInstance().isRunning()) {
			DeltaCloudAPI.startMachine(machine);
			configureDDNS(machine, machine.getConfigs());
			installServer(machine);
		}
	}
	
	/**
	 * Private method to configure and set hostname on machine.
	 * @param machine DeltaCloudInstance server to configure
	 * @param configs configuration array which should be from KatelloConstants.DELTACLOUD_SERVERS or KatelloConstants.DELTACLOUD_CLIENTS
	 */
	private static void configureDDNS(DeltaCloudInstance machine, String[] configs) {
		sshOnClient(machine.getIpAddress(), "rm -f /etc/yum.repos.d/rhevm.repo");
		
		sshOnClient(machine.getIpAddress(), "wget http://hdn.corp.redhat.com/rhel6-csb/RPMS/noarch/redhat-ddns-client-1.3-4.noarch.rpm");
		
		sshOnClient(machine.getIpAddress(), "yum -y localinstall redhat-ddns-client-1.3-4.noarch.rpm --nogpgcheck --disablerepo=*");
		
		sshOnClient(machine.getIpAddress(), "echo \"" + configs[0] + " " + configs[1] + " " + configs[2] + "\" >> /etc/redhat-ddns/hosts");
		
		sshOnClient(machine.getIpAddress(), "redhat-ddns-client enable");
		sshOnClient(machine.getIpAddress(), "redhat-ddns-client update");
		sshOnClient(machine.getIpAddress(), "redhat-ddns-client update");
		
		sshOnClient(machine.getIpAddress(), "hostname " + configs[0] + "." + configs[1]);
		sshOnClient(machine.getIpAddress(), "echo \"" + configs[0] + "." + configs[1] + "\" >> /etc/sysconfig/network");
		sshOnClientNoWait(machine.getIpAddress(), "service network restart");
		
		try { Thread.sleep(5000); } catch (Exception e) {}
		
		sshOnClient(machine.getIpAddress(), "sed -i 's/server 10.16.47.254 /server 10.16.71.254/g' /etc/ntp.conf");
		sshOnClient(machine.getIpAddress(), "service ntpd restart");
		
		machine.setHostName(configs[0] + "." + configs[1]);
	}
	
	/**
	 * Install CFSE server on provided machine.
	 * @param machine DeltaCloudInstance server.
	 */
	private static void installServer(DeltaCloudInstance machine) {
		KatelloUtils.sshOnServer("touch /etc/yum.repos.d/beaker-tasks.repo");
		KatelloUtils.sshOnServer("touch /etc/yum.repos.d/beaker.repo");
		
		String version = System.getProperty("katello.product.version", "1.1");
		String product = System.getProperty("katello.product", "katello");
		
		String yumrepo = 
				"[beaker-tasks]\\\\n" +
				"name=bkr-tasks\\\\n" +
				"baseurl=http://beaker-02.app.eng.bos.redhat.com/rpms/\\\\n"+
				"metadata_expire=3m\\\\n"+
				"enabled=1\\\\n"+
				"gpgcheck=0";
		KatelloUtils.sshOnServer("echo -en \""+yumrepo+"\" > /etc/yum.repos.d/beaker-tasks.repo");
		
		yumrepo = 
				"[beaker]\\\\n" +
				"name=Beaker\\\\n" +
				"baseurl=http://beaker.engineering.redhat.com/harness/RedHatEnterpriseLinux6/\\\\n"+
				"enabled=1\\\\n"+
				"skip_if_unavailable=1\\\\n"+
				"gpgcheck=0";
		KatelloUtils.sshOnServer("echo -en \""+yumrepo+"\" > /etc/yum.repos.d/beaker.repo");
		
		KatelloUtils.sshOnServer("yum -y install beakerlib beakerlib-redhat rhts-python rhts-test-env --disablerepo=* --enablerepo=beaker*");
		KatelloUtils.sshOnServer("mkdir ~/.beaker_client");
		KatelloUtils.sshOnServer("touch ~/.beaker_client/config");
		KatelloUtils.sshOnServer("echo \"HUB_URL = \"https://beaker.engineering.redhat.com\"\" >> ~/.beaker_client/config");
		KatelloUtils.sshOnServer("echo \"AUTH_METHOD = \"password\"\" >> ~/.beaker_client/config");
		KatelloUtils.sshOnServer("chmod a+x ~/.beaker_client/config");
		
		KatelloUtils.sshOnServer("yum install -y Katello-Katello-Sanity-ImportKeys --disablerepo=* --enablerepo=beaker*");
		KatelloUtils.sshOnServer("cd /mnt/tests/Katello/Sanity/ImportKeys/; make run");
		
		KatelloUtils.sshOnServer("yum install -y Katello-Katello-Installation-RegisterRHNClassic --disablerepo=* --enablerepo=beaker*");
		KatelloUtils.sshOnServer("cd /mnt/tests/Katello/Installation/RegisterRHNClassic/; make run");
		
		if (product.equals("katello")) {
			KatelloUtils.sshOnServer("yum install -y Katello-Katello-Installation-ConfigureRepos --disablerepo=* --enablerepo=beaker*");
			KatelloUtils.sshOnServer("cd /mnt/tests/Katello/Installation/ConfigureRepos/; make run");	

			KatelloUtils.sshOnServer("yum install -y Katello-Katello-Installation-KatelloNightly --disablerepo=* --enablerepo=beaker*");
			KatelloUtils.sshOnServer("cd /mnt/tests/Katello/Installation/KatelloNightly/; make run");
		} else if (product.equals("cfse")) {
			KatelloUtils.sshOnServer("yum install -y Katello-Katello-Installation-SystemEngineLatest --disablerepo=* --enablerepo=beaker*");
			KatelloUtils.sshOnServer("cd /mnt/tests/Katello/Installation/SystemEngineLatest/; export CFSE_RELEASE=" + version + "; make run");
		} else if (product.equals("sam")) {
			KatelloUtils.sshOnServer("yum install -y Katello-Katello-Installation-SAMLatest --disablerepo=* --enablerepo=beaker*");
			KatelloUtils.sshOnServer("cd /mnt/tests/Katello/Installation/SAMLatest/; export SAM_RELEASE=" + version + "; make run");
		} else if (product.equals("headpin")) {
			KatelloUtils.sshOnServer("yum install -y Katello-Katello-Installation-ConfigureRepos --disablerepo=* --enablerepo=beaker*");
			KatelloUtils.sshOnServer("cd /mnt/tests/Katello/Installation/ConfigureRepos/; make run");	

			KatelloUtils.sshOnServer("yum install -y Katello-Katello-Installation-HeadpinNightly --disablerepo=* --enablerepo=beaker*");
			KatelloUtils.sshOnServer("cd /mnt/tests/Katello/Installation/HeadpinNightly/; make run");
		}
		
		startKatello();
		
		try { Thread.sleep(5000); } catch (Exception e) {}
		
		KatelloPing ping = new KatelloPing();
		ping.runOn(machine.getIpAddress());
		SSHCommandResult res = ping.cli_ping();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check services up");
	}
	
	/**
	 * Install CFSE client on provided machine and configure it to server.
	 * @param machine DeltaCloudInstance on which client should be installed.
	 * @param server the hostname of server to which the client should be configured.
	 */
	private static void installClient(DeltaCloudInstance machine, String server) {
		String yumrepo = 
				"[beaker-tasks]\\\\n" +
				"name=bkr-tasks\\\\n" +
				"baseurl=http://beaker-02.app.eng.bos.redhat.com/rpms/\\\\n"+
				"metadata_expire=3m\\\\n"+
				"enabled=1\\\\n"+
				"gpgcheck=0";
		KatelloUtils.sshOnClient(machine.getIpAddress(), "echo -en \""+yumrepo+"\" > /etc/yum.repos.d/beaker-tasks.repo");
		
		yumrepo = 
				"[beaker]\\\\n" +
				"name=Beaker\\\\n" +
				"baseurl=http://beaker.engineering.redhat.com/harness/RedHatEnterpriseLinux6/\\\\n"+
				"enabled=1\\\\n"+
				"skip_if_unavailable=1\\\\n"+
				"gpgcheck=0";
		KatelloUtils.sshOnClient(machine.getIpAddress(), "echo -en \""+yumrepo+"\" > /etc/yum.repos.d/beaker.repo");
		
		String version = System.getProperty("katello.product.version", "1.1");
		String product = System.getProperty("katello.product", "katello");
		
		KatelloUtils.sshOnClient(machine.getIpAddress(), "yum -y install beakerlib beakerlib-redhat rhts-python rhts-test-env --disablerepo=* --enablerepo=beaker*");
		KatelloUtils.sshOnClient(machine.getIpAddress(), "mkdir ~/.beaker_client");
		KatelloUtils.sshOnClient(machine.getIpAddress(), "touch ~/.beaker_client/config");
		KatelloUtils.sshOnClient(machine.getIpAddress(), "echo \"HUB_URL = \"https://beaker.engineering.redhat.com\"\" >> ~/.beaker_client/config");
		KatelloUtils.sshOnClient(machine.getIpAddress(), "echo \"AUTH_METHOD = \"password\"\" >> ~/.beaker_client/config");
		KatelloUtils.sshOnClient(machine.getIpAddress(), "chmod a+x ~/.beaker_client/config");
		
		KatelloUtils.sshOnClient(machine.getIpAddress(), "yum install -y Katello-Katello-Sanity-ImportKeys --disablerepo=* --enablerepo=beaker*");
		KatelloUtils.sshOnClient(machine.getIpAddress(), "cd /mnt/tests/Katello/Sanity/ImportKeys/; make run");
		
		KatelloUtils.sshOnClient(machine.getIpAddress(), "yum install -y Katello-Katello-Installation-RegisterRHNClassic --disablerepo=* --enablerepo=beaker*");
		KatelloUtils.sshOnClient(machine.getIpAddress(), "cd /mnt/tests/Katello/Installation/RegisterRHNClassic/; make run");
		
		if (product.equals("katello")) {
			KatelloUtils.sshOnClient(machine.getIpAddress(), "yum install -y Katello-Katello-Installation-ConfigureRepos --disablerepo=* --enablerepo=beaker*");
			KatelloUtils.sshOnClient(machine.getIpAddress(), "cd /mnt/tests/Katello/Installation/ConfigureRepos/; make run");	

			KatelloUtils.sshOnClient(machine.getIpAddress(), "yum install -y Katello-Katello-Configuration-KatelloClient --disablerepo=* --enablerepo=beaker*");
			KatelloUtils.sshOnClient(machine.getIpAddress(), "cd /mnt/tests/Katello/Configuration/KatelloClient/; export KATELLO_SERVER_HOSTNAME=" + server + "; make run");
		} else if (product.equals("cfse")) {
			KatelloUtils.sshOnClient(machine.getIpAddress(), "yum install -y Katello-Katello-Configuration-KatelloClient --disablerepo=* --enablerepo=beaker*");
			KatelloUtils.sshOnClient(machine.getIpAddress(), "cd /mnt/tests/Katello/Configuration/KatelloClient/; export KATELLO_SERVER_HOSTNAME=" + server + "; export CFSE_RELEASE=" + version + "; make run");
		} else if (product.equals("sam") || product.equals("headpin")) {
			KatelloUtils.sshOnClient(machine.getIpAddress(), "yum -y update subscription-manager python-rhsm --disablerepo=\\*beaker\\*");
			KatelloUtils.sshOnClient(machine.getIpAddress(), "yum -y install http://" + server + "/pub/candlepin-cert-consumer-" + server + "-1.0-1.noarch.rpm --disablerepo \\*beaker\\*");
			KatelloUtils.sshOnClient(machine.getIpAddress(), "sed -i \\\"s|host\\s*=.*|host = " + server + "|g\\\" /etc/katello/client.conf");
		}
		
		if (!product.equals("sam") && !product.equals("headpin")) {
			startKatello();
		
			try { Thread.sleep(5000); } catch (Exception e) {}
			
			KatelloPing ping = new KatelloPing();
			ping.runOn(machine.getIpAddress());
			SSHCommandResult res = ping.cli_ping();
			Assert.assertTrue(res.getExitCode().intValue()==0, "Check services up");
		}
	}
}
