package com.redhat.qe.katello.tests.hammer.cli;

import com.redhat.qe.Assert;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;

import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.HammerDomain;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

public class DomainTests extends KatelloCliTestBase {
	
	private SSHCommandResult exec_result;
	private String name;
	private String newName;
	private String fullName;
	
	@BeforeClass(description="Prepare an data to work with")
	public void setup(){
		String uid = KatelloUtils.getUniqueID();
		this.name = uid+".domain";
		this.fullName = "fullname."+uid+".domain";
	}
	
	@Test(description="Create a domain")
	public void test_domainCreate() {
		HammerDomain domain = new HammerDomain(cli_worker, name, fullName, null );
		exec_result = domain.cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(HammerDomain.OUT_CREATE), "Check - returned output string");
	}
	
	//TODO: Confirm error messages 
	@Test(description="Create duplicate domain", dependsOnMethods={"test_domainCreate"})
	public void test_duplicateDomainCreate() {
		//Duplicate name
		HammerDomain domain = new HammerDomain(cli_worker, name, "new Full Name", null);
		exec_result = domain.cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 65, "Check - error code");
		Assert.assertTrue(getOutput(exec_result).contains(HammerDomain.ERR_CREATE), "Check - returned output string");
		//Duplicate fullName
		domain = new HammerDomain(cli_worker, "newName.domain", fullName, null);
		exec_result = domain.cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 65, "Check - error code");
		Assert.assertTrue(getOutput(exec_result).contains(HammerDomain.ERR_CREATE), "Check - returned output string");	
	}
	
	@Test(description="Verify info of a domain", dependsOnMethods={"test_duplicateDomainCreate"})
	public void test_domainInfo()
	{
		HammerDomain domain = new HammerDomain(cli_worker, name, fullName, null);
		assert_DomainInfo(domain);
	}
	
	@Test(description="update previously created domain", dependsOnMethods={"test_domainInfo"})
	public void test_domainUpdate()
	{
		String uid = KatelloUtils.getUniqueID();
		HammerDomain domain = new HammerDomain(cli_worker, name, fullName, null);
		domain.fullName = "newFullName-"+uid;
		newName = "newName."+uid;
		exec_result = domain.update(newName);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(HammerDomain.OUT_UPDATE), "Check - returned output string");
		domain.name = newName;
		assert_DomainInfo(domain);
	}
	//TODO: search, page, order
	@Test(description="List domains. Check if updated name is present", dependsOnMethods={"test_domainUpdate"})
	public void test_domianList()
	{
		HammerDomain domain = new HammerDomain(cli_worker, newName, fullName, null);
		exec_result = domain.cli_list();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(newName), "Check - updated domain name is listed");
		Assert.assertFalse(getOutput(exec_result).contains(name), "Check - previous namenot present");
	}
	
	@Test(description="delete domain", dependsOnMethods={"test_domianList"})
	public void test_domainDelete()
	{
		HammerDomain domain = new HammerDomain(cli_worker, newName, fullName, null);
		exec_result = domain.delete();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(HammerDomain.OUT_DELETE), "Check - returned output string");
		//should not be listed
		exec_result = domain.cli_list();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertFalse(getOutput(exec_result).contains(newName), "Check - deleted domain is not listed");
	}
	
	private void assert_DomainInfo(HammerDomain domain) {
		if (domain.fullName == null) domain.fullName = "";
		if (domain.dns_id == null) domain.dns_id = "";

		exec_result = domain.cli_info();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");

		String match_info = String.format(HammerDomain.REG_DOMAIN_INFO, domain.name, domain.fullName, domain.dns_id).replaceAll("\"", "");
		log.finest(String.format("Domain (info) match regex: [%s]", match_info));
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", " ").matches(match_info), "Domain info should match the provided info");
	}
}
