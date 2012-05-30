package com.redhat.qe.katello.tests.api;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.auto.testng.Assert;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.common.KatelloConstants;

@Test(groups={"cfse-api"})
public class ProvidersTest extends KatelloTestScript {
	protected static Logger log = Logger.getLogger(ProvidersTest.class.getName());
	
	private String org_name;
	private String provider_name;

	@BeforeClass(description="Prepare an organization to work with")
	public void setUp_createOrg(){
		this.org_name = default_org;
	}

	@Test(groups = { "testProviders" }, description = "Create provider")
	public void test_createProvider() {
		
		String uid = getUniqueID();
		this.provider_name = "auto-provider-"+uid;
		String str_json = servertasks.createProvider(
				this.org_name,provider_name, "Provider in test - "+uid,"Custom");
		JSONObject json_prov = KatelloTestScript.toJSONObj(str_json);
		Assert.assertNotNull(json_prov, "Returned string in katello is JSON-formatted");
		
		// ASSERTIONS - katello
		Assert.assertEquals(json_prov.get("name"), 
				provider_name, 
				"Katello - Check provider: name");
		Assert.assertEquals(json_prov.get("description"), 
				"Provider in test - "+uid, 
				"Katello - Check provider: description");
		Assert.assertEquals(json_prov.get("provider_type"), 
				"Custom",
				"Katello - Check provider: provider_type");
		JSONObject json_org = KatelloTestScript.toJSONObj(new KatelloOrg(org_name, null).api_info().getStdout());
		Assert.assertEquals(json_prov.get("organization_id"), 
				json_org.get("id"),
				"Katello - Check provider: organization_id");
	}
	
	@Test (groups={"testProviders"}, description="Import Products", 
			dependsOnMethods="test_createProvider", enabled = false) // Seems moved to another controller.
	public void test_importProducts(){
		// Read data/products.json file. Needs to get replaced by actual values. 
		String sProducts="{}";
		try{
			BufferedReader br = new BufferedReader(new FileReader("data/product.json"));
			sProducts=br.readLine();
			br.close();
		}catch(IOException iex){
			log.severe(iex.getMessage());
			throw new RuntimeException(iex);
		}
		// Replace the values in products.json
		String pid = KatelloTestScript.getUniqueID();
		try{Thread.sleep(1000);}catch(InterruptedException ex){}
		String cid = KatelloTestScript.getUniqueID();
		String repoUrl=KatelloConstants.KATELLO_SMALL_REPO;
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.000+0000'");
		String sTS = df.format(Calendar.getInstance().getTime());
		sProducts = sProducts.replaceAll("\\$\\{product_id\\}", pid);
		sProducts = sProducts.replaceAll("\\$\\{content_id\\}", cid);
		sProducts = sProducts.replaceAll("\\$\\{content_url\\}", repoUrl);
		sProducts = sProducts.replaceAll("\\$\\{product_create_ts\\}", sTS);
		log.finest("Replaced data/products.json: ["+sProducts+"]");
		JSONObject json_prov=servertasks.getProvider(org_name, this.provider_name);
		String prov_id = ((Long)json_prov.get("id")).toString();
		String s = servertasks.import_products(prov_id, sProducts);
//		Assert.assertEquals(s.startsWith("{\"name\":"), true,"Returned output should start with: {\"name\":");
		Assert.assertEquals(s.equals("[true]"), true,"Returned output should be: [true]");
	}
	
	@Test (groups={"testProviders"}, description="Update Provider Properties", dependsOnMethods="test_createProvider")
	public void test_updateProvider(){
		Date dupBefore, dupAfter;
		JSONObject json_updProv = servertasks.getProvider(org_name, provider_name);
		String upd_repo_url = "https://localhost";
		try{
			dupBefore = parseKatelloDate((String)json_updProv.get("updated_at"));
			// update - name
			json_updProv = updateProviderProperty("name", "modified-"+provider_name);
			dupAfter = parseKatelloDate((String)json_updProv.get("updated_at"));
			Assert.assertEquals(json_updProv.get("name"), "modified-"+this.provider_name,"Check updated: name");
			Assert.assertMore(dupAfter.getTime(), dupBefore.getTime(), "Check the timestamp updated");
			this.provider_name = "modified-"+this.provider_name;
			
			//update - repository_url
			dupBefore = dupAfter;
			json_updProv = updateProviderProperty("repository_url", upd_repo_url);
			dupAfter = parseKatelloDate((String)json_updProv.get("updated_at"));
			Assert.assertEquals(json_updProv.get("repository_url"), upd_repo_url,"Check updated: repository_url");
			Assert.assertMore(dupAfter.getTime(), dupBefore.getTime(), "Check the timestamp updated");

			//update - description
			dupBefore = dupAfter;
			json_updProv = updateProviderProperty("description", "Updated: provider ["+provider_name+"]");
			dupAfter = parseKatelloDate((String)json_updProv.get("updated_at"));
			Assert.assertEquals(json_updProv.get("description"), "Updated: provider ["+provider_name+"]","Check updated: description");
			Assert.assertMore(dupAfter.getTime(), dupBefore.getTime(), "Check the timestamp updated");
			// TODO - needs to be applied with additional tests for provider type-RedHat, as only 1 provider of RedHat type could be in org.
		}catch(ParseException pex){
			log.severe(pex.getMessage());
		}
	}
	
	@Test (groups={"testProviders"}, description="List all providers", dependsOnMethods="test_updateProvider")
	public void test_listProviders(){
		// Get providers json string
		String s_json_provs = new KatelloProvider(null, org_name, null, null).api_list(org_name).getStdout();
		JSONArray arr_provs = KatelloTestScript.toJSONArr(s_json_provs) ;
		Assert.assertMore(arr_provs.size(), 0, "Check: providers count >0");
		JSONObject json_prov;
		// we need to find our provider modified (see the test dependency)
		boolean findOurProvider = false;
		for(int i=0;i<arr_provs.size();i++){
			json_prov = (JSONObject)arr_provs.get(i);
			if(json_prov.get("name").equals(this.provider_name))
				findOurProvider = true;
		}
		// We have to have the provider found, else: error.
		Assert.assertTrue(findOurProvider, "Check: we found our provider");
	}
	
	@Test (groups={"testProviders"}, description="Delete provider",
			enabled=true) // BZ: https://bugzilla.redhat.com/show_bug.cgi?id=700423
	public void test_deleteProvider(){
		// Create separate provider to be removed 
		String uid = getUniqueID();
		String providerName = "auto-deleteMe-"+uid;
		String str_json = servertasks.createProvider(
				this.org_name,providerName, "Provider in test - "+uid,"Custom");		
		JSONObject json_prov = KatelloTestScript.toJSONObj(str_json);
		Assert.assertNotNull(json_prov, "Returned string in katello is JSON-formatted");
		
		String sout = servertasks.deleteProvider(org_name, providerName).trim();
		Assert.assertEquals(sout, "Deleted provider [ "+providerName+" ]","Check: message returned by the API call");
		JSONObject obj_del = servertasks.getProvider(org_name, providerName);
		Assert.assertNull(obj_del, "Check: returned getProvider() is null");
	}
	
	@Test(groups={"testOrgs","testProviders"}, description="Generate ",enabled= false)
	public void test_postUeberCert(){ // TODO - seems Candlepin not have them still rpm-ed.
		try{
			String _return = servertasks.apiKatello_POST("", "/organizations/"+"org1"+"/uebercert");
			log.info("Posting ueber cert for: ["+"org1"+"]");
			System.out.println(_return);
		}catch (Exception e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
		
	}
	
	private JSONObject updateProviderProperty(String component, String updValue){
		JSONObject _return = null; String retStr;
		String updProv = String.format("'provider':{'%s':'%s'}",
				component,updValue);
		String provider_id = ((Long)servertasks.getProvider(org_name, this.provider_name).get("id")).toString();
		try{Thread.sleep(1000);}catch(Exception ex){} // for the "update_at" checks
		try{
			retStr = servertasks.apiKatello_PUT(updProv,String.format(
					"/providers/%s",provider_id));
			_return = KatelloTestScript.toJSONObj(retStr);
		}catch(IOException ie){
			log.severe(ie.getMessage());
		}
		return _return;
	}

	
}
