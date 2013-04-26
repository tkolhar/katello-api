package com.redhat.qe.katello.tests.cli;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.obj.KatelloActivationKey;
import com.redhat.qe.katello.base.obj.KatelloChangeset;
import com.redhat.qe.katello.base.obj.KatelloContentDefinition;
import com.redhat.qe.katello.base.obj.KatelloContentView;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloErrata;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.base.obj.KatelloSystemGroup;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.common.TngRunGroups;
import com.redhat.qe.katello.tests.e2e.PromoteErrata;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups=TngRunGroups.TNG_KATELLO_Content)
public class ContentViewTests extends KatelloCliTestScript{
	
	public static final String ERRATA_ZOO_SEA = "RHEA-2012:0002";
	
	String uid = KatelloUtils.getUniqueID();
	String org_name = "orgcon-"+ uid;
	String env_name = "envcon-"+ uid;
	String prov_name = "provcon-" + uid;
	String prod_name = "prodcon-"+ uid;
	String repo_name = "repocon-" + uid;
	String changeset_name2 = "changecon2-" + uid;
	String condef_name = "condef-" + uid;
	String conview_name = "conview-" + uid;
	String pubview_name = "pubview-" + uid;
	String condef_name1 = "condef1-" + uid;
	String condef_name2 = "condef2-" + uid;
	String act_key_name = "act_key" + uid;
	String prod_name2 = "prodcon2-" + uid;
	String repo_name2 = "repo_name2-" + uid;
	String group_name = "group" + uid;
	String system_name1 = "system" + uid;
	String system_uuid1;
	
	SSHCommandResult exec_result;
	KatelloOrg org;
	KatelloEnvironment env;
	KatelloProvider prov;
	KatelloProduct prod;
	KatelloRepo repo;
	KatelloProduct prod2;
	KatelloRepo repo2;
	KatelloChangeset changeset2;
	KatelloContentDefinition condef;
	KatelloContentView conview;
	KatelloActivationKey act_key;
	KatelloSystem sys;
	KatelloSystemGroup group;
	
	@BeforeClass(description="Generate unique objects")
	public void setUp() {
		
		org = new KatelloOrg(org_name,null);
		exec_result = org.cli_create();		              
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		env = new KatelloEnvironment(env_name,null,org_name,KatelloEnvironment.LIBRARY);
		exec_result = env.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		prov = new KatelloProvider(prov_name,org_name,null,null);
		exec_result = prov.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		prod = new KatelloProduct(prod_name,org_name,prov_name,null, null, null,null, null);
		exec_result = prod.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		repo = new KatelloRepo(repo_name,org_name,prod_name,REPO_INECAS_ZOO3, null, null);
		exec_result = repo.create(true);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = prod.promote(env_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	
		
		prod2 = new KatelloProduct(prod_name2,org_name,prov_name,null, null, null,null, null);
		exec_result = prod2.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		repo2 = new KatelloRepo(repo_name2,org_name,prod_name2,PULP_RHEL6_x86_64_REPO, null, null);
		exec_result = repo2.create(true);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = repo2.synchronize();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = repo.synchronize();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		condef = new KatelloContentDefinition(condef_name,null,org_name,null);
		exec_result = condef.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = condef.add_product(prod_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	
		
		exec_result = condef.add_repo(prod_name, repo_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	
		
		exec_result = condef.publish(pubview_name,pubview_name,"Publish Content");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");		
	
		
		// The only way to install ERRATA on System is by system group
		group = new KatelloSystemGroup(group_name, this.org_name);
		exec_result = group.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		KatelloUtils.sshOnClient("sed -i -e \"s/certFrequency.*/certFrequency = 1/\" /etc/rhsm/rhsm.conf");
		KatelloUtils.sshOnClient("service rhsmcertd restart");
		yum_clean();
		KatelloUtils.sshOnClient("service goferd restart;");
	}

	@Test(description="promote content view to environment",groups={"cfse-cli"})
	public void test_promoteContentView() {
		conview = new KatelloContentView(pubview_name, org_name);
		exec_result = conview.promote_view(env_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloContentView.OUT_PROMOTE, this.pubview_name, env_name)), "Content view promote output.");
	}
	
	@Test(description = "Adding a published content view to an activation key",groups={"cfse-cli"}, dependsOnMethods={"test_promoteContentView"})
	public void test_addContentView() {
		
		act_key = new KatelloActivationKey(org_name,env_name,act_key_name,"Act key created");
		exec_result = act_key.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	
		exec_result = act_key.update_add_content_view(pubview_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	
		exec_result = act_key.info();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	
		Assert.assertTrue(getOutput(exec_result).contains(this.pubview_name), "Content view name is in output.");
	}
	
	@Test(description = "Remove a published content view to an activation key",groups={"cfse-cli"}, dependsOnMethods={"test_addContentView"})
	public void test_removeContentViewFromKey() {
		
		exec_result = act_key.update_remove_content_view();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	
		exec_result = act_key.info();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	
		Assert.assertFalse(getOutput(exec_result).contains(this.pubview_name), "Content view name is not in output.");
	}

	@Test(description = "Re Add a published content view to an activation key",groups={"cfse-cli"}, dependsOnMethods={"test_removeContentViewFromKey"})
	public void test_reAddContentViewToKey() {
		
		exec_result = act_key.update_add_content_view(pubview_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	
		exec_result = act_key.info();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	
		Assert.assertTrue(getOutput(exec_result).contains(this.pubview_name), "Content view name is in output.");
	}
	
	@Test(description = "register client via activation key",groups={"cfse-cli"}, dependsOnMethods={"test_reAddContentViewToKey"})
	public void test_registerClient(){
		KatelloUtils.sshOnClient(KatelloSystem.RHSM_CLEAN);
		sys = new KatelloSystem(system_name1, this.org_name, null);
		exec_result = sys.rhsm_registerForce(act_key_name);
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		
		exec_result = sys.rhsm_identity();
		system_uuid1 = KatelloCli.grepCLIOutput("Current identity is", exec_result.getStdout());
		
		exec_result = sys.subscriptions_available();
		String poolId1 = KatelloCli.grepCLIOutput("ID", getOutput(exec_result).trim(),1);
		Assert.assertNotNull(poolId1, "Check - pool Id is not null");
		
		String poolId2 = KatelloCli.grepCLIOutput("ID", getOutput(exec_result).trim(),2);
		Assert.assertNotNull(poolId2, "Check - pool Id is not null");
		
		exec_result = sys.subscribe(poolId1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = sys.subscribe(poolId2);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	}
	
	@Test(description = "Consuming content using an activation key that has a content view definition",groups={"cfse-cli"}, dependsOnMethods={"test_registerClient"})
	public void test_consumeContent()
	{
		yum_clean();
		KatelloUtils.sshOnClient("yum erase -y lion");
		exec_result=KatelloUtils.sshOnClient("yum install -y lion");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = KatelloUtils.sshOnClient("rpm -q lion");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).trim().contains("lion-"));
		
		// chack that packages from other repos not in content view are not available
		exec_result = KatelloUtils.sshOnClient("yum install pulp-agent --disablerepo '*pulp*'");
		Assert.assertTrue(getOutput(exec_result).trim().contains("No package pulp-agent available."));
	}
	
	//@ TODO bug 955706
	@Test(description = "consume Errata content",groups={"cfse-cli"}, dependsOnMethods={"test_consumeContent"})
	public void test_ConsumeErrata(){
		KatelloUtils.sshOnClient("yum erase -y walrus");
		exec_result = KatelloUtils.sshOnClient("yum install -y walrus-0.71-1.noarch");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		KatelloErrata ert = new KatelloErrata(ERRATA_ZOO_SEA, this.org_name, this.prod_name, this.repo_name, this.env_name);
		exec_result = ert.info();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code (errata info --environment Dev)");
		
		exec_result = group.add_systems(system_uuid1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = group.erratas_install(PromoteErrata.ERRATA_ZOO_SEA);
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).trim().contains("Remote action finished"));
		Assert.assertTrue(getOutput(exec_result).trim().contains("Erratum Install Complete"));
	}

	@Test(description = "promoted content view delete by changeset from environment, verify that packages are not availble anymore",groups={"cfse-cli"}, dependsOnMethods={"test_ConsumeErrata"})
	public void test_deletePromotedContentView() {
		KatelloUtils.sshOnClient("yum erase -y walrus");
		
		changeset2 = new KatelloChangeset(changeset_name2,org_name,env_name, true);
		exec_result = changeset2.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = changeset2.update_addView(pubview_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");		
		exec_result = changeset2.apply();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		yum_clean();
		exec_result = KatelloUtils.sshOnClient("yum install -y walrus");
		Assert.assertFalse(exec_result.getExitCode() == 0, "Check - return code");
	}

	//@ TODO bug 956690
	@Test(description = "removed content view on previous scenario promote back by changeset to environment, verify that packages are already availble",
			groups={"cfse-cli"}, dependsOnMethods={"test_deletePromotedContentView"})
	public void test_RePromoteContentView() {
		KatelloUtils.sshOnClient("yum erase -y walrus");
		
		exec_result = conview.promote_view(env_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloContentView.OUT_PROMOTE, this.pubview_name, env_name)), "Content view promote output.");
		yum_clean();
		exec_result = KatelloUtils.sshOnClient("yum install -y walrus");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - error code");
		exec_result = KatelloUtils.sshOnClient("rpm -q walrus");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).trim().contains("walrus-"));
	}
	
	@AfterClass
	public void tearDown() {
		exec_result = org.delete();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	}

}
