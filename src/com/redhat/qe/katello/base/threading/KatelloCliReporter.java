package com.redhat.qe.katello.base.threading;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Logger;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.internal.IResultListener;

import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloCliReporter implements IResultListener, ISuiteListener{
	protected static Logger log =Logger.getLogger(KatelloCliReporter.class.getName());
	private static KatelloCliWorkersPool cliPool = null;
	private final int INDENT_DIGIT_COUNT = 3; // assuming tests count in range: [0..999]
	
	private int cntPass = 0, cntFail = 0, cntSkip = 0;
	
	// Threads
	private static volatile boolean singleThreaded = true;
	private static volatile boolean pyCoverage = false;

	
	@Override
	public void onTestStart(ITestResult result) {
		if(singleThreaded) return;
		cliPool.setMethodRunning(result.getTestClass().getName(), result.getName());
	}

	@Override
	public void onTestSuccess(ITestResult result) {
		if(singleThreaded) return;
		cliPool.setMethodFinished(result.getTestClass().getName(), result.getName());
		cntPass++; logCurrentStatus();
	}

	@Override
	public void onTestFailure(ITestResult result) {
		if(singleThreaded) return;
		cliPool.setMethodFinished(result.getTestClass().getName(), result.getName());
		cntFail++; logCurrentStatus();
	}

	@Override
	public void onTestSkipped(ITestResult result) {
		if(singleThreaded) return;
		cliPool.setMethodFinished(result.getTestClass().getName(), result.getName());
		cntSkip++; logCurrentStatus();
	}

	@Override
	public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
		if(singleThreaded) return;
		cliPool.setMethodFinished(result.getTestClass().getName(), result.getName());
	}

	@Override
	public void onStart(ITestContext context) {
	}

	@Override
	public void onFinish(ITestContext context) {
	}

	@Override
	public void onConfigurationSuccess(ITestResult itr) {
	}

	@Override
	public void onConfigurationFailure(ITestResult itr) {
	}

	@Override
	public void onConfigurationSkip(ITestResult itr) {
	}

	@Override
	public void onStart(ISuite suite) {
		initSuiteOnce(suite);
	}

	@Override
	public void onFinish(ISuite suite) {
		destroySuiteOnce(suite);
	}
	
	private static volatile boolean initiated = false;
	private static synchronized void initSuiteOnce(ISuite suite){
		if(initiated) return;
		String workers = System.getProperty("katello.workers.list","");
		if(workers.trim().equals("")){
			singleThreaded = true;
			pyCoverage = System.getProperty("katello.coverage","false").trim().equalsIgnoreCase("true");
		}
		else{
			singleThreaded = false;
			pyCoverage = false; // no coverage run (how you plan to gather/merge coverage reports from >1 clients :P ?)
		}
		if(!singleThreaded)
			configureThreadsPool(suite);
		if(pyCoverage)
			configurePythonCoverage();
		initiated = true;
	}
	
	private static void configureThreadsPool(ISuite suite){
		cliPool = KatelloCliWorkersPool.getInstance(suite);
		(new Thread(cliPool)).start();
		while(!cliPool.ready())
			try{Thread.sleep(100);}catch(InterruptedException iex){}
	}
	
	private static void configurePythonCoverage(){
		SSHCommandResult res = KatelloUtils.sshOnClient(System.getProperty("katello.client.hostname", "localhost"), 
				"rm -rf ~/htmlcov/ .coverage* &> /dev/null; python -c 'from distutils.sysconfig import get_python_lib; print get_python_lib()'"); // returns site-packages path.
		KatelloCli.CMD_PY_COVERAGE = "coverage run -a --branch " +
				"--include "+res.getStdout().trim()+"/katello/client/core/*.py,"+res.getStdout().trim()+"/katello/client/api/*.py";;
	}
	
	private static volatile boolean destroyed = false;
	private static synchronized void destroySuiteOnce(ISuite suite){ // we may need param suite in the future?
		if(destroyed) return;
		
		if(!singleThreaded)
			cliPool.enableRepos();
		if(pyCoverage)
			collectPythonCoverage();
		destroyed = true;
	}
	
	private static void collectPythonCoverage(){
		String uid = KatelloUtils.getUniqueID();
		String reportTarball = "htmlcov-"+uid+".tar.gz";
		String clientHostname = System.getProperty("katello.client.hostname", "localhost");
		KatelloUtils.sshOnClient(clientHostname,
				"coverage html; cd htmlcov/ && tar czvf "+reportTarball+" $(ls) &>/dev/null;");
		KatelloUtils.scpOnClientGetFile(clientHostname, "~/htmlcov/"+reportTarball, "/tmp");
		KatelloUtils.run_local("rm -rf ./coverage-output/; mkdir ./coverage-output/; mv /tmp/"+reportTarball+" ./coverage-output/; cd ./coverage-output/; tar xzvf "+reportTarball+"; rm -f "+reportTarball);
	}
	
	private void logCurrentStatus(){
		log.info(String.format(":: %s :: %s. :: PASS (%s) :: FAIL (%s) :: SKIP (%s) :: TOTAL (%s) ::",
				new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss").format(Calendar.getInstance().getTime()),
				prettify(String.valueOf(cntPass+cntFail+cntSkip),INDENT_DIGIT_COUNT), 
				prettify(String.valueOf(cntPass),INDENT_DIGIT_COUNT), 
				prettify(String.valueOf(cntFail),INDENT_DIGIT_COUNT),
				prettify(String.valueOf(cntSkip),INDENT_DIGIT_COUNT),
				prettify(String.valueOf(cntPass+cntFail+cntSkip),INDENT_DIGIT_COUNT)));
	}
	
	private String prettify(String msg, int rightIndent){
		String pretty = msg;
		for(int i=0;i<(rightIndent-msg.length());i++)
			pretty = " "+pretty;
		return pretty;
	}
}
