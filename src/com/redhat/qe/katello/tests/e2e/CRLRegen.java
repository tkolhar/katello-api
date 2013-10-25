package com.redhat.qe.katello.tests.e2e;

import java.util.logging.Logger;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups={"cfse-e2e"}, singleThreaded = true)
public class CRLRegen extends KatelloCliTestBase{
	protected static Logger log = Logger.getLogger(BPMTests.class.getName());
	
	private SSHCommandResult exec_result;

	// Katello objects below
	private String org_name;
	private String env_name;
	private String system_name;
	private String repo_name;
	private String provider_name;
	private String product_name;
	
	private String contentView;
	
	@BeforeClass(description="Generate unique names")
	public void setUp(){
		String uid = KatelloUtils.getUniqueID();
		org_name = "org_"+uid;
		env_name = "env_"+uid;
		system_name = "system_"+uid;
		repo_name = "repo"+uid;
		provider_name = "provider"+uid;
		product_name = "product"+uid;
		
		sshOnClient("yum -y erase wolf lion zebra stork mouse tiger || true");
		rhsm_clean();
		
		// Create org:
		KatelloOrg org = new KatelloOrg(this.cli_worker, org_name, "Org 1");
		exec_result = org.cli_create();
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertEquals(getOutput(exec_result).trim(), "Successfully created org [ "+org_name+" ]");
			
		// Create provider:
		KatelloProvider prov = new KatelloProvider(this.cli_worker, provider_name, org_name, "Package provider", null);
		exec_result = prov.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		// Create product:
		KatelloProduct prod = new KatelloProduct(this.cli_worker, product_name, org_name, provider_name, null, null, null, null, null);
		exec_result = prod.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	
		KatelloRepo repo = new KatelloRepo(this.cli_worker, repo_name, org_name, product_name, REPO_INECAS_ZOO3, null, null);
		exec_result = repo.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		KatelloEnvironment env = new KatelloEnvironment(this.cli_worker, env_name, null, org_name, KatelloEnvironment.LIBRARY);
		exec_result = env.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = prov.synchronize();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = prod.synchronize();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = repo.synchronize();
		
		this.contentView = KatelloUtils.promoteProductToEnvironment(cli_worker, org_name, product_name, env_name);
	}
	
	/**
	 * Verify that "yum install" works.
	 * Unsubscribe client.
	 * Verify that "yum install" fails.
	 * Delete CRL list.
	 * Verify that "yum install" fails.
	 * Call "admin crl_regen"
	 * Verify that "yum install" fails.
	 * Subscribe client.
	 * Verify that "yum install" works.
	 */
	@Test(description="CRL Regeneration")
	public void test_crl_regen() {
		
		KatelloSystem sys = new KatelloSystem(this.cli_worker, system_name, this.org_name, this.env_name+"/"+this.contentView);
		exec_result = sys.rhsm_registerForce(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		
		exec_result = sys.subscriptions_available();
		String poolId1 = KatelloUtils.grepCLIOutput("ID", getOutput(exec_result).trim(),1);
		Assert.assertNotNull(poolId1, "Check - pool Id is not null");
		
		exec_result = sys.subscribe(poolId1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		sys.rhsm_refresh();
		
		sshOnClient("service goferd restart;");
		
		exec_result = sshOnClient("yum -y install lion --nogpgcheck");
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code (install lion)");
		
		sshOnClient("subscription-manager unsubscribe --all");
		
//		yum_clean();
		exec_result = sshOnClient("yum -y install zebra --nogpgcheck");
		Assert.assertTrue(exec_result.getExitCode().intValue()==1, "Check - return code (install zebra)");
		
		KatelloUtils.sshOnServer("rm -f /var/lib/candlepin/candlepin-crl.crl");
		
		exec_result = sshOnClient("yum -y install zebra --nogpgcheck");
		Assert.assertTrue(exec_result.getExitCode().intValue()==1, "Check - return code (install zebra)");
		
		KatelloCli cli = new KatelloCli("admin crl_regen", null,null,cli_worker.getClientHostname());
		exec_result = cli.run();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code (admin crl_regen)");
		exec_result = KatelloUtils.sshOnServer("ls -la /var/lib/candlepin/ | grep candlepin-crl.crl");
		Assert.assertTrue(getOutput(exec_result).contains("candlepin-crl.crl"));
		
//		yum_clean();
		exec_result = sshOnClient("yum -y install zebra --nogpgcheck");
		Assert.assertTrue(exec_result.getExitCode().intValue()==1, "Check - return code (install zebra)");
		
		exec_result = sys.subscribe(poolId1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		sys.rhsm_refresh();
		
//		yum_clean();
		exec_result = sshOnClient("yum -y install zebra --nogpgcheck");
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code (install zebra)");
	}
	
}
