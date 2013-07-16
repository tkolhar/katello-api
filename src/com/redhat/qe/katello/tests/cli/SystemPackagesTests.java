package com.redhat.qe.katello.tests.cli;


import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloActivationKey;
import com.redhat.qe.katello.base.obj.KatelloContentDefinition;
import com.redhat.qe.katello.base.obj.KatelloContentView;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.common.TngRunGroups;
import com.redhat.qe.tools.SSHCommandResult;


@Test(groups={"SystemTests",TngRunGroups.TNG_KATELLO_System_Consumer})
public class SystemPackagesTests extends KatelloCliTestBase {

	private SSHCommandResult exec_result;
	private String actkey_name;
	private String def_name;
	private String view_name;
	private String sys_name;
	private String prod_unsubscr;


	@BeforeClass(description="Generate unique names")
	public void setUp() {
		String uid = KatelloUtils.getUniqueID();
		actkey_name = "actkey"+uid;
		def_name = "def"+uid;
		view_name = "def"+uid;
		sys_name = "sys"+uid;
		prod_unsubscr = "prod-unsubscr"+uid;

		KatelloContentDefinition def = new KatelloContentDefinition(cli_worker, def_name, null, base_org_name, null);
		exec_result = def.create();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (def create)");
		exec_result = def.add_repo(base_zoo_product_name, base_zoo_repo_name);
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (def add repo)");
		exec_result = def.publish(view_name, null, null);
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (publish view)");

		sshOnClient("service goferd restart;");

		KatelloContentView view = new KatelloContentView(cli_worker, view_name, base_org_name);
		exec_result = view.promote_view(base_dev_env_name);
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (promote view)");
		KatelloActivationKey key = new KatelloActivationKey(cli_worker, base_org_name, base_dev_env_name, actkey_name, null, null, view_name);
		exec_result = key.create();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (update key)");

		KatelloSystem sys = new KatelloSystem(cli_worker, sys_name, base_org_name, base_dev_env_name);
		sys.rhsm_clean();
		sys.rhsm_registerForce(actkey_name);

		KatelloProduct prod = new KatelloProduct(cli_worker, prod_unsubscr, base_org_name, base_zoo_provider_name, null, null, null, null, null);
		exec_result = prod.create();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (product create)");

		exec_result = sys.subscribe(base_zoo_repo_pool);
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (subscribe)");
		KatelloOrg org = new KatelloOrg(cli_worker, base_org_name, null);
		exec_result = sys.subscribe(org.custom_getPoolId(prod_unsubscr));
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (subscribe)");

		yum_clean();

		sshOnClient("yum remove -y lion walrus cockateel");
	}


	@Test(description="system install package")
	public void test_systemPackageInstall() {
		KatelloSystem sys = new KatelloSystem(cli_worker, sys_name, base_org_name, base_dev_env_name);
		exec_result = sys.packages_install("lion");
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (install package)");
		Assert.assertTrue(getOutput(exec_result).contains("lion"), "Check output");
	}

	@Test(description="system install package group", dependsOnMethods={"test_systemPackageInstall"})
	public void test_systemPackageGroupInstall() {
		KatelloSystem sys = new KatelloSystem(cli_worker, sys_name, base_org_name, base_dev_env_name);
		exec_result = sys.packages_install_group("birds");
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (install package group)");
		Assert.assertTrue(getOutput(exec_result).contains("cockateel"), "Check output (update package)");
	}

	@Test(description="system update package")
	public void test_systemPackageUpdate() {
		String walrus_updated = "walrus-5.21-1";
		KatelloSystem sys = new KatelloSystem(cli_worker, sys_name, base_org_name, base_dev_env_name);
		sshOnClient("yum install -y walrus-0.71-1");
		exec_result = sys.packages_update("walrus");
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (update package)");
		Assert.assertTrue(getOutput(exec_result).contains(walrus_updated), "Check output (update package)");
	}

	@Test(description="system remove package", dependsOnMethods={"test_systemPackageInstall"})
	public void test_systemPackageRemove() {
		KatelloSystem sys = new KatelloSystem(cli_worker, sys_name, base_org_name, base_dev_env_name);
		exec_result = sys.packages_remove("lion");
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (remove package)");
		Assert.assertTrue(getOutput(exec_result).contains("lion"), "Check output (remove package)");
	}

	@Test(description="system remove package group", dependsOnMethods={"test_systemPackageGroupInstall"})
	public void test_systemPackageRemoveGroups() {
		KatelloSystem sys = new KatelloSystem(cli_worker, sys_name, base_org_name, base_dev_env_name);
		exec_result = sys.packages_remove_groups("birds");
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (remove group)");
		Assert.assertTrue(getOutput(exec_result).contains("cockateel"), "Check output (update package)");
	}

	@Test(description="system unsubscribe product")
	public void test_systemUnsubscribe() {
		KatelloSystem sys = new KatelloSystem(cli_worker, sys_name, base_org_name, base_dev_env_name);
		exec_result = sys.subscriptions();
		String subID = null;
		// find subscription id by product name
		for(int i = 1;; ++i) {
			String name = KatelloUtils.grepCLIOutput("Pool Name", getOutput(exec_result), i);
			Assert.assertTrue(true, name);
			if(name == null)
				break;
			else if(prod_unsubscr.equals(name)) {
				subID = KatelloUtils.grepCLIOutput("Subscription ID", getOutput(exec_result), i);
				break;
			}
		}
		exec_result = sys.unsubscribe(subID);
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (system unsubscribe)");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloSystem.OUT_UNSUBSCRIBE, sys_name)), "Check output (system unsubscribe)");
	}
}
