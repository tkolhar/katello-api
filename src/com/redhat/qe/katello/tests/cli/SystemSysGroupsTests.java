package com.redhat.qe.katello.tests.cli;

import java.util.logging.Logger;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.base.obj.KatelloSystemGroup;
import com.redhat.qe.katello.base.tngext.TngPriority;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.common.TngRunGroups;

/**
 * Scenarios in here <b>should</b> go strictly one by one (each time there might be another scenarios doing unregister or whatever else there).
 * @author gkhachik
 *
 */
@TngPriority(300)
@Test(groups={TngRunGroups.TNG_KATELLO_System_Consumer})
public class SystemSysGroupsTests extends KatelloCliTestBase{	
	protected static Logger log = Logger.getLogger(SystemSysGroupsTests.class.getName());
	
	private String sysgroup_name;
	private String org_name;
	private String sys_name;
	private String sys_nonexist_name;
	private String grp_nonexist_name;

	@BeforeClass(description="Generate unique names",groups={"headpin-cli"})
	public void setUp(){
		String uid = KatelloUtils.getUniqueID();
		
		org_name = "org"+uid;
		sys_name = "system"+uid;
		sys_nonexist_name = "nonexisting-system"+uid;
		sysgroup_name = "sysgroup"+uid;
		grp_nonexist_name = "sysgroup-nonexist"+uid;

		exec_result = new KatelloOrg(cli_worker, org_name, null).cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check exit code (org create)");
		KatelloSystemGroup sysgroup = new KatelloSystemGroup(cli_worker, sysgroup_name, org_name);
		exec_result = sysgroup.create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check exit code (system group create)");
	}

	@Test(description="add system to system group")
	public void test_addToGroup() {
		rhsm_clean();
		KatelloSystem sys = new KatelloSystem(cli_worker, sys_name, org_name, "Library");
		exec_result = sys.register();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (system register)");
		exec_result = sys.add_to_groups(sysgroup_name);
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (add system to group)");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloSystem.OUT_ADD_TO_GROUPS, sys_name)), "Check output (add system to group)");
	}

	@Test(description="add system to nonexisting group", dependsOnMethods={"test_addToGroup"})
	public void test_addToNonexistGroup() {
		KatelloSystem sys = new KatelloSystem(cli_worker, sys_name, org_name, "Library");
		exec_result = sys.add_to_groups(grp_nonexist_name);
		Assert.assertTrue(exec_result.getExitCode()==65, "Check exit code (add system to group)");
		Assert.assertTrue(getOutput(exec_result).equals(KatelloSystem.ERR_COULD_NOT_FIND_GROUP), "Check output (add system to group)");
	}

	@Test(description="remove system from system group - group not found", dependsOnMethods={"test_addToGroup"})
	public void test_removeFromNonexistGroup() {
		KatelloSystem sys = new KatelloSystem(cli_worker, sys_name, org_name, "Library");
		exec_result = sys.remove_from_groups(grp_nonexist_name);
		Assert.assertTrue(exec_result.getExitCode()==65, "Check exit code (remove system from group)");
		Assert.assertTrue(getOutput(exec_result).equals(KatelloSystem.ERR_COULD_NOT_FIND_GROUP), "Check output (remove system from group)");
	}

	@Test(description="remove system from sytem groups", dependsOnMethods={"test_addToGroup","test_addToNonexistGroup","test_removeFromNonexistGroup"})
	public void test_removeFromGroup() {
		KatelloSystem sys = new KatelloSystem(cli_worker, sys_name, org_name, "Library");
		exec_result = sys.remove_from_groups(sysgroup_name);
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (remove system from group)");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloSystem.OUT_REMOVE_FROM_GROUPS, sys_name)), "Check output (remove system from group)");
	}

	
	@Test(description="add nonexisting system to group")
	public void test_addNonexistSystemToGroup() {
		KatelloSystem sys = new KatelloSystem(cli_worker, sys_nonexist_name, org_name, "Library");
		exec_result = sys.add_to_groups(sysgroup_name);
		Assert.assertTrue(exec_result.getExitCode()==65, "Check exit code (add system to group)");
		Assert.assertTrue(getOutput(exec_result).equals(""), "Check output (add system to group)");
	}

	@Test(description="remove system from system group - system not found")
	public void test_removeNonexistSystemFromGroup() {
		KatelloSystem sys = new KatelloSystem(cli_worker, sys_nonexist_name, org_name, "Library");
		exec_result = sys.remove_from_groups(sysgroup_name);
		Assert.assertTrue(exec_result.getExitCode()==65, "Check exit code (remove system from group)");
		Assert.assertTrue(getOutput(exec_result).equals(""), "Check output (system remove from group)");
	}	
}
