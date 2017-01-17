package arces.unibo.gateway.adapters.protocol;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;

import java.util.HashMap;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.EndpointManager;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.core.server.resources.CoapExchange;

import arces.unibo.SEPA.application.Consumer;
import arces.unibo.SEPA.application.Logger;
import arces.unibo.SEPA.application.ApplicationProfile;
import arces.unibo.SEPA.application.Logger.VERBOSITY;
import arces.unibo.SEPA.commons.ARBindingsResults;
import arces.unibo.SEPA.commons.Bindings;
import arces.unibo.SEPA.commons.BindingsResults;
import arces.unibo.gateway.adapters.protocol.COAPAdapter.COAPAdapterServer.GatewayCOAPResource.Running;

public class COAPAdapter extends MPAdapter{
	public COAPAdapter(ApplicationProfile appProfile) {
		super(appProfile);
	}

	private COAPAdapterServer server;
	private static HashMap<String,Running> iotHandlers = new HashMap<String,Running>();
	private COAPResourceListener resourceListener;
	
	public class COAPResourceListener extends Consumer {

		public COAPResourceListener(ApplicationProfile appProfile) {
			super(appProfile, "COAP_RESOURCE");
		}

		@Override
		public void notify(ARBindingsResults notify, String spuid, Integer sequence) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void notifyAdded(BindingsResults bindingsResults, String spuid, Integer sequence) {
			for(Bindings bindings : bindingsResults.getBindings()) {
				server.addCOAPResource(bindings.getBindingValue("resource"));
			}
		}

		@Override
		public void notifyRemoved(BindingsResults bindingsResults, String spuid, Integer sequence) {
			for(Bindings bindings : bindingsResults.getBindings()) {
				server.removeCOAPResource(bindings.getBindingValue("resource"));
			}	
		}

		@Override
		public void onSubscribe(BindingsResults bindingsResults, String spuid) {
			notifyAdded(bindingsResults,spuid,0);
		}
		
	}
	public class COAPAdapterServer extends CoapServer {
		private HashMap<String,GatewayCOAPResource> resources = new HashMap<String,GatewayCOAPResource>();
		
		public final int COAP_PORT = NetworkConfig.getStandard().getInt(NetworkConfig.Keys.COAP_PORT);

	    public void start() {
			addEndpoints();
			super.start();
	    }

	    /**
	     * Add individual endpoints listening on default CoAP port on all IPv4 addresses of all network interfaces.
	     */
	    private void addEndpoints() {
	    	for (InetAddress addr : EndpointManager.getEndpointManager().getNetworkInterfaces()) {
	    		// only binds to IPv4 addresses and localhost
				if (addr instanceof Inet4Address || addr.isLoopbackAddress()) {
					InetSocketAddress bindToAddress = new InetSocketAddress(addr, COAP_PORT);
					addEndpoint(new CoapEndpoint(bindToAddress));
				}
			}
	    }

	    public COAPAdapterServer() throws SocketException {

	    }

	    public void addCOAPResource(String resourceURI) {
	    	if (resources.containsKey(resourceURI)) return;
	    	GatewayCOAPResource resource = new GatewayCOAPResource(resourceURI);
	    	add(resource);
	    	resources.put(resourceURI, resource);
	    }
	    
	    public void removeCOAPResource(String resourceURI) {
	    	if(!resources.containsKey(resourceURI)) return;
	    	remove(resources.get(resourceURI));
	    	resources.remove(resourceURI);
	    }
	    
	    class GatewayCOAPResource extends CoapResource  {
	    	protected String name;
	    	protected CoapExchange exchange;
	    	
	        public GatewayCOAPResource(String name) {
				super(name);
				this.name = name;
			}
	        
	        class Running extends Thread {
	        	private CoapExchange exchange;
	        	private String name;
				private String response = "TIMEOUT";
		        private String mpRequest;
		        
		        public void setResponse(String response){
		        	this.response = response;
		        	interrupt();
		        }
		        
	        	public Running(CoapExchange exchange,String name){
	        		this.exchange = exchange;
	        		this.name = name;
	        	}
	        	
				@Override
				public void run() {
					mpRequest = mpRequest(name);
					
					if (mpRequest != null) {
						iotHandlers.put(mpRequest, this);
					
						try {Thread.sleep(15000);} catch (InterruptedException e) {}
			            
						iotHandlers.remove(mpRequest);
					}
					else response = "ERROR";
			            
			        exchange.respond(response);
				}        	
	        }
	        
	        @Override
	        public void handleGET(CoapExchange exchange) {
	        	Running thread = new Running(exchange,name);
	        	thread.start();
	        }
	    }
	}
	
	@Override
	public String protocolURI() {
		return "iot:COAP";
	}

	@Override
	public void mpResponse(String requestURI, String responseString) {		
		if(iotHandlers.get(requestURI)!= null) iotHandlers.get(requestURI).setResponse(responseString);	
	}

	@Override
	public boolean doStart() {
		//Logging.log(VERBOSITY.INFO,adapterName(),"Starting...");
		
		try 
		{
			server = new COAPAdapterServer();
		} 
		catch (SocketException e) {
			Logger.log(VERBOSITY.FATAL,adapterName(),"FAILED to start "+e.getMessage());
			return false;
		}
		
		server.start();
				
		resourceListener = new COAPResourceListener(appProfile);
		
		if (!resourceListener.join()) {
			Logger.log(VERBOSITY.FATAL,adapterName(),"FAILED to join gateway");
			return false;
		}
		
		String subID = resourceListener.subscribe(null);
		
		if (subID == null) {
			Logger.log(VERBOSITY.FATAL,adapterName(),"FAILED to subscribe");
			return false;
		}
		
		Logger.log(VERBOSITY.DEBUG,adapterName(),"Resource subscription "+subID);
		
		Logger.log(VERBOSITY.INFO,adapterName(),"COAP server is running on port "+ server.COAP_PORT + " and Resource Listener subscribed");
		
		return true;
	}

	@Override
	public void doStop() {
		if (server == null) return;
		server.stop();
	}

	@Override
	public String adapterName() {
		return "COAP ADAPTER           ";
	}
}
