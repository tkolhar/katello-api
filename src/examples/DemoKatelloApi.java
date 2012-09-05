package examples;

import org.testng.annotations.Test;

import com.redhat.qe.katello.base.KatelloApiException;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.common.KatelloUtils;

public class DemoKatelloApi extends KatelloTestScript {
    @Test(description="demo new RestEasy API")
	public void test_resteasy_api() {
        String hostname = "host" + KatelloUtils.getUniqueID() + ".example.com";
        String organizationName = "org" + KatelloUtils.getUniqueID();
        String environmentName = "env" + KatelloUtils.getUniqueID();
        String uuid = KatelloUtils.getUUID();
        KatelloSystem consumer;
        try {            
            KatelloOrg org = servertasks.createOrganization(organizationName, "Org Description - " + organizationName);
            servertasks.createEnvironment(org.getCpKey(), environmentName, "Env Description - " + environmentName, KatelloEnvironment.LIBRARY);
            consumer = servertasks.createConsumer(org.getCpKey(), hostname, uuid);
            KatelloSystem _return = servertasks.updatePackages(consumer);
            log.info("Return string is: " + _return);
        } catch (KatelloApiException e) {
            e.printStackTrace();
        }
    }
}
