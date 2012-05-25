package com.redhat.qe.katello.base;

import java.util.ArrayList;

import javax.management.Attribute;

public class KatelloPostParam {

	public String name;
	public ArrayList<Attribute> properties;
	
	public KatelloPostParam(String name, ArrayList<Attribute> properties){
		this.name = name;
		this.properties = properties;
	}
	
	public String toString(){
		String _ret = "";
		
		if(this.name != null)
			_ret = "'"+name+"':{%s}";
		else
			_ret = "%s";
		String content="";
		for(Attribute option: properties){
			if(option.getValue() != null)
				content = String.format("%s'%s':'%s',", content,option.getName(),option.getValue());
		}
		if(content.length()>1)
			content = content.substring(0,content.length()-1); // cut last symbol - ","
		_ret = String.format(_ret, content);
		
		return _ret;
	}
}
