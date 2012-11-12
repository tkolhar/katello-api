package com.redhat.qe.katello.tests.e2e;

import java.util.logging.Logger;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.obj.KatelloChangeset;
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
public class PromoteChangeset extends KatelloCliTestScript{
	protected static Logger log = Logger.getLogger(PromoteChangeset.class.getName());
	
	private String org;
	private String env1 = "DEV";
	private String env2 = "QE";
	private String env3 = "GA";
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
		
		res = prod.promote(this.env1);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
	}

	
	@Test(description="Promote package to DEV")
	public void test_promotePackageToDEV(){
		KatelloChangeset cs = new KatelloChangeset("ZooDEV1"+uniqueID, this.org, this.env1);
		SSHCommandResult res = cs.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		res = cs.update_add_package(this.product, "lion");
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		res = cs.apply();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		KatelloPackage pack = new KatelloPackage(null, null, this.org, this.product, this.repo, this.env1);
		res = pack.cli_list();
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").contains("lion"), "Check - package list output");
	}

	@Test(description="Promote errata to DEV")
	public void test_promoteErrataToDEV(){
		KatelloChangeset cs = new KatelloChangeset("ZooDEV2"+uniqueID, this.org, this.env1);
		SSHCommandResult res = cs.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		res = cs.update_fromProduct_addErrata(this.product, PromoteErrata.ERRATA_ZOO_SEA);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		res = cs.apply();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		KatelloErrata err = new KatelloErrata(null, this.org, this.product, this.repo, this.env1);
		res = err.cli_list();
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").contains(PromoteErrata.ERRATA_ZOO_SEA), "Check - errata list output");
	}
	
	@Test(description="Promote package to QE, verify that it fails as product is not promoted to this env")
	public void test_promotePackageToQE(){
		SSHCommandResult res = new KatelloCli("package list --org \"" + this.org + "\" --repo \"" + this.repo + "\" --product \"" + this.product + "\" | grep \"lion\" | awk '{print $1}'", null).run();
		String packageId = res.getStdout().trim();
		
		KatelloChangeset cs = new KatelloChangeset("ZooQE1"+uniqueID, this.org, this.env2);
		res = cs.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		res = cs.update_add_package(this.product, "lion");
		Assert.assertEquals(res.getExitCode().intValue(), 144, "Check - return code");
		Assert.assertEquals(getOutput(res).trim(), String.format(KatelloPackage.REG_CHS_PROMOTE_ERROR, packageId));
	}

	@Test(description="Promote errata to DEV, verify that it fails as product is not promoted to this env")
	public void test_promoteErrataToQE(){	
		KatelloChangeset cs = new KatelloChangeset("ZooQE2"+uniqueID, this.org, this.env2);
		SSHCommandResult res = cs.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		res = cs.update_fromProduct_addErrata(this.product, PromoteErrata.ERRATA_ZOO_SEA);
		Assert.assertEquals(res.getExitCode().intValue(), 144, "Check - return code");
		Assert.assertEquals(getOutput(res).trim(), String.format(KatelloErrata.REG_CHS_PROMOTE_ERROR, PromoteErrata.ERRATA_ZOO_SEA));
	}	

	@Test(description="Delete package from QE, verify that it fails as product is not promoted to this env")
	public void test_deletePackageFromQE(){
		KatelloChangeset cs = new KatelloChangeset("delZooQE3"+uniqueID, this.org, this.env2, true);
		SSHCommandResult res = cs.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		res = cs.update_add_package(this.product, "lion");
		Assert.assertEquals(res.getExitCode().intValue(), 244, "Check - return code");
		Assert.assertEquals(getOutput(res).trim(), KatelloPackage.REG_CHS_DEL_ERROR);
	}

	@Test(description="Delete errata to DEV, verify that it fails as product is not promoted to this env")
	public void test_deleteErrataFromQE(){	
		KatelloChangeset cs = new KatelloChangeset("delZooQE4"+uniqueID, this.org, this.env2, true);
		SSHCommandResult res = cs.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		res = cs.update_fromProduct_addErrata(this.product, PromoteErrata.ERRATA_ZOO_SEA);
		Assert.assertEquals(res.getExitCode().intValue(), 244, "Check - return code");
		Assert.assertEquals(getOutput(res).trim(), KatelloErrata.REG_CHS_DEL_ERROR);
	}
}
