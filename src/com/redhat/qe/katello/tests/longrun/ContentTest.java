package com.redhat.qe.katello.tests.longrun;

import java.io.File;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.tools.SSHCommandResult;
import com.redhat.qe.tools.SCPTools;

	
@Test(groups={"cfse-cli"})
public class ContentTest extends KatelloCliTestScript{

	private SSHCommandResult res;
	private String org_name;
	private String env_name;
	private KatelloOrg org;
	private KatelloEnvironment env;

	
	@BeforeClass(description="init: create initial stuff")
	public void setUp()
	{
		
		String uid = KatelloUtils.getUniqueID();
		org_name = "org-content" + uid;
		env_name = "env-content" + uid;
		org = new KatelloOrg(this.org_name,"Org-content Created");
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		env = new KatelloEnvironment(this.env_name, "Env-content Created",this.org_name,KatelloEnvironment.LIBRARY);
		res = env.cli_create();
		Assert.assertTrue(res.getExitCode() == 0 , "Check - return code");
		KatelloProvider provider = new KatelloProvider(KatelloProvider.PROVIDER_REDHAT,org_name,null,null);
		SCPTools scp = new SCPTools(
        System.getProperty("katello.client.hostname", "localhost"), 
        System.getProperty("katello.client.ssh.user", "root"), 
        System.getProperty("katello.client.sshkey.private", ".ssh/id_hudson_dsa"), 
        System.getProperty("katello.client.sshkey.passphrase", "null"));
		Assert.assertTrue(scp.sendFile("data"+File.separator+"manifest.zip", "/tmp"),"manifest.zip sent successfully");    
		res = provider.import_manifest("/tmp"+File.separator+"manifest.zip", new Boolean(true));
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		KatelloProduct prod=new KatelloProduct(KatelloProduct.RHEL_SERVER,org_name, KatelloProvider.PROVIDER_REDHAT, null, null, null,null, null);
		res = prod.repository_set_enable(KatelloProduct.REPOSET_RHEL6_RPMS);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo set enable)");
		KatelloRepo repo = new KatelloRepo(KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT,org_name, KatelloProduct.RHEL_SERVER, null, null, null);
		SSHCommandResult res = repo.enable();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo enable)");
		Assert.assertTrue(getOutput(res).contains("enabled."),"Message - (repo enable)");
		res = repo.synchronize();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo synchronize)");
	}

	
	@Test(description = "Content test check whether the registered consumer is " +
			"able to access repo and obtain contents")
	public void test_Content()
	{
		res = KatelloUtils.sshOnClient("rpm -qa | grep yum-utils");
		int exitCode = res.getExitCode().intValue();
		if(exitCode == 1)
		{
			res = KatelloUtils.sshOnClient("yum install -y yum-utils");
			Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");

		}
		res = KatelloUtils.sshOnClient("yum-config-manager --enable beaker-HighAvailability beaker-LoadBalancer beaker-ResilientStorage beaker-ScalableFileSystem beaker-Server beaker-debuginfo beaker-harness beaker-optional beaker-tasks");
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		res = KatelloUtils.sshOnClient("subscription-manager register --user admin --password admin --org "+ this.org_name +" --environment "+this.env_name +" --force");
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		res = KatelloUtils.sshOnClient("yum repolist");
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		res = KatelloUtils.sshOnClient("yum install -y zsh");
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");

	}


}
