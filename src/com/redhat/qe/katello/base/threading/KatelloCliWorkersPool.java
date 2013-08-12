package com.redhat.qe.katello.base.threading;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import org.testng.ISuite;
import org.testng.ITestNGMethod;

import com.redhat.qe.katello.base.threading.KatelloCliDelayedCall.RunStatus;
import com.redhat.qe.katello.common.KatelloUtils;


public class KatelloCliWorkersPool implements Runnable{
	private static KatelloCliWorkersPool _me = null;
	private boolean running;
	private boolean readyToServe;
	
	private static Logger log = Logger.getLogger(KatelloCliWorkersPool.class.getName());
	private KatelloCliWorkersPool(){}

	private ArrayList<KatelloCliDelayedCall> methodsPool = null;
	private ArrayList<KatelloCliWorker> cliWorkers;

	public static synchronized KatelloCliWorkersPool getInstance(ISuite suite){
		if(_me == null){
			if(suite==null) return null; // Running from Eclipse.
			_me = new KatelloCliWorkersPool();
			_me.initMethodsPool(suite);
			_me.initWorkers();
			_me.disableRepos();
			_me.running = false;
			_me.readyToServe = false;
		}
		return _me;
	}
	
	public boolean running(){
		return running;
	}
	
	public synchronized boolean ready(){
		return readyToServe;
	}
	
	private final void initWorkers(){
		cliWorkers = new ArrayList<KatelloCliWorker>();
		KatelloCliWorker kcr;
		
		StringTokenizer tok = new StringTokenizer(
				System.getProperty("katello.workers.list", 
				System.getProperty("katello.client.hostname","localhost")), ",");
		while(tok.hasMoreTokens()){
			kcr = new KatelloCliWorker(
					System.getProperty("katello.server.hostname","localhost"),
					tok.nextToken());
			cliWorkers.add(kcr);
		}
	}
	
	private void disableRepos(){
		for(KatelloCliWorker worker: cliWorkers){
			KatelloUtils.disableYumRepo(worker.getClientHostname(),"beaker");
			KatelloUtils.disableYumRepo(worker.getClientHostname(),"epel");
			KatelloUtils.disableYumRepo(worker.getClientHostname(),"katello-tools");
		}
	}
	
	public void enableRepos(){
		for(KatelloCliWorker worker: cliWorkers){
			KatelloUtils.enableYumRepo(worker.getClientHostname(),"beaker");
			KatelloUtils.enableYumRepo(worker.getClientHostname(),"epel");
			KatelloUtils.enableYumRepo(worker.getClientHostname(),"katello-tools");
		}
	}
	
	private void initMethodsPool(ISuite suite){
		if(methodsPool != null) return;
		log.info("<<< Methods pool initializing");
		List<ITestNGMethod> methodsNG = suite.getAllMethods();
		methodsNG.get(0);
		methodsNG.get(0).getTestClass().getName();
		methodsPool = new ArrayList<KatelloCliDelayedCall>();
		for(ITestNGMethod tmp: methodsNG){
			methodsPool.add(new KatelloCliDelayedCall(tmp.getTestClass().getName(),tmp.getMethodName()));
		}
		log.info("<<< [workers pool]: methods pool initialized with count: ["+methodsNG.size()+"]");
	}
	
	public synchronized KatelloCliWorker getWorker(String threadname, String classname){
		for(int i=0;i<cliWorkers.size();i++){
			if(cliWorkers.get(i).isBusy() && 
					cliWorkers.get(i).getThreadName().equals(threadname) &&
					cliWorkers.get(i).getClassName().equals(classname)){
				KatelloCliWorker _ret = cliWorkers.get(i).clone();
				_ret.setBusy(threadname, classname);
				return _ret;
			}
		}
		for(int j=0;j<cliWorkers.size();j++){
			if(!cliWorkers.get(j).isBusy()){
				cliWorkers.get(j).setBusy(threadname,classname);
				KatelloCliWorker _ret = cliWorkers.get(j).clone();
				_ret.setBusy(threadname,classname);
				return _ret;
			}
		}
		return null;
	}

	@Override
	public void run() {
		this.running = true;
		
		// 1. invoke all of them in pending status.
		ExecutorService executor = Executors.newFixedThreadPool(methodsPool.size());
		for (int i = 0; i < methodsPool.size(); i++) {
			executor.execute(methodsPool.get(i));
		}
		log.info("<<< [workers pool]: methods pool is ready");
		
		// 2. listen to all of them to be FINISH
		executor.shutdown();
		readyToServe = true;
		log.info("<<< [workers pool]: ready to serve");
		
		while (!executor.isTerminated()) {
			sleep(500);
			monitorWorkers();
		}
		log.info("<<< [workers pool]: done with methods pool (finish/timeout)");
		
		// 3. inform all of the world: I am done.
		this.running = false;
		this.readyToServe = false;
	}
	
	public void setMethodRunning(String classname, String methodname){
		getMethod(classname, methodname).setStatus(RunStatus.RUNNING);
	}
	
	public void setMethodFinished(String classname, String methodname){
		getMethod(classname, methodname).setStatus(RunStatus.FINISHED);
	}
	
	private KatelloCliDelayedCall getMethod(String classname, String methodname){
		for (int i = 0; i < methodsPool.size(); i++) {
			if(methodsPool.get(i).getClassname().equals(classname) &&
					methodsPool.get(i).getMethodname().equals(methodname)){
				return methodsPool.get(i);
			}
		}
		return null; // should not happen
	}
	
	private void sleep(int millisec){
		try{Thread.sleep(millisec);}catch(InterruptedException iex){}
	}
	
	private void monitorWorkers(){
		for(int i=0;i<cliWorkers.size();i++){
			if(cliWorkers.get(i).isBusy()){
				if(classRunFinished(cliWorkers.get(i).getClassName())){
					cliWorkers.get(i).setFree();
				}
			}
		}
	}
	
	private boolean classRunFinished(String classname){
		boolean _return = true;
		
		for(int i=0; i<methodsPool.size(); i++){
			if(methodsPool.get(i).getClassname().equals(classname) && !methodsPool.get(i).getStatus().equals(RunStatus.FINISHED)){
				return false;
			}
		}
		return _return;
	}
}
