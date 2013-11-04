package com.redhat.qe.katello.common;

/**
 * TestNG groups definition.<br>
 * Different groups for different products.<br>
 * katello: https://tcms.engineering.redhat.com/admin/testcases/testcasecategory/?product__id__exact=313 
 * @author gkhachik
 * @since 18.April.2013
 */
public interface TngRunGroups {

	public static final String TNG_KATELLO_Activation_Key = "Activation Key"; // tcms++
	public static final String TNG_KATELLO_Errata = "Errata"; // tcms++
	public static final String TNG_KATELLO_Environment = "Environment"; // tcms ++
	public static final String TNG_KATELLO_Generic = "Generic";
	public static final String TNG_KATELLO_Subscriptions = "Subscriptions";
	public static final String TNG_KATELLO_Content = "Content"; // tcms--
	public static final String TNG_KATELLO_Providers_Repos = "Providers / Repos"; // tcms--
	public static final String TNG_KATELLO_System_Groups = "System Groups"; // tcms++
	public static final String TNG_KATELLO_System_Consumer = "System / Consumer"; // tcms++
	public static final String TNG_KATELLO_Organizations = "Organizations"; // tcms++
	public static final String TNG_KATELLO_Manifests_CDN = "Manifests / CDN"; // tcms++
	public static final String TNG_KATELLO_Users_Roles = "Users / Roles"; // tcms++
	public static final String TNG_KATELLO_Install_Configuration = "Install / Configuration";
	public static final String TNG_KATELLO_Foreman = "Foreman";
	public static final String TNG_KATELLO_DEFAULT = "--default--";
}
