package com.redhat.qe.katello.tests.e2e;

import java.util.logging.Logger;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

/**
 * Implementation of following scenario:<BR>
 * Users decides to filter out some package(s) (one of them also has an errata in the repo) and promote to the next env.<BR>
 * Process looks like:<BR>
 * <pre>
 * 	promote product (without sync)
 * 	sync the repo
 *	create another CS
 * 	add repo to changeset (with --from_product option - it promoted before)
 * 	promote the changeset
 *  create deletion type changeset
 *  add frog and bear tinto it and promote
 * 	check that the packages - there are 2 - not in the package list there
 * 	check that the errata (for package: bear) is also absent (TBD) 
 * </pre>
 * @author gkhachik
 */
public class PromoteWithFilters extends KatelloCliTestBase{
	protected static Logger log = Logger.getLogger(PromoteWithFilters.class.getName());

	public static final String ERRATA_ZOO_BEAR = "RHEA-2012:0001";
	public static final String PACKAGES_TO_BLACKLIST = "frog,bear";
	public static final String PACKAGES_TO_BLACKLIST_GREP = "frog|bear|lion"; // plus one package that should exist. Like: "lion"
	
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
		
		log.info("E2E - Create org/env");
		KatelloOrg org = new KatelloOrg(this.cli_worker, this.org, null);
		org.cli_create();
		KatelloEnvironment env = new KatelloEnvironment(this.cli_worker, this.env, null, this.org, KatelloEnvironment.LIBRARY);
		env.cli_create();
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
	
	@Test(description="Promote empty product/repo structure to Dev", dependsOnMethods={"test_prepareRepo"}, enabled=true)
	public void test_promoteToDevNoSync(){
		log.info("E2E - Promote to Dev - not synced");
		KatelloUtils.promoteProductToEnvironment(cli_worker, this.org, this.product, this.env);
	}

	@Test(description="Synchronize repository", dependsOnMethods={"test_promoteToDevNoSync"}, enabled=true)
	public void test_syncRepo(){
		log.info("E2E - Synchronize repo");
		KatelloRepo repo = new KatelloRepo(this.cli_worker, this.repo, this.org, this.product, REPO_INECAS_ZOO3, null, null);
		SSHCommandResult res = repo.synchronize();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo sync)");
	}
	
	@Test(description="Promote repo again: remove some packages", dependsOnMethods={"test_syncRepo"}, enabled=true)
	public void test_promoteToDeSynced(){
		log.info("E2E - Promote to Dev - synced on repo");
		KatelloUtils.promoteRepoToEnvironment(cli_worker, this.org, this.product, this.repo, this.env);

		KatelloUtils.removePackagesFromEnvironment(cli_worker, this.org, this.product, this.repo, new String[] {"bear", "frog"}, this.env);
	}
	
	@Test(description="Check packages: bear,frog - (absent); lion - (present)", dependsOnMethods={"test_promoteToDeSynced"}, enabled=true)
	public void test_packagesPromotedDev(){
		String cmd_packListDev = "package list --org \"%s\" --environment \"%s\" --product \"%s\" --repo \"%s\" | grep -E \"%s\" | wc -l";
		cmd_packListDev = String.format(cmd_packListDev, this.org, this.env, this.product, this.repo, PACKAGES_TO_BLACKLIST_GREP);
		SSHCommandResult res = new KatelloCli(cmd_packListDev, null,null,cli_worker.getClientHostname()).run();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (package list - Dev)");
		Assert.assertTrue(getOutput(res).equals("1"), "Check only one package should exist from grep: "+PACKAGES_TO_BLACKLIST_GREP);
	}
}
