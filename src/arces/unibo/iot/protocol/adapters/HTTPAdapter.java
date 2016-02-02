package arces.unibo.iot.protocol.adapters;

import java.io.IOException;
import java.io.OutputStream;

import java.net.InetSocketAddress;
import java.util.HashMap;

import com.sun.net.httpserver.*;

//action=GET&type=<typeURI>&location=<locationURI>
//action=SET&type=<typeURI>&location=<locationURI>&value=<value>

public class HTTPAdapter extends MPAdapter {
	private static int HTTP_PORT = 8000; 
	
	private static HashMap<String,IoTHandler.Running> iotHandlers = new HashMap<String,IoTHandler.Running>();
	
	public HTTPAdapter() {
		super();
	}
	
	@Override
	public boolean start(){
		System.out.println("****************");
		System.out.println("* HTTP Adapter *");
		System.out.println("****************");
		
		HttpServer server = null;
		
		try {
			server = HttpServer.create(new InetSocketAddress(HTTP_PORT), 0);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("ERROR: HTTP server is NOT running...");
			return false;
		}
		
	    server.createContext("/", new InfoHandler());
	    server.createContext("/iot", new IoTHandler());
	    
	    server.setExecutor(null);
			    
	    server.start();
	    
	    System.out.printf("HTTP server is running on port %d...\n",HTTP_PORT);
	    System.out.println("HTTP Request Strings");
	    System.out.println("--------------------------------------------------------------");
	    System.out.println("action=GET&type=<typeURI>&location=<locationURI>");
	    System.out.println("action=SET&type=<typeURI>&location=<locationURI>&value=<value>");
	    System.out.println("--------------------------------------------------------------");
	    System.out.println("HTTP Response Strings");
	    System.out.println("--------------------------------------------------------------");
	    System.out.println("<value>");
	    System.out.println("--------------------------------------------------------------");
	    
	    return super.start();
	}

	
	@Override
	public void mpResponse(String request,String value) {
		if (iotHandlers.containsKey(request)) iotHandlers.get(request).setResponse(value);
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
				
				System.out.printf("> HTTP MP-Request: %s\n",request);

				String mpRequest = mpRequest(request);
				
				if (mpRequest != null){
					iotHandlers.put(mpRequest, this);
					try { Thread.sleep(timeout);} 
					catch (InterruptedException e) {}
				}
				else response = "ERROR";
				
				System.out.printf("< HTTP MP-Response: %s\n", response);
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
					System.out.printf("Send HTTP Response failed...\n");
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
	    	response += "- Use /iot?action=GET&type=[TEMPERATURE|VALVE|...]&location=[ROOM1|BOLOGNA|...]<br>";
	    	response += "- Use /iot?action=SET&type=[TEMPERATURE|VALVE|...]&location=[ROOM1|BOLOGNA|...]&value=[0|100|Hello|...]<br>";
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

}
