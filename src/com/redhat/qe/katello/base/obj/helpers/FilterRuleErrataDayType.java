package com.redhat.qe.katello.base.obj.helpers;


public class FilterRuleErrataDayType {

	public static final String REG_DATE = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}-\\d{2}:\\d{2}";

	private String start;
	private String end;
	private String [] errata_types;

	public FilterRuleErrataDayType(String start, String end, String [] errata_types) {
		this.start = start;
		this.end = end;
		this.errata_types = errata_types;
	}

	public String filterRule() {
		String rule = "{";
		// add date_range if set
		if(start != null || end != null) {
			rule += "\\\"date_range\\\": {";
			if(start != null)
				rule += "\\\"start\\\": \\\"" + start + "\\\"";
			if(end != null)
				rule += (start != null?",":"") + "\\\"end\\\": \\\"" + end + "\\\"";
			rule += "}";
		}
		// add errata_type if set
		if(errata_types != null && errata_types.length != 0) {
			rule += (start != null || end != null ? ", " : "") + "\\\"errata_type\\\": [";
			for(String err_type : errata_types) {
				rule += "\\\"" + err_type + "\\\", ";
			}
			rule = rule.substring(0, rule.length()-2) + "]";
		}
		rule += "}";
		return rule;
	}

	public String ruleRegExp() {
		String regexp = "\\{";
		// add date_range if set
		if(start != null || end != null) {
			regexp += "date_range: \\{";
			if(end != null)
				regexp += "end: " + REG_DATE;
			if(start != null)
				regexp += (end != null?", +":"") + "start: " + REG_DATE;
			regexp += "\\}";
		}
		// add errata_type if set
		if(errata_types != null && errata_types.length != 0) {
			regexp += (start != null || end != null ? ", +" : "") + "errata_type: \\[";
			for(String err_type : errata_types) {
				regexp += err_type + ", +";
			}
			regexp = regexp.substring(0, regexp.length()-3) + "\\]";
		}
		regexp += "\\}";
		return regexp;
	}

}
