package examples;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.common.KatelloConstants;
import com.redhat.qe.tools.SSHCommandResult;

/**
 * Simple demo of repo synchronization using all the stuff via Katello CLI.
 * @author gkhachik
 *
 */
public class CliSyncRepo implements KatelloConstants{
	String orgname;
	String providername;
	String productname;
	String reponame;
	
	@BeforeClass(description="prepare an org to work with")
	public void setup(){
		SSHCommandResult res;
		this.orgname = "CFSE QE Org";
		this.providername = "provider Zoo";
		this.productname = "Zoo Corp";
		this.reponame = "zoo3";
		
		// Create org
		KatelloOrg org = new KatelloOrg(orgname, "Demo org");
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode()==0, "Check - return code (org create)");
		
		// Add envs: Development -> Test -> Production
		KatelloEnvironment env;
		env = new KatelloEnvironment("Development", null, this.orgname, KatelloEnvironment.LIBRARY);env.cli_create();
		env = new KatelloEnvironment("Testing", null, this.orgname, "Development"); env.cli_create();
		env = new KatelloEnvironment("Production", null, this.orgname, "Testing"); env.cli_create();
	}
	
	@Test
	public void test_generate_provProdRepoAndSync(){
		SSHCommandResult res;
		KatelloProvider prov = new KatelloProvider(this.providername, this.orgname, null, null);
		KatelloProduct prod = new KatelloProduct(this.productname, this.orgname, this.providername, null, null, null, null, null);
		KatelloRepo repo = new KatelloRepo(this.reponame, this.orgname, this.productname, REPO_INECAS_ZOO3, null, null);
		prov.create();
		prod.create();
		repo.create();
		res = repo.synchronize(); // here we go
		Assert.assertTrue(res.getExitCode()==0, "Check - return code (repo sync)");
		// one might wish to add more asserts with extracting: res.getStdout()
	}
	
	@AfterClass(description="destroy the org - make scenario reusable", alwaysRun=true)
	public void cleanup(){
		KatelloOrg org = new KatelloOrg(orgname, null);
		SSHCommandResult res = org.delete();
		Assert.assertTrue(res.getExitCode()==0, "Check - return code (org delete)");
	}
}
