package com.redhat.qe.katello.tests.e2e;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloActivationKey;
import com.redhat.qe.katello.base.obj.KatelloContentDefinition;
import com.redhat.qe.katello.base.obj.KatelloContentFilter;
import com.redhat.qe.katello.base.obj.KatelloContentView;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.base.obj.KatelloSystemGroup;
import com.redhat.qe.katello.base.obj.helpers.FilterRuleErrataDayType;
import com.redhat.qe.katello.base.obj.helpers.FilterRuleErrataIds;
import com.redhat.qe.katello.base.obj.helpers.FilterRulePackage;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.common.TngRunGroups;

@Test(groups=TngRunGroups.TNG_KATELLO_Content, singleThreaded = true)
public class ConsumeFilteredErrata extends KatelloCliTestBase {
	
	String uid = KatelloUtils.getUniqueID();
	String condef_name = "condef-" + uid;
	String errata_filter = "errata filter";
	String pubview_name = "pubview-" + uid;
	String system_name1 = "system-" + uid;
	String act_key_name = "act_key-" + uid;
	String group_name = "group" + uid;
	String system_uuid1;

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
		sshOnClient("yum erase -y kangaroo sheep zebra bird rat ferret crab rabbit eagle fish monkey polecat fox seal bat --disablerepo \\* || true");
		
		condef = new KatelloContentDefinition(cli_worker, condef_name,null,base_org_name,null);
		exec_result = condef.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		exec_result = condef.add_product(base_zoo4_product_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = condef.add_repo(base_zoo4_product_name, base_zoo4_repo_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		// The only way to install ERRATA on System is by system group
		group = new KatelloSystemGroup(this.cli_worker, group_name, base_org_name);
		exec_result = group.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		sshOnClient("sed -i -e \"s/certFrequency.*/certFrequency = 1/\" /etc/rhsm/rhsm.conf");
		sshOnClient("service rhsmcertd restart");
		sshOnClient("service goferd restart;");
	}
    
	@Test(description="Consume content from filtered errata")
	public void test_consumeErrataContent() {

		KatelloContentFilter filter = new KatelloContentFilter(cli_worker, errata_filter, base_org_name, condef_name);

		exec_result = filter.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		exec_result = filter.info();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = filter.add_repo(base_zoo4_product_name, base_zoo4_repo_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloContentFilter.OUT_ADD_REPO, base_zoo4_repo_name, errata_filter)), "Check output");
		
		// add package rules there     
		FilterRulePackage [] include_packages = {
				new FilterRulePackage("kangaroo", "3.6.6-1", null, null),
				new FilterRulePackage("sheep", "1.3.6-1", null, null),
				new FilterRulePackage("zebra", "10.0.7-1", null, null),
				new FilterRulePackage("bird", "5.1.10-1", null, null),
				new FilterRulePackage("rat", "7.7.0-1", null, null),
				new FilterRulePackage("ferret", "1.2.0-1", null, null),
				new FilterRulePackage("crab", "5.5.1-1", null, null),
				new FilterRulePackage("fish", "4.10.2-1", null, null),
				new FilterRulePackage("monkey", "2.8.9-1", null, null),
				new FilterRulePackage("polecat", "9.4.6-1", null, null),
				new FilterRulePackage("fox", "10.8.0-1", null, null),
				new FilterRulePackage("seal", "3.10.0-1", null, null),
				new FilterRulePackage("bat", "3.10.6-1", null, null)
		};

		exec_result = filter.add_rule(KatelloContentFilter.TYPE_INCLUDES, include_packages);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
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
		
		exec_result = condef.publish(pubview_name,pubview_name,null);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		conview = new KatelloContentView(cli_worker, pubview_name, base_org_name);
		exec_result = conview.promote_view(base_test_env_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloContentView.OUT_PROMOTE, this.pubview_name, base_test_env_name)), "Content view promote output.");
		// promoting content view is the same as create changeset, add content view and promote, it is completely legal
		
		act_key = new KatelloActivationKey(this.cli_worker, base_org_name, base_test_env_name, act_key_name,"Act key created", null, pubview_name);
		exec_result = act_key.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");           

		exec_result = act_key.update_add_subscription(base_zoo4_repo_pool);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = act_key.info();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");      
		Assert.assertTrue(getOutput(exec_result).contains(this.pubview_name), "Content view name is in output.");
		Assert.assertTrue(getOutput(exec_result).contains(base_zoo4_repo_pool), "Subscription is in output.");

		//register client, subscribe to pool
		rhsm_clean();
		sys = new KatelloSystem(this.cli_worker, system_name1, base_org_name, null);
		exec_result = sys.rhsm_registerForce(act_key_name);
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		
		sys.setEnvironmentName(base_test_env_name);
		exec_result = sys.list();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		system_uuid1 = KatelloUtils.grepCLIOutput("UUID", getOutput(exec_result));
		
		exec_result = group.add_systems(system_uuid1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		KatelloUtils.sshOnClient(cli_worker.getClientHostname(), "subscription-manager refresh; service rhsmcertd restart");
		yum_clean();			
		
		// consume older packages of erratas
		install_Packages(cli_worker.getClientHostname(),new String[] {"kangaroo-3.6.6-1", "sheep-1.3.6-1", "zebra-10.0.7-1", "bird-5.1.10-1", "rat-7.7.0-1", "ferret-1.2.0-1", "crab-5.5.1-1", 
				"rabbit-9.8.8-1", "eagle-9.8.9-1", "fish-4.10.2-1", "monkey-2.8.9-1", "polecat-9.4.6-1", "fox-10.8.0-1", "seal-3.10.0-1", "bat-3.10.6-1"});
		
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
		
		verify_PackagesNotAvailable(cli_worker.getClientHostname(), new String[] {"bat-3.10.7-1", "bird-5.1.11-1", "rat-7.7.2-1", "fox-10.8.2-1", "crab-5.5.4-1", "monkey-2.8.10-1", "eagle-9.8.10-1"});
	}
}
