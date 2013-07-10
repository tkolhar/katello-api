package com.redhat.qe.katello.tests.upgrade.v1;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloPing;
import com.redhat.qe.katello.common.KatelloConstants;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloUpgrade extends KatelloCliTestBase{
	protected static Logger log = Logger.getLogger(KatelloUpgrade.class.getName());
	private String UPGRADE_REPO_LATEST = 
			"http://download.lab.bos.redhat.com/rel-eng/CloudForms/1.1/latest/el6-se/x86_64/os/";
	private String UPGRADE_TOOLS_REPO_LATEST = 
			"http://download.lab.bos.redhat.com/rel-eng/CloudForms/1.1/latest/el6-tools/x86_64/os/";
	
	private Set<String> clients = new HashSet<String>();
	
	@BeforeClass(description="detect the product",
			dependsOnGroups={TNG_PRE_UPGRADE},
			groups={TNG_UPGRADE})
	public void detectProduct(){
		log.info("Upgrading katello.product: ["+KATELLO_PRODUCT+"] ...");
		if(KATELLO_PRODUCT.equals("sam"))
			UPGRADE_REPO_LATEST = "http://download.devel.redhat.com/devel/candidate-trees/SAM/latest-SAM-1-RHEL-6/compose/SAM/x86_64/os/";
		
		clients.add(System.getProperty("katello.client.hostname"));
		
		for (String client : System.getProperty("katello.upgrade.clients", "").split(",")) {
			clients.add(client);
		}

		if (SetupServers.client_name != null) clients.add(SetupServers.client_name); 
		if (SetupServers.client_name2 != null) clients.add(SetupServers.client_name2);
		if (SetupServers.client_name3 != null) clients.add(SetupServers.client_name3);
		
		clients.remove(System.getProperty("katello.server.hostname"));
	}
	
	@Test(description="prepare the upgrade yum repo", 
			dependsOnGroups={TNG_PRE_UPGRADE}, 
			groups={TNG_UPGRADE})
	public void installYumRepo(){
		String upgradeRepo = System.getProperty("katello.upgrade.repo", UPGRADE_REPO_LATEST);
		String _yumrepo = 
				"["+KatelloConstants.KATELLO_PRODUCT+"-upgrade]\\\\n" +
				"name="+KatelloConstants.KATELLO_PRODUCT+" upgrade\\\\n" +
				"baseurl="+upgradeRepo+"\\\\n"+
				"enabled=1\\\\n"+
				"skip_if_unavailable=1\\\\n"+
				"gpgcheck=0";
		String upgradeRepoTools = System.getProperty("katello.upgrade.tools.repo", UPGRADE_TOOLS_REPO_LATEST);
		String _yumrepoTools = 
				"["+KatelloConstants.KATELLO_PRODUCT+"-tools-upgrade]\\\\n" +
				"name="+KatelloConstants.KATELLO_PRODUCT+" tools upgrade\\\\n" +
				"baseurl="+upgradeRepoTools+"\\\\n"+
				"enabled=1\\\\n"+
				"skip_if_unavailable=1\\\\n"+
				"gpgcheck=0";
		
		if (Boolean.parseBoolean(System.getProperty("katello.upgrade.usecdn", "false"))) {
			KatelloUtils.sshOnServer("subscription-manager clean");
			KatelloUtils.sshOnServer("sed -i 's/^hostname.*/hostname=subscription.rhn.redhat.com/g' /etc/rhsm/rhsm.conf");
			KatelloUtils.sshOnServer("sed -i 's/prefix.*/prefix=\\/subscription/g' /etc/rhsm/rhsm.conf");			
			KatelloUtils.sshOnServer("sed -i 's/baseurl.*/baseurl=https:\\/\\/cdn.redhat.com/g' /etc/rhsm/rhsm.conf");
			KatelloUtils.sshOnServer("sed -i 's/repo_ca_cert.*/repo_ca_cert=%(ca_cert_dir)sredhat-uep.pem/g' /etc/rhsm/rhsm.conf");
			KatelloUtils.sshOnServer("subscription-manager register --username " + System.getProperty("cdn.username", "qa@redhat.com") + " --password " + System.getProperty("cdn.password", "password") + " --autosubscribe --force");
			KatelloUtils.sshOnServer("subscription-manager subscribe --pool " + System.getProperty("cdn.poolid", "8a85f9843affb61f013b1fae79e26a75"));
			KatelloUtils.sshOnServer("yum clean all");
			KatelloUtils.sshOnServer("yum -y install yum-utils");
			if (KATELLO_PRODUCT.equals("cfse")) {
				KatelloUtils.sshOnServer("yum-config-manager --enable rhel-6-server-cf-se-1-rpms");
				KatelloUtils.sshOnServer("yum-config-manager --enable rhel-6-server-cf-tools-1-rpms");
			} else {
				KatelloUtils.sshOnServer("yum-config-manager --enable rhel-6-server-sam-rpms");	
			}
		} else {
			KatelloUtils.sshOnServer("echo -en \""+_yumrepo+"\" > /etc/yum.repos.d/" + KatelloConstants.KATELLO_PRODUCT + "-upgrade.repo");
		}
		KatelloUtils.sshOnServer("sed -i 's/enabled=1/enabled=0/g' /etc/yum.repos.d/" + KatelloConstants.KATELLO_PRODUCT + ".repo");
		
		for (String client : clients) {
			KatelloUtils.sshOnClient(client, "echo -en \""+_yumrepo+"\" > /etc/yum.repos.d/" + KatelloConstants.KATELLO_PRODUCT + "-upgrade.repo");
			KatelloUtils.sshOnClient(client, "echo -en \""+_yumrepoTools+"\" > /etc/yum.repos.d/" + KatelloConstants.KATELLO_PRODUCT + "-tools-upgrade.repo");
		}
	}
	
	@Test(description="stop services", 
			dependsOnMethods={"installYumRepo"},
			dependsOnGroups={TNG_PRE_UPGRADE}, 
			groups={TNG_UPGRADE})
	public void stopServices(){
		if(KATELLO_PRODUCT.equals("cfse"))
			KatelloUtils.stopKatello();
		else
			KatelloUtils.stopHeadpin();
	}

	@Test(description="update rpms", 
			dependsOnMethods={"stopServices"},
			dependsOnGroups={TNG_PRE_UPGRADE}, 
			groups={TNG_UPGRADE})
	public void updateRpms(){
		if(KATELLO_PRODUCT.equals("sam")){
			KatelloUtils.sshOnServer("service elasticsearch start");
			KatelloUtils.sshOnServer(
					"sleep 20; " +
					"curl http://localhost:9200/_flush; sleep 3;" +
					"service elasticsearch stop; sleep 3;");
		}
		KatelloUtils.sshOnServer("yum clean all");
		SSHCommandResult res;
		if (Boolean.parseBoolean(System.getProperty("katello.upgrade.usecdn", "false"))) {
			res = KatelloUtils.sshOnServer("yum upgrade -y --skip-broken");
		} else {
			res = KatelloUtils.sshOnServer("yum upgrade -y");
		}
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (upgrade)");		
	}
	
	@Test(description="run schema upgrade", 
			dependsOnMethods={"updateRpms"},
			dependsOnGroups={TNG_PRE_UPGRADE}, 
			groups={TNG_UPGRADE})
	public void runUpgrade(){
		KatelloUtils.sshOnServer("yes | katello-upgrade"); // TODO using --log=LOG_FILE option. will change to -y after
		if(KATELLO_PRODUCT.equals("cfse"))
			KatelloUtils.stopKatello();
		else
			KatelloUtils.stopHeadpin();
		KatelloUtils.sshOnServer("katello-configure --answer-file=/etc/katello/katello-configure.conf -b");
		if(KATELLO_PRODUCT.equals("sam")) // YES: to be run twice for SAM 1.2
			KatelloUtils.sshOnServer("katello-configure --answer-file=/etc/katello/katello-configure.conf -b");
	}

	@Test(description="upgrade clients", 
			dependsOnMethods={"runUpgrade"},
			dependsOnGroups={TNG_PRE_UPGRADE}, 
			groups={TNG_UPGRADE})
	public void upgradeClients() {
		SSHCommandResult res;
		for (String client : clients) {
			KatelloUtils.sshOnClient(client, "yum clean all");
			res = KatelloUtils.sshOnClient(client, "yum upgrade -y");
			Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (upgrade)");
		}
	}

	@Test(description="ping services", 
			dependsOnMethods={"upgradeClients"},
			dependsOnGroups={TNG_PRE_UPGRADE}, 
			groups={TNG_UPGRADE})
	public void pingSystem(){
		log.info("No need to start services: just ping to check all if ok");
		KatelloPing ping = new KatelloPing(null);
		SSHCommandResult res = ping.cli_ping();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check services up");
	}
}
