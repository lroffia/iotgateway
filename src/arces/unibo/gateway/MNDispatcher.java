package arces.unibo.gateway;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import arces.unibo.SEPA.Aggregator;
import arces.unibo.SEPA.BindingLiteralValue;
import arces.unibo.SEPA.BindingURIValue;
import arces.unibo.SEPA.Bindings;
import arces.unibo.SEPA.BindingsResults;
import arces.unibo.gateway.mapping.ContextAction;
import arces.unibo.gateway.mapping.MNMapper;
import arces.unibo.gateway.mapping.MNRequest;
import arces.unibo.gateway.mapping.MNResponse;

public class MNDispatcher {
	
	private MNRequestDispatcher mnRequestDispatcher;
	private MNResponseDispatcher mnResponseDispatcher;
	private MNMapper mnMapper;
	private MNMap mnMap;
	private HashMap<ContextAction,ArrayList<MNRequest>> requestMap = new HashMap<ContextAction,ArrayList<MNRequest>>();
	
	public class MNRequestDispatcher extends Aggregator {
		
		private static final String IOT_REQUEST =
				" SELECT ?value ?action ?context WHERE { "
						+ "?request rdf:type iot:IoT-Request . "
						+ "?request iot:hasValue ?value . "
						+ "?request iot:hasAction ?action . "
						+ "?request iot:hasContext ?context"
						+ " }";
		
		private static final String MN_PENDING_REQUEST = 
				" INSERT DATA { "
						+ "?request rdf:type iot:MN-Request . "
						+ "?request iot:hasNetwork ?network . "
						+ "?request iot:hasMNRequestString ?value"
						+ " }";
		
		public MNRequestDispatcher() {
			super(IOT_REQUEST,MN_PENDING_REQUEST);
		}
		
		@Override
		public void notify(BindingsResults notify) {
			if (notify == null) return;
			
			for(Bindings bindings : notify.getAddedBindings()){
				String value = bindings.getBindingValue("?value").getValue();
				String action = bindings.getBindingValue("?action").getValue();
				String context = bindings.getBindingValue("?context").getValue();
				ContextAction contextAction = new ContextAction(context, action, value);
				
				//Mapping IoT Request to MN Request List
				ArrayList<MNRequest> mnRequestList = mnMap.ioT2MNRequestList(contextAction);
				
				if (mnRequestList.isEmpty()) {
					System.out.println("MN DISPATCHER : no mapping found for IoT-Request "+contextAction.toString());
					return;	
				}
				
				//Matching request cache
				boolean cacheHit = false;
				for (ContextAction actionCache : requestMap.keySet()){
					if (actionCache.equals(contextAction)){
						requestMap.get(actionCache).addAll(mnRequestList);
						cacheHit = true;
						break;	
					}
				}
				
				//Request not cached: add to cache
				if (!cacheHit) requestMap.put(contextAction, mnRequestList);
				
				//Dispatch MN Requests
				for(MNRequest request : mnRequestList){
					System.out.println("MN DISPATCHER : "+ contextAction.toString()+" --> "+request.toString());
					
					bindings = new Bindings();
					bindings.addBinding("?request", new BindingURIValue(request.getURI()));
					bindings.addBinding("?network", new BindingURIValue(request.getNetwork()));
					bindings.addBinding("?value", new BindingLiteralValue(request.getRequestString()));
					
					update(bindings);
				}
			}
		}

		public void dispatch(MNRequest request){
			Bindings bindings = new Bindings();
			bindings.addBinding("?request", new BindingURIValue("iot:MN-Request_"+UUID.randomUUID().toString()));
			bindings.addBinding("?network", new BindingURIValue(request.getNetwork()));
			bindings.addBinding("?value", new BindingLiteralValue(request.getRequestString()));
			
			update(bindings);
		}
		
	}
	
	public class MNResponseDispatcher extends Aggregator {
		
		private static final String MN_RESPONSE =
				" SELECT ?network ?value WHERE { "
				+ "?response rdf:type iot:MN-Response . "
				+ "?response iot:hasNetwork ?network . "
				+ "?response iot:hasMNResponseString ?value"
				+ " }";
		
		private static final String IOT_RESPONSE = 
				" INSERT DATA { "
						+ "?response rdf:type iot:IoT-Response . "
						+ "?response iot:hasAction ?action . "
						+ "?response iot:hasContext ?context . "
						+ "?response iot:hasValue ?value"
						+ " }";	
		
		public MNResponseDispatcher() {
			super(MN_RESPONSE,IOT_RESPONSE);
		}

		@Override
		public void notify(BindingsResults notify) {
			for (Bindings bindings : notify.getAddedBindings()){

				MNResponse response = new MNResponse(bindings.getBindingValue("?network").getValue(), bindings.getBindingValue("?value").getValue());
				
				//Mapping MN Response to IoT Response
				ContextAction contextAction = mnMap.mnResponse2IoT(response);
				
				if (contextAction == null) {
					System.out.println("MN DISPATCHER : no mapping found for MN-Response "+response.toString());
					return;
				}
				
				System.out.println("MN DISPATCHER : "+response.toString()+ " --> "+contextAction.toString());
				
				bindings = new Bindings();
				bindings.addBinding("?context", new BindingURIValue(contextAction.getContextURI()));
				bindings.addBinding("?action", new BindingURIValue(contextAction.getActionURI()));
				bindings.addBinding("?value", new BindingLiteralValue(contextAction.getValue()));
				bindings.addBinding("?response", new BindingURIValue("iot:IoT-Response_"+UUID.randomUUID().toString()));

				update(bindings);
			}
		}
	}

	public MNDispatcher() {
		mnMap = new MNMap();
		mnRequestDispatcher = new MNRequestDispatcher();
		mnResponseDispatcher = new MNResponseDispatcher();
		mnMapper = new MNMapper(mnMap);
	}
	
	public boolean start() {
		System.out.println("****************************");
		System.out.println("* Multi-Network Dispatcher *");
		System.out.println("****************************");
		
		if(!mnMapper.join()) return false;
		if(!mnRequestDispatcher.join()) return false;
		if(!mnResponseDispatcher.join()) return false;
		
		BindingsResults ret;
		if (!mnMapper.subscribe()) return false;
		else {
			ret = mnMapper.getQueryResults();
			mnMapper.notify(ret);
		}
		
		if (!mnRequestDispatcher.subscribe()) return false;
		else {
			ret = mnRequestDispatcher.getQueryResults();
			mnRequestDispatcher.notify(ret);
		}
		
		if (!mnResponseDispatcher.subscribe()) return false;
		else {
			ret = mnResponseDispatcher.getQueryResults();
			mnResponseDispatcher.notify(ret);
		}
		
		return true;
	}
	
	public void stop(){
		mnResponseDispatcher.unsubscribe();
		mnRequestDispatcher.unsubscribe();
		mnMapper.unsubscribe();
	}
}
