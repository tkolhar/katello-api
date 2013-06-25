package com.redhat.qe.katello.tests.i18n;

import java.io.File;
import java.util.logging.Logger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.tests.e2e.SystemsReport;
import com.redhat.qe.tools.SCPTools;
import com.redhat.qe.tools.SSHCommandResult;

public class ProviderTests extends KatelloCliTestBase {
	protected static Logger log = Logger.getLogger(ProviderTests.class.getName());
	
	private String uid;
	private String org_name;
	
	@BeforeClass(description="init - org", groups={"i18n-init"})
	public void init(){
		uid = KatelloUtils.getUniqueID();
		org_name = getText("org.create.name")+" "+uid;

		KatelloOrg org = new KatelloOrg(org_name, null);
		SSHCommandResult res = org.cli_create();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
	}
	
	@Test(description="provider create")
	public void createProvider(){
		String provider_name = getText("provider.create.name")+" "+uid;
		String provider_description = getText("provider.create.description");
		String outSuccess = getText("provider.create.stdout", provider_name);
		KatelloProvider prov = new KatelloProvider(provider_name, org_name, provider_description, null);
		SSHCommandResult res = prov.create();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(res).equals(outSuccess), "Check - stdout (provider create)");
	}
	
	@Test(description="provider info", dependsOnMethods={"createProvider"})
	public void infoProvider(){
		String provider_name = getText("provider.create.name")+" "+uid;
		KatelloProvider prov = new KatelloProvider(provider_name, org_name, null, null);
		SSHCommandResult res = prov.info();
		String grepName = KatelloUtils.grepCLIOutput(
				getText("provider.list.stdout.property.name"), 
				getOutput(res));
		String grepDescription = KatelloUtils.grepCLIOutput("Description", getOutput(res));
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(provider_name.equals(grepName), 
				"Check - stdout (provider info: name)");
		Assert.assertTrue(getText("provider.create.description").equals(grepDescription), 
				"Check - stdout (provider info: description)");
	}
	
	@Test(description="provider list", dependsOnMethods={"createProvider"})
	public void listProvider(){
		KatelloProvider prov = new KatelloProvider(
				getText("provider.create.name")+" "+uid, org_name, 
				getText("provider.create.description"), null);
		SSHCommandResult res = prov.cli_list();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(res).contains(prov.name), 
				"Check - stdout (provider list: name)");
		Assert.assertTrue(getOutput(res).contains(prov.description), 
				"Check - stdout (provider list: description)");
	}
	
	@Test(description="provider refresh_products")
	public void refreshProductsProvider(){
		String outSuccess = getText("provider.refresh_products.stdout", KatelloProvider.PROVIDER_REDHAT);
		KatelloProvider prov = new KatelloProvider(KatelloProvider.PROVIDER_REDHAT,org_name,null,null);
		SSHCommandResult res = prov.refresh_products();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(res).equals(outSuccess), "Check - stdout (provider refresh_products)");
	}
	
	@Test(description="provider status --name \"Red Hat\"")
	public void statusProvider_RedHat(){
		String outLastSync = getText("provider.status.last_sync.never");
		String outSyncState = getText("provider.status.sync_state.not_synced");
		KatelloProvider prov = new KatelloProvider(KatelloProvider.PROVIDER_REDHAT,org_name,null,null);
		SSHCommandResult res = prov.status();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(outSyncState.equals(KatelloUtils.grepCLIOutput("Sync State", getOutput(res))), 
				"Check - stdout.'Sync State' (provider status: --name Red Hat)");
		Assert.assertTrue(outLastSync.equals(KatelloUtils.grepCLIOutput("Last Sync", getOutput(res))), 
				"Check - stdout.'Last Sync' (provider status: --name Red Hat)");
	}

	@Test(description="provider status: custom", dependsOnMethods={"createProvider"})
	public void statusProvider_Custom(){
		String provider_name = getText("provider.create.name")+" "+uid;
		String outLastSync = getText("provider.status.last_sync.never");
		String outSyncState = getText("provider.status.sync_state.not_synced");
		KatelloProvider prov = new KatelloProvider(provider_name,org_name,null,null);
		SSHCommandResult res = prov.status();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(outSyncState.equals(KatelloUtils.grepCLIOutput("Sync State", getOutput(res))), 
				"Check - stdout.'Sync State' (provider status: --name Red Hat)");
		Assert.assertTrue(outLastSync.equals(KatelloUtils.grepCLIOutput("Last Sync", getOutput(res))), 
				"Check - stdout.'Last Sync' (provider status: --name Red Hat)");
	}

	@Test(description="provider update", dependsOnMethods={"createProvider"})
	public void updateProvider(){
		String provider_name = "todo "+getText("provider.create.name")+" "+uid;
		String outSuccess = getText("provider.update.stdout", getText("provider.update.name")+" "+uid);
		KatelloProvider prov = new KatelloProvider(provider_name, org_name, null, null);
		SSHCommandResult res = prov.create();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		
		prov = new KatelloProvider(provider_name, org_name, null, null);
		res = prov.update(getText("provider.update.name")+" "+uid, null, getText("provider.create.description"));
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(res).equals(outSuccess), "Check - stdout (provider create)");
	}
	
	@Test(description="provider import_manifest", dependsOnMethods={"createProvider"})
	public void importManifestProvider(){
		KatelloProvider prov = new KatelloProvider(KatelloProvider.PROVIDER_REDHAT, org_name, null, null);
		
		SCPTools scp = new SCPTools(
				System.getProperty("katello.client.hostname", "localhost"), 
				System.getProperty("katello.client.ssh.user", "root"), 
				System.getProperty("katello.client.sshkey.private", ".ssh/id_hudson_dsa"), 
				System.getProperty("katello.client.sshkey.passphrase", "null"));
		Assert.assertTrue(scp.sendFile(
				"data"+File.separator+SystemsReport.MANIFEST_2SUBSCRIPTIONS, "/tmp"),
				SystemsReport.MANIFEST_2SUBSCRIPTIONS+" sent successfully");			
		SSHCommandResult res = prov.import_manifest("/tmp"+File.separator+SystemsReport.MANIFEST_2SUBSCRIPTIONS, null);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		Assert.assertTrue(getOutput(res).contains(getText("provider.import_manifest.please_wait")),"provider import_manifest - 1");
		Assert.assertTrue(getOutput(res).contains(getText("provider.import_manifest.imported")),"provider import_manifest - 2");
	}
	
	@Test(description="provider refresh_products", dependsOnMethods={"importManifestProvider"})
	public void refreshProducts(){
		KatelloProvider prov = new KatelloProvider(KatelloProvider.PROVIDER_REDHAT, org_name, null, null);
		SSHCommandResult res;
		
		res = prov.refresh_products();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		Assert.assertTrue(getOutput(res).equals(getText("provider.refresh_products.stdout",prov.name)),"Check - stdout (provider refresh_products)");		
	}
	
	@Test(description="repo enable", dependsOnMethods={"refreshProducts"})
	public void enableRepo(){
		SSHCommandResult res;
		
		KatelloProduct prod=new KatelloProduct(KatelloProduct.RHEL_SERVER,org_name, KatelloProvider.PROVIDER_REDHAT, null, null, null,null, null);
		res = prod.repository_set_enable(KatelloProduct.REPOSET_RHEL6_RPMS);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo set enable)");
		
		KatelloRepo repo = new KatelloRepo(KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT, org_name, 
				KatelloProduct.RHEL_SERVER, null, null, null);
		log.finest(String.format("Enable the repo: [%s] for synchronization",repo.name));
		res = repo.enable();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		Assert.assertTrue(getOutput(res).equals(getText("repo.enable.stdout",repo.name)),"Check - stdout (repo enable)");
	}
	
	@Test(description="provider synchronize (nowait)", dependsOnMethods={"enableRepo"})
	public void synchronizeNowait(){
		KatelloProvider prov = new KatelloProvider(KatelloProvider.PROVIDER_REDHAT, org_name, null, null);
		SSHCommandResult res;
		
		log.info("Invoke provider synchronize - nowait");
		prov.synchronize_nowait();
		log.info("Sleep 60 seconds and check the status");
		try{Thread.sleep(60000);}catch(Exception ex){}
		res = prov.status();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		Assert.assertTrue(getOutput(res).contains(getText("provider.status.inprogress.packages_downloaded")),"Check - stdout (provider status)");		
	}
	
	@Test(description="provider cancel_sync", dependsOnMethods={"synchronizeNowait"})
	public void cancel_sync(){
		KatelloProvider prov = new KatelloProvider(KatelloProvider.PROVIDER_REDHAT, org_name, null, null);
		SSHCommandResult res;
		
		log.info("Cacnel synchronization");
		res = prov.cancel_sync();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		log.info("Sleep 120 seconds and check the status");
		try{Thread.sleep(120000);}catch(Exception ex){}
		res = prov.status();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		Assert.assertTrue(getOutput(res).contains(getText("provider.status.sync_state.cancelled")),"Check - stdout (provider status)");		
	}
	
	@Test(description="provider delete")
	public void delete(){
		String provider_name = "DEL "+getText("provider.create.name")+" "+uid;
		KatelloProvider prov = new KatelloProvider(provider_name, org_name, null, null);
		SSHCommandResult res = prov.create();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		res = prov.delete();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		Assert.assertTrue(getOutput(res).equals(getText("provider.delete.stdout",provider_name)),"Check - stdout (provider delete)");
	}
	
	@AfterClass(description="remove the org")
	public void destroy(){

		log.info(String.format("Remove the org: [%s] with the manifest related",org_name));
		KatelloOrg org = new KatelloOrg(org_name,null);
		SSHCommandResult res = org.delete();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
	}
}
