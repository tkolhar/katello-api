package com.redhat.qe.katello.common;

/**
 * Public interface for storing the general Katello constants.
 * @author gkhachik
 * @since 14.Feb.2011
 *
 */
public interface KatelloConstants {
	public static final String KATELLO_PRODUCT = System.getProperty("katello.product", "katello");
	/** Masked string for passwords */
	public static final String SYSOUT_PWD_MASK = "********";

	public static final String KATELLO_SMALL_REPO = 
		"http://repos.fedorapeople.org/repos/katello/katello/fedora-15/x86_64/";
	public static final String PULP_F15_x86_64_REPO = 
		"http://repos.fedorapeople.org/repos/pulp/pulp/v1/testing/fedora-15/x86_64/";
	public static final String PULP_F15_i386_REPO = 
		"http://repos.fedorapeople.org/repos/pulp/pulp/v1/testing/fedora-15/i386/";
	public static final String PULP_F15_REPO = 
		"http://repos.fedorapeople.org/repos/pulp/pulp/v1/testing/fedora-15/";
	public static final String EXPORT_ZIP_PATH = 
		System.getProperty("user.dir") + "/data/export.zip";
	public static final int PRODUCTS_IN_EXPORT_ZIP = 6;
	public static final String AWESOME_SERVER_BASIC = 
		"Awesome OS Server Basic";
	
	// Some repo URLs
	public static final String REPO_INECAS_ZOO3 = "http://inecas.fedorapeople.org/fakerepos/zoo3/";
	
	/** curl -sk -u {username}:{password} 
	 * https://${servername}/api${call} */
	public static final String KATELLO_HTTP_GET =
		"curl -sk -u {0}:{1} https://{2}/"+KATELLO_PRODUCT+"/api{3}";
	
	/** curl -sk -u ${username}:${password} 
	 * -H \"Accept: application/json\" -H \"content-type: application/json\" 
	 * -d \"${content}\" -X PUT https://${servername}:${port}/api${call}*/
	public static final String KATELLO_HTTP_PUT = 
		"curl -sk -u {0}:{1} -H \"Accept: application/json\" " +
		"-H \"content-type: application/json\" -d \"{2}\" " +
		"-X PUT https://{3}/"+KATELLO_PRODUCT+"/api{4}";
	
	/** curl -sk -u ${username}:${password} -H \"Accept: application/json\" 
	 * -H \"content-type: application/json\" -d \"${content}\" 
	 * -X POST https://${servername}:${port}/api${call}*/
	public static final String KATELLO_HTTP_POST = 
		"curl -sk -u{0}:{1} -H \"Accept: application/json\" " +
		"-H \"content-type: application/json\" -d \"{2}\" " +
		"-X POST https://{3}/"+KATELLO_PRODUCT+"/api{4}";

	public static final String KATELLO_HTTP_POST_MANIFEST = 
		"curl -sk -u{0}:{1} -H \"Accept: application/json\" -# " +
		"-X POST -F import=@{2} https://{3}/"+KATELLO_PRODUCT+"/api{4}";
	
	/** curl -sk -u ${username}:${password} -H \"Accept: application/json\" 
	 * -X DELETE https://${servername}:${port}/api${call}*/
	public static final String KATELLO_HTTP_DELETE = 
		"curl -sk -u {0}:{1} -H \"Accept: application/json\" " +
		"-X DELETE https://{2}/"+KATELLO_PRODUCT+"/api{3}";

	/** curl -sk -k -u {username}:{password} 
	 * https://${servername}:${port}/candlepin${call} */
	public static final String CANDLEPIN_HTTP_GET = 
		"curl -sk -u {0}:{1} https://{2}:{3}/candlepin{4}";
	
	/** curl -sk -u {username}:{password} 
	 * https://${servername}:${port}/pulp/api${call} */ 
	public static final String PULP_HTTP_GET =
		"curl -sk -u {0}:{1} https://{2}:{3}/pulp/api{4}";
		
	/**
	 * arguments are:<br>
	 * 0 - name<br>
	 * 1 - description<br>
	 * 2 - provider_type
	 */
	public static final String JSON_CREATE_PROVIDER =
		"{'organization_id':'%s', " +
		"'provider':{" +
		"'name':'%s', " +
		"'description':'%s', " +
		"'provider_type': '%s'}}";
	
	/**
	 * arguments are:<br>
	 * 0 - name<br>
	 * 1 - description<br>
	 * 2 - provider_type
	 */
	public static final String JSON_CREATE_PROVIDER_WITH_URL =
		"{'organization_id':'%s', " +
		"'provider':{" +
		"'name':'%s', " +
		"'description':'%s', " +
		"'provider_type': '%s', " +
		"'repository_url':'%s'}}";

	public static final String JSON_CREATE_PROVIDER_BYORG =
		"{'provider':{" +
		"'name':'%s', " +
		"'description':'%s', " +
		"'provider_type': '%s'}}";

	public static final String JSON_CREATE_PRODUCT_WITH_URL =
		"{'product':{" +
		"'name':'%s', " +
		"'description':'%s', " +
		"'url':'%s'}}";

	public static final String JSON_CREATE_REPO_WITH_URL =
		"{'name':'%s', " +
		"'product_id':'%s', " +
		"'url':'%s'}";
	
	public static final String JSON_CREATE_USER = 
		"{'username':'%s', 'password':'%s', 'disabled':'%s', 'email':'root@localhost'}";

}
