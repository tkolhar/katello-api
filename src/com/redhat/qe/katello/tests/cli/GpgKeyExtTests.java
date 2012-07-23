package com.redhat.qe.katello.tests.cli;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.katello.base.obj.KatelloGpgKey;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
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
	private String rand;
	private String org;
	private String gpg;
	private String filename;
	
	@BeforeClass(description="init: create org, prepare gpg file on disk")
	public void setUp(){
		SSHCommandResult res;
		rand = KatelloTestScript.getUniqueID();
		this.org = "gpgExt-"+rand;
		this.filename = "/tmp/RPM-GPG-KEY-dummy-packages-generator";
		this.gpg = "key-"+rand;

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
		String rand = KatelloTestScript.getUniqueID();
		KatelloProvider prov = new KatelloProvider("gpgext-"+rand, this.org, null, null);
		prov.create();
		String gpgNotExist = "gpg-"+rand;
		KatelloProduct prod = new KatelloProduct("prod-"+rand, this.org, prov.name, null, gpgNotExist, null, null, null);
		SSHCommandResult res = prod.create();
		Assert.assertTrue(res.getExitCode()==244, "Check return code (product create with wrong gpgkey name)");
		Assert.assertTrue(getOutput(res).equals(String.format(KatelloProduct.ERR_GPGKEY_NOTFOUND, gpgNotExist)), "Check - error string");
	}
	
	@Test(description="create product specifying gpgkey")
	public void test_createProduct_validGpg(){
		String _prov = "gpgProv1-"+this.rand;
		String _prod = "gpgProd1-"+this.rand;
		KatelloProvider prov = new KatelloProvider(_prov, this.org, null, null);
		prov.create();
		
	}
}
