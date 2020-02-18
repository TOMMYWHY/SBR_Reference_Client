
package au.gov.sbr.core.client.beans;

import au.gov.sbr.comn.sbdm_02.BusinessDocumentInstanceTextType;
import au.gov.sbr.comn.sbdm_02.BusinessDocumentInstanceType;
import au.gov.sbr.comn.sbdm_02.BusinessDocumentType;
import au.gov.sbr.xml.XMLUtils.SBRSecurityNamespaceContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.ResourceBundle;
import java.util.TimeZone;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import au.gov.sbr.util.SBRUtils;
import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.observablecollections.ObservableList;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This bean represents a Business Document and Business Document instances on the Standard
 * Business Document Message.
 * 
 * @author SBR
 */
public class BusinessDocumentBean extends BeanBase {

	private static final long serialVersionUID = 1715470535070781241L;
	private static Logger logger = Logger.getLogger(BusinessDocumentBean.class
			.getName());
	private static final DatatypeFactory dtFactory;

	static {
		try {
			dtFactory = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException ex) {
			logger.log(Level.SEVERE, null, ex);
			throw new ExceptionInInitializerError(ex);
		}
	}
	private BusinessDocumentType businessDocument;
	private BusinessDocumentInstanceType businessDocumentInstance;
	// This is may not always be populated and isn't sent with the request so
	// use with caution.
	private String filename;
	/**
	 * List of attachments associated with a business document
	 */
	public ObservableList<AttachmentBean> attachments;
	private AttachmentBean selectedAttachment;

	/**
	 * Constructs a newly allocated BusinessDocumentBean object that represents
	 * the instance document imported from a file.
	 * 
	 * @param filename
	 *            name of the instance document file.
	 * @param creationDatetime
	 *            datetime the instance document was created.
	 * @param instanceDocument
	 *            instance document.
	 */
	public BusinessDocumentBean(String filename,
			GregorianCalendar creationDatetime, Element instanceDocument) {
		this(new BusinessDocumentType(), new BusinessDocumentInstanceType());

		if (filename == null) {
			throw new IllegalArgumentException("filename");
		}

		if (creationDatetime == null) {
			throw new IllegalArgumentException("creationDatetime");
		}

		if (instanceDocument == null) {
			throw new IllegalArgumentException("instanceDocument");
		}

		this.filename = filename;

		// Convert to UTC
		GregorianCalendar utc = new GregorianCalendar(TimeZone
				.getTimeZone("UTC"));
		utc.setTimeInMillis(creationDatetime.getTimeInMillis());

		XMLGregorianCalendar xmlCalendar = dtFactory
				.newXMLGregorianCalendar(utc);

		this.businessDocument.setBusinessDocumentCreationDatetime(xmlCalendar);
		setValidationUriFromInstance(instanceDocument);
		this.businessDocument
				.setBusinessDocumentBusinessGeneratedIdentifierText(UUID
						.randomUUID().toString());

		BusinessDocumentInstanceTextType instanceText = new BusinessDocumentInstanceTextType();
		instanceText.setAny(instanceDocument);

		this.businessDocumentInstance
				.setBusinessDocumentInstanceText(instanceText);
	}

	/**
	 * Constructs a newly allocated BusinessDocumentBean object that represents
	 * the business document instance.
	 * 
	 * @param businessDocument
	 *            business document object.
	 * @param businessDocumentInstance
	 *            business document instance.
	 */
	public BusinessDocumentBean(BusinessDocumentType businessDocument,
			BusinessDocumentInstanceType businessDocumentInstance) {
		super();

		if (businessDocument == null) {
			throw new IllegalArgumentException("businessDocument");
		}

		if (businessDocumentInstance == null) {
			throw new IllegalArgumentException("businessDocumentInstance");
		}

		this.businessDocument = businessDocument;
		this.businessDocumentInstance = businessDocumentInstance;

		attachments = ObservableCollections
				.observableList(new ArrayList<AttachmentBean>());
	}

	/**
	 * Populates and returns a list with all the attachments.
	 * 
	 * @return the list of attachments.
	 */
	public ObservableList<AttachmentBean> getAttachments() {
		return attachments;
	}

	/**
	 * Sets the list of attachments as the attachments to use in a request
	 * submission.
	 * 
	 * @param attachments
	 *            list of attachments.
	 */
	public void setAttachments(ObservableList<AttachmentBean> attachments) {
		ObservableList<AttachmentBean> oldAttachments = this.attachments;

		this.attachments = attachments;

		firePropertyChange("attachments", oldAttachments, attachments);
	}

	/**
	 * Returns the currently selected attachment.
	 * 
	 * @return the selected attachment.
	 */
	public AttachmentBean getSelectedAttachment() {
		return selectedAttachment;
	}

	/**
	 * Sets the selected attachment.
	 * 
	 * @param selectedAttachment
	 *            selected attachment.
	 */
	public void setSelectedAttachment(AttachmentBean selectedAttachment) {
		AttachmentBean oldSelectedAttachment = this.selectedAttachment;

		this.selectedAttachment = selectedAttachment;

		firePropertyChange("selectedAttachment", oldSelectedAttachment,
				selectedAttachment);
		firePropertyChange("attachmentSelected", oldSelectedAttachment != null,
				selectedAttachment != null);
	}

	/**
	 * Determines if an attachment is selected.
	 * 
	 * @return the status of the check.
	 */
	public boolean isAttachmentSelected() {
		return this.selectedAttachment != null;
	}

	/**
	 * Returns the filename of the business document.
	 * 
	 * @return the business document filename.
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * Sets the filename of the business document.
	 * 
	 * @param filename
	 *            filename of the business document.
	 */
	public void setFilename(String filename) {
		String oldFilename = this.filename;

		this.filename = filename;

		firePropertyChange("filename", oldFilename, filename);
	}

	/**
	 * Returns the business generated identifier text of the business document.
	 * 
	 * @return the business generated identifier text.
	 */
	public String getBusinessGeneratedIdentifier() {
		return this.businessDocument
				.getBusinessDocumentBusinessGeneratedIdentifierText();
	}

	/**
	 * Sets the business generated identifier text of the business document.
	 * 
	 * @param businessGeneratedIdentifier
	 *            business generated identifier text.
	 */
	public void setBusinessGeneratedIdentifier(
			String businessGeneratedIdentifier) {
		String oldBusinessGeneratedIdentifier = this.businessDocument
				.getBusinessDocumentBusinessGeneratedIdentifierText();

		this.businessDocument
				.setBusinessDocumentBusinessGeneratedIdentifierText(businessGeneratedIdentifier);

		firePropertyChange("businessGeneratedIdentifier",
				oldBusinessGeneratedIdentifier, businessGeneratedIdentifier);
	}

	/**
	 * Returns the datetime that the business document was created. Returns this
	 * in the Gregorian Calendar format.
	 * 
	 * @return the datetime that the business document was created.
	 */
	public GregorianCalendar getCreation() {
		return this.businessDocument.getBusinessDocumentCreationDatetime()
				.toGregorianCalendar();
	}

	/**
	 * Sets the datetime that the business document was created.
	 * 
	 * @param creation
	 *            datetime of document creation.
	 */
	public void setCreation(GregorianCalendar creation) {
		GregorianCalendar oldCreation = this.businessDocument
				.getBusinessDocumentCreationDatetime().toGregorianCalendar();

		this.businessDocument.setBusinessDocumentCreationDatetime(dtFactory
				.newXMLGregorianCalendar(creation));

		firePropertyChange("creation", oldCreation, creation);
	}

	/**
	 * Returns the government generated identifier text of the business
	 * document.
	 * 
	 * @return the government generated identifier text.
	 */
	public String getGovernmentGeneratedIdentifier() {
		return this.businessDocument
				.getBusinessDocumentGovernmentGeneratedIdentifierText();
	}

	/**
	 * Sets the government generated identifier text of the business document.
	 * 
	 * @param governmentGeneratedIdentifier
	 *            government generated identifier text.
	 */
	public void setGovernmentGeneratedIdentifier(
			String governmentGeneratedIdentifier) {
		String oldGovernmentGeneratedIdentifier = this.businessDocument
				.getBusinessDocumentGovernmentGeneratedIdentifierText();

		this.businessDocument
				.setBusinessDocumentGovernmentGeneratedIdentifierText(governmentGeneratedIdentifier);

		firePropertyChange("governmentGeneratedIdentifier",
				oldGovernmentGeneratedIdentifier, governmentGeneratedIdentifier);
	}

	/**
	 * Returns the sequence number of the business document.
	 * 
	 * @return the sequence number.
	 */
	public BigInteger getSequenceNumber() {
		return this.businessDocument.getBusinessDocumentSequenceNumber();
	}

	/**
	 * Sets the sequence number of the business document.
	 * 
	 * @param sequenceNumber
	 *            sequence number.
	 */
	public void setSequenceNumber(BigInteger sequenceNumber) {
		BigInteger oldSequenceNumber = this.businessDocument
				.getBusinessDocumentSequenceNumber();

		this.businessDocument.setBusinessDocumentSequenceNumber(sequenceNumber);
		this.businessDocumentInstance
				.setBusinessDocumentSequenceNumber(sequenceNumber);

		firePropertyChange("sequenceNumber", oldSequenceNumber, sequenceNumber);
	}

	/**
	 * Returns the validation URI of the business document.
	 * 
	 * @return the validation URI.
	 */
	public String getValidationURI() {
		return this.businessDocument
				.getBusinessDocumentValidationUniformResourceIdentifierText();
	}

	/**
	 * Sets the validation URI of the business document.
	 * 
	 * @param validationURI
	 *            validation URI.
	 */
	public void setValidationURI(String validationURI) {
		String oldValidationURI = this.businessDocument
				.getBusinessDocumentValidationUniformResourceIdentifierText();

		this.businessDocument
				.setBusinessDocumentValidationUniformResourceIdentifierText(validationURI);

		firePropertyChange("validationURI", oldValidationURI, validationURI);
	}

	/**
	 * Returns the instance document.
	 * 
	 * @return the instance document.
	 */
	public Element getInstanceDocument() {
		return this.businessDocumentInstance.getBusinessDocumentInstanceText()
				.getAny();
	}

	/**
	 * Sets the business document instance.
	 * 
	 * @param instanceDocument
	 *            instance document.
	 */
	public void setInstanceDocument(Element instanceDocument) {
		Element oldInstanceDocument = this.businessDocumentInstance
				.getBusinessDocumentInstanceText().getAny();

		this.businessDocumentInstance.getBusinessDocumentInstanceText().setAny(
				instanceDocument);

		firePropertyChange("instanceDocument", oldInstanceDocument,
				instanceDocument);
	}

	/**
	 * Adds an attachment, loaded from a file, to the attachment bean.
	 * 
	 * @param attachment
	 *            attachment file.
	 */
	public void addAttachment(File attachment) {
		try {
			AttachmentBean bean = new AttachmentBean(attachment.getPath(),
					SBRUtils.convertToByteArray(new FileInputStream(attachment)));
			this.attachments.add(bean);
		} catch (IOException ex) {
			Logger.getLogger(BusinessDocumentBean.class.getName()).log(
					Level.SEVERE, null, ex);
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Adds an attachment, loaded from an attachment bean, to the business
	 * document bean.
	 * 
	 * @param attachmentBean
	 *            attachment bean object.
	 */
	public void addAttachment(AttachmentBean attachmentBean) {
		this.attachments.add(attachmentBean);
	}

	/**
	 * Removes an attachment from the business document bean.
	 * 
	 * @param attachmentBean attachment bean object
	 */
	public void removeAttachment(AttachmentBean attachmentBean) {
		this.attachments.remove(attachmentBean);
	}

	// Note that there is no property change support for the BusinessDocument
	// and BusinessDocumentInstance objects.
	/**
	 * Returns the business document.
	 * 
	 * @return the business document.
	 */
	public BusinessDocumentType getBusinessDocument() {
		return businessDocument;
	}

	/**
	 * Sets the business document.
	 * 
	 * @param businessDocument
	 *            business document.
	 */
	public void setBusinessDocument(BusinessDocumentType businessDocument) {
		this.businessDocument = businessDocument;
	}

	/**
	 * Returns the business document instance.
	 * 
	 * @return the business document instance.
	 */
	public BusinessDocumentInstanceType getBusinessDocumentInstance() {
		return businessDocumentInstance;
	}

	/**
	 * Sets the business document instance.
	 * 
	 * @param businessDocumentInstance
	 *            business document instance.
	 */
	public void setBusinessDocumentInstance(
			BusinessDocumentInstanceType businessDocumentInstance) {
		this.businessDocumentInstance = businessDocumentInstance;
	}

	/**
	 * Extracts the Validation Uri from the instance Document and sets it on the
	 * business document header details
	 * 
	 * @param instanceDocument
	 *            the instance document.
	 */
	private void setValidationUriFromInstance(Element instanceDocument) {
		try {
			ResourceBundle resources = ResourceBundle
					.getBundle(BusinessDocumentBean.class.getName());

			XPath xpath = XPathFactory.newInstance().newXPath();
			xpath.setNamespaceContext(new SBRSecurityNamespaceContext());

			// XPath Query for showing all nodes values that has the following
			// namespace uri and local name
			XPathExpression expr = xpath.compile("//*[namespace-uri()='"
					+ resources.getString("link.namespace.url")
					+ "'][local-name()='" + resources.getString("link.node")
					+ "']//@*[namespace-uri()='"
					+ resources.getString("xlink.namespace.url")
					+ "'][local-name()='"
					+ resources.getString("xlink.node.attribute") + "']");

			Node node = (Node) expr.evaluate(instanceDocument,
					XPathConstants.NODE);

			// Set the first node value derived from the XPath Query as the
			// validationURI
			this.businessDocument
					.setBusinessDocumentValidationUniformResourceIdentifierText(((Attr) node)
							.getValue());

		} catch (Exception e) {
			throw new RuntimeException(
					"Error extracting Validation URI from Instance Document", e);
		}
	}
}
