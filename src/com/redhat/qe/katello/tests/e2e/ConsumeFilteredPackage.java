package com.redhat.qe.katello.tests.e2e;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.obj.KatelloActivationKey;
import com.redhat.qe.katello.base.obj.KatelloContentDefinition;
import com.redhat.qe.katello.base.obj.KatelloContentFilter;
import com.redhat.qe.katello.base.obj.KatelloContentView;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.base.obj.helpers.FilterRulePackage;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.common.TngRunGroups;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups=TngRunGroups.TNG_KATELLO_Content)
public class ConsumeFilteredPackage extends KatelloCliTestScript {
	
	String uid = KatelloUtils.getUniqueID();
	String org_name = "orgcon-"+ uid;
	String env_name = "envcon-"+ uid;
	String prov_name = "provcon-" + uid;
	String prod_name = "prodcon-"+ uid;
	String repo_name = "repocon-" + uid;
	String condef_name = "condef-" + uid;
	String package_filter = "package_filter1";
	String pubview_name = "pubview-" + uid;
	String system_name1 = "system-" + uid;
	String act_key_name = "act_key-" + uid;
	
	SSHCommandResult exec_result;
	KatelloOrg org;
	KatelloEnvironment env;
	KatelloProvider prov;
	KatelloProduct prod;
	KatelloRepo repo;
	KatelloContentDefinition condef;
	KatelloContentView conview;
	KatelloActivationKey act_key;
	KatelloSystem sys;
	String system_uuid1;
	
	@BeforeClass(description="Generate unique objects")
	public void setUp() {
		KatelloUtils.sshOnClient("yum erase -y fox cow dog dolphin duck walrus elephant horse kangaroo pike lion");
		
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
		
		exec_result = repo.synchronize();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		condef = new KatelloContentDefinition(condef_name,null,org_name,null);
		exec_result = condef.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		exec_result = condef.add_product(prod_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = condef.add_repo(prod_name, repo_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	}
    
	//@ TODO bug 953655
	@Test(description="Consume content from filtered package")
	public void test_consumePackageContent() {

		KatelloContentFilter filter = new KatelloContentFilter(package_filter, org_name, condef_name);

		exec_result = filter.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		exec_result = filter.info();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = filter.add_repo(prod_name, repo_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloContentFilter.OUT_ADD_REPO, repo_name, package_filter)), "Check output");

		// add package rules there     
		FilterRulePackage [] include_packages = {
				new FilterRulePackage("fox"),
				new FilterRulePackage("tiger"),
				new FilterRulePackage("lion"),
				new FilterRulePackage("bear"),
				new FilterRulePackage("cockateel"),
				new FilterRulePackage("cow", "2.2-3", null, null),
				new FilterRulePackage("walrus", "0.71-1", null, null),
				new FilterRulePackage("dog", null, "4.20", null),
				new FilterRulePackage("dolphin", null, null, "3.11"),
				new FilterRulePackage("duck", null, "0.6", "0.7")
		};

		exec_result = filter.add_rule(KatelloContentFilter.TYPE_INCLUDES, include_packages);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		FilterRulePackage [] exclude_packages = {
				new FilterRulePackage("elephant"),
				new FilterRulePackage("walrus", "5.21-1", null, null),
				new FilterRulePackage("horse", null, "0.21", null),
				new FilterRulePackage("kangaroo", null, null, "0.3"),
				new FilterRulePackage("pike", null, "2.1", "2.3"),
		};

		exec_result = filter.add_rule(KatelloContentFilter.TYPE_EXCLUDES, exclude_packages);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		condef.publish(pubview_name,pubview_name,null);

		conview = new KatelloContentView(pubview_name, org_name);
		exec_result = conview.promote_view(env_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloContentView.OUT_PROMOTE, this.pubview_name, env_name)), "Content view promote output.");

		act_key = new KatelloActivationKey(org_name,env_name,act_key_name,"Act key created");
		exec_result = act_key.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");      
		exec_result = act_key.update_add_content_view(pubview_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");      
		exec_result = act_key.info();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");      
		Assert.assertTrue(getOutput(exec_result).contains(this.pubview_name), "Content view name is in output.");

		//register client, subscribe to pool
		KatelloUtils.sshOnClient(KatelloSystem.RHSM_CLEAN);
		sys = new KatelloSystem(system_name1, this.org_name, null);
		exec_result = sys.rhsm_registerForce(act_key_name);
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");

		exec_result = sys.rhsm_identity();
		system_uuid1 = KatelloCli.grepCLIOutput("Current identity is", exec_result.getStdout());

		exec_result = sys.subscriptions_available();
		String poolId1 = KatelloCli.grepCLIOutput("ID", getOutput(exec_result).trim(),1);
		Assert.assertNotNull(poolId1, "Check - pool Id is not null");

		exec_result = sys.subscribe(poolId1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");


		yum_clean();                   


		// consume packages from include filter, verify that they are available
		install_Package("fox");
		install_Package("cow-2.2-3");
		install_Package("dog-4.23-1");
		install_Package("dolphin-3.10.232-1");
		install_Package("duck-0.6-1");
		install_Package("walrus-0.71-1");

		// consume packages from exclude filter, verify that they are NOT available
		verify_PackageNotAvailable("elephant-8.3-1");
		verify_PackageNotAvailable("walrus-5.21-1");
		verify_PackageNotAvailable("horse-0.22-2");
		verify_PackageNotAvailable("kangaroo-0.2-1");
		verify_PackageNotAvailable("pike-2.2-1");
	}

	private void install_Package(String pkgName) {
		exec_result=KatelloUtils.sshOnClient("yum install -y "+ pkgName);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = KatelloUtils.sshOnClient("rpm -q "+ pkgName);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	}
	
	private void verify_PackageNotAvailable(String pkgName) {
		exec_result=KatelloUtils.sshOnClient("yum install -y "+ pkgName);
		Assert.assertTrue(getOutput(exec_result).trim().contains("No package " + pkgName + " available."));
	}
}
