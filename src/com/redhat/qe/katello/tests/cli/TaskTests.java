package com.redhat.qe.katello.tests.cli;

import com.redhat.qe.Assert;

import org.testng.annotations.Test;

import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloTask;
import com.redhat.qe.katello.base.tngext.TngPriority;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

@TngPriority(32)
public class TaskTests extends KatelloCliTestBase {

	public SSHCommandResult exec_result;
	
	@Test(description="list all tasks")
	public void test_listAll() {
		KatelloTask task = new KatelloTask(cli_worker, base_org_name, null);
		exec_result = task.list();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (list all tasks)");
	}

	@Test(description="list tasks by status")
	public void test_listByStatus() {
		KatelloTask task = new KatelloTask(cli_worker, base_org_name, null);
		exec_result = task.list("finished", null);
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (list tasks by state)");
	}

	@Test(description="list tasks by type")
	public void test_listByType() {
		KatelloTask task = new KatelloTask(cli_worker, base_org_name, null);
		exec_result = task.list(null, "TaskStatus");
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (list tasks by type)");
	}

	@Test(description="task status")
	public void test_taskState() {
		KatelloTask task = new KatelloTask(cli_worker, base_org_name, null);
		exec_result = task.list();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (list tasks)");
		task.uuid = KatelloUtils.grepCLIOutput("UUID", getOutput(exec_result));
		exec_result = task.status();
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (task status)");
		Assert.assertTrue(getOutput(exec_result).replace("\n", "").matches(KatelloTask.REG_STATUS), "Check output (task status)");
	}

	@Test(description="task status - wrong id")
	public void test_taskStatusWrongID() {
		KatelloTask task = new KatelloTask(cli_worker, base_org_name, KatelloUtils.getUniqueID());
		exec_result = task.status();
		Assert.assertTrue(exec_result.getExitCode()==65, "Check exit code (task status)");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloTask.ERR_NOT_FOUND, task.uuid)), "Check output (task status)");
	}

	@Test(description="list tasks by status - wrong status")
	public void test_listByWrongStatus() {
		String state = "wrong-state";
		KatelloTask task = new KatelloTask(cli_worker, base_org_name, null);
		exec_result = task.list(state, null);
		Assert.assertTrue(exec_result.getExitCode()==65, "Check exit code (list tasks by state)");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloTask.ERR_INVALID_STATE, state)), "Check output (task list)");
	}
}
