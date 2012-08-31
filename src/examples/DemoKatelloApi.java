package examples;

import org.testng.annotations.Test;

import com.redhat.qe.katello.base.KatelloApiException;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.tasks.KatelloTasks;

public class DemoKatelloApi extends KatelloTestScript {
    @Test(description="demo new RestEasy API")
	public void test_resteasy_api() {
        KatelloTasks tasks = new KatelloTasks();
        String hostname = "host" + KatelloTestScript.getUniqueID() + ".example.com";
        String organizationName = "org" + KatelloTestScript.getUniqueID();
        String environmentName = "env" + KatelloTestScript.getUniqueID();
        String uuid = KatelloTestScript.getUUID();
        KatelloSystem consumer;
        try {            
            KatelloOrg org = tasks.createOrganization(organizationName, "Org Description - " + organizationName);
            tasks.createEnvironment(org.getCpKey(), environmentName, "Env Description - " + environmentName, KatelloEnvironment.LIBRARY);
            consumer = tasks.createConsumer(org.getCpKey(), hostname, uuid);
            KatelloSystem _return = tasks.updatePackages(consumer);
            log.info("Return string is: " + _return);
        } catch (KatelloApiException e) {
            e.printStackTrace();
        }
    }
}
