package com.redhat.qe.katello.tests.cli;

import java.io.File;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.tools.SSHCommandResult;
import com.redhat.qe.tools.SCPTools;

	
@Test(groups={"headpin-cli"})
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
		SCPTools scp = new SCPTools(
        System.getProperty("katello.client.hostname", "localhost"), 
        System.getProperty("katello.client.ssh.user", "root"), 
        System.getProperty("katello.client.sshkey.private", ".ssh/id_hudson_dsa"), 
        System.getProperty("katello.client.sshkey.passphrase", "null"));
		Assert.assertTrue(scp.sendFile("data"+File.separator+"stack-manifest.zip", "/tmp"),"stack-manifest.zip sent successfully");    
	 	
	}

	
	@Test(description = "Content test check whether the registered consumer is " +
			"able to access repo and obtain contents")
	public void test_Content()
	{

		KatelloUtils.sshOnClient("yum-config-manager --enable beaker-HighAvailability beaker-LoadBalancer beaker-ResilientStorage beaker-ScalableFileSystem beaker-Server beaker-debuginfo beaker-harness beaker-optional beaker-tasks");
		KatelloUtils.sshOnClient("subscription-manager register --user admin --password admin --org ACME_Corporation --environment DEV --force");
		KatelloUtils.sshOnClient("yum repolist");
		KatelloUtils.sshOnClient("yum install -y zsh");
	}


}
