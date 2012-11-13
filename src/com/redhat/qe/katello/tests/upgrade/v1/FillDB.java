package com.redhat.qe.katello.tests.upgrade.v1;

import java.io.File;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.obj.KatelloActivationKey;
import com.redhat.qe.katello.base.obj.KatelloChangeset;
import com.redhat.qe.katello.base.obj.KatelloDistribution;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloFilter;
import com.redhat.qe.katello.base.obj.KatelloGpgKey;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloPermission;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.base.obj.KatelloSystemGroup;
import com.redhat.qe.katello.base.obj.KatelloTemplate;
import com.redhat.qe.katello.base.obj.KatelloUser;
import com.redhat.qe.katello.base.obj.KatelloUserRole;
import com.redhat.qe.katello.common.KatelloConstants;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SCPTools;
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
+	template       template specific actions in the katello server
+	user           user specific actions in the katello server
+	user_role      user role specific actions in the katello server
	
 * @author gkhachik
 *
 */
public class FillDB implements KatelloConstants{

	private String uid;
	private String orgName;
	private String envNameTesting, envNameDevelopment;
	private String userNameAdmin;
	private String userNameDisabled;
	private String userNameGuest;
	
	private String roleReadAll;
	private String roleOrgAdmin;
	
	private String gpgKeyZoo, gpgFilename;
	private String akTesting, akDevelopment;
	private String filterNoBear;
	private String templateF16Iso;
	private String systemGroupMyServers;
	
	private String providerFedora;
	private String providerZoo;
	private String productFedora;
	private String productZoo;
	private String repoFedora;
	private String repoZoo;
	
	
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
		filterNoBear = "noBear-"+uid;
		templateF16Iso = "template-f16-"+uid;
		systemGroupMyServers = "my-servers-"+uid;
		providerFedora = "Fedora-"+uid;
		providerZoo = "Zoo-"+uid;
		productFedora = "Fedora16-"+uid;
		productZoo = "Zoo-"+uid;
		repoFedora = "fedora16-x86_64-"+uid;
		repoZoo = "zoo-noarch-"+uid;
	}
	
	@Test(groups={TNG_PRE_UPGRADE}, 
			description="create org, environment, user")
	public void create_OrgEnvUser(){
		SSHCommandResult res;
		KatelloOrg org = new KatelloOrg(orgName, orgName+" description");
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: org.create");
		res = new KatelloEnvironment(envNameTesting, envNameTesting+" decription", orgName, KatelloEnvironment.LIBRARY).cli_create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: environment.create");
		res = new KatelloEnvironment(envNameDevelopment, envNameDevelopment+" decription", orgName, envNameTesting).cli_create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: environment.create");
		res = new KatelloUser(userNameAdmin, KatelloUser.DEFAULT_USER_EMAIL, KatelloUser.DEFAULT_ADMIN_PASS, false).cli_create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: user.create");
		res = new KatelloUser(userNameDisabled, KatelloUser.DEFAULT_USER_EMAIL, KatelloUser.DEFAULT_USER_PASS, true).cli_create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: user.create --disabled true");
		res = new KatelloUser(userNameGuest, KatelloUser.DEFAULT_USER_EMAIL, KatelloUser.DEFAULT_USER_PASS, false).cli_create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: user.create");
		KatelloUser user = new KatelloUser();
		user.username = userNameAdmin;
		//res = user.update_defaultOrgEnv(orgName, envNameTesting);
		//Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: user.assignDefaultOrgEnv");
	}
	
	@Test(groups={TNG_POST_UPGRADE},
			description="check org, environent, user survived" ) 
	public void check_OrgEnvUser(){
		SSHCommandResult res;
		String _name, _description, _prior;
		
		KatelloOrg org;
		org = new KatelloOrg(orgName, orgName+" description");
		res = org.cli_info();
		_name = KatelloCli.grepCLIOutput("Name", KatelloCliTestScript.sgetOutput(res));
		_description = KatelloCli.grepCLIOutput("Description", KatelloCliTestScript.sgetOutput(res));
		Assert.assertTrue(res.getExitCode().intValue() == 0, "check: org.info");
		Assert.assertTrue(_name.equals(org.name), "stdout: org.name");
		Assert.assertTrue(_description.equals(org.description), "stdout: org.description");
		
		KatelloEnvironment env;
		env = new KatelloEnvironment(envNameTesting, envNameTesting+" decription", orgName, KatelloEnvironment.LIBRARY);
		res = env.cli_info();
		_name = KatelloCli.grepCLIOutput("Name", KatelloCliTestScript.sgetOutput(res));
		_description = KatelloCli.grepCLIOutput("Description", KatelloCliTestScript.sgetOutput(res));
		_prior = KatelloCli.grepCLIOutput("Prior Environment", KatelloCliTestScript.sgetOutput(res));
		Assert.assertTrue(res.getExitCode().intValue() == 0, "check: environment.info");
		Assert.assertTrue(_name.equals(env.getName()), "stdout: environment.name");
		Assert.assertTrue(_description.equals(env.getDescription()), "stdout: environment.description");
		Assert.assertTrue(_prior.equals(KatelloEnvironment.LIBRARY), "stdout: environment.prior");
		
		env = new KatelloEnvironment(envNameDevelopment, envNameDevelopment+" decription", orgName, envNameTesting);
		res = env.cli_info();
		_name = KatelloCli.grepCLIOutput("Name", KatelloCliTestScript.sgetOutput(res));
		_description = KatelloCli.grepCLIOutput("Description", KatelloCliTestScript.sgetOutput(res));
		_prior = KatelloCli.grepCLIOutput("Prior Environment", KatelloCliTestScript.sgetOutput(res));
		Assert.assertTrue(res.getExitCode().intValue() == 0, "check: environment.info");
		Assert.assertTrue(_name.equals(env.getName()), "stdout: environment.name");
		Assert.assertTrue(_description.equals(env.getDescription()), "stdout: environment.description");
		Assert.assertTrue(_prior.equals(envNameTesting), "stdout: environment.prior");
	}
	
	@Test(groups={TNG_PRE_UPGRADE}, dependsOnMethods={"create_OrgEnvUser"},
			description="create role, permission and assignments")
	public void create_permissionsRoles(){
		SSHCommandResult res;
		
		KatelloUser user = new KatelloUser();
		res  = new KatelloUserRole(roleReadAll, roleReadAll+ " description").create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: role.create");
		res  = new KatelloUserRole(roleOrgAdmin, roleOrgAdmin+ " description").create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: role.create");
		
		// Read all permissions
		res = new KatelloPermission("ro-providers-"+uid, orgName, "providers", null, "read", roleReadAll).create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: permission.create");
		res = new KatelloPermission("ro-environments-"+uid, orgName, "environments", null, "read_systems,read_contents,read_changesets", roleReadAll).create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: permission.create");
		res = new KatelloPermission("ro-filters-"+uid, orgName, "filters", null, "read", roleReadAll).create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: permission.create");
		res = new KatelloPermission("ro-roles-"+uid, orgName, "roles", null, "read", roleReadAll).create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: permission.create");
		res = new KatelloPermission("ro-system_templates-"+uid, orgName, "system_templates", null, "read_all", roleReadAll).create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: permission.create");
		res = new KatelloPermission("ro-organizations-"+uid, orgName, "organizations", null, "read,read_systems", roleReadAll).create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: permission.create");
		res = new KatelloPermission("ro-users-"+uid, orgName, "users", null, "read", roleReadAll).create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: permission.create");
		res = new KatelloPermission("ro-activation_keys-"+uid, orgName, "activation_keys", null, "read_all", roleReadAll).create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: permission.create");
		
		user.username = userNameGuest;
		res = user.assign_role(roleReadAll);
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: user.assign_role");

		// Admin org-scope permissions
		res = new KatelloPermission("rw-providers-"+uid, orgName, "providers", null, "create,delete,update,read", roleOrgAdmin).create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: permission.create");
		res = new KatelloPermission("rw-environments-"+uid, orgName, "environments", envNameTesting+","+envNameDevelopment, 
				"manage_changesets,update_systems,promote_changesets,read_changesets," +
				"read_contents,read_systems,register_systems,delete_systems", roleOrgAdmin).create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: permission.create");
		res = new KatelloPermission("rw-filters-"+uid, orgName, "filters", null, "create,delete,update,read", roleOrgAdmin).create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: permission.create");
		res = new KatelloPermission("rw-system_templates-"+uid, orgName, "system_templates", null, "manage_all", roleOrgAdmin).create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: permission.create");
		res = new KatelloPermission("rw-organizations-"+uid, orgName, "organizations", null, 
				"gpg,delete_systems,update,update_systems,read,read_systems,register_systems,sync", roleOrgAdmin).create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: permission.create");
		res = new KatelloPermission("rw-activation_keys-"+uid, orgName, "activation_keys", null, "manage_all,read_all", roleOrgAdmin).create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: permission.create");
				
		user.username = userNameAdmin;
		res = user.assign_role(roleOrgAdmin);
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: user.assign_role");
	}
	
	@Test(groups={TNG_POST_UPGRADE}, dependsOnMethods={"check_OrgEnvUser"},
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE},
			description="check permissions, make different calls")
	public void check_permissionsRoles(){
		/**
		 * TODO - make various calls to see that all accesses are preserved. !!! 
		 * IMPORTANT.
		 * Especially changesets (with environments accesses, see Terms in permission creation (aka: tags there)
		 */
		SSHCommandResult res;
		res = new KatelloPermission("ro-system_groups-"+uid, orgName, "system_groups", null, "read,read_systems", roleReadAll).create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: permission.create");
		res = new KatelloPermission("rw-system_groups-"+uid, orgName, "system_groups", null, 
				"create,delete,delete_systems,update,update_systems,read,read_systems", roleOrgAdmin).create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: permission.create");
	}

	@Test(groups={TNG_PRE_UPGRADE}, dependsOnMethods={"create_permissionsRoles"},
			description="import manifest, enable repo, promote to all envs - as the orgAdmin user", enabled = true)
	public void create_importManifestEnableRHRepoSyncNPromoteAllEnvs(){
		SSHCommandResult res;
		KatelloUser orgAdmin = new KatelloUser(userNameAdmin, null, KatelloUser.DEFAULT_ADMIN_PASS, false);
		String manifest_name = KatelloProvider.MANIFEST_12SUBSCRIPTIONS;

		KatelloProvider provRedHat = new KatelloProvider(KatelloProvider.PROVIDER_REDHAT, orgName, null, null);
		provRedHat.runAs(orgAdmin);

		SCPTools scp = new SCPTools(
				System.getProperty("katello.client.hostname", "localhost"), 
				System.getProperty("katello.client.ssh.user", "root"), 
				System.getProperty("katello.client.sshkey.private", ".ssh/id_hudson_dsa"), 
				System.getProperty("katello.client.sshkey.passphrase", "null"));
		Assert.assertTrue(scp.sendFile("data"+File.separator+manifest_name, "/tmp"), manifest_name+" sent successfully");

		res = provRedHat.import_manifest("/tmp/"+KatelloProvider.MANIFEST_12SUBSCRIPTIONS, true);
		Assert.assertTrue(res.getExitCode().intValue() == 0, "exit: provider.import_manifest");
		KatelloRepo repo = new KatelloRepo(KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT, orgName, KatelloProduct.RHEL_SERVER, null, null, null);
		repo.runAs(orgAdmin);
		res = repo.enable();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: repo.enable");
		res = repo.synchronize();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: repo.synchronize");
		
		KatelloChangeset csTesting = new KatelloChangeset(envNameTesting, orgName, envNameTesting);
		csTesting.runAs(orgAdmin);
		res = csTesting.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: changeset.create");
		res = csTesting.update_addProduct(KatelloProduct.RHEL_SERVER);
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: changeset.update_addProduct");
		res = csTesting.promote();
		
		KatelloChangeset csDEvelopment = new KatelloChangeset(envNameDevelopment, orgName, envNameDevelopment);
		csDEvelopment.runAs(orgAdmin);
		res = csDEvelopment.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: changeset.create");
		res = csDEvelopment.update_addProduct(KatelloProduct.RHEL_SERVER);
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: changeset.update_addProduct");
		res = csDEvelopment.promote();
	}

	@Test(groups={TNG_POST_UPGRADE}, dependsOnMethods={"check_permissionsRoles"},
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE},
			description="check subscription, product, repo info in all environments - as orgAdmin user", enabled = true)
	public void check_importManifestEnableRHRepoPromoteAllEnvs(){
		/**
		 * TODO - make calls to get info about product, repo, subscriptions for all the environments 
		 * to assure they all preserved.
		 * make the calls as the org Admin user.
		 */
	}
	
	@Test(groups={TNG_PRE_UPGRADE}, dependsOnMethods={"create_permissionsRoles"},
			description="create: gpgKey, activationKey, filter, system_group, template objects. To be used later on next step(s) - as orgAdmin user.")
	public void create_gpgKeyActivationKeyFilterSystemGroupTemplate(){
		// GPG Key
		SSHCommandResult res;
		String cmd = String.format("rm -f %s; curl -sk %s -o %s",
				gpgFilename, KatelloGpgKey.REPO_GPG_FILE_ZOO,gpgFilename);
		KatelloUser orgAdmin = new KatelloUser(userNameAdmin, null, KatelloUser.DEFAULT_ADMIN_PASS, false);
		
		res = KatelloUtils.sshOnClient(cmd);
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: curl.gpgkey-zoo");
		KatelloGpgKey gpgKey = new KatelloGpgKey(gpgKeyZoo, orgName, gpgFilename);
		gpgKey.runAs(orgAdmin);
		res = gpgKey.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: gpg_key.create");
		
		// Activation key
		KatelloActivationKey ak = new KatelloActivationKey(orgName, envNameTesting, akTesting, akTesting+" description", null);
//		ak.runAs(orgAdmin);
		res = ak.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: activation_key.create");
		ak = new KatelloActivationKey(orgName, envNameDevelopment, akDevelopment, akDevelopment+" description", null);
//		ak.runAs(orgAdmin);
		res = ak.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: activation_key.create");
		
		// Filter
		KatelloFilter filter = new KatelloFilter(filterNoBear, orgName, envNameDevelopment, "bear");
		filter.runAs(orgAdmin);
		res = filter.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: filter.create");
		
		// Template
		KatelloTemplate temp = new KatelloTemplate(templateF16Iso, templateF16Iso+" description", orgName, null);
		temp.runAs(orgAdmin);
		res = temp.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: template.create");
	}
	
	@Test(groups={TNG_POST_UPGRADE},
			dependsOnMethods={"check_permissionsRoles"},
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE},
			description="check diff. \"keys\" presence - as orgAdmin user", enabled = true)
	public void check_gpgKeyActivationKeyFilterSystemGroupTemplate(){
		/**
		 * TODO - as usual ;)!!!
		 */
		// System Group
		SSHCommandResult res;
		KatelloUser orgAdmin = new KatelloUser(userNameAdmin, null, KatelloUser.DEFAULT_ADMIN_PASS, false);
		KatelloSystemGroup sg = new KatelloSystemGroup(systemGroupMyServers,orgName,systemGroupMyServers+" description", new Integer(2));
		sg.runAs(orgAdmin);
		res = sg.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: system_group.create");
	}
	
	@Test(groups={TNG_PRE_UPGRADE}, dependsOnMethods={"create_permissionsRoles"},
			description="create: providers, products, repos {Fedora, Zoo3}. make sync - as orgAdmin user.")
	public void create_providerProductRepoSyncF16AndZoo3(){
		SSHCommandResult res;
		KatelloUser orgAdmin = new KatelloUser(userNameAdmin, null, KatelloUser.DEFAULT_ADMIN_PASS, false);
		
		KatelloProvider prov = new KatelloProvider(providerFedora, orgName, providerFedora+" description", null);
		prov.runAs(orgAdmin);
		res = prov.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: provider.create");
		
		prov = new KatelloProvider(providerZoo, orgName, providerZoo+" description", null);
		prov.runAs(orgAdmin);
		res = prov.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: provider.create");
		
		KatelloProduct prod = new KatelloProduct(productFedora, orgName, providerFedora, 
				productFedora+" description", null, null, null, null);
		res = prod.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: product.create");
		
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: product.create");
		KatelloRepo repo = new KatelloRepo(repoFedora, orgName, productFedora, 
				KatelloRepo.getFedoraMirror(KatelloRepo.FEDORA_VER16), null, null);
		res = repo.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: repo.create");
		res = repo.synchronize();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: repo.synchronize");
		
		prod = new KatelloProduct(productZoo, orgName, providerZoo, 
				productZoo+" description", null, null, null, null);
		res = prod.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: product.create");
		
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: product.create");
		repo = new KatelloRepo(repoZoo, orgName, productZoo, REPO_INECAS_ZOO3, null, null);
		res = repo.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: repo.create");
		// DON'T synchronize Zoo repo yet, keep it to be synchronized after upgrade
		
		// assign gpgkey - zoo
		prod = new KatelloProduct(productZoo, orgName, null, null, null, null, null, null);
		res = prod.update_gpgkey(gpgKeyZoo);
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: product.update");

		// assign filter - no bear package to be promoted.
		res = prod.add_filter(filterNoBear);
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: product.update");

		// assign: distro, repo, package, package_group, param to the template
		KatelloDistribution dist = new KatelloDistribution(orgName, productFedora,repoFedora, null);
		res = dist.list();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: distribution.list");
		String distroF16 = KatelloCli.grepCLIOutput("Id", res.getStdout());
		Assert.assertTrue(!distroF16.isEmpty(), "Check - distribution exists in repo");
		KatelloTemplate template = new KatelloTemplate(templateF16Iso, null, orgName, null);
		res = template.update_add_repo(productFedora, repoFedora);
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: template.add_repo");
		res = template.update_add_distribution(productFedora, distroF16);
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: template.add_distribution");
		res = template.update_add_package_group("GNOME Desktop Environment");
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: template.add_package_group");
		res = template.update_add_package("firefox");
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: template.add_package");
		res = template.update_add_param("LANG", "fr_FR");
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: template.add_param");
		
		KatelloChangeset cs1 = new KatelloChangeset("cs-fedoraZoo-"+uid, orgName, envNameTesting);
		res = cs1.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: changeset.create");
		res = cs1.update_addProduct(productFedora);
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: changeset.add_product");
		res = cs1.update_addProduct(productZoo);
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: changeset.add_product");
		res = cs1.update_addTemplate(templateF16Iso);
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: changeset.add_template");
		res = cs1.promote();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit: changeset.promote");
	}
}
