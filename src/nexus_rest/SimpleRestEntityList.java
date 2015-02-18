package nexus_rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nexus_http.HttpException;
import nexus_http.MethodNotSupportedException;
import nexus_http.MethodType;

/**
 * These are simple 
 * @author Huoltokäyttis
 *
 */
public class SimpleRestEntityList extends RestEntityList
{
	// ATTRIBUTES	-------------------------------
	
	private List<RestEntity> entities;
	
	
	// CONSTRUCTOR	-------------------------------
	
	/**
	 * Creates a new entityList with the given content
	 * @param name The name of the list
	 * @param parent The parent of the list
	 * @param initialEntities The entities that will fill the list
	 */
	public SimpleRestEntityList(String name, RestEntity parent,
			List<RestEntity> initialEntities)
	{
		super(name, parent);
		
		this.entities = new ArrayList<>();
		if (initialEntities != null)
			this.entities.addAll(initialEntities);
	}
	
	
	// IMPLEMENTED METHODS	----------------------

	@Override
	public void Put(Map<String, String> parameters) throws HttpException
	{
		// Lists don't use attributes
		throw new MethodNotSupportedException(MethodType.PUT);
	}

	@Override
	public void trim(Map<String, String> parameters)
	{
		// Simple lists cannot be sorted since they can't compare entities
	}

	@Override
	protected List<RestEntity> getEntities()
	{
		return this.entities;
	}
}
