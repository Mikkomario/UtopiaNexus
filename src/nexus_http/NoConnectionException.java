package nexus_http;

/**
 * This exception is thrown when the server can't be reached
 * @author Mikko Hilpinen
 * @since 26.1.2015
 */
public class NoConnectionException extends Exception
{
	static final long serialVersionUID = -3408350043116404686L;

	/**
	 * Creates a new exception
	 * @param message The message sent along with the exception
	 */
	public NoConnectionException(String message)
	{
		super(message);
	}

	/**
	 * Creates a new exception
	 * @param cause The cause of the exception
	 */
	public NoConnectionException(Throwable cause)
	{
		super(cause);
	}

	/**
	 * Creates a new exception
	 * @param message The message sent along with the exception
	 * @param cause The cause of the exception
	 */
	public NoConnectionException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
