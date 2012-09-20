package com.redhat.qe.katello.tests.e2e;

import java.util.logging.Logger;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.google.inject.Inject;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloApiException;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.base.obj.KatelloUser;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.tasks.KatelloTasks;
import com.redhat.qe.katello.tasks.impl.KatelloApiTasks;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups={"cfse-e2e"})
public class ConsumerAccess extends KatelloCliTestScript{
	protected static Logger log = Logger.getLogger(BPMTests.class.getName());
	
	private SSHCommandResult exec_result;
    final private KatelloTasks katelloTasks;
	
	// Katello objects below
	private String org_name;
	private String env_name;
	private String user_name;
	private String system_name;
	
	@Inject
	public ConsumerAccess(KatelloTasks katelloTasks) {
	    this.katelloTasks = katelloTasks;
	}

	public ConsumerAccess() {
	    this.katelloTasks = new KatelloApiTasks();
	}
	
	@BeforeClass(description="Generate unique names")
	public void setUp(){
		String uid = KatelloUtils.getUniqueID();
		org_name = "org_"+uid;
		env_name = "env_"+uid;
		user_name = "user_"+uid;
		system_name = "system_"+uid;
		
		KatelloUtils.sshOnClient("yum -y erase wolf lion || true");
		KatelloUtils.sshOnClient("subscription-manager unregister || true");
		
		// Create org:
		KatelloOrg org = new KatelloOrg(org_name, "Org deletion");
		exec_result = org.cli_create();
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), "Successfully created org [ "+org_name+" ]");
		
		KatelloEnvironment env = new KatelloEnvironment(env_name, null, org_name, KatelloEnvironment.LIBRARY);
		exec_result = env.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		

		KatelloUser user = null;
        try {
            user = katelloTasks.createUser(user_name, KatelloUser.DEFAULT_USER_EMAIL, 
            		KatelloUser.DEFAULT_USER_PASS, false);
        } catch (KatelloApiException e) {
            Assert.fail("Could not create user", e);
        }
		Assert.assertNotNull(user, "Check - return string not null");
	}
	
	/**
	 * Register a system.
	 * Create a regular user.
	 * Verify that regular user has not access to retrieve consumer by uuid but admin user has.
	 */
	@Test(description="Retrieve consumer")
	public void test_consumerRetrieve() {
		KatelloUtils.sshOnClient("subscription-manager clean");
		KatelloSystem sys = new KatelloSystem(this.system_name, this.org_name, null);
		exec_result = sys.rhsm_register(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		
		exec_result = sys.rhsm_identity();
		String id = KatelloCli.grepCLIOutput("Current identity is", getOutput(exec_result).trim(),1);

		
		exec_result = KatelloUtils.sshOnClient("curl -H \"Content-Type: application/json\" -H \"Accept: application/json\" -#  -k -u " + user_name + ":" + KatelloUser.DEFAULT_USER_PASS + " https://localhost/"+ System.getProperty("katello.product", "katello") + "/api/consumers/" + id);
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", "").contains("User " + user_name + " is not allowed to access api/systems/show"), "Check - access denied output");
		
		exec_result = KatelloUtils.sshOnClient("curl -H \"Content-Type: application/json\" -H \"Accept: application/json\" -#  -k -u " + KatelloUser.DEFAULT_ADMIN_PASS + ":" + KatelloUser.DEFAULT_ADMIN_PASS + " https://localhost/" + System.getProperty("katello.product", "katello") + "/api/consumers/" + id);
		Assert.assertFalse(getOutput(exec_result).replaceAll("\n", "").contains("User " + KatelloUser.DEFAULT_ADMIN_PASS + " is not allowed to access api/systems/show"), "Check - access granted output");

	}


}
