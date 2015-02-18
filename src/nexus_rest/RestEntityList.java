package nexus_rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import nexus_http.HttpException;
import nexus_http.InvalidParametersException;
import nexus_http.MethodNotSupportedException;
import nexus_http.MethodType;
import nexus_http.NotFoundException;

/**
 * This list contains a set of entities. It is used as a tool for requesting multiple entities 
 * at once.
 * 
 * @author Mikko Hilpinen
 * @since 25.1.2015
 */
public abstract class RestEntityList extends TemporaryRestEntity
{
	// ATTRIBUTES	--------------------------
	
	private boolean trimmed;
	
	
	// CONSTRUCTOR	--------------------------
	
	/**
	 * Creates a new restEntityList that contains the given entities
	 * @param name The name of the list itself
	 * @param parent The parent of this list (the list won't be added as a child since it's 
	 * only temporary)
	 */
	public RestEntityList(String name, RestEntity parent)
	{
		super(name, new SimpleRestData(), parent);
		
		this.trimmed = false;
	}
	
	
	// ABSTRACT METHODS	------------------------
	
	/**
	 * This method should modify the list (including possible sorting) according to 
	 * the parameters provided by the client
	 * @param parameters The parameters provided by the client
	 */
	public abstract void trim(Map<String, String> parameters);
	
	/**
	 * @return The entities this list contains.
	 */
	protected abstract List<RestEntity> getEntities();
	
	
	// IMPLEMENTED METHODS	--------------------

	@Override
	protected RestEntity getMissingEntity(String pathPart,
			Map<String, String> parameters) throws HttpException
	{
		// Trims the list if necessary
		trimIfNecessary(parameters);
		
		// If possible, returns a new list that contains the entities collected from the 
		// entities in the current list
		List<RestEntity> found = new ArrayList<>();
		
		for (RestEntity entity : getEntities())
		{
			try
			{
				found.add(entity.getEntity(pathPart, parameters));
			}
			catch (NotFoundException e)
			{
				// If the entity can't be found, it may still be found from other entities
			}
		}
		
		// Checks if any entities were found
		if (found.isEmpty())
			throw new NotFoundException(getPath() + "/" + pathPart);
		
		return wrapIntoList(pathPart, this, found);
	}
	
	@Override
	protected void prepareDelete(Map<String, String> parameters)
			throws HttpException
	{
		trimIfNecessary(parameters);
		
		// By default, Deletes all the entities in the list
		int successes = 0;
		for (RestEntity entity : getEntities())
		{
			try
			{
				entity.delete(parameters);
				successes ++;
			}
			catch (MethodNotSupportedException e)
			{
				// Unless none of the entities support DELETE, it is still done for some
			}
		}
		
		if (!getEntities().isEmpty() && successes == 0)
			throw new MethodNotSupportedException(MethodType.DELETE);
	}
	
	@Override
	protected Map<String, RestEntity> getMissingEntities(Map<String, String> parameters)
	{
		// The lists give all their entities in the getMissingEntity -method
		return new HashMap<>();
	}
	
	@Override
	public void writeContent(String serverLink, XMLStreamWriter writer, 
			Map<String, String> parameters) throws XMLStreamException, HttpException
	{
		trimIfNecessary(parameters);
		
		// Writes the content of each entity in row
		for (RestEntity entity : getEntities())
		{
			entity.writeContent(serverLink, writer, parameters);
		}
	}
	
	
	// OTHER METHODS	------------------------
	
	/**
	 * Adds a new entity to the list
	 * @param entity The entity that will be added to the list
	 */
	public void addEntity(RestEntity entity)
	{
		if (entity != null && !getEntities().contains(entity))
			getEntities().add(entity);
	}
	
	/**
	 * Removes an entity from the list
	 * @param entity The entity that will be removed from the list
	 */
	public void remove(RestEntity entity)
	{
		getEntities().remove(entity);
	}
	
	/**
	 * Drops a certain amount of entities from the beginning of the list
	 * @param amount
	 */
	private void dropFirst(int amount)
	{
		int dropped = 0;
		while (dropped < amount && !getEntities().isEmpty())
		{
			getEntities().remove(0);
			dropped ++;
		}
	}
	
	/**
	 * Removes the entities from the end of the list until it fits the given size
	 * @param size How many entities the list should hold in the end (at maximum)
	 */
	private void fitToSize(int size)
	{
		if (size <= 0)
		{
			getEntities().clear();
			return;
		}
		
		while(getEntities().size() > size)
		{
			getEntities().remove(getEntities().size() - 1);
		}
	}
	
	/**
	 * Updates the size of the list to match the user's desires. The method supports 
	 * parameters 'from' and 'amount'. The list should be sorted before calling this method.
	 * @param parameters The parameters that define the new size of the list
	 * @throws InvalidParametersException If the parameters couldn't be parsed
	 */
	private void adjustSizeWithParameters(Map<String, String> parameters) throws 
			InvalidParametersException
	{
		int from = 0;
		int amount = getEntities().size();
		
		try
		{
			if (parameters.containsKey("from"))
				from = Integer.parseInt(parameters.get("from"));
			if (parameters.containsKey("amount"))
				amount = Integer.parseInt(parameters.get("amount"));
		}
		catch (NumberFormatException e)
		{
			throw new InvalidParametersException("Could not parse the given parameters");
		}
		
		dropFirst(from);
		fitToSize(amount);
	}
	
	private void trimIfNecessary(Map<String, String> parameters) throws 
			InvalidParametersException
	{
		if (!this.trimmed)
		{
			trim(parameters);
			adjustSizeWithParameters(parameters);
			this.trimmed = true;
		}
	}
}
