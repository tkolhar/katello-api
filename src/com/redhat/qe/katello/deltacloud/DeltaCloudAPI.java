package com.redhat.qe.katello.deltacloud;

import java.net.MalformedURLException;

import org.apache.deltacloud.client.DeltaCloudClientException;
import org.apache.deltacloud.client.DeltaCloudClientImpl;
import org.apache.deltacloud.client.DeltaCloudNotFoundClientException;
import org.apache.deltacloud.client.Instance;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.obj.DeltaCloudInstance;

public class DeltaCloudAPI {
	
	/**
	 * @IMPORTANT DO NOT CALL THESE METRODS DIRECTLY, USE KatelloUtils TO CREATE DeltaCloud machines.
	 */
	
	private static final int MAX_ATTEMPTS = 10; 
	private static final int SLEEP_TIME = 30000;
	
	public static DeltaCloudInstance provideServer(boolean nowait, String hostname) {
		String image = System.getProperty("deltacloud.server.imageid", "7657667a-4108-4484-88d2-c103467a20b3");
		return provideMachine(nowait, hostname, "8192", "50", image);
	}

	public static DeltaCloudInstance provideClient(boolean nowait, String hostname) {
		String image = System.getProperty("deltacloud.client.imageid", "fc06e21b-8973-48e2-9d64-3b5a90f2717e");
		return provideMachine(nowait, hostname, null, null, image);
	}
	public static DeltaCloudInstance provideClient(boolean nowait, String hostname, String imageId) {
		return provideMachine(nowait, hostname, null, null, imageId);
	}
	
	private static DeltaCloudInstance provideMachine(boolean nowait, String hostname, String memory, String storage, String image) {
		
		DeltaCloudInstance machine = new DeltaCloudInstance();

		try {
			String realm = System.getProperty("deltacloud.realm");
			if (realm != null && realm.trim().isEmpty()) realm = null;
			Assert.assertNotNull(System.getProperty("deltacloud.hostname"), "Deltacloud hostname shoud be provided in system property \"deltacloud.hostname\"");
			Assert.assertNotNull(System.getProperty("deltacloud.user"), "Deltacloud username shoud be provided in system property \"deltacloud.user\"");
			Assert.assertNotNull(System.getProperty("deltacloud.password"), "Deltacloud password shoud be provided in system property \"deltacloud.password\"");
			DeltaCloudClientImpl dcl = new DeltaCloudClientImpl(System.getProperty("deltacloud.hostname"), System.getProperty("deltacloud.user"), System.getProperty("deltacloud.password"));
			Instance inst = dcl.createInstance(hostname, image, null, realm, memory, storage);
			machine.setInstance(inst);
			machine.setClient(dcl);
			if (nowait) {
				try {
					boolean started = inst.start(dcl);
					if (!started) {
						inst.start(dcl);
					}
				} catch (Exception e) {}
			} else {
				startMachine(machine);
			}
		} catch (DeltaCloudClientException e) {	e.printStackTrace();
		} catch (MalformedURLException e) { e.printStackTrace();}
		
		return machine;
	}

	public static void stopMachine(DeltaCloudInstance machine) {
		try {
			int i = 0;
			DeltaCloudClientImpl dcl = machine.getClient();
			Instance inst = machine.getInstance();
			while (!inst.isStopped() && ++i <= MAX_ATTEMPTS) {
				try {
					inst.stop(dcl);
				} catch (Exception e) {}
				
				Thread.sleep(SLEEP_TIME);
				
				inst = dcl.listInstances(inst.getId());
			}
			machine.setInstance(inst);
		} catch (DeltaCloudClientException e) {	
		} catch (InterruptedException e) {}
	}
	
	public static void startMachine(DeltaCloudInstance machine) {
		try {
			int i = 0;
			DeltaCloudClientImpl dcl = machine.getClient();
			Instance inst = machine.getInstance();
			while (!inst.isRunning() && ++i <= MAX_ATTEMPTS) {
				try {
					inst.start(dcl);
				} catch (Exception e) {}
				
				Thread.sleep(SLEEP_TIME);
				
				inst = dcl.listInstances(inst.getId());
			}
			i = 0;
			while (inst.getPublicAddresses().size() <= 2 && ++i <= MAX_ATTEMPTS) {
				Thread.sleep(SLEEP_TIME);
				
				inst = dcl.listInstances(inst.getId());
			}
			Assert.assertTrue(inst.getPublicAddresses().size() == 3, "IP address is not provided on machine");
			Assert.assertTrue(inst.isRunning(), "Machine is running");
			machine.setInstance(inst);
			machine.setIpAddress(inst.getPublicAddresses().get(0));
		} catch (DeltaCloudClientException e) {	
		} catch (InterruptedException e) {}
	}
	
	public static void restartMachine(DeltaCloudInstance machine) {
		stopMachine(machine);
		startMachine(machine);
	}
	
	public static void destroyMachine(DeltaCloudInstance machine) {
		stopMachine(machine);

		try {
			Instance inst = machine.getInstance();
			DeltaCloudClientImpl dcl = machine.getClient();
			int i = 0;
			while (inst != null && ++i <= MAX_ATTEMPTS) {
				try {
					inst.destroy(dcl);
				} catch (Exception e) {}
				
				Thread.sleep(SLEEP_TIME);
				
				try {
					inst = dcl.listInstances(inst.getId());
				} catch (Exception e) {
					inst = null;
				}
			}
		} catch (InterruptedException e) {}		
	}
	
	public static boolean isMachineExists(String name) {
		try {
			Assert.assertNotNull(System.getProperty("deltacloud.hostname"), "Deltacloud hostname shoud be provided in system property \"deltacloud.hostname\"");
			Assert.assertNotNull(System.getProperty("deltacloud.user"), "Deltacloud username shoud be provided in system property \"deltacloud.user\"");
			Assert.assertNotNull(System.getProperty("deltacloud.password"), "Deltacloud password shoud be provided in system property \"deltacloud.password\"");
			DeltaCloudClientImpl dcl = new DeltaCloudClientImpl(System.getProperty("deltacloud.hostname"), System.getProperty("deltacloud.user"), System.getProperty("deltacloud.password"));
			for (Instance inst : dcl.listInstances()) {
				if (inst.getName().equals(name)) return true;
			}
		} catch (DeltaCloudNotFoundClientException e) {	return false;
		} catch (DeltaCloudClientException e) {	e.printStackTrace();
		} catch (MalformedURLException e) { e.printStackTrace();}
		
		return false;
	}

}
