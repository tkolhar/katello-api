package com.redhat.qe.katello.base;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;

import com.redhat.qe.katello.base.obj.KatelloContentDefinition;
import com.redhat.qe.katello.base.obj.KatelloContentView;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.base.obj.KatelloUser;
import com.redhat.qe.katello.base.threading.KatelloCliWorker;
import com.redhat.qe.katello.base.threading.KatelloCliWorkersPool;
import com.redhat.qe.katello.common.KatelloConstants;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

@JsonIgnoreProperties(ignoreUnknown=true)
public class KatelloCliTestBase 
extends com.redhat.qe.auto.testng.TestScript 
implements KatelloConstants {
	private static ResourceBundle messageBundle = null;
	private static ResourceBundle inputBundle = null;
	private static final String messageFileName = "messages";
	private static final String inputFileName = "inputs";
	
	private static KatelloCliWorkersPool cliPool; 

	protected static Logger log = Logger.getLogger(KatelloCliTestBase.class.getName());
	protected KatelloCliWorker cli_worker;
	protected static SSHCommandResult exec_result = null;
	
	// base_*** stuff to be reused (if you want ;)
	protected static String base_org_name = null;
	protected static String base_dev_env_name = null;
	protected static String base_test_env_name = null;
	protected static String base_prod_env_name = null;
	protected static String base_zoo_provider_name = null;
	protected static String base_zoo_product_name = null;
	protected static String base_zoo_product_id = null;
	protected static String base_zoo_repo_name = null;
	protected static String base_zoo_repo_pool = null;
	protected static String base_zoo4_provider_name = null;
	protected static String base_zoo4_product_name = null;
	protected static String base_zoo4_product_id = null;
	protected static String base_zoo4_repo_name = null;
	protected static String base_zoo4_repo_pool = null;
	protected static String base_pulp_provider_name = null;
	protected static String base_pulp_product_name = null;
	protected static String base_pulp_product_id = null;
	protected static String base_pulp_repo_name = null;
	protected static String base_pulp_repo_pool = null;
	
	@BeforeSuite(alwaysRun=true)
	public void checkOperational(){
		String tngSuite = System.getProperty("testng.testnames", "unknown"); 
		if(!(tngSuite.equals("CLI_Tests")||
				tngSuite.equals("E2E_Tests")||
				tngSuite.equals("Hammer_CLI_Tests")||
				tngSuite.equals("Longrun_Tests"))) return; // not one of them... return
		
		String clientHostname = System.getProperty("katello.client.hostname", "localhost");
		String adminUsername = System.getProperty("katello.admin.user", "admin");
		String adminPassword = System.getProperty("katello.admin.password", "admin");

		SSHCommandResult exec_result;
		exec_result = KatelloUtils.sshOnClient(clientHostname, String.format(
				"rpm -q katello-cli && katello -u%s -p%s ping",adminUsername,adminPassword));
		if(exec_result.getExitCode().intValue()!=0) 
			throw new SkipException("Your Katello system seems not operational. Katello CLI ping fails.");

		exec_result = KatelloUtils.sshOnClient(clientHostname, String.format(
				"rpm -q subscription-manager && subscription-manager orgs --username %s --password %s",
				adminUsername,adminPassword));
		if(exec_result.getExitCode().intValue()!=0) 
			throw new SkipException("Your Katello system seems not operational. RHSM fails.");

		exec_result = KatelloUtils.sshOnClient(clientHostname, 
				"rpm -q rubygem-hammer_cli rubygem-hammer_cli_foreman && hammer --output base organization list");
		if(exec_result.getExitCode().intValue()!=0) 
			throw new SkipException("Your Katello system seems not operational. Hammer CLI ping fails.");
	}
	
	@BeforeClass(alwaysRun=true)
	public void setUpSuper(){
		KatelloUtils.sleepAsHuman();
		cliPool = KatelloCliWorkersPool.getInstance(null);
		
		// Eclipse mode - get default from property file; init base org and exit.
		if(cliPool==null){ 
			cli_worker = KatelloCliWorker.getSingleMode(); 
			createBaseOrg(this.getClass().getName(), cli_worker);
			return;
		}
		
		cli_worker = cliPool.getWorker(Thread.currentThread().getName(),this.getClass().getName());
		if(cli_worker == null && cliPool.running()){
			log.info(">>> no workers for: "+this.getClass().getName()+" : thread "+Thread.currentThread().getName());
		} // log for protocol and go to sleeping.
		while(cli_worker == null && cliPool.running()){
			try{Thread.sleep(NOWORKER_SLEEP);}catch(InterruptedException iex){}
			cli_worker = cliPool.getWorker(Thread.currentThread().getName(),this.getClass().getName());
		}
		if(!cliPool.running()){
			throw new SkipException("Timeout happened on requesting worker for: "+this.getClass().getName());
		}
		createBaseOrg(this.getClass().getName(), cli_worker); // wait worker to be initialized and invoke it at the very end. there is if (null) - so it would work only on the first invoking. 
	}
	
	protected SSHCommandResult sshOnClient(String _cmd){
		return KatelloUtils.sshOnClient(cli_worker.getClientHostname(), _cmd);
	}
	
	protected void rhsm_clean(){
		rhsm_clean(cli_worker.getClientHostname());
	}
	
	protected void rhsm_clean_only(){
		rhsm_clean_only(cli_worker.getClientHostname());
	}
	
	protected void yum_clean(){
		yum_clean(cli_worker.getClientHostname());
	}
		
	protected void assert_providerRemoved(KatelloProvider prov){
		SSHCommandResult res;
		log.info("Assertions: provider has been removed");
		res = prov.info();
		Assert.assertTrue(res.getExitCode().intValue()==65, "Check - return code");
		Assert.assertEquals(getOutput(res).trim(), "Could not find provider [ "+prov.name+" ] " +
				"within organization [ "+prov.org+" ]", "Check - `provider info` return string");
	}
	
	protected void assert_repoSynced(KatelloRepo repo){
		SSHCommandResult res;
		String REGEXP_REPO_INFO;
		log.info("Assertions: repository has been synchronized");
		res = repo.info();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		
		// pacakge_count != 0
		REGEXP_REPO_INFO = ".*Name\\s*:\\s+"+repo.name+".*Package Count\\s*:\\s+0.*";
		Assert.assertFalse(getOutput(res).replaceAll("\n", "").matches(REGEXP_REPO_INFO), 
				"Repo should not contain packages count: 0");
		// last_sync != never
		REGEXP_REPO_INFO = ".*Name\\s*:\\s+"+repo.name+".*Last Sync\\s*:\\s+never.*";
		Assert.assertFalse(getOutput(res).replaceAll("\n", "").matches(REGEXP_REPO_INFO), 
				"Repo should not contain last_sync == never");
		
		// package_count >0; url, progress, last_sync
		String cnt = KatelloUtils.grepCLIOutput("Package Count", res.getStdout());
		Assert.assertTrue(new Integer(cnt).intValue()>0, "Repo should contain packages count: >0");
	}

	protected void waitfor_packagecount(KatelloRepo repo, int timeoutMinutes){
		long now = Calendar.getInstance().getTimeInMillis() / 1000;
		long start = now;
		long maxWaitSec = start + (timeoutMinutes * 60);
		log.fine("Waiting repo package count available for: minutes=["+timeoutMinutes+"]; " +
				"org=["+repo.org+"]; product=["+repo.product+"]; repo=["+repo.name+"]");
		while(now<maxWaitSec){
			now = Calendar.getInstance().getTimeInMillis() / 1000;
			if(!KatelloUtils.grepCLIOutput("Package Count", getOutput(repo.status())).equals("0"))
				break;
			try{Thread.sleep(60000);}catch (Exception e){}
		}
		if(now<=maxWaitSec)
			log.fine("Repo package count available in: ["+String.valueOf((Calendar.getInstance().getTimeInMillis() / 1000) - start)+"] sec");
		else
			log.warning("Repo package count still not available after: ["+String.valueOf(maxWaitSec - start)+"] sec");
	}
	
	protected void waitfor_reposync(KatelloRepo repo, int timeoutMinutes){
		SSHCommandResult res;
		long now = Calendar.getInstance().getTimeInMillis() / 1000;
		long start = now;
		long maxWaitSec = start + (timeoutMinutes * 60);
		String REGEXP_STATUS_FINISHED = ".*Sync State:\\s+Finished.*";
		log.fine("Waiting repo sync finish for: minutes=["+timeoutMinutes+"]; " +
				"org=["+repo.org+"]; product=["+repo.product+"]; repo=["+repo.name+"]");
		while(now<maxWaitSec){
			res = repo.status();
			now = Calendar.getInstance().getTimeInMillis() / 1000;
			if(getOutput(res).replaceAll("\n", "").matches(REGEXP_STATUS_FINISHED))
				break;
			try{Thread.sleep(60000);}catch (Exception e){}
		}
		if(now<=maxWaitSec)
			log.fine("Repo sync done in: ["+String.valueOf((Calendar.getInstance().getTimeInMillis() / 1000) - start)+"] sec");
		else
			log.warning("Repo sync did not finished after: ["+String.valueOf(maxWaitSec - start)+"] sec");
	}
	
	protected void waitfor_reposync(KatelloRepo repo, String lastsynced, int timeoutMinutes) {
		SSHCommandResult res;
		long now = Calendar.getInstance().getTimeInMillis() / 1000;
		long start = now;
		long maxWaitSec = start + (timeoutMinutes * 60);
		log.fine("Waiting repo sync finish for: minutes=["+timeoutMinutes+"]; " +
				"org=["+repo.org+"]; product=["+repo.product+"]; repo=["+repo.name+"]");
		while(now<maxWaitSec){
			res = repo.info();
			now = Calendar.getInstance().getTimeInMillis() / 1000;
			String newsync = KatelloUtils.grepCLIOutput("Last Sync", getOutput(res).trim(),1);
			if(!lastsynced.equals(newsync))
				break;
			try{Thread.sleep(60000);}catch (Exception e){}
		}
		if(now<=maxWaitSec)
			log.fine("Repo sync done in: ["+String.valueOf((Calendar.getInstance().getTimeInMillis() / 1000) - start)+"] sec");
		else
			log.warning("Repo sync did not finished after: ["+String.valueOf(maxWaitSec - start)+"] sec");
	}

	protected void waitfor_repodata(KatelloRepo repo, int timeoutMinutes){
		SSHCommandResult res;
		long now = Calendar.getInstance().getTimeInMillis() / 1000;
		long start = now;
		long maxWaitSec = start + (timeoutMinutes * 60);
		StringBuilder path = new StringBuilder();
		path.append("/var/lib/pulp/working/repos/");
		path.append(repo.org.trim().replaceAll(" ", "_"));
		path.append("-");
		path.append(repo.product.trim().replaceAll(" ", "_"));
		path.append("-");
		path.append(repo.name.trim().replaceAll(" ", "_"));
		path.append("/importers/yum_importer/");
		path.append(repo.org.trim().replaceAll(" ", "_"));
		path.append("-");
		path.append(repo.product.trim().replaceAll(" ", "_"));
		path.append("-");
		path.append(repo.name.trim().replaceAll(" ", "_"));
		path.append("/repodata");
		while(now<maxWaitSec){
			try{Thread.sleep(10000);}catch (Exception e){}
			res = KatelloUtils.sshOnServer("ls -la " + path.toString());
			now = Calendar.getInstance().getTimeInMillis() / 1000;
			if(getOutput(res).replaceAll("\n", "").contains("repomd.xml"))
				break;			
		}
		if(now<=maxWaitSec)
			log.fine("Repodata is available in: ["+String.valueOf((Calendar.getInstance().getTimeInMillis() / 1000) - start)+"] sec");
		else
			log.warning("Repodata is not available after: ["+String.valueOf(maxWaitSec - start)+"] sec");
	}
	
	/**
	 * Returns list of org names that have imported a manifest that has subscriptions for:<BR>
	 * Red Hat Enterprise Linux Server<BR>
	 * Id: 69
	 * @return empty list or names of the orgs
	 */
	protected ArrayList<String> getOrgsWithImportedManifest(){
		ArrayList<String> orgs = new ArrayList<String>();
		String servername = System.getProperty("katello.server.hostname", "localhost");
		log.info("Scanning ["+servername+"] for organizations with imported manifest");
		KatelloCli cli = new KatelloCli("org list -v | grep \"^Name\" | cut -d: -f2", null);
		SSHCommandResult res = cli.run();
		String[] lines = getOutput(res).split("\n");
		for(String org: lines){
			org = org.trim();
			res = new KatelloCli("product list --provider=\""+KatelloProvider.PROVIDER_REDHAT+
					"\" --org \""+org+"\" -v | grep \"^Id:\\s\\+69\" | wc -l",null).run();
			if(getOutput(res).equals("1")){
				orgs.add(org);
			}
		}
		return orgs;
	}
	
	protected boolean hasOrg_environment(String org, String environment){
		log.info(String.format("Check if the org [%s] has an environment [%s]",org,environment));
		SSHCommandResult res = new KatelloCli("environment list"+
				"\" --org \""+org+"\" -v | grep \"^Name\\s*:\\s\\+"+environment+"\" | wc -l",null).run();
		return getOutput(res).equals("1");
	}
	
	protected void rhsm_clean(String client){
		log.info("RHSM -> unsubscribe, unregister, clean");
		KatelloUtils.sshOnClient(client, 
				"subscription-manager unsubscribe --all; " +
				"subscription-manager unregister; " +
				"subscription-manager clean");
	}
	
	protected void rhsm_clean_only(String client){ 
		log.info("RHSM -> clean");
		KatelloUtils.sshOnClient(client, "subscription-manager clean");
	}

	
	protected void yum_clean(String client) {
		KatelloUtils.sshOnClient(client, "yum clean all; yum repolist");
	}
	
	/**
	 * Seems good one. Reworked recently to read rhsm username/password from the java properties
	 * @param org Organization name
	 * @param environment The environment
	 * @param name system name. take care to have it unique
	 * @param autosubscribe if true, then will add --autosubscribe option. Take care about your /etc/pki/product/*.pem files.
	 * @return res
	 * @author Garik Khachikyan &lt;gkhachik@redhat.com&gt;
	 */
	protected SSHCommandResult rhsm_register(String client, String org, String environment, String name, boolean autosubscribe){
		String rhsmUser = System.getProperty("katello.admin.user", KatelloUser.DEFAULT_ADMIN_USER);
		String rhsmPass = System.getProperty("katello.admin.password", KatelloUser.DEFAULT_ADMIN_PASS);
		log.info("Registering client with: --org \""+org+"\" --environment \""+environment+"\" " +
				"--name \""+name+"\" --autosubscribe "+Boolean.toString(autosubscribe));
		String cmd = String.format(
				"subscription-manager register --username \"%s\" --password \"%s\" --org \"%s\" --environment \"%s\" --name \"%s\"",
				rhsmUser, rhsmPass,org,environment,name);
		if(autosubscribe)
			cmd += " --autosubscribe";
		return KatelloUtils.sshOnClient(client, cmd);
	}
	
	protected String getOutput(SSHCommandResult res){
		return sgetOutput(res);
	}
	
	public static String sgetOutput(SSHCommandResult res){
		return (res.getStdout()+"\n"+res.getStderr()).trim();
	}
	
	/**
	 * Returns the localized message value of provided key.
	 * The second argument args are optional, which are used for missing values (%s) in message text.  
	 * It requires to specify "katello.locale" parameter while running tests, otherwise "KATELLO_DEFAULT_LOCALE" default value should be used.
	 * It lookups in two different message.properties files, first is inputs file, where are kept texts to send to katello as input parameter in CLI.
	 * Second file is contains output messages of katello to verify them in different locale.
	 * It is static method and initializes ResourceBoundles for both messages ".properties" files.
	 * After initializing in clears the cache from previous run. "en_US"
	 */
	public static String getText(String key, Object...args) {
		if (messageBundle == null || inputBundle == null) {
			String localeStr = System.getProperty("katello.locale", KATELLO_DEFAULT_LOCALE);
			String[] split = localeStr.split("_", 2);					
			Locale locale = new Locale(split[0], split[1]);
			
			messageBundle = ResourceBundle.getBundle(messageFileName, locale);
			inputBundle = ResourceBundle.getBundle(inputFileName, locale);
			ResourceBundle.clearCache(); //this is mandatory
		}
		if (messageBundle.containsKey(key)) {
			return String.format(messageBundle.getLocale(), messageBundle.getString(key), args);
		} else if (inputBundle.containsKey(key)) {
			return String.format(inputBundle.getLocale(), inputBundle.getString(key), args);
		} else {
			log.warning("Message by key: " + key + " not found in locale " + messageBundle.getLocale());
			return null;
		}
	}
	
	/**
	 * Installs array of packages on client system and verifies that they are installed.  
	 * @param pkgNames
	 */
	protected void install_Packages(String client, String[] pkgNames) {
		StringBuffer install = new StringBuffer();
		for (String pkg : pkgNames) {
	        install.append(pkg);
	        install.append(" ");
		}
		SSHCommandResult res = KatelloUtils.sshOnClient(client, "yum install -y "+ install.toString());
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		res = KatelloUtils.sshOnClient(client, "rpm -qa | grep -E \"" + install.toString().trim().replace(" ", "|") + "\"");
		for (String pkg : pkgNames) {
			Assert.assertTrue(getOutput(res).contains(pkg), "Package " + pkg + " should be installed");
		}
	}
	
	/**
	 * Check that packages are not available to install on client.
	 * @param pkgNames
	 */
	protected void verify_PackagesNotAvailable(String client, String[] pkgNames) {
		StringBuffer install = new StringBuffer();
		for (String pkg : pkgNames) {
	        install.append(pkg);
	        install.append(" ");
		}
		SSHCommandResult res = KatelloUtils.sshOnClient(client, "yum install -y "+ install.toString());
		for (String pkg : pkgNames) {
			Assert.assertTrue(getOutput(res).trim().contains("No package " + pkg + " available."), "Package " + pkg + " should not be available to install.");
		}
	}
	
	private static synchronized void createBaseOrg(String classname, KatelloCliWorker cli_worker){
		
		if (base_org_name == null) {
			if(!classname.contains("tests.cli.")&&
				!classname.contains("tests.e2e.")) 
				return;

			String uid = KatelloUtils.getUniqueID();
			base_org_name = "CLI Test Org " + uid;
			base_dev_env_name = "CLI Dev env " + uid;
			base_test_env_name = "CLI Test env " + uid;
			base_prod_env_name = "CLI Prod env " + uid;
			base_zoo_provider_name = "CLI Zoo Prov " + uid;
			base_zoo_product_name = "CLI Zoo Prod " + uid;
			base_zoo_repo_name = "CLI Zoo Repo " + uid;
			base_zoo4_provider_name = "CLI Zoo4 Prov " + uid;
			base_zoo4_product_name = "CLI Zoo4 Prod " + uid;
			base_zoo4_repo_name = "CLI Zoo4 Repo " + uid;
			base_pulp_provider_name = "CLI Pulp Prov " + uid;
			base_pulp_product_name = "CLI Pulp Prod " + uid;
			base_pulp_repo_name = "CLI Pulp Repo " + uid;
			KatelloOrg org = new KatelloOrg(cli_worker, base_org_name, null);
			exec_result = org.cli_create();
			Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");

			if (System.getProperty("katello.engine", "katello").equals("headpin")) return; // We are done: if `headpin|sam`

			// -- Environment
			KatelloEnvironment env = new KatelloEnvironment(cli_worker, base_dev_env_name, null, base_org_name, KatelloEnvironment.LIBRARY);
			exec_result = env.cli_create();
			Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");
			env = new KatelloEnvironment(cli_worker, base_test_env_name, null, base_org_name, KatelloEnvironment.LIBRARY);
			exec_result = env.cli_create();
			Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");
			env = new KatelloEnvironment(cli_worker, base_prod_env_name, null, base_org_name, KatelloEnvironment.LIBRARY);
			exec_result = env.cli_create();
			Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");
			
			// == == == zoo3 (inecas)
			// -- Provider
			KatelloProvider prov = new KatelloProvider(cli_worker, base_zoo_provider_name, base_org_name, null, null);
			exec_result = prov.create();
			Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
			// -- Product
			KatelloProduct prod = new KatelloProduct(cli_worker, base_zoo_product_name, base_org_name, base_zoo_provider_name, 
					null, null, null, null, null);
			exec_result = prod.create();
			Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
			// -- Repo
			KatelloRepo repo = new KatelloRepo(cli_worker, base_zoo_repo_name, base_org_name, base_zoo_product_name, REPO_INECAS_ZOO3, null, null);
			exec_result = repo.create();
			Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
			exec_result = repo.synchronize();
			Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
			base_zoo_product_id = prod.custom_getProductId();
			Assert.assertNotNull(base_zoo_product_id, "Check - base_zoo_product_id is not null");
			base_zoo_repo_pool = org.custom_getPoolId(base_zoo_product_name);
			Assert.assertNotNull(base_zoo_repo_pool, "Check - pool Id is not null");

			// == == == pulp
			// -- Provider
			prov = new KatelloProvider(cli_worker, base_pulp_provider_name, base_org_name, null, null);
			exec_result = prov.create();
			Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
			// -- Product
			prod = new KatelloProduct(cli_worker, base_pulp_product_name, base_org_name, base_pulp_provider_name, null, null, null, null, null);
			exec_result = prod.create();
			Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
			// -- Repo
			repo = new KatelloRepo(cli_worker, base_pulp_repo_name, base_org_name, base_pulp_product_name, PULP_RHEL6_x86_64_REPO, null, null);
			exec_result = repo.create(true);
			Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
			exec_result = repo.synchronize();
			Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
			base_pulp_repo_pool = org.custom_getPoolId(base_pulp_product_name);
			Assert.assertNotNull(base_zoo_repo_pool, "Check - pool Id is not null");
			base_pulp_product_id = prod.custom_getProductId();
			Assert.assertNotNull(base_pulp_product_id, "Check - base_pulp_product_id is not null");

			// == == == zoo4 (hhovsepy)
			// -- Provider
			prov = new KatelloProvider(cli_worker, base_zoo4_provider_name, base_org_name, "Zoo4 provider", null);
			exec_result = prov.create();
			Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
			// -- Product
			prod = new KatelloProduct(cli_worker, base_zoo4_product_name, base_org_name, base_zoo4_provider_name, null, null, null, null, null);
			exec_result = prod.create();
			Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
			// -- Repo
			repo = new KatelloRepo(cli_worker, base_zoo4_repo_name, base_org_name, base_zoo4_product_name, REPO_HHOVSEPY_ZOO4, null, null);
			exec_result = repo.create(true);
			Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
			exec_result = repo.synchronize();
			Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
			base_zoo4_repo_pool = org.custom_getPoolId(base_zoo4_product_name);
			Assert.assertNotNull(base_zoo4_repo_pool, "Check - pool Id is not null");
			base_zoo4_product_id = prod.custom_getProductId();
			Assert.assertNotNull(base_zoo4_product_id, "Check - base_zoo_product_id is not null");
		}	
	}
	
	protected void promoteEmptyContentView(String org_name, String... env_names) {
		String uid = KatelloUtils.getUniqueID();
		KatelloContentDefinition condef = new KatelloContentDefinition(cli_worker, "def"+uid, null, org_name, null);
		exec_result = condef.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		String content_view = "view"+uid;
		exec_result = condef.publish(content_view, content_view, "view");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		KatelloContentView conview = new KatelloContentView(cli_worker, content_view, org_name);
		
		for (String env_name : env_names) {
			exec_result = conview.promote_view(env_name);
			Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		}
	}
}
