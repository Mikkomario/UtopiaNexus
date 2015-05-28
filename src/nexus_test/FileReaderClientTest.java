package nexus_test;

import java.io.FileNotFoundException;

import nexus_http.FileReaderClient;
import nexus_rest.ContentType;

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
	 * (optional, default 7777), contentType (optional, default = xml, xml | json), 
	 * encode requests (optional, default true)
	 */
	public static void main(String[] args)
	{
		if (args.length < 2)
		{
			System.out.println("Please provide the correct arguments: testFileName "
					+ "(data/ automatically included) ip, port (optional, "
					+ "default = 7777),  contentType (optional, default = xml, xml | json), "
					+ "encode (optional, default = true)");
			System.exit(0);
		}
		
		int port = 7777;
		if (args.length >= 3)
			port = Integer.parseInt(args[2]);
		ContentType contentType = ContentType.XML;
		if (args.length >= 4)
			contentType = ContentType.parseFromString(args[3]);
		boolean encode = true;
		if (args.length >= 5)
			encode = Boolean.parseBoolean(args[4]);
		
		run(args[0], args[1], port, contentType, encode);
	}
	
	
	// OTHER METHODS	------------------------------
	
	/**
	 * Runs the test
	 * @param testFileName The file that contains the test instructions 
	 * (data/ automatically included)
	 * @param ip The server ip
	 * @param port The server port
	 * @param contentType The content type used by the server
	 * @param encode Does the server use encoding
	 */
	public static void run(String testFileName, String ip, int port, ContentType contentType, 
			boolean encode)
	{
		FileReaderClient client = new FileReaderClient("Test/1.1", ip, port, encode, 
				contentType);
		// Adds a client analyzer to print the data
		new HttpClientAnalyzer(client.getListenerHandler());
		
		try
		{
			client.readFile(testFileName, "*");
		}
		catch (FileNotFoundException e)
		{
			System.err.println("Couldn't find a file from " + testFileName);
		}
	}
}
