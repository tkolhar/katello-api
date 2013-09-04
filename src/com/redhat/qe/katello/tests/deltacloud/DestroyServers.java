package com.redhat.qe.katello.tests.deltacloud;

import org.testng.annotations.Test;

import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.DeltaCloudInstance;
import com.redhat.qe.katello.deltacloud.DeltaCloudAPI;

/**
 * Destroys servers (katello.server.hostname; katello.client.hostname_ in RHEV-M.
 * @author gkhachik
 *
 */
public class DestroyServers extends KatelloCliTestBase{

	@Test
	public void test_destroy(){
		doDestroy(System.getProperty("katello.server.hostname"));
		doDestroy(System.getProperty("katello.client.hostname"));
	}
	
	private void doDestroy(String hostname){
		DeltaCloudInstance dc;
		if(hostname!=null && hostname.indexOf(".usersys.redhat.com")>0){
			hostname = hostname.substring(0,hostname.indexOf(".usersys.redhat.com"));
			dc = DeltaCloudAPI.getRhevmInstance(hostname);
			if(dc!=null && dc.getInstance()!=null)
				DeltaCloudAPI.destroyMachine(dc);
		}
	}
}
