package nexus_rest;

/**
 * The contentType determines in which form the information is sent from the server
 * @author Mikko Hilpinen
 * @since 8.5.2015
 */
public enum ContentType
{
	@SuppressWarnings("javadoc")
	XML,
	@SuppressWarnings("javadoc")
	JSON;
	
	
	// METHODS	--------------------------
	
	/**
	 * @return The apache content type represented by this content type
	 */
	public org.apache.http.entity.ContentType getApacheContentType()
	{
		if (this == XML)
			return org.apache.http.entity.ContentType.TEXT_XML;
		else
			return org.apache.http.entity.ContentType.APPLICATION_JSON;
	}
	
	/**
	 * Returns a content type represented by the string
	 * @param s A string that should represent a content type
	 * @return A content type or null if the string couldn't be parsed
	 */
	public static ContentType parseFromString(String s)
	{
		for (ContentType type : values())
		{
			if (type.toString().equalsIgnoreCase(s))
				return type;
		}
		
		return null;
	}
}
