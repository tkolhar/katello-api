package com.redhat.qe.katello.tests.hammer.cli;

import com.redhat.qe.Assert;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;

import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.HammerComputeResource;
import com.redhat.qe.katello.base.obj.HammerOs;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

public class ComputeResourceTests extends KatelloCliTestBase {
	
	private SSHCommandResult exec_result;
	private String name;
	
	@BeforeClass(description="Prepare an data to work with")
	public void setup(){
		String uid = KatelloUtils.getUniqueID();
		this.name = "compres"+uid;
	}
	
	@Test(description="Create a compute resource")
	public void test_computeResourceCreate() {
		HammerComputeResource compRes = new HammerComputeResource(cli_worker, name, "test resource", "Ovirt", "http://localhost/ovirt", "admin", "admin");
		exec_result = compRes.cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(HammerComputeResource.OUT_CREATE), "Check - returned output string");
	}
	
	@Test(description="Create a compute resource duplicate name", dependsOnMethods={"test_computeResourceCreate"})
	public void test_computeResourceCreateDuplicate() {
		HammerComputeResource compRes = new HammerComputeResource(cli_worker, name, "test resource", "Ovirt", "http://localhost/ovirt", "admin", "admin");
		exec_result = compRes.cli_create();
		Assert.assertFalse(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains("Name has already been taken"), "Check - returned output string");
	}
}
