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
import arces.unibo.gateway.mapping.MPRequest;
import arces.unibo.gateway.mapping.ResourceAction;

public class MPRequestDispatcher extends Aggregator {
	private static final Logger logger = LogManager.getLogger("MPRequestDispatcher");
	private MPMap mpMap;
	MPMappingNotFoundListener listener;
	private HashMap<ResourceAction,ArrayList<MPRequest>> requestMap;
	
	public MPRequestDispatcher(ApplicationProfile appProfile,MPMap mpMap,HashMap<ResourceAction,ArrayList<MPRequest>> requestMap,MPMappingNotFoundListener listener){ 
		super(appProfile,"MP_REQUEST","INSERT_RESOURCE_PENDING_REQUEST");
		this.mpMap = mpMap;
		this.listener = listener;
		this.requestMap = requestMap;
	}
	
	public String subscribe() {return super.subscribe(null);}

	@Override
	public void notify(ARBindingsResults notify, String spuid, Integer sequence) {
		
	}

	@Override
	public void notifyAdded(BindingsResults bindingsResults, String spuid, Integer sequence) {
		for(Bindings bindings : bindingsResults.getBindings()){
			String protocolURI = bindings.getBindingValue("protocol");
			String requestString = bindings.getBindingValue("value");
			String mpRequest = bindings.getBindingValue("request");
			
			//Mapping MP-Request to Resource-Pending-Request
			MPRequest request = new MPRequest(protocolURI, requestString,mpRequest);
			
			logger.info("<< " + request.toString());
			
			ResourceAction resourceAction = mpMap.mpRequest2ResourceAction(request);
			
			//MP-Mapping NOT FOUND
			if (resourceAction == null){
				if (listener != null) listener.mappingNotFound(request);
				continue;
			}
			
			//Dispatch Resource-Pending-Request
			bindings = new Bindings();
			bindings.addBinding("request", new RDFTermURI("iot:Resource-Pending-Request_"+UUID.randomUUID().toString()));
			bindings.addBinding("resource", new RDFTermURI(resourceAction.getResourceURI()));
			bindings.addBinding("action", new RDFTermURI(resourceAction.getActionURI()));
			bindings.addBinding("value", new RDFTermLiteral(resourceAction.getValue()));
			
			logger.info(">> Resource-Pending-Request " + resourceAction.toString());
			
			if(update(bindings)) {		
				//MP-Request cache matching
				for(ResourceAction resource : requestMap.keySet()){
					if (resource.equals(resourceAction)) {
						requestMap.get(resource).add(request);
						return;
					}
				}

				//Add request to cache
				ArrayList<MPRequest> mpRequestsList = new ArrayList<MPRequest>();
				mpRequestsList.add(request);
				requestMap.put(resourceAction, mpRequestsList);
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

	@Override
	public void onError(ErrorResponse errorResponse) {
		// TODO Auto-generated method stub
		
	}
}

