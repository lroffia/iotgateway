package arces.unibo.gateway.garbagecollector;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import arces.unibo.SEPA.client.pattern.Aggregator;
import arces.unibo.SEPA.client.pattern.ApplicationProfile;

import arces.unibo.SEPA.commons.SPARQL.ARBindingsResults;
import arces.unibo.SEPA.commons.SPARQL.Bindings;
import arces.unibo.SEPA.commons.SPARQL.BindingsResults;

public class ResourcePendingRequestRemover extends Aggregator {
	GarbageCollectorListener listener;
	private static final Logger logger = LogManager.getLogger("ResourcePendingRequestRemover");
	boolean monitor;
	
	public ResourcePendingRequestRemover(ApplicationProfile appProfile,boolean monitor,GarbageCollectorListener listener){
		super(appProfile,"RESOURCE_PENDING_REQUEST", "DELETE_RESOURCE_PENDING_REQUEST");
		this.listener = listener;
		this.monitor = monitor;
	}

	@Override
	public void notify(ARBindingsResults notify, String spuid, Integer sequence) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void notifyAdded(BindingsResults bindingsResults, String spuid, Integer sequence) {
		if (monitor) return;
		
		logger.debug( "DELETE RESOURCE PENDING REQUEST "+bindingsResults.toString());
		for (Bindings garbage : bindingsResults.getBindings()) update(garbage);
		
	}

	@Override
	public void notifyRemoved(BindingsResults bindingsResults, String spuid, Integer sequence) {
		if (!monitor || listener == null)  return;
		
		for (Bindings garbage : bindingsResults.getBindings()) 
			listener.removedResourcePendingRequest(
					garbage.getBindingValue("resource"), 
					garbage.getBindingValue("action"), 
					garbage.getBindingValue("value"));	
		
	}

	@Override
	public void onSubscribe(BindingsResults bindingsResults, String spuid) {

	}

	public String subscribe() {
		return super.subscribe(null);
	}

	@Override
	public void brokenSubscription() {
		// TODO Auto-generated method stub
		
	}
}
