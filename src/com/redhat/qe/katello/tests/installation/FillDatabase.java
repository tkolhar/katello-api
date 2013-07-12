package com.redhat.qe.katello.tests.installation;

import java.util.logging.Logger;

import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloContentDefinition;
import com.redhat.qe.katello.base.obj.KatelloContentFilter;
import com.redhat.qe.katello.base.obj.KatelloContentView;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.base.obj.KatelloUser;
import com.redhat.qe.katello.base.obj.KatelloUserRole;
import com.redhat.qe.katello.base.obj.helpers.FilterRulePackage;
import com.redhat.qe.katello.common.KatelloConstants;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups = { "cfse-cli", "headpin-cli" })
public class FillDatabase extends KatelloCliTestBase implements KatelloConstants{
	protected static Logger log = Logger.getLogger(FillDatabase.class.getName());

	String _uid = KatelloUtils.getUniqueID();
	String _org;
	String _provider;
	String _product;
	String _repo;
	String _env;
	String _system;
	String _user1;
	String _role1;
	String _view;
	String _definition;
	String _filter;
	
	@Test(description="init object unique names", 
			groups={TNG_PRE_BACKUP})
	public void init(){
		_org = "backup"+_uid;
		_provider = "Zoo "+_uid;
		_product = "Zoo "+_uid;
		_repo = "zoo3-"+_uid;
		_env = "env-" + _uid;
		_system = "localhost-" + _uid;
		_user1 = "user" + _uid;
		_role1 = "role" + _uid;
		_definition = "definition" + _uid;
		_view = "view" + _uid;
		_filter = "filter" + _uid;
	}
	
	@Test(description="prepare and sync the repo", 
			dependsOnMethods={"init"}, 
			groups={TNG_PRE_BACKUP})
	public void createAndSyncRepo(){
			
		KatelloOrg org = new KatelloOrg(cli_worker, _org, null);
		
		KatelloProvider provider = new KatelloProvider(cli_worker, _provider, _org, null, null);
		KatelloProduct product = new KatelloProduct(cli_worker, _product, _org, _provider, null, null, null, null, null);
		KatelloRepo repo = new KatelloRepo(cli_worker, _repo, _org, _product, REPO_INECAS_ZOO3, null, null);
		
		KatelloEnvironment env = new KatelloEnvironment(cli_worker, _env, null, _org, KatelloEnvironment.LIBRARY);
		
		KatelloUser user1 = new KatelloUser(cli_worker, _user1, _user1+"@redhat.com", "redhat", false);
		KatelloUserRole role1 = new KatelloUserRole(cli_worker, _role1, null);
		
		
		SSHCommandResult res = user1.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");		
		res = role1.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		res = user1.assign_role(role1.name);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		res = provider.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		res = product.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		res = env.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		res = repo.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		repo.synchronize();
		
		KatelloContentDefinition condef = new KatelloContentDefinition(cli_worker, _definition, null, _org, null);
		SSHCommandResult exec_result = condef.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = condef.add_repo(_product, _repo);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		KatelloContentFilter filter = new KatelloContentFilter(cli_worker, _filter, _org, condef.name);
		exec_result = filter.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = filter.add_repo(_product, _repo);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		FilterRulePackage packages[] = new FilterRulePackage[2];
		packages[0] = new FilterRulePackage("lion");
		packages[1] = new FilterRulePackage("zebra");
		
		exec_result = filter.add_rule(KatelloContentFilter.TYPE_INCLUDES, packages);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	
		
		exec_result = condef.publish(_view, _view,"Publish Content");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");		

		KatelloContentView view = new KatelloContentView(cli_worker, _view, _org);
		exec_result = view.promote_view(_env);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	}
	
	@Test(description="verify org survived the recovery", 
			dependsOnGroups={TNG_PRE_BACKUP, TNG_BACKUP}, 
			groups={TNG_POST_RECOVERY})
	public void checkOrgSurvived(){
		KatelloOrg org = new KatelloOrg(cli_worker, _org, null);
		SSHCommandResult res = org.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (org info)");
		
		KatelloUserRole role1 = new KatelloUserRole(cli_worker, _role1, null);
		res = role1.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (role info)");
		
		KatelloUser user = new KatelloUser(cli_worker, _user1, null, null, false);
		res = user.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (user info)");
		
		KatelloContentDefinition condef = new KatelloContentDefinition(cli_worker, _definition, null, _org, null);
		res = condef.info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (user info)");
		
		KatelloContentFilter filter = new KatelloContentFilter(cli_worker, _filter, _org, condef.name);
		res = filter.info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (user info)");
		
		KatelloContentView view = new KatelloContentView(cli_worker, _view, _org);
		res = view.view_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (user info)");
	}

	@Test(description="verify repo survived the recovery", 
			dependsOnGroups={TNG_PRE_BACKUP, TNG_BACKUP}, 
			groups={TNG_POST_RECOVERY})
	public void checkRepoSurvived(){
		KatelloRepo repo = new KatelloRepo(cli_worker, _repo, _org, _product, REPO_INECAS_ZOO3, null, null);
		SSHCommandResult res = repo.info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (org info)");
	}
	
}
