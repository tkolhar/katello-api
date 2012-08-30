package com.redhat.qe.katello.tests.api;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloApiException;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;

@Test(groups={"cfse-api"})
public class EnvironmentsTest extends KatelloTestScript{
	private String org_name;
	private String env_name;
	protected static Logger log = Logger.getLogger(EnvironmentsTest.class.getName());
	
	@BeforeClass(description="Prepare an organization to work with", alwaysRun=true)
	public void setUp_createOrg(){
		String uid = KatelloTestScript.getUniqueID();
		this.org_name = "auto-org-"+uid; 
		String org_descr = "Test Organization "+uid;
		try {
            servertasks.createOrganization(org_name, org_descr);
        } catch (KatelloApiException e) {
            Assert.fail("Could not create organization", e);
        }
	}
	
	// TODO: Move this method elsewhere. APITests perhaps? -bpc
	// Want to keep all JSON out of tests. Too fragile otherwise.
//	@Test (groups={"testEnvs"}, description="Existance of root env created by default", dependsOnMethods="test_createEnvironment_priorLocker")
//	public void test_existsDefaultRoot(){
//		org.json.simple.JSONObject tmpEnv;
//		org.json.simple.JSONArray json = KatelloTestScript.toJSONArr(KatelloApi.get(String.format(KatelloEnvironment.API_CMD_LIST, org_name)).getContent());
//		log.finer("Returned JSON for getEnvironments(): "+json.toJSONString());
//		org.json.simple.JSONObject json_org = KatelloTestScript.toJSONObj(KatelloApi.get(String.format(KatelloOrg.API_CMD_INFO, org_name)).getContent());
//		log.finer("Returned JSON for getOrganization(): "+json_org.toJSONString());
//		org.json.simple.JSONObject json_root = null;
//		for(int i=0;i<json.size();i++){
//			tmpEnv = (org.json.simple.JSONObject)json.get(i); 
//			if(tmpEnv.get("name").equals("root")){
//				json_root = tmpEnv;
//			}
//		}
//
//		Assert.assertNotNull(json, "JSON object returned should be: [!null]");
//		Assert.assertNull(json_root, "JSON root object parsed should be: [null]");
//		Assert.assertMore(json.size(), 0, "Should contain environments count: [>0]");
//	}
	
	@Test (groups={"testEnvs"}, description="List environments should be greater than 0")
	public void test_showEnvironments(){
		List<KatelloEnvironment> envs = null;
        try {
            envs = servertasks.getEnvironments(org_name);
        } catch (KatelloApiException e) {
            Assert.fail("Could not list environments", e);
        }
		log.finer(String.format("Returned environments count is: [%s]",envs.size()));
		Assert.assertMore(envs.size(), 0, "Should return environments >0");
	}
	
	@Test (groups={"testEnvs"}, description="Create an environment")
	public void test_createEnvironment_priorLocker(){
		String uid = KatelloTestScript.getUniqueID();
		this.env_name = "auto-env-"+uid; 
		String env_descr = "Test Environment "+uid;
		try {
		    servertasks.createEnvironment(org_name, env_name, env_descr, KatelloEnvironment.LIBRARY);
        } catch (KatelloApiException e) {
            Assert.fail("Could not create environment", e);
        }
		KatelloEnvironment env = servertasks.getEnvFromOrgList(this.org_name, this.env_name);
		Assert.assertNotNull(env,"Should be in envs. list of the organization");
	}
	
	@Test (groups={"testEnvs"}, description="List an environment", dependsOnMethods="test_createEnvironment_priorLocker")
	public void test_listEnvironment(){
        KatelloEnvironment env = servertasks.getEnvironment(this.org_name, this.env_name);
        Assert.assertEquals(this.env_name, env.getName(),"Check: name");
        KatelloOrg org = null;
        try {
            org = servertasks.getOrganization(this.org_name);
        } catch (KatelloApiException e) {
            Assert.fail("Could not get org info", e);
        }
        Assert.assertEquals(org.getId(), env.getOrganizationId(),"Check: organization_id");		
	}
	
	@Test (groups={"testEnvs"}, description="Update environment properties", dependsOnMethods="test_listEnvironment")
	public void test_updateEnvironment(){
		Date dupBefore, dupAfter;
		KatelloEnvironment updEnv = null;
		KatelloEnvironment env = servertasks.getEnvironment(this.org_name, this.env_name);
		try{			
			// update: description
			dupBefore = parseKatelloDate(env.getUpdatedAt());
			try {
                updEnv = servertasks.updateEnvProperty(this.org_name, this.env_name, "description", "Updated environment description");
            } catch (KatelloApiException e) {
                Assert.fail("Could not update environment property", e);
            }
			dupAfter = parseKatelloDate(updEnv.getUpdatedAt());
			Assert.assertEquals(updEnv.getDescription(), "Updated environment description","Check updated: description");
			Assert.assertMore(dupAfter.getTime(), dupBefore.getTime(), "Check the timestamp updated");

			// Create an env. that would be prior of our ones
			String uid = KatelloTestScript.getUniqueID();
			String env_prior = "dev-"+uid;
			try {
			    env = servertasks.createEnvironment(this.org_name, env_prior, "Prior: "+env_prior, KatelloEnvironment.LIBRARY);
            } catch (KatelloApiException e) {
                Assert.fail("Could not create environment", e);
            }
			KatelloEnvironment priorEnv = servertasks.getEnvironment(org_name, env_prior);
			dupBefore = dupAfter;
            try{Thread.sleep(1000);}catch(Exception ex){} // for the "update_at" checks
			try {
                updEnv = servertasks.updateEnvProperty(this.org_name,  this.env_name,  "prior", priorEnv.getId());
            } catch (KatelloApiException e) {
                Assert.fail("Could not update environment property", e);
            }
			Assert.assertEquals(updEnv.getPriorId(), priorEnv.getId(), "Check prior is "+env_prior);
		}catch(ParseException pex){
			log.severe(pex.getMessage());
		}
	}
	
	@Test (groups={"testEnvs"}, description="Remove an environment created")
	public void test_deleteEnvironment(){
		String uid = KatelloTestScript.getUniqueID();
		String env_name = "remove-env-"+uid; 
		String env_descr = "To Be Removed - "+uid;
		try {
		    servertasks.createEnvironment(this.org_name, env_name, env_descr, KatelloEnvironment.LIBRARY);
        } catch (KatelloApiException e) {
            Assert.fail("Failed to create environment", e);
        }
		String env_id = servertasks.getEnvironment(org_name, env_name).getId().toString();
		String res = null;
        try {
            res = servertasks.deleteEnvironment(this.org_name, env_name);
        } catch (KatelloApiException e) {
            Assert.fail("Could not delete environemnt", e);
        }
		Assert.assertEquals(res, "Deleted environment '"+env_id+"'","Check the text returned");
		List<KatelloEnvironment> envs = null;
        try {
            envs = servertasks.getEnvironments(this.org_name);
        } catch (KatelloApiException e) {
            Assert.fail("Could not list environments", e);
        }		
		Assert.assertEquals(envs.indexOf(env_name), -1,	String.format("Returned environment list does not contain: [%s]",env_name));
	}
	
}

