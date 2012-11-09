package com.redhat.qe.katello.tests.upgrade.v1;

import java.util.logging.Logger;
import org.testng.annotations.Test;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.common.KatelloConstants;
import com.redhat.qe.katello.common.KatelloUtils;

public class KatelloUpgrade extends KatelloCliTestScript{
	protected static Logger log = Logger.getLogger(KatelloUpgrade.class.getName());
	private static String UPGRADE_REPO_LATEST = 
			"http://download.lab.bos.redhat.com/rel-eng/CloudForms/1.1/latest/el6-se/x86_64/os/";
	private static String KATELLO_PRODUCT = System.getProperty("katello.product", "cfse"); 

	// static initializer. Specify your katello.product specific "if"s here.
	{
		if(KATELLO_PRODUCT.equals("sam")){
			UPGRADE_REPO_LATEST = "http://download.devel.redhat.com/devel/candidate-trees/SAM/latest-SAM-1-RHEL-6/compose/SAM/x86_64/os/";
		}
	}
	
	
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
		KatelloUtils.sshOnServer("echo -en \""+_yumrepo+"\" > /etc/yum.repos.d/" + KatelloConstants.KATELLO_PRODUCT + "-upgrade.repo");
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
		KatelloUtils.sshOnServer("yum clean all");
		KatelloUtils.sshOnServer("yum upgrade -y --exclude libxslt"); // TODO --exclude libxslt is workaround which should be removed later
	}
	
	@Test(description="run schema upgrade", 
			dependsOnMethods={"updateRpms"},
			dependsOnGroups={TNG_PRE_UPGRADE}, 
			groups={TNG_UPGRADE})
	public void runUpgrade(){
		KatelloUtils.sshOnServer("yes | katello-upgrade"); // TODO using --log=LOG_FILE option. will change to -y after
		KatelloUtils.stopKatello();
		KatelloUtils.sshOnServer("katello-configure --answer-file=/etc/katello/katello-configure.conf -b");
		KatelloUtils.sshOnServer("sed -i 's/5674/5671/g' /etc/gofer/plugins/katelloplugin.conf"); // even if it will fail for sam - who cares ;)
	}

	@Test(description="start services", 
			dependsOnMethods={"runUpgrade"},
			dependsOnGroups={TNG_PRE_UPGRADE}, 
			groups={TNG_UPGRADE})
	public void startServices(){
		if(KATELLO_PRODUCT.equals("cfse"))
			KatelloUtils.startKatello();
		else
			KatelloUtils.startKatello();
	}
}
