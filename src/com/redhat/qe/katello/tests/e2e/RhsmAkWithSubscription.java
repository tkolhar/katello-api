package com.redhat.qe.katello.tests.e2e;

import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloActivationKey;
import com.redhat.qe.katello.base.obj.KatelloContentDefinition;
import com.redhat.qe.katello.base.obj.KatelloContentView;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.common.KatelloUtils;

/**
 * Client should be able to yum install rpm-s via RHSM registration with activation key.<br>
 * Activation key contains the subscription to the repo/product.
 * 
 * @author gkhachik
 * @since 23.May.2013
 * @see <a href='https://github.com/gkhachik/katello-api/issues/396'>github issue</a><br>
 * 	<a href='https://tcms.engineering.redhat.com/case/268913/?from_plan=4785'>TCMS</a>
 */
@Test(groups={"cfse-e2e"}, singleThreaded = true)
public class RhsmAkWithSubscription extends KatelloCliTestBase{
	
	private final String uid = KatelloUtils.getUniqueID(); 
	private String contentDef = "cdZoo4-"+uid;
	private String contentView = "cvZoo4-"+uid;
	private String ak = "ak-zoo4-"+uid;
	private String system = "awesomeSystem-"+uid;

	private static final String RPM_TO_INSTALL = "bat"; 
	
	@Test(description="promote content to the environment via content view")
	public void test_promoteViaContentView(){
		KatelloContentDefinition _contDef = new KatelloContentDefinition(cli_worker, contentDef, null, base_org_name, null);
		exec_result = _contDef.create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - exitCode");
		exec_result = _contDef.add_product(base_zoo4_product_name);
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - exitCode");
		exec_result = _contDef.publish(contentView, null, null);
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - exitCode");
		exec_result = new KatelloContentView(cli_worker, contentView, base_org_name).promote_view(base_test_env_name);
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - exitCode");
	}
	
	@Test(description="prepare the activation key with subscription and content view inside. Check the `ak info`", 
			dependsOnMethods={"test_promoteViaContentView"})
	public void test_prepareAk(){
		KatelloActivationKey _ak = new KatelloActivationKey(this.cli_worker, base_org_name,base_test_env_name,ak,null,"10",contentView);
		exec_result = _ak.create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - exitCode");
		exec_result = _ak.update_add_subscription(base_zoo4_repo_pool);
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - exitCode");
		exec_result = _ak.info();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - exitCode");
		Assert.assertTrue(KatelloUtils.grepCLIOutput("Usage Limit", getOutput(exec_result)).equals("10"), "Check - ak info('Usage Limit')");
		Assert.assertTrue(KatelloUtils.grepCLIOutput("Content View", getOutput(exec_result)).contains(contentView), "Check - ak info('Content View')");
		Assert.assertTrue(KatelloUtils.grepCLIOutput("Pools", getOutput(exec_result)).contains(base_zoo4_repo_pool), "Check - ak info('Pools')");
	}
	
	@Test(description="register with AK", dependsOnMethods={"test_prepareAk"})
	public void test_registerAndConsumeRpm(){
		rhsm_clean();
		KatelloSystem _sys = new KatelloSystem(this.cli_worker, system, base_org_name, null);
		exec_result = _sys.rhsm_environments();
		Assert.assertTrue(getOutput(exec_result).contains(KatelloEnvironment.LIBRARY+"/"+contentView), "Check - exists env for RHSM: Library/<contentView>");
		Assert.assertTrue(getOutput(exec_result).contains(base_test_env_name.replaceAll(" ", "_")+"/"+contentView), "Check - exists env for RHSM: <env>/<contentView>");
		exec_result = _sys.rhsm_registerForce(ak);
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - exitCode");
		Assert.assertTrue(getOutput(exec_result).contains(KatelloSystem.OUT_RHSM_REGISTERED_OK), "Check - RHSM registered ok string");
	}
	
	@Test(description="consume some content from zoo4", dependsOnMethods={"test_registerAndConsumeRpm"})
	public void test_consumeYumRpm(){
		KatelloUtils.sshOnClient(cli_worker.getClientHostname(), "subscription-manager refresh; service rhsmcertd restart");
		yum_clean();
		sshOnClient("rpm -q "+RPM_TO_INSTALL+" && yum -y erase "+RPM_TO_INSTALL);
		exec_result = sshOnClient("yum -y install "+RPM_TO_INSTALL);
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - exitCode");
		exec_result = sshOnClient("rpm -q "+RPM_TO_INSTALL);
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - exitCode");
		Assert.assertTrue(getOutput(exec_result).contains(RPM_TO_INSTALL+"-"), "Check - rpm is installed");
	}
}
