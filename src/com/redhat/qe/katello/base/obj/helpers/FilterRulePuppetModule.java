package com.redhat.qe.katello.base.obj.helpers;

public class FilterRulePuppetModule {

	public String author;
	public String name;
	public String version;
	public String max_version;
	public String min_version;

	public FilterRulePuppetModule(String author, String name, String version, String min_version, String max_version) {
		this.author = author;
		this.name = name;
		this.version = version;
		this.min_version = min_version;
		this.max_version = max_version;
	}

	public static String filterRule(FilterRulePuppetModule ... modules) {
		if(modules.length == 0)
			return "{}";
		String rule = "{\\\"units\\\": [";
		for(FilterRulePuppetModule module : modules) {
			rule += "{";
			if(module.name != null)
				rule += "\\\"name\\\": \\\"" + module.name + "\\\",";
			if(module.author != null)
				rule += "\\\"author\\\": \\\"" + module.author + "\\\",";
			if(module.max_version != null)
				rule += "\\\"max_version\\\": \\\"" + module.max_version + "\\\",";
			if(module.min_version != null)
				rule += "\\\"min_version\\\": \\\"" + module.min_version + "\\\",";
			if(module.version != null)
				rule += "\\\"version\\\": \\\"" + module.version + "\\\",";
			rule = rule.substring(0, rule.length()-1); // delete extra comma
			rule += "},";
		}
		rule = rule.substring(0, rule.length()-1); // delete extra comma
		rule += "]}";
		return rule;
	}
	
	public static String filterRuleRegExp(FilterRulePuppetModule ... modules) {
		if(modules.length == 0)
			return "\\{\\}";
		String regexp =  "\\{units: \\[";
		for(FilterRulePuppetModule pack : modules) {
			regexp += pack.singleModuleRegExp() + ", +";
		}
		regexp = regexp.substring(0, regexp.length()-3) + "\\]\\}";
		return regexp;
	}

	private String singleModuleRegExp() {
		String str = "\\{";
		if(author != null)
			str += "author: " + author + ", +";
		if(max_version != null)
			str += "max_version: " + max_version + ", +";
		if(min_version != null)
			str += "min_version: " + min_version + ", +";
		if(name != null)
			str += "name: " + name + ", +";
		if(version != null)
			str += "version: " + version + ", +";
		return str.substring(0, str.length()-3) + "\\}"; // delete extra comma
	}
}
