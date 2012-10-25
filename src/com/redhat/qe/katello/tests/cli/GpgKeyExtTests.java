package com.redhat.qe.katello.tests.cli;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.obj.KatelloGpgKey;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.common.KatelloConstants;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

/**
 * Some extended gpgkey scenarios related with product/repo combinations.
 * @author gkhachik
 *
 */
@Test(groups={KatelloConstants.TNG_CFSE_CLI})
public class GpgKeyExtTests extends KatelloCliTestScript{
	private String uid;
	private String org;
	private String gpg;
	private String filename;
	
	private String productNameWithGpg1;

	@BeforeClass(description="init: create org, prepare gpg file on disk")
	public void setUp(){
		SSHCommandResult res;
		uid = KatelloUtils.getUniqueID();
		this.org = "gpgExt-"+uid;
		this.filename = "/tmp/RPM-GPG-KEY-dummy-packages-generator";
		
		this.gpg = "key-"+uid;
		productNameWithGpg1 = "prodGpg1-"+uid;

		// create org
		KatelloOrg org = new KatelloOrg(this.org, "Org for GPG extended cli tests");
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (org create)");

		// wget the gpg file
		String cmd = "rm -f "+this.filename+"; " +
				"curl -sk "+KatelloGpgKey.REPO_GPG_FILE_ZOO+" -o "+this.filename;
		res = KatelloUtils.sshOnClient(cmd);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (get gpg file)");

		// create gpg key in katello
		KatelloGpgKey gpg = new KatelloGpgKey(this.gpg, this.org, this.filename);
		res = gpg.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (gpg_key create)");
	}

	@Test(description="creating product by giving non-existing gpgkey")
	public void test_createProduct_wrongGpgkey(){
		String rand = KatelloUtils.getUniqueID();
		KatelloProvider prov = new KatelloProvider("gpgext-"+rand, this.org, null, null);
		prov.create();
		String gpgNotExist = "gpg-"+rand;
		KatelloProduct prod = new KatelloProduct("prod-"+rand, this.org, prov.name, null, gpgNotExist, null, null, null);
		SSHCommandResult res = prod.create();
		Assert.assertTrue(res.getExitCode()==148, "Check return code (product create with wrong gpgkey name)");
		Assert.assertTrue(getOutput(res).equals(String.format(KatelloProduct.ERR_GPGKEY_NOTFOUND, gpgNotExist)), "Check - error string");
	}

	@Test(description="create product with gpgkey")
	public void test_createProduct_validGpg(){
		SSHCommandResult res;
		String _prov = "1gpgprov-"+uid;
		
		KatelloProvider prov = new KatelloProvider(_prov, this.org, null, null);
		prov.create();
		KatelloProduct prod = new KatelloProduct(productNameWithGpg1, org, _prov, null, gpg, null, null, null);
		res = prod.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create --gpgkey)");
		Assert.assertTrue(getOutput(res).equals(getText("product.create.stdout", productNameWithGpg1)), "Check - stdout (product create)");
	}
	
	@Test(description="add repo to the product with gpg key", dependsOnMethods={"test_createProduct_validGpg"})
	public void test_addRepo_productHasGpg(){
		SSHCommandResult res;
		String repo_name = "repo-with-gpg";
		KatelloRepo repo = new KatelloRepo(repo_name, org, productNameWithGpg1, REPO_INECAS_ZOO3, null, null);
		res = repo.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo create)");
		Assert.assertTrue(getOutput(res).equals(getText("repo.create.stdout", repo_name)), "Check - stdout (repo create)");
		
		res = repo.info();
		String repo_gpg = KatelloCli.grepCLIOutput("GPG key", getOutput(res));
		Assert.assertTrue(!repo_gpg.isEmpty(), "Check - repo info contains \"GPG key\" value");
		Assert.assertTrue(repo_gpg.equals(gpg), "Check - repo gpg key info is what we expect");
	}
	
	@Test(description="add repo to the product without gpg key", dependsOnMethods={"test_createProduct_validGpg"})
	public void test_addRepoNoGpg(){
		SSHCommandResult res;
		String repo_name = "repo-no-gpg";
		KatelloRepo repo = new KatelloRepo(repo_name, org, productNameWithGpg1, REPO_INECAS_ZOO3, null, new Boolean(true));
		res = repo.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo create)");
		Assert.assertTrue(getOutput(res).equals(getText("repo.create.stdout", repo_name)), "Check - stdout (repo create)");
		
		res = repo.info();
		String repo_gpg = KatelloCli.grepCLIOutput("GPG key", getOutput(res));
		Assert.assertTrue(repo_gpg.isEmpty(), "Check - repo info contains no \"GPG key\" value");
		Assert.assertTrue(!repo_gpg.equals(gpg), "Check - repo gpg key info is what we expect");
	}

	@Test(description="add repo with its own gpg key", dependsOnMethods={"test_createProduct_validGpg"})
	public void test_addRepoOtherGpg(){
		SSHCommandResult res;
		String anotherGpg_name = "another-"+this.gpg;
		KatelloGpgKey another_gpg = new KatelloGpgKey(anotherGpg_name, this.org, this.filename);
		res = another_gpg.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (gpg_key create)");
		
		String repo_name = "repo-another-gpg";
		KatelloRepo repo = new KatelloRepo(repo_name, org, productNameWithGpg1, REPO_INECAS_ZOO3, anotherGpg_name, null);
		res = repo.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo create)");
		Assert.assertTrue(getOutput(res).equals(getText("repo.create.stdout", repo_name)), "Check - stdout (repo create)");
		
		res = repo.info();
		String repo_gpg = KatelloCli.grepCLIOutput("GPG key", getOutput(res));
		Assert.assertTrue(!repo_gpg.isEmpty(), "Check - repo info contains no \"GPG key\" value");
		Assert.assertTrue(repo_gpg.equals(anotherGpg_name), "Check - repo gpg key info is what we expect");
	}
}
