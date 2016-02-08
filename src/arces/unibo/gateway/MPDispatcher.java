package arces.unibo.gateway;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

import arces.unibo.SEPA.Aggregator;
import arces.unibo.SEPA.BindingLiteralValue;
import arces.unibo.SEPA.BindingURIValue;
import arces.unibo.SEPA.Bindings;
import arces.unibo.SEPA.BindingsResults;
import arces.unibo.gateway.mapping.ContextAction;
import arces.unibo.gateway.mapping.MPMap;
import arces.unibo.gateway.mapping.MPMapper;
import arces.unibo.gateway.mapping.MPRequest;
import arces.unibo.gateway.mapping.MPResponse;

public class MPDispatcher {
		
	private MPRequestDispatcher mpRequestDispatcher;
	private MPResponseDispatcher mpResponseDispatcher;
	private MPMapper mpMapper;
	private MPMap mpMap;
	private HashMap<ContextAction,ArrayList<MPRequest>> requestMap = new HashMap<ContextAction,ArrayList<MPRequest>>();
	
	class MPRequestDispatcher extends Aggregator {
		private final static String MP_REQUEST =
				" SELECT ?request ?protocol ?value WHERE { "
				+ "?request rdf:type iot:MP-Request . "
				+ "?request iot:hasProtocol ?protocol . "
				+ "?request iot:hasMPRequestString ?value" 
				+ " }";
		
		private final static String IoT_PENDING_REQUEST = 
				" INSERT DATA { "
						+ "?request rdf:type iot:IoT-Request . "
						+ "?request iot:hasValue ?value . "
						+ "?request iot:hasAction ?action . "
						+ "?request iot:hasContext ?context"
						+ " }";		
		
		public MPRequestDispatcher(){
			super(MP_REQUEST,IoT_PENDING_REQUEST);
		}
		
		@Override
		public void notify(BindingsResults results) {
			if (results.getAddedBindings() == null) return;
			
			for(Bindings bindings : results.getAddedBindings()){
				String protocolURI = bindings.getBindingValue("?protocol").getValue();
				String requestString = bindings.getBindingValue("?value").getValue();
				String mpRequest = bindings.getBindingValue("?request").getValue();
				
				//Mapping MP Request to IoT Request
				MPRequest request = new MPRequest(protocolURI, requestString,mpRequest);
				
				ContextAction contextAction = mpMap.mpRequest2IoT(request);
				
				if (contextAction == null){
					System.out.println("MP DISPATCHER : no mapping found for MP-Request "+request.toString());
					continue;
				}
				
				//Dispatch IoT-Request
				System.out.println("MP DISPATCHER : " + request.toString() +" --> " + contextAction.toString());
				
				bindings = new Bindings();
				bindings.addBinding("?request", new BindingURIValue("iot:IoT-Request_"+UUID.randomUUID().toString()));
				bindings.addBinding("?context", new BindingURIValue(contextAction.getContextURI()));
				bindings.addBinding("?action", new BindingURIValue(contextAction.getActionURI()));
				bindings.addBinding("?value", new BindingLiteralValue(contextAction.getValue()));
				
				update(bindings);
				
				//Request cache matching
				for(ContextAction context : requestMap.keySet()){
					if (context.equals(contextAction)) {
						requestMap.get(context).add(request);
						return;
					}
				}

				//Add request to cache
				ArrayList<MPRequest> mpRequestsList = new ArrayList<MPRequest>();
				mpRequestsList.add(request);
				requestMap.put(contextAction, mpRequestsList);
			}
		}
	}
	
	class MPResponseDispatcher extends Aggregator {
		private final static String IoT_RESPONSE =
				" SELECT ?action ?context ?value WHERE { "
						+ "?response rdf:type iot:IoT-Response . "
						+ "?response iot:hasAction ?action . "
						+ "?response iot:hasContext ?context . "
						+ "?response iot:hasValue ?value"
						+ " }";	
		
		private final static String MP_RESPONSE = 
				" INSERT DATA { "
						+ "?request iot:hasMPResponse ?response . "
						+ "?response rdf:type iot:MP-Response . "
						+ "?response iot:hasMPResponseString ?value"
						+ " }";	
		
		public MPResponseDispatcher(){
			super(IoT_RESPONSE,MP_RESPONSE);
		}
		
		@Override
		public void notify(BindingsResults notify) {
			for(Bindings bindings : notify.getAddedBindings()){

				ContextAction context = new ContextAction(bindings.getBindingValue("?context").getValue(), 
						bindings.getBindingValue("?action").getValue(),  
						bindings.getBindingValue("?value").getValue());
				
				ArrayList<MPRequest> requests = getRequests(context);
				
				for (MPRequest request : requests){					
					//Mapping IoT-Response to MP-Response
					String responseString = mpMap.iot2MPResponseString(request.getProtocol(), context);
					
					if (responseString == null) continue;
					
					MPResponse response = new MPResponse(request.getProtocol(), responseString);
					
					System.out.println("MP DISPATCHER : " + context.toString() + " --> " + response.toString());
					
					bindings = new Bindings();
					bindings.addBinding("?request", new BindingURIValue(request.getURI()));
					bindings.addBinding("?response", new BindingURIValue(response.getURI()));
					bindings.addBinding("?value", new BindingLiteralValue(responseString));
					
					update(bindings);
				}
				
				removeIoTRequest(context);
			}
		}
	}
	
	public MPDispatcher() {
		mpMap = new MPMap();
		mpRequestDispatcher = new MPRequestDispatcher();
		mpResponseDispatcher = new MPResponseDispatcher();
		mpMapper = new MPMapper(mpMap);
	}
	
	public boolean start() {
		System.out.println("");
		System.out.println("*****************************");
		System.out.println("* Multi-Protocol Dispatcher *");
		System.out.println("*****************************");
		
		if(!mpMapper.start()) return false;
		if(!mpRequestDispatcher.start()) return false;
		if(!mpResponseDispatcher.start()) return false;
		
		BindingsResults ret = mpMapper.subscribe();
		if (ret == null) return false;
		else mpMapper.notify(ret);
		
		ret = mpRequestDispatcher.subscribe();
		if (ret == null) return false;
		//else mpRequestDispatcher.notify(ret);
		
		ret = mpResponseDispatcher.subscribe();
		if (ret == null) return false;
		//else mpResponseDispatcher.notify(ret);
		
		return true;
	}
	
	
	private ArrayList<MPRequest> getRequests(ContextAction action) {
		Iterator<ContextAction> contexts = requestMap.keySet().iterator();
		while(contexts.hasNext()) {
			ContextAction contextAction = contexts.next();
			if (contextAction.equals(action)) {
				return requestMap.get(contextAction);
			}
		}
		return null;
	}
	
	private void removeIoTRequest(ContextAction request){
		Iterator<ContextAction> contexts = requestMap.keySet().iterator();
		while(contexts.hasNext()) {
			ContextAction contextAction = contexts.next();
			if (contextAction.equals(request)) {
				requestMap.remove(contextAction);
				return;
			}
		}	
	}
}
