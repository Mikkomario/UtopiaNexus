package nexus_http;

import java.io.IOException;
import java.net.Socket;
import java.util.Stack;

import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NoHttpResponseException;
import org.apache.http.impl.DefaultBHttpClientConnection;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpProcessorBuilder;
import org.apache.http.protocol.HttpRequestExecutor;
import org.apache.http.protocol.RequestConnControl;
import org.apache.http.protocol.RequestContent;
import org.apache.http.protocol.RequestExpectContinue;
import org.apache.http.protocol.RequestTargetHost;
import org.apache.http.protocol.RequestUserAgent;
import org.apache.http.util.EntityUtils;

/**
 * Client is a simple tool with which one can make requests to a server
 * 
 * @author Mikko Hilpinen
 * @since 31.7.2014
 */
public class Client
{
	// ATTRIBUTES	-------------------------------------------------------
	
	private String userAgent;
	private String hostAddress;
	private int hostPort;
	private Stack<DefaultBHttpClientConnection> openConnections;
	private boolean encode, reuse;
	
	
	// CONSTRUCTOR	-------------------------------------------------------
	
	/**
	 * Creates a new requester that will be able to make request to the given server.
	 * 
	 * @param userAgent The userAgent used in the requests
	 * @param hostAddress The address (or ip) of the hosting server
	 * @param hostPort The port used to connect to the server
	 * @param encodeRequests Should the sent requests be encoded in UTF-8
	 */
	public Client(String userAgent, String hostAddress, int hostPort, boolean encodeRequests)
	{
		// Initializes attributes
		this.userAgent = userAgent;
		this.hostAddress = hostAddress;
		this.hostPort = hostPort;
		this.openConnections = new Stack<>();
		this.encode = encodeRequests;
		this.reuse = false;
	}

	/**
	 * Performs a request to the server. The connection is opened 
	 * automatically if necessary.
	 * @param request The request that will be sent to the server
	 * @return The response given by the host or null if no response could be retrieved
	 * @throws NoConnectionException If the server can't be reached
	 * @throws NoResponseException If the server didn't respond
	 */
	public ResponseReplicate sendRequest(Request request) throws NoConnectionException, 
			NoResponseException
	{
		// Initializes the connection statistics
		HttpProcessor processor = HttpProcessorBuilder.create()
				.add(new RequestContent())
				.add(new RequestTargetHost())
				.add(new RequestConnControl())
				.add(new RequestUserAgent(this.userAgent))
				.add(new RequestExpectContinue(true)).build();
		
		HttpRequestExecutor executor = new HttpRequestExecutor();
		HttpCoreContext coreContext = HttpCoreContext.create();
		HttpHost host = new HttpHost(this.hostAddress, this.hostPort);
		coreContext.setTargetHost(host);

		// Opens a new connection only if the old one isn't being reused
		if (!this.reuse)
			this.openConnections.push(new DefaultBHttpClientConnection(8 * 1024));
		ConnectionReuseStrategy connectionStrategy = DefaultConnectionReuseStrategy.INSTANCE;
		
		// Creates a new connection if necessary
		if (!this.openConnections.peek().isOpen())
		{
			Socket socket;
			try
			{
				socket = new Socket(host.getHostName(), host.getPort());
				this.openConnections.peek().bind(socket);
			}
			catch (IOException e)
			{
				throw new NoConnectionException(e);
			}
		}
		
		// Creates the request
		HttpRequest httpRequest = request.toHttpRequest(this.encode);
		
		try
		{
			// Processes the request & response
			executor.preProcess(httpRequest, processor, coreContext);
			HttpResponse response = executor.execute(httpRequest, this.openConnections.peek(), 
					coreContext);
			
			executor.postProcess(response, processor, coreContext);
			
			ResponseReplicate replicate = new ResponseReplicate(response);
			EntityUtils.consumeQuietly(response.getEntity());
			
			// Closes the connection and quits
			if (!connectionStrategy.keepAlive(response, coreContext))
			{
				closeLatesConnection();
				this.reuse = false;
			}
			// Or continues the connection
			else
				this.reuse = true;
			
			return replicate;
		}
		catch (NoHttpResponseException e)
		{
			throw new NoResponseException(e);
		}
		catch (HttpException e)
		{
			System.err.println("Failed to perform the request " + request);
			e.printStackTrace();
		}
		catch (IOException e)
		{
			System.err.println("An unknown io exception occured during the request " + 
					request);
			e.printStackTrace();
		}
		
		closeLatesConnection();
		return null;
	}
	
	private void closeLatesConnection()
	{
		// Removes the closed connections from top
		while (!this.openConnections.isEmpty() && !this.openConnections.peek().isOpen())
		{
			this.openConnections.pop();
		}
		try
		{
			if (!this.openConnections.isEmpty())
				this.openConnections.pop().close();
		}
		catch (IOException e)
		{
			System.err.println("Failed to close the connection");
			e.printStackTrace();
		}
	}
	
	/**
	 * Closes all currently open connections used by this requester
	 */
	public void closeAllConnections()
	{
		while (!this.openConnections.isEmpty())
		{
			closeLatesConnection();
		}
	}
}
