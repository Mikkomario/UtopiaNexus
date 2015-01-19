package nexus_rest;

import java.util.Map;

import nexus_http.HttpException;
import nexus_http.MethodNotSupportedException;
import nexus_http.MethodType;

/**
 * Simple restEntities don't limit any methods but don't enhance them either.
 * 
 * @author Mikko Hilpinen
 * @since 19.1.2015
 */
public class SimpleRestEntity extends RestEntity
{
	// TODO: Is this really required?
	
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
	public RestEntity Post(Map<String, String> parameters) throws MethodNotSupportedException
	{
		throw new MethodNotSupportedException(MethodType.POST);
	}

	@Override
	public void Put(Map<String, String> parameters)
	{
		for (String parameterName : parameters.keySet())
		{
			getContent().setAttribute(parameterName, parameters.get(parameterName));
		}
	}

	@Override
	protected void prepareDelete(Map<String, String> parameters) throws HttpException
	{
		// Deletes all the children
		for (int i = 0; i < getChildAmount(); i++)
		{
			((RestEntity) getChild(i)).delete(parameters);
		}
	}
}
