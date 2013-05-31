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
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.obj.DeltaCloudInstance;
import com.redhat.qe.katello.base.obj.KatelloChangeset;
import com.redhat.qe.katello.base.obj.KatelloContentDefinition;
import com.redhat.qe.katello.base.obj.KatelloContentFilter;
import com.redhat.qe.katello.base.obj.KatelloPing;
import com.redhat.qe.katello.base.obj.helpers.FilterRuleErrataIds;
import com.redhat.qe.katello.base.obj.helpers.FilterRulePackage;
import com.redhat.qe.katello.deltacloud.DeltaCloudAPI;
import com.redhat.qe.tools.ExecCommands;
import com.redhat.qe.tools.SCPTools;
import com.redhat.qe.tools.SSHCommandResult;
import com.redhat.qe.tools.SSHCommandRunner;

/**
 * Utility for common (independent from api/cli) and static calls only.<BR>
 * Providing javadoc is appreciated.
 */
public class KatelloUtils implements KatelloConstants {
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
	 * @return DeltaCloud server instance.
	 */
	public static DeltaCloudInstance getDeltaCloudServer() {
		return getDeltaCloudServer(false);
	}
	
	/**
	 * Create new server in DeltaCloud but do not start it.
	 * @return DeltaCloud server instance.
	 */
	public static DeltaCloudInstance getDeltaCloudServerNoWait() {
		return getDeltaCloudServer(true);
	}
	
	/**
	 * Private method for creating DeltaCloud server machine, configuring it.
	 */
	private static DeltaCloudInstance getDeltaCloudServer(boolean nowait) {
		
		String[] configs = getMachineConfigs(true);
		Assert.assertNotNull(configs, "No free machine available on Deltacloud");
		
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
	 * @return DeltaCloud client machine instance.
	 */
	public static DeltaCloudInstance getDeltaCloudClient(String server) {
		
		String image = System.getProperty("deltacloud.client.imageid", "fc06e21b-8973-48e2-9d64-3b5a90f2717e");
		return getDeltaCloudClient(server, image);
	}

	public static DeltaCloudInstance getDeltaCloudClient(String server, String imageId) {
		
		String[] configs = getMachineConfigs(false);
		Assert.assertNotNull(configs, "No free machine available on Deltacloud");

		DeltaCloudInstance client = DeltaCloudAPI.provideClient(false, configs[0],imageId);

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
	
	public static void scpOnClient(String filename, String destinationDir){
		SCPTools scp = new SCPTools(
				System.getProperty("katello.client.hostname", "localhost"), 
				System.getProperty("katello.client.ssh.user", "root"), 
				System.getProperty("katello.client.sshkey.private", ".ssh/id_hudson_dsa"), 
				System.getProperty("katello.client.sshkey.passphrase", "null"));
		Assert.assertTrue(scp.sendFile(filename, destinationDir),
				filename+" sent successfully");
	}
	
	private static String[] getMachineConfigs(boolean isServer) {
		String[][] configs = null;
		if (isServer) {
			configs = KatelloConstants.DELTACLOUD_SERVERS;
		} else {
			configs = KatelloConstants.DELTACLOUD_CLIENTS;
		}
		
		for (String[] config : configs) {
			boolean isHostDisabled = false;
			String result = run_local("/bin/ping -i 1 -c 10 "+config[0] + "." + config[1]);				
			if (result.contains("unknown host") && !DeltaCloudAPI.isMachineExists(config[0])) {
				isHostDisabled = true;
			}	    
			if (isHostDisabled) return config;
		}
		return null;
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
		
		sshOnClient(machine.getIpAddress(), String.format("sed -i \"s/^HOSTNAME=.*/HOSTNAME=%s.%s/\" /etc/sysconfig/network",configs[0],configs[1]));
		sshOnClient(machine.getIpAddress(), "hostname " + configs[0] + "." + configs[1]);
		sshOnClient(machine.getIpAddress(), "service network restart");
		
		try { Thread.sleep(5000); } catch (Exception e) {}
		
		machine.setHostName(configs[0] + "." + configs[1]);
	}
	
	/**
	 * Install CFSE server on provided machine.
	 * @param machine DeltaCloudInstance server.
	 */
	private static void installServer(DeltaCloudInstance machine) {
		String hostIP = machine.getIpAddress();
		String version = System.getProperty("katello.product.version", "1.1");
		String product = System.getProperty("katello.product", "katello");
		String ldap = System.getProperty("ldap.server.type", "");
		
		setupBeakerRepo(hostIP);
		configureBeaker(hostIP);
				
		BeakerUtils.Katello_Sanity_ImportKeys(hostIP);
		BeakerUtils.Katello_Installation_RegisterRHNClassic(hostIP);
		
		configureNtp(hostIP);
		
		// Install the product
		if (product.equals("katello")) {
			BeakerUtils.Katello_Installation_ConfigureRepos(hostIP);
			if (ldap.isEmpty()) {
				BeakerUtils.Katello_Installation_KatelloNightly(hostIP);	
			} else{
				BeakerUtils.Katello_Installation_KatelloWithLdap(hostIP, ldap);
			}
		} else if (product.equals("cfse")) {
			BeakerUtils.Katello_Installation_SystemEngineLatest(hostIP, version);
		} else if (product.equals("sam")) {
			BeakerUtils.Katello_Installation_SAMLatest(hostIP, version);
		} else if (product.equals("headpin")) {
			BeakerUtils.Katello_Installation_ConfigureRepos(hostIP);
			if (ldap.isEmpty()) {
				BeakerUtils.Katello_Installation_HeadpinNightly(hostIP);	
			} else{
				BeakerUtils.Katello_Installation_HeadpinWithLdap(hostIP, ldap);
			}
		} else if (product.equals("sat6")) {
			if (ldap.isEmpty()) {
				BeakerUtils.Katello_Installation_Satellite6Latest(hostIP, version);	
			} else{
				BeakerUtils.Katello_Installation_Satellite6WithLdap(hostIP, version, ldap);
			}
		}
		
		// Configure the server as a self-client
		BeakerUtils.Katello_Configuration_KatelloClient(hostIP, machine.getHostName(), version); // at this time DDNS should return the hostname already! It takes ~5 min.
		
		try { Thread.sleep(5000); } catch (Exception e) {}
		KatelloPing ping = new KatelloPing();
		ping.runOn(machine.getHostName()); // Yes, we can use the hostname already. Assuming installation would take >5 min.
		SSHCommandResult res = ping.cli_ping();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check services up");
	}
	
	/**
	 * Install CFSE client on provided machine and configure it to server.
	 * @param machine DeltaCloudInstance on which client should be installed.
	 * @param server the hostname of server to which the client should be configured.
	 */
	private static void installClient(DeltaCloudInstance machine, String server) {
		String hostname = machine.getIpAddress();
		String version = System.getProperty("katello.product.version", "1.1");
		
		setupBeakerRepo(hostname);
		configureBeaker(hostname);
				
		BeakerUtils.Katello_Sanity_ImportKeys(hostname);
		BeakerUtils.Katello_Installation_RegisterRHNClassic(hostname);
		BeakerUtils.Katello_Configuration_KatelloClient(hostname, server, version);
	}
	
	/**
	 * @author Garik Khachikyan
	 * @since 07.Mar.2013 - Katello nightly katello-1.3.14-1.git.817.b5f1eb9.el6.noarch
	 */
	private static void setupBeakerRepo(String hostname){
		String redhatRelease = KatelloCliTestScript.sgetOutput(KatelloUtils.sshOnClient(hostname, "cat /etc/redhat-release"));
		String bkrRepoUrl = "http://beaker.engineering.redhat.com/harness/RedHatEnterpriseLinux6";
		if(redhatRelease.startsWith(REDHAT_RELEASE_RHEL5X))
			bkrRepoUrl = "http://beaker.engineering.redhat.com/harness/RedHatEnterpriseLinuxServer5";
		
		String yumrepo = 
				"[beaker]\\\\n" +
				"name=Beaker\\\\n" +
				"baseurl="+bkrRepoUrl+"\\\\n"+
				"enabled=1\\\\n"+
				"skip_if_unavailable=1\\\\n"+
				"gpgcheck=0";
		KatelloUtils.sshOnClient(hostname, "echo -en \""+yumrepo+"\" > /etc/yum.repos.d/beaker.repo");
		
		yumrepo = 
				"[beaker-tasks]\\\\n" +
				"name=bkr-tasks\\\\n" +
				"baseurl=http://beaker-02.app.eng.bos.redhat.com/rpms/\\\\n"+
				"metadata_expire=3m\\\\n"+
				"enabled=1\\\\n"+
				"gpgcheck=0";
		KatelloUtils.sshOnClient(hostname, "echo -en \""+yumrepo+"\" > /etc/yum.repos.d/beaker-tasks.repo");
	}
	
	private static void configureBeaker(String hostname){
		String cmds = 
				"yum -y install beakerlib beakerlib-redhat rhts-python rhts-test-env --disablerepo=* --enablerepo=beaker*; " +
				"mkdir ~/.beaker_client; " +
				"touch ~/.beaker_client/config; " +
				"echo \"HUB_URL = \"https://beaker.engineering.redhat.com\"\" >> ~/.beaker_client/config; " +
				"echo \"AUTH_METHOD = \"password\"\" >> ~/.beaker_client/config; " +
				"chmod a+x ~/.beaker_client/config";
		KatelloUtils.sshOnClient(hostname, cmds);
	}
	
	private static void configureNtp(String hostname){
		sshOnClient(hostname, "rpm -q ntp || yum -y install ntp");
		sshOnClient(hostname, "service ntpd restart");
		sshOnClient(hostname, "chkconfig --add ntpd; chkconfig ntpd on");
	}

	public static String promoteReposToEnvironment(String org_name, String[] product_names, String[] repo_names, String env_name) {
		return promoteToEnvironment(org_name, product_names, null, repo_names, new String[] {env_name}, true);
	}

	public static String promoteReposToEnvironments(String org_name, String[] product_names, String[] repo_names, String[] env_names) {
		return promoteToEnvironment(org_name, product_names, null, repo_names, env_names, true);
	}
	
	public static String promoteRepoToEnvironment(String org_name, String product_name, String repo_name, String env_name) {
		return promoteToEnvironment(org_name, new String[] {product_name}, null, new String[] {repo_name}, new String[] {env_name}, true);
	}

	public static String promoteProductToEnvironment(String org_name, String product_name, String env_name) {
		return promoteToEnvironment(org_name, new String[] {product_name}, null, null, new String[] {env_name}, true);
	}

	public static String promoteProductsToEnvironment(String org_name, String[] product_names, String env_name) {
		return promoteToEnvironment(org_name, product_names, null, null, new String[] {env_name}, true);
	}

	public static String promoteProductsToEnvironments(String org_name, String[] product_names, String[] env_names) {
		return promoteToEnvironment(org_name, product_names, null, null, env_names, true);
	}
	
	public static String promoteProductIDsToEnvironment(String org_name, String[] product_IDs, String env_name) {
		return promoteToEnvironment(org_name, null, product_IDs, null, new String[] {env_name}, true);
	}

	public static void removeRepoFromEnvironment(String org_name, String product_name, String repo_name, String env_name) {
		promoteToEnvironment(org_name, new String[] {product_name}, null, new String[] {repo_name}, new String[] {env_name}, false);
	}
	
	public static void removeReposFromEnvironment(String org_name, String[] product_names, String[] repo_names, String env_name) {
		promoteToEnvironment(org_name, product_names, null, repo_names, new String[] {env_name}, false);
	}

	public static void removeProductFromEnvironment(String org_name, String product_name, String env_name) {
		promoteToEnvironment(org_name, new String[] {product_name}, null, null, new String[] {env_name}, false);
	}

	public static void removeProductIDsFromEnvironment(String org_name, String[] product_IDs, String env_name) {
		promoteToEnvironment(org_name, null, product_IDs, null, new String[] {env_name}, false);
	}

	private static String promoteToEnvironment(String org_name, String[] product_names, String[] product_IDs, String[] repo_names, String[] env_names, boolean promote) {

		String uid = KatelloUtils.getUniqueID();
		String pubview_name = "pubview"+uid;		

		KatelloContentDefinition condef = new KatelloContentDefinition("content_definition" + uid, null, org_name, null);
		SSHCommandResult exec_result = condef.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");				
		
		int i = 0;
		if (product_names != null && product_names.length > 0) {
			for (String prod_name : product_names) {
				if (repo_names != null && repo_names.length > 0) {
					if (repo_names.length > i) {
						exec_result = condef.add_repo(prod_name, repo_names[i]);
						Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	
					} else {
						exec_result = condef.add_repo(prod_name, repo_names[0]);
						Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
					}
				} else {
					exec_result = condef.add_product(prod_name);
					Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
				}
				i++;
			}
		}
		
		if (product_IDs != null && product_IDs.length > 0) {
			for (String product_id : product_IDs) {
				exec_result = condef.add_productID(product_id);
				Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	
			}
		}
		
		exec_result = condef.publish(pubview_name, pubview_name,"Publish Content");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");		
	
		if (env_names != null && env_names.length > 0) {
			for (String env_name : env_names) {
				String changeset_name = "changeset"+getUniqueID();
				KatelloChangeset cs = new KatelloChangeset(changeset_name, org_name, env_name);
				exec_result = cs.create();
				Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
				exec_result = cs.update_addView(pubview_name);
				Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
				exec_result = cs.apply();
				Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
			}
		}
		return pubview_name;
	}

	public static String promotePackagesToEnvironment(String org_name, String product_name, String repo_name, String[] packages, String env_name) {
		return promoteToEnvironment(org_name, product_name, repo_name, packages, null, env_name, true);
    }

	public static String promoteErratasToEnvironment(String org_name, String product_name, String repo_name, String[] erratas, String env_name) {
		return promoteToEnvironment(org_name, product_name, repo_name, null, erratas, env_name, true);
    }

	public static String removePackagesFromEnvironment(String org_name, String product_name, String repo_name, String[] packages, String env_name) {
		return promoteToEnvironment(org_name, product_name, repo_name, packages, null, env_name, false);
    }

	public static String removeErratasFromEnvironment(String org_name, String product_name, String repo_name, String[] erratas, String env_name) {
		return promoteToEnvironment(org_name, product_name, repo_name, null, erratas, env_name, false);
    }
	
	private static String promoteToEnvironment(String org_name, String product_name, String repo_name, String[] package_names, String[] erratas, String env_name, boolean promote) {

		String uid = KatelloUtils.getUniqueID();
		String pubview_name = "pubview"+uid;		

		KatelloContentDefinition condef = new KatelloContentDefinition("content_definition" + uid, null, org_name, null);
		SSHCommandResult exec_result = condef.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = condef.add_repo(product_name, repo_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		KatelloContentFilter filter = new KatelloContentFilter("Filter"+uid, org_name, condef.name);
		filter.add_repo(product_name, repo_name);
		exec_result = filter.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		if (erratas != null) {
			FilterRuleErrataIds errata1 = new FilterRuleErrataIds(erratas);
			exec_result = filter.add_rule(( promote ? KatelloContentFilter.TYPE_INCLUDES : KatelloContentFilter.TYPE_EXCLUDES), errata1);
			Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		}
		if (package_names != null) {
			FilterRulePackage [] packages = new FilterRulePackage[package_names.length];
			for (int i = 0; i < package_names.length; i ++) {
				packages[i] = new FilterRulePackage("package_names[i]");
			}
			
			exec_result = filter.add_rule(( promote ? KatelloContentFilter.TYPE_INCLUDES : KatelloContentFilter.TYPE_EXCLUDES), packages);
			Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");		
		}
		exec_result = condef.publish(pubview_name, pubview_name,"Publish Content");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");		

		String changeset_name = "changeset"+uid;
		KatelloChangeset cs = new KatelloChangeset(changeset_name, org_name, env_name);
		exec_result = cs.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = cs.update_addView(pubview_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = cs.apply();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		return pubview_name;
	}
	
	public static void disableYumRepo(String repoPattern){
		log.info("Disabling yum repos: [*"+repoPattern+"*]");
		configureYumRepo(repoPattern, true);
	}
	public static void enableYumRepo(String repoPattern){
		log.info("Enabling back yum repos: [*"+repoPattern+"*]");
		configureYumRepo(repoPattern, false);
	}
	private static void configureYumRepo(String repoPattern, boolean disable){
		int enabled = (disable ? 0: 1);
		sshOnClient("sed -i \"s/^enabled=.*/enabled="+enabled+"/\" /etc/yum.repos.d/*"+repoPattern+"*");
	}

	/**
	 * Returns katello cli output block (usually: [command] list -v options) that has: <BR>
	 * [Property]:  [Value] in its block.<br>
	 * As an example would be getting a pool information for:<BR> 
	 * ("ProductName","High-Availability (8 sockets)",org.subscriptions())
	 * @param property
	 * @param value
	 * @param output
	 * @return
	 */
	public static String grepOutBlock(String property, String value, String output){
		String _return = null;
		String[] lines = output.split("\\n\\n");
		
		for(String line:lines ){
			if(line.startsWith("---") || line.trim().equals("")) continue; // skip it.
			if(KatelloUtils.grepCLIOutput(property, line).equals(value)){
				_return = line.trim();
				break;
			}
		}
		return _return;
	}

	public static String grepCLIOutput(String property, String output) {
	    return grepCLIOutput(property, output, 1);
	}

	public static String grepCLIOutput(String property, String output, int occurence) {
	    int meet_cnt = 0;
	    String[] lines = output.split("\\n");
	    for (int i = 0; i < lines.length; i++) {
	        if (lines[i].startsWith(property)) { // our line
	            meet_cnt++;
	            if (meet_cnt == occurence) {
	                String[] split = lines[i].split(":\\s+");
	                if (split.length < 2) {
	                    if(i==lines.length-1) 
	                    	return "";//last line and has empty value.
	                    else 
	                    	return lines[i + 1].trim(); // regular one (like Description:). return next line.
	                } else {
	                    return split[1].trim(); // the one with "property: Value" format.
	                }
	            }
	        }
	    }
	    log.severe("ERROR: Output can not be extracted for the property: ["+property+"]");
	    return null;
	}
}
