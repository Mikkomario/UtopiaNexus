package nexus_http;

/**
 * HttpExceptions are thrown when requests cannot be handled.
 * 
 * @author Mikko Hilpinen
 * @since 19.1.2015
 */
public class HttpException extends Exception
{
	// ATTRIBUTES	------------------------------
	
	private static final long serialVersionUID = 9090384916105485427L;
	private final int status;
	
	
	// CONSTRUCTOR	-----------------------------
	
	/**
	 * Creates a new exception
	 * 
	 * @param message The message sent to the client
	 * @param status The status code sent to the client
	 */
	public HttpException(String message, int status)
	{
		super(message);
		
		this.status = status;
	}
	
	/**
	 * Creates a new exception
	 * 
	 * @param message The message sent to the client
	 * @param cause The exception that caused this exception
	 * @param status The status code sent to the client
	 */
	public HttpException(String message, Throwable cause, int status)
	{
		super(message, cause);
		
		this.status = status;
	}
	
	
	// GETTERS & SETTERS	---------------------
	
	/**
	 * @return The Http status code associated with this exception
	 */
	public int getStatusCode()
	{
		return this.status;
	}
}
