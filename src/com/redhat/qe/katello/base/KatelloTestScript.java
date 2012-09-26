package com.redhat.qe.katello.base;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.testng.annotations.Guice;

import com.google.inject.Inject;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.common.KatelloConstants;
import com.redhat.qe.katello.guice.CertSSLContext;
import com.redhat.qe.katello.guice.KatelloApiModule;
import com.redhat.qe.katello.guice.PlainSSLContext;
import com.redhat.qe.katello.tasks.KatelloTasks;

@Guice(modules = { KatelloApiModule.class })
public abstract class KatelloTestScript 
	extends com.redhat.qe.auto.testng.TestScript 
	implements KatelloConstants {

	@Inject protected Logger log;
	
	@Inject @PlainSSLContext
	protected KatelloTasks servertasks	= null;
	
	@Inject @CertSSLContext
	protected KatelloTasks servertasksWithCert = null;
	
	private SimpleDateFormat dateFormatter = null;
	
	private static String default_org = null;
	
	public KatelloTestScript() {
		super();
		try {
			dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Date parseKatelloDate(String strDate) throws java.text.ParseException{
		String sDate = strDate.substring(0, 19); // cut the rest with: +01:00 for example.
		return dateFormatter.parse(sDate);
	}
	
	public String getDefaultOrg() throws KatelloApiException {
	    if(default_org == null){ 
			List<KatelloOrg> orgs = servertasks.getOrganizations();
		    for ( KatelloOrg org : orgs ) {
		        if ( org.getId().longValue() == 1 ) {
		            default_org = org.getName();
		        }
		    }	
	    }
	    return default_org;
	}
}
