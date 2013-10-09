package com.redhat.qe.katello.tests.hammer.cli;

import java.util.Arrays;
import java.util.Random;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.Assert;
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
	
	@BeforeClass(description="Prepare an data to work with")
	public void setUp(){
		String uid = KatelloUtils.getUniqueID().substring(7);
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

	@Test(description="create Subnet with all parameters")
	public void testSubnet_createAllParams() {
		SSHCommandResult res;
		
		HammerSubnet sub = new HammerSubnet(cli_worker, name2, "251.10.10.11", "255.255.255.0");
		sub.setGateway("251.10.10.255");
		sub.setDns_primary("251.10.11.255");
		sub.setDns_secondary("251.10.12.255");
		sub.setFrom("251.10.10.10");
		sub.setTo("251.10.10.12");
		res = sub.cli_create();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		
		Assert.assertTrue(getOutput(res).contains(String.format(HammerSubnet.OUT_CREATE, name)),"Check - returned output string");
		
		assert_SubnetInfo(sub);
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
	
	// @ TODO bug
	@Test(description="list Subnet", dependsOnMethods={"testSubnet_update"})
	public void testSubnet_list() {
		SSHCommandResult res;
		
		HammerSubnet sub = new HammerSubnet(cli_worker, name, null, null);
		res = sub.cli_list();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		
		Assert.assertFalse(getOutput(res).contains(name),"Check - old name is not listed");
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

	// @ TODO bug 1016458
	@Test(description="search Subnet")
	public void testSubnet_search() {
		SSHCommandResult res;
		
		HammerSubnet sub = new HammerSubnet(cli_worker, name, null, null);
		res = sub.cli_search(base_names[1]);
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		String[] names = getOutput(res).trim().split("\n");
		
		Assert.assertFalse(getOutput(res).contains(names[1]),"Check - name is listed");
		Assert.assertEquals(names.length, 1, "Count of returned subs must be 1.");
	}
	
	// @ TODO bug 1016458
	@Test(description="list Subnet by order and pagination")
	public void testSubnet_listOrder() {
		SSHCommandResult res;
		
		HammerSubnet sub = new HammerSubnet(cli_worker, null, null, null);
		res = sub.cli_list("name", 1, 5);
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		String[] names = getOutput(res).trim().split("\n");
		String[] sortedNames = Arrays.copyOf(names, names.length);
		Arrays.sort(sortedNames);
		
		Assert.assertEquals(names.length, 5, "Count of returned subs must be 5.");
		Assert.assertEquals(names, sortedNames, "Returned subs are sorted.");
		
		res = sub.cli_list("name", 2, 5);
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		String[] second_names = getOutput(res).trim().split("\n");
		String[] second_sortedNames = Arrays.copyOf(second_names, second_names.length);
		Arrays.sort(second_sortedNames);
		
		Assert.assertEquals(second_names.length, 5, "Count of returned subs must be 5.");
		Assert.assertEquals(second_names, second_sortedNames, "Returned subs are sorted.");
		
		Assert.assertEquals(sortedNames, second_sortedNames, "Returned subs in first and second list must not be the same.");
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
