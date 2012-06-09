package com.redhat.qe.katello.base;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.common.KatelloConstants;
import com.redhat.qe.katello.tasks.KatelloTasks;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloTestScript 
	extends com.redhat.qe.auto.testng.TestScript 
	implements KatelloConstants {

	protected static Logger log = Logger.getLogger(KatelloTestScript.class.getName());
	
	protected KatelloTasks servertasks	= null;
	private SimpleDateFormat dateFormatter = null;
	
	public static String default_org = null;
	
	public KatelloTestScript() {
		super();
		servertasks = new KatelloTasks();
		try {
			dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			if(default_org ==null)
				setup_defaultOrg();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Date parseKatelloDate(String strDate) throws java.text.ParseException{
		String sDate = strDate.substring(0, 19); // cut the rest with: +01:00 for example.
		return dateFormatter.parse(sDate);
	}
	
	/**
	 * Generates the unique string which is the current (timeInMillis / 1000).
	 * @return unique ID string.
	 * @author gkhachik
	 * @since 15.Feb.2011
	 */
	public static String getUniqueID(){
		try{Thread.sleep(1000+Math.abs(new Random().nextInt(200)));}catch(InterruptedException iex){};
		String uid = String.valueOf(
				Calendar.getInstance().getTimeInMillis() / 1000); 
		log.fine(String.format("Generating unique ID: [%s]",uid));
		return uid;
	}
	
	public static String getUUID(){
		return KatelloTasks.run_local(false, "python -c \"import uuid; print uuid.uuid1();\"");
	}
	
	/**
	 * Returns the JSON object of parsing strings of format: {...}
	 * @param str_json JSON string with format {...}
	 * @return JSONObject containing the root.
	 * @author gkhachik
	 * @since 16.Feb.2011
	 */
	public static JSONObject toJSONObj(String str_json){
		JSONObject _return = null;
		try{
			JSONParser parser=new JSONParser();
			Object obj=parser.parse(str_json);
			_return = (JSONObject)obj; // exception would be handled below.
		}catch (ParseException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
		return _return;
	}
	
	/**
	 * Returns the JSON objects array of parsing strings of format: [...]
	 * @param str_json JSON string with format [...]
	 * @return JSONArray containing array of json object(s).
	 * @author gkhachik
	 * @since 16.Feb.2011
	 */
	public static JSONArray toJSONArr(String str_json){
		JSONArray _return = null;
		try{
			JSONParser parser=new JSONParser();
			Object obj=parser.parse(str_json);
			_return = (JSONArray)obj; // exception would be handled below.
		}catch (ParseException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
		return _return;
	}
	
	protected String getOutput(SSHCommandResult res){
		return KatelloCliTestScript.sgetOutput(res);
	}
	
	private void setup_defaultOrg(){
		KatelloOrg _org = new KatelloOrg(null, null);
		SSHCommandResult res = _org.api_list();
		JSONArray orgs = KatelloTestScript.toJSONArr(res.getStdout());
		JSONObject org;
		for(int i=0;i<orgs.size();i++){
			org = (JSONObject)orgs.get(i);
			if(((Long)org.get("id")).intValue()==1){
				default_org = (String)org.get("name");
				return;
			}
		}
	}
}
