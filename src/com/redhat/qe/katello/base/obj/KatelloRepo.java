package com.redhat.qe.katello.base.obj;

import javax.management.Attribute;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.threading.KatelloCliWorker;
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
	public static final String CMD_STATUS = "repo status";
	public static final String CMD_LIST = "repo list -v";
	public static final String CMD_CANCEL_SYNC = "repo cancel_sync";
	public static final String CMD_CONTENT_UPLOAD = "repo content_upload";
	
	public static final String OUT_CREATE = 
			"Successfully created repository [ %s ]";
	public static final String OUT_DISCOVER = 
			"Successfully created repository [ %s ]";
	public static final String ERR_REPO_NOTFOUND = 
			"Could not find repository [ %s ] within organization [ %s ], product [ %s ] and environment [ %s ]";	
	public static final String ERR_REPO_EXISTS = "There is already a repo with the name [ %s ] for product [ %s ]";
	public static final String ERR_LABEL_EXISTS = "Label has already been taken";
	public static final String OUT_REPO_SYNCHED = "Repo [ %s ] synchronized";
	public static final String OUT_NO_SYNC_RUNNIG = "No synchronization is currently running";
	public static final String OUT_SYNC_CANCELLED = "Synchronization cancelled";
	public static final String OUT_REPO_SYNC_CANCELLED = "Repo [ %s ] synchronization canceled";
	public static final String OUT_REPO_ENABLED = "Repository '%s' enabled.";
	public static final String OUT_REPO_DISABLED = "Repository '%s' disabled.";
	public static final String OUT_CONTENT_UPLOADED = "Successfully uploaded '%s' into repository";
	
	public static final String ERR_REPO_SYNC_FAIL = "Repo [ %s ] failed to sync:";
	public static final String ERR_INVALID_MODULE = "Invalid puppet module '%s'. Please make sure the file is valid and is named author-name-version.tar.gz";
	public static final String ERR_INVALID_RPM = "Invalid rpm '%s'. Please check the file and try again.";
	public static final String ERR_INVALID_TYPE = "Content type '%s' not valid. Must be one of: yum, puppet.";
	public static final String ERR_NOT_ACCEPT_PUPPET = "Repo [ %s ] does not accept puppet uploads.";
	public static final String ERR_NOT_ACCEPT_YUM = "Repo [ %s ] does not accept yum uploads.";

	public static final String REG_REPO_INFO = ".*ID\\s*:\\s+\\d+.*Name\\s*:\\s+%s.*URL\\s*:\\s+%s.*Last Sync\\s*:\\s+%s.*GPG Key\\s*:\\s*+%s.*";
	public static final String REG_REPO_STATUS = ".*Package Count\\s*:\\s+\\d+.*Last Sync\\s*:\\s+%s.*";
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
	public String lastSync;
	public boolean nogpgkey = false;
	public String repo_id;
	public String content_type;
	
	public KatelloRepo(KatelloCliWorker kcr, String pName, String pOrg, 
			String pProd, String pUrl, 
			String pGpgkey, Boolean pNogpgkey){
		this.name = pName;
		this.org = pOrg;
		this.product = pProd;
		this.url = pUrl;
		this.gpgkey = pGpgkey;
		this.kcr = kcr;
		
		if(pNogpgkey != null)
			this.nogpgkey = pNogpgkey.booleanValue();
	}
	
	public KatelloRepo(KatelloCliWorker kcr,
			String pName, String pOrg, 
			String pProd, String pUrl, 
			String pGpgkey, Boolean pNogpgkey, String product_label, String product_id){
		this(kcr, pName, pOrg, pProd,pUrl, pGpgkey, pNogpgkey);
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
		opts.add(new Attribute("content_type", content_type));
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

	public SSHCommandResult update_nogpgkey(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("product", product));
		opts.add(new Attribute("nogpgkey", ""));
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
	
	public SSHCommandResult info(String environment, String contentview){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("product", product));
		opts.add(new Attribute("environment", environment));
		opts.add(new Attribute("content_view", contentview));
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
//		opts.add(new Attribute("provider", provider));
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

	public SSHCommandResult list(String environment, String contentview){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("environment", environment));
		opts.add(new Attribute("content_view", contentview));
		opts.add(new Attribute("product", product)); // gkhachik - added, seems was missing.
		opts.add(new Attribute("product_label", product_label));
		opts.add(new Attribute("product_id", product_id));
		return run(CMD_LIST);
	}
	
	public SSHCommandResult listAll(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("product", product));
		opts.add(new Attribute("include_disabled", ""));
		return run(CMD_LIST);
	}

	public SSHCommandResult custom_reposCount(){
		return custom_reposCount(null, null, null);
	}
	
	public SSHCommandResult custom_reposCount(String environment, String contentview, Boolean includeDisabled){
		opts.clear();
		if(environment == null) 
			environment = KatelloEnvironment.LIBRARY;
		if(includeDisabled!=null && includeDisabled)
			opts.add(new Attribute("include_disabled", ""));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("environment", environment));
		opts.add(new Attribute("content_view", contentview));
		opts.add(new Attribute("product", product));
		opts.add(new Attribute("product_label", product_label));
		opts.add(new Attribute("product_id", product_id));
		return runExt(CMD_LIST, " | grep -e \"^Name.*\\:\" | wc -l"); // -v option here in the command is really important
	}
	
	public SSHCommandResult custom_repoListByRegexp(String regexp, boolean includeDisabled){
		opts.clear();
		if(includeDisabled) 
			opts.add(new Attribute("include_disabled", ""));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("product", product));
		opts.add(new Attribute("product_label", product_label));
		opts.add(new Attribute("product_id", product_id));
		return runExt(CMD_LIST, " | grep -E \""+regexp+"\" | cut -f2 -d:"); // there would be a leading space for each line there. take care to trim() it.
	}

	public SSHCommandResult cancel_sync() {
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("product", product));
		opts.add(new Attribute("name", name));
		return run(CMD_CANCEL_SYNC);
	}

	public SSHCommandResult content_upload(String filePath, String contentType, String chunk) {
		opts.clear();
		if(repo_id != null) {
			opts.add(new Attribute("repo_id", repo_id));
		} else {
			opts.add(new Attribute("org", org));
			opts.add(new Attribute("product", product));
			opts.add(new Attribute("repo", name));
		}
		opts.add(new Attribute("filepath", filePath));
		opts.add(new Attribute("content_type", contentType));
		opts.add(new Attribute("chunk", chunk));
		return run(CMD_CONTENT_UPLOAD);
	}

	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **
	
	public void assert_repoHasGpg(){
		SSHCommandResult res;
		
		res = info();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo info)");
		String gpg_key = KatelloUtils.grepCLIOutput("GPG Key", res.getStdout());
		Assert.assertTrue(this.gpgkey.equals(gpg_key), 
				String.format("Check - GPG Key [%s] should be found in the repo info",this.gpgkey));
		KatelloGpgKey gpg = new KatelloGpgKey(this.kcr, this.gpgkey, this.org, null);
		res = gpg.cli_info();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (gpg_key info)");
		String reposWithGpg = KatelloUtils.grepCLIOutput("Repositories", res.getStdout());
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
