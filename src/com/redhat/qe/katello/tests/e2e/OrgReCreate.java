package com.redhat.qe.katello.tests.e2e;

import java.util.logging.Logger;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;


@Test(groups={"cfse-e2e"}, singleThreaded = true)
public class OrgReCreate extends KatelloCliTestBase{
	protected static Logger log = Logger.getLogger(BPMTests.class.getName());
	
	private SSHCommandResult exec_result;

	// Katello objects below
	private String org_name;
	private String repo_name;
	private String env_name;
	private String provider_name;
	private String product_name;
	String uid = KatelloUtils.getUniqueID();

	@BeforeClass(description="Generate unique objects")
	public void setUp() {
		// if name is provided use it, otherwise generate it
		if (org_name == null) { 
			org_name = "Paris"+uid;
		}
		repo_name = "Pulp"+uid;
		env_name = "Dev"+uid;
		provider_name = "provider"+uid;
		product_name = "product"+uid;
	}
	
	@Test(description="Create a new Org, add repo, sync it, delete the org," +
			" verify that repo is deleted from file system.")
	public void test_deleteOrg(){
		
		KatelloOrg org = createOrgStuff();

		exec_result = org.delete();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(String.format("Successfully deleted org [ %s ]",org.name)),"Check - return string");

		// to be sure that the same org can be created after deletion
		createOrgStuff();
	}

	
	private KatelloOrg createOrgStuff() {
		
		// Create org:
		KatelloOrg org = new KatelloOrg(this.cli_worker, org_name, "Org deletion");
		exec_result = org.cli_create();
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), "Successfully created org [ "+org_name+" ]");
		
		// Create provider:
		KatelloProvider prov = new KatelloProvider(this.cli_worker, provider_name, org_name, "Package provider", PULP_RHEL6_x86_64_REPO);
		exec_result = prov.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		// Create product:
		KatelloProduct prod = new KatelloProduct(this.cli_worker, product_name, org_name, provider_name, null, null, null, null, null);
		exec_result = prod.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	
		KatelloRepo repo = new KatelloRepo(this.cli_worker, repo_name, org_name, product_name, PULP_RHEL6_x86_64_REPO, null, null);
		exec_result = repo.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		KatelloEnvironment env = new KatelloEnvironment(this.cli_worker, env_name, null, org_name, KatelloEnvironment.LIBRARY);
		exec_result = env.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = prov.synchronize();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		return org;
	}
	
	
	
}
