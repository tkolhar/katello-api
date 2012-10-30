package com.redhat.qe.katello.tests.cli;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.obj.KatelloChangeset;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.base.obj.KatelloTemplate;
import com.redhat.qe.katello.base.obj.KatelloUser;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups = { "cfse-cli" })
public class TemplateTests extends KatelloCliTestScript {

	protected static Logger log = Logger.getLogger(TemplateTests.class.getName());
	
	private SSHCommandResult exec_result;

	private String templ_name;
	private String org_name;
	private String user_name;
	private String provider_name;
	private String product_name;
	private String repo_name;

	@BeforeClass(description = "Generate unique objects")
	public void setUp() {
		String uid = KatelloUtils.getUniqueID();
		org_name = "org" + uid;
		user_name = "user"+uid;
		provider_name = "provider"+uid;
		product_name = "product"+uid;
		repo_name = "repo"+uid;
		
		// Create org:
		KatelloOrg org = new KatelloOrg(this.org_name, "Package tests");
		exec_result = org.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");		
		
		// Create user:
		KatelloUser user = new KatelloUser(user_name, "root@localhost", KatelloUser.DEFAULT_USER_PASS, false);
		exec_result = user.cli_create();
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
		
		prov.synchronize();

	}
	
	@Test(description = "Create template", groups = { "cli-template" })
	public void test_createTemplate() {
		KatelloTemplate templ = createTemplate();
		
		assert_templInfo(templ);
	}
	
	@Test(description = "Create template and then list it", groups = { "cli-template" })
	public void test_listTemplate() {
		KatelloTemplate templ = createTemplate();
		
		String templ_name2= "template"+KatelloUtils.getUniqueID();
		KatelloTemplate tpl = new KatelloTemplate(templ_name2, null, org_name, null);
		exec_result = tpl.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		assert_templList(Arrays.asList(templ, tpl), new ArrayList<KatelloTemplate>());
	}
	
	@Test(description = "Create template, than update template name", groups = { "cli-template" })
	public void test_updateTemplateName() {
		KatelloTemplate templ = createTemplate();
		
		String oldName = templ.name;
		String newName = templ.name + "new";
		
		exec_result = templ.update_name(newName);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		templ.name = newName;
		assert_templInfo(templ);
		
		templ.name = oldName;
		exec_result = templ.info(null);
		Assert.assertTrue(exec_result.getExitCode() == 65, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), String.format(KatelloTemplate.ERR_TEMPL_NOTFOUND, templ.name, "Library"));
	}
	
	@Test(description = "Create template and add subtemplate to it", groups = { "cli-template" })
	public void test_createSubTemplate() {
		KatelloTemplate templ = createTemplate();
		
		String id = assert_templInfo(templ);
		
		String templ_name2= "template"+KatelloUtils.getUniqueID();
		KatelloTemplate tpl = new KatelloTemplate(templ_name2, "child template", org_name, templ_name);
		exec_result = tpl.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		tpl.parentId = id;
		
		assert_templInfo(tpl);
		assert_templList(Arrays.asList(templ, tpl), new ArrayList<KatelloTemplate>());
	}
	
	@Test(description = "Create template and then delete it", groups = { "cli-template" })
	public void test_deleteTemplate() {
		KatelloTemplate templ = createTemplate();
		
		String templ_name2= "template"+KatelloUtils.getUniqueID();
		KatelloTemplate tpl = new KatelloTemplate(templ_name2, null, org_name, null);
		exec_result = tpl.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = templ.delete();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = templ.info(null);
		Assert.assertTrue(exec_result.getExitCode() == 65, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), String.format(KatelloTemplate.ERR_TEMPL_NOTFOUND, templ.name, "Library"));
		
		assert_templList(Arrays.asList(tpl), Arrays.asList(templ));
	}
	
	@Test(description = "Create template and add subtemplate to it and try to delete parent template, verify error", groups = { "cli-template" })
	public void test_deleteParentTemplate() {
		KatelloTemplate templ = createTemplate();
		
		String templ_name2= "template"+KatelloUtils.getUniqueID();
		KatelloTemplate tpl = new KatelloTemplate(templ_name2, "child template", org_name, templ_name);
		exec_result = tpl.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = templ.delete();
		Assert.assertTrue(exec_result.getExitCode() == 244, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), "The template has children templates.");
	}
	
	@Test(description = "Create template which already exists, verify error", groups = { "cli-template" })
	public void test_createTemplateExists() {
		KatelloTemplate templ = createTemplate();
		
		exec_result = templ.create();
		Assert.assertTrue(exec_result.getExitCode() == 144, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), "Validation failed: Name has already been taken");
	}
	
	@Test(description = "Create template, than add parameter to template", groups = { "cli-template" })
	public void test_updateTemplateAddParam() {
		KatelloTemplate templ = createTemplate();
		
		String paramName = "testparam" + KatelloUtils.getUniqueID();
		final String paramValue = "testparamval";
		
		exec_result = templ.update_add_param(paramName, paramValue);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloTemplate.OUT_UPDATE, templ_name)), "Check - output string (template update)");
		
		exec_result = templ.info(null);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		String match_info = String.format(KatelloTemplate.REG_TEMPL_PARAMS, paramName, paramValue).replaceAll("\"", "");
		Pattern pattern = Pattern.compile(match_info);
		Matcher matcher = pattern.matcher(getOutput(exec_result).replaceAll("\n", " "));
		Assert.assertTrue(matcher.find(), "Check - Parameter should exist in template info");
	}

	@Test(description = "Create template, than add parameter to template and then remove it",  groups = { "cli-template" })
	public void test_updateTemplateRemoveParam() {
		
		KatelloTemplate templ = createTemplate();
		
		String paramName = "testparam" + KatelloUtils.getUniqueID();
		final String paramValue = "testparamval";
		
		exec_result = templ.update_add_param(paramName, paramValue);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloTemplate.OUT_UPDATE, templ_name)), "Check - output string (template update)");
		
		final String paramName2 = "testparam2" + KatelloUtils.getUniqueID();
		final String paramValue2 = "testparamval2";

		exec_result = templ.update_add_param(paramName2, paramValue2);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloTemplate.OUT_UPDATE, templ_name)), "Check - output string (template update)");
		
		exec_result = templ.update_remove_param(paramName2);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloTemplate.OUT_UPDATE, templ_name)), "Check - output string (template update)");
		
		exec_result = templ.info(null);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		String match_info = String.format(KatelloTemplate.REG_TEMPL_PARAMS, paramName2, paramValue2).replaceAll("\"", "");
		Pattern pattern = Pattern.compile(match_info);
		Matcher matcher = pattern.matcher(getOutput(exec_result).replaceAll("\n", " "));
		Assert.assertFalse(matcher.find(), "Check - Parameter should not exist in template info");
	}
	
	@Test(description = "Create template, than add package to template", groups = { "cli-template" })
	public void test_updateTemplateAddPackage() {
		KatelloTemplate templ = createTemplate();
		
		String packageName = "lion";
		
		exec_result = templ.update_add_package(packageName);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloTemplate.OUT_UPDATE, templ_name)), "Check - output string (template update)");
		
		exec_result = templ.info(null);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		String match_info = String.format(KatelloTemplate.REG_TEMPL_PACKAGES, packageName).replaceAll("\"", "");
		Pattern pattern = Pattern.compile(match_info);
		Matcher matcher = pattern.matcher(getOutput(exec_result).replaceAll("\n", " "));
		Assert.assertTrue(matcher.find(), "Check - Package should exist in template info");
	}

	@Test(description = "Create template, than add package to template and then remove it", groups = { "cli-template" })
	public void test_updateTemplateRemovePackage() {
		KatelloTemplate templ = createTemplate();

		String packageName = "lion";

		exec_result = templ.update_add_package(packageName);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloTemplate.OUT_UPDATE, templ_name)), "Check - output string (template update)");
		
		String packageName2 = "zebra";
		
		exec_result = templ.update_add_package(packageName2);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloTemplate.OUT_UPDATE, templ_name)), "Check - output string (template update)");
		
		exec_result = templ.update_remove_package(packageName);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloTemplate.OUT_UPDATE, templ_name)), "Check - output string (template update)");
		
		exec_result = templ.info(null);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		String match_info = String.format(KatelloTemplate.REG_TEMPL_PACKAGES, packageName).replaceAll("\"", "");
		Pattern pattern = Pattern.compile(match_info);
		Matcher matcher = pattern.matcher(getOutput(exec_result).replaceAll("\n", " "));
		Assert.assertFalse(matcher.find(), "Check - Package should not exist in template info");
		
		match_info = String.format(KatelloTemplate.REG_TEMPL_PACKAGES, packageName2).replaceAll("\"", "");
		pattern = Pattern.compile(match_info);
		matcher = pattern.matcher(getOutput(exec_result).replaceAll("\n", " "));
		Assert.assertTrue(matcher.find(), "Check - Package should exist in template info");
	}
	
	@Test(description = "Create template, than add package group to template", groups = { "cli-template" })
	public void test_updateTemplateAddPackageGroup() {
		KatelloTemplate templ = createTemplate();
		
		String packageGroupName = "birds";
		
		exec_result = templ.update_add_package_group(packageGroupName);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloTemplate.OUT_UPDATE, templ_name)), "Check - output string (template update)");
		
		exec_result = templ.info(null);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		String match_info = String.format(KatelloTemplate.REG_TEMPL_PACKAGEGROUPS, packageGroupName).replaceAll("\"", "");
		Pattern pattern = Pattern.compile(match_info);
		Matcher matcher = pattern.matcher(getOutput(exec_result).replaceAll("\n", " "));
		Assert.assertTrue(matcher.find(), "Check - Package shoul Groupd exist in template info");
	}

	@Test(description = "Create template, than add package group to template and then remove it", groups = { "cli-template" })
	public void test_updateTemplateRemovePackageGroup() {
		KatelloTemplate templ = createTemplate();
		
		String packageGroupName = "birds";
		
		exec_result = templ.update_add_package_group(packageGroupName);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloTemplate.OUT_UPDATE, templ_name)), "Check - output string (template update)");
		
		String packageGroupName2 = "mammals";

		exec_result = templ.update_add_package_group(packageGroupName2);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloTemplate.OUT_UPDATE, templ_name)), "Check - output string (template update)");
		
		exec_result = templ.update_remove_package_group(packageGroupName);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloTemplate.OUT_UPDATE, templ_name)), "Check - output string (template update)");
		
		exec_result = templ.info(null);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		String match_info = String.format(KatelloTemplate.REG_TEMPL_PACKAGEGROUPS, packageGroupName).replaceAll("\"", "");
		Pattern pattern = Pattern.compile(match_info);
		Matcher matcher = pattern.matcher(getOutput(exec_result).replaceAll("\n", " "));
		Assert.assertFalse(matcher.find(), "Check - Package Group should not exist in template info");
		
		match_info = String.format(KatelloTemplate.REG_TEMPL_PACKAGEGROUPS, packageGroupName2).replaceAll("\"", "");
		pattern = Pattern.compile(match_info);
		matcher = pattern.matcher(getOutput(exec_result).replaceAll("\n", " "));
		Assert.assertTrue(matcher.find(), "Check - Package Group should exist in template info");
	}

	@Test(description = "Create template, than add repository to template", groups = { "cli-template" })
	public void test_updateTemplateAddRepo() {
		KatelloTemplate templ = createTemplate();
			
		exec_result = templ.update_add_repo(product_name, repo_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloTemplate.OUT_UPDATE, templ_name)), "Check - output string (template update)");
		
		exec_result = templ.info(null);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		String match_info = String.format(KatelloTemplate.REG_TEMPL_REPOS, repo_name).replaceAll("\"", "");
		Pattern pattern = Pattern.compile(match_info);
		Matcher matcher = pattern.matcher(getOutput(exec_result).replaceAll("\n", " "));
		Assert.assertTrue(matcher.find(), "Check - Repository should exist in template info");
	}

	@Test(description = "Create template, than add repository to template and then remove it",  groups = { "cli-template" })
	public void test_updateTemplateRemoveRepo() {
		
		KatelloTemplate templ = createTemplate();
		
		exec_result = templ.update_add_repo(product_name, repo_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloTemplate.OUT_UPDATE, templ_name)), "Check - output string (template update)");
		
		exec_result = templ.update_remove_repo(product_name, repo_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloTemplate.OUT_UPDATE, templ_name)), "Check - output string (template update)");
		
		exec_result = templ.info(null);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		String match_info = String.format(KatelloTemplate.REG_TEMPL_REPOS, repo_name).replaceAll("\"", "");
		Pattern pattern = Pattern.compile(match_info);
		Matcher matcher = pattern.matcher(getOutput(exec_result).replaceAll("\n", " "));
		Assert.assertFalse(matcher.find(), "Check - Repository should not exist in template info");
	}
	
	/**
	 * @TODO write test case to update template and add package group category.
	 * 
	 */

	private String templateExport_name = "template-export-"+KatelloUtils.getUniqueID();
	private String templateExport_env_name = "testing";
	@Test(description="export template - init all")
	public void test_exportTemplatePreparation(){
		KatelloTemplate template = new KatelloTemplate(templateExport_name, null, org_name, null);
		exec_result = template.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (template create)");
		KatelloEnvironment env = new KatelloEnvironment(templateExport_env_name, null, org_name, KatelloEnvironment.LIBRARY);
		exec_result = env.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (environment create)");
		KatelloChangeset cs = new KatelloChangeset("cs-"+templateExport_name, org_name, env.getName());
		exec_result = cs.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (changeset create)");
		exec_result = template.update_add_param("hostname", "localhost");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (template update --add_param)");
		exec_result = cs.update_addTemplate(templateExport_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (changeset update --add_template)");
		exec_result = cs.promote();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (changeset promote)");
		exec_result = template.export(env.getName(), "/tmp/"+templateExport_name+".json", "json");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (template export)");
		exec_result = KatelloUtils.sshOnClient("ls \""+"/tmp/"+templateExport_name+".json\"");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - file exists");
	}
	
	@Test(description="template export --format json", dependsOnMethods={"test_exportTemplatePreparation"})
	public void test_exportTemplateJson(){
		KatelloTemplate template = new KatelloTemplate(templateExport_name, null, org_name, null);
		exec_result = template.export(templateExport_env_name, "/tmp/"+templateExport_name+".json", "json");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (template export --format json)");
		exec_result = KatelloUtils.sshOnClient("ls \""+"/tmp/"+templateExport_name+".json\"");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - file exists");
	}
	
	@Test(description="template export --format tdl", dependsOnMethods={"test_exportTemplatePreparation"})
	public void test_exportTemplateTdlErr(){
		KatelloTemplate template = new KatelloTemplate(templateExport_name, null, org_name, null);
		exec_result = template.export(templateExport_env_name, "/tmp/"+templateExport_name+".tdl", "tdl");
		Assert.assertTrue(exec_result.getExitCode() == 244, "Check - return code (template export --format tdl)");
		Assert.assertTrue(getOutput(exec_result).equals(KatelloTemplate.ERR_TDL_EXPORT_IMPOSSIBLE), 
				"Check - stderr (export not possible)");
	}

	private String assert_templInfo(KatelloTemplate templ) {
		if (templ.description == null) templ.description = "None";
		if (templ.parentId == null) templ.parentId = "None";
		if (templ.revision == null) templ.revision = "1";

		exec_result = templ.info(null);

		String match_info = String.format(KatelloTemplate.REG_TEMPL_INFO, templ.name, templ.revision, templ.description, templ.parentId).replaceAll("\"", "");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		log.finest(String.format("Template (info) match regex: [%s]", match_info));
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", " ").matches(match_info), String.format("Template [%s] should be found in the result info", templ.name));

		return KatelloCli.grepCLIOutput("Id", getOutput(exec_result));
	}
	
	private void assert_templList(List<KatelloTemplate> templates, List<KatelloTemplate> excludeTemplates) {

		exec_result = new KatelloTemplate(templ_name, null, org_name, null).list();

		//filters that exist in list
		for(KatelloTemplate templ : templates){
			if (templ.description == null) templ.description = "None";
			if (templ.parentId == null) templ.parentId = "None";
			
			String match_info = String.format(KatelloTemplate.REG_TEMPL_LIST, templ.name, templ.description, templ.parentId).replaceAll("\"", "");
			Assert.assertTrue(getOutput(exec_result).replaceAll("\n", " ").matches(match_info), String.format("Template [%s] should be found in the result list", templ.name));
		}
		
		//filters that should not exist in list
		for(KatelloTemplate templ : excludeTemplates){
			if (templ.description == null) templ.description = "None";
			if (templ.parentId == null) templ.parentId = "None";
			
			String match_info = String.format(KatelloTemplate.REG_TEMPL_LIST, templ.name, templ.description, templ.parentId).replaceAll("\"", "");
			Assert.assertFalse(getOutput(exec_result).replaceAll("\n", " ").matches(match_info), String.format("Template [%s] should be found in the result list", templ.name));
		}
	}
	
	private KatelloTemplate createTemplate() {
		templ_name = "template"+KatelloUtils.getUniqueID();
		
		KatelloTemplate tpl = new KatelloTemplate(templ_name, null, org_name, null);
		exec_result = tpl.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloTemplate.OUT_CREATE, templ_name)), "Check - output string (template create)");
		
		return tpl;
	}
}
