package nexus_test;

import genesis_event.EventSelector;
import genesis_event.HandlerRelay;
import genesis_util.LatchStateOperator;
import genesis_util.StateOperator;
import nexus_event.HttpEvent;
import nexus_event.HttpEvent.HttpEventSourceType;
import nexus_event.HttpEventListener;
import nexus_event.HttpEventListenerHandler;

/**
 * This class prints data about the send requests and received responses
 * @author Mikko Hilpinen
 * @since 3.5.2015
 */
public class HttpClientAnalyzer implements HttpEventListener
{
	// ATTRIBUTES	---------------------------
	
	private long requestSentMillis;
	private StateOperator isDeadOperator, listensOperator;
	private EventSelector<HttpEvent> eventSelector;
	
	
	// CONSTRUCTOR	---------------------------
	
	/**
	 * Creates a new client analyzer. The analyzer must be added to a handler manually.
	 */
	public HttpClientAnalyzer()
	{
		initialize();
	}
	
	/**
	 * Creates a new client analyzer.
	 * @param handlers The handlers that will handle the analyzer
	 */
	public HttpClientAnalyzer(HandlerRelay handlers)
	{
		initialize();
		
		handlers.addHandled(this);
	}
	
	/**
	 * Creates a new client analyzer
	 * @param handler The handler that will handle the analyzer
	 */
	public HttpClientAnalyzer(HttpEventListenerHandler handler)
	{
		initialize();
		
		handler.add(this);
	}
	
	
	// IMPLEMENTED METHODS	-----------------------

	@Override
	public StateOperator getIsDeadStateOperator()
	{
		return this.isDeadOperator;
	}

	@Override
	public StateOperator getListensToHttpEventsOperator()
	{
		return this.listensOperator;
	}

	@Override
	public EventSelector<HttpEvent> getHttpEventSelector()
	{
		return this.eventSelector;
	}

	@Override
	public void onHttpEvent(HttpEvent e)
	{
		if (e.getSourceType() == HttpEventSourceType.REQUEST)
		{
			this.requestSentMillis = System.currentTimeMillis();
			System.out.println("-------------------");
			System.out.println("Sent request: " + e.getRequest());
		}
		else
		{
			System.out.println("Response status: " + e.getResponse().getStatusCode());
			if (!e.getResponse().getContent().isEmpty())
				System.out.println("Response content: " + e.getResponse().getContent());
			System.out.println("Operation duration: " + 
				(System.currentTimeMillis() - this.requestSentMillis) + " ms");
		}
	}

	
	// OTHER METHODS	------------------------
	
	private void initialize()
	{
		this.requestSentMillis = 0;
		this.isDeadOperator = new LatchStateOperator(false);
		this.listensOperator = new StateOperator(true, true);
		this.eventSelector = HttpEvent.getClientEventSelector();
	}
}
