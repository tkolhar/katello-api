package com.redhat.qe.katello.tests.e2e;

import java.util.logging.Logger;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloErrata;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

/**
 * Implementation of one of V1 scenarios:<BR>
 * Description:<BR>
 * Promoting errata to the next environment.
 * @author gkhachik
 */
public class PromoteErrata extends KatelloCliTestBase{
	protected static Logger log = Logger.getLogger(PromoteErrata.class.getName());

	public static final String ERRATA_ZOO_SEA = "RHEA-2012:0002";
	
	private String org;
	private String env = "Dev";
	private String provider;
	private String product;
	private String repo;
	
	@BeforeClass(description="Init unique names", alwaysRun=true)
	public void setUp(){
		String uniqueID = KatelloUtils.getUniqueID();
		this.org = "Zoo Corporation "+uniqueID;
		this.provider = "ZooProv"+uniqueID;
		this.product = "ZooProd"+uniqueID;
		this.repo = "ZooRepo"+uniqueID;

		
		log.info("E2E - Create org");
		KatelloOrg org = new KatelloOrg(this.cli_worker, this.org, null);
		org.cli_create();
	}
	
	@Test(description="Create org, provider, product and repo", enabled=true)
	public void test_prepareRepo(){
		log.info("E2E - Create provider/product/repo");
		KatelloProvider prov = new KatelloProvider(this.cli_worker, this.provider,this.org, null, null);
		prov.create(); // create provider
		KatelloProduct prod = new KatelloProduct(this.cli_worker, this.product, this.org, this.provider, null, null, null, null, null);
		prod.create(); // create product
		KatelloRepo repo = new KatelloRepo(this.cli_worker, this.repo, this.org, this.product, PackagesWithGPGKey.REPO_INECAS_ZOO3, null, null);
		repo.create(); // create repo
	}
	
	@Test(description="Create environment", enabled=true)
	public void test_prepareEnv(){
		log.info("E2E - Create environment");
		KatelloEnvironment env = new KatelloEnvironment(this.cli_worker, this.env, null, this.org, KatelloEnvironment.LIBRARY);
		env.cli_create();
	}
	
	@Test(description="Promote empty product/repo structure to Dev", dependsOnMethods={"test_prepareRepo","test_prepareEnv"}, enabled=true)
	public void test_promoteToDevNoSync(){
		log.info("E2E - Promote to Dev - not synced");
		KatelloUtils.promoteProductToEnvironment(cli_worker, this.org, this.product, this.env);
	}
		
	@Test(description="Synchronize repository", dependsOnMethods={"test_promoteToDevNoSync"}, enabled=true)
	public void test_syncRepo(){
		log.info("E2E - Synchronize repo");
		KatelloRepo repo = new KatelloRepo(this.cli_worker, this.repo, this.org, this.product, REPO_INECAS_ZOO3, null, null);
		repo.synchronize();
	}
	
	@Test(description="Add errata and promote 2nd time", dependsOnMethods={"test_syncRepo"}, enabled=true)
	public void test_addErrataAndPromote(){
		SSHCommandResult res;
		log.info("E2E - Add errata (only) and promote again");
		KatelloUtils.promoteErratasToEnvironment(cli_worker, this.org, this.product, this.repo, new String[] {ERRATA_ZOO_SEA}, this.env);
		
		KatelloErrata ert = new KatelloErrata(cli_worker, ERRATA_ZOO_SEA, this.org, this.product, this.repo, this.env);
		res = ert.info();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (errata info --environment Dev)");
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").contains(ERRATA_ZOO_SEA), "Check - errata info output");
	}
	
}
