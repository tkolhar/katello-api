package com.redhat.qe.katello.tests.i18n;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.obj.KatelloChangeset;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloTemplate;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

public class TemplateTests extends KatelloCliTestScript {
	
	private String uid;
	private String org_name;
	private String template_name;
	private String template_description; 
	
	@BeforeClass(description="create org", alwaysRun=true)
	public void setUp(){
		uid = KatelloUtils.getUniqueID();
		org_name = getText("org.create.name")+" "+uid;
		template_name = getText("template.create.name")+" "+uid;
		template_description = getText("template.create.description")+" "+uid;
		
		KatelloOrg org = new KatelloOrg(org_name, null);
		SSHCommandResult res = org.cli_create();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
	}
	
	@Test(description="template create")
	public void test_createTemplate(){
		SSHCommandResult res;		
		String outSuccess = getText("template.create.stdout", template_name);
		
		KatelloTemplate template = new KatelloTemplate(template_name, template_description, org_name, null);
		res = template.create();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code (template create)");
		Assert.assertTrue(getOutput(res).equals(outSuccess), "Check - stdout (template create)");
	}
	
	@Test(description="template info", dependsOnMethods={"test_createTemplate"})
	public void test_infoTemplate(){
		SSHCommandResult res;
		KatelloTemplate template = new KatelloTemplate(template_name, null, org_name, null);
		res = template.info(KatelloEnvironment.LIBRARY);
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code (template info)");
		Assert.assertTrue(KatelloCli.grepCLIOutput(getText("template.list.stdout.property.name"), 
				getOutput(res)).equals(template_name),"Check - name in info");
		Assert.assertTrue(KatelloCli.grepCLIOutput("Description", getOutput(res)).equals(template_description),"Check - description in info");
		
	}
	
	@Test(description="template list", dependsOnMethods={"test_createTemplate"})
	public void test_listTemplate(){
		SSHCommandResult res;
		KatelloTemplate template = new KatelloTemplate(null, null, org_name, null);
		res = template.list();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code (template list)");
		Assert.assertTrue(KatelloCli.grepCLIOutput(getText("template.list.stdout.property.name"), 
				getOutput(res)).equals(template_name),"Check - name in list");
		Assert.assertTrue(KatelloCli.grepCLIOutput("Description", getOutput(res)).equals(template_description),"Check - description in list");
		
	}
	
	@Test(description="template update --add_param", dependsOnMethods="test_createTemplate")
	public void test_updateTemplate(){
		SSHCommandResult res;
		String outSuccess = getText("template.update.stdout", template_name);

		KatelloTemplate template = new KatelloTemplate(template_name, null, org_name, null);
		res = template.update_add_param("hostname", "localhost.localadmin");
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code (template update --add_param)");
		Assert.assertTrue(getOutput(res).contains(outSuccess), "Check - stdout (template update)");
	}
	
	@Test(description="export template - JSON format", dependsOnMethods={"test_updateTemplate"})
	public void test_exportTemplateJson(){
		SSHCommandResult res;
		String env_name = "testing";
		String cs_name = "changeset1";
		String filename = "/tmp/template"+uid;
		String outSuccess = getText("template.export.stdout", filename);
		
		KatelloTemplate template = new KatelloTemplate(template_name, null, org_name, null);
		KatelloEnvironment env = new KatelloEnvironment(env_name, null, org_name, KatelloEnvironment.LIBRARY);
		KatelloChangeset cs = new KatelloChangeset(cs_name, org_name, env_name);
		res = env.cli_create();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code (environment create)");
		res = cs.create();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code (changeset create)");
		res = cs.update_addTemplate(template_name);
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code (changeset update --add_template)");
		res = cs.promote();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code (changeset promote)");
		res = template.export(env_name, filename, "json");
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code (template export)");
		Assert.assertTrue(getOutput(res).contains(outSuccess), "Check - stdout (template export)");
		res = KatelloUtils.sshOnClient("ls \""+filename.replaceAll(" ", "\\ ")+"\"");
		Assert.assertTrue(res.getExitCode() == 0, "Check - file exists");
	}
}
