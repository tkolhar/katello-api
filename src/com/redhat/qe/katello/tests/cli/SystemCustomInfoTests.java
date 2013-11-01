package com.redhat.qe.katello.tests.cli;

import java.util.logging.Logger;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliDataProvider;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloContentDefinition;
import com.redhat.qe.katello.base.obj.KatelloContentView;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.base.tngext.TngPriority;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.common.TngRunGroups;

@TngPriority(400)
@Test(groups={TngRunGroups.TNG_KATELLO_System_Consumer})
public class SystemCustomInfoTests extends KatelloCliTestBase{	
	protected static Logger log = Logger.getLogger(SystemCustomInfoTests.class.getName());

	private String orgNameMain;
	private String systemNameCustomInfo;
	private String envName_Dev;
	private String contentName;
	private String contentView;
	
	@BeforeClass(description="Generate unique names",groups={"headpin-cli"})
	public void setUp(){
		String uid = KatelloUtils.getUniqueID();
		this.orgNameMain = "org-sys-"+uid;
		this.systemNameCustomInfo = "sys-CustomInfo-"+uid;
		
		exec_result = new KatelloOrg(this.cli_worker, this.orgNameMain, null).cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");

		this.envName_Dev = null;
	}
	
	@BeforeClass(description="init: katello specific, no headpin", dependsOnMethods={"setUp"})
	public void setUp_katelloOnly(){
		String uid = KatelloUtils.getUniqueID();
		this.envName_Dev = "Dev-"+uid;
		this.contentName = "content-" + uid;
		this.contentView = "contentView-"+uid;
		
		exec_result = new KatelloEnvironment(this.cli_worker, envName_Dev, null, orgNameMain, KatelloEnvironment.LIBRARY).cli_create();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code");
		//Associate content view to the environments
		KatelloContentDefinition contentMain = new KatelloContentDefinition(this.cli_worker, contentName, "descritpion", orgNameMain, contentName);
		exec_result = contentMain.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = contentMain.publish(this.contentView, this.contentView, "Content View for orgNameMain");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		KatelloContentView contentView = new KatelloContentView(this.cli_worker, this.contentView, orgNameMain);
		exec_result = contentView.promote_view(envName_Dev);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	}

	/** TCMS scenario is: <a href="https://tcms.engineering.redhat.com/case/243212/?from_plan=7771">here</a> */
	@Test(description="2f43f492-8e03-4424-8eb8-b9cd2745cf94", 
			groups={"headpin-cli"})
	public void test_system_customInfo_add(){
		rhsm_clean();
		
		KatelloSystem sys = new KatelloSystem(this.cli_worker, this.systemNameCustomInfo, this.orgNameMain, this.envName_Dev);
		exec_result = sys.rhsm_register();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		exec_result = sys.add_custom_info("custom-key","custom-value");
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(
				String.format(KatelloSystem.OUT_ADD_CUSTOM_INFO, "custom-key", "custom-value", sys.name)),
				"Check - returned output string");
		exec_result = sys.info();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		String customInfoStr = KatelloUtils.grepCLIOutput("Custom Info", getOutput(exec_result));
		Assert.assertTrue(customInfoStr.contains("custom-key"), "Check - stdout contains [custom-key]");
		Assert.assertTrue(customInfoStr.contains("custom-value"), "Check - stdout contains [custom-value]");
	}
	
	
	/** TCMS scenario is: <a href="https://tcms.engineering.redhat.com/case/243183/?from_plan=7771">here</a> */
	@Test(description = "cf618455-2f04-4994-844f-e3da4369d900",groups={"headpin-cli"},
			dependsOnMethods={"test_system_customInfo_add"})
	public void test_system_customInfo_update(){
		
		KatelloSystem sys = new KatelloSystem(this.cli_worker, this.systemNameCustomInfo, this.orgNameMain, this.envName_Dev);
		exec_result = sys.update_custom_info("custom-key", "updated-value");
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(
				String.format("Successfully updated Custom Information [ %s ] for System [ %s ]","custom-key",sys.name)),
				"Check - return string");
		exec_result = sys.info();
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		String customInfoStr = KatelloUtils.grepCLIOutput("Custom Info", getOutput(exec_result));
		Assert.assertTrue(customInfoStr.contains("custom-key"), "Check - stdout contains [custom-key]");
		Assert.assertTrue(customInfoStr.contains("updated-value"), "Check - stdout contains [updated-value]");
	}
	
	@Test(description="65037d2b-a924-4f36-8e72-25056d80d097",groups={"headpin-cli"},
			dependsOnMethods={"test_system_customInfo_update"})
	public void test_system_customInfo_remove(){
		KatelloSystem sys = new KatelloSystem(this.cli_worker, this.systemNameCustomInfo, this.orgNameMain, this.envName_Dev);
		exec_result = sys.remove_custom_info("custom-key");
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(
				String.format("Successfully removed Custom Information from System [ %s ]",sys.name)),
				"Check - return string");
		String customInfoStr = KatelloUtils.grepCLIOutput("Custom Info", getOutput(exec_result));
		Assert.assertNull(customInfoStr, "Check - output can not be extracted");
	}
	
	@Test(description = "Add Custom Info - Create custom information for a system - different inputs",
			dataProviderClass = KatelloCliDataProvider.class,dataProvider = "add_custom_info",
			groups={"headpin-cli"}, dependsOnMethods={"test_system_customInfo_remove"})
	public void test_system_customInfo_addVariations(String keyname,String value,Integer exitCode,String output){
		
		KatelloSystem sys = new KatelloSystem(this.cli_worker, this.systemNameCustomInfo, this.orgNameMain, this.envName_Dev);
		exec_result= sys.add_custom_info(keyname,value);
		Assert.assertTrue(exec_result.getExitCode().intValue() == exitCode.intValue(), "Check - return code");
		if(exec_result.getExitCode().intValue() == 0 ){ //
			Assert.assertTrue(getOutput(exec_result).contains(
					String.format("Successfully added Custom Information [ %s : %s ] to System [ %s ]",keyname,value,sys.name)),
					"Check - returned output string");
		}else{ // Failure to be checked
			Assert.assertTrue(getOutput(exec_result).contains(output),"Check - returned error string");
		}
	}	
}
