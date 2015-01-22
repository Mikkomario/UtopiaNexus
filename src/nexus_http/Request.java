package nexus_http;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpRequest;
import org.apache.http.message.BasicHttpRequest;

import flow_recording.ObjectFormatException;

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
		
		initializePathAndParameters(request.getRequestLine().getUri());
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
	
	private Request(MethodType method, String uriAndParameters)
	{
		this.method = method;
		initializePathAndParameters(uriAndParameters);
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
	
	/**
	 * Parses a new request from a string
	 * @param s The string that can be parsed into a request the string should contain a 
	 * method part followed by a whitespace and the uri and parameters. 
	 * For example "GET server/1?parameter1=a&parameter2=b"
	 * @return A request parsed from the string
	 * @throws ObjectFormatException If the request couldn't be parsed from the given string
	 */
	public static Request parseFromString(String s) throws ObjectFormatException
	{
		String[] methodAndBody = s.split(" ");
		
		MethodType method = MethodType.parseFromString(methodAndBody[0]);
		
		if (method == null || methodAndBody.length < 2)
			throw new ObjectFormatException("No method provided");
		
		return new Request(method, methodAndBody[1]);
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
	
	private void initializePathAndParameters(String uriAndParameters)
	{
		// Initializes attributes
		int parametersStartAt = uriAndParameters.indexOf('?');
		
		String pathPart, parameterPart;
		if (parametersStartAt >= 0)
		{
			pathPart = uriAndParameters.substring(0, parametersStartAt);
			parameterPart = uriAndParameters.substring(parametersStartAt + 1);
		}
		else
		{
			pathPart = uriAndParameters;
			parameterPart = new String();
		}
		this.path = pathPart.substring(pathPart.indexOf('/') + 1).split("/");
		
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
}
