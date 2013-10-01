package com.redhat.qe.katello.tests.cli;

import org.testng.annotations.Test;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloAbout;
import com.redhat.qe.tools.SSHCommandResult;

public class AboutTests extends KatelloCliTestBase {

	public SSHCommandResult exec_result;

	// TODO bz# 996895 
	@Test(description="test about command")
	public void test_about() {
		KatelloAbout about = new KatelloAbout(cli_worker);
		exec_result = about.about();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (about)");
		Assert.assertFalse(getOutput(exec_result).equals(""), "Check output (about)");
	}
}
