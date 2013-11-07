package com.redhat.qe.katello.tests.longrun;

import java.util.logging.Logger;

import org.testng.SkipException;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliLongrunBase;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.common.KatelloUtils;

public class KatelloQASetup extends KatelloCliLongrunBase{

	public static final String ORG_KATELLO_QA = "Katello QA Org";
	public static final int RHEL_SYNC_TRIES = 3;
	
	Logger log = Logger.getLogger(KatelloQASetup.class.getName());
	
	@BeforeSuite(description="Setup and prepare Katello QA Org")
	public void setupKatelloQAOrg(){

		// check if org exists.
		KatelloOrg orgQA = new KatelloOrg(cli_worker, ORG_KATELLO_QA, 
				"Katello QA organization. Has RHEL content synced.");
		if(orgQA.cli_info().getExitCode().intValue()!=0){
			exec_result = orgQA.cli_create();
			Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - exit code");
		}else{
			String err = String.format("Org \"%s\" already exists. Please --reset-data=YES and try again.",ORG_KATELLO_QA);
			log.severe(err); throw new SkipException(err);
		}

		// import manifest
		KatelloProvider provRedhat = new KatelloProvider(cli_worker, KatelloProvider.PROVIDER_REDHAT, ORG_KATELLO_QA, null, null);
		KatelloUtils.scpOnClient(null, "data/"+KatelloProvider.MANIFEST_KATELLO_QA_ORG, "/tmp");
		exec_result = provRedhat.import_manifest("/tmp/"+KatelloProvider.MANIFEST_KATELLO_QA_ORG, true);
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - exit code");

		// enable Red Hat Enterprise Linux 6 Server (RPMs)
		KatelloProduct prodRhel = new KatelloProduct(cli_worker, KatelloProduct.RHEL_SERVER, ORG_KATELLO_QA, KatelloProvider.PROVIDER_REDHAT, null, null, null,null, null);
		exec_result = prodRhel.repository_set_enable(KatelloProduct.REPOSET_RHEL6_RPMS);
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - exit code");
		KatelloRepo repo = new KatelloRepo(this.cli_worker, KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT, ORG_KATELLO_QA, KatelloProduct.RHEL_SERVER, null, null, null);
		exec_result = repo.enable();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - exit code");

		// sync the repo - 3 tries in case it would fail in syncing.
		exec_result = repo.synchronize(); int tried = 0;
		while(exec_result.getExitCode().intValue()!=0 && tried<RHEL_SYNC_TRIES){
			tried++;
			log.warning(String.format("Fail syncing after [%d] tries",tried));
			exec_result = repo.synchronize();
		}
		if(exec_result.getExitCode().intValue()==0)
			log.info(String.format("Repo \"%s\" synced after [%d] tries",repo.name,(tried+1)));
		else{
			String err = String.format("Failed to sync\"%s\" after [%d] tries",repo.name,RHEL_SYNC_TRIES);
			log.severe(err); throw new SkipException(err);
		}
	}
	
	@Test
	public void test_debug(){
		
	}
}
