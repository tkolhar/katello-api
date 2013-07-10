package com.redhat.qe.katello.tests.upgrade.v1;

import java.util.logging.Logger;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloActivationKey;
import com.redhat.qe.katello.base.obj.KatelloChangeset;
import com.redhat.qe.katello.base.obj.KatelloDistribution;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloGpgKey;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloPermission;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.base.obj.KatelloSystemGroup;
import com.redhat.qe.katello.base.obj.KatelloUser;
import com.redhat.qe.katello.base.obj.KatelloUserRole;
import com.redhat.qe.katello.common.KatelloConstants;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

/**
 * current list of katello CLI calls:
-	activation_key activation key specific actions in the katello server
+	changeset      changeset specific actions in the katello server
*	distribution   repo specific actions in the katello server
+	environment    environment specific actions in the katello server
*	errata         errata specific actions in the katello server
+	filter         filter specific actions in the katello server
+	gpg_key        GPG key specific actions in the katello server
+	org            organization specific actions in the katello server
*	package        package specific actions in the katello server
*	package_group  package group specific actions in the katello server
+	permission     permission pecific actions in the katello server
+	product        product specific actions in the katello server
+	provider       provider specific actions in the katello server
+	repo           repo specific actions in the katello server
	sync_plan      synchronization plan specific actions in the katello server
	system         system specific actions in the katello server
-	system_group   system group specific actions in the katello server
+	user           user specific actions in the katello server
+	user_role      user role specific actions in the katello server
	
 * @author gkhachik
 *
 */
@Test(enabled=false) // [gkhachik] - need to make some check_*** still working, would fail otherwise for now!
public class FillDB implements KatelloConstants{
	/**
	 * NOTE:<br>
	 * Please be aware (and DON'T use):
	 * 		katello.upgrade.clients[1] (aka: 2nd client)
	 */
	protected static Logger log = Logger.getLogger(FillDB.class.getName());

	private String uid;
	private String orgName;
	private String envNameTesting, envNameDevelopment;
	private String envNamePostUpgrade;
	private String userNameAdmin;
	private String userNameDisabled;
	private String userNameGuest;
	
	private String roleReadAll;
	private String roleOrgAdmin;
	
	private String gpgKeyZoo, gpgFilename;
	private String akTesting, akDevelopment;
	private String systemGroupMyServers;
	
	private String providerFedora;
	private String providerZoo;
	private String productFedora;
	private String productZoo;
	private String repoFedora;
	private String repoZoo;
	
	private String poolIdRhel6;
	private String poolIdFedora;
	private String poolIdZoo;
	
	@BeforeClass(groups={TNG_PRE_UPGRADE}, description="init strings")
	public void init(){
		uid = KatelloUtils.getUniqueID();
		orgName = "CFSE QE Team "+uid;
		envNameTesting = "Testing";
		envNameDevelopment = "Development";
		userNameAdmin = "cfse-admin-"+uid;
		userNameDisabled = "cfse-disabled-"+uid;
		userNameGuest = "cfse-guest-"+uid;
		roleReadAll = KatelloUserRole.ROLE_READ_EVERYTHING+" "+uid;
		roleOrgAdmin = KatelloUserRole.ROLE_ADMINISTRATOR+" "+uid;
		gpgKeyZoo = "GPG-ZOO-"+uid;
		gpgFilename = "/tmp/RPM-GPG-KEY-dummy-packages-generator";
		akTesting = "ak-"+envNameTesting;
		akDevelopment = "ak-"+envNameDevelopment;
		systemGroupMyServers = "my-servers-"+uid;
		providerFedora = "Fedora-"+uid;
		providerZoo = "Zoo-"+uid;
		productFedora = "Fedora16-"+uid;
		productZoo = "Zoo-"+uid;
		repoFedora = "fedora16-x86_64-"+uid;
		repoZoo = "zoo-noarch-"+uid;
		envNamePostUpgrade = "Production";
		
		String clientsStr = System.getProperty("katello.upgrade.clients", "");
		String[] clients = clientsStr.split(",");
		if(clientsStr.isEmpty() || clients.length<2 || clients[1].isEmpty()) {
			throw new SkipException("Please specify \"katello.upgrade.clients\" with at least 2 _different_ clients registered to the server");
		}
	}
	
	@Test(groups={TNG_PRE_UPGRADE}, 
			description="create org, environment, user", enabled = false)
	public void create_OrgEnvUser(){
		SSHCommandResult res;
		KatelloOrg org = new KatelloOrg(null, orgName, orgName+" description");
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: org.create");
		res = new KatelloEnvironment(null, envNameTesting, envNameTesting+" decription", orgName, KatelloEnvironment.LIBRARY).cli_create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: environment.create");
		res = new KatelloEnvironment(null, envNameDevelopment, envNameDevelopment+" decription", orgName, envNameTesting).cli_create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: environment.create");
		res = new KatelloUser(null, userNameAdmin, KatelloUser.DEFAULT_USER_EMAIL, KatelloUser.DEFAULT_ADMIN_PASS, false).cli_create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: user.create");
		res = new KatelloUser(null, userNameDisabled, KatelloUser.DEFAULT_USER_EMAIL, KatelloUser.DEFAULT_USER_PASS, true).cli_create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: user.create --disabled true");
		res = new KatelloUser(null, userNameGuest, KatelloUser.DEFAULT_USER_EMAIL, KatelloUser.DEFAULT_USER_PASS, false).cli_create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: user.create");
	}
	
	@Test(groups={TNG_POST_UPGRADE},
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE},
			description="check org, environent, user survived", enabled = false ) 
	public void check_OrgEnvUser(){
		SSHCommandResult res;
		String _name, _description, _prior;
		KatelloUser usrAdmin, usrGuest;
		usrAdmin = new KatelloUser(null, userNameAdmin, null, KatelloUser.DEFAULT_ADMIN_PASS, false);
		usrGuest = new KatelloUser(null, userNameGuest, null, KatelloUser.DEFAULT_USER_PASS, false);
		
		KatelloOrg org;
		org = new KatelloOrg(null, orgName, orgName+" description");
		org.runAs(usrGuest);
		res = org.cli_info();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "check: org.info");
		_name = KatelloUtils.grepCLIOutput("Name", KatelloCliTestBase.sgetOutput(res));
		_description = KatelloUtils.grepCLIOutput("Description", KatelloCliTestBase.sgetOutput(res));
		Assert.assertTrue(_name.equals(org.name), "stdout: org.name");
		Assert.assertTrue(_description.equals(org.description), "stdout: org.description");
		
		KatelloEnvironment env;
		env = new KatelloEnvironment(null, envNameTesting, envNameTesting+" decription", orgName, KatelloEnvironment.LIBRARY);
		env.runAs(usrGuest);
		res = env.cli_info();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "check: environment.info");
		_name = KatelloUtils.grepCLIOutput("Name", KatelloCliTestBase.sgetOutput(res));
		_description = KatelloUtils.grepCLIOutput("Description", KatelloCliTestBase.sgetOutput(res));
		_prior = KatelloUtils.grepCLIOutput("Prior Environment", KatelloCliTestBase.sgetOutput(res));
		Assert.assertTrue(_name.equals(env.getName()), "stdout: environment.name");
		Assert.assertTrue(_description.equals(env.getDescription()), "stdout: environment.description");
		Assert.assertTrue(_prior.equals(KatelloEnvironment.LIBRARY), "stdout: environment.prior");
		
		env = new KatelloEnvironment(null, envNameDevelopment, envNameDevelopment+" decription", orgName, envNameTesting);
		env.runAs(usrGuest);
		res = env.cli_info();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "check: environment.info");
		_name = KatelloUtils.grepCLIOutput("Name", KatelloCliTestBase.sgetOutput(res));
		_description = KatelloUtils.grepCLIOutput("Description", KatelloCliTestBase.sgetOutput(res));
		_prior = KatelloUtils.grepCLIOutput("Prior Environment", KatelloCliTestBase.sgetOutput(res));
		Assert.assertTrue(_name.equals(env.getName()), "stdout: environment.name");
		Assert.assertTrue(_description.equals(env.getDescription()), "stdout: environment.description");
		Assert.assertTrue(_prior.equals(envNameTesting), "stdout: environment.prior");
		
		res = usrAdmin.update_defaultOrgEnv(orgName, envNameTesting);
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: user.assignDefaultOrgEnv");
	}
	
	@Test(groups={TNG_PRE_UPGRADE}, dependsOnMethods={"create_OrgEnvUser"},
			description="create role, permission and assignments", enabled = false)
	public void create_permissionsRoles(){
		SSHCommandResult res;
		String verbEnvAll = envNameTesting+","+envNameDevelopment;
		
		KatelloUser user = new KatelloUser();
		res  = new KatelloUserRole(null, roleReadAll, roleReadAll+ " description").create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: role.create");
		res  = new KatelloUserRole(null, roleOrgAdmin, roleOrgAdmin+ " description").create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: role.create");
		
		// Read all permissions
		res = new KatelloPermission(null, "ro-providers-"+uid, orgName, "providers", null, "read", roleReadAll).create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: permission.create");
		res = new KatelloPermission(null, "ro-environments-"+uid, orgName, "environments", verbEnvAll, "read_systems,read_contents,read_changesets", roleReadAll).create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: permission.create");
		res = new KatelloPermission(null, "ro-filters-"+uid, orgName, "filters", null, "read", roleReadAll).create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: permission.create");
		res = new KatelloPermission(null, "ro-roles-"+uid, orgName, "roles", null, "read", roleReadAll).create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: permission.create");
		res = new KatelloPermission(null, "ro-organizations-"+uid, orgName, "organizations", null, "read,read_systems", roleReadAll).create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: permission.create");
		res = new KatelloPermission(null, "ro-users-"+uid, orgName, "users", null, "read", roleReadAll).create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: permission.create");
		res = new KatelloPermission(null, "ro-activation_keys-"+uid, orgName, "activation_keys", null, "read_all", roleReadAll).create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: permission.create");
		
		user.username = userNameGuest;
		res = user.assign_role(roleReadAll);
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: user.assign_role");

		// Admin org-scope permissions
		res = new KatelloPermission(null, "rw-providers-"+uid, orgName, "providers", null, "create,delete,update,read", roleOrgAdmin).create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: permission.create");
		res = new KatelloPermission(null, "rw-environments-"+uid, orgName, "environments", verbEnvAll, 
				"manage_changesets,update_systems,promote_changesets,read_changesets," +
				"read_contents,read_systems,register_systems,delete_systems", roleOrgAdmin).create();		
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: permission.create");
		res = new KatelloPermission(null, "rw-filters-"+uid, orgName, "filters", null, "create,delete,update,read", roleOrgAdmin).create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: permission.create");
		res = new KatelloPermission(null, "rw-organizations-"+uid, orgName, "organizations", null, 
				"gpg,delete_systems,update,update_systems,read,read_systems,register_systems,sync", roleOrgAdmin).create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: permission.create");
		res = new KatelloPermission(null, "rw-activation_keys-"+uid, orgName, "activation_keys", null, "manage_all,read_all", roleOrgAdmin).create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: permission.create");
				
		user.username = userNameAdmin;
		res = user.assign_role(roleOrgAdmin);
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: user.assign_role");
	}
	
	@Test(groups={TNG_POST_UPGRADE}, 
			dependsOnMethods={"check_OrgEnvUser"},
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE},
			description="check permissions, make different calls", enabled = false)
	public void check_permissionsRoles(){
		SSHCommandResult res;
		KatelloUser usrAdmin, usrGuest, usrDisabled;
		usrAdmin = new KatelloUser(null, userNameAdmin, null, KatelloUser.DEFAULT_ADMIN_PASS, false);
		usrGuest = new KatelloUser(null, userNameGuest, null, KatelloUser.DEFAULT_USER_PASS, false);
		usrDisabled = new KatelloUser(null, userNameDisabled, null, KatelloUser.DEFAULT_USER_PASS, true);

		res = new KatelloPermission(null, "ro-system_groups-"+uid, orgName, "system_groups", null, "read,read_systems", roleReadAll).create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: permission.create");
		res = new KatelloPermission(null, "rw-system_groups-"+uid, orgName, "system_groups", null, 
				"create,delete,delete_systems,update,update_systems,read,read_systems", roleOrgAdmin).create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: permission.create");
		
		log.info(String.format("Guest actions not permitted for: [%s]",userNameGuest));
		// ============================================================================
		KatelloOrg org = new KatelloOrg(null, "noPermit"+uid, null);org.runAs(usrGuest);
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode().intValue() == 147, "exit: org.create");
		Assert.assertTrue(KatelloCliTestBase.sgetOutput(res).contains(
				String.format(KatelloUser.ERR_NOT_ALLOWED_TO_ACCESS, userNameGuest)), 
				"stderr: not allowed to access (org.create)");

		KatelloEnvironment env = new KatelloEnvironment(null, "notPermit"+uid, null, orgName, KatelloEnvironment.LIBRARY);
		env.runAs(usrGuest);
		res = env.cli_create();
		Assert.assertTrue(res.getExitCode().intValue() == 147, "exit: environment.create");
		Assert.assertTrue(KatelloCliTestBase.sgetOutput(res).contains(
				String.format(KatelloUser.ERR_NOT_ALLOWED_TO_ACCESS, userNameGuest)), 
				"stderr: not allowed to access (environment.create)");
		
		KatelloUserRole role = new KatelloUserRole(null, "notPermit"+uid, null);
		role.runAs(usrGuest);
		res = env.cli_create();
		Assert.assertTrue(res.getExitCode().intValue() == 147, "exit: user_role.create");
		Assert.assertTrue(KatelloCliTestBase.sgetOutput(res).contains(
				String.format(KatelloUser.ERR_NOT_ALLOWED_TO_ACCESS, userNameGuest)), 
				"stderr: not allowed to access (user_role.create)");
		
		KatelloSystem sys = new KatelloSystem(null, "notPermit"+uid, orgName, envNameTesting);
		sys.runAs(usrGuest);
		res = sys.rhsm_registerForce();
		Assert.assertTrue(res.getExitCode().intValue() == 255, "exit: rhsm.register");
		Assert.assertTrue(KatelloCliTestBase.sgetOutput(res).contains(
				String.format(KatelloUser.ERR_NOT_ALLOWED_TO_ACCESS, userNameGuest)), 
				"stderr: not allowed to access (rhsm.register)");

		log.info(String.format("Actions denied for disabled user: [%s]",userNameDisabled));
		// ============================================================================
		org = new KatelloOrg(null, orgName, null);org.runAs(usrDisabled);
		res = org.cli_info();
		Assert.assertTrue(res.getExitCode().intValue() == 145, "exit: org.list");
		Assert.assertTrue(KatelloCliTestBase.sgetOutput(res).contains(
				KatelloUser.ERR_INVALID_CREDENTIALS), 
				"stderr: invalid credentials (org.list)");

		KatelloSystemGroup sysGrp = new KatelloSystemGroup(null, systemGroupMyServers, orgName);
		sysGrp.runAs(usrDisabled);
		res = sysGrp.info();
		Assert.assertTrue(res.getExitCode().intValue() == 145, "exit: system_group.info");
		Assert.assertTrue(KatelloCliTestBase.sgetOutput(res).contains(
				KatelloUser.ERR_INVALID_CREDENTIALS), 
				"stderr: invalid credentials (system_group.info)");
		
		log.info(String.format("Actions granted for admin user: [%s]",userNameAdmin));
		// ============================================================================
		env = new KatelloEnvironment(null, envNamePostUpgrade,null,orgName,envNameDevelopment);
		env.runAs(usrAdmin);
		res = env.cli_create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: environment.create");
		res = env.cli_info();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: environment.info");
		String prior = KatelloUtils.grepCLIOutput("Prior Environment", KatelloCliTestBase.sgetOutput(res));
		Assert.assertTrue(prior.equals(envNameDevelopment), "stdout: prior environment");

		log.info(String.format("Additional rw permissions on environment: [%s]",env.getName()));
		res = new KatelloPermission(null, "rw-environments-postUpgrade-"+uid, orgName, "environments", null, 
				"manage_changesets,update_systems,promote_changesets,read_changesets," +
				"read_contents,read_systems,register_systems,delete_systems", roleOrgAdmin).create(true); // for all.		
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: permission.create");
				
		sys = new KatelloSystem(null, envNamePostUpgrade, orgName, env.getName());
		sys.runAs(usrAdmin);
		res = sys.rhsm_registerForce();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: rhsm.create");
		sys.rhsm_unregister(); // we don't care about the result.
		
		KatelloChangeset cs = new KatelloChangeset(null, envNamePostUpgrade, orgName, env.getName());
		cs.runAs(usrAdmin);
		res = cs.create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: changeset.create");
	}

	@Test(groups={TNG_PRE_UPGRADE}, dependsOnMethods={"create_permissionsRoles"},
			description="import manifest, enable repo, promote to all envs - as the orgAdmin user", enabled = false)
	public void create_importManifestEnableRHRepoSyncNPromoteAllEnvs(){
		SSHCommandResult res;
		KatelloUser orgAdmin = new KatelloUser(null, userNameAdmin, null, KatelloUser.DEFAULT_ADMIN_PASS, false);
		String manifest_name = KatelloProvider.MANIFEST_2SUBSCRIPTIONS;

		KatelloProvider provRedHat = new KatelloProvider(null, KatelloProvider.PROVIDER_REDHAT, orgName, null, null);
		provRedHat.runAs(orgAdmin);

		KatelloUtils.scpOnClient(null, "data/"+manifest_name, "/tmp");

		res = provRedHat.import_manifest("/tmp/"+manifest_name, true);
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: provider.import_manifest");
		KatelloRepo repo = new KatelloRepo(null, KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT, orgName, KatelloProduct.RHEL_SERVER, null, null, null);
		repo.runAs(orgAdmin);
		res = repo.enable();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: repo.enable");
		res = repo.synchronize();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: repo.synchronize");
		
		KatelloUtils.promoteProductToEnvironment(null, orgName, KatelloProduct.RHEL_SERVER, envNameTesting);
		
		KatelloUtils.promoteProductToEnvironment(null, orgName, KatelloProduct.RHEL_SERVER, envNameDevelopment);
	}

	@Test(groups={TNG_POST_UPGRADE}, dependsOnMethods={"check_permissionsRoles"},
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE},
			description="check subscription, product, repo info in all environments - as orgAdmin user", enabled = false)
	public void check_importManifestEnableRHRepoPromoteAllEnvs(){
		/**
		 * TODO - make calls to get info about product, repo, subscriptions for all the environments 
		 * to assure they all preserved.
		 * make the calls as the org Admin user.
		 */
	}
	
	@Test(groups={TNG_PRE_UPGRADE}, dependsOnMethods={"create_permissionsRoles"},
			description="create: gpgKey, activationKey, filter, system_group. " +
					"To be used later on next step(s) - as orgAdmin user.", enabled = false)
	public void create_gpgKeyActivationKeyFilterSystemGroup(){
		// GPG Key
		SSHCommandResult res;
		String cmd = String.format("rm -f %s; curl -sk %s -o %s",
				gpgFilename, KatelloGpgKey.REPO_GPG_FILE_ZOO,gpgFilename);
		KatelloUser orgAdmin = new KatelloUser(null, userNameAdmin, null, KatelloUser.DEFAULT_ADMIN_PASS, false);
		
		res = KatelloUtils.sshOnClient(null, cmd);
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: curl.gpgkey-zoo");
		KatelloGpgKey gpgKey = new KatelloGpgKey(null, gpgKeyZoo, orgName, gpgFilename);
		gpgKey.runAs(orgAdmin);
		res = gpgKey.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: gpg_key.create");
		
		// Activation key
		res = new KatelloActivationKey(null, orgName, envNameTesting, akTesting, akTesting+" description", null).create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: activation_key.create");
		res = new KatelloActivationKey(null, orgName, envNameDevelopment, akDevelopment, akDevelopment+" description", null).create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: activation_key.create");		
	}
	
	@Test(groups={TNG_POST_UPGRADE},
			dependsOnMethods={"check_permissionsRoles"},
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE},
			description="check gpg key can be added to product Zoo", enabled = false)
	public void check_addGpgKey(){
		SSHCommandResult res;
		KatelloUser orgAdmin = new KatelloUser(null, userNameAdmin, null, KatelloUser.DEFAULT_ADMIN_PASS, false);
		
		KatelloProduct prod = new KatelloProduct(null, productZoo,orgName,providerZoo,null,null,null,null,null);
		prod.runAs(orgAdmin);
		res = prod.update_gpgkey(gpgKeyZoo);
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: product.update_gpgkey");
		KatelloGpgKey key = new KatelloGpgKey(null, gpgKeyZoo, orgName, null);
		key.runAs(orgAdmin);
		res = key.cli_info();
		Assert.assertTrue(KatelloCliTestBase.sgetOutput(res).contains(productZoo), "check GPG key info has product Zoo");
	}
	
	@Test(groups={TNG_POST_UPGRADE},
			dependsOnMethods={"check_permissionsRoles"},
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE},
			description="check activation key related features", enabled = false)
	public void check_activationKey(){
		SSHCommandResult res;
		KatelloUser orgAdmin = new KatelloUser(null, userNameAdmin, null, KatelloUser.DEFAULT_ADMIN_PASS, false);
		
		poolIdZoo = new KatelloOrg(null,orgName,null).custom_getPoolId(productZoo);
		Assert.assertNotNull(poolIdZoo, "poolid for Zoo not null");
		poolIdFedora = new KatelloOrg(null,orgName,null).custom_getPoolId(productFedora);
		Assert.assertNotNull(poolIdFedora, "poolid for Fedora not null");
		poolIdRhel6 = new KatelloOrg(null,orgName,null).custom_getPoolId(KatelloProduct.RHEL_SERVER_MARKETING_POOL);
		Assert.assertNotNull(poolIdRhel6, "poolid for RHEL6 not null");

		KatelloActivationKey ak = new KatelloActivationKey(null, orgName, envNameTesting, akTesting, akTesting+" description", null);
		ak.runAs(orgAdmin);
		res = ak.update_add_subscription(poolIdZoo);
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: activation_key.update-add_subscription");
		res = ak.update_add_subscription(poolIdFedora);
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: activation_key.update-add_subscription");
		
		ak = new KatelloActivationKey(null, orgName, envNameDevelopment, akDevelopment, null, null);
		ak.runAs(orgAdmin);
		res = ak.update_add_subscription(poolIdRhel6);
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: activation_key.update-add_subscription");
	}
	
	@Test(groups={TNG_POST_UPGRADE},
			dependsOnMethods={"check_activationKey"},
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE},
			description="check syste group related features", enabled = false)
	public void check_systemGroup(){
		SSHCommandResult res;
		KatelloUser orgAdmin = new KatelloUser(null, userNameAdmin, null, KatelloUser.DEFAULT_ADMIN_PASS, false);

		log.info("Additional ro permissions on system_groups");
		res = new KatelloPermission(null, "ro-system_groups-"+uid, orgName, "system_groups", null, 
				"read,read_systems", roleReadAll).create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: permission.create");

		KatelloUser guest = new KatelloUser(null, userNameGuest, null, KatelloUser.DEFAULT_USER_PASS, false);
		res = guest.assign_role(roleReadAll);
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: user.assign_role");

		log.info("Additional rw permissions on system_groups");
		res = new KatelloPermission(null, "rw-system_groups-"+uid, orgName, "system_groups", null, 
				"create,delete,delete_systems,update,update_systems,read,read_systems", roleOrgAdmin).create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: permission.create");

		res = orgAdmin.assign_role(roleOrgAdmin);
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: user.assign_role");

		KatelloSystemGroup sysGrp = new KatelloSystemGroup(null, systemGroupMyServers, orgName);
		sysGrp.runAs(orgAdmin);
		res = sysGrp.create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: system_group.create");
		
		KatelloActivationKey ak = new KatelloActivationKey(null, orgName, envNameDevelopment, akDevelopment, null, null);
		ak.runAs(orgAdmin);
		res = ak.add_system_group(systemGroupMyServers);
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: activation_key.add_system_group");
	}
	
	@Test(groups={TNG_POST_UPGRADE},
			dependsOnMethods={"check_systemGroup"},
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE},
			description="register via AK and check features", enabled = false)
	public void check_registerViaAk(){
		
	}
	
	@Test(groups={TNG_PRE_UPGRADE}, 
			dependsOnMethods={"create_permissionsRoles"},
			description="create: providers, products, repos {Fedora, Zoo3}. " +
			"Make sync - as orgAdmin user.", enabled = false)
	public void create_providerProductRepoSyncF16AndZoo3(){
		SSHCommandResult res;
		KatelloUser orgAdmin = new KatelloUser(null, userNameAdmin, null, KatelloUser.DEFAULT_ADMIN_PASS, false);
		
		KatelloProvider prov = new KatelloProvider(null, providerFedora, orgName, providerFedora+" description", null);
		prov.runAs(orgAdmin);
		res = prov.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: provider.create");
		
		prov = new KatelloProvider(null, providerZoo, orgName, providerZoo+" description", null);
		prov.runAs(orgAdmin);
		res = prov.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: provider.create");
		
		KatelloProduct prod = new KatelloProduct(null, productFedora, orgName, providerFedora, 
				productFedora+" description", null, null, null, null);
		res = prod.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: product.create");
		
		KatelloRepo repo = new KatelloRepo(null, repoFedora, orgName, productFedora, 
				KatelloRepo.getFedoraMirror(KatelloRepo.FEDORA_VER16), null, null);
		res = repo.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: repo.create");
		res = repo.synchronize();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: repo.synchronize");
		
		prod = new KatelloProduct(null, productZoo, orgName, providerZoo, 
				productZoo+" description", null, null, null, null);
		res = prod.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: product.create");
		
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: product.create");
		repo = new KatelloRepo(null, repoZoo, orgName, productZoo, REPO_INECAS_ZOO3, null, null);
		res = repo.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: repo.create");
		// DON'T synchronize Zoo repo yet, keep it to be synchronized after upgrade
		
		// assign gpgkey - zoo
		prod = new KatelloProduct(null, productZoo, orgName, null, null, null, null, null, null);
		res = prod.update_gpgkey(gpgKeyZoo);
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: product.update");

		// assign: distro, repo, package, package_group, param
		KatelloDistribution dist = new KatelloDistribution(null, orgName, productFedora,repoFedora, null);
		res = dist.list();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: distribution.list");
		String distroF16 = KatelloUtils.grepCLIOutput("ID", res.getStdout());
		Assert.assertTrue(!distroF16.isEmpty(), "Check - distribution exists in repo");
		
		KatelloUtils.promoteProductToEnvironment(null, orgName, productFedora, envNameTesting);
		
		KatelloUtils.promoteProductToEnvironment(null, orgName, productZoo, envNameTesting);
	}
}
