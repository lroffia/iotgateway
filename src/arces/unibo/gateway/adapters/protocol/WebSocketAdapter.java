package arces.unibo.gateway.adapters.protocol;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.websockets.WebSocket;
import org.glassfish.grizzly.websockets.WebSocketAddOn;
import org.glassfish.grizzly.websockets.WebSocketApplication;
import org.glassfish.grizzly.websockets.WebSocketEngine;

import arces.unibo.SEPA.application.Consumer;
import arces.unibo.SEPA.application.SEPALogger;
import arces.unibo.SEPA.application.ApplicationProfile;
import arces.unibo.SEPA.application.SEPALogger.VERBOSITY;
import arces.unibo.SEPA.commons.SPARQL.ARBindingsResults;
import arces.unibo.SEPA.commons.SPARQL.Bindings;
import arces.unibo.SEPA.commons.SPARQL.BindingsResults;
import arces.unibo.SEPA.commons.SPARQL.RDFTermURI;

public class WebSocketAdapter extends MPAdapter {
	private int PORT = 8989;
	
	private HashMap<String,HashSet<WebSocket>> subscriptions = new HashMap<String,HashSet<WebSocket>>();
	private HashMap<String,ResourceListener> clients = new HashMap<String,ResourceListener>();
	
	private ApplicationProfile appProfile;
	
	public class ResourceListener extends Consumer {
		private String resourceURI;

		public ResourceListener(ApplicationProfile appProfile, String subscribeID) {
			super(appProfile, subscribeID);
			// TODO Auto-generated constructor stub
		}
		
		public String subscribe(String resourceURI) {
			this.resourceURI = resourceURI;
			Bindings forcedBindings = new Bindings();
			forcedBindings.addBinding("resource", new RDFTermURI(resourceURI));
			return super.subscribe(forcedBindings);
		}

		@Override
		public void notify(ARBindingsResults notify, String spuid, Integer sequence) {
			String response = notify.toString();
			for (WebSocket socket : subscriptions.get(resourceURI)) {
				socket.send(response);
			}
		}

		@Override
		public void notifyAdded(BindingsResults bindingsResults, String spuid, Integer sequence) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void notifyRemoved(BindingsResults bindingsResults, String spuid, Integer sequence) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onSubscribe(BindingsResults bindingsResults, String spuid) {
			String response = bindingsResults.toString();
			for (WebSocket socket : subscriptions.get(resourceURI)) {
				socket.send(response);
			}
		}

		@Override
		public void brokenSubscription() {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	public class IoTGatewayWS extends WebSocketApplication {
		@Override
		public void onMessage(WebSocket socket, String text) {
			if (text.startsWith("subscribe=")) {
				if (text.split("=").length != 2) {
					socket.send("Request must be in the form: \"subscribe=<resource URI>\"");
					return;
				}
				String resourceURI = text.split("=")[1];
				if (subscriptions.containsKey(resourceURI)) {
					subscriptions.get(resourceURI).add(socket);
					socket.send("Add to active subscriptions @ resource: "+resourceURI);
				}
				else{
					HashSet<WebSocket> sockets = new HashSet<WebSocket>();
					sockets.add(socket);
					subscriptions.put(resourceURI,sockets);
					ResourceListener client = new ResourceListener(appProfile,"RESOURCE");
					if(!client.join()) {
						socket.send("Client not joined");
						return;
					}
					client.subscribe(resourceURI);
					clients.put(resourceURI, client);
					socket.send("New subscription @ resource: "+resourceURI);
				}
				return;
			}
			if (text.startsWith("unsubscribe=")) {
				if (text.split("=").length != 2) {
					socket.send("Request must be in the form: \"unsubscribe=<resource URI>\"");
					return;
				}
				String resourceURI = text.split("=")[1];
				if (subscriptions.containsKey(resourceURI)) {
					subscriptions.get(resourceURI).remove(socket);
					socket.send("Removed socket");
					if (subscriptions.get(resourceURI).isEmpty()) {
						socket.send("Unsubscribe client");
						clients.get(resourceURI).unsubscribe();
						socket.send("Remove client");
						clients.remove(resourceURI);
						socket.send("Remove subscription");
						subscriptions.remove(resourceURI);
					}
				}
				else{
					socket.send("Active subscription on "+resourceURI+" not found");
				}
				return;
			}
			
			socket.send("Request must be in the form: \"subscribe=<resource URI>\" or \"unsubscribe=<resource URI>\"");
		}
	} 
	
	public WebSocketAdapter(ApplicationProfile appProfile) {
		super(appProfile);
		this.appProfile = appProfile;
	}

	@Override
	public String adapterName() {
		return "WEBSOCKET ADAPTER";
	}

	@Override
	public String protocolURI() {
		return "iot:ws";
	}

	@Override
	protected void mpResponse(String requestURI, String responseString) {
		// TODO Auto-generated method stub
	}

	@Override
	protected boolean doStart() {
		IoTGatewayWS wsService = new IoTGatewayWS();
		
		final HttpServer server = HttpServer.createSimpleServer("/var/www", PORT);

        // Register the WebSockets add on with the HttpServer
        server.getListener("grizzly").registerAddOn(new WebSocketAddOn());

        // register the application
        WebSocketEngine.getEngine().register("", "/subscribe", wsService);

        try {
			server.start();
		} catch (IOException e) {
			SEPALogger.log(VERBOSITY.INFO, adapterName(), "Failed to start WebSocket gate on port "+PORT+ " "+e.getMessage());
			return false;
		}
		
		SEPALogger.log(VERBOSITY.INFO, adapterName(), "Started on port "+PORT);
		return true;
	}

	@Override
	protected void doStop() {
		// TODO Auto-generated method stub
		
	}

}
