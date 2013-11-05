package com.redhat.qe.katello.tests.hammer.cli;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.HammerHostgroup;
import com.redhat.qe.katello.base.obj.HammerEnvironment;
import com.redhat.qe.katello.base.obj.HammerOrganization;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

public class HostgroupTests extends KatelloCliTestBase {
	
	private String name;
	private String Id;
	private String environment_id;
	private String[] base_names;
	private String uid = KatelloUtils.getUniqueID().substring(7);
	
	@BeforeClass(description="Prepare an data to work with")
	public void setUp(){
		this.name = "hostgroup"+uid;
		this.base_names = new String[10];
		for (int i = base_names.length - 1; i >= 0; i--) {
			base_names[i] = "host" + i + "group" + uid;
			new HammerHostgroup(cli_worker, base_names[i]).cli_create();
		}
		
		HammerEnvironment env = new HammerEnvironment(cli_worker, "env"+uid);
		exec_result = env.cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		exec_result = env.cli_info();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		environment_id = KatelloUtils.grepCLIOutput("Id", getOutput(exec_result));
	}
	
	@Test(description="create Hostgroup")
	public void testHostgroup_create() {
		SSHCommandResult res;
		
		HammerHostgroup hgroup = new HammerHostgroup(cli_worker, name);
		res = hgroup.cli_create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		
		Assert.assertTrue(getOutput(res).contains(String.format(HammerHostgroup.OUT_CREATE, name)),"Check - returned output string");
	}

	@Test(description="create Hostgroup which name exists", dependsOnMethods={"testHostgroup_create"})
	public void testHostgroup_createExists() {
		SSHCommandResult res;
		
		HammerHostgroup hgroup = new HammerHostgroup(cli_worker, name);
		res = hgroup.cli_create();
		Assert.assertFalse(res.getExitCode().intValue() == 0, "Check - return code");

		Assert.assertTrue(getOutput(res).contains(HammerHostgroup.ERR_NAME_EXISTS),"Check - returned error string");
	}
	
	@Test(description="List hostgroups. Check if name is provided", dependsOnMethods={"testHostgroup_createExists"})
	public void test_HostgroupSearch() {
		HammerHostgroup hgroup = new HammerHostgroup(cli_worker, name);
		exec_result = hgroup.cli_list(name, null, null, null);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(name), "Check - updated hostgroup name is listed");
		
		Id = KatelloUtils.grepCLIOutput("Id", (KatelloUtils.grepOutBlock("Name", name, getOutput(exec_result))));
	}
	
	@Test(description="update previously created hostgroup", dependsOnMethods={"test_HostgroupSearch"})
	public void test_hostgroupUpdate() {
		HammerHostgroup hgroup = new HammerHostgroup(cli_worker, name);
		hgroup.Id = Id;
		hgroup.env_Id = environment_id;
		exec_result = hgroup.update();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(HammerHostgroup.OUT_UPDATE), "Check - returned output string");
		assert_HostgroupInfo(hgroup);
	} 

	@Test(description="set parameter to hostgroup", dependsOnMethods={"test_hostgroupUpdate"})
	public void test_hostgroupSetParam() {
		HammerHostgroup hgroup = new HammerHostgroup(cli_worker, null);
		hgroup.Id = Id;
		exec_result = hgroup.set_parameter("testparam", "testvalue");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(HammerHostgroup.OUT_SET_PARAM), "Check - returned output string");
		hgroup.parameters = "testparam:testvalue";
		//@ TODO parameters are not displayed
		//assert_HostgroupInfo(hgroup);
	}
	
	@Test(description="update parameter to hostgroup", dependsOnMethods={"test_hostgroupSetParam"})
	public void test_hostgroupUpdateParam() {
		HammerHostgroup hgroup = new HammerHostgroup(cli_worker, null);
		hgroup.Id = Id;
		exec_result = hgroup.set_parameter("testparam", "testvalue2");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(HammerHostgroup.OUT_UPDATE_PARAM), "Check - returned output string");
		hgroup.parameters = "testparam:testvalue2";
		//@ TODO parameters are not displayed
		//assert_HostgroupInfo(hgroup);
	}
	
	@Test(description="delete parameter to hostgroup", dependsOnMethods={"test_hostgroupUpdateParam"})
	public void test_hostgroupDeleteParam() {
		HammerHostgroup hgroup = new HammerHostgroup(cli_worker, null);
		hgroup.Id = Id;
		exec_result = hgroup.delete_parameter("testparam");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(HammerHostgroup.OUT_DELETE_PARAM), "Check - returned output string");
		//@ TODO parameters are not displayed
		//assert_HostgroupInfo(hgroup);
	} 

	@Test(description="info Hostgroup not found", dependsOnMethods={"test_hostgroupDeleteParam"})
	public void testHostgroup_infoNotFound() {
		HammerHostgroup hgroup = new HammerHostgroup(cli_worker, null);
		hgroup.Id = "0";
		assert_HostgroupInfo(hgroup);
	}

	@Test(description="delete Hostgroup", dependsOnMethods={"testHostgroup_infoNotFound"})
	public void testHostgroup_delete() {
		HammerHostgroup hgroup = new HammerHostgroup(cli_worker, null);
		hgroup.Id = Id;
		exec_result = hgroup.delete();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(HammerHostgroup.OUT_DELETE),"Check - returned output string");
	}

	@Test(description="update Hostgroup name not found", dependsOnMethods={"testHostgroup_delete"})
	public void testHostgroup_updateNotFound() {
		HammerHostgroup hgroup = new HammerHostgroup(cli_worker, null);
		hgroup.Id = Id;
		exec_result = hgroup.update();
		Assert.assertFalse(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(HammerHostgroup.ERR_NOT_FOUND),"Check - returned error string");
	}
	
	@Test(description="delete Hostgroup name not found", dependsOnMethods={"testHostgroup_updateNotFound"})
	public void testHostgroup_deleteNotFound() {
		HammerHostgroup hgroup = new HammerHostgroup(cli_worker, null);
		hgroup.Id = Id;
		exec_result = hgroup.delete();
		Assert.assertFalse(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(HammerHostgroup.ERR_NOT_FOUND),"Check - returned error string");
	}

	@Test(description="search Hostgroup")
	public void testHostgroup_search() {		
		HammerHostgroup hgroup = new HammerHostgroup(cli_worker, null);
		exec_result = hgroup.cli_search(base_names[1]);
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(KatelloUtils.grepCLIOutput("Name", getOutput(exec_result)).equals(base_names[1]), "Check - name is listed");
		String cnt = KatelloUtils.run_local(false, String.format("echo -e \"%s\"|grep \"Name:\"|wc -l",getOutput(exec_result)));
		Assert.assertTrue(cnt.equals("1"), "Count of returned hostgroups must be 1.");
	}
	
	@Test(description="list Hostgroup by order and pagination")
	public void testHostgroup_listOrder() {
		SSHCommandResult res;
		
		HammerHostgroup hgroup = new HammerHostgroup(cli_worker, null);
		res = hgroup.cli_list("name", 1, 5);
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		String cnt = KatelloUtils.run_local(false, String.format("echo -e \"%s\"|grep \"Name:\"|wc -l",getOutput(res)));
		Assert.assertTrue(cnt.equals("5"), "Count of returned hgroups must be 5.");
		String name1 = KatelloUtils.grepCLIOutput("Name", getOutput(res), 1);
		
		res = hgroup.cli_list("name", 2, 5);
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		cnt = KatelloUtils.run_local(false, String.format("echo -e \"%s\"|grep \"Name:\"|wc -l",getOutput(res)));
		Assert.assertTrue(cnt.equals("5"), "Count of returned hgroups must be 5.");
		String name2 = KatelloUtils.grepCLIOutput("Name", getOutput(res), 1);

		Assert.assertTrue(!name1.equals(name2), "Returned hgroups in first and second list must not be the same.");
	}
	
	private String assert_HostgroupInfo(HammerHostgroup hgroup) {
		if (hgroup.label == null) hgroup.label = hgroup.name;
		if (hgroup.os_Id == null) hgroup.os_Id = "";
		if (hgroup.subnet_Id == null) hgroup.subnet_Id = "";
		if (hgroup.domain_Id == null) hgroup.domain_Id = "";
		if (hgroup.env_Id == null) hgroup.env_Id = "";
		if (hgroup.puppetclass_Ids == null) hgroup.puppetclass_Ids = "";
		if (hgroup.ancestry == null) hgroup.ancestry = "";
		if (hgroup.parameters == null) hgroup.parameters = "";
		
		exec_result = hgroup.cli_info();
		if (exec_result.getExitCode().intValue() == 0) {
			String match_info = String.format(HammerHostgroup.REG_INFO, hgroup.name, hgroup.label, hgroup.os_Id, hgroup.subnet_Id, hgroup.domain_Id,
					hgroup.env_Id, hgroup.puppetclass_Ids, hgroup.ancestry, hgroup.parameters).replaceAll("\"", "");
			log.finest(String.format("Hostgroup (info) match regex: [%s]", match_info));
			Assert.assertTrue(getOutput(exec_result).replaceAll("\n", " ").matches(match_info), "Hostgroup info should match the provided info");
			return KatelloUtils.grepCLIOutput("Id", getOutput(exec_result));
		} else {
			Assert.assertTrue(getOutput(exec_result).equals(HammerOrganization.ERR_404), "Check - returned output string");
			return null;
		}
	} 
}
