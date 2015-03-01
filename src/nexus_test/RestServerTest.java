package nexus_test;

import nexus_rest.RestEntity;
import nexus_rest.StaticRestServer;

/**
 * This class starts a test server. Initially the server only contains a single entity "root"
 * 
 * @author Mikko Hilpinen
 * @since 24.2.2015
 */
public class RestServerTest
{

	private RestServerTest()
	{
		// Interface is static
	}
	
	/**
	 * Starts the server. Type exit to quit.
	 * @param args ip and port
	 */
	public static void main(String[] args)
	{
		if (args.length < 1)
		{
			System.out.println("Please provide the correct parameters: ip and "
					+ "port (optional, default 7777)");
			System.exit(0);
		}
		
		// Creates the server entities
		RestEntity root = new TestRestEntity("root", null);
		
		// Starts the server
		StaticRestServer.setRootEntity(root);
		StaticRestServer.startServer(args);
	}
}
