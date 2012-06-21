package com.redhat.qe.katello.tests.upgrade.v1;

import java.util.logging.Logger;
import org.testng.annotations.Test;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.common.KatelloUtils;

public class KatelloUpgrade extends KatelloCliTestScript{
	protected static Logger log = Logger.getLogger(KatelloUpgrade.class.getName());
	
	public static final String UPGRADE_REPO_LATEST = 
			"http://download.lab.bos.redhat.com/rel-eng/CloudForms/1.0.1/latest/el6-se/x86_64/";
	
	@Test(description="prepare the upgrade yum repo", 
			dependsOnGroups={TNG_PRE_UPGRADE}, 
			groups={TNG_UPGRADE})
	public void test_prepareRepo(){
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
			dependsOnMethods={"test_prepareRepo"},
			dependsOnGroups={TNG_PRE_UPGRADE}, 
			groups={TNG_UPGRADE})
	public void test_stopServices(){
		KatelloUtils.stopKatello();
	}

	@Test(description="update rpms", 
			dependsOnMethods={"test_stopServices"},
			dependsOnGroups={TNG_PRE_UPGRADE}, 
			groups={TNG_UPGRADE})
	public void test_doUpdateRpms(){
		KatelloUtils.sshOnServer("yum -y update pulp* candlepin* katello*"); // TODO maybe blind update everything?
	}
	
	@Test(description="run schema upgrade", 
			dependsOnMethods={"test_doUpdateRpms"},
			dependsOnGroups={TNG_PRE_UPGRADE}, 
			groups={TNG_UPGRADE})
	public void test_doUpgrade(){
		KatelloUtils.sshOnServer("katello-upgrade -y"); // TODO using --log=LOG_FILE option.
	}

	@Test(description="start services", 
			dependsOnMethods={"test_doUpgrade"},
			dependsOnGroups={TNG_PRE_UPGRADE}, 
			groups={TNG_UPGRADE})
	public void test_startServices(){
		KatelloUtils.startKatello();
	}
}
