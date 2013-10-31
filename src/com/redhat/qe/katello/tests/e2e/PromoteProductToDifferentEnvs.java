package com.redhat.qe.katello.tests.e2e;

import java.util.logging.Logger;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.tngext.TngPriority;
import com.redhat.qe.katello.common.KatelloUtils;

@TngPriority(19000)
public class PromoteProductToDifferentEnvs extends KatelloCliTestBase {
	protected static Logger log = Logger.getLogger(PromoteProductToDifferentEnvs.class.getName());

	private String env_name;
	private String env_name2;
	
	@BeforeClass(description="Generate unique objects")
	public void setUp() {
		String uid = KatelloUtils.getUniqueID();
		env_name = "env1"+uid;	
		env_name2 = "env2"+uid;
		
		KatelloEnvironment env = new KatelloEnvironment(this.cli_worker, env_name, null, base_org_name, KatelloEnvironment.LIBRARY);
		exec_result = env.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		KatelloUtils.promoteProductToEnvironment(cli_worker, base_org_name, base_zoo_product_name, env_name);

		env = new KatelloEnvironment(this.cli_worker, env_name2, null, base_org_name, KatelloEnvironment.LIBRARY);
		exec_result = env.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	}
	
	@Test(description = "Promote product to second environment", groups = { "cli-changeset" })
	public void test_promoteProduct() {
		KatelloUtils.promoteProductToEnvironment(cli_worker, base_org_name, base_zoo_product_name, env_name2);
	}
}