package com.redhat.qe.katello.base.obj;

import com.redhat.qe.tools.SSHCommandResult;

public class KatelloVersion extends _KatelloObject{
	   
	    public static final String CMD_VERSION = "version";
	    
	    public static final String REG_VERSION = ".*[\\w\\s\\.]{10,40}.*";
		
		public SSHCommandResult cli_version(){
			opts.clear();
			return run(CMD_VERSION);
		}
		
}