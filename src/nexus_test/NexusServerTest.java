package nexus_test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Random;

import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;

import nexus_http.HttpException;
import nexus_http.InternalServerException;
import nexus_http.InvalidParametersException;
import nexus_http.MethodNotSupportedException;
import nexus_http.MethodType;
import nexus_http.NotFoundException;
import nexus_http.Request;
import nexus_http.RequestHandler;
import nexus_http.Server;

/**
 * This class tests the basic server functionalities of the nexus module
 * 
 * @author Mikko Hilpinen
 * @since 29.12.2014
 */
public class NexusServerTest
{
	// CONSTRUCTOR	--------------------------
	
	private NexusServerTest()
	{
		// The constructor is hidden since the interface is static
	}

	
	// MAIN METHOD	--------------------------
	
	/**
	 * Starts the test server. Type in 'exit' to quit
	 * @param args Not used
	 */
	public static void main(String[] args)
	{
		Server server = new Server(7777);
		server.addRequestHandler(new TestRequestHandler());
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
	
	
	// SUBCLASSES	--------------------------
	
	private static class TestRequestHandler implements RequestHandler
	{
		// IMPLEMENTED METHODS	------------------
		
		@Override
		public void handle(HttpRequest request, HttpResponse response, HttpContext context)
				throws org.apache.http.HttpException, IOException
		{
			// Sends the parsed request as the response
			Request parsedRequest = new Request(request, false);
			response.setStatusCode(HttpStatus.SC_OK);
			HttpEntity body = new StringEntity(parsedRequest.toString());
			response.setEntity(body);
			
			printRequestData(parsedRequest);
			
			sendRandomResponse(response);
		}

		@Override
		public String getAcceptedPath()
		{
			return "/*";
		}
		
		
		// OTHER METHODS	--------------------
		
		private static void printRequestData(Request request)
		{
			System.out.println("Method: " + request.getMethod());
			System.out.println("Path:");
			for (String pathPart : request.getPath())
			{
				System.out.println("-> " + pathPart);
			}
			System.out.println("Parameters:");
			for (String parameterName : request.getParameterNames())
			{
				System.out.println("-> " + parameterName);
			}
		}
		
		private static void sendRandomResponse(HttpResponse response)
		{
			Random random = new Random();
			
			if (random.nextDouble() < 0.3)
			{
				response.setStatusCode(HttpStatus.SC_OK);
				try
				{
					response.setEntity(new StringEntity("OK"));
				}
				catch (UnsupportedEncodingException e)
				{
					System.err.println("Can't encode");
					e.printStackTrace();
				}
			}
			else
			{
				
				HttpException[] exceptions = {
						new InvalidParametersException("Ivalid parameters"), 
						new InternalServerException("server error"), 
						new MethodNotSupportedException(MethodType.GET), 
						new NotFoundException("asdasd")};
				
				HttpException e = exceptions[random.nextInt(exceptions.length)];
				response.setStatusCode(e.getStatusCode());
				response.setEntity(new StringEntity(e.getMessage(), ContentType.TEXT_PLAIN));
			}
		}
	}
}
