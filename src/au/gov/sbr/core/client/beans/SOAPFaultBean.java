
package au.gov.sbr.core.client.beans;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;

import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.observablecollections.ObservableList;

import au.gov.sbr.core.requester.exceptions.SBRCoreException.SBRCode;
import au.gov.sbr.core.requester.exceptions.SBRCoreException;
import au.gov.sbr.core.requester.exceptions.SBRReceiverException;
import au.gov.sbr.core.requester.exceptions.SBRSenderException;
import au.gov.sbr.core.requester.exceptions.SBRUnavailableException;

/**
 * This bean class represents the SOAP Fault / SBR Core Exception pair 
 * that may have been raised during communications with SBR Core Services.
 */
public class SOAPFaultBean extends BeanBase
{
	private static final long serialVersionUID = 1L;
	private SOAPFault mSOAPFault;
	private SBRCode mFaultCode;
	private SBRCode mFaultSubcode;
	private String mNotifyUser;
	private ObservableList<SBRCode> mFaultSubcodes;
	private ObservableList<String> mFaultReasons;
	private SBRCoreException mCoreException;
	private String mOverrideDetailString = "";

	SOAPFaultBean(String overrideDetailString) {
		mOverrideDetailString = overrideDetailString;
	}

	@SuppressWarnings("unchecked")
	public SOAPFaultBean(SBRCoreException coreException)
	{
		super();
		if (coreException == null)
			throw new IllegalArgumentException("SBR Core Exception must not be null.");

		mCoreException = coreException;
		mSOAPFault = (SOAPFault) coreException.getNode();

		SBRCode code = coreException.getCode();
		mFaultCode = code;
		setDisplayName(mSOAPFault.getTextContent());
		mFaultSubcodes = ObservableCollections.observableList(coreException.getSubCodeHierarchy());
		mFaultReasons = ObservableCollections.observableList(new ArrayList<String>());

		if (mFaultSubcodes.size() > 1)
		{
			setFaultSubcode(mFaultSubcodes.get(1));
			mFaultSubcodes.remove(1);
		}

		try
		{
			Iterator<String> reasons = ((SOAPFault)coreException.getNode()).getFaultReasonTexts();
			while (reasons.hasNext())
				mFaultReasons.add(reasons.next());
		}
		catch (SOAPException e) {
			e.printStackTrace();
		}
	}

	public SBRCode getFaultCode() {
		return mFaultCode;
	}

	public void setFaultCode(SBRCode faultCode) {
		mFaultCode = faultCode;
	}

	public SOAPFault getSOAPFault() {
		return mSOAPFault;
	}

	public void setFaultSubcode(SBRCode faultSubcode) {
		mFaultSubcode = faultSubcode;
	}

	public SBRCode getFaultSubcode() {
		return mFaultSubcode;
	}

	public void setSOAPFault(SOAPFault fault)
	{
		SOAPFault oldFault = mSOAPFault;
		mSOAPFault = fault;
		firePropertyChange("soapFault", oldFault, mSOAPFault);
	}

	public String getFaultReason() {
		StringBuffer sb = new StringBuffer();
		if (mFaultReasons != null)
		{
			for (String reason : mFaultReasons)
				sb.append(reason + System.getProperty("line.separator"));
		}

		return new String(sb).trim();
	}

	public ObservableList<SBRCode> getFaultSubcodes() {
		return mFaultSubcodes;
	}

	public void setFaultSubcodes(ObservableList<SBRCode> faultSubcodes) {
		mFaultSubcodes = faultSubcodes;
	}

	public String getFaultNode() {
		return mSOAPFault != null ? mSOAPFault.getFaultNode() : null;
	}

	public void setFaultNode(String node) {
		try {
			mSOAPFault.setFaultNode(node);
		} catch (SOAPException e) {
			e.printStackTrace();
		}
	}

	public String getDetail() {
		return mSOAPFault != null && mSOAPFault.getDetail() != null && 
		mSOAPFault.getDetail().getTextContent() != null 
			? mSOAPFault.getDetail().getTextContent().trim() : mOverrideDetailString;
	}

	public String getNotifyUser() {
		return mNotifyUser;
	}

	public void setNotifyUser(String notify) {
		String oldNotify = mNotifyUser;
		mNotifyUser = notify;
		firePropertyChange("NotifyUser", oldNotify, mNotifyUser);
	}

	public SBRCoreException getCoreException() {
		return mCoreException;
	}

	public boolean hasAvailableAfter() {
		return mCoreException instanceof SBRUnavailableException && 
			((SBRUnavailableException)mCoreException).hasAvailableAfter();
	}

	public boolean isInternalError()
	{
		if (isReceiverError()) 
		{
			for (SBRCode code : getFaultSubcodes())
			{
				if (code.getValue().toLowerCase().contains("sbr.gen.fault.internalerror")) {
					return true;
				}
			}
		}

		return false;
	}

	public boolean isReceiverError() {
		return mCoreException instanceof SBRReceiverException;
	}

	public boolean isSenderError() {
		return mCoreException instanceof SBRSenderException ||
			(getFaultCode() != null && getFaultCode().getValue() != null &&
			getFaultCode().getValue().toLowerCase().contains("sender"));
	}

	public Date getAvailableAfterDate() {
		if (!(mCoreException instanceof SBRUnavailableException))
			return null;

		return ((SBRUnavailableException)mCoreException).getAvailableAfter();
	}
}
