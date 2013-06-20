package com.redhat.qe.katello.tests.e2e;

import java.util.logging.Logger;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloErrata;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloPackage;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups={"cfse-e2e"})
public class PromoteChangeset extends KatelloCliTestBase {
	protected static Logger log = Logger.getLogger(PromoteChangeset.class.getName());
	
	private String org;
	private String env1 = "DEV";
	private String env2 = "GA";
	private String env3 = "QE";
	private String provider;
	private String product;
	private String repo;
	private String uniqueID;
	
	@BeforeClass(description="Init unique names", alwaysRun=true)
	public void setUp(){
		SSHCommandResult res;
		uniqueID = KatelloUtils.getUniqueID();
		this.org = "Zoo Corporation "+uniqueID;
		this.provider = "ZooProv"+uniqueID;
		this.product = "ZooProd"+uniqueID;
		this.repo = "ZooRepo"+uniqueID;
		
		KatelloOrg org = new KatelloOrg(this.org, null);
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		KatelloProvider prov = new KatelloProvider(this.provider,this.org, null, null);
		res = prov.create(); // create provider
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		KatelloProduct prod = new KatelloProduct(this.product, this.org, this.provider, null, null, null, null, null);
		res = prod.create(); // create product
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		KatelloRepo repo = new KatelloRepo(this.repo, this.org, this.product, PackagesWithGPGKey.REPO_INECAS_ZOO3, null, null);
		res = repo.create(); // create repo
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		res = repo.synchronize();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		
		KatelloEnvironment env = new KatelloEnvironment(this.env1, null, this.org, KatelloEnvironment.LIBRARY);
		res = env.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		env = new KatelloEnvironment(this.env2, null, this.org, this.env1);
		res = env.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		env = new KatelloEnvironment(this.env3, null, this.org, this.env2);
		res = env.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		
//		res = prod.promote(this.env1);
//		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
//		
//		res = prod.promote(this.env2);
//		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
	}

	
	@Test(description="Promote package to DEV")
	public void test_promotePackageToDEV(){
		KatelloUtils.promotePackagesToEnvironment(this.org, this.product, this.repo, new String[] {"lion"}, this.env1);
		
		KatelloPackage pack = new KatelloPackage(null, null, this.org, this.product, this.repo, this.env1);
		SSHCommandResult res = pack.cli_list();
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").contains("lion"), "Check - package list output");
	}

	//@ TODO 918157
	@Test(description="Promote errata to DEV")
	public void test_promoteErrataToDEV(){
		KatelloUtils.promoteErratasToEnvironment(this.org, this.product, this.repo, new String[] {PromoteErrata.ERRATA_ZOO_SEA}, this.env1);

		KatelloErrata err = new KatelloErrata(null, this.org, this.product, this.repo, this.env1);
		SSHCommandResult res = err.cli_list();
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").contains(PromoteErrata.ERRATA_ZOO_SEA), "Check - errata list output");
	}
	
	@Test(description="Promote package to GA")
	public void test_promotePackageToGA(){
		KatelloUtils.promotePackagesToEnvironment(this.org, this.product, this.repo, new String[] {"lion"}, this.env2);

		KatelloPackage pack = new KatelloPackage(null, null, this.org, this.product, this.repo, this.env2);
		SSHCommandResult res = pack.cli_list();
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").contains("lion"), "Check - package list output");
	}

	//@ TODO 918157
	@Test(description="Promote errata to GA")
	public void test_promoteErrataToGA(){
		KatelloUtils.promoteErratasToEnvironment(this.org, this.product, this.repo, new String[] {PromoteErrata.ERRATA_ZOO_SEA}, this.env2);
		
		KatelloErrata err = new KatelloErrata(null, this.org, this.product, this.repo, this.env2);
		SSHCommandResult res = err.cli_list();
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").contains(PromoteErrata.ERRATA_ZOO_SEA), "Check - errata list output");
	}

	@Test(description="Delete package from GA", dependsOnMethods={"test_promotePackageToGA"})
	public void test_deletePackageFromGA(){
		KatelloUtils.removePackagesFromEnvironment(this.org, this.product, this.repo, new String[] {"lion"}, this.env2);

		KatelloPackage pack = new KatelloPackage(null, null, this.org, this.product, this.repo, this.env2);
		SSHCommandResult res = pack.cli_list();
		Assert.assertFalse(getOutput(res).replaceAll("\n", "").contains("lion"), "Check - package list output");
	}

	@Test(description="Delete errata from GA", dependsOnMethods={"test_promoteErrataToGA"})
	public void test_deleteErrataFromGA(){
		KatelloUtils.removeErratasFromEnvironment(this.org, this.product, this.repo, new String[] {PromoteErrata.ERRATA_ZOO_SEA}, this.env2);

		KatelloErrata err = new KatelloErrata(null, this.org, this.product, this.repo, this.env2);
		SSHCommandResult res = err.cli_list();
		Assert.assertFalse(getOutput(res).replaceAll("\n", "").contains(PromoteErrata.ERRATA_ZOO_SEA), "Check - errata list output");
	}
}
