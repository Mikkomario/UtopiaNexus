package nexus_http;

import org.apache.http.HttpStatus;

/**
 * This exception is thrown when the given parameters are not provided or can't be used
 * 
 * @author Mikko Hilpinen
 * @since 19.1.2015
 */
public class InvalidParametersException extends HttpException
{
	private static final long serialVersionUID = 3873358568557113083L;

	/**
	 * Creates a new exception
	 * @param message The message returned to the client
	 */
	public InvalidParametersException(String message)
	{
		super(message, HttpStatus.SC_BAD_REQUEST);
	}
}
