package com.redhat.qe.katello.base;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.management.Attribute;

import com.redhat.qe.katello.base.obj.KatelloUser;
import com.redhat.qe.katello.common.KatelloConstants;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloCli implements KatelloConstants {

	static{new com.redhat.qe.auto.testng.TestScript();}// to make properties be initialized (if they don't still)
	
	static protected Logger log = Logger.getLogger(KatelloCli.class.getName());
	public static final String OUT_EMPTY_LIST = "[  ]";
	
	private String command;
	private List<Attribute> args;
	private List<Attribute> opts;
	
	public KatelloCli(String command,List<Attribute> args,List<Attribute> options){
		this.command = command;
		this.args = args;
		this.opts = options;
		if(this.args==null) this.args = new ArrayList<Attribute>();
		if(this.opts==null) this.opts = new ArrayList<Attribute>();
	}
	
	public KatelloCli(String command,List<Attribute> options){
		this.command = command;
		this.args = new ArrayList<Attribute>();
		this.args.add(new Attribute("username", System.getProperty("katello.admin.user", "admin")));
		this.args.add(new Attribute("password", System.getProperty("katello.admin.password", "admin")));
		this.opts = options;
		if(this.opts==null) this.opts = new ArrayList<Attribute>();
	}
	
	public KatelloCli(String command,ArrayList<Attribute> options, KatelloUser user){
		this.command = command;
		this.args = new ArrayList<Attribute>();
		this.args.add(new Attribute("username", user.username));
		this.args.add(new Attribute("password", user.password));
		this.opts = options;
		if(this.opts==null) this.opts = new ArrayList<Attribute>();
	}
		
	public SSHCommandResult run(){
		String cmd = System.getProperty("katello.engine", "katello");
		String locale = System.getProperty("katello.locale", KATELLO_DEFAULT_LOCALE);
		for(int i=0;i<this.args.size();i++){
			cmd = "export LANG=" + locale + " && " + cmd + " --" + args.get(i).getName()+" \""+args.get(i).getValue().toString()+"\"";
		}
		cmd = cmd + " " + this.command;
		for(int i=0;i<this.opts.size();i++){
			if(this.opts.get(i).getValue()!=null)
				cmd = cmd + " --" + opts.get(i).getName()+" \""+opts.get(i).getValue().toString()+"\"";
		}
		
		try {
			return KatelloUtils.sshOnClient(cmd);
		}
		catch (Exception e) {
			e.printStackTrace();
		}return null;
	}
	
	/**
	 * Returns katello cli output block (usually: [command] list -v options) that has: <BR>
	 * [Property]:  [Value] in its block.<br>
	 * As an example would be getting a pool information for:<BR> 
	 * ("ProductName","High-Availability (8 sockets)",org.subscriptions())
	 * @param property
	 * @param value
	 * @param output
	 * @return
	 */
	public static String grepOutBlock(String property, String value, String output){
		String _return = null;
		String[] lines = output.split("\\n\\n");
		
		for(String line:lines ){
			if(line.startsWith("---") || line.trim().equals("")) continue; // skip it.
			if(grepCLIOutput(property, line).equals(value)){
				_return = line.trim();
				break;
			}
		}
		return _return;
	}
	
    public static String grepCLIOutput(String property, String output) {
        return grepCLIOutput(property, output, 1);
    }

    public static String grepCLIOutput(String property, String output, int occurence) {
        int meet_cnt = 0;
        String[] lines = output.split("\\n");
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].startsWith(property)) { // our line
                meet_cnt++;
                if (meet_cnt == occurence) {
                    String[] split = lines[i].split(":\\s+");
                    if (split.length < 2) {
                        return lines[i + 1].trim();
                    } else {
                        return split[1].trim();
                    }
                }
            }
        }
        log.severe("ERROR: Output can not be extracted for the property: [" + property
                + "]");
        return null;
    }

}
