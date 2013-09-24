package com.redhat.qe.katello.tests.installation;

import org.testng.SkipException;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloPing;
import com.redhat.qe.katello.common.KatelloConstants;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups = { "cfse-cli", "headpin-cli" })
public class InstallISOTests extends KatelloCliTestBase {
	
	private String deployment = KatelloConstants.KATELLO_PRODUCT;
	private SSHCommandResult exec_result;
	
	@Test(description="Download ISO file and install to server, configure server and test ping command")
	public void test_install() {
		
		String login = System.getProperty("cdn.username", "qa@redhat.com");
		String password = System.getProperty("cdn.password"); // let user define the password!~
		String iso_url = System.getProperty("iso.file.url"); // iso file too... otherwise exit.
		if(password==null || iso_url==null)
			new SkipException("Following parameters are required: cdn.password; iso.file.url"); // and quit here.
		
		exec_result = KatelloUtils.sshOnServer("subscription-manager register --force --username=" + login + " --password=" + password + " --autosubscribe");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = KatelloUtils.sshOnServer("wget " + iso_url);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = KatelloUtils.sshOnServer("mkdir /tmp/ISO");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = KatelloUtils.sshOnServer("mount *.iso /tmp/ISO -t iso9660 -o loop");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = KatelloUtils.sshOnServer("rpm --import /etc/pki/rpm-gpg/RPM-GPG-KEY-redhat-beta");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = KatelloUtils.sshOnServer("cd /tmp/ISO; ./install_packages");
		
		if (getOutput(exec_result).contains("is not signed")) {
			KatelloUtils.sshOnServer("cp -r /tmp/ISO /tmp/new_ISO");
			KatelloUtils.sshOnServer("chmod +w /tmp/new_ISO/install_packages");
			KatelloUtils.sshOnServer("cd /tmp/new_ISO; sed -i 's/\"katello-foreman-all\"]/\"katello-foreman-all\", \"--nogpgcheck\"]/g' install_packages");
			KatelloUtils.sshOnServer("cd /tmp/new_ISO; sed -i 's/\"katello-headpin-all\"]/\"katello-headpin-all\", \"--nogpgcheck\"]/g' install_packages");
			exec_result = KatelloUtils.sshOnServer("cd /tmp/new_ISO; ./install_packages");
		}
		
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		if (deployment.equals("sam")) {
			KatelloUtils.sshOnServer("katello-configure --deployment=sam --user-name=admin --user-pass=admin --katello-web-workers=2 --job-workers=2 --es-min-mem=512M --es-max-mem=1024M");
		}else if(deployment.equals("headpin")){
			KatelloUtils.sshOnServer("katello-configure --deployment=headpin --user-name=admin --user-pass=admin --katello-web-workers=2 --job-workers=2 --es-min-mem=512M --es-max-mem=1024M");
		}else { // Katello || Sat6
			KatelloUtils.sshOnServer("katello-configure --db-name=katello --db-user=katello --db-password=katello --deployment=katello --user-name=admin --user-pass=admin --katello-web-workers=2 --job-workers=2 --es-min-mem=512M --es-max-mem=1024M");
		}
		
		KatelloPing ping_obj= new KatelloPing(cli_worker);
		exec_result = ping_obj.cli_ping(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
	}
}
