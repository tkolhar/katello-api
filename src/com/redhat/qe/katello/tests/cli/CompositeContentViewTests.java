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
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.common.TngRunGroups;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups=TngRunGroups.TNG_KATELLO_Content)
public class CompositeContentViewTests extends KatelloCliTestScript{
	
	String uid = KatelloUtils.getUniqueID();
	String org_name2 = "orgcon2-"+ uid;
	String env_name2 = "envcon2-"+ uid;
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
	String prov_local1_name = "provlocal1-" + uid;
	String prod_local1_name = "prodlocal1-"+ uid;
	String repo_local1_name = "repolocal1-" + uid;
	String prov_local2_name = "provlocal2-" + uid;
	String prod_local2_name = "prodlocal2-"+ uid;
	String repo_local2_name = "repolocal2-" + uid;
	String prov_local3_name = "provlocal3-" + uid;
	String prod_local3_name = "prodlocal3-"+ uid;
	String repo_local3_name = "repolocal3-" + uid;
	String repo_path1;
	String repo_url1;
	String repo_path2;
	String repo_url2;
	String repo_path3;
	String repo_url3;
	
	SSHCommandResult exec_result;
	KatelloOrg org2;
	KatelloEnvironment env2;
	KatelloChangeset del_changeset;
	KatelloContentDefinition condef1;
	KatelloContentDefinition condef2;
	KatelloContentDefinition compcondef;
	KatelloContentView compconview;
	KatelloActivationKey act_key2;
	KatelloSystem sys2;
	
	@BeforeClass(description="Generate unique objects")
	public void setUp() {

		org2 = new KatelloOrg(org_name2,null);
		exec_result = org2.cli_create();		              
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		env2 = new KatelloEnvironment(env_name2,null,org_name2,KatelloEnvironment.LIBRARY);
		exec_result = env2.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	
		
		
		// create repos and content definition in second organization for composite content view tests 
		createLocalRepo1();
		
		createLocalRepo2();
		
		createLocalRepo3();
		
		condef1 = new KatelloContentDefinition(condef_name1,null,org_name2,null);
		exec_result = condef1.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = condef1.add_product(prod_local1_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	
		
		exec_result = condef1.add_repo(prod_local1_name, repo_local1_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	
				
		exec_result = condef1.publish(pubview_name1_1, pubview_name1_1, "Publish Content");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = condef1.publish(pubview_name1_2, pubview_name1_2, "Publish Content");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		condef2 = new KatelloContentDefinition(condef_name2,null,org_name2,null);
		exec_result = condef2.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = condef2.add_product(prod_local2_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	
		
		exec_result = condef2.add_repo(prod_local2_name, repo_local2_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	
				
		exec_result = condef2.publish(pubview_name2_1, pubview_name2_1, "Publish Content");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = condef2.publish(pubview_name2_2, pubview_name2_2, "Publish Content");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
			
		KatelloUtils.sshOnClient("sed -i -e \"s/certFrequency.*/certFrequency = 1/\" /etc/rhsm/rhsm.conf");
		KatelloUtils.sshOnClient("service rhsmcertd restart");
		yum_clean();
		KatelloUtils.sshOnClient("service goferd restart;");
	}

	@Test(description="Create composite content view definition")
	public void test_createComposite() {
		compcondef = new KatelloContentDefinition(condef_composite_name,null,org_name2,null);
		exec_result = compcondef.create(true);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = compcondef.add_view(pubview_name1_2);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	
		
		exec_result = compcondef.add_view(pubview_name2_2);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = compcondef.publish(pubcompview_name1, pubcompview_name1, "Publish Content");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");		
	}
	
	@Test(description="Check adding old views into composite content view definition", dependsOnMethods={"test_createComposite"})
	public void test_checkOldViewsIntoComposite() {
		exec_result = compcondef.add_view(pubview_name1_1);
		Assert.assertTrue(exec_result.getExitCode() == 244, "Check - return code");	
		Assert.assertTrue(getOutput(exec_result).contains(KatelloContentDefinition.ERR_ADDVIEW), "Error in adding older view");
		
		exec_result = compcondef.add_view(pubview_name2_1);
		Assert.assertTrue(exec_result.getExitCode() == 244, "Check - return code");
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
	
	// @ TODO bug 956266
	@Test(description="Consume content from composite content view definition", dependsOnMethods={"test_addRemoveViewsIntoComposite"})
	public void test_consumeCompositeContent() {
		// erase packages
		exec_result = KatelloUtils.sshOnClient("yum erase -y wolf");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = KatelloUtils.sshOnClient("yum erase -y shark");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = KatelloUtils.sshOnClient("yum erase -y cheetah");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		compconview = new KatelloContentView(pubcompview_name1, org_name2);
		exec_result = compconview.promote_view(env_name2);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloContentView.OUT_PROMOTE, this.pubcompview_name1, env_name2)), "Content view promote output.");
		
		act_key2 = new KatelloActivationKey(org_name2, env_name2, act_key_name2, "Act key2 created");
		exec_result = act_key2.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	
		exec_result = act_key2.update_add_content_view(pubcompview_name1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	
		exec_result = act_key2.info();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	
		Assert.assertTrue(getOutput(exec_result).contains(this.pubcompview_name1), "Content view name is in output.");
		
		KatelloUtils.sshOnClient(KatelloSystem.RHSM_CLEAN);
		sys2 = new KatelloSystem(system_name2, this.org_name2, null);
		exec_result = sys2.rhsm_registerForce(act_key_name2);
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		
		exec_result = sys2.subscriptions_available();
		String poolId1 = KatelloCli.grepCLIOutput("ID", getOutput(exec_result).trim(),1);
		Assert.assertNotNull(poolId1, "Check - pool Id is not null");
		
		String poolId2 = KatelloCli.grepCLIOutput("ID", getOutput(exec_result).trim(),2);
		Assert.assertNotNull(poolId2, "Check - pool Id is not null");
		
		String poolId3 = KatelloCli.grepCLIOutput("ID", getOutput(exec_result).trim(),3);
		Assert.assertNotNull(poolId3, "Check - pool Id is not null");
		
		exec_result = sys2.subscribe(poolId1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = sys2.subscribe(poolId2);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		exec_result = sys2.subscribe(poolId3);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		//install package from content view 1
		exec_result = KatelloUtils.sshOnClient("yum install -y wolf");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		//install package from content view 2
		exec_result = KatelloUtils.sshOnClient("yum install -y shark");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		//package should not be available to install
		exec_result = KatelloUtils.sshOnClient("yum install -y cheetah");
		Assert.assertTrue(getOutput(exec_result).trim().contains("No package cheetah available."));
	}

	@Test(description = "part of promoted composite content view delete by changeset from environment, verify that packages are not availble anymore",
			groups={"cfse-cli"}, dependsOnMethods={"test_consumeCompositeContent"})
	public void test_deletePromotedContentViewPart() {
		KatelloUtils.sshOnClient("yum erase -y walrus");
		
		del_changeset = new KatelloChangeset(del_changeset_name,org_name2,env_name2, true);
		exec_result = del_changeset.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = del_changeset.update_addView(pubview_name1_2);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");		
		exec_result = del_changeset.apply();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = KatelloUtils.sshOnClient("yum install -y walrus");
		Assert.assertTrue(exec_result.getExitCode() == 147, "Check - error code");
	}

	@Test(description = "removed content view on previous scenario promote back by changeset to environment, verify that packages are already availble",
			groups={"cfse-cli"}, dependsOnMethods={"test_deletePromotedContentViewPart"})
	public void test_RePromoteContentViewPart() {
		KatelloUtils.sshOnClient("yum erase -y walrus");
		
		compconview = new KatelloContentView(pubview_name1_2, org_name2);
		exec_result = compconview.promote_view(env_name2);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloContentView.OUT_PROMOTE, this.pubview_name1_1, env_name2)), "Content view promote output.");
		
		exec_result = KatelloUtils.sshOnClient("yum install -y walrus");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - error code");
	}	

	/**
	 * Creates local repo 1 which packages are from REPO_INECAS_ZOO3.
	 */
	private void createLocalRepo1() {
		String uid = KatelloUtils.getUniqueID();
		repo_path1 = "/var/www/html/"+uid;
		repo_url1 = "http://localhost/" + uid;
		
		KatelloUtils.sshOnServer("yum -y install createrepo");
		KatelloUtils.sshOnServer("mkdir /tmp/"+uid);
		KatelloUtils.sshOnServer("createrepo " + repo_path1);
		KatelloUtils.sshOnServer("wget " + REPO_INECAS_ZOO3 + "wolf-9.4-2.noarch.rpm -P "+repo_path1);
		KatelloUtils.sshOnServer("wget " + REPO_INECAS_ZOO3 + "walrus-0.71-1.noarch.rpm -P "+repo_path1);
		KatelloUtils.sshOnServer("createrepo "+repo_path1);
		
		// Create provider:
		KatelloProvider prov = new KatelloProvider(prov_local1_name, org_name2, "Package provider", null);
		exec_result = prov.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		// Create product:
		KatelloProduct prod = new KatelloProduct(prod_local1_name, org_name2, prov_local1_name, null, null, null, null, null);
		exec_result = prod.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	
		KatelloRepo repo = new KatelloRepo(repo_local1_name, org_name2, prod_local1_name, repo_url1, null, null);
		exec_result = repo.create(true);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
//		
//		prod.promote(env_name2);
		
		exec_result = repo.synchronize();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	}

	/**
	 * Creates local repo 2 which packages are from REPO_INECAS_ZOO3.
	 */
	private void createLocalRepo2() {
		String uid = KatelloUtils.getUniqueID();
		repo_path2 = "/var/www/html/"+uid;
		repo_url2 = "http://localhost/" + uid;
		
		KatelloUtils.sshOnServer("yum -y install createrepo");
		KatelloUtils.sshOnServer("mkdir /tmp/"+uid);
		KatelloUtils.sshOnServer("createrepo " + repo_path2);
		KatelloUtils.sshOnServer("wget " + REPO_INECAS_ZOO3 + "shark-0.1-1.noarch.rpm -P "+repo_path2);
		KatelloUtils.sshOnServer("wget " + REPO_INECAS_ZOO3 + "squirrel-0.1-1.noarch.rpm -P "+repo_path2);
		KatelloUtils.sshOnServer("createrepo "+repo_path2);
		
		// Create provider:
		KatelloProvider prov = new KatelloProvider(prov_local2_name, org_name2, "Package provider", null);
		exec_result = prov.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		// Create product:
		KatelloProduct prod = new KatelloProduct(prod_local2_name, org_name2, prov_local2_name, null, null, null, null, null);
		exec_result = prod.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	
		KatelloRepo repo = new KatelloRepo(repo_local2_name, org_name2, prod_local2_name, repo_url2, null, null);
		exec_result = repo.create(true);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
//		prod.promote(env_name2);
		
		exec_result = repo.synchronize();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	}

	/**
	 * Creates local repo 3 which packages are from REPO_INECAS_ZOO3.
	 */
	private void createLocalRepo3() {
		String uid = KatelloUtils.getUniqueID();
		repo_path3 = "/var/www/html/"+uid;
		repo_url3 = "http://localhost/" + uid;
		
		KatelloUtils.sshOnServer("yum -y install createrepo");
		KatelloUtils.sshOnServer("mkdir /tmp/"+uid);
		KatelloUtils.sshOnServer("createrepo " + repo_path3);
		KatelloUtils.sshOnServer("wget " + REPO_INECAS_ZOO3 + "cockateel-3.1-1.noarch.rpm -P "+repo_path3);
		KatelloUtils.sshOnServer("wget " + REPO_INECAS_ZOO3 + "cheetah-1.25.3-5.noarch.rpm -P "+repo_path3);
		KatelloUtils.sshOnServer("createrepo "+repo_path3);
		
		// Create provider:
		KatelloProvider prov = new KatelloProvider(prov_local3_name, org_name2, "Package provider", null);
		exec_result = prov.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		// Create product:
		KatelloProduct prod = new KatelloProduct(prod_local3_name, org_name2, prov_local3_name, null, null, null, null, null);
		exec_result = prod.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	
		KatelloRepo repo = new KatelloRepo(repo_local3_name, org_name2, prod_local3_name, repo_url3, null, null);
		exec_result = repo.create(true);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
//		prod.promote(env_name2);
		
		exec_result = repo.synchronize();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	}
	
	@AfterClass
	public void tearDown() {
		exec_result = org2.delete();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	}

}
