package nexus_rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nexus_http.HttpException;

/**
 * This is a very simple implementation of the RestEntityLinkList. The links cannot be 
 * modified by the client.
 * @author Mikko Hilpinen
 * @since 1.5.2015
 */
public class SimpleRestEntityLinkList extends RestEntityLinkList
{
	// ATTRIBUTES	------------------------
	
	private List<RestEntity> entities;
	
	
	// CONSTRUCTOR	------------------------
	
	/**
	 * Creates a new link list with the given set entities
	 * @param name The name of the list
	 * @param parent The parent entity of this list
	 * @param initialEntities The entities stored in the list
	 */
	public SimpleRestEntityLinkList(String name, RestEntity parent, 
			List<? extends RestEntity> initialEntities)
	{
		super(name, parent);
		
		this.entities = new ArrayList<>();
		if (initialEntities != null)
			this.entities.addAll(initialEntities);
	}

	
	// IMPLEMENTED METHODS	----------------
	
	@Override
	public void trim(Map<String, String> parameters)
	{
		// No trimming required
	}

	@Override
	protected List<RestEntity> getEntities() throws HttpException
	{
		return this.entities;
	}
}
