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
	
	@BeforeClass(description="Prepare an data to work with")
	public void setUp(){
		String uid = KatelloUtils.getUniqueID();
		this.name = "arch"+uid.substring(7);
		this.new_name = "new" + KatelloUtils.getUniqueID().substring(7);
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
		Assert.assertEquals(res.getExitCode().intValue(), 65, "Check - return code");
		
		Assert.assertTrue(getOutput(res).contains(String.format(HammerArchitecture.ERR_NAME_EXISTS, name)),"Check - returned error string");
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
		
		Assert.assertTrue(getOutput(res).contains(String.format(HammerArchitecture.OUT_UPDATE, name)),"Check - returned output string");
	}
	
	@Test(description="list Architecture", dependsOnMethods={"testArchitecture_update"})
	public void testArchitecture_list() {
		SSHCommandResult res;
		
		HammerArchitecture arch = new HammerArchitecture(cli_worker, name);
		res = arch.cli_list();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		
		Assert.assertFalse(getOutput(res).contains(name),"Check - old name is not listed");
		Assert.assertTrue(getOutput(res).contains(new_name),"Check - new name is listed");
	}
	
	@Test(description="info Architecture not found", dependsOnMethods={"testArchitecture_list"})
	public void testArchitecture_infoNotFound() {
		SSHCommandResult res;
		
		HammerArchitecture arch = new HammerArchitecture(cli_worker, name);
		res = arch.cli_info();
		Assert.assertTrue(res.getExitCode().intValue() == 148, "Check - return code");
		
		Assert.assertTrue(getOutput(res).contains(String.format(HammerArchitecture.ERR_NOT_FOUND, name)),"Check - returned error string");
	}

	@Test(description="delete Architecture", dependsOnMethods={"testArchitecture_infoNotFound"})
	public void testArchitecture_delete() {
		SSHCommandResult res;
		
		HammerArchitecture arch = new HammerArchitecture(cli_worker, new_name);
		res = arch.delete();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		
		Assert.assertTrue(getOutput(res).contains(String.format(HammerArchitecture.OUT_DELETE, new_name)),"Check - returned output string");
	}

	@Test(description="update Architecture name not found", dependsOnMethods={"testArchitecture_delete"})
	public void testArchitecture_updateNotFound() {
		SSHCommandResult res;
		
		HammerArchitecture arch = new HammerArchitecture(cli_worker, new_name);
		res = arch.update(name);
		Assert.assertTrue(res.getExitCode().intValue() == 148, "Check - return code");
		
		Assert.assertTrue(getOutput(res).contains(String.format(HammerArchitecture.ERR_NOT_FOUND, new_name)),"Check - returned error string");
	}
	
	@Test(description="delete Architecture name not found", dependsOnMethods={"testArchitecture_updateNotFound"})
	public void testArchitecture_deleteNotFound() {
		SSHCommandResult res;
		
		HammerArchitecture arch = new HammerArchitecture(cli_worker, new_name);
		res = arch.delete();
		Assert.assertTrue(res.getExitCode().intValue() == 148, "Check - return code");
		
		Assert.assertTrue(getOutput(res).contains(String.format(HammerArchitecture.ERR_NOT_FOUND, new_name)),"Check - returned error string");
	}
}
