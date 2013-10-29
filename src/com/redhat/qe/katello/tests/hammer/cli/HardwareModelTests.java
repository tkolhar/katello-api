package com.redhat.qe.katello.tests.hammer.cli;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.HammerHardwareModel;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

public class HardwareModelTests extends KatelloCliTestBase {
	
	private String name;
	private String hwmodel;
	private String info;
	private String vendorclass;
	private String new_hwmodel;
	private String new_info;
	private String new_vendorclass;
	private String new_name;
	
	@BeforeClass(description="Prepare an data to work with", alwaysRun=true)
	public void setUp(){
		String uid = KatelloUtils.getUniqueID();
		this.name = "hwm"+uid;
		this.hwmodel = "hwmodel";
		this.info = "hardware model info";
		this.vendorclass = "vendor class";
		this.new_name = "new" + KatelloUtils.getUniqueID();
		this.new_hwmodel = "newhwmodel";
		this.new_info = "newhardware model info";
		this.new_vendorclass = "newvendor class";
	}
	
	@Test(description="create HardwareModel")
	public void testHardwareModel_create() {
		SSHCommandResult res;
		
		HammerHardwareModel hwm = new HammerHardwareModel(cli_worker, name, hwmodel, info, vendorclass);
		res = hwm.cli_create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		
		Assert.assertTrue(getOutput(res).contains(String.format(HammerHardwareModel.OUT_CREATE, name)),"Check - returned output string");
	}

	@Test(description="create HardwareModel which name exists", dependsOnMethods={"testHardwareModel_create"})
	public void testHardwareModel_createExists() {
		SSHCommandResult res;
		
		HammerHardwareModel hwm = new HammerHardwareModel(cli_worker, name, hwmodel, info, vendorclass);
		res = hwm.cli_create();
		Assert.assertEquals(res.getExitCode().intValue(), 65, "Check - return code");
		
		Assert.assertTrue(getOutput(res).contains(String.format(HammerHardwareModel.ERR_NAME_EXISTS, name)),"Check - returned error string");
	}
	
	@Test(description="info HardwareModel", dependsOnMethods={"testHardwareModel_createExists"})
	public void testHardwareModel_info() {
	
		HammerHardwareModel hwm = new HammerHardwareModel(cli_worker, name, hwmodel, info, vendorclass);
		
		assert_hardwareModelInfo(hwm);
	}
	
	@Test(description="update HardwareModel", dependsOnMethods={"testHardwareModel_info"})
	public void testHardwareModel_update() {
		SSHCommandResult res;
		
		HammerHardwareModel hwm = new HammerHardwareModel(cli_worker, name);
		hwm.hw_model = new_hwmodel;
		hwm.vendor_class = new_vendorclass;
		hwm.info = new_info;
		
		res = hwm.update(new_name);
		Assert.assertEquals(res.getExitCode().intValue(), 0, "Check - return code");
		
		Assert.assertTrue(getOutput(res).contains(String.format(HammerHardwareModel.OUT_UPDATE, name)),"Check - returned output string");
	}
	
	@Test(description="list HardwareModel", dependsOnMethods={"testHardwareModel_update"})
	public void testHardwareModel_list() {
		SSHCommandResult res;
		
		HammerHardwareModel hwm = new HammerHardwareModel(cli_worker, new_name, new_hwmodel, new_info, new_vendorclass);
		res = hwm.cli_list();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		
		assert_hardwareModelList(Arrays.asList(hwm), Arrays.asList(new HammerHardwareModel(cli_worker, name, hwmodel, info, vendorclass)));
	}
	
	// bz#1023379
	@Test(description="info HardwareModel not found", dependsOnMethods={"testHardwareModel_list"})
	public void testHardwareModel_infoNotFound() {
		SSHCommandResult res;
		
		HammerHardwareModel hwm = new HammerHardwareModel(cli_worker, name);
		res = hwm.cli_info();
		Assert.assertTrue(res.getExitCode().intValue() == 148, "Check - return code");
		
		Assert.assertTrue(getOutput(res).contains(String.format(HammerHardwareModel.ERR_NOT_FOUND, name)),"Check - returned error string");
	}

	@Test(description="delete HardwareModel", dependsOnMethods={"testHardwareModel_infoNotFound"})
	public void testHardwareModel_delete() {
		SSHCommandResult res;
		
		HammerHardwareModel hwm = new HammerHardwareModel(cli_worker, new_name);
		res = hwm.delete();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		
		Assert.assertTrue(getOutput(res).contains(String.format(HammerHardwareModel.OUT_DELETE, new_name)),"Check - returned output string");
	}

	@Test(description="update HardwareModel name not found", dependsOnMethods={"testHardwareModel_delete"})
	public void testHardwareModel_updateNotFound() {
		SSHCommandResult res;
		
		HammerHardwareModel hwm = new HammerHardwareModel(cli_worker, new_name);
		res = hwm.update(name);
		Assert.assertTrue(res.getExitCode().intValue() == 148, "Check - return code");
		
		Assert.assertTrue(getOutput(res).contains(String.format(HammerHardwareModel.ERR_NOT_FOUND, new_name)),"Check - returned error string");
	}
	
	@Test(description="delete HardwareModel name not found", dependsOnMethods={"testHardwareModel_updateNotFound"})
	public void testHardwareModel_deleteNotFound() {
		SSHCommandResult res;
		
		HammerHardwareModel hwm = new HammerHardwareModel(cli_worker, new_name);
		res = hwm.delete();
		Assert.assertTrue(res.getExitCode().intValue() == 148, "Check - return code");
		
		Assert.assertTrue(getOutput(res).contains(String.format(HammerHardwareModel.ERR_NOT_FOUND, new_name)),"Check - returned error string");
	}
	
	@AfterClass(description="destroy", alwaysRun=true)
	public void tearDown(){
		
	}

	private void assert_hardwareModelInfo(HammerHardwareModel hwm) {
		if (hwm.info == null) hwm.info = "None";
		if (hwm.vendor_class == null) hwm.vendor_class = "None";
		if (hwm.hw_model == null) hwm.hw_model = "None";

		SSHCommandResult res = hwm.cli_info();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");

		String match_info = String.format(HammerHardwareModel.REG_HWM_INFO, hwm.name, hwm.vendor_class, hwm.hw_model, hwm.info).replaceAll("\"", "");
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		log.finest(String.format("Changeset (info) match regex: [%s]", match_info));
		Assert.assertTrue(getOutput(res).replaceAll("\n", " ").matches(match_info), String.format("Hardware model [%s] should be found in the result info", hwm.name));
	}
		
	private void assert_hardwareModelList(List<HammerHardwareModel> hwms, List<HammerHardwareModel> excludeHwms) {

		SSHCommandResult res = new HammerHardwareModel(cli_worker, name).cli_list();

		//hardware models that exist in list
		for(HammerHardwareModel hwm : hwms) {		
			if (hwm.vendor_class == null) hwm.vendor_class = "None";
			if (hwm.hw_model == null) hwm.hw_model = "None";
			String match_info = String.format(HammerHardwareModel.REG_HWM_LIST, hwm.name, hwm.vendor_class, hwm.hw_model).replaceAll("\"", "");
			Assert.assertTrue(getOutput(res).replaceAll("\n", " ").matches(match_info), String.format("Hardware model [%s] should be found in the result list", hwm.name));
		}
		
		//hardware models that should not exist in list
		for(HammerHardwareModel hwm : excludeHwms) {		
			if (hwm.vendor_class == null) hwm.vendor_class = "None";
			if (hwm.hw_model == null) hwm.hw_model = "None";
			String match_info = String.format(HammerHardwareModel.REG_HWM_LIST, hwm.name, hwm.vendor_class, hwm.hw_model).replaceAll("\"", "");
			Assert.assertFalse(getOutput(res).replaceAll("\n", " ").matches(match_info), String.format("Hardware model [%s] should not be found in the result list", hwm.name));
		}
		
	}
}
