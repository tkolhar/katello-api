package com.redhat.qe.katello.tests.cli;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloGpgKey;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.common.KatelloConstants;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.common.TngRunGroups;
import com.redhat.qe.tools.SSHCommandResult;

/**
 * Some extended gpgkey scenarios related with product/repo combinations.
 * @author gkhachik
 *
 */
@Test(groups={KatelloConstants.TNG_CFSE_CLI,TngRunGroups.TNG_KATELLO_Providers_Repos})
public class GpgKeyExtTests extends KatelloCliTestBase{
	private String uid;
	private String org;
	private String gpg;
	private String filename;
	private String repo_name = "gpg-repo1";
	private String repo_name2 = "gpg-repo2";
	private final String GPG_KEY = "GPG Key";

	private String productNameWithGpg1;

	@BeforeClass(description="init: create org, prepare gpg file on disk", alwaysRun=true)
	public void setUp(){
		SSHCommandResult res;
		uid = KatelloUtils.getUniqueID();
		this.org = "gpgExt-"+uid;
		this.filename = "/tmp/RPM-GPG-KEY-dummy-packages-generator";

		this.gpg = "key-"+uid;
		productNameWithGpg1 = "prodGpg1-"+uid;

		// create org
		KatelloOrg org = new KatelloOrg(this.cli_worker, this.org, "Org for GPG extended cli tests");
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (org create)");

		// wget the gpg file
		String cmd = "rm -f "+this.filename+"; " +
				"curl -sk "+KatelloGpgKey.REPO_GPG_FILE_ZOO+" -o "+this.filename;
		res = sshOnClient(cmd);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (get gpg file)");

		// create gpg key in katello
		KatelloGpgKey gpg = new KatelloGpgKey(cli_worker, this.gpg, this.org, this.filename);
		res = gpg.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (gpg_key create)");
	}

	@Test(description="creating product by giving non-existing gpgkey")
	public void test_createProduct_wrongGpgkey(){
		String rand = KatelloUtils.getUniqueID();
		KatelloProvider prov = new KatelloProvider(this.cli_worker, "gpgext-"+rand, this.org, null, null);
		prov.create();
		String gpgNotExist = "gpg-"+rand;
		KatelloProduct prod = new KatelloProduct(this.cli_worker, "prod-"+rand, this.org, prov.name, null, gpgNotExist, null, null, null);
		SSHCommandResult res = prod.create();
		Assert.assertTrue(res.getExitCode()==148, "Check return code (product create with wrong gpgkey name)");
		Assert.assertTrue(getOutput(res).equals(String.format(KatelloProduct.ERR_GPGKEY_NOTFOUND, gpgNotExist)), "Check - error string");
	}

	@Test(description="create product with gpgkey")
	public void test_createProduct_validGpg(){
		SSHCommandResult res;
		String _prov = "1gpgprov-"+uid;

		KatelloProvider prov = new KatelloProvider(this.cli_worker, _prov, this.org, null, null);
		prov.create();
		KatelloProduct prod = new KatelloProduct(this.cli_worker, productNameWithGpg1, org, _prov, null, gpg, null, null, null);
		res = prod.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create --gpgkey)");
		Assert.assertTrue(getOutput(res).equals(getText("product.create.stdout", productNameWithGpg1)), "Check - stdout (product create)");
	}


	@Test(description="add repo to the product with gpg key", dependsOnMethods={"test_createProduct_validGpg"})
	public void test_add2Repos_WithGpg(){
		SSHCommandResult res;	
		KatelloRepo repo = new KatelloRepo(this.cli_worker, repo_name, org, productNameWithGpg1, REPO_INECAS_ZOO3, gpg, null);
		res = repo.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo create)");

		res = repo.info();
		String repo_gpg = KatelloUtils.grepCLIOutput(GPG_KEY, getOutput(res));
		Assert.assertTrue(repo_gpg!= null && !repo_gpg.isEmpty(), "Check - repo info contains \"GPG Key\" value");
		Assert.assertTrue(repo_gpg.equals(gpg), "Check - repo gpg key info is what we expect");

		KatelloRepo repo2 = new KatelloRepo(this.cli_worker, repo_name2, org, productNameWithGpg1, REPO_INECAS_ZOO3, gpg, null);
		res = repo2.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo create)");

		res = repo2.info();
		repo_gpg = KatelloUtils.grepCLIOutput(GPG_KEY, getOutput(res));
		Assert.assertTrue(repo_gpg!= null && !repo_gpg.isEmpty(), "Check - repo info contains \"GPG Key\" value");
		Assert.assertTrue(repo_gpg.equals(gpg), "Check - repo gpg key info is what we expect");
	}

	@Test(description="add repo to the product with gpg key", dependsOnMethods={"test_createProduct_validGpg"})
	public void test_addRepo_productHasGpg(){
		SSHCommandResult res;
		String repo_name = "repo-with-gpg";
		KatelloRepo repo = new KatelloRepo(this.cli_worker, repo_name, org, productNameWithGpg1, REPO_INECAS_ZOO3, null, null);
		res = repo.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo create)");
		Assert.assertTrue(getOutput(res).equals(getText("repo.create.stdout", repo_name)), "Check - stdout (repo create)");

		res = repo.info();
		String repo_gpg = KatelloUtils.grepCLIOutput(GPG_KEY, getOutput(res));
		Assert.assertTrue(repo_gpg!= null && !repo_gpg.isEmpty(), "Check - repo info contains \"GPG Key\" value");
		Assert.assertTrue(repo_gpg.equals(gpg), "Check - repo gpg key info is what we expect");
	}

	@Test(description="add repo to the product without gpg key", dependsOnMethods={"test_createProduct_validGpg"})
	public void test_addRepoNoGpg(){
		SSHCommandResult res;
		String repo_name = "repo-no-gpg";
		KatelloRepo repo = new KatelloRepo(this.cli_worker, repo_name, org, productNameWithGpg1, REPO_INECAS_ZOO3, null, new Boolean(true));
		res = repo.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo create)");
		Assert.assertTrue(getOutput(res).equals(getText("repo.create.stdout", repo_name)), "Check - stdout (repo create)");

		res = repo.info();
		String repo_gpg = KatelloUtils.grepCLIOutput(GPG_KEY, getOutput(res));
		Assert.assertTrue(repo_gpg!= null && repo_gpg.isEmpty(), "Check - repo info contains no \"GPG Key\" value");
		Assert.assertTrue(!repo_gpg.equals(gpg), "Check - repo gpg key info is what we expect");
	}

	@Test(description="add repo with its own gpg key", dependsOnMethods={"test_createProduct_validGpg"})
	public void test_addRepoOtherGpg(){
		SSHCommandResult res;
		String anotherGpg_name = "another-"+this.gpg;
		KatelloGpgKey another_gpg = new KatelloGpgKey(cli_worker, anotherGpg_name, this.org, this.filename);
		res = another_gpg.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (gpg_key create)");

		String repo_name = "repo-another-gpg";
		KatelloRepo repo = new KatelloRepo(this.cli_worker, repo_name, org, productNameWithGpg1, REPO_INECAS_ZOO3, anotherGpg_name, null);
		res = repo.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo create)");
		Assert.assertTrue(getOutput(res).equals(getText("repo.create.stdout", repo_name)), "Check - stdout (repo create)");

		res = repo.info();
		String repo_gpg = KatelloUtils.grepCLIOutput(GPG_KEY, getOutput(res));
		Assert.assertTrue(repo_gpg!= null && !repo_gpg.isEmpty(), "Check - repo info contains no \"GPG Key\" value");
		Assert.assertTrue(repo_gpg.equals(anotherGpg_name), "Check - repo gpg key info is what we expect");
	}

	// bz#988950
	@Test(description="delete gpg key verify that it is deleted from repos as well", 
			dependsOnMethods={"test_add2Repos_WithGpg","test_addRepo_productHasGpg"}, enabled=false)
	public void test_deleteGPG(){
		KatelloGpgKey gpg = new KatelloGpgKey(cli_worker, this.gpg, this.org, null);
		SSHCommandResult res = gpg.cli_delete();
		Assert.assertTrue(res.getExitCode().intValue()==0,"Check - return code (gpg_key delete)");
		Assert.assertFalse(getOutput(res).replaceAll("\n", "").matches(String.format(KatelloGpgKey.REGEXP_GPG, this.gpg)),
				"Check - gpg key should not be listed");
		res = gpg.cli_info();
		Assert.assertTrue(res.getExitCode().intValue()==65,"Check - return code (gpg_key info)");
		Assert.assertTrue(getOutput(res).equals(String.format(KatelloGpgKey.ERR_KEY_NOT_FOUND, this.gpg)),
				"Check - gpg info error string");

		KatelloRepo repo = new KatelloRepo(this.cli_worker, repo_name, org, productNameWithGpg1, REPO_INECAS_ZOO3, null, null);
		res = repo.info();
		String repo_gpg = KatelloUtils.grepCLIOutput(GPG_KEY, getOutput(res));
		Assert.assertTrue(repo_gpg.isEmpty(), "Check - repo info contains no \"GPG Key\" value");
		Assert.assertTrue(!repo_gpg.equals(gpg), "Check - repo gpg key info is what we expect");

		repo = new KatelloRepo(this.cli_worker, repo_name2, org, productNameWithGpg1, REPO_INECAS_ZOO3, null, null);
		res = repo.info();
		repo_gpg = KatelloUtils.grepCLIOutput(GPG_KEY, getOutput(res));
		Assert.assertTrue(repo_gpg!= null && repo_gpg.isEmpty(), "Check - repo info contains no \"GPG Key\" value");
		Assert.assertTrue(!repo_gpg.equals(gpg), "Check - repo gpg key info is what we expect");
	}

	// bz#1009428
	@Test(description="product update nogpgkey", enabled=true)
	public void test_productGpgKeys() {
		String uid = KatelloUtils.getUniqueID();
		String prov_name = "provider-"+uid;
		String prod_name = "product-"+uid;
		String gpgkey_name = "gpg-key-"+uid;
		exec_result = new KatelloGpgKey(cli_worker, gpgkey_name, org, filename).cli_create();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (create gpgkey)");
		KatelloProvider prov = new KatelloProvider(cli_worker, prov_name, org, null, null);
		exec_result = prov.create();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (create provider)");
		KatelloProduct prod = new KatelloProduct(cli_worker, prod_name, org, prov_name, null, gpgkey_name, null, null, null);
		exec_result = prod.create();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (create product)");
		exec_result = prod.cli_list();
		Assert.assertTrue(getOutput(exec_result).contains(gpgkey_name), "Check output (gpgkey is set)");
		exec_result = prod.update_nogpgkey(false);
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (unset gpgkey)");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloProduct.OUT_UPDATED, prod_name)), "Check output (unset gpgkey)");
		exec_result = prod.cli_list();
		Assert.assertFalse(getOutput(exec_result).contains(gpgkey_name), "Check output (gpgkey is unset)");
	}
}
