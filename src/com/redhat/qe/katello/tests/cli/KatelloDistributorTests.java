package com.redhat.qe.katello.tests.cli;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.redhat.qe.Assert;
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
		exec_result = distributor.add_info(keyname, value);
		Assert.assertTrue(exec_result.getExitCode().intValue() == exitCode.intValue(), "Check - return code");		
	} 
}

