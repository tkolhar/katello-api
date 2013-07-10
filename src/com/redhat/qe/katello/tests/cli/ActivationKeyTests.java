package com.redhat.qe.katello.tests.cli;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliDataProvider;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloActivationKey;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.base.obj.KatelloSystemGroup;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.common.TngRunGroups;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups={TngRunGroups.TNG_KATELLO_Activation_Key})
public class ActivationKeyTests extends KatelloCliTestBase{
	private String organization;
	private String env;
	private String systemgroup;
	
	@BeforeClass(description="init: create org stuff", alwaysRun=true)
	public void setUp(){
		SSHCommandResult res;
		String uid = KatelloUtils.getUniqueID();
		this.organization = "Simple Org"+uid;
		this.env = "GA Env"+uid;

		KatelloOrg org = new KatelloOrg(this.cli_worker, this.organization, null);
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		KatelloEnvironment env = new KatelloEnvironment(this.cli_worker, this.env, null, this.organization, KatelloEnvironment.LIBRARY);
		res = env.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
	}
	
	@Test(description="create AK", dataProvider="activationkey_create", 
			dataProviderClass = KatelloCliDataProvider.class, enabled=true)
	public void test_create(String name, String descr, Integer exitCode, String output){
		SSHCommandResult res;
		
		KatelloActivationKey ak = new KatelloActivationKey(this.cli_worker, this.organization, this.env, name, descr, null);
		res = ak.create();
		Assert.assertTrue(res.getExitCode().intValue() == exitCode.intValue(), "Check - return code");
		
		if(exitCode.intValue()==0){ //
			Assert.assertTrue(getOutput(res).contains(output),"Check - returned output string");
		}else{ // Failure to be checked
			Assert.assertTrue(getOutput(res).contains(output),"Check - returned error string");
		}
	} 
	    
	@Test(description="create AK - same name, diff. orgs")
	public void test_create_diffOrgsSameName(){
		SSHCommandResult res;
		String uid = KatelloUtils.getUniqueID();
		String ak_name = "ak-"+uid;
		String org2 = "org2-"+uid;

		// create 2nd org (and the same env) 
		KatelloOrg org = new KatelloOrg(this.cli_worker, org2, null);
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		KatelloEnvironment env = new KatelloEnvironment(this.cli_worker, this.env, null, org2, KatelloEnvironment.LIBRARY);
		res = env.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		
		KatelloActivationKey ak = new KatelloActivationKey(this.cli_worker, org2, this.env, ak_name, null, null);
		res = ak.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (activation_key create)");
		
		ak = new KatelloActivationKey(this.cli_worker, this.organization, this.env, ak_name, null, null);
		res = ak.create(); // force update IDs 
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (activation_key create)");
		Assert.assertTrue(getOutput(res).contains(
				String.format(KatelloActivationKey.OUT_CREATE,ak_name)), 
				"Check - returned output string (activation_key create)");
		
		ak.asserts_create();
	}
		
	// @ TODO enable test when bug 909612 is fixed.
    @Test(description="add subscription to ak, verify that it is shown in info, remove it, verify that is is not shown", enabled=true)
    public void test_update_addremoveSubscription(){
    	String uid = KatelloUtils.getUniqueID();
    	String akName="ak-subscription-zoo3-"+uid;
    	String providerName = "Zoo3-"+uid;
    	String productName = "Zoo3 "+uid;
    	String repoName = "Zoo3 "+uid;
    	SSHCommandResult res;

    	KatelloActivationKey ak = new KatelloActivationKey(this.cli_worker, this.organization, this.env, akName, "Activation key with Zoo3 subscription", null);
    	res = ak.create();
    	Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (activation_key create)");
    	KatelloProvider prov = new KatelloProvider(this.cli_worker, providerName, this.organization, null, null);
    	prov.create();
    	KatelloProduct prod = new KatelloProduct(this.cli_worker, productName, this.organization, providerName, null, null, null, null, null);
    	res = prod.create();
    	Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");
    	KatelloRepo repo = new KatelloRepo(this.cli_worker, repoName, this.organization, productName, REPO_INECAS_ZOO3, null, null);
    	res = repo.create();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		
    	KatelloOrg org = new KatelloOrg(this.cli_worker, this.organization, null);
    	res = org.subscriptions();
  
		String poolId1 = KatelloUtils.grepCLIOutput("ID", getOutput(res).trim(),1);
		Assert.assertNotNull(poolId1, "Check - pool Id is not null");
		
		res = ak.update_add_subscription(poolId1);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (add subscription)");
		
		
		res = ak.info();
		Assert.assertTrue(res.getStdout().contains(poolId1), "Check that pool Id is in info page.");
		
		res = ak.update_remove_subscription(poolId1);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (add subscription)");
		
		res = ak.info();
		Assert.assertFalse(res.getStdout().contains(poolId1), "Check that pool Id is NOT in info page.");
    }
    
	
    @Test(description="delete a activationkey")
    public void test_delete_activation_key(){
    	String uid = KatelloUtils.getUniqueID();
    	String akName="ak-delete_act_key-"+ uid; 
    	SSHCommandResult res;
    	KatelloActivationKey ak = new KatelloActivationKey(this.cli_worker, this.organization, this.env, akName, "Activation key created to test deletion", null);
    	res = ak.create();
    	Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (activation_key create)");
    	Assert.assertTrue(getOutput(res).contains(
    			String.format(KatelloActivationKey.OUT_CREATE,akName)), 
    			"Check - returned output string ("+KatelloActivationKey.CMD_CREATE+")");
    	res = ak.delete();
    	Assert.assertTrue(res.getExitCode().intValue() == 0,"Check - return code (activation_key delete)");
    	Assert.assertTrue(getOutput(res).contains(
    			String.format(KatelloActivationKey.OUT_DELETE,akName)), 
    			"Check - returned output string ("+KatelloActivationKey.CMD_DELETE+")");
    	res = ak.info();
    	Assert.assertTrue(res.getExitCode().intValue() == 65,"Check - return code (activation_key delete)");
    	Assert.assertTrue(getOutput(res).contains(
    			String.format(KatelloActivationKey.ERROR_INFO,akName)), 
    			"Check - returned output string ("+KatelloActivationKey.CMD_INFO+")");	
    	res = ak.list();
    	Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (activation_key list)");
    }
    
    /** TCMS scenario is: <a href="https://tcms.engineering.redhat.com/case/221907/?from_plan=7771">here</a> */
    @Test(description="5a47305b-52d0-47ea-9b23-74dffe16b4bf")
    public void test_createWithLimit() {
    	String uid = KatelloUtils.getUniqueID();
    	String akName="act_key-"+ uid; 
    	SSHCommandResult res;
    	KatelloActivationKey ak = new KatelloActivationKey(this.cli_worker, this.organization, this.env, akName, "Activation key created to ", "1");
    	res = ak.create();
    	Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (activation_key create)");
    	ak.asserts_create();
    	
    	sshOnClient(KatelloSystem.RHSM_CLEAN);
    	
    	String systemName = "localhost-"+KatelloUtils.getUniqueID();
		KatelloSystem sys = new KatelloSystem(this.cli_worker, systemName, this.organization, null);
		res = sys.rhsm_registerForce(akName); 
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		
		sshOnClient(KatelloSystem.RHSM_CLEAN);
		
		systemName = "localhost-"+KatelloUtils.getUniqueID();
		sys = new KatelloSystem(this.cli_worker, systemName, this.organization, null);
		res = sys.rhsm_registerForce(akName); 
		Assert.assertTrue(res.getExitCode().intValue() == 255, "Check - return code");
		Assert.assertTrue(getOutput(res).contains(
    			String.format(KatelloActivationKey.ERROR_EXCEED, "1", akName)), 
    			"Check - returned output string for registering by activation key");	
    }

    /** TCMS scenario is: <a href="https://tcms.engineering.redhat.com/case/189165/?from_plan=7793">here</a> */
    @Test(description="4495ea44-704d-4079-bd4d-7297f887d15f")
    public void test_updateTheLimit() {
    	String uid = KatelloUtils.getUniqueID();
    	String akName="act_key-"+ uid; 
    	SSHCommandResult res;
    	KatelloActivationKey ak = new KatelloActivationKey(this.cli_worker, this.organization, this.env, akName, "Activation key created to ", "1");
    	res = ak.create();
    	Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (activation_key create)");
    	ak.asserts_create();
    	
    	sshOnClient(KatelloSystem.RHSM_CLEAN);
    	
    	String systemName = "localhost-"+KatelloUtils.getUniqueID();
		KatelloSystem sys = new KatelloSystem(this.cli_worker, systemName, this.organization, null);
		res = sys.rhsm_registerForce(akName); 
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		
		sshOnClient(KatelloSystem.RHSM_CLEAN);
		
		systemName = "localhost-"+KatelloUtils.getUniqueID();
		sys = new KatelloSystem(this.cli_worker, systemName, this.organization, null);
		res = sys.rhsm_registerForce(akName); 
		Assert.assertTrue(res.getExitCode().intValue() == 255, "Check - return code");
		Assert.assertTrue(getOutput(res).contains(
    			String.format(KatelloActivationKey.ERROR_EXCEED, "1", akName)), 
    			"Check - returned output string for registering by activation key");
		
		ak.extend_limit("2");
		res = sys.rhsm_registerForce(akName); 
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
    }

    //@ TODO 927215
    /** TCMS scenario is: <a href="https://tcms.engineering.redhat.com/case/189166/?from_plan=7793">here</a> */
    @Test(description="fc228a30-c0e8-46d3-a254-681222993bd5")
    public void test_unregisterRegister() {
    	String uid = KatelloUtils.getUniqueID();
    	String akName="act_key-"+ uid; 
    	SSHCommandResult res;
    	KatelloActivationKey ak = new KatelloActivationKey(this.cli_worker, this.organization, this.env, akName, "Activation key created to ", "2");
    	res = ak.create();
    	Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (activation_key create)");
    	ak.asserts_create();
    	
    	rhsm_clean();
    	
    	String systemName = "localhost-"+KatelloUtils.getUniqueID();
		KatelloSystem sys = new KatelloSystem(this.cli_worker, systemName, this.organization, null);
		res = sys.rhsm_registerForce(akName); 
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		
		rhsm_clean_only();
		
		systemName = "localhost-"+KatelloUtils.getUniqueID();
		sys = new KatelloSystem(this.cli_worker, systemName, this.organization, null);
		res = sys.rhsm_registerForce(akName); 
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		res = sys.rhsm_identity();
		String system_uuid = KatelloUtils.grepCLIOutput("Current identity is", res.getStdout());
		
		rhsm_clean_only();
		
		// third system should not register
		systemName = "localhost-"+KatelloUtils.getUniqueID();
		KatelloSystem sys3 = new KatelloSystem(this.cli_worker, systemName, this.organization, null);
		res = sys3.rhsm_registerForce(akName); 
		Assert.assertTrue(res.getExitCode().intValue() == 255, "Check - return code");
		Assert.assertTrue(getOutput(res).contains(
    			String.format(KatelloActivationKey.ERROR_EXCEED, "2", akName)), 
    			"Check - returned output string for registering by activation key");	
		
		sys.uuid = system_uuid;
		res = sys.unregister();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		
		res = sys3.rhsm_registerForce(akName);
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
    }
    
    @Test(description="add system group to activationkey",  enabled=true)
    public void test_addSystemGroup() {
    	String uid = KatelloUtils.getUniqueID();
		this.systemgroup = "systemgroup"+uid;
    	String akName="ak-subscription-zoo3-"+uid;
    	SSHCommandResult res;

    	KatelloActivationKey ak = new KatelloActivationKey(this.cli_worker, this.organization, this.env, akName, "Activation key to add system group", null);
    	res = ak.create();
    	Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (activation_key create)");
    	
		KatelloSystemGroup systemGroup = new KatelloSystemGroup(this.cli_worker, this.systemgroup, this.organization);
		res = systemGroup.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		
    	res = ak.add_system_group(this.systemgroup);
    	Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (activation_key add_system_group)");
    	Assert.assertTrue(getOutput(res).contains(
				String.format(KatelloActivationKey.OUT_ADD_SYSTEMGROUP, akName)), 
				"Check - returned output string (activation_key add_system_group)");
    }
    
    @Test(description="remove system group from activationkey", enabled=true)
    public void test_removeSystemGroup() {
    	String uid = KatelloUtils.getUniqueID();
		this.systemgroup = "systemgroup"+uid;
    	String akName="ak-subscription-zoo3-"+uid;
    	SSHCommandResult res;

    	KatelloActivationKey ak = new KatelloActivationKey(this.cli_worker, this.organization, this.env, akName, "Activation key to add and remove system group", null);
    	res = ak.create();
    	Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (activation_key create)");
    	
		KatelloSystemGroup systemGroup = new KatelloSystemGroup(this.cli_worker, this.systemgroup, this.organization);
		res = systemGroup.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		
    	res = ak.add_system_group(this.systemgroup);
    	Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (activation_key add_system_group)");
    	
    	res = ak.remove_system_group(this.systemgroup);
    	Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (activation_key remove_system_group)");
    	Assert.assertTrue(getOutput(res).contains(
				String.format(KatelloActivationKey.OUT_REMOVE_SYSTEMGROUP, akName)), 
				"Check - returned output string (activation_key remove_system_group)");
    }
    
    @Test(description="As a user, I would like to use more than one activation keys.", groups = {"cfse-cli"})
    public void test_regTwokeys() {
    	String uid = KatelloUtils.getUniqueID();
    	String act_list = "";
    	String akName1="act_key1-"+ uid; 
    	String akName2="act_key2-"+uid;
    	SSHCommandResult res;
    	KatelloActivationKey ak = new KatelloActivationKey(this.cli_worker, this.organization, this.env, akName1, "Activation key created  ", null);
    	res = ak.create();
    	Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (activation_key create)");
    	ak.asserts_create();
    	
    	ak = new KatelloActivationKey(this.cli_worker, this.organization, this.env, akName2, "Activation key created ", null);
     	res = ak.create();
     	Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (activation_key create)");
     	ak.asserts_create();     	
    	act_list = akName1 + "," + akName2;
    	sshOnClient(KatelloSystem.RHSM_CLEAN);    	
    	String systemName = "localhost-"+KatelloUtils.getUniqueID();
		KatelloSystem sys = new KatelloSystem(this.cli_worker, systemName, this.organization, null);
		res = sys.rhsm_registerForce_multiplekeys(act_list); 
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		res = sys.info();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(KatelloCliTestBase.sgetOutput(res).contains(akName1 +", "+akName2), 
				          String.format("Activationkeys [%s] found in system [%s] info",act_list,systemName));		
    }
    
    @Test(description="As an admin, I'd like to see which activation key used it for registering the system")
    public void test_Viewregkey() {
    	String uid = KatelloUtils.getUniqueID();
    	String akName="act_key-"+ uid;    
    	SSHCommandResult res;
    	KatelloActivationKey ak = new KatelloActivationKey(this.cli_worker, this.organization, this.env, akName, "Activation key created ", null);
    	res = ak.create();
    	Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (activation_key create)");
    	ak.asserts_create();   	
    	sshOnClient(KatelloSystem.RHSM_CLEAN); 	
    	String systemName = "localhost-"+KatelloUtils.getUniqueID();
		KatelloSystem sys = new KatelloSystem(this.cli_worker, systemName, this.organization, null);
		res = sys.rhsm_registerForce(akName); 
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		res = sys.info();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(KatelloCliTestBase.sgetOutput(res).contains(akName), 
				          String.format("Activationkey [%s] found in system [%s] info",akName,systemName));		
    }
}
