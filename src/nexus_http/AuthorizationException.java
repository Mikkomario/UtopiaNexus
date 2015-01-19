package nexus_http;

import org.apache.http.HttpStatus;

/**
 * These exceptions are caused by authorization issues
 * 
 * @author Mikko Hilpinen
 * @since 19.1.2015
 */
public class AuthorizationException extends HttpException
{
	private static final long serialVersionUID = 4151790179287415795L;

	/**
	 * Creates a new exception
	 */
	public AuthorizationException()
	{
		super("Authorization failed", HttpStatus.SC_UNAUTHORIZED);
	}

	/**
	 * Creates a new exception
	 * @param message The message sent to the client
	 */
	public AuthorizationException(String message)
	{
		super(message, HttpStatus.SC_UNAUTHORIZED);
	}
}
