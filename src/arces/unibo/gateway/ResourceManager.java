package arces.unibo.gateway;

import java.util.ArrayList;
import java.util.UUID;

import arces.unibo.SEPA.Aggregator;
import arces.unibo.SEPA.BindingLiteralValue;
import arces.unibo.SEPA.BindingURIValue;
import arces.unibo.SEPA.BindingValue;
import arces.unibo.SEPA.Bindings;
import arces.unibo.SEPA.BindingsResults;
import arces.unibo.SEPA.Consumer;
import arces.unibo.SEPA.Producer;
import arces.unibo.SEPA.SPARQL;
import arces.unibo.tools.Logging;
import arces.unibo.tools.Logging.VERBOSITY;

class ResourceManager {
	static String tag ="RESOURCE MANAGER";
	
	ResourcePendingRequestListener requestsListener;
	ResourceResponseListener responsesListener;
	
	class ResourcePendingRequestListener extends Consumer {		
		public ResourcePendingRequestListener() {super(SPARQL.subscribe("RESOURCE_PENDING_REQUEST"));}

		@Override
		public void notify(BindingsResults notify) {}

		@Override
		public void notifyAdded(ArrayList<Bindings> bindingsResults) {
			Logging.log(VERBOSITY.DEBUG,tag,"RESOURCE_PENDING_REQUEST NOTIFICATION " + bindingsResults.toString());
			
			for (Bindings bindings : bindingsResults){
				BindingValue action = bindings.getBindingValue("?action");
				BindingValue resourceValue = bindings.getBindingValue("?resourceValue");
				Producer resourceResponse;
				
				if (resourceValue != null && action.getValue().equals("iot:GET")) {
					resourceResponse = new Producer(SPARQL.insert("RESOURCE_RESPONSE"));
					bindings.addBinding("?response", new BindingURIValue("iot:Resource-Response_"+UUID.randomUUID().toString()));
					bindings.addBinding("?value", new BindingLiteralValue(resourceValue.getValue()));
				}
				else {
					resourceResponse = new Producer(SPARQL.insert("RESOURCE_REQUEST"));
					bindings.addBinding("?request", new BindingURIValue("iot:Resource-Request_"+UUID.randomUUID().toString()));	
				}
				
				resourceResponse.join();
				resourceResponse.update(bindings);
				resourceResponse.leave();
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
		public ResourceResponseListener() {super(SPARQL.subscribe("RESOURCE_RESPONSE"),SPARQL.update("RESOURCE"));}

		@Override
		public void notify(BindingsResults notify) {}

		@Override
		public void notifyAdded(ArrayList<Bindings> bindingsResults) {
			Logging.log(VERBOSITY.DEBUG,tag,"RESOURCE_RESPONSE NOTIFICATION " + bindingsResults.toString());
			
			for (Bindings bindings : bindingsResults) update(bindings);
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
		String subID = requestsListener.subscribe(null);
		
		if (subID == null) {
			Logging.log(VERBOSITY.FATAL,tag,"Resource-pending-requests listener subscription FAILED");
			return false;
		}
		
		Logging.log(VERBOSITY.DEBUG,tag,"Resource-pending-requests listener subscription\t"+subID);
		
		responsesListener = new ResourceResponseListener();
		if(!responsesListener.join()) return false;
		subID = responsesListener.subscribe(null);
		
		if (subID == null) {
			Logging.log(VERBOSITY.FATAL,tag,"Resource-responses listener subscription FAILED");
			return false;
		}
		
		Logging.log(VERBOSITY.DEBUG,tag,"Resource-responses listener subscription\t"+subID);
				
		Logging.log(VERBOSITY.INFO,tag,"Started");
		
		return true;
	}
	
	public boolean stop() {
		requestsListener.unsubscribe();
		requestsListener.leave();
		
		responsesListener.unsubscribe();
		responsesListener.leave();
		
		return true;
	}
}

/*
public class ResourceManager {
	private ResourceRequestQueue requestQueue = new ResourceRequestQueue();
	private ResourceResponseCache responseCache = new ResourceResponseCache();
	private ArrayList<String> responseURIs = new ArrayList<String>();
	
	private ResourcePendingRequestListener requestListener = new ResourcePendingRequestListener();
	private ResourceResponseListener responseListener = new ResourceResponseListener();
	private ResourcePendingRequestDispatcher dispatcher = new ResourcePendingRequestDispatcher();
	
	class ResourceRequestQueue {
		private ResourceAction resourceAction = null;
		private boolean newRequest = false;
		
		public synchronized void put(ResourceAction action){
			resourceAction = action;
			newRequest = true;
			notifyAll();
		}
		public synchronized ResourceAction get() {
			while(!newRequest) {
				try {
					wait();
				} catch (InterruptedException e) {
					return null;
				}
			}
			newRequest = false;
			return resourceAction;
		}
	}
	
	class ResourceResponseCache {
		public static final long expiringInterval = 10000;
		
		class ResponseValue {
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
		
		class ActionHash extends HashMap<String,ResponseValue>{
			private static final long serialVersionUID = 2566040138612965806L;
			
		}
		
		class ResourceHash extends HashMap<String,ActionHash> {
			private static final long serialVersionUID = 3014880375121869822L;			
		}
		
		private ResourceHash cache = new ResourceHash();
	
		public synchronized void addEntry(ResourceAction entry){
			String entryResource = entry.getResourceURI();
			String entryAction = entry.getActionURI();
			String entryValue = entry.getValue();
			
			ResponseValue value = new ResponseValue(entryValue);
			
			if (!cache.containsKey(entryResource)) {
				// New cache entry
				ActionHash action = new ActionHash();
				action.put(entryAction, value);
				
				cache.put(entryResource, action);
			}
			else cache.get(entryResource).put(entryAction, value);
		}
		
		public synchronized String getValue(ResourceAction entry){
			if (entry == null) return null;
			String entryResource = entry.getResourceURI();
			String entryAction = entry.getActionURI();
			
			if (!cache.containsKey(entryResource)) return null;
			if (!cache.get(entryResource).containsKey(entryAction)) return null;
			
			ResponseValue value = cache.get(entryResource).get(entryAction);
			if (value.isExpired()) return null;
			return value.getValue();
		}
	}
	
	class ResourcePendingRequestListener extends Consumer {
		
		private static final String RESOURCE_PENDING_REQUEST = "SELECT ?request ?resource ?action ?value WHERE { "
				+ "?request rdf:type iot:Resource-Pending-Request . "
				+ "?request iot:hasValue ?value . "
				+ "?request iot:hasAction ?action . "
				+ "?request iot:hasResource ?resource }";
		
		public ResourcePendingRequestListener() {
			super(RESOURCE_PENDING_REQUEST);
		}

		@Override
		public void notify(BindingsResults notify) {
			
		}

		@Override
		public void notifyAdded(ArrayList<Bindings> bindingsResults) {
			for (Bindings bindings : bindingsResults){
				BindingValue resource = bindings.getBindingValue("?resource");
				BindingValue action = bindings.getBindingValue("?action");
				BindingValue value = bindings.getBindingValue("?value");
				
				ResourceAction resourceAction = new ResourceAction(resource.getValue(), action.getValue(), value.getValue());
				
				//Logging.log("Resource Manager: NEW Resource-Pending-Request "+resourceAction.toString());
				
				requestQueue.put(resourceAction);	
			}
			
		}

		@Override
		public void notifyRemoved(ArrayList<Bindings> bindingsResults) {
			
		}

		@Override
		public void notifyFirst(ArrayList<Bindings> bindingsResults) {
			notifyAdded(bindingsResults);
		}	
	}
	
	class ResourcePendingRequestDispatcher extends Thread {
		ResourceRequestDispatcher requestDispatcher = new ResourceRequestDispatcher();
		ResourceResponseDispatcher responseDispatcher = new ResourceResponseDispatcher();
		
		public boolean init(){
			if (!requestDispatcher.join()) return false;
			if (!responseDispatcher.join()) return false;
			return true;
		}
		
		class ResourceRequestDispatcher extends Producer {
			private static final String RESOURCE_REQUEST = "INSERT DATA { "
					+ "?request rdf:type iot:Resource-Request . "
					+ "?request iot:hasAction ?action . "
					+ "?request iot:hasResource ?resource . "
					+ "?request iot:hasValue ?value }";
			public ResourceRequestDispatcher() {
				super(RESOURCE_REQUEST);
			}
			
			public boolean dispatch(ResourceAction resourceAction) {
				if (resourceAction == null) return false;
				Bindings bindings = new Bindings();
				bindings.addBinding("?request", new BindingURIValue("iot:Resource-Request_"+UUID.randomUUID().toString()));
				bindings.addBinding("?resource", new BindingURIValue(resourceAction.getResourceURI()));
				bindings.addBinding("?action", new BindingURIValue(resourceAction.getActionURI()));
				bindings.addBinding("?value", new BindingLiteralValue(resourceAction.getValue()));
				
				if(update(bindings)) {
					//Logging.log("Resource Manager: DISPATCH Resource-Request "+resourceAction.toString());
					return true;
				}
				return false;
			}
			
		}
		
		class ResourceResponseDispatcher extends Producer {
			private static final String RESOURCE_RESPONSE = "INSERT DATA { "
					+ "?response rdf:type iot:Resource-Response . "
					+ "?response iot:hasAction ?action . "
					+ "?response iot:hasResource ?resource . "
					+ "?response iot:hasValue ?value }";
			
			public ResourceResponseDispatcher() {
				super(RESOURCE_RESPONSE);
			}
			
			public boolean dispatch(ResourceAction resourceAction,String value) {
				Bindings bindings = new Bindings();
				bindings.addBinding("?response", new BindingURIValue("iot:Resource-Response_"+UUID.randomUUID().toString()));
				bindings.addBinding("?resource", new BindingURIValue(resourceAction.getResourceURI()));
				bindings.addBinding("?action", new BindingURIValue(resourceAction.getActionURI()));
				bindings.addBinding("?value", new BindingLiteralValue(value));
				
				responseURIs.add(bindings.getBindingValue("?response").getValue());
				
				if(update(bindings)){
					//Logging.log("Resource Manager: DISPATCH Resource-Response "+bindings.toString());
					return true;
				}
				return false;
			}
			
		}
		
		public void run() {
			while(true){
				//Waiting Resource-Pending Request...
				ResourceAction request = requestQueue.get();
				if (request == null) return;
				
				//Search for response in cache
				String response = responseCache.getValue(request);
				
				//Dispatching
				if (response == null) requestDispatcher.dispatch(request);
				else responseDispatcher.dispatch(request,response);
			}
		}
	}
	
	class ResourceResponseListener extends Consumer {
		private static final String subscribeQuery = "SELECT ?response ?resource ?action ?value WHERE { "
				+ "?response rdf:type iot:Resource-Response . "
				+ "?response iot:hasAction ?action . "
				+ "?response iot:hasResource ?resource . "
				+ "?response iot:hasValue ?value }";
		
		public ResourceResponseListener() {
			super(subscribeQuery);
		}

		@Override
		public void notify(BindingsResults notify) {

		}

		@Override
		public void notifyAdded(ArrayList<Bindings> bindingsResults) {
			for (Bindings bindings : bindingsResults) {
				//Skip Resource-Response generated by the Resource Manager itself
				if(responseURIs.contains(bindings.getBindingValue("?response").getValue())){
					responseURIs.remove(bindings.getBindingValue("?response").getValue());
					continue;
				}
				
				BindingValue resource = bindings.getBindingValue("?resource");
				BindingValue action = bindings.getBindingValue("?action");
				BindingValue value = bindings.getBindingValue("?value");
				
				ResourceAction resourceAction = new ResourceAction(resource.getValue(), action.getValue(), value.getValue());	
			
				//Logging.log("Resource Manager: ADD "+resourceAction.toString()+ " to cache");
				
				responseCache.addEntry(resourceAction);
			}
		}

		@Override
		public void notifyRemoved(ArrayList<Bindings> bindingsResults) {
		}

		@Override
		public void notifyFirst(ArrayList<Bindings> bindingsResults) {
			notifyAdded(bindingsResults);
		}
	}
		
	public boolean start() {
		Logging.log("*****************************");
		Logging.log("*     Resource Manager      *");
		Logging.log("*****************************");
		
		if(!requestListener.join()) return false;
		if(!requestListener.subscribe(null)) return false;
		
		if (!responseListener.join()) return false;
		if (!responseListener.subscribe(null)) return false;
		
		if(!dispatcher.init()) return false;
		dispatcher.start();
		
		return true;
	}
	
	public boolean stop() {
		dispatcher.interrupt();
		
		boolean ret = requestListener.unsubscribe();
		ret = ret && responseListener.unsubscribe();
		
		return ret;
		
	}
}
*/