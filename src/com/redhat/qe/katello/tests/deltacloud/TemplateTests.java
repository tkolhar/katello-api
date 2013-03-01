package com.redhat.qe.katello.tests.deltacloud;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.base.obj.KatelloTemplate;

public class TemplateTests extends BaseDeltacloudTest {
	
	@Test(description="add rhel repo into template")
	public void test_templateAddRHELRepo() {
		KatelloTemplate tpl = new KatelloTemplate(templ_name, null, org_name, null);
		
		exec_result = tpl.update_add_repo(KatelloProduct.RHEL_SERVER, KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = tpl.info(null);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		String match_info = String.format(KatelloTemplate.REG_TEMPL_REPOS, KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT).replaceAll("\"", "");
		Pattern pattern = Pattern.compile(match_info);
		Matcher matcher = pattern.matcher(getOutput(exec_result).replaceAll("\n", " "));
		Assert.assertTrue(matcher.find(), "Check - Repository should exist in template info");
	}

	@Test(description="add wrong repo into template")
	public void test_templateAddWrongRHELRepo() {
		KatelloTemplate tpl = new KatelloTemplate(templ_name, null, org_name, null);

		exec_result = tpl.update_add_repo(KatelloProduct.RHEL_SERVER, "rancidfood");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 65, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloTemplate.ERR_ADD_REPO, "rancidfood", org_name, KatelloProduct.RHEL_SERVER, "Library")), "Check - error string (template update)");
	}
	
	@Test(description = "add package to template")
	public void test_updateTemplateAddRHELPackage() {
		KatelloTemplate templ = new KatelloTemplate(templ_name, null, org_name, null);
		
		String packageName = "telnet";
		
		exec_result = templ.update_add_package(KatelloProduct.RHEL_SERVER, packageName);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloTemplate.OUT_UPDATE, templ_name)), "Check - output string (template update)");
		
		exec_result = templ.info(null);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		String match_info = String.format(KatelloTemplate.REG_TEMPL_PACKAGES, packageName).replaceAll("\"", "");
		Pattern pattern = Pattern.compile(match_info);
		Matcher matcher = pattern.matcher(getOutput(exec_result).replaceAll("\n", " "));
		Assert.assertTrue(matcher.find(), "Check - Package should exist in template info");
	}
	
	@Test(description = "add wrong package to template, verify error")
	public void test_updateTemplateAddWrongRHELPackage() {
		KatelloTemplate tpl = new KatelloTemplate(templ_name, null, org_name, null);

		String packageName = "rancidfood";
		
		exec_result = tpl.update_add_package(KatelloProduct.RHEL_SERVER, packageName);
		Assert.assertTrue(exec_result.getExitCode() == 144, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloTemplate.ERR_ADD_PACKAGE, packageName, "Library")), "Check - output string (template update)");
	}
	
	@Test(description = "add wrong package group to template, verify error")
	public void test_updateTemplateAddWrongRHELPackageGroup() {
		KatelloTemplate templ = new KatelloTemplate(templ_name, null, org_name, null);
		
		String packageGroupName = "rancidfood";
		
		exec_result = templ.update_add_package_group(KatelloProduct.RHEL_SERVER, packageGroupName);
		Assert.assertTrue(exec_result.getExitCode() == 144, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloTemplate.ERR_ADD_PACKAGE_GROUP, packageGroupName, "Library")), "Check - output string (template update)");
	}
}
