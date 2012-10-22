package com.redhat.qe.katello.tasks.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.activation.DataSource;
import javax.activation.FileDataSource;

import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import com.google.inject.Inject;
import com.redhat.qe.katello.base.KatelloApiException;
import com.redhat.qe.katello.base.obj.KatelloEntitlement;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloPool;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.base.obj.KatelloSerial;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.base.obj.KatelloTask;
import com.redhat.qe.katello.base.obj.KatelloUser;
import com.redhat.qe.katello.resource.ConsumerResource;
import com.redhat.qe.katello.resource.OrganizationResource;
import com.redhat.qe.katello.resource.PoolResource;
import com.redhat.qe.katello.resource.ProviderResource;
import com.redhat.qe.katello.resource.RepositoryResource;
import com.redhat.qe.katello.resource.SystemResource;
import com.redhat.qe.katello.resource.TaskResource;
import com.redhat.qe.katello.resource.UserResource;
import com.redhat.qe.katello.tasks.KatelloTasks;

/**
 * Various utility tasks regarding Katello (+components) functionality.
 * @author gkhachik
 * @since 14.Feb.2011
 *
 */
public class KatelloApiTasks implements KatelloTasks {
	@Inject protected Logger log;
	final private OrganizationResource orgResource;
	final private ProviderResource providerResource;
	final private RepositoryResource repositoryResource;
	final private ConsumerResource consumerResource;
	final private UserResource userResource;
	final private PoolResource poolResource;
	final private SystemResource systemResource;
	final private TaskResource taskResource;
	 
	static {
        // this initialization only needs to be done once per VM
	    ResteasyProviderFactory instance = ResteasyProviderFactory.getInstance();	    
        RegisterBuiltin.register(instance);        
	}
	
	@Inject
	KatelloApiTasks(OrganizationResource orgResource,
	                       ProviderResource providerResource,
	                       RepositoryResource repositoryResource,
	                       ConsumerResource consumerResource,
	                       UserResource userResource,
	                       PoolResource poolResource,
	                       SystemResource systemResource,
	                       TaskResource taskResource) {
        this.orgResource = orgResource;
        this.providerResource = providerResource;
        this.repositoryResource = repositoryResource;
        this.consumerResource = consumerResource;
        this.userResource = userResource;
        this.poolResource = poolResource;
        this.systemResource = systemResource;
        this.taskResource = taskResource;
	}
//	private ExecCommands localCommandRunner = null;
// # ************************************************************************* #
// # PUBLIC section                                                            #
// # ************************************************************************* #	


    /* (non-Javadoc)
     * @see com.redhat.qe.katello.tasks.IKatelloTasks#uploadManifest(java.lang.Long, java.lang.String)
     */
    @Override
    public String uploadManifest(Long providerId, String exportZipPath) {
        Map<String, DataSource>  parts = new HashMap<String, DataSource>();
        parts.put("file", new FileDataSource(new File(exportZipPath)));
        Object response = providerResource.import_manifest(providerId, parts);
        return response.toString();
    }
	
	/* (non-Javadoc)
     * @see com.redhat.qe.katello.tasks.IKatelloTasks#getEnvironment(java.lang.String, java.lang.String)
     */
	@Override
    public KatelloEnvironment getEnvironment(String orgName, String envName){
		KatelloEnvironment _return = null;
		try{
			log.info(String.format("Retrieve environment: [%s] of Org: [%s]", envName, orgName));
	        ClientResponse<List<KatelloEnvironment>> envResponse = orgResource.listEnvironments(orgName);
			List<KatelloEnvironment> envs = envResponse.getEntity();
			for ( KatelloEnvironment env : envs ) {
			    if ( env.getName().equals(envName)) {
			        return env;
			    }
			}
		}catch (Exception e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
		return _return;		
	}
	
	/* (non-Javadoc)
     * @see com.redhat.qe.katello.tasks.IKatelloTasks#deleteEnvironment(java.lang.String, java.lang.String)
     */
	@Override
    public String deleteEnvironment(String orgName, String envName) throws KatelloApiException {
		ClientResponse<String> _return = null;
		Long envId = getEnvironment(orgName, envName).getId();
		_return = orgResource.deleteEnvironment(orgName, envId);
		if ( _return.getStatus() > 299 ) throw new KatelloApiException(_return);
		log.info(String.format("Deleted the environment [%s] of org: [%s]",envName,orgName));
		return _return.getEntity();		
	}
	
	/* (non-Javadoc)
     * @see com.redhat.qe.katello.tasks.IKatelloTasks#getEnvFromOrgList(java.lang.String, java.lang.String)
     */
	@Override
    public KatelloEnvironment getEnvFromOrgList(String orgName, String envName){
		List<KatelloEnvironment> envs = orgResource.listEnvironments(orgName).getEntity();
		for ( KatelloEnvironment env : envs ) {
		    if ( env.getName().equals(envName)) {
		        return env;
		    }
		}
		return null;
	}	
	
    /* (non-Javadoc)
     * @see com.redhat.qe.katello.tasks.IKatelloTasks#getOrganizations()
     */
    @Override
    public List<KatelloOrg> getOrganizations() throws KatelloApiException {
        ClientResponse<List<KatelloOrg>> _return = null;
        _return = orgResource.list();
        if ( _return.getStatus() > 299 ) throw new KatelloApiException(_return);
        log.info(String.format("Org list returned with %d entries", _return.getEntity().size()));
        return _return.getEntity();
    }

    /* (non-Javadoc)
     * @see com.redhat.qe.katello.tasks.IKatelloTasks#getOrganization(java.lang.String)
     */
    @Override
    public KatelloOrg getOrganization(String organizationKey) throws KatelloApiException {
        ClientResponse<KatelloOrg> _return = null;
        _return = orgResource.getOrganization(organizationKey);
        if ( _return.getStatus() > 299 ) throw new KatelloApiException(_return);
        return _return.getEntity();
    }

    /* (non-Javadoc)
     * @see com.redhat.qe.katello.tasks.IKatelloTasks#getConsumer(java.lang.String)
     */
    @Override
    public KatelloSystem getConsumer(String consumer_id) throws KatelloApiException {
        ClientResponse<KatelloSystem> _return = null;
        _return = consumerResource.get(consumer_id);
        if ( _return.getStatus() > 299 ) throw new KatelloApiException(_return);
        return _return.getEntity();
    }

    /* (non-Javadoc)
     * @see com.redhat.qe.katello.tasks.IKatelloTasks#getProductsByOrg(java.lang.String)
     */
    @Override
    public List<KatelloProduct> getProductsByOrg(String org_name) throws KatelloApiException {
        ClientResponse<List<KatelloProduct>> _return = null;
        _return = orgResource.listProducts(org_name);
        if ( _return.getStatus() > 299 ) throw new KatelloApiException(_return);
        return _return.getEntity();
    }

    /* (non-Javadoc)
     * @see com.redhat.qe.katello.tasks.IKatelloTasks#getSerials(java.lang.String)
     */
    @Override
    public List<Long> getSerials(String consumerId) throws KatelloApiException {
        List<Long> _return = new ArrayList<Long>();
        ClientResponse<List<KatelloSerial>> serialsResponse = consumerResource.listSerials(consumerId);
        if ( serialsResponse.getStatus() > 299 ) throw new KatelloApiException(serialsResponse);        
        for ( KatelloSerial serial : serialsResponse.getEntity() ) {
            _return.add(serial.getId());
        }
        return _return;
    }

    /* (non-Javadoc)
     * @see com.redhat.qe.katello.tasks.IKatelloTasks#getEnvironments(java.lang.String)
     */
    @Override
    public List<KatelloEnvironment> getEnvironments(String org_name) throws KatelloApiException {
        ClientResponse<List<KatelloEnvironment>> _return = null;
        _return = orgResource.listEnvironments(org_name);
        if ( _return.getStatus() > 299 ) throw new KatelloApiException(_return);
        return _return.getEntity();
    }

    /* (non-Javadoc)
     * @see com.redhat.qe.katello.tasks.IKatelloTasks#listProviders(java.lang.String)
     */
    @Override
    public List<KatelloProvider> listProviders(String org_name) throws KatelloApiException {
        ClientResponse<List<KatelloProvider>> _return = null;
        _return = orgResource.listProviders(org_name);
        if ( _return.getStatus() > 299 ) throw new KatelloApiException(_return);
        return _return.getEntity();
    }

    /* (non-Javadoc)
     * @see com.redhat.qe.katello.tasks.IKatelloTasks#createUser(java.lang.String, java.lang.String, java.lang.String, boolean)
     */
    @Override
    public KatelloUser createUser(String username, String email, String password, boolean disabled) throws KatelloApiException {
        ClientResponse<KatelloUser> _return = null;
        Map<String,Object> user = new HashMap<String,Object>();
        user.put("username", username);
        user.put("password", password);
        user.put("email", email);
        user.put("disabled", Boolean.valueOf(disabled));

        _return = userResource.create(user);
        if ( _return.getStatus() > 299 ) throw new KatelloApiException(_return);
        return _return.getEntity();
    }

    /* (non-Javadoc)
     * @see com.redhat.qe.katello.tasks.IKatelloTasks#listUsers()
     */
    @Override
    public List<KatelloUser> listUsers() throws KatelloApiException {
        ClientResponse<List<KatelloUser>> _return = null;
        _return = userResource.list();
        if ( _return.getStatus() > 299 ) throw new KatelloApiException(_return);
        return _return.getEntity();
    }

    /* (non-Javadoc)
     * @see com.redhat.qe.katello.tasks.IKatelloTasks#getUser(java.lang.Long)
     */
    @Override
    public KatelloUser getUser(Long userId) throws KatelloApiException {
        ClientResponse<KatelloUser> _return = null;
        _return = userResource.get(userId);
        if ( _return.getStatus() > 299 ) throw new KatelloApiException(_return);
        return _return.getEntity();
    }

    
    /* (non-Javadoc)
     * @see com.redhat.qe.katello.tasks.IKatelloTasks#createOrganization(java.lang.String, java.lang.String)
     */
    @Override
    public KatelloOrg createOrganization(String org_name, String org_description) throws KatelloApiException {
        ClientResponse<KatelloOrg> _return = null;
        Map<String,Object> orgPost = new HashMap<String,Object>();
        orgPost.put("name", org_name);
        orgPost.put("description", org_description);
        _return = orgResource.create(orgPost);
        if ( _return.getStatus() > 299 ) throw new KatelloApiException(_return);
        log.info(String.format("Create an org with: name=[%s]; description=[%s]", org_name, org_description));
        return _return.getEntity();
    }

    /* (non-Javadoc)
     * @see com.redhat.qe.katello.tasks.IKatelloTasks#createEnvironment(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public KatelloEnvironment createEnvironment(String orgKey, String env_name,
            String env_descr, String prior) throws KatelloApiException {
        ClientResponse<KatelloEnvironment> _return = null;
        Map<String,Object> envPost = new HashMap<String,Object>();
        envPost.put("name", env_name);
        envPost.put("description", env_descr);
        Long priorId = null;
        List<KatelloEnvironment> envs = orgResource.listEnvironments(orgKey).getEntity();
        for (KatelloEnvironment env : envs ) {
            if(env.getName().equals(prior)){
                priorId = env.getId();
                break;
            }
        }
        envPost.put("prior", priorId);
        Map<String,Object> env = new HashMap<String,Object>();
        env.put("environment", envPost);
        _return = orgResource.createEnvironment(orgKey, env);
        if ( _return.getStatus() > 299 ) throw new KatelloApiException(_return);
        log.info(String.format("Create environment with: name=[%s]; description=[%s]; org=[%s]", env_name, env_descr, orgKey));
        return _return.getEntity();
    }

	/* (non-Javadoc)
     * @see com.redhat.qe.katello.tasks.IKatelloTasks#createProvider(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
	@Override
    public KatelloProvider createProvider(String orgName, String providerName, 
			String description, String type, String url) throws KatelloApiException {
		ClientResponse<KatelloProvider> _return = null;
		try {
		    Map<String,Object> katelloProviderPost = new HashMap<String,Object>();
		    katelloProviderPost.put("organization_id", orgName);
		    Map<String,String> provider = new HashMap<String,String>();
		    provider.put("name", providerName);
		    provider.put("description", description);
		    provider.put("provider_type", type);
		    if ( url != null && !url.isEmpty() ) {
		        provider.put("repository_url", url);
		    }
		    katelloProviderPost.put("provider", provider);
		    _return = providerResource.create(katelloProviderPost);
		    if ( _return.getStatus() > 299 ) throw new KatelloApiException(_return);
			log.info(String.format("Created a provider with: " +
					"name=[%s]; description=[%s]; " +
					"provider_type=[%s], repository_url=[%s]", 
					providerName,description,type,url));
		}catch (Exception e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
		return _return.getEntity();
	}
	
	/* (non-Javadoc)
     * @see com.redhat.qe.katello.tasks.IKatelloTasks#createProduct(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
	@Override
    public KatelloProduct createProduct(String org_name, String provider_name, 
			String productName, String productDescription, String productUrl) throws KatelloApiException {
		ClientResponse<KatelloProduct> _return = null;
		try{
		    Map<String,Object> katelloProduct = new HashMap<String,Object>();
		    Map<String,String> product = new HashMap<String,String>();
		    product.put("name", productName);
		    product.put("description", productDescription);
		    product.put("url", productUrl);
		    katelloProduct.put("product", product);
			Long providerId = getProvider(org_name, provider_name).getId();
			_return = providerResource.create(providerId, katelloProduct);
			if ( _return.getStatus() > 299 ) throw new KatelloApiException(_return);
			log.info(String.format("Created a product for provider: [%s] with: " +
					"name=[%s]; description=[%s]; " +
					"url=[%s]", 
					provider_name,productName,productDescription,productUrl));
		}catch (Exception e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
		return _return.getEntity();
	}

	/* (non-Javadoc)
     * @see com.redhat.qe.katello.tasks.IKatelloTasks#createRepository(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
	@Override
    public KatelloRepo createRepository(String providerName, String candlepin_id, 
	        String repo_name, String repo_url) throws KatelloApiException {
		ClientResponse<KatelloRepo> _return = null;
		try{
		    Map<String,String> repository = new HashMap<String,String>();
		    repository.put("name", repo_name);
		    repository.put("product_id", candlepin_id);
		    repository.put("url", repo_url);
		    _return = repositoryResource.create(repository);
			if ( _return.getStatus() > 299 ) throw new KatelloApiException(_return);
			log.info(String.format("Created a repo for provider: [%s] with: " +
					"name=[%s]; " +
					"product_id=[%s]; "+
					"url=[%s]", 
					providerName,repo_name,candlepin_id,repo_url));
		}catch (Exception e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
		return _return.getEntity();
	}
		
	/* (non-Javadoc)
     * @see com.redhat.qe.katello.tasks.IKatelloTasks#import_products(java.lang.String, java.lang.String, java.util.Map)
     */
	@Override
    public List<KatelloProduct> import_products(String orgName, String providerName, Map<String,Object> products) throws KatelloApiException {
		ClientResponse<List<KatelloProduct>> _return=null;
		Long providerId = getProvider(orgName, providerName).getId();
		try{
		    _return = providerResource.import_products(providerId, products);
		    if ( _return.getStatus() > 299 ) throw new KatelloApiException(_return);
			log.info(String.format("Importing product(s) for provider: id=[%s]", 
					providerId));
		}catch (Exception e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
		return _return.getEntity();
	}
	
	/* (non-Javadoc)
     * @see com.redhat.qe.katello.tasks.IKatelloTasks#listProducts(java.lang.String)
     */
	@Override
    public List<KatelloProduct> listProducts(String orgName) throws KatelloApiException {
	    ClientResponse<List<KatelloProduct>> _return = null;
	    _return = orgResource.listProducts(orgName);
	    if ( _return.getStatus() > 299 ) throw new KatelloApiException(_return);
	    return _return.getEntity();
	}
	
	/* (non-Javadoc)
     * @see com.redhat.qe.katello.tasks.IKatelloTasks#getProductByOrg(java.lang.String, java.lang.String)
     */
	@Override
    public KatelloProduct getProductByOrg(String orgName, String productName) throws KatelloApiException {
		try{		   
		    List<KatelloProduct> products = listProducts(orgName);
		    if ( products.size() == 0 ) return null;
			log.info(String.format("Get product: name=[%s]",productName));
			for ( KatelloProduct product : products ) {
			    if ( product.getName().equals(productName)) {
			        return product;
			    }
			}
		}catch (Exception e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
		return null;
	}
	
	@Override
	public String subscribeConsumer(String consumerId) throws KatelloApiException {
	    ClientResponse<String> _return = null;
	    _return = consumerResource.subscribe(consumerId);
	    if ( _return.getStatus() > 299 ) throw new KatelloApiException(_return);
	    log.info(String.format("Subscribing consumer: [%s]", consumerId));
	    return _return.getEntity();
	}
	
	/* (non-Javadoc)
     * @see com.redhat.qe.katello.tasks.IKatelloTasks#subscribeConsumer(java.lang.String, java.lang.String)
     */
	@Override
    public List<KatelloEntitlement> subscribeConsumerWithPool(String consumerId, String poolId) throws KatelloApiException {
		ClientResponse<List<KatelloEntitlement>> _return = null;
		_return = consumerResource.subscribeWithPool(consumerId, poolId);
		if ( _return.getStatus() > 299 ) throw new KatelloApiException(_return);		
		log.info(String.format("Subscribing consumer: [%s] to the pool: [%s]", consumerId,poolId));
		return _return.getEntity();
	}
	
	/* (non-Javadoc)
     * @see com.redhat.qe.katello.tasks.IKatelloTasks#subscribeConsumerViaSystem(java.lang.String, java.lang.String)
     */
	@Override
    public KatelloSystem subscribeConsumerViaSystem(String consumerId, String poolId) throws KatelloApiException {
	    ClientResponse<KatelloSystem> _return = null;
	    Map<String,Object> pool = new HashMap<String,Object>();
	    pool.put("pool", poolId);
	    pool.put("quantity", Long.valueOf(1));
	    _return = systemResource.subscribe(consumerId, pool);
	    if (_return.getStatus() > 299) throw new KatelloApiException(_return);
	    log.info(String.format("Subscribing consumer: [%s] to the pool: [%s]", consumerId, poolId));
	    return _return.getEntity();
	}
	
	/* (non-Javadoc)
     * @see com.redhat.qe.katello.tasks.IKatelloTasks#getProvider(java.lang.String, java.lang.String)
     */
	@Override
    public KatelloProvider getProvider(String org_name, String byName) throws KatelloApiException {
		ClientResponse<List<KatelloProvider>> providers = orgResource.listProviders(org_name);
		for ( KatelloProvider provider : providers.getEntity() ) {
		    if ( provider.getName().equals(byName)) {
		        return provider;
		    }
		}
		return null;
	}
	
	/* (non-Javadoc)
     * @see com.redhat.qe.katello.tasks.IKatelloTasks#getPool(java.lang.String)
     */
	@Override
    public String getPool(String poolName) throws KatelloApiException {
	    List<KatelloPool> pools = getPools();
	    for ( KatelloPool pool : pools ) {
	        if(pool.getProductName().equals(poolName)) {
	            return pool.getId();
	        }
	    }
	    return null;
	}
	
	/* (non-Javadoc)
     * @see com.redhat.qe.katello.tasks.IKatelloTasks#getPools()
     */
	@Override
    public List<KatelloPool> getPools() throws KatelloApiException {
	    ClientResponse<List<KatelloPool>> _return = null;	    
	    _return = poolResource.list();
	    if ( _return.getStatus() > 299 ) throw new KatelloApiException(_return);
	    return _return.getEntity();
	}

	private String deleteProvider(String orgName, KatelloProvider provider) throws KatelloApiException {
		ClientResponse<String> _return=null;
		Long providerId = getProvider(orgName, provider.getName()).getId();
		_return = providerResource.delete(providerId);
		log.info("Delete provider: name=["+provider.getName()+"]; id=["+providerId+"]");
		return _return.getEntity();
	}

    /* (non-Javadoc)
     * @see com.redhat.qe.katello.tasks.IKatelloTasks#deleteProvider(com.redhat.qe.katello.base.obj.KatelloProvider)
     */
    @Override
    public String deleteProvider(KatelloProvider provider) throws KatelloApiException {
        // This is a workaround until provider api is fixed
        List<KatelloOrg> orgs = orgResource.list().getEntity();
        for ( KatelloOrg org : orgs ) {
            if (org.getId().equals(provider.getOrganizationId())) {
            	return deleteProvider(org.name, provider);
            }
        }
        return "Could not delete provider";
    }	
	
    /* (non-Javadoc)
     * @see com.redhat.qe.katello.tasks.IKatelloTasks#createConsumer(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public KatelloSystem createConsumer(
			String orgName, String hostname, String uuid) throws KatelloApiException {
		ClientResponse<KatelloSystem> _return=null;
		Map<String, Object> sFacts = new HashMap<String,Object>();

		sFacts = createFacts(hostname, uuid, orgName);
		log.info(String.format("Creating consumer with: uuid=[%s]; hostname=[%s]; org_name=[%s]", 
		        uuid, hostname,orgName));
		_return = consumerResource.create(orgName, sFacts);
		if ( _return.getStatus() > 299 ) throw new KatelloApiException(_return);

		return _return.getEntity();		
	}
	
    /* (non-Javadoc)
     * @see com.redhat.qe.katello.tasks.IKatelloTasks#updateFacts(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public KatelloSystem updateFacts(String consumerId, String component, String updValue) throws KatelloApiException {
        ClientResponse<KatelloSystem> _return = null;
        // Get facts object to modify it and update again :)        
        Map<String, String> facts = consumerResource.get(consumerId).getEntity().getFacts();
        facts.put(component, updValue);
        Map<String,Object> updFacts = new HashMap<String,Object>();
        updFacts.put("facts", facts);
        try { Thread.sleep(1000); } catch (Exception ex) {} // for the "update_at" checks
        log.finest(String.format("Update consumer: [%s] facts with: [%s=%s]", consumerId, component, updValue));
        _return = consumerResource.update(consumerId, updFacts);
        if ( _return.getStatus() > 299 ) throw new KatelloApiException(_return);
        return _return.getEntity();
    }
	
    /* (non-Javadoc)
     * @see com.redhat.qe.katello.tasks.IKatelloTasks#updateEnvProperty(java.lang.String, java.lang.String, java.lang.String, java.lang.Object)
     */
    @Override
    public KatelloEnvironment updateEnvProperty(String organizationName, String environmentName, String component, Object updValue) throws KatelloApiException {
        ClientResponse<KatelloEnvironment> _return = null;
        Map<String,Object> env = new HashMap<String,Object>();
        env.put(component, updValue);
        Map<String,Object> updEnv = new HashMap<String,Object>();
        updEnv.put("environment", env);
        
        Long envId = getEnvironment(organizationName, environmentName).getId();
        try{Thread.sleep(1000);}catch(Exception ex){} // for the "update_at" checks
        _return = orgResource.updateEnvironment(organizationName, envId, updEnv);
        if ( _return.getStatus() > 299 ) throw new KatelloApiException(_return);
        return _return.getEntity();
    }
    
    /* (non-Javadoc)
     * @see com.redhat.qe.katello.tasks.IKatelloTasks#updateProviderProperty(java.lang.String, java.lang.String, java.lang.String, java.lang.Object)
     */
    @Override
    public KatelloProvider updateProviderProperty(String organizationName, String providerName, String component, Object updValue) throws KatelloApiException {
        ClientResponse<KatelloProvider> _return = null;
        Map<String,Object> prov = new HashMap<String,Object>();
        prov.put(component, updValue);
        Map<String,Object> updProv = new HashMap<String,Object>();
        updProv.put("provider", prov);
        
        Long providerId = getProvider(organizationName, providerName).getId();
        try{Thread.sleep(1000);}catch(Exception ex){} // for the "update_at" checks
        _return = providerResource.update(providerId, updProv);
        if ( _return.getStatus() > 299 ) throw new KatelloApiException(_return);
        return _return.getEntity();
    }
    
    /* (non-Javadoc)
     * @see com.redhat.qe.katello.tasks.IKatelloTasks#updateUser(java.lang.Long, java.lang.String, java.lang.Object)
     */
    @Override
    public String updateUser(Long userId, String component, Object updValue ) throws KatelloApiException {
        ClientResponse<String> _return = null;
        Map<String,Object> userProp = new HashMap<String,Object>();
        userProp.put(component, updValue);
        Map<String,Object> updUser = new HashMap<String,Object>();
        updUser.put("user", userProp);
        
        try{Thread.sleep(1000);}catch(Exception ex){} // for the update_at checks
        _return = userResource.update(userId, updUser);
        if ( _return.getStatus() > 299 ) throw new KatelloApiException(_return);
        return _return.getEntity();
    }

    /* (non-Javadoc)
     * @see com.redhat.qe.katello.tasks.IKatelloTasks#updatePackages(com.redhat.qe.katello.base.obj.KatelloSystem)
     */
    @Override
    public KatelloSystem updatePackages(KatelloSystem consumer) throws KatelloApiException {
        ClientResponse<KatelloSystem> _return = null;
        List<Map<String,Object>> packages = createPackages();
        _return = consumerResource.updatePackages(consumer.getUuid(), packages);
        if ( _return.getStatus() > 299) throw new KatelloApiException(_return);
        return _return.getEntity();
    }
    
    /* (non-Javadoc)
     * @see com.redhat.qe.katello.tasks.IKatelloTasks#deleteUser(java.lang.Long)
     */
    @Override
    public String deleteUser(Long userId) throws KatelloApiException {
        ClientResponse<String> _return = null;
        _return = userResource.delete(userId);
        if ( _return.getStatus() > 299 ) throw new KatelloApiException(_return);
        log.info(String.format("Remove user: id=[%s]", userId.toString()));
        return _return.getEntity();
    }


    
	/* (non-Javadoc)
     * @see com.redhat.qe.katello.tasks.IKatelloTasks#deleteConsumer(java.lang.String)
     */
	@Override
    public String deleteConsumer(String consumerId) throws KatelloApiException {
		ClientResponse<String> _return=null;
		_return = consumerResource.delete(consumerId);
		if ( _return.getStatus() > 299 ) throw new KatelloApiException(_return);
		log.info(String.format("Remove consumer: uuid=[%s]", 
					consumerId));
		return _return.getEntity();		
	}

	/* (non-Javadoc)
     * @see com.redhat.qe.katello.tasks.IKatelloTasks#unsubscribeConsumer(java.lang.String, java.lang.String)
     */
	@Override
    public KatelloSystem unsubscribeConsumer(String consumerId, String serial) throws KatelloApiException {
		ClientResponse<KatelloSystem> _return=null;
		_return = consumerResource.unsubscribe(consumerId, serial);
		if ( _return.getStatus() > 299 ) throw new KatelloApiException(_return);
		log.info(String.format("Unsubscribe consumer: uuid=[%s] from the product with: serial=[%s]", 
					consumerId,serial));
		return _return.getEntity();		
	}

	/* (non-Javadoc)
     * @see com.redhat.qe.katello.tasks.IKatelloTasks#unsubscribeConsumer(java.lang.String)
     */
	@Override
    public KatelloSystem unsubscribeConsumer(String consumerId) throws KatelloApiException {
		ClientResponse<KatelloSystem> _return=null;
		_return = consumerResource.unsubscribe(consumerId);
		if ( _return.getStatus() > 299 ) throw new KatelloApiException(_return);
		log.info(String.format("Unsubscribe consumer: uuid=[%s] from all entitlements", 
					consumerId));
		return _return.getEntity();		
	}
	
	
// # ************************************************************************* #
// # PRIVATE section                                                           #
// # ************************************************************************* #	

//	/**
//	 * In order to make the ExecCommands instance available to run 
//	 * JSON format line commands,
//	 * here is made a workaround: put command on file and "sh &lt;file&gt;" it.
//	 * @param showLogResults True/False to log/output the result returned
//	 * @param command The command line to be executed locally
//	 * @return Output of the command run
//	 * @author gkhachik
//	 * @since 15.Feb.2011
//	 */
//	private String execute_local(boolean showLogResults, String command){
//		String out = null; String tmp_cmdFile = "/tmp/katello-json.sh";
//		try{
//			// cleanup the running buffer file.
//			this.localCommandRunner.submitCommandToLocalWithReturn(false, 
//					"rm -f "+tmp_cmdFile,"");
//
//			FileOutputStream fout = 
//				new FileOutputStream(tmp_cmdFile);
//			fout.write((command+"\n").getBytes());fout.flush();fout.close();
//			log.finest(String.format("Executing local: [%s]",command));
//			out = this.localCommandRunner.submitCommandToLocalWithReturn(
//					false, "sh "+tmp_cmdFile, ""); // HERE is the run
//			
//			if(showLogResults){ // log output if specified so.
//				// split the lines and out each line.
//				String[] split = out.split("\\n");
//				for(int i=0;i<split.length;i++){
//					log.info("Output: "+split[i]);
//				}
//			}
//			
//			// cleanup the running buffer file.
//			this.localCommandRunner.submitCommandToLocalWithReturn(false, 
//					"rm -f "+tmp_cmdFile,"");
//		}catch(IOException iex){
//			log.log(Level.SEVERE, iex.getMessage(), iex);
//		}
//		return out;
//	}
//	
	/* (non-Javadoc)
     * @see com.redhat.qe.katello.tasks.IKatelloTasks#getEnvironmentPriorId(com.redhat.qe.katello.base.obj.KatelloEnvironment)
     */
	@Override
    public Long getEnvironmentPriorId(KatelloEnvironment env){
	    if(env.getPriorId()==null) store_id(env);
	    return env.getPriorId();
	}

	/**
	 * Retrieves environment.prior.id from API call and stores in prior_id property, note: ID could not be updated. 
	 */
	private void store_id(KatelloEnvironment theEnv){
	    if(theEnv.getPriorId()==null) {	        
	        List<KatelloEnvironment> envs = orgResource.listEnvironments(theEnv.getOrganizationKey()).getEntity();
	        for (KatelloEnvironment env : envs ) {
	            if(env.getName().equals(theEnv.getPrior())){
	                theEnv.setPriorId(env.getId());
	                break;
	            }
	        }
	        if(theEnv.getPriorId()==null)
	            log.warning("Unable to retrieve environment.id for: ["+theEnv.getName()+"]");
	    }
	}
	
	private List<Map<String,Object>> createPackages() {
	    Object[][] packages = {{"Red Hat, Inc.","parted", Long.valueOf(0), "2.1", "17.el6", "x86_64"},
	                           {"Red Hat, Inc.", "gstreamer-tools", Long.valueOf(0), "0.10.29", "1.el6", "x86_64"},
	                           {"Red Hat, Inc.", "pm-utils", Long.valueOf(0), "1.2.5", "9.el6", "x86_64"},
	                           {"Red Hat, Inc.", "iw", Long.valueOf(0), "0.9.17", "4.el6", "x86_64"},
	                           {"Red Hat, Inc.", "crda", Long.valueOf(0), "1.1.1_2010.11.22", "1.el6", "x86_64"},
	                           {"Red Hat, Inc.", "keyutils", Long.valueOf(0), "1.4", "3.el6", "x86_64"},
	                           {"Red Hat, Inc.", "ntp", Long.valueOf(0), "4.2.4p8", "2.el6", "x86_64"},
	                           {"Red Hat, Inc.", "libutempter", Long.valueOf(0), "1.1.5", "4.1.el6", "x86_64"},
	                           {"Red Hat, Inc.", "cronie", Long.valueOf(0), "1.4.4", "7.el6", "x86_64"},
	                           {"Red Hat, Inc.", "boost-iostreams", Long.valueOf(0), "1.41.0", "11.el6_1.2", "x86_64"},
	                           {"Red Hat, Inc.", "certmonger", Long.valueOf(0), "0.50", "3.el6", "x86_64"},
	                           {"Red Hat, Inc.", "upstart", Long.valueOf(0), "0.6.5", "10.el6", "x86_64"}};
	    String[] keys = { "vendor", "name", "epoch", "version", "release", "arch" };
	    List<Map<String,Object>> pkgsList = new ArrayList<Map<String,Object>>();
	    for ( Object[] pkg : packages ) {
	        Map<String,Object> pkgMap = new HashMap<String,Object>();
	        for ( int i = 0; i < keys.length; ++i ) {
                pkgMap.put(keys[i], pkg[i]);	            
	        }
	        pkgsList.add(pkgMap);
	    }
	    return pkgsList;
	}
	
    private Map<String,Object> createFacts(String hostname, String uuid, String orgName) {
        Map<String,Object> allFacts = new HashMap<String,Object>();
        allFacts.put("org_name", orgName);
        Map<String,Object> facts = new HashMap<String,Object>();
        facts.put("dmi.bios.release_date", "01/01/2007");
        facts.put("net.interface.lo.ipaddr", "127.0.0.1");
        facts.put("network.hostname", hostname);
        facts.put("cpu.hypervisor_vendor", "KVM");
        facts.put("system.entitlements_valid", Boolean.TRUE);
        facts.put("dmi.memory.type", "RAM");
        facts.put("dmi.bios.address", "0xe8000");
        facts.put("dmi.bios.runtime_size", "96 KB");
        facts.put("distribution.id", "Santiago");
        facts.put("dmi.memory.maximum_capacity", "1 GB");
        facts.put("dmi.chassis.asset_tag", "Not Specified");
        facts.put("dmi.memory.bank_locator", "Not Specified");
        facts.put("cpu.virtualization_type", "full");
        facts.put("net.interface.eth0.hwaddr", "54:52:00:69:55:b6");
        facts.put("dmi.system.wake-up_type", "Power Switch");
        facts.put("dmi.chassis.boot-up_state", "Safe");
        facts.put("distribution.name", "Red Hat Enterprise Linux Server");
        facts.put("cpu.thread(s)_per_core", "1");
        facts.put("dmi.chassis.manufacturer", "RED HAT");
        facts.put("dmi.bios.bios_revision", "1.0");
        facts.put("dmi.chassis.version", "Not Specified");
        facts.put("distribution.version", "6.1");
        facts.put("uname.version", "#1 SMP Tue Apr 5 19:58:31 EDT 2011");
        facts.put("net.interface.lo.hwaddr", "00:00:00:00:00:00");
        facts.put("dmi.bios.vendor", "QEMU");
        facts.put("dmi.memory.error_correction_type", "Multi-bit ECC");
        facts.put("dmi.memory.locator", "DIMM 0");
        facts.put("dmi.system.manufacturer", "Red Hat");
        facts.put("dmi.chassis.serial_number", "Not Specified");
        facts.put("dmi.bios.rom_size", "64 KB");
        facts.put("cpu.stepping", "3");
        facts.put("uname.release", "2.6.32-130.el6.x86_64");
        facts.put("dmi.system.uuid", uuid);
        facts.put("dmi.memory.array_handle", "0x1000");
        facts.put("cpu.cpu_op-mode(s)", "32-bit, 64-bit");
        facts.put("net.interface.eth0.netmask", "255.255.255.224");
        facts.put("dmi.memory.data_width", "64 bit");
        facts.put("memory.swaptotal", "1015800");
        facts.put("net.interface.lo.broadcast", "0.0.0.0");
        facts.put("dmi.chassis.lock", "Not Present");
        facts.put("cpu.cpu_mhz", "2793.074");
        facts.put("dmi.memory.speed", "  (ns)");
        facts.put("uname.machine", "x86_64");
        facts.put("dmi.memory.form_factor", "DIMM");
        facts.put("dmi.memory.total_width", "64 bit");
        facts.put("cpu.l1d_cache", "32K");
        facts.put("virt.is_guest", Boolean.TRUE);
        facts.put("cpu.cpu(s)", "1");
        facts.put("net.interface.lo.netmask", "255.0.0.0");
        facts.put("net.interface.eth0.broadcast", "10.34.56.31");
        facts.put("dmi.memory.error_information_handle", "Not Provided");
        facts.put("cpu.architecture", "x86_64");
        facts.put("cpu.vendor_id", "GenuineIntel");
        facts.put("dmi.processor.upgrade", "Other");
        facts.put("dmi.system.sku_number", "Not Specified");
        facts.put("cpu.bogomips", "5586.14");
        facts.put("dmi.memory.location", "Other");
        facts.put("dmi.chassis.thermal_state", "Safe");
        facts.put("dmi.system.serial_number", "Not Specified");
        facts.put("cpu.cpu_socket(s)", "1");
        facts.put("dmi.processor.voltage", " ");
        facts.put("uname.sysname", "Linux");
        facts.put("dmi.system.family", "Red Hat Enterprise Linux");
        facts.put("cpu.model", "6");
        facts.put("dmi.processor.version", "Not Specified");
        facts.put("uname.nodename", hostname);
        facts.put("dmi.chassis.power_supply_state", "Safe");
        facts.put("dmi.memory.use", "System Memory");
        facts.put("dmi.system.version", "Not Specified");
        facts.put("memory.memtotal", "1019852");
        facts.put("cpu.on-line_cpu(s)_list", "0");
        facts.put("dmi.system.status", "No errors detected");
        facts.put("dmi.bios.version", "QEMU");
        facts.put("cpu.numa_node(s)", "1");
        facts.put("dmi.chassis.security_status", "Unknown");
        facts.put("virt.host_type", "kvm");
        facts.put("dmi.chassis.type", "Other");
        facts.put("net.interface.eth0.ipaddr", "192.168.0.10");
        facts.put("dmi.processor.type", "Central Processor");
        facts.put("dmi.processor.socket_designation", "CPU 1");
        facts.put("dmi.system.product_name", "KVM");
        facts.put("cpu.byte_order", "Little Endian");
        facts.put("dmi.processor.status", "Populated:Enabled");
        facts.put("cpu.numa_node0_cpu(s)", "0");
        facts.put("cpu.core(s)_per_socket", "1");
        facts.put("dmi.memory.size", "1024 MB");
        facts.put("network.ipaddr", "192.168.0.10");
        facts.put("dmi.processor.family", "Other");
        facts.put("cpu.cpu_family", "6");
        allFacts.put("facts", facts);
        allFacts.put("name", hostname);
        allFacts.put("type", "system");
        return allFacts;
    }


	@Override
	public List<KatelloTask> getTasks(String org_name)
			throws KatelloApiException {
		// TODO Auto-generated method stub
        ClientResponse<List<KatelloTask>> _return = null;
        _return = orgResource.listTasks(org_name);
        if ( _return.getStatus() > 299 ) throw new KatelloApiException(_return);
        return _return.getEntity();
	}

    @Override
    public KatelloTask getTask(String uuid) throws KatelloApiException {
        ClientResponse<KatelloTask> _return = null;
        _return = taskResource.getTask(uuid);
        if ( _return.getStatus() > 299 ) throw new KatelloApiException(_return);
        return _return.getEntity();
    }

}
