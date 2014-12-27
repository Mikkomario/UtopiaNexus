package nexus_http;

/**
 * MethodType describes all the different methods supported by the server. 
 * The enumeration also describes how to use these methods.
 * 
 * @author Mikko Hilpinen
 * @since 23.7.2014
 */
public enum MethodType
{
	/**
	 * GET retrieves data from the server. This does not affect the 
	 * server's status in any way.
	 */
	GET, 
	/**
	 * POST creates a new entity to the server. This fails if there's 
	 * already a similar entity on the server. Post requires all parameters in order to create 
	 * a new object.
	 */
	POST, 
	/**
	 * PUT modifies an existing entity on the server. This fails if 
	 * there isn't already an entity to modify. PUT allows the user to leave out unnecessary 
	 * parameters.
	 */
	PUT, 
	/**
	 * DELETE removes an entity (and all connected entities) from the 
	 * server.
	 */
	DELETE, 
	/**
	 * Head works like get except that no content is returned.
	 */
	HEAD;
	
	/**
	 * Returns a method that is represented with the given string.
	 * 
	 * @param methodString The string that represents a method.
	 * @return A method represented by the given string. Null if the 
	 * string doesn't represent a method.
	 */
	public static MethodType parseFromString(String methodString)
	{
		for (MethodType method : MethodType.values())
		{
			if (methodString.equalsIgnoreCase(method.toString()))
				return method;
		}
		
		return null;
	}
}