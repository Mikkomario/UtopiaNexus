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
public class RestEntity extends TreeNode<RestData> implements Constructable<RestEntity>, Writable
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
	
	public RestEntity get(String pathPart)
	{
		if (this.links.containsKey(pathPart))
			return getLinkedEntity(pathPart);
		
		for (int i = 0; i < getChildAmount(); i++)
		{
			RestEntity child = (RestEntity) getChild(i);
			if (child.getName().equals(pathPart))
				return child;
		}
		
		// TODO: Throw a not found exception
		return null;
	}
}
