package com.redhat.qe.katello.tests.hammer.cli;

import java.util.Random;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliDataProvider;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.HammerSubnet;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

public class SubnetTests extends KatelloCliTestBase {
	
	private String name;
	private String name2;
	private String network = randomNetwork();
	private String new_name;
	private String[] base_names;
	private String uid = KatelloUtils.getUniqueID().substring(7);
	
	@BeforeClass(description="Prepare an data to work with")
	public void setUp() {
		this.name = "subn"+uid;
		this.name2 = "allparams";
		this.new_name = "new" + uid;
		this.base_names = new String[10];
		for (int i = base_names.length - 1; i >= 0; i--) {
			base_names[i] = "su" + i + "bn" + uid;
			new HammerSubnet(cli_worker, base_names[i], randomNetwork(), "255.255.255.0").cli_create();
		}
		new HammerSubnet(cli_worker, name2, null, null).delete();
	}
	
	@Test(description="create Subnet")
	public void testSubnet_create() {
		SSHCommandResult res;
		
		HammerSubnet sub = new HammerSubnet(cli_worker, name, network, "255.255.255.0");
		res = sub.cli_create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		
		Assert.assertTrue(getOutput(res).contains(String.format(HammerSubnet.OUT_CREATE, name)),"Check - returned output string");
	}

	// bz#1023393
	@Test(description="create Subnet with all parameters")
	public void testSubnet_createAllParams() {
		SSHCommandResult res;
		
		HammerSubnet sub = new HammerSubnet(cli_worker, name2, "251.10.10.11", "255.255.255.0");
		sub.setGateway("251.10.10.255");
		sub.setDns_primary("251.10.11.255");
		sub.setDns_secondary("251.10.12.255");
		sub.setFrom("251.10.10.10");
		sub.setTo("251.10.10.12");
		sub.setDomain_id("1");
		sub.setVlanid("2");
		res = sub.cli_create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		
		Assert.assertTrue(getOutput(res).contains(String.format(HammerSubnet.OUT_CREATE, name)),"Check - returned output string");
		
		assert_SubnetInfo(sub);
	}

	// @ TODO bug, update other params
	@Test(description="update Subnet with all parameters", dependsOnMethods={"testSubnet_createAllParams"})
	public void testSubnet_updateAllParams() {
		SSHCommandResult res;
		
		HammerSubnet sub = new HammerSubnet(cli_worker, name2, "251.10.9.11", "255.255.254.0");
		sub.setGateway("251.10.10.250");
		sub.setDns_primary("251.10.11.250");
		sub.setDns_secondary("251.10.12.250");
		sub.setFrom("251.10.9.10");
		sub.setTo("251.10.9.12");
		sub.setDomain_id("3");
		sub.setVlanid("4");
		res = sub.update(null);
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		
		// @ TODO error message
		//Assert.assertTrue(getOutput(res).contains(String.format(HammerSubnet.OUT_UPDATE, name)),"Check - returned output string");
		
		assert_SubnetInfo(sub);
	}
	
	@Test(description="update Subnet with wrong network range", dependsOnMethods={"testSubnet_updateAllParams"})
	public void testSubnet_updateNetworkRangeFail() {
		SSHCommandResult res;
		
		HammerSubnet sub = new HammerSubnet(cli_worker, name2, "251.11.9.16", "255.255.255.0");
		sub.setFrom("251.10.9.13");
		sub.setTo("251.10.9.14");
		res = sub.update(null);
		Assert.assertFalse(res.getExitCode().intValue() == 0, "Check - return code");
	}
	
	@Test(description="create Subnet which name exists", dependsOnMethods={"testSubnet_create"})
	public void testSubnet_createExists() {
		SSHCommandResult res;
		
		HammerSubnet sub = new HammerSubnet(cli_worker, name, network, "255.255.255.0");
		res = sub.cli_create();
		Assert.assertFalse(res.getExitCode().intValue() == 0, "Check - return code");
		
		// @ TODO error message
		//Assert.assertTrue(getOutput(res).contains(String.format(HammerSubnet.ERR_NAME_EXISTS, name)),"Check - returned error string");
	}

	@Test(description="create Subnet with wrong params", 
			dataProvider="subnet_create", dataProviderClass = KatelloCliDataProvider.class, enabled=true)
	public void testSubnet_createWrongParams(String name, String network, String mask, String gateway, String dns_primary, String dns_secondary,
			String from, String to, String domain, String vlan) {
		SSHCommandResult res;
		
		HammerSubnet sub = new HammerSubnet(cli_worker, name, network, mask);
		sub.setGateway(gateway);
		sub.setDns_primary(dns_primary);
		sub.setDns_secondary(dns_secondary);
		sub.setFrom(from);
		sub.setTo(to);
		sub.setDomain_id(domain);
		sub.setVlanid(vlan);
		
		res = sub.cli_create();
		Assert.assertFalse(res.getExitCode().intValue() == 0, "Check - return code");		
		
		// @ TODO error message
		//Assert.assertTrue(getOutput(res).contains(output),"Check - returned error string");
		
		res = sub.cli_info();
		Assert.assertFalse(res.getExitCode().intValue() == 0, "Check - return code");
	}
	
	@Test(description="create Subnet with all parameters")
	public void testSubnet_createNetworkRangeFail() {
		SSHCommandResult res;
		
		HammerSubnet sub = new HammerSubnet(cli_worker, "wrongnetworkrange", "251.11.9.16", "255.255.255.0");
		sub.setFrom("251.10.9.13");
		sub.setTo("251.10.9.14");
		res = sub.cli_create();
		Assert.assertFalse(res.getExitCode().intValue() == 0, "Check - return code");
	}
	
	@Test(description="info Subnet", dependsOnMethods={"testSubnet_createExists"})
	public void testSubnet_info() {
		SSHCommandResult res;
		
		HammerSubnet sub = new HammerSubnet(cli_worker, name, null, null);
		res = sub.cli_info();
		// @ TODO bug
		//Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		
		Assert.assertTrue(getOutput(res).contains(name),"Check - returned output string");
	}
	
	@Test(description="update Subnet", dependsOnMethods={"testSubnet_info"})
	public void testSubnet_update() {
		SSHCommandResult res;
		
		HammerSubnet sub = new HammerSubnet(cli_worker, name, null, null);
		res = sub.update(new_name);
		Assert.assertEquals(res.getExitCode().intValue(), 0, "Check - return code");
		
		// @ TODO error message
		//Assert.assertTrue(getOutput(res).contains(String.format(HammerSubnet.OUT_UPDATE, name)),"Check - returned output string");
	}
	
	// bz#1023379
	@Test(description="list Subnet", dependsOnMethods={"testSubnet_update"})
	public void testSubnet_list() {
		SSHCommandResult res;
		
		HammerSubnet sub = new HammerSubnet(cli_worker, name, null, null);
		res = sub.cli_info();
		Assert.assertTrue(res.getExitCode().intValue() == 128, "Check - return code");
		Assert.assertTrue(!getOutput(res).contains(name),"Check - old name is not listed");
		
		sub = new HammerSubnet(cli_worker, new_name, null, null);
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(getOutput(res).contains(new_name),"Check - new name is listed");
	}
	
	@Test(description="info Subnet not found", dependsOnMethods={"testSubnet_list"})
	public void testSubnet_infoNotFound() {
		SSHCommandResult res;
		
		HammerSubnet sub = new HammerSubnet(cli_worker, name, null, null);
		res = sub.cli_info();
		Assert.assertFalse(res.getExitCode().intValue() == 0, "Check - return code");
		
		// @ TODO error message
		//Assert.assertTrue(getOutput(res).contains(String.format(HammerSubnet.ERR_NOT_FOUND, name)),"Check - returned error string");
	}

	@Test(description="delete Subnet", dependsOnMethods={"testSubnet_infoNotFound"})
	public void testSubnet_delete() {
		SSHCommandResult res;
		
		HammerSubnet sub = new HammerSubnet(cli_worker, new_name, null, null);
		res = sub.delete();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		
		// @ TODO error message
		//Assert.assertTrue(getOutput(res).contains(String.format(HammerSubnet.OUT_DELETE, new_name)),"Check - returned output string");
	}

	@Test(description="update Subnet name not found", dependsOnMethods={"testSubnet_delete"})
	public void testSubnet_updateNotFound() {
		SSHCommandResult res;
		
		HammerSubnet sub = new HammerSubnet(cli_worker, new_name, null, null);
		res = sub.update(name);
		Assert.assertFalse(res.getExitCode().intValue() == 0, "Check - return code");
		
		// @ TODO error message
		//Assert.assertTrue(getOutput(res).contains(String.format(HammerSubnet.ERR_NOT_FOUND, new_name)),"Check - returned error string");
	}
	
	@Test(description="delete Subnet name not found", dependsOnMethods={"testSubnet_updateNotFound"})
	public void testSubnet_deleteNotFound() {
		SSHCommandResult res;
		
		HammerSubnet sub = new HammerSubnet(cli_worker, new_name, null, null);
		res = sub.delete();
		Assert.assertFalse(res.getExitCode().intValue() == 0, "Check - return code");
		
		// @ TODO error message
		//Assert.assertTrue(getOutput(res).contains(String.format(HammerSubnet.ERR_NOT_FOUND, new_name)),"Check - returned error string");
	}

	@Test(description="search Subnet")
	public void testSubnet_search() {
		SSHCommandResult res;
		
		HammerSubnet sub = new HammerSubnet(cli_worker, name, null, null);
		res = sub.cli_search(base_names[1]);
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		String cnt = KatelloUtils.run_local(false, String.format("echo -e \"%s\"|grep \"Name:\"|wc -l",getOutput(res)));
		Assert.assertTrue(cnt.equals("1"), "Count of returned archs must be 1.");
	}
	
	@Test(description="list Subnet by order and pagination")
	public void testSubnet_listOrder() {
		SSHCommandResult res;
		
		HammerSubnet sub = new HammerSubnet(cli_worker, null, null, null);
		res = sub.cli_list("name", 1, 5);
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		String name_1 = KatelloUtils.grepCLIOutput("Name", getOutput(res), 1);
		String name5 = KatelloUtils.grepCLIOutput("Name", getOutput(res), 5);
		String name6 = KatelloUtils.grepCLIOutput("Name", getOutput(res), 6);
		Assert.assertTrue(name5!=null && name6==null, "Count of returned subs must be 5.");
		
		res = sub.cli_list("name", 2, 5);
		String name_2 = KatelloUtils.grepCLIOutput("Name", getOutput(res), 1);
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		name5 = KatelloUtils.grepCLIOutput("Name", getOutput(res), 5);
		name6 = KatelloUtils.grepCLIOutput("Name", getOutput(res), 6);
		Assert.assertTrue(name5!=null && name6==null, "Count of returned subs must be 5.");
		Assert.assertTrue(!name_1.equals(name_2), "Returned subs in first and second list must not be the same.");
	}
	
	public static String randomNetwork() {
		StringBuilder network = new StringBuilder();
		network.append(new Random().nextInt(250));
		network.append(".");
		network.append(new Random().nextInt(255));
		network.append(".");
		network.append(new Random().nextInt(255));
		network.append(".");
		network.append(new Random().nextInt(255));
		
		return network.toString();
	}
	
	private void assert_SubnetInfo(HammerSubnet sub) {
		SSHCommandResult res;
		res = sub.cli_info();
		String match_info = String.format(HammerSubnet.REG_SUBNET_INFO, sub.getName(), sub.getNetwork(), sub.getMask()).replaceAll("\"", "");
		//@ TODO bug
		//Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		log.finest(String.format("Subnet (info) match regex: [%s]",match_info));
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(match_info), 
				String.format("Subnet [%s] should be found in the result info", sub.name));		
	}
}
