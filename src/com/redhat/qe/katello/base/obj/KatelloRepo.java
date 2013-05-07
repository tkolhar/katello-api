package com.redhat.qe.katello.base.obj;

import javax.management.Attribute;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloRepo extends _KatelloObject{
	
	// ** ** ** ** ** ** ** Public constants
	// Red Hat Enterprise Linux 6 Server RPMs x86_64 6Server
	public static final String RH_REPO_PRODUCT_VER = "6Server";
	public static final String RH_REPO_RHEL6_SERVER_RPMS_64BIT = 
			"Red Hat Enterprise Linux 6 Server RPMs x86_64 "+RH_REPO_PRODUCT_VER;
	
	public static final String CMD_CREATE = "repo create";
	public static final String CMD_SYNCHRONIZE = "repo synchronize";
	public static final String CMD_UPDATE = "repo update";
	public static final String CMD_DELETE = "repo delete";
	public static final String CMD_INFO = "repo info";
	public static final String CMD_ENABLE = "repo enable";
	public static final String CMD_DISABLE = "repo disable";
	public static final String CMD_DISCOVER = "repo discover";
	public static final String CMD_ADD_FILTER = "repo add_filter";
	public static final String CMD_REMOVE_FILTER = "repo remove_filter";
	public static final String CMD_FILTER_LIST = "repo list_filters";
	public static final String CMD_STATUS = "repo status";
	public static final String CMD_LIST = "repo list -v";
	
	public static final String OUT_CREATE = 
			"Successfully created repository [ %s ]";
	public static final String OUT_DISCOVER = 
			"Successfully created repository [ %s ]";
	public static final String ERR_REPO_NOTFOUND = 
			"Could not find repository [ %s ] within organization [ %s ], product [ %s ] and environment [ %s ]";	
	public static final String ERR_REPO_EXISTS = "There is already a repo with the name [ %s ] for product [ %s ]";
	public static final String ERR_LABEL_EXISTS = "Label has already been taken";
	public static final String OUT_FILTER_ADDED = 
			"Added filter [ %s ] to repository [ %s ]";
	public static final String OUT_REPO_SYNCHED = "Repo [ %s ] synchronized";
	
	public static final String REG_REPO_INFO = ".*ID\\s*:\\s+\\d+.*Name\\s*:\\s+%s.*URL\\s*:\\s+%s.*Last Sync\\s*:\\s+%s.*GPG Key\\s*:\\s*+%s.*";
	public static final String REG_REPO_STATUS = ".*Package Count\\s*:\\s+\\d+.*Last Sync\\s*:\\s+%s.*";
	public static final String REG_FILTER_LIST = ".*\\s*%s.*\\s+%s.*";
	public static final String REG_REPO_LIST = ".*ID\\s*:\\s+\\d+.*Name\\s*:\\s+%s.*Package Count\\s*:\\s+\\d+.*Last Sync\\s*:\\s+%s.*";
	public static final String REG_REPO_LIST_ARCH = ".*ID\\s*:\\s+\\d+.*Name\\s*:\\s+%s_.*_%s.*";
	public static final String REG_REPO_LASTSYNC = "\\d{4}/\\d{2}/\\d{2}\\s\\d{2}:\\d{2}:\\d{2}";
	public static final String REG_PACKAGE_CNT = ".*Package Count\\s+:\\s+%s.*";

	// ** ** ** ** ** ** ** Class members
	public String name;
	public String org;
	public String product;
	public String product_label;
	public String product_id;
	public String url;
	public String gpgkey;
	public String progress;
	public String lastSync;
	public boolean nogpgkey = false;
	
	public KatelloRepo(String pName, String pOrg, 
			String pProd, String pUrl, 
			String pGpgkey, Boolean pNogpgkey){
		this.name = pName;
		this.org = pOrg;
		this.product = pProd;
		this.url = pUrl;
		this.gpgkey = pGpgkey;
		if(pNogpgkey != null)
			this.nogpgkey = pNogpgkey.booleanValue();
	}
	
	public KatelloRepo(String pName, String pOrg, 
			String pProd, String pUrl, 
			String pGpgkey, Boolean pNogpgkey, String product_label, String product_id){
		this(pName, pOrg, pProd,pUrl, pGpgkey, pNogpgkey);
		this.product_label = product_label;
		this.product_id = product_id;
	}

	public SSHCommandResult create(){		
		return create(false);
	}

	public SSHCommandResult create(boolean unprotected){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("product", product));
		opts.add(new Attribute("url", url));
		opts.add(new Attribute("gpgkey", gpgkey));
		opts.add(new Attribute("product_label", product_label));
		opts.add(new Attribute("product_id", product_id));
		if(nogpgkey)
			opts.add(new Attribute("nogpgkey", ""));	
		if (unprotected)
			opts.add(new Attribute("unprotected", "true"));
		return run(CMD_CREATE);
	}

	public SSHCommandResult delete(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("product", product));
		opts.add(new Attribute("product_label", product_label));
		opts.add(new Attribute("product_id", product_id));
		return run(CMD_DELETE);
	}
	
	public SSHCommandResult list_filters(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("product", product));
		opts.add(new Attribute("product_label", product_label));
		opts.add(new Attribute("product_id", product_id));
		return run(CMD_FILTER_LIST);
	}
	
	public SSHCommandResult synchronize(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("product", product));
		opts.add(new Attribute("product_label", product_label));
		opts.add(new Attribute("product_id", product_id));
		return run(CMD_SYNCHRONIZE);
	}
	
	public SSHCommandResult update_gpgkey(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("product", product));
		opts.add(new Attribute("gpgkey", gpgkey));
		opts.add(new Attribute("product_label", product_label));
		opts.add(new Attribute("product_id", product_id));
		return run(CMD_UPDATE);
	}
	
	public SSHCommandResult info(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("product", product));
		opts.add(new Attribute("product_label", product_label));
		opts.add(new Attribute("product_id", product_id));
		return run(CMD_INFO);
	}
	
	public SSHCommandResult info(String environment){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("product", product));
		opts.add(new Attribute("environment", environment));
		opts.add(new Attribute("product_label", product_label));
		opts.add(new Attribute("product_id", product_id));
		return run(CMD_INFO);
	}

	public SSHCommandResult enable(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("product", product));
		opts.add(new Attribute("product_id", product_id));
		return run(CMD_ENABLE);
	}
	
	public SSHCommandResult disable(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("product", product));
		opts.add(new Attribute("product_id", product_id));
		return run(CMD_DISABLE);
	}
	
	public SSHCommandResult add_filter(String filter){
		opts.clear();
		opts.add(new Attribute("filter", filter));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("product", product));
		opts.add(new Attribute("product_label", product_label));
		opts.add(new Attribute("product_id", product_id));
		return run(CMD_ADD_FILTER);
	}
	
	public SSHCommandResult remove_filter(String filter){
		opts.clear();
		opts.add(new Attribute("filter", filter));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("product", product));
		opts.add(new Attribute("product_label", product_label));
		opts.add(new Attribute("product_id", product_id));
		return run(CMD_REMOVE_FILTER);
	}
	
	public SSHCommandResult status(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("product", product));
		opts.add(new Attribute("product_label", product_label));
		opts.add(new Attribute("product_id", product_id));
		return run(CMD_STATUS);
	}

	public SSHCommandResult status(String environment){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("product", product));
		opts.add(new Attribute("environment", environment));
		opts.add(new Attribute("product_label", product_label));
		opts.add(new Attribute("product_id", product_id));
		return run(CMD_STATUS);
	}

	
	public SSHCommandResult discover(String provider){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("product", product));
		opts.add(new Attribute("url", url));
		opts.add(new Attribute("assumeyes", "y"));
		opts.add(new Attribute("product_label", product_label));
		opts.add(new Attribute("product_id", product_id));
		opts.add(new Attribute("provider", provider));
		return run(CMD_DISCOVER);
	}
	public SSHCommandResult list(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("product", product));
		opts.add(new Attribute("product_label", product_label));
		opts.add(new Attribute("product_id", product_id));
		return run(CMD_LIST);
	}

	public SSHCommandResult list(String environment){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("environment", environment));
		opts.add(new Attribute("product", product)); // gkhachik - added, seems was missing.
		opts.add(new Attribute("product_label", product_label));
		opts.add(new Attribute("product_id", product_id));
		return run(CMD_LIST);
	}
	
	public SSHCommandResult custom_reposCount(String environment){
		opts.clear();
		if(environment == null) 
			environment = KatelloEnvironment.LIBRARY;
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("environment", environment));
		opts.add(new Attribute("product", product));
		opts.add(new Attribute("product_label", product_label));
		opts.add(new Attribute("product_id", product_id));
		return runExt(CMD_LIST, " | grep -e \"^Name.*\\:\" | wc -l"); // -v option here in the command is really important
	}

	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **
	
	public void assert_repoHasGpg(){
		SSHCommandResult res;
		
		res = info();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo info)");
		String gpg_key = KatelloCli.grepCLIOutput("GPG Key", res.getStdout());
		Assert.assertTrue(this.gpgkey.equals(gpg_key), 
				String.format("Check - GPG Key [%s] should be found in the repo info",this.gpgkey));
		KatelloGpgKey gpg = new KatelloGpgKey(this.gpgkey, this.org, null);
		res = gpg.cli_info();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (gpg_key info)");
		String reposWithGpg = KatelloCli.grepCLIOutput("Repositories", res.getStdout());
		Assert.assertTrue(reposWithGpg.contains(this.name), 
				"Check - Repo should be in repositories list of GPG Key");
	}

	public static final String FEDORA_VER16 = "16";
	public static final String FEDORA_VER17 = "17";
	public static String getFedoraMirror(String version){
		String domain="";
		
		String lab_controller = KatelloUtils.sshOnServer("echo ${LAB_CONTROLLER}").getStdout().trim();
		if(lab_controller.equals("")) lab_controller = "lab.rhts.englab.brq.redhat.com";
		
		if(lab_controller.contains("eng.bos.redhat.com"))
			domain = "download.bos.redhat.com";
		else if(lab_controller.equals("eng.nay.redhat.com"))
			domain = "download.eng.nay.redhat.com";
		else if(lab_controller.equals("eng.pnq.redhat.com"))
			domain = "download.eng.pnq.redhat.com";
		else if(lab_controller.equals("eng.tlv.redhat.com"))
			domain = "download.eng.tlv.redhat.com";
		else 
			domain = "download.eng.brq.redhat.com";
			
		String _url = "http://"+domain+"/pub/fedora/linux/releases/"+version+"/Fedora/x86_64/os/";
		return _url;
	}

}
