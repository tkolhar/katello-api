package com.redhat.qe.katello.tests.hammer.cli;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.HammerMedium;
import com.redhat.qe.katello.base.obj.HammerOs;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

public class MediumTests extends KatelloCliTestBase {
	
	private String name;
	private String Id;
	private String[] base_names;
	private String os_id;
	private String os_name;
	private String uid = KatelloUtils.getUniqueID().substring(7);
	
	@BeforeClass(description="Prepare an data to work with")
	public void setUp(){
		this.name = "madium"+uid;
		this.base_names = new String[10];
		for (int i = base_names.length - 1; i >= 0; i--) {
			base_names[i] = "med" + i + "ium" + uid;
			new HammerMedium(cli_worker, base_names[i], HammerMedium.OsFamily.Redhat, "http://"+uid+i).cli_create();
		}
		
		HammerOs os = new HammerOs(cli_worker, "RHEL"+uid, "6.2", "6.5");
		exec_result = os.cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		exec_result = os.cli_info(os.name);
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		os_id = KatelloUtils.grepCLIOutput("Id", getOutput(exec_result));
		os_name = KatelloUtils.grepCLIOutput("Name", getOutput(exec_result));
	}
	
	@Test(description="create Medium")
	public void testMedium_create() {
		SSHCommandResult res;
		
		HammerMedium medium = new HammerMedium(cli_worker, name, HammerMedium.OsFamily.Archlinux, "http://"+uid);
		res = medium.cli_create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		
		Assert.assertTrue(getOutput(res).contains(String.format(HammerMedium.OUT_CREATE, name)),"Check - returned output string");
	}

	@Test(description="create Medium which name exists", dependsOnMethods={"testMedium_create"})
	public void testMedium_createNameExists() {
		SSHCommandResult res;
		
		HammerMedium medium = new HammerMedium(cli_worker, name, HammerMedium.OsFamily.Archlinux, "http://new"+uid);
		res = medium.cli_create();
		Assert.assertFalse(res.getExitCode().intValue() == 0, "Check - return code");

		Assert.assertTrue(getOutput(res).contains(HammerMedium.ERR_NAME_EXISTS),"Check - returned error string");
	}

	@Test(description="create Medium which path exists", dependsOnMethods={"testMedium_create"})
	public void testMedium_createPathExists() {
		SSHCommandResult res;
		
		HammerMedium medium = new HammerMedium(cli_worker, name+"other", HammerMedium.OsFamily.Archlinux, "http://"+uid);
		res = medium.cli_create();
		Assert.assertFalse(res.getExitCode().intValue() == 0, "Check - return code");

		Assert.assertTrue(getOutput(res).contains(HammerMedium.ERR_PATH_EXISTS),"Check - returned error string");
	}

	//@ TODO bz#1026803
	@Test(description="add os to previously created Medium", dependsOnMethods={"testMedium_create"})
	public void test_MediumAddOs() {
		HammerMedium medium = new HammerMedium(cli_worker, name, HammerMedium.OsFamily.Archlinux, "http://"+uid);
		exec_result = medium.add_os(os_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(HammerMedium.OUT_UPDATE), "Check - returned output string");
		medium.operatingsystem_ids = os_id;
		assert_MediumInfo(medium);
	}

	//@ TODO bz#1026803
	@Test(description="remove os from previously created Medium", dependsOnMethods={"test_MediumAddOs"})
	public void test_MediumRemoveOs() {
		HammerMedium medium = new HammerMedium(cli_worker, name, HammerMedium.OsFamily.Archlinux, "http://"+uid);
		exec_result = medium.remove_os(os_id);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(HammerMedium.OUT_UPDATE), "Check - returned output string");
		medium.operatingsystem_ids = null;
		assert_MediumInfo(medium);
	}
	
	@Test(description="List Mediums. Check if name is provided", dependsOnMethods={"testMedium_create"})
	public void test_MediumSearch() {
		HammerMedium medium = new HammerMedium(cli_worker, null);
		exec_result = medium.cli_search("name="+name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(name), "Check - updated Medium name is listed");
		
		Id = KatelloUtils.grepCLIOutput("Id", (KatelloUtils.grepOutBlock("Name", name, getOutput(exec_result))));
	}
	
	@Test(description="update previously created Medium", dependsOnMethods={"test_MediumSearch"})
	public void test_MediumUpdate() {
		HammerMedium medium = new HammerMedium(cli_worker, name);
		medium.Id = Id;
		exec_result = medium.update("new"+name, null);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(HammerMedium.OUT_UPDATE), "Check - returned output string");
		medium.name = "new"+name;
		assert_MediumInfo(medium);
	}

	@Test(description="info Medium not found", dependsOnMethods={"test_MediumUpdate"})
	public void testMedium_infoNotFound() {
		HammerMedium medium = new HammerMedium(cli_worker, null);
		medium.Id = "0";
		assert_MediumInfo(medium);
	}

	@Test(description="delete Medium", dependsOnMethods={"testMedium_infoNotFound"})
	public void testMedium_delete() {
		HammerMedium medium = new HammerMedium(cli_worker, null);
		medium.Id = Id;
		exec_result = medium.delete();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(HammerMedium.OUT_DELETE),"Check - returned output string");
	}

	@Test(description="update Medium name not found", dependsOnMethods={"testMedium_delete"})
	public void testMedium_updateNotFound() {
		HammerMedium medium = new HammerMedium(cli_worker, null);
		medium.Id = Id;
		exec_result = medium.update("new"+name, null);
		Assert.assertFalse(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(HammerMedium.ERR_NOT_FOUND),"Check - returned error string");
	}
	
	@Test(description="delete Medium name not found", dependsOnMethods={"testMedium_updateNotFound"})
	public void testMedium_deleteNotFound() {
		HammerMedium medium = new HammerMedium(cli_worker, null);
		medium.Id = Id;
		exec_result = medium.delete();
		Assert.assertFalse(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(HammerMedium.ERR_NOT_FOUND),"Check - returned error string");
	}

	@Test(description="search Medium")
	public void testMedium_search() {		
		HammerMedium medium = new HammerMedium(cli_worker, null);
		exec_result = medium.cli_search(base_names[1]);
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(KatelloUtils.grepCLIOutput("Name", getOutput(exec_result)).equals(base_names[1]), "Check - name is listed");
		String cnt = KatelloUtils.run_local(false, String.format("echo -e \"%s\"|grep \"Name:\"|wc -l",getOutput(exec_result)));
		Assert.assertTrue(cnt.equals("1"), "Count of returned Mediums must be 1.");
	}
	
	@Test(description="list Medium by order and pagination")
	public void testMedium_listOrder() {
		SSHCommandResult res;
		
		HammerMedium medium = new HammerMedium(cli_worker, null);
		res = medium.cli_list("name", 1, 5);
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		String cnt = KatelloUtils.run_local(false, String.format("echo -e \"%s\"|grep \"Name:\"|wc -l",getOutput(res)));
		Assert.assertTrue(cnt.equals("5"), "Count of returned mediums must be 5.");
		String name1 = KatelloUtils.grepCLIOutput("Name", getOutput(res), 1);
		
		res = medium.cli_list("name", 2, 5);
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		cnt = KatelloUtils.run_local(false, String.format("echo -e \"%s\"|grep \"Name:\"|wc -l",getOutput(res)));
		Assert.assertTrue(cnt.equals("5"), "Count of returned mediums must be 5.");
		String name2 = KatelloUtils.grepCLIOutput("Name", getOutput(res), 1);

		Assert.assertTrue(!name1.equals(name2), "Returned mediums in first and second list must not be the same.");
	}
	
	private void assert_MediumInfo(HammerMedium medium) {
		if (medium.path == null) medium.path = "";
		if (medium.os_family == null) medium.os_family = "";
		if (medium.operatingsystem_ids == null) medium.operatingsystem_ids = "";
		
		exec_result = medium.cli_info();
		if (exec_result.getExitCode().intValue() == 0) {
			String match_info = String.format(HammerMedium.REG_INFO, medium.name, medium.path, medium.os_family, medium.operatingsystem_ids).replaceAll("\"", "");
			log.finest(String.format("Medium (info) match regex: [%s]", match_info));
			Assert.assertTrue(getOutput(exec_result).replaceAll("\n", " ").matches(match_info), "Medium info should match the provided info");
		} else {
			Assert.assertTrue(getOutput(exec_result).equals(HammerMedium.ERR_NOT_FOUND), "Check - returned output string");
		}
	} 
}
