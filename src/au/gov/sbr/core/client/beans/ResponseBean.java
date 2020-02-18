
package au.gov.sbr.core.client.beans;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.observablecollections.ObservableList;

import au.gov.sbr.comn.sbdm_02.AttachmentInstanceType;
import au.gov.sbr.comn.sbdm_02.AttachmentType;
import au.gov.sbr.comn.sbdm_02.BusinessDocumentInstanceType;
import au.gov.sbr.comn.sbdm_02.BusinessDocumentType;
import au.gov.sbr.comn.sbdm_02.MessageEventItemType;
import au.gov.sbr.comn.sbdm_02.MessageTimestampType;
import au.gov.sbr.comn.sbdm_02.StandardBusinessDocumentMessageType;
import au.gov.sbr.core.client.gui.JRCServiceType;
import au.gov.sbr.core.requester.exceptions.SBRCoreException;
import au.gov.sbr.core.requester.exceptions.SBRCoreException.SBRCode;
import au.gov.sbr.core.requester.model.Request;

/**
 * This bean represents the request/response pair. It contains the SBDM from the
 * request, and the SBDM (if any) of the response or, in the case of an error,
 * the Exception that was raised while trying to receive a response.
 * 
 * Since the StandardBusinessDocumentMessageType as generated in the SBR 2011
 * Core Services Requester already contains property change notification, no beans for
 * the SBDM have been created.
 * 
 * @author SBR
 */
public class ResponseBean extends BeanBase {

	private static final long serialVersionUID = -5722145716506942374L;
	private StandardBusinessDocumentMessageType request;
	private StandardBusinessDocumentMessageType response;
	private boolean responseOutstanding;
	private MessageEventItemType selectedMessageEventItem;
	private ObservableList<BusinessDocumentBean> businessDocuments;
	private BusinessDocumentBean selectedBusinessDocument;
	private String status;
	private SOAPFaultBean soapFault;
	private List<SBRCode> selectedSOAPsubcodes;
	private String details;
	private long millis;
	private Request coreRequest;
	private int interval;
	private JRCServiceType serviceType;

	/**
	 * Constructs a newly allocated ResponseBean object that represents the
	 * Standard Business Document Message (SBDM) request type.
	 * 
	 * @param request
	 *            SBDM request type.
	 */
	ResponseBean(StandardBusinessDocumentMessageType request) {
		super();

		if (request == null) {
			throw new IllegalArgumentException("request");
		}

		this.request = request;
		this.responseOutstanding = true;
		businessDocuments = ObservableCollections
				.observableList(new ArrayList<BusinessDocumentBean>());
	}

	public ResponseBean(Request request) throws SBRCoreException {
		this(request.getSBDM());
		coreRequest = request;
	}

	/**
	 * Returns the timestamp of the request message, in Gregorian Calendar
	 * format.
	 * 
	 * @return the request message timestamp.
	 */
	public XMLGregorianCalendar getRequestTimestamp() {
		if (this.request == null
				|| this.request.getStandardBusinessDocumentHeader() == null
				|| this.request.getStandardBusinessDocumentHeader()
						.getMessageTimestamps() == null
				|| this.request.getStandardBusinessDocumentHeader()
						.getMessageTimestamps().getMessageTimestamp() == null
				|| this.request.getStandardBusinessDocumentHeader()
						.getMessageTimestamps().getMessageTimestamp().size() == 0
				|| this.request.getStandardBusinessDocumentHeader()
						.getMessageTimestamps().getMessageTimestamp().get(0) == null
				|| this.request.getStandardBusinessDocumentHeader()
						.getMessageTimestamps().getMessageTimestamp().get(0)
						.getMessageTimestampGenerationDatetime() == null) {
			return null;
		}

		return this.request.getStandardBusinessDocumentHeader()
				.getMessageTimestamps().getMessageTimestamp().get(0)
				.getMessageTimestampGenerationDatetime();
	}

	/**
	 * Clear the request time stamp if the original request is queued for resend.
	 */
	public void clearRequestTimestamp() 
	{
		XMLGregorianCalendar oldTime = this.request.getStandardBusinessDocumentHeader()
		.getMessageTimestamps().getMessageTimestamp().get(0)
		.getMessageTimestampGenerationDatetime();

		this.request.getStandardBusinessDocumentHeader().getMessageTimestamps().
			getMessageTimestamp().get(0).setMessageTimestampGenerationDatetime(null);

		firePropertyChange("requestTimestamp", oldTime, null);
	}

	/**
	 * Returns the message type text of the request message.
	 * 
	 * @return the request message type text.
	 */
	public String getType() {
		if (responseOutstanding) {
			return request.getStandardBusinessDocumentHeader()
					.getMessageTypeText();
		}

		if (response == null
				|| response.getStandardBusinessDocumentHeader() == null
				|| response.getStandardBusinessDocumentHeader()
						.getLodgementReceipt() == null
				|| response.getStandardBusinessDocumentHeader()
						.getLodgementReceipt().getLodgementReceiptIdentifier() == null) {
			return request.getStandardBusinessDocumentHeader()
					.getMessageTypeText();
		}

		return response.getStandardBusinessDocumentHeader()
				.getLodgementReceipt().getLodgementReceiptIdentifier();
	}

	/**
	 * Returns the processing status of the request message.
	 * 
	 * @return the status of the message request processing.
	 */
	public String getStatus() {
		if (responseOutstanding) {
			return "Outstanding";
		}

		if (response == null
				|| response.getStandardBusinessDocumentHeader() == null
				|| response.getStandardBusinessDocumentHeader()
						.getLodgementReceipt() == null
				|| response.getStandardBusinessDocumentHeader()
						.getLodgementReceipt().getLodgementReceiptIdentifier() == null) {
			if (status != null) {
				return status;
			} else {
				return "Status Unknown";
			}
		}

		return "Response";
	}

	/**
	 * Returns the display name of the response bean. The name displayed will
	 * vary depending on the current status of the message processing. The name
	 * is extracted from the Standard Business Document Header (SBDH) of the
	 * response.
	 * 
	 * @return the display name.
	 */
	@Override
	public String getDisplayName() {

		if (responseOutstanding) {
			return request.getStandardBusinessDocumentHeader()
					.getMessageTypeText()
					+ " outstanding";
		}

		if (response == null
				|| response.getStandardBusinessDocumentHeader() == null
				|| response.getStandardBusinessDocumentHeader()
						.getLodgementReceipt() == null
				|| response.getStandardBusinessDocumentHeader()
						.getLodgementReceipt().getLodgementReceiptIdentifier() == null) {
			return request.getStandardBusinessDocumentHeader()
					.getMessageTypeText()
					+ " returned";
		}

		return "Response "
				+ response.getStandardBusinessDocumentHeader()
						.getLodgementReceipt().getLodgementReceiptIdentifier();
	}

	/**
	 * Sets the SOAP Fault from a response.
	 * @param soapFault
	 */
	public void setSOAPFault(SOAPFaultBean soapFault) {
		SOAPFaultBean oldFault = this.soapFault;
		this.soapFault = soapFault;

		firePropertyChange("soapFault", oldFault, soapFault);
	}

	/**
	 * Returns the SOAP Fault
	 * @return SOAPFault
	 */
	public SOAPFaultBean getSOAPFault() {
		return soapFault;
	}
	
	/**
	 * Returns the Standard Business Document Message (SBDM) request.
	 * 
	 * @return the message request.
	 */
	public StandardBusinessDocumentMessageType getRequest() {
		return request;
	}

	/**
	 * Sets the Standard Business Document Message (SBDM) request.
	 * 
	 * @param request
	 *            message request.
	 */
	public void setRequest(StandardBusinessDocumentMessageType request) {
		StandardBusinessDocumentMessageType oldRequest = this.request;
		this.request = request;

		this.firePropertyChange("request", oldRequest, request);
	}

	/**
	 * Returns the Standard Business Document Message (SBDM) response.
	 * 
	 * @return the message response.
	 */
	public StandardBusinessDocumentMessageType getResponse() {
		return response;
	}

	/**
	 * Sets the Standard Business Document Message (SBDM) response. This
	 * includes setting the display name, timestamps, message event items, the
	 * selected message event item, and business documents of the response
	 * message.
	 * 
	 * @param response
	 *            message response.
	 */
	public void setResponse(StandardBusinessDocumentMessageType response) {
		StandardBusinessDocumentMessageType oldResponse = this.response;
		List<MessageTimestampType> oldResponseTimestamps = getResponseTimestamps();
		List<MessageEventItemType> oldResponseMessageEventItems = getResponseMessageEventItems();
		List<BusinessDocumentBean> oldBusinessDocuments = getBusinessDocuments();
		MessageEventItemType oldSelectedMessageEventItem = getSelectedMessageEventItem();
		String oldDisplayName = getDisplayName();
		SOAPFaultBean oldSOAPBean = getSOAPFault();
		List<String> oldSelectedSOAPsubcodes = getResponseSOAPsubcodes();
		
		this.response = response;
		buildBusinessDocumentList();

		this.firePropertyChange("response", oldResponse, response);
		this
				.firePropertyChange("displayName", oldDisplayName,
						getDisplayName());
		this.firePropertyChange("responseTimestamps", oldResponseTimestamps,
				getResponseTimestamps());
		this.firePropertyChange("responseMessageEventItems",
				oldResponseMessageEventItems, getResponseMessageEventItems());
		this.firePropertyChange("selectedMessageEventItem",
				oldSelectedMessageEventItem, getSelectedMessageEventItem());
		this.firePropertyChange("businessDocuments", oldBusinessDocuments,
				getBusinessDocuments());
		this.firePropertyChange("soapFault", oldSOAPBean, getSOAPFault());
		this.firePropertyChange("soapFaultsubcodes", oldSelectedSOAPsubcodes, getResponseSOAPsubcodes());
	}

	/**
	 * Populates a list with all the response business documents and associated
	 * attachments into a Business Document bean.
	 */
	private void buildBusinessDocumentList() {
		businessDocuments.clear();
		int seq = 0;
		try {
			List<BusinessDocumentType> bdList = response
					.getStandardBusinessDocumentHeader().getBusinessDocuments()
					.getBusinessDocument();
			for (Iterator<BusinessDocumentType> iterator = bdList.iterator(); iterator
					.hasNext();) {
				BusinessDocumentType businessDocumentType = iterator.next();
				BusinessDocumentBean bean = new BusinessDocumentBean(
						businessDocumentType, response
								.getStandardBusinessDocumentBody()
								.getBusinessDocumentInstances()
								.getBusinessDocumentInstance().get(seq));
				this.businessDocuments.add(bean);
				int seq2 = 0;
				List<AttachmentType> attachmentList = businessDocumentType
						.getAttachments().getAttachment();
				for (Iterator<AttachmentType> iterator2 = attachmentList
						.iterator(); iterator2.hasNext();) {
					AttachmentType attachmentType = iterator2.next();
					bean.addAttachment(new AttachmentBean(attachmentType,
							response.getStandardBusinessDocumentBody()
									.getAttachmentInstances()
									.getAttachmentInstance().get(seq2)));
					seq2++;
				}
			}
		} catch (Exception e) {
			/* ignore */
		}
	}

	/**
	 * Determines if a response object is outstanding.
	 * 
	 * @return the status of the check.
	 */
	public boolean isResponseOutstanding() {
		return responseOutstanding;
	}

	/**
	 * Sets the status of whether a response is outstanding.
	 * 
	 * @param responseOutstanding
	 *            status of the response message.
	 */
	public void setResponseOutstanding(boolean responseOutstanding) {
		boolean oldResponseOutstanding = this.responseOutstanding;
		String oldDisplayName = getDisplayName();
		String oldStatus = getStatus();

		this.responseOutstanding = responseOutstanding;

		this.firePropertyChange("responseOutstanding", oldResponseOutstanding,
				responseOutstanding);
		this
				.firePropertyChange("displayName", oldDisplayName,
						getDisplayName());
		this.firePropertyChange("status", oldStatus, getStatus());
	}

	/**
	 * Returns the response timestamp from the standard business document
	 * header.
	 * 
	 * @return the message response timestamp.
	 */
	public String getResponseTimestamp() {
		if (this.response == null
				|| this.response.getStandardBusinessDocumentHeader() == null
				|| this.response.getStandardBusinessDocumentHeader()
						.getMessageTimestamps() == null
				|| this.response.getStandardBusinessDocumentHeader()
						.getMessageTimestamps().getMessageTimestamp() == null
				|| this.response.getStandardBusinessDocumentHeader()
						.getMessageTimestamps().getMessageTimestamp().size() == 0
				|| this.response.getStandardBusinessDocumentHeader()
						.getMessageTimestamps().getMessageTimestamp().get(0) == null
				|| this.response.getStandardBusinessDocumentHeader()
						.getMessageTimestamps().getMessageTimestamp().get(0)
						.getMessageTimestampGenerationDatetime() == null) {
			return null;
		}

		return this.response.getStandardBusinessDocumentHeader()
				.getMessageTimestamps().getMessageTimestamp().get(0)
				.getMessageTimestampGenerationDatetime().toString();
	}

	/**
	 * Populates and returns a list with all the response timestamps from the
	 * standard business document header.
	 * 
	 * @return the list of response timestamps.
	 */
	public List<MessageTimestampType> getResponseTimestamps() {
		if (this.response == null
				|| this.response.getStandardBusinessDocumentHeader() == null
				|| this.response.getStandardBusinessDocumentHeader()
						.getMessageTimestamps() == null
				|| this.response.getStandardBusinessDocumentHeader()
						.getMessageTimestamps().getMessageTimestamp() == null
				|| this.response.getStandardBusinessDocumentHeader()
						.getMessageTimestamps().getMessageTimestamp().size() == 0) {
			return null;
		}

		return this.response.getStandardBusinessDocumentHeader()
				.getMessageTimestamps().getMessageTimestamp();
	}

	/**
	 * Populates and returns a list with all the response SOAP subcodes.
	 * @return List<String>
	 */
	public List<String> getResponseSOAPsubcodes() {
		if (this.getSOAPFault() == null || this.getSOAPFault().getFaultSubcodes() == null)
			return null;

		List<String> list = new ArrayList<String>();
		for (SBRCode code : this.getSOAPFault().getFaultSubcodes()) {
			list.add(code.getValue());
		}

		return list;
	}

	/**
	 * Populates and returns a list with all the response message event items
	 * from the standard business document header.
	 * 
	 * @return the list of message event items.
	 */
	public List<MessageEventItemType> getResponseMessageEventItems() {
		if (this.response == null
				|| this.response.getStandardBusinessDocumentHeader() == null
				|| this.response.getStandardBusinessDocumentHeader()
						.getMessageEvent() == null
				|| this.response.getStandardBusinessDocumentHeader()
						.getMessageEvent().getMessageEventItems() == null
				|| this.response.getStandardBusinessDocumentHeader()
						.getMessageEvent().getMessageEventItems()
						.getMessageEventItem() == null
				|| this.response.getStandardBusinessDocumentHeader()
						.getMessageEvent().getMessageEventItems()
						.getMessageEventItem().size() == 0) {
			List<MessageEventItemType> messageEventItemList = new ArrayList<MessageEventItemType>();
			MessageEventItemType messageEventItem = new MessageEventItemType();
			messageEventItem.setMessageEventItemErrorCode(" ");
			messageEventItemList.add(messageEventItem);
			return messageEventItemList;
		}

		return this.response.getStandardBusinessDocumentHeader()
				.getMessageEvent().getMessageEventItems().getMessageEventItem();
	}

	/**
	 * Returns the currently selected message event item.
	 * 
	 * @return the selected message event item.
	 */
	public MessageEventItemType getSelectedMessageEventItem() {
		if (selectedMessageEventItem == null)
			return getResponseMessageEventItems().get(0);
		else
			return selectedMessageEventItem;
	}
	
	/**
	 * Sets the selected message event item.
	 * 
	 * @param selectedMessageEventItem
	 *            selected message event item.
	 */
	public void setSelectedMessageEventItem(
			MessageEventItemType selectedMessageEventItem) {
		MessageEventItemType oldSelectedMessageEventItem = this.selectedMessageEventItem;

		this.selectedMessageEventItem = selectedMessageEventItem;
		
		firePropertyChange("selectedMessageEventItem",
				oldSelectedMessageEventItem, selectedMessageEventItem);
	}

	/**
	 * Returns the current selected SOAP subcode list.
	 * @return List<String>
	 */
	@SuppressWarnings("rawtypes")
	public List getSelectedSOAPsubcodes() {
		if (selectedSOAPsubcodes == null)
			return getResponseSOAPsubcodes();
		else
			return selectedSOAPsubcodes;
	}

	/**
	 * Set the selected SOAP subcodes.
	 * @param subcodes
	 */
	public void setSelectedSOAPsubcodes(List<SBRCode> subcodes) {
		List<SBRCode> oldSubcodes = this.selectedSOAPsubcodes;
		this.selectedSOAPsubcodes = subcodes;
		firePropertyChange("selectedSOAPsubcodes", oldSubcodes, subcodes);
	}

	/**
	 * Populates and returns a list with all the response business document
	 * instances from the standard business document header.
	 * 
	 * @return the list of business document instances.
	 */
	public List<BusinessDocumentInstanceType> getBusinessDocumentInstances() {
		if (this.response == null
				|| this.response.getStandardBusinessDocumentBody() == null
				|| this.response.getStandardBusinessDocumentBody()
						.getBusinessDocumentInstances() == null
				|| this.response.getStandardBusinessDocumentBody()
						.getBusinessDocumentInstances()
						.getBusinessDocumentInstance() == null
				|| this.response.getStandardBusinessDocumentBody()
						.getBusinessDocumentInstances()
						.getBusinessDocumentInstance().size() == 0) {
			return null;
		}

		return this.response.getStandardBusinessDocumentBody()
				.getBusinessDocumentInstances().getBusinessDocumentInstance();
	}

	/**
	 * Populates and returns a list with all the response attachments from the
	 * standard business document header.
	 * 
	 * @return the list of attachments.
	 */
	public List<AttachmentInstanceType> getAttachmentInstances() {
		if (this.response == null
				|| this.response.getStandardBusinessDocumentBody() == null
				|| this.response.getStandardBusinessDocumentBody()
						.getAttachmentInstances() == null
				|| this.response.getStandardBusinessDocumentBody()
						.getAttachmentInstances().getAttachmentInstance() == null
				|| this.response.getStandardBusinessDocumentBody()
						.getAttachmentInstances().getAttachmentInstance()
						.size() == 0) {
			return null;
		}

		return this.response.getStandardBusinessDocumentBody()
				.getAttachmentInstances().getAttachmentInstance();
	}

	/**
	 * Populates and returns an observable list with all the response business
	 * documents.
	 * 
	 * @return the list of business documents.
	 */
	public ObservableList<BusinessDocumentBean> getBusinessDocuments() {
		return businessDocuments;
	}

	/**
	 * Sets the observable list of business documents.
	 * 
	 * @param businessDocuments
	 *            list of business documents.
	 */
	public void setBusinessDocuments(
			ObservableList<BusinessDocumentBean> businessDocuments) {
		ObservableList<BusinessDocumentBean> oldBusinessDocuments = this.businessDocuments;

		this.businessDocuments = businessDocuments;

		firePropertyChange("businessDocuments", oldBusinessDocuments,
				businessDocuments);
	}

	/**
	 * Returns the currently selected business document.
	 * 
	 * @return the selected business document.
	 */
	public BusinessDocumentBean getSelectedBusinessDocument() {
		return selectedBusinessDocument;
	}

	/**
	 * Sets the selected business document.
	 * 
	 * @param selectedBusinessDocument
	 *            selected business document.
	 */
	public void setSelectedBusinessDocument(
			BusinessDocumentBean selectedBusinessDocument) {
		BusinessDocumentBean oldSelectedBusinessDocument = this.selectedBusinessDocument;

		this.selectedBusinessDocument = selectedBusinessDocument;

		firePropertyChange("selectedBusinessDocument",
				oldSelectedBusinessDocument, selectedBusinessDocument);
		firePropertyChange("businessDocumentSelected",
				oldSelectedBusinessDocument != null,
				selectedBusinessDocument != null);
	}

	/**
	 * Determines if a business document is selected.
	 * 
	 * @return the status of the check.
	 */
	public boolean isBusinessDocumentSelected() {
		return this.selectedBusinessDocument != null;
	}

	/**
	 * Sets the status of the response bean.
	 * 
	 * @param status
	 *            status of the response bean.
	 */
	public void setStatus(String status) {
		String oldStatus = this.status;
		this.status = status;

		firePropertyChange("status", oldStatus, this.status);
	}

	/**
	 * Set the action details of the response bean.
	 * @param details
	 */
	public void setDetails(String details) {
		String oldDetails = this.details;
		this.details = details;
		
		firePropertyChange("details", oldDetails, this.details);
	}

	/**
	 * Get the action details of the response bean.
	 * @return String
	 */
	public String getDetails() {
		return details;
	}

	/**
	 * Set retry time delay for request in milliseconds.
	 * @param millis
	 */
	public void setRetryTime(long millis) {
		this.millis = millis;
	}

	/**
	 * Get the retry delay time.
	 * @return Date
	 */
	public Date getRetryTime() {
		return new Date(millis);
	}

	/**
	 * Get the original embedded core service request that was sent for this response.
	 * @return Request
	 */
	public Request getCoreRequest() {
		return coreRequest;
	}

	/**
	 *  Set the last minute interval for the retry request.
	 * @param interval
	 */
	public void setLastRetryMinuteInterval(int interval) {
		this.interval = interval;
	}

	/**
	 * Get the last minute interval of the retry request.
	 * @return int
	 */
	public int getLastRetryMinuteInterval() {
		return interval;
	}

	/**
	 * Set the service type for the embedded request of this response.
	 * @param serviceType
	 */
	public void setRequestServiceType(JRCServiceType serviceType) {
		this.serviceType = serviceType;
	}

	/**
	 * Get the embedded request service type.
	 * @return JRCServiceType
	 */
	public JRCServiceType getRequestServiceType() {
		return serviceType;
	}
}
