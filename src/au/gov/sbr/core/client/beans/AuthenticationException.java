
package au.gov.sbr.core.client.beans;

/**
 * This class and its subclasses are a form of Throwable that indicates
 * conditions that this application might want to catch
 * 
 * @author SBR
 */
public class AuthenticationException extends Exception {

	private static final long serialVersionUID = -5814849769674397433L;

	/**
	 * Constructs a new exception with the specified cause and a detail message
	 * of (cause==null ? null : cause.toString()) (which typically contains the
	 * class and detail message of cause).
	 * 
	 * This constructor is useful for exceptions that are little more than
	 * wrappers for other throwables (for example, PrivilegedActionException).
	 * 
	 * @param cause
	 *            the cause (which is saved for later retrieval by the
	 *            Throwable.getCause() method). (A null value is permitted, and
	 *            indicates that the cause is nonexistent or unknown.)
	 */
	public AuthenticationException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a new exception with the specified detail message and cause.
	 * 
	 * Note that the detail message associated with cause is not automatically
	 * incorporated in this exception's detail message.
	 * 
	 * @param message
	 *            the detail message (which is saved for later retrieval by the
	 *            Throwable.getMessage() method).
	 * @param cause
	 *            the cause (which is saved for later retrieval by the
	 *            Throwable.getCause() method). (A null value is permitted, and
	 *            indicates that the cause is nonexistent or unknown.)
	 */
	public AuthenticationException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new exception with the specified detail message.
	 * 
	 * The cause is not initialized, and may subsequently be initialized by a
	 * call to Throwable.initCause(java.lang.Throwable).
	 * 
	 * @param message
	 *            the detail message. The detail message is saved for later
	 *            retrieval by the Throwable.getMessage() method.
	 */
	public AuthenticationException(String message) {
		super(message);
	}

	/**
	 * Constructs a new exception with null as its detail message.
	 * 
	 * The cause is not initialized, and may subsequently be initialized by a
	 * call to Throwable.initCause(java.lang.Throwable).
	 */
	public AuthenticationException() {
	}
}
