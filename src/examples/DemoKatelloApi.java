package examples;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.grapher.GrapherModule;
import com.google.inject.grapher.InjectorGrapher;
import com.google.inject.grapher.graphviz.GraphvizModule;
import com.google.inject.grapher.graphviz.GraphvizRenderer;
import com.redhat.qe.katello.base.KatelloApiException;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.guice.CertSSLContext;
import com.redhat.qe.katello.guice.KatelloApiModule;
import com.redhat.qe.katello.ssl.KatelloPemThreadLocal;
import com.redhat.qe.katello.tasks.KatelloTasks;

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
            KatelloPemThreadLocal.set(consumer.getIdCert().getCert() + consumer.getIdCert().getKey()); 
            KatelloSystem _return = servertasksWithCert.updatePackages(consumer);
            log.info("Return cert is: " + _return.getIdCert().getCert());
            String subret = servertasksWithCert.subscribeConsumer(_return.getUuid());
            log.info(subret);
            KatelloPemThreadLocal.unset();
        } catch (KatelloApiException e) {
            e.printStackTrace();
        }
    }    
    
    private static void graph(String filename, Injector demoInjector) throws IOException {
        PrintWriter out = new PrintWriter(new File(filename), "UTF-8");
        
        Injector injector = Guice.createInjector(new GrapherModule(), new GraphvizModule());
        GraphvizRenderer renderer = injector.getInstance(GraphvizRenderer.class);
        renderer.setOut(out).setRankdir("TB");
        
        injector.getInstance(InjectorGrapher.class)
            .of(demoInjector)
            .graph();
    }
    
    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new KatelloApiModule());
        KatelloTasks tasks = injector.getInstance(Key.get(KatelloTasks.class, CertSSLContext.class));
        try {
            tasks.getOrganizations();
        } catch (KatelloApiException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
