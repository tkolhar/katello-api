package com.redhat.qe.katello.tests.e2e;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloActivationKey;
import com.redhat.qe.katello.base.obj.KatelloContentDefinition;
import com.redhat.qe.katello.base.obj.KatelloContentFilter;
import com.redhat.qe.katello.base.obj.KatelloContentView;
import com.redhat.qe.katello.base.obj.KatelloPackage;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.base.obj.helpers.FilterRulePackage;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.common.TngRunGroups;

@Test(groups=TngRunGroups.TNG_KATELLO_Content, singleThreaded = true)
public class ConsumeFilteredPackage extends KatelloCliTestBase {
	
	String uid = KatelloUtils.getUniqueID();
	String condef_name = "condef-" + uid;
	String package_filter = "package_filter1";
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
		sshOnClient("yum erase -y fox cow dog dolphin duck walrus elephant horse kangaroo pike lion --disablerepo \\* || true");
		
		condef = new KatelloContentDefinition(cli_worker, condef_name,null,base_org_name,null);
		exec_result = condef.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		exec_result = condef.add_product(base_zoo_product_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = condef.add_repo(base_zoo_product_name, base_zoo_repo_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	}
    
	@Test(description="Consume content from filtered package")
	public void test_consumePackageContent() {

		KatelloContentFilter filter = new KatelloContentFilter(cli_worker, package_filter, base_org_name, condef_name);

		exec_result = filter.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		exec_result = filter.info();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = filter.add_repo(base_zoo_product_name, base_zoo_repo_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloContentFilter.OUT_ADD_REPO, base_zoo_repo_name, package_filter)), "Check output");

		// add package rules there     
		FilterRulePackage [] include_packages = {
				new FilterRulePackage("fox"),
				new FilterRulePackage("tiger"),
				new FilterRulePackage("lion"),
				new FilterRulePackage("bear"),
				new FilterRulePackage("whale"),
				new FilterRulePackage("wolf"),
				new FilterRulePackage("shark"),
				new FilterRulePackage("stork"),
				new FilterRulePackage("cockateel"),
				new FilterRulePackage("cow", "2.2", null, null),
				new FilterRulePackage("walrus", "0.71", null, null),
				new FilterRulePackage("dog", null, "4.20", null),
				new FilterRulePackage("dolphin", null, null, "3.11"),
				new FilterRulePackage("duck", null, "0.5", "0.7")
		};

		exec_result = filter.add_rule(KatelloContentFilter.TYPE_INCLUDES, include_packages);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		FilterRulePackage [] exclude_packages = {
				new FilterRulePackage("elephant"),
				new FilterRulePackage("walrus", "5.21", null, null),
				new FilterRulePackage("horse", null, "0.21", null),
				new FilterRulePackage("kangaroo", null, null, "0.3"),
				new FilterRulePackage("pike", null, "2.1", "2.3"),
		};

		exec_result = filter.add_rule(KatelloContentFilter.TYPE_EXCLUDES, exclude_packages);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		exec_result = condef.publish(pubview_name,pubview_name,null);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		conview = new KatelloContentView(cli_worker, pubview_name, base_org_name);
		exec_result = conview.promote_view(base_dev_env_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloContentView.OUT_PROMOTE, this.pubview_name, base_dev_env_name)), "Content view promote output.");

		act_key = new KatelloActivationKey(this.cli_worker, base_org_name,base_dev_env_name,act_key_name,"Act key created", null, pubview_name);
		exec_result = act_key.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");      
     
		exec_result = act_key.info();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");      
		Assert.assertTrue(getOutput(exec_result).contains(pubview_name), "Content view name is in output.");

		//register client, subscribe to pool
		sshOnClient(KatelloSystem.RHSM_CLEAN);
		sys = new KatelloSystem(this.cli_worker, system_name1, base_org_name, null);
		exec_result = sys.rhsm_registerForce(act_key_name);
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");

		exec_result = sys.rhsm_identity();
		system_uuid1 = KatelloUtils.grepCLIOutput("Current identity is", exec_result.getStdout());


		exec_result = sys.subscribe(base_zoo_repo_pool);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		KatelloUtils.sshOnClient(cli_worker.getClientHostname(), "subscription-manager refresh; service rhsmcertd restart");
		yum_clean();

		KatelloPackage pack = new KatelloPackage(cli_worker, base_org_name, base_zoo_product_name, base_zoo_repo_name, pubview_name);
		exec_result = pack.cli_list();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).trim().contains("fox"), "check package fox exists");
		Assert.assertTrue(getOutput(exec_result).trim().contains("cow-2.2-3"), "check package cow-2.2-3 exists");
		Assert.assertTrue(getOutput(exec_result).trim().contains("dog-4.23-1"), "check package dog-4.23-1 exists");
		Assert.assertTrue(getOutput(exec_result).trim().contains("dolphin-3.10.232-1"), "check package dolphin-3.10.232-1 exists");
		Assert.assertTrue(getOutput(exec_result).trim().contains("duck-0.6-1"), "check package duck-0.6-1 exists");
		Assert.assertTrue(getOutput(exec_result).trim().contains("walrus-0.71-1"), "check package walrus-0.71-1 exists");
		
		Assert.assertFalse(getOutput(exec_result).trim().contains("elephant-8.3-1"), "check package does not elephant-8.3-1 exists");
		Assert.assertFalse(getOutput(exec_result).trim().contains("walrus-5.21-1"), "check package does not walrus-5.21-1 exists");
		Assert.assertFalse(getOutput(exec_result).trim().contains("horse-0.22-2"), "check package does not horse-0.22-2 exists");
		Assert.assertFalse(getOutput(exec_result).trim().contains("kangaroo-0.2-1"), "check package does not kangaroo-0.2-1 exists");
		Assert.assertFalse(getOutput(exec_result).trim().contains("pike-2.2-1"), "check package does not pike-2.2-1 exists");

		// consume packages from include filter, verify that they are available
		install_Packages(cli_worker.getClientHostname(), new String[] {"fox", "cow-2.2-3", "dog-4.23-1", "dolphin-3.10.232-1", "duck-0.6-1", "walrus-0.71-1"});

		// consume packages from exclude filter, verify that they are NOT available
		verify_PackagesNotAvailable(cli_worker.getClientHostname(), new String[] {"elephant-8.3-1", "walrus-5.21-1", "horse-0.22-2", "kangaroo-0.2-1", "pike-2.2-1"});
	}
}
