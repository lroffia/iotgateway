package arces.unibo.gateway.dispatching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

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
import arces.unibo.gateway.mapping.MPRequest;
import arces.unibo.gateway.mapping.MPResponse;
import arces.unibo.gateway.mapping.ResourceAction;

public class MPResponseDispatcher extends Aggregator {
	private static final Logger logger = LogManager.getLogger("MPResponseDispatcher");
	private HashMap<ResourceAction,ArrayList<MPRequest>> requestMap;
	private MPMap mpMap;
	
	public MPResponseDispatcher(ApplicationProfile appProfile, MPMap mpMap,HashMap<ResourceAction,ArrayList<MPRequest>> requestMap){
		super(appProfile,"RESOURCE_RESPONSE","INSERT_MP_RESPONSE");
		this.requestMap = requestMap;
		this.mpMap = mpMap;
	}
	
	public String subscribe() {return super.subscribe(null);}

	@Override
	public void notify(ARBindingsResults notify, String spuid, Integer sequence) {
		
	}

	@Override
	public void notifyAdded(BindingsResults bindingsResults, String spuid, Integer sequence) {
		for(Bindings bindings : bindingsResults.getBindings()){

			ResourceAction resource = new ResourceAction(bindings.getBindingValue("resource"), 
					bindings.getBindingValue("action"),  
					bindings.getBindingValue("value"));
			
			logger.info("<< Resource-Response " + resource.toString());
			
			ArrayList<MPRequest> requests = getRequests(resource);
			
			if (requests == null) {
				logger.warn("MP-REQUEST NOT FOUND FOR "+resource.toString());
				return;
			}
			
			for (MPRequest request : requests){					
				//Mapping Resource-Response to MP-Response
				String responseString = mpMap.resourceAction2MPResponseString(request.getProtocol(), resource);
				
				if (responseString == null) continue;
				
				MPResponse response = new MPResponse(request.getProtocol(), responseString);
				
				bindings = new Bindings();
				bindings.addBinding("request", new RDFTermURI(request.getURI()));
				bindings.addBinding("response", new RDFTermURI(response.getURI()));
				bindings.addBinding("value", new RDFTermLiteral(response.getResponseString()));
				bindings.addBinding("protocol", new RDFTermURI(response.getProtocol()));
				
				logger.info(">> " + response.toString());
				
				update(bindings);
			}
			
			removeResourceActionRequest(resource);
		}
	}

	@Override
	public void notifyRemoved(BindingsResults bindingsResults, String spuid, Integer sequence) {
		
	}

	@Override
	public void onSubscribe(BindingsResults bindingsResults, String spuid) {
		notifyAdded(bindingsResults,spuid,0);
	}
	
	private ArrayList<MPRequest> getRequests(ResourceAction action) {
		Iterator<ResourceAction> resources = requestMap.keySet().iterator();
		while(resources.hasNext()) {
			ResourceAction resourceAction = resources.next();
			if (resourceAction.equals(action)) {
				return requestMap.get(resourceAction);
			}
		}
		return null;
	}
	
	private void removeResourceActionRequest(ResourceAction request){
		Iterator<ResourceAction> resources = requestMap.keySet().iterator();
		while(resources.hasNext()) {
			ResourceAction resource = resources.next();
			if (resource.equals(request)) {
				requestMap.remove(resource);
				return;
			}
		}	
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

