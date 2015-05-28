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
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;

import tempest_io.JsonIOAccessor;

import com.fasterxml.jackson.core.JsonGenerator;

import flow_io.XMLIOAccessor;
import nexus_event.HttpEvent;
import nexus_event.HttpEvent.HttpEventType;
import nexus_event.HttpEventListenerHandler;
import nexus_http.HttpException;
import nexus_http.InternalServerException;
import nexus_http.Request;
import nexus_http.RequestHandler;
import nexus_http.ResponseReplicate;

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
	private HttpEventListenerHandler listenerHandler;
	private ContentType defaultContentType;
	
	
	// CONSTRUCTOR	--------------------------------
	
	/**
	 * Creates a new manager
	 * @param root The root entity
	 * @param serverLink The server part of the link, containing the server address, the port 
	 * number and the first "/"
	 * @param useEncoding Should the manager expect to receive encoded requests. 
	 * The used encoding is UTF-8.
	 * @param defaultContentType The used content type in the case where the client doesn't 
	 * specify their wish
	 */
	public RestManager(RestEntity root, String serverLink, boolean useEncoding, 
			ContentType defaultContentType)
	{
		this.root = root;
		this.serverLink = serverLink;
		this.useEncoding = useEncoding;
		this.listenerHandler = new HttpEventListenerHandler(false);
		this.defaultContentType = defaultContentType;
	}
	
	
	// IMPLEMENTED METHODS	----------------------

	@Override
	public void handle(HttpRequest request, HttpResponse response, HttpContext context)
			throws org.apache.http.HttpException, IOException
	{
		Request parsedRequest = new Request(request, this.useEncoding);
		
		this.listenerHandler.onHttpEvent(new HttpEvent(new Request(parsedRequest), 
				HttpEventType.RECEIVED));
		
		ByteArrayOutputStream output = null;
		XMLStreamWriter xmlWriter = null;
		JsonGenerator jsonWriter = null;
		boolean writerOpen = false;
		
		// TODO: Change this to use headers instead
		ContentType contentType = ContentType.parseFromString(
				parsedRequest.getParameters().get("contentType"));
		if (contentType == null)
			contentType = this.defaultContentType;
		
		// Finds the requested entity
		try
		{	
			RestEntity requested = this.root.getEntity(parsedRequest.getPath(), 1, 
					parsedRequest.getParameters());
			
			switch (parsedRequest.getMethod())
			{
				// For GET, parses the entity and sends the data
				case GET:
					output = new ByteArrayOutputStream();
					if (contentType == ContentType.XML)
						xmlWriter = XMLIOAccessor.createWriter(output);
					else
						jsonWriter = JsonIOAccessor.createWriter(output);
					writerOpen = true;
					
					writeDocumentStart(xmlWriter, jsonWriter, contentType);
					requested.writeContent(this.serverLink, xmlWriter, jsonWriter, contentType, 
							parsedRequest.getParameters());
					
					writeDocumentEnd(xmlWriter, jsonWriter, contentType);
					
					XMLIOAccessor.closeWriter(xmlWriter);
					JsonIOAccessor.closeWriter(jsonWriter);
					writerOpen = false;
					
					response.setEntity(new StringEntity(output.toString(), 
							contentType.getApacheContentType()));
					
					break;
				// For POST, posts a new entity, returns a link to the new entity
				case POST:
					// TODO: WETWET
					RestEntity newEntity = requested.Post(parsedRequest.getParameters());
					
					output = new ByteArrayOutputStream();
					if (contentType == ContentType.XML)
						xmlWriter = XMLIOAccessor.createWriter(output);
					else
						jsonWriter = JsonIOAccessor.createWriter(output);
					writerOpen = true;
					
					writeDocumentStart(xmlWriter, jsonWriter, contentType);
					if (contentType == ContentType.XML)
						RestEntity.writeEntityLink(newEntity.getValidXmlName(), newEntity, 
								this.serverLink, xmlWriter, parsedRequest.getParameters());
					else
						RestEntity.writeEntityLink(newEntity.getName(), newEntity, 
								this.serverLink, jsonWriter);
					writeDocumentEnd(xmlWriter, jsonWriter, contentType);
					
					XMLIOAccessor.closeWriter(xmlWriter);
					JsonIOAccessor.closeWriter(jsonWriter);
					writerOpen = false;
					
					response.setEntity(new StringEntity(output.toString(), 
							contentType.getApacheContentType()));
					
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
			response.setEntity(new StringEntity(e.getMessage(), 
					org.apache.http.entity.ContentType.TEXT_PLAIN));
			
			// For internal server errors, makes an error print as well
			// TODO: Remove this and let an analyzer take care of the job
			if (e instanceof InternalServerException)
			{
				System.err.println("Internal server error: " + e.getMessage());
				e.printStackTrace();
				System.err.println("Caused by request: " + parsedRequest);
			}
		}
		catch(XMLStreamException | IOException e)
		{
			response.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			e.printStackTrace();
		}
		finally
		{
			if (writerOpen)
			{
				XMLIOAccessor.closeWriter(xmlWriter);
				JsonIOAccessor.closeWriter(jsonWriter);
			}
			
			getHttpListenerHandler().onHttpEvent(new HttpEvent(
					new ResponseReplicate(response), HttpEventType.SENT));
		}
	}

	@Override
	public String getAcceptedPath()
	{		
		if (!this.useEncoding)
			return "/" + this.root + "/*";
		else
			return "/" + encodeIfNecessary(this.root.getName()) + "*";
	}
	
	
	// OTHER METHODS	-------------------------
	
	/**
	 * @return The path that is used when requesting the root entity itself
	 */
	public String getAdditionalAcceptedPath()
	{
		return "/" + encodeIfNecessary(this.root.getName());
	}
	
	/**
	 * @return The listenerHandler that handles all http event listeners informed by this 
	 * manager
	 */
	public HttpEventListenerHandler getHttpListenerHandler()
	{
		return this.listenerHandler;
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
	
	private static void writeDocumentStart(XMLStreamWriter xmlWriter, 
			JsonGenerator jsonWriter, ContentType contentType) throws XMLStreamException, 
			IOException
	{
		if (contentType == ContentType.XML)
		{
			XMLIOAccessor.writeDocumentStart("result", xmlWriter);
			XMLIOAccessor.writeXLinkNamespaceIntroduction(xmlWriter);
		}
		else
			jsonWriter.writeStartObject();
	}
	
	private static void writeDocumentEnd(XMLStreamWriter xmlWriter, 
			JsonGenerator jsonWriter, ContentType contentType) throws XMLStreamException, 
			IOException
	{
		if (contentType == ContentType.XML)
		{
			//xmlWriter.writeEndElement();
			XMLIOAccessor.writeDocumentEnd(xmlWriter);
		}
		else
			jsonWriter.writeEndObject();
	}
}
