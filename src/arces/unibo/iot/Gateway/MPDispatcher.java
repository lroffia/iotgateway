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
import arces.unibo.iot.dispatching.MPRequestDispatcher;
import arces.unibo.iot.dispatching.MPResponseDispatcher;

import arces.unibo.iot.mapping.IoTContextAction;
import arces.unibo.iot.mapping.MPMapper;
import arces.unibo.iot.mapping.MPRequest;
import arces.unibo.iot.mapping.MPResponse;

public class MPDispatcher extends Dispatcher {
			
	private static String MP_RESPONSE = 
			" INSERT { "
					+ "?request iot:hasMPResponse ?response . "
					+ "?response rdf:type iot:MP-Response . "
					+ "?response iot:hasMPResponseString ?value"
					+ " }";
	
	private static String MP_REQUEST =
			" SELECT ?request ?protocol ?value WHERE { "
			+ "?request rdf:type iot:MP-Request . "
			+ "?request iot:hasProtocol ?protocol . "
			+ "?request iot:hasMPRequestString ?value" 
			+ " }";

	public MPDispatcher() {
		super(MP_REQUEST, MP_RESPONSE);
	}
	
	private  HashMap<IoTContextAction,ArrayList<String[]>> requestMap = new HashMap<IoTContextAction,ArrayList<String[]>>();
	
	public BindingsResults start() {
		System.out.println("");
		System.out.println("*****************************");
		System.out.println("* Multi-Protocol Dispatcher *");
		System.out.println("*****************************");
		
		map = new MPMap();
		mapper = new MPMapper(map);
		requestDispatcher = new MPRequestDispatcher();
		responseDispatcher = new MPResponseDispatcher();

		return super.start();
	}
	
	//New MP-Request
	@Override
	public void notify(BindingsResults results) {
		if (results.getAddedBindings() == null) return;
		
		Iterator<Bindings> bindingsIt = results.getAddedBindings().iterator();
		while(bindingsIt.hasNext()){
			Bindings bindings = bindingsIt.next();
			String protocolURI = bindings.getBindingValue("?protocol").getValue();
			String requestString = bindings.getBindingValue("?value").getValue();
			String mpRequest = bindings.getBindingValue("?request").getValue();
			
			//Mapping MP Request to IoT Request
			MPRequest request = new MPRequest(protocolURI, requestString);
			IoTContextAction contextAction = map.mpRequest2IoT(request);
			if (contextAction == null){
				System.out.println("MP DISPATCHER : no mapping found for MP-Request "+request.toString());
				continue;
			}
			
			//Dispatch IoT-Request
			System.out.println("MP DISPATCHER : " + request.toString() +" --> " + contextAction.toString());
			
			((MPRequestDispatcher)requestDispatcher).dispatch(contextAction);
			
			//Request cache matching
			Iterator<IoTContextAction> iotRequests = requestMap.keySet().iterator();
			while (iotRequests.hasNext()){
				IoTContextAction context = iotRequests.next();
				
				if (context.equals(contextAction)) {
					requestMap.get(context).add(new String[]{mpRequest,protocolURI});
					return;
				}
			}

			//Add request to cache
			ArrayList<String[]> mpRequestsList = new ArrayList<String[]>();
			mpRequestsList.add(new String[]{mpRequest,protocolURI});
			requestMap.put(contextAction, mpRequestsList);
		}
	}

	//New IoT-Response
	@Override
	public void update(Observable o, Object arg) {
	
		IoTContextAction context = (IoTContextAction) arg;
		
		Iterator<String[]> requests = getRequests(context);
		
		if (requests == null) return;
		
		while(requests.hasNext()) {
			String[] request = requests.next();
			String mpRequest = request[0];
			String protocol = request[1];
			String mpResponse = "iot:MP-Response_"+UUID.randomUUID().toString();
			
			//Mapping IoT-Response to MP-Response
			String responseString = map.iot2MPResponseString(protocol, context);
			
			if (responseString == null) continue;
			MPResponse response = new MPResponse(protocol, responseString);
			
			System.out.println("MP DISPATCHER : " + context.toString() + " --> " + response.toString());
			
			Bindings bindings = new Bindings();
			bindings.addBinding("?request", new BindingURIValue(mpRequest));
			bindings.addBinding("?response", new BindingURIValue(mpResponse));
			bindings.addBinding("?value", new BindingLiteralValue(responseString));
			
			update(bindings);
		}
		
		removeIoTRequest(context);
	}
	
	private Iterator<String[]> getRequests(IoTContextAction action) {
		Iterator<IoTContextAction> contexts = requestMap.keySet().iterator();
		while(contexts.hasNext()) {
			IoTContextAction contextAction = contexts.next();
			if (contextAction.equals(action)) {
				return requestMap.get(contextAction).iterator();
			}
		}
		return null;
	}
	
	private void removeIoTRequest(IoTContextAction request){
		Iterator<IoTContextAction> contexts = requestMap.keySet().iterator();
		while(contexts.hasNext()) {
			IoTContextAction contextAction = contexts.next();
			if (contextAction.equals(request)) {
				requestMap.remove(contextAction);
				return;
			}
		}	
	}
}
