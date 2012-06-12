package com.redhat.qe.katello.base.obj;
import java.util.ArrayList;
import javax.management.Attribute;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.tools.SSHCommandResult;
public class KatelloPing {
	   
	    public static final String CMD_PING = "ping -v";
	    private KatelloCli cli;
		private ArrayList<Attribute> opts;
		
		public KatelloPing(){
			this.opts = new ArrayList<Attribute>();
		}
		
		public SSHCommandResult cli_ping(){
			opts.clear();
			cli = new KatelloCli(CMD_PING, opts);
			return cli.run();
		}
		
}
