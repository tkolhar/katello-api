package com.redhat.qe.katello.tests.e2e;

import java.util.logging.Logger;

import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.common.TngRunGroups;
import com.redhat.qe.tools.SSHCommandResult;


@Test(groups={TngRunGroups.TNG_KATELLO_Organizations}, singleThreaded=true, priority=300) // there is scenario in TCMS under Organizations plan.
public class OrgDeletion extends KatelloCliTestBase{
	protected static Logger log = Logger.getLogger(BPMTests.class.getName());
	
	private SSHCommandResult exec_result;

	// Katello objects below
	private String org_name;
	private String repo_name;
	private String env_name;
	private String provider_name;
	private String product_name;

	/** TCMS scenario is: <a href="https://tcms.engineering.redhat.com/case/243070/?from_plan=7791">here</a> */
	@Test(description="3d47951e-6a69-4297-8afe-bae8632fcb10",groups={TngRunGroups.TNG_KATELLO_Organizations})
	public void test_deleteOrg(){
		
		KatelloOrg org = createOrgStuff(null);
		
		exec_result = org.delete();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(String.format("Successfully deleted org [ %s ]",org.name)),"Check - return string");
		
		// to be sure that the same org can be created after deletion
		createOrgStuff(org.name);
	}
	
	@Test(description="Create 2 Orgs, add same repo to them, sync them, delete the first org", dependsOnMethods={"test_deleteOrg"})
	public void test_deleteOrgSharingRepo(){
		
		KatelloOrg org = createOrgStuff(null);
		
		createOrgStuff(null);
		
		exec_result = org.delete();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(String.format("Successfully deleted org [ %s ]",org.name)),"Check - return string");
		
		// to be sure that the same org can be created after deletion
		createOrgStuff(org.name);
	}
	
	private KatelloOrg createOrgStuff(String name) {
		
		String uid = KatelloUtils.getUniqueID();
		
		// if name is provided use it, otherwise generate it
		if (name == null) { 
			org_name = "Paris"+uid;
		} else {
			org_name = name;
		}
		repo_name = "Pulp"+uid;
		env_name = "Dev"+uid;
		provider_name = "provider"+uid;
		product_name = "product"+uid;
		
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
