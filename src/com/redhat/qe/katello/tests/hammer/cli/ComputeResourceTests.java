package com.redhat.qe.katello.tests.hammer.cli;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.HammerComputeResource;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

public class ComputeResourceTests extends KatelloCliTestBase {
	
	private SSHCommandResult exec_result;
	private String name;
	private String[] base_names;
	private String Id;
	
	@BeforeClass(description="Prepare an data to work with")
	public void setup(){
		String uid = KatelloUtils.getUniqueID();
		this.name = "compres"+uid;
		this.base_names = new String[10];
		for (int i = base_names.length - 1; i >= 0; i--) {
			base_names[i] = "comp" + i + "res" + uid;
			new HammerComputeResource(cli_worker, base_names[i], "test resource", HammerComputeResource.Provider.oVirt, "http://localhost/ovirt", "admin", "admin").cli_create();
		}
	}
	
	@Test(description="Create a compute resource")
	public void test_computeResourceCreate() {
		HammerComputeResource compRes = new HammerComputeResource(cli_worker, name, "test resource", HammerComputeResource.Provider.oVirt, "http://localhost/ovirt", "admin", "admin");
		exec_result = compRes.cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(HammerComputeResource.OUT_CREATE), "Check - returned output string");
	}
	
	@Test(description="Create a compute resource duplicate name", dependsOnMethods={"test_computeResourceCreate"})
	public void test_computeResourceCreateDuplicate() {
		HammerComputeResource compRes = new HammerComputeResource(cli_worker, name, "test resource", HammerComputeResource.Provider.oVirt, "http://localhost/ovirt", "admin", "admin");
		exec_result = compRes.cli_create();
		Assert.assertFalse(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains("Name has already been taken"), "Check - returned output string");
	}
	
	@Test(description="List ComputeResources. Check if name is provided", dependsOnMethods={"test_computeResourceCreate"})
	public void test_ComputeResourceSearch() {
		HammerComputeResource compRes = new HammerComputeResource(cli_worker, null);
		exec_result = compRes.cli_search("name="+name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(name), "Check - updated ComputeResource name is listed");
		
		Id = KatelloUtils.grepCLIOutput("Id", (KatelloUtils.grepOutBlock("Name", name, getOutput(exec_result))));
	}
	
	@Test(description="update previously created ComputeResource", dependsOnMethods={"test_ComputeResourceSearch"})
	public void test_ComputeResourceUpdate() {
		HammerComputeResource compRes = new HammerComputeResource(cli_worker, name, "test resource", HammerComputeResource.Provider.oVirt, "http://localhost/ovirt", "admin", "admin");
		compRes.Id = Id;
		exec_result = compRes.update("new"+name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(HammerComputeResource.OUT_UPDATE), "Check - returned output string");
		compRes.name = "new"+name;
		assert_ComputeResourceInfo(compRes);
	}

	@Test(description="info ComputeResource not found", dependsOnMethods={"test_ComputeResourceUpdate"})
	public void testComputeResource_infoNotFound() {
		HammerComputeResource compRes = new HammerComputeResource(cli_worker, null);
		compRes.Id = "0";
		assert_ComputeResourceInfo(compRes);
	}

	@Test(description="delete ComputeResource", dependsOnMethods={"testComputeResource_infoNotFound"})
	public void testComputeResource_delete() {
		HammerComputeResource compRes = new HammerComputeResource(cli_worker, null);
		compRes.Id = Id;
		exec_result = compRes.delete();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(HammerComputeResource.OUT_DELETE),"Check - returned output string");
	}

	@Test(description="update ComputeResource name not found", dependsOnMethods={"testComputeResource_delete"})
	public void testComputeResource_updateNotFound() {
		HammerComputeResource compRes = new HammerComputeResource(cli_worker, null);
		compRes.Id = Id;
		exec_result = compRes.update("new"+name);
		Assert.assertFalse(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(HammerComputeResource.ERR_NOT_FOUND),"Check - returned error string");
	}
	
	@Test(description="delete ComputeResource name not found", dependsOnMethods={"testComputeResource_updateNotFound"})
	public void testComputeResource_deleteNotFound() {
		HammerComputeResource compRes = new HammerComputeResource(cli_worker, null);
		compRes.Id = Id;
		exec_result = compRes.delete();
		Assert.assertFalse(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(HammerComputeResource.ERR_NOT_FOUND),"Check - returned error string");
	}

	@Test(description="search ComputeResource")
	public void testComputeResource_search() {		
		HammerComputeResource compRes = new HammerComputeResource(cli_worker, null);
		exec_result = compRes.cli_search("name="+base_names[1]);
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(KatelloUtils.grepCLIOutput("Name", getOutput(exec_result)).equals(base_names[1]), "Check - name is listed");
		String cnt = KatelloUtils.run_local(false, String.format("echo -e \"%s\"|grep \"Name:\"|wc -l",getOutput(exec_result)));
		Assert.assertTrue(cnt.equals("1"), "Count of returned ComputeResources must be 1.");
	}
	
	@Test(description="list ComputeResource by order and pagination")
	public void testComputeResource_listOrder() {
		SSHCommandResult res;
		
		HammerComputeResource compRes = new HammerComputeResource(cli_worker, null);
		res = compRes.cli_list("name", 1, 5);
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		String cnt = KatelloUtils.run_local(false, String.format("echo -e \"%s\"|grep \"Name:\"|wc -l",getOutput(res)));
		Assert.assertTrue(cnt.equals("5"), "Count of returned ComputeResources must be 5.");
		String name1 = KatelloUtils.grepCLIOutput("Name", getOutput(res), 1);
		
		res = compRes.cli_list("name", 2, 5);
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		cnt = KatelloUtils.run_local(false, String.format("echo -e \"%s\"|grep \"Name:\"|wc -l",getOutput(res)));
		Assert.assertTrue(cnt.equals("5"), "Count of returned ComputeResources must be 5.");
		String name2 = KatelloUtils.grepCLIOutput("Name", getOutput(res), 1);

		Assert.assertTrue(!name1.equals(name2), "Returned ComputeResources in first and second list must not be the same.");
	}
	
	private void assert_ComputeResourceInfo(HammerComputeResource compRes) {
		if (compRes.name == null) compRes.name = "";
		if (compRes.description == null) compRes.description = "";
		if (compRes.provider == null) compRes.provider = "";
		if (compRes.url == null) compRes.url = "";
		if (compRes.user == null) compRes.user = "";
		if (compRes.uuid == null) compRes.uuid = "";
		
		exec_result = compRes.cli_info();
		if (exec_result.getExitCode().intValue() == 0) {
			String match_info = String.format(HammerComputeResource.REG_INFO, compRes.name, compRes.provider, compRes.url, compRes.description, compRes.user, compRes.uuid).replaceAll("\"", "");
			log.finest(String.format("ComputeResource (info) match regex: [%s]", match_info));
			Assert.assertTrue(getOutput(exec_result).replaceAll("\n", " ").matches(match_info), "ComputeResource info should match the provided info");
		} else {
			Assert.assertTrue(getOutput(exec_result).equals(HammerComputeResource.ERR_NOT_FOUND), "Check - returned output string");
		}
	} 
}
