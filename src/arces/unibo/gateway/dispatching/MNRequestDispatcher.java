package arces.unibo.gateway.dispatching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import arces.unibo.SEPA.client.pattern.Aggregator;
import arces.unibo.SEPA.client.pattern.ApplicationProfile;

import arces.unibo.SEPA.commons.SPARQL.ARBindingsResults;
import arces.unibo.SEPA.commons.SPARQL.Bindings;
import arces.unibo.SEPA.commons.SPARQL.BindingsResults;
import arces.unibo.SEPA.commons.SPARQL.RDFTermLiteral;
import arces.unibo.SEPA.commons.SPARQL.RDFTermURI;
import arces.unibo.SEPA.commons.response.ErrorResponse;
import arces.unibo.gateway.mapping.MNRequest;
import arces.unibo.gateway.mapping.ResourceAction;

public class MNRequestDispatcher extends Aggregator {	
	private static final Logger logger = LogManager.getLogger("MNRequestDispatcher");
	
	private HashMap<ResourceAction,ArrayList<MNRequest>> requestMap = new HashMap<ResourceAction,ArrayList<MNRequest>>();
	private MNMap mnMap;
	private MNMappingNotFoundListener listener;
	
	public MNRequestDispatcher(ApplicationProfile appProfile,MNMap mnMap,MNMappingNotFoundListener listener) {
		super(appProfile,"RESOURCE_REQUEST","INSERT_MN_REQUEST");
		this.mnMap = mnMap;
		this.listener = listener;
	}
	
	public String subscribe() {return super.subscribe(null);}

	public void dispatch(MNRequest request){
		Bindings bindings = new Bindings();
		bindings.addBinding("?request", new RDFTermURI("iot:MN-Request_"+UUID.randomUUID().toString()));
		bindings.addBinding("?network", new RDFTermURI(request.getNetwork()));
		bindings.addBinding("?value", new RDFTermLiteral(request.getRequestString()));
		
		update(bindings);
	}

	@Override
	public void notifyAdded(BindingsResults bindingsResults, String spuid, Integer sequence) {
		for(Bindings bindings : bindingsResults.getBindings()){
			String value = bindings.getBindingValue("value");
			String action = bindings.getBindingValue("action");
			String resource = bindings.getBindingValue("resource");
			ResourceAction resourceRequest= new ResourceAction(resource, action, value);
			
			logger.info("<< Resource-Request "+resourceRequest.toString());
			
			//Mapping Resource Request to MN Request List
			ArrayList<MNRequest> mnRequestList = mnMap.resourceAction2MNRequestList(resourceRequest);
			
			if (mnRequestList.isEmpty()) {
				if (listener != null) listener.mappingNotFound(resourceRequest);
				continue;
			}
			
			//Matching request cache
			boolean cacheHit = false;
			for (ResourceAction actionCache : requestMap.keySet()){
				if (actionCache.equals(resourceRequest)){
					requestMap.get(actionCache).addAll(mnRequestList);
					cacheHit = true;
					break;	
				}
			}
			
			//Request not cached: add to cache
			if (!cacheHit) requestMap.put(resourceRequest, mnRequestList);
			
			//Dispatch MN Requests
			for(MNRequest mnRequest : mnRequestList){
				bindings = new Bindings();
				bindings.addBinding("request", new RDFTermURI(mnRequest.getURI()));
				bindings.addBinding("network", new RDFTermURI(mnRequest.getNetwork()));
				bindings.addBinding("value", new RDFTermLiteral(mnRequest.getRequestString()));
				
				logger.info(">> "+mnRequest.toString());
				
				update(bindings);
			}
		}
	}

	@Override
	public void notify(ARBindingsResults notify, String spuid, Integer sequence) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void notifyRemoved(BindingsResults bindingsResults, String spuid, Integer sequence) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSubscribe(BindingsResults bindingsResults, String spuid) {
		notifyAdded(bindingsResults,spuid,0);	
		
	}

	@Override
	public void brokenSubscription() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onError(ErrorResponse errorResponse) {
		// TODO Auto-generated method stub
		
	}
}
