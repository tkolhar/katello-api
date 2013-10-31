package com.redhat.qe.katello.tests.hammer.cli;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.HammerHostgroup;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

public class HostgroupTests extends KatelloCliTestBase {
	
	private String name;
	private String[] base_names;
	
	@BeforeClass(description="Prepare an data to work with")
	public void setUp(){
		String uid = KatelloUtils.getUniqueID().substring(7);
		this.name = "hostgroup"+uid;
		this.base_names = new String[10];
		for (int i = base_names.length - 1; i >= 0; i--) {
			base_names[i] = "ar" + i + "ch" + uid;
			new HammerHostgroup(cli_worker, base_names[i]).cli_create();
		}
	}
	
	@Test(description="create Hostgroup")
	public void testHostgroup_create() {
		SSHCommandResult res;
		
		HammerHostgroup hgroup = new HammerHostgroup(cli_worker, name);
		res = hgroup.cli_create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		
		Assert.assertTrue(getOutput(res).contains(String.format(HammerHostgroup.OUT_CREATE, name)),"Check - returned output string");
	}

	@Test(description="create Hostgroup which name exists", dependsOnMethods={"testHostgroup_create"})
	public void testHostgroup_createExists() {
		SSHCommandResult res;
		
		HammerHostgroup hgroup = new HammerHostgroup(cli_worker, name);
		res = hgroup.cli_create();
		Assert.assertFalse(res.getExitCode().intValue() == 0, "Check - return code");

		Assert.assertTrue(getOutput(res).contains(HammerHostgroup.ERR_NAME_EXISTS),"Check - returned error string");
	}
}
