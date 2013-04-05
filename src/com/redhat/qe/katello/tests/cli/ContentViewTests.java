package com.redhat.qe.katello.tests.cli;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.obj.KatelloActivationKey;
import com.redhat.qe.katello.base.obj.KatelloChangeset;
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
import com.redhat.qe.katello.tests.e2e.PromoteErrata;
import com.redhat.qe.tools.SSHCommandResult;

public class ContentViewTests extends KatelloCliTestScript{
	
	public static final String ERRATA_ZOO_SEA = "RHEA-2012:0002";
	
	String uid = KatelloUtils.getUniqueID();
	String org_name = "orgcon-"+ uid;
	String org_name2 = "orgcon2-"+ uid;
	String env_name = "envcon-"+ uid;
	String env_name2 = "envcon2-"+ uid;
	String prov_name = "provcon-" + uid;
	String prod_name = "prodcon-"+ uid;
	String repo_name = "repocon-" + uid;
	String changeset_name = "changecon-" + uid;
	String condef_name = "condef-" + uid;
	String conview_name = "conview-" + uid;
	String pubview_name = "pubview-" + uid;
	String condef_name1 = "condef1-" + uid;
	String condef_name2 = "condef2-" + uid;
	String condef_composite_name = "condefcomposite-" + uid;
	String pubview_name1_1 = "pubview1-1" + uid;
	String pubview_name1_2 = "pubview1-2" + uid;
	String pubview_name2_1 = "pubview2-1" + uid;
	String pubview_name2_2 = "pubview2-2" + uid;
	String pubcompview_name1 = "pubcompview1" + uid;
	String act_key_name = "act_key" + uid;
	String prod_name2 = "prodcon2-" + uid;
	String repo_name2 = "repo_name2-" + uid;
	String group_name = "group" + uid;
	String system_name1 = "system" + uid;
	String system_uuid1;
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
	KatelloOrg org;
	KatelloOrg org2;
	KatelloEnvironment env;
	KatelloEnvironment env2;
	KatelloProvider prov;
	KatelloProduct prod;
	KatelloRepo repo;
	KatelloProduct prod2;
	KatelloRepo repo2;
	KatelloChangeset changeset;
	KatelloContentView condef;
	KatelloContentView condef1;
	KatelloContentView condef2;
	KatelloContentView compcondef;
	KatelloActivationKey act_key;
	KatelloSystem sys;
	KatelloSystemGroup group;
	
	@BeforeClass(description="Generate unique objects")
	public void setUp() {
		
		org = new KatelloOrg(org_name,null);
		exec_result = org.cli_create();		              
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		org2 = new KatelloOrg(org_name2,null);
		exec_result = org2.cli_create();		              
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		env = new KatelloEnvironment(env_name,null,org_name,KatelloEnvironment.LIBRARY);
		exec_result = env.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		env2 = new KatelloEnvironment(env_name2,null,org_name2,KatelloEnvironment.LIBRARY);
		exec_result = env2.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		prov = new KatelloProvider(prov_name,org_name,null,null);
		exec_result = prov.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		prod = new KatelloProduct(prod_name,org_name,prov_name,null, null, null,null, null);
		exec_result = prod.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		repo = new KatelloRepo(repo_name,org_name,prod_name,REPO_INECAS_ZOO3, null, null);
		exec_result = repo.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = prod.promote(env_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	
		
		prod2 = new KatelloProduct(prod_name2,org_name,prov_name,null, null, null,null, null);
		exec_result = prod2.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		repo2 = new KatelloRepo(repo_name2,org_name,prod_name,PULP_RHEL6_x86_64_REPO, null, null);
		exec_result = repo2.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = repo.synchronize();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		condef = new KatelloContentView(condef_name,null,org_name,null);
		exec_result = condef.create_definition();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = condef.add_product(prod_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	
		
		exec_result = condef.publish(pubview_name,pubview_name,"Publish Content");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");		
		
		
		// create repos and content definition in second organization for composite content view tests 
		createLocalRepo1();
		
		createLocalRepo2();
		
		createLocalRepo3();
		
		condef1 = new KatelloContentView(condef_name1,null,org_name2,null);
		exec_result = condef1.create_definition();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = condef1.add_product(prod_local1_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	
		
		exec_result = condef1.add_repo(prod_local1_name, repo_local1_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	
				
		exec_result = condef1.publish(pubview_name1_1, pubview_name1_1, "Publish Content");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = condef1.publish(pubview_name1_2, pubview_name1_2, "Publish Content");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		condef2 = new KatelloContentView(condef_name2,null,org_name2,null);
		exec_result = condef2.create_definition();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = condef2.add_product(prod_local2_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	
		
		exec_result = condef2.add_repo(prod_local2_name, repo_local2_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	
				
		exec_result = condef2.publish(pubview_name2_1, pubview_name2_1, "Publish Content");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = condef2.publish(pubview_name2_2, pubview_name2_2, "Publish Content");
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

	@Test(description = "Adding a published content view to an activation key",groups={"cfse-cli"})
	public void test_addContentView(){

		changeset = new KatelloChangeset(changeset_name,org_name,env_name);
		exec_result = changeset.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = changeset.update_addView(pubview_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");		
		exec_result = changeset.apply();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	
		
		act_key = new KatelloActivationKey(org_name,env_name,act_key_name,"Act key created");
		exec_result = act_key.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	
		exec_result = act_key.update_add_content_view(pubview_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	
		exec_result = act_key.info();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	
		Assert.assertTrue(getOutput(exec_result).contains(this.pubview_name), "Content view name is in output.");
	}
	
	@Test(description = "register client via activation key",groups={"cfse-cli"}, dependsOnMethods={"test_addContentView"})
	public void test_registerClient(){
		KatelloUtils.sshOnClient(KatelloSystem.RHSM_CLEAN);
		sys = new KatelloSystem(system_name1, this.org_name, null);
		exec_result = sys.rhsm_registerForce(act_key_name);
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		
		exec_result = sys.rhsm_identity();
		system_uuid1 = KatelloCli.grepCLIOutput("Current identity is", exec_result.getStdout());
	}
	
	@Test(description = "consume Errata content",groups={"cfse-cli"}, dependsOnMethods={"test_registerClient"})
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
	
	//@ TODO will fail because of 947859
	@Test(description = "Remove a published content view to an activation key",groups={"cfse-cli"}, dependsOnMethods={"test_ConsumeErrata"})
	public void test_removeContentView() {
		
		exec_result = act_key.update_remove_content_view();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	
		exec_result = act_key.info();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	
		Assert.assertFalse(getOutput(exec_result).contains(this.pubview_name), "Content view name is not in output.");
	}

	@Test(description="Create composite content view definition")
	public void test_createComposite() {
		//@ TODO
	}
	
	@Test(description="Check adding old views into composite content view definition", dependsOnMethods={"test_createComposite"})
	public void test_checkOldViewsIntoComposite() {
		//@ TODO
	}
	
	@Test(description="Consume content from composite content view definition", dependsOnMethods={"test_checkOldViewsIntoComposite"})
	public void test_consumeCompositeContent() {
		//@ TODO
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
		exec_result = repo.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		prod.promote(env_name2);
		
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
		exec_result = repo.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		prod.promote(env_name2);
		
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
		exec_result = repo.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		prod.promote(env_name2);
		
		exec_result = repo.synchronize();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	}
	
	@AfterClass
	public void tearDown() {
//		exec_result = org.delete();
//		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
//		exec_result = org2.delete();
//		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	}

}
