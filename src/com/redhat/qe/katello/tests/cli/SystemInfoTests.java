package com.redhat.qe.katello.tests.cli;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.common.TngRunGroups;
import com.redhat.qe.tools.SSHCommandResult;

@Test(
		groups={"SystemInfoTests","cfse-cli","headpin-cli",TngRunGroups.TNG_KATELLO_System_Consumer})
public class SystemInfoTests extends KatelloCliTestScript{	
	protected static Logger log = Logger.getLogger(SystemTests.class.getName());

	private SSHCommandResult exec_result;
	private String org;
	private String system;
	private String environment;
	private String keyname = "secret question";
	private String keyname2 = "second question";
	private String value2 = "value2";
	private String value2_edit = "edit2";
	private String keyname3 = "secret location";
	private String value3 = "secret place";
	private String keyInvalid = "invalid key";

	@BeforeClass(description="Generate unique names")
	public void setUp(){
		String uid = KatelloUtils.getUniqueID();
		this.org = "org-default-"+uid;
		this.system = "system-" + uid;
		this.environment = "environment-" + uid;

		KatelloOrg org = new KatelloOrg(this.org, "Default org");
		exec_result = org.cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");

		KatelloEnvironment env = new KatelloEnvironment(this.environment, null, this.org, KatelloEnvironment.LIBRARY);
		exec_result = env.cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");

		KatelloSystem sys = new KatelloSystem(system, this.org, this.environment);
		exec_result = sys.rhsm_registerForce(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
	}

	@Test
	public void checkSystemList() {
		KatelloSystem sys = new KatelloSystem(this.system, this.org, this.environment);
		exec_result = sys.list();
		Assert.assertTrue(getOutput(exec_result).trim().contains(this.system), "System should be contained");
	}

	@Test(dependsOnMethods={"checkSystemList"})
	public void checkSystemInfo() {
		KatelloSystem sys = new KatelloSystem(this.system, this.org, this.environment);
		exec_result = sys.info();
		Assert.assertTrue(getOutput(exec_result).trim().contains(this.system), "System should be contained");

		assert_systemInfo(sys, new LinkedList<String[]>());
	}

	@Test(dependsOnMethods={"checkSystemInfo"})
	public void checkOrgInfo() {
		KatelloOrg org = new KatelloOrg(this.org, "Default org");
		exec_result = org.cli_info();
		Assert.assertTrue(getOutput(exec_result).trim().contains(this.org), "Org should be contained");

		assert_orgInfo(org, new LinkedList<String>());
	}	

	@Test(description="added a parameter to org, verifies that it exists in system as well", dependsOnMethods={"checkOrgInfo"})
	public void addOrgInfo() {
		KatelloOrg org = new KatelloOrg(this.org, "Default org");
		exec_result = org.default_info_add(keyname,"system");
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), 
				String.format(KatelloOrg.OUT_ADD_SYS_INFO, keyname, org.name),
				"Check - add system info output.");

		exec_result = org.default_info_apply("system");
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");

		Assert.assertTrue(getOutput(exec_result).trim().contains( 
				String.format(KatelloOrg.OUT_APPLY_SYS_INFO, org.name)),
				"Check - apply system info output.");

		List<String[]> sysparamsList = new LinkedList<String[]>();
		sysparamsList.add(new String[] {this.keyname, "None"});

		assert_systemInfo(new KatelloSystem(this.system, this.org, this.environment), sysparamsList);

		List<String> orgparamsList = new LinkedList<String>();
		orgparamsList.add(this.keyname);

		assert_orgInfo(org, orgparamsList);
	}

	@Test(description="remove existing parameter from org, verifies that it still exists in system", dependsOnMethods={"addOrgInfo"})
	public void test_removeOrgInfo() {
		KatelloOrg org = new KatelloOrg(this.org, "Default org");
		exec_result = org.default_info_remove(keyname,"system");
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), 
				String.format(KatelloOrg.OUT_REMOVE_SYS_INFO, keyname, org.name),
				"Check - remove system info output.");

		List<String[]> sysparamsList = new LinkedList<String[]>();
		sysparamsList.add(new String[]{this.keyname, "none"});
		assert_systemInfo(new KatelloSystem(this.system, this.org, this.environment), sysparamsList);

		List<String> orgparamsList = new LinkedList<String>();
		//orgparamsList.add(this.keyname);

		assert_orgInfo(org, orgparamsList);
	}

	@Test(description="Sync the systems, the removed default parameters should also be removed from the system", dependsOnMethods={"test_removeOrgInfo"})
	public void test_syncRemovedInfo()
	{
		KatelloOrg org = new KatelloOrg(this.org, "Default org");
		exec_result = org.default_info_apply("System");

		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");

		Assert.assertTrue(getOutput(exec_result).trim().contains( 
				String.format(KatelloOrg.OUT_APPLY_SYS_INFO, org.name)),
				"Check - apply system info output.");

		List<String[]> sysparamsList = new LinkedList<String[]>();

		assert_systemInfo(new KatelloSystem(this.system, this.org, this.environment), sysparamsList);

	}

	@Test(description="trying to remove an invalid parameter from org, verifies the error message", dependsOnMethods={"addOrgInfo"})
	public void test_removeOrgInvalidInfo() {
		KatelloOrg org = new KatelloOrg(this.org, "Default org");
		exec_result = org.default_info_remove(keyInvalid, "System");

		//TODO: AssertTrue for the exact exitCode
		Assert.assertFalse(exec_result.getExitCode().intValue()==0, "Check - return code");

		//TODO: Assert for the correct Error Message. Feature yet to be added
		Assert.assertEquals(getOutput(exec_result).trim(), 
				String.format(KatelloOrg.OUT_REMOVE_SYS_INFO, keyname, org.name),
				"Check - remove system info output.");

		List<String[]> sysparamsList = new LinkedList<String[]>();
		sysparamsList.add(new String[] {this.keyname, "None"});
		assert_systemInfo(new KatelloSystem(this.system, this.org, this.environment), sysparamsList);

		List<String> orgparamsList = new LinkedList<String>();
		orgparamsList.add(this.keyname);

		assert_orgInfo(org, orgparamsList);
	}

	@Test(description="Adding multiple default org parameters without syncing the systems, verify that they do not exist in the system yet", dependsOnMethods={"test_syncRemovedInfo"})
	public void addMultipleOrgInfo()
	{
		KatelloOrg org = new KatelloOrg(this.org, "Default org");
		exec_result = org.default_info_add(keyname2, "System");
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), 
				String.format(KatelloOrg.OUT_ADD_SYS_INFO, keyname2, org.name),
				"Check - add system info output.");

		exec_result = org.default_info_add(keyname3, "System");
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), 
				String.format(KatelloOrg.OUT_ADD_SYS_INFO, keyname3, org.name),
				"Check - add system info output.");


		List<String[]> sysparamsList = new LinkedList<String[]>();

		assert_systemInfo(new KatelloSystem(this.system, this.org, this.environment), sysparamsList);

		List<String> orgparamsList = new LinkedList<String>();
		orgparamsList.add(this.keyname2);
		orgparamsList.add(this.keyname3);

		assert_orgInfo(org, orgparamsList);
		
	}

	@Test(description="adding the same 2 parameters as the default ones to system", dependsOnMethods={"addMultipleOrgInfo"})
	public void test_addSystemInfo() {
		KatelloSystem sys = new KatelloSystem(this.system, this.org, this.environment);
		exec_result = sys.add_custom_info(keyname2, value2);
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), 
				String.format(KatelloSystem.OUT_ADD_CUSTOM_INFO, keyname2, value2, sys.name),
				"Check - add custom info output.");

		List<String[]> sysparamsList = new LinkedList<String[]>();
		sysparamsList.add(new String[] {this.keyname2, value2});

		assert_systemInfo(new KatelloSystem(this.system, this.org, this.environment), sysparamsList);

		exec_result = sys.add_custom_info(keyname3, value3);
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), 
				String.format(KatelloSystem.OUT_ADD_CUSTOM_INFO, keyname3, value3, sys.name),
				"Check - add custom info output.");

		sysparamsList.add(new String[] {this.keyname3, value3});

		assert_systemInfo(new KatelloSystem(this.system, this.org, this.environment), sysparamsList);
	}

	@Test(description="Sync the added default parameters, verify that the sync process of org does not override the custom info keys of the system", dependsOnMethods={"test_addSystemInfo"})
	public void test_syncAddedInfo()
	{
		KatelloOrg org = new KatelloOrg(this.org, "Default org");
		exec_result = org.default_info_apply("System");

		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");

		Assert.assertTrue(getOutput(exec_result).trim().contains( 
				String.format(KatelloOrg.OUT_APPLY_SYS_INFO, org.name)),
				"Check - apply system info output.");

	}

	@Test(description="update a parameter in system", dependsOnMethods={"test_addSystemInfo"})
	public void updateSystemInfo() {
		KatelloSystem sys = new KatelloSystem(this.system, this.org, this.environment);
		exec_result = sys.update_custom_info(keyname2, value2_edit);
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), 
				String.format(KatelloSystem.OUT_UPDATE_CUSTOM_INFO, keyname2, sys.name),
				"Check - update custom info output.");

		List<String[]> sysparamsList = new LinkedList<String[]>();
		sysparamsList.add(new String[] {this.keyname, "None"});
		sysparamsList.add(new String[] {this.keyname2, value2_edit});
		sysparamsList.add(new String[] {this.keyname3, value3});

		assert_systemInfo(new KatelloSystem(this.system, this.org, this.environment), sysparamsList);
	}

	@Test(description="remove existing parameter from system", dependsOnMethods={"updateSystemInfo"})
	public void test_removeSystemInfo() {
		KatelloSystem sys = new KatelloSystem(this.system, this.org, this.environment);
		exec_result = sys.remove_custom_info(keyname2);
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), 
				String.format(KatelloSystem.OUT_REMOVE_CUSTOM_INFO, sys.name),
				"Check - remove custom info output.");

		List<String[]> sysparamsList = new LinkedList<String[]>();
		sysparamsList.add(new String[] {this.keyname, "None"});
		sysparamsList.add(new String[] {this.keyname3, value3});

		assert_systemInfo(new KatelloSystem(this.system, this.org, this.environment), sysparamsList);
	}

	@Test(description="remove all existing default parameters and sync, verify that the info still exists for the system", dependsOnMethods={"updateSystemInfo"})
	public void test_removeAllDefaultInfo() {

		KatelloOrg org = new KatelloOrg(this.org, "Default org");
		exec_result = org.default_info_remove(keyname, "System");
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), 
				String.format(KatelloOrg.OUT_REMOVE_SYS_INFO, keyname, org.name),
				"Check - remove system info output.");

		exec_result = org.default_info_remove(keyname2, "System");
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), 
				String.format(KatelloOrg.OUT_REMOVE_SYS_INFO, keyname2, org.name),
				"Check - remove system info output.");

		exec_result = org.default_info_remove(keyname3, "System");
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), 
				String.format(KatelloOrg.OUT_REMOVE_SYS_INFO, keyname3, org.name),
				"Check - remove system info output.");

		exec_result = org.default_info_apply("System");

		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");

		Assert.assertTrue(getOutput(exec_result).trim().contains( 
				String.format(KatelloOrg.OUT_APPLY_SYS_INFO, org.name)),
				"Check - apply system info output.");

		List<String[]> sysparamsList = new LinkedList<String[]>();
		sysparamsList.add(new String[] {this.keyname, "None"});
		sysparamsList.add(new String[] {this.keyname2, value2});
		sysparamsList.add(new String[] {this.keyname3, value3});

		assert_systemInfo(new KatelloSystem(this.system, this.org, this.environment), sysparamsList);

		List<String> orgparamsList = new LinkedList<String>();

		assert_orgInfo(org, orgparamsList);
	}

	private void assert_systemInfo(KatelloSystem system, List<String[]> paramsList) {

		SSHCommandResult res;
		res = system.info();

		String params = "";

		int i = 0;
		for (String[] param : paramsList) {
			i++;
			params += param[0] + ":\\s+" + param[1];
			if (i != paramsList.size()) {
				params += ",\\s+";
			}
		}

		String match_info = String.format(KatelloSystem.REG_CUSTOM_INFO, params).replaceAll("\"", "");
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		log.finest(String.format("System (info) match regex: [%s]", match_info));
		Assert.assertTrue(getOutput(res).replaceAll("\n", " ").matches(match_info), String.format("System [%s] should be found in the result info", system.name));
	}

	private void assert_orgInfo(KatelloOrg org, List<String> paramsList) {

		SSHCommandResult res;
		res = org.cli_info();

		String params = "";

		int i = 0;
		for (String param : paramsList) {
			i++;
			params += param;
			if (i != paramsList.size()) {
				params += ",\\s+";
			}
		}

		String match_info = String.format(KatelloOrg.REG_CUSTOM_INFO, params).replaceAll("\"", "");
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		log.finest(String.format("Org (info) match regex: [%s]", match_info));
		Assert.assertTrue(getOutput(res).replaceAll("\n", " ").matches(match_info), String.format("Org [%s] should be found in the result info", org.name));
	}
}
