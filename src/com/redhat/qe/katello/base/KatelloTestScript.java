package com.redhat.qe.katello.base;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.testng.annotations.Guice;

import com.google.inject.Inject;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.common.KatelloConstants;
import com.redhat.qe.katello.guice.KatelloApiModule;
import com.redhat.qe.katello.tasks.KatelloTasks;

@Guice(modules = KatelloApiModule.class)
public abstract class KatelloTestScript 
	extends com.redhat.qe.auto.testng.TestScript 
	implements KatelloConstants {

	protected static Logger log = Logger.getLogger(KatelloTestScript.class.getName());
	
	@Inject
	protected KatelloTasks servertasks	= null;
	
	private SimpleDateFormat dateFormatter = null;
	
	public static String default_org = null;
	
	public KatelloTestScript() {
		super();
		try {
			dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			if(default_org ==null)
				setup_defaultOrg();
		}
		catch (Exception e) {
			e.printStackTrace();
		} catch (KatelloApiException e) {
            e.printStackTrace();
        }
	}
	
	public Date parseKatelloDate(String strDate) throws java.text.ParseException{
		String sDate = strDate.substring(0, 19); // cut the rest with: +01:00 for example.
		return dateFormatter.parse(sDate);
	}
	
//	/**
//	 * Returns the JSON object of parsing strings of format: {...}
//	 * @param str_json JSON string with format {...}
//	 * @return JSONObject containing the root.
//	 * @author gkhachik
//	 * @since 16.Feb.2011
//	 */
//	public static JSONObject toJSONObj(String str_json){
//		JSONObject _return = null;
//		try{
//			JSONParser parser=new JSONParser();
//			Object obj=parser.parse(str_json);
//			_return = (JSONObject)obj; // exception would be handled below.
//		}catch (ParseException e) {
//			log.log(Level.SEVERE, e.getMessage(), e);
//		}
//		return _return;
//	}
//	
//	/**
//	 * Returns the JSON objects array of parsing strings of format: [...]
//	 * @param str_json JSON string with format [...]
//	 * @return JSONArray containing array of json object(s).
//	 * @author gkhachik
//	 * @since 16.Feb.2011
//	 */
//	public static JSONArray toJSONArr(String str_json){
//		JSONArray _return = null;
//		try{
//			JSONParser parser=new JSONParser();
//			Object obj=parser.parse(str_json);
//			_return = (JSONArray)obj; // exception would be handled below.
//		}catch (ParseException e) {
//			log.log(Level.SEVERE, e.getMessage(), e);
//		}
//		return _return;
//	}
	
//	protected String getOutput(SSHCommandResult res){
//		return KatelloCliTestScript.sgetOutput(res);
//	}
	
	private void setup_defaultOrg() throws KatelloApiException {
	    List<KatelloOrg> orgs = servertasks.getOrganizations();
	    for ( KatelloOrg org : orgs ) {
	        if ( org.getId().longValue() == 1 ) {
	            default_org = org.getName();
	            break;
	        }
	    }
	}
}
