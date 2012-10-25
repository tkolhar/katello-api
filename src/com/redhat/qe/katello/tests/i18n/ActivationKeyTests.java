package com.redhat.qe.katello.tests.i18n;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.obj.KatelloActivationKey;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

public class ActivationKeyTests extends KatelloCliTestScript {
	
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
	
	@Test(description="create activation_key")
	public void test_createAK(){
		SSHCommandResult res;
		String ak_name = getText("activation_key.create.name")+" "+uid;
		String ak_descr = getText("activation_key.create.description")+" "+uid;
		String outSuccess = getText("activation_key.create.stdout", ak_name);
		
		KatelloActivationKey ak = new KatelloActivationKey(org_name, env_name, ak_name, ak_descr, null);
		res = ak.create();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code (activation_key create)");
		Assert.assertTrue(getOutput(res).equals(outSuccess), "Check - stdout (provider create)");
	}
}
