package com.redhat.qe.katello.tests.deltacloud;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

@Test(groups="cfse-dc-errata")
public class SetupServers extends BaseDeltacloudTest {
	
	@BeforeSuite(description = "setup Deltacloud Server and clients")
	public void setUp() {
		super.setUp();
	}
	
	@AfterSuite(alwaysRun=true)
	public void tearDown() {
		super.tearDown();
	}

}
