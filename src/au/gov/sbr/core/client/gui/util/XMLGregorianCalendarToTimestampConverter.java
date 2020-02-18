
package au.gov.sbr.core.client.gui.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.XMLGregorianCalendar;

import org.jdesktop.beansbinding.Converter;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

/**
 * This class converts XML Gregorian Calendar datetime from messages into a
 * timestamp of SimpleDateFormat object, and the reverse.
 * 
 * @author SBR
 */
public class XMLGregorianCalendarToTimestampConverter extends
		Converter<XMLGregorianCalendar, String> {

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

	private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
			"dd/MM/yyyy hh:mm:ss a");

	/**
	 * Returns the timestamp of SimpleDateFormat object derived from the XML
	 * Gregorian Calendar datetime.
	 * 
	 * @param value
	 *            XML Gregorian Calendar datetime.
	 * @return the timestamp of SimpleDateFormat object.
	 */
	@Override
	public String convertForward(XMLGregorianCalendar value) {
		if (value == null) {
			return "";
		}

		return simpleDateFormat.format(value.toGregorianCalendar().getTime());
	}

	/**
	 * Returns the XML Gregorian Calendar datetime derived from the timestamp of
	 * SimpleDateFormat object.
	 * 
	 * @param value
	 *            timestamp of SimpleDateFormat object.
	 * @return the XML Gregorian Calendar datetime.
	 */
	@Override
	public XMLGregorianCalendar convertReverse(String value) {
		Date valDate = new Date();
		try {
			valDate = simpleDateFormat.parse(value);
		} catch (ParseException e) {
			/* Ignore */
		}
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(valDate);

		return dtf.newXMLGregorianCalendar(cal);
	}
}
