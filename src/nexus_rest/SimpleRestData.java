package nexus_rest;

import java.util.HashMap;
import java.util.Map;

/**
 * SimpleRestData is the most simple form of RestData that can be used along with restEntities
 * 
 * @author Mikko Hilpinen
 * @since 19.1.2015
 */
public class SimpleRestData implements RestData
{
	// ATTRIBUTES	---------------------------
	
	private Map<String, String> attributes;
	
	
	// CONSTRUCTOR	---------------------------
	
	/**
	 * Creates a new empty data collection
	 */
	public SimpleRestData()
	{
		this.attributes = new HashMap<>();
	}
	
	
	// IMPLEMENTED METHODS	-------------------

	@Override
	public Map<String, String> getAttributes()
	{
		return this.attributes;
	}

	@Override
	public void setAttribute(String attributeName, String attributeValue)
	{
		this.attributes.put(attributeName, attributeValue);
	}
}
