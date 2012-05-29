package com.redhat.qe.katello.tasks;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.common.KatelloConstants;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.ExecCommands;

/**
 * Various utility tasks regarding Katello (+components) functionality.
 * @author gkhachik
 * @since 14.Feb.2011
 *
 */
public class KatelloTasks {
	protected static Logger log = 
		Logger.getLogger(KatelloTasks.class.getName());
	private ExecCommands localCommandRunner = null;
// # ************************************************************************* #
// # PUBLIC section                                                            #
// # ************************************************************************* #	
	public KatelloTasks(ExecCommands localRunner) {
		setLocalCommandRunner(localRunner);
	}

	public void setLocalCommandRunner(ExecCommands runner) {
		localCommandRunner = runner;
	}
	
	/** curl -s -u ${username}:${password} -H \"Accept: application/json\" 
	 * -H \"content-type: application/json\" -d \"${content}\" 
	 * -X POST http://${servername}:${port}/api${call}<br>
	 * @param content Call content in JSON format
	 * @param call Relative path of the call, 
	 * e.g. "/organizations/&lt;orgid&gt;/environments"
	 * @return The output string of the call 
	 */
	public String apiKatello_POST(
			String content, String call) throws IOException{
		Object[] call_args ={
				System.getProperty("katello.admin.user", "admin"),
				System.getProperty("katello.admin.password", "admin"),
				content,
				System.getProperty("katello.server.hostname", "localhost"),
				call};
		String url = KatelloConstants.KATELLO_HTTP_POST;
		String mCall = MessageFormat.format(url, call_args);
		return this.execute_local(true, mCall);
	}
	
	public String apiKatello_POST_manifest(
			String manifest, String call) throws IOException{
		Object[] call_args ={
				System.getProperty("katello.admin.user", "admin"),
				System.getProperty("katello.admin.password", "admin"),
				manifest,
				System.getProperty("katello.server.hostname", "localhost"),
				call};
		String url = KatelloConstants.KATELLO_HTTP_POST_MANIFEST;
		String mCall = MessageFormat.format(url, call_args);
		return this.execute_local(true, mCall);
	}
	
	/** curl -s -u ${username}:${password} -H \"Accept: application/json\" 
	 * -H \"content-type: application/json\" -d \"${content}\" 
	 * -X PUT http://${servername}:${port}/api${call}<br>
	 * @param content Call content in JSON format
	 * @param call Relative path of the call, 
	 * e.g. "/organizations/&lt;orgid&gt;/environments"
	 * @return The output string of the call 
	 */
	public String apiKatello_PUT(
			String content, String call) throws IOException{
		Object[] call_args ={
				System.getProperty("katello.admin.user", "admin"),
				System.getProperty("katello.admin.password", "admin"),
				content,
				System.getProperty("katello.server.hostname", "localhost"),
				call};
		String url = KatelloConstants.KATELLO_HTTP_PUT;
		String mCall = MessageFormat.format(url, call_args);
		return this.execute_local(true, mCall);
	}

	public String apiKatello_DELETE(String call) throws IOException{
		Object[] call_args ={
				System.getProperty("katello.admin.user", "admin"),
				System.getProperty("katello.admin.password", "admin"),
				System.getProperty("katello.server.hostname", "localhost"),
				call};
		String url = KatelloConstants.KATELLO_HTTP_DELETE;
		String mCall = MessageFormat.format(url, call_args);
		return this.execute_local(true, mCall);
	}
	
	public JSONObject getEnvironment(String orgName, String envName){
		JSONObject _return = null;
		try{
			log.info(String.format("Retrieve environment: [%s] of Org: [%s]", envName, orgName));
			KatelloEnvironment _env = new KatelloEnvironment(null, null, orgName, null);
			JSONArray envs = KatelloTestScript.toJSONArr(_env.api_list().getStdout());
			JSONObject env;
			for(int i=0;i<envs.size();i++){
				env = (JSONObject)envs.get(i);
				if(((String)env.get("name")).equals(envName))
					return env;
			}
		}catch (Exception e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
		return _return;		
	}
	
	public String deleteEnvironment(String orgName, String envName){
		String _return = null;
		try{
			String env_id = ((Long)getEnvironment(orgName, envName).get("id")).toString();
			_return = apiKatello_DELETE(String.format("/organizations/%s/environments/%s",orgName,env_id));
			log.info(String.format("Deleted the environment [%s] of org: [%s]",envName,orgName));
		}catch (Exception e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
		return _return;		
	}
	
	/**
	 * Retrieves JSON object of environment from the organization details.
	 * @param orgName Organization name
	 * @param envName Environment name we are interested.
	 * @return JSONObject representation of env. object in Org's details.
	 * @author gkhachik
	 * @since 16.Feb.2011 
	 */
	public JSONObject getEnvFromOrgList(String orgName, String envName){
		
		String str_envs = new KatelloEnvironment(null, null, orgName, null).api_list().getStdout();
		JSONArray json_envs = KatelloTestScript.toJSONArr(str_envs);
		for(int i=0;i<json_envs.size();i++){
			JSONObject json_env = (JSONObject)json_envs.get(i);
			if(json_env.get("name").equals(envName)){
				return json_env;
			}
		}
		return null;
	}
	
	public String createProvider(String org_name, String provider_name, 
			String descr, String type){
		String _return = null;
		Object[] json_args ={
				org_name,provider_name,descr,type};
		
		String mCall = String.format(
				KatelloConstants.JSON_CREATE_PROVIDER, json_args);
		try{
			_return = apiKatello_POST(mCall, "/providers");
			log.info(String.format("Created a provider with: " +
					"name=[%s]; description=[%s]; " +
					"provider_type=[%s]", 
					provider_name,descr,type));
		}catch (Exception e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
		return _return;
	}

	public String createProvider(String org_name, String provider_name, 
			String descr, String type, String url){
		String _return = null;
		Object[] json_args ={
				org_name,provider_name,descr,type, url};
		
		String mCall = String.format(
				KatelloConstants.JSON_CREATE_PROVIDER_WITH_URL, json_args);
		try{
			_return = apiKatello_POST(mCall, "/providers");
			log.info(String.format("Created a provider with: " +
					"name=[%s]; description=[%s]; " +
					"provider_type=[%s], repository_url=[%s]", 
					provider_name,descr,type,url));
		}catch (Exception e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
		return _return;
	}
	
	public String createProduct(String org_name, String provider_name, 
			String product_name, String product_descr, String product_url){
		String _return = null;
		Object[] json_args ={
				product_name,product_descr,product_url};
		
		String mCall = String.format(
				KatelloConstants.JSON_CREATE_PRODUCT_WITH_URL, json_args);
		try{
			String provider_id = ((Long)getProvider(org_name, provider_name).get("id")).toString();
			_return = apiKatello_POST(mCall, "/providers/"+provider_id+"/product_create");
			log.info(String.format("Created a product for provider: [%s] with: " +
					"name=[%s]; description=[%s]; " +
					"url=[%s]", 
					provider_name,product_name,product_descr,product_url));
		}catch (Exception e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
		return _return;
	}

	public String createRepository(String providerName, String candlepin_id, 
			String repo_name, String repo_url){
		String _return = null;
		Object[] json_args ={
				repo_name,candlepin_id, repo_url};
		
		String mCall = String.format(
				KatelloConstants.JSON_CREATE_REPO_WITH_URL, json_args);
		try{
			_return = apiKatello_POST(mCall, "/repositories");
			log.info(String.format("Created a repo for provider: [%s] with: " +
					"name=[%s]; " +
					"product_id=[%s]; "+
					"url=[%s]", 
					providerName,repo_name,candlepin_id,repo_url));
		}catch (Exception e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
		return _return;
	}
		
	public String import_products(String provider_id, String products_json){
		String _return=null;
		try{
			_return = apiKatello_POST(products_json, 
					"/providers/"+provider_id+"/import_products");
			log.info(String.format("Importing product(s) for provider: id=[%s]", 
					provider_id));
		}catch (Exception e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
		return _return;		
	}
	
	public JSONObject getProductByOrg(String orgName, String productName){
		JSONObject prod =null;
		try{
			KatelloProduct _prod = new KatelloProduct(null, orgName, null, null, null, null, null, null);
			JSONArray jprods = KatelloTestScript.toJSONArr(_prod.api_list().getStdout()); 
			if(jprods ==null) return null;
			log.info(String.format("Get product: name=[%s]",productName));
			for(int i=0;i<jprods.size();i++){
				prod = (JSONObject)jprods.get(i);
				if(((String)prod.get("name")).equals(productName))
					return prod;
			}
		}catch (Exception e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
		return null;
	}
	
	public String subscribeConsumer(String consumerID, String poolID){
		String _return=null;
		try{
			_return = apiKatello_POST("", 
					"/consumers/"+consumerID+"/entitlements?pool="+poolID);
			log.info(String.format("Subscribing consumer: [%s] to the pool: [%s]", 
					consumerID,poolID));
		}catch (Exception e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
		return _return;		
	}
	
	public JSONObject getProvider(String org_name, String byName){
		JSONArray providers = KatelloTestScript.toJSONArr(
				new KatelloProvider(null, org_name, null, null).api_list(org_name).getStdout());
		JSONObject tmpProv;
		for(int i=0;i<providers.size();i++){
			tmpProv = (JSONObject)providers.get(i);
			if(tmpProv.get("name").equals(byName))
				return tmpProv;
		}
		return null;
	}

	public String deleteProvider(String orgName, String providerName){
		String _return=null;
		String provider_id = ((Long)getProvider(orgName, providerName).get("id")).toString();
		try{
			_return = apiKatello_DELETE("/providers/"+provider_id);
			log.info("Delete provider: name=["+providerName+"]; id=["+provider_id+"]");
		}catch (Exception e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
		return _return;
	}

	public String createConsumer(
			String orgName, String hostname, String uuid, String json_filename){
		String _return=null;
		try{
			String sFacts = "{}";
			try{
				BufferedReader br = new BufferedReader(new FileReader(json_filename));
				sFacts=br.readLine();
				br.close();
			}catch(IOException iex){
				log.severe(iex.getMessage());
				throw new RuntimeException(iex);
			}
			// Replace the values in facts-virt.json
			sFacts = sFacts.replaceAll("\\$\\{HOSTNAME\\}", hostname);
			sFacts = sFacts.replaceAll("\\$\\{UUID\\}", uuid);
			sFacts = sFacts.replaceAll("\\$\\{ORG_NAME\\}", orgName);
			_return = apiKatello_POST(sFacts, "/consumers?owner="+orgName);
//			_return = apiKatello_POST_candlepinOwner(sFacts, 
//					"/consumers?owner="+orgName);
			log.info(String.format("Creating consumer from [%s] template with: uuid=[%s]; hostname=[%s]; org_name=[%s]", 
					json_filename,uuid, hostname,orgName));
		}catch (Exception e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
		return _return;		
	}
	
	public String deleteConsumer(String consumer_id){
		String _return=null;
		try{
			_return = apiKatello_DELETE("/consumers/"+consumer_id);
			log.info(String.format("Remove consumer: uuid=[%s]", 
					consumer_id));
		}catch (Exception e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
		return _return;		
	}

	public String unsubscribeConsumer(String consumer_id, String serial){
		String _return=null;
		try{
			_return = apiKatello_DELETE("/consumers/"+consumer_id+"/certificates/"+serial);
			log.info(String.format("Unsubscribe consumer: uuid=[%s] from the product with: serial=[%s]", 
					consumer_id,serial));
		}catch (Exception e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
		return _return;		
	}

	/**
	 * Unsubscribes from ALL (--all option in rhsm)
	 * @param consumer_id
	 * @return
	 */
	public String unsubscribeConsumer(String consumer_id){
		String _return=null;
		try{
			_return = apiKatello_DELETE("/consumers/"+consumer_id+"/entitlements");
			log.info(String.format("Unsubscribe consumer: uuid=[%s] from all entitlements", 
					consumer_id));
		}catch (Exception e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
		return _return;		
	}
	
	
// # ************************************************************************* #
// # PRIVATE section                                                           #
// # ************************************************************************* #	
	/**
	 * In order to make the ExecCommands instance available to run 
	 * JSON format line commands,
	 * here is made a workaround: put command on file and "sh &lt;file&gt;" it.
	 * @param showLogResults True/False to log/output the result returned
	 * @param command The command line to be executed locally
	 * @return Output of the command run
	 * @author gkhachik
	 * @since 15.Feb.2011
	 */
	private String execute_local(boolean showLogResults, String command){
		String out = null; String tmp_cmdFile = "/tmp/katello-json.sh";
		try{
			// cleanup the running buffer file.
			this.localCommandRunner.submitCommandToLocalWithReturn(false, 
					"rm -f "+tmp_cmdFile,"");

			FileOutputStream fout = 
				new FileOutputStream(tmp_cmdFile);
			fout.write((command+"\n").getBytes());fout.flush();fout.close();
			log.finest(String.format("Executing local: [%s]",command));
			out = this.localCommandRunner.submitCommandToLocalWithReturn(
					false, "sh "+tmp_cmdFile, ""); // HERE is the run
			
			if(showLogResults){ // log output if specified so.
				// split the lines and out each line.
				String[] split = out.split("\\n");
				for(int i=0;i<split.length;i++){
					log.info("Output: "+split[i]);
				}
			}
			
			// cleanup the running buffer file.
			this.localCommandRunner.submitCommandToLocalWithReturn(false, 
					"rm -f "+tmp_cmdFile,"");
		}catch(IOException iex){
			log.log(Level.SEVERE, iex.getMessage(), iex);
		}
		return out;
	}
	
	public static String run_local(boolean showLogResults, String command){
		String out = null; String tmp_cmdFile = "/tmp/katello-"+KatelloTestScript.getUniqueID()+".sh";
		ExecCommands localRunner = new ExecCommands();
		try{
			// cleanup the running buffer file - in case it would exist
			localRunner.submitCommandToLocalWithReturn(false, 
					"rm -f "+tmp_cmdFile,"");
			FileOutputStream fout = 
				new FileOutputStream(tmp_cmdFile);
			fout.write((command+"\n").getBytes());fout.flush();fout.close();
			log.finest(String.format("Executing local: [%s]",command));
			out = localRunner.submitCommandToLocalWithReturn(
					false, "sh "+tmp_cmdFile, ""); // HERE is the run
			
			if(showLogResults){ // log output if specified so.
				// split the lines and out each line.
				String[] split = out.split("\\n");
				for(int i=0;i<split.length;i++){
					log.info("Output: "+split[i]);
				}
			}
		}catch(IOException iex){
			log.log(Level.SEVERE, iex.getMessage(), iex);
		}finally{
			// cleanup the running buffer file.
			try{localRunner.submitCommandToLocalWithReturn(false, 
					"rm -f "+tmp_cmdFile,"");
			}catch(IOException ie){log.log(Level.SEVERE, ie.getMessage(), ie);}
		}
		return out;
	}
	
	public long getDiskFreeForPulpRepos(){
		long dfPulpRepos=Long.MAX_VALUE;
		String res = KatelloUtils.sshOnServer("df `grep \"Alias /pulp/repos\" /etc/httpd/conf.d/pulp.conf | awk '{print $3}'` | tail -1 | awk '{print $3}'").getStdout().trim();
		log.fine("Free disk space for Pulp repositories: ["+res+"]");
		dfPulpRepos = new Long(res).longValue();
		return dfPulpRepos;
	}
	
	public void waitfor_katello(){
		KatelloUtils.sshOnServer("python -c \"from katello.utils import waitfor_katello; waitfor_katello()\"");
	}

	public static String grepCLIOutput(String property, String output){
		return grepCLIOutput(property, output, 1);
	}
	
	public static String grepCLIOutput(String property, String output, int occurence){
		int meet_cnt = 0;
		String[] lines = output.split("\\n");
		for(int i=0;i<lines.length;i++){
			if(lines[i].startsWith(property)){ // our line
				meet_cnt++;
				if(meet_cnt == occurence){
					String[] split = lines[i].split(":\\s+");
					if(split.length<2){
						return lines[i+1].trim();
					}else{
						return split[1].trim();
					}
				}
			}
		}
		log.severe("ERROR: Output can not be extracted for the property: ["+property+"]");
		return null;
	}
	
}
