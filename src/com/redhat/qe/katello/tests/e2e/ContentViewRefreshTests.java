package com.redhat.qe.katello.tests.e2e;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
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
import com.redhat.qe.tools.SSHCommandResult;

@Test(singleThreaded = true)
public class ContentViewRefreshTests extends KatelloCliTestBase{
	
	String uid = KatelloUtils.getUniqueID();
	String org_name2 = "orgcon2-"+ uid;
	String env_name2 = "envcon2-"+ uid;
	String del_changeset_name = "del_changeset-" + uid;
	String condef_name1 = "condef1-" + uid;
	String pubview_name = "pubview" + uid;
	String act_key_name2 = "act_key2" + uid;
	String system_name2 = "system2" + uid;
	String prov_local1_name = "provlocal1-" + uid;
	String prod_local1_name = "prodlocal1-"+ uid;
	String repo_local1_name = "repolocal1-" + uid;
	String repo_path1;
	String repo_url1;
	
	SSHCommandResult exec_result;
	KatelloOrg org2;
	KatelloEnvironment env2;
	KatelloChangeset del_changeset;
	KatelloContentDefinition condef1;
	KatelloContentView conview1;
	KatelloActivationKey act_key2;
	KatelloSystem sys2;
	
	@BeforeClass(description="Generate unique objects")
	public void setUp() {
		// erase packages
		sshOnClient("yum erase -y wolf lion walrus");
		
		org2 = new KatelloOrg(this.cli_worker, org_name2,null);
		exec_result = org2.cli_create();		              
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		env2 = new KatelloEnvironment(this.cli_worker, env_name2,null,org_name2,KatelloEnvironment.LIBRARY);
		exec_result = env2.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	
		
		// create repos and content definition in second organization for composite content view tests 
		createLocalRepo1();
		
		condef1 = new KatelloContentDefinition(cli_worker, condef_name1,null,org_name2,null);
		exec_result = condef1.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = condef1.add_repo(prod_local1_name, repo_local1_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	
				
		exec_result = condef1.publish(pubview_name, pubview_name, "Publish Content");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		condef1.repos = repo_local1_name;
		conview1 = new KatelloContentView(cli_worker, pubview_name, org_name2);
		assert_ContentViewInfo(condef1, conview1, "Publish Content", "Library", "1");
	}
	
	@Test(description = "Adding a published content view to an activation key",groups={"cfse-cli"})
	public void test_addContentView() {
		exec_result = conview1.promote_view(env_name2);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		act_key2 = new KatelloActivationKey(this.cli_worker, org_name2, env_name2, act_key_name2, "Act key created", null, pubview_name);
		exec_result = act_key2.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	

		exec_result = org2.subscriptions();
		String poolId1 = KatelloUtils.grepCLIOutput("ID", getOutput(exec_result).trim(),1);
		Assert.assertNotNull(poolId1, "Check - pool Id is not null");
		exec_result = act_key2.update_add_subscription(poolId1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	}
	
	@Test(description = "register client via activation key",groups={"cfse-cli"}, dependsOnMethods={"test_addContentView"})
	public void test_registerClient() {
		rhsm_clean();
		sys2 = new KatelloSystem(this.cli_worker, system_name2, this.org_name2, null);
		exec_result = sys2.rhsm_registerForce(act_key_name2);
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		
		yum_clean();
		
		install_Packages(cli_worker.getClientHostname(), new String[] {"wolf"});
	}

	@Test(description = "refesh content view, verify version is changed", groups={"cfse-cli"}, dependsOnMethods={"test_registerClient"})
	public void test_refreshContentView() {
		//install non available package from composite content view
		verify_PackagesNotAvailable(cli_worker.getClientHostname(), new String [] {"lion"});
		
		updateLocalRepo1();
		
		exec_result = conview1.refresh_view();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");		
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloContentView.OUT_REFRESH, this.pubview_name)), "Content view refresh output.");
		
		exec_result = conview1.promote_view(env_name2);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		assert_ContentViewInfo(condef1, conview1, "Publish Content", "Library, " + env_name2, "2");
	}	
	
	@Test(description="Consume content from refreshed content view definition", dependsOnMethods={"test_refreshContentView"})
	public void test_consumeRefreshedContent() {
		yum_clean();
		
		// erase packages
		exec_result = sshOnClient("yum erase -y wolf lion walrus");
		
		//install package from refreshed content view
		install_Packages(cli_worker.getClientHostname(), new String[] {"lion"});
		
		// @ TODO bz#1024361
		//verify_PackagesNotAvailable(cli_worker.getClientHostname(), new String [] {"walrus"});
		install_Packages(cli_worker.getClientHostname(), new String[] {"walrus"});
	}
	
	@Test(description="Remove the org",
			dependsOnMethods={"test_consumeRefreshedContent"})
	public void test_deleteOrg(){
		exec_result = org2.delete();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	}
	
	/**
	 * Creates local repo 1 which packages are from REPO_INECAS_ZOO3.
	 */
	private void createLocalRepo1() {
		String uid = KatelloUtils.getUniqueID();
		repo_path1 = "/var/www/html/"+uid;
		repo_url1 = "http://localhost/" + uid;
		
		KatelloUtils.sshOnServer("yum -y install createrepo");
		KatelloUtils.sshOnServer("createrepo " + repo_path1);
		KatelloUtils.sshOnServer("wget " + REPO_INECAS_ZOO3 + "wolf-9.4-2.noarch.rpm -P "+repo_path1);
		KatelloUtils.sshOnServer("wget " + REPO_INECAS_ZOO3 + "walrus-0.71-1.noarch.rpm -P "+repo_path1);
		KatelloUtils.sshOnServer("wget " + REPO_INECAS_ZOO3 + "whale-0.2-1.noarch.rpm -P "+repo_path1);
		KatelloUtils.sshOnServer("wget " + REPO_INECAS_ZOO3 + "stork-0.12-2.noarch.rpm -P "+repo_path1);
		KatelloUtils.sshOnServer("createrepo "+repo_path1);
		
		// Create provider:
		KatelloProvider prov = new KatelloProvider(this.cli_worker, prov_local1_name, org_name2, "Package provider", null);
		exec_result = prov.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		// Create product:
		KatelloProduct prod = new KatelloProduct(this.cli_worker, prod_local1_name, org_name2, prov_local1_name, null, null, null, null, null);
		exec_result = prod.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	
		KatelloRepo repo = new KatelloRepo(this.cli_worker, repo_local1_name, org_name2, prod_local1_name, repo_url1, null, null);
		exec_result = repo.create(true);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = repo.synchronize();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	}
	
	private void updateLocalRepo1() {
		KatelloUtils.sshOnServer("wget " + REPO_INECAS_ZOO3 + "lion-0.4-1.noarch.rpm  -P " + repo_path1);
		KatelloUtils.sshOnServer("rm " + repo_path1 + "/walrus-0.71-1.noarch.rpm -f");
		KatelloUtils.sshOnServer("createrepo " + repo_path1);
		KatelloRepo repo = new KatelloRepo(this.cli_worker, repo_local1_name, org_name2, prod_local1_name, repo_url1, null, null);
		exec_result = repo.synchronize();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	}

	private String assert_ContentViewInfo(KatelloContentDefinition content, KatelloContentView view, String description, String env, String version) {
		SSHCommandResult res;
		if (content.description == null) content.description = "";
		res = view.view_info();
		String match_info = String.format(KatelloContentView.REG_VIEW_INFO, view.getName(), view.getName(), description, content.org,
				content.name, env, version).replaceAll("\"", "");
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		log.finest(String.format("Content view (info) match regex: [%s]",match_info));
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(match_info), 
				String.format("Content view [%s] should be found in the result info", content.org));	
		
		return KatelloUtils.grepCLIOutput("ID", getOutput(res));
	}

}
