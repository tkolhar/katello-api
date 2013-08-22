package com.redhat.qe.katello.base.obj;

import com.redhat.qe.katello.base.threading.KatelloCliWorker;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloAbout extends _KatelloObject {

	public static final String CMD_ABOUT = "about";

	public KatelloAbout(KatelloCliWorker kcr) {
		this.kcr = kcr;
	}

	public SSHCommandResult about() {
		opts.clear();
		return run(CMD_ABOUT);
	}
}
