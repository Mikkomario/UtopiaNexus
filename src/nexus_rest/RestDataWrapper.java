package nexus_rest;

import java.util.Map;

import nexus_http.NotFoundException;

/**
 * RestDataWrapper holds a single attribute key value and is used for presenting limited 
 * data wrapped in a restEntity.
 * 
 * @author Mikko Hilpinen
 * @since 19.1.2015
 */
public class RestDataWrapper extends SimpleRestEntity
{
	// ATTRIBUTES	-------------------------------
	
	private String path;
	
	
	// CONSTRUCTOR	-------------------------------
	
	/**
	 * Creates a new wrapper around the given key-value pair. The entity will not be attached 
	 * to any parent entity directly and is meant for temporary data representation only.
	 * 
	 * @param name The name of the wrapped attribute in the parent entity
	 * @param parent The entity whose attribute is presented
	 */
	public RestDataWrapper(String name, RestEntity parent)
	{
		super(name, new SimpleRestData(), null);
		
		Map<String, String> attributes = parent.getContent().getAttributes();
		if (attributes.containsKey(name))
			getContent().setAttribute(name, attributes.get(name));
		this.path = parent.getPath() + "/" + name;
	}

	
	// IMPLEMENTED METHODS	-----------------------
	
	@Override
	public String getPath()
	{
		return this.path;
	}
	
	@Override
	public RestEntity getEntity(String pathPart) throws NotFoundException
	{
		// Wrappers don't have any entities under them since they 
		// already represent an attribute
		throw new NotFoundException(getPath() + pathPart);
	}
}
