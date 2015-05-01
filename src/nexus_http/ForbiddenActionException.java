package nexus_http;

import org.apache.http.HttpStatus;

import nexus_http.HttpException;

/**
 * These exceptions are thrown when requests are denied based on gameplay mechanics
 * @author Mikko Hilpinen
 * @since 29.3.2015
 */
public class ForbiddenActionException extends HttpException
{
	private static final long serialVersionUID = 9101650731721247316L;

	
	// CONSTRUCTOR	-----------------------
	
	/**
	 * Creates a new exception with the given message
	 * @param message The message sent to the client
	 */
	public ForbiddenActionException(String message)
	{
		super(message, HttpStatus.SC_FORBIDDEN);
	}
}
