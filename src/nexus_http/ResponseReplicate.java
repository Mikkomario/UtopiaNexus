package nexus_http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;

/**
 * Response represents the http response sent by the server. It's a simplified version of 
 * Http response and can be used along with it. The replicate is mostly meant for handling 
 * the requests on client side once the connection has been closed.
 * 
 * @author Mikko Hilpinen
 * @since 27.1.2015
 */
public class ResponseReplicate
{
	// ATTRIBUTES	--------------------------
	
	private int code;
	private String content;
	
	
	// CONSTRUCTOR	--------------------------
	
	/**
	 * Creates a new response from the given response
	 * @param response The response which is replicated
	 */
	public ResponseReplicate(HttpResponse response)
	{
		this.code = response.getStatusLine().getStatusCode();
		try
		{
			ByteArrayOutputStream content = new ByteArrayOutputStream();
			response.getEntity().writeTo(content);
			this.content = content.toString();
		}
		catch (IOException e)
		{
			this.content = "Couldn't read the content";
		}
	}
	
	
	// IMPLEMENTED METHODS	-------------------
	
	@Override
	public String toString()
	{
		return this.code + ": " + this.content;
	}
	
	
	// GETTERS & SETTERS	-------------------
	
	/**
	 * @return The content of the response
	 */
	public String getContent()
	{
		return this.content;
	}
	
	/**
	 * @return The status of the response
	 */
	public int getStatusCode()
	{
		return this.code;
	}
	
	
	// OTHER METHODS	-----------------------
	
	/**
	 * @return is the response status ok
	 */
	public boolean isOK()
	{
		return getStatusCode() == HttpStatus.SC_OK;
	}
}
