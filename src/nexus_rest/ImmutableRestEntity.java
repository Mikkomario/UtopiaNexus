package nexus_rest;

import java.util.Map;

import nexus_http.HttpException;
import nexus_http.MethodNotSupportedException;
import nexus_http.MethodType;
import nexus_http.NotFoundException;

/**
 * These restEntities cannot be modified by client requests.
 * 
 * @author Mikko Hilpinen
 * @since 19.1.2015
 */
public class ImmutableRestEntity extends RestEntity
{
	// CONSTRUCTOR	----------------------------
	
	/**
	 * Creates a new entity with the given attributes. The entity can't be modified further.
	 * @param name The name of the entity
	 * @param parent The parent of the entity
	 * @param attributes The attributes given to the entity
	 */
	public ImmutableRestEntity(String name, RestEntity parent, Map<String, String> attributes)
	{
		super(name, new ImmutableRestData(attributes), parent);
	}
	
	
	// IMPLEMENTED METHODS	---------------------

	@Override
	public RestEntity Post(Map<String, String> parameters) throws HttpException
	{
		throw new MethodNotSupportedException(MethodType.POST);
	}

	@Override
	public void Put(Map<String, String> parameters) throws HttpException
	{
		throw new MethodNotSupportedException(MethodType.PUT);
	}

	@Override
	protected void prepareDelete(Map<String, String> parameters)
			throws HttpException
	{
		throw new MethodNotSupportedException(MethodType.DELETE);
	}

	@Override
	protected RestEntity getMissingEntity(String pathPart,
			Map<String, String> parameters) throws HttpException
	{
		throw new NotFoundException(getPath() + "/" + pathPart);
	}

	@Override
	protected Map<String, RestEntity> getMissingEntities(
			Map<String, String> parameters) throws HttpException
	{
		return null;
	}
}
