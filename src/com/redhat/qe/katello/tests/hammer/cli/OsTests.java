package com.redhat.qe.katello.tests.hammer.cli;

import com.redhat.qe.Assert;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;

import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.HammerOs;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

public class OsTests extends KatelloCliTestBase {
	
	private SSHCommandResult exec_result;
	private String name;
	
	@BeforeClass(description="Prepare an data to work with")
	public void setup(){
		String uid = KatelloUtils.getUniqueID();
		this.name = "RHEL"+uid;
	}
	
	@Test(description="Create a operating system")
	public void test_osCreate() {
		HammerOs os = new HammerOs(cli_worker, name, "6.2", "6.5");
		exec_result = os.cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(HammerOs.OUT_CREATE), "Check - returned output string");
	}
}
