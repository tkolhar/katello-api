package com.redhat.qe.katello.tests.hammer.cli;

import java.util.ArrayList;
import java.util.List;
import javax.management.Attribute;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.HammerCli;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.HammerUser;
import com.redhat.qe.katello.common.KatelloUtils;

/**
 * Exams different [OPTIONS] of the hammer cli 
 * @author gkhachik
 * @since 16.Oct.2013
 */
public class HammerOptionTests extends KatelloCliTestBase{
	private String uid;
	
	@BeforeClass
	public void setUp(){
		this.uid = KatelloUtils.getUniqueID();
	}
	/**
	 * @github: https://github.com/gkhachik/katello-api/issues/589
	 * @tcms: https://tcms.engineering.redhat.com/case/306652/?from_plan=10784
	 */
	@Test(description="--username <NonExistingUser>")
	public void test_option_u_nonExistingUsername(){
		List<Attribute> options = new ArrayList<Attribute>();
		options.add(new Attribute("username", "neu-"+uid));
		HammerCli cli = new HammerCli("user list", options);
		exec_result = cli.run();
		Assert.assertTrue(exec_result.getExitCode().intValue()==129, "Check - exit code");
		Assert.assertTrue(getOutput(exec_result).equals(HammerUser.ERR_INVALID_USER_PASS), "Check - stderr");
	}
	
	
}
