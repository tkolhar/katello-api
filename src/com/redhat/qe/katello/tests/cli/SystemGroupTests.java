package com.redhat.qe.katello.tests.cli;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloContentDefinition;
import com.redhat.qe.katello.base.obj.KatelloContentView;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.base.obj.KatelloSystemGroup;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.common.TngRunGroups;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups={TngRunGroups.TNG_KATELLO_System_Groups})
public class SystemGroupTests extends KatelloCliTestBase{	
	protected static Logger log = Logger.getLogger(SystemGroupTests.class.getName());
	
	String uid = KatelloUtils.getUniqueID();
	private SSHCommandResult exec_result;
	private String orgName= "org-"+uid;
	private String systemGroupName;
	private String envName = null; // initially - for headpin
	private String systemName;
	private String system_uuid;
	private String contentName;
	private String contentView;
	private String sysgroup_name;

	@BeforeClass(description="Generate unique objects", groups={"cfse-cli","headpin-cli"})
	public void setUp() {
		KatelloOrg org = new KatelloOrg(this.cli_worker, this.orgName, null);
		exec_result = org.cli_create();
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code (org create)");
		Assert.assertTrue(exec_result.getStdout().trim().equals(String.format(KatelloOrg.OUT_CREATE,this.orgName)),
				"Check - returned message");
		
		sshOnClient(KatelloSystem.RHSM_CLEAN);
	}
	
	@BeforeClass(description="init: katello specific, no headpin",groups={"cfse-cli"}, dependsOnMethods={"setUp"})
	public void setUp_katelloOnly(){
		this.envName = "Dev-"+uid;
		this.contentName = "content-" + uid;
		this.contentView = "contentView-"+uid;
		// Create the env.
		KatelloEnvironment env = new KatelloEnvironment(this.cli_worker, this.envName, null, this.orgName, KatelloEnvironment.LIBRARY);
		exec_result = env.cli_create();	
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code (env create)");
		// Associate a content view to the env.
		KatelloContentDefinition content = new KatelloContentDefinition(this.cli_worker, contentName, "descritpion", this.orgName, contentName);
		exec_result = content.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = content.publish(contentView, contentView, "New Content View");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		KatelloContentView contentView = new KatelloContentView(this.cli_worker, this.contentView, this.orgName);
		exec_result = contentView.promote_view(this.envName);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		
	}
	
	@Test(description = "Create system group", groups = { "cli-systemgroup", "cfse-cli", "headpin-cli" })
	public void test_createSystemGroup() {
		KatelloSystemGroup systemGroup = createSystemGroup();
		
		assert_SystemGroupInfo(systemGroup);
	}
	
	@Test(description = "Create system group which already exists, verify error", groups = { "cli-systemgroup", "cfse-cli", "headpin-cli" })
	public void test_createSystemGroupExists() {
		KatelloSystemGroup systemGroup = createSystemGroup();
		
		exec_result = systemGroup.create();
		Assert.assertTrue(exec_result.getExitCode() == 166, "Check - return code");
		//@TODO fix message remove Name when bug #846251 is fixed.
		Assert.assertEquals(getOutput(exec_result).trim(), "Validation failed: Name must be unique within one organization");
	}
	
	@Test(description = "Create system group, than update it's name, description and max systems", groups = { "cli-systemgroup", "cfse-cli", "headpin-cli" })
	public void test_updateSystemGroup() {
		KatelloSystemGroup systemGroup = createSystemGroup();
		
		String oldName = systemGroup.name;
		String newName = systemGroup.name + "new";
		String newdescr = "new description";
		
		exec_result = systemGroup.update(newName, newdescr, 1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		systemGroup.name = newName;
		systemGroup.description = newdescr;
		systemGroup.maxSystems = 1;
		assert_SystemGroupInfo(systemGroup);
		
		systemGroup.name = oldName;
		exec_result = systemGroup.info();
		Assert.assertTrue(exec_result.getExitCode() == 65, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(KatelloSystemGroup.ERR_SYSTEMGROUP_NOTFOUND));
	}
	
	@Test(description = "Create system group, than copy it by specifying new name, description and max systems", groups = { "cli-systemgroup", "cfse-cli", "headpin-cli" })
	public void test_copySystemGroup() {
		KatelloSystemGroup systemGroup = createSystemGroup();
		
		String sgroup_name2 = systemGroup.name + "copy";
		String newdescr = "new description";
		
		exec_result = systemGroup.copy(sgroup_name2, newdescr, 1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), String.format(KatelloSystemGroup.OUT_COPY, systemGroup.name, sgroup_name2));
		
		KatelloSystemGroup systemGroup2 = new KatelloSystemGroup(this.cli_worker, sgroup_name2, this.orgName, newdescr, 1);
		assert_SystemGroupInfo(systemGroup2);
		
		assert_systemGroupList(Arrays.asList(systemGroup, systemGroup2), new ArrayList<KatelloSystemGroup>());		
	}

	@Test(description = "Update cloned system group max systems", groups = { "cli-systemgroup", "cfse-cli", "headpin-cli" })
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
	
	@Test(description = "Create system group, add system to it", groups = { "cli-systemgroup", "cfse-cli", "headpin-cli" })
	public void test_addSystemToSystemGroup() {
		
		KatelloSystem sys = addSystemToSystemGroup();

		KatelloSystemGroup systemGroup = new KatelloSystemGroup(this.cli_worker, systemGroupName, orgName);
		systemGroup.totalSystems = 1;
		assert_SystemGroupInfo(systemGroup);
		
		assert_systemList(Arrays.asList(sys), new ArrayList<KatelloSystem>());
	}

	@Test(description = "Remove system group", groups = { "cli-systemgroup", "cfse-cli", "headpin-cli" })
	public void test_removeSystemFromSystemGroup() {
		KatelloSystem sys = addSystemToSystemGroup();
		
		KatelloSystemGroup systemGroup = new KatelloSystemGroup(this.cli_worker, systemGroupName, orgName);
		
		exec_result = systemGroup.remove_systems(system_uuid);
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).trim().matches(String.format(KatelloSystemGroup.OUT_REMOVE_SYSTEMS, systemGroupName)));

		systemGroup.totalSystems = 0;
		assert_SystemGroupInfo(systemGroup);
		
		assert_systemList(new ArrayList<KatelloSystem>(), Arrays.asList(sys));
	}
	
	@Test(description = "Add 2 systems to system group which has max systems 1, verify the error", groups = { "cli-systemgroup", "cfse-cli", "headpin-cli" })
	public void test_addSystemToLimitedSystemGroup() {
		systemGroupName = "system_group"+KatelloUtils.getUniqueID();
		
		KatelloSystemGroup systemGroup = new KatelloSystemGroup(this.cli_worker, this.systemGroupName, this.orgName, null, 1);
		exec_result = systemGroup.create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");
		
		rhsm_clean();
		
		this.systemName = "localhost-"+KatelloUtils.getUniqueID();
		KatelloSystem sys = new KatelloSystem(this.cli_worker, systemName, this.orgName, this.envName);
		exec_result = sys.rhsm_registerForce(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		
		exec_result = sys.rhsm_identity();
		system_uuid = KatelloUtils.grepCLIOutput("Current identity is", exec_result.getStdout());
		sys.uuid = system_uuid;
		
		exec_result = systemGroup.add_systems(system_uuid);
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).trim().matches(String.format(KatelloSystemGroup.OUT_ADD_SYSTEMS, systemGroupName)));
		
		
		sshOnClient(KatelloSystem.RHSM_CLEAN);
		
		this.systemName = "localhost-"+KatelloUtils.getUniqueID();
		sys = new KatelloSystem(this.cli_worker, systemName, this.orgName, this.envName);
		exec_result = sys.rhsm_registerForce(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		
		exec_result = sys.rhsm_identity();
		system_uuid = KatelloUtils.grepCLIOutput("Current identity is", exec_result.getStdout());
		sys.uuid = system_uuid;
		
		exec_result = systemGroup.add_systems(system_uuid);
		Assert.assertTrue(exec_result.getExitCode().intValue() == 166, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), String.format(KatelloSystemGroup.ERR_SYSTEMGROUP_EXCEED, "1", systemGroupName));
	}

	@Test(description = "Add 1 system to system group which has max systems 1, then remove it and add another one", groups = { "cli-systemgroup", "cfse-cli", "headpin-cli" })
	public void test_addSystemToLimitedSystemGroupAfterRemoving() {
		systemGroupName = "system_group"+KatelloUtils.getUniqueID();
		
		KatelloSystemGroup systemGroup = new KatelloSystemGroup(this.cli_worker, this.systemGroupName, this.orgName, null, 1);
		exec_result = systemGroup.create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");
		
		sshOnClient(KatelloSystem.RHSM_CLEAN);
		
		this.systemName = "localhost-"+KatelloUtils.getUniqueID();
		KatelloSystem sys = new KatelloSystem(this.cli_worker, systemName, this.orgName, this.envName);
		exec_result = sys.rhsm_registerForce(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		
		exec_result = sys.rhsm_identity();
		system_uuid = KatelloUtils.grepCLIOutput("Current identity is", exec_result.getStdout());
		sys.uuid = system_uuid;
		
		exec_result = systemGroup.add_systems(system_uuid);
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).trim().matches(String.format(KatelloSystemGroup.OUT_ADD_SYSTEMS, systemGroupName)));
		
		exec_result = systemGroup.remove_systems(system_uuid);
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).trim().matches(String.format(KatelloSystemGroup.OUT_REMOVE_SYSTEMS, systemGroupName)));
		
		sshOnClient(KatelloSystem.RHSM_CLEAN);
		
		this.systemName = "localhost-"+KatelloUtils.getUniqueID();
		sys = new KatelloSystem(this.cli_worker, systemName, this.orgName, this.envName);
		exec_result = sys.rhsm_registerForce(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		
		exec_result = sys.rhsm_identity();
		system_uuid = KatelloUtils.grepCLIOutput("Current identity is", exec_result.getStdout());
		sys.uuid = system_uuid;
		
		exec_result = systemGroup.add_systems(system_uuid);
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).trim().matches(String.format(KatelloSystemGroup.OUT_ADD_SYSTEMS, systemGroupName)));
	}
	
	@Test(description = "Copy system group with system", groups = { "cli-systemgroup", "cfse-cli", "headpin-cli" })
	public void test_copySystemGroupWithSystem() {
		KatelloSystem sys = addSystemToSystemGroup();
		
		KatelloSystemGroup systemGroup = new KatelloSystemGroup(this.cli_worker, systemGroupName, orgName);
		
		systemGroupName = systemGroup.name + "copy";
		String newdescr = "new description";
		
		exec_result = systemGroup.copy(systemGroupName, newdescr, 1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		KatelloSystemGroup systemGroup2 = new KatelloSystemGroup(this.cli_worker, systemGroupName, this.orgName, newdescr, 1);
		systemGroup2.totalSystems = 1;
		assert_SystemGroupInfo(systemGroup2);
		
		assert_systemList(Arrays.asList(sys), new ArrayList<KatelloSystem>());
	}
	
	@Test(description = "List system groups", groups = { "cli-systemgroup", "cfse-cli", "headpin-cli" })
	public void test_listSystemGroup() {
		KatelloSystemGroup systemGroup = createSystemGroup();
		
		String sgroup_name2 = "system_group"+KatelloUtils.getUniqueID();
		KatelloSystemGroup systemGroup2 = new KatelloSystemGroup(this.cli_worker, sgroup_name2, this.orgName);
		exec_result = systemGroup2.create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");
		
		assert_systemGroupList(Arrays.asList(systemGroup, systemGroup2), new ArrayList<KatelloSystemGroup>());
	}

	@Test(description = "Delete created system group and verify that it is not shown in list", groups = { "cli-systemgroup", "cfse-cli", "headpin-cli" })
	public void test_deleteSystemGroup() {
		KatelloSystemGroup systemGroup = createSystemGroup();
		
		String sgroup_name2 = "system_group"+KatelloUtils.getUniqueID();
		KatelloSystemGroup systemGroup2 = new KatelloSystemGroup(this.cli_worker, sgroup_name2, this.orgName);
		exec_result = systemGroup2.create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");
		
		String sgroupID2 = assert_SystemGroupInfo(systemGroup2);
		
		exec_result = systemGroup2.delete();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), "Deleted system group '" + sgroupID2 + "'");
		
		exec_result = systemGroup2.info();
		Assert.assertTrue(exec_result.getExitCode() == 65, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(KatelloSystemGroup.ERR_SYSTEMGROUP_NOTFOUND));
		
		assert_systemGroupList(Arrays.asList(systemGroup), Arrays.asList(systemGroup2));
	}

	@Test(description = "Delete system group with systems and verify that systems are also deleted", groups = { "cli-systemgroup", "cfse-cli", "headpin-cli" })
	public void test_deleteSystemGroupWithSystems() {
		KatelloSystemGroup systemGroup = createSystemGroup();
		
		KatelloSystem system = addSystemToSystemGroup(systemGroup);
		
		exec_result = systemGroup.deleteWithSystems();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), "Deleted system group '" + systemGroupName + "' and it's 1 systems.");
		
		exec_result = systemGroup.info();
		Assert.assertTrue(exec_result.getExitCode() == 65, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(KatelloSystemGroup.ERR_SYSTEMGROUP_NOTFOUND));
		
		
		exec_result = system.list();
		Assert.assertFalse(getOutput(exec_result).trim().contains(system.name), "System should not be in list");
	}
	
	@Test(description = "Delete clonned system group with systems and verify that systems are also deleted", groups = { "cli-systemgroup", "cfse-cli", "headpin-cli" })
	public void test_deleteClonedSystemGroupWithSystems() {
		KatelloSystemGroup systemGroup = createSystemGroup();
		
		KatelloSystem system = addSystemToSystemGroup(systemGroup);
		
		systemGroupName = systemGroup.name + "copy";
		String newdescr = "new description";
		
		exec_result = systemGroup.copy(systemGroupName, newdescr, 1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		KatelloSystemGroup systemGroup2 = new KatelloSystemGroup(this.cli_worker, systemGroupName, this.orgName, newdescr, 1);
		
		exec_result = systemGroup2.deleteWithSystems();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), "Deleted system group '" + systemGroupName + "' and it's 1 systems.");
		
		exec_result = systemGroup2.info();
		Assert.assertTrue(exec_result.getExitCode() == 65, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(KatelloSystemGroup.ERR_SYSTEMGROUP_NOTFOUND));
		
		exec_result = system.list();
		Assert.assertFalse(getOutput(exec_result).trim().contains(system.name), "System should not be in list");
	}

	@Test(description = "Add/Delete system from cloned system group", groups = { "cli-systemgroup", "cfse-cli", "headpin-cli" })
	public void test_addDeleteSystemsFromClonedSystemGroup() {
		KatelloSystemGroup systemGroup = createSystemGroup();
		
		systemGroupName = systemGroup.name + "copy";
		String newdescr = "new description";
		
		exec_result = systemGroup.copy(systemGroupName, newdescr, -1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		KatelloSystemGroup systemGroup2 = new KatelloSystemGroup(this.cli_worker, systemGroupName, this.orgName, newdescr, -1);
		
		KatelloSystem system = addSystemToSystemGroup(systemGroup2);
		
		assert_systemList(Arrays.asList(system), new ArrayList<KatelloSystem>());
		
		exec_result = systemGroup2.remove_systems(system_uuid);
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).trim().matches(String.format(KatelloSystemGroup.OUT_REMOVE_SYSTEMS, systemGroupName)));
		
		assert_systemList(new ArrayList<KatelloSystem>(), Arrays.asList(system));
	}

	@Test(description="system_group job_history")
	public void test_jobHistory() {
		String sys_name = "system"+KatelloUtils.getUniqueID();
		KatelloSystemGroup group = createSystemGroup();
		sysgroup_name = group.name;
		// create dumy job just to list job history
		KatelloSystem sys = new KatelloSystem(cli_worker, sys_name, orgName, envName);
		exec_result = sys.rhsm_registerForce();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (system reg)");
		exec_result = sys.add_to_groups(sysgroup_name);
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (system add group)");
		group.packages_install("lion");

		exec_result = group.job_history();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (sysgroup job_tasks)");
		String jobID = KatelloUtils.grepCLIOutput("ID", getOutput(exec_result));
		Assert.assertNotNull(jobID, "Check job id not null");
		exec_result = group.job_tasks(jobID);
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (sysgroup job_tasks)");
	}

	// TODO bz#985412
	@Test(description="system_group job_tasks test - bad id", dependsOnMethods={"test_jobHistory"})
	public void test_jobTasksBadID() {
		KatelloSystemGroup group = new KatelloSystemGroup(cli_worker, sysgroup_name, orgName);
		exec_result = group.job_tasks("0");
		Assert.assertTrue(exec_result.getExitCode()!=0, "Check exit code (sysgroup job_tasks)");
		Assert.assertFalse(getOutput(exec_result).contains("error: 'tasks'"), "Check error (sysgroup job_tasks)");
	}

	@Test(description="system_group update systems - change systems environment and content view")
	public void test_updateSystems() {
		String uid =  KatelloUtils.getUniqueID();
		String sys_name1 = "system"+uid;
		KatelloSystemGroup group = createSystemGroup();
		KatelloSystem sys1 = new KatelloSystem(cli_worker, sys_name1, orgName, "Library");
		exec_result = sys1.register();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (sys reg)");
		exec_result = sys1.add_to_groups(group.name);
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (sys add group)");

		String def_name = "definition"+uid;
		String view_name = "view"+uid;
		KatelloContentDefinition def = new KatelloContentDefinition(cli_worker, def_name, null, orgName, null);
		def.create();
		def.publish(view_name, null, null);
		KatelloContentView view = new KatelloContentView(cli_worker, view_name, orgName);
		view.promote_view(envName);

		exec_result = group.update_systems(envName, view_name);
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (group update systems)");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloSystemGroup.OUT_UPDATE_SYSTEMS, group.name)), "Check output (group update systems)");

		exec_result = sys1.info();
		String sys_env = KatelloUtils.grepCLIOutput("Environment", getOutput(exec_result));
		Assert.assertTrue(envName.equals(sys_env), "Check output (environment set)");
		Assert.assertTrue(getOutput(exec_result).contains(view_name), "Check output (view set)");
	}
	
	private KatelloSystemGroup createSystemGroup() {
		systemGroupName = "system_group"+KatelloUtils.getUniqueID();
		
		KatelloSystemGroup systemGroup = new KatelloSystemGroup(this.cli_worker, this.systemGroupName, this.orgName);
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
		
		rhsm_clean();
		
		this.systemName = "localhost-"+KatelloUtils.getUniqueID();
		KatelloSystem sys = new KatelloSystem(this.cli_worker, systemName, this.orgName, this.envName);
		exec_result = sys.rhsm_register();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(exec_result.getStdout().trim().contains(KatelloSystem.OUT_CREATE),
				"Check - output (success)");

		exec_result = sys.info();
		system_uuid = KatelloUtils.grepCLIOutput("UUID", getOutput(exec_result).trim(),1);
		sys.uuid = system_uuid;
		
		exec_result = systemGroup.add_systems(system_uuid);
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).trim().matches(String.format(KatelloSystemGroup.OUT_ADD_SYSTEMS, systemGroupName)));
		
		return sys;
	}
	
	private void assert_systemGroupList(List<KatelloSystemGroup> systemGroups, List<KatelloSystemGroup> excludeSystemGroups) {
		exec_result = new KatelloSystemGroup(this.cli_worker, null, orgName).list();

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
		exec_result = new KatelloSystemGroup(this.cli_worker, systemGroupName, orgName).list_systems();

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
		if (systemGroup.maxSystems == null) systemGroup.maxSystems = -1;

		exec_result = systemGroup.info();

		String match_info = String.format(KatelloSystemGroup.REG_SYSTEMGROUP_INFO, systemGroup.name, systemGroup.description, systemGroup.maxSystems).replaceAll("\"", "");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		log.finest(String.format("System Group (info) match regex: [%s]", match_info));
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", " ").matches(match_info), String.format("System Group [%s] should be found in the result info", systemGroup.name));

		String id = KatelloUtils.grepCLIOutput("ID", exec_result.getStdout());
		
		return id;
	}
	
}
