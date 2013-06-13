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
	String org_name = "org_system-"+uid;
	String distributor_name = null;
	
	@BeforeClass(description="Generate unique objects", groups={"cfse-cli","headpin-cli"})
	public void setUp() {
		exec_result = new KatelloOrg(org_name,"Creating Org for a distributor").cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	}

	@Test(description="distributor add custom info",  
			dataProvider="add_distributor_custom_info", dataProviderClass = KatelloCliDataProvider.class, enabled=true,groups={"cfse-cli","headpin-cli"})
	public void test_distributorAddCustomInfo(String keyname, String value,String dis_name,Integer exitCode,String output){
		KatelloDistributor distributor=new KatelloDistributor(org_name,dis_name);
		if(distributor_name==null){
			exec_result = distributor.distributor_create();
			distributor_name = dis_name;
			Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		}

		SSHCommandResult exec_result;
		exec_result = distributor.add_info(keyname, value, null);	
		if(exitCode.intValue()==0){ //
			Assert.assertTrue(getOutput(exec_result).contains(output),"Check - returned output string");
		}else{ // Failure to be checked
			Assert.assertTrue(getOutput(exec_result).contains(output),"Check - returned error string");
		}
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
		dis_rm.name = null;
		exec_result = dis_rm.add_info(test_key,test_val,dis_uuid);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(),String.format(KatelloDistributor.OUT_INFO,test_key,test_val,dis_uuid));				
	}
	
	@Test(description="distributor update custom info",enabled=true,groups={"cfse-cli","headpin-cli"}) //TODO modify the returned string once bz#973929 is fixed
	public void test_distributorUpdateCustomInfo(){		
		SSHCommandResult exec_result;
		uid = KatelloUtils.getUniqueID();
		String org_update_name="org_rm-" + uid;
		String dis_update_name="dis_rm-" + uid;
		String test_key = "test_key" + uid;
		String test_val = "test_val" + uid;
		String test_update_val = "test_update_val" + uid;
		KatelloOrg org_update=new KatelloOrg(org_update_name,"Org for remove distributor");
		exec_result = org_update.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(),String.format(KatelloOrg.OUT_CREATE,org_update_name));
		KatelloDistributor dis_update = new KatelloDistributor(org_update_name,dis_update_name);
		exec_result = dis_update.distributor_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(),String.format(KatelloDistributor.OUT_CREATE,dis_update_name));
		exec_result = dis_update.add_info(test_key, test_val,null);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(),String.format(KatelloDistributor.OUT_INFO,test_key,test_val,dis_update_name));
		exec_result = dis_update.distributor_info();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		String dis_uuid = KatelloCli.grepCLIOutput("UUID", getOutput(exec_result));
		exec_result = dis_update.update_info(test_key,test_update_val,null);
		Assert.assertTrue(exec_result.getExitCode() == 65, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(),String.format(KatelloDistributor.ERR_COULD_NOT_UPDATE_INFO,test_key,dis_update_name));
		exec_result = dis_update.distributor_info();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		dis_update_name=null;
		test_val = "test_update_val_with_uuid" + uid;
		exec_result = dis_update.update_info(test_key,test_val,dis_uuid);
		Assert.assertTrue(exec_result.getExitCode() == 65, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(),String.format(KatelloDistributor.ERR_COULD_NOT_UPDATE_INFO,test_key,dis_uuid));		
		exec_result = dis_update.distributor_info();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	}	
}

