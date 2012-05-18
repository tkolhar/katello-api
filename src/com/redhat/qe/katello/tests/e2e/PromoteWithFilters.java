package com.redhat.qe.katello.tests.e2e;

import java.util.logging.Logger;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import com.redhat.qe.auto.testng.Assert;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.katello.base.obj.KatelloChangeset;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloFilter;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.tools.SSHCommandResult;

/**
 * Implementation of following scenario:<BR>
 * Users decides to filter out some package(s) (one of them also has an errata in the repo) and promote to the next env.<BR>
 * Process looks like:<BR>
 * <pre>
 * 	promote product (without sync)
 * 	sync the repo
 * 	apply filter to the repo
 *	create another CS
 * 	add repo to changeset (with --from_product option - it promoted before)
 * 	promote the changeset
 * 	check that the packages - there are 2 - not in the package list there
 * 	check that the errata (for package: bear) is also absent (TBD) 
 * </pre>
 * @author gkhachik
 */
@Test(groups={"cfse-e2e"})
public class PromoteWithFilters extends KatelloCliTestScript{
	protected static Logger log = Logger.getLogger(PromoteWithFilters.class.getName());

	public static final String ERRATA_ZOO_BEAR = "RHEA-2012:0001";
	public static final String PACKAGES_TO_BLACKLIST = "frog,bear";
	public static final String PACKAGES_TO_BLACKLIST_GREP = "frog|bear|lion"; // plus one package that should exist. Like: "lion"
	
	private String org;
	private String env = "Dev";
	private String provider;
	private String product;
	private String repo;
	private String cs1;
	private String cs2;
	private String filter;
	
	@BeforeTest(description="Init unique names", alwaysRun=true)
	public void setUp(){
		String uniqueID = KatelloTestScript.getUniqueID();
		this.org = "Zoo Corporation "+uniqueID;
		this.provider = "ZooProv"+uniqueID;
		this.product = "ZooProd"+uniqueID;
		this.repo = "ZooRepo"+uniqueID;
		this.cs1 = "cs1-"+uniqueID;
		this.cs2 = "cs2-"+uniqueID;
		this.filter = "noBearFrog"+uniqueID;
		
		log.info("E2E - Create org/env");
		KatelloOrg org = new KatelloOrg(this.org, null);
		org.cli_create();
		KatelloEnvironment env = new KatelloEnvironment(this.env, null, this.org, KatelloEnvironment.LIBRARY);
		env.create();
	}
	
	@Test(description="Create org, provider, product and repo", enabled=true)
	public void test_prepareRepo(){
		log.info("E2E - Create provider/product/repo");
		KatelloProvider prov = new KatelloProvider(this.provider,this.org, null, null);
		prov.create(); // create provider
		KatelloProduct prod = new KatelloProduct(this.product, this.org, this.provider, null, null, null, null, null);
		prod.create(); // create product
		KatelloRepo repo = new KatelloRepo(this.repo, this.org, this.product, PackagesWithGPGKey.REPO_INECAS_ZOO3, null, null);
		repo.create(); // create repo
	}
	
	@Test(description="Promote empty product/repo structure to Dev", dependsOnMethods={"test_prepareRepo"}, enabled=true)
	public void test_promoteToDevNoSync(){
		log.info("E2E - Promote to Dev - not synced");
		KatelloChangeset cs = new KatelloChangeset(this.cs1, this.org, this.env);
		cs.create();
		cs.update_addProduct(this.product);
		cs.promote();
	}

	@Test(description="Synchronize repository", dependsOnMethods={"test_promoteToDevNoSync"}, enabled=true)
	public void test_syncRepo(){
		log.info("E2E - Synchronize repo");
		KatelloRepo repo = new KatelloRepo(this.repo, this.org, this.product, REPO_INECAS_ZOO3, null, null);
		SSHCommandResult res = repo.synchronize();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo sync)");
	}
	
	@Test(description="Create filter and apply to repo. Important to have repo synced before (seems)", dependsOnMethods={"test_syncRepo"}, enabled=true)
	public void test_createApplyFilter(){
		
		log.info("E2E - create filter and apply to the repo");
		KatelloFilter filter = new KatelloFilter(this.filter, this.org, this.env, PACKAGES_TO_BLACKLIST);
		SSHCommandResult res = filter.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (filter create)");
		KatelloRepo repo = new KatelloRepo(this.repo, this.org, this.product, REPO_INECAS_ZOO3, null, null);
		repo.add_filter(this.filter);
	}
	
	@Test(description="Promote repo again: with filter now", dependsOnMethods={"test_createApplyFilter"}, enabled=true)
	public void test_promoteToDeSyncedAndFiltered(){
		log.info("E2E - Promote to Dev - synced and with filter on repo");
		KatelloChangeset cs = new KatelloChangeset(this.cs2, this.org, this.env);
		cs.create();
		cs.update_fromProduct_addRepo(this.product, this.repo);
		SSHCommandResult res = cs.promote();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (changeset promote - with filter & synced)");
	}
	
	@Test(description="Check packages: bear,frog - (absent); lion - (present)", dependsOnMethods={"test_promoteToDeSyncedAndFiltered"}, enabled=true)
	public void test_packagesPromotedDev(){
		String cmd_packListDev = "package list --org \"%s\" --environment \"%s\" --product \"%s\" --repo \"%s\" | grep -E \"%s\" | wc -l";
		cmd_packListDev = String.format(cmd_packListDev, this.org, this.env, this.product, this.repo, PACKAGES_TO_BLACKLIST_GREP);
		SSHCommandResult res = new KatelloCli(cmd_packListDev, null).run();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (package list - Dev)");
		Assert.assertTrue(getOutput(res).equals("1"), "Check only one package should exist from grep: "+PACKAGES_TO_BLACKLIST_GREP);
	}
}
