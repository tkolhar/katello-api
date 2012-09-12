package com.redhat.qe.katello.tasks;

import java.util.List;
import java.util.Map;

import com.redhat.qe.katello.base.KatelloApiException;
import com.redhat.qe.katello.base.obj.KatelloEntitlement;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloPool;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.base.obj.KatelloUser;

public interface KatelloTasks {

    public String uploadManifest(Long providerId, String exportZipPath);

    public KatelloEnvironment getEnvironment(String orgName, String envName);

    public String deleteEnvironment(String orgName, String envName)
            throws KatelloApiException;

    /**
     * Retrieves JSON object of environment from the organization details.
     * @param orgName Organization name
     * @param envName Environment name we are interested.
     * @return JSONObject representation of env. object in Org's details.
     * @author gkhachik
     * @since 16.Feb.2011 
     */
    public KatelloEnvironment getEnvFromOrgList(String orgName, String envName);

    public List<KatelloOrg> getOrganizations() throws KatelloApiException;

    public KatelloOrg getOrganization(String organizationKey)
            throws KatelloApiException;

    public KatelloSystem getConsumer(String consumer_id)
            throws KatelloApiException;

    public List<KatelloProduct> getProductsByOrg(String org_name)
            throws KatelloApiException;

    public List<Long> getSerials(String consumerId) throws KatelloApiException;

    public List<KatelloEnvironment> getEnvironments(String org_name)
            throws KatelloApiException;

    public List<KatelloProvider> listProviders(String org_name)
            throws KatelloApiException;

    public KatelloUser createUser(String username, String email, String password,
            boolean disabled) throws KatelloApiException;

    public List<KatelloUser> listUsers() throws KatelloApiException;

    public KatelloUser getUser(Long userId) throws KatelloApiException;

    public KatelloOrg createOrganization(String org_name, String org_description)
            throws KatelloApiException;

    public KatelloEnvironment createEnvironment(String orgKey, String env_name,
            String env_descr, String prior) throws KatelloApiException;

    public KatelloProvider createProvider(String orgName, String providerName,
            String description, String type, String url) throws KatelloApiException;

    public KatelloProduct createProduct(String org_name, String provider_name,
            String productName, String productDescription, String productUrl)
            throws KatelloApiException;

    public KatelloRepo createRepository(String providerName, String candlepin_id,
            String repo_name, String repo_url) throws KatelloApiException;

    public List<KatelloProduct> import_products(String orgName,
            String providerName, Map<String, Object> products) throws KatelloApiException;

    public List<KatelloProduct> listProducts(String orgName)
            throws KatelloApiException;

    public KatelloProduct getProductByOrg(String orgName, String productName)
            throws KatelloApiException;

    public String subscribeConsumer(String consumerId) throws KatelloApiException;
    
    public List<KatelloEntitlement> subscribeConsumerWithPool(String consumerId,
            String poolId) throws KatelloApiException;

    public KatelloSystem subscribeConsumerViaSystem(String consumerId,
            String poolId) throws KatelloApiException;

    public KatelloProvider getProvider(String org_name, String byName)
            throws KatelloApiException;

    public String getPool(String poolName) throws KatelloApiException;

    public List<KatelloPool> getPools() throws KatelloApiException;

    public String deleteProvider(KatelloProvider provider)
            throws KatelloApiException;

    public KatelloSystem createConsumer(
            String orgName, String hostname, String uuid) throws KatelloApiException;

    public KatelloSystem updateFacts(String consumerId, String component,
            String updValue) throws KatelloApiException;

    public KatelloEnvironment updateEnvProperty(String organizationName,
            String environmentName, String component, Object updValue)
            throws KatelloApiException;

    public KatelloProvider updateProviderProperty(String organizationName,
            String providerName, String component, Object updValue)
            throws KatelloApiException;

    public String updateUser(Long userId, String component, Object updValue)
            throws KatelloApiException;

    public KatelloSystem updatePackages(KatelloSystem consumer)
            throws KatelloApiException;

    public String deleteUser(Long userId) throws KatelloApiException;

    public String deleteConsumer(String consumerId) throws KatelloApiException;

    public KatelloSystem unsubscribeConsumer(String consumerId, String serial)
            throws KatelloApiException;

    /**
     * Unsubscribes from ALL (--all option in rhsm)
     * @param consumer_id
     * @return
     * @throws KatelloApiException  
     */
    public KatelloSystem unsubscribeConsumer(String consumerId)
            throws KatelloApiException;

    public Long getEnvironmentPriorId(KatelloEnvironment env);

}
