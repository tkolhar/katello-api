package com.redhat.qe.katello.tests.deltacloud.setup;

import java.util.ArrayList;
import java.util.StringTokenizer;
import org.testng.annotations.Test;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.DeltaCloudInstance;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

public class PrepareDCEnvironment extends KatelloCliTestBase {

	protected SSHCommandResult res;
	protected static DeltaCloudInstance server;
	protected static ArrayList<DeltaCloudInstance> clients;

	@Test(description="Create server, clients")
	public void test_createServerAndClients(){
		if (server != null) return;
		
		server = KatelloUtils.getDeltaCloudServer();
		String server_name = server.getHostName();
		
		ArrayList<String> clientImages; 
		StringTokenizer tok = new StringTokenizer(System.getProperty("deltacloud.client.imageid"),",");
		clientImages = new ArrayList<String>();
		clients = new ArrayList<DeltaCloudInstance>();
		while(tok.hasMoreTokens()){
			String token = tok.nextToken();
			clientImages.add(token);
			clients.add(KatelloUtils.getDeltaCloudClient(server_name,token));
		}
	}
	
	@Test(description="Write out machine info", dependsOnMethods={"test_createServerAndClients"})
	public void test_dumpServersInfo(){
		String filename = System.getProperty("user.dir")+"/"+System.getProperty("jenkins.build.id","null")+".properties";		
		String sout = "KATELLO_SERVER_HOSTNAME="+server.getHostName()+"\\\\nKATELLO_CLIENTS=";
		for(int i=0;i<clients.size();i++){
			sout += ","+clients.get(i).getHostName();
		}
		KatelloUtils.run_local("echo -en \""+sout+"\" > "+filename);
	}
	
}
