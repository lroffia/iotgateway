package arces.unibo.gateway;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import arces.unibo.SEPA.Aggregator;
import arces.unibo.SEPA.BindingLiteralValue;
import arces.unibo.SEPA.BindingURIValue;
import arces.unibo.SEPA.Bindings;
import arces.unibo.SEPA.BindingsResults;
import arces.unibo.SEPA.Consumer;
import arces.unibo.SEPA.Logger;
import arces.unibo.SEPA.Producer;
import arces.unibo.gateway.mapping.ResourceAction;
import arces.unibo.SEPA.Logger.VERBOSITY;

class ResourceManager {
	static String tag ="RESOURCE MANAGER";
	
	ResourcePendingRequestListener requestsListener;
	ResourceResponseListener responsesListener;

	ResourceCache cache = new ResourceCache();
	
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
			super("RESOURCE_PENDING_REQUEST");
			resourceResponse = new Producer("INSERT_RESOURCE_RESPONSE");
			resourceRequest = new Producer("INSERT_RESOURCE_REQUEST"); 
		}

		@Override
		public void notify(BindingsResults notify) {}

		@Override
		public void notifyAdded(ArrayList<Bindings> bindingsResults) {
			
			for (Bindings bindings : bindingsResults){
				ResourceAction action = new ResourceAction(
						bindings.getBindingValue("?resource").getValue(),
						bindings.getBindingValue("?action").getValue(), 
						bindings.getBindingValue("?value").getValue());
				
				Logger.log(VERBOSITY.INFO,tag,"<< Resource-Pending-Request " + action.toString());
				
				//Cache matching
				String value = cache.get(action);
				
				//Response
				if (value != null) {
					Logger.log(VERBOSITY.INFO,tag,">> Resource-Response (HIT) " + action.toString());
					bindings.addBinding("?response", new BindingURIValue("iot:Resource-Response_"+UUID.randomUUID().toString()));
					bindings.addBinding("?value", new BindingLiteralValue(value));
					resourceResponse.update(bindings);	
				}
				else {
					Logger.log(VERBOSITY.INFO,tag,">> Resource-Request (MISS) " + action.toString());
					bindings.addBinding("?request", new BindingURIValue("iot:Resource-Request_"+UUID.randomUUID().toString()));	
					resourceRequest.update(bindings);	
				}
			}
		}

		@Override
		public void notifyRemoved(ArrayList<Bindings> bindingsResults) {}

		@Override
		public void notifyFirst(ArrayList<Bindings> bindingsResults) {
			notifyAdded(bindingsResults);
		}	
	}
	
	class ResourceResponseListener extends Aggregator {		
		public ResourceResponseListener() {
			super("RESOURCE_RESPONSE","UPDATE_RESOURCE");
		}

		@Override
		public void notify(BindingsResults notify) {}

		@Override
		public void notifyAdded(ArrayList<Bindings> bindingsResults) {			
			for (Bindings bindings : bindingsResults){
				ResourceAction action = new ResourceAction(
						bindings.getBindingValue("?resource").getValue(),
						bindings.getBindingValue("?action").getValue(), 
						bindings.getBindingValue("?value").getValue());
				
				Logger.log(VERBOSITY.INFO,tag,"<< Resource-Response " + action.toString());
				
				if(update(bindings)) {
					Logger.log(VERBOSITY.INFO,tag,"UPDATE RESOURCE & ADD TO CACHE " + action.toString());
					cache.put(action);
				}
			}
		}

		@Override
		public void notifyRemoved(ArrayList<Bindings> bindingsResults) {}

		@Override
		public void notifyFirst(ArrayList<Bindings> bindingsResults) {
			notifyAdded(bindingsResults);
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