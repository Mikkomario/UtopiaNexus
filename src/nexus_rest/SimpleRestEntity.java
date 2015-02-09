package nexus_rest;

import java.util.HashMap;
import java.util.Map;

import nexus_http.HttpException;
import nexus_http.MethodNotSupportedException;
import nexus_http.MethodType;
import nexus_http.NotFoundException;

/**
 * Simple restEntities don't limit any methods but don't enhance them either.
 * 
 * @author Mikko Hilpinen
 * @since 19.1.2015
 */
public class SimpleRestEntity extends RestEntity
{
	// CONSTRUCTOR	-------------------------------
	
	/**
	 * Creates a new restEntity under the given entity
	 * @param name The name of the restEntity
	 * @param content The content within this restEntity
	 * @param parent The parent entity of this entity (optional)
	 */
	public SimpleRestEntity(String name, RestData content, RestEntity parent)
	{
		super(name, content, parent);
	}
	
	
	// IMPLEMENTED METHODS	----------------------

	@Override
	public RestEntity Post(Map<String, String> parameters) throws HttpException
	{
		throw new MethodNotSupportedException(MethodType.POST);
	}

	@Override
	public void Put(Map<String, String> parameters) throws HttpException
	{
		defaultPut(parameters);
	}

	@Override
	protected void prepareDelete(Map<String, String> parameters) throws HttpException
	{
		// No preparation required
	}

	@Override
	protected RestEntity getMissingEntity(String pathPart,
			Map<String, String> parameters) throws HttpException
	{
		// Simple RestEntities don't have any special entities beneath them
		throw new NotFoundException(getPath() + "/" + pathPart);
	}

	@Override
	protected Map<String, RestEntity> getMissingEntities(Map<String, String> parameters)
	{
		return new HashMap<>();
	}
}
