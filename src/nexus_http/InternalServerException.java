package nexus_http;

import org.apache.http.HttpStatus;

/**
 * These exceptions are thrown when something goes wrong within the server itself, which 
 * prevents the request from completing
 * 
 * @author Mikko Hilpinen
 * @since 25.1.2015
 */
public class InternalServerException extends HttpException
{
	private static final long serialVersionUID = 7082966970856907614L;

	/**
	 * Creates a new exception
	 * @param message The message sent with the exception
	 */
	public InternalServerException(String message)
	{
		super(message, HttpStatus.SC_INTERNAL_SERVER_ERROR);
	}

	/**
	 * Creates a new exception
	 * @param message The message sent with the exception
	 * @param cause The cause of the exception
	 */
	public InternalServerException(String message, Throwable cause)
	{
		super(message, cause, HttpStatus.SC_INTERNAL_SERVER_ERROR);
	}
}
