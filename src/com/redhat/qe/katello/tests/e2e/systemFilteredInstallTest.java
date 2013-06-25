package com.redhat.qe.katello.tests.e2e;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
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
import com.redhat.qe.katello.base.obj.helpers.FilterRulePackageGroups;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.common.TngRunGroups;
import com.redhat.qe.tools.SSHCommandResult;



@Test(groups=TngRunGroups.TNG_KATELLO_Content)
public class systemFilteredInstallTest extends KatelloCliTestBase{


	String uid = KatelloUtils.getUniqueID();
	String org_name = "orgcon-"+ uid;
	String env_name = "envcon-"+ uid;
	String prov_name = "provcon-" + uid;
	String prod_name = "prodcon-"+ uid;
	String repo_name = "repocon-" + uid;
	String condef_name = "condef-" + uid;
	String package_filter = "package_filter1";
	String packageGroup_filter = "packagegroup_filter1";
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

	public void test_systemFilteredconsumePackageGroupContent() {

		KatelloContentFilter filter = new KatelloContentFilter(packageGroup_filter, org_name, condef_name);
		exec_result = filter.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = filter.info();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = filter.add_repo(prod_name, repo_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloContentFilter.OUT_ADD_REPO, repo_name, packageGroup_filter)), "Check output");
		exec_result = filter.add_rule(KatelloContentFilter.TYPE_INCLUDES, new FilterRulePackageGroups("mammals"));
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = filter.add_rule(KatelloContentFilter.TYPE_EXCLUDES, new FilterRulePackageGroups("birds"));
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
		rhsm_clean();
		sys = new KatelloSystem(system_name1, this.org_name, null);
		exec_result = sys.rhsm_registerForce(act_key_name);
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		exec_result = sys.rhsm_identity();
		system_uuid1 = KatelloUtils.grepCLIOutput("Current identity is", exec_result.getStdout());
		exec_result = sys.subscriptions_available();
		String poolId1 = KatelloUtils.grepCLIOutput("ID", getOutput(exec_result).trim(),1);
		Assert.assertNotNull(poolId1, "Check - pool Id is not null");
		exec_result = sys.subscribe(poolId1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		yum_clean();
		KatelloUtils.sshOnClient("yum erase -y lion zebra stork cockateel");

		// consume packages from group mammals, verify that they are available
		exec_result=sys.packages_install_group("mammals");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		check_install_Package("lion");
		check_install_Package("zebra");

		// consume packages from group birds, verify that they are NOT available
		exec_result=sys.packages_install_group("birds");
		Assert.assertTrue(getOutput(exec_result).trim().contains("No Group named birds exists"));
	}

	private void check_install_Package(String pkgName)
	{
		exec_result = KatelloUtils.sshOnClient("rpm -q "+ pkgName);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).trim().contains(pkgName+"-"));
	}


}
