package com.redhat.qe.katello.tests.cli;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloGpgKey;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.tngext.TngPriority;
import com.redhat.qe.katello.common.KatelloConstants;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.common.TngRunGroups;
import com.redhat.qe.tools.SSHCommandResult;

@TngPriority(18)
@Test(groups={TngRunGroups.TNG_KATELLO_Providers_Repos})
public class GpgKeyTests extends KatelloCliTestBase{
	private String rand;
	private String org;
	private String gpg;
	private String filename;
	
	@BeforeClass(description="init: create org, prepare gpg file on disk", alwaysRun=true)
	public void setUp(){
		SSHCommandResult res;
		rand = KatelloUtils.getUniqueID();
		this.org = "gpg-"+rand;
		this.filename = "/tmp/RPM-GPG-KEY-dummy-packages-generator";
		this.gpg = "gpgkey-"+rand;

		KatelloOrg org = new KatelloOrg(this.cli_worker, this.org, "Org for GPG cli tests");
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (org create)");
		
		String cmd = "rm -f "+this.filename+"; " +
				"curl -sk "+KatelloGpgKey.REPO_GPG_FILE_ZOO+" -o "+this.filename;
		res = sshOnClient(cmd);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (get gpg file)");
	}

	@Test(description="simply create a gpg key: nothing yet")
	public void create_noProdnoRepo(){
		SSHCommandResult res;
		KatelloGpgKey gpg = new KatelloGpgKey(cli_worker, this.gpg, this.org, this.filename);
		res = gpg.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, 
				"Check - return code (gpg_key create)");
		Assert.assertTrue(getOutput(res).trim().equals(String.format(KatelloGpgKey.OUT_CREATE,this.gpg)), 
				"Check - stdout (gpg_key create)");
	}

	@Test(description="info on gpg key - nothing yet", dependsOnMethods={"create_noProdnoRepo"})
	public void info_noProdnoRepo(){
		SSHCommandResult res;
		KatelloGpgKey gpg = new KatelloGpgKey(cli_worker, this.gpg, this.org, null);
		res = gpg.cli_info();
		String prods = KatelloUtils.grepCLIOutput("Products", getOutput(res));
		String repos = KatelloUtils.grepCLIOutput("Repositories", getOutput(res));
		Assert.assertTrue(res.getExitCode().intValue()==0,"Check - return code (gpg_key info)");
		Assert.assertTrue(prods.equals(KatelloCli.OUT_EMPTY_LIST), "Check - product list is empty");
		Assert.assertTrue(repos.equals(KatelloCli.OUT_EMPTY_LIST), "Check - repositories list is empty");
	}
	
	@Test(description="list gpg keys", dependsOnMethods={"create_noProdnoRepo"})
	public void list(){
		SSHCommandResult res = new KatelloGpgKey(cli_worker, this.gpg, this.org, null).cli_list();
		Assert.assertTrue(res.getExitCode().intValue()==0,"Check - return code (gpg_key list)");
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(String.format(KatelloGpgKey.REGEXP_GPG, this.gpg)),
				"Check - gpg key should be listed");
	}
	
	@Test(description="list gpg keys", dependsOnMethods={"create_noProdnoRepo","info_noProdnoRepo","list"})
	public void delete(){
		KatelloGpgKey gpg = new KatelloGpgKey(cli_worker, this.gpg, this.org, null);
		SSHCommandResult res = gpg.cli_delete();
		Assert.assertTrue(res.getExitCode().intValue()==0,"Check - return code (gpg_key delete)");
		Assert.assertFalse(getOutput(res).replaceAll("\n", "").matches(String.format(KatelloGpgKey.REGEXP_GPG, this.gpg)),
				"Check - gpg key should not be listed");
		res = gpg.cli_info();
		Assert.assertTrue(res.getExitCode().intValue()==65,"Check - return code (gpg_key info)");
		Assert.assertTrue(getOutput(res).equals(String.format(KatelloGpgKey.ERR_KEY_NOT_FOUND, this.gpg)),
				"Check - gpg info error string");
	}

	@Test(description="update gpg key")
	public void test_updateKey() {
		String uid = KatelloUtils.getUniqueID();
		String gpgkey_name = "gpg-key"+uid;
		String gpgkey_newname = "gpg-key-new-"+uid;
		String file_not_exist = "/tmp/gpg-no-such-file-"+uid;

		KatelloGpgKey gpgkey = new KatelloGpgKey(cli_worker, gpgkey_name, org, filename);
		exec_result = gpgkey.cli_create();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (create key)");

		exec_result = gpgkey.update(null, filename);
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (gpgkey update)");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloGpgKey.OUT_UPDATE, gpgkey_name)), "Check output (gpgkey update)");

		exec_result = gpgkey.update(gpgkey_newname, null);
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (gpgkey rename)");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloGpgKey.OUT_UPDATE, gpgkey_name)), "Check output (gpgkey update)");

		exec_result = gpgkey.update(null, file_not_exist);
		Assert.assertTrue(exec_result.getExitCode()==65, "Check exit code (gpgkey rename)");
		Assert.assertTrue(getOutput(exec_result).equals("No such file or directory"), "Check output (gpgkey update)");
	}

	@Test(description="call commands on non existing key")
	public void test_invalidGpgKey() {
		String uid = KatelloUtils.getUniqueID();
		String org_name = "org-"+uid;
		String gpgkey_name = "gpgkey-"+uid;
		String file_not_exist = "/tmp/gpg-no-such-file-"+uid;
		KatelloOrg org = new KatelloOrg(cli_worker, org_name, null);
		exec_result = org.cli_create();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (create org)");

		KatelloGpgKey gpgkey = new KatelloGpgKey(cli_worker, gpgkey_name, org_name, file_not_exist);
		exec_result = gpgkey.cli_list();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (gpgkey list)");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloGpgKey.OUT_NO_KEYS, org_name)), "Check error");
		exec_result = gpgkey.cli_delete();
		Assert.assertTrue(exec_result.getExitCode()==65, "Check exit code (gpgkey delete)");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloGpgKey.ERR_KEY_NOT_FOUND, gpgkey_name)), "Check error");
		exec_result = gpgkey.update("new"+gpgkey_name, null);
		Assert.assertTrue(exec_result.getExitCode()==65, "Check exit code (gpgkey update)");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloGpgKey.ERR_KEY_NOT_FOUND, gpgkey_name)), "Check error");
		exec_result = gpgkey.cli_create();
		Assert.assertTrue(exec_result.getExitCode()==65, "Check exit code (gpgkey create)");
		Assert.assertTrue(getOutput(exec_result).equals("No such file or directory"), "Check output (gpgkey cerate)");
	}
}
