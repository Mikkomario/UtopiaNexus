package nexus_rest;

import java.io.IOException;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;

import nexus_http.HttpException;
import nexus_http.Request;
import nexus_http.RequestHandler;

/**
 * RestManager keeps track of restEntities and handles incoming requests
 * 
 * @author Mikko Hilpinen
 * @since 19.1.2015
 */
public class RestManager implements RequestHandler
{
	// ATTRIBUTES	--------------------------------
	
	private RestEntity root;
	
	
	// CONSTRUCTOR	--------------------------------
	
	/**
	 * Creates a new manager
	 * @param root The root entity
	 */
	public RestManager(RestEntity root)
	{
		this.root = root;
	}
	
	
	// IMPLEMENTED METHODS	----------------------

	@Override
	public void handle(HttpRequest request, HttpResponse response, HttpContext context)
			throws org.apache.http.HttpException, IOException
	{
		Request parsedRequest = new Request(request);
		
		// Finds the requested entity
		try
		{
			RestEntity requested = this.root.getEntity(parsedRequest.getPath());
			
			switch (parsedRequest.getMethod())
			{
				// For GET, parses the entity and sends the data
				// For POST, posts a new entity, returns a link to the new entity
				case POST:
					requested.Post(parsedRequest.getParameters());
					break;
				// For DELETE, deletes the entity, returns a link to the entity above that
				case DELETE:
					requested.delete(parsedRequest.getParameters());
					break;
				// For HEAD, doesn't parse the entity but sends an OK status instead
				default: break;
			}
			
			response.setStatusCode(HttpStatus.SC_OK);
		}
		catch(HttpException e)
		{
			response.setStatusCode(e.getStatusCode());
			response.setEntity(new StringEntity(e.getMessage(), ContentType.TEXT_PLAIN));
		}
	}

	@Override
	public String getAcceptedPath()
	{
		return "/" + this.root.getName() + "/*";
	}
}
