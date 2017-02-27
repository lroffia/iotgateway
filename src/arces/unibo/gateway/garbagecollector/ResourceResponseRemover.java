package arces.unibo.gateway.garbagecollector;

import arces.unibo.SEPA.application.Aggregator;
import arces.unibo.SEPA.application.ApplicationProfile;
import arces.unibo.SEPA.application.SEPALogger;
import arces.unibo.SEPA.application.SEPALogger.VERBOSITY;
import arces.unibo.SEPA.commons.SPARQL.ARBindingsResults;
import arces.unibo.SEPA.commons.SPARQL.Bindings;
import arces.unibo.SEPA.commons.SPARQL.BindingsResults;

public class ResourceResponseRemover extends Aggregator {		
	GarbageCollectorListener listener;
	String tag = "ResourceResponseRemover";
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
		
		SEPALogger.log(VERBOSITY.INFO, tag, "DELETE RESOURCE RESPONSE "+bindingsResults.toString());
		
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
}
