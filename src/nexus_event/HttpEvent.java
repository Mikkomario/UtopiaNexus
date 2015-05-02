package nexus_event;

import java.util.ArrayList;
import java.util.List;

import nexus_http.Request;
import nexus_http.ResponseReplicate;
import genesis_event.Event;

/**
 * HttpEvents are created by activities around Http requests and responses
 * @author Mikko Hilpinen
 * @since 2.5.2015
 */
public class HttpEvent implements Event
{
	// ATTRIBUTES	---------------------------
	
	private HttpEventType eventType;
	private HttpEventSourceType sourceType;
	private Request request;
	private ResponseReplicate response;
	
	
	// CONSTRUCTOR	---------------------------
	
	/**
	 * Creates a new HttpEvent based on the given request
	 * @param request The request that caused the event
	 * @param type The type of event that was caused
	 */
	public HttpEvent(Request request, HttpEventType type)
	{
		this.eventType = type;
		this.sourceType = HttpEventSourceType.REQUEST;
		this.request = request;
		this.response = null;
	}
	
	/**
	 * Creates a new HttpEvent based on the given response
	 * @param response The response that caused the event
	 * @param type The type of event caused by the response
	 */
	public HttpEvent(ResponseReplicate response, HttpEventType type)
	{
		this.eventType = type;
		this.sourceType = HttpEventSourceType.RESPONSE;
		this.request = null;
		this.response = response;
	}
	
	
	// IMPLEMENTED METHODS	------------------

	@Override
	public List<Event.Feature> getFeatures()
	{
		List<Event.Feature> features = new ArrayList<>();
		features.add(this.eventType);
		features.add(this.sourceType);
		return features;
	}
	
	
	// OTHER METHODS	----------------------
	
	/**
	 * @return The request associated with the event (if applicable)
	 */
	public Request getRequest()
	{
		return this.request;
	}
	
	/**
	 * @return The response associated with the event (if applicable)
	 */
	public ResponseReplicate getResponse()
	{
		return this.response;
	}
	
	/**
	 * @return The type of this event
	 */
	public HttpEventType getEventType()
	{
		return this.eventType;
	}
	
	/**
	 * @return The source of this event
	 */
	public HttpEventSourceType getSourceType()
	{
		return this.sourceType;
	}

	
	// INTERFACES	---------------------------
	
	/**
	 * A wrapper for features of HttpEvents
	 * @author Mikko Hilpinen
	 * @since 2.5.2015
	 */
	public static interface Feature extends Event.Feature
	{
		// This interface is used as a wrapper
	}
	
	
	// ENUMERATIONS	---------------------------
	
	/**
	 * Http events can be created by sent or received data
	 * @author Mikko Hilpinen
	 * @since 2.5.2015
	 */
	public static enum HttpEventType implements Feature
	{
		/**
		 * Events with this feature are created when data is sent
		 */
		SENT,
		/**
		 * Events with this feature are created when data is received
		 */
		RECEIVED;
	}
	
	/**
	 * Http events can be caused by either http requests or http responses
	 * @author Mikko Hilpinen
	 * @since 2.5.2015
	 */
	public static enum HttpEventSourceType implements Feature
	{
		/**
		 * Events with this feature are caused by http requests
		 */
		REQUEST,
		/**
		 * Events with this feature are caused by http responses
		 */
		RESPONSE;
	}
}
