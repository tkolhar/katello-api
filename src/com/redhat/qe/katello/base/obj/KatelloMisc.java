package com.redhat.qe.katello.base.obj;

import java.util.logging.Level;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.redhat.qe.katello.base.KatelloApi;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloMisc {

	public static final String API_CMD_GET_POOLS = "/pools"; 
	
	
	public SSHCommandResult api_getPools(){
		return new KatelloApi().get(API_CMD_GET_POOLS);
	}
	
	public JSONObject api_getPoolByProduct(String productName){
		JSONObject pool =null;
		try{
			JSONArray jpools = KatelloTestScript.toJSONArr(api_getPools().getStdout()); 
			if(jpools ==null) return null;
			for(int i=0;i<jpools.size();i++){
				pool = (JSONObject)jpools.get(i);
				if(((String)pool.get("productName")).equals(productName))
					return pool;
			}
		}catch (Exception e) {}
		return null;
	}
}
