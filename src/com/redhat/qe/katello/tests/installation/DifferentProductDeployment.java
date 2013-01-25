package com.redhat.qe.katello.tests.installation;

import org.testng.annotations.Test;
import com.redhat.qe.Assert;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import com.redhat.qe.katello.base.KatelloCliTestScript;
 
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;
import com.redhat.qe.katello.common.KatelloConstants;
import com.redhat.qe.katello.base.obj.KatelloOrg;
 
 
@Test(groups={"cfse-cli","headpin-cli"})
public class DifferentProductDeployment extends KatelloCliTestScript
{
       
        private String deployment = KatelloConstants.KATELLO_PRODUCT;
        List<String> katello_list=new ArrayList<String>();
        private SSHCommandResult exec_result;
        private String tmp_answer_file="/tmp/answer-file";
        private String answer_file="/usr/share/katello/install/default-answer-file";
        private String org_name ="RedHat_QE";
        private String user_name ="qeadmin";
        private String user_pass ="admin";       
        private String engine = System.getProperty("katello.engine", "katello");
        @Test(description = "katello-configure - different product deployment")
        public void test_different_deploy()
        {       
               
                if (deployment.equals("sam") || deployment.equals("headpin"))
                {       
                        katello_list.add("cfse");
                        katello_list.add("katello");
                }
                else
                if (deployment.equals("cfse") || deployment.equals("katello"))
                {
                        katello_list.add("sam");
                        katello_list.add("headpin");
                }

                exec_result = KatelloUtils.sshOnServer("cp "+ answer_file +" "+ tmp_answer_file);
                                    Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code Copying the contents to tmp answer file");
                                    KatelloUtils.sshOnServer("sed -i -e \"s/org_name.*/org_name = "+ org_name +"/\" -e \"s/user_name.*/user_name = "+ user_name + "/\" -e \"s/\\buser_pass.*/user_pass = "+ user_pass + "/\" /tmp/answer-file");

                Iterator it=katello_list.iterator();
                while(it.hasNext())
                {
                        exec_result = KatelloUtils.sshOnServer("katello-configure --deployment="+ it.next().toString() +" --reset-cache=YES--reset-data=YES");               
                        Assert.assertTrue(exec_result.getExitCode() == 2, "Check - return code Configuration failed");     
                }
                exec_result = KatelloUtils.sshOnServer("katello-configure --deployment="+ deployment + " --answer-file=" + tmp_answer_file +" --reset-cache=YES --reset-data=YES");               
                Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code Configuration successful");                   
           
                exec_result = KatelloUtils.sshOnServer("katello-service status");
                Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code Check all the Services Running");
                exec_result = KatelloUtils.sshOnServer(engine +" -u "+ user_name + " -p "+ user_pass + " org list");
                Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code Displayed the Org");

                Assert.assertTrue(getOutput(exec_result).contains(KatelloOrg.getDefaultOrg()), "Check - contains default org");
                
        }       
} 