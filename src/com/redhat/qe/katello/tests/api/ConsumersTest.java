package com.redhat.qe.katello.tests.api;

import java.io.IOException;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import com.redhat.qe.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloMisc;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloSystem;

@Test(groups={"cfse-api"})
public class ConsumersTest extends KatelloTestScript {
	protected static Logger log = Logger.getLogger(ConsumersTest.class.getName());

	private String consumer_id = null;
	private String consumer_name = null;
	private String env_name = null;
	private String org_name = null;
	
	@BeforeClass(description="Prepare an organization to work with")
	public void setUp_createOrg(){
		String uid = KatelloTestScript.getUniqueID();
		org_name = "auto-org-"+uid; 
		String org_descr = "Test Organization "+uid;
		KatelloOrg org = new KatelloOrg(org_name, org_descr);
		org.api_create();
		// create an env.
		uid = KatelloTestScript.getUniqueID();
		env_name = "auto-env-"+uid; 
		String env_descr = "Test Environment "+uid;
		KatelloEnvironment env = new KatelloEnvironment(env_name, env_descr, org_name, KatelloEnvironment.LIBRARY);
		env.api_create();
	}

	@Test(groups = { "testConsumers" }, description = "Create consumer")
	public void test_createConsumer(){
		String pid = KatelloTestScript.getUniqueID();
		this.consumer_name = "auto-"+pid+".yourorg.com";
		String uuid = KatelloTestScript.getUUID();		
		String s = servertasks.createConsumer( org_name, consumer_name, uuid, "data/facts-virt.json");
		JSONObject jcons = KatelloTestScript.toJSONObj(s);
		this.consumer_id = (String)jcons.get("uuid");
		Assert.assertNotNull(consumer_id, "Check returned: uuid");
		Assert.assertEquals(((String)jcons.get("name")), consumer_name, "Check returned: name");
		log.info("Prepared a consumer: ["+consumer_id+"]");
	}
	
	@Test( groups = {"testConsumers"}, description = "Retrieve consumer" , dependsOnMethods = {"test_createConsumer"})
	public void test_getConsumer(){
		JSONObject jcons = KatelloTestScript.toJSONObj(
				new KatelloSystem(null, null, null).api_info(consumer_id).getStdout());
		Assert.assertEquals(consumer_id, (String)jcons.get("uuid"),"Check returned: uuid");
		
		JSONObject json_env = servertasks.getEnvironment(org_name, env_name);
		Assert.assertEquals((Long)json_env.get("id"), (Long)jcons.get("environment_id"),"Check returned: environment_id");
		
		Assert.assertEquals(org_name, (String)((JSONObject)jcons.get("owner")).get("displayName"),"Check returned: owner.displayName");
		Assert.assertEquals("/consumers/"+consumer_id, (String)jcons.get("href"),"Check returned: href");
		Assert.assertEquals(consumer_name, (String)jcons.get("name"),"Check returned: name");
		Assert.assertEquals(consumer_name, (String)((JSONObject)jcons.get("facts")).get("network.hostname"),"Check returned: facts.network.hostname");
		Assert.assertNotNull(((JSONObject)jcons.get("idCert")).get("id"), "Check returned: idCert.id");
	}
	
	@Test( groups = {"testConsumers"}, description = "Update consumer details" , dependsOnMethods = {"test_createConsumer"})
	public void test_updateConsumer(){
		String upd_component,upd_value;
		
		upd_component = "uname.release";upd_value = "2.6.32-130.el6.i386";
		updateFacts(upd_component, upd_value);
		JSONObject jcons = KatelloTestScript.toJSONObj(
				new KatelloSystem(null, null, null).api_info(consumer_id).getStdout());
		Assert.assertEquals(upd_value, (String)((JSONObject)jcons.get("facts")).get(upd_component),
				"Check updated: facts."+upd_component);
		// TODO - check the update_at <> created_at 
		// recently failing due: https://bugzilla.redhat.com/show_bug.cgi?id=700821
	}
	
	@SuppressWarnings("unchecked")
	private JSONObject updateFacts(String component, String updValue){
		JSONObject _return = null; String retStr;
		// Get facts object to modify it and update again :)
		JSONObject jfacts = (JSONObject)KatelloTestScript.toJSONObj(
				new KatelloSystem(null, null, null).api_info(consumer_id).getStdout()).get("facts");
		jfacts.put(component, updValue); // <--- suppress warnings are for me
		String updConsumer = jfacts.toJSONString().replaceAll("\"", "'");
		updConsumer = "{'facts':"+updConsumer+"}";
		try{Thread.sleep(1000);}catch(Exception ex){} // for the "update_at" checks
		try{
			log.finest(String.format("Update consumer: [%s] facts with: [%s=%s]",
					consumer_id,component,updValue));
			retStr = servertasks.apiKatello_PUT(updConsumer,String.format(
					"/consumers/%s",consumer_id));
			_return = KatelloTestScript.toJSONObj(retStr);
		}catch(IOException ie){
			log.severe(ie.getMessage());
		}
		return _return;
	}

	@Test( groups = {"testConsumers"}, description = "Delete consumer", enabled=true)
	public void test_deleteConsumer(){
		String pid = KatelloTestScript.getUniqueID();
		String cname = "auto-"+pid+".delete.me";
		String uuid = KatelloTestScript.getUUID();		
		String s = servertasks.createConsumer(org_name, cname, uuid, "data/facts-virt.json");
		JSONObject jcons = KatelloTestScript.toJSONObj(s);
		String cid = (String)jcons.get("uuid");
		String ret = servertasks.deleteConsumer(cid);
		Assert.assertEquals("", ret,"Check returned string (empty)");
		
		String sCons = new KatelloSystem(null, null, null).api_info(cid).getStdout(); // try to request the removed consumer
		Assert.assertTrue(sCons.contains("Consumer "+cid+" has been deleted"),
				"Check API request to get consumer: ["+cid+"]");
	}
	
	// TODO - re-enable once get fixed the bug: https://bugzilla.redhat.com/show_bug.cgi?id=721000
	// Don't forget the dependent test scenarios as well
	@Test(groups = {"testConsumers","rhsm-related"}, description = "Import export.zip manifest",
			enabled=false) // BZ: https://bugzilla.redhat.com/show_bug.cgi?id=721000
	public void test_importManifest(){
		String pid = KatelloTestScript.getUniqueID();
		String prov_MF = "ExpMan_"+pid;
		servertasks.createProvider(
				org_name, prov_MF, "Provider for importing export.zip",
				"Red Hat","https://cdn.redhat.com "); // the URL here plays no role here. It's just a mandatory field~
		log.info("Check if there was another import of export.zip before.");
		if(servertasks.getProductByOrg(org_name, AWESOME_SERVER_BASIC)==null){ // there is no export.zip processed, FINE~
			try{
				String provider_id = ((Long)servertasks.getProvider(org_name, prov_MF).get("id")).toString();
				KatelloProduct prod = new KatelloProduct(null, org_name, null, null, null, null, null, null);
				int prods_before = ((JSONArray)KatelloTestScript.toJSONArr(prod.api_list().getStdout())).size();
				String ret = servertasks.apiKatello_POST_manifest(EXPORT_ZIP_PATH, "/providers/"+provider_id+"/import_manifest");
				int prods_after = ((JSONArray)KatelloTestScript.toJSONArr(prod.api_list().getStdout())).size();
				Assert.assertEquals(ret, "Manifest imported","Output should be: \"Manifest imported\"");
				Assert.assertTrue((prods_after-prods_before)>=PRODUCTS_IN_EXPORT_ZIP, "Check imported products: >=["+PRODUCTS_IN_EXPORT_ZIP+"]");
			}catch(IOException ie){
				log.severe(ie.getMessage());
			}
		}else{
			log.warning("Skip running of the test: test_importManifest. Cleanup DBs for this test run." );
		}
	}
	
	@Test(dependsOnMethods={"test_createConsumer","test_importManifest"},
			description="Subscribe consumer system to a pool", enabled=false)
	public void test_subscribeConsumer(){
		JSONObject jpool = new KatelloMisc().api_getPoolByProduct(AWESOME_SERVER_BASIC);
		String pool_id = (String)jpool.get("id");
		String ret = servertasks.subscribeConsumer(consumer_id, pool_id);
		JSONObject jentl = (JSONObject)KatelloTestScript.toJSONArr(ret).get(0);
		String entitlement_id = (String)jentl.get("id");
		String certificate_id = (String)((JSONObject)((JSONArray)jentl.get("certificates")).get(0)).get("id");
		Assert.assertEquals((String)((JSONObject)jentl.get("pool")).get("id"), pool_id,"Check returned pool id");
		Assert.assertNotNull(entitlement_id, "Check returned entitlement id");
		Assert.assertNotNull(certificate_id, "Check returned certificate id");
	}
	
	@Test(dependsOnMethods={"test_subscribeConsumer"},
			description="Re-subscribe consumer system to a pool", enabled=false)
	public void test_resubscribeToPool(){
		JSONObject jpool = new KatelloMisc().api_getPoolByProduct(AWESOME_SERVER_BASIC);
		String pool_id = (String)jpool.get("id");
		String ret = servertasks.subscribeConsumer(consumer_id, pool_id);
		Assert.assertEquals(ret, "{\"displayMessage\":\"" +
				"This consumer is already subscribed to the product matching pool " +
				"with id '"+pool_id+"'\"}");
	}
	
	@Test(description="Unsubscribe consumer from the pool just subscribed", enabled=false)
	public void test_unsubscribeConsumer(){
		String pid = KatelloTestScript.getUniqueID();
		String cname = "auto-"+pid+".unsubscribed";
		String uuid = KatelloTestScript.getUUID();		
		String s = servertasks.createConsumer(org_name, cname, uuid, "data/facts-virt.json");
		JSONObject jcons = KatelloTestScript.toJSONObj(s);
		String cid = (String)jcons.get("uuid");
		JSONObject jpool = new KatelloMisc().api_getPoolByProduct(AWESOME_SERVER_BASIC);
		String pool_id = (String)jpool.get("id");
		servertasks.subscribeConsumer(cid, pool_id); // Hope the 100 subscriptions did not get all consumed :P
		JSONArray jserials = KatelloTestScript.toJSONArr(
				new KatelloSystem(null, null, null).api_getSerials(cid).getStdout());
		Assert.assertMore(jserials.size(), 0, "Serials count: >0");
		
		Long serial_id1 = (Long)((JSONObject)jserials.get(0)).get("serial");
		servertasks.unsubscribeConsumer(cid, serial_id1.toString()); // returns nothing
		jserials = KatelloTestScript.toJSONArr(
				new KatelloSystem(null, null, null).api_getSerials(cid).getStdout());
		Assert.assertEquals(jserials.size(), 0,"Check no serials available: no subscriptions");
	}
	
	@Test(description="Unsubscribe consumer from the all pools being subscribed", enabled=false)
	public void test_unsubscribeConsumerAll(){
		JSONObject pool;
		
		String cname = "auto-"+KatelloTestScript.getUniqueID()+".unsubscribe.all";
		String s = servertasks.createConsumer(org_name, cname, KatelloTestScript.getUUID(), "data/facts-virt.json");
		String consumer_id = (String)KatelloTestScript.toJSONObj(s).get("uuid");
		JSONArray pools = KatelloTestScript.toJSONArr(new KatelloMisc().api_getPools().getStdout());
		for(int i=0;i<pools.size();i++){
			pool = (JSONObject)pools.get(i);
			if(((String)((JSONObject)pool.get("owner")).get("displayName")).equals(org_name)){
				try{
					servertasks.subscribeConsumer(consumer_id, (String)pool.get("id"));
				}catch(Exception ex){}// we don't care if some subscriptions would fail :)
			}
		}
		/* at this step we should have the consumer applied to all of the subscriptions 
		 available for org = org_name (i.e.: ACME_Corporation) */
		JSONArray jserials = KatelloTestScript.toJSONArr(
				new KatelloSystem(null, null, null).api_getSerials(consumer_id).getStdout());
		Assert.assertMore(jserials.size(), 1, "Check: subscriptions should be >1");
		
		// unsubscribe all
		servertasks.unsubscribeConsumer(consumer_id);
		jserials = KatelloTestScript.toJSONArr(
				new KatelloSystem(null, null, null).api_getSerials(consumer_id).getStdout());
		Assert.assertEquals(jserials.size(), 0, "Check: subscriptions should be ==0");
	}
	
}
