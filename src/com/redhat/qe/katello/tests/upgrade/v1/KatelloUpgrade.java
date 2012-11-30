package com.redhat.qe.katello.tests.upgrade.v1;

import java.util.logging.Logger;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.obj.KatelloPing;
import com.redhat.qe.katello.common.KatelloConstants;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloUpgrade extends KatelloCliTestScript{
	protected static Logger log = Logger.getLogger(KatelloUpgrade.class.getName());
	private String UPGRADE_REPO_LATEST = 
			"http://download.lab.bos.redhat.com/rel-eng/CloudForms/1.1/latest/el6-se/x86_64/os/";

	@BeforeClass(description="detect the product",
			dependsOnGroups={TNG_PRE_UPGRADE},
			groups={TNG_UPGRADE})
	public void detectProduct(){
		log.info("Upgrading katello.product: ["+KATELLO_PRODUCT+"] ...");
		if(KATELLO_PRODUCT.equals("sam"))
			UPGRADE_REPO_LATEST = "http://download.devel.redhat.com/devel/candidate-trees/SAM/latest-SAM-1-RHEL-6/compose/SAM/x86_64/os/";
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
		if(KATELLO_PRODUCT.equals("sam")){
			KatelloUtils.sshOnServer("service elasticsearch start");
			KatelloUtils.sshOnServer(
					"sleep 20; " +
					"curl http://localhost:9200/_flush; sleep 3;" +
					"service elasticsearch stop; sleep 3;");
		}
		KatelloUtils.sshOnServer("yum clean all");
		KatelloUtils.sshOnServer("yum upgrade -y --exclude libxslt --disablerepo \\*beaker\\*"); // TODO --exclude libxslt is workaround which should be removed later
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
		KatelloUtils.sshOnServer("sed -i 's/5674/5671/g' /etc/gofer/plugins/katelloplugin.conf"); // even if it will fail for sam - who cares ;)
	}

	@Test(description="ping services", 
			dependsOnMethods={"runUpgrade"},
			dependsOnGroups={TNG_PRE_UPGRADE}, 
			groups={TNG_UPGRADE})
	public void pingSystem(){
		log.info("No need to start services: just ping to check all if ok");
		KatelloPing ping = new KatelloPing();
		SSHCommandResult res = ping.cli_ping();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check services up");
	}
}
