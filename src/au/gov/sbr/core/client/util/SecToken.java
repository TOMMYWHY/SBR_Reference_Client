
package au.gov.sbr.core.client.util;

import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import au.gov.abr.securitytokenmanager.exceptions.STMCommunicationException;
import au.gov.abr.securitytokenmanager.exceptions.STMException;
import au.gov.abr.securitytokenmanager.exceptions.STMFaultException;
import au.gov.abr.securitytokenmanager.exceptions.STMTimeoutException;
import au.gov.abr.securitytokenmanager.SecurityTokenManagerClient;
import au.gov.abr.securitytokenmanager.STSClient;
import au.gov.abr.securitytokenmanager.VANguardSTSClient;
import au.gov.abr.securitytokenmanager.SecurityToken;

/**
 * Class used to obtain a token from the STS.
 */
public class SecToken 
{
	/**
	 * Create an instance of SecToken.
	 */
	public SecToken() {}

	/**
	 * Get a security token from Vanguard to use for secure authentication and communication
	 * with the relevant core web service.
	 * @param endpointUrl
	 * @param stsEndpoint
	 * @param certificateChain
	 * @param privateKey
	 * @return SecurityToken
	 * @throws STMException
	 */
	public SecurityToken getSecurityToken(String endpointUrl, String stsEndpoint, Certificate[] certificateChain, PrivateKey privateKey) 
			throws STMFaultException, STMCommunicationException, STMTimeoutException, STMException
	{
		SecurityTokenManagerClient stm = new SecurityTokenManagerClient();
		STSClient sts = stm.getImplementingClass();
		if (sts instanceof VANguardSTSClient) {
			VANguardSTSClient vgClient = (VANguardSTSClient) sts;

			vgClient.setEndpoint(stsEndpoint);
			vgClient.setKeySize("512");
			vgClient.setTTL("30");
		}

		ResourceBundle resources = ResourceBundle.getBundle(SecToken.class.getName());

		int counter = 1;
		int claimsCount = Integer.parseInt(resources.getString("claims.count"));
		List<String> claimList = new ArrayList<String>();

		while (counter <= claimsCount) {
			claimList.add(resources.getString("claim" + counter + ".url"));
			counter++;
		}

		String claims = "";
		for (String claim : claimList)
			claims += claim + ";";
		
		return stm!= null ? stm.getSecurityIdentityToken(endpointUrl, claims, privateKey, certificateChain) : null;
	}
}
