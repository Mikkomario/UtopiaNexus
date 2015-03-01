package nexus_rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import nexus_http.Server;
import nexus_test.TestRestEntity;

/**
 * This class hosts a test server that contains rest entities
 * 
 * @author Mikko Hilpinen
 * @since 19.1.2015
 */
public class StaticRestServer
{
	// ATTRIBUTES	---------------------------
	
	private static RestEntity root = null;
	
	
	// CONSTRUCTOR	---------------------------
	
	private StaticRestServer()
	{
		// The constructor is hidden since the interface is static
	}

	
	// MAIN METHOD	---------------------------
	
	/**
	 * Starts the test server. Type in 'exit' to quit
	 * @param args The first parameter is the server ip. The second parameter is the port number default is 7777)
	 */
	public static void main(String[] args)
	{
		startServer(args);
	}
	
	
	// OTHER METHODS	----------------------------
	
	/**
	 * Sets the root element to the manager before it is created
	 * @param newRoot The root that will be used in the tests
	 */
	public static void setRootEntity(RestEntity newRoot)
	{
		root = newRoot;
	}
	
	/**
	 * Starts the test server. Type in 'exit' to quit. The requests should be encoded in UTF-8
	 * @param args The first parameter is the server ip. The second parameter is the port 
	 * number default is 7777)
	 */
	public static void startServer(String[] args)
	{
		String address = "82.130.11.90";
		int port = 7777;
		
		if (args.length > 0)
			address = args[0];
		if (args.length > 1)
			port = Integer.parseInt(args[1]);
		
		String serverLink = "http://" + address + ":" + port + "/";
		
		Server server = new Server(port);
		if (root == null)
			root = new TestRestEntity("root", null);
		RestManager restManager = new RestManager(root, serverLink, true);
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
