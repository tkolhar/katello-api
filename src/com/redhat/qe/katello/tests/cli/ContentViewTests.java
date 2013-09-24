package com.redhat.qe.katello.tests.cli;

import org.testng.annotations.Test;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloActivationKey;
import com.redhat.qe.katello.base.obj.KatelloChangeset;
import com.redhat.qe.katello.base.obj.KatelloContentDefinition;
import com.redhat.qe.katello.base.obj.KatelloContentView;
import com.redhat.qe.katello.base.obj.KatelloErrata;
import com.redhat.qe.katello.base.obj.KatelloPackage;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.base.obj.KatelloSystemGroup;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.common.TngRunGroups;
import com.redhat.qe.katello.tests.e2e.PromoteErrata;

@Test(groups=TngRunGroups.TNG_KATELLO_Content)
public class ContentViewTests extends KatelloCliTestBase{
	
	public static final String ERRATA_ZOO_SEA = "RHEA-2012:0002";
	
	String uid = KatelloUtils.getUniqueID();
	String changeset_name2 = "changecon2-" + uid;
	String condef_name = "condef-" + uid;
	String conview_name = "conview-" + uid;
	String pubview_name = "pubview-" + uid;
	String view_delete = "viewdelete-" + uid;
	String view_refresh = "viewrefresh-" + uid;
	String condef_name1 = "condef1-" + uid;
	String condef_name2 = "condef2-" + uid;
	String act_key_name = "act_key" + uid;
	String group_name = "group" + uid;
	String system_name1 = "system" + uid;
	String system_uuid1;
	
	KatelloChangeset changeset2;
	KatelloContentDefinition condef;
	KatelloContentView conview;
	KatelloActivationKey act_key;
	KatelloSystem sys;
	KatelloSystemGroup group;
	
	@Test(description="initialization goes here")
	public void init(){		
		condef = new KatelloContentDefinition(cli_worker, condef_name,null,base_org_name,null);
		exec_result = condef.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		exec_result = condef.publish(view_refresh, view_refresh, "view to be refreshed");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		exec_result = condef.add_repo(base_zoo_product_name, base_zoo_repo_name);

		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	
		
		exec_result = condef.publish(pubview_name,pubview_name,"Publish Content");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");		
	
		exec_result = condef.publish(view_delete, view_delete, "view to be deleted");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		// The only way to install ERRATA on System is by system group
		group = new KatelloSystemGroup(this.cli_worker, group_name, base_org_name);
		exec_result = group.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		sshOnClient("sed -i -e \"s/certFrequency.*/certFrequency = 1/\" /etc/rhsm/rhsm.conf");
		sshOnClient("service rhsmcertd restart");
		yum_clean();
		sshOnClient("service goferd restart;");
	}

	@Test(description="promote content view to environment",groups={"cfse-cli"}, dependsOnMethods={"init"})
	public void test_promoteContentView() {
		conview = new KatelloContentView(cli_worker, pubview_name, base_org_name);
		exec_result = conview.promote_view(base_dev_env_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloContentView.OUT_PROMOTE, this.pubview_name, base_dev_env_name)), "Content view promote output.");
	}
	
	@Test(description = "Adding a published content view to an activation key",groups={"cfse-cli"}, dependsOnMethods={"test_promoteContentView"})
	public void test_addContentView() {
		
		act_key = new KatelloActivationKey(this.cli_worker, base_org_name,base_dev_env_name,act_key_name,"Act key created", null, pubview_name);
		exec_result = act_key.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");		
		exec_result = act_key.info();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	
		Assert.assertTrue(getOutput(exec_result).contains(this.pubview_name), "Content view name is in output.");
	}
	
	@Test(description = "register client via activation key",groups={"cfse-cli"}, dependsOnMethods={"test_addContentView"})
	public void test_registerClient(){
		sshOnClient(KatelloSystem.RHSM_CLEAN);
		sys = new KatelloSystem(this.cli_worker, system_name1, base_org_name, null);
		exec_result = sys.rhsm_registerForce(act_key_name);
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		
		exec_result = sys.rhsm_identity();
		system_uuid1 = KatelloUtils.grepCLIOutput("Current identity is", exec_result.getStdout());
		
		exec_result = sys.subscribe(base_zoo_repo_pool);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = sys.subscribe(base_pulp_repo_pool);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	}
	
	@Test(description = "List the packages of content view",groups={"cfse-cli"}, dependsOnMethods={"test_registerClient"})
	public void test_packageList() {
		KatelloPackage pack = new KatelloPackage(cli_worker, base_org_name, base_zoo_product_name, base_zoo_repo_name, pubview_name);
		
		exec_result = pack.cli_list();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		Assert.assertTrue(getOutput(exec_result).contains("lion"),"is package in the list: zebra");
		Assert.assertTrue(getOutput(exec_result).contains("wolf"),"is package in the list: wolf");
		Assert.assertTrue(getOutput(exec_result).contains("zebra"),"is package in the list: zebra");
	}
	
	@Test(description = "Consuming content using an activation key that has a content view definition",groups={"cfse-cli"}, dependsOnMethods={"test_packageList"})
	public void test_consumeContent()
	{
		sshOnClient("yum erase -y lion");
		yum_clean();
		install_Packages(cli_worker.getClientHostname(), new String[] {"lion"});
		
		// chack that packages from other repos not in content view are not available
		exec_result = sshOnClient("yum install pulp-agent --disablerepo '*pulp*'");
		Assert.assertTrue(getOutput(exec_result).trim().contains("No package pulp-agent available."));
	}
	
	@Test(description = "List the erratas of content view",groups={"cfse-cli"}, dependsOnMethods={"test_consumeContent"})
	public void test_errataList() {
		KatelloErrata errata = new KatelloErrata(cli_worker, base_org_name, base_zoo_product_name, base_zoo_repo_name, pubview_name);
		
		exec_result = errata.cli_list();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		Assert.assertTrue(getOutput(exec_result).contains(ERRATA_ZOO_SEA),"is package in the list: " + ERRATA_ZOO_SEA);
	}
	
	@Test(description = "consume Errata content",groups={"cfse-cli"}, dependsOnMethods={"test_errataList"})
	public void test_ConsumeErrata(){
		sshOnClient("yum erase -y walrus");
		exec_result = sshOnClient("yum install -y walrus-0.71-1.noarch");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		KatelloErrata ert = new KatelloErrata(cli_worker, ERRATA_ZOO_SEA, base_org_name, base_zoo_product_name, base_zoo_repo_name, null);
		ert.content_view = pubview_name;
		exec_result = ert.info();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code (errata info --environment Dev)");
		
		exec_result = group.add_systems(system_uuid1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = group.erratas_install(PromoteErrata.ERRATA_ZOO_SEA);
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).trim().contains("Remote action finished"));
		Assert.assertTrue(getOutput(exec_result).trim().contains("Erratum Install Complete"));
	}

	@Test(description = "promoted content view delete by changeset from environment, " +
			"verify that packages are not availble anymore",groups={"cfse-cli"}, dependsOnMethods={"test_ConsumeErrata"})
	public void test_deletePromotedContentView() {
		sshOnClient("yum erase -y walrus");
		rhsm_clean();
		
		changeset2 = new KatelloChangeset(cli_worker, changeset_name2,base_org_name,base_dev_env_name, true);
		exec_result = changeset2.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = changeset2.update_addView(pubview_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");		
		exec_result = changeset2.apply();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		yum_clean();
		
		verify_PackagesNotAvailable(cli_worker.getClientHostname(), new String[] {"walrus"});
	}

	//@ TODO bug 956690
	@Test(description = "removed content view on previous scenario promote back by changeset to environment, verify that packages are already availble",
			groups={"cfse-cli"}, dependsOnMethods={"test_deletePromotedContentView"})
	public void test_RePromoteContentView() {
		sshOnClient("yum erase -y walrus");
		
		exec_result = conview.promote_view(base_dev_env_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloContentView.OUT_PROMOTE, this.pubview_name, base_dev_env_name)), "Content view promote output.");
		
		sys = new KatelloSystem(this.cli_worker, system_name1, base_org_name, null);
		exec_result = sys.rhsm_registerForce(act_key_name);
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
	
		exec_result = sys.subscribe(base_zoo_repo_pool);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = sys.subscribe(base_pulp_repo_pool);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		KatelloUtils.sshOnClient(cli_worker.getClientHostname(), "subscription-manager refresh; service rhsmcertd restart");
		yum_clean();
		install_Packages(cli_worker.getClientHostname(), new String[] {"walrus"});
	}
	
	@Test(description = "delete content view. Check that it is removed from the content definition as well", dependsOnMethods={"init"})
	public void test_deleteContentView() {
		KatelloContentView view = new KatelloContentView(cli_worker, view_delete, base_org_name);
		exec_result = view.delete_view();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check exit code (delete view)");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloContentView.OUT_DELETE, view_delete)), "Check output (delete view)");
		exec_result = condef.info();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check exit code (info)");
		String viewsList = KatelloUtils.grepCLIOutput("Published Views", getOutput(exec_result).trim());
		Assert.assertFalse(viewsList.contains(view_delete), "Check view name note present");
	}

	@Test(description="refresh content view and check new content", dependsOnMethods={"init"})
	public void test_refreshContentView() {
		KatelloContentView view = new KatelloContentView(cli_worker, view_refresh, base_org_name);
		KatelloPackage pkg = new KatelloPackage(cli_worker, base_org_name, base_zoo_product_name, base_zoo_repo_name, view_refresh);
		exec_result = view.refresh_view();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check exit code (refresh view)");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloContentView.OUT_REFRESH, view_refresh)), "Check output (refresh view)");
		exec_result = pkg.cli_list();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code");
	}

	@Test(description="try to delete promoted content view", dependsOnMethods={"init"})
	public void test_deletePromotedView() {
		String view_name = "view-"+KatelloUtils.getUniqueID();
		exec_result = new KatelloContentDefinition(cli_worker, condef_name, null, base_org_name, null).publish(view_name, null, null);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check exit code (definition publish)");
		KatelloContentView view = new KatelloContentView(cli_worker, view_name, base_org_name);
		exec_result = view.promote_view(base_dev_env_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check exit code (promote view)");
		exec_result = view.delete_view();
		Assert.assertTrue(exec_result.getExitCode() == 65, "Check exit code (promote view)");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloContentView.ERR_CANNOT_DELETE, view_name)), "Check output (refresh view)");
	}

	@Test(description="refresh and promote view asynchronously", dependsOnMethods={"init"})
	public void test_promoteRefreshAsync() {
		String view_name = "view-1"+KatelloUtils.getUniqueID();
		exec_result = new KatelloContentDefinition(cli_worker, condef_name, null, base_org_name, null).publish(view_name, null, null);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check exit code (definition publish)");
		KatelloContentView view = new KatelloContentView(cli_worker, view_name, base_org_name);
		exec_result = view.promote_async(base_dev_env_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check exit code (view promote)");
		Assert.assertTrue(getOutput(exec_result).matches(KatelloContentView.OUT_REG_PROMOTE_ASYNC), "Check output (promote view)");
		exec_result = view.refresh_async();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check exit code (view refresh)");
		Assert.assertTrue(getOutput(exec_result).matches(KatelloContentView.OUT_REG_REFRESH_ASYNC), "Check output (refresh view)");
	}

	@Test(description="content view not found - check error")
	public void test_viewNotFound() {
		String view_name = "bad view" + uid;
		KatelloContentView view = new KatelloContentView(cli_worker, view_name, base_org_name);
		exec_result = view.view_info();
		Assert.assertTrue(exec_result.getExitCode()==65, "Check exit code (view not found)");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloContentView.ERR_NOT_FOUND, view_name, base_org_name)), "Check error (view not found)");
	}
}
