package com.redhat.qe.katello.tests.cli;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloChangeset;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.common.TngRunGroups;
import com.redhat.qe.katello.base.obj.KatelloContentDefinition;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.base.KatelloCliDataProvider;


@Test(groups={"cfse-cli",TngRunGroups.TNG_KATELLO_Content})
public class ChangesetTests extends KatelloCliTestBase{

	protected static Logger log = Logger.getLogger(ChangesetTests.class.getName());
	
	private String chst_name;
	private String ch_name;
	private String def_name;
	private String view_name;
	private String repoenv_name;
	private String productenv_name;
	@BeforeClass(description="init: create org stuff", groups = {"cli-changeset"})
	public void setUp() {
		String uid = KatelloUtils.getUniqueID();
		ch_name = "changeset"+uid;
		def_name = "def"+uid;
		view_name = "view"+uid;
		productenv_name = "product-env"+uid;
		repoenv_name = "repo-env"+uid;

		KatelloChangeset chset = new KatelloChangeset(cli_worker, ch_name, base_org_name, base_dev_env_name);
		exec_result = chset.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		KatelloContentDefinition def = new  KatelloContentDefinition(cli_worker, def_name, null, base_org_name, null);
		exec_result = def.create();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (def create)");
		exec_result = def.publish(view_name, null, null);
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (def publish)");
		
		// create promoted product
		KatelloProduct prod = new KatelloProduct(cli_worker, productenv_name, base_org_name, base_zoo_provider_name, null, null, null, null, null);
		exec_result = prod.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		KatelloRepo repoenv = new KatelloRepo(cli_worker, repoenv_name, base_org_name, productenv_name, REPO_INECAS_ZOO3, null, null);
		exec_result = repoenv.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = repoenv.synchronize();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		chset = new KatelloChangeset(cli_worker, ch_name, base_org_name, base_dev_env_name);
		exec_result = chset.update_add_repo(repoenv_name, productenv_name);
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (chset add repo)");
		exec_result = chset.update_add_product(productenv_name);
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (chset add product)");
		exec_result = chset.apply();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	}

	@Test(description = "Create changeset")
	public void test_createChangeset() {
		KatelloChangeset chst = createChangeset();
		
		assert_changesetInfo(chst);
	}

	@Test(description="create changest - variations",
		dataProviderClass=KatelloCliDataProvider.class, dataProvider="changeset_create")
	public void test_createChangesetVar(String name, String description, Integer exit_code, String output) {
		KatelloChangeset chset = new KatelloChangeset(cli_worker, name, base_org_name, base_dev_env_name, description);
		exec_result = chset.create();
		Assert.assertTrue(exec_result.getExitCode()==exit_code.intValue(), "Check exit code (changeset create)");
		if(exit_code.intValue()==0) {
			Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloChangeset.OUT_CREATE, name, base_dev_env_name)), "Check output message");
		} else {
			Assert.assertTrue(getOutput(exec_result).contains(output), "Check output (changeset create)");
		}
	}

	@Test(description = "Create changeset be existing name")
	public void test_createChangesetExists() {
		KatelloChangeset chst = createChangeset();
		
		exec_result = chst.create();
		Assert.assertTrue(exec_result.getExitCode() == 166, "Check - return code (changeset create)");
		Assert.assertEquals(getOutput(exec_result).trim(), "Validation failed: Name Label has already been taken");
	}
	
	@Test(description = "Create 2 changesets, delete one of them")
	public void test_deleteChangeset() {
		KatelloChangeset chst = createChangeset();
		KatelloChangeset chst2 = createChangeset();
		
		final String chstId = assert_changesetInfo(chst);
		assert_changesetInfo(chst2);
		
		exec_result = chst.delete();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (changeset delete)");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloChangeset.OUT_DELETE, chstId)), "Check - output string (changeset delete)");
		
		assert_changesetList(Arrays.asList(chst2), Arrays.asList(chst));
		
		exec_result = chst.delete();
		Assert.assertTrue(exec_result.getExitCode() == 65, "Check - return code (changeset delete)");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloChangeset.ERR_NOT_FOUND, chst.name, chst.environment)), "Check - output string (changeset not exists)");
	}
	
	@Test(description = "Create changeset and then list it")
	public void test_listChangeset() {
		KatelloChangeset chst = createChangeset();
		
		String chst_name2= "changeset"+KatelloUtils.getUniqueID();
		KatelloChangeset cs = new KatelloChangeset(cli_worker, chst_name2, base_org_name, base_dev_env_name);
		exec_result = cs.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (changeset create)");
		
		assert_changesetList(Arrays.asList(chst, cs), new ArrayList<KatelloChangeset>());
	}

	@Test(description = "Create changeset and then promote it")
	public void test_promoteChangeset() {
		KatelloChangeset chst = createChangeset();
		
		assert_changesetInfo(chst);
		
		exec_result = chst.apply();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (changeset create)");
		chst.state = "promoted";
		assert_changesetInfo(chst);
	}
	
	@Test(description = "Create changeset, than update changeset name")
	public void test_updateChangesetName() {
		KatelloChangeset chst = createChangeset();
		
		String oldName = chst.name;
		String newName = chst.name + "new";
		
		exec_result = chst.update_name(newName);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		chst.name = newName;
		assert_changesetInfo(chst);
		
		chst.name = oldName;
		exec_result = chst.info();
		Assert.assertTrue(exec_result.getExitCode() == 65, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), String.format(KatelloChangeset.ERR_NOT_FOUND, chst.name, chst.environment));
	}

	@Test(description="add content view to changeset")
	public void test_addContentView() {
		KatelloChangeset chset = new KatelloChangeset(cli_worker, ch_name, base_org_name, base_dev_env_name);
		exec_result = chset.update_addView(view_name);
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (chset add view)");
		exec_result = chset.info();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (chset info)");
		Assert.assertTrue(getOutput(exec_result).contains(view_name), "Check output");
	}

	@Test(description="add erratum to changeset")
	public void test_addErratum() {
		KatelloChangeset chset = new KatelloChangeset(cli_worker, ch_name, base_org_name, base_dev_env_name);
		String err_id = "RHEA-2012:0002";
		exec_result = chset.update_add_erratum(err_id, productenv_name);
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (chset add erratum)");
		exec_result = chset.info();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (chset info)");
		Assert.assertTrue(getOutput(exec_result).contains(err_id), "Check output");
	}

	@Test(description="add package to changeset")
	public void test_addPackage() {
		KatelloChangeset chset = new KatelloChangeset(cli_worker, ch_name, base_org_name, base_dev_env_name);;
		String pack_name = "whale";
		exec_result = chset.update_add_package(pack_name, productenv_name);
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (chset add package)");
		exec_result = chset.info();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (chset info)");
		Assert.assertTrue(getOutput(exec_result).contains(pack_name), "Check output");
	}

	@Test(description="add product to changeset")
	public void test_addProduct() {
		KatelloChangeset chset = new KatelloChangeset(cli_worker, ch_name, base_org_name, base_dev_env_name);
		exec_result = chset.update_add_product(base_zoo_product_name);
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (chset add product)");
		exec_result = chset.info();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (chset info)");
		Assert.assertTrue(getOutput(exec_result).contains(base_zoo_product_name), "Check output");
	}

	@Test(description="add repo to changeset")
	public void test_addRepo() {
		KatelloChangeset chset = new KatelloChangeset(cli_worker, ch_name, base_org_name, base_dev_env_name);
		exec_result = chset.update_add_repo(base_zoo_repo_name, base_zoo_product_name);
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (chset add repo)");
		exec_result = chset.info();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (chset info)");
		Assert.assertTrue(getOutput(exec_result).contains(base_zoo_repo_name), "Check output");
	}

	@Test(description="update changeset description")
	public void test_updateDescription() {
		KatelloChangeset chset = new KatelloChangeset(cli_worker, ch_name, base_org_name, base_dev_env_name);
		String new_descr = "new description";
		exec_result = chset.update_description(new_descr);
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (changeset update)");
		exec_result = chset.info();
		Assert.assertTrue(getOutput(exec_result).contains(new_descr), "Check output (changeset update)");
	}

	@Test(description="remove content view from changeset", dependsOnMethods={"test_addContentView"})
	public void test_removeContentView() {
		KatelloChangeset chset = new KatelloChangeset(cli_worker, ch_name, base_org_name, base_dev_env_name);
		exec_result = chset.update_removeView(view_name);
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (chset add view)");
		exec_result = chset.info();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (chset info)");
		Assert.assertFalse(getOutput(exec_result).contains(view_name), "Check output");
	}

	@Test(description="remove erratum from changeset", dependsOnMethods={"test_addErratum"})
	public void test_removeErratum() {
		KatelloChangeset chset = new KatelloChangeset(cli_worker, ch_name, base_org_name, base_dev_env_name);
		String err_id = "RHEA-2012:0002";
		exec_result = chset.update_remove_erratum(err_id, productenv_name);
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (chset add erratum)");
		exec_result = chset.info();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (chset info)");
		Assert.assertFalse(getOutput(exec_result).contains(err_id), "Check output");
	}

	@Test(description="remove package from changeset", dependsOnMethods={"test_addPackage"})
	public void test_removePackage() {
		KatelloChangeset chset = new KatelloChangeset(cli_worker, ch_name, base_org_name, base_dev_env_name);;
		String pack_name = "whale-0.2-1.noarch";
		exec_result = chset.update_remove_package(pack_name, productenv_name);
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (chset remove package)");
		exec_result = chset.info();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (chset info)");
		Assert.assertFalse(getOutput(exec_result).contains(pack_name), "Check output");
	}

	@Test(description="remove product from changeset", dependsOnMethods={"test_addProduct"})
	public void test_removeProduct() {
		KatelloChangeset chset = new KatelloChangeset(cli_worker, ch_name, base_org_name, base_dev_env_name);
		exec_result = chset.update_remove_product(base_zoo_product_name);
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (chset remove product)");
		exec_result = chset.info();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (chset info)");
		Assert.assertFalse(getOutput(exec_result).contains(base_zoo_product_name), "Check output");
	}

	@Test(description="remove repo from changeset", dependsOnMethods={"test_addRepo"})
	public void test_removeRepo() {
		KatelloChangeset chset = new KatelloChangeset(cli_worker, ch_name, base_org_name, base_dev_env_name);
		exec_result = chset.update_remove_repo(base_zoo_repo_name, base_zoo_product_name);
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (chset remove repo)");
		exec_result = chset.info();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (chset info)");
		Assert.assertFalse(getOutput(exec_result).contains(base_zoo_repo_name), "Check output");
	}
	
	private KatelloChangeset createChangeset() {
		chst_name = "changeset"+KatelloUtils.getUniqueID();
		
		// create Changeset
		KatelloChangeset cs = new KatelloChangeset(cli_worker, chst_name, base_org_name, base_dev_env_name);
		exec_result = cs.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (changeset create)");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloChangeset.OUT_CREATE, chst_name, base_dev_env_name)), "Check - output string (changeset create)");
		
		return cs;
	}
	
	private String assert_changesetInfo(KatelloChangeset chst) {
		if (chst.description == null) chst.description = "None";
		if (chst.state == null) chst.state = "new";

		exec_result = chst.info();

		String match_info = String.format(KatelloChangeset.REG_CHST_INFO, chst.name, chst.description, chst.state, chst.environment).replaceAll("\"", "");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		log.finest(String.format("Changeset (info) match regex: [%s]", match_info));
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", " ").matches(match_info), String.format("Changeset [%s] should be found in the result info", chst.name));

		return KatelloUtils.grepCLIOutput("ID", getOutput(exec_result));
	}
		
	private void assert_changesetList(List<KatelloChangeset> chsts, List<KatelloChangeset> excludeChsts) {
		exec_result = new KatelloChangeset(cli_worker, chst_name, base_org_name, base_dev_env_name).list();

		//changesets that exist in list
		for(KatelloChangeset chst : chsts) {			
			String match_info = String.format(KatelloChangeset.REG_CHST_LIST, chst.name, chst.environment).replaceAll("\"", "");
			Assert.assertTrue(getOutput(exec_result).replaceAll("\n", " ").matches(match_info), String.format("Changeset [%s] should be found in the result list", chst.name));
		}
		
		//changesets that should not exist in list
		for(KatelloChangeset chst : excludeChsts) {			
			String match_info = String.format(KatelloChangeset.REG_CHST_LIST, chst.name, chst.environment).replaceAll("\"", "");
			Assert.assertFalse(getOutput(exec_result).replaceAll("\n", " ").matches(match_info), String.format("Changeset [%s] should be found in the result list", chst.name));
		}
	}
}
