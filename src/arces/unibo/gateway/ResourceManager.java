package arces.unibo.gateway;

import java.util.HashMap;
import java.util.UUID;

import arces.unibo.SEPA.application.Aggregator;
import arces.unibo.SEPA.application.Consumer;
import arces.unibo.SEPA.application.Logger;
import arces.unibo.SEPA.application.Producer;
import arces.unibo.SEPA.commons.ARBindingsResults;
import arces.unibo.SEPA.commons.Bindings;
import arces.unibo.SEPA.commons.BindingsResults;
import arces.unibo.SEPA.commons.RDFTermLiteral;
import arces.unibo.SEPA.commons.RDFTermURI;
import arces.unibo.SEPA.application.ApplicationProfile;
import arces.unibo.SEPA.application.Logger.VERBOSITY;
import arces.unibo.gateway.mapping.ResourceAction;

class ResourceManager {
	static String tag ="RESOURCE MANAGER";
	
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
			// TODO Auto-generated method stub
			
		}

		@Override
		public void notifyAdded(BindingsResults bindingsResults, String spuid, Integer sequence) {
			for (Bindings bindings : bindingsResults.getBindings()){
				ResourceAction action = new ResourceAction(
						bindings.getBindingValue("resource"),
						bindings.getBindingValue("action"), 
						bindings.getBindingValue("value"));
				
				Logger.log(VERBOSITY.INFO,tag,"<< Resource-Pending-Request " + action.toString());
				
				//Cache matching
				String value = cache.get(action);
				
				//Response
				if (value != null) {
					Logger.log(VERBOSITY.INFO,tag,">> Resource-Response (HIT) " + action.toString());
					bindings.addBinding("response", new RDFTermURI("iot:Resource-Response_"+UUID.randomUUID().toString()));
					bindings.addBinding("value", new RDFTermLiteral(value));
					resourceResponse.update(bindings);	
				}
				else {
					Logger.log(VERBOSITY.INFO,tag,">> Resource-Request (MISS) " + action.toString());
					bindings.addBinding("request", new RDFTermLiteral("iot:Resource-Request_"+UUID.randomUUID().toString()));	
					resourceRequest.update(bindings);	
				}
			}
			
		}

		@Override
		public void notifyRemoved(BindingsResults bindingsResults, String spuid, Integer sequence) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onSubscribe(BindingsResults bindingsResults, String spuid) {
			notifyAdded(bindingsResults,spuid,0);
			
		}	
	}
	
	class ResourceResponseListener extends Aggregator {		
		public ResourceResponseListener() {
			super(appProfile,"RESOURCE_RESPONSE","UPDATE_RESOURCE");
		}

		@Override
		public void notify(ARBindingsResults notify, String spuid, Integer sequence) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void notifyAdded(BindingsResults bindingsResults, String spuid, Integer sequence) {
			for (Bindings bindings : bindingsResults.getBindings()){
				ResourceAction action = new ResourceAction(
						bindings.getBindingValue("resource"),
						bindings.getBindingValue("action"), 
						bindings.getBindingValue("value"));
				
				Logger.log(VERBOSITY.INFO,tag,"<< Resource-Response " + action.toString());
				
				if(update(bindings)) {
					Logger.log(VERBOSITY.INFO,tag,"UPDATE RESOURCE & ADD TO CACHE " + action.toString());
					cache.put(action);
				}
			}
		}

		@Override
		public void notifyRemoved(BindingsResults bindingsResults, String spuid, Integer sequence) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onSubscribe(BindingsResults bindingsResults, String spuid) {
			notifyAdded(bindingsResults,spuid,0);
			
		}
	}
	
	public boolean start() {
		requestsListener = new ResourcePendingRequestListener();
		if(!requestsListener.join()) return false;
		if (requestsListener.subscribe(null) == null) {
			Logger.log(VERBOSITY.FATAL,tag,"Resource-pending-requests listener subscription FAILED");
			return false;
		}
		
		responsesListener = new ResourceResponseListener();
		if(!responsesListener.join()) return false;		
		if (responsesListener.subscribe(null) == null) {
			Logger.log(VERBOSITY.FATAL,tag,"Resource-responses listener subscription FAILED");
			return false;
		}
		

		
		Logger.log(VERBOSITY.INFO,tag,"Started");
		
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