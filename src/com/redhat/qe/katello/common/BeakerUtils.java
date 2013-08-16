package com.redhat.qe.katello.common;

import com.redhat.qe.tools.SSHCommandResult;

public class BeakerUtils {

	public static SSHCommandResult Katello_Installation_RegisterRHNClassic(String hostname){
		String cmds = 
				"yum install -y Katello-Katello-Installation-RegisterRHNClassic --disablerepo=* --enablerepo=beaker*; " +
				"cd /mnt/tests/Katello/Installation/RegisterRHNClassic/; make run";
		return KatelloUtils.sshOnClient(hostname, cmds);
	}
	
	public static SSHCommandResult Katello_Sanity_ImportKeys(String hostname){
		String cmds = 
				"yum install -y Katello-Katello-Sanity-ImportKeys --disablerepo=* --enablerepo=beaker*; " +
				"cd /mnt/tests/Katello/Sanity/ImportKeys/; make run";
		return KatelloUtils.sshOnClient(hostname, cmds);
	}

	public static SSHCommandResult Katello_Configuration_KatelloClient(String hostname, String servername, String releaseVersion, String product){
		String cmds = 
				"yum install -y Katello-Katello-Configuration-KatelloClient --disablerepo=* --enablerepo=beaker*; " +
				"cd /mnt/tests/Katello/Configuration/KatelloClient/; " +
				"export KATELLO_SERVER_HOSTNAME="+servername+"; " +
				"export KATELLO_PRODUCT="+product+"; " +
				"export KATELLO_RELEASE="+releaseVersion+"; make run";
		return KatelloUtils.sshOnClient(hostname, cmds);
	}
	
	public static void install_CandlepinCert(String hostname, String servername){
		KatelloUtils.sshOnClient(hostname, "yum -y update subscription-manager python-rhsm --disablerepo=\\*beaker\\*");
		KatelloUtils.sshOnClient(hostname, "wget http://" + servername + "/pub/candlepin-cert-consumer-" + servername + "-1.0-1.noarch.rpm -O /tmp/candlepin-cert-consumer-" + servername + "-1.0-1.noarch.rpm");
		KatelloUtils.sshOnClient(hostname, "yum -y --nogpgcheck localinstall /tmp/candlepin-cert-consumer-" + servername + "-1.0-1.noarch.rpm");
	}
	
	public static SSHCommandResult Katello_Installation_ConfigureRepos(String hostname){
		String cmds = 
				"yum install -y Katello-Katello-Installation-ConfigureRepos --disablerepo=* --enablerepo=beaker*; " +
				"cd /mnt/tests/Katello/Installation/ConfigureRepos/; make run";
		return KatelloUtils.sshOnClient(hostname, cmds);
	}

	public static SSHCommandResult Katello_Installation_KatelloNightly(String hostname){
		String cmds = 
				"yum install -y Katello-Katello-Installation-KatelloNightly --disablerepo=* --enablerepo=beaker*; " +
				"cd /mnt/tests/Katello/Installation/KatelloNightly/; make run";
		return KatelloUtils.sshOnClient(hostname, cmds);
	}
	
	public static SSHCommandResult Katello_Installation_KatelloWithLdap(String hostname, String ldap_type, String user, String password) {
		String cmds = 
				"yum install -y Katello-Katello-Installation-KatelloWithLdap --disablerepo=* --enablerepo=beaker*; " +
				"cd /mnt/tests/Katello/Installation/KatelloWithLdap/; " +
				"export LDAP_SERVER_TYPE=" + ldap_type + "; export LDAP_USERNAME=" + user + "; export LDAP_PASSWORD=" + password + "; make run";
		return KatelloUtils.sshOnClient(hostname, cmds);
	}
	
	public static SSHCommandResult Katello_Installation_SystemEngineLatest(String hostname, String releaseVersion){
		String cmds = 
				"yum install -y Katello-Katello-Installation-SystemEngineLatest --disablerepo=* --enablerepo=beaker*; " +
				"cd /mnt/tests/Katello/Installation/SystemEngineLatest/; " +
				"export CFSE_RELEASE=" + releaseVersion + "; make run";
		return KatelloUtils.sshOnClient(hostname, cmds);
	}
	
	public static SSHCommandResult Katello_Installation_Satellite6Latest(String hostname, String releaseVersion){
		
		String sat6Url = System.getProperty("SAT6_URL",null);
		String sat6ToolsUrl = System.getProperty("SAT6_TOOLS_URL",null);
		String cmds = 
				"yum install -y Katello-Katello-Installation-Satellite6Latest --disablerepo=\\* --enablerepo=\\*beaker\\*; " +
				"cd /mnt/tests/Katello/Installation/Satellite6Latest/";
		if(sat6Url!=null)
			cmds +="; export SAT6_URL="+sat6Url;
		if(sat6ToolsUrl!=null)
			cmds +="; export SAT6_TOOLS_URL="+sat6ToolsUrl;
		cmds += "; export SAT6_RELEASE=" + releaseVersion + "; make run";
		return KatelloUtils.sshOnClient(hostname, cmds);
	}

	public static SSHCommandResult Katello_Installation_Satellite6WithLdap(String hostname, String releaseVersion, String ldap_type, String user, String password) {
		String sat6Url = System.getProperty("SAT6_URL",null);
		String sat6ToolsUrl = System.getProperty("SAT6_TOOLS_URL",null);
		String cmds = 
				"yum install -y Katello-Katello-Installation-Satellite6WithLdap --disablerepo=\\* --enablerepo=\\*beaker\\*; " +
				"cd /mnt/tests/Katello/Installation/Satellite6WithLdap/";
		if(sat6Url!=null)
			cmds +="; export SAT6_URL="+sat6Url;
		if(sat6ToolsUrl!=null)
			cmds +="; export SAT6_TOOLS_URL="+sat6ToolsUrl;
		cmds += "; export SAT6_RELEASE=" + releaseVersion + "; export LDAP_SERVER_TYPE=" + ldap_type + "; export LDAP_USERNAME=" + user + "; export LDAP_PASSWORD=" + password + "; make run";

		return KatelloUtils.sshOnClient(hostname, cmds);
	}
	
	public static SSHCommandResult Katello_Installation_SAMLatest(String hostname, String releaseVersion){
		String cmds = 
				"yum install -y Katello-Katello-Installation-SAMLatest --disablerepo=* --enablerepo=beaker*; " +
				"cd /mnt/tests/Katello/Installation/SAMLatest/; " +
				"export SAM_RELEASE=" + releaseVersion + "; make run";
		return KatelloUtils.sshOnClient(hostname, cmds);
	}

	public static SSHCommandResult Katello_Installation_SAMLatestWithLdap(String hostname, String releaseVersion, String ldap_type, String user, String password){
		String samUrl = System.getProperty("SAM_INSTALL_URL",null);
		String cmds = 
				"yum install -y Katello-Katello-Installation-SAMLatestWithLdap --disablerepo=* --enablerepo=beaker*; " +
				"cd /mnt/tests/Katello/Installation/SAMLatestWithLdap/; " +
				"export SAM_RELEASE=" + releaseVersion + "; export LDAP_SERVER_TYPE=" + ldap_type + "; export LDAP_USERNAME=" + user + "; export LDAP_PASSWORD=" + password;
		if (samUrl != null) {
			cmds += "; export SAM_INSTALL_URL=" + samUrl; 
		}
		cmds += "; make run";
		return KatelloUtils.sshOnClient(hostname, cmds);
	}
	
	public static SSHCommandResult Katello_Installation_HeadpinNightly(String hostname){
		String cmds = 
				"yum install -y Katello-Katello-Installation-HeadpinNightly --disablerepo=* --enablerepo=beaker*; " +
				"cd /mnt/tests/Katello/Installation/HeadpinNightly/; make run";
		return KatelloUtils.sshOnClient(hostname, cmds);
	}
	
	public static SSHCommandResult Katello_Installation_HeadpinWithLdap(String hostname, String ldap_type, String user, String password) {
		String cmds = 
				"yum install -y Katello-Katello-Installation-HeadpinWithLdap --disablerepo=* --enablerepo=beaker*; " +
				"cd /mnt/tests/Katello/Installation/HeadpinWithLdap/; " +
				"export LDAP_SERVER_TYPE=" + ldap_type + "; export LDAP_USERNAME=" + user + "; export LDAP_PASSWORD=" + password + "; make run";
		return KatelloUtils.sshOnClient(hostname, cmds);
	}
}
