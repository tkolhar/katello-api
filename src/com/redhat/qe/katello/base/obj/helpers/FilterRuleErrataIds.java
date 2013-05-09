package com.redhat.qe.katello.base.obj.helpers;


public class FilterRuleErrataIds {

	private String [] ids;

	public FilterRuleErrataIds(String ... ids) {
		this.ids = ids;
	}
	
	public String filterRule() {
		if(ids.length == 0)
			return "{}";
		String rule = "{\\\"units\\\": [";
		for(String id: ids) {
			rule += "{\\\"id\\\" : \\\"" + id + "\\\"}, ";
		}
		rule = rule.substring(0, rule.length()-2) + "]}";
		return rule;
	}

	public String ruleRegExp() {
		if(ids.length == 0)
			return "\\{\\}";
		String regexp = "\\{";
		regexp += "units: \\[";
		for(String id: ids) {
			regexp += "\\{id: " + id + "\\}, +";
		}
		regexp = regexp.substring(0, regexp.length()-3) + "\\]\\}";
		return regexp;
	}

}
