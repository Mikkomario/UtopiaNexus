package nexus_rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.fasterxml.jackson.core.JsonGenerator;

import nexus_http.HttpException;
import flow_io.XMLIOAccessor;
import flow_recording.Constructable;
import flow_recording.Writable;
import flow_structure.TreeNode;

/**
 * This class represents a resource used in a REST environment. The resources are in a 
 * tree-like structure and may contain links to another resources.
 * 
 * @author Mikko Hilpinen
 * @since 31.12.2014
 */
public abstract class RestEntity extends TreeNode<RestData> implements 
		Constructable<RestEntity>, Writable
{
	// ATTRIBUTES	--------------------------------

	private String name, id;
	private Map<String, RestEntity> links;
	
	private static final String CHILDINDICATOR = "child";
	
	
	// CONSTRUCTOR	--------------------------------
	
	/**
	 * Creates a new entity
	 * @param name The name of the entity
	 * @param content The content within the entity
	 * @param parent The parent entity
	 */
	public RestEntity(String name, RestData content, RestEntity parent)
	{
		super(content, parent);
		
		this.name = name;
		this.links = new HashMap<>();
		this.id = this.name;
	}
	
	
	// ABSTRACT METHODS	---------------------------
	
	/**
	 * Creates a new restEntity under this entity using the given construction data.
	 * @param parameters The parameters that are used in entity construction.
	 * @return The entity that was just created
	 * @throws HttpException If the operation couldn't succeed due to an error in the request
	 */
	public abstract RestEntity Post(Map<String, String> parameters) throws HttpException;
	
	/**
	 * Changes some attributes in the entity
	 * @param parameters The parameters that are to be adjusted
	 * @throws HttpException If the operation couldn't succeed due to an error in the request
	 */
	public abstract void Put(Map<String, String> parameters) throws HttpException;
	
	/**
	 * Makes the necessary changes before the entity is destroyed. The entity won't be 
	 * destroyed if an exception is thrown.
	 * @param parameters The parameters that are used in destroying the entity.
	 * @throws HttpException If the operation couldn't succeed due to an error in the request
	 */
	protected abstract void prepareDelete(Map<String, String> parameters) throws HttpException;
	
	/**
	 * Finds an entity that is not a child, a link or an attribute of this entity, if possible.
	 * @param pathPart The name of the entity that should be reached
	 * @param parameters The parameters provided by the client
	 * @return The entity along the given path
	 * @throws HttpException If the entity couldn't be found
	 */
	protected abstract RestEntity getMissingEntity(String pathPart, 
			Map<String, String> parameters) throws HttpException;
	
	/**
	 * This method should fetch all of the entities that are under this entity but not as 
	 * links or children.
	 * @param parameters The parameters provided by the client
	 * @return A Map containing all the restEntities under this entity but not those 
	 * already registered as links or children. The entities are mapped with their names so 
	 * that they can substitute links or children alike. Null can be returned, in case there 
	 * are no missing entities.
	 * @throws HttpException If the missing entities can't be reached for some reason
	 */
	protected abstract Map<String, RestEntity> getMissingEntities(Map<String, String> parameters) 
			throws HttpException;
	
	
	// IMPLEMENTED METHODS	------------------------

	@Override
	public String getID()
	{
		return this.id;
	}

	@Override
	public void setAttribute(String attributeName, String attributeValue)
	{
		getContent().setAttribute(attributeName, attributeValue);
	}

	@Override
	public void setID(String id)
	{
		this.id = id;
	}

	@Override
	public void setLink(String linkName, RestEntity target)
	{
		// This may also be used for adding a child entity, in which case the name starts 
		// with "child"
		if (linkName.startsWith(CHILDINDICATOR))
			addChild(target);
		else
			this.links.put(linkName, target);
	}

	@Override
	public Map<String, String> getAttributes()
	{
		return getContent().getAttributes();
	}

	@Override
	public Map<String, Writable> getLinks()
	{
		Map<String, Writable> links = new HashMap<>();
		
		for (String linkName : getlinkNames())
		{
			links.put(linkName, getLinkedEntity(linkName));
		}
		
		for (RestEntity child : getChildren())
		{
			links.put(CHILDINDICATOR + child.getName(), child);
		}
		
		return links;
	}
	
	
	// GETTERS & SETTERS	----------------------------------
	
	/**
	 * @return The name of the entity
	 */
	public String getName()
	{
		return this.name;
	}
	
	
	// OTHER METHODS	----------------------------------
	
	/**
	 * @return The entity's name that can be presented in the xml
	 */
	public String getValidXmlName()
	{
		return getValidXmlElementName(getName());
	}
	
	/**
	 * This method wraps the entities into an entityList. The subclasses may override this 
	 * method if they wish to use a specific type of entityList
	 * @param name The name of the list
	 * @param parent The parent of the list
	 * @param entities The entities should fill the list
	 * @return A list with the given data
	 */
	protected RestEntityList wrapIntoList(String name, RestEntity parent, 
			List<RestEntity> entities)
	{
		return new SimpleRestEntityList(name, parent, entities);
	}
	
	/**
	 * Deletes the entity if that's at all possible
	 * @param parameters The parameters used when deleting the entity
	 * @throws HttpException If the operation couldn't succeed due to an error in the request
	 */
	public void delete(Map<String, String> parameters) throws HttpException
	{
		prepareDelete(parameters);
		
		// Deletes all the children as well
		for (RestEntity child : getChildren())
		{
			child.delete(parameters);
		}
		
		setParent(null);
	}
	
	/**
	 * @return The names of the links this entity has
	 */
	public Set<String> getlinkNames()
	{
		return this.links.keySet();
	}
	
	/**
	 * Finds another entity in the other end of the link
	 * @param linkName The name of the link connecting the entities
	 * @return The entity at the other end of the link
	 */
	public RestEntity getLinkedEntity(String linkName)
	{
		return this.links.get(linkName);
	}
	
	/**
	 * @return All entities this one links to (not including the children entities)
	 */
	public List<RestEntity> getLinkedEntities()
	{
		List<RestEntity> targets = new ArrayList<>();
		
		for (String linkName : getlinkNames())
		{
			RestEntity target = getLinkedEntity(linkName);
			if (!targets.contains(target))
				targets.add(target);
		}
		
		return targets;
	}
	
	/**
	 * @return The virtual path that leads to this resource (doesn't include the first '/')
	 */
	public String getPath()
	{
		// TODO: Make a separate directory separator attribute somewhere
		if (getParent() == null)
			return getName();
		else
			return ((RestEntity) getParent()).getPath() + "/" + getName();
	}
	
	/**
	 * Finds an entity in relation to this one
	 * @param pathPart The name of the entity or the link to it
	 * @param parameters The parameters provided by the client
	 * @return The entity along the path
	 * @throws HttpException If the requested path couldn't be found or another problem occurred
	 */
	public RestEntity getEntity(String pathPart, Map<String, String> parameters) 
			throws HttpException
	{
		// All children and links could be requested with "*"
		if (pathPart.equals("*"))
			return getAllEntities(parameters);
		
		// The entity may be a direct link
		if (this.links.containsKey(pathPart))
			return getLinkedEntity(pathPart);
		
		// Or a child entity
		for (RestEntity child : getChildren())
		{
			if (child.getName().equals(pathPart))
				return child;
		}
		
		// Or a single attribute
		if (getContent().getAttributes().containsKey(pathPart))
			return new RestAttributeWrapper(pathPart, this);

		return getMissingEntity(pathPart, parameters);
	}
	
	/**
	 * Finds a resource entity at the end of the given path
	 * @param path The path to the final resource
	 * @param parameters The parameters provided by the client
	 * @return The resource at the end of the path
	 * @throws HttpException If the requested entity couldn't be found
	 */
	public RestEntity getEntity(String[] path, Map<String, String> parameters) 
			throws HttpException
	{
		return getEntity(path, 0, parameters);
	}
	
	/**
	 * Finds a resource entity at the end of the given path
	 * @param path The path to the final resource
	 * @param nextIndex The index of the pathPart that comes after this entity 
	 * (0 if the entity is not on the path)
	 * @param parameters The parameters provided by the client
	 * @return The resource at the end of the path
	 * @throws HttpException If the requested entity couldn't be found
	 */
	public RestEntity getEntity(String[] path, int nextIndex, Map<String, String> parameters) 
			throws HttpException
	{
		if (nextIndex >= path.length)
			return this;
		else
			return getEntity(path[nextIndex], parameters).getEntity(path, nextIndex + 1, 
					parameters);
	}
	
	/**
	 * Writes the entity content as xml data
	 * @param serverLink The server part of the link, containing the server address, the port 
	 * number and the first "/"
	 * @param xmlWriter The writer that writes xml (only required if contentType is xml)
	 * @param jsonWriter The writer that writes json (only required if contentType is json)
	 * @param contentType In which form the entity's content should be written
	 * @param parameters The parameters provided by the client. "linkType" parameter affects 
	 * how the links will be written. "noContent=true" makes it so that the entity won't be 
	 * written at all
	 * @throws XMLStreamException If the writing failed
	 * @throws HttpException If there was another problem during the write
	 * @throws IOException If the json data couldn't be written
	 */
	public void writeContent(String serverLink, XMLStreamWriter xmlWriter, 
			JsonGenerator jsonWriter, ContentType contentType, Map<String, String> parameters) 
			throws XMLStreamException, HttpException, IOException
	{
		// If the parameter 'noContent' is present, doesn't write anything
		if (parameters.containsKey("noContent") && 
				Boolean.parseBoolean(parameters.get("noContent")))
			return;
		
		// Writes the entity element
		if (contentType == ContentType.XML)
		{
			xmlWriter.writeStartElement(getValidXmlName());
			writeLinkAsAttribute(serverLink, xmlWriter, parameters);
		}
		else
			jsonWriter.writeObjectFieldStart(getName());
		
		// Writes the links
		for (String link : getlinkNames())
		{
			if (contentType == ContentType.XML)
				writeEntityLink(link, getLinkedEntity(link), serverLink, xmlWriter, 
						parameters);
			else
				writeEntityLink(link, getLinkedEntity(link), serverLink, jsonWriter);
		}
		// Writes the children
		for (RestEntity child : getChildren())
		{
			if (contentType == ContentType.XML)
				writeEntityLink(child.getValidXmlName(), child, serverLink, xmlWriter, 
						parameters);
			else
				writeEntityLink(child.getName(), child, serverLink, jsonWriter);
		}
		// Writes the attributes
		Map<String, String> attributes = getAttributes();
		for (String attributeName : attributes.keySet())
		{
			if (contentType == ContentType.XML)
				XMLIOAccessor.writeElementWithData(attributeName, 
						attributes.get(attributeName), xmlWriter);
			else
				jsonWriter.writeStringField(attributeName, attributes.get(attributeName));
		}
		
		// Writes the missing entities
		Map<String, RestEntity> missingEntities = getMissingEntities(new HashMap<>());
		if (missingEntities != null)
		{
			for (String entityName : missingEntities.keySet())
			{
				if (contentType == ContentType.XML)
					writeEntityLink(getValidXmlElementName(entityName), 
							missingEntities.get(entityName), serverLink, xmlWriter, parameters);
				else
					writeEntityLink(entityName, missingEntities.get(entityName), serverLink, 
							jsonWriter);
			}
		}
		
		if (contentType == ContentType.XML)
			xmlWriter.writeEndElement();
		else
			jsonWriter.writeEndObject();
	}
	
	/**
	 * Writes a link to the entity as an attribute for the currently open element in the stream
	 * @param serverLink The server part of the link, containing the server address, the port 
	 * number and the first "/"
	 * @param xmlWriter The writer that is capable of writing xml data
	 * @param parameters The parameters provided by the client. The parameter 'linkType' 
	 * affects how the links are written. 'simple' means that only the path is written. 
	 * 'none' means that links won't be written at all. Any other value means that full links 
	 * will be written
	 * @throws XMLStreamException If the attribute couldn't be written into the stream
	 */
	public void writeLinkAsAttribute(String serverLink, XMLStreamWriter xmlWriter,  
			Map<String, String> parameters) throws XMLStreamException
	{
		String linkType = parameters.get("linkType");
		String link = null;
		
		if ("simple".equalsIgnoreCase(linkType))
			link = getPath();
		else if (!"none".equalsIgnoreCase(linkType))
			link = serverLink + getPath();
			
		XMLIOAccessor.writeLinkAsAttribute(link, xmlWriter, false);
	}
	
	/**
	 * @return List containing all the children of this entity
	 */
	protected List<RestEntity> getChildren()
	{
		List<RestEntity> children = new ArrayList<>();
		for (int i = 0; i < getChildAmount(); i++)
		{
			children.add((RestEntity) getChild(i));
		}
		return children;
	}
	
	/**
	 * This is the default solution that can be used with Put. It simply updates those 
	 * attributes that have already been introduced. No checking is done for the validity of 
	 * the attributes, that must be done before calling this method.
	 * 
	 * @param parameters The parameters the client has provided
	 */
	protected void defaultPut(Map<String, String> parameters)
	{
		Map<String, String> attributes = getAttributes();
		for (String parameterName : parameters.keySet())
		{
			if (attributes.containsKey(parameterName))
				setAttribute(parameterName, parameters.get(parameterName));
		}
	}
	
	private RestEntityList getAllEntities(Map<String, String> parameters) throws 
			HttpException
	{
		RestEntityList entities = new SimpleRestEntityList("*", this, getLinkedEntities());
		
		for (RestEntity child : getChildren())
		{
			entities.addEntity(child);
		}
		
		Map<String, RestEntity> missingEntities = getMissingEntities(parameters);
		if (missingEntities != null)
		{
			for (RestEntity entity : missingEntities.values())
			{
				entities.addEntity(entity);
			}
		}
		
		return entities;
	}
	
	/**
	 * Writes a link to an entity in xml
	 * @param linkName The name of the link
	 * @param entity The entity being linked to
	 * @param serverLink The server part of the link
	 * @param writer The writer that will write the link
	 * @param parameters The parameters provided by the client
	 * @throws XMLStreamException If the write failed
	 */
	protected static void writeEntityLink(String linkName, RestEntity entity, String serverLink, 
			XMLStreamWriter writer, Map<String, String> parameters) throws XMLStreamException
	{
		writer.writeStartElement(linkName);
		entity.writeLinkAsAttribute(serverLink, writer, parameters);
		writer.writeEndElement();
	}
	
	/**
	 * Writes a link to an entity in json
	 * @param linkName The name of the link
	 * @param entity The entity being linked to
	 * @param serverLink The server part of the link
	 * @param writer The writer that will write the link
	 * @throws IOException If the write failed
	 */
	protected static void writeEntityLink(String linkName, RestEntity entity, String serverLink, 
			JsonGenerator writer) throws IOException
	{
		writer.writeStringField(linkName, serverLink + entity.getPath());
	}
	
	private static String getValidXmlElementName(String elementName)
	{
		if (elementName == null)
			return "null";
		if (Character.isDigit(elementName.charAt(0)))
			return "_" + elementName;
		
		return elementName;
	}
}
