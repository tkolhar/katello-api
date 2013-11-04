package com.redhat.qe.katello.tests.hammer.cli;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.HammerArchitecture;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

public class ArchitectureTests extends KatelloCliTestBase {
	
	private String name;
	private String new_name;
	private String[] base_names;
	
	@BeforeClass(description="Prepare an data to work with")
	public void setUp(){
		String uid = KatelloUtils.getUniqueID().substring(7);
		this.name = "arch"+uid;
		this.new_name = "new" + uid;
		this.base_names = new String[10];
		for (int i = base_names.length - 1; i >= 0; i--) {
			base_names[i] = "ar" + i + "ch" + uid;
			new HammerArchitecture(cli_worker, base_names[i]).cli_create();
		}
	}
	
	@Test(description="create Architecture")
	public void testArchitecture_create() {
		SSHCommandResult res;
		
		HammerArchitecture arch = new HammerArchitecture(cli_worker, name);
		res = arch.cli_create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		
		Assert.assertTrue(getOutput(res).contains(String.format(HammerArchitecture.OUT_CREATE, name)),"Check - returned output string");
	}

	@Test(description="create Architecture which name exists", dependsOnMethods={"testArchitecture_create"})
	public void testArchitecture_createExists() {
		SSHCommandResult res;
		
		HammerArchitecture arch = new HammerArchitecture(cli_worker, name);
		res = arch.cli_create();
		Assert.assertFalse(res.getExitCode().intValue() == 0, "Check - return code");
		
		// @ TODO error message
		//Assert.assertTrue(getOutput(res).contains(String.format(HammerArchitecture.ERR_NAME_EXISTS, name)),"Check - returned error string");
	}
	
	@Test(description="info Architecture", dependsOnMethods={"testArchitecture_createExists"})
	public void testArchitecture_info() {
		SSHCommandResult res;
		
		HammerArchitecture arch = new HammerArchitecture(cli_worker, name);
		res = arch.cli_info();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		
		Assert.assertTrue(getOutput(res).contains(name),"Check - returned output string");
	}
	
	@Test(description="update Architecture", dependsOnMethods={"testArchitecture_info"})
	public void testArchitecture_update() {
		SSHCommandResult res;
		
		HammerArchitecture arch = new HammerArchitecture(cli_worker, name);
		res = arch.update(new_name);
		Assert.assertEquals(res.getExitCode().intValue(), 0, "Check - return code");
		
		// @ TODO error message
		//Assert.assertTrue(getOutput(res).contains(String.format(HammerArchitecture.OUT_UPDATE, name)),"Check - returned output string");
	}
	
	@Test(description="list Architecture", dependsOnMethods={"testArchitecture_update"})
	public void testArchitecture_list() {
		SSHCommandResult res;
		
		HammerArchitecture arch = new HammerArchitecture(cli_worker, name);
		res = arch.cli_info();
		Assert.assertTrue(res.getExitCode().intValue() == 128, "Check - return code"); // TODO enter a bug		
		Assert.assertTrue(!getOutput(res).contains(name),"Check - old name is not listed");
		
		arch = new HammerArchitecture(cli_worker, new_name);
		res = arch.cli_info();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(res).contains(new_name),"Check - new name is listed");
	}
	
	@Test(description="info Architecture not found", dependsOnMethods={"testArchitecture_list"})
	public void testArchitecture_infoNotFound() {
		SSHCommandResult res;
		
		HammerArchitecture arch = new HammerArchitecture(cli_worker, name);
		res = arch.cli_info();
		Assert.assertFalse(res.getExitCode().intValue() == 0, "Check - return code");
		
		// @ TODO error message
		//Assert.assertTrue(getOutput(res).contains(String.format(HammerArchitecture.ERR_NOT_FOUND, name)),"Check - returned error string");
	}

	@Test(description="delete Architecture", dependsOnMethods={"testArchitecture_infoNotFound"})
	public void testArchitecture_delete() {
		SSHCommandResult res;
		
		HammerArchitecture arch = new HammerArchitecture(cli_worker, new_name);
		res = arch.delete();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		
		// @ TODO error message
		//Assert.assertTrue(getOutput(res).contains(String.format(HammerArchitecture.OUT_DELETE, new_name)),"Check - returned output string");
	}

	@Test(description="update Architecture name not found", dependsOnMethods={"testArchitecture_delete"})
	public void testArchitecture_updateNotFound() {
		SSHCommandResult res;
		
		HammerArchitecture arch = new HammerArchitecture(cli_worker, new_name);
		res = arch.update(name);
		Assert.assertFalse(res.getExitCode().intValue() == 0, "Check - return code");
		
		// @ TODO error message
		//Assert.assertTrue(getOutput(res).contains(String.format(HammerArchitecture.ERR_NOT_FOUND, new_name)),"Check - returned error string");
	}
	
	@Test(description="delete Architecture name not found", dependsOnMethods={"testArchitecture_updateNotFound"})
	public void testArchitecture_deleteNotFound() {
		SSHCommandResult res;
		
		HammerArchitecture arch = new HammerArchitecture(cli_worker, new_name);
		res = arch.delete();
		Assert.assertFalse(res.getExitCode().intValue() == 0, "Check - return code");
		
		// @ TODO error message
		//Assert.assertTrue(getOutput(res).contains(String.format(HammerArchitecture.ERR_NOT_FOUND, new_name)),"Check - returned error string");
	}

	@Test(description="search Architecture")
	public void testArchitecture_search() {
		SSHCommandResult res;
		
		HammerArchitecture arch = new HammerArchitecture(cli_worker, name);
		res = arch.cli_search(base_names[1]);
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(KatelloUtils.grepCLIOutput("Name", getOutput(res)).equals(base_names[1]), "Check - name is listed");
		String cnt = KatelloUtils.run_local(false, String.format("echo -e \"%s\"|grep \"Name:\"|wc -l",getOutput(res)));
		Assert.assertTrue(cnt.equals("1"), "Count of returned archs must be 1.");
	}
	
	@Test(description="list Architecture by order and pagination")
	public void testArchitecture_listOrder() {
		SSHCommandResult res;
		
		HammerArchitecture arch = new HammerArchitecture(cli_worker, null);
		res = arch.cli_list("name", 1, 5);
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		String cnt = KatelloUtils.run_local(false, String.format("echo -e \"%s\"|grep \"Name:\"|wc -l",getOutput(res)));
		Assert.assertTrue(cnt.equals("5"), "Count of returned archs must be 5.");
		String name1 = KatelloUtils.grepCLIOutput("Name", getOutput(res), 1);
		
		res = arch.cli_list("name", 2, 5);
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		cnt = KatelloUtils.run_local(false, String.format("echo -e \"%s\"|grep \"Name:\"|wc -l",getOutput(res)));
		Assert.assertTrue(cnt.equals("5"), "Count of returned archs must be 5.");
		String name2 = KatelloUtils.grepCLIOutput("Name", getOutput(res), 1);

		Assert.assertTrue(!name1.equals(name2), "Returned archs in first and second list must not be the same.");
	}
}
