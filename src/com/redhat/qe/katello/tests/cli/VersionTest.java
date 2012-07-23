package com.redhat.qe.katello.tests.cli;
import org.testng.annotations.Test;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.obj.KatelloVersion;

import com.redhat.qe.tools.SSHCommandResult;
@Test(groups={"headpin-cli"})
public class VersionTest extends KatelloCliTestScript{

	
	private SSHCommandResult exec_result;
	@Test(description = "Version - get the version of the server")
	public void test_Version(){
		KatelloVersion version_obj= new KatelloVersion();
		exec_result = version_obj.cli_version(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
	}
}