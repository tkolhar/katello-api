package com.redhat.qe.katello.tests.cli;
import org.testng.annotations.Test;
import com.redhat.qe.auto.testng.Assert;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.obj.KatelloPing;

import com.redhat.qe.tools.SSHCommandResult;
@Test(groups={"headpin-cli"})
public class PingTest extends KatelloCliTestScript{

	
	private SSHCommandResult exec_result;
	@Test(description = "Ping - get the status of the katello server")
	public void test_Ping(){
		KatelloPing ping_obj= new KatelloPing();
		exec_result = ping_obj.cli_ping(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
	}
}
