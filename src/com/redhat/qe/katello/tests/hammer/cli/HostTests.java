package com.redhat.qe.katello.tests.hammer.cli;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.HammerArchitecture;
import com.redhat.qe.katello.base.obj.HammerDomain;
import com.redhat.qe.katello.base.obj.HammerEnvironment;
import com.redhat.qe.katello.base.obj.HammerHost;
import com.redhat.qe.katello.base.obj.HammerOs;
import com.redhat.qe.katello.base.obj.HammerPartitionTable;
import com.redhat.qe.katello.common.KatelloUtils;

public class HostTests extends KatelloCliTestBase {
	
	private String name;
	private String environment_id;
	private String architecture_id;
	private String domain_id;
	private String puppet_proxy_id = "1";
	private String operatingsystem_id;
	private String ptable_id;
	
	@BeforeClass(description="Prepare an data to work with")
	public void setUp(){
		String uid = KatelloUtils.getUniqueID().substring(7);
		this.name = "host"+uid;
		
		HammerEnvironment env = new HammerEnvironment(cli_worker, "env"+uid);
		exec_result = env.cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		exec_result = env.cli_info();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		environment_id = KatelloUtils.grepCLIOutput("Id", getOutput(exec_result));
		
		HammerArchitecture arch = new HammerArchitecture(cli_worker, "anch"+uid);
		exec_result = arch.cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		exec_result = arch.cli_info();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		architecture_id = KatelloUtils.grepCLIOutput("Id", getOutput(exec_result));
		
		HammerDomain dmn = new HammerDomain(cli_worker, "dmn"+uid);
		exec_result = dmn.cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		exec_result = dmn.cli_info();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		domain_id = KatelloUtils.grepCLIOutput("Id", getOutput(exec_result));
		
		HammerOs os = new HammerOs(cli_worker, "RHEL"+uid, "6.2", "6.5");
		exec_result = os.cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		exec_result = os.cli_info(os.name);
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		operatingsystem_id = KatelloUtils.grepCLIOutput("Id", getOutput(exec_result));
			
		KatelloUtils.sshOnServer("echo \"test\" > /tmp/ptableinfo"+uid);
		HammerPartitionTable ptable = new HammerPartitionTable(cli_worker, "ptb"+uid, "RHEL");
		exec_result = ptable.cli_create("/tmp/ptableinfo"+uid);
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		exec_result = ptable.cli_info();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		ptable_id = KatelloUtils.grepCLIOutput("Id", getOutput(exec_result));
		
		exec_result = ptable.add_os(operatingsystem_id);
		//@ TODO bz#1027170
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
	}
	
	@Test(description="create Host")
	public void testHost_create() {
		
		HammerHost hst = new HammerHost(cli_worker, name, environment_id, architecture_id, domain_id, puppet_proxy_id, operatingsystem_id, "10.10.10.10", "00:1A:4A:22:83:9B", ptable_id);
		exec_result = hst.cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		
		Assert.assertTrue(getOutput(exec_result).contains(String.format(HammerHost.OUT_CREATE, name)),"Check - returned output string");
	}

	@Test(description="create Host which name exists", dependsOnMethods={"testHost_create"})
	public void testHost_createExists() {
		
		HammerHost hst = new HammerHost(cli_worker, name, environment_id, architecture_id, domain_id, puppet_proxy_id, operatingsystem_id, "10.10.10.10", "00:1A:4A:22:83:9B", ptable_id);
		exec_result = hst.cli_create();
		Assert.assertFalse(exec_result.getExitCode().intValue() == 0, "Check - return code");

		Assert.assertTrue(getOutput(exec_result).contains(HammerHost.ERR_NAME_EXISTS),"Check - returned error string");
	}
}
