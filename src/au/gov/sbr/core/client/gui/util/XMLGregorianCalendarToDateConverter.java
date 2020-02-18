
package au.gov.sbr.core.client.gui.util;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.datatype.DatatypeConfigurationException;

import javax.xml.datatype.XMLGregorianCalendar;

import org.jdesktop.beansbinding.Converter;

import javax.xml.datatype.DatatypeFactory;

/**
 * This class converts XML Gregorian Calendar datetime from messages into a Date
 * object, and the reverse.
 * 
 * @author SBR
 */
public class XMLGregorianCalendarToDateConverter extends
		Converter<XMLGregorianCalendar, Date> {

	private static final DatatypeFactory dtf;

	static {
		try {
			dtf = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException ex) {
			Logger.getLogger(
					XMLGregorianCalendarToDateConverter.class.getName()).log(
					Level.SEVERE, null, ex);
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Returns the Date object derived from the XML Gregorian Calendar datetime.
	 * 
	 * @param value
	 *            XML Gregorian Calendar datetime.
	 * @return the Date object.
	 */
	@Override
	public Date convertForward(XMLGregorianCalendar value) {
		if (value == null) {
			return new Date();
		}

		return value.toGregorianCalendar().getTime();
	}

	/**
	 * Returns the XML Gregorian Calendar datetime derived from the Date object.
	 * 
	 * @param value
	 *            Date object.
	 * @return the XML Gregorian Calendar datetime.
	 */
	@Override
	public XMLGregorianCalendar convertReverse(Date value) {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(value);
		return dtf.newXMLGregorianCalendar(cal);
	}
}
