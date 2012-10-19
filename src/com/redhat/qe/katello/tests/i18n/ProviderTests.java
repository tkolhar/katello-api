package com.redhat.qe.katello.tests.i18n;

import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

public class ProviderTests extends KatelloCliTestScript {

	private String org_name;
	private String provider_name;
	private String uid;
	
	@Test(description="init - org", groups={"i18n-init"})
	public void init(){
		uid = KatelloUtils.getUniqueID();
		org_name = getText("org.create.name")+" "+uid;
		KatelloOrg org = new KatelloOrg(org_name, null);
		SSHCommandResult res = org.cli_create();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
	}
	
	@Test(description="create provider", dependsOnMethods={"init"})
	public void createProvider(){
		provider_name = getText("provider.create.name")+" "+uid;
		String provider_description = getText("provider.create.description");
		KatelloProvider prov = new KatelloProvider(provider_name, org_name, provider_description, null);
		SSHCommandResult res = prov.create();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
	}
}
