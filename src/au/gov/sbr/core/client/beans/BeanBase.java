
package au.gov.sbr.core.client.beans;

import java.io.Serializable;
import org.jdesktop.application.AbstractBean;

/**
 * Represents the base definition to encapsulate many objects into the single
 * object bean.
 * 
 * @author SBR
 */
public class BeanBase extends AbstractBean implements Serializable {

	private static final long serialVersionUID = 1L;
	private String displayName;

	/**
	 * Returns the display name of the bean.
	 * 
	 * @return the display name.
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Sets the display name of the bean.
	 * 
	 * @param displayName
	 *            display name.
	 */
	public void setDisplayName(String displayName) {
		String oldDisplayName = this.displayName;

		this.displayName = displayName;

		firePropertyChange("displayName", oldDisplayName, displayName);
	}
}
