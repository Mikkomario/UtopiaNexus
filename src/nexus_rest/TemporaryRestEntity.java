package nexus_rest;

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
	
	private String path;
	
	
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
			this.path = parent.getPath() + "/" + getName();
		else
			this.path = getName();
	}
	
	
	// IMPLEMENTED METHODS	-------------------
	
	@Override
	public String getPath()
	{
		return this.path;
	}
}
