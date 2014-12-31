package nexus_rest;

import java.util.Map;

/**
 * RestData represents some data contained within a RestEntity.
 * 
 * @author Mikko Hilpinen
 * @since 31.12.2014
 */
public interface RestData
{
	/**
	 * @return The key-value-pairs of the attributes the data contains.
	 */
	public Map<String, String> getAttributes();
	
	/**
	 * Changes an attribute in the data
	 * 
	 * @param attributeName The name of the attribute
	 * @param attributeValue The new value given to the attribute
	 */
	public void setAttribute(String attributeName, String attributeValue);
}
