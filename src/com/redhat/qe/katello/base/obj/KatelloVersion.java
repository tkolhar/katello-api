package com.redhat.qe.katello.base.obj;
import java.util.ArrayList;
import javax.management.Attribute;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.tools.SSHCommandResult;
public class KatelloVersion {
	   
	    public static final String CMD_VERSION = "version";
	    private KatelloCli cli;
		private ArrayList<Attribute> opts;
		
		public KatelloVersion(){
			this.opts = new ArrayList<Attribute>();
		}
		
		public SSHCommandResult cli_version(){
			opts.clear();
			cli = new KatelloCli(CMD_VERSION, opts);
			return cli.run();
		}
		
}

