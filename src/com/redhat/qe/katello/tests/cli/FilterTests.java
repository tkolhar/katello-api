package com.redhat.qe.katello.tests.cli;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.auto.testng.Assert;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloFilter;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups={"cfse-cli"})
public class FilterTests extends KatelloCliTestScript{
	String org;
	String environment;
	
	@BeforeClass(description="init: create org stuff", groups = {"cli-filter"})
	public void setUp(){
		SSHCommandResult res;
		String uid = KatelloTestScript.getUniqueID();
		this.org = "filter-"+uid;
		this.environment = "Dev-"+uid;
		KatelloOrg org = new KatelloOrg(this.org, null);
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (org create)");
		KatelloEnvironment env = new KatelloEnvironment(this.environment, null, this.org, KatelloEnvironment.LIBRARY);
		res = env.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (env create)");
	}

	@Test(description="Create filter: no packages", groups = {"cli-filter"})
	public void test_createWithoutPackages(){
		String filterName = "filter"+KatelloTestScript.getUniqueID();
		KatelloFilter filter = new KatelloFilter(filterName, this.org, null, null);
		SSHCommandResult res = filter.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (filter create)");
		Assert.assertTrue(getOutput(res).equals(String.format(KatelloFilter.OUT_CREATE, filterName)), 
				"Check - output string (filter create)");
	}
	
	@Test(description="Create filter: with packages", groups = {"cli-filter"})
	public void test_createWithPackages(){
		String filterName = "filter"+KatelloTestScript.getUniqueID();
		KatelloFilter filter = new KatelloFilter(filterName, this.org, null, "package1, package2");
		SSHCommandResult res = filter.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (filter create)");
		Assert.assertTrue(getOutput(res).equals(String.format(KatelloFilter.OUT_CREATE, filterName)), 
				"Check - output string (filter create)");
		
		assert_filterInfo(filter);
	}
	
	@Test(description="List Filters", groups = {"cli-filter"})
	public void test_listFilters() {
		
		List<KatelloFilter> filters = new ArrayList<KatelloFilter>();
		
		String uid = KatelloTestScript.getUniqueID();
		String filterName = "filter1" + uid;
		KatelloFilter filter = new KatelloFilter(filterName, this.org, null, null);
		filter.create();
		filters.add(filter);
		
		String filterName2 = "filter2" + uid;
		KatelloFilter filter2 = new KatelloFilter(filterName2, this.org, null, null);
		filter2.create();
		filters.add(filter2);
		
		SSHCommandResult res = filter.cli_list();
		Assert.assertEquals(res.getExitCode().intValue(), 0, "Check - return code (filter list)");
		
		for(KatelloFilter flt : filters){
			if(flt.description ==null) flt.description = "None";
			String match_list = String.format(KatelloFilter.REG_FILTER_LIST, flt.name, flt.description).replaceAll("\"", ""); // output not have '"' signs
			Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(match_list), "Check - filter matches ["+flt.name+"]");
		}
		
	}
	
	@Test(description="Delete a Filter", groups = {"cli-filter"})
	public void test_deleteFilter(){
		String uid = KatelloTestScript.getUniqueID();
		String filterName = "filter1" + uid;
		KatelloFilter filter = new KatelloFilter(filterName, this.org, null, null);
		filter.create();
		
		SSHCommandResult res = filter.delete();
		Assert.assertEquals(res.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertTrue(getOutput(res).contains(String.format("Successfully deleted filter [ %s ]",filter.name)),"Check - return string");
		
		res = filter.cli_info();
		Assert.assertEquals(res.getExitCode(), new Integer(148),"Check - return code [148]");
		Assert.assertEquals(getOutput(res).trim(), 
				String.format(KatelloFilter.ERR_FILTER_NOTFOUND,filter.name));
	}
	
	@Test(description="Delete a Filter not exist", groups = {"cli-filter"})
	public void test_deleteFilterNotFound(){
		String uid = KatelloTestScript.getUniqueID();
		String filterName = "filter1" + uid;
		KatelloFilter filter = new KatelloFilter(filterName, this.org, null, null);
		
		SSHCommandResult res = filter.delete();
		Assert.assertEquals(res.getExitCode(), new Integer(148),"Check - return code [148]");
		Assert.assertEquals(getOutput(res).trim(), 
				String.format(KatelloFilter.ERR_FILTER_NOTFOUND,filter.name));
	}
	
	@Test(description="Info Filter not exist", groups = {"cli-filter"})
	public void test_infoFilterNotFound(){
		String uid = KatelloTestScript.getUniqueID();
		String filterName = "filter1" + uid;
		KatelloFilter filter = new KatelloFilter(filterName, this.org, null, null);
		
		SSHCommandResult res = filter.cli_info();
		Assert.assertEquals(res.getExitCode(), new Integer(148),"Check - return code [148]");
		Assert.assertEquals(getOutput(res).trim(), 
				String.format(KatelloFilter.ERR_FILTER_NOTFOUND,filter.name));
	}
	
	@Test(description="Add package to filter", groups = {"cli-filter"})
	public void test_addPackage(){
		String filterName = "filter"+KatelloTestScript.getUniqueID();
		KatelloFilter filter = new KatelloFilter(filterName, this.org, null, "package1");
		SSHCommandResult res = filter.create();
		Assert.assertEquals(res.getExitCode().intValue(), 0, "Check - return code");
		
		res = filter.cli_addPackage("package2");
		Assert.assertEquals(res.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertTrue(getOutput(res).equals(String.format(KatelloFilter.OUT_PACKAGE_ADD, "package2", filterName)),
				"Check - output string (filter added package)");
		filter.packages = "package1, package2";
		
		assert_filterInfo(filter);
	}
	
	@Test(description="Add package to filter which does not exist", groups = {"cli-filter"})
	public void test_addPackageNotFound(){
		String filterName = "filter"+KatelloTestScript.getUniqueID();
		KatelloFilter filter = new KatelloFilter(filterName, this.org, null, "package1");
		
		SSHCommandResult res = filter.cli_addPackage("package2");
		Assert.assertEquals(res.getExitCode(), new Integer(148),"Check - return code [148]");
		Assert.assertEquals(getOutput(res).trim(), 
				String.format(KatelloFilter.ERR_FILTER_NOTFOUND,filter.name));
	}
	
	@Test(description="Remove package from filter", groups = {"cli-filter"})
	public void test_removePackage(){
		String filterName = "filter"+KatelloTestScript.getUniqueID();
		KatelloFilter filter = new KatelloFilter(filterName, this.org, null, "package1, package2");
		SSHCommandResult res = filter.create();
		Assert.assertEquals(res.getExitCode().intValue(), 0, "Check - return code");
		
		res = filter.cli_removePackage("package2");
		Assert.assertEquals(res.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertTrue(getOutput(res).equals(String.format(KatelloFilter.OUT_PACKAGE_REMOVE, "package2", filterName)),
				"Check - output string (filter removed package)");
		filter.packages = "package1";
		
		assert_filterInfo(filter);
	}
	
	@Test(description="Remove package from filter which does not exist", groups = {"cli-filter"})
	public void test_removePackageNotFound(){
		String filterName = "filter"+KatelloTestScript.getUniqueID();
		KatelloFilter filter = new KatelloFilter(filterName, this.org, null, "package1");
		
		SSHCommandResult res = filter.cli_removePackage("package1");
		Assert.assertEquals(res.getExitCode(), new Integer(148),"Check - return code [148]");
		Assert.assertEquals(getOutput(res).trim(), 
				String.format(KatelloFilter.ERR_FILTER_NOTFOUND,filter.name));
	}
	
	private void assert_filterInfo(KatelloFilter filter){
		if (filter.description == null) filter.description = "None";
		if (filter.packages == null) filter.packages = "";
		
		SSHCommandResult res;
		res = filter.cli_info();
		String match_info = String.format(KatelloFilter.REG_FILTER_INFO, filter.name, filter.description, filter.packages).replaceAll("\"", "");
		Assert.assertEquals(res.getExitCode().intValue(), 0, "Check - return code");
		log.finest(String.format("Filter (info) match regex: [%s]",match_info));
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(match_info), 
				String.format("Filter [%s] should be found in the result info",filter.name));		
	}
	
}
