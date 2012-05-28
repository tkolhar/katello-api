package com.redhat.qe.katello.tests.cli;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.redhat.qe.auto.testng.Assert;
import com.redhat.qe.katello.base.KatelloCliDataProvider;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.katello.base.obj.KatelloActivationKey;
import com.redhat.qe.katello.base.obj.KatelloChangeset;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloTemplate;
import com.redhat.qe.tools.SSHCommandResult;

//@Test(groups={"cfse-cli","headpin-cli"})
public class ActivationKeyTests extends KatelloCliTestScript{
	private String organization;
	private String env;
	
	@BeforeClass(description="init: create org stuff", groups = {"cli-activationkey","headpin-cli"})
	public void setUp(){
		SSHCommandResult res;
		String uid = KatelloTestScript.getUniqueID();
		this.organization = "ak-"+uid;
		this.env = "ak-"+uid;
		KatelloOrg org = new KatelloOrg(this.organization, null);
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		KatelloEnvironment env = new KatelloEnvironment(this.env, null, this.organization, KatelloEnvironment.LIBRARY);
		res = env.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
	}
	
	@Test(description="create AK", groups = {"cli-activationkey","headpin-cli"}, 
			dataProvider="activationkey_create", dataProviderClass = KatelloCliDataProvider.class, enabled=true)
	public void test_create(String name, String descr, Integer exitCode, String output){
		SSHCommandResult res;
		
		KatelloActivationKey ak = new KatelloActivationKey(this.organization, this.env, name, descr, null);
		res = ak.create();
		Assert.assertTrue(res.getExitCode().intValue() == exitCode.intValue(), "Check - return code");
		
		if(exitCode.intValue()==0){ //
			Assert.assertTrue(getOutput(res).contains(output),"Check - returned output string");
		}else{ // Failure to be checked
			Assert.assertTrue(getOutput(res).contains(output),"Check - returned error string");
		}
	}
	
	@Test(description="create AK - template does not exist", groups = {"cli-activationkey"}, enabled=true)
	public void test_create_noTemplate(){
		SSHCommandResult res;
		String uid = KatelloTestScript.getUniqueID();
		String ak_name = "ne-"+uid;
		String template_name = "neTemplate-"+uid;
		
		KatelloActivationKey ak = new KatelloActivationKey(this.organization, this.env, ak_name, null, template_name);
		res = ak.create();
		Assert.assertTrue(res.getExitCode().intValue()==65, "Check - return code (activation_key create --template)");
		Assert.assertTrue(getOutput(res).trim().contains(
				String.format(KatelloActivationKey.ERR_TEMPLATE_NOTFOUND,template_name)), 
				"Check - returned error string (activation_key create --template)");
	}
	
	
	@Test(description="create AK - template not exported to the env.", groups = {"cli-activationkey"}, enabled=true)
	public void test_create_TemplateNotForEnv(){
		SSHCommandResult res;
		String uid = KatelloTestScript.getUniqueID();
		String template = "template-"+uid;
		String ak_name = "nfe-"+uid;

		// create the template
		KatelloTemplate tmpl = new KatelloTemplate(template, null, this.organization, null);
		res = tmpl.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (template create)");
		
		KatelloActivationKey ak = new KatelloActivationKey(this.organization, this.env, ak_name, null, template);
		res = ak.create();
		Assert.assertTrue(res.getExitCode().intValue()==65, "Check - return code (activation_key create --template)");
		Assert.assertTrue(getOutput(res).contains(
				String.format(KatelloActivationKey.ERR_TEMPLATE_NOTFOUND,template)), 
				"Check - returned error string (activation_key create --template)");
	}
	
	
	
	@Test(description="create AK - same name, diff. orgs", groups = {"cli-activationkey","headpin-cli"}, enabled=true)
	public void test_create_diffOrgsSameName(){
		SSHCommandResult res;
		String uid = KatelloTestScript.getUniqueID();
		String ak_name = "ak-"+uid;
		String org2 = "org2-"+uid;

		// create 2nd org (and the same env) 
		KatelloOrg org = new KatelloOrg(org2, null);
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		KatelloEnvironment env = new KatelloEnvironment(this.env, null, org2, KatelloEnvironment.LIBRARY);
		res = env.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		
		KatelloActivationKey ak = new KatelloActivationKey(org2, this.env, ak_name, null, null);
		res = ak.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (activation_key create)");
		
		ak = new KatelloActivationKey(this.organization, this.env, ak_name, null, null);
		res = ak.create(); // force update IDs 
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (activation_key create)");
		Assert.assertTrue(getOutput(res).contains(
				String.format(KatelloActivationKey.OUT_CREATE,ak_name)), 
				"Check - returned output string (activation_key create)");
		
		ak.asserts_create();
	}
	
	
	
	@Test(description="create AK - with template",groups = {"cli-activationkey"}, enabled=true)
	public void test_create_withTemplate(){
		SSHCommandResult res;
		String uid = KatelloTestScript.getUniqueID();
		String template = "templateForEnv-"+uid;
		String changeset = "csForEnv-"+uid;
		String ak_name = "akTemplate-"+uid;

		// create template
		KatelloTemplate tmpl = new KatelloTemplate(template, null, this.organization, null);
		res = tmpl.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (template create)");
		
		// create changeset
		KatelloChangeset cs = new KatelloChangeset(changeset, this.organization, this.env);
		res = cs.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (changeset create)");
		
		// add template to changeset
		res = cs.update_addTemplate(template);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (changeset update --add_template)");
		
		// promote changeset to the env.
		res = cs.promote();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (changeset promote)");
		
		KatelloActivationKey ak = new KatelloActivationKey(this.organization, this.env, ak_name, null, template);
		res = ak.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (activation_key create --template)");
		Assert.assertTrue(getOutput(res).contains(
				String.format(KatelloActivationKey.OUT_CREATE,ak_name)), 
				"Check - returned output string (activation_key create --template)");
		
		ak.asserts_create();
	}
	
	
    @Test(description="add subscription to ak", groups = {"cli-activationkey"},enabled=true)
    public void test_update_addSubscription1(){
            String uid = KatelloTestScript.getUniqueID();
            String akName="ak-subscription-zoo3-"+uid;
            String providerName = "Zoo3-"+uid;
            String productName = "Zoo3 "+uid;
            SSHCommandResult res;

            KatelloActivationKey ak = new KatelloActivationKey(this.organization, this.env, akName, "Activation key with Zoo3 subscription", null);
            res = ak.create();
            Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (activation_key create)");
            KatelloProvider prov = new KatelloProvider(providerName, this.organization, null, null);
            prov.create();
            KatelloProduct prod = new KatelloProduct(productName, this.organization, providerName, null, null, null, null, null);
            res = prod.create();
            Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");
            KatelloOrg org = new KatelloOrg(this.organization, null);
            res = org.subscriptions();
    }
}
