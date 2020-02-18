
package au.gov.sbr.core.client.beans;

import java.text.MessageFormat;

import au.gov.abr.akm.credential.store.ABRCredential;
import au.gov.abr.akm.credential.store.ABRUserCredential;

/**
 * This bean represents the data in a AbrUserCredential object from the ABR
 * KeyStore API. It allows for selection of AbrUserCredential and provides a
 * formatted string that represents the AbrUserCredential object.
 * 
 * Note that it exclusively deals with User Credentials, not Device Credentials.
 * 
 * @author SBR
 */
public class CredentialBean extends BeanBase {

	private static final long serialVersionUID = 1L;
	private ABRUserCredential abrCredential;
	private boolean selected;

	/**
	 * Constructs a newly allocated CredentialBean object that represents an
	 * Australian Business Register (ABR) credential. The format of the
	 * credential is defined here.
	 * 
	 * @param abrCredential
	 *            ABR credential object.
	 */
	public CredentialBean(ABRCredential abrCredential) {

		if (abrCredential == null) {
			throw new IllegalArgumentException("ABRCredential is null");
		}

		// make sure we have a valid user credential
		if(!(abrCredential instanceof ABRUserCredential)) {
			throw new IllegalArgumentException(
					"ABRCredential not UserCredential");
		}

		this.abrCredential = (ABRUserCredential) abrCredential;

		String displayName = MessageFormat.format(
				"{0} {1} of {2} with ABN: {3}", this.abrCredential
						.getGivenNames(), this.abrCredential.getFamilyName(),
				this.abrCredential.getLegalName(),
				this.abrCredential.getABN());
		setDisplayName(displayName);
	}

	/**
	 * Returns the alias of the ABR credential.
	 * 
	 * @return the ABR credential alias.
	 */
	public String getAlias() {
		return abrCredential.alias();
	}

	/**
	 * Returns the Australian Business Number (ABN) of the ABR credential.
	 * 
	 * @return the ABR credential ABN.
	 */
	public String getAbn() {
		return abrCredential.getABN();
	}

	/**
	 * Returns the given names and last name of the ABR credential.
	 * 
	 * @return the ABR credential full name.
	 */
	public String getFullName() {
		return abrCredential.getGivenNames() + " "
				+ abrCredential.getFamilyName();

	}

	/**
	 * Returns the organisation name of the ABR credential.
	 * 
	 * @return the ABR credential organisation name.
	 */
	public String getOrganisationName() {
		return abrCredential.getLegalName();
	}

	/**
	 * Determines if an ABR credential is selected.
	 * 
	 * @return the status of the check.
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * Sets the selected ABR credential.
	 * 
	 * @param selected
	 *            selected ABR credential.
	 */
	public void setSelected(boolean selected) {
		boolean oldSelected = this.selected;

		this.selected = selected;

		firePropertyChange("selected", oldSelected, selected);
	}

	/**
	 * Returns the ABR credential.
	 * 
	 * @return the ABR credential object.
	 */
	public ABRUserCredential getCredential() {
		return abrCredential;
	}

	/**
	 * Returns the string representation of the object.
	 * 
	 * @return the character strings.
	 */
	@Override
	public String toString() {
		return getDisplayName();
	}
}
