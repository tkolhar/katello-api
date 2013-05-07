package com.redhat.qe.katello.base.obj.helpers;


public class FilterRulePackageGroups {

	private String [] names;

	public FilterRulePackageGroups(String ... names) {
		this.names = names;
	}

	public String filterRule() {
		if(names.length == 0)
			return "{}";
		String rule = "{\\\"units\\\": [";
		for(String name : names) {
			rule += "{\\\"name\\\": \\\"" + name + "\\\"}, ";
		}
		rule = rule.substring(0, rule.length()-2);
		return rule + "]}";
	}

	public String ruleRegExp() {
		if(names.length == 0)
			return "\\{\\}";
		String regexp =  "\\{units: \\[";
		for(String name : names) {
			regexp += "\\{name: " + name + "\\}, +";
		}
		regexp = regexp.substring(0, regexp.length()-3) + "\\]\\}";
		return regexp;
	}

}
