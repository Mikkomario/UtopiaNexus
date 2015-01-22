package nexus_test;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;

import nexus_http.Client;
import nexus_http.Request;

/**
 * TestClient is used for sending requests to servers
 * 
 * @author Mikko Hilpinen
 * @since 21.1.2015
 */
public class TestClient
{
	private TestClient()
	{
		// The constructor is hidden since the interface is static
	}
	
	/**
	 * Starts the test
	 * @param args The first parameter is the host address, the second is the host port
	 */
	public static void main(String[] args)
	{
		String address = "82.130.11.90";
		int port = 7777;
		
		if (args.length > 0)
			address = args[0];
		if (args.length > 1)
			port = Integer.parseInt(args[1]);
		
		// Creates the client
		Client client = new Client("test/1.1", address, port);
		
		// Checks for user input
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		while (true)
		{
			String input;
			try
			{
				input = br.readLine();
				if (input.equalsIgnoreCase("exit"))
					break;
				else
				{
					HttpResponse response = client.sendRequest(Request.parseFromString(input));
					System.out.println("Status: " + response.getStatusLine());
					ByteArrayOutputStream content = new ByteArrayOutputStream();
					response.getEntity().writeTo(content);
					System.out.println("Content: " + content);
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
		// Closes connections
		client.closeAllConnections();
	}
}
