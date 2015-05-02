package nexus_event;

import genesis_event.HandlerType;

/**
 * This is a collection of different handler types introduced in the nexus module
 * @author Mikko Hilpinen
 * @since 2.5.2015
 */
public enum NexusHandlerType implements HandlerType
{
	/**
	 * HttpEventHandlers inform objects about http events
	 * @see HttpEventListenerHandler
	 */
	HTTPEVENTHANDLER;
	
	
	// IMPLEMENTED METHODS	-------------------------

	@Override
	public Class<?> getSupportedHandledClass()
	{
		return HttpEventListener.class;
	}
}
