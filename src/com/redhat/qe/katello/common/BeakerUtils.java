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

	public static SSHCommandResult Katello_Configuration_KatelloClient(String hostname, String servername, String releaseVersion){
		String cmds = 
				"yum install -y Katello-Katello-Configuration-KatelloClient --disablerepo=* --enablerepo=beaker*; " +
				"cd /mnt/tests/Katello/Configuration/KatelloClient/; " +
				"export KATELLO_SERVER_HOSTNAME="+servername+"; " +
				"export KATELLO_RELEASE="+releaseVersion+"; make run";
		return KatelloUtils.sshOnClient(hostname, cmds);
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
	
	public static SSHCommandResult Katello_Installation_SystemEngineLatest(String hostname, String releaseVersion){
		String cmds = 
				"yum install -y Katello-Katello-Installation-SystemEngineLatest --disablerepo=* --enablerepo=beaker*; " +
				"cd /mnt/tests/Katello/Installation/SystemEngineLatest/; " +
				"export CFSE_RELEASE=" + releaseVersion + "; make run";
		return KatelloUtils.sshOnClient(hostname, cmds);
	}
	
	public static SSHCommandResult Katello_Installation_Satellite6Latest(String hostname, String releaseVersion){
		String cmds = 
				"yum install -y Katello-Katello-Installation-SystemEngineLatest --disablerepo=\\* --enablerepo=\\*beaker\\*; " +
				"cd /mnt/tests/Katello/Installation/Satellite6Latest/; " +
				"export SAT6_RELEASE=" + releaseVersion + "; make run";
		return KatelloUtils.sshOnClient(hostname, cmds);
	}

	public static SSHCommandResult Katello_Installation_SAMLatest(String hostname, String releaseVersion){
		String cmds = 
				"yum install -y Katello-Katello-Installation-SAMLatest --disablerepo=* --enablerepo=beaker*; " +
				"cd /mnt/tests/Katello/Installation/SAMLatest/; " +
				"export SAM_RELEASE=" + releaseVersion + "; make run";
		return KatelloUtils.sshOnClient(hostname, cmds);
	}

	public static SSHCommandResult Katello_Installation_HeadpinNightly(String hostname){
		String cmds = 
				"yum install -y Katello-Katello-Installation-HeadpinNightly --disablerepo=* --enablerepo=beaker*; " +
				"cd /mnt/tests/Katello/Installation/HeadpinNightly/; make run";
		return KatelloUtils.sshOnClient(hostname, cmds);
	}
}
