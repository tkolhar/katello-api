package com.redhat.qe.katello.tests.cli;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.redhat.qe.auto.testng.Assert;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.katello.base.obj.KatelloGpgKey;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.common.KatelloConstants;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.tasks.KatelloTasks;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups={KatelloConstants.TNG_CFSE_CLI})
public class GpgKeyTests extends KatelloCliTestScript{
	private String rand;
	private String org;
	private String gpg;
	private String filename;
	
	@BeforeClass(description="init: create org, prepare gpg file on disk")
	public void setUp(){
		SSHCommandResult res;
		rand = KatelloTestScript.getUniqueID();
		this.org = "gpg-"+rand;
		this.filename = "/tmp/RPM-GPG-KEY-dummy-packages-generator";
		this.gpg = "gpgkey-"+rand;

		KatelloOrg org = new KatelloOrg(this.org, "Org for GPG cli tests");
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (org create)");
		
		String cmd = "rm -f "+this.filename+"; " +
				"curl -sk "+KatelloGpgKey.REPO_GPG_FILE_ZOO+" -o "+this.filename;
		res = KatelloUtils.sshOnClient(cmd);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (get gpg file)");
	}

	@Test(description="simply create a gpg key: nothing yet")
	public void create_noProdnoRepo(){
		SSHCommandResult res;
		KatelloGpgKey gpg = new KatelloGpgKey(this.gpg, this.org, this.filename);
		res = gpg.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, 
				"Check - return code (gpg_key create)");
		Assert.assertTrue(getOutput(res).trim().equals(String.format(KatelloGpgKey.OUT_CREATE,this.gpg)), 
				"Check - stdout (gpg_key create)");
	}

	@Test(description="info on gpg key - nothing yet", dependsOnMethods={"create_noProdnoRepo"})
	public void info_noProdnoRepo(){
		SSHCommandResult res;
		KatelloGpgKey gpg = new KatelloGpgKey(this.gpg, this.org, null);
		res = gpg.cli_info();
		String prods = KatelloTasks.grepCLIOutput("Products", getOutput(res));
		String repos = KatelloTasks.grepCLIOutput("Repositories", getOutput(res));
		Assert.assertTrue(res.getExitCode().intValue()==0,"Check - return code (gpg_key info)");
		Assert.assertTrue(prods.equals(KatelloCli.OUT_EMPTY_LIST), "Check - product list is empty");
		Assert.assertTrue(repos.equals(KatelloCli.OUT_EMPTY_LIST), "Check - repositories list is empty");
	}
	
	@Test(description="list gpg keys", dependsOnMethods={"create_noProdnoRepo"})
	public void list(){
		SSHCommandResult res = new KatelloGpgKey(this.gpg, this.org, null).cli_list();
		Assert.assertTrue(res.getExitCode().intValue()==0,"Check - return code (gpg_key list)");
		String REGEXP_GPG = ".*Name:\\s+"+this.gpg+".*";
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(REGEXP_GPG),
				"Check - gpg key should be listed");
	}
	
	@Test(description="list gpg keys", dependsOnMethods={"create_noProdnoRepo","info_noProdnoRepo","list"})
	public void delete(){
		KatelloGpgKey gpg = new KatelloGpgKey(this.gpg, this.org, null);
		SSHCommandResult res = gpg.cli_delete();
		Assert.assertTrue(res.getExitCode().intValue()==0,"Check - return code (gpg_key delete)");
		String REGEXP_GPG = ".*Name:\\s+"+this.gpg+".*";
		Assert.assertFalse(getOutput(res).replaceAll("\n", "").matches(REGEXP_GPG),
				"Check - gpg key should not be listed");
		res = gpg.cli_info();
		Assert.assertTrue(res.getExitCode().intValue()==65,"Check - return code (gpg_key info)");
		Assert.assertTrue(getOutput(res).equals(String.format(KatelloGpgKey.ERR_KEY_NOT_FOUND, this.gpg)),
				"Check - gpg info error string");
	}
	
}
