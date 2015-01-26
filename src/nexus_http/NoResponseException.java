package nexus_http;

/**
 * These exceptions are thrown when no response can be received from the server
 * @author Mikko Hilpinen
 * @since 26.1.2015
 */
public class NoResponseException extends Exception
{
	private static final long serialVersionUID = -4203770766231370272L;

	/**
	 * Creates a new exception
	 * @param message The message sent along with the exception
	 */
	public NoResponseException(String message)
	{
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Creates a new exception
	 * @param cause The cause of the exception
	 */
	public NoResponseException(Throwable cause)
	{
		super(cause);
		// TODO Auto-generated constructor stub
	}
}
