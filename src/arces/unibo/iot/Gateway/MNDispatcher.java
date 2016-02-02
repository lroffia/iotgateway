package arces.unibo.iot.Gateway;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Observable;
import java.util.UUID;

import arces.unibo.iot.SEPA.BindingLiteralValue;
import arces.unibo.iot.SEPA.BindingURIValue;
import arces.unibo.iot.SEPA.Bindings;
import arces.unibo.iot.SEPA.BindingsResults;

import arces.unibo.iot.dispatching.Dispatcher;
import arces.unibo.iot.dispatching.MNRequestDispatcher;
import arces.unibo.iot.dispatching.MNResponseDispatcher;

import arces.unibo.iot.mapping.IoTContextAction;
import arces.unibo.iot.mapping.MNMapper;
import arces.unibo.iot.mapping.MNRequest;
import arces.unibo.iot.mapping.MNResponse;


public class MNDispatcher extends Dispatcher {
	private static String IOT_RESPONSE = 
			" INSERT { "
					+ "?response rdf:type iot:IoT-Response . "
					+ "?response iot:hasAction ?action . "
					+ "?response iot:hasContext ?context . "
					+ "?response iot:hasValue ?value"
					+ " }";	
	
	private static String IOT_REQUEST =
			" SELECT ?value ?action ?context WHERE { "
					+ "?request rdf:type iot:IoT-Request . "
					+ "?request iot:hasValue ?value . "
					+ "?request iot:hasAction ?action . "
					+ "?request iot:hasContext ?context"
					+ " }";
	
	public MNDispatcher() {
		super(IOT_REQUEST, IOT_RESPONSE);
	}

	@Override
	public void update(Observable o, Object arg) {
		MNResponse response = (MNResponse) arg;
		
		//Mapping MN Response to IoT Response
		IoTContextAction contextAction = map.mnResponse2IoT(response);
		if (contextAction == null) {
			System.out.println("MN DISPATCHER : no mapping found for MN-Response "+response.toString());
			return;
		}
		
		System.out.println("MN DISPATCHER : "+response.toString()+ " --> "+contextAction.toString());
		
		Bindings bindings = new Bindings();
		bindings.addBinding("?context", new BindingURIValue(contextAction.getContextURI()));
		bindings.addBinding("?action", new BindingURIValue(contextAction.getActionURI()));
		bindings.addBinding("?value", new BindingLiteralValue(contextAction.getValue()));
		bindings.addBinding("?response", new BindingURIValue("iot:IoT-Response_"+UUID.randomUUID().toString()));

		update(bindings);
	}

	@Override
	public void notify(BindingsResults notify) {
		if (notify == null) return;
		if (notify.getAddedBindings() == null) return;
		Iterator<Bindings> results = notify.getAddedBindings().iterator();
		
		while(results.hasNext()){
			//Get the IoT Request from the bindings
			Bindings bindings = results.next();
			String value = bindings.getBindingValue("?value").getValue();
			String action = bindings.getBindingValue("?action").getValue();
			String context = bindings.getBindingValue("?context").getValue();
			IoTContextAction contextAction = new IoTContextAction(context, action, value);
			
			//Mapping IoT Request to MN Request List
			ArrayList<MNRequest> mnRequestList = map.ioT2MNRequestList(contextAction);
			if (mnRequestList.isEmpty()) {
				System.out.println("MN DISPATCHER : no mapping found for IoT-Request "+contextAction.toString());
				return;	
			}
			
			//Matching request cache
			boolean cacheHit = false;
			Iterator<IoTContextAction> contextRequests = requestMap.keySet().iterator();
			while(contextRequests.hasNext()){
				IoTContextAction actionCache = contextRequests.next();
				if (actionCache.equals(contextAction)){
					requestMap.get(actionCache).addAll(mnRequestList);
					cacheHit = true;
					break;	
				}
			}
			
			//Request not cached: add to cache
			if (!cacheHit) requestMap.put(contextAction, mnRequestList);
			
			//Dispatch MN Requests
			Iterator<MNRequest> requestIt = mnRequestList.iterator();
			while (requestIt.hasNext()){
				MNRequest request = requestIt.next();
				System.out.println("MN DISPATCHER : "+ contextAction.toString()+" --> "+request.toString());
				((MNRequestDispatcher)requestDispatcher).dispatch(request);
			}
		}
	}
	
	private HashMap<IoTContextAction,ArrayList<MNRequest>> requestMap = new HashMap<IoTContextAction,ArrayList<MNRequest>>();
	
	public BindingsResults start() {
		System.out.println("****************************");
		System.out.println("* Multi-Network Dispatcher *");
		System.out.println("****************************");
		
		map = new MNMap();
		mapper = new MNMapper(map);
		requestDispatcher = new MNRequestDispatcher();
		responseDispatcher = new MNResponseDispatcher();

		return super.start();
	}
}
