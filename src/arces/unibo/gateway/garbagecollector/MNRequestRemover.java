package arces.unibo.gateway.garbagecollector;

import arces.unibo.SEPA.application.Aggregator;
import arces.unibo.SEPA.application.ApplicationProfile;
import arces.unibo.SEPA.application.Logger;
import arces.unibo.SEPA.application.Logger.VERBOSITY;
import arces.unibo.SEPA.commons.ARBindingsResults;
import arces.unibo.SEPA.commons.Bindings;
import arces.unibo.SEPA.commons.BindingsResults;

public class MNRequestRemover extends Aggregator {		
	boolean monitor;
	GarbageCollectorListener listener;
	String tag ="MNRequestRemover";
	
	public MNRequestRemover(ApplicationProfile appProfile,boolean monitor,GarbageCollectorListener listener) {
		super(appProfile,"MN_REQUEST", "DELETE_MN_REQUEST");
		this.monitor = monitor;
		this.listener = listener;
	}

	@Override
	public void notify(ARBindingsResults notify, String spuid, Integer sequence)  {}
	
	public String subscribe() {
		return super.subscribe(null);
	}

	@Override
	public void notifyAdded(BindingsResults bindingsResults, String spuid, Integer sequence) {
		if (monitor) return;
		
		Logger.log(VERBOSITY.DEBUG, tag, "DELETE MN REQUEST "+bindingsResults.toString());
		for (Bindings garbage : bindingsResults.getBindings()) update(garbage);
	}

	@Override
	public void notifyRemoved(BindingsResults bindingsResults, String spuid, Integer sequence) {
		if (!monitor || listener == null) return;
			
		for (Bindings garbage : bindingsResults.getBindings())
			listener.removedMNRequest(
					garbage.getBindingValue("network"), 
					garbage.getBindingValue("value"));	
	}

	@Override
	public void onSubscribe(BindingsResults bindingsResults, String spuid) {
		notifyAdded(bindingsResults,spuid,0);			
	}		
}