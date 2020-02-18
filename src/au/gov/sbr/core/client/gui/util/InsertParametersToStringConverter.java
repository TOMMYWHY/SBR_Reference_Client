
package au.gov.sbr.core.client.gui.util;

import java.util.Iterator;
import java.util.List;

import org.jdesktop.beansbinding.Converter;

import au.gov.sbr.comn.sbdm_02.MessageEventItemParameterType;
import au.gov.sbr.comn.sbdm_02.MessageEventItemType;

/**
 * This class converts MessageEventItemParameters from messages into a specifically
 * formatted String, and the reverse.
 * 
 * @author SBR
 */
public class InsertParametersToStringConverter extends
		Converter<MessageEventItemType, String> {

	private String type;

	/**
	 * Constructs a newly allocated InsertParametersToStringConverter object
	 * that represents the message event item type.
	 * 
	 * @param type
	 *            message event item type.
	 */
	public InsertParametersToStringConverter(String type) {
		this.type = type;
	}

	/**
	 * Returns the formatted message derived from the message event item.
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

		String desc = "short".equalsIgnoreCase(type) ? value
				.getMessageEventItemShortDescription() : value
				.getMessageEventItemDetailedDescription();
		if (value.getParameters() != null) {
			List<MessageEventItemParameterType> paramList = value
					.getParameters().getParameter();
			for (Iterator<MessageEventItemParameterType> iterator = paramList
					.iterator(); iterator.hasNext();) {
				MessageEventItemParameterType param = iterator.next();
				String id = param.getMessageEventItemParameterIdentifier();
				String text = param.getMessageEventItemParameterText();
				desc = desc.replaceAll("\\{" + id + "\\}", text);
			}
		}
		return desc;
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
