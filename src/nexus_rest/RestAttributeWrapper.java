package nexus_rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nexus_http.HttpException;
import nexus_http.MethodNotSupportedException;
import nexus_http.MethodType;
import nexus_http.NotFoundException;

/**
 * RestAttributeWrapper holds a single attribute value and is used for presenting limited 
 * data wrapped in a restEntity.
 * 
 * @author Mikko Hilpinen
 * @since 19.1.2015
 */
public class RestAttributeWrapper extends TemporaryRestEntity
{
	// CONSTRUCTOR	-------------------------------
	
	/**
	 * Creates a new wrapper around the given key-value pair. The entity will not be attached 
	 * to any parent entity directly and is meant for temporary data representation only.
	 * 
	 * @param name The name of the wrapped attribute in the parent entity
	 * @param parent The entity whose attribute is presented
	 */
	public RestAttributeWrapper(String name, RestEntity parent)
	{
		super(name, new SimpleRestData(), parent);
		
		Map<String, String> attributes = parent.getContent().getAttributes();
		if (attributes.containsKey(name))
			getContent().setAttribute(name, attributes.get(name));
	}

	
	// IMPLEMENTED METHODS	-----------------------
	
	@Override
	public RestEntity getEntity(String pathPart, Map<String, String> parameters) 
			throws NotFoundException
	{
		// Wrappers don't have any entities under them since they 
		// already represent an attribute
		throw new NotFoundException(getPath() + pathPart);
	}

	@Override
	public void Put(Map<String, String> parameters) throws HttpException
	{
		// Attributes can't be modified from within
		throw new MethodNotSupportedException(MethodType.PUT);
	}

	@Override
	protected void prepareDelete(Map<String, String> parameters)
			throws HttpException
	{
		// Attributes can't be deleted either
		throw new MethodNotSupportedException(MethodType.DELETE);
	}

	@Override
	protected RestEntity getMissingEntity(String pathPart,
			Map<String, String> parameters) throws NotFoundException
	{
		throw new NotFoundException(getPath() + "/" + pathPart);
	}

	@Override
	protected RestEntityList wrapIntoList(String name, RestEntity parent,
			List<RestEntity> entities)
	{
		return new SimpleRestEntityList(name, parent, entities);
	}

	@Override
	protected Map<String, RestEntity> getMissingEntities(Map<String, String> parameters)
	{
		// There are no entities under attributes
		return new HashMap<>();
	}
}
