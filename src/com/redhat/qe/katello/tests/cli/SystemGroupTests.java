package com.redhat.qe.katello.tests.cli;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloSystemGroup;
import com.redhat.qe.katello.base.obj.KatelloTemplate;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups={"cfse-cli","headpin-cli"})
public class SystemGroupTests extends KatelloCliTestScript{	
	protected static Logger log = Logger.getLogger(SystemGroupTests.class.getName());
	
	private SSHCommandResult exec_result;
	private String orgName;
	private String systemGroupName;
	private String envName;

	@BeforeClass(description="Generate unique names")
	public void setUp(){
		String uid = KatelloTestScript.getUniqueID();
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
		Assert.assertTrue(exec_result.getExitCode() == 144, "Check - return code");
		//@TODO fix message remove Name when bug #846251 ir fixed.
		Assert.assertEquals(getOutput(exec_result).trim(), "Validation failed: Name Name must be unique within one organization");
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
	
	@Test(description = "List system groups", groups = { "cli-systemgroup" })
	public void test_listSystemGroup() {
		KatelloSystemGroup systemGroup = createSystemGroup();
		
		String sgroup_name2 = "system_group"+KatelloTestScript.getUniqueID();
		KatelloSystemGroup systemGroup2 = new KatelloSystemGroup(sgroup_name2, this.orgName);
		exec_result = systemGroup2.create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");
		
		assert_systemGroupList(Arrays.asList(systemGroup, systemGroup2), new ArrayList<KatelloSystemGroup>());
	}

	@Test(description = "Delete created system group and verify that it is not shown in list", groups = { "cli-systemgroup" })
	public void test_deleteSystemGroup() {
		KatelloSystemGroup systemGroup = createSystemGroup();
		
		String sgroup_name2 = "system_group"+KatelloTestScript.getUniqueID();
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
	
	private KatelloSystemGroup createSystemGroup() {
		systemGroupName = "system_group"+KatelloTestScript.getUniqueID();
		
		KatelloSystemGroup systemGroup = new KatelloSystemGroup(this.systemGroupName, this.orgName);
		exec_result = systemGroup.create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloSystemGroup.OUT_CREATE, systemGroupName)), "Check - output string (system_group create)");
		
		return systemGroup;
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
	
	private String assert_SystemGroupInfo(KatelloSystemGroup systemGroup) {
		if (systemGroup.description == null) systemGroup.description = "None";

		exec_result = systemGroup.info();

		String match_info = String.format(KatelloSystemGroup.REG_SYSTEMGROUP_INFO, systemGroup.name, systemGroup.description, systemGroup.totalSystems).replaceAll("\"", "");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		log.finest(String.format("System Group (info) match regex: [%s]", match_info));
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", " ").matches(match_info), String.format("System Group [%s] should be found in the result info", systemGroup.name));

		Pattern pattern = Pattern.compile(KatelloSystemGroup.REG_SYSTEMGROUP_ID);
		Matcher matcher = pattern.matcher(getOutput(exec_result).replaceAll("\n", " "));
		Assert.assertTrue(matcher.find(), "Check - Id should exist in System Group info");
		String id = matcher.group();
		id = id.replace("Id:", "").replace("Name:", "").trim();
		
		return id;
	}
	
}
