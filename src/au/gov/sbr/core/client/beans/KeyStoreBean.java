
package au.gov.sbr.core.client.beans;

import au.gov.abr.akm.exceptions.CertificateChainException;
import au.gov.abr.akm.exceptions.IncorrectPasswordException;
import au.gov.abr.akm.exceptions.InvalidP7CException;
import au.gov.abr.akm.exceptions.KeyStoreLoadException;
import au.gov.abr.akm.exceptions.NullReferenceException;
import au.gov.abr.akm.exceptions.SDKExpiredException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.observablecollections.ObservableList;

import au.gov.abr.akm.credential.store.ABRCredential;
import au.gov.abr.akm.credential.store.ABRKeyStore;
import au.gov.abr.akm.credential.store.ABRProperties;
import au.gov.abr.akm.exceptions.NoSuchAliasException;

/**
 * This bean represents the Authentication KeyStore. It contains all the
 * Properties that are exposed to the GUI for binding to the key store group on
 * the GUI.
 * 
 * @author SBR
 */
public class KeyStoreBean extends BeanBase {

	private static final long serialVersionUID = 1L;
	private ABRKeyStore keyStore;
	private String keyStoreName;
	private String password;
	private ObservableList<CredentialBean> keyStoreAliases;

	/**
	 * Constructs a newly allocated KeyStoreBean object that represents an
	 * in-memory collection of keys and credentials.
	 */
	public KeyStoreBean() {
		super();

		keyStoreAliases = ObservableCollections
				.observableList(new ArrayList<CredentialBean>());
		try {
			initialiseDefaultKeystore();
		} catch (Exception e) {
			/* Cannot initialise allowed selection */;
		}
	}

	/**
	 * Returns the password of the key store.
	 * 
	 * @return the key store password.
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Sets the password of the key store.
	 * 
	 * @param password
	 *            key store password.
	 */
	public void setPassword(String password) {
		String oldPassword = this.password;

		this.password = password;

		firePropertyChange("password", oldPassword, password);
	}

	/**
	 * Returns the ABR key store object.
	 * 
	 * @return the ABR key store object.
	 */
	public ABRKeyStore getKeyStore() {
		return this.keyStore;
	}

	/**
	 * Sets the ABR key store object.
	 * 
	 * @param keyStore
	 *            ABR key store object.
	 */
	public void setKeyStore(ABRKeyStore keyStore) {
		ABRKeyStore oldKeyStore = this.keyStore;

		this.keyStore = keyStore;

		firePropertyChange("keyStore", oldKeyStore, keyStore);
	}

	/**
	 * Returns the name of the ABR key store.
	 * 
	 * @return the ABR key store name.
	 */
	public String getKeyStoreName() {
		return keyStoreName;
	}

	/**
	 * Sets the name of the ABR key store.
	 * 
	 * @param keyStoreName
	 *            ABR key store name.
	 */
	public void setKeyStoreName(String keyStoreName) {
		String oldKeyStoreName = this.keyStoreName;

		this.keyStoreName = keyStoreName;

		firePropertyChange("keyStoreName", oldKeyStoreName, keyStoreName);
	}

	/**
	 * Populates and returns a list with all the key store aliases.
	 * 
	 * @return the list of key store aliases.
	 */
	public ObservableList<CredentialBean> getAliases() {
		return this.keyStoreAliases;
	}

	/**
	 * Sets the list of key store aliases that can be selected to use in a
	 * request submission.
	 * 
	 * @param keyStoreAliases
	 *            list of key store aliases.
	 */
	public void setAliases(ObservableList<CredentialBean> keyStoreAliases) {
		ObservableList<CredentialBean> oldAliases = this.keyStoreAliases;

		this.keyStoreAliases = keyStoreAliases;

		firePropertyChange("keyStoreAliases", oldAliases, keyStoreAliases);
	}

	/**
	 * Returns the ABR credential bean of the selected key store aliases.
	 * 
	 * @return the selected ABR credential.
	 */
	public CredentialBean getAlias() {
		if (keyStoreAliases == null) {
			return null;
		}
		for (Iterator<CredentialBean> iterator = keyStoreAliases.iterator(); iterator
				.hasNext();) {
			CredentialBean cb = (CredentialBean) iterator.next();
			if (cb.isSelected()) {
				return cb;
			}
		}
		return null;
	}

	/**
	 * Sets the selected ABR credential bean of the key store aliases.
	 * 
	 * @param credentialBean
	 *            ABR credential.
	 */
	public void setAlias(CredentialBean credentialBean) {
		for (Iterator<CredentialBean> iterator = keyStoreAliases.iterator(); iterator
				.hasNext();) {
			CredentialBean cb = (CredentialBean) iterator.next();
			if (cb == credentialBean) {
				cb.setSelected(true);
			} else {
				cb.setSelected(false);
			}

		}
	}

	/**
	 * Returns the X509 certificate of the ABR credential of the selected key
	 * store aliases.
	 * 
	 * @return the X509 certificate.
	 */
	public X509Certificate getSelectedCertificate() 
			throws CertificateChainException, InvalidP7CException {
		return getAlias().getCredential().getX509Certificate();
	}

	/**
	 * Returns the private key of the ABR credential of the selected key store
	 * aliases.
	 * 
	 * @return the private key.
	 * @throws NoSuchAliasException
	 * @throws IncorrectPasswordException
	 */
	public PrivateKey getPrivateKey() throws NoSuchAliasException,
			IncorrectPasswordException {
		if (keyStore != null && getAlias() != null) {
			if (password == null) {
				password = "";
			}
			
			ABRCredential credential = getAlias().getCredential();
			try {
				if (credential.isReadyForRenewal())
				{
					credential.renew(password.toCharArray());
				}

				return credential.getPrivateKey(password.toCharArray());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}


	/**
	 * Sets the key store and list of ABR user credentials loaded from the xml
	 * keystore file.
	 * 
	 * @param keystoreFile
	 *            xml keystore file.
	 */
	public void setKeyStoreFile(File keystoreFile) {
		keyStoreAliases.clear();

		try 
		{
			ABRProperties.setSoftwareInfo("ATO CSOU", "SBRCoreServicesTesting", "12.34.0.56", "30 April 2015");

			InputStream is = new FileInputStream(keystoreFile); 
			keyStore = new ABRKeyStore(is);
			List<ABRCredential> abrCreds = keyStore.getCurrentCredentials();
			setCredentials(abrCreds);
			setKeyStoreName(keystoreFile.getAbsolutePath());
		} 
		catch (IOException ex) {
			Logger.getLogger(KeyStoreBean.class.getName()).log(Level.SEVERE,
					null, ex);
			setKeyStoreName("Not updated");
			throw new RuntimeException("Error loading default ABR KeyStore",ex);
			
		}
		catch (NullReferenceException ex) {
			Logger.getLogger(KeyStoreBean.class.getName()).log(Level.SEVERE,
					null, ex);
			setKeyStoreName("Not updated");
			throw new RuntimeException("Error loading default ABR KeyStore",ex);			
		}
		catch (KeyStoreLoadException ex)
		{
			Logger.getLogger(KeyStoreBean.class.getName()).log(Level.SEVERE,
					null, ex);
			setKeyStoreName("Not updated");
			throw new RuntimeException("Error loading default ABR KeyStore",ex);
		}
		catch (SDKExpiredException ex) {
			Logger.getLogger(KeyStoreBean.class.getName()).log(Level.SEVERE,
					null, ex);
			setKeyStoreName("Not updated");
			throw new RuntimeException("Error loading default ABR KeyStore",ex);			
		}
		catch (RuntimeException ex) {
			Logger.getLogger(KeyStoreBean.class.getName()).log(Level.SEVERE,
					null, ex);
			setKeyStoreName("Not updated");
			throw new RuntimeException(ex);
		}

	}

	/**
	 * Attempts to load the key store and list of ABR user credentials from an
	 * xml keystore file in a default location.
	 */
	private void initialiseDefaultKeystore() {
		try {
			// Load the default keystore
			ABRProperties.setSoftwareInfo("ATO CSOU", "SBRCoreServicesTesting", "12.34.0.56", "30 April 2015");
			keyStore = new ABRKeyStore();

			String pathSep = File.separator;
			// Preload the preference file
			setKeyStoreFile(new File(System.getProperty("user.dir")+pathSep+"keystore"+pathSep+"KeyStore.xml"));
			List<ABRCredential> abrCreds = keyStore.getCurrentCredentials();
			setCredentials(abrCreds);

		} catch (NullReferenceException ex) {
			Logger.getLogger(KeyStoreBean.class.getName()).log(Level.SEVERE,
					null, ex);
			throw new RuntimeException("Error loading default ABR KeyStore", ex);			
		} catch (SDKExpiredException ex) {
			Logger.getLogger(KeyStoreBean.class.getName()).log(Level.SEVERE,
					null, ex);
			throw new RuntimeException("Error loading default ABR KeyStore", ex);			
		}
		catch (RuntimeException ex) {
			Logger.getLogger(KeyStoreBean.class.getName()).log(Level.SEVERE,
					null, ex);
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Sets the selected key store alias and associated ABR credential.
	 * 
	 * @param credentials
	 *            list of ABR credentials.
	 */
	private void setCredentials(List<ABRCredential> credentials) {
		keyStoreAliases.clear();

		for (Iterator<ABRCredential> iterator = credentials.iterator(); iterator
				.hasNext();) {
			ABRCredential credential = iterator.next();
			try {
				keyStoreAliases.add(new CredentialBean(credential));
			} catch (Exception e) {
				//Ignore and do nothing
			}
		}
		if (keyStoreAliases.size() > 0) {
			keyStoreAliases.get(0).setSelected(true);
		}

		if (keyStoreAliases.size() > 0) {
			keyStoreAliases.get(0).setSelected(true);
		}		
	}
}
