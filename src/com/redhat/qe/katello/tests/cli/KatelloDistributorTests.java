package com.redhat.qe.katello.tests.cli;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.katello.base.KatelloCliDataProvider;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.obj.KatelloDistributor;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;
public class KatelloDistributorTests extends KatelloCliTestScript{
	SSHCommandResult exec_result;
	String uid = KatelloUtils.getUniqueID();
	String org_distributor_name = "org_system-"+uid;
	String distributor_name = "distributor_name-"+uid;
	KatelloOrg org;
	KatelloDistributor distributor;
	@BeforeClass(description="Generate unique objects", groups={"cfse-cli","headpin-cli"})
	public void setUp() {
		org = new KatelloOrg(org_distributor_name,"Creating Org for a distributor");
		exec_result = org.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(),String.format(KatelloOrg.OUT_CREATE,this.org_distributor_name));
		distributor=new KatelloDistributor(this.org_distributor_name,this.distributor_name);
		exec_result = distributor.distributor_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(),String.format(KatelloDistributor.OUT_CREATE,this.distributor_name));
	}

	@Test(description="distributor add custom info",  
			dataProvider="add_distributor_custom_info", dataProviderClass = KatelloCliDataProvider.class, enabled=true,groups={"cfse-cli","headpin-cli"})
	public void test_distributorAddCustomInfo(String keyname, String value, Integer exitCode){
		SSHCommandResult exec_result;
		exec_result = distributor.add_info(keyname, value, null);
		Assert.assertTrue(exec_result.getExitCode().intValue() == exitCode.intValue(), "Check - return code");		
	} 
	
	@Test(description="distributor remove custom info",enabled=true,groups={"cfse-cli","headpin-cli"})
	public void test_distributorRemoveCustomInfo(){
		SSHCommandResult exec_result;
		uid = KatelloUtils.getUniqueID();
		String org_rm_name="org_rm-" + uid;
		String dis_rm_name="dis_rm-" + uid;
		String test_key = "test_key" + uid;
		String test_val = "test_val" + uid;
		String invalid_key = "invalid_key" + uid;
		KatelloOrg org_rm=new KatelloOrg(org_rm_name,"Org for remove distributor");
		exec_result = org_rm.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(),String.format(KatelloOrg.OUT_CREATE,org_rm_name));
		KatelloDistributor dis_rm = new KatelloDistributor(org_rm_name,dis_rm_name);
		exec_result = dis_rm.distributor_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(),String.format(KatelloDistributor.OUT_CREATE,dis_rm_name));
		exec_result = dis_rm.add_info(test_key, test_val,null);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(),String.format(KatelloDistributor.OUT_INFO,test_key,test_val,dis_rm_name));
		exec_result = dis_rm.distributor_info();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		String dis_uuid = KatelloCli.grepCLIOutput("UUID", getOutput(exec_result));
		exec_result = dis_rm.remove_info(test_key);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(),String.format(KatelloDistributor.OUT_REMOVE_INFO,dis_rm_name));
		exec_result = dis_rm.distributor_info();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = dis_rm.remove_info(invalid_key);
		Assert.assertTrue(exec_result.getExitCode() == 148, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(),String.format(KatelloDistributor.OUT_REMOVE_INVALID_KEY,invalid_key));
		exec_result = dis_rm.add_info(test_key,test_val,dis_uuid);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(),String.format(KatelloDistributor.OUT_INFO,test_key,test_val,dis_uuid));				
	}
}

