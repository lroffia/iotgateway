package arces.unibo.gateway.resources;

import java.util.HashMap;
import java.util.UUID;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import arces.unibo.SEPA.client.pattern.Aggregator;
import arces.unibo.SEPA.client.pattern.ApplicationProfile;
import arces.unibo.SEPA.client.pattern.Consumer;
import arces.unibo.SEPA.client.pattern.Producer;

import arces.unibo.SEPA.commons.SPARQL.ARBindingsResults;
import arces.unibo.SEPA.commons.SPARQL.Bindings;
import arces.unibo.SEPA.commons.SPARQL.BindingsResults;
import arces.unibo.SEPA.commons.SPARQL.RDFTermLiteral;
import arces.unibo.SEPA.commons.SPARQL.RDFTermURI;
import arces.unibo.gateway.mapping.ResourceAction;

public class ResourceManager {
	private final Logger logger = LogManager.getLogger("ResourceManager");
	ResourcePendingRequestListener requestsListener;
	ResourceResponseListener responsesListener;

	ResourceCache cache = new ResourceCache();
	
	private ApplicationProfile appProfile;
	
	public ResourceManager(ApplicationProfile appProfile){
		this.appProfile = appProfile;
	}
	
	class ResourceCache {
		private long expiringInterval = 10000;
		
		private class ResponseValue {
			long timestamp;
			String value;
			
			public ResponseValue(String value) {
				this.value = value;
				this.timestamp = System.currentTimeMillis();
			}
			
			public boolean isExpired(){
				return System.currentTimeMillis() > timestamp + expiringInterval;
			}
			
			public String getValue() {
				return value;
			}
		}
		
		private class ResourceHash extends HashMap<String,ResponseValue> {
			private static final long serialVersionUID = 3014880375121869822L;			
		}
		
		private ResourceHash cache = new ResourceHash();
	
		public synchronized void put(ResourceAction entry){
			cache.put(entry.getResourceURI(), new ResponseValue(entry.getValue()));
		}
		
		public synchronized String get(ResourceAction entry){
			if (entry == null) return null;
			if (entry.getActionURI().equals("iot:SET")) return null;
			if (!cache.containsKey(entry.getResourceURI())) return null;
			if (cache.get(entry.getResourceURI()).isExpired()) return null;
			
			return cache.get(entry.getResourceURI()).getValue();
		}
	}
	
	class ResourcePendingRequestListener extends Consumer {	
		private final Logger logger = LogManager.getLogger("ResourcePendingRequestListener");
		
		Producer resourceResponse;
		Producer resourceRequest;
		
		public boolean join() {
			if (!resourceResponse.join()) return false;
			if (!resourceRequest.join()) return false;
			return super.join();
		}
		
		public boolean leave() {
			return (resourceResponse.leave() &&
					resourceRequest.leave() && super.leave());
		}
		
		public ResourcePendingRequestListener() {
			super(appProfile,"RESOURCE_PENDING_REQUEST");
			resourceResponse = new Producer(appProfile,"INSERT_RESOURCE_RESPONSE");
			resourceRequest = new Producer(appProfile,"INSERT_RESOURCE_REQUEST"); 
		}

		@Override
		public void notify(ARBindingsResults notify, String spuid, Integer sequence) {

		}

		@Override
		public void notifyAdded(BindingsResults bindingsResults, String spuid, Integer sequence) {
			for (Bindings bindings : bindingsResults.getBindings()){
				ResourceAction action = new ResourceAction(
						bindings.getBindingValue("resource"),
						bindings.getBindingValue("action"), 
						bindings.getBindingValue("value"));
				
				logger.info("<< Resource-Pending-Request " + action.toString());
				
				//Cache matching
				String value = cache.get(action);
				
				//Response
				if (value != null) {
					logger.info(">> Resource-Response (HIT) " + action.toString());
					bindings.addBinding("response", new RDFTermURI("iot:Resource-Response_"+UUID.randomUUID().toString()));
					bindings.addBinding("value", new RDFTermLiteral(value));
					resourceResponse.update(bindings);	
				}
				else {
					logger.info(">> Resource-Request (MISS) " + action.toString());
					bindings.addBinding("request", new RDFTermURI("iot:Resource-Request_"+UUID.randomUUID().toString()));	
					resourceRequest.update(bindings);	
				}
			}
			
		}

		@Override
		public void notifyRemoved(BindingsResults bindingsResults, String spuid, Integer sequence) {

		}

		@Override
		public void onSubscribe(BindingsResults bindingsResults, String spuid) {
			notifyAdded(bindingsResults,spuid,0);
			
		}

		@Override
		public void brokenSubscription() {
			// TODO Auto-generated method stub
			
		}	
	}
	
	class ResourceResponseListener extends Aggregator {	
		private final Logger logger = LogManager.getLogger("ResourceResponseListener");
		
		public ResourceResponseListener() {
			super(appProfile,"RESOURCE_RESPONSE","UPDATE_RESOURCE");
		}

		@Override
		public void notify(ARBindingsResults notify, String spuid, Integer sequence) {

		}

		@Override
		public void notifyAdded(BindingsResults bindingsResults, String spuid, Integer sequence) {
			for (Bindings bindings : bindingsResults.getBindings()){
				ResourceAction action = new ResourceAction(
						bindings.getBindingValue("resource"),
						bindings.getBindingValue("action"), 
						bindings.getBindingValue("value"));
				
				logger.info("<< Resource-Response " + action.toString());
				
				if(update(bindings)) {
					logger.info("UPDATE RESOURCE & ADD TO CACHE " + action.toString());
					cache.put(action);
				}
			}
		}

		@Override
		public void notifyRemoved(BindingsResults bindingsResults, String spuid, Integer sequence) {

		}

		@Override
		public void onSubscribe(BindingsResults bindingsResults, String spuid) {
			notifyAdded(bindingsResults,spuid,0);
		}

		@Override
		public void brokenSubscription() {
			// TODO Auto-generated method stub
			
		}
	}
	
	public boolean start() {
		requestsListener = new ResourcePendingRequestListener();
		if(!requestsListener.join()) return false;
		if (requestsListener.subscribe(null) == null) {
			logger.fatal("Resource-pending-requests listener subscription FAILED");
			return false;
		}
		
		responsesListener = new ResourceResponseListener();
		if(!responsesListener.join()) return false;		
		if (responsesListener.subscribe(null) == null) {
			logger.fatal("Resource-responses listener subscription FAILED");
			return false;
		}
			
		logger.info("Started");
		
		return true;
	}
	
	public boolean stop() {
		if (requestsListener == null || responsesListener == null) return false;
		requestsListener.unsubscribe();
		requestsListener.leave();
		
		responsesListener.unsubscribe();
		responsesListener.leave();
		
		return true;
	}
}