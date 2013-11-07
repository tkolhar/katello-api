package com.redhat.qe.katello.base.obj;

import java.util.logging.Logger;

import javax.management.Attribute;

import com.redhat.qe.katello.base.threading.KatelloCliWorker;
import com.redhat.qe.tools.SSHCommandResult;

public class HammerComputeResource extends _HammerObject {
	protected static Logger log = Logger.getLogger(HammerArchitecture.class.getName());
	
	 public enum Provider {Libvirt, oVirt, EC2, Vmware, Openstack, Rackspace, GCE, WRONG};
	
	// ** ** ** ** ** ** ** Public constants
	public static final String CMD_CREATE = "compute_resource create";
	public static final String CMD_UPDATE = "compute_resource update";
	public static final String CMD_DELETE = "compute_resource delete";
	public static final String CMD_LIST = "compute_resource list";
	public static final String CMD_INFO = "compute_resource info";
	
	public static final String OUT_CREATE = "Compute resource created";
	public static final String OUT_UPDATE = "Compute resource updated";
	public static final String OUT_DELETE = "Compute resource deleted";
	
	public static final String ERR_USER_MISS = "Username can't be blank";
	public static final String ERR_PASS_MISS = "Password can't be blank";
	public static final String ERR_UUID_MISS = "Datacenter can't be blank";
	public static final String ERR_CREATE = "Could not create the compute resource";
	public static final String ERR_NOT_FOUND = "404 Resource Not Found";
	
	public static final String REG_INFO = "Id\\s*:\\s+\\d+.*Name\\s*:\\s+%s.*Provider\\s*:\\s+%s.*Url\\s*:\\s+%s.*Description\\s*:\\s+%s.*User\\s*:\\s+%s.*UUID\\s*:\\s*%s.*";

	// ** ** ** ** ** ** ** Class members
	public String Id;
	public String name;
	public String provider;
	public String url;
	public String description;
	public String user;
	public String password;
	public String uuid;
	public String region;
	public String tenant;
	public String server;
	
	public HammerComputeResource(){super();}
	
	public HammerComputeResource(KatelloCliWorker kcr, String pId)
	{
		this.kcr = kcr;
		this.Id = pId;
	}
	
	public HammerComputeResource(KatelloCliWorker kcr, String pName, String pDescription, Provider provider, String url, String user, String password)
	{
		this.kcr = kcr;
		this.name = pName;
		this.description = pDescription;
		if (provider!= null) this.provider = provider.toString();
		this.url = url;
		this.user = user;
		this.password = password;
	}

	public String getName() {
	    return name;
	}

	public void setName(String name) {
	    this.name = name;
	}
	
	public SSHCommandResult cli_create() {
		args.clear();
		args.add(new Attribute("name", this.name));
		args.add(new Attribute("provider", this.provider));
		args.add(new Attribute("url", this.url));
		args.add(new Attribute("description", this.description));
		args.add(new Attribute("user", this.user));
		args.add(new Attribute("password", this.password));
		args.add(new Attribute("uuid", this.uuid));
		args.add(new Attribute("region", this.region));
		args.add(new Attribute("tenant", this.tenant));
		args.add(new Attribute("server", this.server));
		return run(CMD_CREATE);
	}
	
	public SSHCommandResult cli_info() {
		args.clear();
		if (this.Id != null) {
			args.add(new Attribute("id", this.Id));	
		} else {
			args.add(new Attribute("name", this.name));
		}
		return run(CMD_INFO);
	}

	public SSHCommandResult cli_search(String search) {
		args.clear();
		args.add(new Attribute("search", search));
		return run(CMD_LIST);
	}
	
	public SSHCommandResult cli_list() {
		args.clear();
		return run(CMD_LIST);
	}
	
	public SSHCommandResult cli_list(String order, Integer page, Integer per_page) {
		args.clear();
		args.add(new Attribute("order", order));
		args.add(new Attribute("page", page));
		args.add(new Attribute("per-page", per_page));
		return run(CMD_LIST);
	}
	
	public SSHCommandResult update(String newName) {
		args.clear();
		if (this.Id != null) {
			args.add(new Attribute("id", this.Id));	
		} else {
			args.add(new Attribute("name", this.name));
		}
		args.add(new Attribute("new-name", newName));
		args.add(new Attribute("provider", this.provider));
		args.add(new Attribute("url", this.url));
		args.add(new Attribute("description", this.description));
		args.add(new Attribute("user", this.user));
		args.add(new Attribute("password", this.password));
		args.add(new Attribute("uuid", this.uuid));
		args.add(new Attribute("region", this.region));
		args.add(new Attribute("tenant", this.tenant));
		args.add(new Attribute("server", this.server));
		return run(CMD_UPDATE);
	}
	
	public SSHCommandResult delete() {
		args.clear();
		if (this.Id != null) {
			args.add(new Attribute("id", this.Id));	
		} else {
			args.add(new Attribute("name", this.name));
		}
		return run(CMD_DELETE);
	}
}
