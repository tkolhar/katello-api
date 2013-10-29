package com.redhat.qe.katello.tests.cli;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliDataProvider;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloDistributor;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloDistributorTests extends KatelloCliTestBase{
	SSHCommandResult exec_result;
	String uid = KatelloUtils.getUniqueID();
	String org_name = "org_system-"+uid;
	String distributor_name = null;
	String dist_subscriptions_name = "distributor-"+uid;
	
	@BeforeClass(description="Generate unique objects", groups={"cfse-cli","headpin-cli"})
	public void setUp() {
		exec_result = new KatelloOrg(this.cli_worker, org_name,"Creating Org for a distributor").cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		KatelloDistributor distributor = new KatelloDistributor(cli_worker, base_org_name, dist_subscriptions_name, KatelloEnvironment.LIBRARY);
		exec_result = distributor.distributor_create();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (distributor create)");
	}

	@Test(description="distributor add custom info", dataProvider="add_distributor_custom_info", 
			dataProviderClass = KatelloCliDataProvider.class, enabled=true,groups={"cfse-cli","headpin-cli"})
	public void test_distributorAddCustomInfo(String keyname, String value,String dis_name,Integer exitCode,String output){
		KatelloDistributor distributor=new KatelloDistributor(cli_worker, org_name,dis_name);
		if(distributor_name==null){
			exec_result = distributor.distributor_create();
			distributor_name = dis_name;
			Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		}

		SSHCommandResult exec_result;
		exec_result = distributor.add_info(keyname, value, null);	
		System.out.println("OUT_ADD : CODE: "+exec_result.getExitCode() + " STRING: "+getOutput(exec_result));
		Assert.assertTrue(exec_result.getExitCode().intValue() == exitCode.intValue(), "Check - return code");
		if(exitCode.intValue()==0){ //
			Assert.assertTrue(getOutput(exec_result).contains(output),"Check - returned output string");
			exec_result = distributor.distributor_info();
			Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (Distributor Info)");
			String customInfoStr = KatelloUtils.grepCLIOutput("Custom Info", getOutput(exec_result));
			Assert.assertTrue(customInfoStr.contains(keyname), "Check - custom info present");
			Assert.assertTrue(customInfoStr.contains(value), "Check - custom info present");
			
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
		KatelloOrg org_rm=new KatelloOrg(this.cli_worker, org_rm_name,"Org for remove distributor");
		exec_result = org_rm.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(),String.format(KatelloOrg.OUT_CREATE,org_rm_name));
		KatelloDistributor dis_rm = new KatelloDistributor(cli_worker, org_rm_name,dis_rm_name);
		exec_result = dis_rm.distributor_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(),String.format(KatelloDistributor.OUT_CREATE,dis_rm_name));
		exec_result = dis_rm.add_info(test_key, test_val,null);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(),String.format(KatelloDistributor.OUT_INFO,test_key,test_val,dis_rm_name));

		exec_result = dis_rm.remove_info(test_key);
		System.out.println("OUT_REMOVE : CODE: "+exec_result.getExitCode() + " STRING: "+getOutput(exec_result));
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(),String.format(KatelloDistributor.OUT_REMOVE_INFO, dis_rm_name));
		exec_result = dis_rm.distributor_info();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		String customInfoStr = KatelloUtils.grepCLIOutput("Custom Info", getOutput(exec_result));
		Assert.assertFalse(customInfoStr.contains(test_key), "Check - custom info remove");
		exec_result = dis_rm.remove_info(invalid_key);
		Assert.assertTrue(exec_result.getExitCode() == 148, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(),String.format(KatelloDistributor.OUT_INVALID_KEY,invalid_key));
	
	}

	@Test(description="distributor remove custom info using uuid",enabled=true,groups={"cfse-cli","headpin-cli"})
	public void test_removeCustomInfoUUID(){
		SSHCommandResult exec_result;
		uid = KatelloUtils.getUniqueID();
		String dis_rm_name="dis_rm-" + uid;
		String test_key = "test_key" + uid;
		String test_val = "test_val" + uid;
		String invalid_key = "invalid_key" + uid;
		
		KatelloDistributor dis_rm = new KatelloDistributor(cli_worker, org_name,dis_rm_name);
		exec_result = dis_rm.distributor_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = dis_rm.add_info(test_key, test_val,null);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(),String.format(KatelloDistributor.OUT_INFO,test_key,test_val,dis_rm_name));
		
		exec_result = dis_rm.distributor_info();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		String dis_uuid = KatelloUtils.grepCLIOutput("UUID", getOutput(exec_result));
		dis_rm.name = null;
		dis_rm.uuid = dis_uuid;
		exec_result = dis_rm.remove_info(test_key);
		System.out.println("OUT_UUID_REMOVE : CODE: "+exec_result.getExitCode() + " STRING: "+getOutput(exec_result));
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(),String.format(KatelloDistributor.OUT_REMOVE_INFO, dis_uuid));
		exec_result = dis_rm.distributor_info();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		String customInfoStr = KatelloUtils.grepCLIOutput("Custom Info", getOutput(exec_result));
		Assert.assertFalse(customInfoStr.contains(test_key), "Check - custom info remove");
		exec_result = dis_rm.remove_info(invalid_key);
		Assert.assertTrue(exec_result.getExitCode() == 148, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(),String.format(KatelloDistributor.OUT_INVALID_KEY,invalid_key));
		
	}

	@Test(description="distributor update custom info",enabled=true,groups={"cfse-cli","headpin-cli"})
	public void test_distributorUpdateCustomInfo(){		
		SSHCommandResult exec_result;
		uid = KatelloUtils.getUniqueID();
		String org_update_name="org_rm-" + uid;
		String dis_update_name="dis_rm-" + uid;
		String test_key = "test_key" + uid;
		String test_val = "test_val" + uid;
		String test_update_val = "test_update_val" + uid;
		String invalid_key = "invalid_key" + uid;
		
		KatelloOrg org_update=new KatelloOrg(this.cli_worker, org_update_name,"Org for remove distributor");
		exec_result = org_update.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(),String.format(KatelloOrg.OUT_CREATE,org_update_name));
		KatelloDistributor dis_update = new KatelloDistributor(cli_worker, org_update_name,dis_update_name);
		exec_result = dis_update.distributor_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(),String.format(KatelloDistributor.OUT_CREATE,dis_update_name));
		exec_result = dis_update.add_info(test_key, test_val,null);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(),String.format(KatelloDistributor.OUT_INFO,test_key,test_val,dis_update_name));
		exec_result = dis_update.distributor_info();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		String dis_uuid = KatelloUtils.grepCLIOutput("UUID", getOutput(exec_result));
		
		exec_result = dis_update.update_info(test_key,test_update_val,null);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(),String.format(KatelloDistributor.OUT_UPDATE_INFO,test_key, dis_update_name), "Check - output");
		exec_result = dis_update.distributor_info();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		String customInfoStr = KatelloUtils.grepCLIOutput("Custom Info", getOutput(exec_result));
		Assert.assertTrue(customInfoStr.contains(test_key+": "+test_update_val), "Check - custom value update");
		
		exec_result = dis_update.update_info(invalid_key, test_val, null);
		Assert.assertTrue(exec_result.getExitCode() == 148, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(),String.format(KatelloDistributor.OUT_INVALID_KEY,invalid_key));
		exec_result = dis_update.distributor_info();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		customInfoStr = KatelloUtils.grepCLIOutput("Custom Info", getOutput(exec_result));
		Assert.assertFalse(customInfoStr.contains(invalid_key+": "+test_val), "Check - custom value update");
		
		
		//using uuid
		dis_update_name=null;
		test_val = "test_update_val_with_uuid" + uid;
		exec_result = dis_update.update_info(test_key,test_val,dis_uuid);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(),String.format(KatelloDistributor.OUT_UPDATE_INFO,test_key, dis_uuid), "Check - output");		
		exec_result = dis_update.distributor_info();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		customInfoStr = KatelloUtils.grepCLIOutput("Custom Info", getOutput(exec_result));
		Assert.assertTrue(customInfoStr.contains(test_key+": "+test_val), "Check - custom value update");
		
		exec_result = dis_update.update_info(invalid_key, test_val, dis_uuid);
		Assert.assertTrue(exec_result.getExitCode() == 148, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(),String.format(KatelloDistributor.OUT_INVALID_KEY,invalid_key));
	}
	
	// TODO: bz#974452
	// TODO: bz#974466
	@Test(description = "Delete Distributor after attaching/downloading/uploading subscription", enabled=true,groups={"cfse-cli","headpin-cli"})
	public void test_distributorDelete()
	{
	  distributor_name = "newDistributor-"+KatelloUtils.getUniqueID();
	  KatelloDistributor distributor=new KatelloDistributor(cli_worker, org_name, distributor_name);
	  exec_result = distributor.distributor_create();
	  Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (create distributor)");
	  Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloDistributor.OUT_CREATE, distributor_name)), "Check - output for create distributor");

	  //delete distributor
	  exec_result = distributor.distributor_delete();
	  Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (delete distributor)");
	  Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloDistributor.OUT_DELETE, distributor_name)), "Check - output for delete distributor");
	}

	@Test(description="create distributor - many variations",
			dataProviderClass=KatelloCliDataProvider.class,dataProvider="create_distributor")
	public void test_createVariations(String dist_name, Integer exit_code, String output) {
		KatelloDistributor distributor = new KatelloDistributor(cli_worker, base_org_name, dist_name, KatelloEnvironment.LIBRARY);
		exec_result = distributor.distributor_create();
		Assert.assertTrue(exec_result.getExitCode() == exit_code.intValue(), "Check - return code (create distributor)");
		if(exec_result.getExitCode() == 0)
			Assert.assertTrue(getOutput(exec_result).equals(output), "Check output (create distributor");
		else
			Assert.assertTrue(getOutput(exec_result).contains(output), "Check output (create distributor");
	}

	@Test(description="list distributors", dependsOnMethods={"test_createVariations"})
	public void test_listDistributors() {
		KatelloDistributor distributor = new KatelloDistributor(cli_worker, base_org_name, null, null);
		exec_result = distributor.list();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (list distributors)");
		distributor.environment = base_dev_env_name;
		exec_result = distributor.list();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (list distributors)");
	}

	// TODO bz#994977
	@Test(description="create distributor in non-Library environment")
	public void test_createInEnvironment() {
		String dist_env_name = "distributor-env-"+uid;
		KatelloDistributor distributor = new KatelloDistributor(cli_worker, base_org_name, dist_env_name, base_dev_env_name);
		exec_result = distributor.distributor_create();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (create distributor)");
	}

	// TODO bz#994977
	@Test(description="update distributor tests")
	public void test_updateDistributor() {
		String uid = KatelloUtils.getUniqueID();
		String dist_name = "distributor-"+uid;
		String new_name = "distributor-new-"+uid;
		String new_description = "new description";
		KatelloDistributor distributor = new KatelloDistributor(cli_worker, base_org_name, dist_name, KatelloEnvironment.LIBRARY);
		exec_result = distributor.distributor_create();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (create distributor)");

		// update name, description
		exec_result = distributor.update(new_name, null, new_description);
		distributor.name = new_name;
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (update distributor)");
		exec_result = distributor.distributor_info();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (distributor info)");
		Assert.assertTrue(getOutput(exec_result).contains(new_description), "Check ouput code (distributor info)");
		// update environment, bz 994977
		exec_result = distributor.update(null, base_dev_env_name, null);
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (update distributor env)");
	}

	// TODO bz#974447
	@Test(description="list available subscriptions")
	public void test_listAvailableSubscriptions() {
		KatelloDistributor distributor = new KatelloDistributor(cli_worker, base_org_name, dist_subscriptions_name, KatelloEnvironment.LIBRARY);
		exec_result = distributor.available_subscriptions();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (distributor subscriptions)");
	}

	// TODO bz#974452
	@Test(description="subscribe")
	public void test_subscribe() {
		KatelloDistributor distributor = new KatelloDistributor(cli_worker, base_org_name, dist_subscriptions_name, KatelloEnvironment.LIBRARY);
		exec_result = distributor.subscribe(base_zoo_repo_pool);
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (distributor subscriptions)");
	}

	// TODO bz#974447
	@Test(description="list subscriptions", dependsOnMethods={"test_subscribe"})
	public void test_listSubscriptions() {
		KatelloDistributor distributor = new KatelloDistributor(cli_worker, base_org_name, dist_subscriptions_name, KatelloEnvironment.LIBRARY);
		exec_result = distributor.subscriptions();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (distributor subscriptions)");
	}

	// TODO bz#974466
	@Test(description="unsubscribe", dependsOnMethods={"test_listSubscriptions"})
	public void test_unsubscribe() {
		KatelloDistributor distributor = new KatelloDistributor(cli_worker, base_org_name, dist_subscriptions_name, KatelloEnvironment.LIBRARY);
		exec_result = distributor.unsubscribe(base_zoo_repo_pool);
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (distributor subscriptions)");
	}

	// TODO bz#1004284
	@Test(description="distributor not found - check error")
	public void test_distributorNotFound() {
		String dist_name = "distributor-"+KatelloUtils.getUniqueID();
		String dist_uuid = "00000000-0000-0000-0000-000000000000";
		KatelloDistributor distr = new KatelloDistributor(cli_worker, base_org_name, dist_name, base_dev_env_name);
		exec_result = distr.distributor_info();
		Assert.assertTrue(exec_result.getExitCode()==65, "Check exit code (distributor not found)");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloDistributor.ERR_NOT_FOUND_IN_ENV, dist_name, base_dev_env_name, base_org_name)), "Check error (distributor not found)");
		// without env
		distr.environment = null;
		exec_result = distr.distributor_info();
		Assert.assertTrue(exec_result.getExitCode()==65, "Check exit code (distributor not found)");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloDistributor.ERR_NOT_FOUND, dist_name, base_org_name)), "Check error (distributor not found)");
		// by uuid - bz 1004284
		distr.name = null;
		distr.uuid = dist_uuid;
		exec_result = distr.distributor_info();
		Assert.assertTrue(exec_result.getExitCode()==65, "Check exit code (distributor not found)");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloDistributor.ERR_NOT_FOUND, dist_uuid, base_org_name)), "Check error (distributor not found)");
	}
}
