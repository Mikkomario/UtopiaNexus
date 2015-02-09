package nexus_http;

import org.apache.http.HttpStatus;

/**
 * NotFoundException is thrown when an entity or an attribute cannot be found from the server
 * 
 * @author Mikko Hilpinen
 * @since 19.1.2015
 */
public class NotFoundException extends HttpException
{
	// ATTRIBUTES	--------------------------
	
	private static final long serialVersionUID = 6174757715158682257L;

	
	// CONSTRUCTOR	--------------------------
	
	/**
	 * Creates a new exception
	 * @param requestedPath The path that couldn't be found
	 */
	public NotFoundException(String requestedPath)
	{
		super(requestedPath + " was not found on the server", HttpStatus.SC_NOT_FOUND);
	}
}
