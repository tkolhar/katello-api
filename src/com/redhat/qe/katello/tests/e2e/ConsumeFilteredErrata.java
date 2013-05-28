package com.redhat.qe.katello.tests.e2e;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.obj.KatelloActivationKey;
import com.redhat.qe.katello.base.obj.KatelloContentDefinition;
import com.redhat.qe.katello.base.obj.KatelloContentFilter;
import com.redhat.qe.katello.base.obj.KatelloContentView;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.base.obj.KatelloSystemGroup;
import com.redhat.qe.katello.base.obj.helpers.FilterRuleErrataDayType;
import com.redhat.qe.katello.base.obj.helpers.FilterRuleErrataIds;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.common.TngRunGroups;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups=TngRunGroups.TNG_KATELLO_Content)
public class ConsumeFilteredErrata extends KatelloCliTestScript {
	
	String uid = KatelloUtils.getUniqueID();
	String org_name = "orgcon-"+ uid;
	String env_name = "envcon-"+ uid;
	String prov_name = "provcon-" + uid;
	String prod_name = "prodcon-"+ uid;
	String repo_name = "repocon-" + uid;
	String condef_name = "condef-" + uid;
	String errata_filter = "errata filter";
	String pubview_name = "pubview-" + uid;
	String system_name1 = "system-" + uid;
	String act_key_name = "act_key-" + uid;
	String group_name = "group" + uid;
	String system_uuid1;
	
	SSHCommandResult exec_result;
	KatelloOrg org;
	KatelloEnvironment env;
	KatelloProvider prov;
	KatelloProduct prod;
	KatelloRepo repo;
	KatelloContentDefinition condef;
	KatelloContentView conview;
	KatelloActivationKey act_key;
	KatelloSystem sys;
	KatelloSystemGroup group;
	
	final String SHEEP_ERRATA = "RHEA-2012:2913";// 2012-09-17 bugfix
	final String KANGAROO_ERRATA = "RHEA-2012:3234";// 2012-11-15 bugfix
	final String ZEBRA_ERRATA = "RHEA-2012:3693";// 2012-08-27 security
	final String BIRD_ERRATA = "RHEA-2012:3954";// 2012-10-16 bugfix 
	final String RAT_ERRATA = "RHEA-2012:5674";// 2012-01-10 security
	final String FERRET_ERRATA = "RHEA-2012:5746";// 2012-08-17 enhancement
	final String CRAB_ERRATA = "RHEA-2012:6193";// 2012-12-01 bugfix
	final String RABBIT_ERRATA = "RHEA-2012:7655";// 2012-09-25 security
	final String EAGLE_ERRATA = "RHEA-2012:7809";// 2012-04-07 enhancement
	final String FISH_ERRATA = "RHEA-2012:8216";// 2012-10-20 security
	final String MONKEY_ERRATA = "RHEA-2012:8681";// 2012-04-04 security
	final String POLECAT_ERRATA = "RHEA-2012:9541";// 2012-12-21 enhancement
	final String FOX_ERRATA = "RHEA-2012:9645";// 2012-01-03 security
	final String SEAL_ERRATA = "RHEA-2012:9663";// 2012-12-12 security
	final String BAT_ERRATA = "RHEA-2012:9929";// 2012-05-01 bugfix
	
	@BeforeClass(description="Generate unique objects")
	public void setUp() {
		KatelloUtils.sshOnClient("yum erase -y kangaroo sheep zebra bird rat ferret crab rabbit eagle fish monkey polecat fox seal bat");
		
		org = new KatelloOrg(org_name,null);
		exec_result = org.cli_create();		              
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		env = new KatelloEnvironment(env_name,null,org_name,KatelloEnvironment.LIBRARY);
		exec_result = env.cli_create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		prov = new KatelloProvider(prov_name,org_name,null,null);
		exec_result = prov.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		prod = new KatelloProduct(prod_name,org_name,prov_name,null, null, null,null, null);
		exec_result = prod.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		repo = new KatelloRepo(repo_name,org_name,prod_name, REPO_HHOVSEPY_ZOO4, null, null);
		exec_result = repo.create(true);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		exec_result = repo.synchronize();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		condef = new KatelloContentDefinition(condef_name,null,org_name,null);
		exec_result = condef.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		exec_result = condef.add_product(prod_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = condef.add_repo(prod_name, repo_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		// The only way to install ERRATA on System is by system group
		group = new KatelloSystemGroup(group_name, this.org_name);
		exec_result = group.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		KatelloUtils.sshOnClient("sed -i -e \"s/certFrequency.*/certFrequency = 1/\" /etc/rhsm/rhsm.conf");
		KatelloUtils.sshOnClient("service rhsmcertd restart");
		yum_clean();
		KatelloUtils.sshOnClient("service goferd restart;");
	}
    
	//@ TODO BUG 955706
	@Test(description="Consume content from filtered errata")
	public void test_consumeErrataContent() {

		KatelloContentFilter filter = new KatelloContentFilter(errata_filter, org_name, condef_name);

		exec_result = filter.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		exec_result = filter.info();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = filter.add_repo(prod_name, repo_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloContentFilter.OUT_ADD_REPO, repo_name, errata_filter)), "Check output");
		
		
		exec_result = filter.add_rule(KatelloContentFilter.TYPE_INCLUDES, new FilterRuleErrataIds(KANGAROO_ERRATA, FISH_ERRATA));
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		exec_result = filter.add_rule(KatelloContentFilter.TYPE_INCLUDES, new FilterRuleErrataDayType("2012-09-01", "2012-09-31", null));// RABBIT_ERRATA, SHEEP_ERRATA
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");		

		exec_result = filter.add_rule(KatelloContentFilter.TYPE_INCLUDES, new FilterRuleErrataDayType("2012-08-01", "2012-08-30", new String[] {KatelloContentFilter.ERRATA_TYPE_ENHANCEMENT})  );// FERRET_ERRATA
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");


		exec_result = filter.add_rule(KatelloContentFilter.TYPE_EXCLUDES, new FilterRuleErrataIds(BAT_ERRATA, BIRD_ERRATA));
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = filter.add_rule(KatelloContentFilter.TYPE_EXCLUDES, new FilterRuleErrataDayType(null, "2012-01-30", new String[] {KatelloContentFilter.ERRATA_TYPE_SECURITY}) );//RAT_ERRATA, FOX_ERRATA
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");		

		exec_result = filter.add_rule(KatelloContentFilter.TYPE_EXCLUDES, new FilterRuleErrataDayType("2012-12-01", null, new String[] {KatelloContentFilter.ERRATA_TYPE_BUGFIX}) );// CRAB_ERRATA
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");		

		exec_result = filter.add_rule(KatelloContentFilter.TYPE_EXCLUDES, new FilterRuleErrataDayType("2012-04-01", "2012-05-01", null) );// MONKEY_ERRATA, EAGLE_ERRATA
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		condef.publish(pubview_name,pubview_name,null);

		conview = new KatelloContentView(pubview_name, org_name);
		exec_result = conview.promote_view(env_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloContentView.OUT_PROMOTE, this.pubview_name, env_name)), "Content view promote output.");
		// promoting content view is the same as create changeset, add content view and promote, it is completely legal
		
		act_key = new KatelloActivationKey(org_name,env_name,act_key_name,"Act key created");
		exec_result = act_key.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");      
		exec_result = act_key.update_add_content_view(pubview_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");      
		exec_result = new KatelloOrg(this.org_name,null).subscriptions();
		String zoo4PoolId = KatelloCli.grepCLIOutput("ID", getOutput(exec_result), 1);
		exec_result = act_key.update_add_subscription(zoo4PoolId);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = act_key.info();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");      
		Assert.assertTrue(getOutput(exec_result).contains(this.pubview_name), "Content view name is in output.");
		Assert.assertTrue(getOutput(exec_result).contains(zoo4PoolId), "Subscription is in output.");

		//register client, subscribe to pool
		rhsm_clean();
		sys = new KatelloSystem(system_name1, this.org_name, null);
		exec_result = sys.rhsm_registerForce(act_key_name);
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		
		sys.setEnvironmentName(this.env_name);
		exec_result = sys.list();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		system_uuid1 = KatelloCli.grepCLIOutput("UUID", getOutput(exec_result));
		
		exec_result = group.add_systems(system_uuid1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		yum_clean();			
		
		// consume older packages of erratas
		install_Package("kangaroo-3.6.6-1");
		install_Package("sheep-1.3.6-1");
		install_Package("zebra-10.0.7-1");
		install_Package("bird-5.1.10-1");
		install_Package("rat-7.7.0-1");
		install_Package("ferret-1.2.0-1");
		install_Package("crab-5.5.1-1");
		install_Package("rabbit-9.8.8-1");
		install_Package("eagle-9.8.9-1");
		install_Package("fish-4.10.2-1");
		install_Package("monkey-2.8.9-1");
		install_Package("polecat-9.4.6-1");
		install_Package("fox-10.8.0-1");
		install_Package("seal-3.10.0-1");
		install_Package("bat-3.10.6-1");
		
        StringBuffer installOK = new StringBuffer();
        installOK.append(KANGAROO_ERRATA);
        installOK.append(" ");
        installOK.append(FISH_ERRATA);
        installOK.append(" ");
        installOK.append(RABBIT_ERRATA);
        installOK.append(" ");
        installOK.append(SHEEP_ERRATA);
        installOK.append(" ");
        installOK.append(FERRET_ERRATA);
        
		exec_result = group.erratas_install(installOK.toString());
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).trim().contains("Remote action finished"));
		Assert.assertTrue(getOutput(exec_result).trim().contains("Erratum Install Complete"));
		
        StringBuffer installERROR = new StringBuffer();
        installERROR.append(BAT_ERRATA);
        installERROR.append(" ");
        installERROR.append(BIRD_ERRATA);
        installERROR.append(" ");
        installERROR.append(RAT_ERRATA);
        installERROR.append(" ");
        installERROR.append(FOX_ERRATA);
        installERROR.append(" ");
        installERROR.append(CRAB_ERRATA);
        installERROR.append(" ");
        installERROR.append(MONKEY_ERRATA);
        installERROR.append(" ");
        installERROR.append(EAGLE_ERRATA);
        
		exec_result = group.erratas_install(installERROR.toString());
		Assert.assertFalse(exec_result.getExitCode().intValue()==0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).trim().contains("Remote action failed"));
	}
	
	private void install_Package(String pkgName) {
		exec_result=KatelloUtils.sshOnClient("yum install -y "+ pkgName);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	}
	
	@AfterClass(description="cleanup RHSM stuff, yum erase packages")
	public void tearDown(){
		KatelloUtils.sshOnClient("yum erase -y kangaroo sheep zebra bird rat ferret crab rabbit eagle fish monkey polecat fox seal bat || true");
	}
}
