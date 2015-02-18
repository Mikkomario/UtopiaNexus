package nexus_rest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

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
	private boolean useEncoding;
	
	
	// CONSTRUCTOR	--------------------------------
	
	/**
	 * Creates a new manager
	 * @param root The root entity
	 * @param serverLink The server part of the link, containing the server address, the port 
	 * number and the first "/"
	 * @param useEncoding Should the manager expect to receive encoded requests. 
	 * The used encoding is UTF-8.
	 */
	public RestManager(RestEntity root, String serverLink, boolean useEncoding)
	{
		this.root = root;
		this.serverLink = serverLink;
		this.useEncoding = useEncoding;
	}
	
	
	// IMPLEMENTED METHODS	----------------------

	@Override
	public void handle(HttpRequest request, HttpResponse response, HttpContext context)
			throws org.apache.http.HttpException, IOException
	{
		Request parsedRequest = new Request(request, this.useEncoding);
		
		ByteArrayOutputStream xml = null;
		XMLStreamWriter writer = null;
		boolean writerOpen = false;
		
		// Finds the requested entity
		try
		{	
			RestEntity requested = this.root.getEntity(parsedRequest.getPath(), 1, 
					parsedRequest.getParameters());
			
			switch (parsedRequest.getMethod())
			{
				// For GET, parses the entity and sends the data
				case GET:
					xml = new ByteArrayOutputStream();
					writer = XMLIOAccessor.createWriter(xml);
					writerOpen = true;
					XMLIOAccessor.writeDocumentStart("result", writer);
					
					requested.writeContent(this.serverLink, writer, 
							parsedRequest.getParameters());
					
					XMLIOAccessor.writeDocumentEnd(writer);
					XMLIOAccessor.closeWriter(writer);
					writerOpen = false;
					response.setEntity(new StringEntity(xml.toString(), ContentType.TEXT_XML));
					
					break;
				// For POST, posts a new entity, returns a link to the new entity
				case POST:
					// TODO: WETWET
					RestEntity newEntity = requested.Post(parsedRequest.getParameters());
					
					xml = new ByteArrayOutputStream();
					writer = XMLIOAccessor.createWriter(xml);
					writerOpen = true;
					XMLIOAccessor.writeDocumentStart("result", writer);
					
					writer.writeStartElement(newEntity.getName());
					newEntity.writeLinkAsAttribute(this.serverLink, writer, 
							parsedRequest.getParameters());
					writer.writeEndElement();
					
					XMLIOAccessor.writeDocumentEnd(writer);
					XMLIOAccessor.closeWriter(writer);
					writerOpen = false;
					response.setEntity(new StringEntity(xml.toString(), ContentType.TEXT_XML));
					
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
				// TODO: HEAD not working for some reason... (doesn't reach the manager?)
				// For HEAD, doesn't parse the entity but sends an OK status instead
				default: break;
			}
			
			response.setStatusCode(HttpStatus.SC_OK);
		}
		catch(HttpException e)
		{
			// TODO: On invalid parameter exception, the content is cut short
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
		finally
		{
			if (writerOpen)
				XMLIOAccessor.closeWriter(writer);
		}
	}

	@Override
	public String getAcceptedPath()
	{
		if (!this.useEncoding)
			return "/" + this.root + "/*";
		else
			return encodeIfNecessary("/" + this.root.getName()) + "*";
	}
	
	
	// OTHER METHODS	-------------------------
	
	/**
	 * @return The path that is used when requesting the root entity itself
	 */
	public String getAdditionalAcceptedPath()
	{
		return encodeIfNecessary("/" + this.root.getName());
	}
	
	private String encodeIfNecessary(String s)
	{
		if (this.useEncoding)
		{
			try
			{
				return URLEncoder.encode(s, "UTF-8");
			}
			catch (UnsupportedEncodingException e)
			{
				System.err.println("Failed to encode the path");
				e.printStackTrace();
			}
		}
		
		return s;
	}
}
