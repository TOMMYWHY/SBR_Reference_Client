
package au.gov.sbr.core.client.gui.util;

import org.jdesktop.beansbinding.Converter;

import au.gov.sbr.comn.sbdm_02.MessageEventItemType;

/**
 * This class converts MessageEventItem from messages into a specifically
 * formatted String, and the reverse.
 * 
 * @author SBR
 */
public class MessageEventItemToStringConverter extends
		Converter<MessageEventItemType, String> {

	/**
	 * Returns the formatted message derived from the message event item
	 * error code. Only the MessageEventItemErrorCode is retained.
	 * 
	 * @param value
	 *            message event item.
	 * @return the converted message.
	 */
	@Override
	public String convertForward(MessageEventItemType value) {
		if (value == null) {
			return "";
		}

		return value.getMessageEventItemErrorCode();
	}

	/**
	 * Returns the message event item derived from the converted message.
	 * 
	 * @param value
	 *            converted message.
	 * @return null.
	 */
	@Override
	public MessageEventItemType convertReverse(String value) {
		return null;
	}
}
