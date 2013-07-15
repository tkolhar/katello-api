package com.redhat.qe.katello.tests.e2e;

import org.testng.annotations.AfterClass;
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
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.common.TngRunGroups;

@Test(groups=TngRunGroups.TNG_KATELLO_Content, singleThreaded = true)
public class ConsumeCombineContent extends KatelloCliTestBase{

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

	@BeforeClass(description="Generate unique objects")
	public void setUp() {
		sshOnClient("yum erase -y fox cow dog dolphin duck walrus elephant horse kangaroo pike lion");

		condef = new KatelloContentDefinition(cli_worker, condef_name,null,base_org_name,null);
		exec_result = condef.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		exec_result = condef.add_product(base_zoo_product_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		exec_result = condef.add_repo(base_zoo_product_name, base_zoo_repo_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	}

	@Test(description="Consume content from combined filtered package and groups")
	public void test_consumecombinePackageandGroup() 
	{
		KatelloContentFilter filter = new KatelloContentFilter(cli_worker, packageGroup_filter, base_org_name, condef_name);
		exec_result = filter.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = filter.info();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = filter.add_repo(base_zoo_product_name, base_zoo_repo_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloContentFilter.OUT_ADD_REPO, base_zoo_repo_name, packageGroup_filter)), "Check output");
		exec_result = filter.add_rule(KatelloContentFilter.TYPE_INCLUDES, new FilterRulePackageGroups("mammals"));
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		FilterRulePackage [] exclude_packages = {
				new FilterRulePackage("elephant"),
				new FilterRulePackage("walrus"),
		};
		exec_result = filter.add_rule(KatelloContentFilter.TYPE_EXCLUDES, exclude_packages);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		condef.publish(pubview_name,pubview_name,null);
		conview = new KatelloContentView(cli_worker, pubview_name, base_org_name);
		exec_result = conview.promote_view(base_dev_env_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloContentView.OUT_PROMOTE, this.pubview_name, base_dev_env_name)), "Content view promote output.");
		act_key = new KatelloActivationKey(this.cli_worker, base_org_name,base_dev_env_name,act_key_name,"Act key created");
		exec_result = act_key.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");      
		exec_result = act_key.update_add_content_view(pubview_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");      
		
		/* 
		 * gkhachik - adding the poolid of zoo3 to the activation_key so 
		 * system would get automatically also subscribed to the pool. 
		 * Got this workflow from Brad B. and Justin S. 
		 * Confirm: it is working for me at katello version: 1.4.2-1.git.296.fb52d4c.el6
		 * 
		 */
		exec_result = act_key.update_add_subscription(base_zoo_repo_pool);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = act_key.info();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");      
		Assert.assertTrue(getOutput(exec_result).contains(this.pubview_name), "Content view name is in output.");
		Assert.assertTrue(getOutput(exec_result).contains(base_zoo_repo_pool), "Subscription is in output.");

		//register client, subscribe to pool
		rhsm_clean();
		sys = new KatelloSystem(this.cli_worker, system_name1, base_org_name, null);
		exec_result = sys.rhsm_registerForce(act_key_name);
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");

		yum_clean();    
		exec_result = sshOnClient("yum erase -y fox cow dog dolphin wolf elephant walrus");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		// consume packages from group mammals, verify that they are available
		install_Packages(cli_worker.getClientHostname(),new String[] {"fox", "cow", "dog", "dolphin", "wolf"});

		// consume packages from exclude filter, verify that they are NOT available
		exec_result = sshOnClient("yum install -y elephant walrus");
		Assert.assertFalse(exec_result.getExitCode().intValue()==0, "Check - return code");
	}
	
	@AfterClass(description="unregister the system, cleanup packages installed", alwaysRun=true)
	public void tearDown(){
		sshOnClient("yum erase -y fox cow dog dolphin duck walrus elephant horse kangaroo pike lion || true");
		rhsm_clean();// does RHSM unsubscribe --all; unregister; clean
	}

}
