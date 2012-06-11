package com.redhat.qe.katello.tests.cli;

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
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups={"cfse-cli"})
public class ChangesetTests extends KatelloCliTestScript{

	protected static Logger log = Logger.getLogger(TemplateTests.class.getName());
	
	private SSHCommandResult exec_result;
	
	private String org_name;
	private String env_name;
	private String chst_name;
	
	@BeforeClass(description="init: create org stuff", groups = {"cli-filter"})
	public void setUp() {
		
		String uid = KatelloTestScript.getUniqueID();
		org_name = "org" + uid;
		env_name = "env"+uid;
		
		// Create org:
		KatelloOrg org = new KatelloOrg(this.org_name, "Package tests");
		exec_result = org.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		KatelloEnvironment env = new KatelloEnvironment(env_name, null, org_name, KatelloEnvironment.LIBRARY);
		exec_result = env.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code (env create)");
		
	}
	
	@Test(description = "Create changeset", groups = { "cli-changeset" })
	public void test_createTemplate() {
		KatelloChangeset chst = createChangeset();
		
		assert_changesetInfo(chst);
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

		exec_result = chst.info();

		String match_info = String.format(KatelloChangeset.REG_CHST_INFO, chst.name, chst.description, chst.environment).replaceAll("\"", "");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		log.finest(String.format("Changeset (info) match regex: [%s]", match_info));
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", " ").matches(match_info), String.format("Template [%s] should be found in the result info", chst.name));

		Pattern pattern = Pattern.compile(KatelloChangeset.REG_CHST_ID);
		Matcher matcher = pattern.matcher(getOutput(exec_result).replaceAll("\n", " "));
		Assert.assertTrue(matcher.find(), "Check - Id should exist in changeset info");
		String id = matcher.group();
		id = id.replace("Id:", "").replace("Name:", "").trim();
		
		return id;
	}
		
}
