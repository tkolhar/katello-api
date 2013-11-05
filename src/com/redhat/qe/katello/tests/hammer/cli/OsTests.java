package com.redhat.qe.katello.tests.hammer.cli;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.HammerArchitecture;
import com.redhat.qe.katello.base.obj.HammerOrganization;
import com.redhat.qe.katello.base.obj.HammerOs;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

public class OsTests extends KatelloCliTestBase {
	
	private SSHCommandResult exec_result;
	private String name;
	private String Id;
	private String arch_name;
	private String architecture_id;
	private String[] base_names;
	
	@BeforeClass(description="Prepare an data to work with")
	public void setup(){
		String uid = KatelloUtils.getUniqueID();
		this.name = "RHEL"+uid;
		this.arch_name = "anch"+uid;
		this.base_names = new String[10];
		for (int i = base_names.length - 1; i >= 0; i--) {
			base_names[i] = "o" + i + "s" + uid;
			new HammerOs(cli_worker, base_names[i], "6.2", "6.5").cli_create();
		}
		
		HammerArchitecture arch = new HammerArchitecture(cli_worker, arch_name);
		exec_result = arch.cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		exec_result = arch.cli_info();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		architecture_id = KatelloUtils.grepCLIOutput("Id", getOutput(exec_result));
	}
	
	@Test(description="Create a operating system")
	public void test_osCreate() {
		HammerOs os = new HammerOs(cli_worker, name, "6.2", "6.5");
		exec_result = os.cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(HammerOs.OUT_CREATE), "Check - returned output string");
	}
	
	@Test(description="set label on os", dependsOnMethods={"test_osCreate"})
	public void test_OsSetLabel() {
		HammerOs os = new HammerOs(cli_worker, name);
		os.label = name;
		exec_result = os.update();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(HammerOs.OUT_UPDATE), "Check - returned output string");
		
		exec_result = os.cli_info(name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Id = KatelloUtils.grepCLIOutput("Id", getOutput(exec_result));
	}
	
	@Test(description="update previously created Os", dependsOnMethods={"test_OsSetLabel"})
	public void test_OsUpdate() {
		HammerOs os = new HammerOs(cli_worker, name);
		os.Id = Id;
		os.name = name;
		os.arch_ids = architecture_id;
		exec_result = os.update();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(HammerOs.OUT_UPDATE), "Check - returned output string");
		os.arch_ids = arch_name;
		assert_OsInfo(os);
	} 

	@Test(description="set parameter to Os", dependsOnMethods={"test_OsUpdate"})
	public void test_OsSetParam() {
		HammerOs os = new HammerOs(cli_worker, null);
		os.Id = Id;
		exec_result = os.set_parameter("testparam", "testvalue");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(HammerOs.OUT_SET_PARAM), "Check - returned output string");
		os.parameters = "testparam:testvalue";
		assert_OsInfo(os);
	}
	
	@Test(description="update parameter to Os", dependsOnMethods={"test_OsSetParam"})
	public void test_OsUpdateParam() {
		HammerOs os = new HammerOs(cli_worker, null);
		os.Id = Id;
		exec_result = os.set_parameter("testparam", "testvalue2");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(HammerOs.OUT_UPDATE_PARAM), "Check - returned output string");
		os.parameters = "testparam:testvalue2";
		assert_OsInfo(os);
	}
	
	@Test(description="delete parameter to Os", dependsOnMethods={"test_OsUpdateParam"})
	public void test_OsDeleteParam() {
		HammerOs os = new HammerOs(cli_worker, null);
		os.Id = Id;
		exec_result = os.delete_parameter("testparam");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(HammerOs.OUT_DELETE_PARAM), "Check - returned output string");
		assert_OsInfo(os);
	} 

	@Test(description="info Os not found", dependsOnMethods={"test_OsDeleteParam"})
	public void testOs_infoNotFound() {
		HammerOs os = new HammerOs(cli_worker, null);
		os.Id = "0";
		assert_OsInfo(os);
	}

	@Test(description="delete Os", dependsOnMethods={"testOs_infoNotFound"})
	public void testOs_delete() {
		HammerOs os = new HammerOs(cli_worker, null);
		os.Id = Id;
		exec_result = os.delete();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(HammerOs.OUT_DELETE),"Check - returned output string");
	}

	@Test(description="update Os name not found", dependsOnMethods={"testOs_delete"})
	public void testOs_updateNotFound() {
		HammerOs os = new HammerOs(cli_worker, null);
		os.Id = Id;
		exec_result = os.update();
		Assert.assertFalse(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(HammerOs.ERR_NOT_FOUND),"Check - returned error string");
	}
	
	@Test(description="delete Os name not found", dependsOnMethods={"testOs_updateNotFound"})
	public void testOs_deleteNotFound() {
		HammerOs os = new HammerOs(cli_worker, null);
		os.Id = Id;
		exec_result = os.delete();
		Assert.assertFalse(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(HammerOs.ERR_NOT_FOUND),"Check - returned error string");
	}

	@Test(description="search Os")
	public void testOs_search() {		
		HammerOs os = new HammerOs(cli_worker, null);
		exec_result = os.cli_search(base_names[1]);
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(KatelloUtils.grepCLIOutput("Name", getOutput(exec_result)).equals(base_names[1]), "Check - name is listed");
		String cnt = KatelloUtils.run_local(false, String.format("echo -e \"%s\"|grep \"Name:\"|wc -l",getOutput(exec_result)));
		Assert.assertTrue(cnt.equals("1"), "Count of returned Oss must be 1.");
	}
	
	@Test(description="list Os by order and pagination")
	public void testOs_listOrder() {
		SSHCommandResult res;
		
		HammerOs os = new HammerOs(cli_worker, null);
		res = os.cli_list("name", 1, 5);
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		String cnt = KatelloUtils.run_local(false, String.format("echo -e \"%s\"|grep \"Name:\"|wc -l",getOutput(res)));
		Assert.assertTrue(cnt.equals("5"), "Count of returned oss must be 5.");
		String name1 = KatelloUtils.grepCLIOutput("Name", getOutput(res), 1);
		
		res = os.cli_list("name", 2, 5);
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		cnt = KatelloUtils.run_local(false, String.format("echo -e \"%s\"|grep \"Name:\"|wc -l",getOutput(res)));
		Assert.assertTrue(cnt.equals("5"), "Count of returned oss must be 5.");
		String name2 = KatelloUtils.grepCLIOutput("Name", getOutput(res), 1);

		Assert.assertTrue(!name1.equals(name2), "Returned oss in first and second list must not be the same.");
	}
	
	private String assert_OsInfo(HammerOs os) {
		if (os.relName == null) os.relName = "";
		if (os.family == null) os.family = "";
		if (os.installMedia == null) os.installMedia = "";
		if (os.arch_ids == null) os.arch_ids = "";
		if (os.ptable_ids == null) os.ptable_ids = "";
		if (os.config_ids == null) os.config_ids = "";
		if (os.parameters == null) os.parameters = "";
		
		exec_result = os.cli_info(null);
		if (exec_result.getExitCode().intValue() == 0) {
			String match_info = String.format(HammerOs.REG_OS_INFO, os.name, os.relName, os.family, os.installMedia, os.arch_ids,
					os.ptable_ids, os.config_ids, os.parameters).replaceAll("\"", "");
			log.finest(String.format("Os (info) match regex: [%s]", match_info));
			Assert.assertTrue(getOutput(exec_result).replaceAll("\n", " ").matches(match_info), "Os info should match the provided info");
			return KatelloUtils.grepCLIOutput("Id", getOutput(exec_result));
		} else {
			Assert.assertTrue(getOutput(exec_result).equals(HammerOrganization.ERR_404), "Check - returned output string");
			return null;
		}
	}
}
