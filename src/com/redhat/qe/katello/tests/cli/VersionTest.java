package com.redhat.qe.katello.tests.cli;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloVersion;
import com.redhat.qe.katello.base.tngext.TngPriority;
import com.redhat.qe.tools.SSHCommandResult;

@TngPriority(37)
@Test(groups={"headpin-cli","Install / Configuration"})
public class VersionTest extends KatelloCliTestBase{
	private SSHCommandResult exec_result;
	
	@BeforeClass(description="setup", alwaysRun=true)
	public void setUp(){
		
	}
	
	@Test(description = "Version - get the version of the server")
	public void test_Version(){
		KatelloVersion version_obj= new KatelloVersion(cli_worker);
		exec_result = version_obj.cli_version(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).trim().matches(KatelloVersion.REG_VERSION), "Check version output format");
		
		if (KATELLO_PRODUCT.equals("sam") || KATELLO_PRODUCT.equals("headpin")) {
			Assert.assertTrue(getOutput(exec_result).trim().contains("headpin"), "Check version output");
		} else if (KATELLO_PRODUCT.equals("katello")) {
			Assert.assertTrue(getOutput(exec_result).trim().contains("Katello"), "Check version output");
		} else {
			Assert.assertTrue(getOutput(exec_result).trim().contains("CloudForms System Engine"), "Check version output");
		}
	}

	@AfterClass(description="destroy", alwaysRun=true)
	public void tearDown(){
		
	}
}
