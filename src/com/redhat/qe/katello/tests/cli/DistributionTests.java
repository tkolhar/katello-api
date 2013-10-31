package com.redhat.qe.katello.tests.cli;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloDistribution;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.base.tngext.TngPriority;
import com.redhat.qe.katello.common.KatelloUtils;

@TngPriority(14)
public class DistributionTests extends KatelloCliTestBase {

	String prov_name;
	String prod_name;
	String repo_name;
	String repo_id;

	@BeforeClass()
	public void setUp() {
		String uid = KatelloUtils.getUniqueID();
		prov_name = "provider-"+uid;
		prod_name = "product-"+uid;
		repo_name = "repo-"+uid;

		KatelloProvider prov = new KatelloProvider(cli_worker, prov_name, base_org_name, null, null);
		exec_result = prov.create();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (provider create)");
		KatelloProduct prod = new KatelloProduct(null, prod_name, base_org_name, prov_name, null, null, null, null, null);
		exec_result = prod.create();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (product create)");
		KatelloRepo repo = new KatelloRepo(null, repo_name, base_org_name, prod_name, KatelloRepo.getFedoraMirror("18"), null, null);
		exec_result = repo.create();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (repo create)");
		exec_result = repo.synchronize(); // takes 6-7 minutes
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (repo sync)");

		exec_result = repo.info();
		repo_id = KatelloUtils.grepCLIOutput("ID", getOutput(exec_result));
		Assert.assertNotNull(repo_id, "Check not null (repo id)");
	}

	@Test(description="list distributions test")
	public void test_distributionList() {
		KatelloDistribution dist = new KatelloDistribution(null, base_org_name, prod_name, repo_name, null);
		exec_result = dist.list();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (distribution list)");
		Assert.assertTrue(getOutput(exec_result).contains(KatelloDistribution.FEDORA18_DISTRIBUTION), "Check output (distribution list)");

		dist.repo_id = repo_id;
		exec_result = dist.list();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (distribution list)");
		Assert.assertTrue(getOutput(exec_result).contains(KatelloDistribution.FEDORA18_DISTRIBUTION), "Check output (distribution list)");
	}

	@Test(description="distribution info test")
	public void test_distributionInfo() {
		KatelloDistribution dist = new KatelloDistribution(null, base_org_name, prod_name, repo_name, null);
		exec_result = dist.info(repo_id, KatelloDistribution.FEDORA18_DISTRIBUTION);
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (distribution info)");
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", " ").matches(String.format(KatelloDistribution.REG_DISTRIBUTION_INFO, KatelloDistribution.FEDORA18_DISTRIBUTION)), "Check output (distribution info)");

		String fake_distribution = "foo-fedora";
		exec_result = dist.info(repo_id, fake_distribution);
		Assert.assertTrue(exec_result.getExitCode()==148, "Check exit code (distribution list)");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloDistribution.ERR_NOT_FOUND, fake_distribution)), "Check output (distribution list)");
	}
}
