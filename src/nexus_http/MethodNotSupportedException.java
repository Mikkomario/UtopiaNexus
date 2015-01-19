package nexus_http;

import org.apache.http.HttpStatus;

/**
 * These exceptions are thrown when an entity doesn't support the method the client uses
 * 
 * @author Mikko Hilpinen
 * @since 19.1.2015
 */
public class MethodNotSupportedException extends HttpException
{
	private static final long serialVersionUID = -2388225317267500019L;

	/**
	 * Creates a new exception
	 * @param method The method that caused this exception
	 */
	public MethodNotSupportedException(MethodType method)
	{
		super("Unsupported method: " + method, HttpStatus.SC_METHOD_NOT_ALLOWED);
	}
}
