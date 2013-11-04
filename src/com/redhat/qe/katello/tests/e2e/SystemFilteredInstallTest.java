package com.redhat.qe.katello.tests.e2e;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloActivationKey;
import com.redhat.qe.katello.base.obj.KatelloContentDefinition;
import com.redhat.qe.katello.base.obj.KatelloContentFilter;
import com.redhat.qe.katello.base.obj.KatelloContentView;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.base.obj.helpers.FilterRulePackage;
import com.redhat.qe.katello.base.obj.helpers.FilterRulePackageGroups;
import com.redhat.qe.katello.base.tngext.TngPriority;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.common.TngRunGroups;

@TngPriority(20000)
@Test(groups=TngRunGroups.TNG_KATELLO_Content)
public class SystemFilteredInstallTest extends KatelloCliTestBase{
	String uid = KatelloUtils.getUniqueID();
	String condef_name = "condef-" + uid;
	String package_filter = "package_filter1";
	String packageGroup_filter = "packagegroup_filter1";
	String pubview_name = "pubview-" + uid;
	String system_name1 = "system-" + uid;
	String act_key_name = "act_key-" + uid;

	KatelloContentDefinition condef;
	KatelloContentView conview;
	KatelloActivationKey act_key;
	KatelloSystem sys;
	String system_uuid1;

	@BeforeClass(description="Generate unique objects")
	public void setUp() {
		condef = new KatelloContentDefinition(cli_worker, condef_name,null,base_org_name,null);
		exec_result = condef.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		exec_result = condef.add_product(base_zoo_product_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		exec_result = condef.add_repo(base_zoo_product_name, base_zoo_repo_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	}

	public void test_systemFilteredconsumePackageGroupContent() {

		KatelloContentFilter filter = new KatelloContentFilter(cli_worker, packageGroup_filter, base_org_name, condef_name);
		exec_result = filter.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = filter.info();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = filter.add_repo(base_zoo_product_name, base_zoo_repo_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloContentFilter.OUT_ADD_REPO, base_zoo_repo_name, packageGroup_filter)), "Check output");
		
		// add package rules there     
		FilterRulePackage [] include_packages = {
				new FilterRulePackage("trout"),
				new FilterRulePackage("penguin"),
				new FilterRulePackage("cheetah"),
				new FilterRulePackage("stork")
		};
		exec_result = filter.add_rule(KatelloContentFilter.TYPE_INCLUDES, include_packages);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = filter.add_rule(KatelloContentFilter.TYPE_INCLUDES, new FilterRulePackageGroups("mammals"));
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		condef.publish(pubview_name,pubview_name,null);
		conview = new KatelloContentView(cli_worker, pubview_name, base_org_name);
		exec_result = conview.promote_view(base_dev_env_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloContentView.OUT_PROMOTE, this.pubview_name, base_dev_env_name)), "Content view promote output.");
		act_key = new KatelloActivationKey(this.cli_worker, base_org_name,base_dev_env_name,act_key_name,"Act key created", null, pubview_name);
		exec_result = act_key.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");      
 
		exec_result = act_key.info();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");      
		Assert.assertTrue(getOutput(exec_result).contains(this.pubview_name), "Content view name is in output.");

		//register client, subscribe to pool
		rhsm_clean();
		sys = new KatelloSystem(this.cli_worker, system_name1, base_org_name, null);
		exec_result = sys.rhsm_registerForce(act_key_name);
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		exec_result = sys.rhsm_identity();
		system_uuid1 = KatelloUtils.grepCLIOutput("Current identity is", exec_result.getStdout());
		exec_result = sys.subscribe(base_zoo_repo_pool);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		KatelloUtils.sshOnClient(cli_worker.getClientHostname(), "subscription-manager refresh; service rhsmcertd restart");
		yum_clean();
		sshOnClient("yum erase -y lion zebra stork cockateel whale wolf trout");

		// consume packages from group mammals, verify that they are available
		exec_result=sys.packages_install_group("mammals");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		check_install_Package("lion");
		check_install_Package("zebra");

		// consume packages from group birds, verify that they are NOT available
		//@ TODO bz#1024929
		exec_result=sys.packages_install_group("birds");
		Assert.assertTrue(getOutput(exec_result).trim().contains("No new packages installed"));
		//Assert.assertTrue(getOutput(exec_result).trim().contains("No Group named birds exists"));
	}

	private void check_install_Package(String pkgName)
	{
		exec_result = sshOnClient("rpm -q "+ pkgName);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).trim().contains(pkgName+"-"));
	}


}
