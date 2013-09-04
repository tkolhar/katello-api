package com.redhat.qe.katello.tests.cli;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliDataProvider;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloContentDefinition;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.common.TngRunGroups;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups={"cfse-cli",TngRunGroups.TNG_KATELLO_Content})
public class ContentDefinitionTest extends KatelloCliTestBase{
	private String content_name;
	private String content_name_edit;
	private String content_name_prod;
	private String content_name_repo;
	
	private String _cvdClone;
	
	@BeforeClass(description="init: create initial stuff")
	public void setUp() {
		String uid = KatelloUtils.getUniqueID();
		_cvdClone = "cvdClone-"+uid;
		init_cvdClone();
	}
	
	@Test(description = "Create new content definition")
	public void test_Create() {
		KatelloContentDefinition content = createContentDefinition();
		assert_ContentViewDefinitionInfo(content);
		assert_contentList(Arrays.asList(content), new ArrayList<KatelloContentDefinition>());
		content_name_edit = content.name;
	}
	
	@Test(description = "Edit content definition", dependsOnMethods={"test_Create"})
	public void test_Edit() {
		KatelloContentDefinition content = new KatelloContentDefinition(cli_worker, content_name_edit, "descritpion", base_org_name, content_name_edit);
		content.update("edited description");
		content.description = "edited description";
		assert_ContentViewDefinitionInfo(content);
	}

	@Test(description = "update content definition name")
	public void test_updateName() {
		String new_name = "definition" + KatelloUtils.getUniqueID();
		KatelloContentDefinition content = createContentDefinition();
		assert_ContentViewDefinitionInfo(content);
		content.update_name(new_name);
		content.name = new_name;
		assert_ContentViewDefinitionInfo(content);
	}
	
	@Test(description = "Create Content Def with empty name, verify error")
	public void test_createContentDefEmptyName() {
		KatelloContentDefinition content = new KatelloContentDefinition(cli_worker, "", null, base_org_name, null);
		exec_result = content.create();
		Assert.assertEquals(exec_result.getExitCode().intValue(), 2, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains("error: Option --name is required"), "Check - error string (content create)");
	}
	
	@Test(description = "Create Content Def with long name, verify error")
	public void test_createContentDefLongName() {
		String name = KatelloCliDataProvider.strRepeat("Lorem ipsum dolor sit amet", 14);
		
		KatelloContentDefinition content = new KatelloContentDefinition(cli_worker, name, null, base_org_name, null);
		exec_result = content.create();
		Assert.assertEquals(exec_result.getExitCode().intValue(), 166, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).equals(KatelloContentDefinition.ERR_NAME_LONG), "Check - error string (content create)");
	}
	
	@Test(description = "Create 2 new content definitions, delete one of them", dependsOnMethods={"test_Create"})
	public void test_delete() {
		
		KatelloContentDefinition content = createContentDefinition();
		KatelloContentDefinition content2 = createContentDefinition();
		
		String id2 = assert_ContentViewDefinitionInfo(content2);
		content2.setLabel(null); // if we want to delete by Id, then we HAVE TO set the label to null.
		content2.setId(Long.valueOf(id2));
		
		exec_result = content2.delete();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloContentDefinition.OUT_DELETE_DEFINITION, content_name)), "Check - error string (content delete)");
		
		assert_contentList(Arrays.asList(content), Arrays.asList(content2));
	}
	
	@Test(description = "Edit content definition second time", dependsOnMethods={"test_delete"})
	public void test_Edit2() {
		KatelloContentDefinition content = new KatelloContentDefinition(cli_worker, content_name_edit, "descritpion", base_org_name, content_name_edit);
		content.update("edited descr 2");
		content.description = "edited descr 2";
		assert_ContentViewDefinitionInfo(content);
	}
	
	@Test(description = "Create new content definition, add product into it")
	public void test_addProduct() {
		KatelloContentDefinition content = createContentDefinition();
		content_name_prod = content.name;
		exec_result = content.add_product(base_pulp_product_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloContentDefinition.OUT_ADD_PRODUCT, base_pulp_product_name.replaceAll(" ", "_"), content.getName())), "Check - output string (add product)");

		exec_result = content.add_product(base_zoo_product_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloContentDefinition.OUT_ADD_PRODUCT, base_zoo_product_name.replaceAll(" ", "_"), content.getName())), "Check - output string (add product)");

		content.products = base_pulp_product_name+"\\s+"+base_zoo_product_name;
		assert_ContentViewDefinitionInfo(content);
	}

	@Test(description = "remove product from definition", dependsOnMethods={"test_addProduct"})
	public void test_removeProduct() {
		KatelloContentDefinition content = new KatelloContentDefinition(cli_worker, content_name_prod, "descritpion", base_org_name, content_name_prod);
		
		exec_result = content.remove_product(base_zoo_product_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloContentDefinition.OUT_REMOVE_PRODUCT, base_zoo_product_name.replaceAll(" ", "_"), content.getName())), "Check - output string (remove product)");

		content.products = base_pulp_product_name;
		assert_ContentViewDefinitionInfo(content);
	}

	@Test(description = "Create new content definition, add repo into it")
	public void test_addRepo() {
		KatelloContentDefinition content = createContentDefinition();
		content_name_repo = content.name;
		exec_result = content.add_repo(base_pulp_product_name, base_pulp_repo_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloContentDefinition.OUT_ADD_REPO, base_pulp_repo_name, content.getName())), "Check - output string (add repo)");

		exec_result = content.add_repo(base_zoo_product_name, base_zoo_repo_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloContentDefinition.OUT_ADD_REPO, base_zoo_repo_name, content.getName())), "Check - output string (add repo)");

		content.repos = base_pulp_repo_name+"\\s*"+base_zoo_repo_name;
		assert_ContentViewDefinitionInfo(content);
	}

	@Test(description = "remove repo from definition", dependsOnMethods={"test_addRepo"})
	public void test_removeRepo() {
		KatelloContentDefinition content = new KatelloContentDefinition(cli_worker, content_name_repo, "descritpion", base_org_name, content_name_repo);
		
		exec_result = content.remove_repo(base_zoo_product_name, base_zoo_repo_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloContentDefinition.OUT_REMOVE_REPO, base_zoo_repo_name, content.getName())), "Check - output string (remove repo)");

		content.repos = base_pulp_repo_name;
		assert_ContentViewDefinitionInfo(content);
	}
	
	/**
	 * Cloning content definition
	 * @see <a href='https://github.com/gkhachik/katello-api/issues/290'>github issue</a>
	 * @author gkhachik
	 * @since 15.April.2013
	 */
	@Test(description="Clone content definition")
	public void test_clone(){
		String sCvdOrigin = this._cvdClone+"-origin";
		String sCvdClone = this._cvdClone+"-clone";
		KatelloContentDefinition cvdOrigin = new KatelloContentDefinition(cli_worker, sCvdOrigin, null, base_org_name, null);
		exec_result = cvdOrigin.create();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		exec_result = cvdOrigin.add_product(this._cvdClone);
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		
		exec_result = cvdOrigin.clone(sCvdClone, null, null);
		// asserts
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).equals(
				String.format(KatelloContentDefinition.OUT_CLONE_SUCCESSFUL,sCvdClone)), 
				"Check - stdout (successfully cloned)");
		KatelloContentDefinition cvdClone = new KatelloContentDefinition(cli_worker, sCvdClone, null, base_org_name, null);

		exec_result = cvdClone.list();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		String matchInList = ".*Name\\s+:\\s+%s.*Composite\\s+:\\s+False.*";
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", " ").matches(String.format(matchInList,sCvdClone)), "Check - contains in content definition list");
		
		exec_result = cvdClone.info();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(KatelloUtils.grepCLIOutput("Name",getOutput(exec_result)).equals(sCvdClone), "Check - stdout (content definition info: Name)");
		Assert.assertTrue(KatelloUtils.grepCLIOutput("Composite",getOutput(exec_result)).equals("False"), "Check - stdout (content definition info: Composite)");
		Assert.assertTrue(KatelloUtils.grepCLIOutput("Org",getOutput(exec_result)).equals(base_org_name), "Check - stdout (content definition info: Org)");
	}

	@Test(description="publish content definition asynchronously")
	public void test_publishAsync() {
		KatelloContentDefinition def = createContentDefinition();
		exec_result = def.publish_async("view-"+def.name, null, null);
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check exit code (def. publish async)");
		Assert.assertTrue(getOutput(exec_result).matches(KatelloContentDefinition.OUT_REG_PUBLISH_ASYNC), "Check output (def. publish async)");
	}

	@Test(description="definition does not exist - check error")
	public void test_definitionNotfound() {
		String def_name = "definition"+KatelloUtils.getUniqueID();
		KatelloContentDefinition def = new KatelloContentDefinition(cli_worker, def_name, null, base_org_name, null);
		exec_result = def.info();
		Assert.assertTrue(exec_result.getExitCode()==65, "Check exit code (definition not found)");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloContentDefinition.ERR_NOT_FOUND, def_name, base_org_name)), "Check error (definition not found)");
	}

	@Test(description="add content view to non composite definition - check error")
	public void test_addViewNonComposite() {
		String view_name = "definition"+KatelloUtils.getUniqueID();
		KatelloContentDefinition def1 = createContentDefinition();
		KatelloContentDefinition def2 = createContentDefinition();
		exec_result = def1.publish(view_name, null, null);
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (create definition)");
		exec_result = def2.add_view(view_name);
		Assert.assertTrue(exec_result.getExitCode()==65, "Check exit code (add view)");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloContentDefinition.ERR_NOT_COMPOSITE, def2.name)), "Check error (add view)");
	}

	private void assert_contentList(List<KatelloContentDefinition> contents, List<KatelloContentDefinition> excludeContents) {

		SSHCommandResult res = new KatelloContentDefinition(cli_worker, null, null, base_org_name, null).list();

		//contents that exist in list
		for(KatelloContentDefinition cont : contents){
			if (cont.description == null) cont.description = "None";
			
			String match_info = String.format(KatelloContentDefinition.REG_DEF_LIST, cont.name, cont.label, cont.description, cont.org).replaceAll("\"", "");
			Assert.assertTrue(getOutput(res).replaceAll("\n", " ").matches(match_info), String.format("Content Definition [%s] should be found in the result list", cont.name));
		}
		
		//contents that should not exist in list
		for(KatelloContentDefinition cont : excludeContents){
			if (cont.description == null) cont.description = "None";
			
			String match_info = String.format(KatelloContentDefinition.REG_DEF_LIST, cont.name, cont.label, cont.description, cont.org).replaceAll("\"", "");
			Assert.assertFalse(getOutput(res).replaceAll("\n", " ").matches(match_info), String.format("Content definition [%s] should not be found in the result list", cont.name));
		}
	}
	
	private String assert_ContentViewDefinitionInfo(KatelloContentDefinition content) {
		SSHCommandResult res;
		if (content.description == null) content.description = "None";
		res = content.info();
		String match_info = String.format(KatelloContentDefinition.REG_DEF_INFO, content.name, content.label, content.description, content.org,
				content.publishedViews, content.componentViews, ".*", content.repos).replaceAll("\"", ""); // products order can differ: so replacing content.products -> .*
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		log.finest(String.format("Org (info) match regex: [%s]",match_info));
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(match_info), 
				String.format("Org [%s] should be found in the result info", base_org_name));
		
		return KatelloUtils.grepCLIOutput("ID", getOutput(res));
	}
	
	private KatelloContentDefinition createContentDefinition() {
		content_name = "content"+KatelloUtils.getUniqueID();
		KatelloContentDefinition content = new KatelloContentDefinition(cli_worker, content_name, "descritpion", base_org_name, content_name);
		exec_result = content.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloContentDefinition.OUT_CREATE_DEFINITION, content_name)), "Check - out string (content create)");
		
		return content;
	}

	/**
	 * Init required for content view definition cloning scenario(s).
	 * @author gkhachik
	 * @since 15.Apr.2013
	 */
	private void init_cvdClone(){
		exec_result = new KatelloProvider(this.cli_worker, this._cvdClone, base_org_name, null, null).create();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		exec_result = new KatelloProduct(this.cli_worker, this._cvdClone, base_org_name, this._cvdClone, null, null, null, null, null).create();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		// create repo zoo
		exec_result = new KatelloRepo(this.cli_worker, this._cvdClone+"-zoo", base_org_name, this._cvdClone, REPO_INECAS_ZOO3, null, null).create(true);
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		// create repo pulp
		exec_result = new KatelloRepo(this.cli_worker, this._cvdClone+"-pulp", base_org_name, this._cvdClone, PULP_RHEL6_x86_64_REPO, null, null).create(true);
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		// sync the provider
		exec_result = new KatelloProvider(this.cli_worker, this._cvdClone, base_org_name, null, null).synchronize();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
	}
}
