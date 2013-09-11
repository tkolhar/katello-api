package com.redhat.qe.katello.tests.disconnected;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloDisconnected;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

public class DisconnectedTests {
	private KatelloDisconnected disc;
	
	@BeforeClass(description="setup stuff")
	public void setUp(){
		disc = new KatelloDisconnected();		
	}
	
	/**
	 * setup --oauth-secret <grep from /etc/pulp/server.conf>
	 */
	@Test(description="bb9dbe17-222d-4b4a-b779-ec2f968c4ac4")
	public void test_setupMinimal(){
		SSHCommandResult res = KatelloUtils.sshOnServer("egrep \"^oauth_secret: \" /etc/pulp/server.conf | cut -f2 -d' '");
		String oauth_secret = KatelloCliTestBase.sgetOutput(res);
		res = disc.setup(oauth_secret);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
	}
}
