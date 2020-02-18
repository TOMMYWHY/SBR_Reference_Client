
package au.gov.sbr.core.client.gui.util;

import org.jdesktop.beansbinding.Converter;

import au.gov.sbr.comn.sbdm_02.MessageTimestampGenerationSourceCodeType;

/**
 * This class converts MessageTimestampGenerationSourceCodeType from messages
 * into a specifically formatted String, and the reverse.
 * 
 * @author SBR
 */
public class MessageTimestampGenerationSourceCodeTypeToStringConverter extends
		Converter<MessageTimestampGenerationSourceCodeType, String> {

	/**
	 * Returns the formatted message derived from the message timestamp
	 * generation source code.
	 * 
	 * @param value
	 *            message timestamp generation source code.
	 * @return the converted message.
	 */
	@Override
	public String convertForward(MessageTimestampGenerationSourceCodeType value) {
		if (value == null) {
			return "";
		}

		return value.value();
	}

	/**
	 * Returns the message timestamp generation source code derived from the
	 * converted message.
	 * 
	 * @param value
	 *            converted message.
	 * @return the timestamp generation source code.
	 */
	@Override
	public MessageTimestampGenerationSourceCodeType convertReverse(String value) {
		return MessageTimestampGenerationSourceCodeType.fromValue(value);
	}
}
