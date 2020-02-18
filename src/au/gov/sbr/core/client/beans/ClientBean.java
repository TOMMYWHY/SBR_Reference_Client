
package au.gov.sbr.core.client.beans;

import au.gov.abr.akm.exceptions.CertificateChainException;
import au.gov.abr.akm.exceptions.IncorrectPasswordException;
import au.gov.abr.akm.exceptions.InvalidP7CException;
import au.gov.abr.akm.exceptions.NoSuchAliasException;
import au.gov.sbr.comn.sbdm_02.SoftwareInformationType;
import au.gov.sbr.core.client.gui.OrganisationNameCheck;
import au.gov.sbr.core.client.util.FileUtil;
import au.gov.sbr.core.client.util.SecToken;
import au.gov.sbr.core.forms.Form;
import au.gov.sbr.core.forms.FormFactory;
import au.gov.sbr.core.io.BOMUTFInputStream;
import au.gov.sbr.core.requester.exceptions.SBRCoreException;
import au.gov.sbr.core.requester.exceptions.SBRException;
import au.gov.sbr.core.requester.exceptions.SBRTimeoutException;
import au.gov.sbr.core.requester.exceptions.SBRUnavailableException;
import au.gov.sbr.core.requester.model.Attachment;
import au.gov.sbr.core.requester.model.BusinessDocument;
import au.gov.sbr.core.requester.model.Request;
import au.gov.sbr.core.requester.model.Response;
import au.gov.sbr.core.requester.model.SBRCoreServicesRequestFactory;
import au.gov.sbr.core.services.ServiceType;
import au.gov.sbr.core.vanguard.VanguardLookup;
import au.gov.abr.securitytokenmanager.exceptions.STMCommunicationException;
import au.gov.abr.securitytokenmanager.exceptions.STMException;
import au.gov.abr.securitytokenmanager.exceptions.STMFaultException;
import au.gov.abr.securitytokenmanager.exceptions.STMTimeoutException;
import au.gov.abr.securitytokenmanager.SecurityToken;
import au.gov.sbr.core.client.gui.JRCServiceType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.cert.Certificate;
import java.security.PrivateKey;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;
import javax.xml.ws.soap.SOAPFaultException;

import org.jdesktop.application.Application;
import org.jdesktop.application.Task;
import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.observablecollections.ObservableList;
import org.w3c.dom.Document;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSParser;


/**
 * This bean represents the client application's main window and all the
 * properties that are exposed to the GUI for binding.
 * 
 * @author SBR
 */
public class ClientBean extends BeanBase {

	private static final long serialVersionUID = 5244584377819298524L;
	private static Logger logger = Logger.getLogger(ClientBean.class.getName());
	private ObservableList<Form> forms;
	private Form selectedForm;
	private JRCServiceType selectedService;
	private ObservableList<BusinessDocumentBean> businessDocuments;
	private BusinessDocumentBean selectedBusinessDocument;
	private SOAPFaultBean soapFaultBean;
	private KeyStoreBean keyStoreBean;
	private String endpointPrefix;
	private String endpoint;
	private String stsEndpoint;
	// Service radio button enable toggle
	private boolean listRadioEnable = false;
	private boolean lodgeRadioEnable = false;
	private boolean prefillRadioEnable = false;
	private boolean prelodgeRadioEnable = false;
	private boolean altListRadioEnable = false;
	private boolean altLodgeRadioEnable = false;
	// Request options checkboxes values
	private boolean loggerRequest;
	private boolean schemaValidationRequest = true;
	private boolean schematronValidationRequest;
	private boolean xbrlValidationRequest;
	private boolean securityRequest = true;
	private boolean mtomRequest;
	private boolean validateMessageTypeResponse;
	// Responses
	private ObservableList<ResponseBean> responses;
	private ResponseBean selectedResponse;
	private PrivateKey privateKey;
	private Certificate[] certificateChain;
	private ObservableList<ResponseBean> queuedResponses;

	/**
	 * Constructs a new ClientBean object with defined initialised values.
	 */
	public ClientBean() {
		selectedService = JRCServiceType.LIST;

		forms = ObservableCollections.observableList(FormFactory.instance()
				.getFormList());

		businessDocuments = ObservableCollections
				.observableList(new ArrayList<BusinessDocumentBean>());
		responses = ObservableCollections
				.observableList(new ArrayList<ResponseBean>());

		ResourceBundle resources = ResourceBundle.getBundle(ClientBean.class
				.getName());

		//
		//Default to production if property does not exist.
		//
		this.endpointPrefix = resources.getString("core.endpoint.url");
		if (this.endpointPrefix == null || "".equals(this.endpointPrefix)) {
			this.endpointPrefix = "https://test.sbr.gov.au/services/";
		}
		this.stsEndpoint = resources.getString("sts.endpoint.url");
		if (this.stsEndpoint == null || "".equals(this.stsEndpoint)) {
			this.stsEndpoint = "https://thirdparty.authentication.business.gov.au/r3.0/vanguard/S007v1.1/Service.svc";
		}
		 
		keyStoreBean = new KeyStoreBean();
	}

	/**
	 * Sets the selected form to this first indexed form.
	 */
	public void initValues() {
		this.setSelectedForm(forms.get(0));
	}

	/**
	 * Populates and returns a list with all the forms.
	 * 
	 * @return the list of forms.
	 */
	public ObservableList<Form> getForms() {
		return forms;
	}
	
	public SOAPFaultBean getSOAPFault() {
		return soapFaultBean;
	}

	public void setSOAPFault(SOAPFaultBean soapFaultBean) {
		SOAPFaultBean oldFault = this.soapFaultBean;
		this.soapFaultBean = soapFaultBean;
		firePropertyChange("soapFault", oldFault, soapFaultBean);
	}

	public ObservableList<ResponseBean> getQueuedResponses() {
		return queuedResponses;
	}

	/**
	 * Returns the currently selected form.
	 * 
	 * @return the selected form.
	 */
	public Form getSelectedForm() {
		return selectedForm;
	}

	/**
	 * Sets the selected form.
	 * 
	 * @param selectedForm
	 *            selected form.
	 */
	public void setSelectedForm(Form selectedForm) {
		Form oldSelectedForm = this.selectedForm;

		this.selectedForm = selectedForm;

		if (this.selectedForm != null) {
			enableServices(this.selectedForm);
		}

		firePropertyChange("selectedForm", oldSelectedForm, selectedForm);
	}

	/**
	 * Returns the currently selected service.
	 * 
	 * @return the selected service.
	 */
	public JRCServiceType getSelectedService() {
		return selectedService;
	}

	/**
	 * Sets the selected service and corresponding service endpoint.
	 * 
	 * @param selectedService
	 *            selected service.
	 */
	public void setSelectedService(JRCServiceType selectedService) {
		JRCServiceType oldSelectedService = this.selectedService;
		this.selectedService = selectedService;

		if (getSelectedForm() == null)
			setEndpoint(this.endpointPrefix + "${service}.02.service");
		else
			setEndpoint(this.endpointPrefix + selectedService.getUrlPart()
					+ ".02.service");

		firePropertyChange("selectedService", oldSelectedService,
				selectedService);
	}

	/**
	 * Returns the SBR end point to send the request.
	 * 
	 * @return the end point to send the request.
	 */
	public String getEndpoint() {
		return endpoint;
	}

	/**
	 * Sets the SBR end point to send the request.
	 * 
	 * @param endpoint
	 *            end point to send the request.
	 */
	public void setEndpoint(String endpoint) {
		String oldEndpoint = this.endpoint;
		this.endpoint = endpoint;

		firePropertyChange("endpoint", oldEndpoint, endpoint);
	}

	/**
	 * Returns the STS end point.
	 * 
	 * @return the STS end point.
	 */
	public String getStsEndpoint() {
		return stsEndpoint;
	}

	/**
	 * Sets the STS end point.
	 * 
	 * @param stsEndpoint
	 *            STS end point.
	 */
	public void setStsEndpoint(String stsEndpoint) {
		String oldStsEndpoint = this.stsEndpoint;
		this.stsEndpoint = stsEndpoint;

		firePropertyChange("stsEndpoint", oldStsEndpoint, stsEndpoint);
	}

	/**
	 * Determines if the option to sent message as an mtom request is enabled.
	 * 
	 * @return the status of option.
	 */
	public boolean isMtomRequest() {
		return mtomRequest;
	}

	/**
	 * Sets the option to sent message as an mtom request.
	 * 
	 * @param mtomRequest
	 *            status of option.
	 */
	public void setMtomRequest(boolean mtomRequest) {
		boolean oldmtomRequest = this.mtomRequest;
		this.mtomRequest = mtomRequest;

		firePropertyChange("mtomRequest", oldmtomRequest, mtomRequest);
	}

	/**
	 * Determines if the option to print the request and response messages to
	 * the console is enabled.
	 * 
	 * @return the status of option.
	 */
	public boolean isLoggerRequest() {
		return loggerRequest;
	}

	/**
	 * Sets the option to print the request and response messages to the
	 * console.
	 * 
	 * @param loggerRequest
	 *            status of option.
	 */
	public void setLoggerRequest(boolean loggerRequest) {
		boolean oldLoggerRequest = this.loggerRequest;
		this.loggerRequest = loggerRequest;

		firePropertyChange("loggerRequest", oldLoggerRequest, loggerRequest);
	}

	/**
	 * Determines if the option to validate the request message against the
	 * schema is enabled.
	 * 
	 * @return the status of option.
	 */
	public boolean isSchemaValidationRequest() {
		return schemaValidationRequest;
	}

	/**
	 * Sets the option to validate the request message against the schema.
	 * 
	 * @param schemaValidationRequest
	 *            status of option.
	 */
	public void setSchemaValidationRequest(boolean schemaValidationRequest) {
		boolean oldSchemaValidationRequest = this.schemaValidationRequest;
		this.schemaValidationRequest = schemaValidationRequest;

		firePropertyChange("schemaValidationRequest",
				oldSchemaValidationRequest, schemaValidationRequest);
	}

	/**
	 * Determines if the option to validate the request message against the
	 * schematron is enabled.
	 * 
	 * @return the status of option.
	 */
	public boolean isSchematronValidationRequest() {
		return schematronValidationRequest;
	}

	/**
	 * Sets the option to validate the request message against the schematron.
	 * 
	 * @param schematronValidationRequest
	 *            status of option.
	 */
	public void setSchematronValidationRequest(
			boolean schematronValidationRequest) {
		boolean oldSchematronValidationRequest = this.schematronValidationRequest;
		this.schematronValidationRequest = schematronValidationRequest;

		firePropertyChange("schematronValidationRequest",
				oldSchematronValidationRequest, schematronValidationRequest);
	}

	/**
	 * Determines if the option to activate security on the request message is
	 * enabled.
	 * 
	 * @return the status of option.
	 */
	public boolean isSecurityRequest() {
		return securityRequest;
	}

	/**
	 * Sets the option to activate security on the request message.
	 * 
	 * @param securityRequest
	 *            status of option.
	 */
	public void setSecurityRequest(boolean securityRequest) {
		boolean oldSecurityRequest = this.securityRequest;
		this.securityRequest = securityRequest;

		firePropertyChange("securityRequest", oldSecurityRequest,
				securityRequest);
	}

	/**
	 * Determines if the option to validate the message type of the response is
	 * enabled.
	 * 
	 * @return the status of option.
	 */
	public boolean isValidateMessageTypeResponse() {
		return validateMessageTypeResponse;
	}

	/**
	 * Sets the option to validate the message type of the response.
	 * 
	 * @param validateMessageTypeResponse
	 *            status of option.
	 */
	public void setValidateMessageTypeResponse(
			boolean validateMessageTypeResponse) {
		boolean oldValidateMessageTypeResponse = this.validateMessageTypeResponse;
		this.validateMessageTypeResponse = validateMessageTypeResponse;

		firePropertyChange("validateMessageTypeResponse",
				oldValidateMessageTypeResponse, validateMessageTypeResponse);
	}

	/**
	 * Determines if the option to validate the request message against valid
	 * xbrl is enabled.
	 * 
	 * @return the status of option.
	 */
	public boolean isXbrlValidationRequest() {
		return xbrlValidationRequest;
	}

	/**
	 * Sets the option to validate the request message against valid xbrl.
	 * 
	 * @param xbrlValidationRequest
	 *            status of option.
	 */
	public void setXbrlValidationRequest(boolean xbrlValidationRequest) {
		boolean oldXbrlValidationRequest = this.xbrlValidationRequest;
		this.xbrlValidationRequest = xbrlValidationRequest;

		firePropertyChange("xbrlValidationRequest", oldXbrlValidationRequest,
				xbrlValidationRequest);
	}

	/**
	 * Populates and returns a list with all the responses.
	 * 
	 * @return the list of responses.
	 */
	public ObservableList<ResponseBean> getResponses() {
		return responses;
	}

	/**
	 * Returns the selected response.
	 * 
	 * @return the selected response.
	 */
	public ResponseBean getSelectedResponse() {
		return selectedResponse;
	}

	/**
	 * Sets the selected response.
	 * 
	 * @param selectedResponse
	 *            selected response.
	 */
	public void setSelectedResponse(ResponseBean selectedResponse) {
		ResponseBean oldSelectedResponseBean = this.selectedResponse;
		
		AttachmentBean selectedAttachment = null;
		ObservableList<AttachmentBean> list = null;

		if (this.selectedResponse != null && this.selectedResponse.getSelectedBusinessDocument() != null && this.selectedResponse.getSelectedBusinessDocument().getAttachments() != null)
		{
			selectedAttachment = this.selectedResponse.getSelectedBusinessDocument().getSelectedAttachment();
			this.selectedResponse.getSelectedBusinessDocument().setSelectedAttachment(null);
					
			list = this.selectedResponse.getSelectedBusinessDocument().getAttachments();
			this.selectedResponse.getSelectedBusinessDocument().setAttachments(null);
		}

		this.selectedResponse = selectedResponse;

		firePropertyChange("selectedResponse", oldSelectedResponseBean,
				selectedResponse);
		
		if (selectedAttachment != null)
			oldSelectedResponseBean.getSelectedBusinessDocument().setSelectedAttachment(selectedAttachment);
		if (list != null)
			oldSelectedResponseBean.getSelectedBusinessDocument().setAttachments(list);
	}

	/**
	 * Determines if the selected service is of type list.
	 * 
	 * @return the status of check.
	 */
	public boolean isListServiceSelected() {
		return this.selectedService == JRCServiceType.LIST;
	}

	/**
	 * Returns the list radio button label to be displayed.
	 * 
	 * @return the list radio button message text.
	 */
	public String getListMessageText() {
		return this.selectedForm.getListMessageText();
	}

	/**
	 * Sets the selected service to list if it is currently selected as list.
	 * 
	 * @param selected
	 *            selected service.
	 */
	public void setListServiceSelected(boolean selected) {
		boolean oldListServiceSelected = this.selectedService == JRCServiceType.LIST;

		if (selected) {
			this.selectedService = JRCServiceType.LIST;
		} else {
			this.selectedService = null;
		}

		firePropertyChange("listServiceSelected", oldListServiceSelected,
				selected);
	}

	/**
	 * Determines if the selected service is of type lodge.
	 * 
	 * @return the status of check.
	 */
	public boolean isLodgeServiceSelected() {
		return this.selectedService == JRCServiceType.LODGE;
	}

	/**
	 * Returns the lodge radio button label to be displayed.
	 * 
	 * @return the lodge radio button message text.
	 */
	public String getLodgeMessageText() {
		return this.selectedForm.getLodgeMessageText();
	}

	/**
	 * Sets the selected service to lodge if it is currently selected as lodge.
	 * 
	 * @param selected
	 *            selected service.
	 */
	public void setLodgeServiceSelected(boolean selected) {
		boolean oldLodgeServiceSelected = this.selectedService == JRCServiceType.LODGE;

		if (selected) {
			this.selectedService = JRCServiceType.LODGE;
		} else {
			this.selectedService = null;
		}

		firePropertyChange("lodgeServiceSelected", oldLodgeServiceSelected,
				selected);
	}

	/**
	 * Determines if the selected service is of type prefill.
	 * 
	 * @return the status of check.
	 */
	public boolean isPrefillServiceSelected() {
		return this.selectedService == JRCServiceType.PREFILL;
	}

	/**
	 * Returns the prefill radio button label to be displayed.
	 * 
	 * @return the prefill radio button message text.
	 */
	public String getPrefillMessageText() {
		return this.selectedForm.getPrefillMessageText();
	}

	/**
	 * Sets the selected service to prefill if it is currently selected as
	 * prefill.
	 * 
	 * @param selected
	 *            selected service.
	 */
	public void setPrefillServiceSelected(boolean selected) {
		boolean oldPrefillServiceSelected = this.selectedService == JRCServiceType.PREFILL;

		if (selected) {
			this.selectedService = JRCServiceType.PREFILL;
		} else {
			this.selectedService = null;
		}

		firePropertyChange("prefillServiceSelected", oldPrefillServiceSelected,
				selected);
	}

	/**
	 * Determines if the selected service is of type prelodge.
	 * 
	 * @return the status of check.
	 */
	public boolean isPrelodgeServiceSelected() {
		return this.selectedService == JRCServiceType.PRELODGE;
	}

	/**
	 * Returns the prelodge radio button label to be displayed.
	 * 
	 * @return the prelodge radio button message text.
	 */
	public String getPrelodgeMessageText() {
		return this.selectedForm.getPrelodgeMessageText();
	}

	/**
	 * Sets the selected service to prelodge if it is currently selected as
	 * prelodge.
	 * 
	 * @param selected
	 *            selected service.
	 */
	public void setPrelodgeServiceSelected(boolean selected) {
		boolean oldPrelodgeServiceSelected = this.selectedService == JRCServiceType.PRELODGE;

		if (selected) {
			this.selectedService = JRCServiceType.PRELODGE;
		} else {
			this.selectedService = null;
		}

		firePropertyChange("prelodgeServiceSelected",
				oldPrelodgeServiceSelected, selected);
	}

	/**
	 * Determines if the selected service is of type alternate list.
	 * 
	 * @return the status of check.
	 */
	public boolean isAltListServiceSelected() {
		return this.selectedService == JRCServiceType.ALTLIST;
	}

	/**
	 * Returns the alternate list radio button label to be displayed.
	 * 
	 * @return the alternate list radio button message text.
	 */
	public String getAltListMessageText() {
		return this.selectedForm.getAltListMessageText();
	}

	/**
	 * Sets the selected service to alternate list if it is currently selected
	 * as alternate list.
	 * 
	 * @param selected
	 *            selected service.
	 */
	public void setAltListServiceSelected(boolean selected) {
		boolean oldAltListServiceSelected = this.selectedService == JRCServiceType.ALTLIST;

		if (selected) {
			this.selectedService = JRCServiceType.ALTLIST;
		} else {
			this.selectedService = null;
		}

		firePropertyChange("altListServiceSelected", oldAltListServiceSelected,
				selected);
	}

	/**
	 * Determines if the selected service is of type alternate lodge.
	 * 
	 * @return the status of check.
	 */
	public boolean isAltLodgeServiceSelected() {
		return this.selectedService == JRCServiceType.ALTLODGE;
	}

	/**
	 * Returns the alternate lodge radio button label to be displayed.
	 * 
	 * @return the alternate lodge radio button message text.
	 */
	public String getAltLodgeMessageText() {
		return this.selectedForm.getAltLodgeMessageText();
	}

	/**
	 * Sets the selected service to alternate lodge if it is currently selected
	 * as alternate lodge.
	 * 
	 * @param selected
	 *            selected service.
	 */
	public void setAltLodgeServiceSelected(boolean selected) {
		boolean oldAltLodgeServiceSelected = this.selectedService == JRCServiceType.ALTLODGE;

		if (selected) {
			this.selectedService = JRCServiceType.ALTLODGE;
		} else {
			this.selectedService = null;
		}

		firePropertyChange("altLodgeServiceSelected",
				oldAltLodgeServiceSelected, selected);
	}

	/**
	 * Populates and returns a list with all the business documents.
	 * 
	 * @return the list of business documents.
	 */
	public ObservableList<BusinessDocumentBean> getBusinessDocuments() {
		return businessDocuments;
	}

	/**
	 * Sets the list of business documents as the business documents to use in a
	 * request submission.
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
	 *            the selected business document.
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
	 * @return the status of check.
	 */
	public boolean isBusinessDocumentSelected() {
		return this.selectedBusinessDocument != null;
	}

	/**
	 * Returns the current keystore bean.
	 * 
	 * @return the keystore bean.
	 */
	public KeyStoreBean getKeyStore() {
		return keyStoreBean;
	}

	/**
	 * Sets the keystore bean.
	 * 
	 * @param keyStore
	 *            loaded keystore.
	 */
	public void setKeyStore(KeyStoreBean keyStore) {
		KeyStoreBean oldKeyStore = this.keyStoreBean;

		this.keyStoreBean = keyStore;

		firePropertyChange("keyStore", oldKeyStore, keyStore);
	}

	/**
	 * Adds a business document, loaded from a file, to the business document
	 * bean.
	 * 
	 * @param file
	 *            business document file.
	 */
	public void addBusinessDocument(File file) {
		DOMImplementationRegistry registry;
		try {
			registry = DOMImplementationRegistry.newInstance();
		} catch (ClassNotFoundException ex) {
			Logger.getLogger(ClientBean.class.getName()).log(Level.SEVERE,
					null, ex);
			assert false; // This shouldn't happen
			throw new RuntimeException(ex);
		} catch (InstantiationException ex) {
			Logger.getLogger(ClientBean.class.getName()).log(Level.SEVERE,
					null, ex);
			assert false; // This shouldn't happen
			throw new RuntimeException(ex);
		} catch (IllegalAccessException ex) {
			Logger.getLogger(ClientBean.class.getName()).log(Level.SEVERE,
					null, ex);
			assert false; // This shouldn't happen
			throw new RuntimeException(ex);
		} catch (ClassCastException ex) {
			Logger.getLogger(ClientBean.class.getName()).log(Level.SEVERE,
					null, ex);
			assert false; // This shouldn't happen
			throw new RuntimeException(ex);
		}

		DOMImplementationLS impl = (DOMImplementationLS) registry
				.getDOMImplementation("LS");

		LSParser parser = impl.createLSParser(
				DOMImplementationLS.MODE_SYNCHRONOUS, null);

		LSInput input = impl.createLSInput();
		FileInputStream fis = null;
		BOMUTFInputStream b8is = null;

		try {
			fis = new FileInputStream(file);
			b8is = new BOMUTFInputStream(fis);
			if (b8is.hasUTFBom()) {
				b8is.skipBOM();
			}

			input.setByteStream(b8is);
		} catch (FileNotFoundException ex) {
			Logger.getLogger(ClientBean.class.getName()).log(Level.SEVERE,
					null, ex);
			throw new RuntimeException(ex);
		} catch (IOException ex) {
			Logger.getLogger(ClientBean.class.getName()).log(Level.SEVERE,
					null, ex);
			throw new RuntimeException(ex);
		}

		Document doc = parser.parse(input);

		GregorianCalendar lastCreationTime = (GregorianCalendar) Calendar
				.getInstance();
		lastCreationTime.setTimeInMillis(file.lastModified());

		BusinessDocumentBean bean = new BusinessDocumentBean(file.getPath(),
				lastCreationTime, doc.getDocumentElement());

		FileUtil.safeClose(fis, b8is);
		this.businessDocuments.add(bean);
	}

	/**
	 * Sends the message request collated from the client application as a task
	 * thread for processing.
	 * 
	 * @param app
	 *            the client application.
	 */
	public Task<Object, Void> getSendRequest(boolean isResendAvailable, Application app) {
		return new SendRequestTask(isResendAvailable, app);
	}

	/**
	 * Determines if the list radio button is enabled.
	 * 
	 * @return the status of list radio button.
	 */
	public boolean isListRadioEnable() {
		return listRadioEnable;
	}

	/**
	 * Sets the status of the list radio button.
	 * 
	 * @param listRadioEnable
	 *            status of the button.
	 */
	public void setListRadioEnable(boolean listRadioEnable) {
		this.listRadioEnable = listRadioEnable;
	}

	/**
	 * Determines if the lodge radio button is enabled.
	 * 
	 * @return the status of lodge radio button.
	 */
	public boolean isLodgeRadioEnable() {
		return lodgeRadioEnable;
	}

	/**
	 * Sets the status of the lodge radio button.
	 * 
	 * @param lodgeRadioEnable
	 *            status of the button.
	 */
	public void setLodgeRadioEnable(boolean lodgeRadioEnable) {
		this.lodgeRadioEnable = lodgeRadioEnable;
	}

	/**
	 * Determines if the prefill radio button is enabled.
	 * 
	 * @return the status of prefill radio button.
	 */
	public boolean isPrefillRadioEnable() {
		return prefillRadioEnable;
	}

	/**
	 * Sets the status of the prefill radio button.
	 * 
	 * @param prefillRadioEnable
	 *            status of the button.
	 */
	public void setPrefillRadioEnable(boolean prefillRadioEnable) {
		this.prefillRadioEnable = prefillRadioEnable;
	}

	/**
	 * Determines if the prelodge radio button is enabled.
	 * 
	 * @return the status of prelodge radio button.
	 */
	public boolean isPrelodgeRadioEnable() {
		return prelodgeRadioEnable;
	}

	/**
	 * Sets the status of the prelodge radio button.
	 * 
	 * @param prelodgeRadioEnable
	 *            status of the button.
	 */
	public void setPrelodgeRadioEnable(boolean prelodgeRadioEnable) {
		this.prelodgeRadioEnable = prelodgeRadioEnable;
	}

	/**
	 * Determines if the alternate list radio button is enabled.
	 * 
	 * @return the status of alternate list radio button.
	 */
	public boolean isAltListRadioEnable() {
		return altListRadioEnable;
	}

	/**
	 * Sets the status of the alternate list radio button.
	 * 
	 * @param altListRadioEnable
	 *            status of the button.
	 */
	public void setAltListRadioEnable(boolean altListRadioEnable) {
		this.altListRadioEnable = altListRadioEnable;
	}

	/**
	 * Determines if the alternate lodge radio button is enabled.
	 * 
	 * @return the status of alternate lodge radio button.
	 */
	public boolean isAltLodgeRadioEnable() {
		return altLodgeRadioEnable;
	}

	/**
	 * Sets the status of the alternate lodge radio button.
	 * 
	 * @param altLodgeRadioEnable
	 *            status of the button.
	 */
	public void setAltLodgeRadioEnable(boolean altLodgeRadioEnable) {
		this.altLodgeRadioEnable = altLodgeRadioEnable;
	}

	/**
	 * Sets all service radio buttons that are to be enabled based on the
	 * applicable services that apply to the selected form.
	 * 
	 * @param selectedForm
	 *            selected form.
	 */
	private void enableServices(Form selectedForm) {
		if (this.selectedForm.getListMessageRequestType().isEmpty())
			setListRadioEnable(false);
		else
			setListRadioEnable(true);

		if (this.selectedForm.getLodgeMessageRequestType().isEmpty())
			setLodgeRadioEnable(false);
		else
			setLodgeRadioEnable(true);

		if (this.selectedForm.getPrefillMessageRequestType().isEmpty())
			setPrefillRadioEnable(false);
		else
			setPrefillRadioEnable(true);

		if (this.selectedForm.getPrelodgeMessageRequestType().isEmpty())
			setPrelodgeRadioEnable(false);
		else
			setPrelodgeRadioEnable(true);

		if (this.selectedForm.getAltListMessageRequestType().isEmpty())
			setAltListRadioEnable(false);
		else
			setAltListRadioEnable(true);

		if (this.selectedForm.getAltLodgeMessageRequestType().isEmpty())
			setAltLodgeRadioEnable(false);
		else
			setAltLodgeRadioEnable(true);
	}

	@SuppressWarnings("rawtypes")
	public Task resubmit(ResponseBean response, boolean isResendAvailable, Application app) {
		return new RetrySendRequestTask(response, isResendAvailable, app);
	}

	private abstract class RequestTask extends Task<Object, Void>
	{
		/** Response object for the request */
		volatile Response response;
		/** Determine if this request is a security request */
		volatile boolean isSecurityRequest;
		/** Response bean object for the request / response */
		volatile ResponseBean responseBean;
		/** SOAP Fault object for the request / response */
		volatile SOAPFaultBean soapFault;
		/** Core Service request factory */
		volatile SBRCoreServicesRequestFactory requestFactory;
		/** Request object */
		volatile Request req;
		/** Vanguard security token used for secure communications */
		volatile SecurityToken token;
		/** Determine if re-sending (retrying) is available */
		volatile boolean isResendAvailable;
		/** Determine if the request timed out */
		volatile boolean timeoutOccurred;

		/**
		 * Request Task constructor
		 * @param isResendAvailable
		 * @param app
		 */
		RequestTask(boolean isResendAvailable, Application app){
			super(app);
			this.isResendAvailable = isResendAvailable;
			timeoutOccurred = false;
		}

		/**
		 * Get a Vanguard security token.
		 * @param endpointUrl
		 * @return SecurityToken
		 * @throws STMException
		 */
		SecurityToken getToken(String endpointUrl) 
				throws SOAPFaultException, STMFaultException, STMTimeoutException, STMCommunicationException, STMException {

			return new SecToken().getSecurityToken(endpointUrl, stsEndpoint, certificateChain, privateKey);
		}

		/**
		 * Called when the Task is completed.
		 * @param Object
		 */
		@Override
		protected void succeeded(Object result) {
			responseBean.setResponseOutstanding(false);
			// Runs on the EDT. Update the GUI based on
			// the result computed by doInBackground().
			if (this.soapFault != null) {
				responseBean.setSOAPFault(soapFault);
				responseBean.setStatus("Exception");
				responseBean.setDetails("");

				if (isResendAvailable) {
					if (!addToQueue())
						queuedResponses.remove(responseBean);
				}
			} else if (response == null) {
				if (responseBean.getStatus() != null && !responseBean.getStatus().toLowerCase().equals("queued"))
				{
					responseBean.setStatus("Exception");
					responseBean.setDetails("");
				}
			}

			if (response != null)
			{
				responseBean.setStatus("Completed");
				responseBean.setResponse(response.getSBDM());
				if (queuedResponses != null && queuedResponses.contains(responseBean))
				{
					queuedResponses.remove(responseBean);
					responseBean.setRetryTime(0);
					responseBean.setLastRetryMinuteInterval(0);
				}
			}
		}

		/**
		 * Add the failed response bean to the re-send queue.
		 */
		boolean addToQueue()
		{
			if (queuedResponses == null)
				queuedResponses = ObservableCollections.observableList(new ArrayList<ResponseBean>());

	    	SOAPFaultBean soapFaultBean = responseBean.getSOAPFault();
	    	if (soapFaultBean != null && timeoutOccurred)
	    	{
				String details = "Retry at ";
				String date = null;
				try {
					date = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a").format(soapFaultBean.getAvailableAfterDate());
				}
				catch (Exception e) {
					return false; 
				}
				responseBean.setStatus("Queued");
				responseBean.setDetails(details + date);
				responseBean.setRetryTime(soapFaultBean.getAvailableAfterDate().getTime());
				if (!queuedResponses.contains(responseBean))
					return queuedResponses.add(responseBean);
	    	}
	    	else
	    	{
	    		if (soapFaultBean != null && soapFaultBean.isReceiverError())
	    		{
	    			if (soapFaultBean.getCoreException() instanceof SBRUnavailableException)
	    			{
		    			if (soapFaultBean.hasAvailableAfter())
		    			{
		    				if (soapFaultBean.getAvailableAfterDate().after(new Date()))
		    				{
		    					String details = "Retry at ";
		    					String date = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a").format(soapFaultBean.getAvailableAfterDate());
		    					responseBean.setStatus("Queued");
		    					responseBean.setDetails(details + date);
		    					responseBean.setRetryTime(soapFaultBean.getAvailableAfterDate().getTime());
		    					if (!queuedResponses.contains(responseBean))
		    						return queuedResponses.add(responseBean);
		    				}
		    			}
		    			else 
		    				return defaultRetry();
	    			}
	    			else if (soapFaultBean.isInternalError())
	    			{
	    				responseBean.setDetails("");
	    				return defaultRetry();
	    			}
	    		}
	    		else if (soapFaultBean != null && soapFaultBean.isSenderError())
	    		{
	    			VanguardLookup vgLookup = new VanguardLookup(this.soapFault.getSOAPFault());
	    			soapFaultBean.setNotifyUser(vgLookup.getErrorDescription());
	    			if (vgLookup.isRetryable(vgLookup.getCode())) {
	    				soapFaultBean.setNotifyUser(vgLookup.getUserAdvice());
	    				return defaultRetry();
	    			}
	    			else
	    				responseBean.setDetails("");
	    		}
	    		else
	    			responseBean.setDetails("");
	    	}
	    	return false;
		}

		/**
		 * Set the time when the next retry attempt will be sent.
		 */
		boolean defaultRetry()
		{
			if (!isResendAvailable)
				return false;

			if (queuedResponses == null)
				queuedResponses = ObservableCollections.observableList(new ArrayList<ResponseBean>());

			ResourceBundle resources = ResourceBundle.getBundle(ClientBean.class
					.getName());

			int retryTimeMins, defaultAdditionalRetryTimeMins = 0;
			try {
				retryTimeMins = Integer.parseInt(resources.getString("default.retry.time.minutes"));
			}
			catch (NumberFormatException e) {
				retryTimeMins = 10;
			}

			try {
				defaultAdditionalRetryTimeMins = Integer.parseInt(resources.getString("default.additional_retry_time.minutes"));
			}
			catch (NumberFormatException e) {
				defaultAdditionalRetryTimeMins = 5;
			}

			int minutes = queuedResponses.contains(responseBean) ? 
					defaultAdditionalRetryTimeMins : retryTimeMins;

			responseBean.setLastRetryMinuteInterval(responseBean.getLastRetryMinuteInterval() + minutes);
			responseBean.setStatus("Queued");
			long time = new Date().getTime() + (responseBean.getLastRetryMinuteInterval() * 60000); //60,000 ms to 1 minute.
			String date = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a").format(new Date(time));
			responseBean.setDetails("Retry at " + date);
			responseBean.setRetryTime(time);
			if (!queuedResponses.contains(responseBean))
				return queuedResponses.add(responseBean);
			else
			{
				responseBean.clearRequestTimestamp();
				if (responseBean.getStatus().equals("Queued"))
					return true;
			}

			return false;
		}
	}

	private class RetrySendRequestTask extends RequestTask
	{
		public RetrySendRequestTask(final ResponseBean response, boolean isResendAvailable, Application app) 
		{
			super(isResendAvailable, app);
			isSecurityRequest = response.getCoreRequest().isSigning();

			if (isSecurityRequest) {
				privateKey = response.getCoreRequest().getPrivateKey();
				certificateChain = response.getCoreRequest().getCertificateChain();
			}

			req = response.getCoreRequest();
			responseBean = response;
			Thread thread = new Thread() {
				@Override
				public void run() {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							response.setDetails("");
							response.setStatus("Exception");
							queuedResponses.remove(response);
						}
					});
				}
			};
			thread.start();
			
			requestFactory = new SBRCoreServicesRequestFactory(req.getSoftwareInformation(),
					req.isLogging(), req.isSigning(), req.isSchemaValidation(),
					req.isMTOM(), certificateChain, privateKey);		
		}

		@Override
		protected Object doInBackground() throws SBRCoreException
		{
			ServiceType csrc = null;
			switch (selectedService)
			{
				case LIST: case ALTLIST: csrc = ServiceType.LIST; break;
				case PREFILL: csrc = ServiceType.PREFILL; break;
				case PRELODGE: csrc = ServiceType.PRELODGE; break;
				case LODGE: case ALTLODGE: csrc = ServiceType.LODGE; break;
				default: throw new IllegalStateException("Unknown service type");
			}

			req = requestFactory.createRequest(csrc, req.getAgencyIdentifier(), req.getMessageType(), req.getServiceEndpoint());

			// This method runs on a background thread, so don't reference
			// the Swing GUI from here.
			int lastMinuteInterval = responseBean.getLastRetryMinuteInterval();
			if (responseBean.getCoreRequest().getBusinessDocuments() != null && responseBean.getCoreRequest().getBusinessDocuments().size() > 0)
			{
				for (BusinessDocument busDoc : responseBean.getCoreRequest().getBusinessDocuments()) {
					req.addBusinessDocument(busDoc);
				}
			}

			responseBean = new ResponseBean(req);
			responseBean.setResponseOutstanding(true);
			responseBean.setStatus(responseBean.getStatus());
			responseBean.setLastRetryMinuteInterval(lastMinuteInterval);
			responseBean.setRequestServiceType(selectedService);
			
			queuedResponses.add(responseBean);
			ClientBean.this.responses.add(responseBean);

			try 
			{
				if (req.isSigning())
					token = getToken(responseBean.getCoreRequest().getServiceEndpoint());

				response = req.isSigning() ? (Response) req.getResponse(token.getAssertionAsXML(), 
						token.getProofTokenAsString(), token.getExpiryTime(), certificateChain, privateKey)
						: (Response) req.getResponse();			
			}
			catch (SBRTimeoutException e) 
			{
				this.soapFault = new SOAPFaultBean(e.getMessage());
				soapFaultBean = soapFault;
				timeoutOccurred = true;
				defaultRetry();
			}
			catch (SBRCoreException e) 
			{
				if (e.getMessage().contains("SOAPFaultException")) {
					this.soapFault = new SOAPFaultBean(e);
					soapFaultBean = soapFault;
				}
			}
			catch (SBRException e) {
				this.soapFault = new SOAPFaultBean(e.getMessage());
				soapFaultBean = soapFault;
			}
			catch (STMTimeoutException e) {
				this.soapFault = new SOAPFaultBean(e.getMessage());
				soapFaultBean = soapFault;
				timeoutOccurred = true;
				defaultRetry();
			}
			catch (STMFaultException e) {
				this.soapFault = new SOAPFaultBean(e.getMessage());
				soapFaultBean = soapFault;
				defaultRetry();
			}
			catch (STMCommunicationException e) {
				this.soapFault = new SOAPFaultBean(e.getMessage());
				defaultRetry();
			}
			catch (STMException e) 
			{
				final String msg = e.getMessage();

				logger.warning(msg == null ? "Authentication Exception occurred" : msg);
				if (msg != null)
				{
					soapFaultBean = new SOAPFaultBean(msg);
					responseBean.setSOAPFault(soapFaultBean);				
				}

				Thread thread = new Thread() {
					public void run() {
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								if (msg != null && msg.indexOf(':') > 0)
									responseBean.setDetails(msg.substring(0, msg.indexOf(":")));
								else
									responseBean.setDetails(msg);
							}
						});
					}
				};
				thread.start();
			}
			catch (RuntimeException e) {
				final String msg = e.getMessage();

				logger.warning(msg == null ? "Runtime exception occurred" : msg);
				Thread t = new Thread() {
					public void run() {
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								if (msg != null && msg.indexOf(':') > 0) {
									responseBean.setDetails(msg.substring(0, msg.indexOf(":")));
									soapFaultBean = new SOAPFaultBean(msg);
									responseBean.setSOAPFault(soapFaultBean);
								}
								else
									responseBean.setDetails(msg == null ? "Runtime exception occurred" : msg);								
							}
						});
					}
				};
				t.start();
			}

			return null; // return your result
		}
	}

	private class SendRequestTask extends RequestTask 
	{
		String endpointUrl;
		Form selectedForm;
		JRCServiceType serviceType;

		boolean isMtomRequest;

		SendRequestTask(boolean isResendAvailable, Application app) {
			// Runs on the EDT. Copy GUI state that
			// doInBackground() depends on from parameters
			// to SendRequestTask fields, here.
			super(isResendAvailable, app);

			this.selectedForm = ClientBean.this.getSelectedForm();
			this.serviceType = ClientBean.this.getSelectedService();

			this.isSecurityRequest = ClientBean.this.securityRequest;
			this.isMtomRequest = ClientBean.this.mtomRequest;
			this.endpointUrl = ClientBean.this.getEndpoint();

			if (isSecurityRequest) {
				// Get the alias out of the keystore now to avoid having to
				// cache or
				// otherwise ask for the password outside of the GUI thread.

				try {
					privateKey = ClientBean.this.keyStoreBean.getPrivateKey();
					certificateChain = ClientBean.this.keyStoreBean.getAlias().getCredential().getCertificateChain();
				} catch (CertificateChainException ex) {
					Logger.getLogger(ClientBean.class.getName()).log(
							Level.SEVERE, null, ex);
					throw new RuntimeException(ex);					
				} catch (InvalidP7CException ex) {
					Logger.getLogger(ClientBean.class.getName()).log(
							Level.SEVERE, null, ex);
					throw new RuntimeException(ex);					
				}
				catch (IllegalArgumentException ex) {
					Logger.getLogger(ClientBean.class.getName()).log(
							Level.SEVERE, null, ex);
					throw new RuntimeException(ex);
				} catch (NoSuchAliasException ex) {
					Logger.getLogger(ClientBean.class.getName()).log(
							Level.SEVERE, null, ex);
					throw new RuntimeException(ex);
				} catch (IncorrectPasswordException ex) {
					Logger.getLogger(ClientBean.class.getName()).log(
							Level.SEVERE, null, ex);
					throw new RuntimeException(ex);
				}
			}

			SoftwareInformationType softwareInformation = new SoftwareInformationType();
			softwareInformation
					.setOrganisationNameDetailsOrganisationalNameText(OrganisationNameCheck.getOrganisationName());

//			softwareInformation
//					.setSoftwareInformationProductNameText("SBR 2012 Reference Client");
//			softwareInformation
//					.setSoftwareInformationProductVersionText("v2.1.1");
			softwareInformation
			.setSoftwareInformationProductNameText("SBRCoreServicesTesting");
	softwareInformation
			.setSoftwareInformationProductVersionText("12.34.0.56");

			requestFactory = new SBRCoreServicesRequestFactory(softwareInformation, isLoggerRequest(), isSecurityRequest, isSchemaValidationRequest(), isMtomRequest, certificateChain, privateKey);
		}

		@Override
		protected Object doInBackground() throws SBRCoreException
		{
			// Your Task's code here. This method runs
			// on a background thread, so don't reference
			// the Swing GUI from here.
			ServiceType csrc = null;
			switch (serviceType)
			{
				case LIST: case ALTLIST: csrc = ServiceType.LIST; break;
				case PREFILL: csrc = ServiceType.PREFILL; break;
				case PRELODGE: csrc = ServiceType.PRELODGE; break;
				case LODGE: case ALTLODGE: csrc = ServiceType.LODGE; break;
				default: throw new IllegalStateException("Unknown service type");
			}

			try {
				req = requestFactory.createRequest(csrc, selectedForm.getReceiver(), selectedForm.getRequestType(selectedService), endpoint);

				//
				//Transform business document bean to Business document and
				//any attachment beans to Attachment then add to business document and request.
				//
				for (BusinessDocumentBean bean : businessDocuments)
				{
					BusinessDocument busDocument = new BusinessDocument(bean.getBusinessDocument(), bean.getBusinessDocumentInstance());
					if (bean.getAttachments() != null && bean.getAttachments().size() > 0)
					{
						for (AttachmentBean attachBean : bean.getAttachments())
						{
							//
							//Workaround to avoid ASIC SQL exception being thrown back. The
							//below 2 lines of code must be removed once ASIC have rectified the issue.
							//
							if (attachBean.getDescription() == null)
								attachBean.setDescription(attachBean.getFileName());

							busDocument.addAttachment(new Attachment(
									attachBean.getAttachment(), attachBean.getAttachmentInstance()));
						}
					}

					req.addBusinessDocument(busDocument);
				}

				ResponseBean responseBean = new ResponseBean(req);
				responseBean.setResponseOutstanding(true);
				responseBean.setRequestServiceType(selectedService);

				this.responseBean = responseBean;
				ClientBean.this.responses.add(responseBean);

				if (isSecurityRequest)
					token = getToken(endpointUrl);

				response = isSecurityRequest ? (Response) req.getResponse(token.getAssertionAsXML(), 
						token.getProofTokenAsString(), token.getExpiryTime(), certificateChain, privateKey)
						: (Response) req.getResponse();			
			} 
			catch (SBRCoreException e) 
			{
				if (e.getMessage() != null && e.getMessage().contains("SOAPFaultException")) {
					this.soapFault = new SOAPFaultBean(e);
					soapFaultBean = soapFault;
				}
				else
					throw new RuntimeException(e.getCause());
			}
			catch (SBRTimeoutException e) 
			{
				this.soapFault = new SOAPFaultBean(e.getMessage());
				soapFaultBean = soapFault;
				timeoutOccurred = true;
				defaultRetry();
			}
			catch (SBRException e) {
				this.soapFault = new SOAPFaultBean(e.getMessage());
				soapFaultBean = soapFault;
			}
			catch (STMTimeoutException e) {
				this.soapFault = new SOAPFaultBean(e.getMessage());
				soapFaultBean = soapFault;
				timeoutOccurred = true;
				defaultRetry();
			}
			catch (STMFaultException e) {
				this.soapFault = new SOAPFaultBean(e.getReasonText());
				soapFaultBean = soapFault;
				defaultRetry();
			}
			catch (STMCommunicationException e) {
				this.soapFault = new SOAPFaultBean(e.getMessage());
				defaultRetry();
			}
			catch (STMException e) 
			{
				final String msg = e.getMessage();
				logger.warning(msg == null ? "Authentication Exception occurred" : msg);
				if (msg != null) {
					soapFaultBean = new SOAPFaultBean(msg);
					responseBean.setSOAPFault(soapFaultBean);
				}
				Thread thread = new Thread() {
					public void run() {
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								if (msg != null && msg.indexOf(':') > 0)
									responseBean.setDetails(msg.substring(0, msg.indexOf(":")));
								else
									responseBean.setDetails(msg);
							}
						});
					}
				};
				thread.start();
			}
			catch (RuntimeException e) {

				final String msg = e.getMessage();

				logger.warning(msg == null ? "Runtime exception occurred" : msg);
				Thread t = new Thread() {
					public void run() {
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								if (msg != null && msg.indexOf(':') > 0) {
									responseBean.setDetails(msg.substring(0, msg.indexOf(":")));
									soapFaultBean = new SOAPFaultBean(msg);
									responseBean.setSOAPFault(soapFaultBean);
								}
								else
									responseBean.setDetails(msg == null ? "Runtime exception occurred" : msg);								
							}
						});
					}
				};
				t.start();
			}

			return null; // return your result
		}		
	}
}