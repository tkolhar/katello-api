package com.redhat.qe.katello.tests.cli;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.auto.testng.Assert;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloFilter;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups={"cfse-cli"})
public class FilterTests extends KatelloCliTestScript{
	String org;
	String environment;
	
	@BeforeClass(description="init: create org stuff", groups = {"cli-filter"})
	public void setUp(){
		SSHCommandResult res;
		String uid = KatelloTestScript.getUniqueID();
		this.org = "filter-"+uid;
		this.environment = "Dev-"+uid;
		KatelloOrg org = new KatelloOrg(this.org, null);
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (org create)");
		KatelloEnvironment env = new KatelloEnvironment(this.environment, null, this.org, KatelloEnvironment.LIBRARY);
		res = env.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (env create)");
	}

	@Test(description="Create filter: no packages", groups = {"cli-filter"})
	public void test_createWithoutPackages(){
		String filterName = "filter"+KatelloTestScript.getUniqueID();
		KatelloFilter filter = new KatelloFilter(filterName, this.org, null, null);
		SSHCommandResult res = filter.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (filter create)");
		Assert.assertTrue(getOutput(res).equals(String.format(KatelloFilter.OUT_CREATE, filterName)), 
				"Check - output string (filter create)");
	}
}
