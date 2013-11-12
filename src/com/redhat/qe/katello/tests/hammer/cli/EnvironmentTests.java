package com.redhat.qe.katello.tests.hammer.cli;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.HammerDomain;
import com.redhat.qe.katello.base.obj.HammerEnvironment;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

public class EnvironmentTests extends KatelloCliTestBase{

	private SSHCommandResult exec_result;
	private String id;
	private String name;
	private String newName;

	@BeforeClass(description="Prepare data to work with")
	public void setup(){
		String uid = KatelloUtils.getUniqueID();
		this.name = "envName"+uid;
	}

	@Test(description="Create an environment")
	public void test_enviornmentCreate() {
		HammerEnvironment env = new HammerEnvironment(cli_worker, name);
		exec_result = env.cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).equals(HammerEnvironment.OUT_CREATE), "Check - returned output string");
	}

	@Test(description="Create duplicate environment", dependsOnMethods={"test_enviornmentCreate"})
	public void test_duplicateEnvironmentCreate() {
		//Duplicate name
		HammerEnvironment env = new HammerEnvironment(cli_worker, name);
		exec_result = env.cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 65, "Check - error code");
		Assert.assertTrue(getOutput(exec_result).equals(HammerEnvironment.ERR_DUPLICATE_NAME), "Check - returned output string");
	}

	//TODO: create environment name - different variations. Accepts only alphanumeric without spaces

	@Test(description="Verify info of an environment", dependsOnMethods={"test_duplicateEnvironmentCreate"})
	public void test_environmentInfo()
	{
		HammerEnvironment env = new HammerEnvironment(cli_worker, name);
		assert_EnvironmentInfo(env);
		//verify error for invalid env name info
		env = new HammerEnvironment(cli_worker, "invalidName");
		assert_EnvironmentInfo(env);
	}

	//TODO: update using ID of the environment
	@Test(description="update previously created environment", dependsOnMethods={"test_environmentInfo"})
	public void test_environmentUpdate()
	{
		String uid = KatelloUtils.getUniqueID();
		HammerEnvironment env = new HammerEnvironment(cli_worker, name);
		newName = "newName"+uid;
		exec_result = env.update(newName);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(HammerEnvironment.OUT_UPDATE), "Check - returned output string");
		env.name = newName;
		assert_EnvironmentInfo(env);
	}

	//TODO: search, page, order
	@Test(description="List environments. Check if updated name is present", dependsOnMethods={"test_environmentUpdate"})
	public void test_environmentList()
	{
		HammerEnvironment env = new HammerEnvironment(cli_worker, newName);
		exec_result = env.cli_list(null, null, null);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(newName), "Check - updated domain name is listed");
		Assert.assertFalse(getOutput(exec_result).contains(name), "Check - previous namenot present");
	}

	//TODO: delete environment by ID
	@Test(description="delete environment", dependsOnMethods={"test_environmentList"})
	public void test_environmentDelete()
	{
		HammerEnvironment env = new HammerEnvironment(cli_worker, newName);
		exec_result = env.delete();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(HammerEnvironment.OUT_DELETE), "Check - returned output string");
		//should not be listed
		exec_result = env.cli_list(null, null, null);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertFalse(getOutput(exec_result).contains(newName), "Check - deleted domain is not listed");
	}

	private void assert_EnvironmentInfo(HammerEnvironment env) {

		exec_result = env.cli_info();
		if(exec_result.getExitCode().intValue() == 0)
		{
			String match_info = String.format(HammerEnvironment.REG_ENVIRONMENT_INFO, env.name).replaceAll("\"", "");
			log.finest(String.format("Environment (info) match regex: [%s]", match_info));
			Assert.assertTrue(getOutput(exec_result).replaceAll("\n", " ").matches(match_info), "Environment info should match the provided info");
		}
		else
		{
			Assert.assertTrue(exec_result.getExitCode().intValue() == 128, "Check - error code");
			Assert.assertTrue(getOutput(exec_result).equals(HammerEnvironment.ERR_404), "Check - returned output string");
		}
	}

}
