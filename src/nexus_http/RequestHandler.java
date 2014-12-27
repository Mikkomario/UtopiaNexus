package nexus_http;

import org.apache.http.protocol.HttpRequestHandler;

/**
 * RequestHandlers are able to handle requests the server receives. Each handler has a certain 
 * path(s) it accepts.
 * 
 * @author Mikko Hilpinen 
 * @since 27.12.2014
 */
public interface RequestHandler extends HttpRequestHandler
{
	/**
	 * The path the handler accepts / handles. Use '*' to indicate that any path is okay. All 
	 * the paths should start with '/'. Possible paths are, for example: '/*', '/resources/*', 
	 * or '/timeLine/05'
	 * @return The path(s) the handler handles
	 */
	public String getAcceptedPath();
}
