package com.redhat.qe.katello.tests.cli;

import java.util.logging.Logger;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloErrata;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.common.TngRunGroups;
import com.redhat.qe.katello.tests.e2e.PromoteErrata;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups={"cfse-cli",TngRunGroups.TNG_KATELLO_Errata})
public class ErrataTests extends KatelloCliTestBase {

	protected static Logger log = Logger.getLogger(ErrataTests.class.getName());
	
	private SSHCommandResult exec_result;
	
	private String org_name;
	private String provider_name;
	private String product_name;
	private String product_Id;
	private String repo_name;
	private String env_name;
	private String content_view;
	
	@BeforeClass(description="Generate unique objects", alwaysRun=true)
	public void setUp() {
		String uid = KatelloUtils.getUniqueID();
		org_name = "org"+uid;
		provider_name = "provider"+uid;
		product_name = "product"+uid;
		repo_name = "repo"+uid;
		env_name = "env"+uid;
		
		// Create org:
		KatelloOrg org = new KatelloOrg(this.cli_worker, this.org_name,"Package tests");
		exec_result = org.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		// Create provider:
		KatelloProvider prov = new KatelloProvider(this.cli_worker, provider_name, org_name, "Package provider", null);
		exec_result = prov.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		// Create product:
		KatelloProduct prod = new KatelloProduct(this.cli_worker, product_name, org_name, provider_name, null, null, null, null, null);
		exec_result = prod.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = prod.cli_list();
		product_Id = KatelloUtils.grepCLIOutput("ID", getOutput(exec_result).trim(),1);
	
		KatelloRepo repo = new KatelloRepo(this.cli_worker, repo_name, org_name, product_name, REPO_INECAS_ZOO3, null, null);
		exec_result = repo.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		KatelloEnvironment env = new KatelloEnvironment(this.cli_worker, env_name, null, org_name, KatelloEnvironment.LIBRARY);
		exec_result = env.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
				
		prov.synchronize();

		content_view = KatelloUtils.promoteRepoToEnvironment(cli_worker, org_name, product_name, repo_name, env_name);
	}
	
	@Test(description="8782a6e0-f41a-48d5-8599-bfe7f24078f6")
	public void test_errataList() {
		KatelloErrata errata = new KatelloErrata(cli_worker, org_name, product_name, repo_name, content_view);
		errata.setProductId(product_Id);
		
		exec_result = errata.cli_list();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code (errata list)");
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", "").contains(PromoteErrata.ERRATA_ZOO_SEA), "Check - errata list output");
	}

	@Test(description="2542ae61-8de6-40ce-866f-999c39fa9018")
	public void test_errataInfo() {
		KatelloErrata errata = new KatelloErrata(cli_worker, org_name, product_name, repo_name, content_view);
		errata.setId(PromoteErrata.ERRATA_ZOO_SEA);
		errata.setProductId(product_Id);
		
		exec_result = errata.info();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code (errata info)");
		Assert.assertTrue(getOutput(exec_result).replaceAll("\n", "").contains(PromoteErrata.ERRATA_ZOO_SEA), "Check - errata info output");
	}
}
