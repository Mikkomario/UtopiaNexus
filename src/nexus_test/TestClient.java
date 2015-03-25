package nexus_test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import flow_recording.ObjectFormatException;
import nexus_http.Client;
import nexus_http.NoConnectionException;
import nexus_http.NoResponseException;
import nexus_http.Request;
import nexus_http.ResponseReplicate;

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
		String address = "192.168.38.101";
		int port = 7777;
		
		if (args.length > 0)
			address = args[0];
		if (args.length > 1)
			port = Integer.parseInt(args[1]);
		
		// Creates the client
		Client client = new Client("test/1.1", address, port, true);
		
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
					ResponseReplicate response = client.sendRequest(Request.parseFromString(input));
					System.out.println("Status: " + response.getStatusCode());
					System.out.println("Content: " + response.getContent());
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			catch (ObjectFormatException e)
			{
				System.out.println(e.getMessage());
			}
			catch (NoConnectionException e)
			{
				System.out.println("No connection to the server. Exiting.");
				break;
			}
			catch (NoResponseException e)
			{
				System.out.println("No response from the server");
			}
		}
		
		// Closes connections
		client.closeAllConnections();
	}
}
