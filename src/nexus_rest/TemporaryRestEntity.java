package nexus_rest;

import java.util.Map;

import nexus_http.HttpException;
import nexus_http.MethodNotSupportedException;
import nexus_http.MethodType;

/**
 * These entities are never saved into the server but are used as wrappers and temporary 
 * holders.
 * 
 * @author Mikko Hilpinen
 * @since 23.1.2015
 */
public abstract class TemporaryRestEntity extends RestEntity
{
	// ATTRIBUTES	-------------------------
	
	private String rootPath;
	
	
	// CONSTRUCTOR	-------------------------
	
	/**
	 * Creates a new temporary restEntity. The parent won't recognize the entity as a child 
	 * entity, so it wont be remembered automatically.
	 * 
	 * @param name The name of the entity
	 * @param content The content within the entity
	 * @param parent The temporary parent of this entity (only used for determining the 
	 * object's path)
	 */
	public TemporaryRestEntity(String name, RestData content, RestEntity parent)
	{
		super(name, content, null);
		
		if (parent != null)
			this.rootPath = parent.getPath() + "/";
		else
			this.rootPath = "";
	}
	
	/**
	 * Creates a new entity
	 * @param name The name of the entity
	 * @param content The content of the entity
	 * @param rootPath The path preceding this entity's path, including the final '/'
	 */
	public TemporaryRestEntity(String name, RestData content, String rootPath)
	{
		super(name, content, null);
		
		this.rootPath = rootPath;
	}
	
	
	// IMPLEMENTED METHODS	-------------------
	
	@Override
	public String getPath()
	{
		return getRootPath() + getName();
	}
	
	@Override
	public RestEntity Post(Map<String, String> parameters) throws HttpException
	{
		// Temporary entities can't usually create new entities below them since they are gone 
		// after the operation is complete
		throw new MethodNotSupportedException(MethodType.POST);
	}
	
	
	// GETTERS & SETTERS	--------------------
	
	/**
	 * @return The path preceding this entity
	 */
	protected String getRootPath()
	{
		return this.rootPath;
	}
}
