package nexus_event;

import genesis_event.EventSelector;
import genesis_event.Handler;
import genesis_event.HandlerRelay;
import genesis_event.HandlerType;
import genesis_event.StrictEventSelector;

/**
 * These handlers inform multiple HttpEventListeners about http events
 * @author Mikko Hilpinen
 * @since 2.5.2015
 */
public class HttpEventListenerHandler extends Handler<HttpEventListener> implements
		HttpEventListener
{
	// ATTRIBUTES	----------------------------

	private EventSelector<HttpEvent> eventSelector;
	private HttpEvent lastEvent;
	
	
	// CONSTRUCTOR	----------------------------
	
	/**
	 * Creates a new handler
	 * @param autoDeath Will the handler die once it runs out of handleds
	 * @param superHandlers The handlers that will handle this handler
	 */
	public HttpEventListenerHandler(boolean autoDeath, HandlerRelay superHandlers)
	{
		super(autoDeath, superHandlers);

		initialize();
	}
	
	/**
	 * Creates a new handler. The handler must be informed about http events manually.
	 * @param autoDeath Will the handler die once it runs out of handleds
	 */
	public HttpEventListenerHandler(boolean autoDeath)
	{
		super(autoDeath);
		
		initialize();
	}
	
	/**
	 * Creates a new handler.
	 * @param autoDeath Will the handler die once it runs out of handleds
	 * @param superHandler The handler that will inform the handler about http events
	 */
	public HttpEventListenerHandler(boolean autoDeath, HttpEventListenerHandler superHandler)
	{
		super(autoDeath);
		
		initialize();
		
		if (superHandler != null)
			superHandler.add(this);
	}
	
	
	// IMPLEMENTED METHODS	----------------------

	@Override
	public EventSelector<HttpEvent> getHttpEventSelector()
	{
		return this.eventSelector;
	}

	@Override
	public void onHttpEvent(HttpEvent e)
	{
		// Informs the listeners that are interested
		this.lastEvent = e;
		handleObjects(true);
	}

	@Override
	public HandlerType getHandlerType()
	{
		return NexusHandlerType.HTTPEVENTHANDLER;
	}

	@Override
	protected boolean handleObject(HttpEventListener l)
	{
		// Informs only objects that are interested
		if (l.getHttpEventSelector().selects(this.lastEvent))
			l.onHttpEvent(this.lastEvent);
		
		return true;
	}
	
	
	// OTHER METHODS	-----------------------
	
	private void initialize()
	{
		this.eventSelector = new StrictEventSelector<>();
	}
}
