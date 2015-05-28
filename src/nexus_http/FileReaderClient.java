package nexus_http;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import nexus_event.HttpEventListenerHandler;
import nexus_rest.ContentType;

import org.apache.http.HttpStatus;

import tempest_io.JsonIOAccessor;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import flow_io.AbstractFileReader;
import flow_io.XMLIOAccessor;
import flow_recording.ObjectFormatException;

/**
 * This client performs requests based on a file. The requests should have the following 
 * format:<br>
 * MethodName path?parameter1=parameter1Value&parameter2=parameter2Value&...<br>
 * If you want to parse a variable from the response, include the following information to 
 * the start of the line:<br>
 * #varName:contentName=<br>
 * if you want to parse an attribute, the contentName should start with '@', otherwise the 
 * parsed value will be text data in a single element with the given name. You can use 
 * the parsed variables in the subsequent requests (remember to include the '#')
 * @author Mikko Hilpinen
 * @since 22.2.2015
 */
public class FileReaderClient extends AbstractFileReader
{
	// ATTRIBUTES	-------------------------------------
	
	private Map<String, String> parsedVariables;
	private Client client;
	private boolean failed;
	private ContentType serverContentType;
	
	
	// CONSTRUCTOR	-------------------------------------
	
	/**
	 * Creates a new client
	 * @param userAgent The user agent of the client
	 * @param hostAddress The host address
	 * @param hostPort The host port
	 * @param encodeRequests Should the requests be encoded in UTF-8
	 * @param serverContentType The default content type of the server
	 */
	public FileReaderClient(String userAgent, String hostAddress, int hostPort, 
			boolean encodeRequests, ContentType serverContentType)
	{
		this.parsedVariables = new HashMap<>();
		this.client = new Client(userAgent, hostAddress, hostPort, encodeRequests);
		this.failed = false;
		this.serverContentType = serverContentType;
	}
	
	
	// IMPLEMENTED METHODS	-----------------------------

	@Override
	protected void onLine(String line)
	{
		// I the reader failed at a previous line, it won't try again
		if (this.failed)
			return;
		
		// Parses the collected variables into the request if necessary
		for (String idName : this.parsedVariables.keySet())
		{
			line = line.replaceAll(idName, this.parsedVariables.get(idName));
		}
		
		try
		{
			// If a line starts with '#' an element content or an attribute is parsed from the 
			// response
			if (line.startsWith("#"))
			{
				int varEndsAt = line.indexOf('=');
				
				if (varEndsAt < 0)
				{
					System.err.println("Malformed request");
					return;
				}
				
				String[] varParts = line.substring(0, varEndsAt).split("\\:");
				String varName = varParts[0];
				
				// Both elements and attributes can be parsed
				String searchName = "@id";
				boolean searchAttribute = false;
				if (varParts.length >= 2)
					searchName = varParts[1];
				if (searchName.startsWith("@"))
				{
					searchAttribute = true;
					searchName = searchName.substring(1);
				}
				
				Request request = Request.parseFromString(line.substring(varEndsAt + 1));
				// Attributes can't be parsed from json so xml is requested instead
				if (searchAttribute)
					request.setParameter("contentType", ContentType.XML.toString());
				
				ResponseReplicate response = this.client.sendRequest(request);
				
				if (response.getStatusCode() == HttpStatus.SC_OK)
				{
					try
					{
						String parsedValue = parseVariableFromResponse(response, 
								searchName, searchAttribute, this.serverContentType);
						
						if (parsedValue == null)
							System.err.println("Couldn't find " + searchName + 
									" from the response");
						else
						{
							this.parsedVariables.put(varName, parsedValue);
							System.out.println("Parsed variable: " + varName + " = " + 
									parsedValue);
						}
					}
					catch (XMLStreamException | IOException e)
					{
						System.err.println("Couldn't read " + searchName + 
								" from the response");
						e.printStackTrace();
					}
				}
			}
			else
				this.client.sendRequest(Request.parseFromString(line));
		}
		catch (NoConnectionException e)
		{
			System.err.println("No connection to the server");
			this.failed = true;
		}
		catch (ObjectFormatException e)
		{
			System.err.println("Can't parse request from " + line);
		}
		catch (NoResponseException e)
		{
			System.err.println("No response");
			e.printStackTrace();
		}
	}
	
	@Override
	public void readFile(String fileName, String commentIndicator) throws FileNotFoundException
	{
		super.readFile(fileName, commentIndicator);
		
		// Closes the connections afterwards
		this.parsedVariables = new HashMap<>();
		this.failed = false;
		close();
	}
	
	
	// OTHER METHODS	------------------------------------------

	/**
	 * @return The listenerHandler that will inform the listeners about the http events
	 */
	public HttpEventListenerHandler getListenerHandler()
	{
		return this.client.getListenerHandler();
	}
	
	/**
	 * Closes the connections between the client and the server
	 */
	private void close()
	{
		this.client.closeAllConnections();
	}
	
	private static String parseVariableFromResponse(ResponseReplicate response, 
			String contentName, boolean contentIsAttribute, ContentType contentType) throws 
			XMLStreamException, JsonParseException, IOException
	{
		if (contentIsAttribute || contentType == ContentType.XML)
			return parseVariableFromXmlResponse(response, contentName, contentIsAttribute);
		else
			return parseVariableFromJsonResponse(response, contentName);
	}
	
	private static String parseVariableFromXmlResponse(ResponseReplicate response, 
			String contentName, boolean contentIsAttribute) throws 
			UnsupportedEncodingException, XMLStreamException
	{
		XMLStreamReader reader = null;
		try
		{
			reader = XMLIOAccessor.createReader(new ByteArrayInputStream(
					response.getContent().getBytes()));
			while (reader.hasNext())
			{
				if (reader.isStartElement())
				{
					if (contentIsAttribute)
					{
						for (int i = 0; i < reader.getAttributeCount(); i++)
						{
							if (reader.getAttributeLocalName(i).equalsIgnoreCase(contentName))
								return reader.getAttributeValue(i);
						}
					}
					else if (reader.getLocalName().equalsIgnoreCase(contentName))
						return reader.getElementText();
				}
				
				reader.next();
			}
		}
		finally
		{
			XMLIOAccessor.closeReader(reader);
		}
		
		return null;
	}
	
	@SuppressWarnings("resource")
	private static String parseVariableFromJsonResponse(ResponseReplicate response, 
			String contentName) throws JsonParseException, IOException
	{
		JsonParser reader = null;
		
		try
		{
			reader = JsonIOAccessor.createReader(
					new ByteArrayInputStream(response.getContent().getBytes()));
			JsonToken token = reader.nextToken();
			while (token != null)
			{
				if (token == JsonToken.FIELD_NAME && contentName.equals(reader.getCurrentName()))
				{
					token = reader.nextValue();
					return reader.getValueAsString();
				}
				
				token = reader.nextToken();
			}
		}
		finally
		{
			JsonIOAccessor.closeReader(reader);
		}
		
		return null;
	}
}
