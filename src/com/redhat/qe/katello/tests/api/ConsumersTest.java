package com.redhat.qe.katello.tests.api;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.inject.Inject;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloApiException;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.katello.base.obj.KatelloEntitlement;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloPool;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.common.KatelloUtils;

@Test(groups={"cfse-api"})
public class ConsumersTest extends KatelloTestScript {
    @Inject Logger log;

	private String consumer_id = null;
	private String consumer_name = null;
	private String env_name = null;
	private String org_name = null;
	
	@BeforeClass(description="Prepare an organization to work with")
	public void setUp_createOrg() throws KatelloApiException {
		String uid = KatelloUtils.getUniqueID();
		org_name = "auto-org-"+uid; 
		String org_descr = "Test Organization "+uid;
		KatelloOrg org = servertasks.createOrganization(org_name, org_descr);
		System.out.println("\nName : "+org.name +"\n");
		// create an env.
		uid = KatelloUtils.getUniqueID();
		env_name = "auto-env-"+uid; 
		String env_descr = "Test Environment "+uid;
		servertasks.createEnvironment(org.getCpKey(), env_name, env_descr, KatelloEnvironment.LIBRARY);
	}

	
	@Test(groups = { "testConsumers" }, description = "Create consumer")
	public void test_createConsumer(){
		String pid = KatelloUtils.getUniqueID();
		this.consumer_name = "auto-"+pid+".yourorg.com";
		String uuid = KatelloUtils.getUUID();		
		KatelloSystem consumer = null;
        try {
            consumer = servertasks.createConsumer( org_name, consumer_name, uuid);
        } catch (KatelloApiException e) {
            Assert.fail("Could not create consumer", e);
        }
		this.consumer_id = consumer.getUuid();
		Assert.assertNotNull(consumer_id, "Check returned: uuid");
		Assert.assertEquals(consumer.getName(), consumer_name, "Check returned: name");
		log.info("Prepared a consumer: ["+consumer_id+"]");
	}
	
	@Test( groups = {"testConsumers"}, description = "Retrieve consumer" , dependsOnMethods = {"test_createConsumer"})
	public void test_getConsumer(){
		KatelloSystem consumer = null;
        try {
            consumer = servertasks.getConsumer(consumer_id);
        } catch (KatelloApiException e) {
            Assert.fail("Get customer failed", e);
        }
		Assert.assertEquals(consumer_id, consumer.getUuid(),"Check returned: uuid");
		
		KatelloEnvironment environment = servertasks.getEnvironment(org_name, env_name);
		Assert.assertEquals(consumer.getEnvironmentId(), environment.getId(),"Check returned: environment_id");
		
		Assert.assertEquals(org_name, consumer.getOwner().getDisplayName(),"Check returned: owner.displayName");
		Assert.assertEquals("/consumers/"+consumer_id, consumer.getHref(),"Check returned: href");
		Assert.assertEquals(consumer_name, consumer.getName(),"Check returned: name");
		Assert.assertEquals(consumer_name, consumer.getFacts().get("network.hostname"),"Check returned: facts.network.hostname");
		Assert.assertNotNull(consumer.getIdCert().getId(), "Check returned: idCert.id");
	}
	
	@Test( groups = {"testConsumers"}, description = "Update consumer details" , dependsOnMethods = {"test_createConsumer"})
	public void test_updateConsumer(){
		String upd_component,upd_value;
		
		upd_component = "uname.release";upd_value = "2.6.32-130.el6.i386";
		try {
            servertasks.updateFacts(consumer_id, upd_component, upd_value);
            Map<String,String> newFacts = servertasks.getConsumer(consumer_id).getFacts();
            Assert.assertEquals(upd_value, newFacts.get(upd_component),
                    "Check updated: facts."+upd_component);
            // TODO - check the update_at <> created_at 
            // recently failing due: https://bugzilla.redhat.com/show_bug.cgi?id=700821
        } catch (KatelloApiException e) {
            Assert.fail("Update consumer failed", e);
        }
	}
	
	@Test( groups = {"testConsumers"}, description = "Delete consumer", enabled=true)
	public void test_deleteConsumer(){
		String pid = KatelloUtils.getUniqueID();
		String cname = "auto-"+pid+".delete.me";
		String uuid = KatelloUtils.getUUID();		
		KatelloSystem consumer = null;
        try {
            consumer = servertasks.createConsumer(org_name, cname, uuid);
        } catch (KatelloApiException e) {
            Assert.fail("Could not create consumer", e);
        }
		String cid = consumer.getUuid();
		String ret = null;
        try {
            ret = servertasks.deleteConsumer(cid);
        } catch (KatelloApiException e) {
            Assert.fail("Could not delete consumer", e);
        }
        Assert.assertTrue(ret == null || ret.trim().isEmpty(), "Check returned string (empty)");
		
		try {
		    servertasks.getConsumer(cid);
		} catch (KatelloApiException e) {
		    // Expecting this exception.
		    Assert.assertTrue(e.getReturnCode() == 410, "Check API request to get consumer: ["+cid+"]");
//		    Assert.assertTrue(e.getMessage().contains("Consumer "+cid+" has been deleted"),
//				"Check API request to get consumer: ["+cid+"]");
		}
	}
	
	// TODO - re-enable once get fixed the bug: https://bugzilla.redhat.com/show_bug.cgi?id=721000
	// Don't forget the dependent test scenarios as well
	@Test(groups = {"testConsumers","rhsm-related"}, description = "Import export.zip manifest",enabled=false) // BZ: https://bugzilla.redhat.com/show_bug.cgi?id=721000
	public void test_importManifest(){
		String pid = KatelloUtils.getUniqueID();
		String prov_MF = "ExpMan_"+pid;
		// TODO More investigation of the below. Still needed? 
//		try {
//            servertasks.createProvider(
//            		org_name, prov_MF, "Provider for importing export.zip",
//            		"Red Hat","https://cdn.redhat.com ");
//        } catch (KatelloApiException e) {
//            Assert.fail("Could not create provider", e);
//        } // the URL here plays no role here. It's just a mandatory field~
		log.info("Check if there was another import of export.zip before.");
		try {
            if(servertasks.getProductByOrg(org_name, AWESOME_SERVER_BASIC)==null){ // there is no export.zip processed, FINE~
            	Long provider_id = servertasks.getProvider(org_name, prov_MF).getId();
            	int prods_before = servertasks.getProductsByOrg(org_name).size();
                String ret = servertasks.uploadManifest(provider_id, EXPORT_ZIP_PATH);
                int prods_after = servertasks.getProductsByOrg(org_name).size();
                Assert.assertEquals(ret, KatelloProvider.OUT_MANIFEST_IMPORTED,"Output should be: \"Manifest imported\"");
                Assert.assertTrue((prods_after-prods_before)>=PRODUCTS_IN_EXPORT_ZIP, "Check imported products: >=["+PRODUCTS_IN_EXPORT_ZIP+"]");
            }else{
            	log.warning("Skip running of the test: test_importManifest. Cleanup DBs for this test run." );
            }
        } catch (KatelloApiException e) {
            Assert.fail("API call failed", e);
        }
	}
	
	@Test(dependsOnMethods={"test_createConsumer","test_importManifest"},
			description="Subscribe consumer system to a pool", enabled=false)
	public void test_subscribeConsumer(){
	    String pool_id = null;
        try {
            pool_id = servertasks.getPool(AWESOME_SERVER_BASIC);
        } catch (KatelloApiException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
		List<KatelloEntitlement> entitlements = null;
        try {
            entitlements = servertasks.subscribeConsumerWithPool(consumer_id, pool_id);
        } catch (KatelloApiException e) {
            Assert.fail("Could not subscribe consumer", e);
        }
		KatelloEntitlement entitlement = entitlements.get(0);
		String entitlement_id = entitlement.getId();
		String certificate_id = entitlement.getCertificates().get(0).getId();
		Assert.assertEquals(entitlement.getPool().getId(), pool_id,"Check returned pool id");
		Assert.assertNotNull(entitlement_id, "Check returned entitlement id");
		Assert.assertNotNull(certificate_id, "Check returned certificate id");
	}
	
	@Test(dependsOnMethods={"test_subscribeConsumer"},
			description="Re-subscribe consumer system to a pool", enabled=false)
	public void test_resubscribeToPool(){
		String pool_id = null;
        try {
            pool_id = servertasks.getPool(AWESOME_SERVER_BASIC);
        } catch (KatelloApiException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        try {
            servertasks.subscribeConsumerWithPool(consumer_id, pool_id);
        } catch (KatelloApiException e) {
            Assert.assertEquals(e.getMessage(), "{\"displayMessage\":\"" +
                    "This consumer is already subscribed to the product matching pool " +
                    "with id '"+pool_id+"'\"}");
        }
	}
	
	@Test(description="Unsubscribe consumer from the pool just subscribed", enabled=false)
	public void test_unsubscribeConsumer(){
		String pid = KatelloUtils.getUniqueID();
		String cname = "auto-"+pid+".unsubscribed";
		String uuid = KatelloUtils.getUUID();		
		KatelloSystem consumer = null;
        try {
            consumer = servertasks.createConsumer(org_name, cname, uuid);
        } catch (KatelloApiException e) {
            Assert.fail("Could not create consumer", e);
        }
		String cid = consumer.getUuid();
		String pool_id = null;
        try {
            pool_id = servertasks.getPool(AWESOME_SERVER_BASIC);
        } catch (KatelloApiException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
		List<Long> serials = null;
		try {
            servertasks.subscribeConsumerWithPool(cid, pool_id);
            serials = servertasks.getSerials(cid);
        } catch (KatelloApiException e) {
            Assert.fail("API operation failed", e);
        } // Hope the 100 subscriptions did not get all consumed :P
		Assert.assertMore(serials.size(), 0, "Serials count: >0");
		
		Long serial_id1 = serials.get(0);
		try {
            servertasks.unsubscribeConsumer(cid, serial_id1.toString());
        } catch (KatelloApiException e) {
            Assert.fail("Could not unsubscribe consumer", e);
        } // returns nothing
		try {
            serials = servertasks.getSerials(cid);
        } catch (KatelloApiException e) {
            Assert.fail("Could not get serials", e);
        }
		Assert.assertEquals(serials.size(), 0,"Check no serials available: no subscriptions");
	}
	
	@Test(description="Unsubscribe consumer from the all pools being subscribed", enabled=false)
	public void test_unsubscribeConsumerAll(){
		String cname = "auto-"+KatelloUtils.getUniqueID()+".unsubscribe.all";
		KatelloSystem consumer = null;
        try {
            consumer = servertasks.createConsumer(org_name, cname, KatelloUtils.getUUID());
        } catch (KatelloApiException e) {
            Assert.fail("Could not create consumer", e);
        }
		String consumer_id = consumer.getUuid();
		List<KatelloPool> pools = null;
        try {
            pools = servertasks.getPools();
        } catch (KatelloApiException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
		for ( KatelloPool pool : pools ) {
		    if ( pool.getOwner().getDisplayName().equals(org_name)) {
		        try {
		            servertasks.subscribeConsumerWithPool(consumer_id,  pool.getId());
		        } catch (KatelloApiException ex){} // we don't care if some subscriptions would fail
		    }
		}
		/* at this step we should have the consumer applied to all of the subscriptions 
		 available for org = org_name (i.e.: ACME_Corporation) */
		List<Long> serials = null;
        try {
            serials = servertasks.getSerials(consumer_id);
        } catch (KatelloApiException e) {
            Assert.fail("Could not get serials", e);
        }
		Assert.assertMore(serials.size(), 1, "Check: subscriptions should be >1");
		
		// unsubscribe all
		try {
            servertasks.unsubscribeConsumer(consumer_id);
        } catch (KatelloApiException e) {
            Assert.fail("Could not unsubscribe consumer", e);
        }
		try {
            serials = servertasks.getSerials(consumer_id);
        } catch (KatelloApiException e) {
            Assert.fail("Could not get serials", e);
        }
		Assert.assertEquals(serials.size(), 0, "Check: subscriptions should be ==0");
	}
	
  
}
