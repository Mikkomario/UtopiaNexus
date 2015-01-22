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
import flow_recording.XMLObjectWriter;
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
			RestEntity requested = this.root.getEntity(parsedRequest.getPath(), 1, 
					parsedRequest.getParameters());
			
			//System.out.println("Target entity: " + requested.getPath());
			
			switch (parsedRequest.getMethod())
			{
				// For GET, parses the entity and sends the data
				case GET:
					// TODO: The entities may need to be parsed in a different manner but 
					// for now we'll use the default xml writer
					XMLObjectWriter objectWriter = new XMLObjectWriter();
					ByteArrayOutputStream xml = new ByteArrayOutputStream();
					XMLStreamWriter writer = XMLIOAccessor.createWriter(xml);
					objectWriter.openDocument("result", writer);
					objectWriter.writeInto(requested, writer);
					objectWriter.closeDocument(writer);
					XMLIOAccessor.closeWriter(writer);
					
					response.setEntity(new StringEntity(xml.toString(), ContentType.TEXT_XML));
					break;
				// For POST, posts a new entity, returns a link to the new entity
				case POST:
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
		return "/*";
		// TODO: Doesn't accept root but only elements under it
		//return "/" + this.root.getName() + "/*";
	}
}
