package com.redhat.qe.katello.tests.cli;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.obj.KatelloContentDefinition;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.common.TngRunGroups;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups=TngRunGroups.TNG_KATELLO_Content)
public class ContentFilterTests extends KatelloCliTestScript{
	
	public static final String ERRATA_ZOO_SEA = "RHEA-2012:0002";
	
	String uid = KatelloUtils.getUniqueID();
	String org_name = "orgcon-"+ uid;
	String env_name = "envcon-"+ uid;
	String prov_name = "provcon-" + uid;
	String prod_name = "prodcon-"+ uid;
	String repo_name = "repocon-" + uid;
	String condef_name = "condef-" + uid;

	
	SSHCommandResult exec_result;
	KatelloOrg org;
	KatelloEnvironment env;
	KatelloProvider prov;
	KatelloProduct prod;
	KatelloRepo repo;
	KatelloContentDefinition condef;
	
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
		
		exec_result = prod.promote(env_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	
		
		exec_result = repo.synchronize();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		condef = new KatelloContentDefinition(condef_name,null,org_name,null);
		exec_result = condef.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

	}

	
	@AfterClass
	public void tearDown() {
		exec_result = org.delete();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	}

}
