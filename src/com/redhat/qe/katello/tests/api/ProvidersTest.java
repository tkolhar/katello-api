package com.redhat.qe.katello.tests.api;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloApiException;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.common.KatelloConstants;
import com.redhat.qe.katello.common.KatelloUtils;

@Test(groups={"cfse-api"})
public class ProvidersTest extends KatelloTestScript {
    protected static Logger log = Logger.getLogger(ProvidersTest.class.getName());
	
	private String org_name;
	private String provider_name;

	public ProvidersTest(){
		super();
	}
	@BeforeClass(description="Prepare an organization to work with")
	public void setUp_createOrg(){
		try {
			this.org_name = getDefaultOrg();
		} catch (KatelloApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test(groups = { "testProviders" }, description = "Create provider")
	public void test_createProvider() {
		
		String uid = KatelloUtils.getUniqueID();
		this.provider_name = "auto-provider-"+uid;
		KatelloProvider prov = null;
        try {
            prov = servertasks.createProvider(this.org_name,provider_name, "Provider in test - "+uid,"Custom", null);
        } catch (KatelloApiException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
//		Assert.assertNotNull(json_prov, "Returned string in katello is JSON-formatted");
		
		// ASSERTIONS - katello
		Assert.assertEquals(prov.getName(), 
				provider_name, 
				"Katello - Check provider: name");
		Assert.assertEquals(prov.getDescription(), 
				"Provider in test - "+uid, 
				"Katello - Check provider: description");
		Assert.assertEquals(prov.getProviderType(), 
				"Custom",
				"Katello - Check provider: provider_type");
		KatelloOrg org = null;
        try {
            org = servertasks.getOrganization(org_name);
        } catch (KatelloApiException e) {
            // TODO Auto-generated catch block
            Assert.fail("Could not get org info", e);
        }
		Assert.assertEquals(prov.getOrganizationId(), 
				org.getId(),
				"Katello - Check provider: organization_id");
	}
	
	@Test (groups={"testProviders"}, description="Import Products", 
			dependsOnMethods="test_createProvider", enabled = false) // Seems moved to another controller.
	public void test_importProducts(){
		String pid = KatelloUtils.getUniqueID();
		try{Thread.sleep(1000);}catch(InterruptedException ex){}
		String cid = KatelloUtils.getUniqueID();
		String repoUrl=KatelloConstants.KATELLO_SMALL_REPO;
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.000+0000'");
		String sTS = df.format(Calendar.getInstance().getTime());

		Map<String,Object> allProducts = new HashMap<String,Object>();
		Set<Map<String,Object>> products = new HashSet<Map<String,Object>>();
		allProducts.put("products", products);
		Map<String,Object> product = new HashMap<String,Object>();
		product.put("updated", sTS);
		product.put("name", "Test Product - " + pid);
		product.put("created", sTS);
		product.put("href", "/products/" + pid);
		Set<Map<String,Object>> productContentSet = new HashSet<Map<String,Object>>();
		Map<String,Object> productContent = new HashMap<String,Object>();
		Map<String,Object> content = new HashMap<String,Object>();
		content.put("contentUrl", repoUrl);
		content.put("updated", sTS);
		content.put("vendor", "redhat");
		content.put("name", "Test Content - " + cid);
		content.put("created", sTS);
		content.put("label", "test-label-" + cid);
		content.put("gpgUrl", "/some/gpg/url/");
		content.put("type", "yum");
		content.put("id", cid);
		productContent.put("content", content);
		productContent.put("physicalEntitlement", Long.valueOf(0));
		productContent.put("enabled", Boolean.TRUE);
		productContent.put("flexEntitlement", Long.valueOf(0));
		productContentSet.add(productContent);
		product.put("productContent", productContent);
		product.put("multiplier", Long.valueOf(1));
		Set<Map<String,Object>> attributes = new HashSet<Map<String,Object>>();
		String[] attrs = new String[] { "version:1.0", "variant:ALL", "sockets:2", "arch:ALL", "type:SVC" };
		for ( String attr : attrs ) {
		    String[] nameValue = attr.split(":");
		    Map<String,Object> attrMap = new HashMap<String,Object>();
		    attrMap.put("updated", sTS);
		    attrMap.put("name", nameValue[0]);
		    attrMap.put("value", nameValue[1]);
		    attrMap.put("created", sTS);
		    attributes.add(attrMap);
		}
		product.put("attributes", attributes);
		product.put("id", pid);
		//List<KatelloProduct> s = null;
        try {
            //s = servertasks.import_products(org_name, provider_name, allProducts);
        	servertasks.import_products(org_name, provider_name, allProducts);
        } catch (KatelloApiException e) {
            Assert.fail("Could not import products", e);
        }
//		Assert.assertEquals(s.startsWith("{\"name\":"), true,"Returned output should start with: {\"name\":");
//		Assert.assertEquals(s.equals("[true]"), true,"Returned output should be: [true]");
        // TODO Need new asserts once this is enabled
	}
	
	// TODO Turn this into a data driven test.
	@Test (groups={"testProviders"}, description="Update Provider Properties", dependsOnMethods="test_createProvider")
	public void test_updateProvider(){
		Date dupBefore, dupAfter;
		KatelloProvider updProv = null;
        try {
            updProv = servertasks.getProvider(org_name, provider_name);
        } catch (KatelloApiException e) {
            Assert.fail("Could not get provider", e);
        }
		String upd_repo_url = "https://localhost";
		try{
			dupBefore = parseKatelloDate(updProv.getUpdatedAt());
			// update - name
			KatelloProvider updatedProvider = null;
            try {
                updatedProvider = servertasks.updateProviderProperty(org_name, provider_name, "name", "modified-"+provider_name);
            } catch (KatelloApiException e) {
                Assert.fail("Could not update Provider property", e);
            }
            log.info("Date string for updated_at: " + updatedProvider.getUpdatedAt());
			dupAfter = parseKatelloDate(updatedProvider.getUpdatedAt());
			Assert.assertEquals(updatedProvider.getName(), "modified-"+this.provider_name,"Check updated: name");
			Assert.assertMore(dupAfter.getTime(), dupBefore.getTime(), "Check the timestamp updated");
			this.provider_name = "modified-"+this.provider_name;
			
			//update - repository_url
			dupBefore = dupAfter;
			try {
                updatedProvider = servertasks.updateProviderProperty(org_name, provider_name, "repository_url", upd_repo_url);
            } catch (KatelloApiException e) {
                Assert.fail("Could not update Provider property", e);
            }
			dupAfter = parseKatelloDate(updatedProvider.getUpdatedAt());
			Assert.assertEquals(updatedProvider.getRepositoryUrl(), upd_repo_url,"Check updated: repository_url");
			Assert.assertMore(dupAfter.getTime(), dupBefore.getTime(), "Check the timestamp updated");

			//update - description
			dupBefore = dupAfter;
			try {
                updatedProvider = servertasks.updateProviderProperty(org_name, provider_name, "description", "Updated: provider ["+provider_name+"]");
            } catch (KatelloApiException e) {
                Assert.fail("Could not update Provider property", e);
            }
			dupAfter = parseKatelloDate(updatedProvider.getUpdatedAt());
			Assert.assertEquals(updatedProvider.getDescription(), "Updated: provider ["+provider_name+"]","Check updated: description");
			Assert.assertMore(dupAfter.getTime(), dupBefore.getTime(), "Check the timestamp updated");
			// TODO - needs to be applied with additional tests for provider type-RedHat, as only 1 provider of RedHat type could be in org.
		}catch(ParseException pex){
			log.severe(pex.getMessage());
		}
	}
	
	@Test (groups={"testProviders"}, description="List all providers", dependsOnMethods="test_updateProvider")
	public void test_listProviders(){
		// Get providers json string
	    List<KatelloProvider> providers = null;
        try {
            providers = servertasks.listProviders(org_name);
        } catch (KatelloApiException e) {
            Assert.fail("Could not get provider list", e);
        }
		Assert.assertMore(providers.size(), 0, "Check: providers count >0");
		// we need to find our provider modified (see the test dependency)
		boolean findOurProvider = false;
		for ( KatelloProvider provider : providers ) {
		    if ( provider.getName().equals(this.provider_name)){
		        findOurProvider = true;
		    }
		}
		// We have to have the provider found, else: error.
		Assert.assertTrue(findOurProvider, "Check: we found our provider");
	}
	
	@Test (groups={"testProviders"}, description="Delete provider",
			enabled=true) // BZ: https://bugzilla.redhat.com/show_bug.cgi?id=700423
	public void test_deleteProvider(){
		// Create separate provider to be removed 
		String uid = KatelloUtils.getUniqueID();
		String providerName = "auto-deleteMe-"+uid;
		KatelloProvider provider = null;
        try {
            provider = servertasks.createProvider(
            		this.org_name,providerName, "Provider in test - "+uid,"Custom", null);
        } catch (KatelloApiException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }		
		Assert.assertNotNull(provider, "Returned string in katello is JSON-formatted");
		
		String sout = null;
        try {
            sout = servertasks.deleteProvider(provider);
        } catch (KatelloApiException e) {
            // This is not the exception we were looking for. Not yet, anyway.
        }
		Assert.assertEquals(sout, "Deleted provider [ "+providerName+" ]","Check: message returned by the API call");
		try {
            provider = servertasks.getProvider(org_name, providerName);
        } catch (KatelloApiException e) {
            // We are looking for this one.
            Assert.assertNull(provider, "Check: returned getProvider() is null");
        }
	}
	
	@Test(groups={"testOrgs","testProviders"}, description="Generate ",enabled= false)
	public void test_postUeberCert(){ // TODO - seems Candlepin not have them still rpm-ed.
//		try{
//		    servertasks.postCert("org1");
//			String _return = servertasks.apiKatello_POST("", "/organizations/"+"org1"+"/uebercert");
//			log.info("Posting ueber cert for: ["+"org1"+"]");
//			System.out.println(_return);
//		}catch (Exception e) {
//			log.log(Level.SEVERE, e.getMessage(), e);
//		}
		
	}
	
	
}
