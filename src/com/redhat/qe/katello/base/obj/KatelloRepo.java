package com.redhat.qe.katello.base.obj;

import javax.management.Attribute;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCli;
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
	public static final String OUT_FILTER_ADDED = 
			"Added filter [ %s ] to repository [ %s ]";
	public static final String OUT_REPO_SYNCHED = "Repo [ %s ] synced";
	
	public static final String REG_REPO_INFO = ".*Id\\s*:\\s+\\d+.*Name\\s*:\\s+%s.*Url\\s*:\\s+%s.*Last Sync\\s*:\\s+%s.*Progress\\s*:\\s+%s.*GPG key\\s*:\\s*+%s.*";
	public static final String REG_REPO_STATUS = ".*Package Count\\s*:\\s+\\d+.*Last Sync\\s*:\\s+%s.*Sync State\\s*:\\s+%s.*";
	public static final String REG_FILTER_LIST = ".*\\s*%s.*\\s+%s.*";
	public static final String REG_REPO_LIST = ".*Id\\s*:\\s+\\d+.*Name\\s*:\\s+%s.*Package Count\\s*:\\s+\\d+.*Last Sync\\s*:\\s+%s.*";
	
	public static final String REG_REPO_LASTSYNC = "\\d{4}/\\d{2}/\\d{2}\\s\\d{2}:\\d{2}:\\d{2}";

	// ** ** ** ** ** ** ** Class members
	public String name;
	public String org;
	public String product;
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
	
	public SSHCommandResult create(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("product", product));
		opts.add(new Attribute("url", url));
		opts.add(new Attribute("gpgkey", gpgkey));
		return run(CMD_CREATE);
	}	

	public SSHCommandResult delete(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("product", product));
		return run(CMD_DELETE);
	}
	
	public SSHCommandResult list_filters(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("product", product));
		return run(CMD_FILTER_LIST);
	}
	
	public SSHCommandResult synchronize(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("product", product));
		return run(CMD_SYNCHRONIZE);
	}
	
	public SSHCommandResult update_gpgkey(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("product", product));
		opts.add(new Attribute("gpgkey", gpgkey));
		return run(CMD_UPDATE);
	}
	
	public SSHCommandResult info(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("product", product));
		return run(CMD_INFO);
	}
	
	public SSHCommandResult info(String environment){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("product", product));
		opts.add(new Attribute("environment", environment));
		return run(CMD_INFO);
	}

	public SSHCommandResult enable(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("product", product));
		return run(CMD_ENABLE);
	}
	
	public SSHCommandResult disable(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("product", product));
		return run(CMD_DISABLE);
	}
	
	public SSHCommandResult add_filter(String filter){
		opts.clear();
		opts.add(new Attribute("filter", filter));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("product", product));
		return run(CMD_ADD_FILTER);
	}
	
	public SSHCommandResult remove_filter(String filter){
		opts.clear();
		opts.add(new Attribute("filter", filter));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("product", product));
		return run(CMD_REMOVE_FILTER);
	}
	
	public SSHCommandResult status(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("product", product));
		return run(CMD_STATUS);
	}

	public SSHCommandResult status(String environment){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("product", product));
		opts.add(new Attribute("environment", environment));
		return run(CMD_STATUS);
	}

	
	public SSHCommandResult discover(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("product", product));
		opts.add(new Attribute("url", url));
		opts.add(new Attribute("assumeyes", "y"));
		return run(CMD_DISCOVER);
	}
	public SSHCommandResult list(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("product", product));
		return run(CMD_LIST);
	}

	public SSHCommandResult list(String environment){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("environment", environment));
		return run(CMD_LIST);
	}

	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **
	
	public void assert_repoHasGpg(){
		SSHCommandResult res;
		
		res = info();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo info)");
		String gpg_key = KatelloCli.grepCLIOutput("GPG key", res.getStdout());
		Assert.assertTrue(this.gpgkey.equals(gpg_key), 
				String.format("Check - GPG key [%s] should be found in the repo info",this.gpgkey));
		KatelloGpgKey gpg = new KatelloGpgKey(this.gpgkey, this.org, null);
		res = gpg.cli_info();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (gpg_key info)");
		String reposWithGpg = KatelloCli.grepCLIOutput("Repositories", res.getStdout());
		Assert.assertTrue(reposWithGpg.contains(this.name), 
				"Check - Repo should be in repositories list of GPG key");
	}
	
}
