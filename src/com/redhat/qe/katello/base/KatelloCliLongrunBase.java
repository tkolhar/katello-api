package com.redhat.qe.katello.base;

import java.util.ArrayList;
import java.util.logging.Logger;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.common.KatelloConstants;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.tests.longrun.KatelloQASetup;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloCliLongrunBase extends KatelloCliTestBase implements KatelloConstants {

	protected static Logger log = Logger.getLogger(KatelloCliLongrunBase.class.getName());

	protected String base_org_name = null;
	
	protected boolean findSyncedRhelToUse(){
		ArrayList<String> orgs = new KatelloOrg(this.cli_worker,null,null).custom_listNames();
		ArrayList<String> products;
		SSHCommandResult res;
		for(String _org: orgs){
			if (_org.equals(KatelloQASetup.ORG_KATELLO_QA)) continue; 
			products = new KatelloProduct(this.cli_worker, null, _org, KatelloProvider.PROVIDER_REDHAT, null, null, null, null, null).custom_listNames();
			for(String product: products){
				if(product.equals(KatelloProduct.RHEL_SERVER)){
					res = new KatelloProduct(this.cli_worker, product, _org, KatelloProvider.PROVIDER_REDHAT, null, null, null, null, null).status();
					if(!KatelloUtils.grepCLIOutput("Last Sync", KatelloCliTestBase.sgetOutput(res)).equals("never")){
						// We found an org that has a synced RHEL_SERVER content. Let's re-use it.
						this.base_org_name = _org;
						return true;
					}
				}
			}
		}
		return false;
	}

	// ugly solution: KatelloCliTestScript has these calls already. take to rework once have time [gkhachik]
	protected String getOutput(SSHCommandResult res){
		return (res.getStdout()+"\n"+res.getStderr()).trim();
	}
}
