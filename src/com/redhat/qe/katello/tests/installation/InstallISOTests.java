package com.redhat.qe.katello.tests.installation;

import org.testng.SkipException;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloPing;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups = { "cfse-cli", "headpin-cli" })
public class InstallISOTests extends KatelloCliTestBase {
	
	private SSHCommandResult exec_result;
	
	@Test(description="Download ISO file and install to server, configure server and test ping command")
	public void test_install() {
		
		String loginCDN = System.getProperty("cdn.username", "qa@redhat.com");
		String passCDN = System.getProperty("cdn.password"); // let user define the password!~
		String iso_url = System.getProperty("iso.file.url"); // iso file too... otherwise exit.
		if(passCDN==null || iso_url==null)
			new SkipException("Following parameters are required: cdn.password; iso.file.url"); // and quit here.
		
		exec_result = KatelloUtils.sshOnServer("subscription-manager register --force --username=" + loginCDN + " --password=" + passCDN + " --autosubscribe");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = KatelloUtils.sshOnServer("wget " + iso_url);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		String cmdToRun = 
				"mkdir /tmp/ISO;" +
				"mount *.iso /tmp/ISO -t iso9660 -o loop;" +
				"rpm --import /etc/pki/rpm-gpg/RPM-GPG-KEY-redhat-beta;" +
				"cd /tmp/ISO;" +
				"./install_packages";
		exec_result = KatelloUtils.sshOnServer(cmdToRun);
		
		if (getOutput(exec_result).contains("is not signed")) {
			cmdToRun = 
				"cp -r /tmp/ISO /tmp/new_ISO;" +
				"chmod +w /tmp/new_ISO/install_packages;" +
				"cd /tmp/new_ISO;" +
				"sed -i 's/\"katello-foreman-all\"]/\"katello-foreman-all\", \"--nogpgcheck\"]/g' install_packages;" +
				"sed -i 's/\"katello-headpin-all\"]/\"katello-headpin-all\", \"--nogpgcheck\"]/g' install_packages;" +
				"./install_packages";
			exec_result = KatelloUtils.sshOnServer(cmdToRun);
		}
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		String username = System.getProperty("katello.admin.user", "admin");
		String password = System.getProperty("katello.admin.password", "admin");
		String deployment = System.getProperty("katello.product", "katello");
		cmdToRun = String.format("katello-configure " +
			"--deployment=%s " +
			"--user-name=%s " +
			"--user-pass=%s " +
			"--katello-web-workers=2 " +
			"--job-workers=2 " +
			"--es-min-mem=512M " +
			"--es-max-mem=1024M",
			deployment, username, password); 
		if (deployment.equals("sam") || deployment.equals("headpin") || deployment.equals("katello")) {
			KatelloUtils.sshOnServer(cmdToRun);
		}else { // Katello || Sat6
			install_sat6();
		}
		
		KatelloPing ping_obj= new KatelloPing(cli_worker);
		exec_result = ping_obj.cli_ping(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
	}
	
	@AfterTest(alwaysRun=true)
	public void cleanup(){
		String cmdToRun = "subscription-manager unregister; subscription-manager clean";
		log.info("ISO install: unregister from live CDN");
		KatelloUtils.sshOnServer(cmdToRun);		
	}
	
	private void install_sat6(){
		String cmdToRun;
		
		cmdToRun = 
			"iptables -I INPUT 1 -p tcp -m state --state NEW -m tcp --dport 80 -j ACCEPT;" +
			"iptables -I INPUT 1 -p tcp -m state --state NEW -m tcp --dport 443 -j ACCEPT;" +
			"iptables -I INPUT 1 -p tcp -m state --state NEW -m tcp --dport 5674 -j ACCEPT;" +
			"iptables -I INPUT 1 -p tcp -m state --state NEW -m tcp --dport 5671 -j ACCEPT;" +
			"service iptables save";
		log.info("ISO install: configure iptables.");
		KatelloUtils.sshOnServer(cmdToRun);
		
		String username = System.getProperty("katello.admin.user", "admin");
		String password = System.getProperty("katello.admin.password", "admin");
		
		cmdToRun = String.format("katello-configure " +
			"--db-name=katello " +
			"--db-user=katello -" +
			"-db-password=katello " +
			"--deployment=katello " +
			"--user-name=%s " +
			"--user-pass=%s " +
			"--katello-web-workers=2 " +
			"--job-workers=2 " +
			"--es-min-mem=512M " +
			"--es-max-mem=1024M",
			username, password);
		log.info("ISO install: run katello-configure (with parameters)");
		KatelloUtils.sshOnServer(cmdToRun);
		
		// TODO bz#1016134 (remove this workaround once get this fixed.)
		cmdToRun = "[ -d /tmp/new_ISO/ ]; echo \"$?\"";
		exec_result = KatelloUtils.sshOnServer(cmdToRun);
		String repoURL = "file:///tmp/ISO"; String gpgCheck = "1";
		if(KatelloCliTestBase.sgetOutput(exec_result).equals("0")){ // not signed rpms
			repoURL = "file:///tmp/new_ISO";
			gpgCheck = "0";
		}
		
		cmdToRun = "echo -e \"" +
			"[sat6-local]\\n" +
			"name=Satellite 6 Local Install\\n" +
			"baseurl="+repoURL+"\\n" +
			"enabled=1\\n" +
			"gpgcheck="+gpgCheck +
			"\" > /etc/yum.repos.d/sat6-iso.repo; rpm -q node-installer || (yum clean all && yum -y install node-installer)";
		log.info("ISO install: workaround: install node-installer");
		KatelloUtils.sshOnServer(cmdToRun);
		
		cmdToRun = 
			"setenforce 0;" +
			"export FORWARDERS=\"$(rm -rf /tmp/forwarders;for i in $(cat /etc/resolv.conf |grep nameserver|awk '{print $2}'); do echo --dns-forwarders $i>>/tmp/forwarders;done; sed '{:q;N;s/\\n/ /g;t q}' /tmp/forwarders;)\";" +
			"export OAUTH_SECRET=\"$(cat /etc/katello/oauth_token-file)\";" +
			"node-install -v --parent-fqdn $(hostname) --dns true $FORWARDERS --dns-interface eth0 --dns-zone katellolabs.org --dhcp true --dhcp-interface eth0 --pulp false --tftp true --puppet true --puppetca true --register-in-foreman true --pulp-oauth-secret $OAUTH_SECRET --foreman-oauth-secret $OAUTH_SECRET --verbose";
		log.info("ISO install: run node-installer (with parameters)");
		KatelloUtils.sshOnServer(cmdToRun);
			
		cmdToRun = "katello-service restart";
		log.info("ISO install: restart services");
		KatelloUtils.sshOnServer(cmdToRun);
	}
}
