
package au.gov.sbr.core.forms;

//import au.gov.sbr.core.services.ServiceType;
import au.gov.sbr.core.client.gui.JRCServiceType;
import java.util.Properties;

/**
 * This class represents an SBR Form in the GUI. Since forms can be used in a
 * list it provides a selection and display name properties.
 * 
 * @author SBR
 */
public class Form {

	/**
	 * The key to use to retrieve the Name value from the properties file.
	 */
	private static final String NameResourceKey = "Name";
	/**
	 * The key to use to retrieve the Receiver value from the properties file.
	 */
	private static final String ReceiverResourceKey = "Receiver";
	/**
	 * The key to use to retrieve the List Message Request Type value from the
	 * properties file.
	 */
	private static final String ListMessageRequestTypeResourceKey = "ListMessageRequestType";
	/**
	 * The key to use to retrieve the Lodge Message Request Type value from the
	 * properties file.
	 */
	private static final String LodgeMessageRequestTypeResourceKey = "LodgeMessageRequestType";
	/**
	 * The key to use to retrieve the Prelodge Message Request Type value from
	 * the properties file.
	 */
	private static final String PrelodgeMessageRequestTypeResourceKey = "PreLodgeMessageRequestType";
	/**
	 * The key to use to retrieve the Prefill Message Request Type value from
	 * the properties file.
	 */
	private static final String PrefillMessageRequestTypeResourceKey = "PreFillMessageRequestType";
	/**
	 * The key to use to retrieve the Alternate List Message Request Type value
	 * from the properties file.
	 */
	private static final String AltListMessageRequestTypeResourceKey = "AltListMessageRequestType";
	/**
	 * The key to use to retrieve the Alternate Lodge Message Request Type value
	 * from the properties file.
	 */
	private static final String AltLodgeMessageRequestTypeResourceKey = "AltLodgeMessageRequestType";

	/**
	 * The key to use to retrieve the List Message Response Type value from the
	 * properties file.
	 */
	private static final String ListMessageResponseTypeResourceKey = "ListMessageResponseType";
	/**
	 * The key to use to retrieve the Lodge Message Response Type value from the
	 * properties file.
	 */
	private static final String LodgeMessageResponseTypeResourceKey = "LodgeMessageResponseType";
	/**
	 * The key to use to retrieve the Prelodge Message Response Type value from
	 * the properties file.
	 */
	private static final String PrelodgeMessageResponseTypeResourceKey = "PreLodgeMessageResponseType";
	/**
	 * The key to use to retrieve the Prefill Message Response Type value from
	 * the properties file.
	 */
	private static final String PrefillMessageResponseTypeResourceKey = "PreFillMessageResponseType";
	/**
	 * The key to use to retrieve the Alternate List Message Response Type value
	 * from the properties file.
	 */
	private static final String AltListMessageResponseTypeResourceKey = "AltListMessageResponseType";
	/**
	 * The key to use to retrieve the Alternate Lodge Message Response Type
	 * value from the properties file.
	 */
	private static final String AltLodgeMessageResponseTypeResourceKey = "AltLodgeMessageResponseType";

	/**
	 * The key to use to retrieve the List Message Text value from the
	 * properties file.
	 */
	private static final String ListMessageTextResourceKey = "ListMessageText";
	/**
	 * The key to use to retrieve the Lodge Message Text value from the
	 * properties file.
	 */
	private static final String LodgeMessageTextResourceKey = "LodgeMessageText";
	/**
	 * The key to use to retrieve the Prelodge Message Text value from the
	 * properties file.
	 */
	private static final String PrelodgeMessageTextResourceKey = "PreLodgeMessageText";
	/**
	 * The key to use to retrieve the Prefill Message Text value from the
	 * properties file.
	 */
	private static final String PrefillMessageTextResourceKey = "PreFillMessageText";
	/**
	 * The key to use to retrieve the Alternate List Message Text value from the
	 * properties file.
	 */
	private static final String AltListMessageTextResourceKey = "AltListMessageText";
	/**
	 * The key to use to retrieve the Alternate Lodge Message Text value from
	 * the properties file.
	 */
	private static final String AltLodgeMessageTextResourceKey = "AltLodgeMessageText";

	private String name;
	private String receiver;
	private String listMessageRequestType;
	private String listMessageResponseType;
	private String listMessageText;
	private String lodgeMessageRequestType;
	private String lodgeMessageResponseType;
	private String lodgeMessageText;
	private String prefillMessageRequestType;
	private String prefillMessageResponseType;
	private String prefillMessageText;
	private String prelodgeMessageRequestType;
	private String prelodgeMessageResponseType;
	private String prelodgeMessageText;
	private String altListMessageRequestType;
	private String altListMessageResponseType;
	private String altListMessageText;
	private String altLodgeMessageRequestType;
	private String altLodgeMessageResponseType;
	private String altLodgeMessageText;

	/**
	 * Constructs a newly allocated Form object that represents an SBR Form in
	 * the GUI.
	 * 
	 * @param name
	 *            name of the form.
	 * @param receiver
	 *            agency end point in which to send a request.
	 * @param listMessageRequestType
	 *            message type text of the list service request.
	 * @param listMessageResponseType
	 *            message type text of the list service response.
	 * @param lodgeMessageRequestType
	 *            message type text of the lodge service request.
	 * @param lodgeMessageResponseType
	 *            message type text of the lodge service response.
	 * @param prefillMessageRequestType
	 *            message type text of the prefill service request.
	 * @param prefillMessageResponseType
	 *            message type text of the prefill service response.
	 * @param prelodgeMessageRequestType
	 *            message type text of the prelodge service request.
	 * @param prelodgeMessageResponseType
	 *            message type text of the prelodge service response.
	 * @param altListMessageRequestType
	 *            message type text of the alternate list service request.
	 * @param altListMessageResponseType
	 *            message type text of the alternate list service response.
	 * @param altLodgeMessageRequestType
	 *            message type text of the alternate lodge service request.
	 * @param altLodgeMessageResponseType
	 *            message type text of the alternate lodge service response.
	 */
	public Form(String name, String receiver, String listMessageRequestType,
			String listMessageResponseType, String lodgeMessageRequestType,
			String lodgeMessageResponseType, String prefillMessageRequestType,
			String prefillMessageResponseType,
			String prelodgeMessageRequestType,
			String prelodgeMessageResponseType,
			String altListMessageRequestType,
			String altListMessageResponseType,
			String altLodgeMessageRequestType,
			String altLodgeMessageResponseType) {
		if (name == null || "".equals(name))
			throw new IllegalArgumentException("name");

		if (receiver == null || "".equals(receiver))
			throw new IllegalArgumentException("receiver");

		this.name = name;
		this.receiver = receiver;
		this.listMessageRequestType = listMessageRequestType;
		this.listMessageResponseType = listMessageResponseType;
		this.lodgeMessageRequestType = lodgeMessageRequestType;
		this.lodgeMessageResponseType = lodgeMessageResponseType;
		this.prefillMessageRequestType = prefillMessageRequestType;
		this.prefillMessageResponseType = prefillMessageResponseType;
		this.prelodgeMessageRequestType = prelodgeMessageRequestType;
		this.prelodgeMessageResponseType = prelodgeMessageResponseType;
		this.altListMessageRequestType = listMessageRequestType;
		this.altListMessageResponseType = listMessageResponseType;
		this.altLodgeMessageRequestType = lodgeMessageRequestType;
		this.altLodgeMessageResponseType = lodgeMessageResponseType;
	}

	/**
	 * Retrieves the Form values from the corresponding properties file.
	 * 
	 * @param properties
	 *            form properties file.
	 */
	Form(Properties properties) {
		this.name = properties.getProperty(NameResourceKey, null);
		this.receiver = properties.getProperty(ReceiverResourceKey, null);

		if (this.name == null || "".equals(this.name))
			throw new IllegalArgumentException("name");

		if (this.receiver == null || "".equals(this.receiver))
			throw new IllegalArgumentException("receiver");

		this.listMessageRequestType = properties.getProperty(
				ListMessageRequestTypeResourceKey, "");
		this.listMessageResponseType = properties.getProperty(
				ListMessageResponseTypeResourceKey, "");
		this.listMessageText = properties.getProperty(
				ListMessageTextResourceKey, "");

		this.lodgeMessageRequestType = properties.getProperty(
				LodgeMessageRequestTypeResourceKey, "");
		this.lodgeMessageResponseType = properties.getProperty(
				LodgeMessageResponseTypeResourceKey, "");
		this.lodgeMessageText = properties.getProperty(
				LodgeMessageTextResourceKey, "");

		this.prefillMessageRequestType = properties.getProperty(
				PrefillMessageRequestTypeResourceKey, "");
		this.prefillMessageResponseType = properties.getProperty(
				PrefillMessageResponseTypeResourceKey, "");
		this.prefillMessageText = properties.getProperty(
				PrefillMessageTextResourceKey, "");

		this.prelodgeMessageRequestType = properties.getProperty(
				PrelodgeMessageRequestTypeResourceKey, "");
		this.prelodgeMessageResponseType = properties.getProperty(
				PrelodgeMessageResponseTypeResourceKey, "");
		this.prelodgeMessageText = properties.getProperty(
				PrelodgeMessageTextResourceKey, "");

		this.altListMessageRequestType = properties.getProperty(
				AltListMessageRequestTypeResourceKey, "");
		this.altListMessageResponseType = properties.getProperty(
				AltListMessageResponseTypeResourceKey, "");
		this.altListMessageText = properties.getProperty(
				AltListMessageTextResourceKey, "");

		this.altLodgeMessageRequestType = properties.getProperty(
				AltLodgeMessageRequestTypeResourceKey, "");
		this.altLodgeMessageResponseType = properties.getProperty(
				AltLodgeMessageResponseTypeResourceKey, "");
		this.altLodgeMessageText = properties.getProperty(
				AltLodgeMessageTextResourceKey, "");
	}

	/**
	 * Returns the name of the form.
	 * 
	 * @return the name of the form.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the agency end point in which to send a request.
	 * 
	 * @return the agency end point.
	 */
	public String getReceiver() {
		return receiver;
	}

	/**
	 * Returns the message type text of the list service request.
	 * 
	 * @return the message type text of request.
	 */
	public String getListMessageRequestType() {
		return listMessageRequestType;
	}

	/**
	 * Returns the message type text of the list service response.
	 * 
	 * @return the message type text of response.
	 */
	public String getListMessageResponseType() {
		return listMessageResponseType;
	}

	/**
	 * Returns the interaction label of the list service request.
	 * 
	 * @return the interaction label of request.
	 */
	public String getListMessageText() {
		return listMessageText;
	}

	/**
	 * Returns the message type text of the lodge service request.
	 * 
	 * @return the message type text of request.
	 */
	public String getLodgeMessageRequestType() {
		return lodgeMessageRequestType;
	}

	/**
	 * Returns the message type text of the lodge service response.
	 * 
	 * @return the message type text of response.
	 */
	public String getLodgeMessageResponseType() {
		return lodgeMessageResponseType;
	}

	/**
	 * Returns the interaction label of the lodge service request.
	 * 
	 * @return the interaction label of request.
	 */
	public String getLodgeMessageText() {
		return lodgeMessageText;
	}

	/**
	 * Returns the message type text of the prefill service request.
	 * 
	 * @return the message type text of request.
	 */
	public String getPrefillMessageRequestType() {
		return prefillMessageRequestType;
	}

	/**
	 * Returns the message type text of the prefill service response.
	 * 
	 * @return the message type text of response.
	 */
	public String getPrefillMessageResponseType() {
		return prefillMessageResponseType;
	}

	/**
	 * Returns the interaction label of the prefill service request.
	 * 
	 * @return the interaction label of request.
	 */
	public String getPrefillMessageText() {
		return prefillMessageText;
	}

	/**
	 * Returns the message type text of the prelodge service request.
	 * 
	 * @return the message type text of request.
	 */
	public String getPrelodgeMessageRequestType() {
		return prelodgeMessageRequestType;
	}

	/**
	 * Returns the message type text of the prelodge service response.
	 * 
	 * @return the message type text of response.
	 */
	public String getPrelodgeMessageResponseType() {
		return prelodgeMessageResponseType;
	}

	/**
	 * Returns the interaction label of the prelodge service request.
	 * 
	 * @return the interaction label of request.
	 */
	public String getPrelodgeMessageText() {
		return prelodgeMessageText;
	}

	/**
	 * Returns the message type text of the alternate list service request.
	 * 
	 * @return the message type text of request.
	 */
	public String getAltListMessageRequestType() {
		return altListMessageRequestType;
	}

	/**
	 * Returns the message type text of the alternate list service response.
	 * 
	 * @return the message type text of response.
	 */
	public String getAltListMessageResponseType() {
		return altListMessageResponseType;
	}

	/**
	 * Returns the interaction label of the alternate list service request.
	 * 
	 * @return the interaction label of request.
	 */
	public String getAltListMessageText() {
		return altListMessageText;
	}

	/**
	 * Returns the message type text of the alternate lodge service request.
	 * 
	 * @return the message type text of request.
	 */
	public String getAltLodgeMessageRequestType() {
		return altLodgeMessageRequestType;
	}

	/**
	 * Returns the message type text of the alternate lodge service response.
	 * 
	 * @return the message type text of response.
	 */
	public String getAltLodgeMessageResponseType() {
		return altLodgeMessageResponseType;
	}

	/**
	 * Returns the interaction label of the alternate lodge service request.
	 * 
	 * @return the interaction label of request.
	 */
	public String getAltLodgeMessageText() {
		return altLodgeMessageText;
	}

	/**
	 * Returns the request message type text of the form based on the service
	 * type of the interaction.
	 * 
	 * @param serviceType
	 *            service type of request
	 * @return the message type text of request.
	 */
	public String getRequestType(JRCServiceType serviceType) {
		switch (serviceType) {
			case LIST:
				return listMessageRequestType;
			case LODGE:
				return lodgeMessageRequestType;
			case PREFILL:
				return prefillMessageRequestType;
			case PRELODGE:
				return prelodgeMessageRequestType;
			case ALTLIST:
				return altListMessageRequestType;
			case ALTLODGE:
				return altLodgeMessageRequestType;
			default:
				return ""; // Should never happen
		}
	}

	/**
	 * Returns the response message type text of the form based on the service
	 * type of the interaction.
	 * 
	 * @param serviceType
	 *            service type of request
	 * @return the message type text of response.
	 */
	public String getResponseType(JRCServiceType serviceType) {
		switch (serviceType) {
			case LIST:
				return listMessageResponseType;
			case LODGE:
				return lodgeMessageRequestType;
			case PREFILL:
				return prefillMessageRequestType;
			case PRELODGE:
				return prelodgeMessageRequestType;
			case ALTLIST:
				return altListMessageResponseType;
			case ALTLODGE:
				return altLodgeMessageRequestType;
			default:
				return ""; // Should never happen
		}
	}
}
