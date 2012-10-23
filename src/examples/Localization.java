package examples;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.obj.KatelloActivationKey;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

public class Localization extends KatelloCliTestScript{
	
	private String organization;
	private String env;

	
	@BeforeClass(description="init: create org stuff")
	public void setUp(){
		SSHCommandResult res;
		String uid = KatelloUtils.getUniqueID();
		this.organization = "ak-"+uid;
		this.env = "ak-"+uid;

		KatelloOrg org = new KatelloOrg(this.organization, null);
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		KatelloEnvironment env = new KatelloEnvironment(this.env, null, this.organization, KatelloEnvironment.LIBRARY);
		res = env.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
	}
	
    @Test(description="Tests creating activation key by providing name got from i18n messages.properties.")
    public void test_createAKLocale() {
    	String uid = KatelloUtils.getUniqueID();
    	String akName = KatelloCliTestScript.getText("ak.name")+uid;
    	SSHCommandResult res;

    	KatelloActivationKey ak = new KatelloActivationKey(this.organization, this.env, akName, "Activation key to with localized name", null);
    	res = ak.create();
    	Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (activation_key create)");
    	
		    	
    }

}
