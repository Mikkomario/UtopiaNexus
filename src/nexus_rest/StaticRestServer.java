package nexus_rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import nexus_event.HttpEventListener;
import nexus_http.Server;

/**
 * This class hosts a test server that contains rest entities
 * 
 * @author Mikko Hilpinen
 * @since 19.1.2015
 */
public class StaticRestServer
{	
	// CONSTRUCTOR	---------------------------
	
	private StaticRestServer()
	{
		// The constructor is hidden since the interface is static
	}
	
	/**
	 * Starts the test server. Type in 'exit' to quit. The requests should be encoded in UTF-8
	 * @param serverIP The ip of the server
	 * @param port The port number the server uses
	 * @param encode Does the server expect encoded requests
	 * @param defaultContentType Which content type is used by default
	 * @param root The root element of the server
	 * @param listener The listener(s) that will be informed about http events (optional)
	 */
	public static void startServer(String serverIP, int port, boolean encode, 
			ContentType defaultContentType, RestEntity root, HttpEventListener listener)
	{
		// TODO: Create a new thread for this?
		String serverLink = "http://" + serverIP + ":" + port + "/";
		
		Server server = new Server(port);
		RestManager restManager = new RestManager(root, serverLink, encode, defaultContentType);
		if (listener != null)
			restManager.getHttpListenerHandler().add(listener);
		
		server.addRequestHandler(restManager);
		server.addRequestHandler(restManager, restManager.getAdditionalAcceptedPath());
		server.start();
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		System.out.println("Server started. Access through: " + serverLink);
		
		while (true)
		{
			String input;
			try
			{
				input = br.readLine();
				if (input.equalsIgnoreCase("exit"))
				{
					System.out.println("Shutting down server and exiting...");
					break;
				}
				else
					System.out.println("Unknown command " + input);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
		server.end();
		try
		{
			br.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
