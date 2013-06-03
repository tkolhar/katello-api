package com.redhat.qe.katello.tests.e2e;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.obj.KatelloActivationKey;
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

/**
 * Client should be able to yum install rpm-s via RHSM registration with activation key.<br>
 * Activation key contains the subscription to the repo/product.
 * 
 * @author gkhachik
 * @since 23.May.2013
 * @see <a href='https://github.com/gkhachik/katello-api/issues/396'>github issue</a><br>
 * 	<a href='https://tcms.engineering.redhat.com/case/268913/?from_plan=4785'>TCMS</a>
 */
@Test(groups={"cfse-e2e"})
public class RhsmAkWithSubscription extends KatelloCliTestScript{
	
	private final String uid = KatelloUtils.getUniqueID(); 
	private String org = "AwesomeOrg-"+uid;
	private String env = "Testing";
	private String provider = "Zoo4-"+uid;
	private String product = "Zoo4-"+uid;
	private String repo = "zoo4-"+uid;
	private String contentDef = "cdZoo4-"+uid;
	private String contentView = "cvZoo4-"+uid;
	private String ak = "ak-zoo4-"+uid;
	private String system = "awesomeSystem-"+uid;
	private String poolid;
	
	private SSHCommandResult exec_result;
	private static final String RPM_TO_INSTALL = "bat"; 
	
	@BeforeClass(description="preparation")
	public void setUp(){
		exec_result = new KatelloOrg(org, null).cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - exitCode");
		exec_result = new KatelloEnvironment(env, null, org, KatelloEnvironment.LIBRARY).cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - exitCode");
	}
	
	@Test(description="prepare and sync repo")
	public void test_syncRepo(){
		exec_result = new KatelloProvider(provider, org, null, null).create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - exitCode");
		exec_result = new KatelloProduct(product, org, provider, null, null, null, null, null).create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - exitCode");
		exec_result = new KatelloRepo(repo, org, product, REPO_HHOVSEPY_ZOO4, null, null).create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - exitCode");
		exec_result = new KatelloProvider(provider, org, null, null).synchronize();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - exitCode");
	}
	
	@Test(description="promote content to the environment via content view", 
			dependsOnMethods={"test_syncRepo"})
	public void test_promoteViaContentView(){
		KatelloContentDefinition _contDef = new KatelloContentDefinition(contentDef, null, org, null);
		exec_result = _contDef.create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - exitCode");
		exec_result = _contDef.add_product(product);
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - exitCode");
		exec_result = _contDef.publish(contentView, null, null);
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - exitCode");
		exec_result = new KatelloContentView(contentView, org).promote_view(env);
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - exitCode");
	}
	
	@Test(description="prepare the activation key with subscription and content view inside. Check the `ak info`", 
			dependsOnMethods={"test_promoteViaContentView"})
	public void test_prepareAk(){
		KatelloActivationKey _ak = new KatelloActivationKey(org,env,ak,null,"10",contentView);
		exec_result = _ak.create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - exitCode");
		exec_result = new KatelloOrg(org, null).subscriptions();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - exitCode");
		poolid = KatelloUtils.grepCLIOutput("ID", getOutput(exec_result));
		Assert.assertNotNull(poolid, "Check - poolid can be extracted");
		exec_result = _ak.update_add_subscription(poolid);
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - exitCode");
		exec_result = _ak.info();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - exitCode");
		Assert.assertTrue(KatelloUtils.grepCLIOutput("Usage Limit", getOutput(exec_result)).equals("10"), "Check - ak info('Usage Limit')");
		Assert.assertTrue(KatelloUtils.grepCLIOutput("Content View", getOutput(exec_result)).contains(contentView), "Check - ak info('Content View')");
		Assert.assertTrue(KatelloUtils.grepCLIOutput("Pools", getOutput(exec_result)).contains(poolid), "Check - ak info('Pools')");
	}
	
	@Test(description="register with AK", dependsOnMethods={"test_prepareAk"})
	public void test_registerAndConsumeRpm(){
		rhsm_clean();
		KatelloSystem _sys = new KatelloSystem(system, org, null);
		exec_result = _sys.rhsm_environments();
		Assert.assertTrue(getOutput(exec_result).contains(KatelloEnvironment.LIBRARY+"/"+contentView), "Check - exists env for RHSM: Library/<contentView>");
		Assert.assertTrue(getOutput(exec_result).contains(env+"/"+contentView), "Check - exists env for RHSM: <env>/<contentView>");
		exec_result = _sys.rhsm_registerForce(ak);
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - exitCode");
		Assert.assertTrue(getOutput(exec_result).contains(KatelloSystem.OUT_RHSM_REGISTERED_OK), "Check - RHSM registered ok string");
	}
	
	@Test(description="consume some content from zoo4", dependsOnMethods={"test_registerAndConsumeRpm"})
	public void test_consumeYumRpm(){
		KatelloUtils.sshOnClient("rpm -q "+RPM_TO_INSTALL+" && yum -y erase "+RPM_TO_INSTALL);
		exec_result = KatelloUtils.sshOnClient("yum -y install "+RPM_TO_INSTALL);
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - exitCode");
		exec_result = KatelloUtils.sshOnClient("rpm -q "+RPM_TO_INSTALL);
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - exitCode");
		Assert.assertTrue(getOutput(exec_result).contains(RPM_TO_INSTALL+"-"), "Check - rpm is installed");
	}
}
