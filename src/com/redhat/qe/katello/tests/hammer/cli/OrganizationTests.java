package com.redhat.qe.katello.tests.hammer.cli;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.HammerOrganization;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

public class OrganizationTests extends KatelloCliTestBase {

	private SSHCommandResult exec_result;
	private String id;
	private String name;
	private String newName;
	
	@BeforeClass(description="Prepare data to work with")
	public void setup(){
		String uid = KatelloUtils.getUniqueID();
		this.name = "orgName"+uid;
	}
	
	@Test(description="Create an organization")
	public void test_organizationCreate() {
		HammerOrganization org = new HammerOrganization(cli_worker, name);
		exec_result = org.cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).equals(HammerOrganization.OUT_CREATE), "Check - returned output string");
	}
	
	// bz#1023125
	@Test(description="Create duplicate organization", dependsOnMethods={"test_organizationCreate"})
	public void test_duplicateOrgCreate() {
		//Duplicate name
		HammerOrganization org = new HammerOrganization(cli_worker, name);
		exec_result = org.cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 65, "Check - error code");
		Assert.assertTrue(getOutput(exec_result).contains(HammerOrganization.ERR_CREATE), "Check - returned output string");
	}
	
	//TODO: create organization name - different variations. Accepts only alphanumeric without spaces

	@Test(description="Verify info of an organization", dependsOnMethods={"test_organizationCreate"})
	public void test_organizationInfo()
	{
		HammerOrganization org = new HammerOrganization(cli_worker, name);
		assert_OrganizationInfo(org);
		//verify error for invalid org name info
		org = new HammerOrganization(cli_worker, "invalidName");
		assert_OrganizationInfo(org);
		
		//TODO: fetch info by ID
	} 
	
	//TODO: update using ID of the organization
	  @Test(description="update previously created organization", dependsOnMethods={"test_organizationInfo"})
	  public void test_organizationUpdate()
	  {
		  String uid = KatelloUtils.getUniqueID();
		  HammerOrganization org = new HammerOrganization(cli_worker, name);
		  newName = "newName"+uid;
		  exec_result = org.update(newName);
		  Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		  Assert.assertTrue(getOutput(exec_result).contains(HammerOrganization.OUT_UPDATE), "Check - returned output string");
		  org.name = newName;
		  assert_OrganizationInfo(org);

		  //TODO: update to an existing name
	  } 

	  //TODO: search, page, order
	  @Test(description="List organizations. Check if updated name is present", dependsOnMethods={"test_organizationUpdate"})
	  public void test_organizationList()
	  {
		  HammerOrganization org = new HammerOrganization(cli_worker, newName);
		  exec_result = org.cli_list(null, null, null);
		  Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		  Assert.assertTrue(getOutput(exec_result).contains(newName), "Check - updated domain name is listed");
		  Assert.assertFalse(getOutput(exec_result).contains(name), "Check - previous namenot present");
	  }

	  //TODO: delete organization by ID
	  @Test(description="delete environment", dependsOnMethods={"test_organizationList"})
	  public void test_organizationDelete()
	  {
		  HammerOrganization org = new HammerOrganization(cli_worker, newName);
		  exec_result = org.delete(null);
		  Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		  Assert.assertTrue(getOutput(exec_result).contains(HammerOrganization.OUT_DELETE), "Check - returned output string");
		  //should not be listed
		  exec_result = org.cli_list(null, null, null);
		  Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		  Assert.assertFalse(getOutput(exec_result).contains(newName), "Check - deleted domain is not listed");
	  } 
	  
	  //delete non existing organization. Verify error
	  @Test(description="delete environment", dependsOnMethods={"test_organizationList"})
	  public void test_DeleteNonExistentOrg()
	  {
		  HammerOrganization org = new HammerOrganization(cli_worker, "invalidName");
		  exec_result = org.delete(null);
		  Assert.assertTrue(exec_result.getExitCode() == 128, "Check - return code");
		  Assert.assertTrue(getOutput(exec_result).contains(HammerOrganization.ERR_DELETE), "Check - returned output string");
	  } 

	private void assert_OrganizationInfo(HammerOrganization org) {
		exec_result = org.cli_info(null);
		if(exec_result.getExitCode().intValue() == 0) 
		{
			String match_info = String.format(HammerOrganization.REG_ORGANIZATION_INFO, org.name).replaceAll("\"", "");
			log.finest(String.format("Organization (info) match regex: [%s]", match_info));
			Assert.assertTrue(getOutput(exec_result).replaceAll("\n", " ").matches(match_info), "Organization info should match the provided info");
		}
		else
		{
			Assert.assertTrue(exec_result.getExitCode().intValue() == 128, "Check - error code");
			Assert.assertTrue(getOutput(exec_result).equals(HammerOrganization.ERR_404), "Check - returned output string");
		}
	} 
	
}
