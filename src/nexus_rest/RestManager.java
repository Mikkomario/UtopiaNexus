package nexus_rest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;

import flow_io.XMLIOAccessor;
import nexus_http.HttpException;
import nexus_http.InternalServerException;
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
	private String serverLink;
	
	
	// CONSTRUCTOR	--------------------------------
	
	/**
	 * Creates a new manager
	 * @param root The root entity
	 * @param serverLink The server part of the link, containing the server address, the port 
	 * number and the first "/"
	 */
	public RestManager(RestEntity root, String serverLink)
	{
		this.root = root;
		this.serverLink = serverLink;
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
			RestEntity requested = this.root.getEntity(parsedRequest.getPath(), 1, 
					parsedRequest.getParameters());
			
			switch (parsedRequest.getMethod())
			{
				// For GET, parses the entity and sends the data
				case GET:
					ByteArrayOutputStream xml = new ByteArrayOutputStream();
					XMLStreamWriter writer = XMLIOAccessor.createWriter(xml);
					XMLIOAccessor.writeDocumentStart("result", writer);
					
					requested.writeContent(this.serverLink, writer);
					
					XMLIOAccessor.writeDocumentEnd(writer);
					XMLIOAccessor.closeWriter(writer);
					
					response.setEntity(new StringEntity(xml.toString(), ContentType.TEXT_XML));
					break;
				// For POST, posts a new entity, returns a link to the new entity
				case POST:
					// TODO: Return a link to the entity.
					requested.Post(parsedRequest.getParameters());
					break;
				// For PUT, changes an attribute in the entity, returns a link to the 
				// modified entity
				case PUT:
					requested.Put(parsedRequest.getParameters());
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
			
			// For internal server errors, makes an error print as well
			if (e instanceof InternalServerException)
			{
				System.err.println("Internal server error: " + e.getMessage());
				e.printStackTrace();
				System.err.println("Caused by request: " + parsedRequest);
			}
		}
		catch(XMLStreamException e)
		{
			response.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			e.printStackTrace();
		}
	}

	@Override
	public String getAcceptedPath()
	{
		//return "/*";
		// TODO: Doesn't accept root but only elements under it
		return "/" + this.root.getName() + "/*";
		// This must be done in a separate registration. Add support!
	}
	
	
	// OTHER METHODS	-------------------------
	
	/**
	 * @return The path that is used when requesting the root entity itself
	 */
	public String getAdditionalAcceptedPath()
	{
		return "/" + this.root.getName();
	}
}
