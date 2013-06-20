package com.redhat.qe.katello.tests.longrun;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliLongrunBase;
import com.redhat.qe.katello.base.obj.KatelloActivationKey;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloErrata;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloPackage;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.base.obj.KatelloSystemGroup;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups={"cfse-cli"})
public class PromotionTests extends KatelloCliLongrunBase {
	
	private String ert1;
	private String content_view_promote_package;
	private String content_view_remove_package;
	private String content_view_promote_errata;
	private String content_view_remove_errata;

	private SSHCommandResult exec_result;
	private String env_name;
	private String uid = KatelloUtils.getUniqueID();
	private String sys_name;
	private String group_name;
	private String group_name1;
	private String poolId1;
	private String system_uuid;
	
	private boolean repoSynced = false;

	@BeforeClass(description="init: create initial stuff")
	public void setUp(){
		String manifestZip = "manifest.zip";
		this.base_org_name = "Awesome Org "+uid;
		this.env_name = "Dev-"+uid;
		this.sys_name = "testsystem-"+uid;
		this.group_name = "testgroup" + uid;
		this.group_name1 = "testgroup1" + uid;
		
		if(!findSyncedRhelToUse()){
			exec_result = new KatelloOrg(base_org_name, null).cli_create();
			Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - exit.Code");
			KatelloUtils.scpOnClient("data/"+manifestZip, "/tmp");
			exec_result = new KatelloProvider(KatelloProvider.PROVIDER_REDHAT, base_org_name, null, null).import_manifest("/tmp/"+manifestZip, new Boolean(true));
			Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
			KatelloProduct prod=new KatelloProduct(KatelloProduct.RHEL_SERVER, base_org_name, KatelloProvider.PROVIDER_REDHAT, null, null, null,null, null);
			exec_result = prod.repository_set_enable(KatelloProduct.REPOSET_RHEL6_RPMS);
			Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code (repo set enable)");
			KatelloRepo repo = new KatelloRepo(KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT,base_org_name, KatelloProduct.RHEL_SERVER, null, null, null);
			exec_result = repo.enable();
			Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code (repo enable)");
			Assert.assertTrue(getOutput(exec_result).contains("enabled."),"Message - (repo enable)");
		}
		
		exec_result = new KatelloEnvironment(env_name, null, base_org_name, KatelloEnvironment.LIBRARY).cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - exit.Code");
	}

	@Test(description="Sync RHEL6Server content")
	public void test_syncRhel6(){
		// sync
		KatelloRepo repo = new KatelloRepo(KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT, base_org_name, KatelloProduct.RHEL_SERVER, null, null, null);

		exec_result = repo.status();
		this.repoSynced = !getOutput(exec_result).equals("Not synced");
		if(!repoSynced){
			exec_result = repo.synchronize();
			Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code (repo synchronize)");
		}
		exec_result = repo.info();
		Assert.assertFalse(KatelloUtils.grepCLIOutput("Package Count", getOutput(exec_result)).equals("0"), "Check - package count is NOT 0");
		
		exec_result = new KatelloOrg(base_org_name, null).subscriptions();
		poolId1 = KatelloUtils.grepCLIOutput("ID", getOutput(exec_result).trim(),1);
	}
	
	@Test(description="promote rhel packages", dependsOnMethods={"test_syncRhel6"})
	public void test_promoteRHELPackages() {
		content_view_promote_package = KatelloUtils.promotePackagesToEnvironment(base_org_name, KatelloProduct.RHEL_SERVER, KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT, new String[] {"zsh"}, env_name);

		configureClient("actkeypackagepromote" + uid, content_view_promote_package, sys_name, env_name);
	}
	
	@Test(description="list rhel repo packages promoted to test environment", dependsOnMethods={"test_promoteRHELPackages"})
	public void test_listRHELRepoPackages() {
		KatelloPackage pack = new KatelloPackage(base_org_name, KatelloProduct.RHEL_SERVER, KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT, content_view_promote_package);
		exec_result = pack.cli_search("*zsh*");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).trim().contains("zsh"), "Package zsh is listed in environment package list");
	}
	
	@Test(description="install zsh package", dependsOnMethods={"test_listRHELRepoPackages"})
	public void test_installRHELPackage() {
		KatelloUtils.sshOnClient("yum erase -y zsh");
		install_Packages(new String[] {"zsh"});
	}
	
	@Test(description="delete promoted rhel packages", dependsOnMethods={"test_installRHELPackage"})
	public void test_deleteRHELPackages() {
		content_view_remove_package = KatelloUtils.removePackagesFromEnvironment(base_org_name, KatelloProduct.RHEL_SERVER, KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT, new String[] {"zsh"}, env_name);
		
		configureClient("actkeypackageremove" + uid, content_view_remove_package, sys_name, env_name);
	}
	
	@Test(description="list rhel repo packages deleted to test environment", dependsOnMethods={"test_deleteRHELPackages"}, enabled=true)
	public void test_listRHELRepoPackagesDeleted() {
		KatelloPackage pack = new KatelloPackage(base_org_name, KatelloProduct.RHEL_SERVER, KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT, content_view_remove_package);
		exec_result = pack.cli_search("*zsh*");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertFalse(getOutput(exec_result).trim().contains("zsh"), "Package zsh is not listed in environment package list");
		
		KatelloUtils.sshOnClient("yum erase -y zsh");
		verify_PackagesNotAvailable(new String[] {"zsh"});
	}
	
	@Test(description="promote rhel errata", dependsOnMethods={"test_listRHELRepoPackagesDeleted"})
	public void test_promoteRHELErrata() {
		KatelloErrata err = new KatelloErrata(base_org_name, KatelloProduct.RHEL_SERVER, KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT, content_view_remove_package);
		exec_result = err.list_errata_names("RHBA");
		ert1 = getOutput(exec_result).replaceAll("\n", ",").split(",")[0];
	
		content_view_promote_errata = KatelloUtils.promoteErratasToEnvironment(base_org_name, KatelloProduct.RHEL_SERVER, KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT, new String[] {ert1}, env_name);	
		
		configureClient("actkeyerratapromote" + uid, content_view_promote_errata, sys_name, env_name);
	}
	
	@Test(description="list rhel repo errata promoted to test environment", dependsOnMethods={"test_promoteRHELErrata"})
	public void test_listRHELRepoErrata() {
		KatelloErrata errata = new KatelloErrata(base_org_name, KatelloProduct.RHEL_SERVER, KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT, content_view_promote_errata);
		exec_result = errata.cli_list();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).trim().contains(ert1), "Errata " + ert1 + " is listed in environment errata list");
	}
	
	@Test(description="install errata", dependsOnMethods={"test_listRHELRepoErrata"})
	public void test_installRHELRepoErrata() {
		KatelloUtils.sshOnClient("yum clean all");
		exec_result = KatelloUtils.sshOnClient("yum repolist");
		
		KatelloSystemGroup group = new KatelloSystemGroup(group_name, base_org_name);
		exec_result = group.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = group.add_systems(system_uuid);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = group.erratas_install(ert1);
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).trim().contains("Remote action finished"));
		Assert.assertTrue(getOutput(exec_result).trim().contains("Erratum Install Complete"));
		
		KatelloUtils.sshOnClient("service rhsmcertd restart");
		try { Thread.sleep(65000); } catch (Exception ex) {}
		
		exec_result = group.list_erratas();
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertFalse(getOutput(exec_result).replaceAll("\n", "").contains(ert1), "Check - errata list output");
	}

	@Test(description="delete promoted rhel errata", dependsOnMethods={"test_installRHELRepoErrata"})
	public void test_deleteRHELErrata() {
		content_view_remove_errata = KatelloUtils.removeErratasFromEnvironment(base_org_name, KatelloProduct.RHEL_SERVER, KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT, new String[] {ert1}, env_name);
		
		configureClient("actkeyerrataremove" + uid, content_view_remove_errata, sys_name, env_name);
	}
	
	@Test(description="list rhel repo errata deleted to test environment", dependsOnMethods={"test_deleteRHELErrata"})
	public void test_listRHELRepoErrataDeleted() {
		KatelloErrata errata = new KatelloErrata(base_org_name, KatelloProduct.RHEL_SERVER, KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT, content_view_remove_errata);
		exec_result = errata.cli_list();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertFalse(getOutput(exec_result).trim().contains(ert1), "Errata " + ert1 + " is not listed in environment errata list");
		
		KatelloErrata err = new KatelloErrata(base_org_name, KatelloProduct.RHEL_SERVER, KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT, content_view_remove_errata);
		exec_result = err.list_errata_count("RHBA");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertFalse(getOutput(exec_result).replaceAll("\n", "").trim().equals("0"), "Check - erratas are not empty");
		
		exec_result = err.list_errata_count("RHSA");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertFalse(getOutput(exec_result).replaceAll("\n", "").trim().equals("0"), "Check - erratas are not empty");
		
		exec_result = err.list_errata_count("RHEA");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertFalse(getOutput(exec_result).replaceAll("\n", "").trim().equals("0"), "Check - erratas are not empty");
	}
	
	@Test(description = "List the errata details on system", dependsOnMethods={"test_listRHELRepoErrataDeleted"})
	public void test_errataDetailsOnSystem() {
		
		KatelloErrata err = new KatelloErrata(base_org_name, KatelloProduct.RHEL_SERVER, KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT, content_view_remove_errata);
		exec_result = err.list_errata_details_count("RHBA");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertFalse(getOutput(exec_result).replaceAll("\n", "").trim().equals("0"), "Check - erratas are not empty");
		
		exec_result = err.list_errata_details_count("RHSA");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertFalse(getOutput(exec_result).replaceAll("\n", "").trim().equals("0"), "Check - erratas are not empty");
		
		exec_result = err.list_errata_details_count("RHEA");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertFalse(getOutput(exec_result).replaceAll("\n", "").trim().equals("0"), "Check - erratas are not empty");
	}
	
	@Test(description="install errata which was excluded by filter, verify that it fails", dependsOnMethods={"test_errataDetailsOnSystem"})
	public void test_installRHELRepoExcludedErrata() {
		KatelloUtils.sshOnClient("yum clean all");
		exec_result = KatelloUtils.sshOnClient("yum repolist");
		
		KatelloSystemGroup group = new KatelloSystemGroup(group_name1, base_org_name);
		exec_result = group.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = group.add_systems(system_uuid);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = group.erratas_install(ert1);
		Assert.assertFalse(exec_result.getExitCode().intValue()==0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).trim().contains("Remote action failed"));
	}

	private void configureClient(String activationKey, String contentView, String systemName, String envName) {
		rhsm_clean();
		
		KatelloActivationKey act_key = new KatelloActivationKey(base_org_name, envName, activationKey, "Act key created");
		exec_result = act_key.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");      
		exec_result = act_key.update_add_content_view(contentView);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");      
		exec_result = act_key.update_add_subscription(poolId1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		KatelloSystem sys = new KatelloSystem(systemName, base_org_name, envName);
			exec_result = sys.rhsm_registerForce(activationKey); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		exec_result = sys.rhsm_identity();
		system_uuid = KatelloUtils.grepCLIOutput("Current identity is", exec_result.getStdout());
		
		KatelloUtils.sshOnClient("sed -i -e \"s/certFrequency.*/certFrequency = 1/\" /etc/rhsm/rhsm.conf");
		KatelloUtils.sshOnClient("service rhsmcertd restart");
		yum_clean();
		KatelloUtils.sshOnClient("service goferd restart;");		
	}
}
