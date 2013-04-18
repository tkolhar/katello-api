package com.redhat.qe.katello.tests.cli;
import org.testng.annotations.Test;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.obj.KatelloVersion;

import com.redhat.qe.tools.SSHCommandResult;
@Test(groups={"headpin-cli","Install / Configuration"})
public class VersionTest extends KatelloCliTestScript{
	private SSHCommandResult exec_result;
	
	@Test(description = "Version - get the version of the server")
	public void test_Version(){
		KatelloVersion version_obj= new KatelloVersion();
		exec_result = version_obj.cli_version(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).trim().matches(KatelloVersion.REG_VERSION), "Check version output format");
		
		if (KATELLO_PRODUCT.equals("sam") || KATELLO_PRODUCT.equals("headpin")) {
			Assert.assertTrue(getOutput(exec_result).trim().contains("Headpin"), "Check version output");
		} else if (KATELLO_PRODUCT.equals("katello")) {
			Assert.assertTrue(getOutput(exec_result).trim().contains("Katello"), "Check version output");
		} else {
			Assert.assertTrue(getOutput(exec_result).trim().contains("CloudForms System Engine"), "Check version output");
		}
		
	}
}
