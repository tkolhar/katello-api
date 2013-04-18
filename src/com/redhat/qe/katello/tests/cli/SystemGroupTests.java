package com.redhat.qe.katello.tests.cli;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.base.obj.KatelloSystemGroup;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.common.TngRunGroups;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups={"cfse-cli","headpin-cli",TngRunGroups.TNG_KATELLO_System_Groups})
public class SystemGroupTests extends KatelloCliTestScript{	
	protected static Logger log = Logger.getLogger(SystemGroupTests.class.getName());
	
	private SSHCommandResult exec_result;
	private String orgName;
	private String systemGroupName;
	private String envName;
	private String systemName;
	private String system_uuid;

	@BeforeClass(description="Generate unique names")
	public void setUp(){
		String uid = KatelloUtils.getUniqueID();
		this.orgName = "org-"+uid;
		this.envName = "Dev-"+uid;
		
		KatelloOrg org = new KatelloOrg(this.orgName, null);
		exec_result = org.cli_create();
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code (org create)");
		Assert.assertTrue(exec_result.getStdout().trim().equals(String.format(KatelloOrg.OUT_CREATE,this.orgName)),
				"Check - returned message");

		// Create the env.
		KatelloEnvironment env = new KatelloEnvironment(this.envName, null, this.orgName, KatelloEnvironment.LIBRARY);
		exec_result = env.cli_create();	
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code (env create)");
		
		KatelloUtils.sshOnClient(KatelloSystem.RHSM_CLEAN);
	}
	
	@Test(description = "Create system group", groups = { "cli-systemgroup" })
	public void test_createSystemGroup() {
		KatelloSystemGroup systemGroup = createSystemGroup();
		
		assert_SystemGroupInfo(systemGroup);
	}
	
	@Test(description = "Create system group which already exists, verify error", groups = { "cli-systemgroup" })
	public void test_createSystemGroupExists() {
		KatelloSystemGroup systemGroup = createSystemGroup();
		
		exec_result = systemGroup.create();
		Assert.assertTrue(exec_result.getExitCode() == 166, "Check - return code");
		//@TODO fix message remove Name when bug #846251 is fixed.
		Assert.assertEquals(getOutput(exec_result).trim(), "Validation failed: Name must be unique within one organization");
	}
	
	@Test(description = "Create system group, than update it's name, description and max systems", groups = { "cli-systemgroup" })
	public void test_updateSystemGroup() {
		KatelloSystemGroup systemGroup = createSystemGroup();
		
		String oldName = systemGroup.name;
		String newName = systemGroup.name + "new";
		String newdescr = "new description";
		
		exec_result = systemGroup.update(newName, newdescr, 1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		systemGroup.name = newName;
		systemGroup.description = newdescr;
		assert_SystemGroupInfo(systemGroup);
		
		systemGroup.name = oldName;
		exec_result = systemGroup.info();
		Assert.assertTrue(exec_result.getExitCode() == 65, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), String.format(KatelloSystemGroup.ERR_SYSTEMGROUP_NOTFOUND, systemGroup.name, orgName));
	}
	
	@Test(description = "Create system group, than copy it by specifying new name, description and max systems", groups = { "cli-systemgroup" })
	public void test_copySystemGroup() {
		KatelloSystemGroup systemGroup = createSystemGroup();
		
		String sgroup_name2 = systemGroup.name + "copy";
		String newdescr = "new description";
		
		exec_result = systemGroup.copy(sgroup_name2, newdescr, 1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), String.format(KatelloSystemGroup.OUT_COPY, systemGroup.name, sgroup_name2));
		
		KatelloSystemGroup systemGroup2 = new KatelloSystemGroup(sgroup_name2, this.orgName, newdescr, 1);
		assert_SystemGroupInfo(systemGroup2);
		
		assert_systemGroupList(Arrays.asList(systemGroup, systemGroup2), new ArrayList<KatelloSystemGroup>());		
	}

	@Test(description = "Update cloned system group max systems", groups = { "cli-systemgroup" })
	public void test_updateCopiedSystemGroup() {
		KatelloSystemGroup systemGroup = createSystemGroup();
		
		String sgroup_name2 = systemGroup.name + "copy";
		String newdescr = "new description";
		
		exec_result = systemGroup.copy(sgroup_name2, newdescr, 1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		systemGroup.name = sgroup_name2;
		systemGroupName = sgroup_name2;
		
		addSystemToSystemGroup(systemGroup);
		
		String sgroup_name3 = systemGroup.name + "copy3";
		exec_result = systemGroup.copy(sgroup_name3, newdescr, -1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		systemGroup.name = sgroup_name3;
		systemGroupName = sgroup_name3;
		
		addSystemToSystemGroup(systemGroup);
	}
	
	@Test(description = "Create system group, add system to it", groups = { "cli-systemgroup" })
	public void test_addSystemToSystemGroup() {
		
		KatelloSystem sys = addSystemToSystemGroup();

		KatelloSystemGroup systemGroup = new KatelloSystemGroup(systemGroupName, orgName);
		systemGroup.totalSystems = 1;
		assert_SystemGroupInfo(systemGroup);
		
		assert_systemList(Arrays.asList(sys), new ArrayList<KatelloSystem>());
	}

	@Test(description = "Remove system group", groups = { "cli-systemgroup" })
	public void test_removeSystemFromSystemGroup() {
		KatelloSystem sys = addSystemToSystemGroup();
		
		KatelloSystemGroup systemGroup = new KatelloSystemGroup(systemGroupName, orgName);
		
		exec_result = systemGroup.remove_systems(system_uuid);
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), String.format(KatelloSystemGroup.OUT_REMOVE_SYSTEMS, systemGroupName));

		systemGroup.totalSystems = 0;
		assert_SystemGroupInfo(systemGroup);
		
		assert_systemList(new ArrayList<KatelloSystem>(), Arrays.asList(sys));
	}
	
	@Test(description = "Add 2 systems to system group which has max systems 1, verify the error", groups = { "cli-systemgroup" })
	public void test_addSystemToLimitedSystemGroup() {
		systemGroupName = "system_group"+KatelloUtils.getUniqueID();
		
		KatelloSystemGroup systemGroup = new KatelloSystemGroup(this.systemGroupName, this.orgName, null, 1);
		exec_result = systemGroup.create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");
		
		KatelloUtils.sshOnClient(KatelloSystem.RHSM_CLEAN);
		
		this.systemName = "localhost-"+KatelloUtils.getUniqueID();
		KatelloSystem sys = new KatelloSystem(systemName, this.orgName, null);
		exec_result = sys.rhsm_registerForce(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		
		exec_result = sys.rhsm_identity();
		system_uuid = KatelloCli.grepCLIOutput("Current identity is", exec_result.getStdout());
		sys.uuid = system_uuid;
		
		exec_result = systemGroup.add_systems(system_uuid);
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), String.format(KatelloSystemGroup.OUT_ADD_SYSTEMS, systemGroupName));
		
		
		KatelloUtils.sshOnClient(KatelloSystem.RHSM_CLEAN);
		
		this.systemName = "localhost-"+KatelloUtils.getUniqueID();
		sys = new KatelloSystem(systemName, this.orgName, null);
		exec_result = sys.rhsm_registerForce(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		
		exec_result = sys.rhsm_identity();
		system_uuid = KatelloCli.grepCLIOutput("Current identity is", exec_result.getStdout());
		sys.uuid = system_uuid;
		
		exec_result = systemGroup.add_systems(system_uuid);
		Assert.assertTrue(exec_result.getExitCode().intValue() == 166, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), String.format(KatelloSystemGroup.ERR_SYSTEMGROUP_EXCEED, "1", systemGroupName));
	}

	@Test(description = "Add 1 system to system group which has max systems 1, then remove it and add another one", groups = { "cli-systemgroup" })
	public void test_addSystemToLimitedSystemGroupAfterRemoving() {
		systemGroupName = "system_group"+KatelloUtils.getUniqueID();
		
		KatelloSystemGroup systemGroup = new KatelloSystemGroup(this.systemGroupName, this.orgName, null, 1);
		exec_result = systemGroup.create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");
		
		KatelloUtils.sshOnClient(KatelloSystem.RHSM_CLEAN);
		
		this.systemName = "localhost-"+KatelloUtils.getUniqueID();
		KatelloSystem sys = new KatelloSystem(systemName, this.orgName, null);
		exec_result = sys.rhsm_registerForce(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		
		exec_result = sys.rhsm_identity();
		system_uuid = KatelloCli.grepCLIOutput("Current identity is", exec_result.getStdout());
		sys.uuid = system_uuid;
		
		exec_result = systemGroup.add_systems(system_uuid);
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), String.format(KatelloSystemGroup.OUT_ADD_SYSTEMS, systemGroupName));
		
		exec_result = systemGroup.remove_systems(system_uuid);
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), String.format(KatelloSystemGroup.OUT_REMOVE_SYSTEMS, systemGroupName));
		
		KatelloUtils.sshOnClient(KatelloSystem.RHSM_CLEAN);
		
		this.systemName = "localhost-"+KatelloUtils.getUniqueID();
		sys = new KatelloSystem(systemName, this.orgName, null);
		exec_result = sys.rhsm_registerForce(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		
		exec_result = sys.rhsm_identity();
		system_uuid = KatelloCli.grepCLIOutput("Current identity is", exec_result.getStdout());
		sys.uuid = system_uuid;
		
		exec_result = systemGroup.add_systems(system_uuid);
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), String.format(KatelloSystemGroup.OUT_ADD_SYSTEMS, systemGroupName));
	}
	
	@Test(description = "Copy system group with system", groups = { "cli-systemgroup" })
	public void test_copySystemGroupWithSystem() {
		KatelloSystem sys = addSystemToSystemGroup();
		
		KatelloSystemGroup systemGroup = new KatelloSystemGroup(systemGroupName, orgName);
		
		systemGroupName = systemGroup.name + "copy";
		String newdescr = "new description";
		
		exec_result = systemGroup.copy(systemGroupName, newdescr, 1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		KatelloSystemGroup systemGroup2 = new KatelloSystemGroup(systemGroupName, this.orgName, newdescr, 1);
		systemGroup2.totalSystems = 1;
		assert_SystemGroupInfo(systemGroup2);
		
		assert_systemList(Arrays.asList(sys), new ArrayList<KatelloSystem>());
	}
	
	@Test(description = "List system groups", groups = { "cli-systemgroup" })
	public void test_listSystemGroup() {
		KatelloSystemGroup systemGroup = createSystemGroup();
		
		String sgroup_name2 = "system_group"+KatelloUtils.getUniqueID();
		KatelloSystemGroup systemGroup2 = new KatelloSystemGroup(sgroup_name2, this.orgName);
		exec_result = systemGroup2.create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");
		
		assert_systemGroupList(Arrays.asList(systemGroup, systemGroup2), new ArrayList<KatelloSystemGroup>());
	}

	@Test(description = "Delete created system group and verify that it is not shown in list", groups = { "cli-systemgroup" })
	public void test_deleteSystemGroup() {
		KatelloSystemGroup systemGroup = createSystemGroup();
		
		String sgroup_name2 = "system_group"+KatelloUtils.getUniqueID();
		KatelloSystemGroup systemGroup2 = new KatelloSystemGroup(sgroup_name2, this.orgName);
		exec_result = systemGroup2.create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");
		
		String sgroupID2 = assert_SystemGroupInfo(systemGroup2);
		
		exec_result = systemGroup2.delete();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), "Deleted system group '" + sgroupID2 + "'");
		
		exec_result = systemGroup2.info();
		Assert.assertTrue(exec_result.getExitCode() == 65, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), String.format(KatelloSystemGroup.ERR_SYSTEMGROUP_NOTFOUND, sgroup_name2, orgName));
		
		
		assert_systemGroupList(Arrays.asList(systemGroup), Arrays.asList(systemGroup2));
	}

	@Test(description = "Delete system group with systems and verify that systems are also deleted", groups = { "cli-systemgroup" })
	public void test_deleteSystemGroupWithSystems() {
		KatelloSystemGroup systemGroup = createSystemGroup();
		
		KatelloSystem system = addSystemToSystemGroup(systemGroup);
		
		exec_result = systemGroup.deleteWithSystems();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), "Deleted system group '" + systemGroupName + "' and it's 1 systems.");
		
		exec_result = systemGroup.info();
		Assert.assertTrue(exec_result.getExitCode() == 65, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), String.format(KatelloSystemGroup.ERR_SYSTEMGROUP_NOTFOUND, systemGroupName, orgName));
		
		
		exec_result = system.list();
		Assert.assertFalse(getOutput(exec_result).trim().contains(system.name), "System should not be in list");
	}
	
	@Test(description = "Delete clonned system group with systems and verify that systems are also deleted", groups = { "cli-systemgroup" })
	public void test_deleteClonedSystemGroupWithSystems() {
		KatelloSystemGroup systemGroup = createSystemGroup();
		
		KatelloSystem system = addSystemToSystemGroup(systemGroup);
		
		systemGroupName = systemGroup.name + "copy";
		String newdescr = "new description";
		
		exec_result = systemGroup.copy(systemGroupName, newdescr, 1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		KatelloSystemGroup systemGroup2 = new KatelloSystemGroup(systemGroupName, this.orgName, newdescr, 1);
		
		exec_result = systemGroup2.deleteWithSystems();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), "Deleted system group '" + systemGroupName + "' and it's 1 systems.");
		
		exec_result = systemGroup2.info();
		Assert.assertTrue(exec_result.getExitCode() == 65, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), String.format(KatelloSystemGroup.ERR_SYSTEMGROUP_NOTFOUND, systemGroupName, orgName));
		
		
		exec_result = system.list();
		Assert.assertFalse(getOutput(exec_result).trim().contains(system.name), "System should not be in list");
	}

	@Test(description = "Add/Delete system from cloned system group", groups = { "cli-systemgroup" })
	public void test_addDeleteSystemsFromClonedSystemGroup() {
		KatelloSystemGroup systemGroup = createSystemGroup();
		
		systemGroupName = systemGroup.name + "copy";
		String newdescr = "new description";
		
		exec_result = systemGroup.copy(systemGroupName, newdescr, -1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		KatelloSystemGroup systemGroup2 = new KatelloSystemGroup(systemGroupName, this.orgName, newdescr, -1);
		
		KatelloSystem system = addSystemToSystemGroup(systemGroup2);
		
		assert_systemList(Arrays.asList(system), new ArrayList<KatelloSystem>());
		
		exec_result = systemGroup2.remove_systems(system_uuid);
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), String.format(KatelloSystemGroup.OUT_REMOVE_SYSTEMS, systemGroupName));
		
		assert_systemList(new ArrayList<KatelloSystem>(), Arrays.asList(system));
	}
	
	private KatelloSystemGroup createSystemGroup() {
		systemGroupName = "system_group"+KatelloUtils.getUniqueID();
		
		KatelloSystemGroup systemGroup = new KatelloSystemGroup(this.systemGroupName, this.orgName);
		exec_result = systemGroup.create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloSystemGroup.OUT_CREATE, systemGroupName)), "Check - output string (system_group create)");
		
		return systemGroup;
	}
	
	private KatelloSystem addSystemToSystemGroup() {	
		return addSystemToSystemGroup(null);
	}
	
	private KatelloSystem addSystemToSystemGroup(KatelloSystemGroup systemGroup) {
		if (systemGroup == null) {
			systemGroup = createSystemGroup();
		}
		
		KatelloUtils.sshOnClient(KatelloSystem.RHSM_CLEAN);
		
		this.systemName = "localhost-"+KatelloUtils.getUniqueID();
		KatelloSystem sys = new KatelloSystem(systemName, this.orgName, null);
		exec_result = sys.rhsm_registerForce(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(exec_result.getStdout().trim().contains(KatelloSystem.OUT_CREATE),
				"Check - output (success)");

		exec_result = sys.info();
		system_uuid = KatelloCli.grepCLIOutput("UUID", getOutput(exec_result).trim(),1);
		sys.uuid = system_uuid;
		
		exec_result = systemGroup.add_systems(system_uuid);
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), String.format(KatelloSystemGroup.OUT_ADD_SYSTEMS, systemGroupName));
		
		return sys;
	}
	
	private void assert_systemGroupList(List<KatelloSystemGroup> systemGroups, List<KatelloSystemGroup> excludeSystemGroups) {
		exec_result = new KatelloSystemGroup(null, orgName).list();

		//system groups that exist in list
		for(KatelloSystemGroup sgroup : systemGroups) {
			if (sgroup.description == null) sgroup.description = "None";
			
			String match_info = String.format(KatelloSystemGroup.REG_SYSTEMGROUP_LIST, sgroup.name).replaceAll("\"", "");
			Assert.assertTrue(getOutput(exec_result).replaceAll("\n", " ").matches(match_info), String.format("System Group [%s] should be found in the result list", sgroup.name));
		}
		
		//system groups that should not exist in list
		for(KatelloSystemGroup sgroup : excludeSystemGroups) {
			if (sgroup.description == null) sgroup.description = "None";
			
			String match_info = String.format(KatelloSystemGroup.REG_SYSTEMGROUP_LIST, sgroup.name).replaceAll("\"", "");
			Assert.assertFalse(getOutput(exec_result).replaceAll("\n", " ").matches(match_info), String.format("System Group [%s] should not be found in the result list", sgroup.name));
		}		
	}

	private void assert_systemList(List<KatelloSystem> systems, List<KatelloSystem> excludeSystems) {
		exec_result = new KatelloSystemGroup(systemGroupName, orgName).list_systems();

		//systems that exist in list
		for(KatelloSystem system : systems) {			
			String match_info = String.format(KatelloSystemGroup.REG_SYSTEM_LIST, system.uuid, system.name).replaceAll("\"", "");
			Assert.assertTrue(getOutput(exec_result).replaceAll("\n", " ").matches(match_info), String.format("System [%s] should be found in the system group [%s] list", system.name, systemGroupName));
		}
		
		//systems that should not exist in list
		for(KatelloSystem system : excludeSystems) {
			String match_info = String.format(KatelloSystemGroup.REG_SYSTEM_LIST, system.uuid, system.name).replaceAll("\"", "");
			Assert.assertFalse(getOutput(exec_result).replaceAll("\n", " ").matches(match_info), String.format("System [%s] should not be found in the system group [%s] list", system.name, systemGroupName));
		}		
	}
	
	private String assert_SystemGroupInfo(KatelloSystemGroup systemGroup) {
		if (systemGroup.description == null) systemGroup.description = "None";

		exec_result = systemGroup.info();

		String match_info = String.format(KatelloSystemGroup.REG_SYSTEMGROUP_INFO, systemGroup.name, systemGroup.description, systemGroup.totalSystems).replaceAll("\"", "");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		log.finest(String.format("System Group (info) match regex: [%s]", match_info));
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", " ").matches(match_info), String.format("System Group [%s] should be found in the result info", systemGroup.name));

		String id = KatelloCli.grepCLIOutput("ID", exec_result.getStdout());
		
		return id;
	}
	
}
