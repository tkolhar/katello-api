package com.redhat.qe.katello.tests.upgrade.v1;

import java.util.logging.Logger;

import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.base.obj.KatelloUser;
import com.redhat.qe.katello.common.KatelloConstants;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

/**
 * Implementation of test case: 
 * <a href='https://tcms.engineering.redhat.com/case/188126/?from_plan=6653'>
 * Upgrade: pre-populated CFSE system upgrade path
 * </a>
 * <BR>
 * 
 * @author gkhachik
 *
 */
public class PrepopulatedSystemUpgradePath implements KatelloConstants{
	protected static Logger log = Logger.getLogger(PrepopulatedSystemUpgradePath.class.getName());

	String _uid = KatelloUtils.getUniqueID();
	String[] _orgs = {"Tokyo_"+_uid, "SaoPaulo_"+_uid, "Paris_"+_uid, "Dakar_"+_uid};
	String[][] _envs = {
			{"Dev","QA","Release"}, //org[0]
			{"Desenvolvimento","ControleQualidade","Final"}, //org[1]
			{"Dev1","QA1","Release1"}, // org[2]
//			{"Dev2","QA2","Release2"}, // org[2]
			{"Dev","QA","GA"}}; // org[3]
	
	@Test(description="create organizations", 
			groups={TNG_PRE_UPGRADE}, enabled=false)
	public void create_orgs(){
		KatelloOrg tmpOrg; SSHCommandResult res;
		log.info("initialize organizations ...");

		for(String org: _orgs){
			tmpOrg = new KatelloOrg(null, org, null);
			res = tmpOrg.cli_create();
			Assert.assertTrue(res.getExitCode()==0, "Check - exit code (org create)");
		}
	}
	
	@Test(description="create environments for each org", 
			dependsOnMethods={"create_orgs"},
			groups={TNG_PRE_UPGRADE}, enabled=false)
	public void create_envs(){
		KatelloEnvironment tmpEnv; SSHCommandResult res;
		log.info("initialize environments ...");
		
		String prior = KatelloEnvironment.LIBRARY;
		for(int i=0;i<_orgs.length;i++){
			for(int j=0;j<3;j++){
				tmpEnv = new KatelloEnvironment(null, _envs[i][j], null, _orgs[i], prior);
				res = tmpEnv.cli_create();
				Assert.assertTrue(res.getExitCode()==0, "Check - exit code (environment create)");
				if(i==2){ // create the <envName>2 bucket
					tmpEnv = new KatelloEnvironment(null, _envs[i][j].replace('1', '2'), null, _orgs[i], prior.replace('1', '2'));
					res = tmpEnv.cli_create();
					Assert.assertTrue(res.getExitCode()==0, "Check - exit code (environment create)");
				}
				prior = _envs[i][j];
			}
			prior = KatelloEnvironment.LIBRARY;
		}		
	}
	
	@Test(description="create users", 
			dependsOnMethods={"create_orgs"},
			groups={TNG_PRE_UPGRADE}, enabled=false)
	public void create_users(){
		KatelloUser user; SSHCommandResult res;
		log.info("initialize users ...");
		
		// TODO - CFSE still not have default_* properties for `user create`
		user = new KatelloUser(null, "Akihito"+_uid, "Akihito@localhost", "redhat", false);
		res = user.cli_create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (user create)");
		user = new KatelloUser(null, "Dilma_Rousseff"+_uid, "Dilma_Rousseff@localhost", "redhat", false);
		res = user.cli_create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (user create)");
		user = new KatelloUser(null, "Ollanta_Humala"+_uid, "Ollanta_Humala@localhost", "redhat", false);
		res = user.cli_create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (user create)");
	}

	@Test(description="create custom repositories",
			dependsOnMethods={"create_envs"},
			groups={TNG_PRE_UPGRADE}, enabled=false)
	public void create_repos(){
		SSHCommandResult res;
		String[] repo_links ={
				"http://download.lab.bos.redhat.com/released/RHEL-6-JBEAP-6/6.0.0/Server/x86_64/",
				"http://inecas.fedorapeople.org/fakerepos/zoo3/",
				"http://download.lab.bos.redhat.com/released/RHEL-6-RHN-Tools/5.4.1/AS/x86_64/tree/RHNTools/"};
		
		for(int i=0;i<_orgs.length-1;i++){
			KatelloProvider prov = new KatelloProvider(null, "Provider "+_uid, _orgs[i], null, null);
			KatelloProduct prod = new KatelloProduct(null, "Product "+_uid, _orgs[i], prov.name, null, null, null, null, null);
			res = prov.create();
			Assert.assertTrue(res.getExitCode()==0, "Check - exit code (provider create)");
			res = prod.create();
			Assert.assertTrue(res.getExitCode()==0, "Check - exit code (product create)");
			KatelloRepo repo = new KatelloRepo(null, "Repo "+_uid, _orgs[i], "Product "+_uid, repo_links[i], null, null);		
			res = repo.create();
			Assert.assertTrue(res.getExitCode()==0, "Check - exit code (repo create)");
			res = repo.synchronize();
			Assert.assertTrue(res.getExitCode()==0, "Check - exit code (repo synchronize)");

			pushRepoFullCycle(this._orgs[i], _envs[i]);
			if(i==2){ // create the <envName>2 bucket
				String[] org2Envs2={"","",""};
				for(int k=0;k<3;k++)
					org2Envs2[k] = _envs[i][k].replace('1', '2');
				pushRepoFullCycle(this._orgs[i], org2Envs2);
			}
		}
		
	}
	
	/**
	 * promote the content from Library to the whole env set/
	 */
	private void pushRepoFullCycle(String org, String[] envs){
		log.info("promote repo to all environments for the org: ["+org+"]");
		KatelloUtils.promoteProductsToEnvironments(null, org, new String[] {"Product "+_uid}, envs);
	}
}
