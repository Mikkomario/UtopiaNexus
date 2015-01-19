package nexus_rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	
	// TODO: Throw an exception if POST fails
	/**
	 * Creates a new restEntity under this entity using the given construction data.
	 * @param parameters The parameters that are used in entity construction.
	 * @return The entity that was just created
	 */
	public abstract RestEntity Post(Map<String, String> parameters);
	
	// TODO: Throw exceptions on parse issues or other problems
	/**
	 * Changes some attributes in the entity
	 * @param parameters The parameters that are to be adjusted
	 */
	public abstract void Put(Map<String, String> parameters);
	
	// TODO: Again, throw exceptions
	/**
	 * Makes the necessary changes before the entity is destroyed. The entity won't be 
	 * destroyed if an exception is thrown.
	 * @param parameters The parameters that are used in destroying the entity.
	 */
	protected abstract void prepareDelete(Map<String, String> parameters);
	
	
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
	 * Deletes the entity if that's at all possible
	 * @param parameters The parameters used when deleting the entity
	 */
	public void delete(Map<String, String> parameters)
	{
		// TODO: Catch exceptions
		prepareDelete(parameters);
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
	 * @return The virtual path that leads to this resource
	 */
	public String getPath()
	{
		if (getParent() == null)
			return getName();
		else
			return ((RestEntity) getParent()).getPath() + "/" + getName();
	}
	
	/**
	 * Finds an entity in relation to this one
	 * @param pathPart The name of the entity or the link to it
	 * @return The entity along the path
	 */
	public RestEntity getEntity(String pathPart)
	{
		// The entity may be a direct link
		if (this.links.containsKey(pathPart))
			return getLinkedEntity(pathPart);
		
		// Or a child entity
		for (int i = 0; i < getChildAmount(); i++)
		{
			RestEntity child = (RestEntity) getChild(i);
			if (child.getName().equals(pathPart))
				return child;
		}
		
		// Or a single attribute
		if (getContent().getAttributes().containsKey(pathPart))
			return new RestDataWrapper(pathPart, this);
		
		// TODO: Throw a not found exception
		// TODO: Add support for '*' = "all", could also do one for '-' = "any"-
		return null;
	}
	
	/**
	 * Finds a resource entity at the end of the given path
	 * @param path The path to the final resource
	 * @return The resource at the end of the path
	 */
	public RestEntity getEntity(String[] path)
	{
		return getEntity(path, 0);
	}
	
	private RestEntity getEntity(String[] path, int nextIndex)
	{
		if (nextIndex >= path.length)
			return this;
		else
			return getEntity(path[nextIndex]).getEntity(path, nextIndex + 1);
	}
}
