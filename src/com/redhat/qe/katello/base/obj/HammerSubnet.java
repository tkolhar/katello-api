package com.redhat.qe.katello.base.obj;

import java.util.logging.Logger;

import javax.management.Attribute;

import com.redhat.qe.katello.base.threading.KatelloCliWorker;
import com.redhat.qe.tools.SSHCommandResult;

public class HammerSubnet extends _HammerObject{
    protected static Logger log = Logger.getLogger(HammerSubnet.class.getName());

	// ** ** ** ** ** ** ** Public constants
	public static final String CLI_CMD_CREATE = "subnet create";
	public static final String CLI_CMD_INFO = "subnet info";
	public static final String CLI_CMD_LIST = "subnet list";
	public static final String CMD_DELETE = "subnet delete";
	public static final String CMD_UPDATE = "subnet update";
	
	public static final String OUT_CREATE = 
			"Subnet created";
	public static final String OUT_UPDATE = 
			"Subnet [ %s ] updated.";
	public static final String OUT_DELETE = 
			"Subnet [ %s ] deleted.";
	
	public static final String ERR_NAME_EXISTS = 
			"Name has already been taken";
	public static final String ERR_NOT_FOUND =
			"Subnet with id '%s' not found";
	
	public static final String REG_SUBNET_INFO = ".*Id\\s*:\\s+\\d+.*Name\\s*:\\s+%s.*Network\\s*:\\s+%s.*Mask\\s*:\\s+%s.*";
	
	// ** ** ** ** ** ** ** Class members
	public String name;
	
	public String network;
	public String mask;
	public String gateway;
	public String dns_primary;
	public String dns_secondary;
	public String from;
	public String to;
	public String vlanid;
	public String domain_id;
	public String dhcp_id;
	public String tftp_id;
	public String dns_id;
	
	public HammerSubnet(){super();}
	
	public HammerSubnet(KatelloCliWorker kcr, String pName, String network, String mask) {
		this.name = pName;
		this.kcr = kcr;
		this.network = network;
		this.mask = mask;
	}
	
	public String getName() {
	    return name;
	}
	
	public void setName(String name) {
	    this.name = name;
	}

	public String getNetwork() {
		return network;
	}

	public void setNetwork(String network) {
		this.network = network;
	}

	public String getMask() {
		return mask;
	}

	public void setMask(String mask) {
		this.mask = mask;
	}

	public String getGateway() {
		return gateway;
	}

	public void setGateway(String gateway) {
		this.gateway = gateway;
	}

	public String getDns_primary() {
		return dns_primary;
	}

	public void setDns_primary(String dns_primary) {
		this.dns_primary = dns_primary;
	}

	public String getDns_secondary() {
		return dns_secondary;
	}

	public void setDns_secondary(String dns_secondary) {
		this.dns_secondary = dns_secondary;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getVlanid() {
		return vlanid;
	}

	public void setVlanid(String vlanid) {
		this.vlanid = vlanid;
	}

	public String getDomain_id() {
		return domain_id;
	}

	public void setDomain_id(String domain_id) {
		this.domain_id = domain_id;
	}

	public String getDhcp_id() {
		return dhcp_id;
	}

	public void setDhcp_id(String dhcp_id) {
		this.dhcp_id = dhcp_id;
	}

	public String getTftp_id() {
		return tftp_id;
	}

	public void setTftp_id(String tftp_id) {
		this.tftp_id = tftp_id;
	}

	public String getDns_id() {
		return dns_id;
	}

	public void setDns_id(String dns_id) {
		this.dns_id = dns_id;
	}

	public SSHCommandResult cli_create(){		
		args.clear();
		args.add(new Attribute("name", this.name));
		args.add(new Attribute("network", this.network));
		args.add(new Attribute("mask", this.mask));
		args.add(new Attribute("gateway", this.gateway));
		args.add(new Attribute("dns-primary", this.dns_primary));
		args.add(new Attribute("dns-secondary", this.dns_secondary));
		args.add(new Attribute("from", this.from));
		args.add(new Attribute("to", this.to));
		args.add(new Attribute("vlanid", this.vlanid));
		args.add(new Attribute("domain-ids", this.domain_id));
		args.add(new Attribute("dhcp-id", this.dhcp_id));
		args.add(new Attribute("tftp-id", this.tftp_id));
		args.add(new Attribute("dns-id", this.dns_id));
		return run(CLI_CMD_CREATE);
	}
	
	public SSHCommandResult cli_info(){
		args.clear();
		args.add(new Attribute("name", this.name));
		return run(CLI_CMD_INFO);
	}
	
	public SSHCommandResult cli_list(){
		args.clear();
		return run(CLI_CMD_LIST);
	}

	public SSHCommandResult cli_search(String search){
		args.clear();
		args.add(new Attribute("search", search));
		return run(CLI_CMD_LIST);
	}
	
	public SSHCommandResult cli_list(String order, Integer page, Integer per_page){
		args.clear();
		args.add(new Attribute("order", order));
		args.add(new Attribute("page", page));
		args.add(new Attribute("per-page", per_page));
		return run(CLI_CMD_LIST);
	}
	
	public SSHCommandResult delete(){
		args.clear();
		args.add(new Attribute("name", this.name));
		return run(CMD_DELETE);
	}
	
	public SSHCommandResult update(String new_name){
		args.clear();
		args.add(new Attribute("name", this.name));
		args.add(new Attribute("new-name", new_name));
		args.add(new Attribute("network", this.network));
		args.add(new Attribute("mask", this.mask));
		args.add(new Attribute("gateway", this.gateway));
		args.add(new Attribute("dns-primary", this.dns_primary));
		args.add(new Attribute("dns-secondary", this.dns_secondary));
		args.add(new Attribute("from", this.from));
		args.add(new Attribute("to", this.to));
		args.add(new Attribute("vlanid", this.vlanid));
		args.add(new Attribute("domain-ids", this.domain_id));
		args.add(new Attribute("dhcp-id", this.dhcp_id));
		args.add(new Attribute("tftp-id", this.tftp_id));
		args.add(new Attribute("dns-id", this.dns_id));
		return run(CMD_UPDATE);
	}


	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **
}
