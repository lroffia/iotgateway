package arces.unibo.gateway.adapters.protocol;

import java.io.IOException;
import java.io.OutputStream;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

import com.sun.net.httpserver.*;

import arces.unibo.gateway.adapters.protocol.HTTPAdapter.IoTHandler.Running;

public class HTTPAdapter extends MPAdapter {
	private static int HTTP_PORT = 8000; 
	private static HttpServer server = null;
	private static HashMap<String,ArrayList<IoTHandler.Running>> iotHandlers = new HashMap<String,ArrayList<IoTHandler.Running>>();
	private static Semaphore handlersMutex = new Semaphore(1,true);
	private static int nRequests = 0;
	
	public HTTPAdapter() {
		super();
	}
	
	public boolean start(){
		System.out.println("****************");
		System.out.println("* HTTP Adapter *");
		System.out.println("****************");
		
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
	    
	    if(!super.join()) return false;
	    if(!super.subscribe()) return false;
	    return true;
	}
	
	public void stop(){
		super.unsubscribe();
		server.stop(0);
	}

	@Override
	public void mpResponse(String request,String value) {
		try {handlersMutex.acquire();} catch (InterruptedException e) {}
		
		if (iotHandlers.containsKey(request)) {
			for (Running handler : iotHandlers.get(request))
				handler.setResponse(value);
		}
		
		handlersMutex.release();
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
				nRequests++;
				System.out.printf(nRequests + " HTTP ADAPTER: MP-Request<%s>\n",request);

				//SEND MP-REQUEST
				String mpRequest = mpRequest(request);
				
				if (mpRequest != null){
					try {handlersMutex.acquire();} catch (InterruptedException e) {}
					
					if (iotHandlers.containsKey(mpRequest)) iotHandlers.get(mpRequest).add(this);
					else {
						ArrayList<Running> handlers = new ArrayList<Running>();
						handlers.add(this);
						iotHandlers.put(mpRequest, handlers);
					}
					
					handlersMutex.release();
					
					try { Thread.sleep(timeout);} 
					catch (InterruptedException e) {}
				}
				else response = "ERROR";
				
				try {handlersMutex.acquire();} catch (InterruptedException e) {}
				
				if(mpRequest != null) iotHandlers.get(mpRequest).remove(this);
				
				handlersMutex.release();
				
				System.out.printf("HTTP ADAPTER: MP-Response<%s>\n", response);
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
			//try {handlersMutex.acquire();} catch (InterruptedException e) {}
			
			new Running(httpExchange).start();
			
			//handlersMutex.release();
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
