package com.redhat.qe.katello.common;

import java.util.HashMap;
import java.util.Map;

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
	public static final String PULP_RHEL6_x86_64_REPO = 
		"http://repos.fedorapeople.org/repos/pulp/pulp/v1/stable/6Server/x86_64/";
	public static final String PULP_RHEL6_i386_REPO = 
		"http://repos.fedorapeople.org/repos/pulp/pulp/v1/stable/6Server/i386/";
	public static final String PULP_RHEL6_REPO = 
		"http://repos.fedorapeople.org/repos/pulp/pulp/v1/stable/6Server/";
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

	public static final String TNG_CFSE_CLI = "cfse-cli";
	public static final String TNG_PRE_UPGRADE = "pre-upgrade";
	public static final String TNG_UPGRADE = "upgrade";
	public static final String TNG_POST_UPGRADE = "post-upgrade";

	public static final String TNG_PRE_BACKUP = "pre-backup";
	public static final String TNG_BACKUP = "backup";
	public static final String TNG_POST_RECOVERY = "post-recovery";
	
	public static final String KATELLO_DEFAULT_LOCALE = "en_US";
	
	public static final String DELTACLOUD_SERVER_IMAGE_ID = "2e477879-c1d3-4fe0-a5a3-493bccdde031";
	public static final String DELTACLOUD_CLIENT_IMAGE_ID = "2e477879-c1d3-4fe0-a5a3-493bccdde031";
	
	public static final String[][] DELTACLOUD_CLIENTS = new String[][]{{"cfseclient1", "usersys.redhat.com", "335f800994d9adc5947b49728b14d527"}, {"cfseclient2", "usersys.redhat.com", "c9c0395f0730624541d8d26f7eb11be8"}, {"cfseclient3", "usersys.redhat.com", "edcf8c472ca601e33326f50f5d46ba8e"}};
	public static final String[][] DELTACLOUD_SERVERS = new String[][]{{"cfseserver1", "usersys.redhat.com", "502f89b3921dcff92ccdfcf91a6e7db9", "10.16.120.72", "00:1a:4a:10:78:36"}};
	
}
