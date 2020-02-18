
package au.gov.sbr.core.client.gui;

import java.util.ResourceBundle;

public class OrganisationNameCheck
{
	private static final String KEY = "organisation.name";

	static boolean isOrganisationNameSet()
	{
		ResourceBundle bun = ResourceBundle.getBundle(OrganisationNameCheck.class.getName());
		String organisationName = bun.getString(KEY);
		return organisationName != null && !organisationName.trim().equals("");		
	}

	public static String getOrganisationName() {
		return ResourceBundle.getBundle(OrganisationNameCheck.class.getName()).getString(KEY);
	}
}
