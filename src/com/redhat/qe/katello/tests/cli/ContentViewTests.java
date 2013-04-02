package com.redhat.qe.katello.tests.cli;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.obj.KatelloActivationKey;
import com.redhat.qe.katello.base.obj.KatelloChangeset;
import com.redhat.qe.katello.base.obj.KatelloContentView;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

public class ContentViewTests extends KatelloCliTestScript{
	
	String uid = KatelloUtils.getUniqueID();
	String org_name = "orgcon-"+ uid;
	String env_name = "envcon-"+ uid;
	String prov_name = "provcon-" + uid;
	String prod_name = "prodcon-"+ uid;
	String repo_name = "repocon-" + uid;
	String changeset_name = "changecon-" + uid;
	String condef_name = "condef-" + uid;
	String conview_name = "conview-" + uid;
	String pubview_name = "pubview-" + uid;
	String act_key_name = "act_key" + uid;
	String prod_name2 = "prodcon2-" + uid;
	String repo_name2 = "repo_name2-" + uid; 
	SSHCommandResult exec_result;
	KatelloOrg org;
	KatelloEnvironment env;
	KatelloProvider prov;
	KatelloProduct prod;
	KatelloRepo repo;
	KatelloProduct prod2;
	KatelloRepo repo2;
	KatelloChangeset changeset;
	KatelloContentView condef;
	KatelloActivationKey act_key;
	
	@BeforeClass(description="Generate unique objects")
	public void setUp() {
		
		org = new KatelloOrg(org_name,null);
		exec_result = org.cli_create();		              
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		env = new KatelloEnvironment(env_name,null,org_name,KatelloEnvironment.LIBRARY);
		exec_result = env.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		prov = new KatelloProvider(prov_name,org_name,null,null);
		exec_result = prov.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		prod = new KatelloProduct(prod_name,org_name,prov_name,null, null, null,null, null);
		exec_result = prod.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		repo = new KatelloRepo(repo_name,org_name,prod_name,REPO_INECAS_ZOO3, null, null);
		exec_result = repo.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = repo.synchronize();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		changeset = new KatelloChangeset(changeset_name,org_name,env_name);
		exec_result = changeset.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = changeset.apply();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = changeset.update_addProduct(prod_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result =  changeset.promote();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result =  changeset.info();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	
		prod2 = new KatelloProduct(prod_name2,org_name,prov_name,null, null, null,null, null);
		exec_result = prod2.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		repo2 = new KatelloRepo(repo_name2,org_name,prod_name,REPO_INECAS_ZOO3, null, null);
		exec_result = repo2.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
	}
	

	@Test(description = "Adding a published content view to an activation key",groups={"cfse-cli"})
	public void test_addContentView(){
		
		condef = new KatelloContentView(condef_name,null,org_name,null);
		exec_result = condef.create_definition();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");		
		exec_result = condef.add_product(prod_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");			
		exec_result = condef.publish(pubview_name,pubview_name,"Publish Content");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");		
		exec_result = changeset.update_addView(pubview_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");		
		exec_result = changeset.apply();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");		
		act_key = new KatelloActivationKey(org_name,env_name,act_key_name,"Act key created");
		exec_result = act_key.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	
		exec_result = act_key.update_add_content_view(pubview_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	
		exec_result = act_key.info();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");		
		exec_result = condef.definition_info();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = org.delete();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

	}
	


}
