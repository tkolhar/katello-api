package com.redhat.qe.katello.tests.api;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.redhat.qe.auto.testng.Assert;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.katello.base.cli.KatelloEnvironment;

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
		servertasks.createOrganization(this.org_name, org_descr);
	}
	
	@Test (groups={"testEnvs"}, description="Existance of root env created by default", dependsOnMethods="test_createEnvironment_priorLocker")
	public void test_existsDefaultRoot(){
		JSONObject tmpEnv;
		String envs_json = servertasks.getEnvironments(this.org_name);
		JSONArray json = toJSONArr(envs_json);
		log.finer("Returned JSON for getEnvironments(): "+json.toJSONString());
		JSONObject json_org = servertasks.getOrganization(this.org_name);;
		log.finer("Returned JSON for getOrganization(): "+json_org.toJSONString());
		JSONObject json_root = null;
		for(int i=0;i<json.size();i++){
			tmpEnv = (JSONObject)json.get(i); 
			if(tmpEnv.get("name").equals("root")){
				json_root = tmpEnv;
			}
		}

		Assert.assertNotNull(json, "JSON object returned should be: [!null]");
		Assert.assertNull(json_root, "JSON root object parsed should be: [null]");
		Assert.assertMore(json.size(), 0, "Should contain environments count: [>0]");
	}
	
	@Test (groups={"testEnvs"}, description="List environments should be greater than 0")
	public void test_showEnvironments(){
		JSONArray json_envs = toJSONArr(servertasks.getEnvironments(this.org_name));
		log.finer(String.format("Returned environments count is: [%s]",json_envs.size()));
		Assert.assertMore(json_envs.size(), 0, "Should return environments >0");
	}
	
	@Test (groups={"testEnvs"}, description="Create an environment")
	public void test_createEnvironment_priorLocker(){
		String uid = KatelloTestScript.getUniqueID();
		this.env_name = "auto-env-"+uid; 
		String env_descr = "Test Environment "+uid;
		servertasks.createEnvironment(this.org_name, this.env_name, env_descr,KatelloEnvironment.LIBRARY);
		JSONObject json_env = servertasks.getEnvFromOrgList(this.org_name, this.env_name);
		Assert.assertNotNull(json_env,"Should be in envs. list of the organization");
	}
	
	@Test (groups={"testEnvs"}, description="List an environment", dependsOnMethods="test_createEnvironment_priorLocker")
	public void test_listEnvironment(){
        JSONObject json_env = servertasks.getEnvironment(this.org_name, this.env_name);
        Assert.assertEquals(this.env_name, json_env.get("name"),"Check: name");
        JSONObject json_org = servertasks.getOrganization(this.org_name);
        Assert.assertEquals(json_org.get("id"), json_env.get("organization_id"),"Check: organization_id");		
	}
	
	@Test (groups={"testEnvs"}, description="Update environment properties", dependsOnMethods="test_listEnvironment")
	public void test_updateEnvironment(){
		Date dupBefore, dupAfter;
		JSONObject json_updEnv;
		JSONObject json_env = servertasks.getEnvironment(this.org_name, this.env_name);
		try{			
			// update: description
			dupBefore = parseKatelloDate((String)json_env.get("updated_at"));
			json_updEnv = this.updateEnvProperty("description", "Updated environment description");
			dupAfter = parseKatelloDate((String)json_updEnv.get("updated_at"));
			Assert.assertEquals(json_updEnv.get("description"), "Updated environment description","Check updated: description");
			Assert.assertMore(dupAfter.getTime(), dupBefore.getTime(), "Check the timestamp updated");

			// Create an env. that would be prior of our ones
			String uid = KatelloTestScript.getUniqueID();
			String env_prior = "dev-"+uid;
			servertasks.createEnvironment(this.org_name, env_prior, "Prior: "+env_prior);
			
			JSONObject json_prior = servertasks.getEnvironment(org_name, env_prior);
			dupBefore = dupAfter;
			String updEnv = String.format("'environment':{'prior':'%s'}",((Long)json_prior.get("id")).toString());
			try{Thread.sleep(1000);}catch(Exception ex){} // for the "update_at" checks
			String env_id = ((Long)servertasks.getEnvironment(org_name, env_name).get("id")).toString();
			try{
				String res = servertasks.apiKatello_PUT(updEnv,String.format(
						"/organizations/%s/environments/%s",this.org_name,env_id));
				json_updEnv = KatelloTestScript.toJSONObj(res);
				Assert.assertEquals(json_updEnv.get("prior"), 
						env_prior, "Check prior is "+env_prior);
			}catch(IOException ie){
				log.severe(ie.getMessage());
			}
		}catch(ParseException pex){
			log.severe(pex.getMessage());
		}
	}
	
	@Test (groups={"testEnvs"}, description="Remove an environment created")
	public void test_deleteEnvironment(){
		String uid = KatelloTestScript.getUniqueID();
		String env_name = "remove-env-"+uid; 
		String env_descr = "To Be Removed - "+uid;
		servertasks.createEnvironment(this.org_name, env_name, env_descr);
		String env_id = ((Long)servertasks.getEnvironment(org_name, env_name).get("id")).toString();
		String res = servertasks.deleteEnvironment(this.org_name, env_name);
		Assert.assertEquals(res, "Deleted environment '"+env_id+"'","Check the text returned");
		String nil = servertasks.getEnvironments(org_name);
		Assert.assertEquals(nil.indexOf(env_name), -1,
				String.format("Returned environment list does not contain: [%s]",env_name));
	}
	
	private JSONObject updateEnvProperty(String component, String updValue){
		JSONObject _return = null; String retStr;
		String updEnv = String.format("'environment':{'%s':'%s'}",
				component,updValue);
		String env_id = ((Long)servertasks.getEnvironment(org_name, env_name).get("id")).toString();
		try{Thread.sleep(1000);}catch(Exception ex){} // for the "update_at" checks
		try{
			retStr = servertasks.apiKatello_PUT(updEnv,String.format(
					"/organizations/%s/environments/%s",this.org_name,env_id));
			_return = KatelloTestScript.toJSONObj(retStr);
		}catch(IOException ie){
			log.severe(ie.getMessage());
		}
		return _return;
	}
}

