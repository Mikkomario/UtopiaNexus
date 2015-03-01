package nexus_http;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.http.HttpStatus;

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
	
	
	// CONSTRUCTOR	-------------------------------------
	
	/**
	 * Creates a new client
	 * @param userAgent The user agent of the client
	 * @param hostAddress The host address
	 * @param hostPort The host port
	 * @param encodeRequests Should the requests be encoded in UTF-8
	 */
	public FileReaderClient(String userAgent, String hostAddress, int hostPort, 
			boolean encodeRequests)
	{
		this.parsedVariables = new HashMap<>();
		this.client = new Client(userAgent, hostAddress, hostPort, encodeRequests);
		this.failed = false;
	}
	
	
	// IMPLEMENTED METHODS	-----------------------------

	@Override
	protected void onLine(String line)
	{
		// I the reader failed at a previous line, it won't try again
		if (this.failed)
			return;
		
		System.out.println("---------------------------");
		
		// Parses the collected variables into the request if necessary
		for (String idName : this.parsedVariables.keySet())
		{
			line = line.replaceAll(idName, this.parsedVariables.get(idName));
		}
		
		// Prints the request
		System.out.println(line);
		
		try
		{
			ResponseReplicate response = null;
			
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
				
				response = this.client.sendRequest(Request.parseFromString(
						line.substring(varEndsAt + 1)));
				
				if (response.getStatusCode() == HttpStatus.SC_OK)
				{
					try
					{
						String parsedValue = parseVariableFromResponse(response, 
								searchName, searchAttribute);
						
						if (parsedValue == null)
							System.err.println("Couldn't find " + searchName + 
									" from the response");
						else
						{
							this.parsedVariables.put(varName, parsedValue);
							System.out.println("Parsed variable: " + varName + " = " + parsedValue);
						}
					}
					catch (UnsupportedEncodingException | XMLStreamException e)
					{
						System.err.println("Couldn't read " + searchName + 
								" from the response");
						e.printStackTrace();
					}
				}
			}
			else
				response = this.client.sendRequest(Request.parseFromString(line));
			
			// Prints the response
			System.out.println(response.getStatusCode());
			System.out.println(response.getContent());
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
	 * Closes the connections between the client and the server
	 */
	private void close()
	{
		this.client.closeAllConnections();
	}
	
	private static String parseVariableFromResponse(ResponseReplicate response, 
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
}
