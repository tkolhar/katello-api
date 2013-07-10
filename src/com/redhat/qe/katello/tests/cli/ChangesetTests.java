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
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.common.TngRunGroups;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups={"cfse-cli",TngRunGroups.TNG_KATELLO_Content})
public class ChangesetTests extends KatelloCliTestBase{

	protected static Logger log = Logger.getLogger(ChangesetTests.class.getName());
	
	private SSHCommandResult exec_result;
	
	private String org_name;
	private String env_name;
	private String chst_name;
	private String provider_name;
	private String provider_name2;
	private String product_name;
	private String product_name2;
	private String repo_name;
	
	@BeforeClass(description="init: create org stuff", alwaysRun=true)
	public void setUp() {
		
		String uid = KatelloUtils.getUniqueID();
		org_name = "org" + uid;
		env_name = "env"+uid;
		provider_name = "provider"+uid;
		provider_name2 = "provider2"+uid;
		product_name = "product"+uid;
		product_name2 = "product2"+uid;
		repo_name = "repo"+uid;
	}
	
	@Test(description="initialization here")
	public void init(){
		// Create org:
		KatelloOrg org = new KatelloOrg(this.cli_worker, this.org_name, "Package tests");
		exec_result = org.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		// Create provider:
		KatelloProvider prov = new KatelloProvider(this.cli_worker, provider_name, org_name, "Package provider", REPO_INECAS_ZOO3);
		exec_result = prov.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		KatelloProvider prov2 = new KatelloProvider(this.cli_worker, provider_name2, org_name, "Package provider2", null);
		exec_result = prov2.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		// Create product:
		KatelloProduct prod = new KatelloProduct(this.cli_worker, product_name, org_name, provider_name, null, null, null, null, null);
		exec_result = prod.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	
		KatelloProduct prod2 = new KatelloProduct(this.cli_worker, product_name2, org_name, provider_name2, null, null, null, null, null);
		exec_result = prod2.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
			
		KatelloRepo repo = new KatelloRepo(this.cli_worker, repo_name, org_name, product_name, REPO_INECAS_ZOO3, null, null);
		exec_result = repo.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		KatelloEnvironment env = new KatelloEnvironment(this.cli_worker, env_name, null, org_name, KatelloEnvironment.LIBRARY);
		exec_result = env.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (env create)");

		exec_result = repo.synchronize();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	}
	
	@Test(description = "Create changeset", dependsOnMethods={"init"})
	public void test_createChangeset() {
		KatelloChangeset chst = createChangeset();
		
		assert_changesetInfo(chst);
	}

	@Test(description = "Create changeset be existing name", dependsOnMethods={"init"})
	public void test_createChangesetExists() {
		KatelloChangeset chst = createChangeset();
		
		exec_result = chst.create();
		Assert.assertTrue(exec_result.getExitCode() == 166, "Check - return code (changeset create)");
		Assert.assertEquals(getOutput(exec_result).trim(), "Validation failed: Name Label has already been taken");
		
	}
	
	@Test(description = "Create 2 changesets, delete one of them", dependsOnMethods={"init"})
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
	
	@Test(description = "Create changeset and then list it", dependsOnMethods={"init"})
	public void test_listChangeset() {
		KatelloChangeset chst = createChangeset();
		
		String chst_name2= "changeset"+KatelloUtils.getUniqueID();
		KatelloChangeset cs = new KatelloChangeset(cli_worker, chst_name2, org_name, env_name);
		exec_result = cs.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (changeset create)");
		
		assert_changesetList(Arrays.asList(chst, cs), new ArrayList<KatelloChangeset>());
	}

	@Test(description = "Create changeset and then promote it", dependsOnMethods={"init"})
	public void test_promoteChangeset() {
		KatelloChangeset chst = createChangeset();
		
		assert_changesetInfo(chst);
		
		exec_result = chst.apply();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (changeset create)");
		chst.state = "promoted";
		assert_changesetInfo(chst);
	}
	
	@Test(description = "Create changeset, than update changeset name", dependsOnMethods={"init"})
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
	
	private KatelloChangeset createChangeset() {
		chst_name = "changeset"+KatelloUtils.getUniqueID();
		
		// create Changeset
		KatelloChangeset cs = new KatelloChangeset(cli_worker, chst_name, org_name, env_name);
		exec_result = cs.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (changeset create)");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloChangeset.OUT_CREATE, chst_name, env_name)), "Check - output string (changeset create)");
		
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

		exec_result = new KatelloChangeset(cli_worker, chst_name, org_name, env_name).list();

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
