package examples;

import java.text.ParseException;
import java.util.Date;

import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloApiException;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.tasks.KatelloTasks;

public class DemoKatelloApi extends KatelloTestScript {
//	@Test(description="demo KatelloApi.get")
//	public void test_apiGet(){
//		KatelloApi.get(KatelloOrg.API_CMD_LIST);
//	}
//	
//	@Test(description="demo KatelloApi.post - simple")
//	public void test_apiPost_simple(){
//		ArrayList<NameValuePair> opts = new ArrayList<NameValuePair>(); 
//		String rand = KatelloTestScript.getUniqueID();
//		opts.add(new BasicNameValuePair("name", "demoApi-"+rand));
//		opts.add(new BasicNameValuePair("description", "simple description - here it can be null as well"));
//		KatelloPostParam[] params = {new KatelloPostParam(null, opts)};
//		KatelloApi.post(params,KatelloOrg.API_CMD_CREATE); // "SSHCommandResult res = " could be used to extract/assert details from the result
//	}
//
//	@Test(description="demo KatelloApi.post - more complex")
//	public void test_apiPost_complex(){
//		ArrayList<NameValuePair> opts = new ArrayList<NameValuePair>(); 
//		String rand = KatelloTestScript.getUniqueID();
//
//		opts.add(new BasicNameValuePair("name", "demoApi-env-"+rand));
//		opts.add(new BasicNameValuePair("description", null));
//		opts.add(new BasicNameValuePair("prior", "1")); // otherwise you need to use KatelloEnvironment.get_prior_id() for that KatelloEnvironment.LIBRARY env.
//		KatelloPostParam[] params = {new KatelloPostParam("environment", opts)};
//		KatelloApi.post(params, String.format(KatelloEnvironment.API_CMD_CREATE, "ACME_Corporation"));
//	}

    @Test(description="demo new RestEasy API")
	public void test_resteasy_api() {
	    KatelloTasks servertasks = new KatelloTasks();
//	    servertasks.getProvider(org_name, byName);
//	    servertasks.uploadManifest(providerId, exportZipPath);
    }
}
