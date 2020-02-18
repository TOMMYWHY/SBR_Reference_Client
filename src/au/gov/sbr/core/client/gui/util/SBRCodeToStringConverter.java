
package au.gov.sbr.core.client.gui.util;

import org.jdesktop.beansbinding.Converter;

import au.gov.sbr.core.requester.exceptions.SBRCoreException.SBRCode;

/**
 * The SBR Code to String converter acts as an interpreter between the java bound
 * GUI widgets and the actual data from the SBDM.
 * 
 *@author SBR
 */
public class SBRCodeToStringConverter extends Converter<SBRCode, String> 
{
	@Override
	public String convertForward(SBRCode code) {
		return code.getValue();
	}

	@Override
	public SBRCode convertReverse(String arg0) {
		return null;
	}
}
