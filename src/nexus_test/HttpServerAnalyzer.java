package nexus_test;

import genesis_event.EventSelector;
import genesis_event.HandlerRelay;
import genesis_util.SimpleHandled;
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
public class HttpServerAnalyzer extends SimpleHandled implements HttpEventListener
{
	// ATTRIBUTES	-------------------------
	
	private long requestReceivedMillis;
	private Request lastRequest;
	private EventSelector<HttpEvent> eventSelector;
	
	
	// CONSTRUCTOR	------------------------
	
	/**
	 * Creates a new httpServerAnalyzer
	 * @param handler The handler that will inform the analyzer about http events
	 */
	public HttpServerAnalyzer(HttpEventListenerHandler handler)
	{
		super(null);
		initialize();
		handler.add(this);
	}
	
	/**
	 * Creates a new httpServerAnalyzer
	 * @param handlers The handlers that will hanldle this analyzer
	 */
	public HttpServerAnalyzer(HandlerRelay handlers)
	{
		super(handlers);
		initialize();
	}
	
	/**
	 * Creates a new server analyzer. Remember to add the analyzer to a 
	 * httpEventListenerHandler
	 */
	public HttpServerAnalyzer()
	{
		super(null);
		initialize();
	}
	
	
	// IMPLEMENTED METHODS	--------------------------

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
			System.out.println("Response content: " + e.getResponse().getContent());
			System.out.println("Operation time: " + operationMillis + " ms");
		}
	}

	
	// OTHER METHODS	-------------------------
	
	private void initialize()
	{
		this.requestReceivedMillis = 0;
		this.lastRequest = null;
		this.eventSelector = HttpEvent.getServerEventSelector();
	}
}
