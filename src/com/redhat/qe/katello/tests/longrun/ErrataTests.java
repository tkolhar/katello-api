package com.redhat.qe.katello.tests.longrun;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliLongrunBase;
import com.redhat.qe.katello.base.obj.KatelloActivationKey;
import com.redhat.qe.katello.base.obj.KatelloContentFilter;
import com.redhat.qe.katello.base.obj.KatelloContentView;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloErrata;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.base.obj.KatelloSystemGroup;
import com.redhat.qe.katello.base.obj.helpers.FilterRuleErrataIds;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups={"cfse-cli"})
public class ErrataTests extends KatelloCliLongrunBase {
	
	private String ert1;
	private String content_view_promote;
	private String content_view_promote_errata;

	private SSHCommandResult exec_result;
	private String env_name;
	private String uid = KatelloUtils.getUniqueID();
	private String sys_name;
	private String sys_name2;
	private String sys_name3;
	private String group_name;
	private String group_name1;
	private String poolId1;
	private String system_uuid;
	
	private boolean repoSynced = false;

	@BeforeClass(description="init: create initial stuff")
	public void setUp(){
		this.base_org_name = "Awesome Org "+uid;
		this.env_name = "Dev-"+uid;
		this.sys_name = "testsystem-"+uid;
		this.sys_name2 = "testsystem2-"+uid;
		this.sys_name3 = "testsystem3-"+uid;
		this.group_name = "testgroup" + uid;
		this.group_name1 = "testgroup1" + uid;
		
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
		
		//@ TODO bug 1012480
		//exec_result = new KatelloOrg(this.cli_worker, base_org_name, null).subscriptions();
		//poolId1 = KatelloUtils.grepCLIOutput("ID", getOutput(exec_result).trim(),1);
	}

	
	@Test(description="promote rhel repo", dependsOnMethods={"test_syncRhel6"})
	public void test_promoteRHELRepo() {
		content_view_promote = KatelloUtils.promoteRepoToEnvironment(cli_worker, base_org_name, KatelloProduct.RHEL_SERVER, KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT, env_name);	
		
		configureClient("actkeywholerepopromote" + uid, content_view_promote, sys_name, env_name);
	}
	
	@Test(description="list RHEL repo erratas on content view", dependsOnMethods={"test_promoteRHELRepo"})
	public void test_listRHELErratas() {
		KatelloErrata err = new KatelloErrata(cli_worker, base_org_name, KatelloProduct.RHEL_SERVER, KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT, null);
		exec_result = err.custom_list_errata_names("RHBA");
		ert1 = getOutput(exec_result).replaceAll("\n", ",").split(",")[0];
		
		KatelloErrata errata = new KatelloErrata(cli_worker, base_org_name, KatelloProduct.RHEL_SERVER, KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT, content_view_promote);
		
		exec_result = errata.custom_list_errata_count("RHBA");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertFalse(getOutput(exec_result).replaceAll("\n", "").trim().equals("0"), "Check - erratas are not empty");
		
		exec_result = errata.custom_list_errata_count("RHSA");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertFalse(getOutput(exec_result).replaceAll("\n", "").trim().equals("0"), "Check - erratas are not empty");
		
		exec_result = errata.custom_list_errata_count("RHEA");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertFalse(getOutput(exec_result).replaceAll("\n", "").trim().equals("0"), "Check - erratas are not empty");
	}
	
	@Test(description="promote rhel errata", dependsOnMethods={"test_listRHELErratas"})
	public void test_promoteRHELErrata() {
		content_view_promote_errata = KatelloUtils.promoteErratasToEnvironment(cli_worker, base_org_name, KatelloProduct.RHEL_SERVER, KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT, new String[] {ert1}, env_name);	
		
		configureClient("actkeyerratapromote" + uid, content_view_promote_errata, sys_name2, env_name);
	}
	
	@Test(description="list rhel repo errata promoted to test environment", dependsOnMethods={"test_promoteRHELErrata"})
	public void test_listRHELRepoErrata() {
		KatelloErrata errata = new KatelloErrata(cli_worker, base_org_name, KatelloProduct.RHEL_SERVER, KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT, content_view_promote_errata);
		exec_result = errata.cli_list();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).trim().contains(ert1), "Errata " + ert1 + " is listed in environment errata list");
	}
	
	@Test(description="install errata", dependsOnMethods={"test_listRHELRepoErrata"})
	public void test_installRHELRepoErrata() {
		sshOnClient("yum clean all");
		exec_result = sshOnClient("yum repolist");
		
		KatelloSystemGroup group = new KatelloSystemGroup(this.cli_worker, group_name, base_org_name);
		exec_result = group.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = group.add_systems(system_uuid);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = group.erratas_install(ert1);
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).trim().contains("Remote action finished"));
		Assert.assertTrue(getOutput(exec_result).trim().contains("Erratum Install Complete"));
		
		sshOnClient("service rhsmcertd restart");
		try { Thread.sleep(3000); } catch (Exception ex) {}
		
		exec_result = group.list_erratas();
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertFalse(getOutput(exec_result).replaceAll("\n", "").contains(ert1), "Check - errata list output");
	}

	@Test(description="delete promoted rhel errata", dependsOnMethods={"test_installRHELRepoErrata"})
	public void test_deleteRHELErrata() {
		KatelloContentView view = new KatelloContentView(cli_worker, content_view_promote, base_org_name);		
		String def_name = KatelloUtils.grepCLIOutput("Definition", getOutput(view.view_info()).trim(),1);
		
		KatelloContentFilter filter = new KatelloContentFilter(cli_worker, "Filter"+uid, base_org_name, def_name);
		exec_result = filter.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = filter.add_repo(KatelloProduct.RHEL_SERVER, KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		FilterRuleErrataIds errata1 = new FilterRuleErrataIds(ert1);
		exec_result = filter.add_rule(KatelloContentFilter.TYPE_EXCLUDES, errata1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		exec_result = view.refresh_view();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = view.promote_view(env_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		configureClient("actkeyerrataremove" + uid, content_view_promote, sys_name3, env_name);
	}
	
	@Test(description="list rhel repo errata deleted to test environment", dependsOnMethods={"test_deleteRHELErrata"})
	public void test_listRHELRepoErrataDeleted() {
		KatelloErrata errata = new KatelloErrata(cli_worker, base_org_name, KatelloProduct.RHEL_SERVER, KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT, content_view_promote);
		exec_result = errata.cli_list();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertFalse(getOutput(exec_result).trim().contains(ert1), "Errata " + ert1 + " is not listed in environment errata list");
	}
	
	//@ TODO bug 970720
	@Test(description="install errata which was excluded by filter, verify that it fails", dependsOnMethods={"test_listRHELRepoErrataDeleted"})
	public void test_installRHELRepoExcludedErrata() {
		sshOnClient("yum clean all");
		exec_result = sshOnClient("yum repolist");
		
		KatelloSystemGroup group = new KatelloSystemGroup(this.cli_worker, group_name1, base_org_name);
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
		KatelloUtils.sshOnClient(null, "rm -rf /etc/sysconfig/rhn/systemid");
		
		KatelloActivationKey act_key = new KatelloActivationKey(this.cli_worker, base_org_name, envName, activationKey, "Act key created", null, contentView);
		exec_result = act_key.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");  
		// @ TODO bug 1012875
//		exec_result = act_key.update_add_subscription(poolId1);
//		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		KatelloSystem sys = new KatelloSystem(this.cli_worker, systemName, base_org_name, envName);
			exec_result = sys.rhsm_registerForce(activationKey); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		exec_result = sys.rhsm_identity();
		system_uuid = KatelloUtils.grepCLIOutput("Current identity is", exec_result.getStdout());
		
		//@ TODO bug 1012480 remove below section when bug is fixed
		poolId1 = KatelloUtils.grepCLIOutput("Pool ID",
				KatelloUtils.sshOnClient(null, "subscription-manager list --available --all | sed  -e 's/^ \\{1,\\}//'").getStdout().trim(),1);
		Assert.assertNotNull(poolId1);
		exec_result = sys.rhsm_subscribe(poolId1);
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		
		sshOnClient("sed -i -e \"s/certFrequency.*/certFrequency = 1/\" /etc/rhsm/rhsm.conf");
		sshOnClient("service rhsmcertd restart");
		yum_clean();
		sshOnClient("service goferd restart;");		
	}
}
