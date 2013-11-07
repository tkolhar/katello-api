package com.redhat.qe.katello.base.obj;

import java.util.logging.Logger;

import javax.management.Attribute;

import com.redhat.qe.katello.base.threading.KatelloCliWorker;
import com.redhat.qe.tools.SSHCommandResult;

public class HammerProxy extends _HammerObject{

protected static Logger log = Logger.getLogger(HammerProxy.class.getName());
	
	// ** ** ** ** ** ** ** Public constants
	public static final String CMD_CREATE = "proxy create";
	public static final String CMD_UPDATE = "proxy update";
	public static final String CMD_DELETE = "proxy delete";
	public static final String CMD_LIST = "proxy list";
	public static final String CMD_INFO = "proxy info";
	
	public static final String OUT_CREATE = "Smart proxy created";
	public static final String OUT_DELETE = "Smart proxy deleted";
	public static final String OUT_UPDATE = "Smart proxy updated";
	
	public static final String ERR_DUPLICATE = "Could not create the proxy:"+"\n"+"  URL Only one declaration of a proxy is allowed";
	public static final String ERR_CREATE = "Could not create the proxy:";
	public static final String ERR_DELETE = "Could not delete the proxy:"+"\n"+"  404 Resource Not Found";
	public static final String ERR_UPDATE = "Could not update the proxy:"+"\n"+"  404 Resource Not Found";
	public static final String ERR_404 = "404 Resource Not Found";
	
	// ** ** ** ** ** ** ** Class members
		public String name;
		public String id;
		public String url;
		
		public HammerProxy(){super();}
		
		public HammerProxy(KatelloCliWorker kcr, String pName, String pUrl)
		{
			this.kcr = kcr;
			this.name = pName;
			this.url = pUrl;
		}
		
		public SSHCommandResult cli_create() {
			args.clear();
			args.add(new Attribute("name", this.name));
			args.add(new Attribute("url", this.url));
			return run(CMD_CREATE);
		}
		
		public SSHCommandResult cli_info(String id) {
			args.clear();
			args.add(new Attribute("id", id));
			if(id == null)
				args.add(new Attribute("name", this.name));
			return run(CMD_INFO);
		}
		
		public SSHCommandResult update(String newName, String url) {
			args.clear();
			args.add(new Attribute("name", this.name));
			args.add(new Attribute("new-name", newName));
			args.add(new Attribute("url", url));
			return run(CMD_UPDATE);
		}
		
		public SSHCommandResult cli_list(String searchStr, String order, String page) {
			args.clear();
			args.add(new Attribute("search", searchStr));
			args.add(new Attribute("order", order));
			args.add(new Attribute("page", page));
			return run(CMD_LIST);
		}
		
		public SSHCommandResult delete(String id) {
			args.clear();
			args.add(new Attribute("id", this.id));
			if(id == null)
				args.add(new Attribute("name", this.name));
			
			return run(CMD_DELETE);
		}
}
