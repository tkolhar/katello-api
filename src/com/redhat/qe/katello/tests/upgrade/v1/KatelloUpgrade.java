package com.redhat.qe.katello.tests.upgrade.v1;

import java.util.logging.Logger;

import org.testng.annotations.Test;

import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.common.KatelloUtils;

public class KatelloUpgrade extends KatelloCliTestScript{
	protected static Logger log = Logger.getLogger(KatelloUpgrade.class.getName());
	
	public static final String UPGRADE_REPO_LATEST = 
			"http://download.lab.bos.redhat.com/rel-eng/CloudForms/1.1/2012-10-16.1/el6-se/x86_64/os";
	
	public static final String[] CONF_FILES = {"/etc/httpd/conf.d/katello.conf", "/etc/katello/katello.yml", "/etc/katello/thin.yml"};
	
	@Test(description="prepare the upgrade yum repo", 
			dependsOnGroups={TNG_PRE_UPGRADE}, 
			groups={TNG_UPGRADE})
	public void installYumRepo(){
		String upgradeRepo = System.getProperty("katello.upgrade.repo", UPGRADE_REPO_LATEST);
		String _yumrepo = 
				"[cfse-upgrade]\\\\n" +
				"name=System Engine Upgrade\\\\n" +
				"baseurl="+upgradeRepo+"\\\\n"+
				"enabled=1\\\\n"+
				"skip_if_unavailable=1\\\\n"+
				"gpgcheck=0";
		KatelloUtils.sshOnServer("echo -en \""+_yumrepo+"\" > /etc/yum.repos.d/cfse-upgrade.repo");
	}
	
	@Test(description="stop services", 
			dependsOnMethods={"installYumRepo"},
			dependsOnGroups={TNG_PRE_UPGRADE}, 
			groups={TNG_UPGRADE})
	public void stopServices(){
		KatelloUtils.stopKatello();
	}

	@Test(description="update rpms", 
			dependsOnMethods={"stopServices"},
			dependsOnGroups={TNG_PRE_UPGRADE}, 
			groups={TNG_UPGRADE})
	public void updateRpms(){
		KatelloUtils.sshOnServer("yum clean all");
		KatelloUtils.sshOnServer("yum upgrade -y --exclude libxslt"); // TODO --exclude libxslt is workaround which should be removed later
		for (String confFile : CONF_FILES) {
			KatelloUtils.sshOnServer("mv " + confFile + ".rpmnew " + confFile + " -f");
		}
	}
	
	@Test(description="run schema upgrade", 
			dependsOnMethods={"updateRpms"},
			dependsOnGroups={TNG_PRE_UPGRADE}, 
			groups={TNG_UPGRADE})
	public void runUpgrade(){
		KatelloUtils.sshOnServer("katello-upgrade -y"); // TODO using --log=LOG_FILE option.
		KatelloUtils.sshOnServer("katello-service stop");
		KatelloUtils.sshOnServer("katello-configure --answer-file=/etc/katello/katello-configure.conf -b");
		KatelloUtils.sshOnServer("sed -i 's/5674/5671/g' /etc/gofer/plugins/katelloplugin.conf");
	}

	@Test(description="start services", 
			dependsOnMethods={"runUpgrade"},
			dependsOnGroups={TNG_PRE_UPGRADE}, 
			groups={TNG_UPGRADE})
	public void startServices(){
		KatelloUtils.startKatello();
	}
}
