package com.redhat.qe.katello.tests.cli;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloPackage;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.base.obj.KatelloUser;
import com.redhat.qe.katello.base.tngext.TngPriority;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.common.TngRunGroups;
import com.redhat.qe.tools.SSHCommandResult;

@TngPriority(22)
@Test(groups={"cfse-cli",TngRunGroups.TNG_KATELLO_Content})
public class PackageTests extends KatelloCliTestBase {

	protected static Logger log = Logger.getLogger(PackageTests.class.getName());	
	private String user_name;

	
	@BeforeClass(description="Generate unique objects")
	public void setUp(){
		user_name = "user"+KatelloUtils.getUniqueID();
		// Create user:
		KatelloUser user = new KatelloUser(cli_worker, user_name, "root@localhost", KatelloUser.DEFAULT_USER_PASS, false);
		exec_result = user.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	}
	
	@Test(description="package list", groups = {"cli-packages"}, enabled=true)
	public void test_packageList() {
		
		KatelloPackage pack = new KatelloPackage(cli_worker, null, null, base_org_name, base_pulp_product_name, base_pulp_repo_name, null);
		
		exec_result = pack.cli_list();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		Assert.assertTrue(getOutput(exec_result).contains("pulp-admin"),"is package in the list: pulp-admin");
		Assert.assertTrue(getOutput(exec_result).contains("pulp"),"is package in the list: pulp");
		Assert.assertTrue(getOutput(exec_result).contains("python-gofer"),"is package in the list: python-gofer");
		Assert.assertTrue(getOutput(exec_result).contains("python-qpid"),"is package in the list: python-qpid");
		Assert.assertTrue(getOutput(exec_result).contains("pulp-agent"),"is package in the list: pulp-agent");
		Assert.assertTrue(getOutput(exec_result).contains("pulp-consumer"),"is package in the list: pulp-consumer");
		
	}
	
	@Test(description="package search", groups = {"cli-packages"}, enabled=true)
	public void test_packageSearch() {
		KatelloRepo repo = new KatelloRepo(this.cli_worker, base_pulp_repo_name, base_org_name, base_pulp_product_name, null, null, null);
		waitfor_repodata(repo, 1);

		KatelloPackage pack = new KatelloPackage(cli_worker, null, null, base_org_name, base_pulp_product_name, base_pulp_repo_name, null);
		exec_result = pack.cli_search("pulp-agent*");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		Assert.assertTrue(getOutput(exec_result).contains("pulp-agent"), "Check - package name should exist in list result");
		
		Pattern pattern = Pattern.compile(KatelloPackage.REG_PACKAGE_ID);
		Matcher matcher = pattern.matcher(getOutput(exec_result));
		
		Assert.assertTrue(matcher.find(), "Check - package Id should exist in list result");
		
		String id = matcher.group();
		pack.id = id;
		pack.name = "pulp-agent";
		
		assert_packageInfo(pack);
		
	}

	private void assert_packageInfo(KatelloPackage pack){
		SSHCommandResult res;
		res = pack.cli_info();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	
		Assert.assertTrue(getOutput(res).contains(pack.name), "Check - package name should exist in list result");
	}
}
