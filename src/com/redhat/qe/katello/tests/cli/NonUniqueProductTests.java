package com.redhat.qe.katello.tests.cli;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloSyncPlan;
import com.redhat.qe.katello.base.obj.KatelloSyncPlan.SyncPlanInterval;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.common.TngRunGroups;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups=TngRunGroups.TNG_KATELLO_Providers_Repos)
public class NonUniqueProductTests  extends KatelloCliTestBase{

	private String org_name;
	private String prov_name;
	private String prod_name1;
	private String prod_name2;
	private String prod_id1;
	private String prod_id2;
	private String plan_name;
	private String env_name;
	
	@BeforeClass(description="Prepare 2 products with same name to work with", groups = {"cli-product"})
	public void setup_org(){
		SSHCommandResult res;
		String uid = KatelloUtils.getUniqueID();
		this.org_name = "org"+uid;
		this.prov_name = "prov"+uid;
		this.prod_name1 = "prod"+uid;
		this.prod_name2 = this.prod_name1;
		this.plan_name = "plan"+uid;
		this.env_name = "env"+uid;		
		
		KatelloOrg org = new KatelloOrg(this.org_name, null);
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (org create)");
		
		KatelloProvider prov = new KatelloProvider(this.prov_name, this.org_name, null, null);
		res = prov.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (provider create)");
		
		KatelloProduct prod = new KatelloProduct(this.prod_name1, this.org_name, this.prov_name, null, null, null, null, null);
		res = prod.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");
		
		prod = new KatelloProduct(this.prod_name2, this.org_name, this.prov_name, null, null, null, null, null);
		res = prod.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");
		
		res = prod.cli_list();
		prod_id1 = KatelloUtils.grepCLIOutput("ID", getOutput(res).trim(),1);
		prod_id2 = KatelloUtils.grepCLIOutput("ID", getOutput(res).trim(),2);
		
		DateFormat dformat = new SimpleDateFormat("yyyy-MM-dd");
		DateFormat tformat = new SimpleDateFormat("HH:mm:ss");
		KatelloSyncPlan sp = new KatelloSyncPlan(this.plan_name, org_name, null, dformat.format(new Date()), tformat.format(new Date()), SyncPlanInterval.hourly);
		res = sp.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (sync_plan create)");
		
		KatelloEnvironment env = new KatelloEnvironment(this.env_name, null, this.org_name, KatelloEnvironment.LIBRARY);
		res = env.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (environment create)");
	}
	

    @Test(description = "update product by id", groups = {"cli-products"})
	public void test_updateProduct() {
    	KatelloProduct prod = new KatelloProduct(null, prod_id1, this.org_name, this.prov_name, null, null, null, null, null);
    	SSHCommandResult res = prod.update_description("new description");
    	Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product update)");
 	}
    
    @Test(description = "product status by id", groups = {"cli-products"})
	public void test_productStatus() {
    	KatelloProduct prod = new KatelloProduct(null, prod_id1, this.org_name, this.prov_name, null, null, null, null, null);
    	SSHCommandResult res = prod.status();
    	Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product status)");
    	String name = KatelloUtils.grepCLIOutput("Name", getOutput(res).trim(),1);
    	Assert.assertEquals(this.prod_name1, name);
    }

    @Test(description = "product Add sync_plan", groups = {"cli-products"})
	public void test_productAddPlan() {
    	KatelloProduct prod = new KatelloProduct(null, prod_id1, this.org_name, this.prov_name, null, null, null, null, null);
    	
    	SSHCommandResult res = prod.cli_set_plan(this.plan_name);
    	Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product add plan)");
    	
    	res = prod.cli_list();
    	Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product list)");
    	Assert.assertTrue(getOutput(res).trim().contains(this.plan_name));
    }

    @Test(description = "product Remove sync_plan", groups = {"cli-products"}, dependsOnMethods={"test_productAddPlan"})
	public void test_productRemovePlan() {
    	KatelloProduct prod = new KatelloProduct(null, prod_id1, this.org_name, this.prov_name, null, null, null, null, null);
    	
    	SSHCommandResult res = prod.cli_remove_plan();
    	Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product remove plan)");
    	
    	res = prod.cli_list();
    	Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product list)");
    	Assert.assertFalse(getOutput(res).trim().contains(this.plan_name));
    }
    
    @Test(description = "sync product by id", groups = {"cli-products"})
	public void test_syncProduct() {
    	KatelloProduct prod = new KatelloProduct(null, prod_id1, this.org_name, this.prov_name, null, null, null, null, null);
    	SSHCommandResult res = prod.synchronize();
    	Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product sync)");
 	}
    
    @Test(description = "promote product by id", groups = {"cli-products"})
	public void test_promoteProduct() {
    	KatelloUtils.promoteProductIDsToEnvironment(org_name, new String[] {prod_id1}, env_name);
 	}
    
    @Test(description = "delete product by id", groups = {"cli-products"})
	public void test_deleteProduct() {
    	KatelloProduct prod = new KatelloProduct(null, prod_id2, this.org_name, this.prov_name, null, null, null, null, null);
    	SSHCommandResult res = prod.delete();
    	Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product delete)");
 	}

}
