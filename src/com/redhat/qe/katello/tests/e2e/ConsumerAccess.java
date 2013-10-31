package com.redhat.qe.katello.tests.e2e;

import java.util.logging.Logger;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import com.google.inject.Inject;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloApiException;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.base.obj.KatelloUser;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.guice.KatelloApiModule;
import com.redhat.qe.katello.guice.PlainSSLContext;
import com.redhat.qe.katello.tasks.KatelloTasks;
import com.redhat.qe.tools.SSHCommandResult;

public class ConsumerAccess extends KatelloCliTestBase{
	protected static Logger log = Logger.getLogger(BPMTests.class.getName());
	
	private SSHCommandResult exec_result;
    final private KatelloTasks katelloTasks;
	
	// Katello objects below
	private String org_name;
	private String env_name;
	private String user_name;
	private String system_name;
	
	@Inject
	public ConsumerAccess(@PlainSSLContext KatelloTasks katelloTasks) {
	    this.katelloTasks = katelloTasks;
	}

	@BeforeClass(description="Generate unique names")
	public void setUp(){
		String uid = KatelloUtils.getUniqueID();
		org_name = "org_"+uid;
		env_name = "env_"+uid;
		user_name = "user_"+uid;
		system_name = "system_"+uid;
		
		rhsm_clean(); // clean the RHSM registration
		
		// Create org:
		KatelloOrg org = new KatelloOrg(this.cli_worker, org_name, "Org deletion");
		exec_result = org.cli_create();
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), String.format(KatelloOrg.OUT_CREATE,org_name));
		
		KatelloEnvironment env = new KatelloEnvironment(this.cli_worker, env_name, null, org_name, KatelloEnvironment.LIBRARY);
		exec_result = env.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		promoteEmptyContentView(org_name, env_name);
		
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
		KatelloSystem sys = new KatelloSystem(this.cli_worker, this.system_name, this.org_name, this.env_name);
		exec_result = sys.rhsm_register(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		
		String serverApiCurlTemplate =
				"curl -H \"Content-Type: application/json\" -H \"Accept: application/json\" -# " +
				"-k -u %s:%s "+
				System.getProperty("katello.server.protocol","https")+"://"+
				System.getProperty("katello.server.hostname","localhost")+"/"+
				System.getProperty("katello.product", "katello")+"/api";
		exec_result = sys.rhsm_identity();
		String uuid = KatelloUtils.grepCLIOutput("Current identity is", getOutput(exec_result).trim(),1);
		
		exec_result = sshOnClient(
				String.format(serverApiCurlTemplate, user_name,KatelloUser.DEFAULT_USER_PASS)+"/consumers/"+uuid);
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", "").contains("User " + user_name + " is not allowed to access api/v1/systems/show"), "Check - access denied output");
		
		exec_result = sshOnClient(
				String.format(serverApiCurlTemplate, 
						System.getProperty("katello.admin.user",KatelloUser.DEFAULT_ADMIN_USER),
						System.getProperty("katello.admin.password",KatelloUser.DEFAULT_ADMIN_PASS))+"/consumers/"+uuid);
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", "").contains(String.format("\"uuid\":\"%s\"",uuid)), "Check - access granted for admin");
	}
	
}
