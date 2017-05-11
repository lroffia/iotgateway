package arces.unibo.gateway.garbagecollector;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import arces.unibo.SEPA.client.pattern.Aggregator;
import arces.unibo.SEPA.client.pattern.ApplicationProfile;

import arces.unibo.SEPA.commons.SPARQL.ARBindingsResults;
import arces.unibo.SEPA.commons.SPARQL.Bindings;
import arces.unibo.SEPA.commons.SPARQL.BindingsResults;
import arces.unibo.SEPA.commons.response.ErrorResponse;

public class ResourceResponseRemover extends Aggregator {		
	GarbageCollectorListener listener;
	private static final Logger logger = LogManager.getLogger("ResourceResponseRemover");
	boolean monitor;
	
	public ResourceResponseRemover(ApplicationProfile appProfile,boolean monitor,GarbageCollectorListener listener) {
		super(appProfile,"RESOURCE_RESPONSE", "DELETE_RESOURCE_RESPONSE");
		this.monitor = monitor;
		this.listener = listener;
	}

	@Override
	public void notify(ARBindingsResults notify, String spuid, Integer sequence) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void notifyAdded(BindingsResults bindingsResults, String spuid, Integer sequence) {
		if (monitor) return;
		
		logger.debug("DELETE RESOURCE RESPONSE "+bindingsResults.toString());
		
		for (Bindings garbage : bindingsResults.getBindings()) update(garbage);	
		
	}

	@Override
	public void notifyRemoved(BindingsResults bindingsResults, String spuid, Integer sequence) {
		if (!monitor || listener == null) return;
		
		for (Bindings garbage : bindingsResults.getBindings())
			listener.removedResourceResponse(
					garbage.getBindingValue("resource"), 
					garbage.getBindingValue("action"), 
					garbage.getBindingValue("value"));
		
	}

	@Override
	public void onSubscribe(BindingsResults bindingsResults, String spuid) {
		notifyAdded(bindingsResults,spuid,0);	
	}	
	
	public String subscribe() {
		return super.subscribe(null);
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
