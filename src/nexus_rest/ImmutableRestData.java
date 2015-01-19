package nexus_rest;

import java.util.HashMap;
import java.util.Map;

/**
 * This data is immutable, it allows the use of setAttribute but changes won't be made
 * 
 * @author Mikko Hilpinen
 * @since 19.1.2015
 */
public class ImmutableRestData implements RestData
{
	// ATTRIBUTES	------------------------------
	
	private Map<String, String> attributes;
	
	
	// CONSTRUCTOR	------------------------------
	
	/**
	 * Creates new data
	 * @param attributes The attributes that will be given to the data. A copy of this map 
	 * will be used so changes in it won't affect the data
	 */
	public ImmutableRestData(Map<String, String> attributes)
	{
		this.attributes = new HashMap<>();
		this.attributes.putAll(attributes);
	}

	
	// IMPLEMENTED METHODS	---------------------
	
	@Override
	public Map<String, String> getAttributes()
	{
		return this.attributes;
	}

	@Override
	public void setAttribute(String attributeName, String attributeValue)
	{
		// No changes will be made, whatsoever
	}
}
