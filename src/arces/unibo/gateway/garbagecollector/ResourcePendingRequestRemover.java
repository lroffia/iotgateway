package arces.unibo.gateway.garbagecollector;

import arces.unibo.SEPA.application.Aggregator;
import arces.unibo.SEPA.application.ApplicationProfile;
import arces.unibo.SEPA.application.Logger;
import arces.unibo.SEPA.application.Logger.VERBOSITY;
import arces.unibo.SEPA.commons.ARBindingsResults;
import arces.unibo.SEPA.commons.Bindings;
import arces.unibo.SEPA.commons.BindingsResults;

public class ResourcePendingRequestRemover extends Aggregator {
	GarbageCollectorListener listener;
	String tag = "ResourcePendingRequestRemover";
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
		
		Logger.log(VERBOSITY.DEBUG, tag, "DELETE RESOURCE PENDING REQUEST "+bindingsResults.toString());
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
}
