package com.redhat.qe.katello.deltacloud;

import java.util.UUID;
import java.util.logging.Logger;
import org.ovirt.engine.sdk.Api;
import org.ovirt.engine.sdk.entities.Action;
import org.ovirt.engine.sdk.entities.VM;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.obj.DeltaCloudInstance;
import com.redhat.qe.katello.common.KatelloConstants;

public class DeltaCloudAPI implements KatelloConstants {
	protected static Logger log = Logger.getLogger(DeltaCloudAPI.class.getName());
	
	/**
	 * @IMPORTANT DO NOT CALL THESE METRODS DIRECTLY, USE KatelloUtils TO CREATE DeltaCloud machines.
	 */
	public static DeltaCloudInstance provideServer(boolean nowait, String hostname) {
		String image = System.getProperty("deltacloud.server.imageid", "7657667a-4108-4484-88d2-c103467a20b3");
		return provideMachine(hostname, new Long(4096), image);
	}

	public static DeltaCloudInstance provideClient(String hostname) {
		String image = System.getProperty("deltacloud.client.imageid", "fc06e21b-8973-48e2-9d64-3b5a90f2717e");
		return provideMachine(hostname, null, image);
	}
	public static DeltaCloudInstance provideClient(String hostname, String imageId) {
		return provideMachine(hostname, null, imageId);
	}
	
	private static DeltaCloudInstance provideMachine(String hostname, Long memory, String image){		
		DeltaCloudInstance machine = new DeltaCloudInstance();
		Api api = null;
		
		hostname += ".usersys.redhat.com"; // TODO don't hardcode
		
		try {
			String realm = System.getProperty("deltacloud.realm");
			if (realm != null && realm.trim().isEmpty()) realm = null;
			Assert.assertNotNull(System.getProperty("deltacloud.hostname"), "Deltacloud hostname shoud be provided in system property \"deltacloud.hostname\"");
			Assert.assertNotNull(System.getProperty("deltacloud.user"), "Deltacloud username shoud be provided in system property \"deltacloud.user\"");
			Assert.assertNotNull(System.getProperty("deltacloud.password"), "Deltacloud password shoud be provided in system property \"deltacloud.password\"");
			Assert.assertNotNull(System.getProperty("deltacloud.cluster"), "Deltacloud cluster shoud be provided in system property \"deltacloud.cluster\"");

			api = new Api(System.getProperty("deltacloud.hostname"), 
					System.getProperty("deltacloud.user"), 
					System.getProperty("deltacloud.password"),
					true); //ignore SSL 

			VM vmParams = new VM();
			vmParams.setName(hostname.substring(0,hostname.indexOf(".")));
			vmParams.setCluster(api.getClusters().get(System.getProperty("deltacloud.cluster")));
			vmParams.setTemplate(api.getTemplates().get(UUID.fromString(image)));
			if(memory!=null)
				vmParams.setMemory(new Long(memory.longValue()*1024*1024)); // in Mb
//			vmParams.setDescription(value) // One day we may find this useful.
			org.ovirt.engine.sdk.decorators.VM myVM = api.getVMs().add(vmParams);
			
			// wait until gets 'DOWN' (first step: image_locked, then down)
			int waitForDiskPrepared = RHEVM_MAX_WAIT;
			String machineState = api.getVMs().get(myVM.getName()).getStatus().getState();
			while(waitForDiskPrepared>0 && !machineState.equals(RhevStates.down.toString())){
				try{Thread.sleep(1000);}catch(Exception ex){}
				waitForDiskPrepared--;
				machineState = api.getVMs().get(myVM.getName()).getStatus().getState();
			}
			log.finest(String.format("RHEV-M guest bring down (disk created) after: [%s] rounds.",(RHEVM_MAX_WAIT - waitForDiskPrepared)));

			try{Thread.sleep(3000);}catch(InterruptedException ex){}
			
			// Info prepared. Let's START
			myVM.start(new Action() {
				{
					setVm(new org.ovirt.engine.sdk.entities.VM());
				}
			});
			// wait until gets 'UP'
			int waitForUp = RHEVM_MAX_WAIT;
			machineState = api.getVMs().get(myVM.getName()).getStatus().getState();
			while(waitForUp>0 && !machineState.equals(RhevStates.up.toString())){
				try{Thread.sleep(1000);}catch(Exception ex){}
				waitForUp--;
				machineState = api.getVMs().get(myVM.getName()).getStatus().getState();
			}
			log.finest(String.format("RHEV-M guest bring up after: [%s] rounds.",(RHEVM_MAX_WAIT - waitForUp)));
			// sleep a bit to get IP via rhev-agentd
			log.finest(String.format("RHEV-M guest wait to load rhev-agentd for [%s] sec.", (RHEVM_AGENTD_WAIT/1000)));
			try{Thread.sleep(RHEVM_AGENTD_WAIT);}catch(InterruptedException ex){}
			
			// Set the object and return
			machine.setInstance(api.getVMs().get(myVM.getName()));
			
		}catch (Exception e){e.printStackTrace();}
		finally{if(api!=null)api.shutdown();} // REALLY important to have it _always_ executed
		
		return machine;
	}

	public static void destroyMachine(DeltaCloudInstance machine) {
		Api api = null;
		try {
			String realm = System.getProperty("deltacloud.realm");
			if (realm != null && realm.trim().isEmpty()) realm = null;
			Assert.assertNotNull(System.getProperty("deltacloud.hostname"), "Deltacloud hostname shoud be provided in system property \"deltacloud.hostname\"");
			Assert.assertNotNull(System.getProperty("deltacloud.user"), "Deltacloud username shoud be provided in system property \"deltacloud.user\"");
			Assert.assertNotNull(System.getProperty("deltacloud.password"), "Deltacloud password shoud be provided in system property \"deltacloud.password\"");
			Assert.assertNotNull(System.getProperty("deltacloud.cluster"), "Deltacloud cluster shoud be provided in system property \"deltacloud.cluster\"");

			api = new Api(System.getProperty("deltacloud.hostname"), 
					System.getProperty("deltacloud.user"), 
					System.getProperty("deltacloud.password"),
					true); //ignore SSL 
			
			// check if machine exists
			VM myVM = machine.getInstance();
			if(api.getVMs().get(myVM.getName())==null){
				log.warning(String.format("Machine [%s] does not exists in rhev-m list. Nothing to do.",myVM.getName()));return;
			}
			
			// if machine is not 'UP' - exception.
			String machineState = api.getVMs().get(myVM.getName()).getStatus().getState();
			if(!machineState.equals(RhevStates.up.toString())){
				log.warning("Machine is not UP. Could not destroy."); return; // WELL, the case when it already is down? <--- but who the hell put it (manually) to down ?
			}
			// Let's STOP
			org.ovirt.engine.sdk.decorators.VM myVMToDestroy = api.getVMs().get(myVM.getName());
			myVMToDestroy.stop(new Action() {
				{
					setVm(new org.ovirt.engine.sdk.entities.VM());
				}
			});
			
			myVM = machine.getInstance();
			int waitForDown = RHEVM_MAX_WAIT;
			machineState = api.getVMs().get(myVM.getName()).getStatus().getState();
			while(waitForDown>0 && !machineState.equals(RhevStates.down.toString())){
				try{Thread.sleep(1000);}catch(Exception ex){}
				waitForDown--;
				machineState = api.getVMs().get(myVM.getName()).getStatus().getState();
			}
			
			// Let's DROP it
			api.getVMs().get(myVM.getName()).delete(); // we don't care when rhev-m will finally clean it from the list.
		}catch(Exception e){e.printStackTrace();}
		finally{if(api!=null)api.shutdown();} // REALLY important to have it _always_ executed
	}
	
	public static boolean isRhevmInstance(String name) {
		Api api = null;
		try {
			String realm = System.getProperty("deltacloud.realm");
			if (realm != null && realm.trim().isEmpty()) realm = null;
			Assert.assertNotNull(System.getProperty("deltacloud.hostname"), "Deltacloud hostname shoud be provided in system property \"deltacloud.hostname\"");
			Assert.assertNotNull(System.getProperty("deltacloud.user"), "Deltacloud username shoud be provided in system property \"deltacloud.user\"");
			Assert.assertNotNull(System.getProperty("deltacloud.password"), "Deltacloud password shoud be provided in system property \"deltacloud.password\"");
			Assert.assertNotNull(System.getProperty("deltacloud.cluster"), "Deltacloud cluster shoud be provided in system property \"deltacloud.cluster\"");

			api = new Api(System.getProperty("deltacloud.hostname"), 
					System.getProperty("deltacloud.user"), 
					System.getProperty("deltacloud.password"),
					true); //ignore SSL
			return  api.getVMs().get(name)!=null;
		} catch (Exception e){return false;}
		finally{if(api!=null)api.shutdown();} // REALLY important to have it _always_ executed
	}

}
