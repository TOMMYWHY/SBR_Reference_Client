package au.gov.sbr.core.client.gui;

public enum JRCServiceType 
{
	//
	//ENUM types.
	//Note:  Some form types have 2 form request that use the LIST or LODGE core web service.
	//		 As a result, ALTLIST and ALTLODGE have been added to cater for this.
	//
	LIST("list"), LODGE("lodge"), PREFILL("prefill"), PRELODGE("prelodge"), 
	ALTLIST("list"), ALTLODGE("lodge");

	String urlPart;

	/**
	 * The structure of each enum constant of this type. Each JRCServiceType has an
	 * associated url part that is utilised in a message request.
	 */
	JRCServiceType(String urlPart) {
		this.urlPart = urlPart;
	}

	/**
	 * Returns the associated url part of the enum constant.
	 * 
	 * @return the associated url part.
	 */
	public String getUrlPart() {
		return urlPart;
	}
}
