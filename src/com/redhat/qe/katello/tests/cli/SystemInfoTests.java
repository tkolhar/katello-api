package com.redhat.qe.katello.tests.cli;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.common.TngRunGroups;
import com.redhat.qe.tools.SSHCommandResult;

@Test(
		groups={"SystemInfoTests","cfse-cli","headpin-cli",TngRunGroups.TNG_KATELLO_System_Consumer})
public class SystemInfoTests extends KatelloCliTestBase{	
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
		exec_result = org.add_system_info(keyname);
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");
		Assert.assertEquals(exec_result.getStdout().trim(), 
				String.format(KatelloOrg.OUT_ADD_SYS_INFO, keyname, org.name),
				"Check - add system info output.");
		
		exec_result = org.apply_system_info();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");

		Assert.assertTrue(exec_result.getStdout().trim().contains( 
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
	public void removeOrgInfo() {
		KatelloOrg org = new KatelloOrg(this.org, "Default org");
		exec_result = org.remove_system_info(keyname);
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");
		Assert.assertEquals(exec_result.getStdout().trim(), 
				String.format(KatelloOrg.OUT_REMOVE_SYS_INFO, keyname, org.name),
				"Check - remove system info output.");
		
		List<String[]> sysparamsList = new LinkedList<String[]>();
		
		assert_systemInfo(new KatelloSystem(this.system, this.org, this.environment), sysparamsList);
		
		List<String> orgparamsList = new LinkedList<String>();
		orgparamsList.add(this.keyname);
		
		assert_orgInfo(org, orgparamsList);
	}

	@Test(description="adding 2 parameters to system", dependsOnMethods={"removeOrgInfo"})
	public void addSystemInfo() {
		KatelloSystem sys = new KatelloSystem(this.system, this.org, this.environment);
		exec_result = sys.add_custom_info(keyname2, value2);
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");
		Assert.assertEquals(exec_result.getStdout().trim(), 
				String.format(KatelloSystem.OUT_ADD_CUSTOM_INFO, keyname2, value2, sys.name),
				"Check - add custom info output.");
		
		List<String[]> sysparamsList = new LinkedList<String[]>();
		sysparamsList.add(new String[] {this.keyname, "None"});
		sysparamsList.add(new String[] {this.keyname2, value2});
		
		assert_systemInfo(new KatelloSystem(this.system, this.org, this.environment), sysparamsList);
		
		exec_result = sys.add_custom_info(keyname3, value3);
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");
		Assert.assertEquals(exec_result.getStdout().trim(), 
				String.format(KatelloSystem.OUT_ADD_CUSTOM_INFO, keyname3, value3, sys.name),
				"Check - add custom info output.");
		
		sysparamsList.add(new String[] {this.keyname3, value3});
		
		assert_systemInfo(new KatelloSystem(this.system, this.org, this.environment), sysparamsList);
	}

	@Test(description="update a parameter in system", dependsOnMethods={"addSystemInfo"})
	public void updateSystemInfo() {
		KatelloSystem sys = new KatelloSystem(this.system, this.org, this.environment);
		exec_result = sys.update_custom_info(keyname2, value2_edit);
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");
		Assert.assertEquals(exec_result.getStdout().trim(), 
				String.format(KatelloSystem.OUT_UPDATE_CUSTOM_INFO, keyname2, sys.name),
				"Check - update custom info output.");
		
		List<String[]> sysparamsList = new LinkedList<String[]>();
		sysparamsList.add(new String[] {this.keyname, "None"});
		sysparamsList.add(new String[] {this.keyname2, value2_edit});
		sysparamsList.add(new String[] {this.keyname3, value3});
		
		assert_systemInfo(new KatelloSystem(this.system, this.org, this.environment), sysparamsList);
	}
	
	@Test(description="remove existing parameter from system", dependsOnMethods={"updateSystemInfo"})
	public void removeSystemInfo() {
		KatelloSystem sys = new KatelloSystem(this.system, this.org, this.environment);
		exec_result = sys.remove_custom_info(keyname2);
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");
		Assert.assertEquals(exec_result.getStdout().trim(), 
				String.format(KatelloSystem.OUT_REMOVE_CUSTOM_INFO, sys.name),
				"Check - remove custom info output.");
		
		List<String[]> sysparamsList = new LinkedList<String[]>();
		sysparamsList.add(new String[] {this.keyname, "None"});
		sysparamsList.add(new String[] {this.keyname3, value3});
		
		assert_systemInfo(new KatelloSystem(this.system, this.org, this.environment), sysparamsList);
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
