package com.redhat.qe.katello.base.threading;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Logger;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.internal.IResultListener;

public class KatelloCliReporter implements IResultListener, ISuiteListener{
	protected static Logger log =Logger.getLogger(KatelloCliReporter.class.getName());
	private static KatelloCliWorkersPool cliPool = null;
	private final int INDENT_DIGIT_COUNT = 3; // assuming tests count in range: [0..999]
	
	private int cntPass = 0, cntFail = 0, cntSkip = 0;
	private int total;

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
		cliPool = KatelloCliWorkersPool.getInstance(suite);
		(new Thread(cliPool)).start();
		while(!cliPool.ready())
			try{Thread.sleep(100);}catch(InterruptedException iex){}
		total = suite.getAllMethods().size();
	}

	@Override
	public void onFinish(ISuite suite) {
		cliPool.enableRepos();
	}
	
	private void logCurrentStatus(){
		log.info(String.format(":: %s :: %s. :: PASS (%s) :: FAIL (%s) :: SKIP (%s) :: TOTAL (%s) ::",
				new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss").format(Calendar.getInstance().getTime()),
				prettify(String.valueOf(cntPass+cntFail+cntSkip),INDENT_DIGIT_COUNT), 
				prettify(String.valueOf(cntPass),INDENT_DIGIT_COUNT), 
				prettify(String.valueOf(cntFail),INDENT_DIGIT_COUNT),
				prettify(String.valueOf(cntSkip),INDENT_DIGIT_COUNT),
				prettify(String.valueOf(total),INDENT_DIGIT_COUNT)));
	}
	
	private String prettify(String msg, int rightIndent){
		String pretty = msg;
		for(int i=0;i<(rightIndent-msg.length());i++)
			pretty = " "+pretty;
		return pretty;
	}
}
