package nexus_test;

import java.util.Map;

import nexus_http.HttpException;
import nexus_http.InvalidParametersException;
import nexus_http.NotFoundException;
import nexus_rest.RestEntity;
import nexus_rest.SimpleRestData;

/**
 * This class is used for testing the basic methods of restEntities
 * 
 * @author Mikko Hilpinen
 * @since 19.1.2015
 */
public class TestRestEntity extends RestEntity
{
	// CONSTRUCTOR	----------------------------------
	
	/**
	 * Creates a new empty entity
	 * @param name The name of the entity
	 * @param parent The parent of this entity
	 */
	public TestRestEntity(String name, RestEntity parent)
	{
		super(name, new SimpleRestData(), parent);
	}
	
	
	// IMPLEMENTED METHODS	--------------------------

	@Override
	public RestEntity Post(Map<String, String> parameters) throws HttpException
	{
		String name = parameters.get("name");
		if (name == null)
			throw new InvalidParametersException("name not provided");
		
		return new TestRestEntity(name, this);
	}

	@Override
	public void Put(Map<String, String> parameters) throws HttpException
	{
		for (String parameterName : parameters.keySet())
		{
			getContent().setAttribute(parameterName, parameters.get(parameterName));
		}
	}

	@Override
	protected void prepareDelete(Map<String, String> parameters)
			throws HttpException
	{
		// No preparation required
	}

	@Override
	protected RestEntity getMissingEntity(String pathPart,
			Map<String, String> parameters) throws NotFoundException
	{
		// Doesn't hold special entities
		throw new NotFoundException(getPath() + "/" + pathPart);
	}
}
