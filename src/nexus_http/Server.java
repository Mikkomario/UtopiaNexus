package nexus_http;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.net.ssl.SSLServerSocketFactory;

import org.apache.http.ConnectionClosedException;
import org.apache.http.HttpConnectionFactory;
import org.apache.http.HttpException;
import org.apache.http.HttpServerConnection;
import org.apache.http.impl.DefaultBHttpServerConnection;
import org.apache.http.impl.DefaultBHttpServerConnectionFactory;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpProcessorBuilder;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;
import org.apache.http.protocol.UriHttpRequestHandlerMapper;

/**
 * Server hosts a server at a certain port and gives requests to different handlers
 * 
 * @author Mikko Hilpinen
 * @since 26.12.2014
 */
public class Server
{
	// ATTRIBUTES	--------------------------------
	
	private Thread lastRequestThread;
	private int port;
	private UriHttpRequestHandlerMapper mapper;
	
	
	// CONSTRUCTOR	--------------------------------
	
	/**
	 * Creates a new server that will use the given port
	 * @param port The port number the server responds to
	 */
	public Server(int port)
	{
		// Initializes attributes
		this.lastRequestThread = null;
		this.port = port;
		this.mapper = new UriHttpRequestHandlerMapper();
	}

	
	// OTHER METHODS	----------------------------
	
	/**
	 * Adds a new handler to the request handlers informed about client requests
	 * @param handler The handler that will handle requests in this server
	 */
	public void addRequestHandler(RequestHandler handler)
	{
		this.mapper.register(handler.getAcceptedPath(), handler);
	}
	
	/**
	 * Adds a new handler to the request handlers informed about client requests. This can 
	 * be used along with the {@link #addRequestHandler(RequestHandler)} when a single 
	 * handler can handle multiple paths
	 * @param handler The handler that will handle requests in this server
	 * @param acceptedPath The request path the handler uses
	 */
	public void addRequestHandler(RequestHandler handler, String acceptedPath)
	{
		this.mapper.register(acceptedPath, handler);
	}
	
	/**
	 * The server starts listening to client requests
	 */
	public void start()
	{
		// If the server is already active, doesn't do anything
		if (this.lastRequestThread != null)
			return;
		
		// Sets up the http protocol processor
		HttpProcessor processor = HttpProcessorBuilder.create()
				.add(new ResponseDate())
                .add(new ResponseServer("Test/1.1"))
                .add(new ResponseContent())
                .add(new ResponseConnControl()).build();
		
		// Sets up the HTTP service
        HttpService service = new HttpService(processor, this.mapper);
        
        // Starts listening to the reguests
		try
		{
			this.lastRequestThread = new RequestListenerThread(this.port, service, null);
			this.lastRequestThread.setDaemon(true);
	        this.lastRequestThread.start();
		}
		catch (IOException e)
		{
			System.err.println("Failed to create a RequestListener");
			e.printStackTrace();
		}
	}
	
	/**
	 * The server ends listening to client requests
	 */
	public void end()
	{
		if (this.lastRequestThread == null)
			return;
		
		this.lastRequestThread.interrupt();
		this.lastRequestThread = null;
	}
	
	
	// SUBCLASSES	---------------------------
	
	private static class RequestListenerThread extends Thread
	{
		// ATTRIBUTES	-------------------------------------------------------
		
	    private final ServerSocket serversocket;
	    private final HttpService httpService;

	    
	    // CONSTRUCTOR	-------------------------------------------------------
	    
	    /**
	     * Creates a new RequestListenerThread that will handle the requests coming 
	     * to the given port. The thread uses the given service.
	     * 
	     * @param port The port used to connect to the server
	     * @param httpService The service hosted on the server
	     * @param sf The SocketFactroy that will create the socket to listen (optional)
	     * @throws IOException If the serverSocket couldn't be created
	     */
	    public RequestListenerThread(int port, HttpService httpService, 
	    		SSLServerSocketFactory sf) throws IOException
	    {
	    	// Initializes attributes
	    	if (sf == null)
	    		// TODO: Already in use: JVM_Bind
	    		this.serversocket = new ServerSocket(port);
	    	else
	    		this.serversocket = sf.createServerSocket(port);
	    	
	    	this.httpService = httpService;
	    }

	    
	    // IMPLEMENTED METHODS	----------------------------------------------
	    
	    @Override
	    public void run()
	    {	
	    	HttpConnectionFactory<DefaultBHttpServerConnection> connectionFactory = 
	    			DefaultBHttpServerConnectionFactory.INSTANCE;
	    	
	    	// Starts listening to the port
	    	while (!Thread.interrupted())
	    	{
	    		try
	    		{
	    			// Sets up the HTTP connection
	    			Socket socket = this.serversocket.accept();
	    			HttpServerConnection connection = connectionFactory.createConnection(socket);

	    			// Start worker thread for the client
	    			Thread t = new WorkerThread(this.httpService, connection);
	    			t.setDaemon(true);
	    			t.start();
	    		}
	    		catch (InterruptedIOException ex)
	    		{
	    			// Stops the connection when interupted
	    			break;
	    		}
	    		catch (IOException e)
	    		{
	    			System.err.println("I/O error initialising connection thread: "
	    					+ e.getMessage());
	    			break;
	    		}
	    	}
	    }
	}
	
	private static class WorkerThread extends Thread
    {
    	// ATTRIBUTES	--------------------------------------------------
    	
        private final HttpService httpservice;
        private final HttpServerConnection connection;
        
        
        // CONSTRUCTOR	--------------------------------------------------

        public WorkerThread(final HttpService httpservice, 
        		final HttpServerConnection connection)
        {
            super();
            this.httpservice = httpservice;
            this.connection = connection;
        }
        
        
        // IMPLEMENTED METHODS	------------------------------------------

        @Override
        public void run()
        {
            HttpContext context = new BasicHttpContext(null);
            try
            {
                while (!Thread.interrupted() && this.connection.isOpen())
                {
                    this.httpservice.handleRequest(this.connection, context);
                }
            }
            catch (ConnectionClosedException ex)
            {
            	// Client closed connection, ok
            }
            catch (IOException ex)
            {
                System.err.println("I/O error: " + ex.getMessage());
            }
            catch (HttpException ex)
            {
                System.err.println("Unrecoverable HTTP protocol violation: " + ex.getMessage());
            }
            finally
            {
                try
                {
                    this.connection.shutdown();
                }
                catch (IOException ignore)
                {
                	// Ignores the exception
                }
            }
        }
    }
}
