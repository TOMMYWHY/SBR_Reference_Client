
package au.gov.sbr.core.client.beans;

/**
 * This class has the sole purpose of relaying back the key store password.
 * 
 * @author SBR
 */
public interface PasswordCallback {

	/**
	 * Returns the keystore password.
	 * 
	 * @return the keystore password.
	 */
	public char[] getKeyStorePassword();
}
