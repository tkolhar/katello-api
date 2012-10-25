package com.redhat.qe.katello.tests.i18n;

import org.testng.annotations.BeforeClass;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.common.KatelloUtils;

public class ActivationKeyTests extends KatelloCliTestScript {
	
	private String uid;
	private String org_name;
	
	@BeforeClass(description="create org", alwaysRun=true)
	public void setUp(){
		uid = KatelloUtils.getUniqueID();
		org_name = getText("org.create.name")+" "+uid;
		
	}
}
