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
	/*
	 * # ==> General Configuration Settings <== #
	 * # == == == == == == == == == == == == == #
	 */
	public static final int SSH_SLEEP_INTERVAL = 
			new Integer(System.getProperty("general.ssh.sleep","20")).intValue();
	public static final String KATELLO_PRODUCT = 
			System.getProperty("katello.product", "katello");
	public static final int NOWORKER_SLEEP = 10000;
	
	/** Masked string for passwords */
	public static final String SYSOUT_PWD_MASK = "********";

	public static final String KATELLO_SMALL_REPO = 
			"http://repos.fedorapeople.org/repos/katello/katello/fedora-15/x86_64/";
	public static final String PULP_RHEL6_x86_64_REPO = 
			"http://repos.fedorapeople.org/repos/pulp/pulp/v2/stable/6Server/x86_64/";
	public static final String PULP_RHEL6_i386_REPO = 
			"http://repos.fedorapeople.org/repos/pulp/pulp/v2/stable/6Server/i386/";
	public static final String PULP_RHEL6_REPO = 
			"http://repos.fedorapeople.org/repos/pulp/pulp/v2/stable/6Server/";
	public static final String EXPORT_ZIP_PATH = 
			System.getProperty("user.dir") + "/data/export.zip";
	public static final int PRODUCTS_IN_EXPORT_ZIP = 6;
	public static final String AWESOME_SERVER_BASIC = 
			"Awesome OS Server Basic";
	public static final String REPO_DISCOVER_PULP_V2_ALL = 
			"http://repos.fedorapeople.org/repos/pulp/pulp/v2/stable";

	// Some repo URLs
	public static final String REPO_INECAS_ZOO3 = "http://inecas.fedorapeople.org/fakerepos/zoo3/";

	public static final String REPO_HHOVSEPY_ZOO4 = "http://hhovsepy.fedorapeople.org/fakerepos/zoo4/";

	public static final String RPM_GRINDER_RHEL6 = "http://repos.fedorapeople.org/repos/pulp/pulp/stable/2/6Server/x86_64/grinder-0.1.16-1.el6.noarch.rpm http://dl.fedoraproject.org/pub/epel/6/x86_64/PyYAML-3.10-3.el6.x86_64.rpm";
	
	// MANIFEST NAMES
	public static final String MANIFEST_MANIFEST_ZIP = "manifest.zip";
	
	
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

	public static final String[][] DELTACLOUD_CLIENTS = new String[][]{
		{"cliandhra", "usersys.redhat.com", "7b316bd9ba131d9d4dd5a8e19fe7ff9a"},
		{"clibihar", "usersys.redhat.com", "7344b34135854a6bc9e715011173a4b3"},
		{"cliharyana", "usersys.redhat.com", "f82f5f15b88d6a4691d4e0b8e2eb890a"},
		{"clijharkhand", "usersys.redhat.com", "43ca73f3f0b2bf9edc17640fbf991747"},
		{"clikerala", "usersys.redhat.com", "845efd66564fd131de56195c179f5f36"},
		{"climanipur", "usersys.redhat.com", "25c1267c44a2d95e82f561ffcbcd2d66"},
		{"climeghalaya", "usersys.redhat.com", "5818c44f9f0502bce6e9ac8b41d53f50"},
		{"cliodisha", "usersys.redhat.com", "09c4bff19d65e42870bf38b005058871"},
		{"clitripura", "usersys.redhat.com", "c48fbe955a850727e89667da41df6fc1"},
		
//		{"cfseclient1", "usersys.redhat.com", "335f800994d9adc5947b49728b14d527"}, 
//		{"cfseclient2", "usersys.redhat.com", "c9c0395f0730624541d8d26f7eb11be8"}, 
//		{"cfseclient3", "usersys.redhat.com", "edcf8c472ca601e33326f50f5d46ba8e"},
//		{"cfseclient4", "usersys.redhat.com", "948e04c477cbbe239860c67eefe8e9c8"},
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
		{"cfseclient16", "usersys.redhat.com", "80195d5a9af29e541281cff016871546"}
	};

	public static final String[][] DELTACLOUD_SERVERS = new String[][]{
		{"aphrodite",	"usersys.redhat.com", "4508fc3c40226af78bc623802bc60845", "", ""},
		{"apollo",		"usersys.redhat.com", "2ceb3e999f8cabd855a99b6cdc9ad8f7", "", ""},
		{"ares",		"usersys.redhat.com", "dfd3be64c7249a253255131230a9e1e9", "", ""},
		{"artemis",		"usersys.redhat.com", "c3a80832d424b116a542dc17d4629ff6", "", ""},
		{"demeter",		"usersys.redhat.com", "4d2aaae0bf38f62a972a2ed73a7a954f", "", ""},
		{"dionysus",	"usersys.redhat.com", "155886a1175e8a76de1a1e3725c23d65", "", ""},
		{"hades",		"usersys.redhat.com", "24a1fb3436fbd257d6595b3cbdc031c7", "", ""},
		{"hephaestus",	"usersys.redhat.com", "b4ece38c057192894b8b95c0057812d3", "", ""},
		{"hera",		"usersys.redhat.com", "d2e9e980919c0fce3d8c971b02344f93", "", ""},
		{"poseidon",	"usersys.redhat.com", "cf5d3e604683328ea4df4d2d5538c3f1", "", ""},
		{"zeus",		"usersys.redhat.com", "53eaf7a7c6c21f4d37b71c61e0a3d23d", "", ""},

		{"surya",		"usersys.redhat.com", "9e546ec429ac34ec15532353e423e9e1", "", ""},
		{"budha",		"usersys.redhat.com", "fec210404083b85a07ba800dca7ae8b1", "", ""},
		{"sukra",		"usersys.redhat.com", "9581e7b17e3a2f6a45670f9883b52fb8", "", ""},
		{"dhara",		"usersys.redhat.com", "8a701607877b5ec8f9ba7b3c8932c471", "", ""},
		{"chandra",		"usersys.redhat.com", "66a69fa255f01040f2bc9506a4e0b65d", "", ""},
		{"mangala",		"usersys.redhat.com", "e8a3af226d9ee02ff43e6f674eace8b2", "", ""},
		
		{"cfseserver1", "usersys.redhat.com", "502f89b3921dcff92ccdfcf91a6e7db9", "10.16.120.72", "00:1a:4a:10:78:36"},
		{"cfseserver2", "usersys.redhat.com", "b948f4f5b2c262f8a1ba508389e51998", "10.16.120.72", "00:1a:4a:10:78:36"},
		{"cfseserver3", "usersys.redhat.com", "1b97363ff962d8602af1124226da25d8", "10.16.120.72", "00:1a:4a:10:78:36"},
		{"cfseserver4", "usersys.redhat.com", "33b9babe86485c6a0c63ff013552933d", "10.16.120.72", "00:1a:4a:10:78:36"},
		{"cfseserver5", "usersys.redhat.com", "a70017101d57379c611a30cea1f76182", "10.16.120.72", "00:1a:4a:10:78:36"},
		{"cfseserver6", "usersys.redhat.com", "08d568db541879e2f684bcf637cf1375", "10.16.120.72", "00:1a:4a:10:78:36"}
	};

	public static final String REDHAT_RELEASE_RHEL5X = "Red Hat Enterprise Linux Server release 5.";
	
	@SuppressWarnings("serial")
	public static final Map<String, String> DELTACLOUD_IMAGES = new HashMap<String, String>(){{
		put("RHEL 5.7 Server i386", "8716843b-50a8-48fc-add0-3f6ec21aae4a");
		put("RHEL 5.8 Server i386", "3e5195c3-be05-4850-8c4d-9382e5742fad");
		put("RHEL 5.9 Server i386", "87c7f04a-1ac0-4218-b02d-f7c95470ee01");
		put("RHEL 6.0 Server i386", "25dc6565-51e0-4377-b753-e28b17333c61");
		put("RHEL 6.1 Server i386", "bf307ff9-c1eb-4fab-8aa2-e501f70a1de1");
		put("RHEL 6.2 Server i386", "9df656c6-26ed-4198-b4b1-35d8bdbf9b46");
		put("RHEL 6.3 Server i386", "fc06e21b-8973-48e2-9d64-3b5a90f2717e");
		put("RHEL 6.4 Server i386", "f8d463d4-6e3e-43ba-b3d3-1fccc326442d");
		put("RHEL 5.7 Server x86_64", "aa792b31-3c45-4fbb-9a6c-092a19a2674f");
		put("RHEL 5.8 Server x86_64", "06ad3fc6-d879-4a8d-8353-58cf4d4cd579");
		put("RHEL 5.9 Server x86_64", "d56c6de6-aefd-45c0-81a7-9f0685529f3d");
		put("RHEL 6.0 Server x86_64", "623fce7f-24e5-41a0-af5a-15ce12b09509");
		put("RHEL 6.1 Server x86_64", "cb77e9b5-a7ab-4fdf-bdf0-5c47897ad671");
		put("RHEL 6.2 Server x86_64", "b0b5cae3-b703-4412-9fd8-f46f47ae3216");
		put("RHEL 6.3 Server x86_64", "d927a3cd-342f-4f88-ae1d-f5da9e26a581");
		put("RHEL 6.4 Server x86_64", "24689ef2-0970-4523-adfc-3a14d4fcc89c");
	}};
	
	public static final int RHEVM_MAX_WAIT = 1200; // in seconds, so: 20 min.
	public static final int RHEVM_AGENTD_WAIT = 300; // in seconds, so: 2 min.
}
