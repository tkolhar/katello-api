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
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.common.TngRunGroups;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups={"cfse-cli",TngRunGroups.TNG_KATELLO_Content})
public class ContentDefinitionTest extends KatelloCliTestBase{

	private SSHCommandResult exec_result;
	private String org_name;
	private String provider_name;
	private String product_name;
	private String repo_name;
	private String provider_name2;
	private String product_name2;
	private String repo_name2;
	private String content_name;
	private String content_name_edit;
	private String content_name_prod;
	private String content_name_repo;
	
	private String _cvdClone;
	
	@BeforeClass(description="init: create initial stuff")
	public void setUp() {
		String uid = KatelloUtils.getUniqueID();
		org_name = "org"+uid;
		provider_name = "provider"+uid;
		product_name = "product"+uid;
		repo_name = "repo"+uid;
		provider_name2 = "provider2"+uid;
		product_name2 = "product2"+uid;
		repo_name2 = "repo2"+uid;
		_cvdClone = "cvdClone-"+uid;
	}

	@Test(description="initialization goes here")
	public void init(){
		// Create org:
		KatelloOrg org = new KatelloOrg(this.cli_worker, this.org_name,"Package tests");
		exec_result = org.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		// Create provider:
		KatelloProvider prov = new KatelloProvider(this.cli_worker, provider_name, org_name, "Package provider", PULP_RHEL6_x86_64_REPO);
		exec_result = prov.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		// Create product:
		KatelloProduct prod = new KatelloProduct(this.cli_worker, product_name, org_name, provider_name, null, null, null, null, null);
		exec_result = prod.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		KatelloRepo repo = new KatelloRepo(this.cli_worker, repo_name, org_name, product_name, PULP_RHEL6_x86_64_REPO, null, null);
		exec_result = repo.create(true);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		// Create second provider:
		prov = new KatelloProvider(this.cli_worker, provider_name2, org_name, "Package provider", REPO_INECAS_ZOO3);
		exec_result = prov.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		// Create product:
		prod = new KatelloProduct(this.cli_worker, product_name2, org_name, provider_name2, null, null, null, null, null);
		exec_result = prod.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		repo = new KatelloRepo(this.cli_worker, repo_name2, org_name, product_name2, REPO_INECAS_ZOO3, null, null);
		exec_result = repo.create(true);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		// init-s
		init_cvdClone();
	}
	
	@Test(description = "Create new content definition", dependsOnMethods={"init"})
	public void test_Create() {
		
		KatelloContentDefinition content = createContentDefinition();
		assert_ContentViewDefinitionInfo(content);
		assert_contentList(Arrays.asList(content), new ArrayList<KatelloContentDefinition>());
		content_name_edit = content.name;
	}
	
	@Test(description = "Edit content definition", dependsOnMethods={"test_Create"})
	public void test_Edit() {
		KatelloContentDefinition content = new KatelloContentDefinition(cli_worker, content_name_edit, "descritpion", org_name, content_name_edit);
		content.update("edited description");
		content.description = "edited description";
		assert_ContentViewDefinitionInfo(content);
	}
	
	@Test(description = "Create Content Def with empty name, verify error", dependsOnMethods={"init"})
	public void test_createContentDefEmptyName() {
		KatelloContentDefinition content = new KatelloContentDefinition(cli_worker, "", null, org_name, null);
		exec_result = content.create();
		Assert.assertEquals(exec_result.getExitCode().intValue(), 166, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(KatelloContentDefinition.ERR_NAME_EMPTY), "Check - error string (content create)");
	}
	
	@Test(description = "Create Content Def with long name, verify error", dependsOnMethods={"init"})
	public void test_createContentDefLongName() {
		String name = KatelloCliDataProvider.strRepeat("Lorem ipsum dolor sit amet", 14);
		
		KatelloContentDefinition content = new KatelloContentDefinition(cli_worker, name, null, org_name, null);
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
		KatelloContentDefinition content = new KatelloContentDefinition(cli_worker, content_name_edit, "descritpion", org_name, content_name_edit);
		content.update("edited descr 2");
		content.description = "edited descr 2";
		assert_ContentViewDefinitionInfo(content);
	}
	
	@Test(description = "Create new content definition, add product into it", dependsOnMethods={"init"})
	public void test_addProduct() {
		KatelloContentDefinition content = createContentDefinition();
		content_name_prod = content.name;
		exec_result = content.add_product(product_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloContentDefinition.OUT_ADD_PRODUCT, product_name, content.getName())), "Check - output string (add product)");

		exec_result = content.add_product(product_name2);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloContentDefinition.OUT_ADD_PRODUCT, product_name2, content.getName())), "Check - output string (add product)");

		content.products = product_name+"\\s+"+product_name2;
		assert_ContentViewDefinitionInfo(content);
	}

	@Test(description = "remove product from definition", dependsOnMethods={"test_addProduct"})
	public void test_removeProduct() {
		KatelloContentDefinition content = new KatelloContentDefinition(cli_worker, content_name_prod, "descritpion", org_name, content_name_prod);
		
		exec_result = content.remove_product(product_name2);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloContentDefinition.OUT_REMOVE_PRODUCT, product_name2, content.getName())), "Check - output string (remove product)");

		content.products = product_name;
		assert_ContentViewDefinitionInfo(content);
	}

	@Test(description = "Create new content definition, add repo into it", dependsOnMethods={"init"})
	public void test_addRepo() {
		KatelloContentDefinition content = createContentDefinition();
		content_name_repo = content.name;
		exec_result = content.add_repo(product_name, repo_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloContentDefinition.OUT_ADD_REPO, repo_name, content.getName())), "Check - output string (add repo)");

		exec_result = content.add_repo(product_name2, repo_name2);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloContentDefinition.OUT_ADD_REPO, repo_name2, content.getName())), "Check - output string (add repo)");

		content.repos = repo_name+"\\s+"+repo_name2;
		assert_ContentViewDefinitionInfo(content);
	}

	@Test(description = "remove repo from definition", dependsOnMethods={"test_addRepo"})
	public void test_removeRepo() {
		KatelloContentDefinition content = new KatelloContentDefinition(cli_worker, content_name_repo, "descritpion", org_name, content_name_repo);
		
		exec_result = content.remove_repo(product_name2, repo_name2);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloContentDefinition.OUT_REMOVE_REPO, repo_name2, content.getName())), "Check - output string (remove repo)");

		content.repos = repo_name;
		assert_ContentViewDefinitionInfo(content);
	}
	
	/**
	 * Cloning content definition
	 * @see <a href='https://github.com/gkhachik/katello-api/issues/290'>github issue</a>
	 * @author gkhachik
	 * @since 15.April.2013
	 */
	@Test(description="Clone content definition", dependsOnMethods={"init"})
	public void test_clone(){
		String sCvdOrigin = this._cvdClone+"-origin";
		String sCvdClone = this._cvdClone+"-clone";
		KatelloContentDefinition cvdOrigin = new KatelloContentDefinition(cli_worker, sCvdOrigin, null, this.org_name, null);
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
		KatelloContentDefinition cvdClone = new KatelloContentDefinition(cli_worker, sCvdClone, null, this.org_name, null);

		exec_result = cvdClone.list();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		String matchInList = ".*Name\\s+:\\s+%s.*Composite\\s+:\\s+False.*";
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", " ").matches(String.format(matchInList,sCvdClone)), "Check - contains in content definition list");
		
		exec_result = cvdClone.info();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(KatelloUtils.grepCLIOutput("Name",getOutput(exec_result)).equals(sCvdClone), "Check - stdout (content definition info: Name)");
		Assert.assertTrue(KatelloUtils.grepCLIOutput("Composite",getOutput(exec_result)).equals("False"), "Check - stdout (content definition info: Composite)");
		Assert.assertTrue(KatelloUtils.grepCLIOutput("Org",getOutput(exec_result)).equals(this.org_name), "Check - stdout (content definition info: Org)");
	}

	private void assert_contentList(List<KatelloContentDefinition> contents, List<KatelloContentDefinition> excludeContents) {

		SSHCommandResult res = new KatelloContentDefinition(cli_worker, null, null, org_name, null).list();

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
				content.publishedViews, content.componentViews, content.products, content.repos).replaceAll("\"", "");
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		log.finest(String.format("Org (info) match regex: [%s]",match_info));
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(match_info), 
				String.format("Org [%s] should be found in the result info", org_name));	
		
		return KatelloUtils.grepCLIOutput("ID", getOutput(res));
	}
	
	private KatelloContentDefinition createContentDefinition() {
		content_name = "content"+KatelloUtils.getUniqueID();
		KatelloContentDefinition content = new KatelloContentDefinition(cli_worker, content_name, "descritpion", org_name, content_name);
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
		exec_result = new KatelloProvider(this.cli_worker, this._cvdClone, this.org_name, null, null).create();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		exec_result = new KatelloProduct(this.cli_worker, this._cvdClone, this.org_name, this._cvdClone, null, null, null, null, null).create();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		// create repo zoo
		exec_result = new KatelloRepo(this.cli_worker, this._cvdClone+"-zoo", this.org_name, this._cvdClone, REPO_INECAS_ZOO3, null, null).create(true);
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		// create repo pulp
		exec_result = new KatelloRepo(this.cli_worker, this._cvdClone+"-pulp", this.org_name, this._cvdClone, PULP_RHEL6_x86_64_REPO, null, null).create(true);
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		// sync the provider
		exec_result = new KatelloProvider(this.cli_worker, this._cvdClone, this.org_name, null, null).synchronize();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
	}
}
