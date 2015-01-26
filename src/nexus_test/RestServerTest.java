package nexus_test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import nexus_http.Server;
import nexus_rest.RestManager;

/**
 * This class hosts a test server that contains rest entities
 * 
 * @author Mikko Hilpinen
 * @since 19.1.2015
 */
public class RestServerTest
{
	// CONSTRUCTOR	---------------------------
	
	private RestServerTest()
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
		String address = "10.100.39.173";
		int port = 7777;
		
		if (args.length > 0)
			address = args[0];
		if (args.length > 1)
			port = Integer.parseInt(args[1]);
		
		String serverLink = "http://" + address + ":" + port + "/";
		
		Server server = new Server(port);
		RestManager restManager = new RestManager(new TestRestEntity("root", null), 
				serverLink, true);
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
