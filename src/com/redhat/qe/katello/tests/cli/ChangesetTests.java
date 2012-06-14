package com.redhat.qe.katello.tests.cli;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.auto.testng.Assert;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.katello.base.obj.KatelloChangeset;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.base.obj.KatelloTemplate;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups={"cfse-cli"})
public class ChangesetTests extends KatelloCliTestScript{

	protected static Logger log = Logger.getLogger(ChangesetTests.class.getName());
	
	private SSHCommandResult exec_result;
	
	private String org_name;
	private String env_name;
	private String chst_name;
	private String provider_name;
	private String product_name;
	private String repo_name;
	private String templ_name;
	
	@BeforeClass(description="init: create org stuff", groups = {"cli-changeset"})
	public void setUp() {
		
		String uid = KatelloTestScript.getUniqueID();
		org_name = "org" + uid;
		env_name = "env"+uid;
		provider_name = "provider"+uid;
		product_name = "product"+uid;
		repo_name = "repo"+uid;
		templ_name = "template"+KatelloTestScript.getUniqueID();
		
		// Create org:
		KatelloOrg org = new KatelloOrg(this.org_name, "Package tests");
		exec_result = org.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		// Create provider:
		KatelloProvider prov = new KatelloProvider(provider_name, org_name, "Package provider", REPO_INECAS_ZOO3);
		exec_result = prov.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		// Create product:
		KatelloProduct prod = new KatelloProduct(product_name, org_name, provider_name, null, null, null, null, null);
		exec_result = prod.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	
		KatelloRepo repo = new KatelloRepo(repo_name, org_name, product_name, REPO_INECAS_ZOO3, null, null);
		exec_result = repo.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		KatelloEnvironment env = new KatelloEnvironment(env_name, null, org_name, KatelloEnvironment.LIBRARY);
		exec_result = env.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (env create)");
		
		KatelloTemplate tpl = new KatelloTemplate(templ_name, null, org_name, null);
		exec_result = tpl.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloTemplate.OUT_CREATE, templ_name)), "Check - output string (template create)");

	}
	
	@Test(description = "Create changeset", groups = { "cli-changeset" })
	public void test_createChangeset() {
		KatelloChangeset chst = createChangeset();
		
		assert_changesetInfo(chst);
	}

	@Test(description = "Create changeset be existing name", groups = { "cli-changeset" })
	public void test_createChangesetExists() {
		KatelloChangeset chst = createChangeset();
		
		exec_result = chst.create();
		Assert.assertTrue(exec_result.getExitCode() == 144, "Check - return code (changeset create)");
		Assert.assertEquals(getOutput(exec_result).trim(), "Validation failed: Name Must be unique within an environment");
		
	}
	
	@Test(description = "Create 2 changesets, delete one of them", groups = { "cli-changeset" })
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
	
	@Test(description = "Create changeset and then list it", groups = { "cli-changeset" })
	public void test_listChangeset() {
		KatelloChangeset chst = createChangeset();
		
		String chst_name2= "changeset"+KatelloTestScript.getUniqueID();
		KatelloChangeset cs = new KatelloChangeset(chst_name2, org_name, env_name);
		exec_result = cs.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (changeset create)");
		
		assert_changesetList(Arrays.asList(chst, cs), new ArrayList<KatelloChangeset>());
	}

	@Test(description = "Create changeset and then promote it", groups = { "cli-changeset" })
	public void test_promoteChangeset() {
		KatelloChangeset chst = createChangeset();
		
		assert_changesetInfo(chst);
		
		exec_result = chst.promote();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (changeset create)");
		chst.state = "promoted";
		assert_changesetInfo(chst);
	}
	
	@Test(description = "Create changeset, than update changeset name", groups = { "cli-changeset" })
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
	
	@Test(description = "Create changeset, than add product to changeset", groups = { "cli-changeset" })
	public void test_updateChangesetAddProduct() {
		KatelloChangeset chst = createChangeset();
		
		exec_result = chst.update_addProduct(product_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloChangeset.OUT_UPDATE, chst_name)), "Check - output string (changeset update)");
		
		exec_result = chst.info();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		String match_info = String.format(KatelloChangeset.REG_CHST_PRODUCTS, product_name).replaceAll("\"", "");
		Pattern pattern = Pattern.compile(match_info);
		Matcher matcher = pattern.matcher(getOutput(exec_result).replaceAll("\n", " "));
		Assert.assertTrue(matcher.find(), "Check - Product should exist in changeset info");
	}
	
	@Test(description = "Create changeset, than add repo to changeset", groups = { "cli-changeset" })
	public void test_updateChangesetAddRepo() {
		KatelloChangeset chst = createChangeset();
		
		exec_result = chst.update_fromProduct_addRepo(product_name, repo_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloChangeset.OUT_UPDATE, chst_name)), "Check - output string (changeset update)");
		
		exec_result = chst.info();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		String match_info = String.format(KatelloChangeset.REG_CHST_REPOS, repo_name).replaceAll("\"", "");
		Pattern pattern = Pattern.compile(match_info);
		Matcher matcher = pattern.matcher(getOutput(exec_result).replaceAll("\n", " "));
		Assert.assertTrue(matcher.find(), "Check - Repository should exist in changeset info");
	}

	@Test(description = "Create changeset, than add template to changeset", groups = { "cli-changeset" })
	public void test_updateChangesetAddTemplate() {
		KatelloChangeset chst = createChangeset();
		
		exec_result = chst.update_addTemplate(templ_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloChangeset.OUT_UPDATE, chst_name)), "Check - output string (changeset update)");
		
		exec_result = chst.info();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		String match_info = String.format(KatelloChangeset.REG_CHST_TEMPLS, templ_name).replaceAll("\"", "");
		Pattern pattern = Pattern.compile(match_info);
		Matcher matcher = pattern.matcher(getOutput(exec_result).replaceAll("\n", " "));
		Assert.assertTrue(matcher.find(), "Check - Template should exist in changeset info");
	}
	
	private KatelloChangeset createChangeset() {
		chst_name = "changeset"+KatelloTestScript.getUniqueID();
		
		// create Changeset
		KatelloChangeset cs = new KatelloChangeset(chst_name, org_name, env_name);
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

		Pattern pattern = Pattern.compile(KatelloChangeset.REG_CHST_ID);
		Matcher matcher = pattern.matcher(getOutput(exec_result).replaceAll("\n", " "));
		Assert.assertTrue(matcher.find(), "Check - Id should exist in changeset info");
		String id = matcher.group();
		id = id.replace("Id:", "").replace("Name:", "").trim();
		
		return id;
	}
		
	private void assert_changesetList(List<KatelloChangeset> chsts, List<KatelloChangeset> excludeChsts) {

		exec_result = new KatelloChangeset(chst_name, org_name, env_name).list();

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
