package nexus_test;

import genesis_event.EventSelector;
import genesis_event.HandlerRelay;
import genesis_util.LatchStateOperator;
import genesis_util.StateOperator;
import nexus_event.HttpEvent;
import nexus_event.HttpEvent.HttpEventSourceType;
import nexus_event.HttpEventListener;
import nexus_event.HttpEventListenerHandler;
import nexus_http.Request;

/**
 * HttpServerAnalyzer prints data about server events
 * @author Mikko Hilpinen
 * @since 2.5.2015
 */
public class HttpServerAnalyzer implements HttpEventListener
{
	// ATTRIBUTES	-------------------------
	
	private long requestReceivedMillis;
	private Request lastRequest;
	private StateOperator isDeadOperator, listensOperator;
	private EventSelector<HttpEvent> eventSelector;
	
	
	// CONSTRUCTOR	------------------------
	
	/**
	 * Creates a new httpServerAnalyzer
	 * @param handler The handler that will inform the analyzer about http events
	 */
	public HttpServerAnalyzer(HttpEventListenerHandler handler)
	{
		initialize();
		handler.add(this);
	}
	
	/**
	 * Creates a new httpServerAnalyzer
	 * @param handlers The handlers that will hanldle this analyzer
	 */
	public HttpServerAnalyzer(HandlerRelay handlers)
	{
		initialize();
		handlers.addHandled(this);
	}
	
	/**
	 * Creates a new server analyzer. Remember to add the analyzer to a 
	 * httpEventListenerHandler
	 */
	public HttpServerAnalyzer()
	{
		initialize();
	}
	
	
	// IMPLEMENTED METHODS	--------------------------

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
			this.lastRequest = e.getRequest();
			this.requestReceivedMillis = System.currentTimeMillis();
		}
		else
		{
			long operationMillis = System.currentTimeMillis() - this.requestReceivedMillis;
			System.out.println("---------------------------");
			System.out.println("Request: " + this.lastRequest.toString());
			System.out.println("Response status: " + e.getResponse().getStatusCode());
			System.out.println("Response content length: " + 
					e.getResponse().getContent().length());
			System.out.println("Operation time: " + operationMillis + " ms");
		}
	}

	
	// OTHER METHODS	-------------------------
	
	private void initialize()
	{
		this.requestReceivedMillis = 0;
		this.lastRequest = null;
		this.isDeadOperator = new LatchStateOperator(false);
		this.listensOperator = new StateOperator(true, true);
		this.eventSelector = HttpEvent.getServerEventSelector();
	}
}
