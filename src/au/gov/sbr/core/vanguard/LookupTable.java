
package au.gov.sbr.core.vanguard;

/**
 * Message table for displayable Vanguard errors.
 * 
 *@author SBR
 */
enum LookupTable 
{
	E2183("A mandatory request was made for an unrecognised claim."),
	E2014("The credential supplied by the initiating party has been revoked."),
	E2169("The credential supplied by the initiating party is not recognised."),
	E2015("The credential supplied by the initiating party has expired."),
	E2017("The validity start date of the credential supplied by the initiating party is in the future."),
	E2029("The credential supplied by the initiating party could not be processed and may be corrupt."),
	E2020("The Credential Authority that issued the credential supplied by the initiating party is not recognised."),
	E2180("No usage policy for the credential supplied could be found.  This would occur if a " +
			"certificate that was valid but not supported by the STS was presented."),
	E2003("The relying party specified in the AppliesTo element is not recognised."),
	E1001("The request could not be satisfied due to an internal Vanguard error."),
	E1003("The request could not be satisfied due to an internal Vanguard error."),
	E1004("The request could not be satisfied due to an internal Vanguard error."),
	E2190("Claim data could not be found due to an internal Vanguard error. Attempt this request again."),
	E2182("A mandatory claim specified in the request could not be provided.  " +
			"Check the claim types being specified in the request.");
	
	String errorDesc;

	LookupTable(String errorDesc) {
		this.errorDesc = errorDesc;
	}

	public String getErrorDescription() {
		return errorDesc;
	}
}
