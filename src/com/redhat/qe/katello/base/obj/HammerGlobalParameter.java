package com.redhat.qe.katello.base.obj;

import java.util.logging.Logger;

import javax.management.Attribute;

import com.redhat.qe.katello.base.threading.KatelloCliWorker;
import com.redhat.qe.tools.SSHCommandResult;

public class HammerGlobalParameter extends _HammerObject{
	protected static Logger log = Logger.getLogger(HammerOs.class.getName());
	
	// ** ** ** ** ** ** ** Public constants
		public static final String CMD_SET = "global_parameter set";
		public static final String CMD_LIST = "global_parameter list";
		public static final String CMD_DELETE = "global_parameter delete";
		
		public static final String ERR_NO_DATA = "No data.";
		public static final String ERR_DELETE = "Could not delete the global parameter:"+"\n"+"  404 Resource Not Found";
		
		// ** ** ** ** ** ** ** Class members
		public String value;
		public String name;
		
		public HammerGlobalParameter(){super();}
		
		public HammerGlobalParameter(KatelloCliWorker kcr, String pname, String pvalue){
			this.kcr = kcr;
			this.name = pname;
			this.value = pvalue;
		}
		
		public SSHCommandResult set() {
			args.clear();
			args.add(new Attribute("name", name));
			args.add(new Attribute("value", value));
			return run(CMD_SET);
		}
		
		public SSHCommandResult list(String searchStr, String order, String page) {
			args.clear();
			args.add(new Attribute("search", searchStr));
			args.add(new Attribute("order", order));
			args.add(new Attribute("page", page));
			return run(CMD_LIST);
		}
		
		public SSHCommandResult delete() {
			args.clear();
			args.add(new Attribute("name", name));
			return run(CMD_DELETE);
		}
			
}
