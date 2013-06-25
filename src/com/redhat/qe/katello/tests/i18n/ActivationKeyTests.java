package com.redhat.qe.katello.tests.i18n;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloActivationKey;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloSystemGroup;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

public class ActivationKeyTests extends KatelloCliTestBase {
	
	private String uid;
	private String org_name;
	private String env_name;
	
	@BeforeClass(description="create org", alwaysRun=true)
	public void setUp(){
		uid = KatelloUtils.getUniqueID();
		org_name = getText("org.create.name")+" "+uid;
		env_name = getText("environment.create.name")+" "+uid;
		
		KatelloOrg org = new KatelloOrg(org_name, null);
		SSHCommandResult res = org.cli_create();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		KatelloEnvironment env = new KatelloEnvironment(env_name, null, org_name, KatelloEnvironment.LIBRARY);
		res = env.cli_create();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
	}
	
	@Test(description="activation_key create")
	public void test_createAK(){
		SSHCommandResult res;
		String ak_name = getText("activation_key.create.name")+" "+uid;
		String ak_descr = getText("activation_key.create.description")+" "+uid;
		String outSuccess = getText("activation_key.create.stdout", ak_name);
		
		KatelloActivationKey ak = new KatelloActivationKey(org_name, env_name, ak_name, ak_descr, null);
		res = ak.create();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code (activation_key create)");
		Assert.assertTrue(getOutput(res).equals(outSuccess), "Check - stdout (activation_key create)");
	}
	
	@Test(description="activation_key list", dependsOnMethods={"test_createAK"})
	public void test_listAK(){
		String ak_name = getText("activation_key.create.name")+" "+uid;
		String ak_descr = getText("activation_key.create.description")+" "+uid;

		KatelloActivationKey key = new KatelloActivationKey(org_name, env_name, null, null, null);
		SSHCommandResult res = key.list();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code (activation_key list)");
		Assert.assertTrue(KatelloUtils.grepCLIOutput(getText("activation_key.list.stdout.property.name"), 
				getOutput(res)).equals(ak_name),"Check - name in list");
		Assert.assertTrue(KatelloUtils.grepCLIOutput("Description", getOutput(res)).equals(ak_descr),"Check - description in list");
	}
	
	@Test(description="activation_key update", dependsOnMethods={"test_createAK"})
	public void test_updateAK(){
		String ak_name = getText("activation_key.create.name")+" "+uid;
		KatelloActivationKey key = new KatelloActivationKey(org_name, env_name, ak_name, null, null);
		SSHCommandResult res = key.extend_limit("10");
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code (activation_key update)");
		Assert.assertTrue(getOutput(res).equals(getText("activation_key.update.stdout", ak_name)), "Check - stdout (activation_key update)");
	}
	
	@Test(description="activation_key info", dependsOnMethods={"test_createAK"})
	public void test_infoAK(){
		String ak_name = getText("activation_key.create.name")+" "+uid;
		String ak_descr = getText("activation_key.create.description")+" "+uid;

		KatelloActivationKey key = new KatelloActivationKey(org_name, env_name, ak_name, null, null);
		SSHCommandResult res = key.info();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code (activation_key info)");
		Assert.assertTrue(KatelloUtils.grepCLIOutput(getText("activation_key.list.stdout.property.name"), 
				getOutput(res)).equals(ak_name),"Check - name in info");
		Assert.assertTrue(KatelloUtils.grepCLIOutput("Description", getOutput(res)).equals(ak_descr),"Check - description in info");
	}
	
	@Test(description="activation_key add_system_group", dependsOnMethods={"test_createAK"})
	public void test_addSystemGroupAK(){
		SSHCommandResult res;
		String ak_name = getText("activation_key.create.name")+" "+uid;
		String sg_name = getText("system_group.create.name")+" "+uid;

		KatelloSystemGroup sg = new KatelloSystemGroup(sg_name, org_name);
		res = sg.create();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code (system_group create)");		
		KatelloActivationKey key = new KatelloActivationKey(org_name, env_name, ak_name, null, null);
		res = key.add_system_group(sg_name);
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code (activation_key add_system_group)");
		Assert.assertTrue(getOutput(res).equals(getText("activation_key.add_system_group.stdout", ak_name)),
				"Check - stdout (activation_key add_system_group)");
	}
	
	@Test(description="activation_key remove_system_group", dependsOnMethods={"test_addSystemGroupAK"})
	public void test_removeSystemGroupAK(){
		SSHCommandResult res;
		String ak_name = getText("activation_key.create.name")+" "+uid;
		String sg_name = getText("system_group.create.name")+" "+uid;

		KatelloActivationKey key = new KatelloActivationKey(org_name, env_name, ak_name, null, null);
		res = key.remove_system_group(sg_name);
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code (activation_key remove_system_group)");
		Assert.assertTrue(getOutput(res).equals(getText("activation_key.remove_system_group.stdout", ak_name)),
				"Check - stdout (activation_key remove_system_group)");
	}
	
	@Test(description="activation_key delete", dependsOnMethods={"test_removeSystemGroupAK","test_listAK","test_updateAK","test_infoAK"})
	public void test_deleteAK(){
		SSHCommandResult res;
		String ak_name = getText("activation_key.create.name")+" "+uid;

		KatelloActivationKey key = new KatelloActivationKey(org_name, env_name, ak_name, null, null);
		res = key.delete();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code (activation_key delete)");
		Assert.assertTrue(getOutput(res).equals(getText("activation_key.delete.stdout", ak_name)),
				"Check - stdout (activation_key delete)");
	}
	
}
