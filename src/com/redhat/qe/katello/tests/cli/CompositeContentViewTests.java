package com.redhat.qe.katello.tests.cli;

import org.testng.annotations.Test;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloActivationKey;
import com.redhat.qe.katello.base.obj.KatelloChangeset;
import com.redhat.qe.katello.base.obj.KatelloContentDefinition;
import com.redhat.qe.katello.base.obj.KatelloContentView;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.common.TngRunGroups;

@Test(groups=TngRunGroups.TNG_KATELLO_Content)
public class CompositeContentViewTests extends KatelloCliTestBase{
	
	String uid = KatelloUtils.getUniqueID();
	String del_changeset_name = "del_changeset-" + uid;
	String condef_name1 = "condef1-" + uid;
	String condef_name2 = "condef2-" + uid;
	String condef_composite_name = "condefcomposite-" + uid;
	String pubview_name1_1 = "pubview1-1" + uid;
	String pubview_name1_2 = "pubview1-2" + uid;
	String pubview_name2_1 = "pubview2-1" + uid;
	String pubview_name2_2 = "pubview2-2" + uid;
	String pubcompview_name1 = "pubcompview1" + uid;
	String act_key_name2 = "act_key2" + uid;
	String system_name2 = "system2" + uid;

	KatelloChangeset del_changeset;
	KatelloContentDefinition condef1;
	KatelloContentDefinition condef2;
	KatelloContentDefinition compcondef;
	KatelloContentView compconview;
	KatelloContentView conview1;
	KatelloContentView conview2;
	KatelloActivationKey act_key2;
	KatelloSystem sys2;
	
	@Test(description="initialization here")
	public void init(){
		condef1 = new KatelloContentDefinition(cli_worker, condef_name1,null, base_org_name, null);
		exec_result = condef1.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = condef1.add_repo(base_zoo_product_name, base_zoo_repo_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	
				
		exec_result = condef1.publish(pubview_name1_1, pubview_name1_1, "Publish Content");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = condef1.publish(pubview_name1_2, pubview_name1_2, "Publish Content");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		condef2 = new KatelloContentDefinition(cli_worker, condef_name2,null,base_org_name,null);
		exec_result = condef2.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = condef2.add_repo(base_zoo4_product_name, base_zoo4_repo_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	
				
		exec_result = condef2.publish(pubview_name2_1, pubview_name2_1, "Publish Content");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = condef2.publish(pubview_name2_2, pubview_name2_2, "Publish Content");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		conview1 = new KatelloContentView(cli_worker, pubview_name1_2, base_org_name);
		exec_result = conview1.promote_view(base_dev_env_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		conview2 = new KatelloContentView(cli_worker, pubview_name2_2, base_org_name);
		exec_result = conview2.promote_view(base_dev_env_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
			
		KatelloUtils.sshOnClient(cli_worker.getClientHostname(),"sed -i -e \"s/certFrequency.*/certFrequency = 1/\" /etc/rhsm/rhsm.conf");
		KatelloUtils.sshOnClient(cli_worker.getClientHostname(),"service rhsmcertd restart");
		yum_clean(cli_worker.getClientHostname());
		KatelloUtils.sshOnClient(cli_worker.getClientHostname(),"service goferd restart;");
	}
	
	@Test(description="Create composite content view definition", dependsOnMethods={"init"})
	public void test_createComposite() {
		compcondef = new KatelloContentDefinition(cli_worker, condef_composite_name, null, base_org_name, null);
		exec_result = compcondef.create(true);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = compcondef.add_view(pubview_name1_2);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	
		
		exec_result = compcondef.add_view(pubview_name2_2);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	}
	
	@Test(description="Check adding old views into composite content view definition", dependsOnMethods={"test_createComposite"})
	public void test_checkOldViewsIntoComposite() {
		exec_result = compcondef.add_view(pubview_name1_1);
		Assert.assertTrue(exec_result.getExitCode() == 144, "Check - return code");	
		Assert.assertTrue(getOutput(exec_result).contains(KatelloContentDefinition.ERR_ADDVIEW), "Error in adding older view");
		
		exec_result = compcondef.add_view(pubview_name2_1);
		Assert.assertTrue(exec_result.getExitCode() == 144, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(KatelloContentDefinition.ERR_ADDVIEW), "Error in adding older view");
	}

	@Test(description="add/remove views into composite content view definition", dependsOnMethods={"test_checkOldViewsIntoComposite"})
	public void test_addRemoveViewsIntoComposite() {
		exec_result = compcondef.remove_view(pubview_name1_2);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = compcondef.info();
		Assert.assertFalse(getOutput(exec_result).contains(pubview_name1_2), "Not contains view");
		Assert.assertTrue(getOutput(exec_result).contains(pubview_name2_2), "Contains view");
		
		exec_result = compcondef.remove_view(pubview_name2_2);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = compcondef.info();
		Assert.assertFalse(getOutput(exec_result).contains(pubview_name1_2), "Not contains view");
		Assert.assertFalse(getOutput(exec_result).contains(pubview_name2_2), "Not contains view");
		
		exec_result = compcondef.remove_view(pubview_name2_2);
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (Cannot remove when not a component)");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloContentDefinition.ERR_NOT_A_COMPONENT, pubview_name2_2, compcondef.name)), "Check output (not a component)");

		exec_result = compcondef.add_view(pubview_name1_2);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	

		exec_result = compcondef.info();
		Assert.assertTrue(getOutput(exec_result).contains(pubview_name1_2), "Contains view");
		Assert.assertFalse(getOutput(exec_result).contains(pubview_name2_2), "Not contains view");
		
		exec_result = compcondef.add_view(pubview_name2_2);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = compcondef.info();
		Assert.assertTrue(getOutput(exec_result).contains(pubview_name1_2), "Contains view");
		Assert.assertTrue(getOutput(exec_result).contains(pubview_name2_2), "Contains view");
	}

	
	@Test(description="Consume content from composite content view definition", dependsOnMethods={"test_addRemoveViewsIntoComposite"})
	public void test_consumeCompositeContent() {
		// erase packages
		KatelloUtils.sshOnClient(cli_worker.getClientHostname(),"yum erase -y wolf lion crab walrus shark cheetah");
		
		exec_result = compcondef.publish(pubcompview_name1, pubcompview_name1, "Publish Content");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");		
		
		compconview = new KatelloContentView(cli_worker, pubcompview_name1, base_org_name);
		exec_result = compconview.promote_view(base_dev_env_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloContentView.OUT_PROMOTE, this.pubcompview_name1, base_dev_env_name)), "Content view promote output.");
		
		act_key2 = new KatelloActivationKey(cli_worker, base_org_name, base_dev_env_name, act_key_name2, "Act key2 created", null, pubcompview_name1);
		exec_result = act_key2.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	
		exec_result = act_key2.info();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	
		Assert.assertTrue(getOutput(exec_result).contains(this.pubcompview_name1), "Content view name is in output.");
		
		KatelloUtils.sshOnClient(cli_worker.getClientHostname(),KatelloSystem.RHSM_CLEAN);
		sys2 = new KatelloSystem(cli_worker, system_name2, base_org_name, null);
		exec_result = sys2.rhsm_registerForce(act_key_name2);
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		exec_result = sys2.subscribe(base_zoo_repo_pool);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = sys2.subscribe(base_pulp_repo_pool);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		exec_result = sys2.subscribe(base_zoo4_repo_pool);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		yum_clean(cli_worker.getClientHostname());
		
		//install packages from content view 1 and 2
		install_Packages(cli_worker.getClientHostname(),new String[] {"lion", "crab"});
		
		//package should not be available to install
		exec_result = KatelloUtils.sshOnClient(cli_worker.getClientHostname(),"yum install pulp-agent --disablerepo '*pulp*'");
		Assert.assertTrue(getOutput(exec_result).trim().contains("No package pulp-agent available."));
	}

	@Test(description = "part of promoted composite content view delete by changeset from environment, then repromote composite view, verify that packages are still availble",
			groups={"cfse-cli"}, dependsOnMethods={"test_consumeCompositeContent"})
	public void test_deletePromotedContentViewPart() {
		KatelloUtils.sshOnClient(cli_worker.getClientHostname(),"yum erase -y zebra");
		
		del_changeset = new KatelloChangeset(cli_worker, del_changeset_name, base_org_name, base_dev_env_name, true);
		exec_result = del_changeset.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = del_changeset.update_addView(pubview_name1_2);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");		
		exec_result = del_changeset.apply();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		install_Packages(cli_worker.getClientHostname(),new String[] {"zebra"});
	}

	@Test(description = "removed content view on previous scenario promote back by changeset to environment, verify that packages are availble",
			groups={"cfse-cli"}, dependsOnMethods={"test_deletePromotedContentViewPart"})
	public void test_RePromoteContentViewPart() {
		KatelloUtils.sshOnClient(cli_worker.getClientHostname(),"yum erase -y tiger");
		
		compconview = new KatelloContentView(cli_worker, pubview_name1_2, base_org_name);
		exec_result = compconview.promote_view(base_dev_env_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(
				KatelloContentView.OUT_PROMOTE, this.pubview_name1_2, base_dev_env_name)), 
				"Content view promote output.");
		install_Packages(cli_worker.getClientHostname(),new String [] {"tiger"});
	}	
}
