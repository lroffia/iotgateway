package arces.unibo.gateway.adapters.protocol;

import java.io.IOException;
import java.io.OutputStream;

import java.net.InetSocketAddress;
import java.util.HashMap;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.sun.net.httpserver.*;

import arces.unibo.SEPA.client.pattern.ApplicationProfile;


public class HTTPAdapter extends MPAdapter {
	private static int HTTP_PORT = 8888; 
	private static HttpServer server = null;
	private static HashMap<String,IoTHandler.Running> iotHandlers = new HashMap<String,IoTHandler.Running>();
	private static final Logger logger = LogManager.getLogger("HTTPAdapter");
	
	public HTTPAdapter(ApplicationProfile appProfile) {
		super(appProfile);
	}
	
	public boolean doStart(){		
		try 
		{
			server = HttpServer.create(new InetSocketAddress(HTTP_PORT), 0);
		} 
		catch (IOException e) {
			e.printStackTrace();
			logger.fatal(e.getMessage());
			return false;
		}
		
	    server.createContext("/", new InfoHandler());
	    server.createContext("/iot", new IoTHandler());	    
	    server.setExecutor(null);
	    server.start();
	    
	    logger.info("Started on port "+HTTP_PORT);
	    
	    return true;
	}
	
	public void doStop(){
		if (server == null) return;
		server.stop(0);
	}

	@Override
	public void mpResponse(String requestURI,String responseString) {
		if (iotHandlers.containsKey(requestURI)) 
			iotHandlers.get(requestURI).setResponse(responseString);
	}

	@Override
	public String protocolURI() {
		return "iot:HTTP";
	}
	
	class IoTHandler implements HttpHandler {
		int timeout = 15000;		
		
		class Running extends Thread {
			private HttpExchange httpExchange;
			private String response ="TIMEOUT";
			private String request ="";
			
			public Running(HttpExchange httpExchange) {
				this.httpExchange = httpExchange;
				request = httpExchange.getRequestURI().getQuery();
			}
			
			public void run() {
				//SEND MP-REQUEST
				String requestURI = mpRequest(request);
				
				if (requestURI != null){
					iotHandlers.put(requestURI, this);
					
					try { Thread.sleep(timeout);} 
					catch (InterruptedException e) {}
					
					iotHandlers.remove(requestURI);
				}
				else response = "ERROR";
				
				sendResponse();
			}
			
			public void setResponse(String response){
				this.response = response;
				interrupt();
			}
			
			private void sendResponse() {
				try 
				{
					httpExchange.sendResponseHeaders(200, response.length());
					OutputStream os = httpExchange.getResponseBody();
					os.write(response.getBytes());
					os.close();
				} 
				catch (IOException e) {
					logger.fatal("Send HTTP Response failed");
				}
			}
		}
		
		@Override
		public void handle(HttpExchange httpExchange) throws IOException {
			new Running(httpExchange).start();
		}	
	}
	
	static class InfoHandler implements HttpHandler {
	    public void handle(HttpExchange httpExchange) throws IOException {
	    	String response = "";
	    	response += "<html><body>";
	    	response += "Welcome to the IoT Gateway HTTP Adapter<br>";
	    	response += "</body></html>";
		    try {
				httpExchange.sendResponseHeaders(200, response.length());
				OutputStream os = httpExchange.getResponseBody();
				os.write(response.getBytes());
				os.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
	}

	@Override
	public String adapterName() {
		return "HTTP ADAPTER";
	}
}
