package com.redhat.qe.katello.tests.i18n;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

public class EnvironmentTests extends KatelloCliTestBase {
	
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
	}
	
	@Test(description="environment create")
	public void test_createEnvironment(){
		SSHCommandResult res;
		KatelloEnvironment env = new KatelloEnvironment(env_name, getText("environment.create.description"), 
				org_name, KatelloEnvironment.LIBRARY);
		res = env.cli_create();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		String outSuccess = getText("environment.create.stdout", env_name);
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code (environment create)");
		Assert.assertTrue(getOutput(res).equals(outSuccess), "Check - stdout (environment create)");
	}
	
	@Test(description="environment info", dependsOnMethods={"test_createEnvironment"})
	public void test_infoEnvironment(){
		KatelloEnvironment env = new KatelloEnvironment(env_name, null, org_name, null);
		SSHCommandResult res = env.cli_info();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code (environment info)");
		Assert.assertTrue(KatelloUtils.grepCLIOutput(getText("environment.list.stdout.property.name"), 
				getOutput(res)).equals(env_name),"Check - name in info");
		Assert.assertTrue(KatelloUtils.grepCLIOutput("Description", getOutput(res)).equals(getText("environment.create.description")),"Check - description in info");
	}
	
	@Test(description="environment list", dependsOnMethods={"test_createEnvironment"})
	public void test_listEnvironment(){
		KatelloEnvironment env = new KatelloEnvironment(null, null, org_name, null);
		SSHCommandResult res = env.cli_list();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(res).contains(env_name), 
				"Check - stdout (environment list: name)");
		Assert.assertTrue(getOutput(res).contains(getText("environment.create.description")), 
				"Check - stdout (environment list: description)");
	}
	
	@Test(description="environment update", dependsOnMethods={"test_createEnvironment"})
	public void test_updateEnvironment(){
		KatelloEnvironment env = new KatelloEnvironment(env_name, null, org_name, null);
		SSHCommandResult res= env.cli_update(getText("environment.update.description"));
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code (environment update)");
		Assert.assertTrue(getOutput(res).equals(getText("environment.update.stdout", env_name)), "Check - stdout (environment update)");
	}

	@Test(description="environment delete", dependsOnMethods={"test_infoEnvironment","test_updateEnvironment","test_listEnvironment"})
	public void test_deleteEnvironment(){
		SSHCommandResult res;

		KatelloEnvironment env = new KatelloEnvironment(env_name, null, org_name, null);
		res = env.cli_delete();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code (environment delete)");
		Assert.assertTrue(getOutput(res).equals(getText("environment.delete.stdout", env_name)),
				"Check - stdout (environment delete)");
	}
	
}
