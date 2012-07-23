package com.redhat.qe.katello.tests.upgrade.v1;

import java.util.logging.Logger;
import org.testng.annotations.Test;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.common.KatelloConstants;
import com.redhat.qe.tools.SSHCommandResult;

public class ScenCustomRepo implements KatelloConstants{
	protected static Logger log = Logger.getLogger(ScenCustomRepo.class.getName());

	String _uid = KatelloTestScript.getUniqueID();
	String _org;
	String _provider;
	String _product;
	String _repo;
	
	@Test(description="init object unique names", 
			groups={TNG_PRE_UPGRADE})
	public void init(){
		_org = "upgV1-"+_uid;
		_provider = "Zoo "+_uid;
		_product = "Zoo "+_uid;
		_repo = "zoo3-"+_uid;
	}
	
	@Test(description="prepare and sync the repo", 
			dependsOnMethods={"init"}, 
			groups={TNG_PRE_UPGRADE})
	public void createAndSyncRepo(){
		KatelloOrg org = new KatelloOrg(_org, null);
		KatelloProvider provider = new KatelloProvider(_provider, _org, null, null);
		KatelloProduct product = new KatelloProduct(_product, _org, _provider, null, null, null, null, null);
		KatelloRepo repo = new KatelloRepo(_repo, _org, _product, REPO_INECAS_ZOO3, null, null);
		
		org.cli_create();
		provider.create();
		product.create();
		repo.create(); 
		repo.synchronize();
	}
	
	@Test(description="verify org survived the upgrade", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE})
	public void checkOrgSurvived(){
		// TODO - more checks to be go here.
		KatelloOrg org = new KatelloOrg(_org, null);
		SSHCommandResult res = org.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (org info)");
	}
}
