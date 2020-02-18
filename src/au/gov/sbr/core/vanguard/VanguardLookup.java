
package au.gov.sbr.core.vanguard;

import java.util.Arrays;
import java.util.List;

import javax.xml.soap.SOAPFault;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The Vanguard Lookup class determines if the current Vanguard Exception/error can be 
 * retried or not.
 * 
 *@author SBR
 */
public class VanguardLookup 
{
	/** Retryable Vanguard codes */
	private final String[] RETRYABLE_CODES = new String[] { 
		"E1001", "E1003", "E1004", "E2190"
	}; 

	/** SOAP Fault */
	private SOAPFault mFault;

	/**
	 * Create an instance of Vanguard lookup.
	 * @param fault
	 */
	public VanguardLookup(SOAPFault fault) {
		mFault = fault;
	}

	/**
	 * Get the Event code.
	 * @return String
	 */
	public String getCode()
	{
		if (mFault.getDetail() == null)
			return "";

		Node node = findNode(mFault.getDetail(), "eventcode", true, true);
		return node != null ? node.getTextContent() : "";
	}

	/**
	 * Get the user advice.
	 * @return String
	 */
	public String getUserAdvice()
	{
		if (mFault.getDetail() == null)
			return "";

		Node node = findNode(mFault.getDetail(), "useradvice", true, true);
		return node != null ? node.getTextContent() : "";
	}

	/**
	 * Get the error description.
	 * @return String
	 */
	public String getErrorDescription() 
	{
		List<LookupTable> values = Arrays.asList(LookupTable.values());
		boolean isCodeFound = false;
		for (LookupTable lookup : values)
		{
			if (lookup.toString().equalsIgnoreCase(getCode())) {
				isCodeFound = true;
				break;
			}
		}

		return values != null && isCodeFound ? 
				LookupTable.valueOf(getCode()).getErrorDescription() : "";
	}

	/**
	 * Determine if the error code can be retried or not.
	 * @param errorCode
	 * @return boolean
	 */
	public boolean isRetryable(String errorCode) 
	{
		if (errorCode == null)
			return false;

		for (int i = 0; i < RETRYABLE_CODES.length; i++)
		{
			if (RETRYABLE_CODES[i].equals(errorCode))
				return true;
		}

		return false;
	}

	/**
	 * Find node within the XML / SOAP Fault.
	 * @param root
	 * @param elementName
	 * @param deep
	 * @param elementsOnly
	 * @return Node
	 */
	private Node findNode(Node root, String elementName, boolean deep,
			boolean elementsOnly) {
		// Check to see if root has any children if not return null
		if (!(root.hasChildNodes()))
			return null;

		// Root has children, so continue searching for them
		Node matchingNode = null;
		String nodeName = null;
		Node child = null;

		NodeList childNodes = root.getChildNodes();
		int noChildren = childNodes.getLength();
		for (int i = 0; i < noChildren; i++) {
			if (matchingNode == null) {
				child = childNodes.item(i);
				nodeName = child.getNodeName().toLowerCase();
				if ((nodeName != null) & (nodeName.contains(elementName)))
					return child;
				if (deep)
					matchingNode = findNode(child, elementName, deep,
							elementsOnly);
			} else
				break;
		}

		if (!elementsOnly) {
			NamedNodeMap childAttrs = root.getAttributes();
			noChildren = childAttrs.getLength();
			for (int i = 0; i < noChildren; i++) {
				if (matchingNode == null) {
					child = childAttrs.item(i);
					nodeName = child.getNodeName();
					if ((nodeName != null) & (nodeName.equals(elementName)))
						return child;
				} else
					break;
			}
		}

		return matchingNode;
	}
}
