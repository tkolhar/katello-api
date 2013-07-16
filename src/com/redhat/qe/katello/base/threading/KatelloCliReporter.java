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
	
	private static volatile boolean initiated = false;
	private static volatile boolean destroyed = false; 

	@Override
	public void onTestStart(ITestResult result) {
		cliPool.setMethodRunning(result.getTestClass().getName(), result.getName());
	}

	@Override
	public void onTestSuccess(ITestResult result) {
		cliPool.setMethodFinished(result.getTestClass().getName(), result.getName());
		cntPass++; logCurrentStatus();
	}

	@Override
	public void onTestFailure(ITestResult result) {
		cliPool.setMethodFinished(result.getTestClass().getName(), result.getName());
		cntFail++; logCurrentStatus();
	}

	@Override
	public void onTestSkipped(ITestResult result) {
		cliPool.setMethodFinished(result.getTestClass().getName(), result.getName());
		cntSkip++; logCurrentStatus();
	}

	@Override
	public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
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
		if(!initiated){
			cliPool = KatelloCliWorkersPool.getInstance(suite);
			(new Thread(cliPool)).start();
			while(!cliPool.ready())
				try{Thread.sleep(100);}catch(InterruptedException iex){}

			configurePythonCoverage();
			initiated = true;
		}
	}

	@Override
	public void onFinish(ISuite suite) {
		if(!destroyed){
			cliPool.enableRepos();
			collectPythonCoverage();
			destroyed = true;
		}
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
	
	private void configurePythonCoverage(){
		if(!System.getProperty("katello.workers.list","").trim().equals("")) return; // on multi-threading mode - NO COVERAGE!!!
		if(System.getProperty("katello.coverage", "false").trim().equalsIgnoreCase("true")){
			SSHCommandResult res = KatelloUtils.sshOnClient(System.getProperty("katello.client.hostname", "localhost"), 
					"rm -rf ~/htmlcov/ .coverage* &> /dev/null; python -c 'from distutils.sysconfig import get_python_lib; print get_python_lib()'"); // returns site-packages path.
			KatelloCli.CMD_PY_COVERAGE = "coverage run -a --branch " +
					"--include "+res.getStdout().trim()+"/katello/client/core/*.py,"+res.getStdout().trim()+"/katello/client/api/*.py";;
		}
	}
	
	private void collectPythonCoverage(){
		if(!System.getProperty("katello.workers.list","").trim().equals("")) return; // on multi-threading mode - NO COVERAGE!!!
		if(System.getProperty("katello.coverage", "false").trim().equalsIgnoreCase("true")){
			String uid = KatelloUtils.getUniqueID();
			String reportTarball = "htmlcov-"+uid+".tar.gz";
			String clientHostname = System.getProperty("katello.client.hostname", "localhost");
			KatelloUtils.sshOnClient(clientHostname,
					"coverage html; cd htmlcov/ && tar czvf "+reportTarball+" $(ls) &>/dev/null;");
			KatelloUtils.scpOnClientGetFile(clientHostname, "~/htmlcov/"+reportTarball, "/tmp");
			KatelloUtils.run_local("rm -rf ./coverage-output/; mkdir ./coverage-output/; mv /tmp/"+reportTarball+" ./coverage-output/; cd ./coverage-output/; tar xzvf "+reportTarball+"; rm -f "+reportTarball);
		}
	}
}
