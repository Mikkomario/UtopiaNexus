package nexus_http;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpRequest;
import org.apache.http.message.BasicHttpRequest;

/**
 * Requests are events generated from HttpRequests
 * 
 * @author Mikko Hilpinen
 * @since 27.12.2014
 */
public class Request
{
	// ATTRIBUTES	------------------------------
	
	private MethodType method;
	private String[] path;
	private Map<String, String> parameters;
	
	
	// CONSTRUCTOR	------------------------------
	
	/**
	 * Creates a new Request from the given HttpRequest
	 * @param request The request this request is parsed from
	 */
	public Request(HttpRequest request)
	{
		// Initializes attributes
		this.method = 
				MethodType.parseFromString(request.getRequestLine().getMethod().toString());
		
		int parametersStartAt = request.getRequestLine().getUri().indexOf('?');
		
		String pathPart, parameterPart;
		if (parametersStartAt >= 0)
		{
			pathPart = request.getRequestLine().getUri().substring(0, parametersStartAt);
			parameterPart = request.getRequestLine().getUri().substring(parametersStartAt + 1);
		}
		else
		{
			pathPart = request.getRequestLine().getUri();
			parameterPart = new String();
		}
		this.path = pathPart.substring(pathPart.indexOf('/') + 1).split("/");
		//this.path = pathPart.split("/");
		
		this.parameters = new HashMap<>();
		for (String keyValuePair : parameterPart.split("\\&"))
		{
			if (keyValuePair.isEmpty())
				continue;
			
			int keyValueSeparatedAt = keyValuePair.indexOf('=');
			
			if (keyValueSeparatedAt >= 0)
				this.parameters.put(keyValuePair.substring(0, keyValueSeparatedAt), 
						keyValuePair.substring(keyValueSeparatedAt + 1));
			else
				this.parameters.put(keyValuePair, new String());
		}
	}
	
	/**
	 * Creates a new request with the given data
	 * @param method The method that describes this request
	 * @param path The target path of this request
	 * @param parameters The parameters used in this request
	 */
	public Request(MethodType method, String[] path, HashMap<String, String> parameters)
	{
		// Initializes attributes
		this.method = method;
		this.path = path;
		this.parameters = parameters;
	}
	
	/**
	 * Creates a new request that doen't use parameters
	 * @param method The method that describes this request
	 * @param path The target path of this request
	 */
	public Request(MethodType method, String[] path)
	{
		// Initializes attributes
		this.method = method;
		this.path = path;
		this.parameters = new HashMap<>();
	}
	
	
	// IMPLEMENTED METHODS	-----------------------
	
	@Override
	public String toString()
	{
		String uri = new String();
		for (String pathPart : this.path)
		{
			uri += "/" + pathPart;
		}
		uri += createParameterString(this.parameters);
		
		return uri;
	}
	
	
	// GETTERS & SETTERS	-----------------------
	
	/**
	 * @return The method used by this request
	 */
	public MethodType getMethod()
	{
		return this.method;
	}
	
	/**
	 * @return The target path of this request (clone)
	 */
	public String[] getPath()
	{
		return this.path.clone();
	}
	
	/**
	 * @return The parameters given with this request
	 */
	public Map<String, String> getParameters()
	{
		return this.parameters;
	}
	
	
	// OTHER METHODS	-------------------------
	
	/**
	 * @return The names of the parameters used by this request
	 */
	public Set<String> getParameterNames()
	{
		return this.parameters.keySet();
	}
	
	/**
	 * @param parameterName The name of the parameter
	 * @return The value of the parameter in string format or null if no such parameter exists.
	 */
	public String getParameterValue(String parameterName)
	{
		return this.parameters.get(parameterName);
	}
	
	/**
	 * @param parameterName The name of the parameter
	 * @return Does this request contain a parameter with the given name
	 */
	public boolean hasParameter(String parameterName)
	{
		return this.parameters.containsKey(parameterName);
	}
	
	/**
	 * Creates a HttpRequest from this request instance
	 * @return The HttpRequest parsed from this request
	 */
	public HttpRequest toHttpRequest()
	{
		return new BasicHttpRequest(this.method.toString(), this.toString());
	}
	
	private static String createParameterString(Map<String, String> parameterValues)
	{
		String parameterString = "";
		int parameterAmount = parameterValues.size();
		int parametersWritten = 0;
		
		// If there are parameters, they are separated from the uri with '?'
		if (parameterAmount > 0)
			parameterString = "?";
		
		for (String parameterName : parameterValues.keySet())
		{
			parameterString = parameterString.concat(parameterName + "=" + 
					parameterValues.get(parameterName));
			parametersWritten ++;
			// & is added between the parameters
			if (parametersWritten != parameterAmount)
				parameterString = parameterString.concat("&");
		}
		
		return parameterString;
	}
}
