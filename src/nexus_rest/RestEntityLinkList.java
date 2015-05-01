package nexus_rest;

import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import nexus_http.HttpException;
import nexus_http.MethodNotSupportedException;
import nexus_http.MethodType;
import nexus_http.NotFoundException;

/**
 * These lists only write links to their linked entities. Those entities can be accessed 
 * through the list, however.
 * @author Mikko Hilpinen
 * @since 1.5.2015
 */
public abstract class RestEntityLinkList extends RestEntityList
{
	// CONSTRUCTOR	-----------------------
	
	/**
	 * Creates a new link list
	 * @param name The name of the list
	 * @param parent The parent entity of this list
	 */
	public RestEntityLinkList(String name, RestEntity parent)
	{
		super(name, parent);
	}
	
	
	// IMPLEMENTED METHODS	--------------
	
	/**
	 * Link lists cannot be modified by the client
	 */
	@Override
	public void Put(Map<String, String> parameters) throws HttpException
	{
		throw new MethodNotSupportedException(MethodType.PUT);
	}
	
	@Override
	protected RestEntity getMissingEntity(String pathPart,
			Map<String, String> parameters) throws HttpException
	{
		// Trims the list if necessary
		trimIfNecessary(parameters);
		
		// If the pathPart is a digit, returns an entity from the given index
		try
		{
			int index = Integer.parseInt(pathPart);
			if (index >= 0 && index < getEntities().size())
				return getEntities().get(index);
		}
		catch (NumberFormatException e)
		{
			// Otherwise expects the path part to be a name of a linked entity
			for (RestEntity entity : getEntities())
			{
				if (entity.getName().equals(pathPart))
					return entity;
			}
		}
		
		throw new NotFoundException(getPath() + "/" + pathPart);
	}
	
	@Override
	protected void prepareDelete(Map<String, String> parameters)
			throws HttpException
	{
		throw new MethodNotSupportedException(MethodType.DELETE);
	}
	
	@Override
	protected Map<String, RestEntity> getMissingEntities(Map<String, String> parameters) 
			throws HttpException
	{
		// Returns all the entities behind the links
		Map<String, RestEntity> entities = new HashMap<>();
		for (RestEntity entity : getEntities())
		{
			entities.put(entity.getName(), entity);
		}
		
		return entities;
	}
	
	@Override
	public void writeContent(String serverLink, XMLStreamWriter writer, 
			Map<String, String> parameters) throws XMLStreamException, HttpException
	{
		trimIfNecessary(parameters);
		
		// Writes a link to each entity in the list
		for (RestEntity entity : getEntities())
		{
			writer.writeStartElement(entity.getName());
			entity.writeLinkAsAttribute(serverLink, writer, parameters);
			writer.writeEndElement();
		}
	}
}
