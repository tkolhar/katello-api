package com.redhat.qe.katello.tests.e2e;

import java.util.logging.Logger;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloGpgKey;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

/**
 * Scenario:<BR>
 * GPG Keys: End to end "If I sign it can I install it"?<BR>
 * @author gkhachik
 */
public class PackagesWithGPGKey extends KatelloCliTestBase{
	protected static Logger log = Logger.getLogger(PackagesWithGPGKey.class.getName());

	private String org;
	private String env = "Dev";
	private String provider;
	private String product;
	private String repo;
	private String gpg_key;
	private String system;
	
	private String contentView;
	
	@BeforeClass(description="Init unique names", alwaysRun=true)
	public void setUp(){
		String uniqueID = KatelloUtils.getUniqueID();
		this.org = "GPGOrg"+uniqueID;
		this.provider = "GPGProv"+uniqueID;
		this.product = "GPGProd"+uniqueID;
		this.repo = "GPGRepo_Zoo"+uniqueID;
		this.gpg_key = "gpg_zoo"+uniqueID;
		
		log.info("E2E - Cleanup GPG stuff");
		sshOnClient("yum -y erase wolf lion || true");
		sshOnClient("subscription-manager unregister || true");
		sshOnClient("rpm -e "+KatelloGpgKey.GPG_PUBKEY_RPM_ZOO+" || true");
		
		log.info("E2E - Create org");
		KatelloOrg org = new KatelloOrg(this.cli_worker, this.org, null);
		org.cli_create();
	}
	
	@Test(description="Create environment, gpg key", enabled=true)
	public void test_prepareEnvGpgKey(){
		log.info("E2E - Create environment/gpg key");
		KatelloEnvironment env = new KatelloEnvironment(this.cli_worker, this.env, null, this.org, KatelloEnvironment.LIBRARY);
		env.cli_create();
		sshOnClient("wget "+KatelloGpgKey.REPO_GPG_FILE_ZOO+" -O /tmp/RPM-GPG-KEY-dummy-packages-generator");
		KatelloGpgKey gpg_key = new KatelloGpgKey(cli_worker, this.gpg_key, this.org, "/tmp/RPM-GPG-KEY-dummy-packages-generator");
		gpg_key.cli_create();
	}
	
	@Test(description="Create org, provider, product and repo", dependsOnMethods={"test_prepareEnvGpgKey"}, enabled=true)
	public void test_prepareRepo(){
		log.info("E2E - Create provider/product/repo");
		KatelloProvider prov = new KatelloProvider(this.cli_worker, this.provider,this.org, null, null);
		prov.create(); // create provider
		KatelloProduct prod = new KatelloProduct(this.cli_worker, this.product, this.org, this.provider, null, null, null, null, null);
		prod.create(); // create product
		KatelloRepo repo = new KatelloRepo(this.cli_worker, this.repo, this.org, this.product, REPO_INECAS_ZOO3, this.gpg_key, null);
		repo.create(); // create repo
		repo.assert_repoHasGpg();
	}
	
	@Test(description="Synchronize repository", dependsOnMethods={"test_prepareRepo"}, enabled=true)
	public void test_syncRepo(){
		log.info("E2E - Synchronize repo");
		KatelloRepo repo = new KatelloRepo(this.cli_worker, this.repo, this.org, this.product, REPO_INECAS_ZOO3, null, null);
		repo.synchronize();
	}
	
	@Test(description="Promote product content to the environment", dependsOnMethods={"test_syncRepo"}, enabled=true)
	public void test_promoteRepoToEnv(){
		
		log.info("E2E - Promote product to the environment");		
		this.contentView = KatelloUtils.promoteProductToEnvironment(cli_worker, org, product, env);
	}
	
	@Test(description="Subscribe client system to the product", dependsOnMethods={"test_promoteRepoToEnv"}, enabled=true)
	public void test_subscribeClient(){
		SSHCommandResult res;
		this.system = "system-PackagesWithGPGKey-"+KatelloUtils.getUniqueID();
		rhsm_clean();
		
		log.info("E2E - Subscribe client system");
		KatelloSystem sys = new KatelloSystem(this.cli_worker, this.system, this.org, this.env+"/"+this.contentView);
		res = sys.rhsm_register();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (rhsm register)");
		
		KatelloOrg org = new KatelloOrg(this.cli_worker, this.org, null);
		String poolID = KatelloUtils.grepCLIOutput("ID",org.subscriptions().getStdout());
		String poolName = KatelloUtils.grepCLIOutput("Subscription",org.subscriptions().getStdout());
		res = sys.rhsm_subscribe(poolID);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (rhsm subscribe)");
		Assert.assertTrue(getOutput(res).startsWith("Successfully"), 
				"Check - return message starts with word \"Successfully\" (rhsm subscribe)");
		Assert.assertTrue(getOutput(res).contains(poolID) || getOutput(res).contains(poolName), 
				"Check - return message contains pool name "+ poolName);
		
		sshOnClient("service goferd restart;");
	}
	
	@Test(description="Consume package installation, check gpgcheck flag", dependsOnMethods={"test_subscribeClient"}, enabled=true)
	public void test_installPackage(){
		SSHCommandResult res;

		log.info("E2E - check repo, install package");
		sshOnClient("yum -y erase wolf lion || true");
		yum_clean();
		res = sshOnClient("cat /etc/yum.repos.d/redhat.repo");// out redhat.repo
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (out redhat.repo)");
		String REPO_STRUCT = String.format(".*name = %s.*enabled = 1.*gpgcheck = 1.*",this.repo);
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(REPO_STRUCT), "Check - redhat.repo content");
		res = sshOnClient("yes | yum install wolf");
		Assert.assertTrue(getOutput(res).contains("Refusing to automatically import keys when running unattended."),
				"Check - error string (GPG check)");
		res = sshOnClient("yum -y install wolf --disablerepo \\* --enablerepo *"+this.repo+"*");
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (yum install wolf)");
		res = sshOnClient("rpm -qi "+KatelloGpgKey.GPG_PUBKEY_RPM_ZOO);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (rpm info of gpg-key)");
	}
	
	@Test(description="Install package remotely", dependsOnMethods={"test_subscribeClient"}, enabled = true)
	public void test_installRemotePackage(){
		SSHCommandResult res;
		
		log.info("E2E - try remote package installation");
		sshOnClient("yum -y erase wolf lion || true");
		sshOnClient("rpm --import /tmp/RPM-GPG-KEY-dummy-packages-generator");
		KatelloSystem system = new KatelloSystem(this.cli_worker, this.system, this.org, null);
		res = system.packages_install("lion");
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (remote install lion)");
		Assert.assertTrue(getOutput(res).contains(KatelloSystem.OUT_REMOTE_ACTION_DONE),
				"Check - output string (remote action finished)");
		Assert.assertTrue(getOutput(res).contains("lion-"),
				"Check - output string (contains package name installed)");
		res = sshOnClient("rpm -q lion");
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (rpm -q lion)");
	}

	@AfterTest(description="unregister system", alwaysRun=true)
	public void tearDown(){
		log.info("E2E - RHSM unregister system");
		rhsm_clean();
	}
	
}
