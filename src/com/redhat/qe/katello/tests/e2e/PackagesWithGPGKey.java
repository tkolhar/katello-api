package com.redhat.qe.katello.tests.e2e;

import java.util.logging.Logger;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.redhat.qe.auto.testng.Assert;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.katello.base.obj.KatelloChangeset;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloGpgKey;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.tasks.KatelloTasks;
import com.redhat.qe.tools.SSHCommandResult;

/**
 * Scenario:<BR>
 * GPG keys: End to end "If I sign it can I install it"?<BR>
 * @author gkhachik
 */
@Test(groups={"cfse-e2e"})
public class PackagesWithGPGKey extends KatelloCliTestScript{
	protected static Logger log = Logger.getLogger(PackagesWithGPGKey.class.getName());

	private String org;
	private String env = "Dev";
	private String provider;
	private String product;
	private String repo;
	private String gpg_key;
	private String system;
	
	@BeforeClass(description="Init unique names", alwaysRun=true)
	public void setUp(){
		String uniqueID = KatelloTestScript.getUniqueID();
		this.org = "GPGOrg"+uniqueID;
		this.provider = "GPGProv"+uniqueID;
		this.product = "GPGProd"+uniqueID;
		this.repo = "GPGRepo_Zoo"+uniqueID;
		this.gpg_key = "gpg_zoo"+uniqueID;
		
		log.info("E2E - Cleanup GPG stuff");
		KatelloUtils.sshOnClient("yum -y erase wolf lion || true");
		KatelloUtils.sshOnClient("subscription-manager unregister || true");
		KatelloUtils.sshOnClient("rpm -e "+KatelloGpgKey.GPG_PUBKEY_RPM_ZOO+" || true");
		
		log.info("E2E - Create org");
		KatelloOrg org = new KatelloOrg(this.org, null);
		org.cli_create();
	}
	
	@Test(description="Create environment, gpg key", enabled=true)
	public void test_prepareEnvGpgKey(){
		log.info("E2E - Create environment/gpg key");
		KatelloEnvironment env = new KatelloEnvironment(this.env, null, this.org, KatelloEnvironment.LIBRARY);
		env.cli_create();
		KatelloUtils.sshOnClient("wget "+KatelloGpgKey.REPO_GPG_FILE_ZOO+" -O /tmp/RPM-GPG-KEY-dummy-packages-generator");
		KatelloGpgKey gpg_key = new KatelloGpgKey(this.gpg_key, this.org, "/tmp/RPM-GPG-KEY-dummy-packages-generator");
		gpg_key.cli_create();
	}
	
	@Test(description="Create org, provider, product and repo", dependsOnMethods={"test_prepareEnvGpgKey"}, enabled=true)
	public void test_prepareRepo(){
		log.info("E2E - Create provider/product/repo");
		KatelloProvider prov = new KatelloProvider(this.provider,this.org, null, null);
		prov.create(); // create provider
		KatelloProduct prod = new KatelloProduct(this.product, this.org, this.provider, null, null, null, null, null);
		prod.create(); // create product
		KatelloRepo repo = new KatelloRepo(this.repo, this.org, this.product, REPO_INECAS_ZOO3, this.gpg_key, null);
		repo.create(); // create repo
		repo.assert_repoHasGpg();
	}
	
	@Test(description="Synchronize repository", dependsOnMethods={"test_prepareRepo"}, enabled=true)
	public void test_syncRepo(){
		log.info("E2E - Synchronize repo");
		KatelloRepo repo = new KatelloRepo(this.repo, this.org, this.product, REPO_INECAS_ZOO3, null, null);
		repo.synchronize();
	}
	
	@Test(description="Promote product content to the environment", dependsOnMethods={"test_syncRepo"}, enabled=true)
	public void test_promoteRepoToEnv(){
		SSHCommandResult res;
		
		log.info("E2E - Promote product to the environment");
		String changeset_dev = "cs_"+this.env;
		KatelloChangeset cs_dev = new KatelloChangeset(changeset_dev, this.org, this.env);
		cs_dev.create(); // create changeset
		cs_dev.update_addProduct(this.product); // add product
		res = cs_dev.promote(); // promote
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (changeset promote)");
	}
	
	@Test(description="Subscribe client system to the product", dependsOnMethods={"test_promoteRepoToEnv"}, enabled=true)
	public void test_subscribeClient(){
		SSHCommandResult res;
		this.system = "system-PackagesWithGPGKey-"+KatelloTestScript.getUniqueID();
		
		log.info("E2E - Subscribe client system");
		KatelloUtils.sshOnClient("subscription-manager clean");
		res = KatelloUtils.sshOnClient(String.format(
					"subscription-manager register --username admin --password admin" +
					" --org \"%s\" --environment \"%s\" --name \"%s\"",
					this.org, this.env, this.system));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (rhsm register)");
		
		KatelloOrg org = new KatelloOrg(this.org, null);
		String poolID = KatelloTasks.grepCLIOutput("Id",org.subscriptions().getStdout());
		res = KatelloUtils.sshOnClient(String.format("subscription-manager subscribe --pool=%s",poolID));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (rhsm subscribe)");
		Assert.assertTrue(getOutput(res).startsWith("Successfully"), 
				"Check - return message starts with word \"Successfully\" (rhsm subscribe)");
		Assert.assertTrue(getOutput(res).contains(poolID), 
				"Check - return message contains word \"Successfully\" (rhsm subscribe)");
	}
	
	@Test(description="Consume package installation, check gpgcheck flag", dependsOnMethods={"test_subscribeClient"}, enabled=true)
	public void test_installPackage(){
		SSHCommandResult res;
		
		log.info("E2E - check repo, install package");
		KatelloUtils.sshOnClient("yum repolist"); // refresh repos
		res = KatelloUtils.sshOnClient("cat /etc/yum.repos.d/redhat.repo");// out redhat.repo
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (out redhat.repo)");
		String REPO_STRUCT = String.format(".*name = %s.*enabled = 1.*gpgcheck = 1.*",this.repo);
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(REPO_STRUCT), "Check - redhat.repo content");
		res = KatelloUtils.sshOnClient("yum -y install wolf --disablerepo \\* --enablerepo *"+this.repo+"*");
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (yum install wolf)");
		res = KatelloUtils.sshOnClient("rpm -qi "+KatelloGpgKey.GPG_PUBKEY_RPM_ZOO);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (rpm info of gpg-key)");
	}
	
	@Test(description="Install package remotely", dependsOnMethods={"test_subscribeClient"}, enabled = true)
	public void test_installRemotePackage(){
		SSHCommandResult res;
		
		log.info("E2E - try remote package installation");
		KatelloSystem system = new KatelloSystem(this.system, this.org, null);
		res = system.packages_install("lion");
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (remote install lion)");
		Assert.assertTrue(getOutput(res).contains(KatelloSystem.OUT_REMOTE_ACTION_DONE),
				"Check - output string (remote action finished)");
		Assert.assertTrue(getOutput(res).contains("lion-"),
				"Check - output string (contains package name installed)");
		res = KatelloUtils.sshOnClient("rpm -q lion");
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (rpm -q lion)");
	}

	@AfterTest(description="unregister system", alwaysRun=true)
	public void tearDown(){
		log.info("E2E - RHSM unregister system");
		KatelloUtils.sshOnClient("subscription-manager unregister || true");
	}
	
}
