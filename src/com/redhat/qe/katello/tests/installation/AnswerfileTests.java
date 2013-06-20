package com.redhat.qe.katello.tests.installation;

import org.testng.annotations.Test;
import com.redhat.qe.Assert;

import com.redhat.qe.katello.base.KatelloCliTestBase;

import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;
import com.redhat.qe.katello.common.KatelloConstants;

@Test(groups={"headpin-cli"})
public class AnswerfileTests extends KatelloCliTestBase{
	
	private SSHCommandResult exec_result;

	private String tmp_answer_file="/tmp/answer-file";
	private String answer_file="/usr/share/katello/install/default-answer-file";
	private String org_name ="RedHat_QE";
	private String user_name ="qeadmin";
	private String user_pass ="admin"; 	  
	private String deployment = KatelloConstants.KATELLO_PRODUCT;
	private String engine = System.getProperty("katello.engine", "katello");

	@Test(description = "katello-configure with answer file")
	public void test_katello_configure_answer_file() {
	
		    
			exec_result = KatelloUtils.sshOnServer("cp "+ answer_file +" "+ tmp_answer_file);
			Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code Copying the contents to tmp answer file");
			KatelloUtils.sshOnServer("sed -i -e \"s/org_name.*/org_name = "+ org_name +"/\" -e \"s/user_name.*/user_name = "+ user_name + "/\" -e \"s/\\buser_pass.*/user_pass = "+ user_pass + "/\" /tmp/answer-file");
			exec_result = KatelloUtils.sshOnServer("katello-configure --deployment="+ deployment +" --answer-file="+ tmp_answer_file +" --reset-cache=YES --reset-data=YES");			
			Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code Configure Successful");
			exec_result = KatelloUtils.sshOnServer("katello-service status");
			Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code Check all the Services Running");
			exec_result = KatelloUtils.sshOnServer(engine +" -u "+ user_name + " -p "+ user_pass + " org list");
			Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code Displayed the Org");
	}  	  
}

