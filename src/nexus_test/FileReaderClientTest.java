package nexus_test;

import java.io.FileNotFoundException;

import nexus_http.FileReaderClient;

/**
 * This class uses the fileReaderTestClient to connect a server
 * @author Mikko Hilpinen
 * @since 24.2.2015
 */
public class FileReaderClientTest
{
	// CONSTRUCTOR	-----------------------------
	
	private FileReaderClientTest()
	{
		// The interface is static
	}
	
	
	// MAIN METHOD	------------------------------
	
	/**
	 * Starts the test
	 * @param args testFileName ('data/' automatically included), address (ip), port 
	 * (optional, default 7777), encode requests (optional, default true)
	 */
	public static void main(String[] args)
	{
		if (args.length < 2)
		{
			System.out.println("Please provide the correct arguments: testFileName "
					+ "(data/ automatically included) ip, port (optional, "
					+ "default = 7777), encode (optional, default = true)");
			System.exit(0);
		}
		
		int port = 7777;
		if (args.length >= 3)
			port = Integer.parseInt(args[2]);
		boolean encode = true;
		if (args.length >= 4)
			encode = Boolean.parseBoolean(args[3]);
		
		FileReaderClient client = new FileReaderClient("Test/1.1", args[1], port, 
				encode);
		try
		{
			client.readFile(args[0], "*");
		}
		catch (FileNotFoundException e)
		{
			System.err.println("Couldn't find a file from " + args[0]);
		}
	}
}
