package com.redhat.qe.katello.tests.e2e;

import java.util.logging.Logger;

import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloPackage;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups={"cfse-e2e"}, singleThreaded = true)
public class PromotePackageWithDashes extends KatelloCliTestBase {
	protected static Logger log = Logger.getLogger(PromotePackageWithDashes.class.getName());

	private final String packageName = "pulp-selinux-server";
	
	@Test(description = "promote package with dashes to environment", groups = { "cli-changeset" })
	public void test_promotePackage() {
		String package_content_view = KatelloUtils.promotePackagesToEnvironment(cli_worker, base_org_name, base_pulp_product_name, base_pulp_repo_name, new String[] {packageName}, base_dev_env_name);
		
		KatelloPackage pack = new KatelloPackage(cli_worker, base_org_name, base_pulp_product_name, base_pulp_repo_name, package_content_view);
		SSHCommandResult res = pack.cli_list();
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").contains(packageName), "Check - package list output");
	}
	
}
