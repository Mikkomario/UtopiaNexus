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
	 * @param args Not used
	 */
	public static void main(String[] args)
	{
		Server server = new Server(7777);
		server.addRequestHandler(new RestManager(new TestRestEntity("root", null)));
		server.start();
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
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
