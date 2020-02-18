
package au.gov.sbr.core.client.gui.util;

import au.gov.sbr.comn.sbdm_02.MessageEventItemSeverityCodeType;
import org.jdesktop.beansbinding.Converter;

/**
 * This class converts MessageEventItemSeverityCodeType from messages into a
 * specifically formatted String, and the reverse.
 * 
 * @author SBR
 */
public class MessageEventItemSeverityCodeTypeToStringConverter extends
		Converter<MessageEventItemSeverityCodeType, String> {

	/**
	 * Returns the formatted message derived from the message event item
	 * severity code.
	 * 
	 * @param value
	 *            message event item severity code.
	 * @return the converted message.
	 */
	@Override
	public String convertForward(MessageEventItemSeverityCodeType value) {
		if (value == null) {
			return "";
		}
		return value.value();
	}

	/**
	 * Returns the message event item severity code derived from the converted
	 * message.
	 * 
	 * @param value
	 *            converted message.
	 * @return the message event item severity code.
	 */
	@Override
	public MessageEventItemSeverityCodeType convertReverse(String value) {
		return MessageEventItemSeverityCodeType.fromValue(value);
	}
}
