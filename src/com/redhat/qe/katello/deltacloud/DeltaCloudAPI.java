package com.redhat.qe.katello.deltacloud;

import java.net.MalformedURLException;

import org.apache.deltacloud.client.DeltaCloudClientException;
import org.apache.deltacloud.client.DeltaCloudClientImpl;
import org.apache.deltacloud.client.Instance;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.obj.DeltaCloudInstance;

public class DeltaCloudAPI {
	
	/**
	 * @IMPORTANT DO NOT CALL THESE METRODS DIRECTLY, USE KatelloUtils TO CREATE DeltaCloud machines.
	 */
	
	private static final int MAX_ATTEMPTS = 10; 
	
	public static DeltaCloudInstance provideServer(boolean nowait) {
		return provideMachine(nowait);
	}

	public static DeltaCloudInstance provideClient(boolean nowait) {
		return provideMachine(nowait);
	}
	
	private static DeltaCloudInstance provideMachine(boolean nowait) {
		
		DeltaCloudInstance machine = new DeltaCloudInstance();

		try {
			String image = System.getProperty("deltacloud.imageid", "2e477879-c1d3-4fe0-a5a3-493bccdde031");
			Assert.assertNotNull(System.getProperty("deltacloud.hostname"), "Deltacloud hostname shoud be provided in system property \"deltacloud.hostname\"");
			Assert.assertNotNull(System.getProperty("deltacloud.user"), "Deltacloud username shoud be provided in system property \"deltacloud.user\"");
			Assert.assertNotNull(System.getProperty("deltacloud.password"), "Deltacloud password shoud be provided in system property \"deltacloud.password\"");
			DeltaCloudClientImpl dcl = new DeltaCloudClientImpl(System.getProperty("deltacloud.hostname"), System.getProperty("deltacloud.user"), System.getProperty("deltacloud.password"));
			Instance inst = dcl.createInstance(image);
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
				
				Thread.sleep(10000);
				
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
				
				Thread.sleep(10000);
				
				inst = dcl.listInstances(inst.getId());
			}
			i = 0;
			while (inst.getPublicAddresses().size() <= 2 && ++i <= MAX_ATTEMPTS) {
				Thread.sleep(10000);
				
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
				
				Thread.sleep(10000);
				
				try {
					inst = dcl.listInstances(inst.getId());
				} catch (Exception e) {
					inst = null;
				}
			}
		} catch (InterruptedException e) {}		
	}

}
