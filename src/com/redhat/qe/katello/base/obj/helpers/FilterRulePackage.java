package com.redhat.qe.katello.base.obj.helpers;


public class FilterRulePackage {

	private String name;
	private String version;
	private String min_version;
	private String max_version;

	public FilterRulePackage(String name) {
		this.name = name;
	}

	public FilterRulePackage(String name, String version, String min_version, String max_version) {
		this(name);
		this.version = version;
		this.min_version = min_version;
		this.max_version = max_version;
	}


	public String filterRule() {
		return FilterRulePackage.filterRule(this);
	}

	public String ruleRegExp() {
		return FilterRulePackage.ruleRegExp(this);
	}


	public static String filterRule(FilterRulePackage ... packages) {
		if(packages.length == 0)
			return "{}";
		String rule;
		rule = "{\\\"units\\\": [";
		for(FilterRulePackage pack : packages) {
			rule += pack.singleRuleJSON() + ", ";
		}
		rule = rule.substring(0, rule.length()-2);
		rule += "]}";
		return rule;
	}

	public static String ruleRegExp(FilterRulePackage ... packages) {
		if(packages.length == 0)
			return "\\{\\}";
		String regexp =  "\\{units: \\[";
		for(FilterRulePackage pack : packages) {
			regexp += pack.singleRuleRegExp() + ", +";
		}
		regexp = regexp.substring(0, regexp.length()-3) + "\\]\\}";
		return regexp;
	}


	private String singleRuleJSON() {
		String str = "{";
		if(name != null)
			str += "\\\"name\\\" : \\\""+ name +"\\\", ";
		if(version != null)
			str += "\\\"version\\\" : \\\"" + version + "\\\", ";
		if(min_version != null)
			str += "\\\"min_version\\\" : \\\"" + min_version + "\\\", ";
		if(max_version != null)
			str += "\\\"max_version\\\" : \\\"" + max_version + "\\\", ";
		return str.substring(0, str.length()-2) + "}";
	}

	private String singleRuleRegExp() {
		String str = "\\{";
		if(max_version != null)
			str += "max_version: " + max_version + ", +";
		if(min_version != null)
			str += "min_version: " + min_version + ", +";
		if(name != null)
			str += "name: " + name + ", +";
		if(version != null)
			str += "version: " + version + ", +";
		return str.substring(0, str.length()-3) + "\\}";
	}

}
