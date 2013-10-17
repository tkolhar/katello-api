package com.redhat.qe.katello.tests.cli;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloPuppetModule;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.common.KatelloUtils;

public class PuppetModuleTests extends KatelloCliTestBase {

	String prov_name;
	String prod_name;
	String repo_name;
	List<String> module_names = new ArrayList<String>();

	@BeforeClass()
	public void setUp() {
		String uid = KatelloUtils.getUniqueID();
		prov_name = "provider-"+uid;
		prod_name = "product-"+uid;
		repo_name = "puppetrepo-"+uid;

		KatelloProvider prov = new KatelloProvider(cli_worker, prov_name, base_org_name, null, null);
		exec_result = prov.create();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (provider create)");
		KatelloProduct prod = new KatelloProduct(cli_worker, prod_name, null, base_org_name, prov_name, null, null, null, null, null);
		exec_result = prod.create();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (product create)");
		KatelloRepo repo = new KatelloRepo(cli_worker, repo_name, base_org_name, prod_name, null, null, null);
		repo.content_type = "puppet";
		exec_result = repo.create();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (repo create)");

		module_names.add(uploadModuleFromPuppetlabs(repo, "jproyo", "git", "0.1.0"));
		module_names.add(uploadModuleFromPuppetlabs(repo, "puppetlabs", "nova", "2.2.0-rc1"));
		module_names.add(uploadModuleFromPuppetlabs(repo, "garethr", "docker", "0.4.1"));
	}

	@Test(description="list puppet modules")
	public void test_puppetmoduleList() {
		KatelloPuppetModule puppet = new KatelloPuppetModule(cli_worker, base_org_name, repo_name, prod_name);
		exec_result = puppet.list();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code ()");
		for(String module: module_names) {
			Assert.assertTrue(getOutput(exec_result).contains(module), "Check output (module listed)");
		}
	}

	@Test(description="info")
	public void test_puppetmoduleInfo() {
		KatelloPuppetModule puppet = new KatelloPuppetModule(cli_worker, base_org_name, repo_name, prod_name);
		exec_result = puppet.list();
		puppet.id = KatelloUtils.grepCLIOutput("ID", getOutput(exec_result));
		exec_result = puppet.info();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (puppet module info)");

		puppet.id = "0";
		exec_result = puppet.info();
		Assert.assertTrue(exec_result.getExitCode()==148, "Check exit code (puppet module info)");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloPuppetModule.ERR_NOT_FOUND, puppet.id)), "Check error (puppet module info)");
	}

	// TODO bz#1019172
	@Test(description="search")
	public void test_puppetmoduleSearch() {
		KatelloPuppetModule puppet = new KatelloPuppetModule(cli_worker, base_org_name, repo_name, prod_name);
		exec_result = puppet.search("*");
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (puppet module search)");
		Assert.assertTrue(getOutput(exec_result).contains(module_names.get(0)), "Check output (module found)");
	}

	// Downloads puppet module from Puppet Forge and uploads to repo.
	private String uploadModuleFromPuppetlabs(KatelloRepo repo, String moduleAuthor, String moduleName, String moduleVersion) {
		String file = String.format("/tmp/%s-%s-%s.tar.gz", moduleAuthor, moduleName, moduleVersion);
		String url = String.format("https://forge.puppetlabs.com/%s/%s/%s.tar.gz", moduleAuthor, moduleName, moduleVersion);
		String cmd = String.format("rm -f %s ; wget %s -O %s", file, url, file);
		exec_result = sshOnClient(cmd);
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (wget puppet module)");
		exec_result = repo.content_upload(file, "puppet", null);
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (upload puppet module)");
		return moduleAuthor+"-"+moduleName;
	}
}
