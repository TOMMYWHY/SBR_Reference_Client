
package au.gov.sbr.core.client.beans;

import au.gov.sbr.comn.sbdm_02.AttachmentInstanceType;
import au.gov.sbr.comn.sbdm_02.AttachmentType;
import au.gov.sbr.comn.sbdm_02.MessageAttachmentInstanceBinaryObjectType;
import au.gov.sbr.core.client.util.FileUtil;
import java.math.BigInteger;

/**
 * This bean represents an Attachment and Attachment instances on the Standard
 * Business Document Message.
 * 
 * @author SBR
 */
public class AttachmentBean extends BeanBase {

	private static final long serialVersionUID = -6103020028352570809L;
	private AttachmentType attachment;
	private AttachmentInstanceType attachmentInstance;
	@SuppressWarnings("unused")
	private String displayName;

	/**
	 * Constructs a newly allocated AttachmentBean object that represents the
	 * attachment imported from a file.
	 * 
	 * @param fileName
	 *            name of the attachment file.
	 * @param fileContents
	 *            the bytes of contents from the file.
	 */
	public AttachmentBean(String fileName, byte[] fileContents) {
		this(new AttachmentType(), new AttachmentInstanceType());

		if (fileName == null) {
			throw new IllegalArgumentException("fileName");
		}

		if (fileContents == null) {
			throw new IllegalArgumentException("fileContents");
		}

		this.displayName = fileName;
		this.attachment.setMessageAttachmentFileNameText(fileName
				.substring(fileName.lastIndexOf('\\') + 1));

		this.attachmentInstance
				.setMessageAttachmentInstanceBinaryObject(new MessageAttachmentInstanceBinaryObjectType());
		this.attachmentInstance.getMessageAttachmentInstanceBinaryObject()
				.setValue(fileContents);

		this.attachmentInstance.getMessageAttachmentInstanceBinaryObject()
				.setContentType(FileUtil.getContentType(fileName));

	}

	/**
	 * Constructs a newly allocated AttachmentBean object that represents the
	 * attachment instance.
	 * 
	 * @param attachment
	 *            attachment object.
	 * @param attachmentInstance
	 *            attachment instance.
	 */
	public AttachmentBean(AttachmentType attachment,
			AttachmentInstanceType attachmentInstance) {
		super();

		if (attachment == null) {
			throw new IllegalArgumentException("attachment");
		}

		if (attachmentInstance == null) {
			throw new IllegalArgumentException("attachmentInstance");
		}

		this.attachment = attachment;
		this.attachmentInstance = attachmentInstance;

		this.displayName = attachment.getMessageAttachmentFileNameText();
	}

	/**
	 * Returns the content type of the attachment.
	 * 
	 * @return the attachment content type.
	 */
	public String getContentType() {
		return this.attachmentInstance
				.getMessageAttachmentInstanceBinaryObject().getContentType();
	}

	/**
	 * Sets the content type of the attachment.
	 * 
	 * @param contentType
	 *            attachment content type.
	 */
	public void setContentType(String contentType) {
		String oldContentType = this.attachmentInstance
				.getMessageAttachmentInstanceBinaryObject().getContentType();

		this.attachmentInstance.getMessageAttachmentInstanceBinaryObject()
				.setContentType(contentType);

		firePropertyChange("contentType", oldContentType, contentType);
	}

	/**
	 * Returns the description text of the attachment.
	 * 
	 * @return the attachment description text.
	 */
	public String getDescription() {
		return this.attachment.getMessageAttachmentDescriptionText();
	}

	/**
	 * Sets the description text of the attachment.
	 * 
	 * @param description
	 *            attachment description text.
	 */
	public void setDescription(String description) {
		String oldDescription = this.attachment
				.getMessageAttachmentDescriptionText();

		this.attachment.setMessageAttachmentDescriptionText(description);

		firePropertyChange("description", oldDescription, description);
	}

	/**
	 * Returns the bytes of content from the attachment.
	 * 
	 * @return the attachment content.
	 */
	public byte[] getFileContents() {
		return this.attachmentInstance
				.getMessageAttachmentInstanceBinaryObject().getValue();
	}

	/**
	 * Sets the bytes of content from the attachment.
	 * 
	 * @param fileContents
	 *            attachment content.
	 */
	public void setFileContents(byte[] fileContents) {
		byte[] oldFileContents = this.attachmentInstance
				.getMessageAttachmentInstanceBinaryObject().getValue();

		this.attachmentInstance.getMessageAttachmentInstanceBinaryObject()
				.setValue(fileContents);

		firePropertyChange("fileContents", oldFileContents, fileContents);
	}

	/**
	 * Returns the filename of the attachment.
	 * 
	 * @return the attachment filename.
	 */
	public String getFileName() {
		return this.attachment.getMessageAttachmentFileNameText();
	}

	/**
	 * Sets the filename of the attachment.
	 * 
	 * @param fileName
	 *            attachment filename.
	 */
	public void setFileName(String fileName) {
		String oldFileName = this.attachment.getMessageAttachmentFileNameText();

		this.attachment.setMessageAttachmentFileNameText(fileName);

		firePropertyChange("fileName", oldFileName, fileName);
	}

	/**
	 * Returns the sequence number of the attachment.
	 * 
	 * @return the sequence number.
	 */
	public BigInteger getSequenceNumber() {
		return this.attachment.getMessageAttachmentSequenceNumber();
	}

	/**
	 * Sets the sequence number of the attachment.
	 * 
	 * @param sequenceNumber
	 *            sequence number.
	 */
	public void setSequenceNumber(BigInteger sequenceNumber) {
		BigInteger oldSequenceNumber = this.attachment
				.getMessageAttachmentSequenceNumber();

		this.attachment.setMessageAttachmentSequenceNumber(sequenceNumber);
		this.attachmentInstance
				.setMessageAttachmentSequenceNumber(sequenceNumber);

		firePropertyChange("sequenceNumber", oldSequenceNumber, sequenceNumber);
	}

	// Note that there is no property change support for the AttachmentType
	// and AttachmentInstanceType objects. These are read-only properties.
	/**
	 * Returns the attachment.
	 * 
	 * @return the attachment.
	 */
	public AttachmentType getAttachment() {
		return attachment;
	}

	/**
	 * Returns the attachment instance.
	 * 
	 * @return the attachment instance.
	 */
	public AttachmentInstanceType getAttachmentInstance() {
		return attachmentInstance;
	}
}
