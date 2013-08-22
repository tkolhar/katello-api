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
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.DeltaCloudInstance;
import com.redhat.qe.katello.base.obj.KatelloContentDefinition;
import com.redhat.qe.katello.base.obj.KatelloContentFilter;
import com.redhat.qe.katello.base.obj.KatelloContentView;
import com.redhat.qe.katello.base.obj.KatelloPing;
import com.redhat.qe.katello.base.obj.KatelloUser;
import com.redhat.qe.katello.base.obj.helpers.FilterRuleErrataIds;
import com.redhat.qe.katello.base.obj.helpers.FilterRulePackage;
import com.redhat.qe.katello.base.threading.KatelloCliWorker;
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
    public static synchronized String getUniqueID(){
        KatelloUtils.sleepAsHuman();
        String uid = String.valueOf(
                Calendar.getInstance().getTimeInMillis()); 
        log.fine(String.format("Generating unique ID: [%s]",uid));
        return uid;
    }
    
    public static synchronized String getUUID(){
        return UUID.randomUUID().toString();
    }
    
	/**
	 * Executes ssh command on client-side.<br>
	 * Credentials (passphrase) and other settings are all taken from System.properties.
	 * @param _hostName the client on which command will be executed
	 * @param _cmd Command string to execute. Multiple commands could be provided with ";".
	 * @return SSHCommandResult object
	 * @author Garik Khachikyan <gkhachik@redhat.com>
	 */
	public static synchronized SSHCommandResult sshOnClient(String _hostname, String _cmd){
		sleepAsHuman();
		return getSSHClient(_hostname).runCommandAndWait(_cmd);
	}

	/**
	 * Executes ssh command(s) on client side and returns without waiting its result.<br>
	 * Useful for some async commands like: provider synchronize (with option to cancel it later).
	 * @param _hostName
	 * @param _cmd
	 * @author Garik Khachikyan <gkhachik@redhat.com>
	 */
	public static synchronized void sshOnClientNoWait(String _hostName, String _cmd){
		sleepAsHuman();
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
		sleepAsHuman();
		return getSSHServer().runCommandAndWait(_cmd);
	}
	
	public static void sleepAsHuman(){
		try{
			Thread.sleep(50+Math.abs(new Random().nextLong())%SSH_SLEEP_INTERVAL);
		}catch (InterruptedException e){
			log.warning(e.getMessage());
		}
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
	
	protected static synchronized SSHCommandRunner getSSHServer(){
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
		System.setProperty("katello.client.hostname", server.getIpAddress());
		
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

	public static DeltaCloudInstance getDeltaCloudClientCertOnly(String server, String imageId) {
		
		String[] configs = getMachineConfigs(false);
		Assert.assertNotNull(configs, "No free machine available on Deltacloud");

		DeltaCloudInstance client = DeltaCloudAPI.provideClient(false, configs[0],imageId);

		Assert.assertNotNull(client.getClient());
		
		configureDDNS(client, configs);
		
		_sshClients.put(client.getHostName(), _sshClients.get(client.getIpAddress()));
		
		installCandlepinCert(client, server);
		
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
	
	public static void scpOnClient(String client, String filename, String destinationDir){
		String hostname = (client == null ? System.getProperty("katello.client.hostname", "localhost") : client);
		SCPTools scp = new SCPTools(
				hostname, 
				System.getProperty("katello.client.ssh.user", "root"), 
				System.getProperty("katello.client.sshkey.private", ".ssh/id_hudson_dsa"), 
				System.getProperty("katello.client.sshkey.passphrase", "null"));
		Assert.assertTrue(scp.sendFile(filename, destinationDir),
				filename+" sent successfully");
	}
	
	public static boolean scpOnClientGetFile(String client, String filename, String destinationDir){
		String hostname = (client == null ? System.getProperty("katello.client.hostname", "localhost") : client);
		SCPTools scp = new SCPTools(
				hostname, 
				System.getProperty("katello.client.ssh.user", "root"), 
				System.getProperty("katello.client.sshkey.private", ".ssh/id_hudson_dsa"), 
				System.getProperty("katello.client.sshkey.passphrase", "null"));
		return scp.getFile(filename, destinationDir);
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
		
		KatelloUtils.sleepAsHuman();
		
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
		String user = System.getProperty("katello.admin.user", KatelloUser.DEFAULT_ADMIN_USER);
		String password = System.getProperty("katello.admin.password", KatelloUser.DEFAULT_ADMIN_PASS);

		BeakerUtils.Katello_Installation_RegisterRHNClassic(hostIP);
		
		configureNtp(hostIP);
		
		if (Boolean.parseBoolean(System.getProperty("deltacloud.installserver", "true"))) {
			// Install the product
			if (product.equals("katello")) {
				BeakerUtils.Katello_Installation_ConfigureRepos(hostIP);
				if (ldap.isEmpty()) {
					BeakerUtils.Katello_Installation_KatelloNightly(hostIP);	
				} else{
					BeakerUtils.Katello_Installation_KatelloWithLdap(hostIP, ldap, user, password);
				}
			} else if (product.equals("cfse")) {
				BeakerUtils.Katello_Installation_SystemEngineLatest(hostIP, version);
			} else if (product.equals("sam")) {
				if (ldap.isEmpty()) {
					BeakerUtils.Katello_Installation_SAMLatest(hostIP, version);
				} else {
					BeakerUtils.Katello_Installation_SAMLatestWithLdap(hostIP, version, ldap, user, password);
				}
			} else if (product.equals("headpin")) {
				BeakerUtils.Katello_Installation_ConfigureRepos(hostIP);
				if (ldap.isEmpty()) {
					BeakerUtils.Katello_Installation_HeadpinNightly(hostIP);	
				} else{
					BeakerUtils.Katello_Installation_HeadpinWithLdap(hostIP, ldap, user, password);
				}
			} else if (product.equals("sat6")) {
				if (ldap.isEmpty()) {
					BeakerUtils.Katello_Installation_Satellite6Latest(hostIP, version);	
				} else{
					BeakerUtils.Katello_Installation_Satellite6WithLdap(hostIP, version, ldap, user, password);
				}
			}
			
			// Configure the server as a self-client
			BeakerUtils.Katello_Configuration_KatelloClient(hostIP, machine.getHostName(), version, product); // at this time DDNS should return the hostname already! It takes ~5 min.
			
			KatelloUtils.sleepAsHuman();
			KatelloPing ping = new KatelloPing(new KatelloCliWorker(machine.getHostName(), machine.getHostName()));
			SSHCommandResult res = ping.cli_ping();
			Assert.assertTrue(res.getExitCode().intValue()==0, "Check services up");
		}
	}
	
	/**
	 * Install CFSE client on provided machine and configure it to server.
	 * @param machine DeltaCloudInstance on which client should be installed.
	 * @param server the hostname of server to which the client should be configured.
	 */
	private static void installClient(DeltaCloudInstance machine, String server) {
		String hostname = machine.getIpAddress();
		String version = System.getProperty("katello.product.version", "1.1");
		String product = System.getProperty("katello.product", "katello");

		BeakerUtils.Katello_Installation_RegisterRHNClassic(hostname);
		configureNtp(hostname);
		BeakerUtils.Katello_Installation_ConfigureRepos(hostname);
		BeakerUtils.Katello_Configuration_KatelloClient(hostname, server, version, product);
	}
	
	/**
	 * Install server's candlepin cert on client machine.
	 * @param machine DeltaCloudInstance on which client should be installed.
	 * @param server the hostname of server to which the client should be configured.
	 */
	private static void installCandlepinCert(DeltaCloudInstance machine, String server) {
		String hostname = machine.getIpAddress();

		BeakerUtils.Katello_Installation_RegisterRHNClassic(hostname);
		configureNtp(hostname);
		BeakerUtils.install_CandlepinCert(hostname, server);
	}
		
	private static void configureNtp(String hostname){
		sshOnClient(hostname, "rpm -q ntp || yum -y install ntp");
		sshOnClient(hostname, "chkconfig --add ntpd; chkconfig ntpd on");
		sshOnClient(hostname, "service ntpd stop");
		sshOnClient(hostname, "ntpdate clock.redhat.com");
		sshOnClient(hostname, "service ntpd start");
	}

	public static String promoteReposToEnvironment(KatelloCliWorker kcr, String org_name, String[] product_names, String[] repo_names, String env_name) {
		return promoteToEnvironment(kcr, org_name, product_names, null, repo_names, new String[] {env_name}, true);
	}

	public static String promoteReposToEnvironments(KatelloCliWorker kcr, String org_name, String[] product_names, String[] repo_names, String[] env_names) {
		return promoteToEnvironment(kcr, org_name, product_names, null, repo_names, env_names, true);
	}
	
	public static String promoteRepoToEnvironment(KatelloCliWorker kcr, String org_name, String product_name, String repo_name, String env_name) {
		return promoteToEnvironment(kcr, org_name, new String[] {product_name}, null, new String[] {repo_name}, new String[] {env_name}, true);
	}

	public static String promoteProductToEnvironment(KatelloCliWorker kcr, String org_name, String product_name, String env_name) {
		return promoteToEnvironment(kcr, org_name, new String[] {product_name}, null, null, new String[] {env_name}, true);
	}

	public static String promoteProductsToEnvironment(KatelloCliWorker kcr, String org_name, String[] product_names, String env_name) {
		return promoteToEnvironment(kcr, org_name, product_names, null, null, new String[] {env_name}, true);
	}

	public static String promoteProductsToEnvironments(KatelloCliWorker kcr, String org_name, String[] product_names, String[] env_names) {
		return promoteToEnvironment(kcr, org_name, product_names, null, null, env_names, true);
	}
	
	public static String promoteProductIDsToEnvironment(KatelloCliWorker kcr, String org_name, String[] product_IDs, String env_name) {
		return promoteToEnvironment(kcr, org_name, null, product_IDs, null, new String[] {env_name}, true);
	}

	public static void removeRepoFromEnvironment(KatelloCliWorker kcr, String org_name, String product_name, String repo_name, String env_name) {
		promoteToEnvironment(kcr, org_name, new String[] {product_name}, null, new String[] {repo_name}, new String[] {env_name}, false);
	}
	
	public static void removeReposFromEnvironment(KatelloCliWorker kcr, String org_name, String[] product_names, String[] repo_names, String env_name) {
		promoteToEnvironment(kcr, org_name, product_names, null, repo_names, new String[] {env_name}, false);
	}

	public static void removeProductFromEnvironment(KatelloCliWorker kcr, String org_name, String product_name, String env_name) {
		promoteToEnvironment(kcr, org_name, new String[] {product_name}, null, null, new String[] {env_name}, false);
	}

	public static void removeProductIDsFromEnvironment(KatelloCliWorker kcr, String org_name, String[] product_IDs, String env_name) {
		promoteToEnvironment(kcr, org_name, null, product_IDs, null, new String[] {env_name}, false);
	}

	private static String promoteToEnvironment(KatelloCliWorker kcr, String org_name, String[] product_names, String[] product_IDs, String[] repo_names, String[] env_names, boolean promote) {

		String uid = KatelloUtils.getUniqueID();
		String pubview_name = "pubview"+uid;		

		KatelloContentDefinition condef = new KatelloContentDefinition(kcr, "content_definition" + uid, null, org_name, null);
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
		
		KatelloContentView view = new KatelloContentView(kcr, pubview_name, org_name);
	
		if (env_names != null && env_names.length > 0) {
			for (String env_name : env_names) {
				exec_result = view.promote_view(env_name);
				Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
			}
		}
		return pubview_name;
	}

	public static String promotePackagesToEnvironment(KatelloCliWorker kcr, String org_name, String product_name, String repo_name, String[] packages, String env_name) {
		return promoteToEnvironment(kcr, org_name, product_name, repo_name, packages, null, env_name, true);
    }

	public static String promoteErratasToEnvironment(KatelloCliWorker kcr, String org_name, String product_name, String repo_name, String[] erratas, String env_name) {
		return promoteToEnvironment(kcr, org_name, product_name, repo_name, null, erratas, env_name, true);
    }

	public static String removePackagesFromEnvironment(KatelloCliWorker kcr, String org_name, String product_name, String repo_name, String[] packages, String env_name) {
		return promoteToEnvironment(kcr, org_name, product_name, repo_name, packages, null, env_name, false);
    }

	public static String removeErratasFromEnvironment(KatelloCliWorker kcr, String org_name, String product_name, String repo_name, String[] erratas, String env_name) {
		return promoteToEnvironment(kcr, org_name, product_name, repo_name, null, erratas, env_name, false);
    }
	
	private static String promoteToEnvironment(KatelloCliWorker kcr, String org_name, String product_name, String repo_name, String[] package_names, String[] erratas, String env_name, boolean promote) {

		String uid = KatelloUtils.getUniqueID();
		String pubview_name = "pubview"+uid;		

		KatelloContentDefinition condef = new KatelloContentDefinition(kcr, "content_definition" + uid, null, org_name, null);
		SSHCommandResult exec_result = condef.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = condef.add_repo(product_name, repo_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		KatelloContentFilter filter = new KatelloContentFilter(kcr, "Filter"+uid, org_name, condef.name);
		exec_result = filter.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = filter.add_repo(product_name, repo_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		if (erratas != null) {
			FilterRuleErrataIds errata1 = new FilterRuleErrataIds(erratas);
			exec_result = filter.add_rule(( promote ? KatelloContentFilter.TYPE_INCLUDES : KatelloContentFilter.TYPE_EXCLUDES), errata1);
			Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		}
		if (package_names != null) {
			FilterRulePackage [] packages = new FilterRulePackage[package_names.length];
			for (int i = 0; i < package_names.length; i ++) {
				packages[i] = new FilterRulePackage(package_names[i]);
			}
			
			exec_result = filter.add_rule(( promote ? KatelloContentFilter.TYPE_INCLUDES : KatelloContentFilter.TYPE_EXCLUDES), packages);
			Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");		
		}
		exec_result = condef.publish(pubview_name, pubview_name,"Publish Content");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");		

		KatelloContentView view = new KatelloContentView(kcr, pubview_name, org_name);
		exec_result = view.promote_view(env_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		return pubview_name;
	}
	
	public static void disableYumRepo(String client, String repoPattern){
		log.info("Disabling yum repos: [*"+repoPattern+"*]");
		configureYumRepo(client, repoPattern, true);
	}
	public static void enableYumRepo(String client, String repoPattern){
		log.info("Enabling back yum repos: [*"+repoPattern+"*]");
		configureYumRepo(client, repoPattern, false);
	}
	private static void configureYumRepo(String client, String repoPattern, boolean disable){
		int enabled = (disable ? 0: 1);
		sshOnClient(client, "sed -i \"s/^enabled=.*/enabled="+enabled+"/\" /etc/yum.repos.d/*"+repoPattern+"*");
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
	
	public static String getKatelloConfigureCommand() {
		StringBuilder result = new StringBuilder("katello-configure --deployment=" + KatelloConstants.KATELLO_PRODUCT + " ");
		String configFile = KatelloUtils.sshOnServer("cat /etc/katello/katello-configure.conf | grep -Ev \"^#\"").getStdout();
		result.append(configFile.replaceAll("\\n", " "));
		return result.toString();
	}
	
	public static boolean isKatelloAvailable(String hostname) {
		String result = run_local("/bin/ping -i 1 -c 10 " + hostname);				
		if (result.contains("unknown host")) {
			return false;
		}
		
		return new KatelloPing(new KatelloCliWorker(hostname, hostname)).cli_ping().getExitCode().intValue()==0;
	}
}
