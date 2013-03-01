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

	public static final String[][] DELTACLOUD_CLIENTS = new String[][]{{"cfseclient1", "usersys.redhat.com", "335f800994d9adc5947b49728b14d527"}, 
		{"cfseclient2", "usersys.redhat.com", "c9c0395f0730624541d8d26f7eb11be8"}, 
		{"cfseclient3", "usersys.redhat.com", "edcf8c472ca601e33326f50f5d46ba8e"},
		{"cfseclient4", "usersys.redhat.com", "948e04c477cbbe239860c67eefe8e9c8"},
		{"cfseclient5", "usersys.redhat.com", "b42c3213817e650718011f825ca4a67a"},
		{"cfseclient6", "usersys.redhat.com", "4cf6fd9dc5906e0b7c20a755ed703ed9"},
		{"cfseclient7", "usersys.redhat.com", "74f91b196150c2531a665694cee6db4e"},
		{"cfseclient8", "usersys.redhat.com", "2d6986fd7f6ca172d5eb0dd80ffda595"},
		{"cfseclient9", "usersys.redhat.com", "1d61e735256ab8821084434ebf385159"},
		{"cfseclient10", "usersys.redhat.com", "920475bf72055f926b533c55e9792734"},
		{"cfseclient11", "usersys.redhat.com", "53b376bcc018246d03b46b1b57143f8d"},
		{"cfseclient12", "usersys.redhat.com", "372b245d8a629326d5919e8566544617"},
		{"cfseclient13", "usersys.redhat.com", "76f4bb9fd5f56b5bb70fdf530c659e30"},
		{"cfseclient14", "usersys.redhat.com", "47c46ba4e4d2da0cf6cfcc8c8d4b1258"},
		{"cfseclient15", "usersys.redhat.com", "ac53b538a98d386e5d8959cd2937c533"},
		{"cfseclient16", "usersys.redhat.com", "80195d5a9af29e541281cff016871546"}};
	
	public static final String[][] DELTACLOUD_SERVERS = new String[][]{{"cfseserver1", "usersys.redhat.com", "502f89b3921dcff92ccdfcf91a6e7db9", "10.16.120.72", "00:1a:4a:10:78:36"},
		{"cfseserver2", "usersys.redhat.com", "b948f4f5b2c262f8a1ba508389e51998", "10.16.120.72", "00:1a:4a:10:78:36"},
		{"cfseserver3", "usersys.redhat.com", "1b97363ff962d8602af1124226da25d8", "10.16.120.72", "00:1a:4a:10:78:36"},
		{"cfseserver4", "usersys.redhat.com", "33b9babe86485c6a0c63ff013552933d", "10.16.120.72", "00:1a:4a:10:78:36"},
		{"cfseserver5", "usersys.redhat.com", "a70017101d57379c611a30cea1f76182", "10.16.120.72", "00:1a:4a:10:78:36"},
		{"cfseserver6", "usersys.redhat.com", "08d568db541879e2f684bcf637cf1375", "10.16.120.72", "00:1a:4a:10:78:36"}};

}
