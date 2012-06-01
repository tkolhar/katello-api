package com.redhat.qe.katello.tests.cli;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.redhat.qe.auto.testng.Assert;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloTemplate;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups = { "cfse-cli" })
public class TemplateTests extends KatelloCliTestScript {

	protected static Logger log = Logger.getLogger(TemplateTests.class.getName());
	
	private SSHCommandResult exec_result;

	private String templ_name;
	private String org_name;


	@BeforeTest(description = "Generate unique objects")
	public void setUp() {
		String uid = KatelloTestScript.getUniqueID();
		org_name = "org" + uid;

		// Create org:
		KatelloOrg org = new KatelloOrg(this.org_name, "Package tests");
		exec_result = org.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	}
	
	@Test(description = "Create template", groups = { "cli-template" })
	public void test_createTemplate() {
		KatelloTemplate templ = createTemplate();
		
		assert_templInfo(templ);
	}
	
	@Test(description = "Create template and then list it", groups = { "cli-template" })
	public void test_listTemplate() {
		KatelloTemplate templ = createTemplate();
		
		String templ_name2= "template"+KatelloTestScript.getUniqueID();
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
		
		String templ_name2= "template"+KatelloTestScript.getUniqueID();
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
		
		String templ_name2= "template"+KatelloTestScript.getUniqueID();
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
		
		String templ_name2= "template"+KatelloTestScript.getUniqueID();
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
	
	private String assert_templInfo(KatelloTemplate templ) {
		if (templ.description == null) templ.description = "None";
		if (templ.parentId == null) templ.parentId = "None";
		if (templ.revision == null) templ.revision = "1";

		exec_result = templ.info(null);

		String match_info = String.format(KatelloTemplate.REG_TEMPL_INFO, templ.name, templ.revision, templ.description, templ.parentId).replaceAll("\"", "");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		log.finest(String.format("Template (info) match regex: [%s]", match_info));
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", " ").matches(match_info), String.format("Template [%s] should be found in the result info", templ.name));

		Pattern pattern = Pattern.compile(KatelloTemplate.REG_TEMPL_ID);
		Matcher matcher = pattern.matcher(getOutput(exec_result).replaceAll("\n", " "));
		Assert.assertTrue(matcher.find(), "Check - Id should exist in template info");
		String id = matcher.group();
		id = id.replace("Id:", "").replace("Name:", "").trim();
		
		return id;
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
		templ_name = "template"+KatelloTestScript.getUniqueID();
		
		KatelloTemplate tpl = new KatelloTemplate(templ_name, null, org_name, null);
		exec_result = tpl.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloTemplate.OUT_CREATE, templ_name)), "Check - output string (template create)");
		
		return tpl;
	}
}
