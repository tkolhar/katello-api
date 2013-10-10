package com.redhat.qe.katello.tests.longrun;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliLongrunBase;
import com.redhat.qe.katello.base.obj.KatelloActivationKey;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloPackage;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups={"cfse-cli"})
public class PackageTests extends KatelloCliLongrunBase {
	
	private String content_view_promote_package;
	private String content_view_remove_package;
	private SSHCommandResult exec_result;
	private String env_name;
	private String uid = KatelloUtils.getUniqueID();
	private String sys_name;
	private String sys_name2;
	private String poolId1;
	
	private boolean repoSynced = false;

	@BeforeClass(description="init: create initial stuff")
	public void setUp(){
		this.base_org_name = "Awesome Org "+uid;
		this.env_name = "Dev-"+uid;
		this.sys_name = "testsystem-"+uid;
		this.sys_name2 = "testsystem2-"+uid;
		
		if(!findSyncedRhelToUse()){
			exec_result = new KatelloOrg(this.cli_worker, base_org_name, null).cli_create();
			Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - exit.Code");
			KatelloUtils.scpOnClient(cli_worker.getClientHostname(), "data/"+KatelloProvider.MANIFEST_12SUBSCRIPTIONS, "/tmp");
			exec_result = new KatelloProvider(this.cli_worker, KatelloProvider.PROVIDER_REDHAT, base_org_name, null, null).import_manifest("/tmp/"+KatelloProvider.MANIFEST_12SUBSCRIPTIONS, new Boolean(true));
			Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
			KatelloProduct prod=new KatelloProduct(this.cli_worker, KatelloProduct.RHEL_SERVER, base_org_name, KatelloProvider.PROVIDER_REDHAT, null, null, null,null, null);
			exec_result = prod.repository_set_enable(KatelloProduct.REPOSET_RHEL6_RPMS);
			Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code (repo set enable)");
			KatelloRepo repo = new KatelloRepo(this.cli_worker, KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT,base_org_name, KatelloProduct.RHEL_SERVER, null, null, null);
			exec_result = repo.enable();
			Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code (repo enable)");
			Assert.assertTrue(getOutput(exec_result).contains("enabled."),"Message - (repo enable)");
		}
		
		exec_result = new KatelloEnvironment(this.cli_worker, env_name, null, base_org_name, KatelloEnvironment.LIBRARY).cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - exit.Code");
	}

	@Test(description="Sync RHEL6Server content")
	public void test_syncRhel6(){
		// sync
		KatelloRepo repo = new KatelloRepo(this.cli_worker, KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT, base_org_name, KatelloProduct.RHEL_SERVER, null, null, null);

		exec_result = repo.status();
		this.repoSynced = !getOutput(exec_result).equals("Not synced");
		if(!repoSynced){
			exec_result = repo.synchronize();
			Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code (repo synchronize)");
		}
		exec_result = repo.info();
		Assert.assertFalse(KatelloUtils.grepCLIOutput("Package Count", getOutput(exec_result)).equals("0"), "Check - package count is NOT 0");
		
		//@ TODO bz#1012480
		//exec_result = new KatelloOrg(this.cli_worker, base_org_name, null).subscriptions();
		//poolId1 = KatelloUtils.grepCLIOutput("ID", getOutput(exec_result).trim(),1);
	}
	
	@Test(description="promote rhel packages", dependsOnMethods={"test_syncRhel6"})
	public void test_promoteRHELPackages() {
		content_view_promote_package = KatelloUtils.promotePackagesToEnvironment(cli_worker, base_org_name, KatelloProduct.RHEL_SERVER, KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT, new String[] {"zsh"}, env_name);

		configureClient("actkeypackagepromote" + uid, content_view_promote_package, sys_name, env_name);
	}
	
	@Test(description="list rhel repo packages promoted to test environment", dependsOnMethods={"test_promoteRHELPackages"})
	public void test_listRHELRepoPackages() {
		KatelloPackage pack = new KatelloPackage(cli_worker, base_org_name, KatelloProduct.RHEL_SERVER, KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT, content_view_promote_package);
		exec_result = pack.cli_search("*zsh*");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).trim().contains("zsh"), "Package zsh is listed in environment package list");
	}
	
	@Test(description="install zsh package", dependsOnMethods={"test_listRHELRepoPackages"})
	public void test_installRHELPackage() {
		sshOnClient("yum erase -y zsh");
		install_Packages(cli_worker.getClientHostname(), new String[] {"zsh"});
	}
	
	@Test(description="delete promoted rhel packages", dependsOnMethods={"test_installRHELPackage"})
	public void test_deleteRHELPackages() {
		content_view_remove_package = KatelloUtils.removePackagesFromEnvironment(cli_worker, base_org_name, KatelloProduct.RHEL_SERVER, KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT, new String[] {"zsh"}, env_name);
		
		configureClient("actkeypackageremove" + uid, content_view_remove_package, sys_name2, env_name);
	}
	
	@Test(description="list rhel repo packages deleted to test environment", dependsOnMethods={"test_deleteRHELPackages"}, enabled=true)
	public void test_listRHELRepoPackagesDeleted() {
		KatelloPackage pack = new KatelloPackage(cli_worker, base_org_name, KatelloProduct.RHEL_SERVER, KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT, content_view_remove_package);
		exec_result = pack.cli_search("*zsh*");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertFalse(getOutput(exec_result).trim().contains("zsh"), "Package zsh is not listed in environment package list");
		
		sshOnClient("yum erase -y zsh");
		verify_PackagesNotAvailable(cli_worker.getClientHostname(), new String[] {"zsh"});
	}

	private void configureClient(String activationKey, String contentView, String systemName, String envName) {
		rhsm_clean();
		KatelloUtils.sshOnClient(null, "rm -rf /etc/sysconfig/rhn/systemid");
		
		KatelloActivationKey act_key = new KatelloActivationKey(this.cli_worker, base_org_name, envName, activationKey, "Act key created", null, contentView);
		exec_result = act_key.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");      
		// @ TODO bz#1012875
//		exec_result = act_key.update_add_subscription(poolId1);
//		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		KatelloSystem sys = new KatelloSystem(this.cli_worker, systemName, base_org_name, envName);
			exec_result = sys.rhsm_registerForce(activationKey); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		
		//@ TODO bz#1012480
		poolId1 = KatelloUtils.grepCLIOutput("Pool ID",
				KatelloUtils.sshOnClient(null, "subscription-manager list --available --all | sed  -e 's/^ \\{1,\\}//'").getStdout().trim(),1);
		Assert.assertNotNull(poolId1);
		exec_result = sys.rhsm_subscribe(poolId1);
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		
		exec_result = sys.rhsm_identity();
		
		sshOnClient("sed -i -e \"s/certFrequency.*/certFrequency = 1/\" /etc/rhsm/rhsm.conf");
		sshOnClient("service rhsmcertd restart");
		yum_clean();
		sshOnClient("service goferd restart;");		
	}
}
